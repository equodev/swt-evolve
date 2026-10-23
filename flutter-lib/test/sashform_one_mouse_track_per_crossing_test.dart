// A SashForm reports MouseEnter and MouseExit once per crossing, through the hover arbiter every
// control shares, with the pointer position; a second, position-less pair from the layout's own
// mouse region makes the crossing look different from one run to the next.

import 'package:flutter/gestures.dart';
import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/gen/event.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/sash.dart';
import 'package:swtflutter/src/gen/sashform.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/gen/text.dart';

class _RecordingSashFormSwt extends SashFormSwt<VSashForm> {
  const _RecordingSashFormSwt({required super.value, required this.calls});

  final List<String> calls;

  @override
  void sendMouseTrackMouseEnter(VSashForm val, VEvent? payload) =>
      calls.add('enter:${payload?.x},${payload?.y}');

  @override
  void sendMouseTrackMouseExit(VSashForm val, VEvent? payload) =>
      calls.add('exit:${payload?.x},${payload?.y}');
}

VRectangle _rect(int x, int y, int w, int h) => VRectangle()
  ..x = x
  ..y = y
  ..width = w
  ..height = h;

VText _text(int id, int x) => VText()
  ..id = id
  ..style = SWT.SINGLE | SWT.BORDER
  ..text = 'pane'
  ..visible = true
  ..enabled = true
  ..bounds = _rect(x, 0, 140, 24);

void main() {
  testWidgets('a crossing into and out of a SashForm reports one MouseEnter and one MouseExit',
      (tester) async {
    final calls = <String>[];
    await tester.pumpWidget(EvolveApp(
      theme: ThemeMode.light,
      contentWidget: Align(
        alignment: Alignment.topLeft,
        child: SizedBox(
          width: 300,
          height: 200,
          child: _RecordingSashFormSwt(
            calls: calls,
            value: VSashForm()
              ..swt = 'SashForm'
              ..id = 1
              ..style = SWT.HORIZONTAL
              ..visible = true
              ..enabled = true
              ..bounds = _rect(0, 0, 300, 200)
              ..weights = [1, 1]
              ..children = [_text(2, 0), _text(3, 150)],
          ),
        ),
      ),
    ));
    await tester.pumpAndSettle();

    final mouse = await tester.createGesture(kind: PointerDeviceKind.mouse);
    await mouse.addPointer(location: const Offset(400, 100));
    await tester.pump();
    await mouse.moveTo(const Offset(148, 150));
    await tester.pump();
    await mouse.moveTo(const Offset(400, 100));
    await tester.pump();
    await mouse.removePointer();

    expect(calls, ['enter:148,150', 'exit:400,100']);
  });

  // A release settles what the pointer is over once the sash has followed it there: settled against
  // the layout before the drag's last step, the pointer is still over the pane the sash left.
  testWidgets('a sash drag and its release cross nothing on the SashForm, the pointer staying on the sash',
      (tester) async {
    final calls = <String>[];
    // The panes and the sash where Java's SashFormLayout puts them, the sash at [sashX].
    Future<void> pumpForm(int sashX) => tester.pumpWidget(EvolveApp(
      theme: ThemeMode.light,
      contentWidget: Align(
        alignment: Alignment.topLeft,
        child: SizedBox(
          width: 300,
          height: 200,
          child: _RecordingSashFormSwt(
            calls: calls,
            value: VSashForm()
              ..swt = 'SashForm'
              ..id = 1
              ..style = SWT.HORIZONTAL
              ..visible = true
              ..enabled = true
              ..bounds = _rect(0, 0, 300, 200)
              ..weights = [1, 1]
              ..children = [
                _text(2, 0)..bounds = _rect(0, 0, sashX, 200),
                VSash()
                  ..swt = 'Sash'
                  ..id = 4
                  ..style = SWT.VERTICAL
                  ..visible = true
                  ..enabled = true
                  ..bounds = _rect(sashX, 0, 10, 200),
                _text(3, sashX + 10)..bounds = _rect(sashX + 10, 0, 290 - sashX, 200),
              ],
          ),
        ),
      ),
    ));
    await pumpForm(140);
    await tester.pumpAndSettle();

    final sash = tester.getCenter(find.byType(SashSwt));
    final mouse = await tester.createGesture(kind: PointerDeviceKind.mouse);
    await mouse.addPointer(location: sash);
    await tester.pumpAndSettle();
    calls.clear();

    await mouse.down(sash);
    await tester.pump();
    for (var i = 1; i <= 8; i++) {
      await mouse.moveTo(sash + Offset(i * 10.0, 0));
      // Java answers each step's Selection by laying the sash out under the pointer.
      await pumpForm(140 + i * 10);
      if (i < 8) await tester.pump(const Duration(milliseconds: 16));
    }
    await mouse.up();
    await tester.pumpAndSettle();
    final afterRelease = calls.toList();
    await mouse.removePointer();

    expect(afterRelease, isEmpty);
  });
}
