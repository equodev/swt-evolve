import 'package:flutter/material.dart';
import 'package:theme_tailor_annotation/theme_tailor_annotation.dart';
import 'package:json_annotation/json_annotation.dart';
import 'json_converters.dart';

part 'display_theme_extension.tailor.dart';
part 'display_theme_extension.g.dart';

@TailorMixin()
@JsonSerializable(explicitToJson: true)
@ColorConverter()
@DurationConverter()
@TextStyleConverter()
class DisplayThemeExtension extends ThemeExtension<DisplayThemeExtension>
    with _$DisplayThemeExtensionTailorMixin {
  final Color titleBarColor;
  final Color titleBarHoverColor;
  final Color titleBarTextColor;
  final double titleBarHeight;
  final double toolWindowTitleBarHeight;
  final double titleBarBorderRadius;

  final Color closeButtonColor;
  final Color closeButtonHoverColor;
  final Color closeButtonHoverIconColor;
  final Color minimizeButtonColor;
  final Color minimizeButtonHoverColor;
  final Color maximizeButtonColor;
  final Color maximizeButtonHoverColor;
  final double titleBarButtonSize;
  final double titleBarButtonBorderRadius;
  final Duration titleBarButtonAnimationDuration;

  final Color dialogBorderColor;
  final double dialogBorderWidth;
  final double dialogBorderRadius;
  final Color dialogShadowColor;
  final double dialogShadowBlurRadius;
  final double dialogShadowSpreadRadius;
  final double dialogShadowOffsetX;
  final double dialogShadowOffsetY;
  final Color dialogBackgroundColor;
  final Color tooltipShellBackgroundColor;

  final Color modalOverlayColor;

  final TextStyle? titleTextStyle;
  final TextStyle? toolWindowTitleTextStyle;

  const DisplayThemeExtension({
    required this.titleBarColor,
    required this.titleBarHoverColor,
    required this.titleBarTextColor,
    required this.titleBarHeight,
    required this.toolWindowTitleBarHeight,
    required this.titleBarBorderRadius,
    required this.closeButtonColor,
    required this.closeButtonHoverColor,
    required this.closeButtonHoverIconColor,
    required this.minimizeButtonColor,
    required this.minimizeButtonHoverColor,
    required this.maximizeButtonColor,
    required this.maximizeButtonHoverColor,
    required this.titleBarButtonSize,
    required this.titleBarButtonBorderRadius,
    required this.titleBarButtonAnimationDuration,
    required this.dialogBorderColor,
    required this.dialogBorderWidth,
    required this.dialogBorderRadius,
    required this.dialogShadowColor,
    required this.dialogShadowBlurRadius,
    required this.dialogShadowSpreadRadius,
    required this.dialogShadowOffsetX,
    required this.dialogShadowOffsetY,
    required this.dialogBackgroundColor,
    required this.tooltipShellBackgroundColor,
    required this.modalOverlayColor,
    this.titleTextStyle,
    this.toolWindowTitleTextStyle,
  });

  factory DisplayThemeExtension.fromJson(Map<String, dynamic> json) =>
      _$DisplayThemeExtensionFromJson(json);

  Map<String, dynamic> toJson() => _$DisplayThemeExtensionToJson(this);
}
