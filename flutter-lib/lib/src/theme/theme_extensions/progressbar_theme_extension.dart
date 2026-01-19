import 'package:flutter/material.dart';
import 'package:theme_tailor_annotation/theme_tailor_annotation.dart';
import 'package:json_annotation/json_annotation.dart';
import 'json_converters.dart';

part 'progressbar_theme_extension.tailor.dart';
part 'progressbar_theme_extension.g.dart';

@TailorMixin()
@JsonSerializable(explicitToJson: true)
@ColorConverter()
@DurationConverter()
@CurveConverter()
class ProgressBarThemeExtension extends ThemeExtension<ProgressBarThemeExtension> with _$ProgressBarThemeExtensionTailorMixin {
  // Colors
  final Color backgroundColor;
  final Color progressColor;
  final Color disabledProgressColor;
  final Color borderColor;
  
  // Sizing
  final double defaultWidth;
  final double defaultHeight;
  final double borderWidth;
  final double borderRadius;
  
  // Animation - basic settings
  final Duration indeterminateDuration;
  final double indeterminateBarWidthFactor; 
  final Curve indeterminateCurve;
  final bool indeterminateEnableSizeAnimation;
  
  // Type A animation (cycle 0)
  final double indeterminateSizeStartA;   
  final double indeterminateSizeMidA;        
  final double indeterminateSizeEndA;        
  final double indeterminateSpeedFirstA;     
  final double indeterminateSpeedSecondA;    
  
  // Type B animation (cycle 1)
  final double indeterminateSizeStartB;
  final double indeterminateSizeMidB;
  final double indeterminateSizeEndB;
  final double indeterminateSpeedFirstB;
  final double indeterminateSpeedSecondB;
  
  const ProgressBarThemeExtension({
    required this.backgroundColor,
    required this.progressColor,
    required this.disabledProgressColor,
    required this.borderColor,
    required this.defaultWidth,
    required this.defaultHeight,
    required this.borderWidth,
    required this.borderRadius,
    required this.indeterminateDuration,
    required this.indeterminateBarWidthFactor,
    required this.indeterminateCurve,
    required this.indeterminateEnableSizeAnimation,
    required this.indeterminateSizeStartA,
    required this.indeterminateSizeMidA,
    required this.indeterminateSizeEndA,
    required this.indeterminateSpeedFirstA,
    required this.indeterminateSpeedSecondA,
    required this.indeterminateSizeStartB,
    required this.indeterminateSizeMidB,
    required this.indeterminateSizeEndB,
    required this.indeterminateSpeedFirstB,
    required this.indeterminateSpeedSecondB,
  });

  factory ProgressBarThemeExtension.fromJson(Map<String, dynamic> json) =>
      _$ProgressBarThemeExtensionFromJson(json);

  Map<String, dynamic> toJson() => _$ProgressBarThemeExtensionToJson(this);
}