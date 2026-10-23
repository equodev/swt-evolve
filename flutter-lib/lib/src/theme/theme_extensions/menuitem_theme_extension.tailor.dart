// coverage:ignore-file
// GENERATED CODE - DO NOT MODIFY BY HAND
// ignore_for_file: type=lint
// ignore_for_file: unused_element, deprecated_member_use, deprecated_member_use_from_same_package, use_function_type_syntax_for_parameters, unnecessary_const, avoid_init_to_null, invalid_override_different_default_values_named, prefer_expression_function_bodies, annotate_overrides, invalid_annotation_target, unnecessary_question_mark

part of 'menuitem_theme_extension.dart';

// **************************************************************************
// TailorAnnotationsGenerator
// **************************************************************************

mixin _$MenuItemThemeExtensionTailorMixin
    on ThemeExtension<MenuItemThemeExtension> {
  Duration get animationDuration;
  Color get backgroundColor;
  Color get hoverBackgroundColor;
  Color get selectedBackgroundColor;
  Color get disabledBackgroundColor;
  Color get textColor;
  Color get disabledTextColor;
  Color get acceleratorTextColor;
  Color get iconColor;
  Color get disabledIconColor;
  Color get separatorColor;
  double get borderRadius;
  double get itemHeight;
  double get minItemWidth;
  double get iconSize;
  double get separatorHeight;
  double get separatorMargin;
  EdgeInsets get itemPadding;
  double get iconTextSpacing;
  double get textAcceleratorSpacing;
  double get checkboxSize;
  double get checkboxCheckmarkSize;
  double get radioButtonSize;
  double get radioButtonInnerSize;
  double get disabledOpacity;
  TextStyle? get textStyle;
  TextStyle? get acceleratorTextStyle;

  @override
  MenuItemThemeExtension copyWith({
    Duration? animationDuration,
    Color? backgroundColor,
    Color? hoverBackgroundColor,
    Color? selectedBackgroundColor,
    Color? disabledBackgroundColor,
    Color? textColor,
    Color? disabledTextColor,
    Color? acceleratorTextColor,
    Color? iconColor,
    Color? disabledIconColor,
    Color? separatorColor,
    double? borderRadius,
    double? itemHeight,
    double? minItemWidth,
    double? iconSize,
    double? separatorHeight,
    double? separatorMargin,
    EdgeInsets? itemPadding,
    double? iconTextSpacing,
    double? textAcceleratorSpacing,
    double? checkboxSize,
    double? checkboxCheckmarkSize,
    double? radioButtonSize,
    double? radioButtonInnerSize,
    double? disabledOpacity,
    TextStyle? textStyle,
    TextStyle? acceleratorTextStyle,
  }) {
    return MenuItemThemeExtension(
      animationDuration: animationDuration ?? this.animationDuration,
      backgroundColor: backgroundColor ?? this.backgroundColor,
      hoverBackgroundColor: hoverBackgroundColor ?? this.hoverBackgroundColor,
      selectedBackgroundColor:
          selectedBackgroundColor ?? this.selectedBackgroundColor,
      disabledBackgroundColor:
          disabledBackgroundColor ?? this.disabledBackgroundColor,
      textColor: textColor ?? this.textColor,
      disabledTextColor: disabledTextColor ?? this.disabledTextColor,
      acceleratorTextColor: acceleratorTextColor ?? this.acceleratorTextColor,
      iconColor: iconColor ?? this.iconColor,
      disabledIconColor: disabledIconColor ?? this.disabledIconColor,
      separatorColor: separatorColor ?? this.separatorColor,
      borderRadius: borderRadius ?? this.borderRadius,
      itemHeight: itemHeight ?? this.itemHeight,
      minItemWidth: minItemWidth ?? this.minItemWidth,
      iconSize: iconSize ?? this.iconSize,
      separatorHeight: separatorHeight ?? this.separatorHeight,
      separatorMargin: separatorMargin ?? this.separatorMargin,
      itemPadding: itemPadding ?? this.itemPadding,
      iconTextSpacing: iconTextSpacing ?? this.iconTextSpacing,
      textAcceleratorSpacing:
          textAcceleratorSpacing ?? this.textAcceleratorSpacing,
      checkboxSize: checkboxSize ?? this.checkboxSize,
      checkboxCheckmarkSize:
          checkboxCheckmarkSize ?? this.checkboxCheckmarkSize,
      radioButtonSize: radioButtonSize ?? this.radioButtonSize,
      radioButtonInnerSize: radioButtonInnerSize ?? this.radioButtonInnerSize,
      disabledOpacity: disabledOpacity ?? this.disabledOpacity,
      textStyle: textStyle ?? this.textStyle,
      acceleratorTextStyle: acceleratorTextStyle ?? this.acceleratorTextStyle,
    );
  }

  @override
  MenuItemThemeExtension lerp(
    covariant ThemeExtension<MenuItemThemeExtension>? other,
    double t,
  ) {
    if (other is! MenuItemThemeExtension) return this as MenuItemThemeExtension;
    return MenuItemThemeExtension(
      animationDuration: t < 0.5 ? animationDuration : other.animationDuration,
      backgroundColor: Color.lerp(backgroundColor, other.backgroundColor, t)!,
      hoverBackgroundColor: Color.lerp(
        hoverBackgroundColor,
        other.hoverBackgroundColor,
        t,
      )!,
      selectedBackgroundColor: Color.lerp(
        selectedBackgroundColor,
        other.selectedBackgroundColor,
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
      acceleratorTextColor: Color.lerp(
        acceleratorTextColor,
        other.acceleratorTextColor,
        t,
      )!,
      iconColor: Color.lerp(iconColor, other.iconColor, t)!,
      disabledIconColor: Color.lerp(
        disabledIconColor,
        other.disabledIconColor,
        t,
      )!,
      separatorColor: Color.lerp(separatorColor, other.separatorColor, t)!,
      borderRadius: t < 0.5 ? borderRadius : other.borderRadius,
      itemHeight: t < 0.5 ? itemHeight : other.itemHeight,
      minItemWidth: t < 0.5 ? minItemWidth : other.minItemWidth,
      iconSize: t < 0.5 ? iconSize : other.iconSize,
      separatorHeight: t < 0.5 ? separatorHeight : other.separatorHeight,
      separatorMargin: t < 0.5 ? separatorMargin : other.separatorMargin,
      itemPadding: t < 0.5 ? itemPadding : other.itemPadding,
      iconTextSpacing: t < 0.5 ? iconTextSpacing : other.iconTextSpacing,
      textAcceleratorSpacing: t < 0.5
          ? textAcceleratorSpacing
          : other.textAcceleratorSpacing,
      checkboxSize: t < 0.5 ? checkboxSize : other.checkboxSize,
      checkboxCheckmarkSize: t < 0.5
          ? checkboxCheckmarkSize
          : other.checkboxCheckmarkSize,
      radioButtonSize: t < 0.5 ? radioButtonSize : other.radioButtonSize,
      radioButtonInnerSize: t < 0.5
          ? radioButtonInnerSize
          : other.radioButtonInnerSize,
      disabledOpacity: t < 0.5 ? disabledOpacity : other.disabledOpacity,
      textStyle: TextStyle.lerp(textStyle, other.textStyle, t),
      acceleratorTextStyle: TextStyle.lerp(
        acceleratorTextStyle,
        other.acceleratorTextStyle,
        t,
      ),
    );
  }

  @override
  bool operator ==(Object other) {
    return identical(this, other) ||
        (other.runtimeType == runtimeType &&
            other is MenuItemThemeExtension &&
            const DeepCollectionEquality().equals(
              animationDuration,
              other.animationDuration,
            ) &&
            const DeepCollectionEquality().equals(
              backgroundColor,
              other.backgroundColor,
            ) &&
            const DeepCollectionEquality().equals(
              hoverBackgroundColor,
              other.hoverBackgroundColor,
            ) &&
            const DeepCollectionEquality().equals(
              selectedBackgroundColor,
              other.selectedBackgroundColor,
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
              acceleratorTextColor,
              other.acceleratorTextColor,
            ) &&
            const DeepCollectionEquality().equals(iconColor, other.iconColor) &&
            const DeepCollectionEquality().equals(
              disabledIconColor,
              other.disabledIconColor,
            ) &&
            const DeepCollectionEquality().equals(
              separatorColor,
              other.separatorColor,
            ) &&
            const DeepCollectionEquality().equals(
              borderRadius,
              other.borderRadius,
            ) &&
            const DeepCollectionEquality().equals(
              itemHeight,
              other.itemHeight,
            ) &&
            const DeepCollectionEquality().equals(
              minItemWidth,
              other.minItemWidth,
            ) &&
            const DeepCollectionEquality().equals(iconSize, other.iconSize) &&
            const DeepCollectionEquality().equals(
              separatorHeight,
              other.separatorHeight,
            ) &&
            const DeepCollectionEquality().equals(
              separatorMargin,
              other.separatorMargin,
            ) &&
            const DeepCollectionEquality().equals(
              itemPadding,
              other.itemPadding,
            ) &&
            const DeepCollectionEquality().equals(
              iconTextSpacing,
              other.iconTextSpacing,
            ) &&
            const DeepCollectionEquality().equals(
              textAcceleratorSpacing,
              other.textAcceleratorSpacing,
            ) &&
            const DeepCollectionEquality().equals(
              checkboxSize,
              other.checkboxSize,
            ) &&
            const DeepCollectionEquality().equals(
              checkboxCheckmarkSize,
              other.checkboxCheckmarkSize,
            ) &&
            const DeepCollectionEquality().equals(
              radioButtonSize,
              other.radioButtonSize,
            ) &&
            const DeepCollectionEquality().equals(
              radioButtonInnerSize,
              other.radioButtonInnerSize,
            ) &&
            const DeepCollectionEquality().equals(
              disabledOpacity,
              other.disabledOpacity,
            ) &&
            const DeepCollectionEquality().equals(textStyle, other.textStyle) &&
            const DeepCollectionEquality().equals(
              acceleratorTextStyle,
              other.acceleratorTextStyle,
            ));
  }

  @override
  int get hashCode {
    return Object.hashAll([
      runtimeType.hashCode,
      const DeepCollectionEquality().hash(animationDuration),
      const DeepCollectionEquality().hash(backgroundColor),
      const DeepCollectionEquality().hash(hoverBackgroundColor),
      const DeepCollectionEquality().hash(selectedBackgroundColor),
      const DeepCollectionEquality().hash(disabledBackgroundColor),
      const DeepCollectionEquality().hash(textColor),
      const DeepCollectionEquality().hash(disabledTextColor),
      const DeepCollectionEquality().hash(acceleratorTextColor),
      const DeepCollectionEquality().hash(iconColor),
      const DeepCollectionEquality().hash(disabledIconColor),
      const DeepCollectionEquality().hash(separatorColor),
      const DeepCollectionEquality().hash(borderRadius),
      const DeepCollectionEquality().hash(itemHeight),
      const DeepCollectionEquality().hash(minItemWidth),
      const DeepCollectionEquality().hash(iconSize),
      const DeepCollectionEquality().hash(separatorHeight),
      const DeepCollectionEquality().hash(separatorMargin),
      const DeepCollectionEquality().hash(itemPadding),
      const DeepCollectionEquality().hash(iconTextSpacing),
      const DeepCollectionEquality().hash(textAcceleratorSpacing),
      const DeepCollectionEquality().hash(checkboxSize),
      const DeepCollectionEquality().hash(checkboxCheckmarkSize),
      const DeepCollectionEquality().hash(radioButtonSize),
      const DeepCollectionEquality().hash(radioButtonInnerSize),
      const DeepCollectionEquality().hash(disabledOpacity),
      const DeepCollectionEquality().hash(textStyle),
      const DeepCollectionEquality().hash(acceleratorTextStyle),
    ]);
  }
}

extension MenuItemThemeExtensionBuildContextProps on BuildContext {
  MenuItemThemeExtension get menuItemThemeExtension =>
      Theme.of(this).extension<MenuItemThemeExtension>()!;
  Duration get animationDuration => menuItemThemeExtension.animationDuration;
  Color get backgroundColor => menuItemThemeExtension.backgroundColor;
  Color get hoverBackgroundColor => menuItemThemeExtension.hoverBackgroundColor;
  Color get selectedBackgroundColor =>
      menuItemThemeExtension.selectedBackgroundColor;
  Color get disabledBackgroundColor =>
      menuItemThemeExtension.disabledBackgroundColor;
  Color get textColor => menuItemThemeExtension.textColor;
  Color get disabledTextColor => menuItemThemeExtension.disabledTextColor;
  Color get acceleratorTextColor => menuItemThemeExtension.acceleratorTextColor;
  Color get iconColor => menuItemThemeExtension.iconColor;
  Color get disabledIconColor => menuItemThemeExtension.disabledIconColor;
  Color get separatorColor => menuItemThemeExtension.separatorColor;
  double get borderRadius => menuItemThemeExtension.borderRadius;
  double get itemHeight => menuItemThemeExtension.itemHeight;
  double get minItemWidth => menuItemThemeExtension.minItemWidth;
  double get iconSize => menuItemThemeExtension.iconSize;
  double get separatorHeight => menuItemThemeExtension.separatorHeight;
  double get separatorMargin => menuItemThemeExtension.separatorMargin;
  EdgeInsets get itemPadding => menuItemThemeExtension.itemPadding;
  double get iconTextSpacing => menuItemThemeExtension.iconTextSpacing;
  double get textAcceleratorSpacing =>
      menuItemThemeExtension.textAcceleratorSpacing;
  double get checkboxSize => menuItemThemeExtension.checkboxSize;
  double get checkboxCheckmarkSize =>
      menuItemThemeExtension.checkboxCheckmarkSize;
  double get radioButtonSize => menuItemThemeExtension.radioButtonSize;
  double get radioButtonInnerSize =>
      menuItemThemeExtension.radioButtonInnerSize;
  double get disabledOpacity => menuItemThemeExtension.disabledOpacity;
  TextStyle? get textStyle => menuItemThemeExtension.textStyle;
  TextStyle? get acceleratorTextStyle =>
      menuItemThemeExtension.acceleratorTextStyle;
}
