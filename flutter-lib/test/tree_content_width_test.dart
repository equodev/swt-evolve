// How far a Tree without columns scrolls sideways follows its widest row, measured again for an item
// whose text changed, whether or not a row shows it.

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
const _long = 'Item 59, the widest of them all by some way';

VTreeItem _item(int id, {int seq = 1, String? text}) => VTreeItem()
  ..id = id
  ..seq = seq
  ..text = text ?? 'Item $id'
  ..texts = [text ?? 'Item $id'];

// More rows than the Tree shows; the last, the widest, is never on screen.
List<VTreeItem> _items() => [for (var id = 10; id < 59; id++) _item(id), _item(59, text: _long)];

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

double _horizontalExtent(WidgetTester tester) {
  for (final scrollable in tester.widgetList<Scrollable>(find.byType(Scrollable))) {
    if (scrollable.axisDirection == AxisDirection.right) {
      return tester.state<ScrollableState>(find.byWidget(scrollable)).position.maxScrollExtent;
    }
  }
  return 0;
}

void main() {
  testWidgets('an unseen item that grows or shrinks moves how far the Tree scrolls', (tester) async {
    await _mount(tester);
    final wide = _horizontalExtent(tester);
    expect(wide, greaterThan(0), reason: 'the last item is wider than the Tree');

    await deliverChange(_item(59, seq: 2, text: '$_long, and longer still'),
        keys: [WireKey.k_Item_text, WireKey.k_TreeItem_texts], base: 1);
    await tester.pumpAndSettle();
    expect(_horizontalExtent(tester), greaterThan(wide), reason: 'its longer text is measured');

    await deliverChange(_item(59, seq: 3, text: _long), keys: [WireKey.k_Item_text, WireKey.k_TreeItem_texts], base: 2);
    await tester.pumpAndSettle();
    expect(_horizontalExtent(tester), moreOrLessEquals(wide), reason: 'and its old text again');
  });

}
