import 'package:flutter/material.dart';

import 'theme.dart';
import 'theme_extensions/color_scheme_extension.dart';
import 'type_scales.dart';

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

  /// The type scale this theme draws in. Null means the shared one.
  ///
  /// A *named* scale rather than a bare function, because the Java size model mirrors these sizes
  /// for `computeSize` and is keyed by the scale's name: naming it is what lets the measurement
  /// run once per distinct set of sizes instead of once per theme. Themes that want the same
  /// sizes share one scale and one measurement.
  ///
  /// The scale is applied *before* the per-widget extensions are built, because they take their
  /// fonts from the text theme — a size changed afterwards would leave every widget still
  /// measuring and drawing the old one.
  final TypeScale? typeScale;

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
    this.typeScale,
    this.focusIndicators = false,
  });

  /// The type sizes this theme wants, or [base] untouched when it does not care.
  TextTheme applyTypeSizes(TextTheme base) => typeScale?.apply(base) ?? base;

  /// The title-bar colour for [dark], or null when this theme does not specify one.
  Color? titleBarColor(bool dark) => dark ? darkTitleBarColor : lightTitleBarColor;

  ThemeData applyWidgetOverrides(ThemeData theme, {required bool dark}) =>
      (dark ? darkWidgetOverrides : lightWidgetOverrides)?.call(theme) ?? theme;

  /// The theme this draws for [dark], over the shared per-widget builders.
  ThemeData build(int? backgroundColor, {required bool dark}) {
    if (!dark) {
      return applyWidgetOverrides(
        createLightNonDefaultTheme(
          backgroundColor,
          overrideColorScheme: lightColorScheme,
          overrideColorSchemeExtension:
              lightColorSchemeExtension ?? createColorSchemeExtension(lightColorScheme),
          overrideTypeSizes: typeScale?.apply,
        ),
        dark: false,
      );
    }
    final scheme = darkColorScheme ?? lightColorScheme;
    return applyWidgetOverrides(
      createDarkNonDefaultTheme(
        backgroundColor,
        overrideColorScheme: scheme,
        overrideColorSchemeExtension:
            darkColorSchemeExtension ?? createColorSchemeExtension(scheme),
        overrideTypeSizes: typeScale?.apply,
      ),
      dark: true,
    );
  }
}

/// The name of the theme drawn when the flags name none, or one [namedThemes] does not hold. Java
/// keys its mirrored sizes by the same name.
const String kDefaultThemeName = 'standard';

/// The themes `swt.evolve.theme_name` selects, by name. An app built on this one adds its own
/// before it runs [main].
final Map<String, NamedTheme> namedThemes = {kDefaultThemeName: kStandardTheme};

/// The theme [name] selects: the one registered under it, else the default one.
NamedTheme namedThemeFor(String? name) =>
    namedThemes[name?.trim()] ?? namedThemes[kDefaultThemeName] ?? kStandardTheme;

/// Builds the light or dark theme when the flags name no theme at all; null draws the default one.
/// An app built on this one sets it.
ThemeData Function(int? backgroundColor, {required bool dark})? unnamedThemeBuilder;

/// Neutral greys with one teal accent, light and dark.
final NamedTheme kStandardTheme = NamedTheme(
  lightColorScheme: _standardLightScheme,
  darkColorScheme: _standardDarkScheme,
  lightColorSchemeExtension: createColorSchemeExtension(_standardLightScheme).copyWith(
    primaryHovered: const Color(0xFF115E59),
    surfaceToolbar: const Color(0xFFF6F7F8),
    toolbarDivider: const Color(0xFFE1E4E8),
    ctabFolderSelectedTextColor: const Color(0xFF1F2328),
    ctabFolderUnselectedColor: const Color(0xFFEEF0F2),
    neutral: const Color(0xFFF2F3F5),
  ),
  darkColorSchemeExtension: createColorSchemeExtension(_standardDarkScheme).copyWith(
    primaryHovered: const Color(0xFF5EEAD4),
    surfaceToolbar: const Color(0xFF1A1C1F),
    toolbarDivider: const Color(0xFF2C3036),
    ctabFolderSelectedTextColor: const Color(0xFFE6E8EB),
    ctabFolderUnselectedColor: const Color(0xFF1A1C1F),
    onSurfaceVariantDisabled: const Color(0xFF4B515A),
    neutral: const Color(0xFF26292E),
  ),
);

// White on the light primary is ~5.5:1 and the dark primary carries near-black text (~9:1), both
// over WCAG AA's 4.5:1. tertiary is transparent: the tertiary button is a flat, borderless one.
// outline is opaque because a scroll bar's thumb is drawn in it.
const _standardLightScheme = ColorScheme(
  brightness: Brightness.light,
  primary: Color(0xFF0F766E),
  onPrimary: Color(0xFFFFFFFF),
  primaryContainer: Color(0xFF134E4A),
  onPrimaryContainer: Color(0xFFF0FDFA),
  secondary: Color(0xFFF1F5F5),
  onSecondary: Color(0xFF134E4A),
  secondaryContainer: Color(0xFFDDEDEB),
  onSecondaryContainer: Color(0xFF134E4A),
  tertiary: Color(0x00FFFFFF),
  onTertiary: Color(0xFF134E4A),
  error: Color(0xFFDC2626),
  onError: Color(0xFFFFFFFF),
  errorContainer: Color(0xFFFEF2F2),
  onErrorContainer: Color(0xFF7F1D1D),
  surface: Color(0xFFFFFFFF),
  onSurface: Color(0xFF1F2328),
  onSurfaceVariant: Color(0xFF5B636E),
  surfaceVariant: Color(0xFFF2F3F5),
  surfaceContainerHighest: Color(0xFFFAFAFB),
  surfaceContainerHigh: Color(0xFFF6F7F8),
  surfaceContainerLow: Color(0xFFF2F3F5),
  outline: Color(0xFFC2C8CF),
  outlineVariant: Color(0x4D1F2328),
  inverseSurface: Color(0xFF2D3138),
  onInverseSurface: Color(0xFFF6F7F8),
  inversePrimary: Color(0xFF5EEAD4),
  surfaceTint: Color(0xFF0F766E),
  shadow: Color(0xFF000000),
  scrim: Color(0xFF000000),
);

const _standardDarkScheme = ColorScheme(
  brightness: Brightness.dark,
  primary: Color(0xFF2DD4BF),
  onPrimary: Color(0xFF042F2E),
  primaryContainer: Color(0xFF99F6E4),
  onPrimaryContainer: Color(0xFF042F2E),
  secondary: Color(0xFF26292E),
  onSecondary: Color(0xFF99F6E4),
  secondaryContainer: Color(0xFF2E3338),
  onSecondaryContainer: Color(0xFFCCFBF1),
  tertiary: Color(0x00FFFFFF),
  onTertiary: Color(0xFFCCFBF1),
  error: Color(0xFFF87171),
  onError: Color(0xFFFFFFFF),
  errorContainer: Color(0xFF7F1D1D),
  onErrorContainer: Color(0xFFFCA5A5),
  surface: Color(0xFF1E2023),
  onSurface: Color(0xFFE6E8EB),
  onSurfaceVariant: Color(0xFFB0B7C1),
  surfaceVariant: Color(0xFF26292E),
  surfaceContainer: Color(0xFF2B2E33),
  surfaceContainerHighest: Color(0xFF16181B),
  surfaceContainerHigh: Color(0xFF1A1C20),
  surfaceContainerLow: Color(0xFF222529),
  outline: Color(0xFF3A3F46),
  outlineVariant: Color(0xFF2C3036),
  inverseSurface: Color(0xFFE6E8EB),
  onInverseSurface: Color(0xFF1E2023),
  inversePrimary: Color(0xFF0F766E),
  surfaceTint: Color(0xFF2DD4BF),
  shadow: Color(0xFF000000),
  scrim: Color(0xFF000000),
);
