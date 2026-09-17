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

  /// The hover tooltip -- the one a Control's toolTipText opens -- is a plainer surface than the
  /// balloon [padding] sizes, and no platform is placing it in web mode, so its own geometry lives
  /// here: how far its top-left corner clears the pointer, and how close it may come to the edge
  /// of the window before it is pulled back inside.
  final EdgeInsets hoverPadding;
  final double pointerOffsetX;
  final double pointerOffsetY;
  final double screenMargin;
  final double minWidth;
  final double maxWidth;
  final double minHeight;

  final Color shadowColor;
  final double shadowBlurRadius;
  final double shadowOffsetY;

  /// The rich tooltip -- the card a main-toolbar ToolItem opens when `swt.evolve.rich_tooltips` is
  /// on. It is a wider, taller surface than the hover strip above, so it carries its own geometry
  /// and text styles; severity colours and the shadow are shared with the plain tooltip.
  final double richMaxWidth;

  /// Sized against the card's own text, not against the balloon's [iconSize] -- a 24px glyph beside
  /// a 13px title reads as two unrelated things stacked next to each other.
  final double richIconSize;
  final EdgeInsets richPadding;
  final double richBorderRadius;
  final Color richBackgroundColor;

  /// Vertical distance between the item and the top of the card.
  final double richGap;
  final double richTitleBodySpacing;
  final TextStyle? richTitleTextStyle;
  final TextStyle? richBodyTextStyle;

  final TextStyle? richShortcutTextStyle;
  final Color richShortcutBackgroundColor;
  final Color richShortcutBorderColor;
  final EdgeInsets richShortcutPadding;
  final double richShortcutBorderRadius;

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
    required this.hoverPadding,
    required this.pointerOffsetX,
    required this.pointerOffsetY,
    required this.screenMargin,
    required this.minWidth,
    required this.maxWidth,
    required this.minHeight,
    required this.shadowColor,
    required this.shadowBlurRadius,
    required this.shadowOffsetY,
    required this.richMaxWidth,
    required this.richIconSize,
    required this.richPadding,
    required this.richBorderRadius,
    required this.richBackgroundColor,
    required this.richGap,
    required this.richTitleBodySpacing,
    this.richTitleTextStyle,
    this.richBodyTextStyle,
    this.richShortcutTextStyle,
    required this.richShortcutBackgroundColor,
    required this.richShortcutBorderColor,
    required this.richShortcutPadding,
    required this.richShortcutBorderRadius,
  });

  factory TooltipThemeExtension.fromJson(Map<String, dynamic> json) =>
      _$TooltipThemeExtensionFromJson(json);

  Map<String, dynamic> toJson() => _$TooltipThemeExtensionToJson(this);
}
