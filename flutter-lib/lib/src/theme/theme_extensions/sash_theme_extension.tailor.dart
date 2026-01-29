// coverage:ignore-file
// GENERATED CODE - DO NOT MODIFY BY HAND
// ignore_for_file: type=lint
// ignore_for_file: unused_element, deprecated_member_use, deprecated_member_use_from_same_package, use_function_type_syntax_for_parameters, unnecessary_const, avoid_init_to_null, invalid_override_different_default_values_named, prefer_expression_function_bodies, annotate_overrides, invalid_annotation_target, unnecessary_question_mark

part of 'sash_theme_extension.dart';

// **************************************************************************
// TailorAnnotationsGenerator
// **************************************************************************

mixin _$SashThemeExtensionTailorMixin on ThemeExtension<SashThemeExtension> {
  Color get backgroundColor;
  Color get sashColor;
  Color get sashHoverColor;
  double get sashCenterOpacity;
  double get sashCenterHoverOpacity;
  double get hitAreaSize;
  double get defaultSize;

  @override
  SashThemeExtension copyWith({
    Color? backgroundColor,
    Color? sashColor,
    Color? sashHoverColor,
    double? sashCenterOpacity,
    double? sashCenterHoverOpacity,
    double? hitAreaSize,
    double? defaultSize,
  }) {
    return SashThemeExtension(
      backgroundColor: backgroundColor ?? this.backgroundColor,
      sashColor: sashColor ?? this.sashColor,
      sashHoverColor: sashHoverColor ?? this.sashHoverColor,
      sashCenterOpacity: sashCenterOpacity ?? this.sashCenterOpacity,
      sashCenterHoverOpacity:
          sashCenterHoverOpacity ?? this.sashCenterHoverOpacity,
      hitAreaSize: hitAreaSize ?? this.hitAreaSize,
      defaultSize: defaultSize ?? this.defaultSize,
    );
  }

  @override
  SashThemeExtension lerp(
    covariant ThemeExtension<SashThemeExtension>? other,
    double t,
  ) {
    if (other is! SashThemeExtension) return this as SashThemeExtension;
    return SashThemeExtension(
      backgroundColor: Color.lerp(backgroundColor, other.backgroundColor, t)!,
      sashColor: Color.lerp(sashColor, other.sashColor, t)!,
      sashHoverColor: Color.lerp(sashHoverColor, other.sashHoverColor, t)!,
      sashCenterOpacity: t < 0.5 ? sashCenterOpacity : other.sashCenterOpacity,
      sashCenterHoverOpacity: t < 0.5
          ? sashCenterHoverOpacity
          : other.sashCenterHoverOpacity,
      hitAreaSize: t < 0.5 ? hitAreaSize : other.hitAreaSize,
      defaultSize: t < 0.5 ? defaultSize : other.defaultSize,
    );
  }

  @override
  bool operator ==(Object other) {
    return identical(this, other) ||
        (other.runtimeType == runtimeType &&
            other is SashThemeExtension &&
            const DeepCollectionEquality().equals(
              backgroundColor,
              other.backgroundColor,
            ) &&
            const DeepCollectionEquality().equals(sashColor, other.sashColor) &&
            const DeepCollectionEquality().equals(
              sashHoverColor,
              other.sashHoverColor,
            ) &&
            const DeepCollectionEquality().equals(
              sashCenterOpacity,
              other.sashCenterOpacity,
            ) &&
            const DeepCollectionEquality().equals(
              sashCenterHoverOpacity,
              other.sashCenterHoverOpacity,
            ) &&
            const DeepCollectionEquality().equals(
              hitAreaSize,
              other.hitAreaSize,
            ) &&
            const DeepCollectionEquality().equals(
              defaultSize,
              other.defaultSize,
            ));
  }

  @override
  int get hashCode {
    return Object.hash(
      runtimeType.hashCode,
      const DeepCollectionEquality().hash(backgroundColor),
      const DeepCollectionEquality().hash(sashColor),
      const DeepCollectionEquality().hash(sashHoverColor),
      const DeepCollectionEquality().hash(sashCenterOpacity),
      const DeepCollectionEquality().hash(sashCenterHoverOpacity),
      const DeepCollectionEquality().hash(hitAreaSize),
      const DeepCollectionEquality().hash(defaultSize),
    );
  }
}

extension SashThemeExtensionBuildContextProps on BuildContext {
  SashThemeExtension get sashThemeExtension =>
      Theme.of(this).extension<SashThemeExtension>()!;
  Color get backgroundColor => sashThemeExtension.backgroundColor;
  Color get sashColor => sashThemeExtension.sashColor;
  Color get sashHoverColor => sashThemeExtension.sashHoverColor;
  double get sashCenterOpacity => sashThemeExtension.sashCenterOpacity;
  double get sashCenterHoverOpacity =>
      sashThemeExtension.sashCenterHoverOpacity;
  double get hitAreaSize => sashThemeExtension.hitAreaSize;
  double get defaultSize => sashThemeExtension.defaultSize;
}
