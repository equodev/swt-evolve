// A V_SCROLL | H_SCROLL Tree whose rows overflow both ways shows both scroll bars, as on SWT: the
// vertical bar on its right edge does not disappear because the rows are also wider than the tree.

import 'package:flutter/gestures.dart';
import 'package:flutter/rendering.dart';
import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/gen/tree.dart';
import 'package:swtflutter/src/gen/treecolumn.dart';
import 'package:swtflutter/src/gen/treeitem.dart';

const double _treeWidth = 200;
const double _treeHeight = 300;

/// How close to the tree's right edge a vertical thumb has to be drawn.
const double _thumbEdgeTolerance = 4;

String _shortLabel(int i) => 'Item $i';

String _wideLabel(int i) => 'Item $i with a label much wider than the tree itself';

VTree _tree(String Function(int) label, {List<VTreeColumn> columns = const []}) => VTree()
  ..id = 1
  ..style = SWT.H_SCROLL | SWT.V_SCROLL
  ..enabled = true
  ..headerVisible = false
  ..linesVisible = false
  ..columns = columns
  ..items = [
    for (int i = 0; i < 40; i++)
      VTreeItem()
        ..id = 10 + i
        ..text = label(i)
        ..texts = [for (int c = 0; c < (columns.isEmpty ? 1 : columns.length); c++) label(i)],
  ]
  ..bounds = (VRectangle()
    ..x = 0
    ..y = 0
    ..width = _treeWidth.toInt()
    ..height = _treeHeight.toInt());

Widget _wrap(VTree value) => EvolveApp(
      theme: ThemeMode.light,
      contentWidget: Align(
        alignment: Alignment.topLeft,
        child: SizedBox(
          width: _treeWidth,
          height: _treeHeight,
          child: TreeSwt<VTree>(value: value),
        ),
      ),
    );

/// Whether one of the tree's scroll bars paints a vertical thumb hugging its right edge.
bool _hasVerticalThumb(WidgetTester tester) {
  for (final element in find
      .descendant(
          of: find.byWidgetPredicate((w) => w is RawScrollbar), matching: find.byType(CustomPaint))
      .evaluate()) {
    if ((element.widget as CustomPaint).foregroundPainter is! ScrollbarPainter) continue;
    final box = element.renderObject! as RenderBox;
    bool isVerticalThumb(Rect r) =>
        r.height > r.width && r.right > box.size.width - _thumbEdgeTolerance && r.height < box.size.height;
    final matcher = paints
      ..something((method, arguments) {
        final shape = arguments.isEmpty ? null : arguments.first;
        if (shape is RRect) return isVerticalThumb(shape.outerRect);
        if (shape is Rect) return isVerticalThumb(shape);
        return false;
      });
    if ((matcher as Matcher).matches(box, {})) return true;
  }
  return false;
}

void main() {
  testWidgets('rows that fit the width show the vertical scroll bar', (tester) async {
    await tester.pumpWidget(_wrap(_tree(_shortLabel)));
    await tester.pumpAndSettle();

    expect(_hasVerticalThumb(tester), isTrue);
  });

  testWidgets('rows wider than the tree still show the vertical scroll bar', (tester) async {
    await tester.pumpWidget(_wrap(_tree(_wideLabel)));
    await tester.pumpAndSettle();

    expect(_hasVerticalThumb(tester), isTrue,
        reason: 'the tree overflows downwards, so the vertical bar must be drawn');
  });

  testWidgets('scrolling down keeps the vertical scroll bar', (tester) async {
    await tester.pumpWidget(_wrap(_tree(_wideLabel)));
    await tester.pumpAndSettle();

    await tester.sendEventToBinding(PointerScrollEvent(
      position: const Offset(_treeWidth / 2, _treeHeight / 2),
      scrollDelta: const Offset(0, 200),
    ));
    await tester.pumpAndSettle();

    final list = tester.state<ScrollableState>(find.byWidgetPredicate(
        (w) => w is Scrollable && w.axisDirection == AxisDirection.down));
    expect(list.position.pixels, greaterThan(0), reason: 'the wheel must have scrolled the rows');
    expect(_hasVerticalThumb(tester), isTrue);
  });

  testWidgets('a tree with columns shows the vertical scroll bar', (tester) async {
    await tester.pumpWidget(_wrap(_tree(_shortLabel, columns: [
      VTreeColumn()
        ..id = 2
        ..width = 120
        ..alignment = SWT.LEFT
        ..text = 'Name',
    ])));
    await tester.pumpAndSettle();

    expect(_hasVerticalThumb(tester), isTrue);
  });
}
