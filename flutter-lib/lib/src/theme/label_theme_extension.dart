import 'package:flutter/material.dart';

class LabelThemeExtension extends ThemeExtension<LabelThemeExtension> {
  // Text colors
  final Color primaryTextColor;
  final Color secondaryTextColor;
  final Color errorTextColor;
  final Color warningTextColor;
  final Color successTextColor;
  final Color disabledTextColor;
  final Color linkTextColor;
  final Color linkHoverTextColor;
  
  // Background colors
  final Color? backgroundColor;
  final Color? hoverBackgroundColor;
  final Color? selectedBackgroundColor;
  
  // Border colors
  final Color? borderColor;
  final Color? focusBorderColor;
  
  // Typography properties
  final double fontSize;
  final double smallFontSize;
  final double largeFontSize;
  final FontWeight fontWeight;
  final FontWeight boldFontWeight;
  final String? fontFamily;
  final double letterSpacing;
  final double lineHeight;
  
  // Complete TextStyle properties
  final TextStyle primaryTextStyle;
  final TextStyle secondaryTextStyle;
  final TextStyle errorTextStyle;
  final TextStyle warningTextStyle;
  final TextStyle successTextStyle;
  final TextStyle disabledTextStyle;
  final TextStyle linkTextStyle;
  
  // Dimensions
  final double minHeight;
  final double minWidth;
  final double maxWidth;
  
  // Spacing and padding
  final EdgeInsets padding;
  final EdgeInsets margin;
  final double horizontalPadding;
  final double verticalPadding;
  final double iconTextSpacing;
  
  // Border properties
  final double borderRadius;
  final double borderWidth;
  final double focusBorderWidth;
  
  // Icon properties
  final double iconSize;
  final double smallIconSize;
  final double largeIconSize;
  
  // Interactive properties
  final bool isSelectable;
  final bool showTooltip;
  final Duration hoverAnimationDuration;
  final Curve hoverAnimationCurve;
  
  // Alignment and positioning
  final TextAlign textAlign;
  final MainAxisAlignment mainAxisAlignment;
  final CrossAxisAlignment crossAxisAlignment;
  
  // Decoration properties
  final TextDecoration textDecoration;
  final TextDecoration hoverTextDecoration;
  final double decorationThickness;

  const LabelThemeExtension({
    required this.primaryTextColor,
    required this.secondaryTextColor,
    required this.errorTextColor,
    required this.warningTextColor,
    required this.successTextColor,
    required this.disabledTextColor,
    required this.linkTextColor,
    required this.linkHoverTextColor,
    this.backgroundColor,
    this.hoverBackgroundColor,
    this.selectedBackgroundColor,
    this.borderColor,
    this.focusBorderColor,
    required this.fontSize,
    required this.smallFontSize,
    required this.largeFontSize,
    required this.fontWeight,
    required this.boldFontWeight,
    this.fontFamily,
    required this.letterSpacing,
    required this.lineHeight,
    required this.primaryTextStyle,
    required this.secondaryTextStyle,
    required this.errorTextStyle,
    required this.warningTextStyle,
    required this.successTextStyle,
    required this.disabledTextStyle,
    required this.linkTextStyle,
    required this.minHeight,
    required this.minWidth,
    required this.maxWidth,
    required this.padding,
    required this.margin,
    required this.horizontalPadding,
    required this.verticalPadding,
    required this.iconTextSpacing,
    required this.borderRadius,
    required this.borderWidth,
    required this.focusBorderWidth,
    required this.iconSize,
    required this.smallIconSize,
    required this.largeIconSize,
    required this.isSelectable,
    required this.showTooltip,
    required this.hoverAnimationDuration,
    required this.hoverAnimationCurve,
    required this.textAlign,
    required this.mainAxisAlignment,
    required this.crossAxisAlignment,
    required this.textDecoration,
    required this.hoverTextDecoration,
    required this.decorationThickness,
  });

  @override
  LabelThemeExtension copyWith({
    Color? primaryTextColor,
    Color? secondaryTextColor,
    Color? errorTextColor,
    Color? warningTextColor,
    Color? successTextColor,
    Color? disabledTextColor,
    Color? linkTextColor,
    Color? linkHoverTextColor,
    Color? backgroundColor,
    Color? hoverBackgroundColor,
    Color? selectedBackgroundColor,
    Color? borderColor,
    Color? focusBorderColor,
    double? fontSize,
    double? smallFontSize,
    double? largeFontSize,
    FontWeight? fontWeight,
    FontWeight? boldFontWeight,
    String? fontFamily,
    double? letterSpacing,
    double? lineHeight,
    TextStyle? primaryTextStyle,
    TextStyle? secondaryTextStyle,
    TextStyle? errorTextStyle,
    TextStyle? warningTextStyle,
    TextStyle? successTextStyle,
    TextStyle? disabledTextStyle,
    TextStyle? linkTextStyle,
    double? minHeight,
    double? minWidth,
    double? maxWidth,
    EdgeInsets? padding,
    EdgeInsets? margin,
    double? horizontalPadding,
    double? verticalPadding,
    double? iconTextSpacing,
    double? borderRadius,
    double? borderWidth,
    double? focusBorderWidth,
    double? iconSize,
    double? smallIconSize,
    double? largeIconSize,
    bool? isSelectable,
    bool? showTooltip,
    Duration? hoverAnimationDuration,
    Curve? hoverAnimationCurve,
    TextAlign? textAlign,
    MainAxisAlignment? mainAxisAlignment,
    CrossAxisAlignment? crossAxisAlignment,
    TextDecoration? textDecoration,
    TextDecoration? hoverTextDecoration,
    double? decorationThickness,
  }) {
    return LabelThemeExtension(
      primaryTextColor: primaryTextColor ?? this.primaryTextColor,
      secondaryTextColor: secondaryTextColor ?? this.secondaryTextColor,
      errorTextColor: errorTextColor ?? this.errorTextColor,
      warningTextColor: warningTextColor ?? this.warningTextColor,
      successTextColor: successTextColor ?? this.successTextColor,
      disabledTextColor: disabledTextColor ?? this.disabledTextColor,
      linkTextColor: linkTextColor ?? this.linkTextColor,
      linkHoverTextColor: linkHoverTextColor ?? this.linkHoverTextColor,
      backgroundColor: backgroundColor ?? this.backgroundColor,
      hoverBackgroundColor: hoverBackgroundColor ?? this.hoverBackgroundColor,
      selectedBackgroundColor: selectedBackgroundColor ?? this.selectedBackgroundColor,
      borderColor: borderColor ?? this.borderColor,
      focusBorderColor: focusBorderColor ?? this.focusBorderColor,
      fontSize: fontSize ?? this.fontSize,
      smallFontSize: smallFontSize ?? this.smallFontSize,
      largeFontSize: largeFontSize ?? this.largeFontSize,
      fontWeight: fontWeight ?? this.fontWeight,
      boldFontWeight: boldFontWeight ?? this.boldFontWeight,
      fontFamily: fontFamily ?? this.fontFamily,
      letterSpacing: letterSpacing ?? this.letterSpacing,
      lineHeight: lineHeight ?? this.lineHeight,
      primaryTextStyle: primaryTextStyle ?? this.primaryTextStyle,
      secondaryTextStyle: secondaryTextStyle ?? this.secondaryTextStyle,
      errorTextStyle: errorTextStyle ?? this.errorTextStyle,
      warningTextStyle: warningTextStyle ?? this.warningTextStyle,
      successTextStyle: successTextStyle ?? this.successTextStyle,
      disabledTextStyle: disabledTextStyle ?? this.disabledTextStyle,
      linkTextStyle: linkTextStyle ?? this.linkTextStyle,
      minHeight: minHeight ?? this.minHeight,
      minWidth: minWidth ?? this.minWidth,
      maxWidth: maxWidth ?? this.maxWidth,
      padding: padding ?? this.padding,
      margin: margin ?? this.margin,
      horizontalPadding: horizontalPadding ?? this.horizontalPadding,
      verticalPadding: verticalPadding ?? this.verticalPadding,
      iconTextSpacing: iconTextSpacing ?? this.iconTextSpacing,
      borderRadius: borderRadius ?? this.borderRadius,
      borderWidth: borderWidth ?? this.borderWidth,
      focusBorderWidth: focusBorderWidth ?? this.focusBorderWidth,
      iconSize: iconSize ?? this.iconSize,
      smallIconSize: smallIconSize ?? this.smallIconSize,
      largeIconSize: largeIconSize ?? this.largeIconSize,
      isSelectable: isSelectable ?? this.isSelectable,
      showTooltip: showTooltip ?? this.showTooltip,
      hoverAnimationDuration: hoverAnimationDuration ?? this.hoverAnimationDuration,
      hoverAnimationCurve: hoverAnimationCurve ?? this.hoverAnimationCurve,
      textAlign: textAlign ?? this.textAlign,
      mainAxisAlignment: mainAxisAlignment ?? this.mainAxisAlignment,
      crossAxisAlignment: crossAxisAlignment ?? this.crossAxisAlignment,
      textDecoration: textDecoration ?? this.textDecoration,
      hoverTextDecoration: hoverTextDecoration ?? this.hoverTextDecoration,
      decorationThickness: decorationThickness ?? this.decorationThickness,
    );
  }

  @override
  LabelThemeExtension lerp(LabelThemeExtension? other, double t) {
    if (other is! LabelThemeExtension) {
      return this;
    }
    return LabelThemeExtension(
      primaryTextColor: Color.lerp(primaryTextColor, other.primaryTextColor, t)!,
      secondaryTextColor: Color.lerp(secondaryTextColor, other.secondaryTextColor, t)!,
      errorTextColor: Color.lerp(errorTextColor, other.errorTextColor, t)!,
      warningTextColor: Color.lerp(warningTextColor, other.warningTextColor, t)!,
      successTextColor: Color.lerp(successTextColor, other.successTextColor, t)!,
      disabledTextColor: Color.lerp(disabledTextColor, other.disabledTextColor, t)!,
      linkTextColor: Color.lerp(linkTextColor, other.linkTextColor, t)!,
      linkHoverTextColor: Color.lerp(linkHoverTextColor, other.linkHoverTextColor, t)!,
      backgroundColor: Color.lerp(backgroundColor, other.backgroundColor, t),
      hoverBackgroundColor: Color.lerp(hoverBackgroundColor, other.hoverBackgroundColor, t),
      selectedBackgroundColor: Color.lerp(selectedBackgroundColor, other.selectedBackgroundColor, t),
      borderColor: Color.lerp(borderColor, other.borderColor, t),
      focusBorderColor: Color.lerp(focusBorderColor, other.focusBorderColor, t),
      fontSize: lerpDouble(fontSize, other.fontSize, t)!,
      smallFontSize: lerpDouble(smallFontSize, other.smallFontSize, t)!,
      largeFontSize: lerpDouble(largeFontSize, other.largeFontSize, t)!,
      fontWeight: FontWeight.lerp(fontWeight, other.fontWeight, t)!,
      boldFontWeight: FontWeight.lerp(boldFontWeight, other.boldFontWeight, t)!,
      fontFamily: t < 0.5 ? fontFamily : other.fontFamily,
      letterSpacing: lerpDouble(letterSpacing, other.letterSpacing, t)!,
      lineHeight: lerpDouble(lineHeight, other.lineHeight, t)!,
      primaryTextStyle: TextStyle.lerp(primaryTextStyle, other.primaryTextStyle, t)!,
      secondaryTextStyle: TextStyle.lerp(secondaryTextStyle, other.secondaryTextStyle, t)!,
      errorTextStyle: TextStyle.lerp(errorTextStyle, other.errorTextStyle, t)!,
      warningTextStyle: TextStyle.lerp(warningTextStyle, other.warningTextStyle, t)!,
      successTextStyle: TextStyle.lerp(successTextStyle, other.successTextStyle, t)!,
      disabledTextStyle: TextStyle.lerp(disabledTextStyle, other.disabledTextStyle, t)!,
      linkTextStyle: TextStyle.lerp(linkTextStyle, other.linkTextStyle, t)!,
      minHeight: lerpDouble(minHeight, other.minHeight, t)!,
      minWidth: lerpDouble(minWidth, other.minWidth, t)!,
      maxWidth: lerpDouble(maxWidth, other.maxWidth, t)!,
      padding: EdgeInsets.lerp(padding, other.padding, t)!,
      margin: EdgeInsets.lerp(margin, other.margin, t)!,
      horizontalPadding: lerpDouble(horizontalPadding, other.horizontalPadding, t)!,
      verticalPadding: lerpDouble(verticalPadding, other.verticalPadding, t)!,
      iconTextSpacing: lerpDouble(iconTextSpacing, other.iconTextSpacing, t)!,
      borderRadius: lerpDouble(borderRadius, other.borderRadius, t)!,
      borderWidth: lerpDouble(borderWidth, other.borderWidth, t)!,
      focusBorderWidth: lerpDouble(focusBorderWidth, other.focusBorderWidth, t)!,
      iconSize: lerpDouble(iconSize, other.iconSize, t)!,
      smallIconSize: lerpDouble(smallIconSize, other.smallIconSize, t)!,
      largeIconSize: lerpDouble(largeIconSize, other.largeIconSize, t)!,
      isSelectable: t < 0.5 ? isSelectable : other.isSelectable,
      showTooltip: t < 0.5 ? showTooltip : other.showTooltip,
      hoverAnimationDuration: lerpDuration(hoverAnimationDuration, other.hoverAnimationDuration, t),
      hoverAnimationCurve: t < 0.5 ? hoverAnimationCurve : other.hoverAnimationCurve,
      textAlign: t < 0.5 ? textAlign : other.textAlign,
      mainAxisAlignment: t < 0.5 ? mainAxisAlignment : other.mainAxisAlignment,
      crossAxisAlignment: t < 0.5 ? crossAxisAlignment : other.crossAxisAlignment,
      textDecoration: t < 0.5 ? textDecoration : other.textDecoration,
      hoverTextDecoration: t < 0.5 ? hoverTextDecoration : other.hoverTextDecoration,
      decorationThickness: lerpDouble(decorationThickness, other.decorationThickness, t)!,
    );
  }

  Duration lerpDuration(Duration a, Duration b, double t) {
    return Duration(microseconds: (a.inMicroseconds + (b.inMicroseconds - a.inMicroseconds) * t).round());
  }

  double? lerpDouble(double? a, double? b, double t) {
    if (a == null && b == null) return null;
    a ??= 0.0;
    b ??= 0.0;
    return a + (b - a) * t;
  }
}