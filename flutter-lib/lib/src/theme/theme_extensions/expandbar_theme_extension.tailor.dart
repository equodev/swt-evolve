// coverage:ignore-file
// GENERATED CODE - DO NOT MODIFY BY HAND
// ignore_for_file: type=lint
// ignore_for_file: unused_element, deprecated_member_use, deprecated_member_use_from_same_package, use_function_type_syntax_for_parameters, unnecessary_const, avoid_init_to_null, invalid_override_different_default_values_named, prefer_expression_function_bodies, annotate_overrides, invalid_annotation_target, unnecessary_question_mark

part of 'expandbar_theme_extension.dart';

// **************************************************************************
// TailorAnnotationsGenerator
// **************************************************************************

mixin _$ExpandBarThemeExtensionTailorMixin
    on ThemeExtension<ExpandBarThemeExtension> {
  Color get backgroundColor;
  Color get borderColor;
  Color get foregroundColor;
  Color get foregroundExpandedColor;
  Color get headerBackgroundColor;
  Color get headerBackgroundExpandedColor;
  Color get headerBackgroundHoveredColor;
  Color get contentBackgroundColor;
  Color get iconColor;
  Color get iconExpandedColor;
  Color get disabledForegroundColor;
  Color get disabledBackgroundColor;
  double get borderWidth;
  double get borderRadius;
  double get itemBorderRadius;
  EdgeInsets get headerPadding;
  EdgeInsets get contentPadding;
  EdgeInsets get containerPadding;
  double get itemSpacing;
  TextStyle? get headerTextStyle;
  Duration get animationDuration;
  Curve get animationCurve;
  double get iconSize;

  @override
  ExpandBarThemeExtension copyWith({
    Color? backgroundColor,
    Color? borderColor,
    Color? foregroundColor,
    Color? foregroundExpandedColor,
    Color? headerBackgroundColor,
    Color? headerBackgroundExpandedColor,
    Color? headerBackgroundHoveredColor,
    Color? contentBackgroundColor,
    Color? iconColor,
    Color? iconExpandedColor,
    Color? disabledForegroundColor,
    Color? disabledBackgroundColor,
    double? borderWidth,
    double? borderRadius,
    double? itemBorderRadius,
    EdgeInsets? headerPadding,
    EdgeInsets? contentPadding,
    EdgeInsets? containerPadding,
    double? itemSpacing,
    TextStyle? headerTextStyle,
    Duration? animationDuration,
    Curve? animationCurve,
    double? iconSize,
  }) {
    return ExpandBarThemeExtension(
      backgroundColor: backgroundColor ?? this.backgroundColor,
      borderColor: borderColor ?? this.borderColor,
      foregroundColor: foregroundColor ?? this.foregroundColor,
      foregroundExpandedColor:
          foregroundExpandedColor ?? this.foregroundExpandedColor,
      headerBackgroundColor:
          headerBackgroundColor ?? this.headerBackgroundColor,
      headerBackgroundExpandedColor:
          headerBackgroundExpandedColor ?? this.headerBackgroundExpandedColor,
      headerBackgroundHoveredColor:
          headerBackgroundHoveredColor ?? this.headerBackgroundHoveredColor,
      contentBackgroundColor:
          contentBackgroundColor ?? this.contentBackgroundColor,
      iconColor: iconColor ?? this.iconColor,
      iconExpandedColor: iconExpandedColor ?? this.iconExpandedColor,
      disabledForegroundColor:
          disabledForegroundColor ?? this.disabledForegroundColor,
      disabledBackgroundColor:
          disabledBackgroundColor ?? this.disabledBackgroundColor,
      borderWidth: borderWidth ?? this.borderWidth,
      borderRadius: borderRadius ?? this.borderRadius,
      itemBorderRadius: itemBorderRadius ?? this.itemBorderRadius,
      headerPadding: headerPadding ?? this.headerPadding,
      contentPadding: contentPadding ?? this.contentPadding,
      containerPadding: containerPadding ?? this.containerPadding,
      itemSpacing: itemSpacing ?? this.itemSpacing,
      headerTextStyle: headerTextStyle ?? this.headerTextStyle,
      animationDuration: animationDuration ?? this.animationDuration,
      animationCurve: animationCurve ?? this.animationCurve,
      iconSize: iconSize ?? this.iconSize,
    );
  }

  @override
  ExpandBarThemeExtension lerp(
    covariant ThemeExtension<ExpandBarThemeExtension>? other,
    double t,
  ) {
    if (other is! ExpandBarThemeExtension)
      return this as ExpandBarThemeExtension;
    return ExpandBarThemeExtension(
      backgroundColor: Color.lerp(backgroundColor, other.backgroundColor, t)!,
      borderColor: Color.lerp(borderColor, other.borderColor, t)!,
      foregroundColor: Color.lerp(foregroundColor, other.foregroundColor, t)!,
      foregroundExpandedColor: Color.lerp(
        foregroundExpandedColor,
        other.foregroundExpandedColor,
        t,
      )!,
      headerBackgroundColor: Color.lerp(
        headerBackgroundColor,
        other.headerBackgroundColor,
        t,
      )!,
      headerBackgroundExpandedColor: Color.lerp(
        headerBackgroundExpandedColor,
        other.headerBackgroundExpandedColor,
        t,
      )!,
      headerBackgroundHoveredColor: Color.lerp(
        headerBackgroundHoveredColor,
        other.headerBackgroundHoveredColor,
        t,
      )!,
      contentBackgroundColor: Color.lerp(
        contentBackgroundColor,
        other.contentBackgroundColor,
        t,
      )!,
      iconColor: Color.lerp(iconColor, other.iconColor, t)!,
      iconExpandedColor: Color.lerp(
        iconExpandedColor,
        other.iconExpandedColor,
        t,
      )!,
      disabledForegroundColor: Color.lerp(
        disabledForegroundColor,
        other.disabledForegroundColor,
        t,
      )!,
      disabledBackgroundColor: Color.lerp(
        disabledBackgroundColor,
        other.disabledBackgroundColor,
        t,
      )!,
      borderWidth: t < 0.5 ? borderWidth : other.borderWidth,
      borderRadius: t < 0.5 ? borderRadius : other.borderRadius,
      itemBorderRadius: t < 0.5 ? itemBorderRadius : other.itemBorderRadius,
      headerPadding: t < 0.5 ? headerPadding : other.headerPadding,
      contentPadding: t < 0.5 ? contentPadding : other.contentPadding,
      containerPadding: t < 0.5 ? containerPadding : other.containerPadding,
      itemSpacing: t < 0.5 ? itemSpacing : other.itemSpacing,
      headerTextStyle: TextStyle.lerp(
        headerTextStyle,
        other.headerTextStyle,
        t,
      ),
      animationDuration: t < 0.5 ? animationDuration : other.animationDuration,
      animationCurve: t < 0.5 ? animationCurve : other.animationCurve,
      iconSize: t < 0.5 ? iconSize : other.iconSize,
    );
  }

  @override
  bool operator ==(Object other) {
    return identical(this, other) ||
        (other.runtimeType == runtimeType &&
            other is ExpandBarThemeExtension &&
            const DeepCollectionEquality().equals(
              backgroundColor,
              other.backgroundColor,
            ) &&
            const DeepCollectionEquality().equals(
              borderColor,
              other.borderColor,
            ) &&
            const DeepCollectionEquality().equals(
              foregroundColor,
              other.foregroundColor,
            ) &&
            const DeepCollectionEquality().equals(
              foregroundExpandedColor,
              other.foregroundExpandedColor,
            ) &&
            const DeepCollectionEquality().equals(
              headerBackgroundColor,
              other.headerBackgroundColor,
            ) &&
            const DeepCollectionEquality().equals(
              headerBackgroundExpandedColor,
              other.headerBackgroundExpandedColor,
            ) &&
            const DeepCollectionEquality().equals(
              headerBackgroundHoveredColor,
              other.headerBackgroundHoveredColor,
            ) &&
            const DeepCollectionEquality().equals(
              contentBackgroundColor,
              other.contentBackgroundColor,
            ) &&
            const DeepCollectionEquality().equals(iconColor, other.iconColor) &&
            const DeepCollectionEquality().equals(
              iconExpandedColor,
              other.iconExpandedColor,
            ) &&
            const DeepCollectionEquality().equals(
              disabledForegroundColor,
              other.disabledForegroundColor,
            ) &&
            const DeepCollectionEquality().equals(
              disabledBackgroundColor,
              other.disabledBackgroundColor,
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
              itemBorderRadius,
              other.itemBorderRadius,
            ) &&
            const DeepCollectionEquality().equals(
              headerPadding,
              other.headerPadding,
            ) &&
            const DeepCollectionEquality().equals(
              contentPadding,
              other.contentPadding,
            ) &&
            const DeepCollectionEquality().equals(
              containerPadding,
              other.containerPadding,
            ) &&
            const DeepCollectionEquality().equals(
              itemSpacing,
              other.itemSpacing,
            ) &&
            const DeepCollectionEquality().equals(
              headerTextStyle,
              other.headerTextStyle,
            ) &&
            const DeepCollectionEquality().equals(
              animationDuration,
              other.animationDuration,
            ) &&
            const DeepCollectionEquality().equals(
              animationCurve,
              other.animationCurve,
            ) &&
            const DeepCollectionEquality().equals(iconSize, other.iconSize));
  }

  @override
  int get hashCode {
    return Object.hashAll([
      runtimeType.hashCode,
      const DeepCollectionEquality().hash(backgroundColor),
      const DeepCollectionEquality().hash(borderColor),
      const DeepCollectionEquality().hash(foregroundColor),
      const DeepCollectionEquality().hash(foregroundExpandedColor),
      const DeepCollectionEquality().hash(headerBackgroundColor),
      const DeepCollectionEquality().hash(headerBackgroundExpandedColor),
      const DeepCollectionEquality().hash(headerBackgroundHoveredColor),
      const DeepCollectionEquality().hash(contentBackgroundColor),
      const DeepCollectionEquality().hash(iconColor),
      const DeepCollectionEquality().hash(iconExpandedColor),
      const DeepCollectionEquality().hash(disabledForegroundColor),
      const DeepCollectionEquality().hash(disabledBackgroundColor),
      const DeepCollectionEquality().hash(borderWidth),
      const DeepCollectionEquality().hash(borderRadius),
      const DeepCollectionEquality().hash(itemBorderRadius),
      const DeepCollectionEquality().hash(headerPadding),
      const DeepCollectionEquality().hash(contentPadding),
      const DeepCollectionEquality().hash(containerPadding),
      const DeepCollectionEquality().hash(itemSpacing),
      const DeepCollectionEquality().hash(headerTextStyle),
      const DeepCollectionEquality().hash(animationDuration),
      const DeepCollectionEquality().hash(animationCurve),
      const DeepCollectionEquality().hash(iconSize),
    ]);
  }
}

extension ExpandBarThemeExtensionBuildContextProps on BuildContext {
  ExpandBarThemeExtension get expandBarThemeExtension =>
      Theme.of(this).extension<ExpandBarThemeExtension>()!;
  Color get backgroundColor => expandBarThemeExtension.backgroundColor;
  Color get borderColor => expandBarThemeExtension.borderColor;
  Color get foregroundColor => expandBarThemeExtension.foregroundColor;
  Color get foregroundExpandedColor =>
      expandBarThemeExtension.foregroundExpandedColor;
  Color get headerBackgroundColor =>
      expandBarThemeExtension.headerBackgroundColor;
  Color get headerBackgroundExpandedColor =>
      expandBarThemeExtension.headerBackgroundExpandedColor;
  Color get headerBackgroundHoveredColor =>
      expandBarThemeExtension.headerBackgroundHoveredColor;
  Color get contentBackgroundColor =>
      expandBarThemeExtension.contentBackgroundColor;
  Color get iconColor => expandBarThemeExtension.iconColor;
  Color get iconExpandedColor => expandBarThemeExtension.iconExpandedColor;
  Color get disabledForegroundColor =>
      expandBarThemeExtension.disabledForegroundColor;
  Color get disabledBackgroundColor =>
      expandBarThemeExtension.disabledBackgroundColor;
  double get borderWidth => expandBarThemeExtension.borderWidth;
  double get borderRadius => expandBarThemeExtension.borderRadius;
  double get itemBorderRadius => expandBarThemeExtension.itemBorderRadius;
  EdgeInsets get headerPadding => expandBarThemeExtension.headerPadding;
  EdgeInsets get contentPadding => expandBarThemeExtension.contentPadding;
  EdgeInsets get containerPadding => expandBarThemeExtension.containerPadding;
  double get itemSpacing => expandBarThemeExtension.itemSpacing;
  TextStyle? get headerTextStyle => expandBarThemeExtension.headerTextStyle;
  Duration get animationDuration => expandBarThemeExtension.animationDuration;
  Curve get animationCurve => expandBarThemeExtension.animationCurve;
  double get iconSize => expandBarThemeExtension.iconSize;
}
