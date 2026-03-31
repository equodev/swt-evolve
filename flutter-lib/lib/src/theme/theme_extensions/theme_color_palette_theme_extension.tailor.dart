// coverage:ignore-file
// GENERATED CODE - DO NOT MODIFY BY HAND
// ignore_for_file: type=lint
// ignore_for_file: unused_element, deprecated_member_use, deprecated_member_use_from_same_package, use_function_type_syntax_for_parameters, unnecessary_const, avoid_init_to_null, invalid_override_different_default_values_named, prefer_expression_function_bodies, annotate_overrides, invalid_annotation_target, unnecessary_question_mark

part of 'theme_color_palette_theme_extension.dart';

// **************************************************************************
// TailorAnnotationsGenerator
// **************************************************************************

mixin _$ThemeColorPaletteThemeExtensionTailorMixin
    on ThemeExtension<ThemeColorPaletteThemeExtension> {
  double get swatchSize;
  double get swatchBorderRadius;
  double get swatchSpacing;
  double get trailingGapAfterSwatches;
  EdgeInsets get chevronPadding;
  double get chevronIconSize;
  Color get swatchBorderColor;
  Color get chevronIconColor;
  String get expandTooltip;
  String get collapseTooltip;
  String get sampleHexCsv;

  @override
  ThemeColorPaletteThemeExtension copyWith({
    double? swatchSize,
    double? swatchBorderRadius,
    double? swatchSpacing,
    double? trailingGapAfterSwatches,
    EdgeInsets? chevronPadding,
    double? chevronIconSize,
    Color? swatchBorderColor,
    Color? chevronIconColor,
    String? expandTooltip,
    String? collapseTooltip,
    String? sampleHexCsv,
  }) {
    return ThemeColorPaletteThemeExtension(
      swatchSize: swatchSize ?? this.swatchSize,
      swatchBorderRadius: swatchBorderRadius ?? this.swatchBorderRadius,
      swatchSpacing: swatchSpacing ?? this.swatchSpacing,
      trailingGapAfterSwatches:
          trailingGapAfterSwatches ?? this.trailingGapAfterSwatches,
      chevronPadding: chevronPadding ?? this.chevronPadding,
      chevronIconSize: chevronIconSize ?? this.chevronIconSize,
      swatchBorderColor: swatchBorderColor ?? this.swatchBorderColor,
      chevronIconColor: chevronIconColor ?? this.chevronIconColor,
      expandTooltip: expandTooltip ?? this.expandTooltip,
      collapseTooltip: collapseTooltip ?? this.collapseTooltip,
      sampleHexCsv: sampleHexCsv ?? this.sampleHexCsv,
    );
  }

  @override
  ThemeColorPaletteThemeExtension lerp(
    covariant ThemeExtension<ThemeColorPaletteThemeExtension>? other,
    double t,
  ) {
    if (other is! ThemeColorPaletteThemeExtension)
      return this as ThemeColorPaletteThemeExtension;
    return ThemeColorPaletteThemeExtension(
      swatchSize: t < 0.5 ? swatchSize : other.swatchSize,
      swatchBorderRadius: t < 0.5
          ? swatchBorderRadius
          : other.swatchBorderRadius,
      swatchSpacing: t < 0.5 ? swatchSpacing : other.swatchSpacing,
      trailingGapAfterSwatches: t < 0.5
          ? trailingGapAfterSwatches
          : other.trailingGapAfterSwatches,
      chevronPadding: t < 0.5 ? chevronPadding : other.chevronPadding,
      chevronIconSize: t < 0.5 ? chevronIconSize : other.chevronIconSize,
      swatchBorderColor: Color.lerp(
        swatchBorderColor,
        other.swatchBorderColor,
        t,
      )!,
      chevronIconColor: Color.lerp(
        chevronIconColor,
        other.chevronIconColor,
        t,
      )!,
      expandTooltip: t < 0.5 ? expandTooltip : other.expandTooltip,
      collapseTooltip: t < 0.5 ? collapseTooltip : other.collapseTooltip,
      sampleHexCsv: t < 0.5 ? sampleHexCsv : other.sampleHexCsv,
    );
  }

  @override
  bool operator ==(Object other) {
    return identical(this, other) ||
        (other.runtimeType == runtimeType &&
            other is ThemeColorPaletteThemeExtension &&
            const DeepCollectionEquality().equals(
              swatchSize,
              other.swatchSize,
            ) &&
            const DeepCollectionEquality().equals(
              swatchBorderRadius,
              other.swatchBorderRadius,
            ) &&
            const DeepCollectionEquality().equals(
              swatchSpacing,
              other.swatchSpacing,
            ) &&
            const DeepCollectionEquality().equals(
              trailingGapAfterSwatches,
              other.trailingGapAfterSwatches,
            ) &&
            const DeepCollectionEquality().equals(
              chevronPadding,
              other.chevronPadding,
            ) &&
            const DeepCollectionEquality().equals(
              chevronIconSize,
              other.chevronIconSize,
            ) &&
            const DeepCollectionEquality().equals(
              swatchBorderColor,
              other.swatchBorderColor,
            ) &&
            const DeepCollectionEquality().equals(
              chevronIconColor,
              other.chevronIconColor,
            ) &&
            const DeepCollectionEquality().equals(
              expandTooltip,
              other.expandTooltip,
            ) &&
            const DeepCollectionEquality().equals(
              collapseTooltip,
              other.collapseTooltip,
            ) &&
            const DeepCollectionEquality().equals(
              sampleHexCsv,
              other.sampleHexCsv,
            ));
  }

  @override
  int get hashCode {
    return Object.hash(
      runtimeType.hashCode,
      const DeepCollectionEquality().hash(swatchSize),
      const DeepCollectionEquality().hash(swatchBorderRadius),
      const DeepCollectionEquality().hash(swatchSpacing),
      const DeepCollectionEquality().hash(trailingGapAfterSwatches),
      const DeepCollectionEquality().hash(chevronPadding),
      const DeepCollectionEquality().hash(chevronIconSize),
      const DeepCollectionEquality().hash(swatchBorderColor),
      const DeepCollectionEquality().hash(chevronIconColor),
      const DeepCollectionEquality().hash(expandTooltip),
      const DeepCollectionEquality().hash(collapseTooltip),
      const DeepCollectionEquality().hash(sampleHexCsv),
    );
  }
}

extension ThemeColorPaletteThemeExtensionBuildContextProps on BuildContext {
  ThemeColorPaletteThemeExtension get themeColorPaletteThemeExtension =>
      Theme.of(this).extension<ThemeColorPaletteThemeExtension>()!;
  double get swatchSize => themeColorPaletteThemeExtension.swatchSize;
  double get swatchBorderRadius =>
      themeColorPaletteThemeExtension.swatchBorderRadius;
  double get swatchSpacing => themeColorPaletteThemeExtension.swatchSpacing;
  double get trailingGapAfterSwatches =>
      themeColorPaletteThemeExtension.trailingGapAfterSwatches;
  EdgeInsets get chevronPadding =>
      themeColorPaletteThemeExtension.chevronPadding;
  double get chevronIconSize => themeColorPaletteThemeExtension.chevronIconSize;
  Color get swatchBorderColor =>
      themeColorPaletteThemeExtension.swatchBorderColor;
  Color get chevronIconColor =>
      themeColorPaletteThemeExtension.chevronIconColor;
  String get expandTooltip => themeColorPaletteThemeExtension.expandTooltip;
  String get collapseTooltip => themeColorPaletteThemeExtension.collapseTooltip;
  String get sampleHexCsv => themeColorPaletteThemeExtension.sampleHexCsv;
}
