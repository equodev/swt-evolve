// coverage:ignore-file
// GENERATED CODE - DO NOT MODIFY BY HAND
// ignore_for_file: type=lint
// ignore_for_file: unused_element, deprecated_member_use, deprecated_member_use_from_same_package, use_function_type_syntax_for_parameters, unnecessary_const, avoid_init_to_null, invalid_override_different_default_values_named, prefer_expression_function_bodies, annotate_overrides, invalid_annotation_target, unnecessary_question_mark

part of 'message_box_theme_extension.dart';

// **************************************************************************
// TailorAnnotationsGenerator
// **************************************************************************

mixin _$MessageBoxThemeExtensionTailorMixin
    on ThemeExtension<MessageBoxThemeExtension> {
  TextStyle? get titleStyle;
  TextStyle? get messageStyle;
  Color get backgroundColor;
  Color get iconErrorColor;
  Color get iconWarningColor;
  Color get iconInfoColor;
  Color get iconQuestionColor;
  double get borderRadius;
  double get minWidth;
  double get maxWidth;
  EdgeInsets get padding;
  double get iconTitleSpacing;
  double get titleMessageSpacing;
  double get messageIndent;
  double get contentButtonsSpacing;
  double get buttonSpacing;

  @override
  MessageBoxThemeExtension copyWith({
    TextStyle? titleStyle,
    TextStyle? messageStyle,
    Color? backgroundColor,
    Color? iconErrorColor,
    Color? iconWarningColor,
    Color? iconInfoColor,
    Color? iconQuestionColor,
    double? borderRadius,
    double? minWidth,
    double? maxWidth,
    EdgeInsets? padding,
    double? iconTitleSpacing,
    double? titleMessageSpacing,
    double? messageIndent,
    double? contentButtonsSpacing,
    double? buttonSpacing,
  }) {
    return MessageBoxThemeExtension(
      titleStyle: titleStyle ?? this.titleStyle,
      messageStyle: messageStyle ?? this.messageStyle,
      backgroundColor: backgroundColor ?? this.backgroundColor,
      iconErrorColor: iconErrorColor ?? this.iconErrorColor,
      iconWarningColor: iconWarningColor ?? this.iconWarningColor,
      iconInfoColor: iconInfoColor ?? this.iconInfoColor,
      iconQuestionColor: iconQuestionColor ?? this.iconQuestionColor,
      borderRadius: borderRadius ?? this.borderRadius,
      minWidth: minWidth ?? this.minWidth,
      maxWidth: maxWidth ?? this.maxWidth,
      padding: padding ?? this.padding,
      iconTitleSpacing: iconTitleSpacing ?? this.iconTitleSpacing,
      titleMessageSpacing: titleMessageSpacing ?? this.titleMessageSpacing,
      messageIndent: messageIndent ?? this.messageIndent,
      contentButtonsSpacing:
          contentButtonsSpacing ?? this.contentButtonsSpacing,
      buttonSpacing: buttonSpacing ?? this.buttonSpacing,
    );
  }

  @override
  MessageBoxThemeExtension lerp(
    covariant ThemeExtension<MessageBoxThemeExtension>? other,
    double t,
  ) {
    if (other is! MessageBoxThemeExtension)
      return this as MessageBoxThemeExtension;
    return MessageBoxThemeExtension(
      titleStyle: TextStyle.lerp(titleStyle, other.titleStyle, t),
      messageStyle: TextStyle.lerp(messageStyle, other.messageStyle, t),
      backgroundColor: Color.lerp(backgroundColor, other.backgroundColor, t)!,
      iconErrorColor: Color.lerp(iconErrorColor, other.iconErrorColor, t)!,
      iconWarningColor: Color.lerp(
        iconWarningColor,
        other.iconWarningColor,
        t,
      )!,
      iconInfoColor: Color.lerp(iconInfoColor, other.iconInfoColor, t)!,
      iconQuestionColor: Color.lerp(
        iconQuestionColor,
        other.iconQuestionColor,
        t,
      )!,
      borderRadius: t < 0.5 ? borderRadius : other.borderRadius,
      minWidth: t < 0.5 ? minWidth : other.minWidth,
      maxWidth: t < 0.5 ? maxWidth : other.maxWidth,
      padding: t < 0.5 ? padding : other.padding,
      iconTitleSpacing: t < 0.5 ? iconTitleSpacing : other.iconTitleSpacing,
      titleMessageSpacing: t < 0.5
          ? titleMessageSpacing
          : other.titleMessageSpacing,
      messageIndent: t < 0.5 ? messageIndent : other.messageIndent,
      contentButtonsSpacing: t < 0.5
          ? contentButtonsSpacing
          : other.contentButtonsSpacing,
      buttonSpacing: t < 0.5 ? buttonSpacing : other.buttonSpacing,
    );
  }

  @override
  bool operator ==(Object other) {
    return identical(this, other) ||
        (other.runtimeType == runtimeType &&
            other is MessageBoxThemeExtension &&
            const DeepCollectionEquality().equals(
              titleStyle,
              other.titleStyle,
            ) &&
            const DeepCollectionEquality().equals(
              messageStyle,
              other.messageStyle,
            ) &&
            const DeepCollectionEquality().equals(
              backgroundColor,
              other.backgroundColor,
            ) &&
            const DeepCollectionEquality().equals(
              iconErrorColor,
              other.iconErrorColor,
            ) &&
            const DeepCollectionEquality().equals(
              iconWarningColor,
              other.iconWarningColor,
            ) &&
            const DeepCollectionEquality().equals(
              iconInfoColor,
              other.iconInfoColor,
            ) &&
            const DeepCollectionEquality().equals(
              iconQuestionColor,
              other.iconQuestionColor,
            ) &&
            const DeepCollectionEquality().equals(
              borderRadius,
              other.borderRadius,
            ) &&
            const DeepCollectionEquality().equals(minWidth, other.minWidth) &&
            const DeepCollectionEquality().equals(maxWidth, other.maxWidth) &&
            const DeepCollectionEquality().equals(padding, other.padding) &&
            const DeepCollectionEquality().equals(
              iconTitleSpacing,
              other.iconTitleSpacing,
            ) &&
            const DeepCollectionEquality().equals(
              titleMessageSpacing,
              other.titleMessageSpacing,
            ) &&
            const DeepCollectionEquality().equals(
              messageIndent,
              other.messageIndent,
            ) &&
            const DeepCollectionEquality().equals(
              contentButtonsSpacing,
              other.contentButtonsSpacing,
            ) &&
            const DeepCollectionEquality().equals(
              buttonSpacing,
              other.buttonSpacing,
            ));
  }

  @override
  int get hashCode {
    return Object.hash(
      runtimeType.hashCode,
      const DeepCollectionEquality().hash(titleStyle),
      const DeepCollectionEquality().hash(messageStyle),
      const DeepCollectionEquality().hash(backgroundColor),
      const DeepCollectionEquality().hash(iconErrorColor),
      const DeepCollectionEquality().hash(iconWarningColor),
      const DeepCollectionEquality().hash(iconInfoColor),
      const DeepCollectionEquality().hash(iconQuestionColor),
      const DeepCollectionEquality().hash(borderRadius),
      const DeepCollectionEquality().hash(minWidth),
      const DeepCollectionEquality().hash(maxWidth),
      const DeepCollectionEquality().hash(padding),
      const DeepCollectionEquality().hash(iconTitleSpacing),
      const DeepCollectionEquality().hash(titleMessageSpacing),
      const DeepCollectionEquality().hash(messageIndent),
      const DeepCollectionEquality().hash(contentButtonsSpacing),
      const DeepCollectionEquality().hash(buttonSpacing),
    );
  }
}

extension MessageBoxThemeExtensionBuildContextProps on BuildContext {
  MessageBoxThemeExtension get messageBoxThemeExtension =>
      Theme.of(this).extension<MessageBoxThemeExtension>()!;
  TextStyle? get titleStyle => messageBoxThemeExtension.titleStyle;
  TextStyle? get messageStyle => messageBoxThemeExtension.messageStyle;
  Color get backgroundColor => messageBoxThemeExtension.backgroundColor;
  Color get iconErrorColor => messageBoxThemeExtension.iconErrorColor;
  Color get iconWarningColor => messageBoxThemeExtension.iconWarningColor;
  Color get iconInfoColor => messageBoxThemeExtension.iconInfoColor;
  Color get iconQuestionColor => messageBoxThemeExtension.iconQuestionColor;
  double get borderRadius => messageBoxThemeExtension.borderRadius;
  double get minWidth => messageBoxThemeExtension.minWidth;
  double get maxWidth => messageBoxThemeExtension.maxWidth;
  EdgeInsets get padding => messageBoxThemeExtension.padding;
  double get iconTitleSpacing => messageBoxThemeExtension.iconTitleSpacing;
  double get titleMessageSpacing =>
      messageBoxThemeExtension.titleMessageSpacing;
  double get messageIndent => messageBoxThemeExtension.messageIndent;
  double get contentButtonsSpacing =>
      messageBoxThemeExtension.contentButtonsSpacing;
  double get buttonSpacing => messageBoxThemeExtension.buttonSpacing;
}
