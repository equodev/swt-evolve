import 'package:flutter/material.dart';
import 'package:theme_tailor_annotation/theme_tailor_annotation.dart';
import 'package:json_annotation/json_annotation.dart';
import 'json_converters.dart';

part 'ctabitem_theme_extension.tailor.dart';
part 'ctabitem_theme_extension.g.dart';

@TailorMixin()
@JsonSerializable(explicitToJson: true)
@ColorConverter()
@DurationConverter()
@TextStyleConverter()
class CTabItemThemeExtension extends ThemeExtension<CTabItemThemeExtension> with _$CTabItemThemeExtensionTailorMixin {
  // Tab item text colors
  final Color tabItemTextColor;
  final Color tabItemSelectedTextColor;
  final Color tabItemDisabledTextColor;
  
  // Tab item typography
  final TextStyle? tabItemTextStyle;
  final TextStyle? tabItemSelectedTextStyle;
  
  // Tab item padding
  final double tabItemHorizontalPadding;
  final double tabItemVerticalPadding;
  
  // Tab item image spacing
  final double tabItemImageTextSpacing;
  
  const CTabItemThemeExtension({
    required this.tabItemTextColor,
    required this.tabItemSelectedTextColor,
    required this.tabItemDisabledTextColor,
    this.tabItemTextStyle,
    this.tabItemSelectedTextStyle,
    required this.tabItemHorizontalPadding,
    required this.tabItemVerticalPadding,
    required this.tabItemImageTextSpacing,
  });

  factory CTabItemThemeExtension.fromJson(Map<String, dynamic> json) =>
      _$CTabItemThemeExtensionFromJson(json);

  Map<String, dynamic> toJson() => _$CTabItemThemeExtensionToJson(this);
}

