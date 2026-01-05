// coverage:ignore-file
// GENERATED CODE - DO NOT MODIFY BY HAND
// ignore_for_file: type=lint
// ignore_for_file: unused_element, deprecated_member_use, deprecated_member_use_from_same_package, use_function_type_syntax_for_parameters, unnecessary_const, avoid_init_to_null, invalid_override_different_default_values_named, prefer_expression_function_bodies, annotate_overrides, invalid_annotation_target, unnecessary_question_mark

part of 'ctabitem_theme_extension.dart';

// **************************************************************************
// TailorAnnotationsGenerator
// **************************************************************************

mixin _$CTabItemThemeExtensionTailorMixin
    on ThemeExtension<CTabItemThemeExtension> {
  Color get tabItemTextColor;
  Color get tabItemSelectedTextColor;
  Color get tabItemDisabledTextColor;
  TextStyle? get tabItemTextStyle;
  TextStyle? get tabItemSelectedTextStyle;
  double get tabItemHorizontalPadding;
  double get tabItemVerticalPadding;
  double get tabItemImageTextSpacing;

  @override
  CTabItemThemeExtension copyWith({
    Color? tabItemTextColor,
    Color? tabItemSelectedTextColor,
    Color? tabItemDisabledTextColor,
    TextStyle? tabItemTextStyle,
    TextStyle? tabItemSelectedTextStyle,
    double? tabItemHorizontalPadding,
    double? tabItemVerticalPadding,
    double? tabItemImageTextSpacing,
  }) {
    return CTabItemThemeExtension(
      tabItemTextColor: tabItemTextColor ?? this.tabItemTextColor,
      tabItemSelectedTextColor:
          tabItemSelectedTextColor ?? this.tabItemSelectedTextColor,
      tabItemDisabledTextColor:
          tabItemDisabledTextColor ?? this.tabItemDisabledTextColor,
      tabItemTextStyle: tabItemTextStyle ?? this.tabItemTextStyle,
      tabItemSelectedTextStyle:
          tabItemSelectedTextStyle ?? this.tabItemSelectedTextStyle,
      tabItemHorizontalPadding:
          tabItemHorizontalPadding ?? this.tabItemHorizontalPadding,
      tabItemVerticalPadding:
          tabItemVerticalPadding ?? this.tabItemVerticalPadding,
      tabItemImageTextSpacing:
          tabItemImageTextSpacing ?? this.tabItemImageTextSpacing,
    );
  }

  @override
  CTabItemThemeExtension lerp(
      covariant ThemeExtension<CTabItemThemeExtension>? other, double t) {
    if (other is! CTabItemThemeExtension) return this as CTabItemThemeExtension;
    return CTabItemThemeExtension(
      tabItemTextColor:
          Color.lerp(tabItemTextColor, other.tabItemTextColor, t)!,
      tabItemSelectedTextColor: Color.lerp(
          tabItemSelectedTextColor, other.tabItemSelectedTextColor, t)!,
      tabItemDisabledTextColor: Color.lerp(
          tabItemDisabledTextColor, other.tabItemDisabledTextColor, t)!,
      tabItemTextStyle:
          TextStyle.lerp(tabItemTextStyle, other.tabItemTextStyle, t),
      tabItemSelectedTextStyle: TextStyle.lerp(
          tabItemSelectedTextStyle, other.tabItemSelectedTextStyle, t),
      tabItemHorizontalPadding:
          t < 0.5 ? tabItemHorizontalPadding : other.tabItemHorizontalPadding,
      tabItemVerticalPadding:
          t < 0.5 ? tabItemVerticalPadding : other.tabItemVerticalPadding,
      tabItemImageTextSpacing:
          t < 0.5 ? tabItemImageTextSpacing : other.tabItemImageTextSpacing,
    );
  }

  @override
  bool operator ==(Object other) {
    return identical(this, other) ||
        (other.runtimeType == runtimeType &&
            other is CTabItemThemeExtension &&
            const DeepCollectionEquality()
                .equals(tabItemTextColor, other.tabItemTextColor) &&
            const DeepCollectionEquality().equals(
                tabItemSelectedTextColor, other.tabItemSelectedTextColor) &&
            const DeepCollectionEquality().equals(
                tabItemDisabledTextColor, other.tabItemDisabledTextColor) &&
            const DeepCollectionEquality()
                .equals(tabItemTextStyle, other.tabItemTextStyle) &&
            const DeepCollectionEquality().equals(
                tabItemSelectedTextStyle, other.tabItemSelectedTextStyle) &&
            const DeepCollectionEquality().equals(
                tabItemHorizontalPadding, other.tabItemHorizontalPadding) &&
            const DeepCollectionEquality()
                .equals(tabItemVerticalPadding, other.tabItemVerticalPadding) &&
            const DeepCollectionEquality().equals(
                tabItemImageTextSpacing, other.tabItemImageTextSpacing));
  }

  @override
  int get hashCode {
    return Object.hash(
      runtimeType.hashCode,
      const DeepCollectionEquality().hash(tabItemTextColor),
      const DeepCollectionEquality().hash(tabItemSelectedTextColor),
      const DeepCollectionEquality().hash(tabItemDisabledTextColor),
      const DeepCollectionEquality().hash(tabItemTextStyle),
      const DeepCollectionEquality().hash(tabItemSelectedTextStyle),
      const DeepCollectionEquality().hash(tabItemHorizontalPadding),
      const DeepCollectionEquality().hash(tabItemVerticalPadding),
      const DeepCollectionEquality().hash(tabItemImageTextSpacing),
    );
  }
}

extension CTabItemThemeExtensionBuildContextProps on BuildContext {
  CTabItemThemeExtension get cTabItemThemeExtension =>
      Theme.of(this).extension<CTabItemThemeExtension>()!;
  Color get tabItemTextColor => cTabItemThemeExtension.tabItemTextColor;
  Color get tabItemSelectedTextColor =>
      cTabItemThemeExtension.tabItemSelectedTextColor;
  Color get tabItemDisabledTextColor =>
      cTabItemThemeExtension.tabItemDisabledTextColor;
  TextStyle? get tabItemTextStyle => cTabItemThemeExtension.tabItemTextStyle;
  TextStyle? get tabItemSelectedTextStyle =>
      cTabItemThemeExtension.tabItemSelectedTextStyle;
  double get tabItemHorizontalPadding =>
      cTabItemThemeExtension.tabItemHorizontalPadding;
  double get tabItemVerticalPadding =>
      cTabItemThemeExtension.tabItemVerticalPadding;
  double get tabItemImageTextSpacing =>
      cTabItemThemeExtension.tabItemImageTextSpacing;
}
