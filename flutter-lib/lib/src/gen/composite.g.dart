// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'composite.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

VComposite _$VCompositeFromJson(Map<String, dynamic> json) => VComposite()
  ..swt = json['swt'] as String
  ..id = (json['id'] as num).toInt()
  ..style = (json['style'] as num).toInt()
  ..background = json['background'] == null
      ? null
      : VColor.fromJson(json['background'] as Map<String, dynamic>)
  ..bounds = json['bounds'] == null
      ? null
      : VRectangle.fromJson(json['bounds'] as Map<String, dynamic>)
  ..dragDetect = json['dragDetect'] as bool?
  ..enabled = json['enabled'] as bool?
  ..foreground = json['foreground'] == null
      ? null
      : VColor.fromJson(json['foreground'] as Map<String, dynamic>)
  ..orientation = (json['orientation'] as num?)?.toInt()
  ..textDirection = (json['textDirection'] as num?)?.toInt()
  ..toolTipText = json['toolTipText'] as String?
  ..touchEnabled = json['touchEnabled'] as bool?
  ..visible = json['visible'] as bool?
  ..capture = json['capture'] as bool?
  ..redraw = json['redraw'] as bool?
  ..scrollbarsMode = (json['scrollbarsMode'] as num?)?.toInt()
  ..backgroundMode = (json['backgroundMode'] as num?)?.toInt()
  ..children = (json['children'] as List<dynamic>?)
      ?.map((e) => VControl.fromJson(e as Map<String, dynamic>))
      .toList()
  ..layoutDeferred = json['layoutDeferred'] as bool?
  ..tabList = (json['tabList'] as List<dynamic>?)
      ?.map((e) => VControl.fromJson(e as Map<String, dynamic>))
      .toList();

Map<String, dynamic> _$VCompositeToJson(VComposite instance) =>
    <String, dynamic>{
      'swt': instance.swt,
      'id': instance.id,
      'style': instance.style,
      'background': instance.background,
      'bounds': instance.bounds,
      'dragDetect': instance.dragDetect,
      'enabled': instance.enabled,
      'foreground': instance.foreground,
      'orientation': instance.orientation,
      'textDirection': instance.textDirection,
      'toolTipText': instance.toolTipText,
      'touchEnabled': instance.touchEnabled,
      'visible': instance.visible,
      'capture': instance.capture,
      'redraw': instance.redraw,
      'scrollbarsMode': instance.scrollbarsMode,
      'backgroundMode': instance.backgroundMode,
      'children': instance.children,
      'layoutDeferred': instance.layoutDeferred,
      'tabList': instance.tabList,
    };
