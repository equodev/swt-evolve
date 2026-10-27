// A TreeItem's per-column foreground and font -- what a JFace colour/font label provider sets through
// ViewerCell -- reach the row. The colour follows use_swt_font_colors, global or per widget, which
// falls back to use_swt_fonts; the font's weight and slant always apply, as the item-wide font's do.

import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/gen/color.dart';
import 'package:swtflutter/src/gen/font.dart';
import 'package:swtflutter/src/gen/fontdata.dart';
import 'package:swtflutter/src/gen/label.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/gen/tree.dart';
import 'package:swtflutter/src/gen/treeitem.dart';
import 'package:swtflutter/src/impl/config_flags.dart';
import 'package:swtflutter/src/impl/widget_config.dart';

const _depthBlue = Color(0xFF0A64C8);
const _label = 'Depth_Full_Offset';
const _ownLabel = 'App coloured label';

VTree _tree() => VTree()
  ..id = 1
  ..style = SWT.V_SCROLL
  ..enabled = true
  ..columns = []
  ..items = [
    VTreeItem()
      ..id = 10
      ..text = _label
      ..texts = [_label]
      ..foregrounds = [
        VColor()
          ..alpha = 0xFF
          ..red = 0x0A
          ..green = 0x64
          ..blue = 0xC8,
      ]
      ..fonts = [
        VFont()
          ..fontData = [
            VFontData()
              ..name = 'Arial'
              ..height = 10
              ..style = SWT.BOLD,
          ],
      ],
  ]
  ..bounds = (VRectangle()
    ..x = 0
    ..y = 0
    ..width = 300
    ..height = 200);

VLabel _appColoredLabel() => VLabel()
  ..id = 20
  ..style = SWT.NONE
  ..enabled = true
  ..text = _ownLabel
  ..foreground = (VColor()
    ..alpha = 0xFF
    ..red = 0x0A
    ..green = 0x64
    ..blue = 0xC8)
  ..bounds = (VRectangle()
    ..x = 0
    ..y = 0
    ..width = 200
    ..height = 20);

/// A control's own text, which its build colours above the scope it publishes for its items.
Future<Color?> _ownLabelColor(WidgetTester tester) async {
  await tester.pumpWidget(EvolveApp(
    theme: ThemeMode.light,
    contentWidget: SizedBox(
      width: 200,
      height: 20,
      child: LabelSwt<VLabel>(value: _appColoredLabel()),
    ),
  ));
  await tester.pumpAndSettle();
  return tester.widget<Text>(find.text(_ownLabel)).style?.color;
}

Future<TextStyle> _labelStyle(WidgetTester tester) async {
  await tester.pumpWidget(EvolveApp(
    theme: ThemeMode.light,
    contentWidget: SizedBox(
      width: 300,
      height: 200,
      child: TreeSwt<VTree>(value: _tree()),
    ),
  ));
  await tester.pumpAndSettle();
  return tester.widget<Text>(find.text(_label)).style!;
}

void main() {
  setUp(() => setConfigFlags(ConfigFlags()));

  testWidgets('the theme colours the cell unless something asks for the application colour',
      (tester) async {
    final style = await _labelStyle(tester);

    expect(style.color, isNot(_depthBlue));
    expect(style.fontWeight, FontWeight.bold,
        reason: 'the cell font travelled and its weight applies, as an item-wide font\'s does');
  });

  testWidgets('use_swt_font_colors_tree draws the colour the cell was given', (tester) async {
    setConfigFlags(ConfigFlags()..use_swt_font_colors_by_widget = {'tree': true});

    expect((await _labelStyle(tester)).color, _depthBlue);
  });

  testWidgets('a flag for another widget leaves the tree on the theme', (tester) async {
    setConfigFlags(ConfigFlags()..use_swt_font_colors_by_widget = {'table': true});

    expect((await _labelStyle(tester)).color, isNot(_depthBlue));
  });

  testWidgets('use_swt_font_colors draws it for every widget', (tester) async {
    setConfigFlags(ConfigFlags()..use_swt_font_colors = true);

    expect((await _labelStyle(tester)).color, _depthBlue);
  });

  testWidgets('left unset, use_swt_font_colors follows use_swt_fonts', (tester) async {
    setConfigFlags(ConfigFlags()..use_swt_fonts = true);

    expect((await _labelStyle(tester)).color, _depthBlue);
  });

  testWidgets('the per-widget value wins over the global one', (tester) async {
    setConfigFlags(ConfigFlags()
      ..use_swt_fonts = true
      ..use_swt_font_colors_by_widget = {'tree': false});

    expect((await _labelStyle(tester)).color, isNot(_depthBlue));
  });

  testWidgets('a control answers to its own name, not to another widget\'s', (tester) async {
    setConfigFlags(ConfigFlags()..use_swt_font_colors_by_widget = {'tree': true});
    expect(await _ownLabelColor(tester), isNot(_depthBlue));

    setConfigFlags(ConfigFlags()..use_swt_font_colors_by_widget = {'label': true});
    expect(await _ownLabelColor(tester), _depthBlue);
  });

  test('a column the item set nothing for decodes as null', () {
    final item = VTreeItem.fromJson({
      'swt': 'TreeItem',
      'id': 10,
      'foregrounds': [
        null,
        {'r': 10, 'g': 100, 'b': 200, 'a': 255},
      ],
    });

    expect(item.foregrounds, hasLength(2));
    expect(item.foregrounds![0], isNull);
    expect(item.foregrounds![1]!.blue, 200);
  });
}
