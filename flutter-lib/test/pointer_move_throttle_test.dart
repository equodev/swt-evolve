// Moves are sent at most one per throttle window, and the last one of a window still has to reach
// Java: a pointer that stops inside one would otherwise leave Display.getCursorLocation() a step
// behind it. A single-line Text is where it showed, entering it builds the field in its place.

import 'package:flutter/gestures.dart';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/comm/comm_frame.dart' show EquoCommBase;
import 'package:swtflutter/src/gen/display.dart';
import 'package:swtflutter/src/gen/event.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/gen/text.dart';
import 'package:swtflutter/src/impl/control_evolve.dart' show ControlImpl;
import 'package:swtflutter/src/impl/display_evolve.dart';
import 'package:swtflutter/src/impl/utils/pending_move.dart';

VText _field() => VText()
  ..swt = 'Text'
  ..id = 4
  ..style = SWT.SINGLE | SWT.BORDER
  ..text = 'hello'
  ..enabled = true
  ..visible = true
  ..bounds = (VRectangle()
    ..x = 0
    ..y = 0
    ..width = 260
    ..height = 30);

/// Pumps the field, [left] pixels from the view's left edge, and returns every pointer event it
/// sends from then on, as (channel, event).
Future<List<(String, VEvent?)>> _pumpField(WidgetTester tester, {ValueNotifier<double>? left}) async {
  final offset = left ?? ValueNotifier<double>(0);
  final sent = <(String, VEvent?)>[];
  EquoCommBase.sendObserverForTesting = (action, payload) {
    if (action.contains('/Mouse')) sent.add((action.substring('Text/4/'.length), payload as VEvent?));
  };
  addTearDown(() {
    EquoCommBase.sendObserverForTesting = null;
    PendingMove.reset();
  });
  await tester.pumpWidget(EvolveApp(
    theme: ThemeMode.light,
    contentWidget: Align(
      alignment: Alignment.topLeft,
      child: ValueListenableBuilder<double>(
        valueListenable: offset,
        builder: (_, dx, child) => Padding(padding: EdgeInsets.only(left: dx), child: child),
        child: SizedBox(width: 400, height: 200, child: TextSwt<VText>(value: _field())),
      ),
    ),
  ));
  await tester.pumpAndSettle();
  return sent;
}

(int?, int?) _lastMove(List<(String, VEvent?)> sent) {
  final moves = sent.where((s) => s.$1 == 'MouseMove/MouseMove').toList();
  expect(moves, isNotEmpty);
  return (moves.last.$2?.x, moves.last.$2?.y);
}

void main() {
  testWidgets('a move on the frame after entering a field at rest is reported', (tester) async {
    final sent = await _pumpField(tester);
    final pointer = TestPointer(1, PointerDeviceKind.mouse);
    await tester.sendEventToBinding(pointer.hover(const Offset(127, 12)));
    await tester.pump();
    await tester.sendEventToBinding(pointer.hover(const Offset(130, 15)));
    await tester.pump(const Duration(milliseconds: 20));

    expect(_lastMove(sent), (130, 15));
    await tester.sendEventToBinding(pointer.removePointer());
  });

  testWidgets('a move inside the throttle window is sent when the window ends', (tester) async {
    final sent = await _pumpField(tester);
    final pointer = TestPointer(1, PointerDeviceKind.mouse);
    await tester.sendEventToBinding(pointer.hover(const Offset(127, 12)));
    await tester.sendEventToBinding(pointer.hover(const Offset(130, 15)));
    await tester.pump(const Duration(milliseconds: 20));

    expect(_lastMove(sent), (130, 15));
    await tester.sendEventToBinding(pointer.removePointer());
  });

  testWidgets('a held move goes out before the press that follows it', (tester) async {
    final sent = await _pumpField(tester);
    final pointer = TestPointer(1, PointerDeviceKind.mouse);
    await tester.sendEventToBinding(pointer.hover(const Offset(127, 12)));
    await tester.sendEventToBinding(pointer.hover(const Offset(130, 15)));
    await tester.sendEventToBinding(pointer.down(const Offset(130, 15)));
    await tester.sendEventToBinding(pointer.up());
    await tester.pump(const Duration(milliseconds: 20));

    final order = sent.map((s) => s.$1).toList();
    expect(order.indexOf('Mouse/MouseDown'), greaterThan(order.lastIndexOf('MouseMove/MouseMove')),
        reason: 'the move happened before the press');
    expect(_lastMove(sent), (130, 15));
    await tester.sendEventToBinding(pointer.removePointer());
  });

  testWidgets('every drag move is sent, however close together they arrive', (tester) async {
    final sent = await _pumpField(tester);
    final pointer = TestPointer(1, PointerDeviceKind.mouse);
    await tester.sendEventToBinding(pointer.hover(const Offset(20, 15)));
    await tester.pump(const Duration(milliseconds: 20));
    await tester.sendEventToBinding(pointer.down(const Offset(20, 15)));
    await tester.pump(const Duration(milliseconds: 20));
    await tester.sendEventToBinding(pointer.move(const Offset(50, 15)));
    await tester.sendEventToBinding(pointer.move(const Offset(80, 15)));
    await tester.sendEventToBinding(pointer.move(const Offset(110, 15)));
    await tester.pump(const Duration(milliseconds: 20));

    final drags = sent.where((s) => s.$1 == 'MouseMove/MouseMove' && s.$2?.stateMask != 0).map((s) => s.$2?.x);
    expect(drags, [50, 80, 110]);
    await tester.sendEventToBinding(pointer.up());
    await tester.pump(const Duration(milliseconds: 20));
    await tester.sendEventToBinding(pointer.removePointer());
  });

  // Keys reach Java on the Display's channel, not through the control, and must not overtake a move
  // made before them either.
  testWidgets('a held move goes out before a key typed after it', (tester) async {
    final sent = <String>[];
    EquoCommBase.sendObserverForTesting = (action, payload) {
      if (action.contains('/Mouse') || action.contains('/Key/')) sent.add(action);
    };
    addTearDown(() {
      EquoCommBase.sendObserverForTesting = null;
      PendingMove.reset();
    });
    await tester.pumpWidget(EvolveApp(
      theme: ThemeMode.light,
      contentWidget: Stack(children: [
        Positioned.fill(child: DisplaySwt(value: VDisplay()..id = 5)),
        Align(
          alignment: Alignment.topLeft,
          child: SizedBox(width: 400, height: 200, child: TextSwt<VText>(value: _field())),
        ),
      ]),
    ));
    await tester.pumpAndSettle();
    final pointer = TestPointer(1, PointerDeviceKind.mouse);
    await tester.sendEventToBinding(pointer.hover(const Offset(127, 12)));
    await tester.sendEventToBinding(pointer.hover(const Offset(130, 15)));
    await tester.sendKeyEvent(LogicalKeyboardKey.f5);
    await tester.pump(const Duration(milliseconds: 20));

    expect(sent.indexOf('Display/5/Key/KeyDown'), greaterThan(sent.lastIndexOf('Text/4/MouseMove/MouseMove')),
        reason: 'the move happened before the key');
    await tester.sendEventToBinding(pointer.removePointer());
  });

  // A drag's last move is where the selection or the sash ends; one dropped leaves it short of the
  // pointer, on the screen and in Java, until the next move.
  testWidgets('a drag move inside the throttle window is sent when the window ends', (tester) async {
    final sent = await _pumpField(tester);
    final pointer = TestPointer(1, PointerDeviceKind.mouse);
    await tester.sendEventToBinding(pointer.hover(const Offset(20, 12)));
    await tester.sendEventToBinding(pointer.down(const Offset(20, 12)));
    await tester.pump(const Duration(milliseconds: 20));
    await tester.sendEventToBinding(pointer.move(const Offset(60, 12)));
    await tester.sendEventToBinding(pointer.move(const Offset(90, 14)));
    await tester.pump(const Duration(milliseconds: 20));

    expect(_lastMove(sent), (90, 14));
    await tester.sendEventToBinding(pointer.up());
    await tester.pump(const Duration(milliseconds: 20));
    await tester.sendEventToBinding(pointer.removePointer());
  });

  testWidgets('a held drag move goes out before the release', (tester) async {
    final sent = await _pumpField(tester);
    final pointer = TestPointer(1, PointerDeviceKind.mouse);
    await tester.sendEventToBinding(pointer.hover(const Offset(20, 12)));
    await tester.sendEventToBinding(pointer.down(const Offset(20, 12)));
    await tester.pump(const Duration(milliseconds: 20));
    await tester.sendEventToBinding(pointer.move(const Offset(60, 12)));
    await tester.sendEventToBinding(pointer.move(const Offset(90, 14)));
    await tester.sendEventToBinding(pointer.up());
    await tester.pump(const Duration(milliseconds: 20));

    final order = sent.map((s) => s.$1).toList();
    expect(order.indexOf('Mouse/MouseUp'), greaterThan(order.lastIndexOf('MouseMove/MouseMove')),
        reason: 'the move happened before the release');
    expect(_lastMove(sent), (90, 14));
    await tester.sendEventToBinding(pointer.removePointer());
  });

  // A move the browser reports ahead of its frame goes to Java at once, so Java's answer can be
  // drawn in that frame; the same move delivered with the frame is not reported a second time.
  testWidgets('a move reported ahead of its frame is sent, and not again with the frame', (tester) async {
    final sent = await _pumpField(tester);
    final pointer = TestPointer(1, PointerDeviceKind.mouse);
    await tester.sendEventToBinding(pointer.hover(const Offset(20, 12)));
    await tester.sendEventToBinding(pointer.down(const Offset(20, 12)));
    await tester.pump(const Duration(milliseconds: 20));
    await tester.sendEventToBinding(pointer.move(const Offset(40, 12)));
    await tester.pump(const Duration(milliseconds: 20));
    final before = sent.length;

    ControlImpl.rawMove(70, 14, kPrimaryButton);
    // Inside the throttle window of the move before it, so it goes when the window ends.
    await tester.pump(const Duration(milliseconds: 20));

    expect(sent.length, before + 1);
    expect(_lastMove(sent), (70, 14));
    await tester.sendEventToBinding(pointer.move(const Offset(70, 14)));
    await tester.pump(const Duration(milliseconds: 20));
    expect(sent.length, before + 1, reason: 'the frame delivered the same move');

    await tester.sendEventToBinding(pointer.up());
    await tester.pump(const Duration(milliseconds: 20));
    final released = sent.length;
    ControlImpl.rawMove(90, 14, kPrimaryButton);
    await tester.pump(const Duration(milliseconds: 20));
    expect(sent.length, released, reason: 'nothing is held any more');
    await tester.sendEventToBinding(pointer.removePointer());
  });

  // A pane resized from its own edge moves under the held pointer: the move ahead of the frame is
  // where the pointer is on the control now, the same place the frame's move then reports.
  testWidgets('a move reported ahead of its frame is relative to where the control moved', (tester) async {
    final left = ValueNotifier<double>(0);
    final sent = await _pumpField(tester, left: left);
    final pointer = TestPointer(1, PointerDeviceKind.mouse);
    await tester.sendEventToBinding(pointer.hover(const Offset(20, 12)));
    await tester.sendEventToBinding(pointer.down(const Offset(20, 12)));
    await tester.pump(const Duration(milliseconds: 20));
    await tester.sendEventToBinding(pointer.move(const Offset(40, 12)));
    await tester.pump(const Duration(milliseconds: 20));
    left.value = 30;
    await tester.pump(const Duration(milliseconds: 20));
    final before = sent.length;

    ControlImpl.rawMove(70, 14, kPrimaryButton);
    await tester.pump(const Duration(milliseconds: 20));

    expect(sent.length, before + 1);
    expect(_lastMove(sent), (40, 14));
    await tester.sendEventToBinding(pointer.move(const Offset(70, 14)));
    await tester.pump(const Duration(milliseconds: 20));
    expect(sent.length, before + 1, reason: 'the frame delivered the same move');

    await tester.sendEventToBinding(pointer.up());
    await tester.pump(const Duration(milliseconds: 20));
    await tester.sendEventToBinding(pointer.removePointer());
  });

  testWidgets('a move reported ahead of any drag is not sent', (tester) async {
    final sent = await _pumpField(tester);
    ControlImpl.rawMove(70, 14, kPrimaryButton);
    expect(sent.where((s) => s.$1 == 'MouseMove/MouseMove'), isEmpty);
  });
}
