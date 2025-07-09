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
  ..capture = json['capture'] as bool?
  ..dragDetect = json['dragDetect'] as bool?
  ..enabled = json['enabled'] as bool?
  ..foreground = json['foreground'] == null
      ? null
      : VColor.fromJson(json['foreground'] as Map<String, dynamic>)
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
  ..borderVisible = json['borderVisible'] as bool?
  ..fixedTabHeight = (json['fixedTabHeight'] as num?)?.toInt()
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
  ..maximized = json['maximized'] as bool?
  ..minChars = (json['minChars'] as num?)?.toInt()
  ..minimized = json['minimized'] as bool?
  ..mru = json['mru'] as bool?
  ..onBottom = json['onBottom'] as bool?
  ..selectedIndex = (json['selectedIndex'] as num?)?.toInt()
  ..selectionBackground = json['selectionBackground'] == null
      ? null
      : VColor.fromJson(json['selectionBackground'] as Map<String, dynamic>)
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
  ..selectionHighlightBarThickness =
      (json['selectionHighlightBarThickness'] as num?)?.toInt()
  ..showMax = json['showMax'] as bool?
  ..showMin = json['showMin'] as bool?
  ..showSelectedImage = json['showSelectedImage'] as bool?
  ..showUnselectedClose = json['showUnselectedClose'] as bool?
  ..showUnselectedImage = json['showUnselectedImage'] as bool?
  ..simple = json['simple'] as bool?
  ..single = json['single'] as bool?
  ..topRight = json['topRight'] == null
      ? null
      : VControl.fromJson(json['topRight'] as Map<String, dynamic>)
  ..topRightAlignment = (json['topRightAlignment'] as num?)?.toInt();

Map<String, dynamic> _$VCTabFolderToJson(VCTabFolder instance) =>
    <String, dynamic>{
      'swt': instance.swt,
      'id': instance.id,
      'style': instance.style,
      'background': instance.background,
      'bounds': instance.bounds,
      'capture': instance.capture,
      'dragDetect': instance.dragDetect,
      'enabled': instance.enabled,
      'foreground': instance.foreground,
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
      'borderVisible': instance.borderVisible,
      'fixedTabHeight': instance.fixedTabHeight,
      'gradientColors': instance.gradientColors,
      'gradientPercents': instance.gradientPercents,
      'gradientVertical': instance.gradientVertical,
      'highlightEnabled': instance.highlightEnabled,
      'items': instance.items,
      'maximized': instance.maximized,
      'minChars': instance.minChars,
      'minimized': instance.minimized,
      'mru': instance.mru,
      'onBottom': instance.onBottom,
      'selectedIndex': instance.selectedIndex,
      'selectionBackground': instance.selectionBackground,
      'selectionForeground': instance.selectionForeground,
      'selectionGradientColors': instance.selectionGradientColors,
      'selectionGradientPercents': instance.selectionGradientPercents,
      'selectionGradientVertical': instance.selectionGradientVertical,
      'selectionHighlightBarThickness': instance.selectionHighlightBarThickness,
      'showMax': instance.showMax,
      'showMin': instance.showMin,
      'showSelectedImage': instance.showSelectedImage,
      'showUnselectedClose': instance.showUnselectedClose,
      'showUnselectedImage': instance.showUnselectedImage,
      'simple': instance.simple,
      'single': instance.single,
      'topRight': instance.topRight,
      'topRightAlignment': instance.topRightAlignment,
    };
