// coverage:ignore-file
// GENERATED CODE - DO NOT MODIFY BY HAND
// ignore_for_file: type=lint
// ignore_for_file: unused_element, deprecated_member_use, deprecated_member_use_from_same_package, use_function_type_syntax_for_parameters, unnecessary_const, avoid_init_to_null, invalid_override_different_default_values_named, prefer_expression_function_bodies, annotate_overrides, invalid_annotation_target, unnecessary_question_mark

part of 'ccombo_theme_extension.dart';

// **************************************************************************
// TailorAnnotationsGenerator
// **************************************************************************

mixin _$CComboThemeExtensionTailorMixin
    on ThemeExtension<CComboThemeExtension> {
  Duration get animationDuration;
  Color get backgroundColor;
  Color get disabledBackgroundColor;
  Color get hoverBackgroundColor;
  Color get textColor;
  Color get disabledTextColor;
  Color get borderColor;
  Color get disabledBorderColor;
  Color get iconColor;
  Color get disabledIconColor;
  Color get selectedItemBackgroundColor;
  double get borderWidth;
  double get borderRadius;
  double get itemHeight;
  double get iconSize;
  EdgeInsets get textFieldPadding;
  TextStyle? get textStyle;
  Color get dividerColor;
  double get dividerHeight;
  double get dividerThickness;

  @override
  CComboThemeExtension copyWith({
    Duration? animationDuration,
    Color? backgroundColor,
    Color? disabledBackgroundColor,
    Color? hoverBackgroundColor,
    Color? textColor,
    Color? disabledTextColor,
    Color? borderColor,
    Color? disabledBorderColor,
    Color? iconColor,
    Color? disabledIconColor,
    Color? selectedItemBackgroundColor,
    double? borderWidth,
    double? borderRadius,
    double? itemHeight,
    double? iconSize,
    EdgeInsets? textFieldPadding,
    TextStyle? textStyle,
    Color? dividerColor,
    double? dividerHeight,
    double? dividerThickness,
  }) {
    return CComboThemeExtension(
      animationDuration: animationDuration ?? this.animationDuration,
      backgroundColor: backgroundColor ?? this.backgroundColor,
      disabledBackgroundColor:
          disabledBackgroundColor ?? this.disabledBackgroundColor,
      hoverBackgroundColor: hoverBackgroundColor ?? this.hoverBackgroundColor,
      textColor: textColor ?? this.textColor,
      disabledTextColor: disabledTextColor ?? this.disabledTextColor,
      borderColor: borderColor ?? this.borderColor,
      disabledBorderColor: disabledBorderColor ?? this.disabledBorderColor,
      iconColor: iconColor ?? this.iconColor,
      disabledIconColor: disabledIconColor ?? this.disabledIconColor,
      selectedItemBackgroundColor:
          selectedItemBackgroundColor ?? this.selectedItemBackgroundColor,
      borderWidth: borderWidth ?? this.borderWidth,
      borderRadius: borderRadius ?? this.borderRadius,
      itemHeight: itemHeight ?? this.itemHeight,
      iconSize: iconSize ?? this.iconSize,
      textFieldPadding: textFieldPadding ?? this.textFieldPadding,
      textStyle: textStyle ?? this.textStyle,
      dividerColor: dividerColor ?? this.dividerColor,
      dividerHeight: dividerHeight ?? this.dividerHeight,
      dividerThickness: dividerThickness ?? this.dividerThickness,
    );
  }

  @override
  CComboThemeExtension lerp(
    covariant ThemeExtension<CComboThemeExtension>? other,
    double t,
  ) {
    if (other is! CComboThemeExtension) return this as CComboThemeExtension;
    return CComboThemeExtension(
      animationDuration: t < 0.5 ? animationDuration : other.animationDuration,
      backgroundColor: Color.lerp(backgroundColor, other.backgroundColor, t)!,
      disabledBackgroundColor: Color.lerp(
        disabledBackgroundColor,
        other.disabledBackgroundColor,
        t,
      )!,
      hoverBackgroundColor: Color.lerp(
        hoverBackgroundColor,
        other.hoverBackgroundColor,
        t,
      )!,
      textColor: Color.lerp(textColor, other.textColor, t)!,
      disabledTextColor: Color.lerp(
        disabledTextColor,
        other.disabledTextColor,
        t,
      )!,
      borderColor: Color.lerp(borderColor, other.borderColor, t)!,
      disabledBorderColor: Color.lerp(
        disabledBorderColor,
        other.disabledBorderColor,
        t,
      )!,
      iconColor: Color.lerp(iconColor, other.iconColor, t)!,
      disabledIconColor: Color.lerp(
        disabledIconColor,
        other.disabledIconColor,
        t,
      )!,
      selectedItemBackgroundColor: Color.lerp(
        selectedItemBackgroundColor,
        other.selectedItemBackgroundColor,
        t,
      )!,
      borderWidth: t < 0.5 ? borderWidth : other.borderWidth,
      borderRadius: t < 0.5 ? borderRadius : other.borderRadius,
      itemHeight: t < 0.5 ? itemHeight : other.itemHeight,
      iconSize: t < 0.5 ? iconSize : other.iconSize,
      textFieldPadding: t < 0.5 ? textFieldPadding : other.textFieldPadding,
      textStyle: TextStyle.lerp(textStyle, other.textStyle, t),
      dividerColor: Color.lerp(dividerColor, other.dividerColor, t)!,
      dividerHeight: t < 0.5 ? dividerHeight : other.dividerHeight,
      dividerThickness: t < 0.5 ? dividerThickness : other.dividerThickness,
    );
  }

  @override
  bool operator ==(Object other) {
    return identical(this, other) ||
        (other.runtimeType == runtimeType &&
            other is CComboThemeExtension &&
            const DeepCollectionEquality().equals(
              animationDuration,
              other.animationDuration,
            ) &&
            const DeepCollectionEquality().equals(
              backgroundColor,
              other.backgroundColor,
            ) &&
            const DeepCollectionEquality().equals(
              disabledBackgroundColor,
              other.disabledBackgroundColor,
            ) &&
            const DeepCollectionEquality().equals(
              hoverBackgroundColor,
              other.hoverBackgroundColor,
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
              disabledBorderColor,
              other.disabledBorderColor,
            ) &&
            const DeepCollectionEquality().equals(iconColor, other.iconColor) &&
            const DeepCollectionEquality().equals(
              disabledIconColor,
              other.disabledIconColor,
            ) &&
            const DeepCollectionEquality().equals(
              selectedItemBackgroundColor,
              other.selectedItemBackgroundColor,
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
              itemHeight,
              other.itemHeight,
            ) &&
            const DeepCollectionEquality().equals(iconSize, other.iconSize) &&
            const DeepCollectionEquality().equals(
              textFieldPadding,
              other.textFieldPadding,
            ) &&
            const DeepCollectionEquality().equals(textStyle, other.textStyle) &&
            const DeepCollectionEquality().equals(
              dividerColor,
              other.dividerColor,
            ) &&
            const DeepCollectionEquality().equals(
              dividerHeight,
              other.dividerHeight,
            ) &&
            const DeepCollectionEquality().equals(
              dividerThickness,
              other.dividerThickness,
            ));
  }

  @override
  int get hashCode {
    return Object.hashAll([
      runtimeType.hashCode,
      const DeepCollectionEquality().hash(animationDuration),
      const DeepCollectionEquality().hash(backgroundColor),
      const DeepCollectionEquality().hash(disabledBackgroundColor),
      const DeepCollectionEquality().hash(hoverBackgroundColor),
      const DeepCollectionEquality().hash(textColor),
      const DeepCollectionEquality().hash(disabledTextColor),
      const DeepCollectionEquality().hash(borderColor),
      const DeepCollectionEquality().hash(disabledBorderColor),
      const DeepCollectionEquality().hash(iconColor),
      const DeepCollectionEquality().hash(disabledIconColor),
      const DeepCollectionEquality().hash(selectedItemBackgroundColor),
      const DeepCollectionEquality().hash(borderWidth),
      const DeepCollectionEquality().hash(borderRadius),
      const DeepCollectionEquality().hash(itemHeight),
      const DeepCollectionEquality().hash(iconSize),
      const DeepCollectionEquality().hash(textFieldPadding),
      const DeepCollectionEquality().hash(textStyle),
      const DeepCollectionEquality().hash(dividerColor),
      const DeepCollectionEquality().hash(dividerHeight),
      const DeepCollectionEquality().hash(dividerThickness),
    ]);
  }
}

extension CComboThemeExtensionBuildContextProps on BuildContext {
  CComboThemeExtension get cComboThemeExtension =>
      Theme.of(this).extension<CComboThemeExtension>()!;
  Duration get animationDuration => cComboThemeExtension.animationDuration;
  Color get backgroundColor => cComboThemeExtension.backgroundColor;
  Color get disabledBackgroundColor =>
      cComboThemeExtension.disabledBackgroundColor;
  Color get hoverBackgroundColor => cComboThemeExtension.hoverBackgroundColor;
  Color get textColor => cComboThemeExtension.textColor;
  Color get disabledTextColor => cComboThemeExtension.disabledTextColor;
  Color get borderColor => cComboThemeExtension.borderColor;
  Color get disabledBorderColor => cComboThemeExtension.disabledBorderColor;
  Color get iconColor => cComboThemeExtension.iconColor;
  Color get disabledIconColor => cComboThemeExtension.disabledIconColor;
  Color get selectedItemBackgroundColor =>
      cComboThemeExtension.selectedItemBackgroundColor;
  double get borderWidth => cComboThemeExtension.borderWidth;
  double get borderRadius => cComboThemeExtension.borderRadius;
  double get itemHeight => cComboThemeExtension.itemHeight;
  double get iconSize => cComboThemeExtension.iconSize;
  EdgeInsets get textFieldPadding => cComboThemeExtension.textFieldPadding;
  TextStyle? get textStyle => cComboThemeExtension.textStyle;
  Color get dividerColor => cComboThemeExtension.dividerColor;
  double get dividerHeight => cComboThemeExtension.dividerHeight;
  double get dividerThickness => cComboThemeExtension.dividerThickness;
}
