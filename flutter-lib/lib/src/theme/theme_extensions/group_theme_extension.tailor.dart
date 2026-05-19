// coverage:ignore-file
// GENERATED CODE - DO NOT MODIFY BY HAND
// ignore_for_file: type=lint
// ignore_for_file: unused_element, deprecated_member_use, deprecated_member_use_from_same_package, use_function_type_syntax_for_parameters, unnecessary_const, avoid_init_to_null, invalid_override_different_default_values_named, prefer_expression_function_bodies, annotate_overrides, invalid_annotation_target, unnecessary_question_mark

part of 'group_theme_extension.dart';

// **************************************************************************
// TailorAnnotationsGenerator
// **************************************************************************

mixin _$GroupThemeExtensionTailorMixin on ThemeExtension<GroupThemeExtension> {
  Color get backgroundColor;
  Color get borderColor;
  EdgeInsets get padding;
  EdgeInsets get margin;
  double get borderWidth;
  double get borderRadius;
  Color get foregroundColor;
  TextStyle? get textStyle;
  double get titleHorizontalOffset;
  double get titleLabelPadding;
  Color get shadowHighlightColor;
  Color get shadowDarkColor;
  double get shadowOutOpacity;
  double get shadowOutOpacityAlt;
  double get shadowOutBlurRadius;
  double get shadowOutBlurRadiusAlt;
  double get shadowOutElevation;
  double get shadowSecondaryElevation;
  double get shadowEtchedOpacity;
  double get shadowEtchedBlurRadius;
  double get shadowInBorderFactor;
  double get shadowInBgFactor;

  @override
  GroupThemeExtension copyWith({
    Color? backgroundColor,
    Color? borderColor,
    EdgeInsets? padding,
    EdgeInsets? margin,
    double? borderWidth,
    double? borderRadius,
    Color? foregroundColor,
    TextStyle? textStyle,
    double? titleHorizontalOffset,
    double? titleLabelPadding,
    Color? shadowHighlightColor,
    Color? shadowDarkColor,
    double? shadowOutOpacity,
    double? shadowOutOpacityAlt,
    double? shadowOutBlurRadius,
    double? shadowOutBlurRadiusAlt,
    double? shadowOutElevation,
    double? shadowSecondaryElevation,
    double? shadowEtchedOpacity,
    double? shadowEtchedBlurRadius,
    double? shadowInBorderFactor,
    double? shadowInBgFactor,
  }) {
    return GroupThemeExtension(
      backgroundColor: backgroundColor ?? this.backgroundColor,
      borderColor: borderColor ?? this.borderColor,
      padding: padding ?? this.padding,
      margin: margin ?? this.margin,
      borderWidth: borderWidth ?? this.borderWidth,
      borderRadius: borderRadius ?? this.borderRadius,
      foregroundColor: foregroundColor ?? this.foregroundColor,
      textStyle: textStyle ?? this.textStyle,
      titleHorizontalOffset:
          titleHorizontalOffset ?? this.titleHorizontalOffset,
      titleLabelPadding: titleLabelPadding ?? this.titleLabelPadding,
      shadowHighlightColor: shadowHighlightColor ?? this.shadowHighlightColor,
      shadowDarkColor: shadowDarkColor ?? this.shadowDarkColor,
      shadowOutOpacity: shadowOutOpacity ?? this.shadowOutOpacity,
      shadowOutOpacityAlt: shadowOutOpacityAlt ?? this.shadowOutOpacityAlt,
      shadowOutBlurRadius: shadowOutBlurRadius ?? this.shadowOutBlurRadius,
      shadowOutBlurRadiusAlt:
          shadowOutBlurRadiusAlt ?? this.shadowOutBlurRadiusAlt,
      shadowOutElevation: shadowOutElevation ?? this.shadowOutElevation,
      shadowSecondaryElevation:
          shadowSecondaryElevation ?? this.shadowSecondaryElevation,
      shadowEtchedOpacity: shadowEtchedOpacity ?? this.shadowEtchedOpacity,
      shadowEtchedBlurRadius:
          shadowEtchedBlurRadius ?? this.shadowEtchedBlurRadius,
      shadowInBorderFactor: shadowInBorderFactor ?? this.shadowInBorderFactor,
      shadowInBgFactor: shadowInBgFactor ?? this.shadowInBgFactor,
    );
  }

  @override
  GroupThemeExtension lerp(
    covariant ThemeExtension<GroupThemeExtension>? other,
    double t,
  ) {
    if (other is! GroupThemeExtension) return this as GroupThemeExtension;
    return GroupThemeExtension(
      backgroundColor: Color.lerp(backgroundColor, other.backgroundColor, t)!,
      borderColor: Color.lerp(borderColor, other.borderColor, t)!,
      padding: t < 0.5 ? padding : other.padding,
      margin: t < 0.5 ? margin : other.margin,
      borderWidth: t < 0.5 ? borderWidth : other.borderWidth,
      borderRadius: t < 0.5 ? borderRadius : other.borderRadius,
      foregroundColor: Color.lerp(foregroundColor, other.foregroundColor, t)!,
      textStyle: TextStyle.lerp(textStyle, other.textStyle, t),
      titleHorizontalOffset: t < 0.5
          ? titleHorizontalOffset
          : other.titleHorizontalOffset,
      titleLabelPadding: t < 0.5 ? titleLabelPadding : other.titleLabelPadding,
      shadowHighlightColor: Color.lerp(
        shadowHighlightColor,
        other.shadowHighlightColor,
        t,
      )!,
      shadowDarkColor: Color.lerp(shadowDarkColor, other.shadowDarkColor, t)!,
      shadowOutOpacity: t < 0.5 ? shadowOutOpacity : other.shadowOutOpacity,
      shadowOutOpacityAlt: t < 0.5
          ? shadowOutOpacityAlt
          : other.shadowOutOpacityAlt,
      shadowOutBlurRadius: t < 0.5
          ? shadowOutBlurRadius
          : other.shadowOutBlurRadius,
      shadowOutBlurRadiusAlt: t < 0.5
          ? shadowOutBlurRadiusAlt
          : other.shadowOutBlurRadiusAlt,
      shadowOutElevation: t < 0.5
          ? shadowOutElevation
          : other.shadowOutElevation,
      shadowSecondaryElevation: t < 0.5
          ? shadowSecondaryElevation
          : other.shadowSecondaryElevation,
      shadowEtchedOpacity: t < 0.5
          ? shadowEtchedOpacity
          : other.shadowEtchedOpacity,
      shadowEtchedBlurRadius: t < 0.5
          ? shadowEtchedBlurRadius
          : other.shadowEtchedBlurRadius,
      shadowInBorderFactor: t < 0.5
          ? shadowInBorderFactor
          : other.shadowInBorderFactor,
      shadowInBgFactor: t < 0.5 ? shadowInBgFactor : other.shadowInBgFactor,
    );
  }

  @override
  bool operator ==(Object other) {
    return identical(this, other) ||
        (other.runtimeType == runtimeType &&
            other is GroupThemeExtension &&
            const DeepCollectionEquality().equals(
              backgroundColor,
              other.backgroundColor,
            ) &&
            const DeepCollectionEquality().equals(
              borderColor,
              other.borderColor,
            ) &&
            const DeepCollectionEquality().equals(padding, other.padding) &&
            const DeepCollectionEquality().equals(margin, other.margin) &&
            const DeepCollectionEquality().equals(
              borderWidth,
              other.borderWidth,
            ) &&
            const DeepCollectionEquality().equals(
              borderRadius,
              other.borderRadius,
            ) &&
            const DeepCollectionEquality().equals(
              foregroundColor,
              other.foregroundColor,
            ) &&
            const DeepCollectionEquality().equals(textStyle, other.textStyle) &&
            const DeepCollectionEquality().equals(
              titleHorizontalOffset,
              other.titleHorizontalOffset,
            ) &&
            const DeepCollectionEquality().equals(
              titleLabelPadding,
              other.titleLabelPadding,
            ) &&
            const DeepCollectionEquality().equals(
              shadowHighlightColor,
              other.shadowHighlightColor,
            ) &&
            const DeepCollectionEquality().equals(
              shadowDarkColor,
              other.shadowDarkColor,
            ) &&
            const DeepCollectionEquality().equals(
              shadowOutOpacity,
              other.shadowOutOpacity,
            ) &&
            const DeepCollectionEquality().equals(
              shadowOutOpacityAlt,
              other.shadowOutOpacityAlt,
            ) &&
            const DeepCollectionEquality().equals(
              shadowOutBlurRadius,
              other.shadowOutBlurRadius,
            ) &&
            const DeepCollectionEquality().equals(
              shadowOutBlurRadiusAlt,
              other.shadowOutBlurRadiusAlt,
            ) &&
            const DeepCollectionEquality().equals(
              shadowOutElevation,
              other.shadowOutElevation,
            ) &&
            const DeepCollectionEquality().equals(
              shadowSecondaryElevation,
              other.shadowSecondaryElevation,
            ) &&
            const DeepCollectionEquality().equals(
              shadowEtchedOpacity,
              other.shadowEtchedOpacity,
            ) &&
            const DeepCollectionEquality().equals(
              shadowEtchedBlurRadius,
              other.shadowEtchedBlurRadius,
            ) &&
            const DeepCollectionEquality().equals(
              shadowInBorderFactor,
              other.shadowInBorderFactor,
            ) &&
            const DeepCollectionEquality().equals(
              shadowInBgFactor,
              other.shadowInBgFactor,
            ));
  }

  @override
  int get hashCode {
    return Object.hashAll([
      runtimeType.hashCode,
      const DeepCollectionEquality().hash(backgroundColor),
      const DeepCollectionEquality().hash(borderColor),
      const DeepCollectionEquality().hash(padding),
      const DeepCollectionEquality().hash(margin),
      const DeepCollectionEquality().hash(borderWidth),
      const DeepCollectionEquality().hash(borderRadius),
      const DeepCollectionEquality().hash(foregroundColor),
      const DeepCollectionEquality().hash(textStyle),
      const DeepCollectionEquality().hash(titleHorizontalOffset),
      const DeepCollectionEquality().hash(titleLabelPadding),
      const DeepCollectionEquality().hash(shadowHighlightColor),
      const DeepCollectionEquality().hash(shadowDarkColor),
      const DeepCollectionEquality().hash(shadowOutOpacity),
      const DeepCollectionEquality().hash(shadowOutOpacityAlt),
      const DeepCollectionEquality().hash(shadowOutBlurRadius),
      const DeepCollectionEquality().hash(shadowOutBlurRadiusAlt),
      const DeepCollectionEquality().hash(shadowOutElevation),
      const DeepCollectionEquality().hash(shadowSecondaryElevation),
      const DeepCollectionEquality().hash(shadowEtchedOpacity),
      const DeepCollectionEquality().hash(shadowEtchedBlurRadius),
      const DeepCollectionEquality().hash(shadowInBorderFactor),
      const DeepCollectionEquality().hash(shadowInBgFactor),
    ]);
  }
}

extension GroupThemeExtensionBuildContextProps on BuildContext {
  GroupThemeExtension get groupThemeExtension =>
      Theme.of(this).extension<GroupThemeExtension>()!;
  Color get backgroundColor => groupThemeExtension.backgroundColor;
  Color get borderColor => groupThemeExtension.borderColor;
  EdgeInsets get padding => groupThemeExtension.padding;
  EdgeInsets get margin => groupThemeExtension.margin;
  double get borderWidth => groupThemeExtension.borderWidth;
  double get borderRadius => groupThemeExtension.borderRadius;
  Color get foregroundColor => groupThemeExtension.foregroundColor;
  TextStyle? get textStyle => groupThemeExtension.textStyle;
  double get titleHorizontalOffset => groupThemeExtension.titleHorizontalOffset;
  double get titleLabelPadding => groupThemeExtension.titleLabelPadding;
  Color get shadowHighlightColor => groupThemeExtension.shadowHighlightColor;
  Color get shadowDarkColor => groupThemeExtension.shadowDarkColor;
  double get shadowOutOpacity => groupThemeExtension.shadowOutOpacity;
  double get shadowOutOpacityAlt => groupThemeExtension.shadowOutOpacityAlt;
  double get shadowOutBlurRadius => groupThemeExtension.shadowOutBlurRadius;
  double get shadowOutBlurRadiusAlt =>
      groupThemeExtension.shadowOutBlurRadiusAlt;
  double get shadowOutElevation => groupThemeExtension.shadowOutElevation;
  double get shadowSecondaryElevation =>
      groupThemeExtension.shadowSecondaryElevation;
  double get shadowEtchedOpacity => groupThemeExtension.shadowEtchedOpacity;
  double get shadowEtchedBlurRadius =>
      groupThemeExtension.shadowEtchedBlurRadius;
  double get shadowInBorderFactor => groupThemeExtension.shadowInBorderFactor;
  double get shadowInBgFactor => groupThemeExtension.shadowInBgFactor;
}
