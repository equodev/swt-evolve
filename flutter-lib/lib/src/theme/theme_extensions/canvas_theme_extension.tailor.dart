// coverage:ignore-file
// GENERATED CODE - DO NOT MODIFY BY HAND
// ignore_for_file: type=lint
// ignore_for_file: unused_element, deprecated_member_use, deprecated_member_use_from_same_package, use_function_type_syntax_for_parameters, unnecessary_const, avoid_init_to_null, invalid_override_different_default_values_named, prefer_expression_function_bodies, annotate_overrides, invalid_annotation_target, unnecessary_question_mark

part of 'canvas_theme_extension.dart';

// **************************************************************************
// TailorAnnotationsGenerator
// **************************************************************************

mixin _$CanvasThemeExtensionTailorMixin
    on ThemeExtension<CanvasThemeExtension> {
  double get defaultWidth;
  double get defaultHeight;
  Color get backgroundColor;
  Color get foregroundColor;

  @override
  CanvasThemeExtension copyWith({
    double? defaultWidth,
    double? defaultHeight,
    Color? backgroundColor,
    Color? foregroundColor,
  }) {
    return CanvasThemeExtension(
      defaultWidth: defaultWidth ?? this.defaultWidth,
      defaultHeight: defaultHeight ?? this.defaultHeight,
      backgroundColor: backgroundColor ?? this.backgroundColor,
      foregroundColor: foregroundColor ?? this.foregroundColor,
    );
  }

  @override
  CanvasThemeExtension lerp(
    covariant ThemeExtension<CanvasThemeExtension>? other,
    double t,
  ) {
    if (other is! CanvasThemeExtension) return this as CanvasThemeExtension;
    return CanvasThemeExtension(
      defaultWidth: t < 0.5 ? defaultWidth : other.defaultWidth,
      defaultHeight: t < 0.5 ? defaultHeight : other.defaultHeight,
      backgroundColor: Color.lerp(backgroundColor, other.backgroundColor, t)!,
      foregroundColor: Color.lerp(foregroundColor, other.foregroundColor, t)!,
    );
  }

  @override
  bool operator ==(Object other) {
    return identical(this, other) ||
        (other.runtimeType == runtimeType &&
            other is CanvasThemeExtension &&
            const DeepCollectionEquality().equals(
              defaultWidth,
              other.defaultWidth,
            ) &&
            const DeepCollectionEquality().equals(
              defaultHeight,
              other.defaultHeight,
            ) &&
            const DeepCollectionEquality().equals(
              backgroundColor,
              other.backgroundColor,
            ) &&
            const DeepCollectionEquality().equals(
              foregroundColor,
              other.foregroundColor,
            ));
  }

  @override
  int get hashCode {
    return Object.hash(
      runtimeType.hashCode,
      const DeepCollectionEquality().hash(defaultWidth),
      const DeepCollectionEquality().hash(defaultHeight),
      const DeepCollectionEquality().hash(backgroundColor),
      const DeepCollectionEquality().hash(foregroundColor),
    );
  }
}

extension CanvasThemeExtensionBuildContextProps on BuildContext {
  CanvasThemeExtension get canvasThemeExtension =>
      Theme.of(this).extension<CanvasThemeExtension>()!;
  double get defaultWidth => canvasThemeExtension.defaultWidth;
  double get defaultHeight => canvasThemeExtension.defaultHeight;
  Color get backgroundColor => canvasThemeExtension.backgroundColor;
  Color get foregroundColor => canvasThemeExtension.foregroundColor;
}
