// A theme switch recolours a CTabItem's icon to the new text colour: the icon already shown stays
// in the tab until the recoloured one is ready, never an empty gap in its place.

import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/src/impl/config_flags.dart';
import 'package:swtflutter/src/impl/utils/image_utils.dart';
import 'package:swtflutter/src/impl/widget_config.dart';

import 'support/ctab_folder_fixtures.dart';
import 'support/icon_renders.dart';

// Icons follow the text colour, so a theme switch recolours them.
ConfigFlags _flags(String theme) => ConfigFlags()
  ..force_theme = theme
  ..preserve_icon_colors = false;

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
    await tester.pumpWidget(ctabFolderWithIcon(png, tabText: 'Recipes'));
    await settleIconRenders(tester);
    expect(ctabIcon, findsOneWidget, reason: 'the icon is drawn under the first theme');
    final firstThemeRender = shownIconRender(tester, ctabItem);

    applyConfigFlags(_flags('dark'));
    // Every frame of the switch's animation, before any recoloured render could have landed.
    for (var frame = 0; frame < 8; frame++) {
      await tester.pump(const Duration(milliseconds: 40));
      expect(ctabIcon, findsOneWidget,
          reason: 'frame $frame of the theme switch lost the icon already shown');
    }

    await settleIconRenders(tester);
    expect(ctabIcon, findsOneWidget, reason: 'the icon is drawn under the new theme');
    expect(shownIconRender(tester, ctabItem), isNot(same(firstThemeRender)),
        reason: 'the icon still shows its render for the first theme');
  });
}
