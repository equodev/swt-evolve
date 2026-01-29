// coverage:ignore-file
// GENERATED CODE - DO NOT MODIFY BY HAND
// ignore_for_file: type=lint
// ignore_for_file: unused_element, deprecated_member_use, deprecated_member_use_from_same_package, use_function_type_syntax_for_parameters, unnecessary_const, avoid_init_to_null, invalid_override_different_default_values_named, prefer_expression_function_bodies, annotate_overrides, invalid_annotation_target, unnecessary_question_mark

part of 'coolitem_theme_extension.dart';

// **************************************************************************
// TailorAnnotationsGenerator
// **************************************************************************

mixin _$CoolItemThemeExtensionTailorMixin
    on ThemeExtension<CoolItemThemeExtension> {
  TextStyle? get textStyle;
  EdgeInsets get contentPadding;

  @override
  CoolItemThemeExtension copyWith({
    TextStyle? textStyle,
    EdgeInsets? contentPadding,
  }) {
    return CoolItemThemeExtension(
      textStyle: textStyle ?? this.textStyle,
      contentPadding: contentPadding ?? this.contentPadding,
    );
  }

  @override
  CoolItemThemeExtension lerp(
    covariant ThemeExtension<CoolItemThemeExtension>? other,
    double t,
  ) {
    if (other is! CoolItemThemeExtension) return this as CoolItemThemeExtension;
    return CoolItemThemeExtension(
      textStyle: TextStyle.lerp(textStyle, other.textStyle, t),
      contentPadding: t < 0.5 ? contentPadding : other.contentPadding,
    );
  }

  @override
  bool operator ==(Object other) {
    return identical(this, other) ||
        (other.runtimeType == runtimeType &&
            other is CoolItemThemeExtension &&
            const DeepCollectionEquality().equals(textStyle, other.textStyle) &&
            const DeepCollectionEquality().equals(
              contentPadding,
              other.contentPadding,
            ));
  }

  @override
  int get hashCode {
    return Object.hash(
      runtimeType.hashCode,
      const DeepCollectionEquality().hash(textStyle),
      const DeepCollectionEquality().hash(contentPadding),
    );
  }
}

extension CoolItemThemeExtensionBuildContextProps on BuildContext {
  CoolItemThemeExtension get coolItemThemeExtension =>
      Theme.of(this).extension<CoolItemThemeExtension>()!;
  TextStyle? get textStyle => coolItemThemeExtension.textStyle;
  EdgeInsets get contentPadding => coolItemThemeExtension.contentPadding;
}
