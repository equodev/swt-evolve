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

/// Pumps the field and returns every pointer event it sends from then on, as (channel, event).
Future<List<(String, VEvent?)>> _pumpField(WidgetTester tester) async {
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
      child: SizedBox(width: 400, height: 200, child: TextSwt<VText>(value: _field())),
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
}
