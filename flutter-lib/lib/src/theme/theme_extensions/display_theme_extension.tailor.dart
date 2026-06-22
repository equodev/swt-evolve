// coverage:ignore-file
// GENERATED CODE - DO NOT MODIFY BY HAND
// ignore_for_file: type=lint
// ignore_for_file: unused_element, deprecated_member_use, deprecated_member_use_from_same_package, use_function_type_syntax_for_parameters, unnecessary_const, avoid_init_to_null, invalid_override_different_default_values_named, prefer_expression_function_bodies, annotate_overrides, invalid_annotation_target, unnecessary_question_mark

part of 'display_theme_extension.dart';

// **************************************************************************
// TailorAnnotationsGenerator
// **************************************************************************

mixin _$DisplayThemeExtensionTailorMixin
    on ThemeExtension<DisplayThemeExtension> {
  Color get titleBarColor;
  Color get titleBarHoverColor;
  Color get titleBarTextColor;
  double get titleBarHeight;
  double get toolWindowTitleBarHeight;
  double get titleBarBorderRadius;
  Color get closeButtonColor;
  Color get closeButtonHoverColor;
  Color get closeButtonHoverIconColor;
  Color get minimizeButtonColor;
  Color get minimizeButtonHoverColor;
  Color get maximizeButtonColor;
  Color get maximizeButtonHoverColor;
  double get titleBarButtonSize;
  double get titleBarButtonBorderRadius;
  Duration get titleBarButtonAnimationDuration;
  Color get dialogBorderColor;
  double get dialogBorderWidth;
  double get dialogBorderRadius;
  Color get dialogShadowColor;
  double get dialogShadowBlurRadius;
  double get dialogShadowSpreadRadius;
  double get dialogShadowOffsetX;
  double get dialogShadowOffsetY;
  Color get dialogBackgroundColor;
  Color get tooltipShellBackgroundColor;
  Color get modalOverlayColor;
  TextStyle? get titleTextStyle;
  TextStyle? get toolWindowTitleTextStyle;

  @override
  DisplayThemeExtension copyWith({
    Color? titleBarColor,
    Color? titleBarHoverColor,
    Color? titleBarTextColor,
    double? titleBarHeight,
    double? toolWindowTitleBarHeight,
    double? titleBarBorderRadius,
    Color? closeButtonColor,
    Color? closeButtonHoverColor,
    Color? closeButtonHoverIconColor,
    Color? minimizeButtonColor,
    Color? minimizeButtonHoverColor,
    Color? maximizeButtonColor,
    Color? maximizeButtonHoverColor,
    double? titleBarButtonSize,
    double? titleBarButtonBorderRadius,
    Duration? titleBarButtonAnimationDuration,
    Color? dialogBorderColor,
    double? dialogBorderWidth,
    double? dialogBorderRadius,
    Color? dialogShadowColor,
    double? dialogShadowBlurRadius,
    double? dialogShadowSpreadRadius,
    double? dialogShadowOffsetX,
    double? dialogShadowOffsetY,
    Color? dialogBackgroundColor,
    Color? tooltipShellBackgroundColor,
    Color? modalOverlayColor,
    TextStyle? titleTextStyle,
    TextStyle? toolWindowTitleTextStyle,
  }) {
    return DisplayThemeExtension(
      titleBarColor: titleBarColor ?? this.titleBarColor,
      titleBarHoverColor: titleBarHoverColor ?? this.titleBarHoverColor,
      titleBarTextColor: titleBarTextColor ?? this.titleBarTextColor,
      titleBarHeight: titleBarHeight ?? this.titleBarHeight,
      toolWindowTitleBarHeight:
          toolWindowTitleBarHeight ?? this.toolWindowTitleBarHeight,
      titleBarBorderRadius: titleBarBorderRadius ?? this.titleBarBorderRadius,
      closeButtonColor: closeButtonColor ?? this.closeButtonColor,
      closeButtonHoverColor:
          closeButtonHoverColor ?? this.closeButtonHoverColor,
      closeButtonHoverIconColor:
          closeButtonHoverIconColor ?? this.closeButtonHoverIconColor,
      minimizeButtonColor: minimizeButtonColor ?? this.minimizeButtonColor,
      minimizeButtonHoverColor:
          minimizeButtonHoverColor ?? this.minimizeButtonHoverColor,
      maximizeButtonColor: maximizeButtonColor ?? this.maximizeButtonColor,
      maximizeButtonHoverColor:
          maximizeButtonHoverColor ?? this.maximizeButtonHoverColor,
      titleBarButtonSize: titleBarButtonSize ?? this.titleBarButtonSize,
      titleBarButtonBorderRadius:
          titleBarButtonBorderRadius ?? this.titleBarButtonBorderRadius,
      titleBarButtonAnimationDuration:
          titleBarButtonAnimationDuration ??
          this.titleBarButtonAnimationDuration,
      dialogBorderColor: dialogBorderColor ?? this.dialogBorderColor,
      dialogBorderWidth: dialogBorderWidth ?? this.dialogBorderWidth,
      dialogBorderRadius: dialogBorderRadius ?? this.dialogBorderRadius,
      dialogShadowColor: dialogShadowColor ?? this.dialogShadowColor,
      dialogShadowBlurRadius:
          dialogShadowBlurRadius ?? this.dialogShadowBlurRadius,
      dialogShadowSpreadRadius:
          dialogShadowSpreadRadius ?? this.dialogShadowSpreadRadius,
      dialogShadowOffsetX: dialogShadowOffsetX ?? this.dialogShadowOffsetX,
      dialogShadowOffsetY: dialogShadowOffsetY ?? this.dialogShadowOffsetY,
      dialogBackgroundColor:
          dialogBackgroundColor ?? this.dialogBackgroundColor,
      tooltipShellBackgroundColor:
          tooltipShellBackgroundColor ?? this.tooltipShellBackgroundColor,
      modalOverlayColor: modalOverlayColor ?? this.modalOverlayColor,
      titleTextStyle: titleTextStyle ?? this.titleTextStyle,
      toolWindowTitleTextStyle:
          toolWindowTitleTextStyle ?? this.toolWindowTitleTextStyle,
    );
  }

  @override
  DisplayThemeExtension lerp(
    covariant ThemeExtension<DisplayThemeExtension>? other,
    double t,
  ) {
    if (other is! DisplayThemeExtension) return this as DisplayThemeExtension;
    return DisplayThemeExtension(
      titleBarColor: Color.lerp(titleBarColor, other.titleBarColor, t)!,
      titleBarHoverColor: Color.lerp(
        titleBarHoverColor,
        other.titleBarHoverColor,
        t,
      )!,
      titleBarTextColor: Color.lerp(
        titleBarTextColor,
        other.titleBarTextColor,
        t,
      )!,
      titleBarHeight: t < 0.5 ? titleBarHeight : other.titleBarHeight,
      toolWindowTitleBarHeight: t < 0.5
          ? toolWindowTitleBarHeight
          : other.toolWindowTitleBarHeight,
      titleBarBorderRadius: t < 0.5
          ? titleBarBorderRadius
          : other.titleBarBorderRadius,
      closeButtonColor: Color.lerp(
        closeButtonColor,
        other.closeButtonColor,
        t,
      )!,
      closeButtonHoverColor: Color.lerp(
        closeButtonHoverColor,
        other.closeButtonHoverColor,
        t,
      )!,
      closeButtonHoverIconColor: Color.lerp(
        closeButtonHoverIconColor,
        other.closeButtonHoverIconColor,
        t,
      )!,
      minimizeButtonColor: Color.lerp(
        minimizeButtonColor,
        other.minimizeButtonColor,
        t,
      )!,
      minimizeButtonHoverColor: Color.lerp(
        minimizeButtonHoverColor,
        other.minimizeButtonHoverColor,
        t,
      )!,
      maximizeButtonColor: Color.lerp(
        maximizeButtonColor,
        other.maximizeButtonColor,
        t,
      )!,
      maximizeButtonHoverColor: Color.lerp(
        maximizeButtonHoverColor,
        other.maximizeButtonHoverColor,
        t,
      )!,
      titleBarButtonSize: t < 0.5
          ? titleBarButtonSize
          : other.titleBarButtonSize,
      titleBarButtonBorderRadius: t < 0.5
          ? titleBarButtonBorderRadius
          : other.titleBarButtonBorderRadius,
      titleBarButtonAnimationDuration: t < 0.5
          ? titleBarButtonAnimationDuration
          : other.titleBarButtonAnimationDuration,
      dialogBorderColor: Color.lerp(
        dialogBorderColor,
        other.dialogBorderColor,
        t,
      )!,
      dialogBorderWidth: t < 0.5 ? dialogBorderWidth : other.dialogBorderWidth,
      dialogBorderRadius: t < 0.5
          ? dialogBorderRadius
          : other.dialogBorderRadius,
      dialogShadowColor: Color.lerp(
        dialogShadowColor,
        other.dialogShadowColor,
        t,
      )!,
      dialogShadowBlurRadius: t < 0.5
          ? dialogShadowBlurRadius
          : other.dialogShadowBlurRadius,
      dialogShadowSpreadRadius: t < 0.5
          ? dialogShadowSpreadRadius
          : other.dialogShadowSpreadRadius,
      dialogShadowOffsetX: t < 0.5
          ? dialogShadowOffsetX
          : other.dialogShadowOffsetX,
      dialogShadowOffsetY: t < 0.5
          ? dialogShadowOffsetY
          : other.dialogShadowOffsetY,
      dialogBackgroundColor: Color.lerp(
        dialogBackgroundColor,
        other.dialogBackgroundColor,
        t,
      )!,
      tooltipShellBackgroundColor: Color.lerp(
        tooltipShellBackgroundColor,
        other.tooltipShellBackgroundColor,
        t,
      )!,
      modalOverlayColor: Color.lerp(
        modalOverlayColor,
        other.modalOverlayColor,
        t,
      )!,
      titleTextStyle: TextStyle.lerp(titleTextStyle, other.titleTextStyle, t),
      toolWindowTitleTextStyle: TextStyle.lerp(
        toolWindowTitleTextStyle,
        other.toolWindowTitleTextStyle,
        t,
      ),
    );
  }

  @override
  bool operator ==(Object other) {
    return identical(this, other) ||
        (other.runtimeType == runtimeType &&
            other is DisplayThemeExtension &&
            const DeepCollectionEquality().equals(
              titleBarColor,
              other.titleBarColor,
            ) &&
            const DeepCollectionEquality().equals(
              titleBarHoverColor,
              other.titleBarHoverColor,
            ) &&
            const DeepCollectionEquality().equals(
              titleBarTextColor,
              other.titleBarTextColor,
            ) &&
            const DeepCollectionEquality().equals(
              titleBarHeight,
              other.titleBarHeight,
            ) &&
            const DeepCollectionEquality().equals(
              toolWindowTitleBarHeight,
              other.toolWindowTitleBarHeight,
            ) &&
            const DeepCollectionEquality().equals(
              titleBarBorderRadius,
              other.titleBarBorderRadius,
            ) &&
            const DeepCollectionEquality().equals(
              closeButtonColor,
              other.closeButtonColor,
            ) &&
            const DeepCollectionEquality().equals(
              closeButtonHoverColor,
              other.closeButtonHoverColor,
            ) &&
            const DeepCollectionEquality().equals(
              closeButtonHoverIconColor,
              other.closeButtonHoverIconColor,
            ) &&
            const DeepCollectionEquality().equals(
              minimizeButtonColor,
              other.minimizeButtonColor,
            ) &&
            const DeepCollectionEquality().equals(
              minimizeButtonHoverColor,
              other.minimizeButtonHoverColor,
            ) &&
            const DeepCollectionEquality().equals(
              maximizeButtonColor,
              other.maximizeButtonColor,
            ) &&
            const DeepCollectionEquality().equals(
              maximizeButtonHoverColor,
              other.maximizeButtonHoverColor,
            ) &&
            const DeepCollectionEquality().equals(
              titleBarButtonSize,
              other.titleBarButtonSize,
            ) &&
            const DeepCollectionEquality().equals(
              titleBarButtonBorderRadius,
              other.titleBarButtonBorderRadius,
            ) &&
            const DeepCollectionEquality().equals(
              titleBarButtonAnimationDuration,
              other.titleBarButtonAnimationDuration,
            ) &&
            const DeepCollectionEquality().equals(
              dialogBorderColor,
              other.dialogBorderColor,
            ) &&
            const DeepCollectionEquality().equals(
              dialogBorderWidth,
              other.dialogBorderWidth,
            ) &&
            const DeepCollectionEquality().equals(
              dialogBorderRadius,
              other.dialogBorderRadius,
            ) &&
            const DeepCollectionEquality().equals(
              dialogShadowColor,
              other.dialogShadowColor,
            ) &&
            const DeepCollectionEquality().equals(
              dialogShadowBlurRadius,
              other.dialogShadowBlurRadius,
            ) &&
            const DeepCollectionEquality().equals(
              dialogShadowSpreadRadius,
              other.dialogShadowSpreadRadius,
            ) &&
            const DeepCollectionEquality().equals(
              dialogShadowOffsetX,
              other.dialogShadowOffsetX,
            ) &&
            const DeepCollectionEquality().equals(
              dialogShadowOffsetY,
              other.dialogShadowOffsetY,
            ) &&
            const DeepCollectionEquality().equals(
              dialogBackgroundColor,
              other.dialogBackgroundColor,
            ) &&
            const DeepCollectionEquality().equals(
              tooltipShellBackgroundColor,
              other.tooltipShellBackgroundColor,
            ) &&
            const DeepCollectionEquality().equals(
              modalOverlayColor,
              other.modalOverlayColor,
            ) &&
            const DeepCollectionEquality().equals(
              titleTextStyle,
              other.titleTextStyle,
            ) &&
            const DeepCollectionEquality().equals(
              toolWindowTitleTextStyle,
              other.toolWindowTitleTextStyle,
            ));
  }

  @override
  int get hashCode {
    return Object.hashAll([
      runtimeType.hashCode,
      const DeepCollectionEquality().hash(titleBarColor),
      const DeepCollectionEquality().hash(titleBarHoverColor),
      const DeepCollectionEquality().hash(titleBarTextColor),
      const DeepCollectionEquality().hash(titleBarHeight),
      const DeepCollectionEquality().hash(toolWindowTitleBarHeight),
      const DeepCollectionEquality().hash(titleBarBorderRadius),
      const DeepCollectionEquality().hash(closeButtonColor),
      const DeepCollectionEquality().hash(closeButtonHoverColor),
      const DeepCollectionEquality().hash(closeButtonHoverIconColor),
      const DeepCollectionEquality().hash(minimizeButtonColor),
      const DeepCollectionEquality().hash(minimizeButtonHoverColor),
      const DeepCollectionEquality().hash(maximizeButtonColor),
      const DeepCollectionEquality().hash(maximizeButtonHoverColor),
      const DeepCollectionEquality().hash(titleBarButtonSize),
      const DeepCollectionEquality().hash(titleBarButtonBorderRadius),
      const DeepCollectionEquality().hash(titleBarButtonAnimationDuration),
      const DeepCollectionEquality().hash(dialogBorderColor),
      const DeepCollectionEquality().hash(dialogBorderWidth),
      const DeepCollectionEquality().hash(dialogBorderRadius),
      const DeepCollectionEquality().hash(dialogShadowColor),
      const DeepCollectionEquality().hash(dialogShadowBlurRadius),
      const DeepCollectionEquality().hash(dialogShadowSpreadRadius),
      const DeepCollectionEquality().hash(dialogShadowOffsetX),
      const DeepCollectionEquality().hash(dialogShadowOffsetY),
      const DeepCollectionEquality().hash(dialogBackgroundColor),
      const DeepCollectionEquality().hash(tooltipShellBackgroundColor),
      const DeepCollectionEquality().hash(modalOverlayColor),
      const DeepCollectionEquality().hash(titleTextStyle),
      const DeepCollectionEquality().hash(toolWindowTitleTextStyle),
    ]);
  }
}

extension DisplayThemeExtensionBuildContextProps on BuildContext {
  DisplayThemeExtension get displayThemeExtension =>
      Theme.of(this).extension<DisplayThemeExtension>()!;
  Color get titleBarColor => displayThemeExtension.titleBarColor;
  Color get titleBarHoverColor => displayThemeExtension.titleBarHoverColor;
  Color get titleBarTextColor => displayThemeExtension.titleBarTextColor;
  double get titleBarHeight => displayThemeExtension.titleBarHeight;
  double get toolWindowTitleBarHeight =>
      displayThemeExtension.toolWindowTitleBarHeight;
  double get titleBarBorderRadius => displayThemeExtension.titleBarBorderRadius;
  Color get closeButtonColor => displayThemeExtension.closeButtonColor;
  Color get closeButtonHoverColor =>
      displayThemeExtension.closeButtonHoverColor;
  Color get closeButtonHoverIconColor =>
      displayThemeExtension.closeButtonHoverIconColor;
  Color get minimizeButtonColor => displayThemeExtension.minimizeButtonColor;
  Color get minimizeButtonHoverColor =>
      displayThemeExtension.minimizeButtonHoverColor;
  Color get maximizeButtonColor => displayThemeExtension.maximizeButtonColor;
  Color get maximizeButtonHoverColor =>
      displayThemeExtension.maximizeButtonHoverColor;
  double get titleBarButtonSize => displayThemeExtension.titleBarButtonSize;
  double get titleBarButtonBorderRadius =>
      displayThemeExtension.titleBarButtonBorderRadius;
  Duration get titleBarButtonAnimationDuration =>
      displayThemeExtension.titleBarButtonAnimationDuration;
  Color get dialogBorderColor => displayThemeExtension.dialogBorderColor;
  double get dialogBorderWidth => displayThemeExtension.dialogBorderWidth;
  double get dialogBorderRadius => displayThemeExtension.dialogBorderRadius;
  Color get dialogShadowColor => displayThemeExtension.dialogShadowColor;
  double get dialogShadowBlurRadius =>
      displayThemeExtension.dialogShadowBlurRadius;
  double get dialogShadowSpreadRadius =>
      displayThemeExtension.dialogShadowSpreadRadius;
  double get dialogShadowOffsetX => displayThemeExtension.dialogShadowOffsetX;
  double get dialogShadowOffsetY => displayThemeExtension.dialogShadowOffsetY;
  Color get dialogBackgroundColor =>
      displayThemeExtension.dialogBackgroundColor;
  Color get tooltipShellBackgroundColor =>
      displayThemeExtension.tooltipShellBackgroundColor;
  Color get modalOverlayColor => displayThemeExtension.modalOverlayColor;
  TextStyle? get titleTextStyle => displayThemeExtension.titleTextStyle;
  TextStyle? get toolWindowTitleTextStyle =>
      displayThemeExtension.toolWindowTitleTextStyle;
}
