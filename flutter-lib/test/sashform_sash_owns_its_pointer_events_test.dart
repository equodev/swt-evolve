// A SashForm's divider is its Sash child on every SWT, so a press, drag or release on it is the
// Sash's MouseDown, Selection and MouseUp, never the SashForm's own.

import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/comm/comm_frame.dart';
import 'package:swtflutter/src/comm/comm_ws.dart';
import 'package:swtflutter/src/gen/label.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/sash.dart';
import 'package:swtflutter/src/gen/sashform.dart';
import 'package:swtflutter/src/gen/swt.dart';

VRectangle _rect(int x, int y, int w, int h) => VRectangle()
  ..x = x
  ..y = y
  ..width = w
  ..height = h;

VLabel _label(int id, int x) => VLabel()
  ..swt = 'Label'
  ..id = id
  ..style = SWT.NONE
  ..text = 'pane $id'
  ..visible = true
  ..enabled = true
  ..bounds = _rect(x, 0, 195, 300);

// Laid out as Java's SashFormLayout places them: pane, 10-wide sash, pane.
VSashForm _sashForm() => VSashForm()
  ..swt = 'SashForm'
  ..id = 42
  ..style = SWT.HORIZONTAL
  ..enabled = true
  ..visible = true
  ..sashWidth = 10
  ..weights = [1, 1]
  ..bounds = _rect(0, 0, 400, 300)
  ..children = [
    _label(1, 0),
    VSash()
      ..swt = 'Sash'
      ..id = 3
      ..style = SWT.VERTICAL
      ..visible = true
      ..enabled = true
      ..bounds = _rect(195, 0, 10, 300),
    _label(2, 205),
  ];

void main() {
  testWidgets('a press, drag and release on the divider is the Sash\'s, not the SashForm\'s',
      (tester) async {
    EquoCommBase.recordSentFrames = true;
    addTearDown(() => EquoCommBase.recordSentFrames = false);
    await tester.pumpWidget(EvolveApp(
      theme: ThemeMode.light,
      contentWidget: Align(
        alignment: Alignment.topLeft,
        child: SizedBox(
          width: 400,
          height: 300,
          child: SashFormSwt(value: _sashForm()),
        ),
      ),
    ));
    await tester.pumpAndSettle();
    EquoCommService.commForTesting.clearSentFrames();

    final divider = find.byType(SashSwt);
    expect(divider, findsOneWidget);
    expect(tester.getRect(divider), const Rect.fromLTWH(195, 0, 10, 300),
        reason: 'the sash is drawn where Java laid it out');
    final gesture = await tester.startGesture(tester.getCenter(divider));
    await tester.pump();
    await gesture.moveBy(const Offset(20, 0));
    await tester.pump();
    await gesture.up();
    await tester.pumpAndSettle();

    final sent = EquoCommService.commForTesting.sentFrames
        .map((f) => EquoCommBase.decodeFrame(f).$1)
        .toList();
    expect(sent.where((c) => c.startsWith('SashForm/42/')), isEmpty,
        reason: 'the divider is the Sash, so the SashForm reports neither its press nor its drag');
    expect(sent, containsAllInOrder(['Sash/3/Mouse/MouseDown', 'Sash/3/Selection/Selection', 'Sash/3/Mouse/MouseUp']));
  });
}
