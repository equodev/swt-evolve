// coverage:ignore-file
// GENERATED CODE - DO NOT MODIFY BY HAND
// ignore_for_file: type=lint
// ignore_for_file: unused_element, deprecated_member_use, deprecated_member_use_from_same_package, use_function_type_syntax_for_parameters, unnecessary_const, avoid_init_to_null, invalid_override_different_default_values_named, prefer_expression_function_bodies, annotate_overrides, invalid_annotation_target, unnecessary_question_mark

part of 'combo_theme_extension.dart';

// **************************************************************************
// TailorAnnotationsGenerator
// **************************************************************************

mixin _$ComboThemeExtensionTailorMixin on ThemeExtension<ComboThemeExtension> {
  Color get backgroundColor;
  Color get textColor;
  Color get borderColor;
  Color get iconColor;
  Color get selectedItemBackgroundColor;
  Color get hoverBackgroundColor;
  Color get disabledBackgroundColor;
  Color get disabledTextColor;
  Color get disabledBorderColor;
  double get borderRadius;
  double get borderWidth;
  EdgeInsets get textFieldPadding;
  EdgeInsets get itemPadding;
  double get height;
  double get itemHeight;
  double get iconSize;
  TextStyle? get textStyle;
  TextStyle? get itemTextStyle;
  Color get dividerColor;
  double get dividerHeight;
  double get dividerThickness;
  Duration get animationDuration;

  @override
  ComboThemeExtension copyWith({
    Color? backgroundColor,
    Color? textColor,
    Color? borderColor,
    Color? iconColor,
    Color? selectedItemBackgroundColor,
    Color? hoverBackgroundColor,
    Color? disabledBackgroundColor,
    Color? disabledTextColor,
    Color? disabledBorderColor,
    double? borderRadius,
    double? borderWidth,
    EdgeInsets? textFieldPadding,
    EdgeInsets? itemPadding,
    double? height,
    double? itemHeight,
    double? iconSize,
    TextStyle? textStyle,
    TextStyle? itemTextStyle,
    Color? dividerColor,
    double? dividerHeight,
    double? dividerThickness,
    Duration? animationDuration,
  }) {
    return ComboThemeExtension(
      backgroundColor: backgroundColor ?? this.backgroundColor,
      textColor: textColor ?? this.textColor,
      borderColor: borderColor ?? this.borderColor,
      iconColor: iconColor ?? this.iconColor,
      selectedItemBackgroundColor:
          selectedItemBackgroundColor ?? this.selectedItemBackgroundColor,
      hoverBackgroundColor: hoverBackgroundColor ?? this.hoverBackgroundColor,
      disabledBackgroundColor:
          disabledBackgroundColor ?? this.disabledBackgroundColor,
      disabledTextColor: disabledTextColor ?? this.disabledTextColor,
      disabledBorderColor: disabledBorderColor ?? this.disabledBorderColor,
      borderRadius: borderRadius ?? this.borderRadius,
      borderWidth: borderWidth ?? this.borderWidth,
      textFieldPadding: textFieldPadding ?? this.textFieldPadding,
      itemPadding: itemPadding ?? this.itemPadding,
      height: height ?? this.height,
      itemHeight: itemHeight ?? this.itemHeight,
      iconSize: iconSize ?? this.iconSize,
      textStyle: textStyle ?? this.textStyle,
      itemTextStyle: itemTextStyle ?? this.itemTextStyle,
      dividerColor: dividerColor ?? this.dividerColor,
      dividerHeight: dividerHeight ?? this.dividerHeight,
      dividerThickness: dividerThickness ?? this.dividerThickness,
      animationDuration: animationDuration ?? this.animationDuration,
    );
  }

  @override
  ComboThemeExtension lerp(
    covariant ThemeExtension<ComboThemeExtension>? other,
    double t,
  ) {
    if (other is! ComboThemeExtension) return this as ComboThemeExtension;
    return ComboThemeExtension(
      backgroundColor: Color.lerp(backgroundColor, other.backgroundColor, t)!,
      textColor: Color.lerp(textColor, other.textColor, t)!,
      borderColor: Color.lerp(borderColor, other.borderColor, t)!,
      iconColor: Color.lerp(iconColor, other.iconColor, t)!,
      selectedItemBackgroundColor: Color.lerp(
        selectedItemBackgroundColor,
        other.selectedItemBackgroundColor,
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
      disabledTextColor: Color.lerp(
        disabledTextColor,
        other.disabledTextColor,
        t,
      )!,
      disabledBorderColor: Color.lerp(
        disabledBorderColor,
        other.disabledBorderColor,
        t,
      )!,
      borderRadius: t < 0.5 ? borderRadius : other.borderRadius,
      borderWidth: t < 0.5 ? borderWidth : other.borderWidth,
      textFieldPadding: t < 0.5 ? textFieldPadding : other.textFieldPadding,
      itemPadding: t < 0.5 ? itemPadding : other.itemPadding,
      height: t < 0.5 ? height : other.height,
      itemHeight: t < 0.5 ? itemHeight : other.itemHeight,
      iconSize: t < 0.5 ? iconSize : other.iconSize,
      textStyle: TextStyle.lerp(textStyle, other.textStyle, t),
      itemTextStyle: TextStyle.lerp(itemTextStyle, other.itemTextStyle, t),
      dividerColor: Color.lerp(dividerColor, other.dividerColor, t)!,
      dividerHeight: t < 0.5 ? dividerHeight : other.dividerHeight,
      dividerThickness: t < 0.5 ? dividerThickness : other.dividerThickness,
      animationDuration: t < 0.5 ? animationDuration : other.animationDuration,
    );
  }

  @override
  bool operator ==(Object other) {
    return identical(this, other) ||
        (other.runtimeType == runtimeType &&
            other is ComboThemeExtension &&
            const DeepCollectionEquality().equals(
              backgroundColor,
              other.backgroundColor,
            ) &&
            const DeepCollectionEquality().equals(textColor, other.textColor) &&
            const DeepCollectionEquality().equals(
              borderColor,
              other.borderColor,
            ) &&
            const DeepCollectionEquality().equals(iconColor, other.iconColor) &&
            const DeepCollectionEquality().equals(
              selectedItemBackgroundColor,
              other.selectedItemBackgroundColor,
            ) &&
            const DeepCollectionEquality().equals(
              hoverBackgroundColor,
              other.hoverBackgroundColor,
            ) &&
            const DeepCollectionEquality().equals(
              disabledBackgroundColor,
              other.disabledBackgroundColor,
            ) &&
            const DeepCollectionEquality().equals(
              disabledTextColor,
              other.disabledTextColor,
            ) &&
            const DeepCollectionEquality().equals(
              disabledBorderColor,
              other.disabledBorderColor,
            ) &&
            const DeepCollectionEquality().equals(
              borderRadius,
              other.borderRadius,
            ) &&
            const DeepCollectionEquality().equals(
              borderWidth,
              other.borderWidth,
            ) &&
            const DeepCollectionEquality().equals(
              textFieldPadding,
              other.textFieldPadding,
            ) &&
            const DeepCollectionEquality().equals(
              itemPadding,
              other.itemPadding,
            ) &&
            const DeepCollectionEquality().equals(height, other.height) &&
            const DeepCollectionEquality().equals(
              itemHeight,
              other.itemHeight,
            ) &&
            const DeepCollectionEquality().equals(iconSize, other.iconSize) &&
            const DeepCollectionEquality().equals(textStyle, other.textStyle) &&
            const DeepCollectionEquality().equals(
              itemTextStyle,
              other.itemTextStyle,
            ) &&
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
            ) &&
            const DeepCollectionEquality().equals(
              animationDuration,
              other.animationDuration,
            ));
  }

  @override
  int get hashCode {
    return Object.hashAll([
      runtimeType.hashCode,
      const DeepCollectionEquality().hash(backgroundColor),
      const DeepCollectionEquality().hash(textColor),
      const DeepCollectionEquality().hash(borderColor),
      const DeepCollectionEquality().hash(iconColor),
      const DeepCollectionEquality().hash(selectedItemBackgroundColor),
      const DeepCollectionEquality().hash(hoverBackgroundColor),
      const DeepCollectionEquality().hash(disabledBackgroundColor),
      const DeepCollectionEquality().hash(disabledTextColor),
      const DeepCollectionEquality().hash(disabledBorderColor),
      const DeepCollectionEquality().hash(borderRadius),
      const DeepCollectionEquality().hash(borderWidth),
      const DeepCollectionEquality().hash(textFieldPadding),
      const DeepCollectionEquality().hash(itemPadding),
      const DeepCollectionEquality().hash(height),
      const DeepCollectionEquality().hash(itemHeight),
      const DeepCollectionEquality().hash(iconSize),
      const DeepCollectionEquality().hash(textStyle),
      const DeepCollectionEquality().hash(itemTextStyle),
      const DeepCollectionEquality().hash(dividerColor),
      const DeepCollectionEquality().hash(dividerHeight),
      const DeepCollectionEquality().hash(dividerThickness),
      const DeepCollectionEquality().hash(animationDuration),
    ]);
  }
}

extension ComboThemeExtensionBuildContextProps on BuildContext {
  ComboThemeExtension get comboThemeExtension =>
      Theme.of(this).extension<ComboThemeExtension>()!;
  Color get backgroundColor => comboThemeExtension.backgroundColor;
  Color get textColor => comboThemeExtension.textColor;
  Color get borderColor => comboThemeExtension.borderColor;
  Color get iconColor => comboThemeExtension.iconColor;
  Color get selectedItemBackgroundColor =>
      comboThemeExtension.selectedItemBackgroundColor;
  Color get hoverBackgroundColor => comboThemeExtension.hoverBackgroundColor;
  Color get disabledBackgroundColor =>
      comboThemeExtension.disabledBackgroundColor;
  Color get disabledTextColor => comboThemeExtension.disabledTextColor;
  Color get disabledBorderColor => comboThemeExtension.disabledBorderColor;
  double get borderRadius => comboThemeExtension.borderRadius;
  double get borderWidth => comboThemeExtension.borderWidth;
  EdgeInsets get textFieldPadding => comboThemeExtension.textFieldPadding;
  EdgeInsets get itemPadding => comboThemeExtension.itemPadding;
  double get height => comboThemeExtension.height;
  double get itemHeight => comboThemeExtension.itemHeight;
  double get iconSize => comboThemeExtension.iconSize;
  TextStyle? get textStyle => comboThemeExtension.textStyle;
  TextStyle? get itemTextStyle => comboThemeExtension.itemTextStyle;
  Color get dividerColor => comboThemeExtension.dividerColor;
  double get dividerHeight => comboThemeExtension.dividerHeight;
  double get dividerThickness => comboThemeExtension.dividerThickness;
  Duration get animationDuration => comboThemeExtension.animationDuration;
}
