import 'package:flutter/material.dart';
import 'package:flutter/services.dart' show FontLoader, rootBundle;
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/gen/ccombo.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/swt.dart';

const String _selected = 'Default';
const List<String> _items = [
  _selected,
  'Short',
  'Medium item',
  '02_First_Surface_Decimated_long_name',
  '01_Second_Surface_Decimated_long_name',
];

Future<void> _loadInter() async {
  final loader = FontLoader('Inter')
    ..addFont(rootBundle.load('assets/fonts/Inter_18pt-Medium.ttf'));
  await loader.load();
}

VCCombo _hintedCCombo() => VCCombo()
  ..swt = 'CCombo'
  ..id = 7
  ..style = SWT.BORDER | SWT.READ_ONLY
  ..enabled = true
  ..text = _selected
  ..items = _items
  ..bounds = (VRectangle()
    ..x = 0
    ..y = 0
    ..width = 100
    ..height = 24);

Future<void> _pumpAndOpen(WidgetTester tester) async {
  await tester.pumpWidget(EvolveApp(
    theme: ThemeMode.light,
    contentWidget: Align(
      alignment: Alignment.topLeft,
      child: CComboSwt<VCCombo>(value: _hintedCCombo()),
    ),
  ));
  await tester.pumpAndSettle();
  await tester.tap(find.byType(CComboSwt<VCCombo>));
  await tester.pumpAndSettle();
}

Finder _listItem(String item) =>
    find.byWidgetPredicate((Widget w) => w is Text && w.data == item).hitTestable();

double _textWidth(String text, TextStyle style) => (TextPainter(
      text: TextSpan(text: text, style: style),
      maxLines: 1,
      textDirection: TextDirection.ltr,
    )..layout())
    .width;

void main() {
  setUpAll(_loadInter);

  testWidgets('the open list is as wide as its widest item, not as the field',
      (WidgetTester tester) async {
    await _pumpAndOpen(tester);

    for (final String item in _items) {
      final Finder cell = _listItem(item);
      expect(cell, findsOneWidget);
      final TextStyle style = tester.widget<Text>(cell).style!;
      final double itemWidth = _textWidth(item, style);
      final Size cellSize = tester.getSize(cell);
      expect(cellSize.width, greaterThanOrEqualTo(itemWidth),
          reason: 'Item "$item" is laid out '
              '${cellSize.width.toStringAsFixed(2)}px wide for '
              '${itemWidth.toStringAsFixed(2)}px of text: the list kept the '
              "field's width instead of widening to the item.");
    }
  });

  testWidgets('the open list is never narrower than the field',
      (WidgetTester tester) async {
    await _pumpAndOpen(tester);

    final Size field = tester.getSize(find.byType(CComboSwt<VCCombo>));
    final Finder shortItem = _listItem('Short');
    final Finder row =
        find.ancestor(of: shortItem, matching: find.byType(MenuItemButton));
    expect(tester.getSize(row).width, greaterThanOrEqualTo(field.width - 2),
        reason: 'A short item row must still span the field, as in SWT.');
  });

  testWidgets('the closed field keeps the width the application gave it',
      (WidgetTester tester) async {
    await _pumpAndOpen(tester);

    expect(tester.getSize(find.byType(CComboSwt<VCCombo>)).width, 100.0);
  });
}
