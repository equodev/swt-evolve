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
  double? bufferScaleY;
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
      bufferScaleY = other.bufferScaleY;
      themedCanvas = other.themedCanvas;
      hoverFills = other.hoverFills;
      selectionFills = other.selectionFills;
    }
  }

  @override
  bool readWire(int key, WidgetWireReader r) {
    switch (key) {
      case 276: // XORMode
        XORMode = r.boolean();
        return true;
      case 277: // alpha
        alpha = r.intBoxed();
        return true;
      case 278: // antialias
        antialias = r.int32();
        return true;
      case 279: // background
        background = r.color();
        return true;
      case 280: // backgroundPattern
        backgroundPattern = r.object(VPattern.new);
        return true;
      case 283: // clipping
        clipping = r.rect();
        return true;
      case 284: // clippingPath
        clippingPath = r.object(VPathData.new);
        return true;
      case 285: // clippingRects
        clippingRects = r.ints();
        return true;
      case 287: // fillRule
        fillRule = r.intBoxed();
        return true;
      case 288: // font
        font = r.object(VFont.new);
        return true;
      case 289: // foreground
        foreground = r.color();
        return true;
      case 290: // foregroundPattern
        foregroundPattern = r.object(VPattern.new);
        return true;
      case 292: // interpolation
        interpolation = r.int32();
        return true;
      case 293: // lineCap
        lineCap = r.intBoxed();
        return true;
      case 294: // lineDash
        lineDash = r.ints();
        return true;
      case 296: // lineJoin
        lineJoin = r.intBoxed();
        return true;
      case 297: // lineStyle
        lineStyle = r.intBoxed();
        return true;
      case 298: // lineWidth
        lineWidth = r.int32();
        return true;
      case 302: // transform
        transform = r.object(VTransform.new);
        return true;
      case 286: // clippingText
        clippingText = r.object(VPath.new);
        return true;
      case 295: // lineDashOffset
        lineDashOffset = r.float32();
        return true;
      case 281: // bufferScale
        bufferScale = r.float32();
        return true;
      case 282: // bufferScaleY
        bufferScaleY = r.float32();
        return true;
      case 301: // themedCanvas
        themedCanvas = r.boolBoxed();
        return true;
      case 291: // hoverFills
        hoverFills = r.list((r) => r.rgb());
        return true;
      case 299: // selectionFills
        selectionFills = r.list((r) => r.rgb());
        return true;
      case 300: // style
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
      if (v_ != null) {
        w.keyBoolean(276, v_);
      }
    }
    {
      final v_ = alpha;
      if (v_ != null) {
        w.key(277);
        w.intBoxed(v_);
      }
    }
    {
      final v_ = antialias;
      if (v_ != null) {
        w.keyInt32(278, v_);
      }
    }
    {
      final v_ = background;
      if (v_ != null) {
        w.key(279);
        w.colorValue(v_);
      }
    }
    {
      final v_ = backgroundPattern;
      if (v_ != null) {
        w.key(280);
        w.object(v_.writeWire);
      }
    }
    {
      final v_ = clipping;
      if (v_ != null) {
        w.key(283);
        w.rectValue(v_);
      }
    }
    {
      final v_ = clippingPath;
      if (v_ != null) {
        w.key(284);
        w.object(v_.writeWire);
      }
    }
    {
      final v_ = clippingRects;
      if (v_ != null) {
        w.key(285);
        w.ints(v_);
      }
    }
    {
      final v_ = fillRule;
      if (v_ != null) {
        w.key(287);
        w.intBoxed(v_);
      }
    }
    {
      final v_ = font;
      if (v_ != null) {
        w.key(288);
        w.object(v_.writeWire);
      }
    }
    {
      final v_ = foreground;
      if (v_ != null) {
        w.key(289);
        w.colorValue(v_);
      }
    }
    {
      final v_ = foregroundPattern;
      if (v_ != null) {
        w.key(290);
        w.object(v_.writeWire);
      }
    }
    {
      final v_ = interpolation;
      if (v_ != null) {
        w.keyInt32(292, v_);
      }
    }
    {
      final v_ = lineCap;
      if (v_ != null) {
        w.key(293);
        w.intBoxed(v_);
      }
    }
    {
      final v_ = lineDash;
      if (v_ != null) {
        w.key(294);
        w.ints(v_);
      }
    }
    {
      final v_ = lineJoin;
      if (v_ != null) {
        w.key(296);
        w.intBoxed(v_);
      }
    }
    {
      final v_ = lineStyle;
      if (v_ != null) {
        w.key(297);
        w.intBoxed(v_);
      }
    }
    {
      final v_ = lineWidth;
      if (v_ != null) {
        w.keyInt32(298, v_);
      }
    }
    {
      final v_ = transform;
      if (v_ != null) {
        w.key(302);
        w.object(v_.writeWire);
      }
    }
    {
      final v_ = clippingText;
      if (v_ != null) {
        w.key(286);
        w.object(v_.writeWire);
      }
    }
    {
      final v_ = lineDashOffset;
      if (v_ != null) {
        w.key(295);
        w.float32(v_.toDouble());
      }
    }
    {
      final v_ = bufferScale;
      if (v_ != null) {
        w.key(281);
        w.float32(v_.toDouble());
      }
    }
    {
      final v_ = bufferScaleY;
      if (v_ != null) {
        w.key(282);
        w.float32(v_.toDouble());
      }
    }
    {
      final v_ = themedCanvas;
      if (v_ != null) {
        w.key(301);
        w.boolBoxed(v_);
      }
    }
    {
      final v_ = hoverFills;
      if (v_ != null) {
        w.key(291);
        w.list(v_, (e) => w.rgbValue(e));
      }
    }
    {
      final v_ = selectionFills;
      if (v_ != null) {
        w.key(299);
        w.list(v_, (e) => w.rgbValue(e));
      }
    }
    w.keyInt32(300, style);
  }

  @override
  void visitWire(void Function(int key, Object? value) visit) {
    super.visitWire(visit);
    visit(276, XORMode);
    visit(277, alpha);
    visit(278, antialias);
    visit(279, background);
    visit(280, backgroundPattern);
    visit(283, clipping);
    visit(284, clippingPath);
    visit(285, clippingRects);
    visit(287, fillRule);
    visit(288, font);
    visit(289, foreground);
    visit(290, foregroundPattern);
    visit(292, interpolation);
    visit(293, lineCap);
    visit(294, lineDash);
    visit(296, lineJoin);
    visit(297, lineStyle);
    visit(298, lineWidth);
    visit(302, transform);
    visit(286, clippingText);
    visit(295, lineDashOffset);
    visit(281, bufferScale);
    visit(282, bufferScaleY);
    visit(301, themedCanvas);
    visit(291, hoverFills);
    visit(299, selectionFills);
    visit(300, style);
  }
}

class VGCCopyAreaImageintint {
  VImage? image;
  int x;
  int y;

  VGCCopyAreaImageintint({this.x = 0, this.y = 0});

  /// The op as a GC's binary frame carries it; see GcWireReader.
  factory VGCCopyAreaImageintint.fromBinary(GcWireReader r) =>
      VGCCopyAreaImageintint()..readBinary(r);

  /// Sets every argument to the next op [r] reads: an object refilled per op.
  void readBinary(GcWireReader r) {
    final op = this;
    {
      final ww = r.wire();
      op.image = ww.image();
    }
    op.x = r.i();
    op.y = r.i();
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
  factory VGCCopyAreaintintintintintintboolean.fromBinary(GcWireReader r) =>
      VGCCopyAreaintintintintintintboolean()..readBinary(r);

  /// Sets every argument to the next op [r] reads: an object refilled per op.
  void readBinary(GcWireReader r) {
    final op = this;
    op.srcX = r.i();
    op.srcY = r.i();
    op.width = r.i();
    op.height = r.i();
    op.destX = r.i();
    op.destY = r.i();
    op.paint = r.b();
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
  factory VGCDrawArcintintintintintint.fromBinary(GcWireReader r) =>
      VGCDrawArcintintintintintint()..readBinary(r);

  /// Sets every argument to the next op [r] reads: an object refilled per op.
  void readBinary(GcWireReader r) {
    final op = this;
    op.x = r.i();
    op.y = r.i();
    op.width = r.i();
    op.height = r.i();
    op.startAngle = r.i();
    op.arcAngle = r.i();
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
  factory VGCDrawFocusintintintint.fromBinary(GcWireReader r) =>
      VGCDrawFocusintintintint()..readBinary(r);

  /// Sets every argument to the next op [r] reads: an object refilled per op.
  void readBinary(GcWireReader r) {
    final op = this;
    op.x = r.i();
    op.y = r.i();
    op.width = r.i();
    op.height = r.i();
  }
}

class VGCDrawImageImageintint {
  VImage? image;
  int x;
  int y;

  VGCDrawImageImageintint({this.x = 0, this.y = 0});

  /// The op as a GC's binary frame carries it; see GcWireReader.
  factory VGCDrawImageImageintint.fromBinary(GcWireReader r) =>
      VGCDrawImageImageintint()..readBinary(r);

  /// Sets every argument to the next op [r] reads: an object refilled per op.
  void readBinary(GcWireReader r) {
    final op = this;
    {
      final ww = r.wire();
      op.image = ww.image();
    }
    op.x = r.i();
    op.y = r.i();
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
  factory VGCDrawImageImageintintintint.fromBinary(GcWireReader r) =>
      VGCDrawImageImageintintintint()..readBinary(r);

  /// Sets every argument to the next op [r] reads: an object refilled per op.
  void readBinary(GcWireReader r) {
    final op = this;
    {
      final ww = r.wire();
      op.image = ww.image();
    }
    op.destX = r.i();
    op.destY = r.i();
    op.destWidth = r.i();
    op.destHeight = r.i();
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
  factory VGCDrawImageImageintintintintintintintint.fromBinary(
    GcWireReader r,
  ) => VGCDrawImageImageintintintintintintintint()..readBinary(r);

  /// Sets every argument to the next op [r] reads: an object refilled per op.
  void readBinary(GcWireReader r) {
    final op = this;
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
  }
}

class VGCDrawLineintintintint {
  int x1;
  int y1;
  int x2;
  int y2;

  VGCDrawLineintintintint({this.x1 = 0, this.y1 = 0, this.x2 = 0, this.y2 = 0});

  /// The op as a GC's binary frame carries it; see GcWireReader.
  factory VGCDrawLineintintintint.fromBinary(GcWireReader r) =>
      VGCDrawLineintintintint()..readBinary(r);

  /// Sets every argument to the next op [r] reads: an object refilled per op.
  void readBinary(GcWireReader r) {
    final op = this;
    op.x1 = r.i();
    op.y1 = r.i();
    op.x2 = r.i();
    op.y2 = r.i();
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
  factory VGCDrawOvalintintintint.fromBinary(GcWireReader r) =>
      VGCDrawOvalintintintint()..readBinary(r);

  /// Sets every argument to the next op [r] reads: an object refilled per op.
  void readBinary(GcWireReader r) {
    final op = this;
    op.x = r.i();
    op.y = r.i();
    op.width = r.i();
    op.height = r.i();
  }
}

class VGCDrawPathPath {
  VPath? path;

  VGCDrawPathPath();

  /// The op as a GC's binary frame carries it; see GcWireReader.
  factory VGCDrawPathPath.fromBinary(GcWireReader r) =>
      VGCDrawPathPath()..readBinary(r);

  /// Sets every argument to the next op [r] reads: an object refilled per op.
  void readBinary(GcWireReader r) {
    final op = this;
    {
      final ww = r.wire();
      op.path = ww.object(VPath.new);
    }
  }
}

class VGCDrawPointintint {
  int x;
  int y;

  VGCDrawPointintint({this.x = 0, this.y = 0});

  /// The op as a GC's binary frame carries it; see GcWireReader.
  factory VGCDrawPointintint.fromBinary(GcWireReader r) =>
      VGCDrawPointintint()..readBinary(r);

  /// Sets every argument to the next op [r] reads: an object refilled per op.
  void readBinary(GcWireReader r) {
    final op = this;
    op.x = r.i();
    op.y = r.i();
  }
}

class VGCDrawPolygonint {
  List<int>? pointArray;

  VGCDrawPolygonint();

  /// The op as a GC's binary frame carries it; see GcWireReader.
  factory VGCDrawPolygonint.fromBinary(GcWireReader r) =>
      VGCDrawPolygonint()..readBinary(r);

  /// Sets every argument to the next op [r] reads: an object refilled per op.
  void readBinary(GcWireReader r) {
    final op = this;
    op.pointArray = r.ints();
  }
}

class VGCDrawPolylineint {
  List<int>? pointArray;

  VGCDrawPolylineint();

  /// The op as a GC's binary frame carries it; see GcWireReader.
  factory VGCDrawPolylineint.fromBinary(GcWireReader r) =>
      VGCDrawPolylineint()..readBinary(r);

  /// Sets every argument to the next op [r] reads: an object refilled per op.
  void readBinary(GcWireReader r) {
    final op = this;
    op.pointArray = r.ints();
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
  factory VGCDrawRectangleintintintint.fromBinary(GcWireReader r) =>
      VGCDrawRectangleintintintint()..readBinary(r);

  /// Sets every argument to the next op [r] reads: an object refilled per op.
  void readBinary(GcWireReader r) {
    final op = this;
    op.x = r.i();
    op.y = r.i();
    op.width = r.i();
    op.height = r.i();
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
  factory VGCDrawRoundRectangleintintintintintint.fromBinary(GcWireReader r) =>
      VGCDrawRoundRectangleintintintintintint()..readBinary(r);

  /// Sets every argument to the next op [r] reads: an object refilled per op.
  void readBinary(GcWireReader r) {
    final op = this;
    op.x = r.i();
    op.y = r.i();
    op.width = r.i();
    op.height = r.i();
    op.arcWidth = r.i();
    op.arcHeight = r.i();
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
  factory VGCDrawTextStringintintint.fromBinary(GcWireReader r) =>
      VGCDrawTextStringintintint()..readBinary(r);

  /// Sets every argument to the next op [r] reads: an object refilled per op.
  void readBinary(GcWireReader r) {
    final op = this;
    op.string = r.string() ?? '';
    op.x = r.i();
    op.y = r.i();
    op.flags = r.i();
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
  factory VGCFillArcintintintintintint.fromBinary(GcWireReader r) =>
      VGCFillArcintintintintintint()..readBinary(r);

  /// Sets every argument to the next op [r] reads: an object refilled per op.
  void readBinary(GcWireReader r) {
    final op = this;
    op.x = r.i();
    op.y = r.i();
    op.width = r.i();
    op.height = r.i();
    op.startAngle = r.i();
    op.arcAngle = r.i();
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
  ) => VGCFillGradientRectangleintintintintboolean()..readBinary(r);

  /// Sets every argument to the next op [r] reads: an object refilled per op.
  void readBinary(GcWireReader r) {
    final op = this;
    op.x = r.i();
    op.y = r.i();
    op.width = r.i();
    op.height = r.i();
    op.vertical = r.b();
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
  factory VGCFillOvalintintintint.fromBinary(GcWireReader r) =>
      VGCFillOvalintintintint()..readBinary(r);

  /// Sets every argument to the next op [r] reads: an object refilled per op.
  void readBinary(GcWireReader r) {
    final op = this;
    op.x = r.i();
    op.y = r.i();
    op.width = r.i();
    op.height = r.i();
  }
}

class VGCFillPathPath {
  VPath? path;

  VGCFillPathPath();

  /// The op as a GC's binary frame carries it; see GcWireReader.
  factory VGCFillPathPath.fromBinary(GcWireReader r) =>
      VGCFillPathPath()..readBinary(r);

  /// Sets every argument to the next op [r] reads: an object refilled per op.
  void readBinary(GcWireReader r) {
    final op = this;
    {
      final ww = r.wire();
      op.path = ww.object(VPath.new);
    }
  }
}

class VGCFillPolygonint {
  List<int>? pointArray;

  VGCFillPolygonint();

  /// The op as a GC's binary frame carries it; see GcWireReader.
  factory VGCFillPolygonint.fromBinary(GcWireReader r) =>
      VGCFillPolygonint()..readBinary(r);

  /// Sets every argument to the next op [r] reads: an object refilled per op.
  void readBinary(GcWireReader r) {
    final op = this;
    op.pointArray = r.ints();
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
  factory VGCFillRectangleintintintint.fromBinary(GcWireReader r) =>
      VGCFillRectangleintintintint()..readBinary(r);

  /// Sets every argument to the next op [r] reads: an object refilled per op.
  void readBinary(GcWireReader r) {
    final op = this;
    op.x = r.i();
    op.y = r.i();
    op.width = r.i();
    op.height = r.i();
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
  factory VGCFillRoundRectangleintintintintintint.fromBinary(GcWireReader r) =>
      VGCFillRoundRectangleintintintintintint()..readBinary(r);

  /// Sets every argument to the next op [r] reads: an object refilled per op.
  void readBinary(GcWireReader r) {
    final op = this;
    op.x = r.i();
    op.y = r.i();
    op.width = r.i();
    op.height = r.i();
    op.arcWidth = r.i();
    op.arcHeight = r.i();
  }
}
