// coverage:ignore-file
// GENERATED CODE - DO NOT MODIFY BY HAND
// ignore_for_file: type=lint
// ignore_for_file: unused_element, deprecated_member_use, deprecated_member_use_from_same_package, use_function_type_syntax_for_parameters, unnecessary_const, avoid_init_to_null, invalid_override_different_default_values_named, prefer_expression_function_bodies, annotate_overrides, invalid_annotation_target, unnecessary_question_mark

part of 'tooltip_theme_extension.dart';

// **************************************************************************
// TailorAnnotationsGenerator
// **************************************************************************

mixin _$TooltipThemeExtensionTailorMixin
    on ThemeExtension<TooltipThemeExtension> {
  Duration get waitDuration;
  Duration get fadeInDuration;
  Duration get fadeOutDuration;
  double get slideOffsetY;
  Color get backgroundColor;
  Color get informationBackgroundColor;
  Color get warningBackgroundColor;
  Color get errorBackgroundColor;
  Color get borderColor;
  double get borderWidth;
  double get borderRadius;
  double get balloonBorderRadius;
  Color get textColor;
  TextStyle? get titleTextStyle;
  TextStyle? get messageTextStyle;
  int get messageMaxLines;
  double get titleMessageSpacing;
  Color get informationIconColor;
  Color get warningIconColor;
  Color get errorIconColor;
  double get iconSize;
  double get iconSpacing;
  EdgeInsets get padding;
  double get minWidth;
  double get maxWidth;
  double get minHeight;
  Color get shadowColor;
  double get shadowBlurRadius;
  double get shadowOffsetY;

  @override
  TooltipThemeExtension copyWith({
    Duration? waitDuration,
    Duration? fadeInDuration,
    Duration? fadeOutDuration,
    double? slideOffsetY,
    Color? backgroundColor,
    Color? informationBackgroundColor,
    Color? warningBackgroundColor,
    Color? errorBackgroundColor,
    Color? borderColor,
    double? borderWidth,
    double? borderRadius,
    double? balloonBorderRadius,
    Color? textColor,
    TextStyle? titleTextStyle,
    TextStyle? messageTextStyle,
    int? messageMaxLines,
    double? titleMessageSpacing,
    Color? informationIconColor,
    Color? warningIconColor,
    Color? errorIconColor,
    double? iconSize,
    double? iconSpacing,
    EdgeInsets? padding,
    double? minWidth,
    double? maxWidth,
    double? minHeight,
    Color? shadowColor,
    double? shadowBlurRadius,
    double? shadowOffsetY,
  }) {
    return TooltipThemeExtension(
      waitDuration: waitDuration ?? this.waitDuration,
      fadeInDuration: fadeInDuration ?? this.fadeInDuration,
      fadeOutDuration: fadeOutDuration ?? this.fadeOutDuration,
      slideOffsetY: slideOffsetY ?? this.slideOffsetY,
      backgroundColor: backgroundColor ?? this.backgroundColor,
      informationBackgroundColor:
          informationBackgroundColor ?? this.informationBackgroundColor,
      warningBackgroundColor:
          warningBackgroundColor ?? this.warningBackgroundColor,
      errorBackgroundColor: errorBackgroundColor ?? this.errorBackgroundColor,
      borderColor: borderColor ?? this.borderColor,
      borderWidth: borderWidth ?? this.borderWidth,
      borderRadius: borderRadius ?? this.borderRadius,
      balloonBorderRadius: balloonBorderRadius ?? this.balloonBorderRadius,
      textColor: textColor ?? this.textColor,
      titleTextStyle: titleTextStyle ?? this.titleTextStyle,
      messageTextStyle: messageTextStyle ?? this.messageTextStyle,
      messageMaxLines: messageMaxLines ?? this.messageMaxLines,
      titleMessageSpacing: titleMessageSpacing ?? this.titleMessageSpacing,
      informationIconColor: informationIconColor ?? this.informationIconColor,
      warningIconColor: warningIconColor ?? this.warningIconColor,
      errorIconColor: errorIconColor ?? this.errorIconColor,
      iconSize: iconSize ?? this.iconSize,
      iconSpacing: iconSpacing ?? this.iconSpacing,
      padding: padding ?? this.padding,
      minWidth: minWidth ?? this.minWidth,
      maxWidth: maxWidth ?? this.maxWidth,
      minHeight: minHeight ?? this.minHeight,
      shadowColor: shadowColor ?? this.shadowColor,
      shadowBlurRadius: shadowBlurRadius ?? this.shadowBlurRadius,
      shadowOffsetY: shadowOffsetY ?? this.shadowOffsetY,
    );
  }

  @override
  TooltipThemeExtension lerp(
    covariant ThemeExtension<TooltipThemeExtension>? other,
    double t,
  ) {
    if (other is! TooltipThemeExtension) return this as TooltipThemeExtension;
    return TooltipThemeExtension(
      waitDuration: t < 0.5 ? waitDuration : other.waitDuration,
      fadeInDuration: t < 0.5 ? fadeInDuration : other.fadeInDuration,
      fadeOutDuration: t < 0.5 ? fadeOutDuration : other.fadeOutDuration,
      slideOffsetY: t < 0.5 ? slideOffsetY : other.slideOffsetY,
      backgroundColor: Color.lerp(backgroundColor, other.backgroundColor, t)!,
      informationBackgroundColor: Color.lerp(
        informationBackgroundColor,
        other.informationBackgroundColor,
        t,
      )!,
      warningBackgroundColor: Color.lerp(
        warningBackgroundColor,
        other.warningBackgroundColor,
        t,
      )!,
      errorBackgroundColor: Color.lerp(
        errorBackgroundColor,
        other.errorBackgroundColor,
        t,
      )!,
      borderColor: Color.lerp(borderColor, other.borderColor, t)!,
      borderWidth: t < 0.5 ? borderWidth : other.borderWidth,
      borderRadius: t < 0.5 ? borderRadius : other.borderRadius,
      balloonBorderRadius: t < 0.5
          ? balloonBorderRadius
          : other.balloonBorderRadius,
      textColor: Color.lerp(textColor, other.textColor, t)!,
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
      informationIconColor: Color.lerp(
        informationIconColor,
        other.informationIconColor,
        t,
      )!,
      warningIconColor: Color.lerp(
        warningIconColor,
        other.warningIconColor,
        t,
      )!,
      errorIconColor: Color.lerp(errorIconColor, other.errorIconColor, t)!,
      iconSize: t < 0.5 ? iconSize : other.iconSize,
      iconSpacing: t < 0.5 ? iconSpacing : other.iconSpacing,
      padding: t < 0.5 ? padding : other.padding,
      minWidth: t < 0.5 ? minWidth : other.minWidth,
      maxWidth: t < 0.5 ? maxWidth : other.maxWidth,
      minHeight: t < 0.5 ? minHeight : other.minHeight,
      shadowColor: Color.lerp(shadowColor, other.shadowColor, t)!,
      shadowBlurRadius: t < 0.5 ? shadowBlurRadius : other.shadowBlurRadius,
      shadowOffsetY: t < 0.5 ? shadowOffsetY : other.shadowOffsetY,
    );
  }

  @override
  bool operator ==(Object other) {
    return identical(this, other) ||
        (other.runtimeType == runtimeType &&
            other is TooltipThemeExtension &&
            const DeepCollectionEquality().equals(
              waitDuration,
              other.waitDuration,
            ) &&
            const DeepCollectionEquality().equals(
              fadeInDuration,
              other.fadeInDuration,
            ) &&
            const DeepCollectionEquality().equals(
              fadeOutDuration,
              other.fadeOutDuration,
            ) &&
            const DeepCollectionEquality().equals(
              slideOffsetY,
              other.slideOffsetY,
            ) &&
            const DeepCollectionEquality().equals(
              backgroundColor,
              other.backgroundColor,
            ) &&
            const DeepCollectionEquality().equals(
              informationBackgroundColor,
              other.informationBackgroundColor,
            ) &&
            const DeepCollectionEquality().equals(
              warningBackgroundColor,
              other.warningBackgroundColor,
            ) &&
            const DeepCollectionEquality().equals(
              errorBackgroundColor,
              other.errorBackgroundColor,
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
              balloonBorderRadius,
              other.balloonBorderRadius,
            ) &&
            const DeepCollectionEquality().equals(textColor, other.textColor) &&
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
            const DeepCollectionEquality().equals(
              informationIconColor,
              other.informationIconColor,
            ) &&
            const DeepCollectionEquality().equals(
              warningIconColor,
              other.warningIconColor,
            ) &&
            const DeepCollectionEquality().equals(
              errorIconColor,
              other.errorIconColor,
            ) &&
            const DeepCollectionEquality().equals(iconSize, other.iconSize) &&
            const DeepCollectionEquality().equals(
              iconSpacing,
              other.iconSpacing,
            ) &&
            const DeepCollectionEquality().equals(padding, other.padding) &&
            const DeepCollectionEquality().equals(minWidth, other.minWidth) &&
            const DeepCollectionEquality().equals(maxWidth, other.maxWidth) &&
            const DeepCollectionEquality().equals(minHeight, other.minHeight) &&
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
      const DeepCollectionEquality().hash(waitDuration),
      const DeepCollectionEquality().hash(fadeInDuration),
      const DeepCollectionEquality().hash(fadeOutDuration),
      const DeepCollectionEquality().hash(slideOffsetY),
      const DeepCollectionEquality().hash(backgroundColor),
      const DeepCollectionEquality().hash(informationBackgroundColor),
      const DeepCollectionEquality().hash(warningBackgroundColor),
      const DeepCollectionEquality().hash(errorBackgroundColor),
      const DeepCollectionEquality().hash(borderColor),
      const DeepCollectionEquality().hash(borderWidth),
      const DeepCollectionEquality().hash(borderRadius),
      const DeepCollectionEquality().hash(balloonBorderRadius),
      const DeepCollectionEquality().hash(textColor),
      const DeepCollectionEquality().hash(titleTextStyle),
      const DeepCollectionEquality().hash(messageTextStyle),
      const DeepCollectionEquality().hash(messageMaxLines),
      const DeepCollectionEquality().hash(titleMessageSpacing),
      const DeepCollectionEquality().hash(informationIconColor),
      const DeepCollectionEquality().hash(warningIconColor),
      const DeepCollectionEquality().hash(errorIconColor),
      const DeepCollectionEquality().hash(iconSize),
      const DeepCollectionEquality().hash(iconSpacing),
      const DeepCollectionEquality().hash(padding),
      const DeepCollectionEquality().hash(minWidth),
      const DeepCollectionEquality().hash(maxWidth),
      const DeepCollectionEquality().hash(minHeight),
      const DeepCollectionEquality().hash(shadowColor),
      const DeepCollectionEquality().hash(shadowBlurRadius),
      const DeepCollectionEquality().hash(shadowOffsetY),
    ]);
  }
}

extension TooltipThemeExtensionBuildContextProps on BuildContext {
  TooltipThemeExtension get tooltipThemeExtension =>
      Theme.of(this).extension<TooltipThemeExtension>()!;
  Duration get waitDuration => tooltipThemeExtension.waitDuration;
  Duration get fadeInDuration => tooltipThemeExtension.fadeInDuration;
  Duration get fadeOutDuration => tooltipThemeExtension.fadeOutDuration;
  double get slideOffsetY => tooltipThemeExtension.slideOffsetY;
  Color get backgroundColor => tooltipThemeExtension.backgroundColor;
  Color get informationBackgroundColor =>
      tooltipThemeExtension.informationBackgroundColor;
  Color get warningBackgroundColor =>
      tooltipThemeExtension.warningBackgroundColor;
  Color get errorBackgroundColor => tooltipThemeExtension.errorBackgroundColor;
  Color get borderColor => tooltipThemeExtension.borderColor;
  double get borderWidth => tooltipThemeExtension.borderWidth;
  double get borderRadius => tooltipThemeExtension.borderRadius;
  double get balloonBorderRadius => tooltipThemeExtension.balloonBorderRadius;
  Color get textColor => tooltipThemeExtension.textColor;
  TextStyle? get titleTextStyle => tooltipThemeExtension.titleTextStyle;
  TextStyle? get messageTextStyle => tooltipThemeExtension.messageTextStyle;
  int get messageMaxLines => tooltipThemeExtension.messageMaxLines;
  double get titleMessageSpacing => tooltipThemeExtension.titleMessageSpacing;
  Color get informationIconColor => tooltipThemeExtension.informationIconColor;
  Color get warningIconColor => tooltipThemeExtension.warningIconColor;
  Color get errorIconColor => tooltipThemeExtension.errorIconColor;
  double get iconSize => tooltipThemeExtension.iconSize;
  double get iconSpacing => tooltipThemeExtension.iconSpacing;
  EdgeInsets get padding => tooltipThemeExtension.padding;
  double get minWidth => tooltipThemeExtension.minWidth;
  double get maxWidth => tooltipThemeExtension.maxWidth;
  double get minHeight => tooltipThemeExtension.minHeight;
  Color get shadowColor => tooltipThemeExtension.shadowColor;
  double get shadowBlurRadius => tooltipThemeExtension.shadowBlurRadius;
  double get shadowOffsetY => tooltipThemeExtension.shadowOffsetY;
}
