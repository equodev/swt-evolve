// Drift guard. `Tree.getItem(Point)` has to answer, synchronously on the SWT UI thread,
// whether a point falls on an item's expand/collapse arrow — native SWT resolves no item there, and
// apps read that null as "the click was not on a row" and clear their selection. There is no Dart
// round-trip available for that answer, so the horizontal geometry is duplicated in Java in
// `dev.equo.swt.size.TreeSizes`.
//
// These tests pin the rendered arrow to the very numbers TreeSizes computes, so a change to the
// tree theme turns this red instead of silently desyncing the two sides.

import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/gen/event.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/gen/tree.dart';
import 'package:swtflutter/src/gen/treeitem.dart';

/// Width shared with `TreeGetItemOverExpanderTest` on the Java side.
const double treeWidth = 300;

// TreeSizes' constants, mirrored from theme_settings/tree_theme_settings.dart.
const double edgeGapFraction = 0.0275;
const double itemPaddingLeft = 8.0;
const double itemIndent = 16.0;
const double expandIconSize = 12.0;

double expanderLeft(int level) =>
    treeWidth * edgeGapFraction + itemPaddingLeft + itemIndent * level;

class _CapturingTree extends TreeSwt<VTree> {
  _CapturingTree({required super.value});

  final List<VEvent?> expands = [];
  final List<VEvent?> collapses = [];

  @override
  void sendTreeExpand(VTree val, VEvent? payload) => expands.add(payload);

  @override
  void sendTreeCollapse(VTree val, VEvent? payload) => collapses.add(payload);
}

/// A root branch (expanded, so it shows a "down" arrow) whose only child is itself a branch
/// (collapsed, so it shows a "right" arrow one indent level in).
VTree _nestedTree() => VTree()
  ..id = 1
  ..style = SWT.MULTI
  ..enabled = true
  ..items = [
    VTreeItem()
      ..id = 10
      ..text = 'Branch'
      ..expanded = true
      ..items = [
        VTreeItem()
          ..id = 20
          ..text = 'Child'
          ..expanded = false
          ..items = [
            VTreeItem()
              ..id = 30
              ..text = 'Grand child',
          ],
      ],
  ]
  ..bounds = (VRectangle()
    ..x = 0
    ..y = 0
    ..width = treeWidth.toInt()
    ..height = 400);

Widget _wrap(TreeSwt<VTree> tree) => EvolveApp(
      theme: ThemeMode.light,
      contentWidget:
          SizedBox(width: treeWidth, height: 400, child: tree),
    );

List<int> _selectedIds(VTree value) =>
    (value.selection ?? []).map((item) => item.id).toList();

void main() {
  late _CapturingTree tree;
  late VTree value;
  late Rect treeRect;

  Future<void> pumpTree(WidgetTester tester) async {
    value = _nestedTree();
    tree = _CapturingTree(value: value);
    await tester.pumpWidget(_wrap(tree));
    await tester.pump();
    treeRect = tester.getRect(find.byWidget(tree));
  }

  /// Rect of [icon] in tree-local coordinates — the frame `Tree.getItem(Point)` works in.
  Rect localIcon(WidgetTester tester, IconData icon) =>
      tester.getRect(find.byIcon(icon)).translate(-treeRect.left, -treeRect.top);

  testWidgets('the root arrow sits where TreeSizes puts it', (tester) async {
    await pumpTree(tester);

    final arrow = localIcon(tester, Icons.keyboard_arrow_down);
    expect(arrow.left, moreOrLessEquals(expanderLeft(0)));
    expect(arrow.width, moreOrLessEquals(expandIconSize));
  });

  testWidgets('each nesting level shifts the arrow by one indent', (tester) async {
    await pumpTree(tester);

    final arrow = localIcon(tester, Icons.keyboard_arrow_right);
    expect(arrow.left, moreOrLessEquals(expanderLeft(1)));
    expect(arrow.width, moreOrLessEquals(expandIconSize));
  });

  testWidgets('a tap at the left edge of the band toggles, one pixel left of it selects',
      (tester) async {
    await pumpTree(tester);
    final rowY = localIcon(tester, Icons.keyboard_arrow_down).center.dy;

    await tester.tapAt(
        treeRect.topLeft.translate(expanderLeft(0) + 0.5, rowY));
    await tester.pump();

    expect(tree.collapses, hasLength(1),
        reason: 'the first pixel of the band belongs to the expander');
    expect(_selectedIds(value), isEmpty);

    await pumpTree(tester);
    await tester.tapAt(treeRect.topLeft.translate(expanderLeft(0) - 1, rowY));
    await tester.pump();

    expect(tree.collapses, isEmpty,
        reason: 'left of the band is row content, not the expander');
    expect(_selectedIds(value), equals([10]));
  });

  testWidgets('a tap at the right edge of the band is already row content',
      (tester) async {
    await pumpTree(tester);
    final rowY = localIcon(tester, Icons.keyboard_arrow_down).center.dy;

    await tester.tapAt(treeRect.topLeft
        .translate(expanderLeft(0) + expandIconSize, rowY));
    await tester.pump();

    expect(tree.collapses, isEmpty);
    expect(_selectedIds(value), equals([10]));
  });
}
