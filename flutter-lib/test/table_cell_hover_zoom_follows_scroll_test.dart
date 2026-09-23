// A hovered Table cell draws its enlarged copy in the Overlay, outside the scrolling subtree.
// Nothing about that copy may go stale while it is on screen: it has to sit on the cell it belongs
// to as the table scrolls under the pointer, and it has to paint what that cell currently holds.
//
// The scroll step is deliberately smaller than the hovered box: past its edge the pointer leaves
// the cell, MouseTracker hides the copy, and the test would pass without ever moving one.

import 'package:flutter/gestures.dart' show PointerDeviceKind;
import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/gen/table.dart';
import 'package:swtflutter/src/gen/tablecolumn.dart';
import 'package:swtflutter/src/gen/tableitem.dart';

import 'delivery/support/deliver.dart';

const double _viewportHeight = 200;
const double _tableWidth = 200;
const int _rows = 200;
const double _scrollStep = 6;
const String _probe = 'row 3';

VTable _table({String prefix = 'row', int seq = 1}) => VTable()
  ..id = 1
  ..seq = seq
  ..style = SWT.NONE
  ..headerVisible = false
  ..linesVisible = false
  ..itemCount = _rows
  ..columns = [
    VTableColumn()
      ..id = 2
      ..seq = seq
      ..width = _tableWidth.toInt()
      ..alignment = SWT.LEFT
      ..text = 'Col1'
  ]
  ..items = [
    for (int i = 0; i < _rows; i++)
      VTableItem()
        ..id = 100 + i
        ..seq = seq
        ..texts = ['$prefix $i']
  ]
  ..bounds = (VRectangle()
    ..x = 0
    ..y = 0
    ..width = _tableWidth.toInt()
    ..height = _viewportHeight.toInt());

Widget _wrap(VTable value) => EvolveApp(
      theme: ThemeMode.light,
      contentWidget: SizedBox(
        width: _tableWidth,
        height: _viewportHeight,
        child: TableSwt<VTable>(value: value),
      ),
    );

/// The cell still laid out inside the table -- what the copy is anchored to.
Finder _inPlace(String text) =>
    find.descendant(of: find.byType(Table), matching: find.text(text));

ScrollController _scroll(WidgetTester tester) =>
    tester.widget<Scrollable>(find.byType(Scrollable).last).controller!;

/// Rect of the overlay copy: the only [Text] carrying [text] that is not the in-place cell.
Rect _copyRect(WidgetTester tester, String text) {
  final inPlace = tester.getRect(_inPlace(text));
  final all = find.text(text);
  expect(all, findsNWidgets(2), reason: 'expected the cell and its overlay copy');
  final first = tester.getRect(all.at(0));
  return first == inPlace ? tester.getRect(all.at(1)) : first;
}

Future<TestGesture> _hover(WidgetTester tester, Finder target) async {
  final gesture = await tester.createGesture(kind: PointerDeviceKind.mouse);
  await gesture.addPointer(location: Offset.zero);
  addTearDown(gesture.removePointer);
  await gesture.moveTo(tester.getCenter(target));
  await tester.pumpAndSettle();
  return gesture;
}

void main() {
  testWidgets('the enlarged copy follows its cell as the table scrolls under the pointer',
      (tester) async {
    await tester.pumpWidget(_wrap(_table()));
    await tester.pumpAndSettle();

    await _hover(tester, _inPlace(_probe));
    final restingCopy = _copyRect(tester, _probe);

    final controller = _scroll(tester);
    final before = tester.getRect(_inPlace(_probe));
    controller.jumpTo(controller.offset + _scrollStep);
    await tester.pumpAndSettle();

    final after = tester.getRect(_inPlace(_probe));
    expect(after.top, closeTo(before.top - _scrollStep, 0.01),
        reason: 'the cell itself must have moved for this to prove anything');

    // The copy grows from the cell's centre-left, so that edge -- not its centre -- is the anchor
    // that has to keep matching.
    final copy = _copyRect(tester, _probe);
    expect(copy.left, closeTo(after.left, 0.5));
    expect(copy.center.dy, closeTo(after.center.dy, 0.5),
        reason: 'the copy stayed at ${restingCopy.center} instead of tracking its cell');
  });

  testWidgets('the enlarged copy repaints the content its cell now holds', (tester) async {
    await tester.pumpWidget(_wrap(_table()));
    await tester.pumpAndSettle();

    await _hover(tester, _inPlace(_probe));
    expect(find.text(_probe), findsNWidgets(2));

    await deliverWhole(_table(prefix: 'cell', seq: 2));
    await tester.pumpAndSettle();

    expect(find.text(_probe), findsNothing,
        reason: 'the copy keeps painting the frame it was inserted in');
    expect(find.text('cell 3'), findsNWidgets(2));
  });

  testWidgets('the copy of a half-scrolled cell is cut at the body, not drawn over the header',
      (tester) async {
    await tester.pumpWidget(_wrap(_table()));
    await tester.pumpAndSettle();

    // Leaves the probe straddling the top edge: a few pixels of it are still hoverable, the rest is
    // scrolled out and must not reappear in the copy.
    final controller = _scroll(tester);
    final cell = tester.getRect(_inPlace(_probe));
    final body = tester.getRect(find.byType(Scrollable).last);
    controller.jumpTo(cell.top - body.top + cell.height / 2);
    await tester.pumpAndSettle();
    await _hover(tester, _inPlace(_probe));

    final clip = tester.widget<ClipRect>(find.descendant(
      of: find.byType(CompositedTransformFollower),
      matching: find.byType(ClipRect),
    ));
    // The clip is expressed in the cell's own coordinates, the space the follower puts its child in.
    final clipTop = tester.getTopLeft(_inPlace(_probe)).dy + clip.clipper!.getClip(Size.zero).top;
    expect(clipTop, closeTo(body.top, 0.01));
  });
}
