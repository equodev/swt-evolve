import 'package:flutter/material.dart';
import 'package:swtflutter/src/impl/widget_config.dart';
import 'package:swtflutter/src/impl/config_flags.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:swtflutter/src/theme/named_themes.dart';
import 'package:swtflutter/src/theme/theme.dart';
import 'package:swtflutter/src/theme/theme_extensions/color_scheme_extension.dart';
import 'package:swtflutter/src/theme/theme_extensions/button_theme_extension.dart';
import 'package:swtflutter/src/theme/theme_extensions/canvas_theme_extension.dart';
import 'package:swtflutter/src/theme/theme_extensions/ccombo_theme_extension.dart';
import 'package:swtflutter/src/theme/theme_extensions/combo_theme_extension.dart';
import 'package:swtflutter/src/theme/theme_extensions/ctabfolder_theme_extension.dart';
import 'package:swtflutter/src/theme/theme_extensions/list_theme_extension.dart';
import 'package:swtflutter/src/theme/theme_extensions/menu_theme_extension.dart';
import 'package:swtflutter/src/theme/theme_extensions/scrolledcomposite_theme_extension.dart';
import 'package:swtflutter/src/theme/theme_extensions/spinner_theme_extension.dart';
import 'package:swtflutter/src/theme/theme_extensions/table_theme_extension.dart';
import 'package:swtflutter/src/theme/theme_extensions/tabfolder_theme_extension.dart';
import 'package:swtflutter/src/theme/theme_extensions/tabitem_theme_extension.dart';
import 'package:swtflutter/src/theme/theme_extensions/tree_theme_extension.dart';

ThemeData _built(NamedTheme named, {required bool dark}) {
  final scheme = dark ? (named.darkColorScheme ?? named.lightColorScheme) : named.lightColorScheme;
  final extension = (dark ? named.darkColorSchemeExtension : named.lightColorSchemeExtension) ??
      createColorSchemeExtension(scheme);
  return dark
      ? createDarkNonDefaultTheme(null, overrideColorScheme: scheme, overrideColorSchemeExtension: extension)
      : createLightNonDefaultTheme(null, overrideColorScheme: scheme, overrideColorSchemeExtension: extension);
}

void main() {
  group('a named theme without widget overrides', () {
    for (final entry in kNamedThemes.entries.where((e) => e.key != 'hb')) {
      for (final dark in [false, true]) {
        test('${entry.key} ${dark ? 'dark' : 'light'} keeps the theme it was built with', () {
          final theme = _built(entry.value, dark: dark);
          expect(identical(entry.value.applyWidgetOverrides(theme, dark: dark), theme), isTrue);
        });
      }
    }
  });

  group('focus visuals follow the focus_indicators flag, defaulting per theme', () {
    tearDown(resetConfigFlags);
    for (final entry in kNamedThemes.entries) {
      for (final flag in <bool?>[null, true, false]) {
        test('${entry.key}, focus_indicators=$flag', () {
          resetConfigFlags();
          setConfigFlags(ConfigFlags()
            ..theme_name = entry.key
            ..focus_indicators = flag);
          final on = flag ?? entry.value.focusIndicators;
          final theme = entry.value.applyWidgetOverrides(_built(entry.value, dark: true), dark: true);
          final ext = theme.extension<ColorSchemeExtension>()!;
          final primary = theme.colorScheme.primary;
          Color expected(Color off) => on ? ext.surfaceBorderFocused : off;
          expect(theme.extension<ButtonThemeExtension>()!.focusRingColor, expected(ext.stateDefaultEnabled));
          expect(theme.extension<CTabFolderThemeExtension>()!.tabFocusRingColor, expected(ext.stateDefaultEnabled));
          expect(theme.extension<TabFolderThemeExtension>()!.tabFocusRingColor, expected(ext.stateDefaultEnabled));
          expect(theme.extension<TreeThemeExtension>()!.itemFocusRingColor, expected(ext.stateDefaultEnabled));
          expect(theme.extension<ComboThemeExtension>()!.focusedBorderColor, expected(ext.primaryBorder));
          expect(theme.extension<CComboThemeExtension>()!.focusedBorderColor, expected(ext.primaryBorder));
          expect(theme.extension<ListThemeExtension>()!.focusedBorderColor, expected(primary));
          expect(theme.extension<SpinnerThemeExtension>()!.focusedBorderColor, expected(primary));
          expect(theme.extension<ScrolledCompositeThemeExtension>()!.focusedBorderColor, expected(primary));
          expect(theme.extension<CanvasThemeExtension>()!.focusColor, expected(primary));
        });
      }
    }
  });

  group('the other added slots do not show unless a theme sets them', () {
    for (final dark in [false, true]) {
      test(dark ? 'dark' : 'light', () {
        resetConfigFlags();
        final theme = dark ? createDarkNonDefaultTheme(null) : createLightNonDefaultTheme(null);
        expect(theme.extension<MenuThemeExtension>()!.popupBorderColor.a, 0);
        expect(theme.extension<CComboThemeExtension>()!.itemHoverBackgroundColor.a, 0);
        expect(theme.extension<ButtonThemeExtension>()!.focusRingWidth, 2.0);
        expect(theme.extension<CTabFolderThemeExtension>()!.tabFocusRingWidth, 2.0);
        expect(theme.extension<TabFolderThemeExtension>()!.tabFocusRingWidth, 2.0);
        expect(theme.extension<TreeThemeExtension>()!.itemFocusRingWidth, 2.0);
        final tabItem = theme.extension<TabItemThemeExtension>()!;
        expect(tabItem.selectedTextColor, tabItem.textColor);
      });
    }
  });

  test('hb dark carries the design colours into the widget themes', () {
    resetConfigFlags();
    setConfigFlags(ConfigFlags()..theme_name = 'hb');
    addTearDown(resetConfigFlags);
    final hb = kNamedThemes['hb']!;
    final theme = hb.applyWidgetOverrides(_built(hb, dark: true), dark: true);
    const focus = Color(0xFFFF52A8);

    final ctab = theme.extension<CTabFolderThemeExtension>()!;
    expect(ctab.tabSelectedBackgroundColor, const Color(0xFF433D47));
    expect(ctab.tabBarBorderColor.a, 0);
    expect(ctab.tabFocusRingColor, focus);
    expect(ctab.tabHighlightColor, const Color(0xFFD9D5E1));
    expect(theme.extension<TabFolderThemeExtension>()!.tabFocusRingColor, focus);
    expect(theme.extension<TabItemThemeExtension>()!.selectedTextColor, const Color(0xFFE3E0E8));
    expect(theme.extension<TableThemeExtension>()!.selectedBackgroundColor, const Color(0xFF4E4853));
    expect(theme.extension<TreeThemeExtension>()!.selectedBackgroundColor, const Color(0xFF323134));
    expect(theme.extension<TreeThemeExtension>()!.itemFocusRingColor, focus);
    expect(theme.extension<ButtonThemeExtension>()!.focusRingColor, focus);
    expect(theme.extension<ButtonThemeExtension>()!.checkboxSelectedColor, const Color(0xFFAF9EBC));
    expect(theme.extension<ListThemeExtension>()!.focusedBorderColor, focus);
    expect(theme.extension<ComboThemeExtension>()!.focusedBorderColor, focus);
    expect(theme.extension<MenuThemeExtension>()!.popupBorderColor, const Color(0xFF342F39));
    expect(theme.extension<ComboThemeExtension>()!.hoverBackgroundColor, const Color(0xFF3A343E));
    expect(theme.extension<CComboThemeExtension>()!.itemHoverBackgroundColor, const Color(0xFF3A343E));
    expect(theme.extension<CComboThemeExtension>()!.selectedItemBackgroundColor, const Color(0xFF3A343E));
  });

  test('hb light uses the darker focus pink', () {
    resetConfigFlags();
    setConfigFlags(ConfigFlags()..theme_name = 'hb');
    addTearDown(resetConfigFlags);
    final hb = kNamedThemes['hb']!;
    final theme = hb.applyWidgetOverrides(_built(hb, dark: false), dark: false);
    expect(theme.extension<ButtonThemeExtension>()!.focusRingColor, const Color(0xFFE0338A));
    expect(theme.extension<CTabFolderThemeExtension>()!.tabSelectedBackgroundColor, const Color(0xFFFFFFFF));
  });
}
