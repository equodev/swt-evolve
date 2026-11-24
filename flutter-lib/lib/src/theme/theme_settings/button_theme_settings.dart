import 'dart:math';

import 'package:flutter/material.dart';
import '../theme_extensions/button_theme_extension.dart';
import '../theme_extensions/color_scheme_extension.dart';
import '../../gen/button.dart';
import '../../gen/control.dart';
import '../../gen/color.dart';
import '../../gen/font.dart';
import '../../impl/color_utils.dart';
import '../../impl/utils/widget_utils.dart';
import '../../impl/widget_config.dart';

ButtonThemeExtension getButtonLightTheme({
  required ColorScheme colorScheme,
  required TextTheme textTheme,
  required ColorSchemeExtension colorSchemeExtension,
}) {
  return _getButtonTheme(
    isDark: false,
    colorScheme: colorScheme,
    textTheme: textTheme,
    colorSchemeExtension: colorSchemeExtension,
  );
}

ButtonThemeExtension getButtonDarkTheme({
  required ColorScheme colorScheme,
  required TextTheme textTheme,
  required ColorSchemeExtension colorSchemeExtension,
}) {
  return _getButtonTheme(
    isDark: true,
    colorScheme: colorScheme,
    textTheme: textTheme,
    colorSchemeExtension: colorSchemeExtension,
  );
}

ButtonThemeExtension _getButtonTheme({
  required bool isDark,
  required ColorScheme colorScheme,
  required TextTheme textTheme,
  required ColorSchemeExtension colorSchemeExtension,
}) {
  // A dark theme's outline is too dim to show a check box or radio that is not selected.
  final indicatorBorderColor = isDark
      ? boundaryOn(colorScheme.outline, colorScheme.surface)
      : colorScheme.outline;

  return ButtonThemeExtension(
    // Timing
    buttonPressDelay: const Duration(milliseconds: 150),
    enableTapAnimation: true,
    
    // InkWell colors
    splashColor: colorSchemeExtension.stateDefaultEnabled,
    highlightColor: colorSchemeExtension.stateDefaultEnabled,

    controlBackgroundColor: colorScheme.surface,

    pushButtonColor: colorScheme.primary,
    pushButtonTextColor: colorScheme.onPrimary,
    pushButtonHoverColor: colorSchemeExtension.primaryHovered,
    pushButtonPressedColor: colorScheme.primaryContainer,
    pushButtonDisabledColor: colorSchemeExtension.primaryVariantDisabled,
    pushButtonBorderColor: colorScheme.primary,

    secondaryButtonColor: colorScheme.secondary,
    secondaryButtonHoverColor: colorScheme.secondaryContainer,
    secondaryButtonPressedColor: colorSchemeExtension.secondaryPressed,
    secondaryButtonTextColor: colorScheme.onSecondary,
    secondaryButtonBorderColor: colorScheme.secondary,
    
    // Selectable Button colors
    selectableButtonColor: colorScheme.primary,
    
    // Toggle Button colors
    toggleButtonColor: colorScheme.primaryContainer,
    toggleButtonBorderColor: colorScheme.outline,
    
    // Radio Button colors
    radioButtonSelectedColor: colorScheme.primary,
    radioButtonHoverColor: colorScheme.surfaceVariant,
    radioButtonSelectedHoverColor: Color.lerp(colorScheme.primary, colorScheme.onSurface, 0.2) ?? colorScheme.primaryContainer,
    radioButtonBorderColor: indicatorBorderColor,
    radioButtonTextColor: textTheme.bodyMedium?.color ?? colorScheme.onSurface,
    
    // Dropdown Button colors
    dropdownButtonColor: colorScheme.surface,
    dropdownButtonTextColor: textTheme.bodyMedium?.color ?? colorScheme.onSurface,
    dropdownButtonHoverColor: colorScheme.surfaceVariant,
    dropdownButtonBorderColor: colorScheme.outline,
    dropdownButtonIconColor: colorScheme.onSurfaceVariant,
    
    // Checkbox colors
    checkboxColor: colorScheme.surface,
    checkboxSelectedColor: colorScheme.primary,
    checkboxBorderColor: indicatorBorderColor,
    checkboxCheckmarkColor: colorScheme.onPrimary,
    checkboxTextColor: textTheme.bodyMedium?.color ?? colorScheme.onSurface,
    checkboxHoverColor: colorScheme.surfaceVariant,
    
    // Border radius
    pushButtonBorderRadius: 4.0,
    radioButtonBorderRadius: 16.8,
    dropdownButtonBorderRadius: 4.0,
    checkboxBorderRadius: 2.0,
    checkboxGrayedBorderRadius: 1.0,
    
    // Border width
    pushButtonBorderWidth: 1.0,
    radioButtonBorderWidth: 2.0,
    radioButtonSelectedBorderWidth: 6.0,
    dropdownButtonBorderWidth: 1.0,
    checkboxBorderWidth: 2.0,
    
    pushButtonFontStyle: textTheme.bodyMedium,
    radioButtonFontStyle: textTheme.bodyMedium,
    dropdownButtonFontStyle: textTheme.bodyMedium,
    checkboxFontStyle: textTheme.bodyMedium,
    
    // Radio Button sizes
    radioButtonSize: 16.8,
    radioButtonInnerSize: 8.0,
    radioButtonTextSpacing: 8.0,
    
    // Dropdown Button sizes
    // With the 1px border on each side, an ARROW button is Sizes.SCROLL_BAR_SIZE (17), a scroll bar arrow as natively.
    dropdownButtonIconSize: 15.0,
    
    // Checkbox sizes
    checkboxSize: 16.8,
    checkboxCheckmarkSizeMultiplier: 0.7,
    checkboxGrayedMarginMultiplier: 0.17,
    
    // Common sizes
    imageTextSpacing: 8.0,

    // Push Button padding
    pushButtonPadding: const EdgeInsets.only(left: 8.0, right: 8.0, top: 3.0, bottom: 5.0),

    // Disabled colors
    disabledBackgroundColor: colorSchemeExtension.primaryVariantDisabled,
    disabledForegroundColor: colorScheme.onSurfaceVariant,
    focusRingColor: focusIndicators ? colorSchemeExtension.surfaceBorderFocused : colorSchemeExtension.stateDefaultEnabled,
    focusRingWidth: 2.0,

    minTextContrast: kMinTextContrast,
  );
}


Color getButtonBackgroundColor(
  VButton state,
  ButtonThemeExtension widgetTheme,
  Color? defaultThemeColor, {
  bool? enabled,
  bool? isPrimary,
}) {
  final isEnabled = enabled ?? (state.enabled ?? true);
  final isPrimaryButton = isPrimary ?? (state.primary ?? false);
  
  if (!isEnabled) {
    return widgetTheme.pushButtonDisabledColor;
  }
  
  Color finalDefaultColor = defaultThemeColor ?? (isPrimaryButton
      ? widgetTheme.pushButtonColor
      : widgetTheme.secondaryButtonColor);
  
  // Note: SWT background color is applied to the container behind the button,
  // not to the button itself. The button colors remain from the theme.
  
  return finalDefaultColor;
}

Color getPushButtonHoverBackgroundColor(
  VButton state,
  ButtonThemeExtension widgetTheme, {
  bool? enabled,
  bool? isPrimary,
}) {
  final isEnabled = enabled ?? (state.enabled ?? true);
  final isPrimaryButton = isPrimary ?? (state.primary ?? false);
  
  if (!isEnabled) {
    return widgetTheme.pushButtonDisabledColor;
  }
  
  final defaultColor = isPrimaryButton
      ? widgetTheme.pushButtonHoverColor
      : widgetTheme.secondaryButtonHoverColor;
  
  return getButtonBackgroundColor(
    state,
    widgetTheme,
    defaultColor,
    enabled: isEnabled,
    isPrimary: isPrimaryButton,
  );
}

Color getPushButtonBorderColor(
  VButton state,
  ButtonThemeExtension widgetTheme, {
  bool? isPrimary,
  bool? enabled,
  Color? primaryBorderDisabled,
  Color? secondaryBorderDisabled,
}) {
  final isPrimaryButton = isPrimary ?? (state.primary ?? false);
  final isEnabled = enabled ?? (state.enabled ?? true);
  if (!isEnabled) {
    if (isPrimaryButton && primaryBorderDisabled != null) {
      return primaryBorderDisabled;
    }
    if (!isPrimaryButton && secondaryBorderDisabled != null) {
      return secondaryBorderDisabled;
    }
  }
  return isPrimaryButton
      ? widgetTheme.pushButtonBorderColor
      : widgetTheme.secondaryButtonBorderColor;
}

Color getPushButtonPressedColor(ButtonThemeExtension widgetTheme, bool isPrimary) {
  return isPrimary
      ? widgetTheme.pushButtonPressedColor
      : widgetTheme.secondaryButtonPressedColor;
}

Color getPushButtonTextColor(
  ButtonThemeExtension widgetTheme,
  bool isPrimary,
  bool enabled,
) {
  if (!enabled) return widgetTheme.disabledForegroundColor;
  return isPrimary
      ? widgetTheme.pushButtonTextColor
      : widgetTheme.secondaryButtonTextColor;
}

/// The share of its glyph box a Material keyboard_arrow chevron inks: 7.41 of 24 units in the
/// direction it points, 12 of 24 across it.
const _chevronInkDepth = 7.41 / 24;
const _chevronInkSpan = 12 / 24;

/// The size of an arrow button's chevron icon. As native SWT, the arrow grows with the button, its
/// long side half the button's shorter side; it is never drawn below the theme's size, which is the
/// smallest that reads, unless its ink would not fit inside the border.
double getArrowIconSize(VButton state, ButtonThemeExtension widgetTheme, {required bool horizontal}) {
  if (!hasBounds(state.bounds)) return widgetTheme.dropdownButtonIconSize;
  final width = state.bounds!.width.toDouble();
  final height = state.bounds!.height.toDouble();
  final border = widgetTheme.pushButtonBorderWidth;
  final scaled = min(width, height) / 2 / _chevronInkSpan;
  final fitWidth = max(0.0, width - 2 * border) / (horizontal ? _chevronInkDepth : _chevronInkSpan);
  final fitHeight = max(0.0, height - 2 * border) / (horizontal ? _chevronInkSpan : _chevronInkDepth);
  return min(max(widgetTheme.dropdownButtonIconSize, scaled), min(fitWidth, fitHeight));
}

/// An arrow button's corner radius: within a quarter of its shorter side, so that a narrow button
/// still reads as a rectangle rather than a pill.
double getArrowButtonRadius(VButton state, ButtonThemeExtension widgetTheme) {
  if (!hasBounds(state.bounds)) return widgetTheme.pushButtonBorderRadius;
  final shortSide = min(state.bounds!.width, state.bounds!.height).toDouble();
  return min(widgetTheme.pushButtonBorderRadius, shortSide / 4);
}

double getCheckboxSize(
  VButton state,
  ButtonThemeExtension widgetTheme, {
  double? textLineHeight,
  bool? hasText,
  bool? hasImage,
}) {
  if (hasBounds(state.bounds)) {
    // Use minimum of width/height to make a square
    final minDimension = state.bounds!.width < state.bounds!.height
        ? state.bounds!.width.toDouble()
        : state.bounds!.height.toDouble();
    return min(minDimension, widgetTheme.checkboxSize);
  }
  
  return widgetTheme.checkboxSize;
}

double getRadioButtonSize(
  VButton state,
  ButtonThemeExtension widgetTheme, {
  double? textLineHeight,
  bool? hasText,
  bool? hasImage,
}) {
  if (hasBounds(state.bounds)) {
    final minDimension = state.bounds!.width < state.bounds!.height
        ? state.bounds!.width.toDouble()
        : state.bounds!.height.toDouble();
    return min(minDimension, widgetTheme.radioButtonSize);
  }

  return widgetTheme.radioButtonSize;
}

/// The ring painted over a focused button; no border while unfocused or when the theme sets no ring colour.
BoxDecoration getButtonFocusRingDecoration(ButtonThemeExtension theme, {required bool focused}) {
  final ring = theme.focusRingColor;
  return BoxDecoration(
    border: focused && ring.a > 0 ? Border.all(color: ring, width: theme.focusRingWidth) : null,
    borderRadius: BorderRadius.circular(theme.pushButtonBorderRadius),
  );
}
