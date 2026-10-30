// A Keyword/Debug pair of radio items is drawn by the bar as one segmented control. The control
// follows the items' selection on its own, without the bar rebuilding; any other change of an item
// still reaches the bar, which decides what the items are drawn as.

import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/gen/toolbar.dart';
import 'package:swtflutter/src/gen/toolitem.dart';
import 'package:swtflutter/src/impl/utils/quiet_animations.dart';
import 'package:swtflutter/src/gen/widgets.dart' show WireKey;

import 'delivery/support/deliver.dart';

VToolItem _item(int id, String text, {required bool selected, int seq = 10}) => VToolItem()
  ..id = id
  ..style = SWT.RADIO
  ..seq = seq
  ..enabled = true
  ..text = text
  ..selection = selected;

VToolBar _bar() => VToolBar()
  ..id = 1
  ..style = SWT.HORIZONTAL | SWT.FLAT
  ..seq = 10
  ..enabled = true
  ..visible = true
  ..items = [_item(2, 'Keyword', selected: true), _item(3, 'Debug', selected: false)]
  ..bounds = (VRectangle()
    ..x = 0
    ..y = 0
    ..width = 300
    ..height = 40);

Future<void> _mount(WidgetTester tester) async {
  freshClient();
  await tester.pumpWidget(EvolveApp(
    theme: ThemeMode.light,
    contentWidget: SizedBox(width: 300, height: 40, child: ToolBarSwt<VToolBar>(value: _bar())),
  ));
  await tester.pumpAndSettle();
}

/// Where the selected segment's highlight is drawn, once it has settled.
Rect _highlight(WidgetTester tester) {
  final box = find.byType(SlidingBox);
  final highlight = tester.widget<SlidingBox>(box);
  final area = tester.getRect(box);
  return Rect.fromLTWH(area.left + highlight.left, area.top, highlight.width, area.height);
}

void main() {
  testWidgets('selecting the other item moves the highlight without rebuilding the bar', (tester) async {
    await _mount(tester);
    expect(_highlight(tester).center.dx, lessThan(tester.getCenter(find.text('Debug')).dx));

    var barBuilds = 0;
    debugOnRebuildDirtyWidget = (element, _) {
      if (element.widget is ToolBarSwt) barBuilds++;
    };
    addTearDown(() => debugOnRebuildDirtyWidget = null);
    await deliverChange(_item(2, 'Keyword', selected: false, seq: 11), keys: [WireKey.k_ToolItem_selection], base: 10);
    await deliverChange(_item(3, 'Debug', selected: true, seq: 11), keys: [WireKey.k_ToolItem_selection], base: 10);
    await tester.pumpAndSettle();

    final highlight = _highlight(tester);
    final debug = tester.getCenter(find.text('Debug'));
    expect(highlight.left, lessThan(debug.dx));
    expect(highlight.right, greaterThan(debug.dx));
    expect(barBuilds, 0);
  });

  testWidgets('renaming an item gives it back to the bar to draw', (tester) async {
    await _mount(tester);
    expect(find.text('Keyword'), findsOneWidget);

    await deliverChange(_item(3, 'Run', selected: false, seq: 11), keys: [WireKey.k_Item_text], base: 10);
    await tester.pumpAndSettle();

    // Both are ordinary items: the Keyword/Debug pair is gone.
    expect(find.text('Run'), findsOneWidget);
    expect(find.text('Debug'), findsNothing);
  });
}
