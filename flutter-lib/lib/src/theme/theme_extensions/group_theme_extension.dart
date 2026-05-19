import 'package:flutter/material.dart';
import 'package:theme_tailor_annotation/theme_tailor_annotation.dart';
import 'package:json_annotation/json_annotation.dart';
import 'json_converters.dart';

part 'group_theme_extension.tailor.dart';
part 'group_theme_extension.g.dart';

@TailorMixin()
@JsonSerializable(explicitToJson: true)
@ColorConverter()
@EdgeInsetsConverter()
@TextStyleConverter()
class GroupThemeExtension extends ThemeExtension<GroupThemeExtension> with _$GroupThemeExtensionTailorMixin {

  final Color backgroundColor;
  final Color borderColor;
  final EdgeInsets padding;
  final EdgeInsets margin;
  final double borderWidth;
  final double borderRadius;
  final Color foregroundColor;
  final TextStyle? textStyle;
  final double titleHorizontalOffset;
  final double titleLabelPadding;
  final Color shadowHighlightColor;
  final Color shadowDarkColor;
  final double shadowOutOpacity;
  final double shadowOutOpacityAlt;
  final double shadowOutBlurRadius;
  final double shadowOutBlurRadiusAlt;
  final double shadowOutElevation;
  final double shadowSecondaryElevation;
  final double shadowEtchedOpacity;
  final double shadowEtchedBlurRadius;
  final double shadowInBorderFactor;
  final double shadowInBgFactor;

  const GroupThemeExtension({
    required this.backgroundColor,
    required this.borderColor,
    required this.padding,
    required this.margin,
    required this.borderWidth,
    required this.borderRadius,
    required this.foregroundColor,
    this.textStyle,
    required this.titleHorizontalOffset,
    required this.titleLabelPadding,
    required this.shadowHighlightColor,
    required this.shadowDarkColor,
    required this.shadowOutOpacity,
    required this.shadowOutOpacityAlt,
    required this.shadowOutBlurRadius,
    required this.shadowOutBlurRadiusAlt,
    required this.shadowOutElevation,
    required this.shadowSecondaryElevation,
    required this.shadowEtchedOpacity,
    required this.shadowEtchedBlurRadius,
    required this.shadowInBorderFactor,
    required this.shadowInBgFactor,
  });

  factory GroupThemeExtension.fromJson(Map<String, dynamic> json) =>
      _$GroupThemeExtensionFromJson(json);

  Map<String, dynamic> toJson() => _$GroupThemeExtensionToJson(this);
}
