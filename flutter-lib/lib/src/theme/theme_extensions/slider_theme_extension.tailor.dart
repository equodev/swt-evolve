// coverage:ignore-file
// GENERATED CODE - DO NOT MODIFY BY HAND
// ignore_for_file: type=lint
// ignore_for_file: unused_element, deprecated_member_use, deprecated_member_use_from_same_package, use_function_type_syntax_for_parameters, unnecessary_const, avoid_init_to_null, invalid_override_different_default_values_named, prefer_expression_function_bodies, annotate_overrides, invalid_annotation_target, unnecessary_question_mark

part of 'slider_theme_extension.dart';

// **************************************************************************
// TailorAnnotationsGenerator
// **************************************************************************

mixin _$SliderThemeExtensionTailorMixin
    on ThemeExtension<SliderThemeExtension> {
  Color get activeTrackColor;
  Color get inactiveTrackColor;
  Color get disabledActiveTrackColor;
  Color get disabledInactiveTrackColor;
  Color get thumbColor;
  Color get disabledThumbColor;
  Color get overlayColor;
  double get trackHeight;
  double get trackBorderRadius;
  double get thumbRadius;
  double get minWidth;
  double get minHeight;
  double get minVerticalWidth;
  double get minVerticalHeight;
  int get minimum;
  int get maximum;
  int get selection;
  int get increment;
  int get pageIncrement;
  int get thumb;

  @override
  SliderThemeExtension copyWith({
    Color? activeTrackColor,
    Color? inactiveTrackColor,
    Color? disabledActiveTrackColor,
    Color? disabledInactiveTrackColor,
    Color? thumbColor,
    Color? disabledThumbColor,
    Color? overlayColor,
    double? trackHeight,
    double? trackBorderRadius,
    double? thumbRadius,
    double? minWidth,
    double? minHeight,
    double? minVerticalWidth,
    double? minVerticalHeight,
    int? minimum,
    int? maximum,
    int? selection,
    int? increment,
    int? pageIncrement,
    int? thumb,
  }) {
    return SliderThemeExtension(
      activeTrackColor: activeTrackColor ?? this.activeTrackColor,
      inactiveTrackColor: inactiveTrackColor ?? this.inactiveTrackColor,
      disabledActiveTrackColor:
          disabledActiveTrackColor ?? this.disabledActiveTrackColor,
      disabledInactiveTrackColor:
          disabledInactiveTrackColor ?? this.disabledInactiveTrackColor,
      thumbColor: thumbColor ?? this.thumbColor,
      disabledThumbColor: disabledThumbColor ?? this.disabledThumbColor,
      overlayColor: overlayColor ?? this.overlayColor,
      trackHeight: trackHeight ?? this.trackHeight,
      trackBorderRadius: trackBorderRadius ?? this.trackBorderRadius,
      thumbRadius: thumbRadius ?? this.thumbRadius,
      minWidth: minWidth ?? this.minWidth,
      minHeight: minHeight ?? this.minHeight,
      minVerticalWidth: minVerticalWidth ?? this.minVerticalWidth,
      minVerticalHeight: minVerticalHeight ?? this.minVerticalHeight,
      minimum: minimum ?? this.minimum,
      maximum: maximum ?? this.maximum,
      selection: selection ?? this.selection,
      increment: increment ?? this.increment,
      pageIncrement: pageIncrement ?? this.pageIncrement,
      thumb: thumb ?? this.thumb,
    );
  }

  @override
  SliderThemeExtension lerp(
    covariant ThemeExtension<SliderThemeExtension>? other,
    double t,
  ) {
    if (other is! SliderThemeExtension) return this as SliderThemeExtension;
    return SliderThemeExtension(
      activeTrackColor: Color.lerp(
        activeTrackColor,
        other.activeTrackColor,
        t,
      )!,
      inactiveTrackColor: Color.lerp(
        inactiveTrackColor,
        other.inactiveTrackColor,
        t,
      )!,
      disabledActiveTrackColor: Color.lerp(
        disabledActiveTrackColor,
        other.disabledActiveTrackColor,
        t,
      )!,
      disabledInactiveTrackColor: Color.lerp(
        disabledInactiveTrackColor,
        other.disabledInactiveTrackColor,
        t,
      )!,
      thumbColor: Color.lerp(thumbColor, other.thumbColor, t)!,
      disabledThumbColor: Color.lerp(
        disabledThumbColor,
        other.disabledThumbColor,
        t,
      )!,
      overlayColor: Color.lerp(overlayColor, other.overlayColor, t)!,
      trackHeight: t < 0.5 ? trackHeight : other.trackHeight,
      trackBorderRadius: t < 0.5 ? trackBorderRadius : other.trackBorderRadius,
      thumbRadius: t < 0.5 ? thumbRadius : other.thumbRadius,
      minWidth: t < 0.5 ? minWidth : other.minWidth,
      minHeight: t < 0.5 ? minHeight : other.minHeight,
      minVerticalWidth: t < 0.5 ? minVerticalWidth : other.minVerticalWidth,
      minVerticalHeight: t < 0.5 ? minVerticalHeight : other.minVerticalHeight,
      minimum: t < 0.5 ? minimum : other.minimum,
      maximum: t < 0.5 ? maximum : other.maximum,
      selection: t < 0.5 ? selection : other.selection,
      increment: t < 0.5 ? increment : other.increment,
      pageIncrement: t < 0.5 ? pageIncrement : other.pageIncrement,
      thumb: t < 0.5 ? thumb : other.thumb,
    );
  }

  @override
  bool operator ==(Object other) {
    return identical(this, other) ||
        (other.runtimeType == runtimeType &&
            other is SliderThemeExtension &&
            const DeepCollectionEquality().equals(
              activeTrackColor,
              other.activeTrackColor,
            ) &&
            const DeepCollectionEquality().equals(
              inactiveTrackColor,
              other.inactiveTrackColor,
            ) &&
            const DeepCollectionEquality().equals(
              disabledActiveTrackColor,
              other.disabledActiveTrackColor,
            ) &&
            const DeepCollectionEquality().equals(
              disabledInactiveTrackColor,
              other.disabledInactiveTrackColor,
            ) &&
            const DeepCollectionEquality().equals(
              thumbColor,
              other.thumbColor,
            ) &&
            const DeepCollectionEquality().equals(
              disabledThumbColor,
              other.disabledThumbColor,
            ) &&
            const DeepCollectionEquality().equals(
              overlayColor,
              other.overlayColor,
            ) &&
            const DeepCollectionEquality().equals(
              trackHeight,
              other.trackHeight,
            ) &&
            const DeepCollectionEquality().equals(
              trackBorderRadius,
              other.trackBorderRadius,
            ) &&
            const DeepCollectionEquality().equals(
              thumbRadius,
              other.thumbRadius,
            ) &&
            const DeepCollectionEquality().equals(minWidth, other.minWidth) &&
            const DeepCollectionEquality().equals(minHeight, other.minHeight) &&
            const DeepCollectionEquality().equals(
              minVerticalWidth,
              other.minVerticalWidth,
            ) &&
            const DeepCollectionEquality().equals(
              minVerticalHeight,
              other.minVerticalHeight,
            ) &&
            const DeepCollectionEquality().equals(minimum, other.minimum) &&
            const DeepCollectionEquality().equals(maximum, other.maximum) &&
            const DeepCollectionEquality().equals(selection, other.selection) &&
            const DeepCollectionEquality().equals(increment, other.increment) &&
            const DeepCollectionEquality().equals(
              pageIncrement,
              other.pageIncrement,
            ) &&
            const DeepCollectionEquality().equals(thumb, other.thumb));
  }

  @override
  int get hashCode {
    return Object.hashAll([
      runtimeType.hashCode,
      const DeepCollectionEquality().hash(activeTrackColor),
      const DeepCollectionEquality().hash(inactiveTrackColor),
      const DeepCollectionEquality().hash(disabledActiveTrackColor),
      const DeepCollectionEquality().hash(disabledInactiveTrackColor),
      const DeepCollectionEquality().hash(thumbColor),
      const DeepCollectionEquality().hash(disabledThumbColor),
      const DeepCollectionEquality().hash(overlayColor),
      const DeepCollectionEquality().hash(trackHeight),
      const DeepCollectionEquality().hash(trackBorderRadius),
      const DeepCollectionEquality().hash(thumbRadius),
      const DeepCollectionEquality().hash(minWidth),
      const DeepCollectionEquality().hash(minHeight),
      const DeepCollectionEquality().hash(minVerticalWidth),
      const DeepCollectionEquality().hash(minVerticalHeight),
      const DeepCollectionEquality().hash(minimum),
      const DeepCollectionEquality().hash(maximum),
      const DeepCollectionEquality().hash(selection),
      const DeepCollectionEquality().hash(increment),
      const DeepCollectionEquality().hash(pageIncrement),
      const DeepCollectionEquality().hash(thumb),
    ]);
  }
}

extension SliderThemeExtensionBuildContextProps on BuildContext {
  SliderThemeExtension get sliderThemeExtension =>
      Theme.of(this).extension<SliderThemeExtension>()!;
  Color get activeTrackColor => sliderThemeExtension.activeTrackColor;
  Color get inactiveTrackColor => sliderThemeExtension.inactiveTrackColor;
  Color get disabledActiveTrackColor =>
      sliderThemeExtension.disabledActiveTrackColor;
  Color get disabledInactiveTrackColor =>
      sliderThemeExtension.disabledInactiveTrackColor;
  Color get thumbColor => sliderThemeExtension.thumbColor;
  Color get disabledThumbColor => sliderThemeExtension.disabledThumbColor;
  Color get overlayColor => sliderThemeExtension.overlayColor;
  double get trackHeight => sliderThemeExtension.trackHeight;
  double get trackBorderRadius => sliderThemeExtension.trackBorderRadius;
  double get thumbRadius => sliderThemeExtension.thumbRadius;
  double get minWidth => sliderThemeExtension.minWidth;
  double get minHeight => sliderThemeExtension.minHeight;
  double get minVerticalWidth => sliderThemeExtension.minVerticalWidth;
  double get minVerticalHeight => sliderThemeExtension.minVerticalHeight;
  int get minimum => sliderThemeExtension.minimum;
  int get maximum => sliderThemeExtension.maximum;
  int get selection => sliderThemeExtension.selection;
  int get increment => sliderThemeExtension.increment;
  int get pageIncrement => sliderThemeExtension.pageIncrement;
  int get thumb => sliderThemeExtension.thumb;
}
