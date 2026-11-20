// A tree row is found by its identifier whether or not it has children, including a row whose
// children arrive after it is shown; a row with children has its expander tagged too.

import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/gen/tree.dart';
import 'package:swtflutter/src/gen/treeitem.dart';
import 'package:swtflutter/src/gen/widgets.dart' show WireKey;
import 'package:swtflutter/src/impl/utils/test_anchors.dart';

import 'delivery/support/deliver.dart';

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

VTreeItem _leaf() => VTreeItem()
  ..id = 10
  ..seq = 1
  ..text = 'leaf';

VTreeItem _parent() => VTreeItem()
  ..id = 10
  ..seq = 10
  ..text = 'parent'
  ..itemCount = 1
  ..items = [VTreeItem()
    ..id = 11
    ..text = 'child'];

Future<void> _settle(WidgetTester tester) async {
  for (var i = 0; i < 5; i++) {
    await tester.runAsync(() => Future<void>.delayed(const Duration(milliseconds: 50)));
    await tester.pump();
  }
  await tester.pumpAndSettle();
}

Future<void> _pump(WidgetTester tester, VTreeItem row) async {
  await tester.pumpWidget(EvolveApp(
    theme: ThemeMode.light,
    contentWidget: SizedBox(width: 300, height: 200, child: TreeSwt<VTree>(value: _tree(row))),
  ));
  await _settle(tester);
}

void main() {
  setUpAll(() => TestAnchors.enabled = true);
  tearDownAll(() => TestAnchors.enabled = false);
  setUp(freshClient);

  testWidgets('a leaf row is found by its identifier', (tester) async {
    await _pump(tester, _leaf());

    expect(TestAnchors.locate('TreeItem/10'), isNotNull);
  });

  testWidgets('a row whose children arrive is still found by its identifier, and so is its expander',
      (tester) async {
    await _pump(tester, _leaf());
    expect(TestAnchors.locate('TreeItem/10'), isNotNull);
    expect(TestAnchors.locate('TreeItemExpander/10'), isNull, reason: 'a leaf has no expander');

    await deliverChange(_parent(), keys: [WireKey.k_TreeItem_itemCount, WireKey.k_TreeItem_items], base: 1);
    await _settle(tester);

    expect(TestAnchors.locate('TreeItem/10'), isNotNull);
    expect(TestAnchors.locate('TreeItemExpander/10'), isNotNull);
  });
}
