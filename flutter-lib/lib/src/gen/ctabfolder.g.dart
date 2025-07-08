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
      .toList()
  ..borderVisible = json['borderVisible'] as bool?
  ..items = (json['items'] as List<dynamic>?)
      ?.map((e) => VCTabItem.fromJson(e as Map<String, dynamic>))
      .toList()
  ..minimized = json['minimized'] as bool?
  ..showMin = json['showMin'] as bool?
  ..minChars = (json['minChars'] as num?)?.toInt()
  ..maximized = json['maximized'] as bool?
  ..showMax = json['showMax'] as bool?
  ..mru = json['mru'] as bool?
  ..selectedIndex = (json['selectedIndex'] as num?)?.toInt()
  ..selectionBackground = json['selectionBackground'] == null
      ? null
      : VColor.fromJson(json['selectionBackground'] as Map<String, dynamic>)
  ..selectionForeground = json['selectionForeground'] == null
      ? null
      : VColor.fromJson(json['selectionForeground'] as Map<String, dynamic>)
  ..simple = json['simple'] as bool?
  ..single = json['single'] as bool?
  ..topRight = json['topRight'] == null
      ? null
      : VControl.fromJson(json['topRight'] as Map<String, dynamic>)
  ..topRightAlignment = (json['topRightAlignment'] as num?)?.toInt()
  ..showUnselectedClose = json['showUnselectedClose'] as bool?
  ..showUnselectedImage = json['showUnselectedImage'] as bool?
  ..showSelectedImage = json['showSelectedImage'] as bool?
  ..gradientColors = (json['gradientColors'] as List<dynamic>?)
      ?.map((e) => VColor.fromJson(e as Map<String, dynamic>))
      .toList()
  ..gradientPercents = (json['gradientPercents'] as List<dynamic>?)
      ?.map((e) => (e as num).toInt())
      .toList()
  ..gradientVertical = json['gradientVertical'] as bool?
  ..selectionGradientColors =
      (json['selectionGradientColors'] as List<dynamic>?)
          ?.map((e) => VColor.fromJson(e as Map<String, dynamic>))
          .toList()
  ..selectionGradientPercents =
      (json['selectionGradientPercents'] as List<dynamic>?)
          ?.map((e) => (e as num).toInt())
          .toList()
  ..selectionGradientVertical = json['selectionGradientVertical'] as bool?
  ..selectionHighlightBarThickness =
      (json['selectionHighlightBarThickness'] as num?)?.toInt()
  ..fixedTabHeight = (json['fixedTabHeight'] as num?)?.toInt()
  ..onBottom = json['onBottom'] as bool?
  ..highlightEnabled = json['highlightEnabled'] as bool?;

Map<String, dynamic> _$VCTabFolderToJson(VCTabFolder instance) =>
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
      'borderVisible': instance.borderVisible,
      'items': instance.items,
      'minimized': instance.minimized,
      'showMin': instance.showMin,
      'minChars': instance.minChars,
      'maximized': instance.maximized,
      'showMax': instance.showMax,
      'mru': instance.mru,
      'selectedIndex': instance.selectedIndex,
      'selectionBackground': instance.selectionBackground,
      'selectionForeground': instance.selectionForeground,
      'simple': instance.simple,
      'single': instance.single,
      'topRight': instance.topRight,
      'topRightAlignment': instance.topRightAlignment,
      'showUnselectedClose': instance.showUnselectedClose,
      'showUnselectedImage': instance.showUnselectedImage,
      'showSelectedImage': instance.showSelectedImage,
      'gradientColors': instance.gradientColors,
      'gradientPercents': instance.gradientPercents,
      'gradientVertical': instance.gradientVertical,
      'selectionGradientColors': instance.selectionGradientColors,
      'selectionGradientPercents': instance.selectionGradientPercents,
      'selectionGradientVertical': instance.selectionGradientVertical,
      'selectionHighlightBarThickness': instance.selectionHighlightBarThickness,
      'fixedTabHeight': instance.fixedTabHeight,
      'onBottom': instance.onBottom,
      'highlightEnabled': instance.highlightEnabled,
    };
