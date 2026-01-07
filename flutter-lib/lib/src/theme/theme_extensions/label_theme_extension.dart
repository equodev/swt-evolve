import 'package:flutter/material.dart';
import 'package:theme_tailor_annotation/theme_tailor_annotation.dart';
import 'package:json_annotation/json_annotation.dart';
import 'json_converters.dart';

part 'label_theme_extension.tailor.dart';
part 'label_theme_extension.g.dart';

@TailorMixin()
@JsonSerializable(explicitToJson: true)
@ColorConverter()
@DurationConverter()
@TextStyleConverter()
@EdgeInsetsConverter()
@TextDecorationConverter()
@TextAlignConverter()
@MainAxisAlignmentConverter()
@CrossAxisAlignmentConverter()
class LabelThemeExtension extends ThemeExtension<LabelThemeExtension> with _$LabelThemeExtensionTailorMixin {
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
  
  // Font styles
  final TextStyle? primaryTextStyle;
  final TextStyle? secondaryTextStyle;
  final TextStyle? errorTextStyle;
  final TextStyle? warningTextStyle;
  final TextStyle? successTextStyle;
  final TextStyle? disabledTextStyle;
  final TextStyle? linkTextStyle;
  
  // Spacing and padding
  final EdgeInsets padding;
  final EdgeInsets margin;
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
  final double disabledOpacity;

  final Duration hoverAnimationDuration;
  
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
    this.primaryTextStyle,
    this.secondaryTextStyle,
    this.errorTextStyle,
    this.warningTextStyle,
    this.successTextStyle,
    this.disabledTextStyle,
    this.linkTextStyle,
    required this.padding,
    required this.margin,
    required this.iconTextSpacing,
    required this.borderRadius,
    required this.borderWidth,
    required this.focusBorderWidth,
    required this.iconSize,
    required this.smallIconSize,
    required this.largeIconSize,
    required this.isSelectable,
    required this.showTooltip,
    required this.disabledOpacity,
    required this.hoverAnimationDuration,
    required this.textAlign,
    required this.mainAxisAlignment,
    required this.crossAxisAlignment,
    required this.textDecoration,
    required this.hoverTextDecoration,
    required this.decorationThickness,
  });

  factory LabelThemeExtension.fromJson(Map<String, dynamic> json) =>
      _$LabelThemeExtensionFromJson(json);

  Map<String, dynamic> toJson() => _$LabelThemeExtensionToJson(this);
}
