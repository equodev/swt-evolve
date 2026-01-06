// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'toolitem_theme_extension.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

ToolItemThemeExtension _$ToolItemThemeExtensionFromJson(
        Map<String, dynamic> json) =>
    ToolItemThemeExtension(
      enabledColor:
          const ColorConverter().fromJson(json['enabledColor'] as String),
      disabledColor:
          const ColorConverter().fromJson(json['disabledColor'] as String),
      hoverColor: const ColorConverter().fromJson(json['hoverColor'] as String),
      selectedBackgroundColor: const ColorConverter()
          .fromJson(json['selectedBackgroundColor'] as String),
      separatorColor:
          const ColorConverter().fromJson(json['separatorColor'] as String),
      dropdownIconColor:
          const ColorConverter().fromJson(json['dropdownIconColor'] as String),
      borderRadius: (json['borderRadius'] as num).toDouble(),
      separatorWidth: (json['separatorWidth'] as num).toDouble(),
      separatorThickness: (json['separatorThickness'] as num).toDouble(),
      separatorIndent: (json['separatorIndent'] as num).toDouble(),
      defaultIconSize: (json['defaultIconSize'] as num).toDouble(),
      iconSize: (json['iconSize'] as num).toDouble(),
      emptyButtonSize: (json['emptyButtonSize'] as num).toDouble(),
      dropdownArrowSize: (json['dropdownArrowSize'] as num).toDouble(),
      buttonPadding: const EdgeInsetsConverter()
          .fromJson(json['buttonPadding'] as Map<String, dynamic>),
      textPadding: const EdgeInsetsConverter()
          .fromJson(json['textPadding'] as Map<String, dynamic>),
      splashOpacity: (json['splashOpacity'] as num).toDouble(),
      highlightOpacity: (json['highlightOpacity'] as num).toDouble(),
      separatorOpacity: (json['separatorOpacity'] as num).toDouble(),
      separatorBarWidth: (json['separatorBarWidth'] as num).toDouble(),
      separatorBarMargin: const EdgeInsetsConverter()
          .fromJson(json['separatorBarMargin'] as Map<String, dynamic>),
      tooltipMargin: const EdgeInsetsConverter()
          .fromJson(json['tooltipMargin'] as Map<String, dynamic>),
      tooltipWaitDuration:
          Duration(microseconds: (json['tooltipWaitDuration'] as num).toInt()),
      segmentSelectedBackgroundColor: const ColorConverter()
          .fromJson(json['segmentSelectedBackgroundColor'] as String),
      segmentInnerColor:
          const ColorConverter().fromJson(json['segmentInnerColor'] as String),
      segmentUnselectedBackgroundColor: const ColorConverter()
          .fromJson(json['segmentUnselectedBackgroundColor'] as String),
      segmentSelectedTextColor: const ColorConverter()
          .fromJson(json['segmentSelectedTextColor'] as String),
      segmentUnselectedTextColor: const ColorConverter()
          .fromJson(json['segmentUnselectedTextColor'] as String),
      segmentBorderRadius: (json['segmentBorderRadius'] as num).toDouble(),
      segmentPadding: const EdgeInsetsConverter()
          .fromJson(json['segmentPadding'] as Map<String, dynamic>),
      loadingIndicatorSizeFactor:
          (json['loadingIndicatorSizeFactor'] as num).toDouble(),
      loadingIndicatorStrokeWidth:
          (json['loadingIndicatorStrokeWidth'] as num).toDouble(),
      tooltipPreferBelow: json['tooltipPreferBelow'] as bool,
      tooltipVerticalOffset: (json['tooltipVerticalOffset'] as num).toDouble(),
      segmentAnimationDuration: Duration(
          microseconds: (json['segmentAnimationDuration'] as num).toInt()),
      segmentKeywordText: json['segmentKeywordText'] as String,
      segmentDebugText: json['segmentDebugText'] as String,
      specialDropdownTooltipText: json['specialDropdownTooltipText'] as String,
      specialDropdownBackgroundColor: const ColorConverter()
          .fromJson(json['specialDropdownBackgroundColor'] as String),
      specialDropdownTextColor: const ColorConverter()
          .fromJson(json['specialDropdownTextColor'] as String),
      specialDropdownSeparatorColor: const ColorConverter()
          .fromJson(json['specialDropdownSeparatorColor'] as String),
      specialDropdownArrowColor: const ColorConverter()
          .fromJson(json['specialDropdownArrowColor'] as String),
      specialDropdownPadding: const EdgeInsetsConverter()
          .fromJson(json['specialDropdownPadding'] as Map<String, dynamic>),
      specialDropdownItemSpacing:
          (json['specialDropdownItemSpacing'] as num).toDouble(),
      fontStyle: const TextStyleConverter()
          .fromJson(json['fontStyle'] as Map<String, dynamic>?),
    );

Map<String, dynamic> _$ToolItemThemeExtensionToJson(
        ToolItemThemeExtension instance) =>
    <String, dynamic>{
      'enabledColor': const ColorConverter().toJson(instance.enabledColor),
      'disabledColor': const ColorConverter().toJson(instance.disabledColor),
      'hoverColor': const ColorConverter().toJson(instance.hoverColor),
      'selectedBackgroundColor':
          const ColorConverter().toJson(instance.selectedBackgroundColor),
      'separatorColor': const ColorConverter().toJson(instance.separatorColor),
      'dropdownIconColor':
          const ColorConverter().toJson(instance.dropdownIconColor),
      'borderRadius': instance.borderRadius,
      'separatorWidth': instance.separatorWidth,
      'separatorThickness': instance.separatorThickness,
      'separatorIndent': instance.separatorIndent,
      'fontStyle': const TextStyleConverter().toJson(instance.fontStyle),
      'defaultIconSize': instance.defaultIconSize,
      'iconSize': instance.iconSize,
      'emptyButtonSize': instance.emptyButtonSize,
      'dropdownArrowSize': instance.dropdownArrowSize,
      'buttonPadding':
          const EdgeInsetsConverter().toJson(instance.buttonPadding),
      'textPadding': const EdgeInsetsConverter().toJson(instance.textPadding),
      'splashOpacity': instance.splashOpacity,
      'highlightOpacity': instance.highlightOpacity,
      'separatorOpacity': instance.separatorOpacity,
      'separatorBarWidth': instance.separatorBarWidth,
      'separatorBarMargin':
          const EdgeInsetsConverter().toJson(instance.separatorBarMargin),
      'tooltipMargin':
          const EdgeInsetsConverter().toJson(instance.tooltipMargin),
      'tooltipWaitDuration': instance.tooltipWaitDuration.inMicroseconds,
      'segmentSelectedBackgroundColor': const ColorConverter()
          .toJson(instance.segmentSelectedBackgroundColor),
      'segmentInnerColor':
          const ColorConverter().toJson(instance.segmentInnerColor),
      'segmentUnselectedBackgroundColor': const ColorConverter()
          .toJson(instance.segmentUnselectedBackgroundColor),
      'segmentSelectedTextColor':
          const ColorConverter().toJson(instance.segmentSelectedTextColor),
      'segmentUnselectedTextColor':
          const ColorConverter().toJson(instance.segmentUnselectedTextColor),
      'segmentBorderRadius': instance.segmentBorderRadius,
      'segmentPadding':
          const EdgeInsetsConverter().toJson(instance.segmentPadding),
      'loadingIndicatorSizeFactor': instance.loadingIndicatorSizeFactor,
      'loadingIndicatorStrokeWidth': instance.loadingIndicatorStrokeWidth,
      'tooltipPreferBelow': instance.tooltipPreferBelow,
      'tooltipVerticalOffset': instance.tooltipVerticalOffset,
      'segmentAnimationDuration':
          instance.segmentAnimationDuration.inMicroseconds,
      'segmentKeywordText': instance.segmentKeywordText,
      'segmentDebugText': instance.segmentDebugText,
      'specialDropdownTooltipText': instance.specialDropdownTooltipText,
      'specialDropdownBackgroundColor': const ColorConverter()
          .toJson(instance.specialDropdownBackgroundColor),
      'specialDropdownTextColor':
          const ColorConverter().toJson(instance.specialDropdownTextColor),
      'specialDropdownSeparatorColor':
          const ColorConverter().toJson(instance.specialDropdownSeparatorColor),
      'specialDropdownArrowColor':
          const ColorConverter().toJson(instance.specialDropdownArrowColor),
      'specialDropdownPadding':
          const EdgeInsetsConverter().toJson(instance.specialDropdownPadding),
      'specialDropdownItemSpacing': instance.specialDropdownItemSpacing,
    };
