import 'package:flutter/material.dart';
import 'package:theme_tailor_annotation/theme_tailor_annotation.dart';
import 'package:json_annotation/json_annotation.dart';
import 'json_converters.dart';

part 'menuitem_theme_extension.tailor.dart';
part 'menuitem_theme_extension.g.dart';

@TailorMixin()
@JsonSerializable(explicitToJson: true)
@ColorConverter()
@DurationConverter()
@TextStyleConverter()
@EdgeInsetsConverter()
class MenuItemThemeExtension extends ThemeExtension<MenuItemThemeExtension> with _$MenuItemThemeExtensionTailorMixin {
  // Animation
  final Duration animationDuration;

  // Background colors
  final Color backgroundColor;
  final Color hoverBackgroundColor;
  final Color selectedBackgroundColor;
  final Color disabledBackgroundColor;

  // Text colors
  final Color textColor;
  final Color disabledTextColor;
  final Color acceleratorTextColor;

  // Icon colors
  final Color iconColor;
  final Color disabledIconColor;

  // Separator colors
  final Color separatorColor;

  // Rim of the check and radio boxes a host toolkit draws in its own menus; MenuItem itself
  // draws only a mark.
  final Color checkboxBorderColor;

  // Border styling
  final double borderRadius;

  // Sizing
  final double itemHeight;
  final double minItemWidth;
  final double iconSize;
  final double separatorHeight;
  final double separatorMargin;
  final EdgeInsets itemPadding;
  final double iconTextSpacing;
  final double textAcceleratorSpacing;

  // Checkbox sizes
  final double checkboxSize;
  final double checkboxCheckmarkSize;

  // Radio button sizes
  final double radioButtonSize;
  final double radioButtonInnerSize;

  // Disabled opacity
  final double disabledOpacity;

  // Font styles
  final TextStyle? textStyle;
  final TextStyle? acceleratorTextStyle;

  const MenuItemThemeExtension({
    required this.animationDuration,
    required this.backgroundColor,
    required this.hoverBackgroundColor,
    required this.selectedBackgroundColor,
    required this.disabledBackgroundColor,
    required this.textColor,
    required this.disabledTextColor,
    required this.acceleratorTextColor,
    required this.iconColor,
    required this.disabledIconColor,
    required this.separatorColor,
    required this.checkboxBorderColor,
    required this.borderRadius,
    required this.itemHeight,
    required this.minItemWidth,
    required this.iconSize,
    required this.separatorHeight,
    required this.separatorMargin,
    required this.itemPadding,
    required this.iconTextSpacing,
    required this.textAcceleratorSpacing,
    required this.checkboxSize,
    required this.checkboxCheckmarkSize,
    required this.radioButtonSize,
    required this.radioButtonInnerSize,
    required this.disabledOpacity,
    this.textStyle,
    this.acceleratorTextStyle,
  });

  factory MenuItemThemeExtension.fromJson(Map<String, dynamic> json) =>
      _$MenuItemThemeExtensionFromJson(json);

  Map<String, dynamic> toJson() => _$MenuItemThemeExtensionToJson(this);
}
