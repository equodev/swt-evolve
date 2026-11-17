// A theme switch recolours a CTabItem's icon to the new text colour: the icon already shown stays
// in the tab until the recoloured one is ready, never an empty gap in its place.

import 'dart:typed_data';

import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/gen/ctabfolder.dart';
import 'package:swtflutter/src/gen/ctabitem.dart';
import 'package:swtflutter/src/gen/image.dart';
import 'package:swtflutter/src/gen/imagedata.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/impl/config_flags.dart';
import 'package:swtflutter/src/impl/ctabfolder_evolve.dart';
import 'package:swtflutter/src/impl/utils/image_utils.dart';
import 'package:swtflutter/src/impl/widget_config.dart';

import 'support/icon_renders.dart';

// Icons follow the text colour, so a theme switch recolours them.
ConfigFlags _flags(String theme) => ConfigFlags()
  ..force_theme = theme
  ..preserve_icon_colors = false;

VCTabFolder _folder(Uint8List png) => VCTabFolder()
  ..id = 1
  ..style = SWT.NONE
  ..tabPosition = SWT.TOP
  ..selection = 0
  ..bounds = (VRectangle()
    ..x = 0
    ..y = 0
    ..width = 300
    ..height = 200)
  ..items = [
    VCTabItem()
      ..id = 2
      ..text = 'Recipes'
      ..showing = true
      ..image = (VImage()
        ..imageData = (VImageData()
          ..width = 16
          ..height = 16
          ..data = png)),
  ];

Finder get _item => find.byType(CTabItemSwt<VCTabItem>);

Finder get _icon => find.descendant(of: _item, matching: find.byType(Image));

void main() {
  setUp(() {
    resetConfigFlags();
    ImageUtils.clearCache();
  });
  tearDown(resetConfigFlags);

  testWidgets('a theme switch keeps the CTabItem icon on screen while it is recoloured',
      (tester) async {
    final png = await iconPng(tester);
    applyConfigFlags(_flags('light'));
    await tester.pumpWidget(EvolveApp(
      theme: ThemeMode.light,
      contentWidget: Align(
        alignment: Alignment.topLeft,
        child: SizedBox(
          width: 300,
          height: 200,
          child: CTabFolderSwt<VCTabFolder>(value: _folder(png)),
        ),
      ),
    ));
    await letIconRendersLand(tester);
    expect(_icon, findsOneWidget, reason: 'the icon is drawn under the first theme');
    final firstThemeRender = shownIconRender(tester, _item);

    applyConfigFlags(_flags('dark'));
    // Every frame of the switch's animation, before any recoloured render could have landed.
    for (var frame = 0; frame < 8; frame++) {
      await tester.pump(const Duration(milliseconds: 40));
      expect(_icon, findsOneWidget,
          reason: 'frame $frame of the theme switch lost the icon already shown');
    }

    await letIconRendersLand(tester);
    expect(_icon, findsOneWidget, reason: 'the icon is drawn under the new theme');
    expect(shownIconRender(tester, _item), isNot(same(firstThemeRender)),
        reason: 'the icon still shows its render for the first theme');
  });
}
