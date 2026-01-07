// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'label_theme_extension.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

LabelThemeExtension _$LabelThemeExtensionFromJson(Map<String, dynamic> json) =>
    LabelThemeExtension(
      primaryTextColor: const ColorConverter().fromJson(
        json['primaryTextColor'] as String,
      ),
      secondaryTextColor: const ColorConverter().fromJson(
        json['secondaryTextColor'] as String,
      ),
      errorTextColor: const ColorConverter().fromJson(
        json['errorTextColor'] as String,
      ),
      warningTextColor: const ColorConverter().fromJson(
        json['warningTextColor'] as String,
      ),
      successTextColor: const ColorConverter().fromJson(
        json['successTextColor'] as String,
      ),
      disabledTextColor: const ColorConverter().fromJson(
        json['disabledTextColor'] as String,
      ),
      linkTextColor: const ColorConverter().fromJson(
        json['linkTextColor'] as String,
      ),
      linkHoverTextColor: const ColorConverter().fromJson(
        json['linkHoverTextColor'] as String,
      ),
      backgroundColor: _$JsonConverterFromJson<String, Color>(
        json['backgroundColor'],
        const ColorConverter().fromJson,
      ),
      hoverBackgroundColor: _$JsonConverterFromJson<String, Color>(
        json['hoverBackgroundColor'],
        const ColorConverter().fromJson,
      ),
      selectedBackgroundColor: _$JsonConverterFromJson<String, Color>(
        json['selectedBackgroundColor'],
        const ColorConverter().fromJson,
      ),
      borderColor: _$JsonConverterFromJson<String, Color>(
        json['borderColor'],
        const ColorConverter().fromJson,
      ),
      focusBorderColor: _$JsonConverterFromJson<String, Color>(
        json['focusBorderColor'],
        const ColorConverter().fromJson,
      ),
      primaryTextStyle: const TextStyleConverter().fromJson(
        json['primaryTextStyle'] as Map<String, dynamic>?,
      ),
      secondaryTextStyle: const TextStyleConverter().fromJson(
        json['secondaryTextStyle'] as Map<String, dynamic>?,
      ),
      errorTextStyle: const TextStyleConverter().fromJson(
        json['errorTextStyle'] as Map<String, dynamic>?,
      ),
      warningTextStyle: const TextStyleConverter().fromJson(
        json['warningTextStyle'] as Map<String, dynamic>?,
      ),
      successTextStyle: const TextStyleConverter().fromJson(
        json['successTextStyle'] as Map<String, dynamic>?,
      ),
      disabledTextStyle: const TextStyleConverter().fromJson(
        json['disabledTextStyle'] as Map<String, dynamic>?,
      ),
      linkTextStyle: const TextStyleConverter().fromJson(
        json['linkTextStyle'] as Map<String, dynamic>?,
      ),
      padding: const EdgeInsetsConverter().fromJson(
        json['padding'] as Map<String, dynamic>,
      ),
      margin: const EdgeInsetsConverter().fromJson(
        json['margin'] as Map<String, dynamic>,
      ),
      iconTextSpacing: (json['iconTextSpacing'] as num).toDouble(),
      borderRadius: (json['borderRadius'] as num).toDouble(),
      borderWidth: (json['borderWidth'] as num).toDouble(),
      focusBorderWidth: (json['focusBorderWidth'] as num).toDouble(),
      iconSize: (json['iconSize'] as num).toDouble(),
      smallIconSize: (json['smallIconSize'] as num).toDouble(),
      largeIconSize: (json['largeIconSize'] as num).toDouble(),
      isSelectable: json['isSelectable'] as bool,
      showTooltip: json['showTooltip'] as bool,
      disabledOpacity: (json['disabledOpacity'] as num).toDouble(),
      hoverAnimationDuration: Duration(
        microseconds: (json['hoverAnimationDuration'] as num).toInt(),
      ),
      textAlign: const TextAlignConverter().fromJson(
        (json['textAlign'] as num).toInt(),
      ),
      mainAxisAlignment: const MainAxisAlignmentConverter().fromJson(
        (json['mainAxisAlignment'] as num).toInt(),
      ),
      crossAxisAlignment: const CrossAxisAlignmentConverter().fromJson(
        (json['crossAxisAlignment'] as num).toInt(),
      ),
      textDecoration: const TextDecorationConverter().fromJson(
        json['textDecoration'] as String,
      ),
      hoverTextDecoration: const TextDecorationConverter().fromJson(
        json['hoverTextDecoration'] as String,
      ),
      decorationThickness: (json['decorationThickness'] as num).toDouble(),
    );

Map<String, dynamic> _$LabelThemeExtensionToJson(
  LabelThemeExtension instance,
) => <String, dynamic>{
  'primaryTextColor': const ColorConverter().toJson(instance.primaryTextColor),
  'secondaryTextColor': const ColorConverter().toJson(
    instance.secondaryTextColor,
  ),
  'errorTextColor': const ColorConverter().toJson(instance.errorTextColor),
  'warningTextColor': const ColorConverter().toJson(instance.warningTextColor),
  'successTextColor': const ColorConverter().toJson(instance.successTextColor),
  'disabledTextColor': const ColorConverter().toJson(
    instance.disabledTextColor,
  ),
  'linkTextColor': const ColorConverter().toJson(instance.linkTextColor),
  'linkHoverTextColor': const ColorConverter().toJson(
    instance.linkHoverTextColor,
  ),
  'backgroundColor': _$JsonConverterToJson<String, Color>(
    instance.backgroundColor,
    const ColorConverter().toJson,
  ),
  'hoverBackgroundColor': _$JsonConverterToJson<String, Color>(
    instance.hoverBackgroundColor,
    const ColorConverter().toJson,
  ),
  'selectedBackgroundColor': _$JsonConverterToJson<String, Color>(
    instance.selectedBackgroundColor,
    const ColorConverter().toJson,
  ),
  'borderColor': _$JsonConverterToJson<String, Color>(
    instance.borderColor,
    const ColorConverter().toJson,
  ),
  'focusBorderColor': _$JsonConverterToJson<String, Color>(
    instance.focusBorderColor,
    const ColorConverter().toJson,
  ),
  'primaryTextStyle': const TextStyleConverter().toJson(
    instance.primaryTextStyle,
  ),
  'secondaryTextStyle': const TextStyleConverter().toJson(
    instance.secondaryTextStyle,
  ),
  'errorTextStyle': const TextStyleConverter().toJson(instance.errorTextStyle),
  'warningTextStyle': const TextStyleConverter().toJson(
    instance.warningTextStyle,
  ),
  'successTextStyle': const TextStyleConverter().toJson(
    instance.successTextStyle,
  ),
  'disabledTextStyle': const TextStyleConverter().toJson(
    instance.disabledTextStyle,
  ),
  'linkTextStyle': const TextStyleConverter().toJson(instance.linkTextStyle),
  'padding': const EdgeInsetsConverter().toJson(instance.padding),
  'margin': const EdgeInsetsConverter().toJson(instance.margin),
  'iconTextSpacing': instance.iconTextSpacing,
  'borderRadius': instance.borderRadius,
  'borderWidth': instance.borderWidth,
  'focusBorderWidth': instance.focusBorderWidth,
  'iconSize': instance.iconSize,
  'smallIconSize': instance.smallIconSize,
  'largeIconSize': instance.largeIconSize,
  'isSelectable': instance.isSelectable,
  'showTooltip': instance.showTooltip,
  'disabledOpacity': instance.disabledOpacity,
  'hoverAnimationDuration': instance.hoverAnimationDuration.inMicroseconds,
  'textAlign': const TextAlignConverter().toJson(instance.textAlign),
  'mainAxisAlignment': const MainAxisAlignmentConverter().toJson(
    instance.mainAxisAlignment,
  ),
  'crossAxisAlignment': const CrossAxisAlignmentConverter().toJson(
    instance.crossAxisAlignment,
  ),
  'textDecoration': const TextDecorationConverter().toJson(
    instance.textDecoration,
  ),
  'hoverTextDecoration': const TextDecorationConverter().toJson(
    instance.hoverTextDecoration,
  ),
  'decorationThickness': instance.decorationThickness,
};

Value? _$JsonConverterFromJson<Json, Value>(
  Object? json,
  Value? Function(Json json) fromJson,
) => json == null ? null : fromJson(json as Json);

Json? _$JsonConverterToJson<Json, Value>(
  Value? value,
  Json? Function(Value value) toJson,
) => value == null ? null : toJson(value);
