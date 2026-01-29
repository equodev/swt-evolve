// coverage:ignore-file
// GENERATED CODE - DO NOT MODIFY BY HAND
// ignore_for_file: type=lint
// ignore_for_file: unused_element, deprecated_member_use, deprecated_member_use_from_same_package, use_function_type_syntax_for_parameters, unnecessary_const, avoid_init_to_null, invalid_override_different_default_values_named, prefer_expression_function_bodies, annotate_overrides, invalid_annotation_target, unnecessary_question_mark

part of 'coolbar_theme_extension.dart';

// **************************************************************************
// TailorAnnotationsGenerator
// **************************************************************************

mixin _$CoolBarThemeExtensionTailorMixin
    on ThemeExtension<CoolBarThemeExtension> {
  Color get backgroundColor;
  Color get borderColor;
  double get borderWidth;
  double get borderRadius;
  EdgeInsets get padding;
  Duration get animationDuration;

  @override
  CoolBarThemeExtension copyWith({
    Color? backgroundColor,
    Color? borderColor,
    double? borderWidth,
    double? borderRadius,
    EdgeInsets? padding,
    Duration? animationDuration,
  }) {
    return CoolBarThemeExtension(
      backgroundColor: backgroundColor ?? this.backgroundColor,
      borderColor: borderColor ?? this.borderColor,
      borderWidth: borderWidth ?? this.borderWidth,
      borderRadius: borderRadius ?? this.borderRadius,
      padding: padding ?? this.padding,
      animationDuration: animationDuration ?? this.animationDuration,
    );
  }

  @override
  CoolBarThemeExtension lerp(
    covariant ThemeExtension<CoolBarThemeExtension>? other,
    double t,
  ) {
    if (other is! CoolBarThemeExtension) return this as CoolBarThemeExtension;
    return CoolBarThemeExtension(
      backgroundColor: Color.lerp(backgroundColor, other.backgroundColor, t)!,
      borderColor: Color.lerp(borderColor, other.borderColor, t)!,
      borderWidth: t < 0.5 ? borderWidth : other.borderWidth,
      borderRadius: t < 0.5 ? borderRadius : other.borderRadius,
      padding: t < 0.5 ? padding : other.padding,
      animationDuration: t < 0.5 ? animationDuration : other.animationDuration,
    );
  }

  @override
  bool operator ==(Object other) {
    return identical(this, other) ||
        (other.runtimeType == runtimeType &&
            other is CoolBarThemeExtension &&
            const DeepCollectionEquality().equals(
              backgroundColor,
              other.backgroundColor,
            ) &&
            const DeepCollectionEquality().equals(
              borderColor,
              other.borderColor,
            ) &&
            const DeepCollectionEquality().equals(
              borderWidth,
              other.borderWidth,
            ) &&
            const DeepCollectionEquality().equals(
              borderRadius,
              other.borderRadius,
            ) &&
            const DeepCollectionEquality().equals(padding, other.padding) &&
            const DeepCollectionEquality().equals(
              animationDuration,
              other.animationDuration,
            ));
  }

  @override
  int get hashCode {
    return Object.hash(
      runtimeType.hashCode,
      const DeepCollectionEquality().hash(backgroundColor),
      const DeepCollectionEquality().hash(borderColor),
      const DeepCollectionEquality().hash(borderWidth),
      const DeepCollectionEquality().hash(borderRadius),
      const DeepCollectionEquality().hash(padding),
      const DeepCollectionEquality().hash(animationDuration),
    );
  }
}

extension CoolBarThemeExtensionBuildContextProps on BuildContext {
  CoolBarThemeExtension get coolBarThemeExtension =>
      Theme.of(this).extension<CoolBarThemeExtension>()!;
  Color get backgroundColor => coolBarThemeExtension.backgroundColor;
  Color get borderColor => coolBarThemeExtension.borderColor;
  double get borderWidth => coolBarThemeExtension.borderWidth;
  double get borderRadius => coolBarThemeExtension.borderRadius;
  EdgeInsets get padding => coolBarThemeExtension.padding;
  Duration get animationDuration => coolBarThemeExtension.animationDuration;
}
