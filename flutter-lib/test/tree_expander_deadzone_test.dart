// Regression: the Tree expand/collapse arrow intermittently "stopped responding".
// Root cause was a dead zone — the row's tap handler ceded the whole expander band (full row
// height) expecting the arrow to take it, but the arrow's hit area was only the 12x12 icon
// centred in a ~34px row. A tap in the gap (same x as the arrow, but above/below the icon) was
// handled by neither: nothing expanded, no event was sent. The fix gives the row's single tap
// handler the whole expander column, so any tap in that column toggles.
//
// These tests tap the previously-dead points and assert the branch still toggles — with the old
// code the tap landed in the gap and nothing happened.

import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/gen/event.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/gen/tree.dart';
import 'package:swtflutter/src/gen/treeitem.dart';

/// A Tree that records the Expand/Collapse events its items try to send (there is no Java in a
/// widget test, so the round-trip that would flip the live `expanded` flag never happens — the
/// sent event is the observable that proves the tap was handled).
class _CapturingTree extends TreeSwt<VTree> {
  _CapturingTree({required super.value});

  final List<VEvent?> expands = [];
  final List<VEvent?> collapses = [];

  @override
  void sendTreeExpand(VTree val, VEvent? payload) => expands.add(payload);

  @override
  void sendTreeCollapse(VTree val, VEvent? payload) => collapses.add(payload);
}

VTreeItem _leaf(int id, String text) => VTreeItem()
  ..id = id
  ..text = text;

VTree _treeWithBranch({required bool expanded}) => VTree()
  ..id = 1
  ..style = SWT.MULTI
  ..enabled = true
  ..items = [
    VTreeItem()
      ..id = 10
      ..text = 'Branch 0'
      ..expanded = expanded
      ..items = [_leaf(20, 'Leaf 0.0'), _leaf(21, 'Leaf 0.1')],
  ]
  ..bounds = (VRectangle()
    ..x = 0
    ..y = 0
    ..width = 800
    ..height = 400);

Widget _wrap(TreeSwt<VTree> tree) => EvolveApp(
      theme: ThemeMode.light,
      contentWidget: SizedBox(width: 800, height: 400, child: tree),
    );

List<int> _selectedIds(VTree value) =>
    (value.selection ?? []).map((item) => item.id).toList();

void main() {
  testWidgets(
      'tapping the expander column above the icon (old dead zone) expands the branch',
      (tester) async {
    final value = _treeWithBranch(expanded: false);
    final tree = _CapturingTree(value: value);
    await tester.pumpWidget(_wrap(tree));
    await tester.pump();

    final icon = tester.getRect(find.byIcon(Icons.keyboard_arrow_right));
    // Same x as the arrow (inside the expander band), but 4px above the 12x12 icon — the
    // region that used to be swallowed by neither the row nor the arrow.
    await tester.tapAt(Offset(icon.center.dx, icon.top - 4));
    await tester.pump();

    expect(tree.expands, hasLength(1),
        reason: 'a tap in the expander column must expand, even above the icon');
    expect(tree.collapses, isEmpty);
    expect(_selectedIds(value), isEmpty,
        reason: 'an expander tap toggles expansion, it does not select the row');
  });

  testWidgets(
      'tapping the expander column below the icon (old dead zone) expands the branch',
      (tester) async {
    final value = _treeWithBranch(expanded: false);
    final tree = _CapturingTree(value: value);
    await tester.pumpWidget(_wrap(tree));
    await tester.pump();

    final icon = tester.getRect(find.byIcon(Icons.keyboard_arrow_right));
    await tester.tapAt(Offset(icon.center.dx, icon.bottom + 4));
    await tester.pump();

    expect(tree.expands, hasLength(1),
        reason: 'a tap below the icon but inside the row must still expand');
    expect(tree.collapses, isEmpty);
  });

  testWidgets('tapping the expander on an expanded branch collapses it',
      (tester) async {
    final value = _treeWithBranch(expanded: true);
    final tree = _CapturingTree(value: value);
    await tester.pumpWidget(_wrap(tree));
    await tester.pump();

    final icon = tester.getRect(find.byIcon(Icons.keyboard_arrow_down));
    await tester.tapAt(Offset(icon.center.dx, icon.top - 4));
    await tester.pump();

    expect(tree.collapses, hasLength(1));
    expect(tree.expands, isEmpty);
  });

  testWidgets(
      'tapping the row content selects the item and does not toggle expansion',
      (tester) async {
    final value = _treeWithBranch(expanded: false);
    final tree = _CapturingTree(value: value);
    await tester.pumpWidget(_wrap(tree));
    await tester.pump();

    // The label sits well to the right of the expander column.
    await tester.tap(find.text('Branch 0'));
    await tester.pump();

    expect(_selectedIds(value), equals([10]),
        reason: 'a tap on the content selects the row');
    expect(tree.expands, isEmpty,
        reason: 'a content tap must not toggle expansion');
    expect(tree.collapses, isEmpty);
  });
}
