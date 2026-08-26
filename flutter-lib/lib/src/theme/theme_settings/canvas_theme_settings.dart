import 'package:flutter/material.dart';
import '../theme_extensions/canvas_theme_extension.dart';
import '../theme_extensions/color_scheme_extension.dart';

CanvasThemeExtension getCanvasLightTheme({
  required ColorScheme colorScheme,
  required TextTheme textTheme,
  required ColorSchemeExtension colorSchemeExtension,
}) {
  return _getCanvasTheme(
    isDark: false,
    colorScheme: colorScheme,
    textTheme: textTheme,
    colorSchemeExtension: colorSchemeExtension,
  );
}

CanvasThemeExtension getCanvasDarkTheme({
  required ColorScheme colorScheme,
  required TextTheme textTheme,
  required ColorSchemeExtension colorSchemeExtension,
}) {
  return _getCanvasTheme(
    isDark: true,
    colorScheme: colorScheme,
    textTheme: textTheme,
    colorSchemeExtension: colorSchemeExtension,
  );
}

CanvasThemeExtension _getCanvasTheme({
  required bool isDark,
  required ColorScheme colorScheme,
  required TextTheme textTheme,
  required ColorSchemeExtension colorSchemeExtension,
}) {
  final surface = isDark ? colorScheme.surface : colorSchemeExtension.neutral;

  return CanvasThemeExtension(
    defaultWidth: 64.0,
    defaultHeight: 64.0,

    // Colors
    backgroundColor: surface,
    foregroundColor: colorScheme.onSurface,

    // What a GC paints with. Several slots share an origin today; they exist so the theme can
    // separate them later without the drawing side changing.
    fillColor: surface,
    textBackgroundColor: surface,
    // An outline is a deliberate mark, so it takes the accent: at the text color it would read
    // as one more glyph.
    strokeColor: colorScheme.primary,
    textColor: colorScheme.onSurface,
    pointColor: colorScheme.onSurface,
    // Softer than the text — at the text's tone, a dense grid of lines reads as heavy.
    lineColor: colorScheme.outline,
    focusColor: colorScheme.primary,
    // Starts on the accent container rather than inside the surface family, whose whole range is
    // ~7 points of lightness: an application asking for a gradient wants the ramp seen.
    gradientStartColor: colorScheme.primaryContainer,
    gradientEndColor: surface,
    patternStartColor: colorScheme.primaryContainer,
    patternEndColor: surface,
    imageTintColor: colorScheme.onSurface,
    // Big enough for a 2x icon, far below any real picture; 4/255 admits a grey the encoder
    // rounded unevenly without admitting a desaturated color.
    glyphTintMaxSide: 64,
    glyphTintChannelTolerance: 4,
  );
}
