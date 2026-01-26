// coverage:ignore-file
// GENERATED CODE - DO NOT MODIFY BY HAND
// ignore_for_file: type=lint
// ignore_for_file: unused_element, deprecated_member_use, deprecated_member_use_from_same_package, use_function_type_syntax_for_parameters, unnecessary_const, avoid_init_to_null, invalid_override_different_default_values_named, prefer_expression_function_bodies, annotate_overrides, invalid_annotation_target, unnecessary_question_mark

part of 'group_theme_extension.dart';

// **************************************************************************
// TailorAnnotationsGenerator
// **************************************************************************

mixin _$GroupThemeExtensionTailorMixin on ThemeExtension<GroupThemeExtension> {
  Color get backgroundColor;
  Color get borderColor;
  EdgeInsets get padding;
  EdgeInsets get margin;
  double get borderWidth;
  double get borderRadius;
  Color get foregroundColor;
  TextStyle? get textStyle;

  @override
  GroupThemeExtension copyWith({
    Color? backgroundColor,
    Color? borderColor,
    EdgeInsets? padding,
    EdgeInsets? margin,
    double? borderWidth,
    double? borderRadius,
    Color? foregroundColor,
    TextStyle? textStyle,
  }) {
    return GroupThemeExtension(
      backgroundColor: backgroundColor ?? this.backgroundColor,
      borderColor: borderColor ?? this.borderColor,
      padding: padding ?? this.padding,
      margin: margin ?? this.margin,
      borderWidth: borderWidth ?? this.borderWidth,
      borderRadius: borderRadius ?? this.borderRadius,
      foregroundColor: foregroundColor ?? this.foregroundColor,
      textStyle: textStyle ?? this.textStyle,
    );
  }

  @override
  GroupThemeExtension lerp(
    covariant ThemeExtension<GroupThemeExtension>? other,
    double t,
  ) {
    if (other is! GroupThemeExtension) return this as GroupThemeExtension;
    return GroupThemeExtension(
      backgroundColor: Color.lerp(backgroundColor, other.backgroundColor, t)!,
      borderColor: Color.lerp(borderColor, other.borderColor, t)!,
      padding: t < 0.5 ? padding : other.padding,
      margin: t < 0.5 ? margin : other.margin,
      borderWidth: t < 0.5 ? borderWidth : other.borderWidth,
      borderRadius: t < 0.5 ? borderRadius : other.borderRadius,
      foregroundColor: Color.lerp(foregroundColor, other.foregroundColor, t)!,
      textStyle: TextStyle.lerp(textStyle, other.textStyle, t),
    );
  }

  @override
  bool operator ==(Object other) {
    return identical(this, other) ||
        (other.runtimeType == runtimeType &&
            other is GroupThemeExtension &&
            const DeepCollectionEquality().equals(
              backgroundColor,
              other.backgroundColor,
            ) &&
            const DeepCollectionEquality().equals(
              borderColor,
              other.borderColor,
            ) &&
            const DeepCollectionEquality().equals(padding, other.padding) &&
            const DeepCollectionEquality().equals(margin, other.margin) &&
            const DeepCollectionEquality().equals(
              borderWidth,
              other.borderWidth,
            ) &&
            const DeepCollectionEquality().equals(
              borderRadius,
              other.borderRadius,
            ) &&
            const DeepCollectionEquality().equals(
              foregroundColor,
              other.foregroundColor,
            ) &&
            const DeepCollectionEquality().equals(textStyle, other.textStyle));
  }

  @override
  int get hashCode {
    return Object.hash(
      runtimeType.hashCode,
      const DeepCollectionEquality().hash(backgroundColor),
      const DeepCollectionEquality().hash(borderColor),
      const DeepCollectionEquality().hash(padding),
      const DeepCollectionEquality().hash(margin),
      const DeepCollectionEquality().hash(borderWidth),
      const DeepCollectionEquality().hash(borderRadius),
      const DeepCollectionEquality().hash(foregroundColor),
      const DeepCollectionEquality().hash(textStyle),
    );
  }
}

extension GroupThemeExtensionBuildContextProps on BuildContext {
  GroupThemeExtension get groupThemeExtension =>
      Theme.of(this).extension<GroupThemeExtension>()!;
  Color get backgroundColor => groupThemeExtension.backgroundColor;
  Color get borderColor => groupThemeExtension.borderColor;
  EdgeInsets get padding => groupThemeExtension.padding;
  EdgeInsets get margin => groupThemeExtension.margin;
  double get borderWidth => groupThemeExtension.borderWidth;
  double get borderRadius => groupThemeExtension.borderRadius;
  Color get foregroundColor => groupThemeExtension.foregroundColor;
  TextStyle? get textStyle => groupThemeExtension.textStyle;
}
