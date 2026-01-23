// coverage:ignore-file
// GENERATED CODE - DO NOT MODIFY BY HAND
// ignore_for_file: type=lint
// ignore_for_file: unused_element, deprecated_member_use, deprecated_member_use_from_same_package, use_function_type_syntax_for_parameters, unnecessary_const, avoid_init_to_null, invalid_override_different_default_values_named, prefer_expression_function_bodies, annotate_overrides, invalid_annotation_target, unnecessary_question_mark

part of 'clabel_theme_extension.dart';

// **************************************************************************
// TailorAnnotationsGenerator
// **************************************************************************

mixin _$CLabelThemeExtensionTailorMixin
    on ThemeExtension<CLabelThemeExtension> {
  Color get primaryTextColor;
  Color get disabledTextColor;
  Color? get backgroundColor;
  TextStyle? get primaryTextStyle;
  TextStyle? get disabledTextStyle;
  double get iconSize;
  double get iconTextSpacing;
  double get disabledOpacity;
  TextAlign get textAlign;
  MainAxisAlignment get mainAxisAlignment;
  CrossAxisAlignment get crossAxisAlignment;

  @override
  CLabelThemeExtension copyWith({
    Color? primaryTextColor,
    Color? disabledTextColor,
    Color? backgroundColor,
    TextStyle? primaryTextStyle,
    TextStyle? disabledTextStyle,
    double? iconSize,
    double? iconTextSpacing,
    double? disabledOpacity,
    TextAlign? textAlign,
    MainAxisAlignment? mainAxisAlignment,
    CrossAxisAlignment? crossAxisAlignment,
  }) {
    return CLabelThemeExtension(
      primaryTextColor: primaryTextColor ?? this.primaryTextColor,
      disabledTextColor: disabledTextColor ?? this.disabledTextColor,
      backgroundColor: backgroundColor ?? this.backgroundColor,
      primaryTextStyle: primaryTextStyle ?? this.primaryTextStyle,
      disabledTextStyle: disabledTextStyle ?? this.disabledTextStyle,
      iconSize: iconSize ?? this.iconSize,
      iconTextSpacing: iconTextSpacing ?? this.iconTextSpacing,
      disabledOpacity: disabledOpacity ?? this.disabledOpacity,
      textAlign: textAlign ?? this.textAlign,
      mainAxisAlignment: mainAxisAlignment ?? this.mainAxisAlignment,
      crossAxisAlignment: crossAxisAlignment ?? this.crossAxisAlignment,
    );
  }

  @override
  CLabelThemeExtension lerp(
    covariant ThemeExtension<CLabelThemeExtension>? other,
    double t,
  ) {
    if (other is! CLabelThemeExtension) return this as CLabelThemeExtension;
    return CLabelThemeExtension(
      primaryTextColor: Color.lerp(
        primaryTextColor,
        other.primaryTextColor,
        t,
      )!,
      disabledTextColor: Color.lerp(
        disabledTextColor,
        other.disabledTextColor,
        t,
      )!,
      backgroundColor: Color.lerp(backgroundColor, other.backgroundColor, t),
      primaryTextStyle: TextStyle.lerp(
        primaryTextStyle,
        other.primaryTextStyle,
        t,
      ),
      disabledTextStyle: TextStyle.lerp(
        disabledTextStyle,
        other.disabledTextStyle,
        t,
      ),
      iconSize: t < 0.5 ? iconSize : other.iconSize,
      iconTextSpacing: t < 0.5 ? iconTextSpacing : other.iconTextSpacing,
      disabledOpacity: t < 0.5 ? disabledOpacity : other.disabledOpacity,
      textAlign: t < 0.5 ? textAlign : other.textAlign,
      mainAxisAlignment: t < 0.5 ? mainAxisAlignment : other.mainAxisAlignment,
      crossAxisAlignment: t < 0.5
          ? crossAxisAlignment
          : other.crossAxisAlignment,
    );
  }

  @override
  bool operator ==(Object other) {
    return identical(this, other) ||
        (other.runtimeType == runtimeType &&
            other is CLabelThemeExtension &&
            const DeepCollectionEquality().equals(
              primaryTextColor,
              other.primaryTextColor,
            ) &&
            const DeepCollectionEquality().equals(
              disabledTextColor,
              other.disabledTextColor,
            ) &&
            const DeepCollectionEquality().equals(
              backgroundColor,
              other.backgroundColor,
            ) &&
            const DeepCollectionEquality().equals(
              primaryTextStyle,
              other.primaryTextStyle,
            ) &&
            const DeepCollectionEquality().equals(
              disabledTextStyle,
              other.disabledTextStyle,
            ) &&
            const DeepCollectionEquality().equals(iconSize, other.iconSize) &&
            const DeepCollectionEquality().equals(
              iconTextSpacing,
              other.iconTextSpacing,
            ) &&
            const DeepCollectionEquality().equals(
              disabledOpacity,
              other.disabledOpacity,
            ) &&
            const DeepCollectionEquality().equals(textAlign, other.textAlign) &&
            const DeepCollectionEquality().equals(
              mainAxisAlignment,
              other.mainAxisAlignment,
            ) &&
            const DeepCollectionEquality().equals(
              crossAxisAlignment,
              other.crossAxisAlignment,
            ));
  }

  @override
  int get hashCode {
    return Object.hash(
      runtimeType.hashCode,
      const DeepCollectionEquality().hash(primaryTextColor),
      const DeepCollectionEquality().hash(disabledTextColor),
      const DeepCollectionEquality().hash(backgroundColor),
      const DeepCollectionEquality().hash(primaryTextStyle),
      const DeepCollectionEquality().hash(disabledTextStyle),
      const DeepCollectionEquality().hash(iconSize),
      const DeepCollectionEquality().hash(iconTextSpacing),
      const DeepCollectionEquality().hash(disabledOpacity),
      const DeepCollectionEquality().hash(textAlign),
      const DeepCollectionEquality().hash(mainAxisAlignment),
      const DeepCollectionEquality().hash(crossAxisAlignment),
    );
  }
}

extension CLabelThemeExtensionBuildContextProps on BuildContext {
  CLabelThemeExtension get cLabelThemeExtension =>
      Theme.of(this).extension<CLabelThemeExtension>()!;
  Color get primaryTextColor => cLabelThemeExtension.primaryTextColor;
  Color get disabledTextColor => cLabelThemeExtension.disabledTextColor;
  Color? get backgroundColor => cLabelThemeExtension.backgroundColor;
  TextStyle? get primaryTextStyle => cLabelThemeExtension.primaryTextStyle;
  TextStyle? get disabledTextStyle => cLabelThemeExtension.disabledTextStyle;
  double get iconSize => cLabelThemeExtension.iconSize;
  double get iconTextSpacing => cLabelThemeExtension.iconTextSpacing;
  double get disabledOpacity => cLabelThemeExtension.disabledOpacity;
  TextAlign get textAlign => cLabelThemeExtension.textAlign;
  MainAxisAlignment get mainAxisAlignment =>
      cLabelThemeExtension.mainAxisAlignment;
  CrossAxisAlignment get crossAxisAlignment =>
      cLabelThemeExtension.crossAxisAlignment;
}
