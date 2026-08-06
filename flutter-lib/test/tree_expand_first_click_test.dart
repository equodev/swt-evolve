// Regression: expanding a Tree branch with the mouse "fired one click late" — clicking the
// arrow appeared to do nothing and the branch only opened on a later interaction.
//
// Root cause was an asymmetry between the two ways to expand. The keyboard path
// (TreeImpl._navigateRight/_navigateLeft) flips `expanded` locally and rebuilds, then sends
// the SWT event; the mouse path (TreeItemImpl._toggleExpand) only sent the event, so the row
// could not repaint until Java echoed the whole tree state back. Any delay in that round-trip
// — and it is only flushed on the next turn of the SWT event loop — showed up as a click that
// did nothing, with the branch opening on the interaction after it.
//
// A tap in the expander column must therefore be observable in two ways, and both are
// asserted below: the Expand/Collapse event goes out (Java stays authoritative) *and* the
// tree renders the new state on that same tap, with no round-trip.

import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/gen/event.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/gen/tree.dart';
import 'package:swtflutter/src/gen/treeitem.dart';

/// A Tree that records the Expand/Collapse events its items send. There is no Java in a widget
/// test, so nothing ever echoes state back — which is exactly the condition this regression is
/// about: the rendered result must come from the tap alone.
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

void main() {
  testWidgets('tapping the arrow expands the branch on that same click',
      (tester) async {
    final value = _treeWithBranch(expanded: false);
    final tree = _CapturingTree(value: value);
    await tester.pumpWidget(_wrap(tree));
    await tester.pump();

    expect(find.text('Leaf 0.0'), findsNothing);

    await tester.tapAt(tester.getRect(find.byIcon(Icons.keyboard_arrow_right)).center);
    await tester.pump();

    expect(tree.expands, hasLength(1),
        reason: 'Java stays authoritative — the SWT Expand must still be sent');
    expect(value.items!.first.expanded, isTrue,
        reason: 'the tap itself must flip the state, not the Java echo');
    expect(find.text('Leaf 0.0'), findsOneWidget,
        reason: 'the children must be on screen after the first click');
  });

  testWidgets('tapping the arrow collapses the branch on that same click',
      (tester) async {
    final value = _treeWithBranch(expanded: true);
    final tree = _CapturingTree(value: value);
    await tester.pumpWidget(_wrap(tree));
    await tester.pump();

    expect(find.text('Leaf 0.0'), findsOneWidget);

    await tester.tapAt(tester.getRect(find.byIcon(Icons.keyboard_arrow_down)).center);
    await tester.pump();

    expect(tree.collapses, hasLength(1));
    expect(value.items!.first.expanded, isFalse);
    expect(find.text('Leaf 0.0'), findsNothing);
  });

  testWidgets('a row-content tap still selects and never toggles', (tester) async {
    // The optimistic flip must be reachable only from the expander column — a click on the
    // label is a selection, as before.
    final value = _treeWithBranch(expanded: false);
    final tree = _CapturingTree(value: value);
    await tester.pumpWidget(_wrap(tree));
    await tester.pump();

    await tester.tap(find.text('Branch 0'));
    await tester.pump();

    expect(tree.expands, isEmpty);
    expect(value.items!.first.expanded, isFalse);
    expect(find.text('Leaf 0.0'), findsNothing);
  });

  testWidgets('the keyboard path keeps expanding on the first press',
      (tester) async {
    // The behaviour the mouse path was measured against — guards the parity.
    final value = _treeWithBranch(expanded: false);
    final tree = _CapturingTree(value: value);
    await tester.pumpWidget(_wrap(tree));
    await tester.pump();

    await tester.tap(find.text('Branch 0'));
    await tester.pump();
    await tester.sendKeyEvent(LogicalKeyboardKey.arrowRight);
    await tester.pump();

    expect(tree.expands, hasLength(1));
    expect(value.items!.first.expanded, isTrue);
    expect(find.text('Leaf 0.0'), findsOneWidget);
  });
}
