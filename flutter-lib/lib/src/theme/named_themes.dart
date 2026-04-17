import 'package:flutter/material.dart';
import 'theme_extensions/color_scheme_extension.dart';
import 'theme.dart';

class NamedTheme {
  final ColorScheme lightColorScheme;
  final ColorScheme? darkColorScheme;
  final ColorSchemeExtension? lightColorSchemeExtension;
  final ColorSchemeExtension? darkColorSchemeExtension;

  const NamedTheme({
    required this.lightColorScheme,
    this.darkColorScheme,
    this.lightColorSchemeExtension,
    this.darkColorSchemeExtension,
  });
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
  'cursor': NamedTheme(
    lightColorScheme: _cursorLightScheme(),
    darkColorScheme: _cursorDarkScheme(),
    darkColorSchemeExtension: createColorSchemeExtension(_cursorDarkScheme()).copyWith(
      onSurfaceVariantDisabled: const Color(0xFF4A4A4A),
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
};

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
