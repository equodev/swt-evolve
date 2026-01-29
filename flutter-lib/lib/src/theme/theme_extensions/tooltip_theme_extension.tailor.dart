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
  Color get backgroundColor;
  double get borderRadius;
  TextStyle? get messageTextStyle;
  Duration get waitDuration;

  @override
  TooltipThemeExtension copyWith({
    Color? backgroundColor,
    double? borderRadius,
    TextStyle? messageTextStyle,
    Duration? waitDuration,
  }) {
    return TooltipThemeExtension(
      backgroundColor: backgroundColor ?? this.backgroundColor,
      borderRadius: borderRadius ?? this.borderRadius,
      messageTextStyle: messageTextStyle ?? this.messageTextStyle,
      waitDuration: waitDuration ?? this.waitDuration,
    );
  }

  @override
  TooltipThemeExtension lerp(
    covariant ThemeExtension<TooltipThemeExtension>? other,
    double t,
  ) {
    if (other is! TooltipThemeExtension) return this as TooltipThemeExtension;
    return TooltipThemeExtension(
      backgroundColor: Color.lerp(backgroundColor, other.backgroundColor, t)!,
      borderRadius: t < 0.5 ? borderRadius : other.borderRadius,
      messageTextStyle: TextStyle.lerp(
        messageTextStyle,
        other.messageTextStyle,
        t,
      ),
      waitDuration: t < 0.5 ? waitDuration : other.waitDuration,
    );
  }

  @override
  bool operator ==(Object other) {
    return identical(this, other) ||
        (other.runtimeType == runtimeType &&
            other is TooltipThemeExtension &&
            const DeepCollectionEquality().equals(
              backgroundColor,
              other.backgroundColor,
            ) &&
            const DeepCollectionEquality().equals(
              borderRadius,
              other.borderRadius,
            ) &&
            const DeepCollectionEquality().equals(
              messageTextStyle,
              other.messageTextStyle,
            ) &&
            const DeepCollectionEquality().equals(
              waitDuration,
              other.waitDuration,
            ));
  }

  @override
  int get hashCode {
    return Object.hash(
      runtimeType.hashCode,
      const DeepCollectionEquality().hash(backgroundColor),
      const DeepCollectionEquality().hash(borderRadius),
      const DeepCollectionEquality().hash(messageTextStyle),
      const DeepCollectionEquality().hash(waitDuration),
    );
  }
}

extension TooltipThemeExtensionBuildContextProps on BuildContext {
  TooltipThemeExtension get tooltipThemeExtension =>
      Theme.of(this).extension<TooltipThemeExtension>()!;
  Color get backgroundColor => tooltipThemeExtension.backgroundColor;
  double get borderRadius => tooltipThemeExtension.borderRadius;
  TextStyle? get messageTextStyle => tooltipThemeExtension.messageTextStyle;
  Duration get waitDuration => tooltipThemeExtension.waitDuration;
}
