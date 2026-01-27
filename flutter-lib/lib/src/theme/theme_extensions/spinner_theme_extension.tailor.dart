// coverage:ignore-file
// GENERATED CODE - DO NOT MODIFY BY HAND
// ignore_for_file: type=lint
// ignore_for_file: unused_element, deprecated_member_use, deprecated_member_use_from_same_package, use_function_type_syntax_for_parameters, unnecessary_const, avoid_init_to_null, invalid_override_different_default_values_named, prefer_expression_function_bodies, annotate_overrides, invalid_annotation_target, unnecessary_question_mark

part of 'spinner_theme_extension.dart';

// **************************************************************************
// TailorAnnotationsGenerator
// **************************************************************************

mixin _$SpinnerThemeExtensionTailorMixin
    on ThemeExtension<SpinnerThemeExtension> {
  Duration get animationDuration;
  Color get backgroundColor;
  Color get disabledBackgroundColor;
  Color get buttonBackgroundColor;
  Color get buttonHoverColor;
  Color get buttonPressedColor;
  Color get textColor;
  Color get disabledTextColor;
  Color get borderColor;
  Color get focusedBorderColor;
  Color get disabledBorderColor;
  Color get iconColor;
  Color get iconHoverColor;
  Color get disabledIconColor;
  double get borderWidth;
  double get borderRadius;
  double get minWidth;
  double get minHeight;
  double get buttonWidth;
  double get iconSize;
  EdgeInsets get textFieldPadding;
  TextStyle? get textStyle;
  int get defaultMinimum;
  int get defaultMaximum;
  int get defaultSelection;
  int get defaultIncrement;
  int get defaultPageIncrement;
  int get defaultDigits;
  int get defaultTextLimit;

  @override
  SpinnerThemeExtension copyWith({
    Duration? animationDuration,
    Color? backgroundColor,
    Color? disabledBackgroundColor,
    Color? buttonBackgroundColor,
    Color? buttonHoverColor,
    Color? buttonPressedColor,
    Color? textColor,
    Color? disabledTextColor,
    Color? borderColor,
    Color? focusedBorderColor,
    Color? disabledBorderColor,
    Color? iconColor,
    Color? iconHoverColor,
    Color? disabledIconColor,
    double? borderWidth,
    double? borderRadius,
    double? minWidth,
    double? minHeight,
    double? buttonWidth,
    double? iconSize,
    EdgeInsets? textFieldPadding,
    TextStyle? textStyle,
    int? defaultMinimum,
    int? defaultMaximum,
    int? defaultSelection,
    int? defaultIncrement,
    int? defaultPageIncrement,
    int? defaultDigits,
    int? defaultTextLimit,
  }) {
    return SpinnerThemeExtension(
      animationDuration: animationDuration ?? this.animationDuration,
      backgroundColor: backgroundColor ?? this.backgroundColor,
      disabledBackgroundColor:
          disabledBackgroundColor ?? this.disabledBackgroundColor,
      buttonBackgroundColor:
          buttonBackgroundColor ?? this.buttonBackgroundColor,
      buttonHoverColor: buttonHoverColor ?? this.buttonHoverColor,
      buttonPressedColor: buttonPressedColor ?? this.buttonPressedColor,
      textColor: textColor ?? this.textColor,
      disabledTextColor: disabledTextColor ?? this.disabledTextColor,
      borderColor: borderColor ?? this.borderColor,
      focusedBorderColor: focusedBorderColor ?? this.focusedBorderColor,
      disabledBorderColor: disabledBorderColor ?? this.disabledBorderColor,
      iconColor: iconColor ?? this.iconColor,
      iconHoverColor: iconHoverColor ?? this.iconHoverColor,
      disabledIconColor: disabledIconColor ?? this.disabledIconColor,
      borderWidth: borderWidth ?? this.borderWidth,
      borderRadius: borderRadius ?? this.borderRadius,
      minWidth: minWidth ?? this.minWidth,
      minHeight: minHeight ?? this.minHeight,
      buttonWidth: buttonWidth ?? this.buttonWidth,
      iconSize: iconSize ?? this.iconSize,
      textFieldPadding: textFieldPadding ?? this.textFieldPadding,
      textStyle: textStyle ?? this.textStyle,
      defaultMinimum: defaultMinimum ?? this.defaultMinimum,
      defaultMaximum: defaultMaximum ?? this.defaultMaximum,
      defaultSelection: defaultSelection ?? this.defaultSelection,
      defaultIncrement: defaultIncrement ?? this.defaultIncrement,
      defaultPageIncrement: defaultPageIncrement ?? this.defaultPageIncrement,
      defaultDigits: defaultDigits ?? this.defaultDigits,
      defaultTextLimit: defaultTextLimit ?? this.defaultTextLimit,
    );
  }

  @override
  SpinnerThemeExtension lerp(
    covariant ThemeExtension<SpinnerThemeExtension>? other,
    double t,
  ) {
    if (other is! SpinnerThemeExtension) return this as SpinnerThemeExtension;
    return SpinnerThemeExtension(
      animationDuration: t < 0.5 ? animationDuration : other.animationDuration,
      backgroundColor: Color.lerp(backgroundColor, other.backgroundColor, t)!,
      disabledBackgroundColor: Color.lerp(
        disabledBackgroundColor,
        other.disabledBackgroundColor,
        t,
      )!,
      buttonBackgroundColor: Color.lerp(
        buttonBackgroundColor,
        other.buttonBackgroundColor,
        t,
      )!,
      buttonHoverColor: Color.lerp(
        buttonHoverColor,
        other.buttonHoverColor,
        t,
      )!,
      buttonPressedColor: Color.lerp(
        buttonPressedColor,
        other.buttonPressedColor,
        t,
      )!,
      textColor: Color.lerp(textColor, other.textColor, t)!,
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
      disabledBorderColor: Color.lerp(
        disabledBorderColor,
        other.disabledBorderColor,
        t,
      )!,
      iconColor: Color.lerp(iconColor, other.iconColor, t)!,
      iconHoverColor: Color.lerp(iconHoverColor, other.iconHoverColor, t)!,
      disabledIconColor: Color.lerp(
        disabledIconColor,
        other.disabledIconColor,
        t,
      )!,
      borderWidth: t < 0.5 ? borderWidth : other.borderWidth,
      borderRadius: t < 0.5 ? borderRadius : other.borderRadius,
      minWidth: t < 0.5 ? minWidth : other.minWidth,
      minHeight: t < 0.5 ? minHeight : other.minHeight,
      buttonWidth: t < 0.5 ? buttonWidth : other.buttonWidth,
      iconSize: t < 0.5 ? iconSize : other.iconSize,
      textFieldPadding: t < 0.5 ? textFieldPadding : other.textFieldPadding,
      textStyle: TextStyle.lerp(textStyle, other.textStyle, t),
      defaultMinimum: t < 0.5 ? defaultMinimum : other.defaultMinimum,
      defaultMaximum: t < 0.5 ? defaultMaximum : other.defaultMaximum,
      defaultSelection: t < 0.5 ? defaultSelection : other.defaultSelection,
      defaultIncrement: t < 0.5 ? defaultIncrement : other.defaultIncrement,
      defaultPageIncrement: t < 0.5
          ? defaultPageIncrement
          : other.defaultPageIncrement,
      defaultDigits: t < 0.5 ? defaultDigits : other.defaultDigits,
      defaultTextLimit: t < 0.5 ? defaultTextLimit : other.defaultTextLimit,
    );
  }

  @override
  bool operator ==(Object other) {
    return identical(this, other) ||
        (other.runtimeType == runtimeType &&
            other is SpinnerThemeExtension &&
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
              buttonBackgroundColor,
              other.buttonBackgroundColor,
            ) &&
            const DeepCollectionEquality().equals(
              buttonHoverColor,
              other.buttonHoverColor,
            ) &&
            const DeepCollectionEquality().equals(
              buttonPressedColor,
              other.buttonPressedColor,
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
              focusedBorderColor,
              other.focusedBorderColor,
            ) &&
            const DeepCollectionEquality().equals(
              disabledBorderColor,
              other.disabledBorderColor,
            ) &&
            const DeepCollectionEquality().equals(iconColor, other.iconColor) &&
            const DeepCollectionEquality().equals(
              iconHoverColor,
              other.iconHoverColor,
            ) &&
            const DeepCollectionEquality().equals(
              disabledIconColor,
              other.disabledIconColor,
            ) &&
            const DeepCollectionEquality().equals(
              borderWidth,
              other.borderWidth,
            ) &&
            const DeepCollectionEquality().equals(
              borderRadius,
              other.borderRadius,
            ) &&
            const DeepCollectionEquality().equals(minWidth, other.minWidth) &&
            const DeepCollectionEquality().equals(minHeight, other.minHeight) &&
            const DeepCollectionEquality().equals(
              buttonWidth,
              other.buttonWidth,
            ) &&
            const DeepCollectionEquality().equals(iconSize, other.iconSize) &&
            const DeepCollectionEquality().equals(
              textFieldPadding,
              other.textFieldPadding,
            ) &&
            const DeepCollectionEquality().equals(textStyle, other.textStyle) &&
            const DeepCollectionEquality().equals(
              defaultMinimum,
              other.defaultMinimum,
            ) &&
            const DeepCollectionEquality().equals(
              defaultMaximum,
              other.defaultMaximum,
            ) &&
            const DeepCollectionEquality().equals(
              defaultSelection,
              other.defaultSelection,
            ) &&
            const DeepCollectionEquality().equals(
              defaultIncrement,
              other.defaultIncrement,
            ) &&
            const DeepCollectionEquality().equals(
              defaultPageIncrement,
              other.defaultPageIncrement,
            ) &&
            const DeepCollectionEquality().equals(
              defaultDigits,
              other.defaultDigits,
            ) &&
            const DeepCollectionEquality().equals(
              defaultTextLimit,
              other.defaultTextLimit,
            ));
  }

  @override
  int get hashCode {
    return Object.hashAll([
      runtimeType.hashCode,
      const DeepCollectionEquality().hash(animationDuration),
      const DeepCollectionEquality().hash(backgroundColor),
      const DeepCollectionEquality().hash(disabledBackgroundColor),
      const DeepCollectionEquality().hash(buttonBackgroundColor),
      const DeepCollectionEquality().hash(buttonHoverColor),
      const DeepCollectionEquality().hash(buttonPressedColor),
      const DeepCollectionEquality().hash(textColor),
      const DeepCollectionEquality().hash(disabledTextColor),
      const DeepCollectionEquality().hash(borderColor),
      const DeepCollectionEquality().hash(focusedBorderColor),
      const DeepCollectionEquality().hash(disabledBorderColor),
      const DeepCollectionEquality().hash(iconColor),
      const DeepCollectionEquality().hash(iconHoverColor),
      const DeepCollectionEquality().hash(disabledIconColor),
      const DeepCollectionEquality().hash(borderWidth),
      const DeepCollectionEquality().hash(borderRadius),
      const DeepCollectionEquality().hash(minWidth),
      const DeepCollectionEquality().hash(minHeight),
      const DeepCollectionEquality().hash(buttonWidth),
      const DeepCollectionEquality().hash(iconSize),
      const DeepCollectionEquality().hash(textFieldPadding),
      const DeepCollectionEquality().hash(textStyle),
      const DeepCollectionEquality().hash(defaultMinimum),
      const DeepCollectionEquality().hash(defaultMaximum),
      const DeepCollectionEquality().hash(defaultSelection),
      const DeepCollectionEquality().hash(defaultIncrement),
      const DeepCollectionEquality().hash(defaultPageIncrement),
      const DeepCollectionEquality().hash(defaultDigits),
      const DeepCollectionEquality().hash(defaultTextLimit),
    ]);
  }
}

extension SpinnerThemeExtensionBuildContextProps on BuildContext {
  SpinnerThemeExtension get spinnerThemeExtension =>
      Theme.of(this).extension<SpinnerThemeExtension>()!;
  Duration get animationDuration => spinnerThemeExtension.animationDuration;
  Color get backgroundColor => spinnerThemeExtension.backgroundColor;
  Color get disabledBackgroundColor =>
      spinnerThemeExtension.disabledBackgroundColor;
  Color get buttonBackgroundColor =>
      spinnerThemeExtension.buttonBackgroundColor;
  Color get buttonHoverColor => spinnerThemeExtension.buttonHoverColor;
  Color get buttonPressedColor => spinnerThemeExtension.buttonPressedColor;
  Color get textColor => spinnerThemeExtension.textColor;
  Color get disabledTextColor => spinnerThemeExtension.disabledTextColor;
  Color get borderColor => spinnerThemeExtension.borderColor;
  Color get focusedBorderColor => spinnerThemeExtension.focusedBorderColor;
  Color get disabledBorderColor => spinnerThemeExtension.disabledBorderColor;
  Color get iconColor => spinnerThemeExtension.iconColor;
  Color get iconHoverColor => spinnerThemeExtension.iconHoverColor;
  Color get disabledIconColor => spinnerThemeExtension.disabledIconColor;
  double get borderWidth => spinnerThemeExtension.borderWidth;
  double get borderRadius => spinnerThemeExtension.borderRadius;
  double get minWidth => spinnerThemeExtension.minWidth;
  double get minHeight => spinnerThemeExtension.minHeight;
  double get buttonWidth => spinnerThemeExtension.buttonWidth;
  double get iconSize => spinnerThemeExtension.iconSize;
  EdgeInsets get textFieldPadding => spinnerThemeExtension.textFieldPadding;
  TextStyle? get textStyle => spinnerThemeExtension.textStyle;
  int get defaultMinimum => spinnerThemeExtension.defaultMinimum;
  int get defaultMaximum => spinnerThemeExtension.defaultMaximum;
  int get defaultSelection => spinnerThemeExtension.defaultSelection;
  int get defaultIncrement => spinnerThemeExtension.defaultIncrement;
  int get defaultPageIncrement => spinnerThemeExtension.defaultPageIncrement;
  int get defaultDigits => spinnerThemeExtension.defaultDigits;
  int get defaultTextLimit => spinnerThemeExtension.defaultTextLimit;
}
