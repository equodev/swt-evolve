import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/gen/image.dart';
import 'package:swtflutter/src/gen/imagedata.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/gen/toolbar.dart';
import 'package:swtflutter/src/gen/toolitem.dart';

const List<int> _pngBytes = <int>[
  0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A, //
  0x00, 0x00, 0x00, 0x0D, 0x49, 0x48, 0x44, 0x52,
  0x00, 0x00, 0x00, 0x01, 0x00, 0x00, 0x00, 0x01,
  0x08, 0x06, 0x00, 0x00, 0x00, 0x1F, 0x15, 0xC4,
  0x89, 0x00, 0x00, 0x00, 0x0A, 0x49, 0x44, 0x41,
  0x54, 0x78, 0x9C, 0x63, 0x00, 0x01, 0x00, 0x00,
  0x05, 0x00, 0x01, 0x0D, 0x0A, 0x2D, 0xB4, 0x00,
  0x00, 0x00, 0x00, 0x49, 0x45, 0x4E, 0x44, 0xAE,
  0x42, 0x60, 0x82,
];

VImage _icon() => VImage()
  ..imageData = (VImageData()
    ..width = 16
    ..height = 16
    ..data = _pngBytes);

// An image-bearing item, matching every real Eclipse toolbar item.
VToolItem _item({required int id, required int style, required bool selection}) => VToolItem()
  ..id = id
  ..style = style
  ..enabled = true
  ..selection = selection
  ..toolTipText = 'Scroll Lock'
  ..image = _icon();

VToolBar _toolBar(int id, VToolItem item) => VToolBar()
  ..id = id
  ..style = SWT.HORIZONTAL | SWT.FLAT
  ..enabled = true
  ..visible = true
  ..items = [item]
  ..bounds = (VRectangle()
    ..x = 0
    ..y = 0
    ..width = 400
    ..height = 40);

int _nextId = 100;

Future<BoxDecoration> _pumpAndFindDecoration(
  WidgetTester tester, {
  required int style,
  required bool selection,
}) async {
  final toolBarId = _nextId++;
  final toolItemId = _nextId++;
  await tester.pumpWidget(EvolveApp(
    theme: ThemeMode.light,
    contentWidget: SizedBox(
      width: 400,
      height: 40,
      child: ToolBarSwt(
        value: _toolBar(toolBarId, _item(id: toolItemId, style: style, selection: selection)),
      ),
    ),
  ));
  await tester.pumpAndSettle();

  // The button's own decoration carries a borderRadius; the outer wrapper
  // Container (plain background fill) does not, which disambiguates the two.
  final container = tester.widget<Container>(
    find.descendant(
      of: find.byType(ToolItemSwt),
      matching: find.byWidgetPredicate((w) =>
          w is Container &&
          w.decoration is BoxDecoration &&
          (w.decoration as BoxDecoration).borderRadius != null),
    ),
  );
  return container.decoration as BoxDecoration;
}

void main() {
  // Both styles are asserted within a single testWidgets call: pumping a
  // second unrelated widget tree from a later testWidgets block in this file
  // hangs pumpAndSettle (a pre-existing framework/test-harness interaction,
  // unrelated to this fix), so every pump here stays within one test.
  testWidgets('a toggled-on image-bearing CHECK/RADIO item is drawn selected', (tester) async {
    for (final style in [SWT.CHECK, SWT.RADIO]) {
      final selectedDecoration =
          await _pumpAndFindDecoration(tester, style: style, selection: true);
      final unselectedDecoration =
          await _pumpAndFindDecoration(tester, style: style, selection: false);

      expect(selectedDecoration.border, isNotNull,
          reason: 'native SWT draws a border box around a toggled-on item');
      expect(unselectedDecoration.border, isNull,
          reason: 'an un-toggled item must not carry the selected border');
      expect(selectedDecoration.color, isNot(equals(unselectedDecoration.color)),
          reason: 'a toggled-on item must not look identical to an unselected one');
    }
  });
}
