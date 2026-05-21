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
@EdgeInsetsConverter()
class TooltipThemeExtension extends ThemeExtension<TooltipThemeExtension>
    with _$TooltipThemeExtensionTailorMixin {
  final Duration waitDuration;
  final Duration fadeInDuration;
  final Duration fadeOutDuration;
  final double slideOffsetY;

  final Color backgroundColor;
  final Color informationBackgroundColor;
  final Color warningBackgroundColor;
  final Color errorBackgroundColor;

  final Color borderColor;
  final double borderWidth;
  final double borderRadius;
  final double balloonBorderRadius;

  final Color textColor;
  final TextStyle? titleTextStyle;
  final TextStyle? messageTextStyle;
  final int messageMaxLines;
  final double titleMessageSpacing;

  final Color informationIconColor;
  final Color warningIconColor;
  final Color errorIconColor;
  final double iconSize;
  final double iconSpacing;

  final EdgeInsets padding;
  final double minWidth;
  final double maxWidth;
  final double minHeight;

  final Color shadowColor;
  final double shadowBlurRadius;
  final double shadowOffsetY;

  const TooltipThemeExtension({
    required this.waitDuration,
    required this.fadeInDuration,
    required this.fadeOutDuration,
    required this.slideOffsetY,
    required this.backgroundColor,
    required this.informationBackgroundColor,
    required this.warningBackgroundColor,
    required this.errorBackgroundColor,
    required this.borderColor,
    required this.borderWidth,
    required this.borderRadius,
    required this.balloonBorderRadius,
    required this.textColor,
    this.titleTextStyle,
    this.messageTextStyle,
    required this.messageMaxLines,
    required this.titleMessageSpacing,
    required this.informationIconColor,
    required this.warningIconColor,
    required this.errorIconColor,
    required this.iconSize,
    required this.iconSpacing,
    required this.padding,
    required this.minWidth,
    required this.maxWidth,
    required this.minHeight,
    required this.shadowColor,
    required this.shadowBlurRadius,
    required this.shadowOffsetY,
  });

  factory TooltipThemeExtension.fromJson(Map<String, dynamic> json) =>
      _$TooltipThemeExtensionFromJson(json);

  Map<String, dynamic> toJson() => _$TooltipThemeExtensionToJson(this);
}
