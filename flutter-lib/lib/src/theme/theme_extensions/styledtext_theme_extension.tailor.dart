// coverage:ignore-file
// GENERATED CODE - DO NOT MODIFY BY HAND
// ignore_for_file: type=lint
// ignore_for_file: unused_element, deprecated_member_use, deprecated_member_use_from_same_package, use_function_type_syntax_for_parameters, unnecessary_const, avoid_init_to_null, invalid_override_different_default_values_named, prefer_expression_function_bodies, annotate_overrides, invalid_annotation_target, unnecessary_question_mark

part of 'styledtext_theme_extension.dart';

// **************************************************************************
// TailorAnnotationsGenerator
// **************************************************************************

mixin _$StyledTextThemeExtensionTailorMixin
    on ThemeExtension<StyledTextThemeExtension> {
  Color get backgroundColor;
  Color get foregroundColor;

  @override
  StyledTextThemeExtension copyWith({
    Color? backgroundColor,
    Color? foregroundColor,
  }) {
    return StyledTextThemeExtension(
      backgroundColor: backgroundColor ?? this.backgroundColor,
      foregroundColor: foregroundColor ?? this.foregroundColor,
    );
  }

  @override
  StyledTextThemeExtension lerp(
    covariant ThemeExtension<StyledTextThemeExtension>? other,
    double t,
  ) {
    if (other is! StyledTextThemeExtension)
      return this as StyledTextThemeExtension;
    return StyledTextThemeExtension(
      backgroundColor: Color.lerp(backgroundColor, other.backgroundColor, t)!,
      foregroundColor: Color.lerp(foregroundColor, other.foregroundColor, t)!,
    );
  }

  @override
  bool operator ==(Object other) {
    return identical(this, other) ||
        (other.runtimeType == runtimeType &&
            other is StyledTextThemeExtension &&
            const DeepCollectionEquality().equals(
              backgroundColor,
              other.backgroundColor,
            ) &&
            const DeepCollectionEquality().equals(
              foregroundColor,
              other.foregroundColor,
            ));
  }

  @override
  int get hashCode {
    return Object.hash(
      runtimeType.hashCode,
      const DeepCollectionEquality().hash(backgroundColor),
      const DeepCollectionEquality().hash(foregroundColor),
    );
  }
}

extension StyledTextThemeExtensionBuildContextProps on BuildContext {
  StyledTextThemeExtension get styledTextThemeExtension =>
      Theme.of(this).extension<StyledTextThemeExtension>()!;
  Color get backgroundColor => styledTextThemeExtension.backgroundColor;
  Color get foregroundColor => styledTextThemeExtension.foregroundColor;
}
