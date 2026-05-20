// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'viewform.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

VViewForm _$VViewFormFromJson(Map<String, dynamic> json) => VViewForm()
  ..swt = json['swt'] as String
  ..id = (json['id'] as num).toInt()
  ..style = (json['style'] as num).toInt()
  ..background = json['background'] == null
      ? null
      : VColor.fromJson(json['background'] as Map<String, dynamic>)
  ..backgroundImage = json['backgroundImage'] == null
      ? null
      : VImage.fromJson(json['backgroundImage'] as Map<String, dynamic>)
  ..bounds = json['bounds'] == null
      ? null
      : VRectangle.fromJson(json['bounds'] as Map<String, dynamic>)
  ..capture = json['capture'] as bool?
  ..cursor = json['cursor'] == null
      ? null
      : VCursor.fromJson(json['cursor'] as Map<String, dynamic>)
  ..dragDetect = json['dragDetect'] as bool?
  ..enabled = json['enabled'] as bool?
  ..font = json['font'] == null
      ? null
      : VFont.fromJson(json['font'] as Map<String, dynamic>)
  ..foreground = json['foreground'] == null
      ? null
      : VColor.fromJson(json['foreground'] as Map<String, dynamic>)
  ..menu = json['menu'] == null
      ? null
      : VMenu.fromJson(json['menu'] as Map<String, dynamic>)
  ..orientation = (json['orientation'] as num?)?.toInt()
  ..redraw = json['redraw'] as bool?
  ..region = json['region'] == null
      ? null
      : VRegion.fromJson(json['region'] as Map<String, dynamic>)
  ..textDirection = (json['textDirection'] as num?)?.toInt()
  ..toolTipText = json['toolTipText'] as String?
  ..touchEnabled = json['touchEnabled'] as bool?
  ..visible = json['visible'] as bool?
  ..horizontalBar = json['horizontalBar'] == null
      ? null
      : VScrollBar.fromJson(json['horizontalBar'] as Map<String, dynamic>)
  ..scrollbarsMode = (json['scrollbarsMode'] as num?)?.toInt()
  ..verticalBar = json['verticalBar'] == null
      ? null
      : VScrollBar.fromJson(json['verticalBar'] as Map<String, dynamic>)
  ..backgroundMode = (json['backgroundMode'] as num?)?.toInt()
  ..children = (json['children'] as List<dynamic>?)
      ?.map((e) => VControl.fromJson(e as Map<String, dynamic>))
      .toList()
  ..layoutDeferred = json['layoutDeferred'] as bool?
  ..tabList = (json['tabList'] as List<dynamic>?)
      ?.map((e) => VControl.fromJson(e as Map<String, dynamic>))
      .toList()
  ..borderVisible = json['borderVisible'] as bool?
  ..content = json['content'] == null
      ? null
      : VControl.fromJson(json['content'] as Map<String, dynamic>)
  ..topCenter = json['topCenter'] == null
      ? null
      : VControl.fromJson(json['topCenter'] as Map<String, dynamic>)
  ..topCenterSeparate = json['topCenterSeparate'] as bool?
  ..topLeft = json['topLeft'] == null
      ? null
      : VControl.fromJson(json['topLeft'] as Map<String, dynamic>)
  ..topRight = json['topRight'] == null
      ? null
      : VControl.fromJson(json['topRight'] as Map<String, dynamic>);

Map<String, dynamic> _$VViewFormToJson(VViewForm instance) => <String, dynamic>{
  'swt': instance.swt,
  'id': instance.id,
  'style': instance.style,
  'background': ?instance.background,
  'backgroundImage': ?instance.backgroundImage,
  'bounds': ?instance.bounds,
  'capture': ?instance.capture,
  'cursor': ?instance.cursor,
  'dragDetect': ?instance.dragDetect,
  'enabled': ?instance.enabled,
  'font': ?instance.font,
  'foreground': ?instance.foreground,
  'menu': ?instance.menu,
  'orientation': ?instance.orientation,
  'redraw': ?instance.redraw,
  'region': ?instance.region,
  'textDirection': ?instance.textDirection,
  'toolTipText': ?instance.toolTipText,
  'touchEnabled': ?instance.touchEnabled,
  'visible': ?instance.visible,
  'horizontalBar': ?instance.horizontalBar,
  'scrollbarsMode': ?instance.scrollbarsMode,
  'verticalBar': ?instance.verticalBar,
  'backgroundMode': ?instance.backgroundMode,
  'children': ?instance.children,
  'layoutDeferred': ?instance.layoutDeferred,
  'tabList': ?instance.tabList,
  'borderVisible': ?instance.borderVisible,
  'content': ?instance.content,
  'topCenter': ?instance.topCenter,
  'topCenterSeparate': ?instance.topCenterSeparate,
  'topLeft': ?instance.topLeft,
  'topRight': ?instance.topRight,
};
