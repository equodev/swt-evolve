// What a drag passing over a control costs it. The drop feedback that comes and goes under the
// pointer changes how the item is drawn, never which widgets make it up, so nothing is built again
// from scratch; it is drawn where it repaints the least; and the controls a drag passes over do not
// light up for it, since natively the control the button was pressed on keeps the mouse.

import 'package:flutter/gestures.dart';
import 'package:flutter/material.dart';
import 'package:flutter/rendering.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/gen/canvas.dart';
import 'package:swtflutter/src/gen/composite.dart';
import 'package:swtflutter/src/gen/ctabfolder.dart';
import 'package:swtflutter/src/gen/ctabitem.dart';
import 'package:swtflutter/src/gen/event.dart';
import 'package:swtflutter/src/gen/label.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/gen/table.dart';
import 'package:swtflutter/src/gen/tablecolumn.dart';
import 'package:swtflutter/src/gen/tableitem.dart';
import 'package:swtflutter/src/gen/tree.dart';
import 'package:swtflutter/src/gen/treeitem.dart';
import 'package:swtflutter/src/impl/utils/dnd_utils.dart';

import 'delivery/support/deliver.dart';

const int _dropTargetId = 7;

VRectangle _rect(int x, int y, int w, int h) => VRectangle()
  ..x = x
  ..y = y
  ..width = w
  ..height = h;

VTable _table() => VTable()
  ..id = 2
  ..style = SWT.NONE
  ..enabled = true
  ..headerVisible = false
  ..linesVisible = false
  ..dragSource = true
  ..dropTargetId = _dropTargetId
  ..columns = [VTableColumn()..id = 3..width = 400..text = 'C']
  ..items = [for (var i = 0; i < 12; i++) VTableItem()..id = 100 + i..texts = ['Row $i']]
  ..bounds = _rect(0, 0, 400, 400);

VTree _tree() => VTree()
  ..id = 1
  ..style = SWT.NONE
  ..enabled = true
  ..dragSource = true
  ..dropTargetId = _dropTargetId
  ..items = [for (var i = 0; i < 12; i++) VTreeItem()..id = 100 + i..text = 'Item $i']
  ..bounds = _rect(0, 0, 400, 400);

Widget _app(Widget content, {double width = 400, double height = 400}) => EvolveApp(
      theme: ThemeMode.light,
      contentWidget: SizedBox(width: width, height: height, child: content),
    );

Future<void> _answerSelect() => deliverEvent('DropTarget/$_dropTargetId/Drop/negotiationResult',
    VEvent()..detail = Dnd.dropMove..feedback = Dnd.feedbackSelect);

/// Presses at [start] with a mouse and crosses the drag slop, leaving the button held.
Future<TestGesture> _press(WidgetTester tester, Offset start) async {
  final gesture = await tester.createGesture(kind: PointerDeviceKind.mouse, buttons: kPrimaryButton);
  await gesture.addPointer(location: start);
  await gesture.down(start);
  await tester.pump(const Duration(milliseconds: 50));
  for (var i = 0; i < 3; i++) {
    await gesture.moveBy(const Offset(0, 4));
    await tester.pump(const Duration(milliseconds: 16));
  }
  return gesture;
}

/// Moves to each of [points], a frame after each.
Future<void> _moveThrough(WidgetTester tester, TestGesture gesture, List<Offset> points) async {
  for (final point in points) {
    await gesture.moveTo(point);
    await tester.pump(const Duration(milliseconds: 16));
  }
}

Future<void> _release(WidgetTester tester, TestGesture gesture) async {
  await gesture.up();
  await tester.pumpAndSettle();
  // Nothing answers the dragStart verdict here; let the fail-open timer run out.
  await tester.pump(const Duration(milliseconds: 400));
}

Element _elementOf(WidgetTester tester, String text) => tester.element(find.text(text).first);

/// Counts the widgets of type [W] built, or built again, while [body] runs.
Future<int> _buildsOf<W extends Widget>(Future<void> Function() body) async {
  var count = 0;
  debugOnRebuildDirtyWidget = (element, _) {
    if (element.widget is W) count++;
  };
  try {
    await body();
  } finally {
    debugOnRebuildDirtyWidget = null;
  }
  return count;
}

/// The paragraphs painted while [body] runs that read [text].
Future<int> _paintsOfText(String text, Future<void> Function() body) async {
  var count = 0;
  debugOnProfilePaint = (renderObject) {
    if (renderObject is RenderParagraph && renderObject.text.toPlainText() == text) count++;
  };
  try {
    await body();
  } finally {
    debugOnProfilePaint = null;
  }
  return count;
}

/// Counts the render objects of type [R] painted while [body] runs.
Future<int> _paintsOf<R extends RenderObject>(Future<void> Function() body) async {
  var count = 0;
  debugOnProfilePaint = (renderObject) {
    if (renderObject is R) count++;
  };
  try {
    await body();
  } finally {
    debugOnProfilePaint = null;
  }
  return count;
}

void main() {
  setUp(freshClient);

  testWidgets('a drag passing in and out of a drop target builds none of its children again',
      (tester) async {
    final source = VCanvas()
      ..id = 1
      ..style = SWT.NONE
      ..enabled = true
      ..visible = true
      ..dragSource = true
      ..bounds = _rect(0, 0, 100, 300);
    final target = VComposite()
      ..id = 2
      ..style = SWT.NONE
      ..enabled = true
      ..visible = true
      ..dropTargetId = _dropTargetId
      ..bounds = _rect(200, 0, 200, 300)
      ..children = [
        VLabel()
          ..id = 10
          ..style = SWT.NONE
          ..enabled = true
          ..visible = true
          ..text = 'Inside'
          ..bounds = _rect(10, 10, 150, 24)
      ];
    await tester.pumpWidget(_app(
      Stack(children: [
        Positioned(left: 0, top: 0, width: 100, height: 300, child: CanvasSwt<VCanvas>(value: source)),
        Positioned(left: 200, top: 0, width: 200, height: 300, child: CompositeSwt<VComposite>(value: target)),
      ]),
    ));
    await tester.pump();
    final before = _elementOf(tester, 'Inside');

    final gesture = await _press(tester, const Offset(50, 150));
    await _moveThrough(tester, gesture, const [Offset(300, 150), Offset(50, 150), Offset(300, 150)]);
    expect(identical(_elementOf(tester, 'Inside'), before), isTrue,
        reason: 'the drag entering and leaving changes the border, not the children under it');
    await _release(tester, gesture);
  });

  testWidgets('a Tree row the drop feedback passes over is not built again', (tester) async {
    await tester.pumpWidget(_app(TreeSwt<VTree>(value: _tree())));
    await tester.pump();
    Offset row(String text) => tester.getCenter(find.text(text).first);
    final before = _elementOf(tester, 'Item 4');

    final gesture = await _press(tester, row('Item 0'));
    await _answerSelect();
    await _moveThrough(tester, gesture, [row('Item 3'), row('Item 4'), row('Item 5')]);
    expect(identical(_elementOf(tester, 'Item 4'), before), isTrue);
    await _release(tester, gesture);
  });

  testWidgets('the drop feedback moving between Table rows does not repaint the rows', (tester) async {
    await tester.pumpWidget(_app(TableSwt<VTable>(value: _table())));
    await tester.pump();
    Offset row(String text) => tester.getCenter(find.text(text).first);

    final gesture = await _press(tester, row('Row 0'));
    await _answerSelect();
    await _moveThrough(tester, gesture, [row('Row 3')]);
    final painted = await _paintsOf<RenderTable>(
        () => _moveThrough(tester, gesture, [row('Row 4'), row('Row 5'), row('Row 6')]));
    expect(painted, 0, reason: 'the feedback is drawn in a layer of its own, over the rows');
    await _release(tester, gesture);
  });

  testWidgets('a Table cell grows under a passing pointer, but not under a drag', (tester) async {
    await tester.pumpWidget(_app(TableSwt<VTable>(value: _table())));
    await tester.pump();
    Offset row(String text) => tester.getCenter(find.text(text).first);
    final grown = find.byType(CompositedTransformFollower);

    final gesture = await _press(tester, row('Row 0'));
    await _moveThrough(tester, gesture, [row('Row 3'), row('Row 4')]);
    expect(grown, findsNothing, reason: 'the button held keeps the mouse with the row it was pressed on');
    await _release(tester, gesture);

    await gesture.moveTo(row('Row 4') + const Offset(0, 2));
    await tester.pump();
    expect(grown, findsOneWidget, reason: 'the first move after release hovers the cell under it');
  });
  testWidgets('the drop feedback moving between Tree rows repaints no row', (tester) async {
    await tester.pumpWidget(_app(TreeSwt<VTree>(value: _tree())));
    await tester.pump();
    Offset row(String text) => tester.getCenter(find.text(text).first);

    final gesture = await _press(tester, row('Item 0'));
    await _answerSelect();
    await _moveThrough(tester, gesture, [row('Item 3')]);
    final painted = await _paintsOf<RenderParagraph>(
        () => _moveThrough(tester, gesture, [row('Item 4'), row('Item 5'), row('Item 6')]));
    expect(painted, 0, reason: 'the feedback is painted in a layer of its own, over the rows');
    await _release(tester, gesture);
  });

  testWidgets('a hover moving over a Tree\'s rows repaints no other control', (tester) async {
    final composite = VComposite()
      ..id = 2
      ..style = SWT.NONE
      ..enabled = true
      ..visible = true
      ..bounds = _rect(0, 0, 400, 400)
      ..children = [
        _tree()
          ..dragSource = false
          ..dropTargetId = null
          ..bounds = _rect(0, 0, 300, 400),
        VLabel()
          ..id = 50
          ..style = SWT.NONE
          ..enabled = true
          ..visible = true
          ..text = 'Beside'
          ..bounds = _rect(310, 10, 80, 24),
      ];
    await tester.pumpWidget(_app(CompositeSwt<VComposite>(value: composite)));
    await tester.pumpAndSettle();
    Offset row(String text) => tester.getCenter(find.text(text).first);

    final mouse = await tester.createGesture(kind: PointerDeviceKind.mouse);
    await mouse.addPointer(location: row('Item 0'));
    await tester.pump();
    final painted = await _paintsOfText('Beside', () async {
      for (final item in ['Item 1', 'Item 2', 'Item 3']) {
        await mouse.moveTo(row(item));
        await tester.pump();
      }
    });
    expect(painted, 0, reason: 'a row lighting up repaints the Tree, not the controls beside it');
    await mouse.removePointer();
  });

  testWidgets('a drag along a CTabFolder strip, or a hover over its bar, builds none of its tabs',
      (tester) async {
    VCTabFolder folder(int id) => VCTabFolder()
      ..id = id
      ..style = SWT.NONE
      ..enabled = true
      ..selection = 0
      ..dragSource = true
      ..dropTargetId = id == 2 ? _dropTargetId : null
      ..items = [for (var i = 0; i < 4; i++) VCTabItem()..id = id * 100 + i..text = 'Tab $id.$i'..showing = true];
    await tester.pumpWidget(_app(
      Stack(children: [
        Positioned(left: 0, top: 0, width: 600, height: 200, child: CTabFolderSwt<VCTabFolder>(value: folder(1))),
        Positioned(left: 0, top: 200, width: 600, height: 200, child: CTabFolderSwt<VCTabFolder>(value: folder(2))),
      ]),
      width: 600,
    ));
    await tester.pumpAndSettle();
    Offset tab(String text) => tester.getCenter(find.text(text).first);

    final mouse = await tester.createGesture(kind: PointerDeviceKind.mouse);
    await mouse.addPointer(location: const Offset(590, 390));
    await tester.pump();
    final hovering = await _buildsOf<Text>(() async {
      await mouse.moveTo(tab('Tab 2.0') + const Offset(0, 2));
      await tester.pumpAndSettle();
      await mouse.moveTo(const Offset(590, 390));
      await tester.pumpAndSettle();
    });
    expect(hovering, lessThanOrEqualTo(2),
        reason: 'only the tab under the pointer changes; the bar and its other tabs stay as built');
    await mouse.removePointer();

    final gesture = await _press(tester, tab('Tab 1.0'));
    await _moveThrough(tester, gesture, [tab('Tab 2.0')]);
    final dragging = await _buildsOf<Text>(
        () => _moveThrough(tester, gesture, [tab('Tab 2.1'), tab('Tab 2.2'), tab('Tab 2.3')]));
    expect(dragging, 0, reason: 'the tint and the marker move by painting alone');
    await _release(tester, gesture);
  });

  testWidgets('the item a drag picks up is dimmed, not built again', (tester) async {
    await tester.pumpWidget(_app(TreeSwt<VTree>(value: _tree())));
    await tester.pump();
    final before = _elementOf(tester, 'Item 2');

    final gesture = await _press(tester, tester.getCenter(find.text('Item 2')));
    expect(identical(_elementOf(tester, 'Item 2'), before), isTrue);
    await _release(tester, gesture);
  });
}
