// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'canvas.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

VCanvas _$VCanvasFromJson(Map<String, dynamic> json) => VCanvas()
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
  ..horizontalBar = json['horizontalBar'] == null
      ? null
      : VScrollBar.fromJson(json['horizontalBar'] as Map<String, dynamic>)
  ..verticalBar = json['verticalBar'] == null
      ? null
      : VScrollBar.fromJson(json['verticalBar'] as Map<String, dynamic>)
  ..backgroundMode = (json['backgroundMode'] as num?)?.toInt()
  ..children = (json['children'] as List<dynamic>?)
      ?.map((e) => VControl.fromJson(e as Map<String, dynamic>))
      .toList();

Map<String, dynamic> _$VCanvasToJson(VCanvas instance) => <String, dynamic>{
  'swt': instance.swt,
  'id': instance.id,
  'style': instance.style,
  'background': ?instance.background,
  'backgroundImage': ?instance.backgroundImage,
  'bounds': ?instance.bounds,
  'cursor': ?instance.cursor,
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
  'horizontalBar': ?instance.horizontalBar,
  'verticalBar': ?instance.verticalBar,
  'backgroundMode': ?instance.backgroundMode,
  'children': ?instance.children,
};
