// An application's text colour, once honoured, was picked against the application's own ground.
// Drawn on the theme's ground instead it keeps its hue but is moved to a lightness that reads there.

import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/gen/color.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/gen/tree.dart';
import 'package:swtflutter/src/gen/treeitem.dart';
import 'package:swtflutter/src/impl/config_flags.dart';
import 'package:swtflutter/src/impl/utils/widget_utils.dart';
import 'package:swtflutter/src/impl/widget_config.dart';
import 'package:swtflutter/src/theme/theme_extensions/tree_theme_extension.dart';

const _black = Color(0xFF000000);
const _blue = Color(0xFF0A64C8);
const _darkGround = Color(0xFF1F1F1F);
const _lightGround = Color(0xFFFFFFFF);
// Reads on the dark theme's surface, not on its selected-row background.
const _midGrey = Color(0xFF8A8A8A);

VColor _v(Color c) => VColor()
  ..alpha = 0xFF
  ..red = (c.r * 255).round()
  ..green = (c.g * 255).round()
  ..blue = (c.b * 255).round();

VTree _tree() => VTree()
  ..id = 1
  ..style = SWT.V_SCROLL | SWT.CHECK
  ..enabled = true
  ..columns = []
  ..items = [
    VTreeItem()
      ..id = 10
      ..text = 'black item'
      ..texts = ['black item']
      ..foregrounds = [_v(_black)],
    VTreeItem()
      ..id = 11
      ..text = 'blue item'
      ..texts = ['blue item']
      ..foregrounds = [_v(_blue)],
  ]
  ..bounds = (VRectangle()
    ..x = 0
    ..y = 0
    ..width = 300
    ..height = 200);

Future<({Color black, Color blue, Color ground})> _render(
    WidgetTester tester, ThemeMode mode) async {
  await tester.pumpWidget(EvolveApp(
    theme: mode,
    contentWidget: SizedBox(width: 300, height: 200, child: TreeSwt<VTree>(value: _tree())),
  ));
  await tester.pumpAndSettle();
  Color colorOf(String label) => tester.widget<Text>(find.text(label)).style!.color!;
  final ground = Theme.of(tester.element(find.text('black item')))
      .extension<TreeThemeExtension>()!
      .backgroundColor;
  return (black: colorOf('black item'), blue: colorOf('blue item'), ground: ground);
}

void main() {
  setUp(() => setConfigFlags(ConfigFlags()..use_swt_font_colors_by_widget = {'tree': true}));

  group('legibleOn', () {
    test('keeps a colour that already reads on the ground', () {
      expect(legibleOn(_black, _lightGround, minRatio: kMinTextContrast), _black);
      expect(legibleOn(_blue, _lightGround, minRatio: kMinTextContrast), _blue);
    });

    test('turns black to white on a dark ground', () {
      expect(legibleOn(_black, _darkGround, minRatio: kMinTextContrast), const Color(0xFFFFFFFF));
    });

    test('keeps the hue of a coloured text it has to lighten', () {
      final adapted = legibleOn(_blue, _darkGround, minRatio: kMinTextContrast);

      expect(contrastRatio(adapted, _darkGround), greaterThanOrEqualTo(4.5));
      expect(HSLColor.fromColor(adapted).hue, closeTo(HSLColor.fromColor(_blue).hue, 1));
      expect(adapted.computeLuminance(), greaterThan(_blue.computeLuminance()));
    });

    test('darkens what is too light for a light ground', () {
      const paleYellow = Color(0xFFFFF59D);
      final adapted = legibleOn(paleYellow, _lightGround, minRatio: kMinTextContrast);

      expect(contrastRatio(adapted, _lightGround), greaterThanOrEqualTo(4.5));
      expect(HSLColor.fromColor(adapted).hue, closeTo(HSLColor.fromColor(paleYellow).hue, 1));
      expect(adapted.computeLuminance(), lessThan(paleYellow.computeLuminance()));
    });
  });

  group('a selected row is judged against its selection background', () {
    Future<void> expectReadableWhenSelected(WidgetTester tester, VTree tree) async {
      await tester.pumpWidget(EvolveApp(
        theme: ThemeMode.dark,
        contentWidget: SizedBox(width: 300, height: 200, child: TreeSwt<VTree>(value: tree)),
      ));
      await tester.pumpAndSettle();
      final theme = Theme.of(tester.element(find.text('grey item'))).extension<TreeThemeExtension>()!;
      final selectedGround =
          Color.alphaBlend(theme.selectedBackgroundColor, theme.backgroundColor);
      final color = tester.widget<Text>(find.text('grey item')).style!.color!;

      expect(contrastRatio(_midGrey, theme.backgroundColor), greaterThanOrEqualTo(4.5),
          reason: 'the colour must read on the plain ground for this case to mean anything');
      expect(contrastRatio(color, selectedGround), greaterThanOrEqualTo(4.5));
    }

    VTree selectedTree({VColor? treeForeground, List<VColor?>? itemForegrounds}) => VTree()
      ..id = 2
      ..style = SWT.V_SCROLL
      ..enabled = true
      ..foreground = treeForeground
      ..columns = []
      ..items = [
        VTreeItem()
          ..id = 20
          ..text = 'grey item'
          ..texts = ['grey item']
          ..foregrounds = itemForegrounds,
      ]
      ..selection = [VTreeItem()..id = 20]
      ..bounds = (VRectangle()
        ..x = 0
        ..y = 0
        ..width = 300
        ..height = 200);

    testWidgets('for the item\'s own colour', (tester) async {
      await expectReadableWhenSelected(tester, selectedTree(itemForegrounds: [_v(_midGrey)]));
    });

    testWidgets('for the colour it inherits from the Tree', (tester) async {
      await expectReadableWhenSelected(tester, selectedTree(treeForeground: _v(_midGrey)));
    });
  });

  testWidgets('on the dark theme the application colours stay readable', (tester) async {
    final r = await _render(tester, ThemeMode.dark);

    expect(contrastRatio(r.black, r.ground), greaterThanOrEqualTo(4.5),
        reason: 'black text on the dark ground is invisible');
    expect(contrastRatio(r.blue, r.ground), greaterThanOrEqualTo(4.5));
    expect(HSLColor.fromColor(r.blue).hue, closeTo(HSLColor.fromColor(_blue).hue, 1),
        reason: 'the blue item still reads as blue, apart from the black one');
  });

  testWidgets('with use_swt_colors the application owns the ground, and its colour is kept',
      (tester) async {
    setConfigFlags(ConfigFlags()
      ..use_swt_colors = true
      ..use_swt_font_colors_by_widget = {'tree': true});

    final r = await _render(tester, ThemeMode.dark);

    expect(r.black, _black);
    expect(r.blue, _blue);
  });

  testWidgets('on the light theme the application colours are drawn as given', (tester) async {
    final r = await _render(tester, ThemeMode.light);

    expect(r.black, _black);
    expect(r.blue, _blue);
  });
}
