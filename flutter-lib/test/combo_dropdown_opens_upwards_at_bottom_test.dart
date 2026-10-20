// A Combo sitting on the bottom edge of the window -- the perspective combo of a workbench status
// bar -- has no room for its list underneath. Native SWT then shows the list above the field; a
// list placed below is laid out past the window's edge and never seen.

import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/gen/combo.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/swt.dart';

const List<String> _items = ['General', 'Database', 'Debug'];

VCombo _combo([List<String> items = _items]) => VCombo()
  ..swt = 'Combo'
  ..id = 1
  ..style = SWT.DROP_DOWN | SWT.READ_ONLY
  ..enabled = true
  ..text = items.first
  ..items = items
  ..bounds = (VRectangle()
    ..x = 0
    ..y = 0
    ..width = 106
    ..height = 30);

Future<void> _open(WidgetTester tester, Widget content) async {
  await tester.binding.setSurfaceSize(const Size(400, 300));
  addTearDown(() => tester.binding.setSurfaceSize(null));
  await tester.pumpWidget(EvolveApp(theme: ThemeMode.light, contentWidget: content));
  await tester.pumpAndSettle();
  await tester.tap(find.byType(ComboSwt<VCombo>));
  await tester.pumpAndSettle();
}

Future<void> _openAt(WidgetTester tester, Alignment alignment, [List<String> items = _items]) =>
    _open(tester, Align(alignment: alignment, child: ComboSwt<VCombo>(value: _combo(items))));

Finder _item(String item) => find.byWidgetPredicate((w) => w is Text && w.data == item);

Rect _itemRect(WidgetTester tester, String item) => tester.getRect(_item(item));

Rect _listRect(WidgetTester tester, String anyItem) => tester.getRect(
    find.ancestor(of: _item(anyItem), matching: find.byType(SingleChildScrollView)).first);

void main() {
  testWidgets('a Combo on the bottom edge opens its list above the field', (tester) async {
    await _openAt(tester, Alignment.bottomLeft);

    final Rect field = tester.getRect(find.byType(ComboSwt<VCombo>));
    for (final item in _items) {
      final Rect cell = _itemRect(tester, item);
      expect(cell.bottom, lessThanOrEqualTo(field.top),
          reason: '"$item" is laid out at $cell, not above the field at $field');
      expect(cell.top, greaterThanOrEqualTo(0));
    }
  });

  testWidgets('a Combo with room underneath still opens its list below the field',
      (tester) async {
    await _openAt(tester, Alignment.topLeft);

    final Rect field = tester.getRect(find.byType(ComboSwt<VCombo>));
    expect(_itemRect(tester, _items.first).top, greaterThanOrEqualTo(field.bottom));
  });

  testWidgets('a list that fits on neither side takes the roomier one and stays in the window',
      (tester) async {
    final items = [for (var i = 0; i < 40; i++) 'Item $i'];
    // Below the middle: more room above the field than under it.
    await _openAt(tester, const Alignment(-1, 0.4), items);

    final Rect field = tester.getRect(find.byType(ComboSwt<VCombo>));
    final Rect list = _listRect(tester, items.first);
    expect(list.bottom, lessThanOrEqualTo(field.top),
        reason: 'the list at $list should open above the field at $field');
    expect(list.top, greaterThanOrEqualTo(0),
        reason: 'the list at $list must be cut to the room above, not run off the window');
  });

  testWidgets('an open list follows its field when the field moves', (tester) async {
    final controller = ScrollController();
    addTearDown(controller.dispose);
    await _open(
      tester,
      SingleChildScrollView(
        controller: controller,
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            const SizedBox(height: 60),
            ComboSwt<VCombo>(value: _combo()),
            const SizedBox(height: 600),
          ],
        ),
      ),
    );

    final double gap =
        _itemRect(tester, _items.first).top - tester.getRect(find.byType(ComboSwt<VCombo>)).bottom;

    controller.jumpTo(40);
    await tester.pumpAndSettle();

    final Rect field = tester.getRect(find.byType(ComboSwt<VCombo>));
    expect(_itemRect(tester, _items.first).top - field.bottom, gap,
        reason: 'the list stayed where the field was before the scroll');
  });
}
