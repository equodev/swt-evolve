import 'package:flutter/material.dart';

class TextThemeExtension extends ThemeExtension<TextThemeExtension> {
  // Text colors
  final Color textColor;
  final Color disabledTextColor;
  final Color placeholderColor;
  final Color helperTextColor;
  final Color errorTextColor;
  
  // Background colors
  final Color backgroundColor;
  final Color disabledBackgroundColor;
  final Color hoverBackgroundColor;
  final Color focusedBackgroundColor;
  
  // Border colors
  final Color borderColor;
  final Color hoverBorderColor;
  final Color focusedBorderColor;
  final Color errorBorderColor;
  final Color disabledBorderColor;
  
  // Icon colors
  final Color prefixIconColor;
  final Color suffixIconColor;
  final Color disabledIconColor;
  final Color errorIconColor;
  
  // Dimensions
  final double height;
  final double minHeight;
  final double? maxHeight;
  final double? minWidth;
  final double? maxWidth;
  final bool fullWidth;
  
  // Border properties
  final double borderRadius;
  final double borderWidth;
  final double hoverBorderWidth;
  final double focusedBorderWidth;
  final double errorBorderWidth;
  
  // Padding and spacing
  final EdgeInsets contentPadding;
  final EdgeInsets prefixPadding;
  final EdgeInsets suffixPadding;
  final double prefixIconSpacing;
  final double suffixIconSpacing;
  
  // Typography
  final double fontSize;
  final FontWeight fontWeight;
  final String? fontFamily;
  final double letterSpacing;
  final double lineHeight;
  
  // Helper text
  final double helperTextFontSize;
  final double helperTextSpacing;
  
  // Icon sizes
  final double prefixIconSize;
  final double suffixIconSize;
  final double errorIconSize;
  
  // Interactive properties
  final Duration focusAnimationDuration;
  final Curve focusAnimationCurve;
  final Duration hoverAnimationDuration;
  final Curve hoverAnimationCurve;
  
  // Password field
  final Color passwordToggleColor;
  final Color passwordToggleHoverColor;
  final double passwordToggleSize;

  const TextThemeExtension({
    required this.textColor,
    required this.disabledTextColor,
    required this.placeholderColor,
    required this.helperTextColor,
    required this.errorTextColor,
    required this.backgroundColor,
    required this.disabledBackgroundColor,
    required this.hoverBackgroundColor,
    required this.focusedBackgroundColor,
    required this.borderColor,
    required this.hoverBorderColor,
    required this.focusedBorderColor,
    required this.errorBorderColor,
    required this.disabledBorderColor,
    required this.prefixIconColor,
    required this.suffixIconColor,
    required this.disabledIconColor,
    required this.errorIconColor,
    required this.height,
    required this.minHeight,
    this.maxHeight,
    this.minWidth,
    this.maxWidth,
    required this.fullWidth,
    required this.borderRadius,
    required this.borderWidth,
    required this.hoverBorderWidth,
    required this.focusedBorderWidth,
    required this.errorBorderWidth,
    required this.contentPadding,
    required this.prefixPadding,
    required this.suffixPadding,
    required this.prefixIconSpacing,
    required this.suffixIconSpacing,
    required this.fontSize,
    required this.fontWeight,
    this.fontFamily,
    required this.letterSpacing,
    required this.lineHeight,
    required this.helperTextFontSize,
    required this.helperTextSpacing,
    required this.prefixIconSize,
    required this.suffixIconSize,
    required this.errorIconSize,
    required this.focusAnimationDuration,
    required this.focusAnimationCurve,
    required this.hoverAnimationDuration,
    required this.hoverAnimationCurve,
    required this.passwordToggleColor,
    required this.passwordToggleHoverColor,
    required this.passwordToggleSize,
  });

  @override
  TextThemeExtension copyWith({
    Color? textColor,
    Color? disabledTextColor,
    Color? placeholderColor,
    Color? helperTextColor,
    Color? errorTextColor,
    Color? backgroundColor,
    Color? disabledBackgroundColor,
    Color? hoverBackgroundColor,
    Color? focusedBackgroundColor,
    Color? borderColor,
    Color? hoverBorderColor,
    Color? focusedBorderColor,
    Color? errorBorderColor,
    Color? disabledBorderColor,
    Color? prefixIconColor,
    Color? suffixIconColor,
    Color? disabledIconColor,
    Color? errorIconColor,
    double? height,
    double? minHeight,
    double? maxHeight,
    double? minWidth,
    double? maxWidth,
    bool? fullWidth,
    double? borderRadius,
    double? borderWidth,
    double? hoverBorderWidth,
    double? focusedBorderWidth,
    double? errorBorderWidth,
    EdgeInsets? contentPadding,
    EdgeInsets? prefixPadding,
    EdgeInsets? suffixPadding,
    double? prefixIconSpacing,
    double? suffixIconSpacing,
    double? fontSize,
    FontWeight? fontWeight,
    String? fontFamily,
    double? letterSpacing,
    double? lineHeight,
    double? helperTextFontSize,
    double? helperTextSpacing,
    double? prefixIconSize,
    double? suffixIconSize,
    double? errorIconSize,
    Duration? focusAnimationDuration,
    Curve? focusAnimationCurve,
    Duration? hoverAnimationDuration,
    Curve? hoverAnimationCurve,
    Color? passwordToggleColor,
    Color? passwordToggleHoverColor,
    double? passwordToggleSize,
  }) {
    return TextThemeExtension(
      textColor: textColor ?? this.textColor,
      disabledTextColor: disabledTextColor ?? this.disabledTextColor,
      placeholderColor: placeholderColor ?? this.placeholderColor,
      helperTextColor: helperTextColor ?? this.helperTextColor,
      errorTextColor: errorTextColor ?? this.errorTextColor,
      backgroundColor: backgroundColor ?? this.backgroundColor,
      disabledBackgroundColor: disabledBackgroundColor ?? this.disabledBackgroundColor,
      hoverBackgroundColor: hoverBackgroundColor ?? this.hoverBackgroundColor,
      focusedBackgroundColor: focusedBackgroundColor ?? this.focusedBackgroundColor,
      borderColor: borderColor ?? this.borderColor,
      hoverBorderColor: hoverBorderColor ?? this.hoverBorderColor,
      focusedBorderColor: focusedBorderColor ?? this.focusedBorderColor,
      errorBorderColor: errorBorderColor ?? this.errorBorderColor,
      disabledBorderColor: disabledBorderColor ?? this.disabledBorderColor,
      prefixIconColor: prefixIconColor ?? this.prefixIconColor,
      suffixIconColor: suffixIconColor ?? this.suffixIconColor,
      disabledIconColor: disabledIconColor ?? this.disabledIconColor,
      errorIconColor: errorIconColor ?? this.errorIconColor,
      height: height ?? this.height,
      minHeight: minHeight ?? this.minHeight,
      maxHeight: maxHeight ?? this.maxHeight,
      minWidth: minWidth ?? this.minWidth,
      maxWidth: maxWidth ?? this.maxWidth,
      fullWidth: fullWidth ?? this.fullWidth,
      borderRadius: borderRadius ?? this.borderRadius,
      borderWidth: borderWidth ?? this.borderWidth,
      hoverBorderWidth: hoverBorderWidth ?? this.hoverBorderWidth,
      focusedBorderWidth: focusedBorderWidth ?? this.focusedBorderWidth,
      errorBorderWidth: errorBorderWidth ?? this.errorBorderWidth,
      contentPadding: contentPadding ?? this.contentPadding,
      prefixPadding: prefixPadding ?? this.prefixPadding,
      suffixPadding: suffixPadding ?? this.suffixPadding,
      prefixIconSpacing: prefixIconSpacing ?? this.prefixIconSpacing,
      suffixIconSpacing: suffixIconSpacing ?? this.suffixIconSpacing,
      fontSize: fontSize ?? this.fontSize,
      fontWeight: fontWeight ?? this.fontWeight,
      fontFamily: fontFamily ?? this.fontFamily,
      letterSpacing: letterSpacing ?? this.letterSpacing,
      lineHeight: lineHeight ?? this.lineHeight,
      helperTextFontSize: helperTextFontSize ?? this.helperTextFontSize,
      helperTextSpacing: helperTextSpacing ?? this.helperTextSpacing,
      prefixIconSize: prefixIconSize ?? this.prefixIconSize,
      suffixIconSize: suffixIconSize ?? this.suffixIconSize,
      errorIconSize: errorIconSize ?? this.errorIconSize,
      focusAnimationDuration: focusAnimationDuration ?? this.focusAnimationDuration,
      focusAnimationCurve: focusAnimationCurve ?? this.focusAnimationCurve,
      hoverAnimationDuration: hoverAnimationDuration ?? this.hoverAnimationDuration,
      hoverAnimationCurve: hoverAnimationCurve ?? this.hoverAnimationCurve,
      passwordToggleColor: passwordToggleColor ?? this.passwordToggleColor,
      passwordToggleHoverColor: passwordToggleHoverColor ?? this.passwordToggleHoverColor,
      passwordToggleSize: passwordToggleSize ?? this.passwordToggleSize,
    );
  }

  @override
  TextThemeExtension lerp(TextThemeExtension? other, double t) {
    if (other is! TextThemeExtension) {
      return this;
    }
    
    return TextThemeExtension(
      textColor: Color.lerp(textColor, other.textColor, t)!,
      disabledTextColor: Color.lerp(disabledTextColor, other.disabledTextColor, t)!,
      placeholderColor: Color.lerp(placeholderColor, other.placeholderColor, t)!,
      helperTextColor: Color.lerp(helperTextColor, other.helperTextColor, t)!,
      errorTextColor: Color.lerp(errorTextColor, other.errorTextColor, t)!,
      backgroundColor: Color.lerp(backgroundColor, other.backgroundColor, t)!,
      disabledBackgroundColor: Color.lerp(disabledBackgroundColor, other.disabledBackgroundColor, t)!,
      hoverBackgroundColor: Color.lerp(hoverBackgroundColor, other.hoverBackgroundColor, t)!,
      focusedBackgroundColor: Color.lerp(focusedBackgroundColor, other.focusedBackgroundColor, t)!,
      borderColor: Color.lerp(borderColor, other.borderColor, t)!,
      hoverBorderColor: Color.lerp(hoverBorderColor, other.hoverBorderColor, t)!,
      focusedBorderColor: Color.lerp(focusedBorderColor, other.focusedBorderColor, t)!,
      errorBorderColor: Color.lerp(errorBorderColor, other.errorBorderColor, t)!,
      disabledBorderColor: Color.lerp(disabledBorderColor, other.disabledBorderColor, t)!,
      prefixIconColor: Color.lerp(prefixIconColor, other.prefixIconColor, t)!,
      suffixIconColor: Color.lerp(suffixIconColor, other.suffixIconColor, t)!,
      disabledIconColor: Color.lerp(disabledIconColor, other.disabledIconColor, t)!,
      errorIconColor: Color.lerp(errorIconColor, other.errorIconColor, t)!,
      height: (height * (1 - t) + other.height * t),
      minHeight: (minHeight * (1 - t) + other.minHeight * t),
      maxHeight: maxHeight != null && other.maxHeight != null 
          ? (maxHeight! * (1 - t) + other.maxHeight! * t)
          : (t < 0.5 ? maxHeight : other.maxHeight),
      minWidth: minWidth != null && other.minWidth != null
          ? (minWidth! * (1 - t) + other.minWidth! * t)
          : (t < 0.5 ? minWidth : other.minWidth),
      maxWidth: maxWidth != null && other.maxWidth != null
          ? (maxWidth! * (1 - t) + other.maxWidth! * t)
          : (t < 0.5 ? maxWidth : other.maxWidth),
      fullWidth: t < 0.5 ? fullWidth : other.fullWidth,
      borderRadius: (borderRadius * (1 - t) + other.borderRadius * t),
      borderWidth: (borderWidth * (1 - t) + other.borderWidth * t),
      hoverBorderWidth: (hoverBorderWidth * (1 - t) + other.hoverBorderWidth * t),
      focusedBorderWidth: (focusedBorderWidth * (1 - t) + other.focusedBorderWidth * t),
      errorBorderWidth: (errorBorderWidth * (1 - t) + other.errorBorderWidth * t),
      contentPadding: EdgeInsets.lerp(contentPadding, other.contentPadding, t)!,
      prefixPadding: EdgeInsets.lerp(prefixPadding, other.prefixPadding, t)!,
      suffixPadding: EdgeInsets.lerp(suffixPadding, other.suffixPadding, t)!,
      prefixIconSpacing: (prefixIconSpacing * (1 - t) + other.prefixIconSpacing * t),
      suffixIconSpacing: (suffixIconSpacing * (1 - t) + other.suffixIconSpacing * t),
      fontSize: (fontSize * (1 - t) + other.fontSize * t),
      fontWeight: FontWeight.lerp(fontWeight, other.fontWeight, t)!,
      fontFamily: t < 0.5 ? fontFamily : other.fontFamily,
      letterSpacing: (letterSpacing * (1 - t) + other.letterSpacing * t),
      lineHeight: (lineHeight * (1 - t) + other.lineHeight * t),
      helperTextFontSize: (helperTextFontSize * (1 - t) + other.helperTextFontSize * t),
      helperTextSpacing: (helperTextSpacing * (1 - t) + other.helperTextSpacing * t),
      prefixIconSize: (prefixIconSize * (1 - t) + other.prefixIconSize * t),
      suffixIconSize: (suffixIconSize * (1 - t) + other.suffixIconSize * t),
      errorIconSize: (errorIconSize * (1 - t) + other.errorIconSize * t),
      focusAnimationDuration: Duration(
        milliseconds: (focusAnimationDuration.inMilliseconds * (1 - t) + 
                     other.focusAnimationDuration.inMilliseconds * t).round(),
      ),
      focusAnimationCurve: t < 0.5 ? focusAnimationCurve : other.focusAnimationCurve,
      hoverAnimationDuration: Duration(
        milliseconds: (hoverAnimationDuration.inMilliseconds * (1 - t) + 
                     other.hoverAnimationDuration.inMilliseconds * t).round(),
      ),
      hoverAnimationCurve: t < 0.5 ? hoverAnimationCurve : other.hoverAnimationCurve,
      passwordToggleColor: Color.lerp(passwordToggleColor, other.passwordToggleColor, t)!,
      passwordToggleHoverColor: Color.lerp(passwordToggleHoverColor, other.passwordToggleHoverColor, t)!,
      passwordToggleSize: (passwordToggleSize * (1 - t) + other.passwordToggleSize * t),
    );
  }
}

