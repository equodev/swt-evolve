// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'slider.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

VSlider _$VSliderFromJson(Map<String, dynamic> json) => VSlider()
  ..swt = json['swt'] as String
  ..id = (json['id'] as num).toInt()
  ..seq = (json['_s'] as num?)?.toInt() ?? 0
  ..style = (json['style'] as num?)?.toInt() ?? 0
  ..background = json['background'] == null
      ? null
      : VColor.fromJson(json['background'] as Map<String, dynamic>)
  ..backgroundImage = json['backgroundImage'] == null
      ? null
      : VImage.fromJson(json['backgroundImage'] as Map<String, dynamic>)
  ..bounds = json['bounds'] == null
      ? null
      : VRectangle.fromJson(json['bounds'] as Map<String, dynamic>)
  ..cursor = json['cursor'] == null
      ? null
      : VCursor.fromJson(json['cursor'] as Map<String, dynamic>)
  ..dragDetect = json['dragDetect'] as bool?
  ..dragSource = json['dragSource'] as bool?
  ..dropTargetId = (json['dropTargetId'] as num?)?.toInt()
  ..enabled = json['enabled'] as bool?
  ..font = json['font'] == null
      ? null
      : VFont.fromJson(json['font'] as Map<String, dynamic>)
  ..foreground = json['foreground'] == null
      ? null
      : VColor.fromJson(json['foreground'] as Map<String, dynamic>)
  ..hasOwnBackground = json['hasOwnBackground'] as bool?
  ..menu = json['menu'] == null
      ? null
      : VMenu.fromJson(json['menu'] as Map<String, dynamic>)
  ..region = json['region'] == null
      ? null
      : VRegion.fromJson(json['region'] as Map<String, dynamic>)
  ..toolTipText = json['toolTipText'] as String?
  ..visible = json['visible'] as bool?
  ..increment = (json['increment'] as num?)?.toInt()
  ..maximum = (json['maximum'] as num?)?.toInt()
  ..minimum = (json['minimum'] as num?)?.toInt()
  ..pageIncrement = (json['pageIncrement'] as num?)?.toInt()
  ..selection = (json['selection'] as num?)?.toInt()
  ..thumb = (json['thumb'] as num?)?.toInt();

Map<String, dynamic> _$VSliderToJson(VSlider instance) => <String, dynamic>{
  'swt': instance.swt,
  'id': instance.id,
  'style': instance.style,
  'background': ?instance.background,
  'backgroundImage': ?instance.backgroundImage,
  'bounds': ?instance.bounds,
  'cursor': ?instance.cursor,
  'dragDetect': ?instance.dragDetect,
  'dragSource': ?instance.dragSource,
  'dropTargetId': ?instance.dropTargetId,
  'enabled': ?instance.enabled,
  'font': ?instance.font,
  'foreground': ?instance.foreground,
  'hasOwnBackground': ?instance.hasOwnBackground,
  'menu': ?instance.menu,
  'region': ?instance.region,
  'toolTipText': ?instance.toolTipText,
  'visible': ?instance.visible,
  'increment': ?instance.increment,
  'maximum': ?instance.maximum,
  'minimum': ?instance.minimum,
  'pageIncrement': ?instance.pageIncrement,
  'selection': ?instance.selection,
  'thumb': ?instance.thumb,
};
