import 'package:flutter/material.dart';
import 'package:theme_tailor_annotation/theme_tailor_annotation.dart';
import 'package:json_annotation/json_annotation.dart';
import 'json_converters.dart';

part 'composite_theme_extension.tailor.dart';
part 'composite_theme_extension.g.dart';

@TailorMixin()
@JsonSerializable(explicitToJson: true)
@ColorConverter()
@DurationConverter()
class CompositeThemeExtension extends ThemeExtension<CompositeThemeExtension> with _$CompositeThemeExtensionTailorMixin {
  final Color backgroundColor;
  final Color disabledBackgroundColor;
  final Color borderColor;
  final Color focusedBorderColor;
  final double borderWidth;
  final double borderRadius;
  final double contentPadding;

  final Color workbenchAreaGapColor;
  final Color panelBorderColor;
  final double panelBorderWidth;
  final double panelBorderRadius;
  final double panelChildGap;
  final Color panelShadowColor;
  final double panelShadowBlurRadius;
  final double panelShadowDx;
  final double panelShadowDy;

  const CompositeThemeExtension({
    required this.backgroundColor,
    required this.disabledBackgroundColor,
    required this.borderColor,
    required this.focusedBorderColor,
    required this.borderWidth,
    required this.borderRadius,
    required this.contentPadding,
    required this.workbenchAreaGapColor,
    required this.panelBorderColor,
    required this.panelBorderWidth,
    required this.panelBorderRadius,
    required this.panelChildGap,
    required this.panelShadowColor,
    required this.panelShadowBlurRadius,
    required this.panelShadowDx,
    required this.panelShadowDy,
  });

  factory CompositeThemeExtension.fromJson(Map<String, dynamic> json) =>
      _$CompositeThemeExtensionFromJson(json);

  Map<String, dynamic> toJson() => _$CompositeThemeExtensionToJson(this);
}
