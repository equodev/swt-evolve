// coverage:ignore-file
// GENERATED CODE - DO NOT MODIFY BY HAND
// ignore_for_file: type=lint
// ignore_for_file: unused_element, deprecated_member_use, deprecated_member_use_from_same_package, use_function_type_syntax_for_parameters, unnecessary_const, avoid_init_to_null, invalid_override_different_default_values_named, prefer_expression_function_bodies, annotate_overrides, invalid_annotation_target, unnecessary_question_mark

part of 'composite_theme_extension.dart';

// **************************************************************************
// TailorAnnotationsGenerator
// **************************************************************************

mixin _$CompositeThemeExtensionTailorMixin
    on ThemeExtension<CompositeThemeExtension> {
  Color get backgroundColor;
  Color get disabledBackgroundColor;
  Color get borderColor;
  Color get focusedBorderColor;
  double get borderWidth;
  double get borderRadius;
  double get contentPadding;
  Color get workbenchAreaGapColor;
  Color get panelBorderColor;
  double get panelBorderWidth;
  double get panelBorderRadius;
  double get panelChildGap;
  Color get panelShadowColor;
  double get panelShadowBlurRadius;
  double get panelShadowDx;
  double get panelShadowDy;

  @override
  CompositeThemeExtension copyWith({
    Color? backgroundColor,
    Color? disabledBackgroundColor,
    Color? borderColor,
    Color? focusedBorderColor,
    double? borderWidth,
    double? borderRadius,
    double? contentPadding,
    Color? workbenchAreaGapColor,
    Color? panelBorderColor,
    double? panelBorderWidth,
    double? panelBorderRadius,
    double? panelChildGap,
    Color? panelShadowColor,
    double? panelShadowBlurRadius,
    double? panelShadowDx,
    double? panelShadowDy,
  }) {
    return CompositeThemeExtension(
      backgroundColor: backgroundColor ?? this.backgroundColor,
      disabledBackgroundColor:
          disabledBackgroundColor ?? this.disabledBackgroundColor,
      borderColor: borderColor ?? this.borderColor,
      focusedBorderColor: focusedBorderColor ?? this.focusedBorderColor,
      borderWidth: borderWidth ?? this.borderWidth,
      borderRadius: borderRadius ?? this.borderRadius,
      contentPadding: contentPadding ?? this.contentPadding,
      workbenchAreaGapColor:
          workbenchAreaGapColor ?? this.workbenchAreaGapColor,
      panelBorderColor: panelBorderColor ?? this.panelBorderColor,
      panelBorderWidth: panelBorderWidth ?? this.panelBorderWidth,
      panelBorderRadius: panelBorderRadius ?? this.panelBorderRadius,
      panelChildGap: panelChildGap ?? this.panelChildGap,
      panelShadowColor: panelShadowColor ?? this.panelShadowColor,
      panelShadowBlurRadius:
          panelShadowBlurRadius ?? this.panelShadowBlurRadius,
      panelShadowDx: panelShadowDx ?? this.panelShadowDx,
      panelShadowDy: panelShadowDy ?? this.panelShadowDy,
    );
  }

  @override
  CompositeThemeExtension lerp(
    covariant ThemeExtension<CompositeThemeExtension>? other,
    double t,
  ) {
    if (other is! CompositeThemeExtension)
      return this as CompositeThemeExtension;
    return CompositeThemeExtension(
      backgroundColor: Color.lerp(backgroundColor, other.backgroundColor, t)!,
      disabledBackgroundColor: Color.lerp(
        disabledBackgroundColor,
        other.disabledBackgroundColor,
        t,
      )!,
      borderColor: Color.lerp(borderColor, other.borderColor, t)!,
      focusedBorderColor: Color.lerp(
        focusedBorderColor,
        other.focusedBorderColor,
        t,
      )!,
      borderWidth: t < 0.5 ? borderWidth : other.borderWidth,
      borderRadius: t < 0.5 ? borderRadius : other.borderRadius,
      contentPadding: t < 0.5 ? contentPadding : other.contentPadding,
      workbenchAreaGapColor: Color.lerp(
        workbenchAreaGapColor,
        other.workbenchAreaGapColor,
        t,
      )!,
      panelBorderColor: Color.lerp(
        panelBorderColor,
        other.panelBorderColor,
        t,
      )!,
      panelBorderWidth: t < 0.5 ? panelBorderWidth : other.panelBorderWidth,
      panelBorderRadius: t < 0.5 ? panelBorderRadius : other.panelBorderRadius,
      panelChildGap: t < 0.5 ? panelChildGap : other.panelChildGap,
      panelShadowColor: Color.lerp(
        panelShadowColor,
        other.panelShadowColor,
        t,
      )!,
      panelShadowBlurRadius: t < 0.5
          ? panelShadowBlurRadius
          : other.panelShadowBlurRadius,
      panelShadowDx: t < 0.5 ? panelShadowDx : other.panelShadowDx,
      panelShadowDy: t < 0.5 ? panelShadowDy : other.panelShadowDy,
    );
  }

  @override
  bool operator ==(Object other) {
    return identical(this, other) ||
        (other.runtimeType == runtimeType &&
            other is CompositeThemeExtension &&
            const DeepCollectionEquality().equals(
              backgroundColor,
              other.backgroundColor,
            ) &&
            const DeepCollectionEquality().equals(
              disabledBackgroundColor,
              other.disabledBackgroundColor,
            ) &&
            const DeepCollectionEquality().equals(
              borderColor,
              other.borderColor,
            ) &&
            const DeepCollectionEquality().equals(
              focusedBorderColor,
              other.focusedBorderColor,
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
              contentPadding,
              other.contentPadding,
            ) &&
            const DeepCollectionEquality().equals(
              workbenchAreaGapColor,
              other.workbenchAreaGapColor,
            ) &&
            const DeepCollectionEquality().equals(
              panelBorderColor,
              other.panelBorderColor,
            ) &&
            const DeepCollectionEquality().equals(
              panelBorderWidth,
              other.panelBorderWidth,
            ) &&
            const DeepCollectionEquality().equals(
              panelBorderRadius,
              other.panelBorderRadius,
            ) &&
            const DeepCollectionEquality().equals(
              panelChildGap,
              other.panelChildGap,
            ) &&
            const DeepCollectionEquality().equals(
              panelShadowColor,
              other.panelShadowColor,
            ) &&
            const DeepCollectionEquality().equals(
              panelShadowBlurRadius,
              other.panelShadowBlurRadius,
            ) &&
            const DeepCollectionEquality().equals(
              panelShadowDx,
              other.panelShadowDx,
            ) &&
            const DeepCollectionEquality().equals(
              panelShadowDy,
              other.panelShadowDy,
            ));
  }

  @override
  int get hashCode {
    return Object.hash(
      runtimeType.hashCode,
      const DeepCollectionEquality().hash(backgroundColor),
      const DeepCollectionEquality().hash(disabledBackgroundColor),
      const DeepCollectionEquality().hash(borderColor),
      const DeepCollectionEquality().hash(focusedBorderColor),
      const DeepCollectionEquality().hash(borderWidth),
      const DeepCollectionEquality().hash(borderRadius),
      const DeepCollectionEquality().hash(contentPadding),
      const DeepCollectionEquality().hash(workbenchAreaGapColor),
      const DeepCollectionEquality().hash(panelBorderColor),
      const DeepCollectionEquality().hash(panelBorderWidth),
      const DeepCollectionEquality().hash(panelBorderRadius),
      const DeepCollectionEquality().hash(panelChildGap),
      const DeepCollectionEquality().hash(panelShadowColor),
      const DeepCollectionEquality().hash(panelShadowBlurRadius),
      const DeepCollectionEquality().hash(panelShadowDx),
      const DeepCollectionEquality().hash(panelShadowDy),
    );
  }
}

extension CompositeThemeExtensionBuildContextProps on BuildContext {
  CompositeThemeExtension get compositeThemeExtension =>
      Theme.of(this).extension<CompositeThemeExtension>()!;
  Color get backgroundColor => compositeThemeExtension.backgroundColor;
  Color get disabledBackgroundColor =>
      compositeThemeExtension.disabledBackgroundColor;
  Color get borderColor => compositeThemeExtension.borderColor;
  Color get focusedBorderColor => compositeThemeExtension.focusedBorderColor;
  double get borderWidth => compositeThemeExtension.borderWidth;
  double get borderRadius => compositeThemeExtension.borderRadius;
  double get contentPadding => compositeThemeExtension.contentPadding;
  Color get workbenchAreaGapColor =>
      compositeThemeExtension.workbenchAreaGapColor;
  Color get panelBorderColor => compositeThemeExtension.panelBorderColor;
  double get panelBorderWidth => compositeThemeExtension.panelBorderWidth;
  double get panelBorderRadius => compositeThemeExtension.panelBorderRadius;
  double get panelChildGap => compositeThemeExtension.panelChildGap;
  Color get panelShadowColor => compositeThemeExtension.panelShadowColor;
  double get panelShadowBlurRadius =>
      compositeThemeExtension.panelShadowBlurRadius;
  double get panelShadowDx => compositeThemeExtension.panelShadowDx;
  double get panelShadowDy => compositeThemeExtension.panelShadowDy;
}
