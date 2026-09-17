// coverage:ignore-file
// GENERATED CODE - DO NOT MODIFY BY HAND
// ignore_for_file: type=lint
// ignore_for_file: unused_element, deprecated_member_use, deprecated_member_use_from_same_package, use_function_type_syntax_for_parameters, unnecessary_const, avoid_init_to_null, invalid_override_different_default_values_named, prefer_expression_function_bodies, annotate_overrides, invalid_annotation_target, unnecessary_question_mark

part of 'toast_theme_extension.dart';

// **************************************************************************
// TailorAnnotationsGenerator
// **************************************************************************

mixin _$ToastThemeExtensionTailorMixin on ThemeExtension<ToastThemeExtension> {
  Color get backgroundColor;
  Color get borderColor;
  double get borderWidth;
  double get borderRadius;
  TextStyle? get titleTextStyle;
  TextStyle? get messageTextStyle;
  int get messageMaxLines;
  double get titleMessageSpacing;
  Color get infoColor;
  Color get successColor;
  Color get warningColor;
  Color get errorColor;
  double get accentBarWidth;
  double get iconSize;
  double get iconSpacing;
  Color get closeIconColor;
  double get closeIconSize;
  EdgeInsets get padding;
  double get width;
  EdgeInsets get margin;
  double get spacing;
  int get maxVisible;
  Duration get slideInDuration;
  Duration get fadeOutDuration;
  Duration get displayDuration;
  double get slideOffsetX;
  Color get shadowColor;
  double get shadowBlurRadius;
  double get shadowOffsetY;

  @override
  ToastThemeExtension copyWith({
    Color? backgroundColor,
    Color? borderColor,
    double? borderWidth,
    double? borderRadius,
    TextStyle? titleTextStyle,
    TextStyle? messageTextStyle,
    int? messageMaxLines,
    double? titleMessageSpacing,
    Color? infoColor,
    Color? successColor,
    Color? warningColor,
    Color? errorColor,
    double? accentBarWidth,
    double? iconSize,
    double? iconSpacing,
    Color? closeIconColor,
    double? closeIconSize,
    EdgeInsets? padding,
    double? width,
    EdgeInsets? margin,
    double? spacing,
    int? maxVisible,
    Duration? slideInDuration,
    Duration? fadeOutDuration,
    Duration? displayDuration,
    double? slideOffsetX,
    Color? shadowColor,
    double? shadowBlurRadius,
    double? shadowOffsetY,
  }) {
    return ToastThemeExtension(
      backgroundColor: backgroundColor ?? this.backgroundColor,
      borderColor: borderColor ?? this.borderColor,
      borderWidth: borderWidth ?? this.borderWidth,
      borderRadius: borderRadius ?? this.borderRadius,
      titleTextStyle: titleTextStyle ?? this.titleTextStyle,
      messageTextStyle: messageTextStyle ?? this.messageTextStyle,
      messageMaxLines: messageMaxLines ?? this.messageMaxLines,
      titleMessageSpacing: titleMessageSpacing ?? this.titleMessageSpacing,
      infoColor: infoColor ?? this.infoColor,
      successColor: successColor ?? this.successColor,
      warningColor: warningColor ?? this.warningColor,
      errorColor: errorColor ?? this.errorColor,
      accentBarWidth: accentBarWidth ?? this.accentBarWidth,
      iconSize: iconSize ?? this.iconSize,
      iconSpacing: iconSpacing ?? this.iconSpacing,
      closeIconColor: closeIconColor ?? this.closeIconColor,
      closeIconSize: closeIconSize ?? this.closeIconSize,
      padding: padding ?? this.padding,
      width: width ?? this.width,
      margin: margin ?? this.margin,
      spacing: spacing ?? this.spacing,
      maxVisible: maxVisible ?? this.maxVisible,
      slideInDuration: slideInDuration ?? this.slideInDuration,
      fadeOutDuration: fadeOutDuration ?? this.fadeOutDuration,
      displayDuration: displayDuration ?? this.displayDuration,
      slideOffsetX: slideOffsetX ?? this.slideOffsetX,
      shadowColor: shadowColor ?? this.shadowColor,
      shadowBlurRadius: shadowBlurRadius ?? this.shadowBlurRadius,
      shadowOffsetY: shadowOffsetY ?? this.shadowOffsetY,
    );
  }

  @override
  ToastThemeExtension lerp(
    covariant ThemeExtension<ToastThemeExtension>? other,
    double t,
  ) {
    if (other is! ToastThemeExtension) return this as ToastThemeExtension;
    return ToastThemeExtension(
      backgroundColor: Color.lerp(backgroundColor, other.backgroundColor, t)!,
      borderColor: Color.lerp(borderColor, other.borderColor, t)!,
      borderWidth: t < 0.5 ? borderWidth : other.borderWidth,
      borderRadius: t < 0.5 ? borderRadius : other.borderRadius,
      titleTextStyle: TextStyle.lerp(titleTextStyle, other.titleTextStyle, t),
      messageTextStyle: TextStyle.lerp(
        messageTextStyle,
        other.messageTextStyle,
        t,
      ),
      messageMaxLines: t < 0.5 ? messageMaxLines : other.messageMaxLines,
      titleMessageSpacing: t < 0.5
          ? titleMessageSpacing
          : other.titleMessageSpacing,
      infoColor: Color.lerp(infoColor, other.infoColor, t)!,
      successColor: Color.lerp(successColor, other.successColor, t)!,
      warningColor: Color.lerp(warningColor, other.warningColor, t)!,
      errorColor: Color.lerp(errorColor, other.errorColor, t)!,
      accentBarWidth: t < 0.5 ? accentBarWidth : other.accentBarWidth,
      iconSize: t < 0.5 ? iconSize : other.iconSize,
      iconSpacing: t < 0.5 ? iconSpacing : other.iconSpacing,
      closeIconColor: Color.lerp(closeIconColor, other.closeIconColor, t)!,
      closeIconSize: t < 0.5 ? closeIconSize : other.closeIconSize,
      padding: t < 0.5 ? padding : other.padding,
      width: t < 0.5 ? width : other.width,
      margin: t < 0.5 ? margin : other.margin,
      spacing: t < 0.5 ? spacing : other.spacing,
      maxVisible: t < 0.5 ? maxVisible : other.maxVisible,
      slideInDuration: t < 0.5 ? slideInDuration : other.slideInDuration,
      fadeOutDuration: t < 0.5 ? fadeOutDuration : other.fadeOutDuration,
      displayDuration: t < 0.5 ? displayDuration : other.displayDuration,
      slideOffsetX: t < 0.5 ? slideOffsetX : other.slideOffsetX,
      shadowColor: Color.lerp(shadowColor, other.shadowColor, t)!,
      shadowBlurRadius: t < 0.5 ? shadowBlurRadius : other.shadowBlurRadius,
      shadowOffsetY: t < 0.5 ? shadowOffsetY : other.shadowOffsetY,
    );
  }

  @override
  bool operator ==(Object other) {
    return identical(this, other) ||
        (other.runtimeType == runtimeType &&
            other is ToastThemeExtension &&
            const DeepCollectionEquality().equals(
              backgroundColor,
              other.backgroundColor,
            ) &&
            const DeepCollectionEquality().equals(
              borderColor,
              other.borderColor,
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
              titleTextStyle,
              other.titleTextStyle,
            ) &&
            const DeepCollectionEquality().equals(
              messageTextStyle,
              other.messageTextStyle,
            ) &&
            const DeepCollectionEquality().equals(
              messageMaxLines,
              other.messageMaxLines,
            ) &&
            const DeepCollectionEquality().equals(
              titleMessageSpacing,
              other.titleMessageSpacing,
            ) &&
            const DeepCollectionEquality().equals(infoColor, other.infoColor) &&
            const DeepCollectionEquality().equals(
              successColor,
              other.successColor,
            ) &&
            const DeepCollectionEquality().equals(
              warningColor,
              other.warningColor,
            ) &&
            const DeepCollectionEquality().equals(
              errorColor,
              other.errorColor,
            ) &&
            const DeepCollectionEquality().equals(
              accentBarWidth,
              other.accentBarWidth,
            ) &&
            const DeepCollectionEquality().equals(iconSize, other.iconSize) &&
            const DeepCollectionEquality().equals(
              iconSpacing,
              other.iconSpacing,
            ) &&
            const DeepCollectionEquality().equals(
              closeIconColor,
              other.closeIconColor,
            ) &&
            const DeepCollectionEquality().equals(
              closeIconSize,
              other.closeIconSize,
            ) &&
            const DeepCollectionEquality().equals(padding, other.padding) &&
            const DeepCollectionEquality().equals(width, other.width) &&
            const DeepCollectionEquality().equals(margin, other.margin) &&
            const DeepCollectionEquality().equals(spacing, other.spacing) &&
            const DeepCollectionEquality().equals(
              maxVisible,
              other.maxVisible,
            ) &&
            const DeepCollectionEquality().equals(
              slideInDuration,
              other.slideInDuration,
            ) &&
            const DeepCollectionEquality().equals(
              fadeOutDuration,
              other.fadeOutDuration,
            ) &&
            const DeepCollectionEquality().equals(
              displayDuration,
              other.displayDuration,
            ) &&
            const DeepCollectionEquality().equals(
              slideOffsetX,
              other.slideOffsetX,
            ) &&
            const DeepCollectionEquality().equals(
              shadowColor,
              other.shadowColor,
            ) &&
            const DeepCollectionEquality().equals(
              shadowBlurRadius,
              other.shadowBlurRadius,
            ) &&
            const DeepCollectionEquality().equals(
              shadowOffsetY,
              other.shadowOffsetY,
            ));
  }

  @override
  int get hashCode {
    return Object.hashAll([
      runtimeType.hashCode,
      const DeepCollectionEquality().hash(backgroundColor),
      const DeepCollectionEquality().hash(borderColor),
      const DeepCollectionEquality().hash(borderWidth),
      const DeepCollectionEquality().hash(borderRadius),
      const DeepCollectionEquality().hash(titleTextStyle),
      const DeepCollectionEquality().hash(messageTextStyle),
      const DeepCollectionEquality().hash(messageMaxLines),
      const DeepCollectionEquality().hash(titleMessageSpacing),
      const DeepCollectionEquality().hash(infoColor),
      const DeepCollectionEquality().hash(successColor),
      const DeepCollectionEquality().hash(warningColor),
      const DeepCollectionEquality().hash(errorColor),
      const DeepCollectionEquality().hash(accentBarWidth),
      const DeepCollectionEquality().hash(iconSize),
      const DeepCollectionEquality().hash(iconSpacing),
      const DeepCollectionEquality().hash(closeIconColor),
      const DeepCollectionEquality().hash(closeIconSize),
      const DeepCollectionEquality().hash(padding),
      const DeepCollectionEquality().hash(width),
      const DeepCollectionEquality().hash(margin),
      const DeepCollectionEquality().hash(spacing),
      const DeepCollectionEquality().hash(maxVisible),
      const DeepCollectionEquality().hash(slideInDuration),
      const DeepCollectionEquality().hash(fadeOutDuration),
      const DeepCollectionEquality().hash(displayDuration),
      const DeepCollectionEquality().hash(slideOffsetX),
      const DeepCollectionEquality().hash(shadowColor),
      const DeepCollectionEquality().hash(shadowBlurRadius),
      const DeepCollectionEquality().hash(shadowOffsetY),
    ]);
  }
}

extension ToastThemeExtensionBuildContextProps on BuildContext {
  ToastThemeExtension get toastThemeExtension =>
      Theme.of(this).extension<ToastThemeExtension>()!;
  Color get backgroundColor => toastThemeExtension.backgroundColor;
  Color get borderColor => toastThemeExtension.borderColor;
  double get borderWidth => toastThemeExtension.borderWidth;
  double get borderRadius => toastThemeExtension.borderRadius;
  TextStyle? get titleTextStyle => toastThemeExtension.titleTextStyle;
  TextStyle? get messageTextStyle => toastThemeExtension.messageTextStyle;
  int get messageMaxLines => toastThemeExtension.messageMaxLines;
  double get titleMessageSpacing => toastThemeExtension.titleMessageSpacing;

  /// The stripe down the leading edge, and the icon, both take the severity colour.
  Color get infoColor => toastThemeExtension.infoColor;
  Color get successColor => toastThemeExtension.successColor;
  Color get warningColor => toastThemeExtension.warningColor;
  Color get errorColor => toastThemeExtension.errorColor;
  double get accentBarWidth => toastThemeExtension.accentBarWidth;
  double get iconSize => toastThemeExtension.iconSize;
  double get iconSpacing => toastThemeExtension.iconSpacing;
  Color get closeIconColor => toastThemeExtension.closeIconColor;
  double get closeIconSize => toastThemeExtension.closeIconSize;
  EdgeInsets get padding => toastThemeExtension.padding;
  double get width => toastThemeExtension.width;

  /// Distance from the corner of the window the stack is anchored to, and between two stacked
  /// toasts.
  EdgeInsets get margin => toastThemeExtension.margin;
  double get spacing => toastThemeExtension.spacing;

  /// How many toasts stay on screen at once; older ones are dropped as new ones arrive rather than
  /// growing a column that eventually covers the window.
  int get maxVisible => toastThemeExtension.maxVisible;
  Duration get slideInDuration => toastThemeExtension.slideInDuration;
  Duration get fadeOutDuration => toastThemeExtension.fadeOutDuration;

  /// Used when the caller passes no duration of its own.
  Duration get displayDuration => toastThemeExtension.displayDuration;
  double get slideOffsetX => toastThemeExtension.slideOffsetX;
  Color get shadowColor => toastThemeExtension.shadowColor;
  double get shadowBlurRadius => toastThemeExtension.shadowBlurRadius;
  double get shadowOffsetY => toastThemeExtension.shadowOffsetY;
}
