import 'package:flutter/material.dart';
import 'package:theme_tailor_annotation/theme_tailor_annotation.dart';
import 'package:json_annotation/json_annotation.dart';
import 'json_converters.dart';

part 'tooltip_theme_extension.tailor.dart';
part 'tooltip_theme_extension.g.dart';

@TailorMixin()
@JsonSerializable(explicitToJson: true)
@ColorConverter()
@DurationConverter()
@TextStyleConverter()
class TooltipThemeExtension extends ThemeExtension<TooltipThemeExtension>
    with _$TooltipThemeExtensionTailorMixin {
  final Color backgroundColor;
  final double borderRadius;
  final TextStyle? messageTextStyle;
  final Duration waitDuration;

  const TooltipThemeExtension({
    required this.backgroundColor,
    required this.borderRadius,
    this.messageTextStyle,
    required this.waitDuration,
  });

  factory TooltipThemeExtension.fromJson(Map<String, dynamic> json) =>
      _$TooltipThemeExtensionFromJson(json);

  Map<String, dynamic> toJson() => _$TooltipThemeExtensionToJson(this);
}
