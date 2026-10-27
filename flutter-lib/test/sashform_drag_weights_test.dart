// A drag on a SashForm's divider is its Sash's Selection, sent while the sash moves so Java lays the
// panes out under the pointer during the drag (DartSashForm.onDragSash), not only at the drop.

import 'dart:convert';

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
import 'package:swtflutter/src/impl/widget_config.dart';

VRectangle _rect(int x, int y, int w, int h) => VRectangle()
  ..x = x
  ..y = y
  ..width = w
  ..height = h;

VLabel _panel(int id, int x) => VLabel()
  ..swt = 'Label'
  ..id = id
  ..style = SWT.NONE
  ..text = 'panel $id'
  ..bounds = _rect(x, 0, 195, 300);

// Laid out as Java's SashFormLayout places them: pane, 10-wide sash, pane.
VSashForm _sashForm(int id) => VSashForm()
  ..swt = 'SashForm'
  ..id = id
  ..style = SWT.HORIZONTAL
  ..enabled = true
  ..sashWidth = 10
  ..weights = [1, 1]
  ..bounds = _rect(0, 0, 400, 300)
  ..children = [
    _panel(id + 1, 0),
    VSash()
      ..swt = 'Sash'
      ..id = id + 2
      ..style = SWT.VERTICAL
      ..enabled = true
      ..bounds = _rect(195, 0, 10, 300),
    _panel(id + 3, 205),
  ];

/// The x of every Selection the form's sash sent, in order.
List<int> _sashSelections(int formId) => EquoCommService.commForTesting.sentFrames
    .map(EquoCommBase.decodeFrame)
    .where((f) => f.$1 == 'Sash/${formId + 2}/Selection/Selection')
    .map((f) => (jsonDecode(f.$2) as Map<String, dynamic>)['x'] as int)
    .toList();

Future<void> _pumpForm(WidgetTester tester, int id) async {
  await tester.pumpWidget(EvolveApp(
    theme: ThemeMode.light,
    contentWidget: Align(
      alignment: Alignment.topLeft,
      child: SizedBox(width: 400, height: 300, child: SashFormSwt(value: _sashForm(id))),
    ),
  ));
  await tester.pumpAndSettle();
}

void main() {
  setUp(() {
    EquoCommBase.recordSentFrames = true;
    EquoCommService.commForTesting.clearSentFrames();
  });
  tearDown(() => EquoCommBase.recordSentFrames = false);

  testWidgets('dragging a SashForm divider sends the Sash\'s Selection while it moves',
      (WidgetTester tester) async {
    await _pumpForm(tester, 42);

    final gesture = await tester.startGesture(tester.getCenter(find.byType(SashSwt)));
    await gesture.moveBy(const Offset(30, 0));
    await tester.pump();
    await gesture.moveBy(const Offset(30, 0));
    await tester.pump();

    final live = _sashSelections(42);
    expect(live, isNotEmpty,
        reason: 'the sash must report where it is DURING the drag so Java reflows the panes as it '
            'moves, not only at drop');

    await gesture.up();
    await tester.pumpAndSettle();

    expect(_sashSelections(42).last, greaterThan(195),
        reason: 'the divider moved right, so the sash must report a position right of where it was');
  });

  testWidgets('a zoomed SashForm divider moves as far as the pointer, in SWT units',
      (WidgetTester tester) async {
    // The panes are laid out in SWT units and the drag is read off the pointer in the view's
    // pixels. At 1.0 they coincide; at any other scale a missed conversion puts the sash elsewhere
    // than the pointer moved it to.
    Future<int> sashShiftAt(double scale, int id) async {
      setAppScaleOverride(scale);
      addTearDown(() => setAppScaleOverride(null));
      await _pumpForm(tester, id);

      final gesture = await tester.startGesture(tester.getCenter(find.byType(SashSwt)));
      await gesture.moveBy(const Offset(30, 0));
      await tester.pump();
      await gesture.moveBy(const Offset(30, 0));
      await tester.pump();
      await gesture.up();
      await tester.pumpAndSettle();
      return _sashSelections(id).last - 195;
    }

    // The pointer travels 60 of the view's pixels: 60 SWT units at 1.0, 40 at 1.5.
    expect(await sashShiftAt(1.0, 42), closeTo(60, 1));
    expect(await sashShiftAt(1.5, 52), closeTo(40, 1));
  });
}
