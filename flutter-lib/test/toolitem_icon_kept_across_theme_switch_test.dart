// A theme switch recolours a ToolItem's icon: the icon already shown stays on screen until the
// recoloured one is ready, never a loading indicator in its place.

import 'dart:typed_data';

import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/gen/image.dart';
import 'package:swtflutter/src/gen/imagedata.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/gen/toolbar.dart';
import 'package:swtflutter/src/gen/toolitem.dart';
import 'package:swtflutter/src/impl/config_flags.dart';
import 'package:swtflutter/src/impl/utils/image_utils.dart';
import 'package:swtflutter/src/impl/widget_config.dart';

import 'support/icon_renders.dart';

ConfigFlags _flags(String theme) => ConfigFlags()
  ..force_theme = theme
  ..preserve_icon_colors = true;

VToolBar _toolBar(Uint8List png) => VToolBar()
  ..id = 1
  ..style = SWT.HORIZONTAL | SWT.FLAT
  ..enabled = true
  ..visible = true
  ..items = [
    VToolItem()
      ..id = 2
      ..style = SWT.PUSH
      ..enabled = true
      ..image = (VImage()
        ..imageData = (VImageData()
          ..width = 16
          ..height = 16
          ..data = png)),
  ]
  ..bounds = (VRectangle()
    ..x = 0
    ..y = 0
    ..width = 200
    ..height = 40);

Finder get _item => find.byType(ToolItemSwt);

Finder get _icon => find.descendant(of: _item, matching: find.byType(Image));

Finder get _spinner => find.descendant(of: _item, matching: find.byType(CircularProgressIndicator));

void main() {
  setUp(() {
    resetConfigFlags();
    ImageUtils.clearCache();
  });
  tearDown(resetConfigFlags);

  testWidgets('a theme switch keeps the ToolItem icon on screen while it is recoloured',
      (tester) async {
    final png = await iconPng(tester);
    applyConfigFlags(_flags('light'));
    await tester.pumpWidget(EvolveApp(
      theme: ThemeMode.light,
      contentWidget: SizedBox(width: 200, height: 40, child: ToolBarSwt(value: _toolBar(png))),
    ));
    await settleIconRenders(tester);
    expect(_icon, findsOneWidget, reason: 'the icon is drawn under the first theme');
    final firstThemeRender = shownIconRender(tester, _item);

    // The palette's pick, as Java echoes it back on swt.evolve.properties.
    applyConfigFlags(_flags('dark'));
    // Every frame of the switch's animation, before any recoloured render could have landed.
    for (var frame = 0; frame < 8; frame++) {
      await tester.pump(const Duration(milliseconds: 40));
      expect(_spinner, findsNothing,
          reason: 'frame $frame of the theme switch shows a loading indicator for the icon');
      expect(_icon, findsOneWidget,
          reason: 'frame $frame of the theme switch lost the icon already shown');
    }

    await settleIconRenders(tester);
    expect(_spinner, findsNothing);
    expect(_icon, findsOneWidget, reason: 'the icon is drawn under the new theme');
    expect(shownIconRender(tester, _item), isNot(same(firstThemeRender)),
        reason: 'the icon still shows its render for the first theme');
  });
}
