// SWT reports MouseMove and MouseUp relative to where the control is when they happen, also for a
// pane that moves under the held pointer while it is resized from its own edge.

import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:swtflutter/src/gen/widgets.dart' show wireChannelNames;

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/gen/composite.dart';
import 'package:swtflutter/src/gen/event.dart';
import 'package:swtflutter/src/gen/label.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/swt.dart';

class _CapturingCompositeSwt extends CompositeSwt<VComposite> {
  const _CapturingCompositeSwt({required super.value, required this.onEvent});

  final void Function(String ev, VEvent? payload) onEvent;

  @override
  void sendEvent(VComposite val, int channel, VEvent? payload) => onEvent(wireChannelNames[channel]!, payload);
}

VRectangle _rect(int x, int y, int w, int h) => VRectangle()
  ..x = x
  ..y = y
  ..width = w
  ..height = h;

VComposite _pane({required bool withChild}) => VComposite()
  ..swt = 'Composite'
  ..id = 1
  ..style = SWT.NONE
  ..enabled = true
  ..visible = true
  ..bounds = _rect(0, 0, 200, 200)
  ..children = withChild
      ? [
          VLabel()
            ..swt = 'Label'
            ..id = 2
            ..style = SWT.NONE
            ..visible = true
            ..text = 'view'
            ..bounds = _rect(100, 150, 80, 20),
        ]
      : null;

/// Presses at x = 1 inside a pane that starts at x = 200, then moves the pane left by 10 for every
/// 10 px the pointer moves left, as a listener resizing the pane from its edge does. Returns the
/// x of each MouseMove and of the MouseUp.
Future<({List<int> moves, int? up})> _dragEdge(WidgetTester tester, {required bool withChild}) async {
  final left = ValueNotifier<double>(200);
  final moves = <int>[];
  int? up;
  await tester.pumpWidget(EvolveApp(
    theme: ThemeMode.light,
    contentWidget: Align(
      alignment: Alignment.topLeft,
      child: ValueListenableBuilder<double>(
        valueListenable: left,
        builder: (_, x, child) => Padding(padding: EdgeInsets.only(left: x), child: child),
        child: SizedBox(
          width: 200,
          height: 200,
          child: _CapturingCompositeSwt(
            value: _pane(withChild: withChild),
            onEvent: (ev, payload) {
              if (ev.endsWith('MouseMove')) moves.add(payload!.x!);
              if (ev.endsWith('MouseUp')) up = payload!.x;
            },
          ),
        ),
      ),
    ),
  ));
  await tester.pumpAndSettle();

  final g = await tester.startGesture(const Offset(201, 50));
  for (var i = 1; i <= 3; i++) {
    // Past the drag-move throttle, which reads the wall clock.
    await tester.runAsync(() => Future<void>.delayed(const Duration(milliseconds: 20)));
    await g.moveTo(Offset(201 - i * 10.0, 50));
    await tester.pump();
    left.value = 200 - i * 10.0;
    await tester.pump();
  }
  await g.up();
  await tester.pumpAndSettle();
  return (moves: moves, up: up);
}

void main() {
  for (final withChild in [false, true]) {
    final shape = withChild ? 'a pane with children' : 'a childless pane';
    testWidgets('$shape reports a held pointer relative to where it has moved', (tester) async {
      final r = await _dragEdge(tester, withChild: withChild);

      // The first move comes before the pane has moved: 191 - 200. Each later one finds the pane
      // already 10 px further left, under a pointer that moved the same 10 px.
      expect(r.moves, [-9, -9, -9],
          reason: 'natively the pointer stays where it is on the pane while the pane follows it');
      expect(r.up, 1, reason: 'released at 171 over a pane that now starts at 170');
    });
  }
}
