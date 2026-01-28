import 'package:flutter/material.dart';
import 'package:theme_tailor_annotation/theme_tailor_annotation.dart';
import 'package:json_annotation/json_annotation.dart';
import 'json_converters.dart';

part 'menu_theme_extension.tailor.dart';
part 'menu_theme_extension.g.dart';

@TailorMixin()
@JsonSerializable(explicitToJson: true)
@ColorConverter()
@DurationConverter()
@TextStyleConverter()
@EdgeInsetsConverter()
class MenuThemeExtension extends ThemeExtension<MenuThemeExtension> with _$MenuThemeExtensionTailorMixin {
  // Animation
  final Duration animationDuration;

  // Background colors
  final Color backgroundColor;
  final Color menuBarBackgroundColor;
  final Color popupBackgroundColor;
  final Color hoverBackgroundColor;
  final Color disabledBackgroundColor;

  // Text colors
  final Color textColor;
  final Color disabledTextColor;

  // Border colors
  final Color borderColor;
  final Color menuBarBorderColor;

  // Border styling
  final double borderWidth;
  final double borderRadius;
  final Color disabledBorderColor;

  // Sizing
  final double menuBarHeight;
  final double minMenuWidth;
  final double maxMenuWidth;
  final EdgeInsets menuBarItemPadding;
  final EdgeInsets popupPadding;
  final EdgeInsets menuBarItemMargin;
  // Elevation
  final double popupElevation;

  // Font style
  final TextStyle? textStyle;

  const MenuThemeExtension({
    required this.animationDuration,
    required this.backgroundColor,
    required this.menuBarBackgroundColor,
    required this.popupBackgroundColor,
    required this.hoverBackgroundColor,
    required this.disabledBackgroundColor,
    required this.textColor,
    required this.disabledTextColor,
    required this.borderColor,
    required this.menuBarBorderColor,
    required this.borderWidth,
    required this.borderRadius,
    required this.disabledBorderColor,
    required this.menuBarHeight,
    required this.minMenuWidth,
    required this.maxMenuWidth,
    required this.menuBarItemPadding,
    required this.popupPadding,
    required this.popupElevation,
    required this.menuBarItemMargin,
    this.textStyle,
  });

  factory MenuThemeExtension.fromJson(Map<String, dynamic> json) =>
      _$MenuThemeExtensionFromJson(json);

  Map<String, dynamic> toJson() => _$MenuThemeExtensionToJson(this);
}
