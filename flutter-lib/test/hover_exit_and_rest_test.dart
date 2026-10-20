// Where a crossing lands, and when a resting pointer hovers: natively a control that loses the hover
// to a child reports its MouseExit at the pointer, and a pointer that enters and stays still gets a
// MouseHover without moving again, even when the control came in under it.

import 'package:flutter/gestures.dart';
import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/gen/canvas.dart';
import 'package:swtflutter/src/gen/composite.dart';
import 'package:swtflutter/src/gen/event.dart';
import 'package:swtflutter/src/gen/label.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/swt.dart';
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
  void sendEvent(VComposite val, String ev, VEvent? payload) => sent.add((ev, payload));
}

class _RecordingLabelSwt extends LabelSwt<VLabel> {
  const _RecordingLabelSwt({required super.value, required this.sent});

  final List<(String, VEvent?)> sent;

  @override
  void sendEvent(VLabel val, String ev, VEvent? payload) => sent.add((ev, payload));
}

Future<void> _pump(WidgetTester tester, Widget control) async {
  await tester.pumpWidget(EvolveApp(
    theme: ThemeMode.light,
    contentWidget: Align(
      alignment: Alignment.topLeft,
      child: SizedBox(width: 400, height: 200, child: control),
    ),
  ));
  await tester.pumpAndSettle();
}

void main() {
  testWidgets('a parent that loses the hover to its child reports the exit at the pointer',
      (tester) async {
    final sent = <(String, VEvent?)>[];
    await _pump(
      tester,
      _RecordingCompositeSwt(
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
    );

    final pointer = TestPointer(1, PointerDeviceKind.mouse);
    await tester.sendEventToBinding(pointer.hover(const Offset(50, 100)));
    await tester.pump();
    await tester.sendEventToBinding(pointer.hover(const Offset(150, 100)));
    await tester.pump();
    await tester.sendEventToBinding(pointer.hover(const Offset(300, 120)));
    await tester.pump();

    final exits = sent.where((s) => s.$1.contains('MouseExit')).toList();
    expect(exits, hasLength(1));
    expect((exits.single.$2?.x, exits.single.$2?.y), (300, 120),
        reason: 'the Composite was crossed only on entry, at (50, 100); its exit is where the '
            'pointer is when the Canvas takes the hover');
  });

  testWidgets('a control that comes in under a resting pointer is hovered', (tester) async {
    final sent = <(String, VEvent?)>[];
    await _pump(tester, const SizedBox.expand());
    final pointer = TestPointer(1, PointerDeviceKind.mouse);
    await tester.sendEventToBinding(pointer.hover(const Offset(60, 40)));
    await tester.pump();

    // The pointer does not move again: the Label is entered by the layout that puts it under the
    // pointer, with no hover event of its own.
    await _pump(
      tester,
      _RecordingLabelSwt(
        sent: sent,
        value: VLabel()
          ..swt = 'Label'
          ..id = 3
          ..style = SWT.NONE
          ..text = 'label'
          ..enabled = true
          ..visible = true
          ..bounds = _rect(0, 0, 400, 200),
      ),
    );

    await tester.pump(Duration(milliseconds: ControlImpl.hoverDelayMs + 50));

    final hovers = sent.where((s) => s.$1.contains('MouseHover')).toList();
    expect(hovers, hasLength(1), reason: 'the stillness that makes a MouseHover due starts at the enter');
    expect((hovers.single.$2?.x, hovers.single.$2?.y), (60, 40));
  });
}
