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

  // Checkbox colors
  final Color checkboxColor;
  final Color checkboxSelectedColor;
  final Color checkboxHoverColor;
  final Color checkboxBorderColor;
  final Color checkboxCheckmarkColor;

  // Radio button colors
  final Color radioButtonColor;
  final Color radioButtonSelectedColor;
  final Color radioButtonHoverColor;
  final Color radioButtonSelectedHoverColor;
  final Color radioButtonBorderColor;
  final Color radioButtonInnerColor;

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
  final double checkboxBorderRadius;
  final double checkboxBorderWidth;
  final double checkboxCheckmarkSize;

  // Radio button sizes
  final double radioButtonSize;
  final double radioButtonBorderWidth;
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
    required this.checkboxColor,
    required this.checkboxSelectedColor,
    required this.checkboxHoverColor,
    required this.checkboxBorderColor,
    required this.checkboxCheckmarkColor,
    required this.radioButtonColor,
    required this.radioButtonSelectedColor,
    required this.radioButtonHoverColor,
    required this.radioButtonSelectedHoverColor,
    required this.radioButtonBorderColor,
    required this.radioButtonInnerColor,
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
    required this.checkboxBorderRadius,
    required this.checkboxBorderWidth,
    required this.checkboxCheckmarkSize,
    required this.radioButtonSize,
    required this.radioButtonBorderWidth,
    required this.radioButtonInnerSize,
    required this.disabledOpacity,
    this.textStyle,
    this.acceleratorTextStyle,
  });

  factory MenuItemThemeExtension.fromJson(Map<String, dynamic> json) =>
      _$MenuItemThemeExtensionFromJson(json);

  Map<String, dynamic> toJson() => _$MenuItemThemeExtensionToJson(this);
}
