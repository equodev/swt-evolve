import 'package:flutter/material.dart';

import '../theme_extensions/scaling_scale_theme_extension.dart';

ScalingScaleThemeExtension getScalingScaleLightTheme({
  required ColorScheme colorScheme,
}) {
  return _build(colorScheme: colorScheme);
}

ScalingScaleThemeExtension getScalingScaleDarkTheme({
  required ColorScheme colorScheme,
}) {
  return _build(colorScheme: colorScheme);
}

ScalingScaleThemeExtension _build({required ColorScheme colorScheme}) {
  return ScalingScaleThemeExtension(
    sliderWidth: 110.0,
    sliderTrackHeight: 2.0,
    thumbRadius: 5.0,
    overlayRadius: 10.0,
    chevronIconSize: 18.0,
    chevronPadding: const EdgeInsets.symmetric(horizontal: 4, vertical: 2),
    chevronIconColor: colorScheme.onSurfaceVariant,
    labelTextSize: 10.0,
    labelTextColor: colorScheme.onSurfaceVariant,
    trailingGap: 4.0,
    sliderMin: 0.5,
    sliderMax: 2.0,
    expandTooltip: 'App scale',
    collapseTooltip: 'Collapse scale',
    resetTooltip: 'Double-tap to reset to 100%',
  );
}
