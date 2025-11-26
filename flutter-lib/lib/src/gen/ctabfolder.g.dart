// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'ctabfolder.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

VCTabFolder _$VCTabFolderFromJson(Map<String, dynamic> json) => VCTabFolder()
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
  ..MRUVisible = json['MRUVisible'] as bool?
  ..borderVisible = json['borderVisible'] as bool?
  ..gradientColors = (json['gradientColors'] as List<dynamic>?)
      ?.map((e) => VColor.fromJson(e as Map<String, dynamic>))
      .toList()
  ..gradientPercents = (json['gradientPercents'] as List<dynamic>?)
      ?.map((e) => (e as num).toInt())
      .toList()
  ..gradientVertical = json['gradientVertical'] as bool?
  ..highlightEnabled = json['highlightEnabled'] as bool?
  ..items = (json['items'] as List<dynamic>?)
      ?.map((e) => VCTabItem.fromJson(e as Map<String, dynamic>))
      .toList()
  ..maximizeVisible = json['maximizeVisible'] as bool?
  ..maximized = json['maximized'] as bool?
  ..minimizeVisible = json['minimizeVisible'] as bool?
  ..minimized = json['minimized'] as bool?
  ..minimumCharacters = (json['minimumCharacters'] as num?)?.toInt()
  ..selectedImageVisible = json['selectedImageVisible'] as bool?
  ..selection = (json['selection'] as num?)?.toInt()
  ..selectionBackground = json['selectionBackground'] == null
      ? null
      : VColor.fromJson(json['selectionBackground'] as Map<String, dynamic>)
  ..selectionBarThickness = (json['selectionBarThickness'] as num?)?.toInt()
  ..selectionBgImage = json['selectionBgImage'] == null
      ? null
      : VImage.fromJson(json['selectionBgImage'] as Map<String, dynamic>)
  ..selectionForeground = json['selectionForeground'] == null
      ? null
      : VColor.fromJson(json['selectionForeground'] as Map<String, dynamic>)
  ..selectionGradientColors =
      (json['selectionGradientColors'] as List<dynamic>?)
          ?.map((e) => VColor.fromJson(e as Map<String, dynamic>))
          .toList()
  ..selectionGradientPercents =
      (json['selectionGradientPercents'] as List<dynamic>?)
          ?.map((e) => (e as num).toInt())
          .toList()
  ..selectionGradientVertical = json['selectionGradientVertical'] as bool?
  ..simple = json['simple'] as bool?
  ..single = json['single'] as bool?
  ..tabHeight = (json['tabHeight'] as num?)?.toInt()
  ..tabPosition = (json['tabPosition'] as num?)?.toInt()
  ..topRight = json['topRight'] == null
      ? null
      : VControl.fromJson(json['topRight'] as Map<String, dynamic>)
  ..topRightAlignment = (json['topRightAlignment'] as num?)?.toInt()
  ..unselectedCloseVisible = json['unselectedCloseVisible'] as bool?
  ..unselectedImageVisible = json['unselectedImageVisible'] as bool?;

Map<String, dynamic> _$VCTabFolderToJson(VCTabFolder instance) =>
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
      'MRUVisible': instance.MRUVisible,
      'borderVisible': instance.borderVisible,
      'gradientColors': instance.gradientColors,
      'gradientPercents': instance.gradientPercents,
      'gradientVertical': instance.gradientVertical,
      'highlightEnabled': instance.highlightEnabled,
      'items': instance.items,
      'maximizeVisible': instance.maximizeVisible,
      'maximized': instance.maximized,
      'minimizeVisible': instance.minimizeVisible,
      'minimized': instance.minimized,
      'minimumCharacters': instance.minimumCharacters,
      'selectedImageVisible': instance.selectedImageVisible,
      'selection': instance.selection,
      'selectionBackground': instance.selectionBackground,
      'selectionBarThickness': instance.selectionBarThickness,
      'selectionBgImage': instance.selectionBgImage,
      'selectionForeground': instance.selectionForeground,
      'selectionGradientColors': instance.selectionGradientColors,
      'selectionGradientPercents': instance.selectionGradientPercents,
      'selectionGradientVertical': instance.selectionGradientVertical,
      'simple': instance.simple,
      'single': instance.single,
      'tabHeight': instance.tabHeight,
      'tabPosition': instance.tabPosition,
      'topRight': instance.topRight,
      'topRightAlignment': instance.topRightAlignment,
      'unselectedCloseVisible': instance.unselectedCloseVisible,
      'unselectedImageVisible': instance.unselectedImageVisible,
    };
