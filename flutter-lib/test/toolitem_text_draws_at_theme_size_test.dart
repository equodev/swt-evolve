// A text-only ToolItem draws its text in the theme's style, whatever the ToolBar's font, and the
// padding `ToolItemSizes.java` measured for its style holds it: in a bar Java sized that way, every
// item's text comes out at the theme's size, unscaled.

import 'package:flutter/material.dart';
import 'package:flutter/rendering.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/gen/font.dart';
import 'package:swtflutter/src/gen/fontdata.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/gen/toolbar.dart';
import 'package:swtflutter/src/gen/toolitem.dart';
import 'package:swtflutter/src/impl/config_flags.dart';
import 'package:swtflutter/src/impl/widget_config.dart';
import 'package:swtflutter/src/theme/theme_extensions/toolitem_theme_extension.dart';

const String _label = 'Columns';

const _styles = {'PUSH': SWT.PUSH, 'CHECK': SWT.CHECK, 'DROP_DOWN': SWT.DROP_DOWN};

VToolItem _item(int style) => VToolItem()
  ..id = 2
  ..style = style
  ..enabled = true
  ..text = _label;

/// The item's size on its own, which is what `tool/measure_toolitem.dart` records for ToolItemSizes.
Future<Size> _measuredItem(WidgetTester tester, int style) async {
  final key = GlobalKey();
  await tester.pumpWidget(EvolveApp(
    theme: ThemeMode.light,
    contentWidget: Align(
      alignment: Alignment.topLeft,
      child: ToolItemSwt(key: key, value: _item(style)),
    ),
  ));
  await tester.pump();
  return tester.getSize(find.byKey(key));
}

VFont _font(int height) => VFont()
  ..fontData = [
    VFontData()
      ..name = 'System'
      ..height = height
      ..style = SWT.NORMAL
  ];

Future<void> _pumpBar(WidgetTester tester,
    {required int style, required int fontHeight, required double width, required double height}) async {
  await tester.pumpWidget(EvolveApp(
    theme: ThemeMode.light,
    contentWidget: Align(
      alignment: Alignment.topLeft,
      child: SizedBox(
        width: width,
        height: height,
        child: ToolBarSwt(
          value: VToolBar()
            ..id = 1
            ..style = SWT.HORIZONTAL | SWT.FLAT | SWT.RIGHT
            ..enabled = true
            ..visible = true
            ..font = _font(fontHeight)
            ..items = [_item(style)]
            ..bounds = (VRectangle()
              ..x = 0
              ..y = 0
              ..width = width.ceil()
              ..height = height.ceil()),
        ),
      ),
    ),
  ));
  await tester.runAsync(() => Future<void>.delayed(const Duration(milliseconds: 50)));
  for (var i = 0; i < 4; i++) {
    await tester.pump(const Duration(milliseconds: 50));
  }
}

/// The size the label reaches the screen at: its style's size times any scale above it.
double _drawnFontSize(WidgetTester tester) {
  final paragraph = tester.renderObject<RenderParagraph>(find.text(_label));
  final scale = tester.getRect(find.text(_label)).height / paragraph.size.height;
  return paragraph.text.style!.fontSize! * scale;
}

void main() {
  setUp(() => setConfigFlags(ConfigFlags()..use_swt_fonts = false));
  tearDown(resetConfigFlags);

  for (final name in _styles.keys) {
    testWidgets('$name text draws at the theme size in the bar ToolItemSizes measures', (tester) async {
      final measured = await _measuredItem(tester, _styles[name]!);
      for (final fontHeight in [8, 30]) {
        await _pumpBar(tester, style: _styles[name]!, fontHeight: fontHeight, width: 400, height: 80);
        final themeSize = Theme.of(tester.element(find.text(_label)))
            .extension<ToolItemThemeExtension>()!
            .fontStyle!
            .fontSize!;
        expect(_drawnFontSize(tester), moreOrLessEquals(themeSize, epsilon: 0.1),
            reason: 'roomy bar, ToolBar font $fontHeight');

        await _pumpBar(tester,
            style: _styles[name]!,
            fontHeight: fontHeight,
            width: measured.width.ceilToDouble(),
            height: measured.height.ceilToDouble());
        expect(_drawnFontSize(tester), moreOrLessEquals(themeSize, epsilon: 0.1),
            reason: 'measured bar, ToolBar font $fontHeight');
      }
    });
  }
}
