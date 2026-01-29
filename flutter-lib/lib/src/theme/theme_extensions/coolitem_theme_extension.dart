import 'package:flutter/material.dart';
import 'package:theme_tailor_annotation/theme_tailor_annotation.dart';
import 'package:json_annotation/json_annotation.dart';
import 'json_converters.dart';

part 'coolitem_theme_extension.tailor.dart';
part 'coolitem_theme_extension.g.dart';

@TailorMixin()
@JsonSerializable(explicitToJson: true)
@TextStyleConverter()
@EdgeInsetsConverter()
class CoolItemThemeExtension extends ThemeExtension<CoolItemThemeExtension>
    with _$CoolItemThemeExtensionTailorMixin {
  final TextStyle? textStyle;
  final EdgeInsets contentPadding;

  const CoolItemThemeExtension({
    this.textStyle,
    required this.contentPadding,
  });

  factory CoolItemThemeExtension.fromJson(Map<String, dynamic> json) =>
      _$CoolItemThemeExtensionFromJson(json);

  Map<String, dynamic> toJson() => _$CoolItemThemeExtensionToJson(this);
}
