// coverage:ignore-file
// GENERATED CODE - DO NOT MODIFY BY HAND
// ignore_for_file: type=lint
// ignore_for_file: unused_element, deprecated_member_use, deprecated_member_use_from_same_package, use_function_type_syntax_for_parameters, unnecessary_const, avoid_init_to_null, invalid_override_different_default_values_named, prefer_expression_function_bodies, annotate_overrides, invalid_annotation_target, unnecessary_question_mark

part of 'toolbar_theme_extension.dart';

// **************************************************************************
// TailorAnnotationsGenerator
// **************************************************************************

mixin _$ToolBarThemeExtensionTailorMixin
    on ThemeExtension<ToolBarThemeExtension> {
  Color get backgroundColor;
  Color get borderColor;
  double get borderWidth;
  Color get shadowColor;
  double get shadowOpacity;
  double get shadowBlurRadius;
  Offset get shadowOffset;
  EdgeInsets get itemPadding;
  Color get compositeBackgroundColor;
  Color get toolbarBackgroundColor;
  double get keywordLeftOffset;
  double get dividerVerticalPadding;
  double get separatorThickness;
  double get separatorWidth;
  double get separatorHeight;

  @override
  ToolBarThemeExtension copyWith({
    Color? backgroundColor,
    Color? borderColor,
    double? borderWidth,
    Color? shadowColor,
    double? shadowOpacity,
    double? shadowBlurRadius,
    Offset? shadowOffset,
    EdgeInsets? itemPadding,
    Color? compositeBackgroundColor,
    Color? toolbarBackgroundColor,
    double? keywordLeftOffset,
    double? dividerVerticalPadding,
    double? separatorThickness,
    double? separatorWidth,
    double? separatorHeight,
  }) {
    return ToolBarThemeExtension(
      backgroundColor: backgroundColor ?? this.backgroundColor,
      borderColor: borderColor ?? this.borderColor,
      borderWidth: borderWidth ?? this.borderWidth,
      shadowColor: shadowColor ?? this.shadowColor,
      shadowOpacity: shadowOpacity ?? this.shadowOpacity,
      shadowBlurRadius: shadowBlurRadius ?? this.shadowBlurRadius,
      shadowOffset: shadowOffset ?? this.shadowOffset,
      itemPadding: itemPadding ?? this.itemPadding,
      compositeBackgroundColor:
          compositeBackgroundColor ?? this.compositeBackgroundColor,
      toolbarBackgroundColor:
          toolbarBackgroundColor ?? this.toolbarBackgroundColor,
      keywordLeftOffset: keywordLeftOffset ?? this.keywordLeftOffset,
      dividerVerticalPadding:
          dividerVerticalPadding ?? this.dividerVerticalPadding,
      separatorThickness: separatorThickness ?? this.separatorThickness,
      separatorWidth: separatorWidth ?? this.separatorWidth,
      separatorHeight: separatorHeight ?? this.separatorHeight,
    );
  }

  @override
  ToolBarThemeExtension lerp(
    covariant ThemeExtension<ToolBarThemeExtension>? other,
    double t,
  ) {
    if (other is! ToolBarThemeExtension) return this as ToolBarThemeExtension;
    return ToolBarThemeExtension(
      backgroundColor: Color.lerp(backgroundColor, other.backgroundColor, t)!,
      borderColor: Color.lerp(borderColor, other.borderColor, t)!,
      borderWidth: t < 0.5 ? borderWidth : other.borderWidth,
      shadowColor: Color.lerp(shadowColor, other.shadowColor, t)!,
      shadowOpacity: t < 0.5 ? shadowOpacity : other.shadowOpacity,
      shadowBlurRadius: t < 0.5 ? shadowBlurRadius : other.shadowBlurRadius,
      shadowOffset: t < 0.5 ? shadowOffset : other.shadowOffset,
      itemPadding: t < 0.5 ? itemPadding : other.itemPadding,
      compositeBackgroundColor: Color.lerp(
        compositeBackgroundColor,
        other.compositeBackgroundColor,
        t,
      )!,
      toolbarBackgroundColor: Color.lerp(
        toolbarBackgroundColor,
        other.toolbarBackgroundColor,
        t,
      )!,
      keywordLeftOffset: t < 0.5 ? keywordLeftOffset : other.keywordLeftOffset,
      dividerVerticalPadding: t < 0.5
          ? dividerVerticalPadding
          : other.dividerVerticalPadding,
      separatorThickness: t < 0.5
          ? separatorThickness
          : other.separatorThickness,
      separatorWidth: t < 0.5 ? separatorWidth : other.separatorWidth,
      separatorHeight: t < 0.5 ? separatorHeight : other.separatorHeight,
    );
  }

  @override
  bool operator ==(Object other) {
    return identical(this, other) ||
        (other.runtimeType == runtimeType &&
            other is ToolBarThemeExtension &&
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
              shadowColor,
              other.shadowColor,
            ) &&
            const DeepCollectionEquality().equals(
              shadowOpacity,
              other.shadowOpacity,
            ) &&
            const DeepCollectionEquality().equals(
              shadowBlurRadius,
              other.shadowBlurRadius,
            ) &&
            const DeepCollectionEquality().equals(
              shadowOffset,
              other.shadowOffset,
            ) &&
            const DeepCollectionEquality().equals(
              itemPadding,
              other.itemPadding,
            ) &&
            const DeepCollectionEquality().equals(
              compositeBackgroundColor,
              other.compositeBackgroundColor,
            ) &&
            const DeepCollectionEquality().equals(
              toolbarBackgroundColor,
              other.toolbarBackgroundColor,
            ) &&
            const DeepCollectionEquality().equals(
              keywordLeftOffset,
              other.keywordLeftOffset,
            ) &&
            const DeepCollectionEquality().equals(
              dividerVerticalPadding,
              other.dividerVerticalPadding,
            ) &&
            const DeepCollectionEquality().equals(
              separatorThickness,
              other.separatorThickness,
            ) &&
            const DeepCollectionEquality().equals(
              separatorWidth,
              other.separatorWidth,
            ) &&
            const DeepCollectionEquality().equals(
              separatorHeight,
              other.separatorHeight,
            ));
  }

  @override
  int get hashCode {
    return Object.hash(
      runtimeType.hashCode,
      const DeepCollectionEquality().hash(backgroundColor),
      const DeepCollectionEquality().hash(borderColor),
      const DeepCollectionEquality().hash(borderWidth),
      const DeepCollectionEquality().hash(shadowColor),
      const DeepCollectionEquality().hash(shadowOpacity),
      const DeepCollectionEquality().hash(shadowBlurRadius),
      const DeepCollectionEquality().hash(shadowOffset),
      const DeepCollectionEquality().hash(itemPadding),
      const DeepCollectionEquality().hash(compositeBackgroundColor),
      const DeepCollectionEquality().hash(toolbarBackgroundColor),
      const DeepCollectionEquality().hash(keywordLeftOffset),
      const DeepCollectionEquality().hash(dividerVerticalPadding),
      const DeepCollectionEquality().hash(separatorThickness),
      const DeepCollectionEquality().hash(separatorWidth),
      const DeepCollectionEquality().hash(separatorHeight),
    );
  }
}

extension ToolBarThemeExtensionBuildContextProps on BuildContext {
  ToolBarThemeExtension get toolBarThemeExtension =>
      Theme.of(this).extension<ToolBarThemeExtension>()!;
  Color get backgroundColor => toolBarThemeExtension.backgroundColor;
  Color get borderColor => toolBarThemeExtension.borderColor;
  double get borderWidth => toolBarThemeExtension.borderWidth;
  Color get shadowColor => toolBarThemeExtension.shadowColor;
  double get shadowOpacity => toolBarThemeExtension.shadowOpacity;
  double get shadowBlurRadius => toolBarThemeExtension.shadowBlurRadius;
  Offset get shadowOffset => toolBarThemeExtension.shadowOffset;
  EdgeInsets get itemPadding => toolBarThemeExtension.itemPadding;
  Color get compositeBackgroundColor =>
      toolBarThemeExtension.compositeBackgroundColor;
  Color get toolbarBackgroundColor =>
      toolBarThemeExtension.toolbarBackgroundColor;
  double get keywordLeftOffset => toolBarThemeExtension.keywordLeftOffset;
  double get dividerVerticalPadding =>
      toolBarThemeExtension.dividerVerticalPadding;
  double get separatorThickness => toolBarThemeExtension.separatorThickness;
  double get separatorWidth => toolBarThemeExtension.separatorWidth;
  double get separatorHeight => toolBarThemeExtension.separatorHeight;
}
