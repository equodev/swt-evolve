import 'package:flutter/material.dart';
import 'package:theme_tailor_annotation/theme_tailor_annotation.dart';
import 'package:json_annotation/json_annotation.dart';
import 'json_converters.dart';

part 'styledtext_theme_extension.tailor.dart';
part 'styledtext_theme_extension.g.dart';

@TailorMixin()
@JsonSerializable(explicitToJson: true)
@ColorConverter()
class StyledTextThemeExtension
    extends ThemeExtension<StyledTextThemeExtension>
    with _$StyledTextThemeExtensionTailorMixin {
  final Color backgroundColor;
  final Color foregroundColor;

  const StyledTextThemeExtension({
    required this.backgroundColor,
    required this.foregroundColor,
  });

  factory StyledTextThemeExtension.fromJson(Map<String, dynamic> json) =>
      _$StyledTextThemeExtensionFromJson(json);

  Map<String, dynamic> toJson() => _$StyledTextThemeExtensionToJson(this);
}
