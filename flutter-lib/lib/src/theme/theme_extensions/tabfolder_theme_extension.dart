import 'package:flutter/material.dart';
import 'package:theme_tailor_annotation/theme_tailor_annotation.dart';
import 'package:json_annotation/json_annotation.dart';
import 'json_converters.dart';

part 'tabfolder_theme_extension.tailor.dart';
part 'tabfolder_theme_extension.g.dart';

@TailorMixin()
@JsonSerializable(explicitToJson: true)
@ColorConverter()
@DurationConverter()
@TextStyleConverter()
class TabFolderThemeExtension extends ThemeExtension<TabFolderThemeExtension> with _$TabFolderThemeExtensionTailorMixin {
  // Tab bar colors
  final Color tabBarBackgroundColor;
  final Color tabBarBorderColor;
  
  // Tab colors
  final Color tabBackgroundColor;
  final Color tabSelectedBackgroundColor;
  final Color tabHoverBackgroundColor;
  final Color tabDisabledBackgroundColor;
  
  // Tab border colors
  final Color tabBorderColor;
  final Color tabSelectedBorderColor;
  final Color tabHoverBorderColor;
  final Color tabDisabledBorderColor;
  
  // Tab text colors
  final Color tabTextColor;
  final Color tabSelectedTextColor;
  final Color tabHoverTextColor;
  final Color tabDisabledTextColor;
  
  // Tab border properties
  final double tabBorderWidth;
  final double tabSelectedBorderWidth;
  final double tabBorderRadius;
  
  // Tab padding
  final double tabPadding;
  
  // Tab typography
  final TextStyle? tabTextStyle;
  final TextStyle? tabSelectedTextStyle;
  
  // Tab content area
  final Color tabContentBackgroundColor;
  final Color tabContentBorderColor;
  
  const TabFolderThemeExtension({
    required this.tabBarBackgroundColor,
    required this.tabBarBorderColor,
    required this.tabBackgroundColor,
    required this.tabSelectedBackgroundColor,
    required this.tabHoverBackgroundColor,
    required this.tabDisabledBackgroundColor,
    required this.tabBorderColor,
    required this.tabSelectedBorderColor,
    required this.tabHoverBorderColor,
    required this.tabDisabledBorderColor,
    required this.tabTextColor,
    required this.tabSelectedTextColor,
    required this.tabHoverTextColor,
    required this.tabDisabledTextColor,
    required this.tabBorderWidth,
    required this.tabSelectedBorderWidth,
    required this.tabBorderRadius,
    required this.tabPadding,
    this.tabTextStyle,
    this.tabSelectedTextStyle,
    required this.tabContentBackgroundColor,
    required this.tabContentBorderColor,
  });

  factory TabFolderThemeExtension.fromJson(Map<String, dynamic> json) =>
      _$TabFolderThemeExtensionFromJson(json);

  Map<String, dynamic> toJson() => _$TabFolderThemeExtensionToJson(this);
}

