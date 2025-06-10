// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'scrolledcomposite.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

ScrolledCompositeValue _$ScrolledCompositeValueFromJson(
        Map<String, dynamic> json) =>
    ScrolledCompositeValue()
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
      ..visible = json['visible'] as bool?
      ..backgroundMode = (json['backgroundMode'] as num?)?.toInt()
      ..layout = json['layout'] == null
          ? null
          : LayoutValue.fromJson(json['layout'] as Map<String, dynamic>)
      ..layoutDeferred = json['layoutDeferred'] as bool?
      ..alwaysShowScrollBars = json['alwaysShowScrollBars'] as bool?
      ..expandHorizontal = json['expandHorizontal'] as bool?
      ..expandVertical = json['expandVertical'] as bool?
      ..minWidth = (json['minWidth'] as num?)?.toInt()
      ..minHeight = (json['minHeight'] as num?)?.toInt()
      ..showFocusedControl = json['showFocusedControl'] as bool?;

Map<String, dynamic> _$ScrolledCompositeValueToJson(
        ScrolledCompositeValue instance) =>
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
      'visible': instance.visible,
      'backgroundMode': instance.backgroundMode,
      'layout': instance.layout,
      'layoutDeferred': instance.layoutDeferred,
      'alwaysShowScrollBars': instance.alwaysShowScrollBars,
      'expandHorizontal': instance.expandHorizontal,
      'expandVertical': instance.expandVertical,
      'minWidth': instance.minWidth,
      'minHeight': instance.minHeight,
      'showFocusedControl': instance.showFocusedControl,
    };
