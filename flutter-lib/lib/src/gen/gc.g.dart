// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'gc.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

VGC _$VGCFromJson(Map<String, dynamic> json) => VGC()
  ..swt = json['swt'] as String
  ..id = (json['id'] as num).toInt()
  ..style = (json['style'] as num).toInt()
  ..XORMode = json['XORMode'] as bool?
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
  ..font = json['font'] == null
      ? null
      : VFont.fromJson(json['font'] as Map<String, dynamic>)
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
  ..textAntialias = (json['textAntialias'] as num?)?.toInt();

Map<String, dynamic> _$VGCToJson(VGC instance) => <String, dynamic>{
  'swt': instance.swt,
  'id': instance.id,
  'style': instance.style,
  'XORMode': instance.XORMode,
  'advanced': instance.advanced,
  'alpha': instance.alpha,
  'antialias': instance.antialias,
  'background': instance.background,
  'clipping': instance.clipping,
  'fillRule': instance.fillRule,
  'font': instance.font,
  'foreground': instance.foreground,
  'interpolation': instance.interpolation,
  'lineCap': instance.lineCap,
  'lineDash': instance.lineDash,
  'lineJoin': instance.lineJoin,
  'lineStyle': instance.lineStyle,
  'lineWidth': instance.lineWidth,
  'textAntialias': instance.textAntialias,
};

VGCCopyAreaImageintint _$VGCCopyAreaImageintintFromJson(
  Map<String, dynamic> json,
) =>
    VGCCopyAreaImageintint(
        x: (json['x'] as num?)?.toInt() ?? 0,
        y: (json['y'] as num?)?.toInt() ?? 0,
      )
      ..image = json['image'] == null
          ? null
          : VImage.fromJson(json['image'] as Map<String, dynamic>);

Map<String, dynamic> _$VGCCopyAreaImageintintToJson(
  VGCCopyAreaImageintint instance,
) => <String, dynamic>{
  'image': instance.image,
  'x': instance.x,
  'y': instance.y,
};

VGCCopyAreaintintintintintint _$VGCCopyAreaintintintintintintFromJson(
  Map<String, dynamic> json,
) => VGCCopyAreaintintintintintint(
  srcX: (json['srcX'] as num?)?.toInt() ?? 0,
  srcY: (json['srcY'] as num?)?.toInt() ?? 0,
  width: (json['width'] as num?)?.toInt() ?? 0,
  height: (json['height'] as num?)?.toInt() ?? 0,
  destX: (json['destX'] as num?)?.toInt() ?? 0,
  destY: (json['destY'] as num?)?.toInt() ?? 0,
);

Map<String, dynamic> _$VGCCopyAreaintintintintintintToJson(
  VGCCopyAreaintintintintintint instance,
) => <String, dynamic>{
  'srcX': instance.srcX,
  'srcY': instance.srcY,
  'width': instance.width,
  'height': instance.height,
  'destX': instance.destX,
  'destY': instance.destY,
};

VGCCopyAreaintintintintintintboolean
_$VGCCopyAreaintintintintintintbooleanFromJson(Map<String, dynamic> json) =>
    VGCCopyAreaintintintintintintboolean(
      srcX: (json['srcX'] as num?)?.toInt() ?? 0,
      srcY: (json['srcY'] as num?)?.toInt() ?? 0,
      width: (json['width'] as num?)?.toInt() ?? 0,
      height: (json['height'] as num?)?.toInt() ?? 0,
      destX: (json['destX'] as num?)?.toInt() ?? 0,
      destY: (json['destY'] as num?)?.toInt() ?? 0,
      paint: json['paint'] as bool? ?? false,
    );

Map<String, dynamic> _$VGCCopyAreaintintintintintintbooleanToJson(
  VGCCopyAreaintintintintintintboolean instance,
) => <String, dynamic>{
  'srcX': instance.srcX,
  'srcY': instance.srcY,
  'width': instance.width,
  'height': instance.height,
  'destX': instance.destX,
  'destY': instance.destY,
  'paint': instance.paint,
};

VGCDrawArcintintintintintint _$VGCDrawArcintintintintintintFromJson(
  Map<String, dynamic> json,
) => VGCDrawArcintintintintintint(
  x: (json['x'] as num?)?.toInt() ?? 0,
  y: (json['y'] as num?)?.toInt() ?? 0,
  width: (json['width'] as num?)?.toInt() ?? 0,
  height: (json['height'] as num?)?.toInt() ?? 0,
  startAngle: (json['startAngle'] as num?)?.toInt() ?? 0,
  arcAngle: (json['arcAngle'] as num?)?.toInt() ?? 0,
);

Map<String, dynamic> _$VGCDrawArcintintintintintintToJson(
  VGCDrawArcintintintintintint instance,
) => <String, dynamic>{
  'x': instance.x,
  'y': instance.y,
  'width': instance.width,
  'height': instance.height,
  'startAngle': instance.startAngle,
  'arcAngle': instance.arcAngle,
};

VGCDrawFocusintintintint _$VGCDrawFocusintintintintFromJson(
  Map<String, dynamic> json,
) => VGCDrawFocusintintintint(
  x: (json['x'] as num?)?.toInt() ?? 0,
  y: (json['y'] as num?)?.toInt() ?? 0,
  width: (json['width'] as num?)?.toInt() ?? 0,
  height: (json['height'] as num?)?.toInt() ?? 0,
);

Map<String, dynamic> _$VGCDrawFocusintintintintToJson(
  VGCDrawFocusintintintint instance,
) => <String, dynamic>{
  'x': instance.x,
  'y': instance.y,
  'width': instance.width,
  'height': instance.height,
};

VGCDrawImageImageintint _$VGCDrawImageImageintintFromJson(
  Map<String, dynamic> json,
) =>
    VGCDrawImageImageintint(
        x: (json['x'] as num?)?.toInt() ?? 0,
        y: (json['y'] as num?)?.toInt() ?? 0,
      )
      ..image = json['image'] == null
          ? null
          : VImage.fromJson(json['image'] as Map<String, dynamic>);

Map<String, dynamic> _$VGCDrawImageImageintintToJson(
  VGCDrawImageImageintint instance,
) => <String, dynamic>{
  'image': instance.image,
  'x': instance.x,
  'y': instance.y,
};

VGCDrawImageImageintintintintintintintint
_$VGCDrawImageImageintintintintintintintintFromJson(
  Map<String, dynamic> json,
) =>
    VGCDrawImageImageintintintintintintintint(
        srcX: (json['srcX'] as num?)?.toInt() ?? 0,
        srcY: (json['srcY'] as num?)?.toInt() ?? 0,
        srcWidth: (json['srcWidth'] as num?)?.toInt() ?? 0,
        srcHeight: (json['srcHeight'] as num?)?.toInt() ?? 0,
        destX: (json['destX'] as num?)?.toInt() ?? 0,
        destY: (json['destY'] as num?)?.toInt() ?? 0,
        destWidth: (json['destWidth'] as num?)?.toInt() ?? 0,
        destHeight: (json['destHeight'] as num?)?.toInt() ?? 0,
      )
      ..image = json['image'] == null
          ? null
          : VImage.fromJson(json['image'] as Map<String, dynamic>);

Map<String, dynamic> _$VGCDrawImageImageintintintintintintintintToJson(
  VGCDrawImageImageintintintintintintintint instance,
) => <String, dynamic>{
  'image': instance.image,
  'srcX': instance.srcX,
  'srcY': instance.srcY,
  'srcWidth': instance.srcWidth,
  'srcHeight': instance.srcHeight,
  'destX': instance.destX,
  'destY': instance.destY,
  'destWidth': instance.destWidth,
  'destHeight': instance.destHeight,
};

VGCDrawLineintintintint _$VGCDrawLineintintintintFromJson(
  Map<String, dynamic> json,
) => VGCDrawLineintintintint(
  x1: (json['x1'] as num?)?.toInt() ?? 0,
  y1: (json['y1'] as num?)?.toInt() ?? 0,
  x2: (json['x2'] as num?)?.toInt() ?? 0,
  y2: (json['y2'] as num?)?.toInt() ?? 0,
);

Map<String, dynamic> _$VGCDrawLineintintintintToJson(
  VGCDrawLineintintintint instance,
) => <String, dynamic>{
  'x1': instance.x1,
  'y1': instance.y1,
  'x2': instance.x2,
  'y2': instance.y2,
};

VGCDrawOvalintintintint _$VGCDrawOvalintintintintFromJson(
  Map<String, dynamic> json,
) => VGCDrawOvalintintintint(
  x: (json['x'] as num?)?.toInt() ?? 0,
  y: (json['y'] as num?)?.toInt() ?? 0,
  width: (json['width'] as num?)?.toInt() ?? 0,
  height: (json['height'] as num?)?.toInt() ?? 0,
);

Map<String, dynamic> _$VGCDrawOvalintintintintToJson(
  VGCDrawOvalintintintint instance,
) => <String, dynamic>{
  'x': instance.x,
  'y': instance.y,
  'width': instance.width,
  'height': instance.height,
};

VGCDrawPointintint _$VGCDrawPointintintFromJson(Map<String, dynamic> json) =>
    VGCDrawPointintint(
      x: (json['x'] as num?)?.toInt() ?? 0,
      y: (json['y'] as num?)?.toInt() ?? 0,
    );

Map<String, dynamic> _$VGCDrawPointintintToJson(VGCDrawPointintint instance) =>
    <String, dynamic>{'x': instance.x, 'y': instance.y};

VGCDrawPolygonint _$VGCDrawPolygonintFromJson(Map<String, dynamic> json) =>
    VGCDrawPolygonint()
      ..pointArray = (json['pointArray'] as List<dynamic>?)
          ?.map((e) => (e as num).toInt())
          .toList();

Map<String, dynamic> _$VGCDrawPolygonintToJson(VGCDrawPolygonint instance) =>
    <String, dynamic>{'pointArray': instance.pointArray};

VGCDrawPolylineint _$VGCDrawPolylineintFromJson(Map<String, dynamic> json) =>
    VGCDrawPolylineint()
      ..pointArray = (json['pointArray'] as List<dynamic>?)
          ?.map((e) => (e as num).toInt())
          .toList();

Map<String, dynamic> _$VGCDrawPolylineintToJson(VGCDrawPolylineint instance) =>
    <String, dynamic>{'pointArray': instance.pointArray};

VGCDrawRectangleRectangle _$VGCDrawRectangleRectangleFromJson(
  Map<String, dynamic> json,
) => VGCDrawRectangleRectangle()
  ..rect = json['rect'] == null
      ? null
      : VRectangle.fromJson(json['rect'] as Map<String, dynamic>);

Map<String, dynamic> _$VGCDrawRectangleRectangleToJson(
  VGCDrawRectangleRectangle instance,
) => <String, dynamic>{'rect': instance.rect};

VGCDrawRectangleintintintint _$VGCDrawRectangleintintintintFromJson(
  Map<String, dynamic> json,
) => VGCDrawRectangleintintintint(
  x: (json['x'] as num?)?.toInt() ?? 0,
  y: (json['y'] as num?)?.toInt() ?? 0,
  width: (json['width'] as num?)?.toInt() ?? 0,
  height: (json['height'] as num?)?.toInt() ?? 0,
);

Map<String, dynamic> _$VGCDrawRectangleintintintintToJson(
  VGCDrawRectangleintintintint instance,
) => <String, dynamic>{
  'x': instance.x,
  'y': instance.y,
  'width': instance.width,
  'height': instance.height,
};

VGCDrawRoundRectangleintintintintintint
_$VGCDrawRoundRectangleintintintintintintFromJson(Map<String, dynamic> json) =>
    VGCDrawRoundRectangleintintintintintint(
      x: (json['x'] as num?)?.toInt() ?? 0,
      y: (json['y'] as num?)?.toInt() ?? 0,
      width: (json['width'] as num?)?.toInt() ?? 0,
      height: (json['height'] as num?)?.toInt() ?? 0,
      arcWidth: (json['arcWidth'] as num?)?.toInt() ?? 0,
      arcHeight: (json['arcHeight'] as num?)?.toInt() ?? 0,
    );

Map<String, dynamic> _$VGCDrawRoundRectangleintintintintintintToJson(
  VGCDrawRoundRectangleintintintintintint instance,
) => <String, dynamic>{
  'x': instance.x,
  'y': instance.y,
  'width': instance.width,
  'height': instance.height,
  'arcWidth': instance.arcWidth,
  'arcHeight': instance.arcHeight,
};

VGCDrawStringStringintint _$VGCDrawStringStringintintFromJson(
  Map<String, dynamic> json,
) => VGCDrawStringStringintint(
  string: json['string'] as String? ?? '',
  x: (json['x'] as num?)?.toInt() ?? 0,
  y: (json['y'] as num?)?.toInt() ?? 0,
);

Map<String, dynamic> _$VGCDrawStringStringintintToJson(
  VGCDrawStringStringintint instance,
) => <String, dynamic>{
  'string': instance.string,
  'x': instance.x,
  'y': instance.y,
};

VGCDrawStringStringintintboolean _$VGCDrawStringStringintintbooleanFromJson(
  Map<String, dynamic> json,
) => VGCDrawStringStringintintboolean(
  string: json['string'] as String? ?? '',
  x: (json['x'] as num?)?.toInt() ?? 0,
  y: (json['y'] as num?)?.toInt() ?? 0,
  isTransparent: json['isTransparent'] as bool? ?? false,
);

Map<String, dynamic> _$VGCDrawStringStringintintbooleanToJson(
  VGCDrawStringStringintintboolean instance,
) => <String, dynamic>{
  'string': instance.string,
  'x': instance.x,
  'y': instance.y,
  'isTransparent': instance.isTransparent,
};

VGCDrawTextStringintint _$VGCDrawTextStringintintFromJson(
  Map<String, dynamic> json,
) => VGCDrawTextStringintint(
  string: json['string'] as String? ?? '',
  x: (json['x'] as num?)?.toInt() ?? 0,
  y: (json['y'] as num?)?.toInt() ?? 0,
);

Map<String, dynamic> _$VGCDrawTextStringintintToJson(
  VGCDrawTextStringintint instance,
) => <String, dynamic>{
  'string': instance.string,
  'x': instance.x,
  'y': instance.y,
};

VGCDrawTextStringintintboolean _$VGCDrawTextStringintintbooleanFromJson(
  Map<String, dynamic> json,
) => VGCDrawTextStringintintboolean(
  string: json['string'] as String? ?? '',
  x: (json['x'] as num?)?.toInt() ?? 0,
  y: (json['y'] as num?)?.toInt() ?? 0,
  isTransparent: json['isTransparent'] as bool? ?? false,
);

Map<String, dynamic> _$VGCDrawTextStringintintbooleanToJson(
  VGCDrawTextStringintintboolean instance,
) => <String, dynamic>{
  'string': instance.string,
  'x': instance.x,
  'y': instance.y,
  'isTransparent': instance.isTransparent,
};

VGCDrawTextStringintintint _$VGCDrawTextStringintintintFromJson(
  Map<String, dynamic> json,
) => VGCDrawTextStringintintint(
  string: json['string'] as String? ?? '',
  x: (json['x'] as num?)?.toInt() ?? 0,
  y: (json['y'] as num?)?.toInt() ?? 0,
  flags: (json['flags'] as num?)?.toInt() ?? 0,
);

Map<String, dynamic> _$VGCDrawTextStringintintintToJson(
  VGCDrawTextStringintintint instance,
) => <String, dynamic>{
  'string': instance.string,
  'x': instance.x,
  'y': instance.y,
  'flags': instance.flags,
};

VGCFillArcintintintintintint _$VGCFillArcintintintintintintFromJson(
  Map<String, dynamic> json,
) => VGCFillArcintintintintintint(
  x: (json['x'] as num?)?.toInt() ?? 0,
  y: (json['y'] as num?)?.toInt() ?? 0,
  width: (json['width'] as num?)?.toInt() ?? 0,
  height: (json['height'] as num?)?.toInt() ?? 0,
  startAngle: (json['startAngle'] as num?)?.toInt() ?? 0,
  arcAngle: (json['arcAngle'] as num?)?.toInt() ?? 0,
);

Map<String, dynamic> _$VGCFillArcintintintintintintToJson(
  VGCFillArcintintintintintint instance,
) => <String, dynamic>{
  'x': instance.x,
  'y': instance.y,
  'width': instance.width,
  'height': instance.height,
  'startAngle': instance.startAngle,
  'arcAngle': instance.arcAngle,
};

VGCFillGradientRectangleintintintintboolean
_$VGCFillGradientRectangleintintintintbooleanFromJson(
  Map<String, dynamic> json,
) => VGCFillGradientRectangleintintintintboolean(
  x: (json['x'] as num?)?.toInt() ?? 0,
  y: (json['y'] as num?)?.toInt() ?? 0,
  width: (json['width'] as num?)?.toInt() ?? 0,
  height: (json['height'] as num?)?.toInt() ?? 0,
  vertical: json['vertical'] as bool? ?? false,
);

Map<String, dynamic> _$VGCFillGradientRectangleintintintintbooleanToJson(
  VGCFillGradientRectangleintintintintboolean instance,
) => <String, dynamic>{
  'x': instance.x,
  'y': instance.y,
  'width': instance.width,
  'height': instance.height,
  'vertical': instance.vertical,
};

VGCFillOvalintintintint _$VGCFillOvalintintintintFromJson(
  Map<String, dynamic> json,
) => VGCFillOvalintintintint(
  x: (json['x'] as num?)?.toInt() ?? 0,
  y: (json['y'] as num?)?.toInt() ?? 0,
  width: (json['width'] as num?)?.toInt() ?? 0,
  height: (json['height'] as num?)?.toInt() ?? 0,
);

Map<String, dynamic> _$VGCFillOvalintintintintToJson(
  VGCFillOvalintintintint instance,
) => <String, dynamic>{
  'x': instance.x,
  'y': instance.y,
  'width': instance.width,
  'height': instance.height,
};

VGCFillPolygonint _$VGCFillPolygonintFromJson(Map<String, dynamic> json) =>
    VGCFillPolygonint()
      ..pointArray = (json['pointArray'] as List<dynamic>?)
          ?.map((e) => (e as num).toInt())
          .toList();

Map<String, dynamic> _$VGCFillPolygonintToJson(VGCFillPolygonint instance) =>
    <String, dynamic>{'pointArray': instance.pointArray};

VGCFillRectangleRectangle _$VGCFillRectangleRectangleFromJson(
  Map<String, dynamic> json,
) => VGCFillRectangleRectangle()
  ..rect = json['rect'] == null
      ? null
      : VRectangle.fromJson(json['rect'] as Map<String, dynamic>);

Map<String, dynamic> _$VGCFillRectangleRectangleToJson(
  VGCFillRectangleRectangle instance,
) => <String, dynamic>{'rect': instance.rect};

VGCFillRectangleintintintint _$VGCFillRectangleintintintintFromJson(
  Map<String, dynamic> json,
) => VGCFillRectangleintintintint(
  x: (json['x'] as num?)?.toInt() ?? 0,
  y: (json['y'] as num?)?.toInt() ?? 0,
  width: (json['width'] as num?)?.toInt() ?? 0,
  height: (json['height'] as num?)?.toInt() ?? 0,
);

Map<String, dynamic> _$VGCFillRectangleintintintintToJson(
  VGCFillRectangleintintintint instance,
) => <String, dynamic>{
  'x': instance.x,
  'y': instance.y,
  'width': instance.width,
  'height': instance.height,
};

VGCFillRoundRectangleintintintintintint
_$VGCFillRoundRectangleintintintintintintFromJson(Map<String, dynamic> json) =>
    VGCFillRoundRectangleintintintintintint(
      x: (json['x'] as num?)?.toInt() ?? 0,
      y: (json['y'] as num?)?.toInt() ?? 0,
      width: (json['width'] as num?)?.toInt() ?? 0,
      height: (json['height'] as num?)?.toInt() ?? 0,
      arcWidth: (json['arcWidth'] as num?)?.toInt() ?? 0,
      arcHeight: (json['arcHeight'] as num?)?.toInt() ?? 0,
    );

Map<String, dynamic> _$VGCFillRoundRectangleintintintintintintToJson(
  VGCFillRoundRectangleintintintintintint instance,
) => <String, dynamic>{
  'x': instance.x,
  'y': instance.y,
  'width': instance.width,
  'height': instance.height,
  'arcWidth': instance.arcWidth,
  'arcHeight': instance.arcHeight,
};
