// coverage:ignore-file
// GENERATED CODE - DO NOT MODIFY BY HAND
// ignore_for_file: type=lint
// ignore_for_file: unused_element, deprecated_member_use, deprecated_member_use_from_same_package, use_function_type_syntax_for_parameters, unnecessary_const, avoid_init_to_null, invalid_override_different_default_values_named, prefer_expression_function_bodies, annotate_overrides, invalid_annotation_target, unnecessary_question_mark

part of 'scrolledcomposite_theme_extension.dart';

// **************************************************************************
// TailorAnnotationsGenerator
// **************************************************************************

mixin _$ScrolledCompositeThemeExtensionTailorMixin
    on ThemeExtension<ScrolledCompositeThemeExtension> {
  Duration get animationDuration;
  Color get backgroundColor;
  Color get disabledBackgroundColor;
  Color get scrollbarThumbColor;
  Color get scrollbarThumbHoverColor;
  Color get scrollbarTrackColor;
  Color get borderColor;
  Color get focusedBorderColor;
  double get borderWidth;
  double get borderRadius;
  double get scrollbarThickness;
  double get scrollbarRadius;
  double get scrollbarMinThumbLength;
  double get defaultMinWidth;
  double get defaultMinHeight;
  double get contentPadding;

  @override
  ScrolledCompositeThemeExtension copyWith({
    Duration? animationDuration,
    Color? backgroundColor,
    Color? disabledBackgroundColor,
    Color? scrollbarThumbColor,
    Color? scrollbarThumbHoverColor,
    Color? scrollbarTrackColor,
    Color? borderColor,
    Color? focusedBorderColor,
    double? borderWidth,
    double? borderRadius,
    double? scrollbarThickness,
    double? scrollbarRadius,
    double? scrollbarMinThumbLength,
    double? defaultMinWidth,
    double? defaultMinHeight,
    double? contentPadding,
  }) {
    return ScrolledCompositeThemeExtension(
      animationDuration: animationDuration ?? this.animationDuration,
      backgroundColor: backgroundColor ?? this.backgroundColor,
      disabledBackgroundColor:
          disabledBackgroundColor ?? this.disabledBackgroundColor,
      scrollbarThumbColor: scrollbarThumbColor ?? this.scrollbarThumbColor,
      scrollbarThumbHoverColor:
          scrollbarThumbHoverColor ?? this.scrollbarThumbHoverColor,
      scrollbarTrackColor: scrollbarTrackColor ?? this.scrollbarTrackColor,
      borderColor: borderColor ?? this.borderColor,
      focusedBorderColor: focusedBorderColor ?? this.focusedBorderColor,
      borderWidth: borderWidth ?? this.borderWidth,
      borderRadius: borderRadius ?? this.borderRadius,
      scrollbarThickness: scrollbarThickness ?? this.scrollbarThickness,
      scrollbarRadius: scrollbarRadius ?? this.scrollbarRadius,
      scrollbarMinThumbLength:
          scrollbarMinThumbLength ?? this.scrollbarMinThumbLength,
      defaultMinWidth: defaultMinWidth ?? this.defaultMinWidth,
      defaultMinHeight: defaultMinHeight ?? this.defaultMinHeight,
      contentPadding: contentPadding ?? this.contentPadding,
    );
  }

  @override
  ScrolledCompositeThemeExtension lerp(
    covariant ThemeExtension<ScrolledCompositeThemeExtension>? other,
    double t,
  ) {
    if (other is! ScrolledCompositeThemeExtension)
      return this as ScrolledCompositeThemeExtension;
    return ScrolledCompositeThemeExtension(
      animationDuration: t < 0.5 ? animationDuration : other.animationDuration,
      backgroundColor: Color.lerp(backgroundColor, other.backgroundColor, t)!,
      disabledBackgroundColor: Color.lerp(
        disabledBackgroundColor,
        other.disabledBackgroundColor,
        t,
      )!,
      scrollbarThumbColor: Color.lerp(
        scrollbarThumbColor,
        other.scrollbarThumbColor,
        t,
      )!,
      scrollbarThumbHoverColor: Color.lerp(
        scrollbarThumbHoverColor,
        other.scrollbarThumbHoverColor,
        t,
      )!,
      scrollbarTrackColor: Color.lerp(
        scrollbarTrackColor,
        other.scrollbarTrackColor,
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
      scrollbarThickness: t < 0.5
          ? scrollbarThickness
          : other.scrollbarThickness,
      scrollbarRadius: t < 0.5 ? scrollbarRadius : other.scrollbarRadius,
      scrollbarMinThumbLength: t < 0.5
          ? scrollbarMinThumbLength
          : other.scrollbarMinThumbLength,
      defaultMinWidth: t < 0.5 ? defaultMinWidth : other.defaultMinWidth,
      defaultMinHeight: t < 0.5 ? defaultMinHeight : other.defaultMinHeight,
      contentPadding: t < 0.5 ? contentPadding : other.contentPadding,
    );
  }

  @override
  bool operator ==(Object other) {
    return identical(this, other) ||
        (other.runtimeType == runtimeType &&
            other is ScrolledCompositeThemeExtension &&
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
              scrollbarThumbColor,
              other.scrollbarThumbColor,
            ) &&
            const DeepCollectionEquality().equals(
              scrollbarThumbHoverColor,
              other.scrollbarThumbHoverColor,
            ) &&
            const DeepCollectionEquality().equals(
              scrollbarTrackColor,
              other.scrollbarTrackColor,
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
              scrollbarThickness,
              other.scrollbarThickness,
            ) &&
            const DeepCollectionEquality().equals(
              scrollbarRadius,
              other.scrollbarRadius,
            ) &&
            const DeepCollectionEquality().equals(
              scrollbarMinThumbLength,
              other.scrollbarMinThumbLength,
            ) &&
            const DeepCollectionEquality().equals(
              defaultMinWidth,
              other.defaultMinWidth,
            ) &&
            const DeepCollectionEquality().equals(
              defaultMinHeight,
              other.defaultMinHeight,
            ) &&
            const DeepCollectionEquality().equals(
              contentPadding,
              other.contentPadding,
            ));
  }

  @override
  int get hashCode {
    return Object.hash(
      runtimeType.hashCode,
      const DeepCollectionEquality().hash(animationDuration),
      const DeepCollectionEquality().hash(backgroundColor),
      const DeepCollectionEquality().hash(disabledBackgroundColor),
      const DeepCollectionEquality().hash(scrollbarThumbColor),
      const DeepCollectionEquality().hash(scrollbarThumbHoverColor),
      const DeepCollectionEquality().hash(scrollbarTrackColor),
      const DeepCollectionEquality().hash(borderColor),
      const DeepCollectionEquality().hash(focusedBorderColor),
      const DeepCollectionEquality().hash(borderWidth),
      const DeepCollectionEquality().hash(borderRadius),
      const DeepCollectionEquality().hash(scrollbarThickness),
      const DeepCollectionEquality().hash(scrollbarRadius),
      const DeepCollectionEquality().hash(scrollbarMinThumbLength),
      const DeepCollectionEquality().hash(defaultMinWidth),
      const DeepCollectionEquality().hash(defaultMinHeight),
      const DeepCollectionEquality().hash(contentPadding),
    );
  }
}

extension ScrolledCompositeThemeExtensionBuildContextProps on BuildContext {
  ScrolledCompositeThemeExtension get scrolledCompositeThemeExtension =>
      Theme.of(this).extension<ScrolledCompositeThemeExtension>()!;
  Duration get animationDuration =>
      scrolledCompositeThemeExtension.animationDuration;
  Color get backgroundColor => scrolledCompositeThemeExtension.backgroundColor;
  Color get disabledBackgroundColor =>
      scrolledCompositeThemeExtension.disabledBackgroundColor;
  Color get scrollbarThumbColor =>
      scrolledCompositeThemeExtension.scrollbarThumbColor;
  Color get scrollbarThumbHoverColor =>
      scrolledCompositeThemeExtension.scrollbarThumbHoverColor;
  Color get scrollbarTrackColor =>
      scrolledCompositeThemeExtension.scrollbarTrackColor;
  Color get borderColor => scrolledCompositeThemeExtension.borderColor;
  Color get focusedBorderColor =>
      scrolledCompositeThemeExtension.focusedBorderColor;
  double get borderWidth => scrolledCompositeThemeExtension.borderWidth;
  double get borderRadius => scrolledCompositeThemeExtension.borderRadius;
  double get scrollbarThickness =>
      scrolledCompositeThemeExtension.scrollbarThickness;
  double get scrollbarRadius => scrolledCompositeThemeExtension.scrollbarRadius;
  double get scrollbarMinThumbLength =>
      scrolledCompositeThemeExtension.scrollbarMinThumbLength;
  double get defaultMinWidth => scrolledCompositeThemeExtension.defaultMinWidth;
  double get defaultMinHeight =>
      scrolledCompositeThemeExtension.defaultMinHeight;
  double get contentPadding => scrolledCompositeThemeExtension.contentPadding;
}
