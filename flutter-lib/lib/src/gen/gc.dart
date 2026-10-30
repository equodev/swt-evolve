import 'dart:convert';
import 'package:flutter/widgets.dart';
import '../comm/gc_wire.dart';
import '../comm/widget_wire.dart';
import '../gen/color.dart';
import '../gen/font.dart';
import '../gen/image.dart';
import '../gen/path.dart';
import '../gen/pathdata.dart';
import '../gen/pattern.dart';
import '../gen/rectangle.dart';
import '../gen/rgb.dart';
import '../gen/transform.dart';
import '../gen/widget.dart';
import '../gen/widgets.dart';
import '../impl/gc_evolve.dart';
import 'widget.dart';
import 'widgets.dart';

class GCSwt<V extends VGC> extends WidgetSwt<V> {
  const GCSwt({super.key, required super.value});

  @override
  State createState() => GCImpl<GCSwt<VGC>, VGC>();
}

abstract class GCState<T extends GCSwt, V extends VGC>
    extends WidgetSwtState<T, V> {}

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
  bool? themedCanvas;
  List<VRGB?>? hoverFills;
  List<VRGB?>? selectionFills;

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
      themedCanvas = other.themedCanvas;
      hoverFills = other.hoverFills;
      selectionFills = other.selectionFills;
    }
  }

  @override
  bool readWire(int key, WidgetWireReader r) {
    switch (key) {
      case 145: // XORMode
        XORMode = r.boolean();
        return true;
      case 146: // alpha
        alpha = r.intBoxed();
        return true;
      case 147: // antialias
        antialias = r.int32();
        return true;
      case 148: // background
        background = r.color();
        return true;
      case 560: // backgroundPattern
        backgroundPattern = r.object(VPattern.new);
        return true;
      case 150: // clipping
        clipping = r.rect();
        return true;
      case 151: // clippingPath
        clippingPath = r.object(VPathData.new);
        return true;
      case 152: // clippingRects
        clippingRects = r.ints();
        return true;
      case 153: // fillRule
        fillRule = r.intBoxed();
        return true;
      case 154: // font
        font = r.object(VFont.new);
        return true;
      case 155: // foreground
        foreground = r.color();
        return true;
      case 561: // foregroundPattern
        foregroundPattern = r.object(VPattern.new);
        return true;
      case 156: // interpolation
        interpolation = r.int32();
        return true;
      case 157: // lineCap
        lineCap = r.intBoxed();
        return true;
      case 158: // lineDash
        lineDash = r.ints();
        return true;
      case 160: // lineJoin
        lineJoin = r.intBoxed();
        return true;
      case 161: // lineStyle
        lineStyle = r.intBoxed();
        return true;
      case 162: // lineWidth
        lineWidth = r.int32();
        return true;
      case 562: // transform
        transform = r.object(VTransform.new);
        return true;
      case 44: // clippingText
        clippingText = r.object(VPath.new);
        return true;
      case 159: // lineDashOffset
        lineDashOffset = r.float32();
        return true;
      case 149: // bufferScale
        bufferScale = r.float32();
        return true;
      case 1356: // themedCanvas
        themedCanvas = r.boolBoxed();
        return true;
      case 1372: // hoverFills
        hoverFills = r.list(() => r.rgb());
        return true;
      case 1373: // selectionFills
        selectionFills = r.list(() => r.rgb());
        return true;
      case 163: // style
        style = r.int32();
        return true;
      default:
        return super.readWire(key, r);
    }
  }

  @override
  void writeWire(WidgetWireWriter w) {
    super.writeWire(w);
    {
      final v_ = XORMode;
      if (v_ != null && w.wants(145)) {
        w.key(145);
        w.boolean(v_);
      }
    }
    {
      final v_ = alpha;
      if (v_ != null && w.wants(146)) {
        w.key(146);
        w.intBoxed(v_);
      }
    }
    {
      final v_ = antialias;
      if (v_ != null && w.wants(147)) {
        w.key(147);
        w.int32(v_);
      }
    }
    {
      final v_ = background;
      if (v_ != null && w.wants(148)) {
        w.key(148);
        w.colorValue(v_);
      }
    }
    {
      final v_ = backgroundPattern;
      if (v_ != null && w.wants(560)) {
        w.key(560);
        w.object(v_.writeWire);
      }
    }
    {
      final v_ = clipping;
      if (v_ != null && w.wants(150)) {
        w.key(150);
        w.rectValue(v_);
      }
    }
    {
      final v_ = clippingPath;
      if (v_ != null && w.wants(151)) {
        w.key(151);
        w.object(v_.writeWire);
      }
    }
    {
      final v_ = clippingRects;
      if (v_ != null && w.wants(152)) {
        w.key(152);
        w.ints(v_);
      }
    }
    {
      final v_ = fillRule;
      if (v_ != null && w.wants(153)) {
        w.key(153);
        w.intBoxed(v_);
      }
    }
    {
      final v_ = font;
      if (v_ != null && w.wants(154)) {
        w.key(154);
        w.object(v_.writeWire);
      }
    }
    {
      final v_ = foreground;
      if (v_ != null && w.wants(155)) {
        w.key(155);
        w.colorValue(v_);
      }
    }
    {
      final v_ = foregroundPattern;
      if (v_ != null && w.wants(561)) {
        w.key(561);
        w.object(v_.writeWire);
      }
    }
    {
      final v_ = interpolation;
      if (v_ != null && w.wants(156)) {
        w.key(156);
        w.int32(v_);
      }
    }
    {
      final v_ = lineCap;
      if (v_ != null && w.wants(157)) {
        w.key(157);
        w.intBoxed(v_);
      }
    }
    {
      final v_ = lineDash;
      if (v_ != null && w.wants(158)) {
        w.key(158);
        w.ints(v_);
      }
    }
    {
      final v_ = lineJoin;
      if (v_ != null && w.wants(160)) {
        w.key(160);
        w.intBoxed(v_);
      }
    }
    {
      final v_ = lineStyle;
      if (v_ != null && w.wants(161)) {
        w.key(161);
        w.intBoxed(v_);
      }
    }
    {
      final v_ = lineWidth;
      if (v_ != null && w.wants(162)) {
        w.key(162);
        w.int32(v_);
      }
    }
    {
      final v_ = transform;
      if (v_ != null && w.wants(562)) {
        w.key(562);
        w.object(v_.writeWire);
      }
    }
    {
      final v_ = clippingText;
      if (v_ != null && w.wants(44)) {
        w.key(44);
        w.object(v_.writeWire);
      }
    }
    {
      final v_ = lineDashOffset;
      if (v_ != null && w.wants(159)) {
        w.key(159);
        w.float32(v_.toDouble());
      }
    }
    {
      final v_ = bufferScale;
      if (v_ != null && w.wants(149)) {
        w.key(149);
        w.float32(v_.toDouble());
      }
    }
    {
      final v_ = themedCanvas;
      if (v_ != null && w.wants(1356)) {
        w.key(1356);
        w.boolBoxed(v_);
      }
    }
    {
      final v_ = hoverFills;
      if (v_ != null && w.wants(1372)) {
        w.key(1372);
        w.list(v_, (e) => w.rgbValue(e));
      }
    }
    {
      final v_ = selectionFills;
      if (v_ != null && w.wants(1373)) {
        w.key(1373);
        w.list(v_, (e) => w.rgbValue(e));
      }
    }
    if (w.wants(163)) {
      w.key(163);
      w.int32(style);
    }
  }

  @override
  void visitWire(void Function(int key, Object? value) visit) {
    super.visitWire(visit);
    visit(145, XORMode);
    visit(146, alpha);
    visit(147, antialias);
    visit(148, background);
    visit(560, backgroundPattern);
    visit(150, clipping);
    visit(151, clippingPath);
    visit(152, clippingRects);
    visit(153, fillRule);
    visit(154, font);
    visit(155, foreground);
    visit(561, foregroundPattern);
    visit(156, interpolation);
    visit(157, lineCap);
    visit(158, lineDash);
    visit(160, lineJoin);
    visit(161, lineStyle);
    visit(162, lineWidth);
    visit(562, transform);
    visit(44, clippingText);
    visit(159, lineDashOffset);
    visit(149, bufferScale);
    visit(1356, themedCanvas);
    visit(1372, hoverFills);
    visit(1373, selectionFills);
    visit(163, style);
  }
}

class VGCCopyAreaImageintint {
  VImage? image;
  int x;
  int y;

  VGCCopyAreaImageintint({this.x = 0, this.y = 0});

  /// The op as a GC's binary frame carries it; see GcWireReader.
  factory VGCCopyAreaImageintint.fromBinary(GcWireReader r) {
    final op = VGCCopyAreaImageintint();
    {
      final ww = r.wire();
      op.image = ww.image();
    }
    op.x = r.i();
    op.y = r.i();
    return op;
  }
}

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

  /// The op as a GC's binary frame carries it; see GcWireReader.
  factory VGCCopyAreaintintintintintintboolean.fromBinary(GcWireReader r) {
    final op = VGCCopyAreaintintintintintintboolean();
    op.srcX = r.i();
    op.srcY = r.i();
    op.width = r.i();
    op.height = r.i();
    op.destX = r.i();
    op.destY = r.i();
    op.paint = r.b();
    return op;
  }
}

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

  /// The op as a GC's binary frame carries it; see GcWireReader.
  factory VGCDrawArcintintintintintint.fromBinary(GcWireReader r) {
    final op = VGCDrawArcintintintintintint();
    op.x = r.i();
    op.y = r.i();
    op.width = r.i();
    op.height = r.i();
    op.startAngle = r.i();
    op.arcAngle = r.i();
    return op;
  }
}

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

  /// The op as a GC's binary frame carries it; see GcWireReader.
  factory VGCDrawFocusintintintint.fromBinary(GcWireReader r) {
    final op = VGCDrawFocusintintintint();
    op.x = r.i();
    op.y = r.i();
    op.width = r.i();
    op.height = r.i();
    return op;
  }
}

class VGCDrawImageImageintint {
  VImage? image;
  int x;
  int y;

  VGCDrawImageImageintint({this.x = 0, this.y = 0});

  /// The op as a GC's binary frame carries it; see GcWireReader.
  factory VGCDrawImageImageintint.fromBinary(GcWireReader r) {
    final op = VGCDrawImageImageintint();
    {
      final ww = r.wire();
      op.image = ww.image();
    }
    op.x = r.i();
    op.y = r.i();
    return op;
  }
}

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

  /// The op as a GC's binary frame carries it; see GcWireReader.
  factory VGCDrawImageImageintintintint.fromBinary(GcWireReader r) {
    final op = VGCDrawImageImageintintintint();
    {
      final ww = r.wire();
      op.image = ww.image();
    }
    op.destX = r.i();
    op.destY = r.i();
    op.destWidth = r.i();
    op.destHeight = r.i();
    return op;
  }
}

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

  /// The op as a GC's binary frame carries it; see GcWireReader.
  factory VGCDrawImageImageintintintintintintintint.fromBinary(GcWireReader r) {
    final op = VGCDrawImageImageintintintintintintintint();
    {
      final ww = r.wire();
      op.image = ww.image();
    }
    op.srcX = r.i();
    op.srcY = r.i();
    op.srcWidth = r.i();
    op.srcHeight = r.i();
    op.destX = r.i();
    op.destY = r.i();
    op.destWidth = r.i();
    op.destHeight = r.i();
    return op;
  }
}

class VGCDrawLineintintintint {
  int x1;
  int y1;
  int x2;
  int y2;

  VGCDrawLineintintintint({this.x1 = 0, this.y1 = 0, this.x2 = 0, this.y2 = 0});

  /// The op as a GC's binary frame carries it; see GcWireReader.
  factory VGCDrawLineintintintint.fromBinary(GcWireReader r) {
    final op = VGCDrawLineintintintint();
    op.x1 = r.i();
    op.y1 = r.i();
    op.x2 = r.i();
    op.y2 = r.i();
    return op;
  }
}

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

  /// The op as a GC's binary frame carries it; see GcWireReader.
  factory VGCDrawOvalintintintint.fromBinary(GcWireReader r) {
    final op = VGCDrawOvalintintintint();
    op.x = r.i();
    op.y = r.i();
    op.width = r.i();
    op.height = r.i();
    return op;
  }
}

class VGCDrawPathPath {
  VPath? path;

  VGCDrawPathPath();

  /// The op as a GC's binary frame carries it; see GcWireReader.
  factory VGCDrawPathPath.fromBinary(GcWireReader r) {
    final op = VGCDrawPathPath();
    {
      final ww = r.wire();
      op.path = ww.object(VPath.new);
    }
    return op;
  }
}

class VGCDrawPointintint {
  int x;
  int y;

  VGCDrawPointintint({this.x = 0, this.y = 0});

  /// The op as a GC's binary frame carries it; see GcWireReader.
  factory VGCDrawPointintint.fromBinary(GcWireReader r) {
    final op = VGCDrawPointintint();
    op.x = r.i();
    op.y = r.i();
    return op;
  }
}

class VGCDrawPolygonint {
  List<int>? pointArray;

  VGCDrawPolygonint();

  /// The op as a GC's binary frame carries it; see GcWireReader.
  factory VGCDrawPolygonint.fromBinary(GcWireReader r) {
    final op = VGCDrawPolygonint();
    op.pointArray = r.ints();
    return op;
  }
}

class VGCDrawPolylineint {
  List<int>? pointArray;

  VGCDrawPolylineint();

  /// The op as a GC's binary frame carries it; see GcWireReader.
  factory VGCDrawPolylineint.fromBinary(GcWireReader r) {
    final op = VGCDrawPolylineint();
    op.pointArray = r.ints();
    return op;
  }
}

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

  /// The op as a GC's binary frame carries it; see GcWireReader.
  factory VGCDrawRectangleintintintint.fromBinary(GcWireReader r) {
    final op = VGCDrawRectangleintintintint();
    op.x = r.i();
    op.y = r.i();
    op.width = r.i();
    op.height = r.i();
    return op;
  }
}

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

  /// The op as a GC's binary frame carries it; see GcWireReader.
  factory VGCDrawRoundRectangleintintintintintint.fromBinary(GcWireReader r) {
    final op = VGCDrawRoundRectangleintintintintintint();
    op.x = r.i();
    op.y = r.i();
    op.width = r.i();
    op.height = r.i();
    op.arcWidth = r.i();
    op.arcHeight = r.i();
    return op;
  }
}

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

  /// The op as a GC's binary frame carries it; see GcWireReader.
  factory VGCDrawTextStringintintint.fromBinary(GcWireReader r) {
    final op = VGCDrawTextStringintintint();
    op.string = r.string() ?? '';
    op.x = r.i();
    op.y = r.i();
    op.flags = r.i();
    return op;
  }
}

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

  /// The op as a GC's binary frame carries it; see GcWireReader.
  factory VGCFillArcintintintintintint.fromBinary(GcWireReader r) {
    final op = VGCFillArcintintintintintint();
    op.x = r.i();
    op.y = r.i();
    op.width = r.i();
    op.height = r.i();
    op.startAngle = r.i();
    op.arcAngle = r.i();
    return op;
  }
}

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

  /// The op as a GC's binary frame carries it; see GcWireReader.
  factory VGCFillGradientRectangleintintintintboolean.fromBinary(
    GcWireReader r,
  ) {
    final op = VGCFillGradientRectangleintintintintboolean();
    op.x = r.i();
    op.y = r.i();
    op.width = r.i();
    op.height = r.i();
    op.vertical = r.b();
    return op;
  }
}

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

  /// The op as a GC's binary frame carries it; see GcWireReader.
  factory VGCFillOvalintintintint.fromBinary(GcWireReader r) {
    final op = VGCFillOvalintintintint();
    op.x = r.i();
    op.y = r.i();
    op.width = r.i();
    op.height = r.i();
    return op;
  }
}

class VGCFillPathPath {
  VPath? path;

  VGCFillPathPath();

  /// The op as a GC's binary frame carries it; see GcWireReader.
  factory VGCFillPathPath.fromBinary(GcWireReader r) {
    final op = VGCFillPathPath();
    {
      final ww = r.wire();
      op.path = ww.object(VPath.new);
    }
    return op;
  }
}

class VGCFillPolygonint {
  List<int>? pointArray;

  VGCFillPolygonint();

  /// The op as a GC's binary frame carries it; see GcWireReader.
  factory VGCFillPolygonint.fromBinary(GcWireReader r) {
    final op = VGCFillPolygonint();
    op.pointArray = r.ints();
    return op;
  }
}

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

  /// The op as a GC's binary frame carries it; see GcWireReader.
  factory VGCFillRectangleintintintint.fromBinary(GcWireReader r) {
    final op = VGCFillRectangleintintintint();
    op.x = r.i();
    op.y = r.i();
    op.width = r.i();
    op.height = r.i();
    return op;
  }
}

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

  /// The op as a GC's binary frame carries it; see GcWireReader.
  factory VGCFillRoundRectangleintintintintintint.fromBinary(GcWireReader r) {
    final op = VGCFillRoundRectangleintintintintintint();
    op.x = r.i();
    op.y = r.i();
    op.width = r.i();
    op.height = r.i();
    op.arcWidth = r.i();
    op.arcHeight = r.i();
    return op;
  }
}
