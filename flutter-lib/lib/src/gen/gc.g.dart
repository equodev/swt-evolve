// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'gc.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

VGC _$VGCFromJson(Map<String, dynamic> json) => VGC()
  ..swt = json['swt'] as String
  ..id = (json['id'] as num).toInt()
  ..style = (json['style'] as num).toInt()
  ..advanced = json['advanced'] as bool?
  ..alpha = (json['alpha'] as num?)?.toInt()
  ..antialias = (json['antialias'] as num?)?.toInt()
  ..background = json['background'] == null
      ? null
      : VColor.fromJson(json['background'] as Map<String, dynamic>)
  ..clipping = json['clipping'] == null
      ? null
      : VRectangle.fromJson(json['clipping'] as Map<String, dynamic>)
  ..fillRule = (json['fillRule'] as num?)?.toInt()
  ..foreground = json['foreground'] == null
      ? null
      : VColor.fromJson(json['foreground'] as Map<String, dynamic>)
  ..interpolation = (json['interpolation'] as num?)?.toInt()
  ..lineCap = (json['lineCap'] as num?)?.toInt()
  ..lineDash = (json['lineDash'] as List<dynamic>?)
      ?.map((e) => (e as num).toInt())
      .toList()
  ..lineJoin = (json['lineJoin'] as num?)?.toInt()
  ..lineStyle = (json['lineStyle'] as num?)?.toInt()
  ..lineWidth = (json['lineWidth'] as num?)?.toInt()
  ..textAntialias = (json['textAntialias'] as num?)?.toInt()
  ..xORMode = json['xORMode'] as bool?;

Map<String, dynamic> _$VGCToJson(VGC instance) => <String, dynamic>{
      'swt': instance.swt,
      'id': instance.id,
      'style': instance.style,
      'advanced': instance.advanced,
      'alpha': instance.alpha,
      'antialias': instance.antialias,
      'background': instance.background,
      'clipping': instance.clipping,
      'fillRule': instance.fillRule,
      'foreground': instance.foreground,
      'interpolation': instance.interpolation,
      'lineCap': instance.lineCap,
      'lineDash': instance.lineDash,
      'lineJoin': instance.lineJoin,
      'lineStyle': instance.lineStyle,
      'lineWidth': instance.lineWidth,
      'textAntialias': instance.textAntialias,
      'xORMode': instance.xORMode,
    };

VGCCopyArea _$VGCCopyAreaFromJson(Map<String, dynamic> json) => VGCCopyArea(
      srcX: (json['srcX'] as num?)?.toInt() ?? 0,
      srcY: (json['srcY'] as num?)?.toInt() ?? 0,
      width: (json['width'] as num?)?.toInt() ?? 0,
      height: (json['height'] as num?)?.toInt() ?? 0,
      destX: (json['destX'] as num?)?.toInt() ?? 0,
      destY: (json['destY'] as num?)?.toInt() ?? 0,
      paint: json['paint'] as bool? ?? false,
    );

Map<String, dynamic> _$VGCCopyAreaToJson(VGCCopyArea instance) =>
    <String, dynamic>{
      'srcX': instance.srcX,
      'srcY': instance.srcY,
      'width': instance.width,
      'height': instance.height,
      'destX': instance.destX,
      'destY': instance.destY,
      'paint': instance.paint,
    };

VGCDrawArc _$VGCDrawArcFromJson(Map<String, dynamic> json) => VGCDrawArc(
      x: (json['x'] as num?)?.toInt() ?? 0,
      y: (json['y'] as num?)?.toInt() ?? 0,
      width: (json['width'] as num?)?.toInt() ?? 0,
      height: (json['height'] as num?)?.toInt() ?? 0,
      startAngle: (json['startAngle'] as num?)?.toInt() ?? 0,
      arcAngle: (json['arcAngle'] as num?)?.toInt() ?? 0,
    );

Map<String, dynamic> _$VGCDrawArcToJson(VGCDrawArc instance) =>
    <String, dynamic>{
      'x': instance.x,
      'y': instance.y,
      'width': instance.width,
      'height': instance.height,
      'startAngle': instance.startAngle,
      'arcAngle': instance.arcAngle,
    };

VGCDrawFocus _$VGCDrawFocusFromJson(Map<String, dynamic> json) => VGCDrawFocus(
      x: (json['x'] as num?)?.toInt() ?? 0,
      y: (json['y'] as num?)?.toInt() ?? 0,
      width: (json['width'] as num?)?.toInt() ?? 0,
      height: (json['height'] as num?)?.toInt() ?? 0,
    );

Map<String, dynamic> _$VGCDrawFocusToJson(VGCDrawFocus instance) =>
    <String, dynamic>{
      'x': instance.x,
      'y': instance.y,
      'width': instance.width,
      'height': instance.height,
    };

VGCDrawLine _$VGCDrawLineFromJson(Map<String, dynamic> json) => VGCDrawLine(
      x1: (json['x1'] as num?)?.toInt() ?? 0,
      y1: (json['y1'] as num?)?.toInt() ?? 0,
      x2: (json['x2'] as num?)?.toInt() ?? 0,
      y2: (json['y2'] as num?)?.toInt() ?? 0,
    );

Map<String, dynamic> _$VGCDrawLineToJson(VGCDrawLine instance) =>
    <String, dynamic>{
      'x1': instance.x1,
      'y1': instance.y1,
      'x2': instance.x2,
      'y2': instance.y2,
    };

VGCDrawOval _$VGCDrawOvalFromJson(Map<String, dynamic> json) => VGCDrawOval(
      x: (json['x'] as num?)?.toInt() ?? 0,
      y: (json['y'] as num?)?.toInt() ?? 0,
      width: (json['width'] as num?)?.toInt() ?? 0,
      height: (json['height'] as num?)?.toInt() ?? 0,
    );

Map<String, dynamic> _$VGCDrawOvalToJson(VGCDrawOval instance) =>
    <String, dynamic>{
      'x': instance.x,
      'y': instance.y,
      'width': instance.width,
      'height': instance.height,
    };

VGCDrawPoint _$VGCDrawPointFromJson(Map<String, dynamic> json) => VGCDrawPoint(
      x: (json['x'] as num?)?.toInt() ?? 0,
      y: (json['y'] as num?)?.toInt() ?? 0,
    );

Map<String, dynamic> _$VGCDrawPointToJson(VGCDrawPoint instance) =>
    <String, dynamic>{
      'x': instance.x,
      'y': instance.y,
    };

VGCDrawPolygon _$VGCDrawPolygonFromJson(Map<String, dynamic> json) =>
    VGCDrawPolygon(
      pointArray: (json['pointArray'] as List<dynamic>?)
              ?.map((e) => (e as num).toInt())
              .toList() ??
          null,
    );

Map<String, dynamic> _$VGCDrawPolygonToJson(VGCDrawPolygon instance) =>
    <String, dynamic>{
      'pointArray': instance.pointArray,
    };

VGCDrawPolyline _$VGCDrawPolylineFromJson(Map<String, dynamic> json) =>
    VGCDrawPolyline(
      pointArray: (json['pointArray'] as List<dynamic>?)
              ?.map((e) => (e as num).toInt())
              .toList() ??
          null,
    );

Map<String, dynamic> _$VGCDrawPolylineToJson(VGCDrawPolyline instance) =>
    <String, dynamic>{
      'pointArray': instance.pointArray,
    };

VGCDrawRectangle _$VGCDrawRectangleFromJson(Map<String, dynamic> json) =>
    VGCDrawRectangle(
      x: (json['x'] as num?)?.toInt() ?? 0,
      y: (json['y'] as num?)?.toInt() ?? 0,
      width: (json['width'] as num?)?.toInt() ?? 0,
      height: (json['height'] as num?)?.toInt() ?? 0,
    );

Map<String, dynamic> _$VGCDrawRectangleToJson(VGCDrawRectangle instance) =>
    <String, dynamic>{
      'x': instance.x,
      'y': instance.y,
      'width': instance.width,
      'height': instance.height,
    };

VGCDrawRoundRectangle _$VGCDrawRoundRectangleFromJson(
        Map<String, dynamic> json) =>
    VGCDrawRoundRectangle(
      x: (json['x'] as num?)?.toInt() ?? 0,
      y: (json['y'] as num?)?.toInt() ?? 0,
      width: (json['width'] as num?)?.toInt() ?? 0,
      height: (json['height'] as num?)?.toInt() ?? 0,
      arcWidth: (json['arcWidth'] as num?)?.toInt() ?? 0,
      arcHeight: (json['arcHeight'] as num?)?.toInt() ?? 0,
    );

Map<String, dynamic> _$VGCDrawRoundRectangleToJson(
        VGCDrawRoundRectangle instance) =>
    <String, dynamic>{
      'x': instance.x,
      'y': instance.y,
      'width': instance.width,
      'height': instance.height,
      'arcWidth': instance.arcWidth,
      'arcHeight': instance.arcHeight,
    };

VGCDrawText _$VGCDrawTextFromJson(Map<String, dynamic> json) => VGCDrawText(
      string: json['string'] as String? ?? '',
      x: (json['x'] as num?)?.toInt() ?? 0,
      y: (json['y'] as num?)?.toInt() ?? 0,
      flags: (json['flags'] as num?)?.toInt() ?? 0,
    );

Map<String, dynamic> _$VGCDrawTextToJson(VGCDrawText instance) =>
    <String, dynamic>{
      'string': instance.string,
      'x': instance.x,
      'y': instance.y,
      'flags': instance.flags,
    };

VGCFillArc _$VGCFillArcFromJson(Map<String, dynamic> json) => VGCFillArc(
      x: (json['x'] as num?)?.toInt() ?? 0,
      y: (json['y'] as num?)?.toInt() ?? 0,
      width: (json['width'] as num?)?.toInt() ?? 0,
      height: (json['height'] as num?)?.toInt() ?? 0,
      startAngle: (json['startAngle'] as num?)?.toInt() ?? 0,
      arcAngle: (json['arcAngle'] as num?)?.toInt() ?? 0,
    );

Map<String, dynamic> _$VGCFillArcToJson(VGCFillArc instance) =>
    <String, dynamic>{
      'x': instance.x,
      'y': instance.y,
      'width': instance.width,
      'height': instance.height,
      'startAngle': instance.startAngle,
      'arcAngle': instance.arcAngle,
    };

VGCFillGradientRectangle _$VGCFillGradientRectangleFromJson(
        Map<String, dynamic> json) =>
    VGCFillGradientRectangle(
      x: (json['x'] as num?)?.toInt() ?? 0,
      y: (json['y'] as num?)?.toInt() ?? 0,
      width: (json['width'] as num?)?.toInt() ?? 0,
      height: (json['height'] as num?)?.toInt() ?? 0,
      vertical: json['vertical'] as bool? ?? false,
    );

Map<String, dynamic> _$VGCFillGradientRectangleToJson(
        VGCFillGradientRectangle instance) =>
    <String, dynamic>{
      'x': instance.x,
      'y': instance.y,
      'width': instance.width,
      'height': instance.height,
      'vertical': instance.vertical,
    };

VGCFillOval _$VGCFillOvalFromJson(Map<String, dynamic> json) => VGCFillOval(
      x: (json['x'] as num?)?.toInt() ?? 0,
      y: (json['y'] as num?)?.toInt() ?? 0,
      width: (json['width'] as num?)?.toInt() ?? 0,
      height: (json['height'] as num?)?.toInt() ?? 0,
    );

Map<String, dynamic> _$VGCFillOvalToJson(VGCFillOval instance) =>
    <String, dynamic>{
      'x': instance.x,
      'y': instance.y,
      'width': instance.width,
      'height': instance.height,
    };

VGCFillPolygon _$VGCFillPolygonFromJson(Map<String, dynamic> json) =>
    VGCFillPolygon(
      pointArray: (json['pointArray'] as List<dynamic>?)
              ?.map((e) => (e as num).toInt())
              .toList() ??
          null,
    );

Map<String, dynamic> _$VGCFillPolygonToJson(VGCFillPolygon instance) =>
    <String, dynamic>{
      'pointArray': instance.pointArray,
    };

VGCFillRectangle _$VGCFillRectangleFromJson(Map<String, dynamic> json) =>
    VGCFillRectangle(
      x: (json['x'] as num?)?.toInt() ?? 0,
      y: (json['y'] as num?)?.toInt() ?? 0,
      width: (json['width'] as num?)?.toInt() ?? 0,
      height: (json['height'] as num?)?.toInt() ?? 0,
    );

Map<String, dynamic> _$VGCFillRectangleToJson(VGCFillRectangle instance) =>
    <String, dynamic>{
      'x': instance.x,
      'y': instance.y,
      'width': instance.width,
      'height': instance.height,
    };

VGCFillRoundRectangle _$VGCFillRoundRectangleFromJson(
        Map<String, dynamic> json) =>
    VGCFillRoundRectangle(
      x: (json['x'] as num?)?.toInt() ?? 0,
      y: (json['y'] as num?)?.toInt() ?? 0,
      width: (json['width'] as num?)?.toInt() ?? 0,
      height: (json['height'] as num?)?.toInt() ?? 0,
      arcWidth: (json['arcWidth'] as num?)?.toInt() ?? 0,
      arcHeight: (json['arcHeight'] as num?)?.toInt() ?? 0,
    );

Map<String, dynamic> _$VGCFillRoundRectangleToJson(
        VGCFillRoundRectangle instance) =>
    <String, dynamic>{
      'x': instance.x,
      'y': instance.y,
      'width': instance.width,
      'height': instance.height,
      'arcWidth': instance.arcWidth,
      'arcHeight': instance.arcHeight,
    };
