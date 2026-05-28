import 'package:flutter/material.dart';
import 'package:theme_tailor_annotation/theme_tailor_annotation.dart';
import 'package:json_annotation/json_annotation.dart';
import 'json_converters.dart';

part 'message_box_theme_extension.tailor.dart';
part 'message_box_theme_extension.g.dart';

@TailorMixin()
@JsonSerializable(explicitToJson: true)
@ColorConverter()
@TextStyleConverter()
@EdgeInsetsConverter()
class MessageBoxThemeExtension extends ThemeExtension<MessageBoxThemeExtension>
    with _$MessageBoxThemeExtensionTailorMixin {
  final TextStyle? titleStyle;
  final TextStyle? messageStyle;
  final Color backgroundColor;
  final Color iconErrorColor;
  final Color iconWarningColor;
  final Color iconInfoColor;
  final Color iconQuestionColor;
  final double borderRadius;
  final double minWidth;
  final double maxWidth;
  final EdgeInsets padding;
  final double iconTitleSpacing;
  final double titleMessageSpacing;
  final double messageIndent;
  final double contentButtonsSpacing;
  final double buttonSpacing;

  const MessageBoxThemeExtension({
    this.titleStyle,
    this.messageStyle,
    required this.backgroundColor,
    required this.iconErrorColor,
    required this.iconWarningColor,
    required this.iconInfoColor,
    required this.iconQuestionColor,
    required this.borderRadius,
    required this.minWidth,
    required this.maxWidth,
    required this.padding,
    required this.iconTitleSpacing,
    required this.titleMessageSpacing,
    required this.messageIndent,
    required this.contentButtonsSpacing,
    required this.buttonSpacing,
  });

  factory MessageBoxThemeExtension.fromJson(Map<String, dynamic> json) =>
      _$MessageBoxThemeExtensionFromJson(json);

  Map<String, dynamic> toJson() => _$MessageBoxThemeExtensionToJson(this);
}
