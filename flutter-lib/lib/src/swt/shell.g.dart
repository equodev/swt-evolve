// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'shell.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

ShellValue _$ShellValueFromJson(Map<String, dynamic> json) => ShellValue()
  ..swt = json['swt'] as String
  ..id = (json['id'] as num).toInt()
  ..children = (json['children'] as List<dynamic>?)
      ?.map((e) => WidgetValue.fromJson(e as Map<String, dynamic>))
      .toList()
  ..style = (json['style'] as num).toInt()
  ..orientation = (json['orientation'] as num?)?.toInt()
  ..textDirection = (json['textDirection'] as num?)?.toInt()
  ..bounds = json['bounds'] == null
      ? null
      : RectangleValue.fromJson(json['bounds'] as Map<String, dynamic>)
  ..dragDetect = json['dragDetect'] as bool?
  ..enabled = json['enabled'] as bool?
  ..layoutData = json['layoutData']
  ..menu = json['menu'] == null
      ? null
      : MenuValue.fromJson(json['menu'] as Map<String, dynamic>)
  ..toolTipText = json['toolTipText'] as String?
  ..touchEnabled = json['touchEnabled'] as bool?
  ..backgroundMode = (json['backgroundMode'] as num?)?.toInt()
  ..layout = json['layout'] == null
      ? null
      : LayoutValue.fromJson(json['layout'] as Map<String, dynamic>)
  ..layoutDeferred = json['layoutDeferred'] as bool?
  ..menuBar = json['menuBar']
  ..minimized = json['minimized'] as bool?
  ..text = json['text'] as String?
  ..alpha = (json['alpha'] as num?)?.toInt()
  ..fullScreen = json['fullScreen'] as bool?
  ..maximized = json['maximized'] as bool?
  ..modified = json['modified'] as bool?
  ..visible = json['visible'] as bool?
  ..imeInputMode = (json['imeInputMode'] as num?)?.toInt();

Map<String, dynamic> _$ShellValueToJson(ShellValue instance) =>
    <String, dynamic>{
      'swt': instance.swt,
      'id': instance.id,
      'children': instance.children,
      'style': instance.style,
      'orientation': instance.orientation,
      'textDirection': instance.textDirection,
      'bounds': instance.bounds,
      'dragDetect': instance.dragDetect,
      'enabled': instance.enabled,
      'layoutData': instance.layoutData,
      'menu': instance.menu,
      'toolTipText': instance.toolTipText,
      'touchEnabled': instance.touchEnabled,
      'backgroundMode': instance.backgroundMode,
      'layout': instance.layout,
      'layoutDeferred': instance.layoutDeferred,
      'menuBar': instance.menuBar,
      'minimized': instance.minimized,
      'text': instance.text,
      'alpha': instance.alpha,
      'fullScreen': instance.fullScreen,
      'maximized': instance.maximized,
      'modified': instance.modified,
      'visible': instance.visible,
      'imeInputMode': instance.imeInputMode,
    };
