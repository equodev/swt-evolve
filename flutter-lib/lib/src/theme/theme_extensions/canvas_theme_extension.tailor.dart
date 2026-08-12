// coverage:ignore-file
// GENERATED CODE - DO NOT MODIFY BY HAND
// ignore_for_file: type=lint
// ignore_for_file: unused_element, deprecated_member_use, deprecated_member_use_from_same_package, use_function_type_syntax_for_parameters, unnecessary_const, avoid_init_to_null, invalid_override_different_default_values_named, prefer_expression_function_bodies, annotate_overrides, invalid_annotation_target, unnecessary_question_mark

part of 'canvas_theme_extension.dart';

// **************************************************************************
// TailorAnnotationsGenerator
// **************************************************************************

mixin _$CanvasThemeExtensionTailorMixin
    on ThemeExtension<CanvasThemeExtension> {
  double get defaultWidth;
  double get defaultHeight;
  Color get backgroundColor;
  Color get foregroundColor;
  Color get fillColor;
  Color get strokeColor;
  Color get lineColor;
  Color get pointColor;
  Color get textColor;
  Color get textBackgroundColor;
  Color get focusColor;
  Color get gradientStartColor;
  Color get gradientEndColor;
  Color get patternStartColor;
  Color get patternEndColor;
  Color get imageTintColor;

  @override
  CanvasThemeExtension copyWith({
    double? defaultWidth,
    double? defaultHeight,
    Color? backgroundColor,
    Color? foregroundColor,
    Color? fillColor,
    Color? strokeColor,
    Color? lineColor,
    Color? pointColor,
    Color? textColor,
    Color? textBackgroundColor,
    Color? focusColor,
    Color? gradientStartColor,
    Color? gradientEndColor,
    Color? patternStartColor,
    Color? patternEndColor,
    Color? imageTintColor,
  }) {
    return CanvasThemeExtension(
      defaultWidth: defaultWidth ?? this.defaultWidth,
      defaultHeight: defaultHeight ?? this.defaultHeight,
      backgroundColor: backgroundColor ?? this.backgroundColor,
      foregroundColor: foregroundColor ?? this.foregroundColor,
      fillColor: fillColor ?? this.fillColor,
      strokeColor: strokeColor ?? this.strokeColor,
      lineColor: lineColor ?? this.lineColor,
      pointColor: pointColor ?? this.pointColor,
      textColor: textColor ?? this.textColor,
      textBackgroundColor: textBackgroundColor ?? this.textBackgroundColor,
      focusColor: focusColor ?? this.focusColor,
      gradientStartColor: gradientStartColor ?? this.gradientStartColor,
      gradientEndColor: gradientEndColor ?? this.gradientEndColor,
      patternStartColor: patternStartColor ?? this.patternStartColor,
      patternEndColor: patternEndColor ?? this.patternEndColor,
      imageTintColor: imageTintColor ?? this.imageTintColor,
    );
  }

  @override
  CanvasThemeExtension lerp(
    covariant ThemeExtension<CanvasThemeExtension>? other,
    double t,
  ) {
    if (other is! CanvasThemeExtension) return this as CanvasThemeExtension;
    return CanvasThemeExtension(
      defaultWidth: t < 0.5 ? defaultWidth : other.defaultWidth,
      defaultHeight: t < 0.5 ? defaultHeight : other.defaultHeight,
      backgroundColor: Color.lerp(backgroundColor, other.backgroundColor, t)!,
      foregroundColor: Color.lerp(foregroundColor, other.foregroundColor, t)!,
      fillColor: Color.lerp(fillColor, other.fillColor, t)!,
      strokeColor: Color.lerp(strokeColor, other.strokeColor, t)!,
      lineColor: Color.lerp(lineColor, other.lineColor, t)!,
      pointColor: Color.lerp(pointColor, other.pointColor, t)!,
      textColor: Color.lerp(textColor, other.textColor, t)!,
      textBackgroundColor: Color.lerp(
        textBackgroundColor,
        other.textBackgroundColor,
        t,
      )!,
      focusColor: Color.lerp(focusColor, other.focusColor, t)!,
      gradientStartColor: Color.lerp(
        gradientStartColor,
        other.gradientStartColor,
        t,
      )!,
      gradientEndColor: Color.lerp(
        gradientEndColor,
        other.gradientEndColor,
        t,
      )!,
      patternStartColor: Color.lerp(
        patternStartColor,
        other.patternStartColor,
        t,
      )!,
      patternEndColor: Color.lerp(patternEndColor, other.patternEndColor, t)!,
      imageTintColor: Color.lerp(imageTintColor, other.imageTintColor, t)!,
    );
  }

  @override
  bool operator ==(Object other) {
    return identical(this, other) ||
        (other.runtimeType == runtimeType &&
            other is CanvasThemeExtension &&
            const DeepCollectionEquality().equals(
              defaultWidth,
              other.defaultWidth,
            ) &&
            const DeepCollectionEquality().equals(
              defaultHeight,
              other.defaultHeight,
            ) &&
            const DeepCollectionEquality().equals(
              backgroundColor,
              other.backgroundColor,
            ) &&
            const DeepCollectionEquality().equals(
              foregroundColor,
              other.foregroundColor,
            ) &&
            const DeepCollectionEquality().equals(fillColor, other.fillColor) &&
            const DeepCollectionEquality().equals(
              strokeColor,
              other.strokeColor,
            ) &&
            const DeepCollectionEquality().equals(lineColor, other.lineColor) &&
            const DeepCollectionEquality().equals(
              pointColor,
              other.pointColor,
            ) &&
            const DeepCollectionEquality().equals(textColor, other.textColor) &&
            const DeepCollectionEquality().equals(
              textBackgroundColor,
              other.textBackgroundColor,
            ) &&
            const DeepCollectionEquality().equals(
              focusColor,
              other.focusColor,
            ) &&
            const DeepCollectionEquality().equals(
              gradientStartColor,
              other.gradientStartColor,
            ) &&
            const DeepCollectionEquality().equals(
              gradientEndColor,
              other.gradientEndColor,
            ) &&
            const DeepCollectionEquality().equals(
              patternStartColor,
              other.patternStartColor,
            ) &&
            const DeepCollectionEquality().equals(
              patternEndColor,
              other.patternEndColor,
            ) &&
            const DeepCollectionEquality().equals(
              imageTintColor,
              other.imageTintColor,
            ));
  }

  @override
  int get hashCode {
    return Object.hash(
      runtimeType.hashCode,
      const DeepCollectionEquality().hash(defaultWidth),
      const DeepCollectionEquality().hash(defaultHeight),
      const DeepCollectionEquality().hash(backgroundColor),
      const DeepCollectionEquality().hash(foregroundColor),
      const DeepCollectionEquality().hash(fillColor),
      const DeepCollectionEquality().hash(strokeColor),
      const DeepCollectionEquality().hash(lineColor),
      const DeepCollectionEquality().hash(pointColor),
      const DeepCollectionEquality().hash(textColor),
      const DeepCollectionEquality().hash(textBackgroundColor),
      const DeepCollectionEquality().hash(focusColor),
      const DeepCollectionEquality().hash(gradientStartColor),
      const DeepCollectionEquality().hash(gradientEndColor),
      const DeepCollectionEquality().hash(patternStartColor),
      const DeepCollectionEquality().hash(patternEndColor),
      const DeepCollectionEquality().hash(imageTintColor),
    );
  }
}

extension CanvasThemeExtensionBuildContextProps on BuildContext {
  CanvasThemeExtension get canvasThemeExtension =>
      Theme.of(this).extension<CanvasThemeExtension>()!;
  double get defaultWidth => canvasThemeExtension.defaultWidth;
  double get defaultHeight => canvasThemeExtension.defaultHeight;
  Color get backgroundColor => canvasThemeExtension.backgroundColor;
  Color get foregroundColor => canvasThemeExtension.foregroundColor;
  Color get fillColor => canvasThemeExtension.fillColor;
  Color get strokeColor => canvasThemeExtension.strokeColor;
  Color get lineColor => canvasThemeExtension.lineColor;
  Color get pointColor => canvasThemeExtension.pointColor;
  Color get textColor => canvasThemeExtension.textColor;
  Color get textBackgroundColor => canvasThemeExtension.textBackgroundColor;
  Color get focusColor => canvasThemeExtension.focusColor;
  Color get gradientStartColor => canvasThemeExtension.gradientStartColor;
  Color get gradientEndColor => canvasThemeExtension.gradientEndColor;
  Color get patternStartColor => canvasThemeExtension.patternStartColor;
  Color get patternEndColor => canvasThemeExtension.patternEndColor;
  Color get imageTintColor => canvasThemeExtension.imageTintColor;
}
