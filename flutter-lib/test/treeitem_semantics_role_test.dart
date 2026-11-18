// A tree row's semantics node is a button whether or not it has children. On the web a role
// change rebuilds the node's element without its flt-semantics-identifier, so a row whose
// children arrive after it is shown must not change role.

import 'package:flutter/material.dart';
import 'package:flutter/semantics.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/gen/tree.dart';
import 'package:swtflutter/src/gen/treeitem.dart';

VTree _tree(VTreeItem row) => VTree()
  ..id = 1
  ..style = SWT.SINGLE
  ..enabled = true
  ..visible = true
  ..items = [row]
  ..bounds = (VRectangle()
    ..x = 0
    ..y = 0
    ..width = 300
    ..height = 200);

Future<void> _pump(WidgetTester tester, VTreeItem row) async {
  await tester.pumpWidget(EvolveApp(
    theme: ThemeMode.light,
    contentWidget: SizedBox(width: 300, height: 200, child: TreeSwt<VTree>(value: _tree(row))),
  ));
  for (var i = 0; i < 5; i++) {
    await tester.runAsync(() => Future<void>.delayed(const Duration(milliseconds: 50)));
    await tester.pump();
  }
  await tester.pumpAndSettle();
}

SemanticsData _row(WidgetTester tester) {
  SemanticsNode? found;
  void visit(SemanticsNode node) {
    if (node.getSemanticsData().identifier == 'TreeItem/10') found = node;
    node.visitChildren((child) {
      visit(child);
      return true;
    });
  }

  visit(tester.binding.pipelineOwner.semanticsOwner!.rootSemanticsNode!);
  expect(found, isNotNull, reason: 'the row carries its identifier');
  return found!.getSemanticsData();
}

void main() {
  testWidgets('a leaf row is a button', (tester) async {
    final handle = tester.ensureSemantics();
    await _pump(tester, VTreeItem()
      ..id = 10
      ..text = 'leaf');

    expect(_row(tester).flagsCollection.isButton, isTrue);
    handle.dispose();
  });

  testWidgets('a row with children is a button too, with the expander a node of its own', (tester) async {
    final handle = tester.ensureSemantics();
    await _pump(tester, VTreeItem()
      ..id = 10
      ..text = 'parent'
      ..itemCount = 1
      ..items = [VTreeItem()
        ..id = 11
        ..text = 'child']);

    final row = _row(tester);
    expect(row.flagsCollection.isButton, isTrue);
    expect(find.bySemanticsIdentifier('TreeItemExpander/10'), findsOneWidget);
    handle.dispose();
  });
}
