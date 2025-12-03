// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'scrolledcomposite.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

VScrolledComposite _$VScrolledCompositeFromJson(Map<String, dynamic> json) =>
    VScrolledComposite()
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
      ..textDirection = (json['textDirection'] as num?)?.toInt()
      ..toolTipText = json['toolTipText'] as String?
      ..touchEnabled = json['touchEnabled'] as bool?
      ..visible = json['visible'] as bool?
      ..scrollbarsMode = (json['scrollbarsMode'] as num?)?.toInt()
      ..backgroundMode = (json['backgroundMode'] as num?)?.toInt()
      ..children = (json['children'] as List<dynamic>?)
          ?.map((e) => VControl.fromJson(e as Map<String, dynamic>))
          .toList()
      ..layoutDeferred = json['layoutDeferred'] as bool?
      ..tabList = (json['tabList'] as List<dynamic>?)
          ?.map((e) => VControl.fromJson(e as Map<String, dynamic>))
          .toList()
      ..alwaysShowScrollBars = json['alwaysShowScrollBars'] as bool?
      ..content = json['content'] == null
          ? null
          : VControl.fromJson(json['content'] as Map<String, dynamic>)
      ..expandHorizontal = json['expandHorizontal'] as bool?
      ..expandVertical = json['expandVertical'] as bool?
      ..minHeight = (json['minHeight'] as num?)?.toInt()
      ..minSize = (json['minSize'] as num?)?.toInt()
      ..minWidth = (json['minWidth'] as num?)?.toInt()
      ..origin = json['origin'] == null
          ? null
          : VPoint.fromJson(json['origin'] as Map<String, dynamic>)
      ..showFocusedControl = json['showFocusedControl'] as bool?;

Map<String, dynamic> _$VScrolledCompositeToJson(VScrolledComposite instance) =>
    <String, dynamic>{
      'swt': instance.swt,
      'id': instance.id,
      'style': instance.style,
      'background': instance.background,
      'backgroundImage': instance.backgroundImage,
      'bounds': instance.bounds,
      'capture': instance.capture,
      'dragDetect': instance.dragDetect,
      'enabled': instance.enabled,
      'font': instance.font,
      'foreground': instance.foreground,
      'menu': instance.menu,
      'orientation': instance.orientation,
      'redraw': instance.redraw,
      'textDirection': instance.textDirection,
      'toolTipText': instance.toolTipText,
      'touchEnabled': instance.touchEnabled,
      'visible': instance.visible,
      'scrollbarsMode': instance.scrollbarsMode,
      'backgroundMode': instance.backgroundMode,
      'children': instance.children,
      'layoutDeferred': instance.layoutDeferred,
      'tabList': instance.tabList,
      'alwaysShowScrollBars': instance.alwaysShowScrollBars,
      'content': instance.content,
      'expandHorizontal': instance.expandHorizontal,
      'expandVertical': instance.expandVertical,
      'minHeight': instance.minHeight,
      'minSize': instance.minSize,
      'minWidth': instance.minWidth,
      'origin': instance.origin,
      'showFocusedControl': instance.showFocusedControl,
    };
