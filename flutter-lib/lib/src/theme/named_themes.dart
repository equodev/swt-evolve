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
  ),
  'yellowk': NamedTheme(
    lightColorScheme: _yellowkLightScheme(),
    darkColorScheme: _yellowkDarkScheme(),
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

ColorScheme _cursorDarkScheme() => ColorScheme.fromSeed(
  seedColor: const Color(0xFF626262),
  brightness: Brightness.dark,
).copyWith(
  primary: const Color(0xFF9A9A9A),
  onPrimary: const Color(0xFF141414),
  primaryContainer: const Color(0xFF2A2A2A),
  onPrimaryContainer: const Color(0xFFF2F2F2),
  secondary: const Color(0xFF2A2A2A),
  onSecondary: const Color(0xFFF2F2F2),
  secondaryContainer: const Color(0xFF3A3A3A),
  onSecondaryContainer: const Color(0xFFF0F0F0),
  // surface: Cursor's signature dark charcoal
  surface: const Color(0xFF1C1C1E),
  onSurface: const Color(0xFFF4F4F5), // zinc-100 — all text on dark
  onSurfaceVariant: const Color(0xFFA1A1AA), // zinc-400 — secondary text/icons
  // surfaceVariant: slightly lighter charcoal — hover/disabled
  surfaceVariant: const Color(0xFF27272A), // zinc-800
  surfaceContainerHighest: const Color(0xFF18181B),
  surfaceContainerHigh: const Color(0xFF1F1F23),
  surfaceContainerLow: const Color(0xFF1A1A1D),
  // outline: zinc grays for dark mode borders
  outline: const Color(0xFF52525B), // zinc-600
  outlineVariant: const Color(0xFF3F3F46), // zinc-700
  error: const Color(0xFFF87171), // lighter red for dark mode
  onError: const Color(0xFFFFFFFF),
  errorContainer: const Color(0xFF7F1D1D),
  onErrorContainer: const Color(0xFFFCA5A5),
  inverseSurface: const Color(0xFFF4F4F5),
  onInverseSurface: const Color(0xFF1C1C1E),
  inversePrimary: const Color(0xFF626262),
  surfaceTint: const Color(0xFF626262),
  shadow: const Color(0xFF000000),
  scrim: const Color(0xFF000000),
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
