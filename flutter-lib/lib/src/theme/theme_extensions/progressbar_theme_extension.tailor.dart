// coverage:ignore-file
// GENERATED CODE - DO NOT MODIFY BY HAND
// ignore_for_file: type=lint
// ignore_for_file: unused_element, deprecated_member_use, deprecated_member_use_from_same_package, use_function_type_syntax_for_parameters, unnecessary_const, avoid_init_to_null, invalid_override_different_default_values_named, prefer_expression_function_bodies, annotate_overrides, invalid_annotation_target, unnecessary_question_mark

part of 'progressbar_theme_extension.dart';

// **************************************************************************
// TailorAnnotationsGenerator
// **************************************************************************

mixin _$ProgressBarThemeExtensionTailorMixin
    on ThemeExtension<ProgressBarThemeExtension> {
  Color get backgroundColor;
  Color get progressColor;
  Color get disabledProgressColor;
  Color get borderColor;
  double get defaultWidth;
  double get defaultHeight;
  double get borderWidth;
  double get borderRadius;
  Duration get indeterminateDuration;
  double get indeterminateBarWidthFactor;
  Curve get indeterminateCurve;
  bool get indeterminateEnableSizeAnimation;
  double get indeterminateSizeStartA;
  double get indeterminateSizeMidA;
  double get indeterminateSizeEndA;
  double get indeterminateSpeedFirstA;
  double get indeterminateSpeedSecondA;
  double get indeterminateSizeStartB;
  double get indeterminateSizeMidB;
  double get indeterminateSizeEndB;
  double get indeterminateSpeedFirstB;
  double get indeterminateSpeedSecondB;

  @override
  ProgressBarThemeExtension copyWith({
    Color? backgroundColor,
    Color? progressColor,
    Color? disabledProgressColor,
    Color? borderColor,
    double? defaultWidth,
    double? defaultHeight,
    double? borderWidth,
    double? borderRadius,
    Duration? indeterminateDuration,
    double? indeterminateBarWidthFactor,
    Curve? indeterminateCurve,
    bool? indeterminateEnableSizeAnimation,
    double? indeterminateSizeStartA,
    double? indeterminateSizeMidA,
    double? indeterminateSizeEndA,
    double? indeterminateSpeedFirstA,
    double? indeterminateSpeedSecondA,
    double? indeterminateSizeStartB,
    double? indeterminateSizeMidB,
    double? indeterminateSizeEndB,
    double? indeterminateSpeedFirstB,
    double? indeterminateSpeedSecondB,
  }) {
    return ProgressBarThemeExtension(
      backgroundColor: backgroundColor ?? this.backgroundColor,
      progressColor: progressColor ?? this.progressColor,
      disabledProgressColor:
          disabledProgressColor ?? this.disabledProgressColor,
      borderColor: borderColor ?? this.borderColor,
      defaultWidth: defaultWidth ?? this.defaultWidth,
      defaultHeight: defaultHeight ?? this.defaultHeight,
      borderWidth: borderWidth ?? this.borderWidth,
      borderRadius: borderRadius ?? this.borderRadius,
      indeterminateDuration:
          indeterminateDuration ?? this.indeterminateDuration,
      indeterminateBarWidthFactor:
          indeterminateBarWidthFactor ?? this.indeterminateBarWidthFactor,
      indeterminateCurve: indeterminateCurve ?? this.indeterminateCurve,
      indeterminateEnableSizeAnimation:
          indeterminateEnableSizeAnimation ??
          this.indeterminateEnableSizeAnimation,
      indeterminateSizeStartA:
          indeterminateSizeStartA ?? this.indeterminateSizeStartA,
      indeterminateSizeMidA:
          indeterminateSizeMidA ?? this.indeterminateSizeMidA,
      indeterminateSizeEndA:
          indeterminateSizeEndA ?? this.indeterminateSizeEndA,
      indeterminateSpeedFirstA:
          indeterminateSpeedFirstA ?? this.indeterminateSpeedFirstA,
      indeterminateSpeedSecondA:
          indeterminateSpeedSecondA ?? this.indeterminateSpeedSecondA,
      indeterminateSizeStartB:
          indeterminateSizeStartB ?? this.indeterminateSizeStartB,
      indeterminateSizeMidB:
          indeterminateSizeMidB ?? this.indeterminateSizeMidB,
      indeterminateSizeEndB:
          indeterminateSizeEndB ?? this.indeterminateSizeEndB,
      indeterminateSpeedFirstB:
          indeterminateSpeedFirstB ?? this.indeterminateSpeedFirstB,
      indeterminateSpeedSecondB:
          indeterminateSpeedSecondB ?? this.indeterminateSpeedSecondB,
    );
  }

  @override
  ProgressBarThemeExtension lerp(
    covariant ThemeExtension<ProgressBarThemeExtension>? other,
    double t,
  ) {
    if (other is! ProgressBarThemeExtension)
      return this as ProgressBarThemeExtension;
    return ProgressBarThemeExtension(
      backgroundColor: Color.lerp(backgroundColor, other.backgroundColor, t)!,
      progressColor: Color.lerp(progressColor, other.progressColor, t)!,
      disabledProgressColor: Color.lerp(
        disabledProgressColor,
        other.disabledProgressColor,
        t,
      )!,
      borderColor: Color.lerp(borderColor, other.borderColor, t)!,
      defaultWidth: t < 0.5 ? defaultWidth : other.defaultWidth,
      defaultHeight: t < 0.5 ? defaultHeight : other.defaultHeight,
      borderWidth: t < 0.5 ? borderWidth : other.borderWidth,
      borderRadius: t < 0.5 ? borderRadius : other.borderRadius,
      indeterminateDuration: t < 0.5
          ? indeterminateDuration
          : other.indeterminateDuration,
      indeterminateBarWidthFactor: t < 0.5
          ? indeterminateBarWidthFactor
          : other.indeterminateBarWidthFactor,
      indeterminateCurve: t < 0.5
          ? indeterminateCurve
          : other.indeterminateCurve,
      indeterminateEnableSizeAnimation: t < 0.5
          ? indeterminateEnableSizeAnimation
          : other.indeterminateEnableSizeAnimation,
      indeterminateSizeStartA: t < 0.5
          ? indeterminateSizeStartA
          : other.indeterminateSizeStartA,
      indeterminateSizeMidA: t < 0.5
          ? indeterminateSizeMidA
          : other.indeterminateSizeMidA,
      indeterminateSizeEndA: t < 0.5
          ? indeterminateSizeEndA
          : other.indeterminateSizeEndA,
      indeterminateSpeedFirstA: t < 0.5
          ? indeterminateSpeedFirstA
          : other.indeterminateSpeedFirstA,
      indeterminateSpeedSecondA: t < 0.5
          ? indeterminateSpeedSecondA
          : other.indeterminateSpeedSecondA,
      indeterminateSizeStartB: t < 0.5
          ? indeterminateSizeStartB
          : other.indeterminateSizeStartB,
      indeterminateSizeMidB: t < 0.5
          ? indeterminateSizeMidB
          : other.indeterminateSizeMidB,
      indeterminateSizeEndB: t < 0.5
          ? indeterminateSizeEndB
          : other.indeterminateSizeEndB,
      indeterminateSpeedFirstB: t < 0.5
          ? indeterminateSpeedFirstB
          : other.indeterminateSpeedFirstB,
      indeterminateSpeedSecondB: t < 0.5
          ? indeterminateSpeedSecondB
          : other.indeterminateSpeedSecondB,
    );
  }

  @override
  bool operator ==(Object other) {
    return identical(this, other) ||
        (other.runtimeType == runtimeType &&
            other is ProgressBarThemeExtension &&
            const DeepCollectionEquality().equals(
              backgroundColor,
              other.backgroundColor,
            ) &&
            const DeepCollectionEquality().equals(
              progressColor,
              other.progressColor,
            ) &&
            const DeepCollectionEquality().equals(
              disabledProgressColor,
              other.disabledProgressColor,
            ) &&
            const DeepCollectionEquality().equals(
              borderColor,
              other.borderColor,
            ) &&
            const DeepCollectionEquality().equals(
              defaultWidth,
              other.defaultWidth,
            ) &&
            const DeepCollectionEquality().equals(
              defaultHeight,
              other.defaultHeight,
            ) &&
            const DeepCollectionEquality().equals(
              borderWidth,
              other.borderWidth,
            ) &&
            const DeepCollectionEquality().equals(
              borderRadius,
              other.borderRadius,
            ) &&
            const DeepCollectionEquality().equals(
              indeterminateDuration,
              other.indeterminateDuration,
            ) &&
            const DeepCollectionEquality().equals(
              indeterminateBarWidthFactor,
              other.indeterminateBarWidthFactor,
            ) &&
            const DeepCollectionEquality().equals(
              indeterminateCurve,
              other.indeterminateCurve,
            ) &&
            const DeepCollectionEquality().equals(
              indeterminateEnableSizeAnimation,
              other.indeterminateEnableSizeAnimation,
            ) &&
            const DeepCollectionEquality().equals(
              indeterminateSizeStartA,
              other.indeterminateSizeStartA,
            ) &&
            const DeepCollectionEquality().equals(
              indeterminateSizeMidA,
              other.indeterminateSizeMidA,
            ) &&
            const DeepCollectionEquality().equals(
              indeterminateSizeEndA,
              other.indeterminateSizeEndA,
            ) &&
            const DeepCollectionEquality().equals(
              indeterminateSpeedFirstA,
              other.indeterminateSpeedFirstA,
            ) &&
            const DeepCollectionEquality().equals(
              indeterminateSpeedSecondA,
              other.indeterminateSpeedSecondA,
            ) &&
            const DeepCollectionEquality().equals(
              indeterminateSizeStartB,
              other.indeterminateSizeStartB,
            ) &&
            const DeepCollectionEquality().equals(
              indeterminateSizeMidB,
              other.indeterminateSizeMidB,
            ) &&
            const DeepCollectionEquality().equals(
              indeterminateSizeEndB,
              other.indeterminateSizeEndB,
            ) &&
            const DeepCollectionEquality().equals(
              indeterminateSpeedFirstB,
              other.indeterminateSpeedFirstB,
            ) &&
            const DeepCollectionEquality().equals(
              indeterminateSpeedSecondB,
              other.indeterminateSpeedSecondB,
            ));
  }

  @override
  int get hashCode {
    return Object.hashAll([
      runtimeType.hashCode,
      const DeepCollectionEquality().hash(backgroundColor),
      const DeepCollectionEquality().hash(progressColor),
      const DeepCollectionEquality().hash(disabledProgressColor),
      const DeepCollectionEquality().hash(borderColor),
      const DeepCollectionEquality().hash(defaultWidth),
      const DeepCollectionEquality().hash(defaultHeight),
      const DeepCollectionEquality().hash(borderWidth),
      const DeepCollectionEquality().hash(borderRadius),
      const DeepCollectionEquality().hash(indeterminateDuration),
      const DeepCollectionEquality().hash(indeterminateBarWidthFactor),
      const DeepCollectionEquality().hash(indeterminateCurve),
      const DeepCollectionEquality().hash(indeterminateEnableSizeAnimation),
      const DeepCollectionEquality().hash(indeterminateSizeStartA),
      const DeepCollectionEquality().hash(indeterminateSizeMidA),
      const DeepCollectionEquality().hash(indeterminateSizeEndA),
      const DeepCollectionEquality().hash(indeterminateSpeedFirstA),
      const DeepCollectionEquality().hash(indeterminateSpeedSecondA),
      const DeepCollectionEquality().hash(indeterminateSizeStartB),
      const DeepCollectionEquality().hash(indeterminateSizeMidB),
      const DeepCollectionEquality().hash(indeterminateSizeEndB),
      const DeepCollectionEquality().hash(indeterminateSpeedFirstB),
      const DeepCollectionEquality().hash(indeterminateSpeedSecondB),
    ]);
  }
}

extension ProgressBarThemeExtensionBuildContextProps on BuildContext {
  ProgressBarThemeExtension get progressBarThemeExtension =>
      Theme.of(this).extension<ProgressBarThemeExtension>()!;
  Color get backgroundColor => progressBarThemeExtension.backgroundColor;
  Color get progressColor => progressBarThemeExtension.progressColor;
  Color get disabledProgressColor =>
      progressBarThemeExtension.disabledProgressColor;
  Color get borderColor => progressBarThemeExtension.borderColor;
  double get defaultWidth => progressBarThemeExtension.defaultWidth;
  double get defaultHeight => progressBarThemeExtension.defaultHeight;
  double get borderWidth => progressBarThemeExtension.borderWidth;
  double get borderRadius => progressBarThemeExtension.borderRadius;
  Duration get indeterminateDuration =>
      progressBarThemeExtension.indeterminateDuration;
  double get indeterminateBarWidthFactor =>
      progressBarThemeExtension.indeterminateBarWidthFactor;
  Curve get indeterminateCurve => progressBarThemeExtension.indeterminateCurve;
  bool get indeterminateEnableSizeAnimation =>
      progressBarThemeExtension.indeterminateEnableSizeAnimation;
  double get indeterminateSizeStartA =>
      progressBarThemeExtension.indeterminateSizeStartA;
  double get indeterminateSizeMidA =>
      progressBarThemeExtension.indeterminateSizeMidA;
  double get indeterminateSizeEndA =>
      progressBarThemeExtension.indeterminateSizeEndA;
  double get indeterminateSpeedFirstA =>
      progressBarThemeExtension.indeterminateSpeedFirstA;
  double get indeterminateSpeedSecondA =>
      progressBarThemeExtension.indeterminateSpeedSecondA;
  double get indeterminateSizeStartB =>
      progressBarThemeExtension.indeterminateSizeStartB;
  double get indeterminateSizeMidB =>
      progressBarThemeExtension.indeterminateSizeMidB;
  double get indeterminateSizeEndB =>
      progressBarThemeExtension.indeterminateSizeEndB;
  double get indeterminateSpeedFirstB =>
      progressBarThemeExtension.indeterminateSpeedFirstB;
  double get indeterminateSpeedSecondB =>
      progressBarThemeExtension.indeterminateSpeedSecondB;
}
