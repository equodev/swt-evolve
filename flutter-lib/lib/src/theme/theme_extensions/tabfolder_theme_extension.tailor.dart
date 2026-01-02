// coverage:ignore-file
// GENERATED CODE - DO NOT MODIFY BY HAND
// ignore_for_file: type=lint
// ignore_for_file: unused_element, deprecated_member_use, deprecated_member_use_from_same_package, use_function_type_syntax_for_parameters, unnecessary_const, avoid_init_to_null, invalid_override_different_default_values_named, prefer_expression_function_bodies, annotate_overrides, invalid_annotation_target, unnecessary_question_mark

part of 'tabfolder_theme_extension.dart';

// **************************************************************************
// TailorAnnotationsGenerator
// **************************************************************************

mixin _$TabFolderThemeExtensionTailorMixin
    on ThemeExtension<TabFolderThemeExtension> {
  Color get tabBarBackgroundColor;
  Color get tabBarBorderColor;
  Color get tabBackgroundColor;
  Color get tabSelectedBackgroundColor;
  Color get tabHoverBackgroundColor;
  Color get tabDisabledBackgroundColor;
  Color get tabBorderColor;
  Color get tabSelectedBorderColor;
  Color get tabHoverBorderColor;
  Color get tabDisabledBorderColor;
  Color get tabTextColor;
  Color get tabSelectedTextColor;
  Color get tabHoverTextColor;
  Color get tabDisabledTextColor;
  double get tabBorderWidth;
  double get tabSelectedBorderWidth;
  double get tabBorderRadius;
  double get tabPadding;
  TextStyle? get tabTextStyle;
  TextStyle? get tabSelectedTextStyle;
  Color get tabContentBackgroundColor;
  Color get tabContentBorderColor;

  @override
  TabFolderThemeExtension copyWith({
    Color? tabBarBackgroundColor,
    Color? tabBarBorderColor,
    Color? tabBackgroundColor,
    Color? tabSelectedBackgroundColor,
    Color? tabHoverBackgroundColor,
    Color? tabDisabledBackgroundColor,
    Color? tabBorderColor,
    Color? tabSelectedBorderColor,
    Color? tabHoverBorderColor,
    Color? tabDisabledBorderColor,
    Color? tabTextColor,
    Color? tabSelectedTextColor,
    Color? tabHoverTextColor,
    Color? tabDisabledTextColor,
    double? tabBorderWidth,
    double? tabSelectedBorderWidth,
    double? tabBorderRadius,
    double? tabPadding,
    TextStyle? tabTextStyle,
    TextStyle? tabSelectedTextStyle,
    Color? tabContentBackgroundColor,
    Color? tabContentBorderColor,
  }) {
    return TabFolderThemeExtension(
      tabBarBackgroundColor:
          tabBarBackgroundColor ?? this.tabBarBackgroundColor,
      tabBarBorderColor: tabBarBorderColor ?? this.tabBarBorderColor,
      tabBackgroundColor: tabBackgroundColor ?? this.tabBackgroundColor,
      tabSelectedBackgroundColor:
          tabSelectedBackgroundColor ?? this.tabSelectedBackgroundColor,
      tabHoverBackgroundColor:
          tabHoverBackgroundColor ?? this.tabHoverBackgroundColor,
      tabDisabledBackgroundColor:
          tabDisabledBackgroundColor ?? this.tabDisabledBackgroundColor,
      tabBorderColor: tabBorderColor ?? this.tabBorderColor,
      tabSelectedBorderColor:
          tabSelectedBorderColor ?? this.tabSelectedBorderColor,
      tabHoverBorderColor: tabHoverBorderColor ?? this.tabHoverBorderColor,
      tabDisabledBorderColor:
          tabDisabledBorderColor ?? this.tabDisabledBorderColor,
      tabTextColor: tabTextColor ?? this.tabTextColor,
      tabSelectedTextColor: tabSelectedTextColor ?? this.tabSelectedTextColor,
      tabHoverTextColor: tabHoverTextColor ?? this.tabHoverTextColor,
      tabDisabledTextColor: tabDisabledTextColor ?? this.tabDisabledTextColor,
      tabBorderWidth: tabBorderWidth ?? this.tabBorderWidth,
      tabSelectedBorderWidth:
          tabSelectedBorderWidth ?? this.tabSelectedBorderWidth,
      tabBorderRadius: tabBorderRadius ?? this.tabBorderRadius,
      tabPadding: tabPadding ?? this.tabPadding,
      tabTextStyle: tabTextStyle ?? this.tabTextStyle,
      tabSelectedTextStyle: tabSelectedTextStyle ?? this.tabSelectedTextStyle,
      tabContentBackgroundColor:
          tabContentBackgroundColor ?? this.tabContentBackgroundColor,
      tabContentBorderColor:
          tabContentBorderColor ?? this.tabContentBorderColor,
    );
  }

  @override
  TabFolderThemeExtension lerp(
      covariant ThemeExtension<TabFolderThemeExtension>? other, double t) {
    if (other is! TabFolderThemeExtension)
      return this as TabFolderThemeExtension;
    return TabFolderThemeExtension(
      tabBarBackgroundColor:
          Color.lerp(tabBarBackgroundColor, other.tabBarBackgroundColor, t)!,
      tabBarBorderColor:
          Color.lerp(tabBarBorderColor, other.tabBarBorderColor, t)!,
      tabBackgroundColor:
          Color.lerp(tabBackgroundColor, other.tabBackgroundColor, t)!,
      tabSelectedBackgroundColor: Color.lerp(
          tabSelectedBackgroundColor, other.tabSelectedBackgroundColor, t)!,
      tabHoverBackgroundColor: Color.lerp(
          tabHoverBackgroundColor, other.tabHoverBackgroundColor, t)!,
      tabDisabledBackgroundColor: Color.lerp(
          tabDisabledBackgroundColor, other.tabDisabledBackgroundColor, t)!,
      tabBorderColor: Color.lerp(tabBorderColor, other.tabBorderColor, t)!,
      tabSelectedBorderColor:
          Color.lerp(tabSelectedBorderColor, other.tabSelectedBorderColor, t)!,
      tabHoverBorderColor:
          Color.lerp(tabHoverBorderColor, other.tabHoverBorderColor, t)!,
      tabDisabledBorderColor:
          Color.lerp(tabDisabledBorderColor, other.tabDisabledBorderColor, t)!,
      tabTextColor: Color.lerp(tabTextColor, other.tabTextColor, t)!,
      tabSelectedTextColor:
          Color.lerp(tabSelectedTextColor, other.tabSelectedTextColor, t)!,
      tabHoverTextColor:
          Color.lerp(tabHoverTextColor, other.tabHoverTextColor, t)!,
      tabDisabledTextColor:
          Color.lerp(tabDisabledTextColor, other.tabDisabledTextColor, t)!,
      tabBorderWidth: t < 0.5 ? tabBorderWidth : other.tabBorderWidth,
      tabSelectedBorderWidth:
          t < 0.5 ? tabSelectedBorderWidth : other.tabSelectedBorderWidth,
      tabBorderRadius: t < 0.5 ? tabBorderRadius : other.tabBorderRadius,
      tabPadding: t < 0.5 ? tabPadding : other.tabPadding,
      tabTextStyle: TextStyle.lerp(tabTextStyle, other.tabTextStyle, t),
      tabSelectedTextStyle:
          TextStyle.lerp(tabSelectedTextStyle, other.tabSelectedTextStyle, t),
      tabContentBackgroundColor: Color.lerp(
          tabContentBackgroundColor, other.tabContentBackgroundColor, t)!,
      tabContentBorderColor:
          Color.lerp(tabContentBorderColor, other.tabContentBorderColor, t)!,
    );
  }

  @override
  bool operator ==(Object other) {
    return identical(this, other) ||
        (other.runtimeType == runtimeType &&
            other is TabFolderThemeExtension &&
            const DeepCollectionEquality()
                .equals(tabBarBackgroundColor, other.tabBarBackgroundColor) &&
            const DeepCollectionEquality()
                .equals(tabBarBorderColor, other.tabBarBorderColor) &&
            const DeepCollectionEquality()
                .equals(tabBackgroundColor, other.tabBackgroundColor) &&
            const DeepCollectionEquality().equals(
                tabSelectedBackgroundColor, other.tabSelectedBackgroundColor) &&
            const DeepCollectionEquality().equals(
                tabHoverBackgroundColor, other.tabHoverBackgroundColor) &&
            const DeepCollectionEquality().equals(
                tabDisabledBackgroundColor, other.tabDisabledBackgroundColor) &&
            const DeepCollectionEquality()
                .equals(tabBorderColor, other.tabBorderColor) &&
            const DeepCollectionEquality()
                .equals(tabSelectedBorderColor, other.tabSelectedBorderColor) &&
            const DeepCollectionEquality()
                .equals(tabHoverBorderColor, other.tabHoverBorderColor) &&
            const DeepCollectionEquality()
                .equals(tabDisabledBorderColor, other.tabDisabledBorderColor) &&
            const DeepCollectionEquality()
                .equals(tabTextColor, other.tabTextColor) &&
            const DeepCollectionEquality()
                .equals(tabSelectedTextColor, other.tabSelectedTextColor) &&
            const DeepCollectionEquality()
                .equals(tabHoverTextColor, other.tabHoverTextColor) &&
            const DeepCollectionEquality()
                .equals(tabDisabledTextColor, other.tabDisabledTextColor) &&
            const DeepCollectionEquality()
                .equals(tabBorderWidth, other.tabBorderWidth) &&
            const DeepCollectionEquality()
                .equals(tabSelectedBorderWidth, other.tabSelectedBorderWidth) &&
            const DeepCollectionEquality()
                .equals(tabBorderRadius, other.tabBorderRadius) &&
            const DeepCollectionEquality()
                .equals(tabPadding, other.tabPadding) &&
            const DeepCollectionEquality()
                .equals(tabTextStyle, other.tabTextStyle) &&
            const DeepCollectionEquality()
                .equals(tabSelectedTextStyle, other.tabSelectedTextStyle) &&
            const DeepCollectionEquality().equals(
                tabContentBackgroundColor, other.tabContentBackgroundColor) &&
            const DeepCollectionEquality()
                .equals(tabContentBorderColor, other.tabContentBorderColor));
  }

  @override
  int get hashCode {
    return Object.hashAll([
      runtimeType.hashCode,
      const DeepCollectionEquality().hash(tabBarBackgroundColor),
      const DeepCollectionEquality().hash(tabBarBorderColor),
      const DeepCollectionEquality().hash(tabBackgroundColor),
      const DeepCollectionEquality().hash(tabSelectedBackgroundColor),
      const DeepCollectionEquality().hash(tabHoverBackgroundColor),
      const DeepCollectionEquality().hash(tabDisabledBackgroundColor),
      const DeepCollectionEquality().hash(tabBorderColor),
      const DeepCollectionEquality().hash(tabSelectedBorderColor),
      const DeepCollectionEquality().hash(tabHoverBorderColor),
      const DeepCollectionEquality().hash(tabDisabledBorderColor),
      const DeepCollectionEquality().hash(tabTextColor),
      const DeepCollectionEquality().hash(tabSelectedTextColor),
      const DeepCollectionEquality().hash(tabHoverTextColor),
      const DeepCollectionEquality().hash(tabDisabledTextColor),
      const DeepCollectionEquality().hash(tabBorderWidth),
      const DeepCollectionEquality().hash(tabSelectedBorderWidth),
      const DeepCollectionEquality().hash(tabBorderRadius),
      const DeepCollectionEquality().hash(tabPadding),
      const DeepCollectionEquality().hash(tabTextStyle),
      const DeepCollectionEquality().hash(tabSelectedTextStyle),
      const DeepCollectionEquality().hash(tabContentBackgroundColor),
      const DeepCollectionEquality().hash(tabContentBorderColor),
    ]);
  }
}

extension TabFolderThemeExtensionBuildContextProps on BuildContext {
  TabFolderThemeExtension get tabFolderThemeExtension =>
      Theme.of(this).extension<TabFolderThemeExtension>()!;
  Color get tabBarBackgroundColor =>
      tabFolderThemeExtension.tabBarBackgroundColor;
  Color get tabBarBorderColor => tabFolderThemeExtension.tabBarBorderColor;
  Color get tabBackgroundColor => tabFolderThemeExtension.tabBackgroundColor;
  Color get tabSelectedBackgroundColor =>
      tabFolderThemeExtension.tabSelectedBackgroundColor;
  Color get tabHoverBackgroundColor =>
      tabFolderThemeExtension.tabHoverBackgroundColor;
  Color get tabDisabledBackgroundColor =>
      tabFolderThemeExtension.tabDisabledBackgroundColor;
  Color get tabBorderColor => tabFolderThemeExtension.tabBorderColor;
  Color get tabSelectedBorderColor =>
      tabFolderThemeExtension.tabSelectedBorderColor;
  Color get tabHoverBorderColor => tabFolderThemeExtension.tabHoverBorderColor;
  Color get tabDisabledBorderColor =>
      tabFolderThemeExtension.tabDisabledBorderColor;
  Color get tabTextColor => tabFolderThemeExtension.tabTextColor;
  Color get tabSelectedTextColor =>
      tabFolderThemeExtension.tabSelectedTextColor;
  Color get tabHoverTextColor => tabFolderThemeExtension.tabHoverTextColor;
  Color get tabDisabledTextColor =>
      tabFolderThemeExtension.tabDisabledTextColor;
  double get tabBorderWidth => tabFolderThemeExtension.tabBorderWidth;
  double get tabSelectedBorderWidth =>
      tabFolderThemeExtension.tabSelectedBorderWidth;
  double get tabBorderRadius => tabFolderThemeExtension.tabBorderRadius;
  double get tabPadding => tabFolderThemeExtension.tabPadding;
  TextStyle? get tabTextStyle => tabFolderThemeExtension.tabTextStyle;
  TextStyle? get tabSelectedTextStyle =>
      tabFolderThemeExtension.tabSelectedTextStyle;
  Color get tabContentBackgroundColor =>
      tabFolderThemeExtension.tabContentBackgroundColor;
  Color get tabContentBorderColor =>
      tabFolderThemeExtension.tabContentBorderColor;
}
