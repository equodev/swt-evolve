// A Tree that scrolls sideways keeps what it shows when an item's width crosses the viewport's:
// renaming an item below the visible rows wider than the Tree, and back, changes how far the Tree
// scrolls, and mounts nothing new.

import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/gen/tree.dart';
import 'package:swtflutter/src/gen/treeitem.dart';
import 'package:swtflutter/src/gen/widgets.dart' show WireKey;

import 'delivery/support/deliver.dart';

const double _width = 240;
const double _height = 200;
const _wide = 'Item 59 renamed to a name far wider than the Tree that shows it';

VTreeItem _item(int id, {int seq = 1, String? text}) => VTreeItem()
  ..id = id
  ..seq = seq
  ..text = text ?? 'Item $id'
  ..texts = [text ?? 'Item $id'];

// Far more rows than the Tree shows: the last one is never on screen. New for each test: the
// registry adopts these objects, stamps and all.
List<VTreeItem> _items() => [for (var id = 10; id < 60; id++) _item(id)];

VTree _tree() => VTree()
  ..id = 1
  ..seq = 1
  ..style = SWT.H_SCROLL | SWT.V_SCROLL
  ..enabled = true
  ..headerVisible = false
  ..linesVisible = false
  ..columns = []
  ..items = _items()
  ..bounds = (VRectangle()
    ..width = _width.toInt()
    ..height = _height.toInt());

Future<void> _mount(WidgetTester tester) async {
  freshClient();
  await tester.pumpWidget(EvolveApp(
    theme: ThemeMode.light,
    contentWidget: SizedBox(width: _width, height: _height, child: TreeSwt<VTree>(value: _tree())),
  ));
  await tester.pumpAndSettle();
}

Set<Element> _elements(WidgetTester tester) => Set<Element>.identity()..addAll(tester.allElements);

/// The elements there now that were not in [before].
List<String> _mounted(WidgetTester tester, Set<Element> before) =>
    [for (final e in tester.allElements) if (!before.contains(e)) '${e.widget.runtimeType}'];

double _horizontalExtent(WidgetTester tester) {
  for (final scrollable in tester.widgetList<Scrollable>(find.byType(Scrollable))) {
    if (scrollable.axisDirection == AxisDirection.right) {
      return tester.state<ScrollableState>(find.byWidget(scrollable)).position.maxScrollExtent;
    }
  }
  return 0;
}

Future<void> _rename(WidgetTester tester, {required int seq, required String text}) async {
  // Java's change of an item no row shows reaches the Tree, which holds it.
  await deliverChange(_item(59, seq: seq, text: text), keys: [WireKey.k_Item_text, WireKey.k_TreeItem_texts], base: seq - 1);
  await tester.pumpAndSettle();
}

void main() {
  testWidgets('renaming an unseen item back and forth across the viewport width mounts nothing',
      (tester) async {
    await _mount(tester);
    expect(_horizontalExtent(tester), 0, reason: 'every item fits');
    // The first time an item is wider than the Tree, its horizontal viewport is made.
    await _rename(tester, seq: 2, text: _wide);
    expect(_horizontalExtent(tester), greaterThan(0), reason: 'the renamed item is wider than the Tree');
    await _rename(tester, seq: 3, text: 'Item 59');
    expect(_horizontalExtent(tester), 0, reason: 'every item fits again');
    final shown = _elements(tester);

    await _rename(tester, seq: 4, text: _wide);
    expect(_horizontalExtent(tester), greaterThan(0));
    expect(_mounted(tester, shown), isEmpty, reason: 'the horizontal viewport stayed');

    await _rename(tester, seq: 5, text: 'Item 59');
    expect(_horizontalExtent(tester), 0);
    expect(_mounted(tester, shown), isEmpty, reason: 'and stays');
  });

  testWidgets('renaming an unseen item across the viewport width rebuilds no row', (tester) async {
    await _mount(tester);
    // Once across and back, which makes the horizontal viewport (see above).
    await _rename(tester, seq: 2, text: _wide);
    await _rename(tester, seq: 3, text: 'Item 59');
    final rebuilt = <String>[];
    debugOnRebuildDirtyWidget = (element, _) {
      // The row and everything of the row's own: its wrapper, the item widget, what it builds.
      final type = '${element.widget.runtimeType}';
      if (type.startsWith('TreeItem')) rebuilt.add(type);
    };
    addTearDown(() => debugOnRebuildDirtyWidget = null);

    await _rename(tester, seq: 4, text: _wide);
    expect(_horizontalExtent(tester), greaterThan(0), reason: 'the rename arrived');
    await _rename(tester, seq: 5, text: 'Item 59');
    debugOnRebuildDirtyWidget = null;

    expect(rebuilt, isEmpty, reason: 'a row takes the width the list gives it, whatever the widest item');
    final row = tester.getSize(find.byWidgetPredicate((w) => w is TreeItemSwt).first);
    expect(row.width, tester.getSize(find.byType(ListView)).width, reason: 'and fills the list');
  });
}
