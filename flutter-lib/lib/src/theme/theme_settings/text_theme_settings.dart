import 'package:flutter/material.dart';
import '../theme_extensions/text_theme_extension.dart';
import '../theme_extensions/color_scheme_extension.dart';
import '../../gen/text.dart';
import '../../gen/swt.dart';
import '../../gen/color.dart';
import '../../gen/font.dart';
import '../../impl/color_utils.dart';
import '../../impl/utils/font_utils.dart';
import '../../impl/utils/widget_utils.dart';
import '../../impl/widget_config.dart';

TextThemeExtension getTextLightTheme({
  required ColorScheme colorScheme,
  required TextTheme textTheme,
  required ColorSchemeExtension colorSchemeExtension,
}) {
  return _getTextTheme(
    isDark: false,
    colorScheme: colorScheme,
    textTheme: textTheme,
    colorSchemeExtension: colorSchemeExtension,
  );
}

TextThemeExtension getTextDarkTheme({
  required ColorScheme colorScheme,
  required TextTheme textTheme,
  required ColorSchemeExtension colorSchemeExtension,
}) {
  return _getTextTheme(
    isDark: true,
    colorScheme: colorScheme,
    textTheme: textTheme,
    colorSchemeExtension: colorSchemeExtension,
  );
}

TextThemeExtension _getTextTheme({
  required bool isDark,
  required ColorScheme colorScheme,
  required TextTheme textTheme,
  required ColorSchemeExtension colorSchemeExtension,
}) {
  final baseTextStyle = textTheme.bodyMedium ?? const TextStyle();
  
  return TextThemeExtension(
    textColor: colorSchemeExtension.labelInputDefault,
    disabledTextColor: colorSchemeExtension.labelInputDisabled,
    placeholderColor: colorSchemeExtension.surfacePlaceholder,
    helperTextColor: colorScheme.onSurfaceVariant,
    errorTextColor: colorScheme.error,
    
    backgroundColor: colorScheme.surface,
    disabledBackgroundColor: colorScheme.surfaceVariant,
    hoverBackgroundColor: colorSchemeExtension.surfaceFocused,
    focusedBackgroundColor: colorSchemeExtension.surfaceFocused,
    
    borderColor: colorSchemeExtension.surfaceBorderEnabled,
    hoverBorderColor: colorSchemeExtension.surfaceBorderHovered,
    focusedBorderColor: colorSchemeExtension.surfaceBorderFocused,
    errorBorderColor: colorScheme.error,
    disabledBorderColor: colorSchemeExtension.surfaceBorderDisabled,    
    
    // Border properties
    borderRadius: 4.0,
    borderWidth: 1.0,
    hoverBorderWidth: 2.0,
    focusedBorderWidth: 2.0,
    errorBorderWidth: 1.0,
    
    // Padding and spacing
    contentPadding: const EdgeInsets.symmetric(horizontal: 12.0, vertical: 10.0),
    
    // Typography
    fontSize: baseTextStyle.fontSize ?? 14.0,
    fontWeight: baseTextStyle.fontWeight ?? FontWeight.w400,
    fontFamily: baseTextStyle.fontFamily,
    letterSpacing: baseTextStyle.letterSpacing ?? 0.0,
    lineHeight: baseTextStyle.height ?? 1.4,
    
    // Helper text
    helperTextFontSize: 12.0,
    helperTextSpacing: 4.0,
    
    // Interactive properties
    focusAnimationDuration: const Duration(milliseconds: 200),
    focusAnimationCurve: Curves.easeInOut,
    hoverAnimationDuration: const Duration(milliseconds: 150),
    hoverAnimationCurve: Curves.easeInOut,
    
    // Password field
    passwordToggleColor: colorScheme.onSurfaceVariant,
    passwordToggleHoverColor: colorScheme.onSurface,
    passwordToggleSize: 20.0,
  );
}

TextStyle getTextFieldTextStyle(
  BuildContext context,
  VText state,
  TextThemeExtension widgetTheme,
) {
  final enabled = state.enabled ?? false;
  
  final textColor = enabled 
      ? getForegroundColor(
          foreground: state.foreground,
          defaultColor: widgetTheme.textColor,
        )
      : widgetTheme.disabledTextColor;

  final baseStyle = TextStyle(
    color: textColor,
    fontSize: widgetTheme.fontSize,
    fontWeight: widgetTheme.fontWeight,
    fontFamily: widgetTheme.fontFamily,
    letterSpacing: widgetTheme.letterSpacing,
    height: widgetTheme.lineHeight,
  );

  return getTextStyle(
    context: context,
    font: state.font,
    textColor: textColor,
    baseTextStyle: baseStyle,
  );
}

InputDecoration getInputDecoration(
  BuildContext context,
  VText state,
  TextThemeExtension widgetTheme,
  TextEditingController controller,
  Function() onClear,
) {
  final enabled = state.enabled ?? false;
  
  final defaultBgColor = enabled
      ? widgetTheme.backgroundColor
      : widgetTheme.disabledBackgroundColor;
  final bgColor = getBackgroundColor(
    background: state.background,
    defaultColor: defaultBgColor,
  ) ?? defaultBgColor;
  
  final hintColor = widgetTheme.placeholderColor;
  
  final normalBorderColor = widgetTheme.borderColor;
  final focusedBorderColor = widgetTheme.focusedBorderColor;
  final disabledBorderColor = widgetTheme.disabledBorderColor;
  final borderWidth = widgetTheme.borderWidth;
  final focusedBorderWidth = widgetTheme.focusedBorderWidth;
  final borderRadius = widgetTheme.borderRadius;

  return InputDecoration(
    hintText: state.message,
    hintStyle: TextStyle(color: hintColor),
    isDense: true,
    contentPadding: widgetTheme.contentPadding,
    border: OutlineInputBorder(
      borderRadius: BorderRadius.circular(borderRadius),
      borderSide: BorderSide(color: normalBorderColor, width: borderWidth),
    ),
    enabledBorder: OutlineInputBorder(
      borderRadius: BorderRadius.circular(borderRadius),
      borderSide: BorderSide(color: normalBorderColor, width: borderWidth),
    ),
    focusedBorder: OutlineInputBorder(
      borderRadius: BorderRadius.circular(borderRadius),
      borderSide: BorderSide(color: focusedBorderColor, width: focusedBorderWidth),
    ),
    disabledBorder: OutlineInputBorder(
      borderRadius: BorderRadius.circular(borderRadius),
      borderSide: BorderSide(color: disabledBorderColor, width: borderWidth),
    ),
    fillColor: bgColor,
    filled: true,
    counterText: ''
  );
}


