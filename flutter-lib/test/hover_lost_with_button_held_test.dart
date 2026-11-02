// A control the pointer leaves while a button is held reports no MouseHover afterwards: natively the
// hover is only reported where the pointer rests, and its MouseExit waits for the release.

import 'package:flutter/gestures.dart';
import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/gen/canvas.dart';
import 'package:swtflutter/src/gen/composite.dart';
import 'package:swtflutter/src/gen/event.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/gen/widgets.dart' show wireChannelNames;
import 'package:swtflutter/src/impl/control_evolve.dart';

VRectangle _rect(int x, int y, int w, int h) => VRectangle()
  ..x = x
  ..y = y
  ..width = w
  ..height = h;

class _RecordingCompositeSwt extends CompositeSwt<VComposite> {
  const _RecordingCompositeSwt({required super.value, required this.sent});

  final List<(String, VEvent?)> sent;

  @override
  void sendEvent(VComposite val, int channel, VEvent? payload) => sent.add((wireChannelNames[channel]!, payload));
}

Future<void> _pumpCompositeWithCanvas(WidgetTester tester, List<(String, VEvent?)> sent) async {
  await tester.pumpWidget(EvolveApp(
    theme: ThemeMode.light,
    contentWidget: Align(
      alignment: Alignment.topLeft,
      child: SizedBox(
        width: 400,
        height: 200,
        child: _RecordingCompositeSwt(
          sent: sent,
          value: VComposite()
            ..swt = 'Composite'
            ..id = 1
            ..style = SWT.NONE
            ..enabled = true
            ..visible = true
            ..bounds = _rect(0, 0, 400, 200)
            ..children = [
              VCanvas()
                ..swt = 'Canvas'
                ..id = 2
                ..style = SWT.NONE
                ..enabled = true
                ..visible = true
                ..bounds = _rect(200, 0, 200, 200),
            ],
        ),
      ),
    ),
  ));
  await tester.pumpAndSettle();
}

Iterable<(String, VEvent?)> _hovers(List<(String, VEvent?)> sent) =>
    sent.where((s) => s.$1.contains('MouseHover'));

void main() {
  final hold = Duration(milliseconds: ControlImpl.hoverDelayMs + 50);

  Iterable<(String, VEvent?)> exits(List<(String, VEvent?)> sent) =>
      sent.where((s) => s.$1.contains('MouseExit'));

  testWidgets('a parent left for its child while a button is held reports no hover',
      (tester) async {
    final sent = <(String, VEvent?)>[];
    await _pumpCompositeWithCanvas(tester, sent);

    final pointer = TestPointer(1, PointerDeviceKind.mouse);
    await tester.sendEventToBinding(pointer.hover(const Offset(50, 100)));
    await tester.pump();
    expect(sent.where((s) => s.$1.contains('MouseEnter')), hasLength(1));

    await tester.sendEventToBinding(pointer.down(const Offset(50, 100)));
    await tester.sendEventToBinding(pointer.move(const Offset(300, 120)));
    await tester.pump(hold);

    expect(_hovers(sent), isEmpty,
        reason: 'the Composite was entered at (50, 100), and the pointer has left it since');
    expect(exits(sent), isEmpty, reason: 'a held button defers the MouseExit to the release');

    await tester.sendEventToBinding(pointer.up());
    await tester.pump(hold);
    expect(_hovers(sent), isEmpty);
    expect(exits(sent), hasLength(1), reason: 'the release settles the hover on the Canvas');
  });

  testWidgets('a held drag that stays inside the parent reports no hover', (tester) async {
    final sent = <(String, VEvent?)>[];
    await _pumpCompositeWithCanvas(tester, sent);

    final pointer = TestPointer(1, PointerDeviceKind.mouse);
    await tester.sendEventToBinding(pointer.hover(const Offset(50, 100)));
    await tester.pump();
    await tester.sendEventToBinding(pointer.down(const Offset(50, 100)));
    await tester.sendEventToBinding(pointer.move(const Offset(120, 100)));
    await tester.pump(hold);

    expect(_hovers(sent), isEmpty,
        reason: 'the pointer moved from (50, 100) with the button held: it never rested there');

    await tester.sendEventToBinding(pointer.up());
    await tester.pump();
    expect(_hovers(sent), isEmpty);
  });

  testWidgets('a parent dragged back onto before the release reports no exit', (tester) async {
    final sent = <(String, VEvent?)>[];
    await _pumpCompositeWithCanvas(tester, sent);

    final pointer = TestPointer(1, PointerDeviceKind.mouse);
    await tester.sendEventToBinding(pointer.hover(const Offset(50, 100)));
    await tester.pump();
    await tester.sendEventToBinding(pointer.down(const Offset(50, 100)));
    await tester.sendEventToBinding(pointer.move(const Offset(300, 120)));
    await tester.pump();
    await tester.sendEventToBinding(pointer.move(const Offset(60, 100)));
    await tester.pump();
    await tester.sendEventToBinding(pointer.up());
    await tester.pump(hold);

    expect(exits(sent), isEmpty);
    expect(sent.where((s) => s.$1.contains('MouseEnter')), hasLength(1));
  });

  testWidgets('a parent the pointer rests on after a release reports one hover there', (tester) async {
    final sent = <(String, VEvent?)>[];
    await _pumpCompositeWithCanvas(tester, sent);

    final pointer = TestPointer(1, PointerDeviceKind.mouse);
    await tester.sendEventToBinding(pointer.hover(const Offset(50, 100)));
    await tester.pump();
    await tester.sendEventToBinding(pointer.down(const Offset(50, 100)));
    await tester.sendEventToBinding(pointer.move(const Offset(300, 120)));
    await tester.pump();
    await tester.sendEventToBinding(pointer.move(const Offset(60, 100)));
    await tester.pump();
    await tester.sendEventToBinding(pointer.up());
    await tester.pump(hold * 2);

    final hovers = _hovers(sent).toList();
    expect(hovers, hasLength(1), reason: 'the pointer rests at (60, 100) once the button is released');
    expect((hovers.single.$2!.x, hovers.single.$2!.y), (60, 100));
  });

  testWidgets('a parent hovered on to a new point counts its hover from there', (tester) async {
    final sent = <(String, VEvent?)>[];
    await _pumpCompositeWithCanvas(tester, sent);

    final pointer = TestPointer(1, PointerDeviceKind.mouse);
    await tester.sendEventToBinding(pointer.hover(const Offset(50, 100)));
    await tester.pump(hold ~/ 2);
    await tester.sendEventToBinding(pointer.hover(const Offset(70, 100)));
    await tester.pump(hold);

    final hovers = _hovers(sent).toList();
    expect(hovers, hasLength(1), reason: 'the pointer only rested at (70, 100)');
    expect((hovers.single.$2!.x, hovers.single.$2!.y), (70, 100));
  });
}
