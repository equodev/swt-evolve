// coverage:ignore-file
// GENERATED CODE - DO NOT MODIFY BY HAND
// ignore_for_file: type=lint
// ignore_for_file: unused_element, deprecated_member_use, deprecated_member_use_from_same_package, use_function_type_syntax_for_parameters, unnecessary_const, avoid_init_to_null, invalid_override_different_default_values_named, prefer_expression_function_bodies, annotate_overrides, invalid_annotation_target, unnecessary_question_mark

part of 'menu_theme_extension.dart';

// **************************************************************************
// TailorAnnotationsGenerator
// **************************************************************************

mixin _$MenuThemeExtensionTailorMixin on ThemeExtension<MenuThemeExtension> {
  Duration get animationDuration;
  Color get backgroundColor;
  Color get menuBarBackgroundColor;
  Color get popupBackgroundColor;
  Color get hoverBackgroundColor;
  Color get disabledBackgroundColor;
  Color get textColor;
  Color get disabledTextColor;
  Color get borderColor;
  Color get menuBarBorderColor;
  double get borderWidth;
  double get borderRadius;
  Color get disabledBorderColor;
  double get menuBarHeight;
  double get minMenuWidth;
  double get maxMenuWidth;
  EdgeInsets get menuBarItemPadding;
  EdgeInsets get popupPadding;
  EdgeInsets get menuBarItemMargin;
  double get popupElevation;
  TextStyle? get textStyle;

  @override
  MenuThemeExtension copyWith({
    Duration? animationDuration,
    Color? backgroundColor,
    Color? menuBarBackgroundColor,
    Color? popupBackgroundColor,
    Color? hoverBackgroundColor,
    Color? disabledBackgroundColor,
    Color? textColor,
    Color? disabledTextColor,
    Color? borderColor,
    Color? menuBarBorderColor,
    double? borderWidth,
    double? borderRadius,
    Color? disabledBorderColor,
    double? menuBarHeight,
    double? minMenuWidth,
    double? maxMenuWidth,
    EdgeInsets? menuBarItemPadding,
    EdgeInsets? popupPadding,
    EdgeInsets? menuBarItemMargin,
    double? popupElevation,
    TextStyle? textStyle,
  }) {
    return MenuThemeExtension(
      animationDuration: animationDuration ?? this.animationDuration,
      backgroundColor: backgroundColor ?? this.backgroundColor,
      menuBarBackgroundColor:
          menuBarBackgroundColor ?? this.menuBarBackgroundColor,
      popupBackgroundColor: popupBackgroundColor ?? this.popupBackgroundColor,
      hoverBackgroundColor: hoverBackgroundColor ?? this.hoverBackgroundColor,
      disabledBackgroundColor:
          disabledBackgroundColor ?? this.disabledBackgroundColor,
      textColor: textColor ?? this.textColor,
      disabledTextColor: disabledTextColor ?? this.disabledTextColor,
      borderColor: borderColor ?? this.borderColor,
      menuBarBorderColor: menuBarBorderColor ?? this.menuBarBorderColor,
      borderWidth: borderWidth ?? this.borderWidth,
      borderRadius: borderRadius ?? this.borderRadius,
      disabledBorderColor: disabledBorderColor ?? this.disabledBorderColor,
      menuBarHeight: menuBarHeight ?? this.menuBarHeight,
      minMenuWidth: minMenuWidth ?? this.minMenuWidth,
      maxMenuWidth: maxMenuWidth ?? this.maxMenuWidth,
      menuBarItemPadding: menuBarItemPadding ?? this.menuBarItemPadding,
      popupPadding: popupPadding ?? this.popupPadding,
      menuBarItemMargin: menuBarItemMargin ?? this.menuBarItemMargin,
      popupElevation: popupElevation ?? this.popupElevation,
      textStyle: textStyle ?? this.textStyle,
    );
  }

  @override
  MenuThemeExtension lerp(
    covariant ThemeExtension<MenuThemeExtension>? other,
    double t,
  ) {
    if (other is! MenuThemeExtension) return this as MenuThemeExtension;
    return MenuThemeExtension(
      animationDuration: t < 0.5 ? animationDuration : other.animationDuration,
      backgroundColor: Color.lerp(backgroundColor, other.backgroundColor, t)!,
      menuBarBackgroundColor: Color.lerp(
        menuBarBackgroundColor,
        other.menuBarBackgroundColor,
        t,
      )!,
      popupBackgroundColor: Color.lerp(
        popupBackgroundColor,
        other.popupBackgroundColor,
        t,
      )!,
      hoverBackgroundColor: Color.lerp(
        hoverBackgroundColor,
        other.hoverBackgroundColor,
        t,
      )!,
      disabledBackgroundColor: Color.lerp(
        disabledBackgroundColor,
        other.disabledBackgroundColor,
        t,
      )!,
      textColor: Color.lerp(textColor, other.textColor, t)!,
      disabledTextColor: Color.lerp(
        disabledTextColor,
        other.disabledTextColor,
        t,
      )!,
      borderColor: Color.lerp(borderColor, other.borderColor, t)!,
      menuBarBorderColor: Color.lerp(
        menuBarBorderColor,
        other.menuBarBorderColor,
        t,
      )!,
      borderWidth: t < 0.5 ? borderWidth : other.borderWidth,
      borderRadius: t < 0.5 ? borderRadius : other.borderRadius,
      disabledBorderColor: Color.lerp(
        disabledBorderColor,
        other.disabledBorderColor,
        t,
      )!,
      menuBarHeight: t < 0.5 ? menuBarHeight : other.menuBarHeight,
      minMenuWidth: t < 0.5 ? minMenuWidth : other.minMenuWidth,
      maxMenuWidth: t < 0.5 ? maxMenuWidth : other.maxMenuWidth,
      menuBarItemPadding: t < 0.5
          ? menuBarItemPadding
          : other.menuBarItemPadding,
      popupPadding: t < 0.5 ? popupPadding : other.popupPadding,
      menuBarItemMargin: t < 0.5 ? menuBarItemMargin : other.menuBarItemMargin,
      popupElevation: t < 0.5 ? popupElevation : other.popupElevation,
      textStyle: TextStyle.lerp(textStyle, other.textStyle, t),
    );
  }

  @override
  bool operator ==(Object other) {
    return identical(this, other) ||
        (other.runtimeType == runtimeType &&
            other is MenuThemeExtension &&
            const DeepCollectionEquality().equals(
              animationDuration,
              other.animationDuration,
            ) &&
            const DeepCollectionEquality().equals(
              backgroundColor,
              other.backgroundColor,
            ) &&
            const DeepCollectionEquality().equals(
              menuBarBackgroundColor,
              other.menuBarBackgroundColor,
            ) &&
            const DeepCollectionEquality().equals(
              popupBackgroundColor,
              other.popupBackgroundColor,
            ) &&
            const DeepCollectionEquality().equals(
              hoverBackgroundColor,
              other.hoverBackgroundColor,
            ) &&
            const DeepCollectionEquality().equals(
              disabledBackgroundColor,
              other.disabledBackgroundColor,
            ) &&
            const DeepCollectionEquality().equals(textColor, other.textColor) &&
            const DeepCollectionEquality().equals(
              disabledTextColor,
              other.disabledTextColor,
            ) &&
            const DeepCollectionEquality().equals(
              borderColor,
              other.borderColor,
            ) &&
            const DeepCollectionEquality().equals(
              menuBarBorderColor,
              other.menuBarBorderColor,
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
              disabledBorderColor,
              other.disabledBorderColor,
            ) &&
            const DeepCollectionEquality().equals(
              menuBarHeight,
              other.menuBarHeight,
            ) &&
            const DeepCollectionEquality().equals(
              minMenuWidth,
              other.minMenuWidth,
            ) &&
            const DeepCollectionEquality().equals(
              maxMenuWidth,
              other.maxMenuWidth,
            ) &&
            const DeepCollectionEquality().equals(
              menuBarItemPadding,
              other.menuBarItemPadding,
            ) &&
            const DeepCollectionEquality().equals(
              popupPadding,
              other.popupPadding,
            ) &&
            const DeepCollectionEquality().equals(
              menuBarItemMargin,
              other.menuBarItemMargin,
            ) &&
            const DeepCollectionEquality().equals(
              popupElevation,
              other.popupElevation,
            ) &&
            const DeepCollectionEquality().equals(textStyle, other.textStyle));
  }

  @override
  int get hashCode {
    return Object.hashAll([
      runtimeType.hashCode,
      const DeepCollectionEquality().hash(animationDuration),
      const DeepCollectionEquality().hash(backgroundColor),
      const DeepCollectionEquality().hash(menuBarBackgroundColor),
      const DeepCollectionEquality().hash(popupBackgroundColor),
      const DeepCollectionEquality().hash(hoverBackgroundColor),
      const DeepCollectionEquality().hash(disabledBackgroundColor),
      const DeepCollectionEquality().hash(textColor),
      const DeepCollectionEquality().hash(disabledTextColor),
      const DeepCollectionEquality().hash(borderColor),
      const DeepCollectionEquality().hash(menuBarBorderColor),
      const DeepCollectionEquality().hash(borderWidth),
      const DeepCollectionEquality().hash(borderRadius),
      const DeepCollectionEquality().hash(disabledBorderColor),
      const DeepCollectionEquality().hash(menuBarHeight),
      const DeepCollectionEquality().hash(minMenuWidth),
      const DeepCollectionEquality().hash(maxMenuWidth),
      const DeepCollectionEquality().hash(menuBarItemPadding),
      const DeepCollectionEquality().hash(popupPadding),
      const DeepCollectionEquality().hash(menuBarItemMargin),
      const DeepCollectionEquality().hash(popupElevation),
      const DeepCollectionEquality().hash(textStyle),
    ]);
  }
}

extension MenuThemeExtensionBuildContextProps on BuildContext {
  MenuThemeExtension get menuThemeExtension =>
      Theme.of(this).extension<MenuThemeExtension>()!;
  Duration get animationDuration => menuThemeExtension.animationDuration;
  Color get backgroundColor => menuThemeExtension.backgroundColor;
  Color get menuBarBackgroundColor => menuThemeExtension.menuBarBackgroundColor;
  Color get popupBackgroundColor => menuThemeExtension.popupBackgroundColor;
  Color get hoverBackgroundColor => menuThemeExtension.hoverBackgroundColor;
  Color get disabledBackgroundColor =>
      menuThemeExtension.disabledBackgroundColor;
  Color get textColor => menuThemeExtension.textColor;
  Color get disabledTextColor => menuThemeExtension.disabledTextColor;
  Color get borderColor => menuThemeExtension.borderColor;
  Color get menuBarBorderColor => menuThemeExtension.menuBarBorderColor;
  double get borderWidth => menuThemeExtension.borderWidth;
  double get borderRadius => menuThemeExtension.borderRadius;
  Color get disabledBorderColor => menuThemeExtension.disabledBorderColor;
  double get menuBarHeight => menuThemeExtension.menuBarHeight;
  double get minMenuWidth => menuThemeExtension.minMenuWidth;
  double get maxMenuWidth => menuThemeExtension.maxMenuWidth;
  EdgeInsets get menuBarItemPadding => menuThemeExtension.menuBarItemPadding;
  EdgeInsets get popupPadding => menuThemeExtension.popupPadding;
  EdgeInsets get menuBarItemMargin => menuThemeExtension.menuBarItemMargin;
  double get popupElevation => menuThemeExtension.popupElevation;
  TextStyle? get textStyle => menuThemeExtension.textStyle;
}
