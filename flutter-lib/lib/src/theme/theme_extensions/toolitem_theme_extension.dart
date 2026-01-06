import 'package:flutter/material.dart';
import 'package:theme_tailor_annotation/theme_tailor_annotation.dart';
import 'package:json_annotation/json_annotation.dart';
import 'json_converters.dart';

part 'toolitem_theme_extension.tailor.dart';
part 'toolitem_theme_extension.g.dart';

@TailorMixin()
@JsonSerializable(explicitToJson: true)
@ColorConverter()
@TextStyleConverter()
@EdgeInsetsConverter()
@DurationConverter()
class ToolItemThemeExtension extends ThemeExtension<ToolItemThemeExtension> with _$ToolItemThemeExtensionTailorMixin {
  final Color enabledColor;
  final Color disabledColor;
  final Color hoverColor;
  final Color selectedBackgroundColor;
  final Color separatorColor;
  final Color dropdownIconColor;
  final double borderRadius;
  final double separatorWidth;
  final double separatorThickness;
  final double separatorIndent;
  final TextStyle? fontStyle;
  final double defaultIconSize;
  final double iconSize;
  final double emptyButtonSize;
  final double dropdownArrowSize;
  final EdgeInsets buttonPadding;
  final EdgeInsets textPadding;
  final double splashOpacity;
  final double highlightOpacity;
  final double separatorOpacity;
  final double separatorBarWidth;
  final EdgeInsets separatorBarMargin;
  final EdgeInsets tooltipMargin;
  final Duration tooltipWaitDuration;
  final Color segmentSelectedBackgroundColor;
  final Color segmentInnerColor;
  final Color segmentUnselectedBackgroundColor;
  final Color segmentSelectedTextColor;
  final Color segmentUnselectedTextColor;
  final double segmentBorderRadius;
  final EdgeInsets segmentPadding;
  final double loadingIndicatorSizeFactor;
  final double loadingIndicatorStrokeWidth;
  final bool tooltipPreferBelow;
  final double tooltipVerticalOffset;
  final Duration segmentAnimationDuration;
  final String segmentKeywordText;
  final String segmentDebugText;
  final String specialDropdownTooltipText;
  final Color specialDropdownBackgroundColor;
  final Color specialDropdownTextColor;
  final Color specialDropdownSeparatorColor;
  final Color specialDropdownArrowColor;
  final EdgeInsets specialDropdownPadding;
  final double specialDropdownItemSpacing;
  
  const ToolItemThemeExtension({
    required this.enabledColor,
    required this.disabledColor,
    required this.hoverColor,
    required this.selectedBackgroundColor,
    required this.separatorColor,
    required this.dropdownIconColor,
    required this.borderRadius,
    required this.separatorWidth,
    required this.separatorThickness,
    required this.separatorIndent,
    required this.defaultIconSize,
    required this.iconSize,
    required this.emptyButtonSize,
    required this.dropdownArrowSize,
    required this.buttonPadding,
    required this.textPadding,
    required this.splashOpacity,
    required this.highlightOpacity,
    required this.separatorOpacity,
    required this.separatorBarWidth,
    required this.separatorBarMargin,
    required this.tooltipMargin,
    required this.tooltipWaitDuration,
    required this.segmentSelectedBackgroundColor,
    required this.segmentInnerColor,
    required this.segmentUnselectedBackgroundColor,
    required this.segmentSelectedTextColor,
    required this.segmentUnselectedTextColor,
    required this.segmentBorderRadius,
    required this.segmentPadding,
    required this.loadingIndicatorSizeFactor,
    required this.loadingIndicatorStrokeWidth,
    required this.tooltipPreferBelow,
    required this.tooltipVerticalOffset,
    required this.segmentAnimationDuration,
    required this.segmentKeywordText,
    required this.segmentDebugText,
    required this.specialDropdownTooltipText,
    required this.specialDropdownBackgroundColor,
    required this.specialDropdownTextColor,
    required this.specialDropdownSeparatorColor,
    required this.specialDropdownArrowColor,
    required this.specialDropdownPadding,
    required this.specialDropdownItemSpacing,
    this.fontStyle,
  });

  factory ToolItemThemeExtension.fromJson(Map<String, dynamic> json) =>
      _$ToolItemThemeExtensionFromJson(json);

  Map<String, dynamic> toJson() => _$ToolItemThemeExtensionToJson(this);
  
  static Duration _durationFromJson(int json) => Duration(milliseconds: json);
  static int _durationToJson(Duration duration) => duration.inMilliseconds;
}

