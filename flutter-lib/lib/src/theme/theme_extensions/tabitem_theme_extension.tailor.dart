// coverage:ignore-file
// GENERATED CODE - DO NOT MODIFY BY HAND
// ignore_for_file: type=lint
// ignore_for_file: unused_element, deprecated_member_use, deprecated_member_use_from_same_package, use_function_type_syntax_for_parameters, unnecessary_const, avoid_init_to_null, invalid_override_different_default_values_named, prefer_expression_function_bodies, annotate_overrides, invalid_annotation_target, unnecessary_question_mark

part of 'tabitem_theme_extension.dart';

// **************************************************************************
// TailorAnnotationsGenerator
// **************************************************************************

mixin _$TabItemThemeExtensionTailorMixin
    on ThemeExtension<TabItemThemeExtension> {
  Color get textColor;
  Color get disabledTextColor;
  double get iconSize;
  EdgeInsets get containerPadding;
  EdgeInsets get imagePadding;
  EdgeInsets get textPadding;
  double get imageTextSpacing;
  TextStyle? get textStyle;

  @override
  TabItemThemeExtension copyWith({
    Color? textColor,
    Color? disabledTextColor,
    double? iconSize,
    EdgeInsets? containerPadding,
    EdgeInsets? imagePadding,
    EdgeInsets? textPadding,
    double? imageTextSpacing,
    TextStyle? textStyle,
  }) {
    return TabItemThemeExtension(
      textColor: textColor ?? this.textColor,
      disabledTextColor: disabledTextColor ?? this.disabledTextColor,
      iconSize: iconSize ?? this.iconSize,
      containerPadding: containerPadding ?? this.containerPadding,
      imagePadding: imagePadding ?? this.imagePadding,
      textPadding: textPadding ?? this.textPadding,
      imageTextSpacing: imageTextSpacing ?? this.imageTextSpacing,
      textStyle: textStyle ?? this.textStyle,
    );
  }

  @override
  TabItemThemeExtension lerp(
      covariant ThemeExtension<TabItemThemeExtension>? other, double t) {
    if (other is! TabItemThemeExtension) return this as TabItemThemeExtension;
    return TabItemThemeExtension(
      textColor: Color.lerp(textColor, other.textColor, t)!,
      disabledTextColor:
          Color.lerp(disabledTextColor, other.disabledTextColor, t)!,
      iconSize: t < 0.5 ? iconSize : other.iconSize,
      containerPadding: t < 0.5 ? containerPadding : other.containerPadding,
      imagePadding: t < 0.5 ? imagePadding : other.imagePadding,
      textPadding: t < 0.5 ? textPadding : other.textPadding,
      imageTextSpacing: t < 0.5 ? imageTextSpacing : other.imageTextSpacing,
      textStyle: TextStyle.lerp(textStyle, other.textStyle, t),
    );
  }

  @override
  bool operator ==(Object other) {
    return identical(this, other) ||
        (other.runtimeType == runtimeType &&
            other is TabItemThemeExtension &&
            const DeepCollectionEquality().equals(textColor, other.textColor) &&
            const DeepCollectionEquality()
                .equals(disabledTextColor, other.disabledTextColor) &&
            const DeepCollectionEquality().equals(iconSize, other.iconSize) &&
            const DeepCollectionEquality()
                .equals(containerPadding, other.containerPadding) &&
            const DeepCollectionEquality()
                .equals(imagePadding, other.imagePadding) &&
            const DeepCollectionEquality()
                .equals(textPadding, other.textPadding) &&
            const DeepCollectionEquality()
                .equals(imageTextSpacing, other.imageTextSpacing) &&
            const DeepCollectionEquality().equals(textStyle, other.textStyle));
  }

  @override
  int get hashCode {
    return Object.hash(
      runtimeType.hashCode,
      const DeepCollectionEquality().hash(textColor),
      const DeepCollectionEquality().hash(disabledTextColor),
      const DeepCollectionEquality().hash(iconSize),
      const DeepCollectionEquality().hash(containerPadding),
      const DeepCollectionEquality().hash(imagePadding),
      const DeepCollectionEquality().hash(textPadding),
      const DeepCollectionEquality().hash(imageTextSpacing),
      const DeepCollectionEquality().hash(textStyle),
    );
  }
}

extension TabItemThemeExtensionBuildContextProps on BuildContext {
  TabItemThemeExtension get tabItemThemeExtension =>
      Theme.of(this).extension<TabItemThemeExtension>()!;
  Color get textColor => tabItemThemeExtension.textColor;
  Color get disabledTextColor => tabItemThemeExtension.disabledTextColor;
  double get iconSize => tabItemThemeExtension.iconSize;
  EdgeInsets get containerPadding => tabItemThemeExtension.containerPadding;
  EdgeInsets get imagePadding => tabItemThemeExtension.imagePadding;
  EdgeInsets get textPadding => tabItemThemeExtension.textPadding;
  double get imageTextSpacing => tabItemThemeExtension.imageTextSpacing;
  TextStyle? get textStyle => tabItemThemeExtension.textStyle;
}
