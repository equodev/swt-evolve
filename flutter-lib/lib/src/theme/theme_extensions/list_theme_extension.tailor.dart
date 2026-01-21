// coverage:ignore-file
// GENERATED CODE - DO NOT MODIFY BY HAND
// ignore_for_file: type=lint
// ignore_for_file: unused_element, deprecated_member_use, deprecated_member_use_from_same_package, use_function_type_syntax_for_parameters, unnecessary_const, avoid_init_to_null, invalid_override_different_default_values_named, prefer_expression_function_bodies, annotate_overrides, invalid_annotation_target, unnecessary_question_mark

part of 'list_theme_extension.dart';

// **************************************************************************
// TailorAnnotationsGenerator
// **************************************************************************

mixin _$ListThemeExtensionTailorMixin on ThemeExtension<ListThemeExtension> {
  Color get backgroundColor;
  Color get selectedItemBackgroundColor;
  Color get hoverItemBackgroundColor;
  Color get disabledBackgroundColor;
  Color get textColor;
  Color get selectedItemTextColor;
  Color get disabledTextColor;
  Color get borderColor;
  Color get focusedBorderColor;
  double get borderWidth;
  double get borderRadius;
  double get itemHeight;
  EdgeInsets get itemPadding;
  TextStyle? get textStyle;
  Duration get animationDuration;

  @override
  ListThemeExtension copyWith({
    Color? backgroundColor,
    Color? selectedItemBackgroundColor,
    Color? hoverItemBackgroundColor,
    Color? disabledBackgroundColor,
    Color? textColor,
    Color? selectedItemTextColor,
    Color? disabledTextColor,
    Color? borderColor,
    Color? focusedBorderColor,
    double? borderWidth,
    double? borderRadius,
    double? itemHeight,
    EdgeInsets? itemPadding,
    TextStyle? textStyle,
    Duration? animationDuration,
  }) {
    return ListThemeExtension(
      backgroundColor: backgroundColor ?? this.backgroundColor,
      selectedItemBackgroundColor:
          selectedItemBackgroundColor ?? this.selectedItemBackgroundColor,
      hoverItemBackgroundColor:
          hoverItemBackgroundColor ?? this.hoverItemBackgroundColor,
      disabledBackgroundColor:
          disabledBackgroundColor ?? this.disabledBackgroundColor,
      textColor: textColor ?? this.textColor,
      selectedItemTextColor:
          selectedItemTextColor ?? this.selectedItemTextColor,
      disabledTextColor: disabledTextColor ?? this.disabledTextColor,
      borderColor: borderColor ?? this.borderColor,
      focusedBorderColor: focusedBorderColor ?? this.focusedBorderColor,
      borderWidth: borderWidth ?? this.borderWidth,
      borderRadius: borderRadius ?? this.borderRadius,
      itemHeight: itemHeight ?? this.itemHeight,
      itemPadding: itemPadding ?? this.itemPadding,
      textStyle: textStyle ?? this.textStyle,
      animationDuration: animationDuration ?? this.animationDuration,
    );
  }

  @override
  ListThemeExtension lerp(
    covariant ThemeExtension<ListThemeExtension>? other,
    double t,
  ) {
    if (other is! ListThemeExtension) return this as ListThemeExtension;
    return ListThemeExtension(
      backgroundColor: Color.lerp(backgroundColor, other.backgroundColor, t)!,
      selectedItemBackgroundColor: Color.lerp(
        selectedItemBackgroundColor,
        other.selectedItemBackgroundColor,
        t,
      )!,
      hoverItemBackgroundColor: Color.lerp(
        hoverItemBackgroundColor,
        other.hoverItemBackgroundColor,
        t,
      )!,
      disabledBackgroundColor: Color.lerp(
        disabledBackgroundColor,
        other.disabledBackgroundColor,
        t,
      )!,
      textColor: Color.lerp(textColor, other.textColor, t)!,
      selectedItemTextColor: Color.lerp(
        selectedItemTextColor,
        other.selectedItemTextColor,
        t,
      )!,
      disabledTextColor: Color.lerp(
        disabledTextColor,
        other.disabledTextColor,
        t,
      )!,
      borderColor: Color.lerp(borderColor, other.borderColor, t)!,
      focusedBorderColor: Color.lerp(
        focusedBorderColor,
        other.focusedBorderColor,
        t,
      )!,
      borderWidth: t < 0.5 ? borderWidth : other.borderWidth,
      borderRadius: t < 0.5 ? borderRadius : other.borderRadius,
      itemHeight: t < 0.5 ? itemHeight : other.itemHeight,
      itemPadding: t < 0.5 ? itemPadding : other.itemPadding,
      textStyle: TextStyle.lerp(textStyle, other.textStyle, t),
      animationDuration: t < 0.5 ? animationDuration : other.animationDuration,
    );
  }

  @override
  bool operator ==(Object other) {
    return identical(this, other) ||
        (other.runtimeType == runtimeType &&
            other is ListThemeExtension &&
            const DeepCollectionEquality().equals(
              backgroundColor,
              other.backgroundColor,
            ) &&
            const DeepCollectionEquality().equals(
              selectedItemBackgroundColor,
              other.selectedItemBackgroundColor,
            ) &&
            const DeepCollectionEquality().equals(
              hoverItemBackgroundColor,
              other.hoverItemBackgroundColor,
            ) &&
            const DeepCollectionEquality().equals(
              disabledBackgroundColor,
              other.disabledBackgroundColor,
            ) &&
            const DeepCollectionEquality().equals(textColor, other.textColor) &&
            const DeepCollectionEquality().equals(
              selectedItemTextColor,
              other.selectedItemTextColor,
            ) &&
            const DeepCollectionEquality().equals(
              disabledTextColor,
              other.disabledTextColor,
            ) &&
            const DeepCollectionEquality().equals(
              borderColor,
              other.borderColor,
            ) &&
            const DeepCollectionEquality().equals(
              focusedBorderColor,
              other.focusedBorderColor,
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
            const DeepCollectionEquality().equals(
              itemPadding,
              other.itemPadding,
            ) &&
            const DeepCollectionEquality().equals(textStyle, other.textStyle) &&
            const DeepCollectionEquality().equals(
              animationDuration,
              other.animationDuration,
            ));
  }

  @override
  int get hashCode {
    return Object.hash(
      runtimeType.hashCode,
      const DeepCollectionEquality().hash(backgroundColor),
      const DeepCollectionEquality().hash(selectedItemBackgroundColor),
      const DeepCollectionEquality().hash(hoverItemBackgroundColor),
      const DeepCollectionEquality().hash(disabledBackgroundColor),
      const DeepCollectionEquality().hash(textColor),
      const DeepCollectionEquality().hash(selectedItemTextColor),
      const DeepCollectionEquality().hash(disabledTextColor),
      const DeepCollectionEquality().hash(borderColor),
      const DeepCollectionEquality().hash(focusedBorderColor),
      const DeepCollectionEquality().hash(borderWidth),
      const DeepCollectionEquality().hash(borderRadius),
      const DeepCollectionEquality().hash(itemHeight),
      const DeepCollectionEquality().hash(itemPadding),
      const DeepCollectionEquality().hash(textStyle),
      const DeepCollectionEquality().hash(animationDuration),
    );
  }
}

extension ListThemeExtensionBuildContextProps on BuildContext {
  ListThemeExtension get listThemeExtension =>
      Theme.of(this).extension<ListThemeExtension>()!;
  Color get backgroundColor => listThemeExtension.backgroundColor;
  Color get selectedItemBackgroundColor =>
      listThemeExtension.selectedItemBackgroundColor;
  Color get hoverItemBackgroundColor =>
      listThemeExtension.hoverItemBackgroundColor;
  Color get disabledBackgroundColor =>
      listThemeExtension.disabledBackgroundColor;
  Color get textColor => listThemeExtension.textColor;
  Color get selectedItemTextColor => listThemeExtension.selectedItemTextColor;
  Color get disabledTextColor => listThemeExtension.disabledTextColor;
  Color get borderColor => listThemeExtension.borderColor;
  Color get focusedBorderColor => listThemeExtension.focusedBorderColor;
  double get borderWidth => listThemeExtension.borderWidth;
  double get borderRadius => listThemeExtension.borderRadius;
  double get itemHeight => listThemeExtension.itemHeight;
  EdgeInsets get itemPadding => listThemeExtension.itemPadding;
  TextStyle? get textStyle => listThemeExtension.textStyle;
  Duration get animationDuration => listThemeExtension.animationDuration;
}
