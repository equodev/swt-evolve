import 'package:flutter/material.dart';
import 'theme_extensions/color_scheme_extension.dart';
import 'theme.dart';
import 'theme_extensions/button_theme_extension.dart';
import 'theme_extensions/ccombo_theme_extension.dart';
import 'theme_extensions/combo_theme_extension.dart';
import 'theme_extensions/ctabfolder_theme_extension.dart';
import 'theme_extensions/ctabitem_theme_extension.dart';
import 'theme_extensions/menu_theme_extension.dart';
import 'theme_extensions/menuitem_theme_extension.dart';
import 'theme_extensions/scale_theme_extension.dart';
import 'theme_extensions/slider_theme_extension.dart';
import 'theme_extensions/table_theme_extension.dart';
import 'theme_extensions/tabfolder_theme_extension.dart';
import 'theme_extensions/tabitem_theme_extension.dart';
import 'theme_extensions/tree_theme_extension.dart';

class NamedTheme {
  final ColorScheme lightColorScheme;
  final ColorScheme? darkColorScheme;
  final ColorSchemeExtension? lightColorSchemeExtension;
  final ColorSchemeExtension? darkColorSchemeExtension;

  /// Colour for the Client-Side-Decorations title bar, per brightness. A theme that wants a
  /// specific title bar sets these; leaving them null derives one from the colour scheme
  /// instead (see CsdOverlayStrip). The `csd_titlebar_color` flag overrides both.
  final Color? lightTitleBarColor;
  final Color? darkTitleBarColor;

  /// Adjusts the per-widget extensions for design values no colour-scheme slot expresses.
  final ThemeData Function(ThemeData theme)? lightWidgetOverrides;
  final ThemeData Function(ThemeData theme)? darkWidgetOverrides;

  /// Whether focus rings and focus-coloured borders show when the flag is not set.
  final bool focusIndicators;

  const NamedTheme({
    required this.lightColorScheme,
    this.darkColorScheme,
    this.lightColorSchemeExtension,
    this.darkColorSchemeExtension,
    this.lightTitleBarColor,
    this.darkTitleBarColor,
    this.lightWidgetOverrides,
    this.darkWidgetOverrides,
    this.focusIndicators = false,
  });

  /// The title-bar colour for [dark], or null when this theme does not specify one.
  Color? titleBarColor(bool dark) => dark ? darkTitleBarColor : lightTitleBarColor;

  ThemeData applyWidgetOverrides(ThemeData theme, {required bool dark}) =>
      (dark ? darkWidgetOverrides : lightWidgetOverrides)?.call(theme) ?? theme;
}

final Map<String, NamedTheme> kNamedThemes = {
  'nondefault': NamedTheme(
    lightColorScheme: createLightColorScheme(),
    darkColorScheme: createDarkColorScheme(),
    lightColorSchemeExtension:
        createColorSchemeExtension(createLightColorScheme()),
    darkColorSchemeExtension:
        createColorSchemeExtension(createDarkColorScheme()),
  ),
  'hb': NamedTheme(
    lightColorScheme: _hbLightScheme(),
    darkColorScheme: _hbDarkScheme(),
    lightColorSchemeExtension: createColorSchemeExtension(_hbLightScheme()).copyWith(
      primaryHovered: const Color(0xFF74647F),
      primaryVariantDisabled: const Color(0xFFEFECF3),
      secondaryPressed: const Color(0xFFCAC5D2),
      surfaceBorderEnabled: const Color(0xFFCAC5D2),
      surfaceBorderHovered: const Color(0xFF9A91A2),
      surfaceBorderFocused: const Color(0xFFE0338A),
      surfaceBorderDisabled: const Color(0xFFE3E0E8),
      surfacePlaceholder: const Color(0xFF8B8293),
      onSurfaceVariantDisabled: const Color(0xFFA7A2AC),
      labelInputDefault: const Color(0xFF1D1C1F),
      labelInputDisabled: const Color(0xFFA7A2AC),
      surfaceToolbar: const Color(0xFFF6F5F8),
      toolbarDivider: const Color(0xFFE3E0E8),
      ctabFolderHighlightColor: const Color(0xFF5D4D68),
      ctabFolderSelectedTextColor: const Color(0xFF1D1C1F),
      ctabFolderUnselectedColor: const Color(0xFFEFECF3),
      compositePanelBorderColor: const Color(0xFFE3E0E8),
      neutral: const Color(0xFFEFECF3),
    ),
    darkColorSchemeExtension: createColorSchemeExtension(_hbDarkScheme()).copyWith(
      primaryHovered: const Color(0xFF74647F),
      primaryVariantDisabled: const Color(0x66000000),
      secondaryPressed: const Color(0xFF5A4F62),
      surfaceBorderEnabled: const Color(0xFF57515C),
      surfaceBorderHovered: const Color(0xFFA7A2AC),
      surfaceBorderFocused: const Color(0xFFFF52A8),
      surfaceBorderDisabled: const Color(0xFF323134),
      surfacePlaceholder: const Color(0xFFA7A2AC),
      onSurfaceVariantDisabled: const Color(0xFF545356),
      labelInputDefault: const Color(0xFFF6F5F8),
      labelInputDisabled: const Color(0xFF545356),
      surfaceToolbar: const Color(0xFF131214),
      toolbarDivider: const Color(0xFF342F39),
      ctabFolderHighlightColor: const Color(0xFFD9D5E1),
      ctabFolderSelectedTextColor: const Color(0xFFE3E0E8),
      ctabFolderUnselectedColor: const Color(0xFF131214),
      compositePanelBorderColor: const Color(0xFF342F39),
      neutral: const Color(0xFF323134),
    ),
    darkTitleBarColor: const Color(0xFF000000),
    lightWidgetOverrides: _hbLightWidgets,
    darkWidgetOverrides: _hbDarkWidgets,
    focusIndicators: true,
  ),
  'cursor': NamedTheme(
    lightColorScheme: _cursorLightScheme(),
    darkColorScheme: _cursorDarkScheme(),
    darkColorSchemeExtension: createColorSchemeExtension(_cursorDarkScheme()).copyWith(
      onSurfaceVariantDisabled: const Color(0xFF4A4A4A),
    ),
  ),
  'pinkFd': NamedTheme(
    lightColorScheme: _pinkFdLightScheme(),
    darkColorScheme: _pinkFdDarkScheme(),
    // primaryHovered is the one slot createColorSchemeExtension does not derive from the
    // scheme — it returns a fixed blue, which is what push buttons hover to.
    lightColorSchemeExtension: createColorSchemeExtension(_pinkFdLightScheme()).copyWith(
      primaryHovered: const Color(0xFFC72E73),
    ),
    darkColorSchemeExtension: createColorSchemeExtension(_pinkFdDarkScheme()).copyWith(
      primaryHovered: const Color(0xFFF591BC),
    ),
  ),
  'yellowk': NamedTheme(
    lightColorScheme: _yellowkLightScheme(),
    darkColorScheme: _yellowkDarkScheme(),
  ),
  'equo': NamedTheme(
    lightColorScheme: _equoLightScheme,
    darkColorScheme: _equoDarkScheme,
    lightColorSchemeExtension: createColorSchemeExtension(_equoLightScheme).copyWith(
      primaryHovered: const Color(0xFF2070E8),
      surfaceBorderFocused: const Color(0xFF3884FF),
      surfaceToolbar: const Color(0xFFF0F5FF),
      toolbarDivider: const Color(0xFFC5D9FF),
      ctabFolderHighlightColor: const Color(0xFF3884FF),
      ctabFolderSelectedTextColor: const Color(0xFF111827),
      ctabFolderUnselectedColor: const Color(0xFFE0ECFF),
      neutral: const Color(0xFFEBF2FF),
    ),
    darkColorSchemeExtension: createColorSchemeExtension(_equoDarkScheme).copyWith(
      primaryHovered: const Color(0xFF4F8AE8),
      surfaceBorderFocused: const Color(0xFF6BA3FF),
      surfaceToolbar: const Color(0xFF191D26),
      toolbarDivider: const Color(0xFF2E3D5C),
      ctabFolderHighlightColor: const Color(0xFF6BA3FF),
      ctabFolderSelectedTextColor: const Color(0xFFE8EFFE),
      ctabFolderUnselectedColor: const Color(0xFF1A2035),
      onSurfaceVariantDisabled: const Color(0xFF3E4F72),
      neutral: const Color(0xFF1A2035),
    ),
  ),
  // Reusable green theme (seed #84C145). Vendor-neutral name on purpose: any
  // green-branded POC can select it with -Dswt.evolve.theme_name=green.
  'green': NamedTheme(
    lightColorScheme: _greenLightScheme(),
    darkColorScheme: _greenDarkScheme(),
    lightColorSchemeExtension: createColorSchemeExtension(_greenLightScheme()).copyWith(
      primaryHovered: const Color(0xFF6C9E39),
      surfaceBorderFocused: const Color(0xFF84C145),
      surfaceToolbar: const Color(0xFFF3F8EA),
      toolbarDivider: const Color(0xFFCFE3AF),
      ctabFolderHighlightColor: const Color(0xFF84C145),
      ctabFolderSelectedTextColor: const Color(0xFF1B2410),
      ctabFolderUnselectedColor: const Color(0xFFE4F1CE),
      neutral: const Color(0xFFEFF6E4),
    ),
    darkColorSchemeExtension: createColorSchemeExtension(_greenDarkScheme()).copyWith(
      primaryHovered: const Color(0xFF9BD05F),
      surfaceBorderFocused: const Color(0xFF84C145),
      surfaceToolbar: const Color(0xFF1A2113),
      toolbarDivider: const Color(0xFF37481F),
      ctabFolderHighlightColor: const Color(0xFF84C145),
      ctabFolderSelectedTextColor: const Color(0xFFF0F8E6),
      ctabFolderUnselectedColor: const Color(0xFF1A2314),
      onSurfaceVariantDisabled: const Color(0xFF4E6236),
      neutral: const Color(0xFF1A2314),
    ),
  ),
};

// Dark-first muted violet; light is derived, with the focus pink darkened for 3:1 on white.
ColorScheme _hbLightScheme() => createLightColorScheme().copyWith(
  primary: const Color(0xFF5D4D68),
  onPrimary: const Color(0xFFFFFFFF),
  primaryContainer: const Color(0xFF4A3D53),
  onPrimaryContainer: const Color(0xFFFFFFFF),
  secondary: const Color(0xFFE4E0EA),
  onSecondary: const Color(0xFF3A343E),
  secondaryContainer: const Color(0xFFD9D5E1),
  onSecondaryContainer: const Color(0xFF1D1C1F),
  surface: const Color(0xFFFFFFFF),
  onSurface: const Color(0xFF1D1C1F),
  onSurfaceVariant: const Color(0xFF57515C),
  surfaceVariant: const Color(0xFFECE9F0),
  surfaceContainerLow: const Color(0xFFF6F5F8),
  surfaceContainer: const Color(0xFFF6F5F8),
  surfaceContainerHigh: const Color(0xFFEFECF3),
  surfaceContainerHighest: const Color(0xFFE5E0EB),
  outline: const Color(0xFFCAC5D2),
  outlineVariant: const Color(0xFFE3E0E8),
  error: const Color(0xFFA94F4D),
  onError: const Color(0xFFFFFFFF),
  errorContainer: const Color(0xFFF9E3E2),
  onErrorContainer: const Color(0xFF5A2220),
);

ColorScheme _hbDarkScheme() => createDarkColorScheme().copyWith(
  primary: const Color(0xFF5D4D68),
  onPrimary: const Color(0xFFE5E0EB),
  primaryContainer: const Color(0xFF80708B),
  onPrimaryContainer: const Color(0xFFFFFFFF),
  secondary: const Color(0xFF3A343E),
  onSecondary: const Color(0xFFBAB3C0),
  secondaryContainer: const Color(0xFF514659),
  onSecondaryContainer: const Color(0xFFF6F5F8),
  surface: const Color(0xFF1D1C1F),
  onSurface: const Color(0xFFF6F5F8),
  onSurfaceVariant: const Color(0xFFA7A2AC),
  surfaceVariant: const Color(0xFF433D47),
  surfaceContainerLow: const Color(0xFF131214),
  surfaceContainer: const Color(0xFF28232C),
  surfaceContainerHigh: const Color(0xFF323134),
  surfaceContainerHighest: const Color(0xFF342F39),
  outline: const Color(0xFF57515C),
  outlineVariant: const Color(0xFF323134),
  error: const Color(0xFFC16E6C),
  onError: const Color(0xFFF6F5F8),
  errorContainer: const Color(0xFF4A2A29),
  onErrorContainer: const Color(0xFFF2D4D3),
);

// Same neutral scheme as 'nondefault', with its violet accents swapped for the pinkFd brand
// pink (#E83E8C). onPrimary is near-black rather than white: white on this pink is a ~3.8:1
// contrast ratio (fails WCAG AA's 4.5:1 for text), near-black reaches ~5.5:1.
// Dark mode lifts the primary so it reads on a dark surface.
ColorScheme _pinkFdLightScheme() => createLightColorScheme().copyWith(
  primary: const Color(0xFFE83E8C),
  onPrimary: const Color(0xFF171819),
  secondary: const Color(0xFFFBE0EF),
  onSecondary: const Color(0xFF99195B),
  secondaryContainer: const Color(0xFFFBE0EF),
  onSecondaryContainer: const Color(0xFF99195B),
  onTertiary: const Color(0xFF43494E),
);

ColorScheme _pinkFdDarkScheme() => createDarkColorScheme().copyWith(
  primary: const Color(0xFFF06FA9),
  onPrimary: const Color(0xFF141414),
  secondary: const Color(0xFF8A0F48),
  onSecondary: const Color(0xFFFFFFFF),
  secondaryContainer: const Color(0xFF52092C),
  onSecondaryContainer: const Color(0xFFF8D5E4),
);

ColorScheme _cursorLightScheme() => ColorScheme.fromSeed(
  seedColor: const Color(0xFF626262),
  brightness: Brightness.light,
).copyWith(
  primary: const Color(0xFF626262),
  onPrimary: const Color(0xFFFFFFFF),
  primaryContainer: const Color(0xFF2F2F2F),
  onPrimaryContainer: const Color(0xFFF5F5F5),
  secondary: const Color(0xFFEDEDED),
  onSecondary: const Color(0xFF1F1F1F),
  secondaryContainer: const Color(0xFFD9D9D9),
  onSecondaryContainer: const Color(0xFF2A2A2A),
  tertiary: const Color(0x00FFFFFF),
  onTertiary: const Color(0xFF1C1C1E),
  error: const Color(0xFFDC0A56),
  onError: const Color(0xFFFFFFFF),
  errorContainer: const Color(0xFFFAF2F4),
  onErrorContainer: const Color(0xFF59040D),
  surface: const Color(0xFFFFFFFF),
  onSurface: const Color(0xFF18181B), // near-black with zinc tint — all body text
  onSurfaceVariant: const Color(0xFF71717A), // zinc-500 — secondary icons/text
  surfaceVariant: const Color(0xFFF2F2F2),
  surfaceContainerHighest: const Color(0xFFFAFBFC),
  surfaceContainerHigh: const Color(0xFFF7F7FA),
  surfaceContainerLow: const Color(0xFFF4F4F8),
  // outline: semi-transparent zinc — subtle borders
  outline: const Color(0x1A18181B),
  outlineVariant: const Color(0x4D18181B),
  inverseSurface: const Color(0xFF18181B), // tooltip bg
  onInverseSurface: const Color(0xFFF4F4F5), // tooltip text
  inversePrimary: const Color(0xFF626262),
  surfaceTint: const Color(0xFF626262),
  shadow: const Color(0xFF000000),
  scrim: const Color(0xFF000000),
);

ColorScheme _cursorDarkScheme() => const ColorScheme(
  brightness: Brightness.dark,
  primary: Color(0xFF767676),
  onPrimary: Color(0xFFF0F0F0),
  primaryContainer: Color(0xFF2A2A2A),
  onPrimaryContainer: Color(0xFFC0C0C0),
  secondary: Color(0xFF252525),
  onSecondary: Color(0xFF909090),
  secondaryContainer: Color(0xFF2E2E2E),
  onSecondaryContainer: Color(0xFFAAAAAA),
  tertiary: Color(0x00FFFFFF),
  onTertiary: Color(0xFFCCCCCC),
  error: Color(0xFFF87171),
  onError: Color(0xFFFFFFFF),
  errorContainer: Color(0xFF7F1D1D),
  onErrorContainer: Color(0xFFFCA5A5),
  surface: Color(0xFF1F1F1F),
  onSurface: Color(0xFFADADAD),
  onSurfaceVariant: Color(0xFF606060),
  surfaceVariant: Color(0xFF252525),
  surfaceContainerHighest: Color(0xFF141414),
  surfaceContainerHigh: Color(0xFF181818),
  surfaceContainerLow: Color(0xFF1E1E1E),
  outline: Color(0xFF383838),
  outlineVariant: Color(0xFF282828),
  shadow: Color(0xFF000000),
  scrim: Color(0xFF000000),
  inverseSurface: Color(0xFFD4D4D4),
  onInverseSurface: Color(0xFF181818),
  inversePrimary: Color(0xFF767676),
  surfaceTint: Color(0xFF767676),
);

ColorScheme _yellowkLightScheme() => ColorScheme.fromSeed(
  seedColor: const Color(0xFFFDD800),
  brightness: Brightness.light,
).copyWith(
  // Primary: brand yellow — buttons, checkboxes, radios, sliders, selected tabs, focus borders
  primary: const Color(0xFFFDD800),
  // onPrimary: MUST be black — yellow is very bright (contrast ratio ~18:1 with #171819)
  onPrimary: const Color(0xFF171819),
  // primaryContainer: brand orange — toggle unselected bg, menu item selected, table row selected (20% opacity)
  primaryContainer: const Color(0xFFF89639),
  onPrimaryContainer: const Color(0xFF1A0A00), // very dark, readable on orange
  // secondary: very light yellow tint — secondary button background
  secondary: const Color(0xFFFFF8D6),
  onSecondary: const Color(0xFF171819), // near-black on the light yellow bg
  // secondaryContainer: slightly deeper yellow — secondary button hover, combo hover
  secondaryContainer: const Color(0xFFFFEFA0),
  onSecondaryContainer: const Color(0xFF171819),
  tertiary: const Color(0x00FFFFFF),
  onTertiary: const Color(0xFF171819),
  // surface: white (as brand specifies)
  surface: const Color(0xFFFFFFFF),
  onSurface: const Color(0xFF171819), // near-black — all body text
  onSurfaceVariant: const Color(0xFF797B80), // medium gray — secondary icons, disabled text
  // surfaceVariant: very light warm yellow — hover and disabled backgrounds
  surfaceVariant: const Color(0xFFFEF9E8),
  surfaceContainerHighest: const Color(0xFFFAFBFC),
  surfaceContainerHigh: const Color(0xFFF7F8FA),
  surfaceContainerLow: const Color(0xFFF5F5F5),
  // outline: semi-transparent black — subtle borders (checkbox, radio, toggle, input, table)
  outline: const Color(0x1A171819),
  outlineVariant: const Color(0x4D171819),
  error: const Color(0xFFDC0A56),
  onError: const Color(0xFFFFFFFF),
  errorContainer: const Color(0xFFFAF2F4),
  onErrorContainer: const Color(0xFF59040D),
  inverseSurface: const Color(0xFF1F2937),  // tooltip background
  onInverseSurface: const Color(0xFFF9FAFB), // tooltip text
  inversePrimary: const Color(0xFFFDD800),
  surfaceTint: const Color(0xFFFDD800),
  shadow: const Color(0xFF000000),
  scrim: const Color(0xFF000000),
);

const _equoLightScheme = ColorScheme(
  brightness: Brightness.light,
  primary: Color(0xFF3884FF),
  onPrimary: Color(0xFFFFFFFF),
  primaryContainer: Color(0xFF1A5FCC),
  onPrimaryContainer: Color(0xFFF0F5FF),
  secondary: Color(0xFFEBF2FF),
  onSecondary: Color(0xFF0F2A6E),
  secondaryContainer: Color(0xFFD4E6FF),
  onSecondaryContainer: Color(0xFF0F2A6E),
  tertiary: Color(0x00FFFFFF),
  onTertiary: Color(0xFF0F2A6E),
  error: Color(0xFFDC0A56),
  onError: Color(0xFFFFFFFF),
  errorContainer: Color(0xFFFAF2F4),
  onErrorContainer: Color(0xFF59040D),
  surface: Color(0xFFFFFFFF),
  onSurface: Color(0xFF111827),
  onSurfaceVariant: Color(0xFF4B6093),
  surfaceVariant: Color(0xFFEBF2FF),
  surfaceContainerHighest: Color(0xFFFAFBFF),
  surfaceContainerHigh: Color(0xFFF5F8FF),
  surfaceContainerLow: Color(0xFFF0F5FF),
  outline: Color(0x1A111827),
  outlineVariant: Color(0x4D111827),
  inverseSurface: Color(0xFF1A2540),
  onInverseSurface: Color(0xFFF0F5FF),
  inversePrimary: Color(0xFF3884FF),
  surfaceTint: Color(0xFF3884FF),
  shadow: Color(0xFF000000),
  scrim: Color(0xFF000000),
);

const _equoDarkScheme = ColorScheme(
  brightness: Brightness.dark,
  primary: Color(0xFF6BA3FF),
  onPrimary: Color(0xFF001240),
  primaryContainer: Color(0xFF1A4DB3),
  onPrimaryContainer: Color(0xFFC0D8FF),
  secondary: Color(0xFF1A2545),
  onSecondary: Color(0xFF8AAFEF),
  secondaryContainer: Color(0xFF1F3058),
  onSecondaryContainer: Color(0xFFC0D8FF),
  tertiary: Color(0x00FFFFFF),
  onTertiary: Color(0xFFC0D8FF),
  error: Color(0xFFF87171),
  onError: Color(0xFFFFFFFF),
  errorContainer: Color(0xFF7F1D1D),
  onErrorContainer: Color(0xFFFCA5A5),
  surface: Color(0xFF212429),
  onSurface: Color(0xFFE8EFFE),
  onSurfaceVariant: Color(0xFF6B89C9),
  surfaceVariant: Color(0xFF1A2035),
  surfaceContainer: Color(0xFF373737),
  surfaceContainerHighest: Color(0xFF161922),
  surfaceContainerHigh: Color(0xFF1A1F2A),
  surfaceContainerLow: Color(0xFF1E2330),
  outline: Color(0xFF2E3D5C),
  outlineVariant: Color(0xFF1E2D45),
  inverseSurface: Color(0xFFD4E2FF),
  onInverseSurface: Color(0xFF212429),
  inversePrimary: Color(0xFF6BA3FF),
  surfaceTint: Color(0xFF6BA3FF),
  shadow: Color(0xFF000000),
  scrim: Color(0xFF000000),
);

ColorScheme _yellowkDarkScheme() => ColorScheme.fromSeed(
  seedColor: const Color(0xFFFDD800),
  brightness: Brightness.dark,
).copyWith(
  // Keep brand yellow in dark mode — it's naturally high-contrast on dark surfaces
  primary: const Color(0xFFFDD800),
  onPrimary: const Color(0xFF171819), // black text on yellow button
  primaryContainer: const Color(0xFFC47800), // darker orange for dark mode
  onPrimaryContainer: const Color(0xFFFFE0B2),
  secondary: const Color(0xFF3D3200), // dark yellow-tinted container
  onSecondary: const Color(0xFFFFF8D6),
  secondaryContainer: const Color(0xFF5A4A00),
  onSecondaryContainer: const Color(0xFFFFEFA0),
  surface: const Color(0xFF1F2937),
  onSurface: const Color(0xFFF9FAFB),
  onSurfaceVariant: const Color(0xFF9CA3AF),
  surfaceVariant: const Color(0xFF374151),
  outline: const Color(0xFF6B7280),
  outlineVariant: const Color(0xFF4B5563),
  error: const Color(0xFFF87171),
  onError: const Color(0xFFFFFFFF),
  errorContainer: const Color(0xFF7F1D1D),
  onErrorContainer: const Color(0xFFFCA5A5),
  inverseSurface: const Color(0xFFF9FAFB),
  onInverseSurface: const Color(0xFF1F2937),
  inversePrimary: const Color(0xFFFDD800),
  surfaceTint: const Color(0xFFFDD800),
  shadow: const Color(0xFF000000),
  scrim: const Color(0xFF000000),
);

// Seeded from the lime brand green #84C145. Like yellowk, the primary is bright,
// so onPrimary is near-black (contrast ~9.7:1) rather than white.
ColorScheme _greenLightScheme() => ColorScheme.fromSeed(
  seedColor: const Color(0xFF84C145),
  brightness: Brightness.light,
).copyWith(
  // Primary: brand green — buttons, checkboxes, radios, selected tabs, focus borders
  primary: const Color(0xFF84C145),
  onPrimary: const Color(0xFF17240A), // near-black — green is bright
  // primaryContainer: deeper green — menu item selected, table row selected
  primaryContainer: const Color(0xFF5C8730),
  onPrimaryContainer: const Color(0xFFF0F8E6),
  // secondary: very light green tint — secondary button background
  secondary: const Color(0xFFEFF6E4),
  onSecondary: const Color(0xFF2C3F14),
  secondaryContainer: const Color(0xFFDCEBC4),
  onSecondaryContainer: const Color(0xFF2C3F14),
  tertiary: const Color(0x00FFFFFF),
  onTertiary: const Color(0xFF2C3F14),
  error: const Color(0xFFDC0A56),
  onError: const Color(0xFFFFFFFF),
  errorContainer: const Color(0xFFFAF2F4),
  onErrorContainer: const Color(0xFF59040D),
  surface: const Color(0xFFFFFFFF),
  onSurface: const Color(0xFF1B2410), // near-black green-tinted — body text
  onSurfaceVariant: const Color(0xFF5E6B4C), // muted green-gray — secondary icons/text
  surfaceVariant: const Color(0xFFF3F8EA),
  surfaceContainerHighest: const Color(0xFFFAFCF6),
  surfaceContainerHigh: const Color(0xFFF6FAEF),
  surfaceContainerLow: const Color(0xFFF1F7E7),
  outline: const Color(0x1A1B2410),
  outlineVariant: const Color(0x4D1B2410),
  inverseSurface: const Color(0xFF2A3818),
  onInverseSurface: const Color(0xFFF0F8E6),
  inversePrimary: const Color(0xFF84C145),
  surfaceTint: const Color(0xFF84C145),
  shadow: const Color(0xFF000000),
  scrim: const Color(0xFF000000),
);

ColorScheme _greenDarkScheme() => ColorScheme.fromSeed(
  seedColor: const Color(0xFF84C145),
  brightness: Brightness.dark,
).copyWith(
  // Keep brand green in dark mode — high-contrast on dark surfaces
  primary: const Color(0xFF84C145),
  onPrimary: const Color(0xFF17240A), // black text on green button
  primaryContainer: const Color(0xFF4D7028),
  onPrimaryContainer: const Color(0xFFE4F3CE),
  secondary: const Color(0xFF28351A),
  onSecondary: const Color(0xFFDCEBC4),
  secondaryContainer: const Color(0xFF37481F),
  onSecondaryContainer: const Color(0xFFE4F3CE),
  tertiary: const Color(0x00FFFFFF),
  onTertiary: const Color(0xFFE4F3CE),
  error: const Color(0xFFF87171),
  onError: const Color(0xFFFFFFFF),
  errorContainer: const Color(0xFF7F1D1D),
  onErrorContainer: const Color(0xFFFCA5A5),
  surface: const Color(0xFF1E241A),
  onSurface: const Color(0xFFF0F8E6),
  onSurfaceVariant: const Color(0xFF9CB07E),
  surfaceVariant: const Color(0xFF28351A),
  surfaceContainerHighest: const Color(0xFF141A10),
  surfaceContainerHigh: const Color(0xFF191F14),
  surfaceContainerLow: const Color(0xFF1D2418),
  outline: const Color(0xFF3D4E28),
  outlineVariant: const Color(0xFF2A3818),
  inverseSurface: const Color(0xFFDDECC8),
  onInverseSurface: const Color(0xFF1E241A),
  inversePrimary: const Color(0xFF84C145),
  surfaceTint: const Color(0xFF84C145),
  shadow: const Color(0xFF000000),
  scrim: const Color(0xFF000000),
);

ThemeData _withExtensions(ThemeData theme, List<ThemeExtension<dynamic>?> replacements) {
  final merged = Map<Object, ThemeExtension<dynamic>>.of(theme.extensions);
  for (final extension in replacements) {
    if (extension != null) merged[extension.type] = extension;
  }
  return theme.copyWith(extensions: merged.values);
}

const _transparent = Color(0x00000000);

ThemeData _hbDarkWidgets(ThemeData t) {
  return _withExtensions(t, [
    t.extension<CTabFolderThemeExtension>()?.copyWith(
      tabBarBorderColor: _transparent,
      tabSelectedBackgroundColor: const Color(0xFF433D47),
      tabHoverBackgroundColor: const Color(0xFF3A343E),
    ),
    t.extension<CTabItemThemeExtension>()?.copyWith(
      tabItemTextColor: const Color(0xFFBAB3C0),
      tabItemSelectedTextColor: const Color(0xFFE3E0E8),
      tabItemDisabledTextColor: const Color(0xFF545356),
    ),
    t.extension<TabFolderThemeExtension>()?.copyWith(
      tabBarBackgroundColor: const Color(0xFF131214),
      tabBackgroundColor: const Color(0xFF131214),
      tabSelectedBackgroundColor: const Color(0xFF433D47),
      tabBarBorderColor: _transparent,
      tabBorderColor: _transparent,
      tabSelectedBorderColor: const Color(0xFFD9D5E1),
    ),
    t.extension<TabItemThemeExtension>()?.copyWith(
      textColor: const Color(0xFFBAB3C0),
      selectedTextColor: const Color(0xFFE3E0E8),
      disabledTextColor: const Color(0xFF545356),
    ),
    t.extension<TableThemeExtension>()?.copyWith(
      selectedBackgroundColor: const Color(0xFF4E4853),
      rowSelectedBorderColor: _transparent,
      alternateRowBackgroundColor: const Color(0xFF323134),
      headerBackgroundColor: const Color(0xFF433D47),
      headerTextColor: const Color(0xFFA7A2AC),
      headerBorderColor: const Color(0xFF57515C),
      rowSeparatorColor: const Color(0xFF57515C),
      linesColor: const Color(0xFF57515C),
      borderColor: const Color(0xFF57515C),
    ),
    t.extension<TreeThemeExtension>()?.copyWith(
      selectedBackgroundColor: const Color(0xFF323134),
      itemHoverBackgroundColor: const Color(0xFF262528),
      itemSelectedTextColor: const Color(0xFFF6F5F8),
    ),
    t.extension<MenuThemeExtension>()?.copyWith(
      popupBackgroundColor: const Color(0xFF131214),
      hoverBackgroundColor: const Color(0xFF3A343E),
      popupBorderColor: const Color(0xFF342F39),
    ),
    t.extension<MenuItemThemeExtension>()?.copyWith(
      hoverBackgroundColor: const Color(0xFF3A343E),
      textColor: const Color(0xFFBAB3C0),
    ),
    t.extension<ComboThemeExtension>()?.copyWith(
      hoverBackgroundColor: const Color(0xFF3A343E),
      selectedItemBackgroundColor: const Color(0xFF3A343E),
    ),
    t.extension<CComboThemeExtension>()?.copyWith(
      itemHoverBackgroundColor: const Color(0xFF3A343E),
      selectedItemBackgroundColor: const Color(0xFF3A343E),
    ),
    t.extension<ButtonThemeExtension>()?.copyWith(
      checkboxBorderColor: const Color(0xFF9A91A2),
      checkboxSelectedColor: const Color(0xFFAF9EBC),
      checkboxCheckmarkColor: const Color(0xFF1D1C1F),
      radioButtonBorderColor: const Color(0xFF9A91A2),
      radioButtonSelectedColor: const Color(0xFFAF9EBC),
      radioButtonSelectedHoverColor: const Color(0xFFADA0B8),
    ),
    // The rail is the track colour at the design's 60% opacity.
    t.extension<ScaleThemeExtension>()?.copyWith(
      activeTrackColor: const Color(0xFF9F8EAD),
      thumbColor: const Color(0xFF9F8EAD),
      thumbHoverColor: const Color(0xFF776284),
      inactiveTrackColor: const Color(0x999F8EAD),
    ),
    t.extension<SliderThemeExtension>()?.copyWith(
      activeTrackColor: const Color(0xFF9F8EAD),
      thumbColor: const Color(0xFF9F8EAD),
      inactiveTrackColor: const Color(0x999F8EAD),
    ),
  ]);
}

ThemeData _hbLightWidgets(ThemeData t) {
  return _withExtensions(t, [
    t.extension<CTabFolderThemeExtension>()?.copyWith(
      tabBarBorderColor: _transparent,
      tabSelectedBackgroundColor: const Color(0xFFFFFFFF),
      tabHoverBackgroundColor: const Color(0xFFE4E0EA),
    ),
    t.extension<CTabItemThemeExtension>()?.copyWith(
      tabItemTextColor: const Color(0xFF57515C),
      tabItemSelectedTextColor: const Color(0xFF1D1C1F),
      tabItemDisabledTextColor: const Color(0xFFA7A2AC),
    ),
    t.extension<TabFolderThemeExtension>()?.copyWith(
      tabBarBackgroundColor: const Color(0xFFF6F5F8),
      tabBackgroundColor: const Color(0xFFF6F5F8),
      tabSelectedBackgroundColor: const Color(0xFFFFFFFF),
      tabBarBorderColor: _transparent,
      tabBorderColor: _transparent,
      tabSelectedBorderColor: const Color(0xFF5D4D68),
    ),
    t.extension<TabItemThemeExtension>()?.copyWith(
      textColor: const Color(0xFF57515C),
      selectedTextColor: const Color(0xFF1D1C1F),
      disabledTextColor: const Color(0xFFA7A2AC),
    ),
    t.extension<TableThemeExtension>()?.copyWith(
      selectedBackgroundColor: const Color(0xFFE5E0EB),
      rowSelectedBorderColor: _transparent,
      alternateRowBackgroundColor: const Color(0xFFF6F5F8),
      headerBackgroundColor: const Color(0xFFEFECF3),
      headerTextColor: const Color(0xFF57515C),
      headerBorderColor: const Color(0xFFE3E0E8),
      rowSeparatorColor: const Color(0xFFE3E0E8),
      linesColor: const Color(0xFFE3E0E8),
      borderColor: const Color(0xFFE3E0E8),
    ),
    t.extension<TreeThemeExtension>()?.copyWith(
      selectedBackgroundColor: const Color(0xFFEFECF3),
      itemHoverBackgroundColor: const Color(0xFFF6F5F8),
      itemSelectedTextColor: const Color(0xFF1D1C1F),
    ),
    t.extension<MenuThemeExtension>()?.copyWith(
      popupBackgroundColor: const Color(0xFFFFFFFF),
      hoverBackgroundColor: const Color(0xFFEFECF3),
      popupBorderColor: const Color(0xFFE3E0E8),
    ),
    t.extension<MenuItemThemeExtension>()?.copyWith(
      hoverBackgroundColor: const Color(0xFFEFECF3),
      textColor: const Color(0xFF3A343E),
    ),
    t.extension<ComboThemeExtension>()?.copyWith(
      hoverBackgroundColor: const Color(0xFFEFECF3),
      selectedItemBackgroundColor: const Color(0xFFE5E0EB),
    ),
    t.extension<CComboThemeExtension>()?.copyWith(
      itemHoverBackgroundColor: const Color(0xFFEFECF3),
      selectedItemBackgroundColor: const Color(0xFFE5E0EB),
    ),
    t.extension<ButtonThemeExtension>()?.copyWith(
      checkboxBorderColor: const Color(0xFF8B8293),
      checkboxSelectedColor: const Color(0xFF5D4D68),
      checkboxCheckmarkColor: const Color(0xFFFFFFFF),
      radioButtonBorderColor: const Color(0xFF8B8293),
      radioButtonSelectedColor: const Color(0xFF5D4D68),
      radioButtonSelectedHoverColor: const Color(0xFF74647F),
    ),
    t.extension<ScaleThemeExtension>()?.copyWith(
      activeTrackColor: const Color(0xFF5D4D68),
      thumbColor: const Color(0xFF5D4D68),
      thumbHoverColor: const Color(0xFF74647F),
      inactiveTrackColor: const Color(0x995D4D68),
    ),
    t.extension<SliderThemeExtension>()?.copyWith(
      activeTrackColor: const Color(0xFF5D4D68),
      thumbColor: const Color(0xFF5D4D68),
      inactiveTrackColor: const Color(0x995D4D68),
    ),
  ]);
}
