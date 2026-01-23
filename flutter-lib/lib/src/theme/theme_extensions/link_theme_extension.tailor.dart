// coverage:ignore-file
// GENERATED CODE - DO NOT MODIFY BY HAND
// ignore_for_file: type=lint
// ignore_for_file: unused_element, deprecated_member_use, deprecated_member_use_from_same_package, use_function_type_syntax_for_parameters, unnecessary_const, avoid_init_to_null, invalid_override_different_default_values_named, prefer_expression_function_bodies, annotate_overrides, invalid_annotation_target, unnecessary_question_mark

part of 'link_theme_extension.dart';

// **************************************************************************
// TailorAnnotationsGenerator
// **************************************************************************

mixin _$LinkThemeExtensionTailorMixin on ThemeExtension<LinkThemeExtension> {
  Color get textColor;
  Color get linkTextColor;
  Color get linkHoverTextColor;
  Color get disabledTextColor;
  Color? get backgroundColor;
  Color? get hoverBackgroundColor;
  TextStyle? get textStyle;
  TextStyle? get linkTextStyle;
  TextStyle? get disabledTextStyle;
  EdgeInsets get padding;
  double get minHeight;
  double get disabledOpacity;
  Duration get hoverAnimationDuration;
  TextDecoration get linkDecoration;
  TextDecoration get linkHoverDecoration;
  TextAlign get textAlign;

  @override
  LinkThemeExtension copyWith({
    Color? textColor,
    Color? linkTextColor,
    Color? linkHoverTextColor,
    Color? disabledTextColor,
    Color? backgroundColor,
    Color? hoverBackgroundColor,
    TextStyle? textStyle,
    TextStyle? linkTextStyle,
    TextStyle? disabledTextStyle,
    EdgeInsets? padding,
    double? minHeight,
    double? disabledOpacity,
    Duration? hoverAnimationDuration,
    TextDecoration? linkDecoration,
    TextDecoration? linkHoverDecoration,
    TextAlign? textAlign,
  }) {
    return LinkThemeExtension(
      textColor: textColor ?? this.textColor,
      linkTextColor: linkTextColor ?? this.linkTextColor,
      linkHoverTextColor: linkHoverTextColor ?? this.linkHoverTextColor,
      disabledTextColor: disabledTextColor ?? this.disabledTextColor,
      backgroundColor: backgroundColor ?? this.backgroundColor,
      hoverBackgroundColor: hoverBackgroundColor ?? this.hoverBackgroundColor,
      textStyle: textStyle ?? this.textStyle,
      linkTextStyle: linkTextStyle ?? this.linkTextStyle,
      disabledTextStyle: disabledTextStyle ?? this.disabledTextStyle,
      padding: padding ?? this.padding,
      minHeight: minHeight ?? this.minHeight,
      disabledOpacity: disabledOpacity ?? this.disabledOpacity,
      hoverAnimationDuration:
          hoverAnimationDuration ?? this.hoverAnimationDuration,
      linkDecoration: linkDecoration ?? this.linkDecoration,
      linkHoverDecoration: linkHoverDecoration ?? this.linkHoverDecoration,
      textAlign: textAlign ?? this.textAlign,
    );
  }

  @override
  LinkThemeExtension lerp(
    covariant ThemeExtension<LinkThemeExtension>? other,
    double t,
  ) {
    if (other is! LinkThemeExtension) return this as LinkThemeExtension;
    return LinkThemeExtension(
      textColor: Color.lerp(textColor, other.textColor, t)!,
      linkTextColor: Color.lerp(linkTextColor, other.linkTextColor, t)!,
      linkHoverTextColor: Color.lerp(
        linkHoverTextColor,
        other.linkHoverTextColor,
        t,
      )!,
      disabledTextColor: Color.lerp(
        disabledTextColor,
        other.disabledTextColor,
        t,
      )!,
      backgroundColor: Color.lerp(backgroundColor, other.backgroundColor, t),
      hoverBackgroundColor: Color.lerp(
        hoverBackgroundColor,
        other.hoverBackgroundColor,
        t,
      ),
      textStyle: TextStyle.lerp(textStyle, other.textStyle, t),
      linkTextStyle: TextStyle.lerp(linkTextStyle, other.linkTextStyle, t),
      disabledTextStyle: TextStyle.lerp(
        disabledTextStyle,
        other.disabledTextStyle,
        t,
      ),
      padding: t < 0.5 ? padding : other.padding,
      minHeight: t < 0.5 ? minHeight : other.minHeight,
      disabledOpacity: t < 0.5 ? disabledOpacity : other.disabledOpacity,
      hoverAnimationDuration: t < 0.5
          ? hoverAnimationDuration
          : other.hoverAnimationDuration,
      linkDecoration: t < 0.5 ? linkDecoration : other.linkDecoration,
      linkHoverDecoration: t < 0.5
          ? linkHoverDecoration
          : other.linkHoverDecoration,
      textAlign: t < 0.5 ? textAlign : other.textAlign,
    );
  }

  @override
  bool operator ==(Object other) {
    return identical(this, other) ||
        (other.runtimeType == runtimeType &&
            other is LinkThemeExtension &&
            const DeepCollectionEquality().equals(textColor, other.textColor) &&
            const DeepCollectionEquality().equals(
              linkTextColor,
              other.linkTextColor,
            ) &&
            const DeepCollectionEquality().equals(
              linkHoverTextColor,
              other.linkHoverTextColor,
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
              hoverBackgroundColor,
              other.hoverBackgroundColor,
            ) &&
            const DeepCollectionEquality().equals(textStyle, other.textStyle) &&
            const DeepCollectionEquality().equals(
              linkTextStyle,
              other.linkTextStyle,
            ) &&
            const DeepCollectionEquality().equals(
              disabledTextStyle,
              other.disabledTextStyle,
            ) &&
            const DeepCollectionEquality().equals(padding, other.padding) &&
            const DeepCollectionEquality().equals(minHeight, other.minHeight) &&
            const DeepCollectionEquality().equals(
              disabledOpacity,
              other.disabledOpacity,
            ) &&
            const DeepCollectionEquality().equals(
              hoverAnimationDuration,
              other.hoverAnimationDuration,
            ) &&
            const DeepCollectionEquality().equals(
              linkDecoration,
              other.linkDecoration,
            ) &&
            const DeepCollectionEquality().equals(
              linkHoverDecoration,
              other.linkHoverDecoration,
            ) &&
            const DeepCollectionEquality().equals(textAlign, other.textAlign));
  }

  @override
  int get hashCode {
    return Object.hash(
      runtimeType.hashCode,
      const DeepCollectionEquality().hash(textColor),
      const DeepCollectionEquality().hash(linkTextColor),
      const DeepCollectionEquality().hash(linkHoverTextColor),
      const DeepCollectionEquality().hash(disabledTextColor),
      const DeepCollectionEquality().hash(backgroundColor),
      const DeepCollectionEquality().hash(hoverBackgroundColor),
      const DeepCollectionEquality().hash(textStyle),
      const DeepCollectionEquality().hash(linkTextStyle),
      const DeepCollectionEquality().hash(disabledTextStyle),
      const DeepCollectionEquality().hash(padding),
      const DeepCollectionEquality().hash(minHeight),
      const DeepCollectionEquality().hash(disabledOpacity),
      const DeepCollectionEquality().hash(hoverAnimationDuration),
      const DeepCollectionEquality().hash(linkDecoration),
      const DeepCollectionEquality().hash(linkHoverDecoration),
      const DeepCollectionEquality().hash(textAlign),
    );
  }
}

extension LinkThemeExtensionBuildContextProps on BuildContext {
  LinkThemeExtension get linkThemeExtension =>
      Theme.of(this).extension<LinkThemeExtension>()!;
  Color get textColor => linkThemeExtension.textColor;
  Color get linkTextColor => linkThemeExtension.linkTextColor;
  Color get linkHoverTextColor => linkThemeExtension.linkHoverTextColor;
  Color get disabledTextColor => linkThemeExtension.disabledTextColor;
  Color? get backgroundColor => linkThemeExtension.backgroundColor;
  Color? get hoverBackgroundColor => linkThemeExtension.hoverBackgroundColor;
  TextStyle? get textStyle => linkThemeExtension.textStyle;
  TextStyle? get linkTextStyle => linkThemeExtension.linkTextStyle;
  TextStyle? get disabledTextStyle => linkThemeExtension.disabledTextStyle;
  EdgeInsets get padding => linkThemeExtension.padding;
  double get minHeight => linkThemeExtension.minHeight;
  double get disabledOpacity => linkThemeExtension.disabledOpacity;
  Duration get hoverAnimationDuration =>
      linkThemeExtension.hoverAnimationDuration;
  TextDecoration get linkDecoration => linkThemeExtension.linkDecoration;
  TextDecoration get linkHoverDecoration =>
      linkThemeExtension.linkHoverDecoration;
  TextAlign get textAlign => linkThemeExtension.textAlign;
}
