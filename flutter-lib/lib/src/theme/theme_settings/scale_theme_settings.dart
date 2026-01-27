import 'package:flutter/material.dart';
import '../theme_extensions/scale_theme_extension.dart';
import '../theme_extensions/color_scheme_extension.dart';
import '../../gen/scale.dart';
import '../../impl/utils/widget_utils.dart';

ScaleThemeExtension getScaleLightTheme({
  required ColorScheme colorScheme,
  required TextTheme textTheme,
  required ColorSchemeExtension colorSchemeExtension,
}) {
  return _getScaleTheme(
    isDark: false,
    colorScheme: colorScheme,
    textTheme: textTheme,
    colorSchemeExtension: colorSchemeExtension,
  );
}

ScaleThemeExtension getScaleDarkTheme({
  required ColorScheme colorScheme,
  required TextTheme textTheme,
  required ColorSchemeExtension colorSchemeExtension,
}) {
  return _getScaleTheme(
    isDark: true,
    colorScheme: colorScheme,
    textTheme: textTheme,
    colorSchemeExtension: colorSchemeExtension,
  );
}

ScaleThemeExtension _getScaleTheme({
  required bool isDark,
  required ColorScheme colorScheme,
  required TextTheme textTheme,
  required ColorSchemeExtension colorSchemeExtension,
}) {
  return ScaleThemeExtension(
    // Animation
    animationDuration: const Duration(milliseconds: 150),

    // Track colors
    activeTrackColor: colorScheme.primary,
    inactiveTrackColor: colorScheme.outline,
    disabledActiveTrackColor: colorSchemeExtension.onSurfaceVariantDisabled,
    disabledInactiveTrackColor: colorScheme.outlineVariant,

    // Thumb colors
    thumbColor: colorScheme.primary,
    thumbHoverColor: colorScheme.primaryContainer,
    disabledThumbColor: colorSchemeExtension.onSurfaceVariantDisabled,

    // Tick mark colors
    tickMarkColor: colorScheme.outline,
    disabledTickMarkColor: colorScheme.outlineVariant,

    // Overlay colors
    overlayColor: Colors.transparent,

    // Track sizing
    trackHeight: 2.0,

    // Thumb sizing
    thumbRadius: 6.0,
    thumbHoverRadius: 8.0,

    // Tick mark sizing
    tickMarkHeight: 16.0,
    tickMarkWidth: 1.0,
    tickMarkCount: 11,

    // Default minimum sizes (used when no bounds provided)
    minWidth: 120.0,
    minHeight: 40.0,
    minVerticalWidth: 40.0,
    minVerticalHeight: 120.0,

    // Padding
    horizontalPadding: 24.0,

    // Default scale values
    defaultMinimum: 0,
    defaultMaximum: 100,
    defaultSelection: 0,
    defaultIncrement: 1,
    defaultPageIncrement: 10,
  );
}

// Helper to get track colors based on state
Color getScaleActiveTrackColor(
  ScaleThemeExtension widgetTheme, {
  required bool isEnabled,
}) {
  if (!isEnabled) {
    return widgetTheme.disabledActiveTrackColor;
  }
  return widgetTheme.activeTrackColor;
}

Color getScaleInactiveTrackColor(
  ScaleThemeExtension widgetTheme, {
  required bool isEnabled,
}) {
  if (!isEnabled) {
    return widgetTheme.disabledInactiveTrackColor;
  }
  return widgetTheme.inactiveTrackColor;
}

// Helper to get thumb color based on state
Color getScaleThumbColor(
  ScaleThemeExtension widgetTheme, {
  required bool isEnabled,
  required bool isHovered,
}) {
  if (!isEnabled) {
    return widgetTheme.disabledThumbColor;
  }
  if (isHovered) {
    return widgetTheme.thumbHoverColor;
  }
  return widgetTheme.thumbColor;
}

// Helper to get tick mark color based on state
Color getScaleTickMarkColor(
  ScaleThemeExtension widgetTheme, {
  required bool isEnabled,
}) {
  if (!isEnabled) {
    return widgetTheme.disabledTickMarkColor;
  }
  return widgetTheme.tickMarkColor;
}

// Helper to calculate scale size based on bounds and orientation
Size getScaleSize(
  VScale state,
  ScaleThemeExtension widgetTheme, {
  required bool isVertical,
}) {
  if (hasBounds(state.bounds)) {
    return Size(
      state.bounds!.width.toDouble(),
      state.bounds!.height.toDouble(),
    );
  }

  if (isVertical) {
    return Size(widgetTheme.minVerticalWidth, widgetTheme.minVerticalHeight);
  }
  return Size(widgetTheme.minWidth, widgetTheme.minHeight);
}

// Helper to generate tick positions
List<double> generateTickPositions(
  double min,
  double max,
  int tickCount,
) {
  if (tickCount <= 1) return [min];
  final interval = (max - min) / (tickCount - 1);
  return List<double>.generate(tickCount, (index) => min + (index * interval));
}
