import 'package:flutter/material.dart';
import 'package:theme_tailor_annotation/theme_tailor_annotation.dart';
import 'package:json_annotation/json_annotation.dart';
import 'json_converters.dart';

part 'link_theme_extension.tailor.dart';
part 'link_theme_extension.g.dart';

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
class LinkThemeExtension extends ThemeExtension<LinkThemeExtension> with _$LinkThemeExtensionTailorMixin {
  // Text colors
  final Color textColor;
  final Color linkTextColor;
  final Color linkHoverTextColor;
  final Color disabledTextColor;

  // Background colors
  final Color? backgroundColor;
  final Color? hoverBackgroundColor;

  // Font styles
  final TextStyle? textStyle;
  final TextStyle? linkTextStyle;
  final TextStyle? disabledTextStyle;

  // Spacing and padding
  final EdgeInsets padding;

  // Interactive properties
  final double disabledOpacity;
  final Duration hoverAnimationDuration;

  // Decoration properties
  final TextDecoration linkDecoration;
  final TextDecoration linkHoverDecoration;

  // Alignment
  final TextAlign textAlign;

  const LinkThemeExtension({
    required this.textColor,
    required this.linkTextColor,
    required this.linkHoverTextColor,
    required this.disabledTextColor,
    this.backgroundColor,
    this.hoverBackgroundColor,
    this.textStyle,
    this.linkTextStyle,
    this.disabledTextStyle,
    required this.padding,
    required this.disabledOpacity,
    required this.hoverAnimationDuration,
    required this.linkDecoration,
    required this.linkHoverDecoration,
    required this.textAlign,
  });

  factory LinkThemeExtension.fromJson(Map<String, dynamic> json) =>
      _$LinkThemeExtensionFromJson(json);

  Map<String, dynamic> toJson() => _$LinkThemeExtensionToJson(this);
}
