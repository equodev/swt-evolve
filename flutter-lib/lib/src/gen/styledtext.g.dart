// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'styledtext.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

VStyledText _$VStyledTextFromJson(Map<String, dynamic> json) => VStyledText()
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
  ..horizontalBar = json['horizontalBar'] == null
      ? null
      : VScrollBar.fromJson(json['horizontalBar'] as Map<String, dynamic>)
  ..verticalBar = json['verticalBar'] == null
      ? null
      : VScrollBar.fromJson(json['verticalBar'] as Map<String, dynamic>)
  ..backgroundMode = (json['backgroundMode'] as num?)?.toInt()
  ..children = (json['children'] as List<dynamic>?)
      ?.map((e) => VControl.fromJson(e as Map<String, dynamic>))
      .toList()
  ..alwaysShowScrollBars = json['alwaysShowScrollBars'] as bool?
  ..bottomMargin = (json['bottomMargin'] as num?)?.toInt()
  ..caretOffset = (json['caretOffset'] as num?)?.toInt()
  ..doubleClickEnabled = json['doubleClickEnabled'] as bool?
  ..editable = json['editable'] as bool?
  ..horizontalPixel = (json['horizontalPixel'] as num?)?.toInt()
  ..leftMargin = (json['leftMargin'] as num?)?.toInt()
  ..marginColor = json['marginColor'] == null
      ? null
      : VColor.fromJson(json['marginColor'] as Map<String, dynamic>)
  ..rightMargin = (json['rightMargin'] as num?)?.toInt()
  ..selectionBackground = json['selectionBackground'] == null
      ? null
      : VColor.fromJson(json['selectionBackground'] as Map<String, dynamic>)
  ..selectionForeground = json['selectionForeground'] == null
      ? null
      : VColor.fromJson(json['selectionForeground'] as Map<String, dynamic>)
  ..selectionRange = json['selectionRange'] == null
      ? null
      : VPoint.fromJson(json['selectionRange'] as Map<String, dynamic>)
  ..tabs = (json['tabs'] as num?)?.toInt()
  ..text = json['text'] as String?
  ..topMargin = (json['topMargin'] as num?)?.toInt()
  ..topPixel = (json['topPixel'] as num?)?.toInt()
  ..wordWrap = json['wordWrap'] as bool?
  ..renderer = json['renderer'] == null
      ? null
      : VStyledTextRenderer.fromJson(json['renderer'] as Map<String, dynamic>);

Map<String, dynamic> _$VStyledTextToJson(VStyledText instance) =>
    <String, dynamic>{
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
      'horizontalBar': ?instance.horizontalBar,
      'verticalBar': ?instance.verticalBar,
      'backgroundMode': ?instance.backgroundMode,
      'children': ?instance.children,
      'alwaysShowScrollBars': ?instance.alwaysShowScrollBars,
      'bottomMargin': ?instance.bottomMargin,
      'caretOffset': ?instance.caretOffset,
      'doubleClickEnabled': ?instance.doubleClickEnabled,
      'editable': ?instance.editable,
      'horizontalPixel': ?instance.horizontalPixel,
      'leftMargin': ?instance.leftMargin,
      'marginColor': ?instance.marginColor,
      'rightMargin': ?instance.rightMargin,
      'selectionBackground': ?instance.selectionBackground,
      'selectionForeground': ?instance.selectionForeground,
      'selectionRange': ?instance.selectionRange,
      'tabs': ?instance.tabs,
      'text': ?instance.text,
      'topMargin': ?instance.topMargin,
      'topPixel': ?instance.topPixel,
      'wordWrap': ?instance.wordWrap,
      'renderer': ?instance.renderer,
    };
