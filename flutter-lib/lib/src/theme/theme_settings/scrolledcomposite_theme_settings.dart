import 'package:flutter/material.dart';
import '../theme_extensions/scrolledcomposite_theme_extension.dart';
import '../theme_extensions/color_scheme_extension.dart';
import '../../gen/scrolledcomposite.dart';
import '../../impl/utils/widget_utils.dart';

ScrolledCompositeThemeExtension getScrolledCompositeLightTheme({
  required ColorScheme colorScheme,
  required TextTheme textTheme,
  required ColorSchemeExtension colorSchemeExtension,
}) {
  return _getScrolledCompositeTheme(
    isDark: false,
    colorScheme: colorScheme,
    textTheme: textTheme,
    colorSchemeExtension: colorSchemeExtension,
  );
}

ScrolledCompositeThemeExtension getScrolledCompositeDarkTheme({
  required ColorScheme colorScheme,
  required TextTheme textTheme,
  required ColorSchemeExtension colorSchemeExtension,
}) {
  return _getScrolledCompositeTheme(
    isDark: true,
    colorScheme: colorScheme,
    textTheme: textTheme,
    colorSchemeExtension: colorSchemeExtension,
  );
}

ScrolledCompositeThemeExtension _getScrolledCompositeTheme({
  required bool isDark,
  required ColorScheme colorScheme,
  required TextTheme textTheme,
  required ColorSchemeExtension colorSchemeExtension,
}) {
  return ScrolledCompositeThemeExtension(
    // Animation
    animationDuration: const Duration(milliseconds: 200),

    // Background colors
    backgroundColor: colorScheme.surface,
    disabledBackgroundColor: colorScheme.surfaceVariant,

    // Scrollbar colors
    scrollbarThumbColor: colorScheme.outline,
    scrollbarThumbHoverColor: colorScheme.onSurfaceVariant,
    scrollbarTrackColor: Colors.transparent,

    // Border colors
    borderColor: colorScheme.outline,
    focusedBorderColor: colorScheme.primary,

    // Border styling
    borderWidth: 0.0,
    borderRadius: 0.0,

    // Scrollbar styling
    scrollbarThickness: 8.0,
    scrollbarRadius: 4.0,
    scrollbarMinThumbLength: 48.0,

    // Default minimum sizes
    defaultMinWidth: 0.0,
    defaultMinHeight: 0.0,

    // Padding
    contentPadding: 0.0,
  );
}

// Helper to get background color with SWT support
Color getScrolledCompositeBackgroundColor(
  VScrolledComposite state,
  ScrolledCompositeThemeExtension widgetTheme, {
  required bool isEnabled,
}) {
  if (!isEnabled) {
    return widgetTheme.disabledBackgroundColor;
  }

  return getBackgroundColor(
    background: state.background,
    defaultColor: widgetTheme.backgroundColor,
  ) ?? widgetTheme.backgroundColor;
}

// Helper to calculate size based on bounds
Size getScrolledCompositeSize(
  VScrolledComposite state,
  ScrolledCompositeThemeExtension widgetTheme,
) {
  if (hasBounds(state.bounds)) {
    return Size(
      state.bounds!.width.toDouble(),
      state.bounds!.height.toDouble(),
    );
  }
  return Size(widgetTheme.defaultMinWidth, widgetTheme.defaultMinHeight);
}

// Helper to get minimum content size
Size getScrolledCompositeMinContentSize(
  VScrolledComposite state,
  ScrolledCompositeThemeExtension widgetTheme,
) {
  final minWidth = state.minWidth?.toDouble() ?? widgetTheme.defaultMinWidth;
  final minHeight = state.minHeight?.toDouble() ?? widgetTheme.defaultMinHeight;
  return Size(minWidth, minHeight);
}

// Helper to check if scrollbars should always be visible
bool shouldAlwaysShowScrollBars(VScrolledComposite state) {
  return state.alwaysShowScrollBars ?? true;
}

// Helper to check expand flags
bool shouldExpandHorizontal(VScrolledComposite state) {
  return state.expandHorizontal ?? false;
}

bool shouldExpandVertical(VScrolledComposite state) {
  return state.expandVertical ?? false;
}
