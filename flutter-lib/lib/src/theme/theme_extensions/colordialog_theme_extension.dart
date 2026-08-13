import 'package:flutter/material.dart';
import 'package:theme_tailor_annotation/theme_tailor_annotation.dart';
import 'package:json_annotation/json_annotation.dart';
import 'json_converters.dart';

part 'colordialog_theme_extension.tailor.dart';
part 'colordialog_theme_extension.g.dart';

@TailorMixin()
@JsonSerializable(explicitToJson: true)
@ColorConverter()
@TextStyleConverter()
@EdgeInsetsConverter()
class ColorDialogThemeExtension extends ThemeExtension<ColorDialogThemeExtension>
    with _$ColorDialogThemeExtensionTailorMixin {
  // Dialog chrome
  final TextStyle? titleStyle;
  final TextStyle? labelStyle;
  final Color backgroundColor;
  final double borderRadius;
  final double maxWidth;
  final EdgeInsets padding;
  final double titleContentSpacing;
  final double sectionSpacing;
  final double contentButtonsSpacing;
  final double buttonSpacing;

  // Saturation/value plane and its thumb
  final double saturationValueHeight;
  final double thumbRadius;
  final double thumbOutlineRadius;
  final double thumbStrokeWidth;
  final Color thumbColor;
  final Color thumbOutlineColor;

  // Hue slider and its thumb
  final double hueSliderHeight;
  final double hueThumbWidth;
  final double hueThumbStrokeWidth;
  final Color hueThumbColor;

  // Preset / custom colour swatches
  final double swatchSize;
  final double swatchSpacing;
  final double swatchBorderRadius;
  final double swatchBorderWidth;
  final Color swatchBorderColor;
  final double selectedSwatchBorderWidth;
  final Color selectedSwatchBorderColor;

  // Preview chip + hex field
  final double previewWidth;
  final double previewHeight;
  final double previewBorderRadius;
  final Color previewBorderColor;
  final double previewSpacing;
  final double hexFieldWidth;

  const ColorDialogThemeExtension({
    this.titleStyle,
    this.labelStyle,
    required this.backgroundColor,
    required this.borderRadius,
    required this.maxWidth,
    required this.padding,
    required this.titleContentSpacing,
    required this.sectionSpacing,
    required this.contentButtonsSpacing,
    required this.buttonSpacing,
    required this.saturationValueHeight,
    required this.thumbRadius,
    required this.thumbOutlineRadius,
    required this.thumbStrokeWidth,
    required this.thumbColor,
    required this.thumbOutlineColor,
    required this.hueSliderHeight,
    required this.hueThumbWidth,
    required this.hueThumbStrokeWidth,
    required this.hueThumbColor,
    required this.swatchSize,
    required this.swatchSpacing,
    required this.swatchBorderRadius,
    required this.swatchBorderWidth,
    required this.swatchBorderColor,
    required this.selectedSwatchBorderWidth,
    required this.selectedSwatchBorderColor,
    required this.previewWidth,
    required this.previewHeight,
    required this.previewBorderRadius,
    required this.previewBorderColor,
    required this.previewSpacing,
    required this.hexFieldWidth,
  });

  factory ColorDialogThemeExtension.fromJson(Map<String, dynamic> json) =>
      _$ColorDialogThemeExtensionFromJson(json);

  Map<String, dynamic> toJson() => _$ColorDialogThemeExtensionToJson(this);
}
