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

  const GroupThemeExtension({
    required this.backgroundColor,
    required this.borderColor,
    required this.padding,
    required this.margin,
    required this.borderWidth,
    required this.borderRadius,
    required this.foregroundColor,
    this.textStyle,
  });

  factory GroupThemeExtension.fromJson(Map<String, dynamic> json) =>
      _$GroupThemeExtensionFromJson(json);

  Map<String, dynamic> toJson() => _$GroupThemeExtensionToJson(this);
}
