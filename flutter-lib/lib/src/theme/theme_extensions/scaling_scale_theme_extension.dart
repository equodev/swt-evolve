import 'package:flutter/material.dart';
import 'package:json_annotation/json_annotation.dart';
import 'package:theme_tailor_annotation/theme_tailor_annotation.dart';

import 'json_converters.dart';

part 'scaling_scale_theme_extension.tailor.dart';
part 'scaling_scale_theme_extension.g.dart';

@TailorMixin()
@JsonSerializable(explicitToJson: true)
@ColorConverter()
@EdgeInsetsConverter()
class ScalingScaleThemeExtension
    extends ThemeExtension<ScalingScaleThemeExtension>
    with _$ScalingScaleThemeExtensionTailorMixin {
  final double sliderWidth;
  final double sliderTrackHeight;
  final double thumbRadius;
  final double overlayRadius;
  final double chevronIconSize;
  final EdgeInsets chevronPadding;
  final Color chevronIconColor;
  final double labelTextSize;
  final Color labelTextColor;
  final double trailingGap;
  final double sliderMin;
  final double sliderMax;
  final String expandTooltip;
  final String collapseTooltip;
  final String resetTooltip;

  const ScalingScaleThemeExtension({
    required this.sliderWidth,
    required this.sliderTrackHeight,
    required this.thumbRadius,
    required this.overlayRadius,
    required this.chevronIconSize,
    required this.chevronPadding,
    required this.chevronIconColor,
    required this.labelTextSize,
    required this.labelTextColor,
    required this.trailingGap,
    required this.sliderMin,
    required this.sliderMax,
    required this.expandTooltip,
    required this.collapseTooltip,
    required this.resetTooltip,
  });

  factory ScalingScaleThemeExtension.fromJson(Map<String, dynamic> json) =>
      _$ScalingScaleThemeExtensionFromJson(json);

  Map<String, dynamic> toJson() => _$ScalingScaleThemeExtensionToJson(this);
}
