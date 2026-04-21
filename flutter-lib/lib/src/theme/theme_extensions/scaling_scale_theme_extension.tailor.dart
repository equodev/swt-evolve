// coverage:ignore-file
// GENERATED CODE - DO NOT MODIFY BY HAND
// ignore_for_file: type=lint
// ignore_for_file: unused_element, deprecated_member_use, deprecated_member_use_from_same_package, use_function_type_syntax_for_parameters, unnecessary_const, avoid_init_to_null, invalid_override_different_default_values_named, prefer_expression_function_bodies, annotate_overrides, invalid_annotation_target, unnecessary_question_mark

part of 'scaling_scale_theme_extension.dart';

// **************************************************************************
// TailorAnnotationsGenerator
// **************************************************************************

mixin _$ScalingScaleThemeExtensionTailorMixin
    on ThemeExtension<ScalingScaleThemeExtension> {
  double get sliderWidth;
  double get sliderTrackHeight;
  double get thumbRadius;
  double get overlayRadius;
  double get chevronIconSize;
  EdgeInsets get chevronPadding;
  Color get chevronIconColor;
  double get labelTextSize;
  Color get labelTextColor;
  double get trailingGap;
  double get sliderMin;
  double get sliderMax;
  String get expandTooltip;
  String get collapseTooltip;
  String get resetTooltip;

  @override
  ScalingScaleThemeExtension copyWith({
    double? sliderWidth,
    double? sliderTrackHeight,
    double? thumbRadius,
    double? overlayRadius,
    double? chevronIconSize,
    EdgeInsets? chevronPadding,
    Color? chevronIconColor,
    double? labelTextSize,
    Color? labelTextColor,
    double? trailingGap,
    double? sliderMin,
    double? sliderMax,
    String? expandTooltip,
    String? collapseTooltip,
    String? resetTooltip,
  }) {
    return ScalingScaleThemeExtension(
      sliderWidth: sliderWidth ?? this.sliderWidth,
      sliderTrackHeight: sliderTrackHeight ?? this.sliderTrackHeight,
      thumbRadius: thumbRadius ?? this.thumbRadius,
      overlayRadius: overlayRadius ?? this.overlayRadius,
      chevronIconSize: chevronIconSize ?? this.chevronIconSize,
      chevronPadding: chevronPadding ?? this.chevronPadding,
      chevronIconColor: chevronIconColor ?? this.chevronIconColor,
      labelTextSize: labelTextSize ?? this.labelTextSize,
      labelTextColor: labelTextColor ?? this.labelTextColor,
      trailingGap: trailingGap ?? this.trailingGap,
      sliderMin: sliderMin ?? this.sliderMin,
      sliderMax: sliderMax ?? this.sliderMax,
      expandTooltip: expandTooltip ?? this.expandTooltip,
      collapseTooltip: collapseTooltip ?? this.collapseTooltip,
      resetTooltip: resetTooltip ?? this.resetTooltip,
    );
  }

  @override
  ScalingScaleThemeExtension lerp(
    covariant ThemeExtension<ScalingScaleThemeExtension>? other,
    double t,
  ) {
    if (other is! ScalingScaleThemeExtension)
      return this as ScalingScaleThemeExtension;
    return ScalingScaleThemeExtension(
      sliderWidth: t < 0.5 ? sliderWidth : other.sliderWidth,
      sliderTrackHeight: t < 0.5 ? sliderTrackHeight : other.sliderTrackHeight,
      thumbRadius: t < 0.5 ? thumbRadius : other.thumbRadius,
      overlayRadius: t < 0.5 ? overlayRadius : other.overlayRadius,
      chevronIconSize: t < 0.5 ? chevronIconSize : other.chevronIconSize,
      chevronPadding: t < 0.5 ? chevronPadding : other.chevronPadding,
      chevronIconColor: Color.lerp(
        chevronIconColor,
        other.chevronIconColor,
        t,
      )!,
      labelTextSize: t < 0.5 ? labelTextSize : other.labelTextSize,
      labelTextColor: Color.lerp(labelTextColor, other.labelTextColor, t)!,
      trailingGap: t < 0.5 ? trailingGap : other.trailingGap,
      sliderMin: t < 0.5 ? sliderMin : other.sliderMin,
      sliderMax: t < 0.5 ? sliderMax : other.sliderMax,
      expandTooltip: t < 0.5 ? expandTooltip : other.expandTooltip,
      collapseTooltip: t < 0.5 ? collapseTooltip : other.collapseTooltip,
      resetTooltip: t < 0.5 ? resetTooltip : other.resetTooltip,
    );
  }

  @override
  bool operator ==(Object other) {
    return identical(this, other) ||
        (other.runtimeType == runtimeType &&
            other is ScalingScaleThemeExtension &&
            const DeepCollectionEquality().equals(
              sliderWidth,
              other.sliderWidth,
            ) &&
            const DeepCollectionEquality().equals(
              sliderTrackHeight,
              other.sliderTrackHeight,
            ) &&
            const DeepCollectionEquality().equals(
              thumbRadius,
              other.thumbRadius,
            ) &&
            const DeepCollectionEquality().equals(
              overlayRadius,
              other.overlayRadius,
            ) &&
            const DeepCollectionEquality().equals(
              chevronIconSize,
              other.chevronIconSize,
            ) &&
            const DeepCollectionEquality().equals(
              chevronPadding,
              other.chevronPadding,
            ) &&
            const DeepCollectionEquality().equals(
              chevronIconColor,
              other.chevronIconColor,
            ) &&
            const DeepCollectionEquality().equals(
              labelTextSize,
              other.labelTextSize,
            ) &&
            const DeepCollectionEquality().equals(
              labelTextColor,
              other.labelTextColor,
            ) &&
            const DeepCollectionEquality().equals(
              trailingGap,
              other.trailingGap,
            ) &&
            const DeepCollectionEquality().equals(sliderMin, other.sliderMin) &&
            const DeepCollectionEquality().equals(sliderMax, other.sliderMax) &&
            const DeepCollectionEquality().equals(
              expandTooltip,
              other.expandTooltip,
            ) &&
            const DeepCollectionEquality().equals(
              collapseTooltip,
              other.collapseTooltip,
            ) &&
            const DeepCollectionEquality().equals(
              resetTooltip,
              other.resetTooltip,
            ));
  }

  @override
  int get hashCode {
    return Object.hash(
      runtimeType.hashCode,
      const DeepCollectionEquality().hash(sliderWidth),
      const DeepCollectionEquality().hash(sliderTrackHeight),
      const DeepCollectionEquality().hash(thumbRadius),
      const DeepCollectionEquality().hash(overlayRadius),
      const DeepCollectionEquality().hash(chevronIconSize),
      const DeepCollectionEquality().hash(chevronPadding),
      const DeepCollectionEquality().hash(chevronIconColor),
      const DeepCollectionEquality().hash(labelTextSize),
      const DeepCollectionEquality().hash(labelTextColor),
      const DeepCollectionEquality().hash(trailingGap),
      const DeepCollectionEquality().hash(sliderMin),
      const DeepCollectionEquality().hash(sliderMax),
      const DeepCollectionEquality().hash(expandTooltip),
      const DeepCollectionEquality().hash(collapseTooltip),
      const DeepCollectionEquality().hash(resetTooltip),
    );
  }
}

extension ScalingScaleThemeExtensionBuildContextProps on BuildContext {
  ScalingScaleThemeExtension get scalingScaleThemeExtension =>
      Theme.of(this).extension<ScalingScaleThemeExtension>()!;
  double get sliderWidth => scalingScaleThemeExtension.sliderWidth;
  double get sliderTrackHeight => scalingScaleThemeExtension.sliderTrackHeight;
  double get thumbRadius => scalingScaleThemeExtension.thumbRadius;
  double get overlayRadius => scalingScaleThemeExtension.overlayRadius;
  double get chevronIconSize => scalingScaleThemeExtension.chevronIconSize;
  EdgeInsets get chevronPadding => scalingScaleThemeExtension.chevronPadding;
  Color get chevronIconColor => scalingScaleThemeExtension.chevronIconColor;
  double get labelTextSize => scalingScaleThemeExtension.labelTextSize;
  Color get labelTextColor => scalingScaleThemeExtension.labelTextColor;
  double get trailingGap => scalingScaleThemeExtension.trailingGap;
  double get sliderMin => scalingScaleThemeExtension.sliderMin;
  double get sliderMax => scalingScaleThemeExtension.sliderMax;
  String get expandTooltip => scalingScaleThemeExtension.expandTooltip;
  String get collapseTooltip => scalingScaleThemeExtension.collapseTooltip;
  String get resetTooltip => scalingScaleThemeExtension.resetTooltip;
}
