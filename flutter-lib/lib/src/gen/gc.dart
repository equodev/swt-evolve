import 'dart:convert';
import 'package:flutter/widgets.dart';
import 'package:json_annotation/json_annotation.dart';
import '../gen/color.dart';
import '../gen/font.dart';
import '../gen/image.dart';
import '../gen/path.dart';
import '../gen/pathdata.dart';
import '../gen/pattern.dart';
import '../gen/rectangle.dart';
import '../gen/transform.dart';
import '../gen/widget.dart';
import '../impl/gc_evolve.dart';
import 'widget.dart';
import 'widgets.dart';

part 'gc.g.dart';

class GCSwt<V extends VGC> extends WidgetSwt<V> {
  const GCSwt({super.key, required super.value});

  @override
  State createState() => GCImpl<GCSwt<VGC>, VGC>();
}

abstract class GCState<T extends GCSwt, V extends VGC>
    extends WidgetSwtState<T, V> {}

@JsonSerializable()
class VGC extends VWidget {
  VGC() : this.empty();
  VGC.empty() {
    swt = "GC";
  }

  bool? XORMode;
  int? alpha;
  int? antialias;
  VColor? background;
  VPattern? backgroundPattern;
  VRectangle? clipping;
  VPathData? clippingPath;
  List<int>? clippingRects;
  int? fillRule;
  VFont? font;
  VColor? foreground;
  VPattern? foregroundPattern;
  int? interpolation;
  int? lineCap;
  List<int>? lineDash;
  int? lineJoin;
  int? lineStyle;
  int? lineWidth;
  VTransform? transform;

  VPath? clippingText;
  double? lineDashOffset;
  double? bufferScale;

  @override
  void copyFrom(VWidget other) {
    super.copyFrom(other);
    if (other is VGC) {
      XORMode = other.XORMode;
      alpha = other.alpha;
      antialias = other.antialias;
      background = other.background;
      backgroundPattern = other.backgroundPattern;
      clipping = other.clipping;
      clippingPath = other.clippingPath;
      clippingRects = other.clippingRects;
      fillRule = other.fillRule;
      font = other.font;
      foreground = other.foreground;
      foregroundPattern = other.foregroundPattern;
      interpolation = other.interpolation;
      lineCap = other.lineCap;
      lineDash = other.lineDash;
      lineJoin = other.lineJoin;
      lineStyle = other.lineStyle;
      lineWidth = other.lineWidth;
      transform = other.transform;
      clippingText = other.clippingText;
      lineDashOffset = other.lineDashOffset;
      bufferScale = other.bufferScale;
    }
  }

  @override
  void readProperty(String key, Map<String, dynamic> json) {
    switch (key) {
      case 'XORMode':
        XORMode = json['XORMode'] as bool?;
      case 'alpha':
        alpha = (json['alpha'] as num?)?.toInt();
      case 'antialias':
        antialias = (json['antialias'] as num?)?.toInt();
      case 'background':
        background = VColor.read(json['background']);
      case 'backgroundPattern':
        backgroundPattern = json['backgroundPattern'] == null
            ? null
            : VPattern.fromJson(
                json['backgroundPattern'] as Map<String, dynamic>,
              );
      case 'clipping':
        clipping = json['clipping'] == null
            ? null
            : VRectangle.fromJson(json['clipping'] as Map<String, dynamic>);
      case 'clippingPath':
        clippingPath = json['clippingPath'] == null
            ? null
            : VPathData.fromJson(json['clippingPath'] as Map<String, dynamic>);
      case 'clippingRects':
        clippingRects = (json['clippingRects'] as List<dynamic>?)
            ?.map((e) => (e as num).toInt())
            .toList();
      case 'fillRule':
        fillRule = (json['fillRule'] as num?)?.toInt();
      case 'font':
        font = json['font'] == null
            ? null
            : VFont.fromJson(json['font'] as Map<String, dynamic>);
      case 'foreground':
        foreground = VColor.read(json['foreground']);
      case 'foregroundPattern':
        foregroundPattern = json['foregroundPattern'] == null
            ? null
            : VPattern.fromJson(
                json['foregroundPattern'] as Map<String, dynamic>,
              );
      case 'interpolation':
        interpolation = (json['interpolation'] as num?)?.toInt();
      case 'lineCap':
        lineCap = (json['lineCap'] as num?)?.toInt();
      case 'lineDash':
        lineDash = (json['lineDash'] as List<dynamic>?)
            ?.map((e) => (e as num).toInt())
            .toList();
      case 'lineJoin':
        lineJoin = (json['lineJoin'] as num?)?.toInt();
      case 'lineStyle':
        lineStyle = (json['lineStyle'] as num?)?.toInt();
      case 'lineWidth':
        lineWidth = (json['lineWidth'] as num?)?.toInt();
      case 'transform':
        transform = json['transform'] == null
            ? null
            : VTransform.fromJson(json['transform'] as Map<String, dynamic>);
      case 'clippingText':
        clippingText = json['clippingText'] == null
            ? null
            : VPath.fromJson(json['clippingText'] as Map<String, dynamic>);
      case 'lineDashOffset':
        lineDashOffset = (json['lineDashOffset'] as num?)?.toDouble();
      case 'bufferScale':
        bufferScale = (json['bufferScale'] as num?)?.toDouble();
      default:
        super.readProperty(key, json);
    }
  }

  factory VGC.fromJson(Map<String, dynamic> json) =>
      _$VGCFromJson(json)..isReference = json.containsKey('_r');
  Map<String, dynamic> toJson() => _$VGCToJson(this);
}

@JsonSerializable()
class VGCCopyAreaImageintint {
  VImage? image;
  int x;
  int y;

  VGCCopyAreaImageintint({this.x = 0, this.y = 0});

  factory VGCCopyAreaImageintint.fromJson(Map<String, dynamic> json) =>
      _$VGCCopyAreaImageintintFromJson(json);
  Map<String, dynamic> toJson() => _$VGCCopyAreaImageintintToJson(this);

  /// The op as Java sends it, its arguments in order, or spelled out by name.
  factory VGCCopyAreaImageintint.fromWire(Object? json) {
    if (json is! List) {
      return VGCCopyAreaImageintint.fromJson(json as Map<String, dynamic>);
    }
    return VGCCopyAreaImageintint()
      ..image = json[0] == null
          ? null
          : VImage.fromJson(json[0] as Map<String, dynamic>)
      ..x = (json[1] as num).toInt()
      ..y = (json[2] as num).toInt();
  }
}

@JsonSerializable()
class VGCCopyAreaintintintintintintboolean {
  int srcX;
  int srcY;
  int width;
  int height;
  int destX;
  int destY;
  bool paint;

  VGCCopyAreaintintintintintintboolean({
    this.srcX = 0,
    this.srcY = 0,
    this.width = 0,
    this.height = 0,
    this.destX = 0,
    this.destY = 0,
    this.paint = false,
  });

  factory VGCCopyAreaintintintintintintboolean.fromJson(
    Map<String, dynamic> json,
  ) => _$VGCCopyAreaintintintintintintbooleanFromJson(json);
  Map<String, dynamic> toJson() =>
      _$VGCCopyAreaintintintintintintbooleanToJson(this);

  /// The op as Java sends it, its arguments in order, or spelled out by name.
  factory VGCCopyAreaintintintintintintboolean.fromWire(Object? json) {
    if (json is! List) {
      return VGCCopyAreaintintintintintintboolean.fromJson(
        json as Map<String, dynamic>,
      );
    }
    return VGCCopyAreaintintintintintintboolean()
      ..srcX = (json[0] as num).toInt()
      ..srcY = (json[1] as num).toInt()
      ..width = (json[2] as num).toInt()
      ..height = (json[3] as num).toInt()
      ..destX = (json[4] as num).toInt()
      ..destY = (json[5] as num).toInt()
      ..paint = json[6] as bool;
  }
}

@JsonSerializable()
class VGCDrawArcintintintintintint {
  int x;
  int y;
  int width;
  int height;
  int startAngle;
  int arcAngle;

  VGCDrawArcintintintintintint({
    this.x = 0,
    this.y = 0,
    this.width = 0,
    this.height = 0,
    this.startAngle = 0,
    this.arcAngle = 0,
  });

  factory VGCDrawArcintintintintintint.fromJson(Map<String, dynamic> json) =>
      _$VGCDrawArcintintintintintintFromJson(json);
  Map<String, dynamic> toJson() => _$VGCDrawArcintintintintintintToJson(this);

  /// The op as Java sends it, its arguments in order, or spelled out by name.
  factory VGCDrawArcintintintintintint.fromWire(Object? json) {
    if (json is! List) {
      return VGCDrawArcintintintintintint.fromJson(
        json as Map<String, dynamic>,
      );
    }
    return VGCDrawArcintintintintintint()
      ..x = (json[0] as num).toInt()
      ..y = (json[1] as num).toInt()
      ..width = (json[2] as num).toInt()
      ..height = (json[3] as num).toInt()
      ..startAngle = (json[4] as num).toInt()
      ..arcAngle = (json[5] as num).toInt();
  }
}

@JsonSerializable()
class VGCDrawFocusintintintint {
  int x;
  int y;
  int width;
  int height;

  VGCDrawFocusintintintint({
    this.x = 0,
    this.y = 0,
    this.width = 0,
    this.height = 0,
  });

  factory VGCDrawFocusintintintint.fromJson(Map<String, dynamic> json) =>
      _$VGCDrawFocusintintintintFromJson(json);
  Map<String, dynamic> toJson() => _$VGCDrawFocusintintintintToJson(this);

  /// The op as Java sends it, its arguments in order, or spelled out by name.
  factory VGCDrawFocusintintintint.fromWire(Object? json) {
    if (json is! List) {
      return VGCDrawFocusintintintint.fromJson(json as Map<String, dynamic>);
    }
    return VGCDrawFocusintintintint()
      ..x = (json[0] as num).toInt()
      ..y = (json[1] as num).toInt()
      ..width = (json[2] as num).toInt()
      ..height = (json[3] as num).toInt();
  }
}

@JsonSerializable()
class VGCDrawImageImageintint {
  VImage? image;
  int x;
  int y;

  VGCDrawImageImageintint({this.x = 0, this.y = 0});

  factory VGCDrawImageImageintint.fromJson(Map<String, dynamic> json) =>
      _$VGCDrawImageImageintintFromJson(json);
  Map<String, dynamic> toJson() => _$VGCDrawImageImageintintToJson(this);

  /// The op as Java sends it, its arguments in order, or spelled out by name.
  factory VGCDrawImageImageintint.fromWire(Object? json) {
    if (json is! List) {
      return VGCDrawImageImageintint.fromJson(json as Map<String, dynamic>);
    }
    return VGCDrawImageImageintint()
      ..image = json[0] == null
          ? null
          : VImage.fromJson(json[0] as Map<String, dynamic>)
      ..x = (json[1] as num).toInt()
      ..y = (json[2] as num).toInt();
  }
}

@JsonSerializable()
class VGCDrawImageImageintintintint {
  VImage? image;
  int destX;
  int destY;
  int destWidth;
  int destHeight;

  VGCDrawImageImageintintintint({
    this.destX = 0,
    this.destY = 0,
    this.destWidth = 0,
    this.destHeight = 0,
  });

  factory VGCDrawImageImageintintintint.fromJson(Map<String, dynamic> json) =>
      _$VGCDrawImageImageintintintintFromJson(json);
  Map<String, dynamic> toJson() => _$VGCDrawImageImageintintintintToJson(this);

  /// The op as Java sends it, its arguments in order, or spelled out by name.
  factory VGCDrawImageImageintintintint.fromWire(Object? json) {
    if (json is! List) {
      return VGCDrawImageImageintintintint.fromJson(
        json as Map<String, dynamic>,
      );
    }
    return VGCDrawImageImageintintintint()
      ..image = json[0] == null
          ? null
          : VImage.fromJson(json[0] as Map<String, dynamic>)
      ..destX = (json[1] as num).toInt()
      ..destY = (json[2] as num).toInt()
      ..destWidth = (json[3] as num).toInt()
      ..destHeight = (json[4] as num).toInt();
  }
}

@JsonSerializable()
class VGCDrawImageImageintintintintintintintint {
  VImage? image;
  int srcX;
  int srcY;
  int srcWidth;
  int srcHeight;
  int destX;
  int destY;
  int destWidth;
  int destHeight;

  VGCDrawImageImageintintintintintintintint({
    this.srcX = 0,
    this.srcY = 0,
    this.srcWidth = 0,
    this.srcHeight = 0,
    this.destX = 0,
    this.destY = 0,
    this.destWidth = 0,
    this.destHeight = 0,
  });

  factory VGCDrawImageImageintintintintintintintint.fromJson(
    Map<String, dynamic> json,
  ) => _$VGCDrawImageImageintintintintintintintintFromJson(json);
  Map<String, dynamic> toJson() =>
      _$VGCDrawImageImageintintintintintintintintToJson(this);

  /// The op as Java sends it, its arguments in order, or spelled out by name.
  factory VGCDrawImageImageintintintintintintintint.fromWire(Object? json) {
    if (json is! List) {
      return VGCDrawImageImageintintintintintintintint.fromJson(
        json as Map<String, dynamic>,
      );
    }
    return VGCDrawImageImageintintintintintintintint()
      ..image = json[0] == null
          ? null
          : VImage.fromJson(json[0] as Map<String, dynamic>)
      ..srcX = (json[1] as num).toInt()
      ..srcY = (json[2] as num).toInt()
      ..srcWidth = (json[3] as num).toInt()
      ..srcHeight = (json[4] as num).toInt()
      ..destX = (json[5] as num).toInt()
      ..destY = (json[6] as num).toInt()
      ..destWidth = (json[7] as num).toInt()
      ..destHeight = (json[8] as num).toInt();
  }
}

@JsonSerializable()
class VGCDrawLineintintintint {
  int x1;
  int y1;
  int x2;
  int y2;

  VGCDrawLineintintintint({this.x1 = 0, this.y1 = 0, this.x2 = 0, this.y2 = 0});

  factory VGCDrawLineintintintint.fromJson(Map<String, dynamic> json) =>
      _$VGCDrawLineintintintintFromJson(json);
  Map<String, dynamic> toJson() => _$VGCDrawLineintintintintToJson(this);

  /// The op as Java sends it, its arguments in order, or spelled out by name.
  factory VGCDrawLineintintintint.fromWire(Object? json) {
    if (json is! List) {
      return VGCDrawLineintintintint.fromJson(json as Map<String, dynamic>);
    }
    return VGCDrawLineintintintint()
      ..x1 = (json[0] as num).toInt()
      ..y1 = (json[1] as num).toInt()
      ..x2 = (json[2] as num).toInt()
      ..y2 = (json[3] as num).toInt();
  }
}

@JsonSerializable()
class VGCDrawOvalintintintint {
  int x;
  int y;
  int width;
  int height;

  VGCDrawOvalintintintint({
    this.x = 0,
    this.y = 0,
    this.width = 0,
    this.height = 0,
  });

  factory VGCDrawOvalintintintint.fromJson(Map<String, dynamic> json) =>
      _$VGCDrawOvalintintintintFromJson(json);
  Map<String, dynamic> toJson() => _$VGCDrawOvalintintintintToJson(this);

  /// The op as Java sends it, its arguments in order, or spelled out by name.
  factory VGCDrawOvalintintintint.fromWire(Object? json) {
    if (json is! List) {
      return VGCDrawOvalintintintint.fromJson(json as Map<String, dynamic>);
    }
    return VGCDrawOvalintintintint()
      ..x = (json[0] as num).toInt()
      ..y = (json[1] as num).toInt()
      ..width = (json[2] as num).toInt()
      ..height = (json[3] as num).toInt();
  }
}

@JsonSerializable()
class VGCDrawPathPath {
  VPath? path;

  VGCDrawPathPath();

  factory VGCDrawPathPath.fromJson(Map<String, dynamic> json) =>
      _$VGCDrawPathPathFromJson(json);
  Map<String, dynamic> toJson() => _$VGCDrawPathPathToJson(this);

  /// The op as Java sends it, its arguments in order, or spelled out by name.
  factory VGCDrawPathPath.fromWire(Object? json) {
    if (json is! List) {
      return VGCDrawPathPath.fromJson(json as Map<String, dynamic>);
    }
    return VGCDrawPathPath()
      ..path = json[0] == null
          ? null
          : VPath.fromJson(json[0] as Map<String, dynamic>);
  }
}

@JsonSerializable()
class VGCDrawPointintint {
  int x;
  int y;

  VGCDrawPointintint({this.x = 0, this.y = 0});

  factory VGCDrawPointintint.fromJson(Map<String, dynamic> json) =>
      _$VGCDrawPointintintFromJson(json);
  Map<String, dynamic> toJson() => _$VGCDrawPointintintToJson(this);

  /// The op as Java sends it, its arguments in order, or spelled out by name.
  factory VGCDrawPointintint.fromWire(Object? json) {
    if (json is! List) {
      return VGCDrawPointintint.fromJson(json as Map<String, dynamic>);
    }
    return VGCDrawPointintint()
      ..x = (json[0] as num).toInt()
      ..y = (json[1] as num).toInt();
  }
}

@JsonSerializable()
class VGCDrawPolygonint {
  List<int>? pointArray;

  VGCDrawPolygonint();

  factory VGCDrawPolygonint.fromJson(Map<String, dynamic> json) =>
      _$VGCDrawPolygonintFromJson(json);
  Map<String, dynamic> toJson() => _$VGCDrawPolygonintToJson(this);

  /// The op as Java sends it, its arguments in order, or spelled out by name.
  factory VGCDrawPolygonint.fromWire(Object? json) {
    if (json is! List) {
      return VGCDrawPolygonint.fromJson(json as Map<String, dynamic>);
    }
    return VGCDrawPolygonint()
      ..pointArray = (json[0] as List<dynamic>?)
          ?.map((e) => (e as num).toInt())
          .toList();
  }
}

@JsonSerializable()
class VGCDrawPolylineint {
  List<int>? pointArray;

  VGCDrawPolylineint();

  factory VGCDrawPolylineint.fromJson(Map<String, dynamic> json) =>
      _$VGCDrawPolylineintFromJson(json);
  Map<String, dynamic> toJson() => _$VGCDrawPolylineintToJson(this);

  /// The op as Java sends it, its arguments in order, or spelled out by name.
  factory VGCDrawPolylineint.fromWire(Object? json) {
    if (json is! List) {
      return VGCDrawPolylineint.fromJson(json as Map<String, dynamic>);
    }
    return VGCDrawPolylineint()
      ..pointArray = (json[0] as List<dynamic>?)
          ?.map((e) => (e as num).toInt())
          .toList();
  }
}

@JsonSerializable()
class VGCDrawRectangleintintintint {
  int x;
  int y;
  int width;
  int height;

  VGCDrawRectangleintintintint({
    this.x = 0,
    this.y = 0,
    this.width = 0,
    this.height = 0,
  });

  factory VGCDrawRectangleintintintint.fromJson(Map<String, dynamic> json) =>
      _$VGCDrawRectangleintintintintFromJson(json);
  Map<String, dynamic> toJson() => _$VGCDrawRectangleintintintintToJson(this);

  /// The op as Java sends it, its arguments in order, or spelled out by name.
  factory VGCDrawRectangleintintintint.fromWire(Object? json) {
    if (json is! List) {
      return VGCDrawRectangleintintintint.fromJson(
        json as Map<String, dynamic>,
      );
    }
    return VGCDrawRectangleintintintint()
      ..x = (json[0] as num).toInt()
      ..y = (json[1] as num).toInt()
      ..width = (json[2] as num).toInt()
      ..height = (json[3] as num).toInt();
  }
}

@JsonSerializable()
class VGCDrawRoundRectangleintintintintintint {
  int x;
  int y;
  int width;
  int height;
  int arcWidth;
  int arcHeight;

  VGCDrawRoundRectangleintintintintintint({
    this.x = 0,
    this.y = 0,
    this.width = 0,
    this.height = 0,
    this.arcWidth = 0,
    this.arcHeight = 0,
  });

  factory VGCDrawRoundRectangleintintintintintint.fromJson(
    Map<String, dynamic> json,
  ) => _$VGCDrawRoundRectangleintintintintintintFromJson(json);
  Map<String, dynamic> toJson() =>
      _$VGCDrawRoundRectangleintintintintintintToJson(this);

  /// The op as Java sends it, its arguments in order, or spelled out by name.
  factory VGCDrawRoundRectangleintintintintintint.fromWire(Object? json) {
    if (json is! List) {
      return VGCDrawRoundRectangleintintintintintint.fromJson(
        json as Map<String, dynamic>,
      );
    }
    return VGCDrawRoundRectangleintintintintintint()
      ..x = (json[0] as num).toInt()
      ..y = (json[1] as num).toInt()
      ..width = (json[2] as num).toInt()
      ..height = (json[3] as num).toInt()
      ..arcWidth = (json[4] as num).toInt()
      ..arcHeight = (json[5] as num).toInt();
  }
}

@JsonSerializable()
class VGCDrawTextStringintintint {
  String string;
  int x;
  int y;
  int flags;

  VGCDrawTextStringintintint({
    this.string = '',
    this.x = 0,
    this.y = 0,
    this.flags = 0,
  });

  factory VGCDrawTextStringintintint.fromJson(Map<String, dynamic> json) =>
      _$VGCDrawTextStringintintintFromJson(json);
  Map<String, dynamic> toJson() => _$VGCDrawTextStringintintintToJson(this);

  /// The op as Java sends it, its arguments in order, or spelled out by name.
  factory VGCDrawTextStringintintint.fromWire(Object? json) {
    if (json is! List) {
      return VGCDrawTextStringintintint.fromJson(json as Map<String, dynamic>);
    }
    return VGCDrawTextStringintintint()
      ..string = json[0] as String
      ..x = (json[1] as num).toInt()
      ..y = (json[2] as num).toInt()
      ..flags = (json[3] as num).toInt();
  }
}

@JsonSerializable()
class VGCFillArcintintintintintint {
  int x;
  int y;
  int width;
  int height;
  int startAngle;
  int arcAngle;

  VGCFillArcintintintintintint({
    this.x = 0,
    this.y = 0,
    this.width = 0,
    this.height = 0,
    this.startAngle = 0,
    this.arcAngle = 0,
  });

  factory VGCFillArcintintintintintint.fromJson(Map<String, dynamic> json) =>
      _$VGCFillArcintintintintintintFromJson(json);
  Map<String, dynamic> toJson() => _$VGCFillArcintintintintintintToJson(this);

  /// The op as Java sends it, its arguments in order, or spelled out by name.
  factory VGCFillArcintintintintintint.fromWire(Object? json) {
    if (json is! List) {
      return VGCFillArcintintintintintint.fromJson(
        json as Map<String, dynamic>,
      );
    }
    return VGCFillArcintintintintintint()
      ..x = (json[0] as num).toInt()
      ..y = (json[1] as num).toInt()
      ..width = (json[2] as num).toInt()
      ..height = (json[3] as num).toInt()
      ..startAngle = (json[4] as num).toInt()
      ..arcAngle = (json[5] as num).toInt();
  }
}

@JsonSerializable()
class VGCFillGradientRectangleintintintintboolean {
  int x;
  int y;
  int width;
  int height;
  bool vertical;

  VGCFillGradientRectangleintintintintboolean({
    this.x = 0,
    this.y = 0,
    this.width = 0,
    this.height = 0,
    this.vertical = false,
  });

  factory VGCFillGradientRectangleintintintintboolean.fromJson(
    Map<String, dynamic> json,
  ) => _$VGCFillGradientRectangleintintintintbooleanFromJson(json);
  Map<String, dynamic> toJson() =>
      _$VGCFillGradientRectangleintintintintbooleanToJson(this);

  /// The op as Java sends it, its arguments in order, or spelled out by name.
  factory VGCFillGradientRectangleintintintintboolean.fromWire(Object? json) {
    if (json is! List) {
      return VGCFillGradientRectangleintintintintboolean.fromJson(
        json as Map<String, dynamic>,
      );
    }
    return VGCFillGradientRectangleintintintintboolean()
      ..x = (json[0] as num).toInt()
      ..y = (json[1] as num).toInt()
      ..width = (json[2] as num).toInt()
      ..height = (json[3] as num).toInt()
      ..vertical = json[4] as bool;
  }
}

@JsonSerializable()
class VGCFillOvalintintintint {
  int x;
  int y;
  int width;
  int height;

  VGCFillOvalintintintint({
    this.x = 0,
    this.y = 0,
    this.width = 0,
    this.height = 0,
  });

  factory VGCFillOvalintintintint.fromJson(Map<String, dynamic> json) =>
      _$VGCFillOvalintintintintFromJson(json);
  Map<String, dynamic> toJson() => _$VGCFillOvalintintintintToJson(this);

  /// The op as Java sends it, its arguments in order, or spelled out by name.
  factory VGCFillOvalintintintint.fromWire(Object? json) {
    if (json is! List) {
      return VGCFillOvalintintintint.fromJson(json as Map<String, dynamic>);
    }
    return VGCFillOvalintintintint()
      ..x = (json[0] as num).toInt()
      ..y = (json[1] as num).toInt()
      ..width = (json[2] as num).toInt()
      ..height = (json[3] as num).toInt();
  }
}

@JsonSerializable()
class VGCFillPathPath {
  VPath? path;

  VGCFillPathPath();

  factory VGCFillPathPath.fromJson(Map<String, dynamic> json) =>
      _$VGCFillPathPathFromJson(json);
  Map<String, dynamic> toJson() => _$VGCFillPathPathToJson(this);

  /// The op as Java sends it, its arguments in order, or spelled out by name.
  factory VGCFillPathPath.fromWire(Object? json) {
    if (json is! List) {
      return VGCFillPathPath.fromJson(json as Map<String, dynamic>);
    }
    return VGCFillPathPath()
      ..path = json[0] == null
          ? null
          : VPath.fromJson(json[0] as Map<String, dynamic>);
  }
}

@JsonSerializable()
class VGCFillPolygonint {
  List<int>? pointArray;

  VGCFillPolygonint();

  factory VGCFillPolygonint.fromJson(Map<String, dynamic> json) =>
      _$VGCFillPolygonintFromJson(json);
  Map<String, dynamic> toJson() => _$VGCFillPolygonintToJson(this);

  /// The op as Java sends it, its arguments in order, or spelled out by name.
  factory VGCFillPolygonint.fromWire(Object? json) {
    if (json is! List) {
      return VGCFillPolygonint.fromJson(json as Map<String, dynamic>);
    }
    return VGCFillPolygonint()
      ..pointArray = (json[0] as List<dynamic>?)
          ?.map((e) => (e as num).toInt())
          .toList();
  }
}

@JsonSerializable()
class VGCFillRectangleintintintint {
  int x;
  int y;
  int width;
  int height;

  VGCFillRectangleintintintint({
    this.x = 0,
    this.y = 0,
    this.width = 0,
    this.height = 0,
  });

  factory VGCFillRectangleintintintint.fromJson(Map<String, dynamic> json) =>
      _$VGCFillRectangleintintintintFromJson(json);
  Map<String, dynamic> toJson() => _$VGCFillRectangleintintintintToJson(this);

  /// The op as Java sends it, its arguments in order, or spelled out by name.
  factory VGCFillRectangleintintintint.fromWire(Object? json) {
    if (json is! List) {
      return VGCFillRectangleintintintint.fromJson(
        json as Map<String, dynamic>,
      );
    }
    return VGCFillRectangleintintintint()
      ..x = (json[0] as num).toInt()
      ..y = (json[1] as num).toInt()
      ..width = (json[2] as num).toInt()
      ..height = (json[3] as num).toInt();
  }
}

@JsonSerializable()
class VGCFillRoundRectangleintintintintintint {
  int x;
  int y;
  int width;
  int height;
  int arcWidth;
  int arcHeight;

  VGCFillRoundRectangleintintintintintint({
    this.x = 0,
    this.y = 0,
    this.width = 0,
    this.height = 0,
    this.arcWidth = 0,
    this.arcHeight = 0,
  });

  factory VGCFillRoundRectangleintintintintintint.fromJson(
    Map<String, dynamic> json,
  ) => _$VGCFillRoundRectangleintintintintintintFromJson(json);
  Map<String, dynamic> toJson() =>
      _$VGCFillRoundRectangleintintintintintintToJson(this);

  /// The op as Java sends it, its arguments in order, or spelled out by name.
  factory VGCFillRoundRectangleintintintintintint.fromWire(Object? json) {
    if (json is! List) {
      return VGCFillRoundRectangleintintintintintint.fromJson(
        json as Map<String, dynamic>,
      );
    }
    return VGCFillRoundRectangleintintintintintint()
      ..x = (json[0] as num).toInt()
      ..y = (json[1] as num).toInt()
      ..width = (json[2] as num).toInt()
      ..height = (json[3] as num).toInt()
      ..arcWidth = (json[4] as num).toInt()
      ..arcHeight = (json[5] as num).toInt();
  }
}
