import 'package:flutter/material.dart';
import 'package:theme_tailor_annotation/theme_tailor_annotation.dart';
import 'package:json_annotation/json_annotation.dart';
import 'json_converters.dart';

part 'toast_theme_extension.tailor.dart';
part 'toast_theme_extension.g.dart';

/// The transient card `Toasts.show` raises over the main-toolbar window. SWT has no widget for it,
/// so none of this comes from a platform: every value here is ours to choose, and an application
/// that wants a different look changes them rather than the code.
@TailorMixin()
@JsonSerializable(explicitToJson: true)
@ColorConverter()
@DurationConverter()
@TextStyleConverter()
@EdgeInsetsConverter()
class ToastThemeExtension extends ThemeExtension<ToastThemeExtension>
    with _$ToastThemeExtensionTailorMixin {
  final Color backgroundColor;
  final Color borderColor;
  final double borderWidth;
  final double borderRadius;

  final TextStyle? titleTextStyle;
  final TextStyle? messageTextStyle;
  final int messageMaxLines;
  final double titleMessageSpacing;

  /// The stripe down the leading edge, and the icon, both take the severity colour.
  final Color infoColor;
  final Color successColor;
  final Color warningColor;
  final Color errorColor;
  final double accentBarWidth;

  final double iconSize;
  final double iconSpacing;
  final Color closeIconColor;
  final double closeIconSize;

  final EdgeInsets padding;
  final double width;

  /// Distance from the corner of the window the stack is anchored to, and between two stacked
  /// toasts.
  final EdgeInsets margin;
  final double spacing;

  /// How many toasts stay on screen at once; older ones are dropped as new ones arrive rather than
  /// growing a column that eventually covers the window.
  final int maxVisible;

  final Duration slideInDuration;
  final Duration fadeOutDuration;

  /// Used when the caller passes no duration of its own.
  final Duration displayDuration;
  final double slideOffsetX;

  final Color shadowColor;
  final double shadowBlurRadius;
  final double shadowOffsetY;

  const ToastThemeExtension({
    required this.backgroundColor,
    required this.borderColor,
    required this.borderWidth,
    required this.borderRadius,
    this.titleTextStyle,
    this.messageTextStyle,
    required this.messageMaxLines,
    required this.titleMessageSpacing,
    required this.infoColor,
    required this.successColor,
    required this.warningColor,
    required this.errorColor,
    required this.accentBarWidth,
    required this.iconSize,
    required this.iconSpacing,
    required this.closeIconColor,
    required this.closeIconSize,
    required this.padding,
    required this.width,
    required this.margin,
    required this.spacing,
    required this.maxVisible,
    required this.slideInDuration,
    required this.fadeOutDuration,
    required this.displayDuration,
    required this.slideOffsetX,
    required this.shadowColor,
    required this.shadowBlurRadius,
    required this.shadowOffsetY,
  });

  factory ToastThemeExtension.fromJson(Map<String, dynamic> json) =>
      _$ToastThemeExtensionFromJson(json);

  Map<String, dynamic> toJson() => _$ToastThemeExtensionToJson(this);
}
