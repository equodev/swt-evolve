import 'dart:math' as math;
import 'dart:ui' as ui;
import 'dart:typed_data';
import 'dart:convert';
import 'package:flutter/widgets.dart';
import 'package:flutter/services.dart';
import '../gen/canvas.dart';
import '../gen/color.dart';
import '../gen/gc.dart';
import '../gen/image.dart';
import '../gen/swt.dart';
import '../gen/widget.dart';
import 'canvas_evolve.dart';
import './utils/image_utils.dart';
import './utils/font_utils.dart';
import 'assets_manager.dart';
import 'color_utils.dart';
import 'dart:ui' as ui;
import 'package:flutter/services.dart' show rootBundle;
import 'package:flutter_svg/flutter_svg.dart';

class GCImpl<T extends GCSwt, V extends VGC> extends GCState<T, V> {
  List<Shape> shapes = [];

  @override
  void initState() {
    super.initState();
  }

  Color get bg =>
      colorFromVColor(state.background, defaultColor: Color(0xFFFFFFFF));
  Color get fg =>
      colorFromVColor(state.foreground, defaultColor: Color(0xFF333232));
  double get lineWidth => (state.lineWidth ?? 1).toDouble();
  int get lineCap => state.lineCap ?? 1;
  int get lineJoin => state.lineJoin ?? 1;
  Rect? get clipping {
    if (state.clipping == null) return null;
    final width = state.clipping!.width?.toDouble() ?? 0;
    final height = state.clipping!.height?.toDouble() ?? 0;
    if (width <= 0 || height <= 0) return null;
    return Rect.fromLTWH(state.clipping!.x?.toDouble() ?? 0,
        state.clipping!.y?.toDouble() ?? 0, width, height);
  }

  Size get bounds {
    final canvas = context.findAncestorWidgetOfExactType<CanvasSwt>();
    if (canvas != null && canvas.value.id == state.id) {
      final canvasState = context.findAncestorStateOfType<CanvasImpl>();
      return canvasState?.getBounds() ?? const Size(100, 100);
    }
    return const Size(100, 100);
  }

  //todo sendPaintPaint should be called everytime gc redraws and also make shapes list empty
  @override
  Widget build(BuildContext context) {
    return CustomPaint(
      size: bounds,
      painter: ScenePainter(List.unmodifiable(shapes)),
    );
  }

  @override
  void setValue(V value) {
    state = value!;
    extraSetState();
  }

  Map<String, dynamic> _convertSwtFontStyle(int swtFontStyle) {
    FontWeight weight = FontWeight.normal;
    FontStyle style = FontStyle.normal;

    switch (swtFontStyle) {
      case 1:
        weight = FontWeight.bold;
        break;
      case 2:
        style = FontStyle.italic;
        break;
      case 3:
        weight = FontWeight.bold;
        style = FontStyle.italic;
        break;
    }

    return {
      'weight': weight,
      'style': style,
    };
  }

  Color applyAlpha(Color color) {
    final alpha = state.alpha ?? 255;
    if (alpha == 255) return color;
    return color.withOpacity(alpha / 255.0);
  }

  double _degToRad(double deg) => deg * 3.14159 / 180;

  void _addRoundRectShape({
    required int x,
    required int y,
    required int width,
    required int height,
    required int arcWidth,
    required int arcHeight,
    required bool isFilled,
  }) {
    final rect = _getRectFromArgs(x, y, width, height);
    final radiusX = arcWidth.toDouble() / 2.0;
    final radiusY = arcHeight.toDouble() / 2.0;
    final color = isFilled ? applyAlpha(bg) : applyAlpha(fg);
    final strokeWidth = isFilled ? 0.0 : lineWidth;

    setState(() => shapes = [
          ...shapes,
          RoundRectShape(
              rect, radiusX, radiusY, color, strokeWidth, lineCap, lineJoin,
              isFilled: isFilled, clipRect: clipping)
        ]);
  }

  void _addOvalShape(
      {required int x,
      required int y,
      required int width,
      required int height,
      required bool isFilled}) {
    final rect = _getRectFromArgs(x, y, width, height);
    final color = isFilled ? applyAlpha(bg) : applyAlpha(fg);
    final strokeWidth = isFilled ? 0.0 : lineWidth;

    setState(() => shapes = [
          ...shapes,
          OvalShape(rect, color, strokeWidth,
              isFilled: isFilled, clipRect: clipping)
        ]);
  }

  void _addRectShape(
      {required int x,
      required int y,
      required int width,
      required int height,
      required bool isFilled}) {
    final rect = _getRectFromArgs(x, y, width, height);
    final color = isFilled ? applyAlpha(bg) : applyAlpha(fg);
    final strokeWidth = isFilled ? 0.0 : lineWidth;

    setState(() => shapes = [
          ...shapes,
          RectShape(rect, color, strokeWidth, lineCap, lineJoin,
              isFilled: isFilled, clipRect: clipping)
        ]);
  }

  void _addPolygonShape(
      {required List<int> points, required bool isFilled, int minPoints = 6}) {
    if (points.length >= minPoints && points.length % 2 == 0) {
      final color = isFilled ? applyAlpha(bg) : applyAlpha(fg);
      final strokeWidth = isFilled ? 0.0 : lineWidth;

      setState(() => shapes = [
            ...shapes,
            PolygonShape(points, color, strokeWidth, lineCap, lineJoin,
                isFilled: isFilled, clipRect: clipping)
          ]);
    }
  }

  void _addPolylineShape(
      {required List<int> points, required bool isFilled, int minPoints = 2}) {
    if (points.length >= minPoints && points.length % 2 == 0) {
      final color = isFilled ? applyAlpha(bg) : applyAlpha(fg);
      final strokeWidth = isFilled ? 0.0 : lineWidth;

      setState(() => shapes = [
        ...shapes,
        PolylineShape(points, color, strokeWidth, lineCap, lineJoin,
            isFilled: isFilled, clipRect: clipping)
      ]);
    }
  }

  void _addArcShape(
      {required int x,
      required int y,
      required int width,
      required int height,
      required int startAngle,
      required int arcAngle,
      required bool isFilled}) {
    final rect = _getRectFromArgs(x, y, width, height);
    final startAngleRadians = _degToRad(-startAngle.toDouble());
    final sweepAngleRadians = _degToRad(-arcAngle.toDouble());
    final color = isFilled ? applyAlpha(bg) : applyAlpha(fg);
    final strokeWidth = isFilled ? 0.0 : lineWidth;

    setState(() => shapes = [
          ...shapes,
          ArcShape(rect, startAngleRadians, sweepAngleRadians, color,
              strokeWidth, lineCap, lineJoin,
              isFilled: isFilled, clipRect: clipping)
        ]);
  }

  @override
  void onDrawArc(VGCDrawArc opArgs) {
    _addArcShape(
      x: opArgs.x ?? 0,
      y: opArgs.y ?? 0,
      width: opArgs.width ?? 0,
      height: opArgs.height ?? 0,
      startAngle: opArgs.startAngle ?? 0,
      arcAngle: opArgs.arcAngle ?? 0,
      isFilled: false,
    );
  }

  @override
  void onDrawFocus(VGCDrawFocus opArgs) {
    final rect =
        _getRectFromArgs(opArgs.x, opArgs.y, opArgs.width, opArgs.height);

    setState(() =>
        shapes = [...shapes, FocusRectShape(rect, applyAlpha(fg), clipping)]);
  }

  @override
  void onDrawLine(VGCDrawLine opArgs) {
    final x1 = opArgs.x1?.toDouble() ?? 0.0;
    final y1 = opArgs.y1?.toDouble() ?? 0.0;
    final x2 = opArgs.x2?.toDouble() ?? 0.0;
    final y2 = opArgs.y2?.toDouble() ?? 0.0;
    setState(() => shapes = [
          ...shapes,
          LineShape(Offset(x1, y1), Offset(x2, y2), applyAlpha(fg), lineWidth,
              lineCap, lineJoin, clipping)
        ]);
  }

  @override
  void onDrawOval(VGCDrawOval opArgs) {
    _addOvalShape(
      x: opArgs.x ?? 0,
      y: opArgs.y ?? 0,
      width: opArgs.width ?? 0,
      height: opArgs.height ?? 0,
      isFilled: false,
    );
  }

  @override
  void onDrawPoint(VGCDrawPoint opArgs) {
    final point = Offset(
      (opArgs.x ?? 0).toDouble(),
      (opArgs.y ?? 0).toDouble(),
    );

    setState(() =>
        shapes = [...shapes, PointShape(point, applyAlpha(fg), clipping)]);
  }

  @override
  void onDrawPolygon(VGCDrawPolygon opArgs) {
    _addPolygonShape(
      points: opArgs.pointArray ?? [],
      isFilled: false,
      minPoints: 6,
    );
  }

  @override
  void onDrawPolyline(VGCDrawPolyline opArgs) {
    _addPolylineShape(
      points: opArgs.pointArray ?? [],
      isFilled: false,
      minPoints: 2,
    );
  }

  @override
  void onDrawRectangle(VGCDrawRectangle opArgs) {
    _addRectShape(
      x: opArgs.x ?? 0,
      y: opArgs.y ?? 0,
      width: opArgs.width ?? 0,
      height: opArgs.height ?? 0,
      isFilled: false,
    );
  }

  @override
  void onDrawRoundRectangle(VGCDrawRoundRectangle opArgs) {
    _addRoundRectShape(
      x: opArgs.x ?? 0,
      y: opArgs.y ?? 0,
      width: opArgs.width ?? 0,
      height: opArgs.height ?? 0,
      arcWidth: opArgs.arcWidth ?? 0,
      arcHeight: opArgs.arcHeight ?? 0,
      isFilled: false,
    );
  }

  @override
  void onDrawText(VGCDrawText opArgs) {
    final text = opArgs.string ?? '';
    final x = (opArgs.x ?? 0).toDouble();
    final y = (opArgs.y ?? 0).toDouble();
    final flags = opArgs.flags ?? 0;

    final vFont = state.font;
    FontUtils.printFontData(vFont, context: 'GC.drawText');

    final processedText = _processTextFlags(text, flags);
    final isTransparent = (flags & SWT.DRAW_TRANSPARENT) != 0;

    final textStyle = FontUtils.textStyleFromVFont(
      vFont,
      context,
      color: applyAlpha(fg),
    );

    List<Shape> newShapes = [];

    if (!isTransparent) {
      final textPainter = TextPainter(
        text: TextSpan(text: processedText, style: textStyle),
        textDirection: TextDirection.ltr,
      )..layout();

      final rect = Rect.fromLTWH(x, y, textPainter.width, textPainter.height);
      newShapes.add(RectShape(rect, applyAlpha(bg), 0, lineCap, lineJoin,
          isFilled: true, clipRect: clipping));
    }

    newShapes.add(TextShape(processedText, Offset(x, y), textStyle, clipping));
    setState(() => shapes = [...shapes, ...newShapes]);
  }

  String _processTextFlags(String text, int flags) {
    return text
        .replaceAll('\r\n', (flags & SWT.DRAW_DELIMITER) != 0 ? '\n' : ' ')
        .replaceAll('\r', (flags & SWT.DRAW_DELIMITER) != 0 ? '\n' : ' ')
        .replaceAll('\n', (flags & SWT.DRAW_DELIMITER) != 0 ? '\n' : ' ')
        .replaceAll('\t', (flags & SWT.DRAW_TAB) != 0 ? '    ' : ' ');
  }

  @override
  void onFillArc(VGCFillArc opArgs) {
    _addArcShape(
      x: opArgs.x ?? 0,
      y: opArgs.y ?? 0,
      width: opArgs.width ?? 0,
      height: opArgs.height ?? 0,
      startAngle: opArgs.startAngle ?? 0,
      arcAngle: opArgs.arcAngle ?? 0,
      isFilled: true,
    );
  }

  @override
  void onFillGradientRectangle(VGCFillGradientRectangle opArgs) {
    final rect =
        _getRectFromArgs(opArgs.x, opArgs.y, opArgs.width, opArgs.height);

    final vertical = opArgs.vertical ?? false;
    final fromColor = applyAlpha(fg);
    final toColor = applyAlpha(bg);

    setState(() => shapes = [
          ...shapes,
          GradientRectShape(rect, fromColor, toColor, vertical, clipping)
        ]);
  }

  @override
  void onFillOval(VGCFillOval opArgs) {
    _addOvalShape(
      x: opArgs.x ?? 0,
      y: opArgs.y ?? 0,
      width: opArgs.width ?? 0,
      height: opArgs.height ?? 0,
      isFilled: true,
    );
  }

  @override
  void onFillPolygon(VGCFillPolygon opArgs) {
    _addPolygonShape(
      points: opArgs.pointArray ?? [],
      isFilled: true,
      minPoints: 6,
    );
  }

  @override
  void onFillRectangle(VGCFillRectangle opArgs) {
    _addRectShape(
      x: opArgs.x ?? 0,
      y: opArgs.y ?? 0,
      width: opArgs.width ?? 0,
      height: opArgs.height ?? 0,
      isFilled: true,
    );
  }

  @override
  void onFillRoundRectangle(VGCFillRoundRectangle opArgs) {
    _addRoundRectShape(
      x: opArgs.x ?? 0,
      y: opArgs.y ?? 0,
      width: opArgs.width ?? 0,
      height: opArgs.height ?? 0,
      arcWidth: opArgs.arcWidth ?? 0,
      arcHeight: opArgs.arcHeight ?? 0,
      isFilled: true,
    );
  }

  @override
  void onCopyArea(VGCCopyArea opArgs) {
    final srcRect =
        _getRectFromArgs(opArgs.srcX, opArgs.srcY, opArgs.width, opArgs.height);
    final destOffset = Offset(
      (opArgs.destX ?? 0) - (opArgs.srcX ?? 0).toDouble(),
      (opArgs.destY ?? 0) - (opArgs.srcY ?? 0).toDouble(),
    );

    final copiedShapes = shapes
        .where((shape) => _shapeIntersects(shape, srcRect))
        .map((shape) => _translateShapeWithClip(shape, destOffset, srcRect))
        .toList();

    setState(() => shapes = [...shapes, ...copiedShapes]);
  }

  Rect _getRectFromArgs(int? x, int? y, int? w, int? h) => Rect.fromLTWH(
      (x ?? 0).toDouble(),
      (y ?? 0).toDouble(),
      (w ?? 0).toDouble(),
      (h ?? 0).toDouble());

  bool _shapeIntersects(Shape shape, Rect area) {
    return switch (shape) {
      LineShape s => area.contains(s.p1) ||
          area.contains(s.p2) ||
          area.overlaps(Rect.fromPoints(s.p1, s.p2)),
      RectShape s => s.rect.overlaps(area),
      OvalShape s => s.rect.overlaps(area),
      ArcShape s => s.rect.overlaps(area),
      RoundRectShape s => s.rect.overlaps(area),
      GradientRectShape s => s.rect.overlaps(area),
      FocusRectShape s => s.rect.overlaps(area),
      TextShape s => area.contains(s.off),
      PointShape s => area.contains(s.point),
      PolygonShape s =>
        s.points.indexed.where((e) => e.$1 % 2 == 0).any((e) => area.contains(
            Offset(s.points[e.$1].toDouble(), s.points[e.$1 + 1].toDouble()))),
      ImageShape s => s.destRect.overlaps(area),
      _ => true,
    };
  }

  Shape _translateShapeWithClip(Shape shape, Offset offset, Rect srcArea) {
    final clipArea = srcArea.translate(offset.dx, offset.dy);

    return switch (shape) {
      LineShape s => LineShape(s.p1 + offset, s.p2 + offset, s.color,
          s.strokeWidth, s.lineCap, s.lineJoin, clipArea),
      RectShape s => RectShape(s.rect.translate(offset.dx, offset.dy), s.color,
          s.strokeWidth, s.lineCap, s.lineJoin,
          isFilled: s.isFilled, clipRect: clipArea),
      OvalShape s => OvalShape(
          s.rect.translate(offset.dx, offset.dy), s.color, s.strokeWidth,
          isFilled: s.isFilled, clipRect: clipArea),
      TextShape s => TextShape(s.text, s.off + offset, s.style, clipArea),
      PointShape s => PointShape(s.point + offset, s.color, clipArea),
      PolygonShape s => PolygonShape(_translatePoints(s.points, offset),
          s.color, s.strokeWidth, s.lineCap, s.lineJoin,
          isFilled: s.isFilled, clipRect: clipArea),
      ArcShape s => ArcShape(
          s.rect.translate(offset.dx, offset.dy),
          s.startAngle,
          s.sweepAngle,
          s.color,
          s.strokeWidth,
          s.lineCap,
          s.lineJoin,
          isFilled: s.isFilled,
          clipRect: clipArea),
      RoundRectShape s => RoundRectShape(s.rect.translate(offset.dx, offset.dy),
          s.radiusX, s.radiusY, s.color, s.strokeWidth, s.lineCap, s.lineJoin,
          isFilled: s.isFilled, clipRect: clipArea),
      GradientRectShape s => GradientRectShape(
          s.rect.translate(offset.dx, offset.dy),
          s.fromColor,
          s.toColor,
          s.vertical,
          clipArea),
      FocusRectShape s => FocusRectShape(
          s.rect.translate(offset.dx, offset.dy), s.color, clipArea),
      ImageShape s when s.type == ImageType.raster => ImageShape.raster(
          s.image!, s.srcRect!, s.destRect.translate(offset.dx, offset.dy),
          clipRect: clipArea),
      ImageShape s when s.type == ImageType.svg => ImageShape.svg(
          s.pictureInfo!, s.destRect.translate(offset.dx, offset.dy),
          clipRect: clipArea),
      _ => shape,
    };
  }

  List<int> _translatePoints(List<int> points, Offset offset) {
    final result = <int>[];
    for (int i = 0; i < points.length; i += 2) {
      result.add(points[i] + offset.dx.toInt());
      result.add(points[i + 1] + offset.dy.toInt());
    }
    return result;
  }

  @override
  void onCopyAreaImage(VGCCopyAreaImage opArgs) async {
    if (opArgs.image == null) return;
    final ui.Image gcImage = await _renderShapesToUiImage(shapes, bounds);
    final ui.Image? destImage =
        await ImageUtils.decodeVImageToUIImage(opArgs.image!);
    if (destImage == null || !mounted) return;
    final recorder = ui.PictureRecorder();
    final canvas = Canvas(recorder);
    canvas.drawImage(destImage, Offset.zero, Paint());
    canvas.drawImage(
        gcImage, Offset(opArgs.x.toDouble(), opArgs.y.toDouble()), Paint());
    final picture = recorder.endRecording();
    final ui.Image c = await picture.toImage(destImage.width, destImage.height);
    //todo image must be returned to java or replace the original java image with c
  }

  @override
  void onDrawImage(VGCDrawImage opArgs) {
    final capturedClipping = clipping;
    if (opArgs.srcImage == null) return;
    _processImageAsync(opArgs.srcImage!, opArgs, capturedClipping);
  }

  void _processImageAsync(
      VImage vImage, VGCDrawImage opArgs, Rect? capturedClipping) async {
    final imageShape =
        await ImageShape.fromVImage(vImage, opArgs, capturedClipping);
    if (mounted) {
      setState(() => shapes = [...shapes, imageShape]);
    }
  }

  Future<ui.Image> _renderShapesToUiImage(List<Shape> shapes, Size size) async {
    final recorder = ui.PictureRecorder();
    final canvas = Canvas(recorder);

    canvas.drawRect(
      Offset.zero & size,
      Paint()..color = Color(0xFFFFFFFF),
    );

    for (final shape in shapes) {
      shape.draw(canvas);
    }

    final picture = recorder.endRecording();
    return picture.toImage(size.width.toInt(), size.height.toInt());
  }
}

/* ─────────────── ScenePainter ─────────────── */

class ScenePainter extends CustomPainter {
  ScenePainter(this.shapes);
  final List<Shape> shapes;

  @override
  void paint(Canvas canvas, Size size) {
    canvas.drawRect(Offset.zero & size, Paint()..color = Color(0xFFFFFFFF));
    canvas.save();

    for (final s in shapes) {
      s.draw(canvas);
    }

    canvas.restore();
  }

  @override
  bool shouldRepaint(ScenePainter o) => o.shapes != shapes;
}

abstract class Shape {
  void draw(Canvas c);
  @override
  String toString();

// Each shape can have an associated clip
  Rect? get clipRect => null;
}

class TextShape extends Shape {
  TextShape(this.text, this.off, this.style, [this.clipRect]);
  final String text;
  final Offset off;
  final TextStyle style;
  @override
  final Rect? clipRect;

  @override
  void draw(Canvas c) {
    if (clipRect != null) {
      c.save();
      c.clipRect(clipRect!);
    }

    final tp = TextPainter(
      text: TextSpan(text: text, style: style),
      textDirection: TextDirection.ltr,
    )..layout();

    tp.paint(c, off);

    if (clipRect != null) {
      c.restore();
    }
  }

  @override
  String toString() =>
      'Text "$text" @ $off${clipRect != null ? " [clipped]" : ""}';
}

class LineShape extends Shape {
  LineShape(this.p1, this.p2, this.color, this.strokeWidth, this.lineCap,
      this.lineJoin,
      [this.clipRect]);

  final Offset p1, p2;
  final Color color;
  final double strokeWidth;
  final int lineCap;
  final int lineJoin;
  @override
  final Rect? clipRect;

  @override
  void draw(Canvas c) {
    if (clipRect != null) {
      c.save();
      c.clipRect(clipRect!);
    }

    c.drawLine(
        p1,
        p2,
        Paint()
          ..color = color
          ..strokeWidth = strokeWidth
          ..strokeCap = getStrokeCap(lineCap)
          ..strokeJoin = getStrokeJoin(lineJoin));

    if (clipRect != null) {
      c.restore();
    }
  }

  @override
  String toString() =>
      'Line $p1 → $p2 (width: $strokeWidth)${clipRect != null ? " [clipped]" : ""}';
}

class OvalShape extends Shape {
  OvalShape(this.rect, this.color, this.strokeWidth,
      {this.isFilled = false, this.clipRect});

  final Rect rect;
  final Color color;
  final double strokeWidth;
  final bool isFilled;
  @override
  final Rect? clipRect;

  @override
  void draw(Canvas c) {
    if (clipRect != null) {
      c.save();
      c.clipRect(clipRect!);
    }

    c.drawOval(
        rect,
        Paint()
          ..color = color
          ..style = isFilled ? PaintingStyle.fill : PaintingStyle.stroke
          ..strokeWidth = strokeWidth);

    if (clipRect != null) {
      c.restore();
    }
  }

  @override
  String toString() =>
      '${isFilled ? "Fill" : ""}Oval $rect${clipRect != null ? " [clipped]" : ""}';
}

class RectShape extends Shape {
  RectShape(
      this.rect, this.color, this.strokeWidth, this.lineCap, this.lineJoin,
      {this.isFilled = false, this.clipRect});

  final Rect rect;
  final Color color;
  final double strokeWidth;
  final int lineCap;
  final int lineJoin;
  final bool isFilled;
  @override
  final Rect? clipRect;

  @override
  void draw(Canvas c) {
    if (clipRect != null) {
      c.save();
      c.clipRect(clipRect!);
    }

    final paint = Paint()..color = color;

    if (isFilled) {
      paint.style = PaintingStyle.fill;
    } else {
      paint
        ..style = PaintingStyle.stroke
        ..strokeWidth = strokeWidth
        ..strokeCap = getStrokeCap(lineCap)
        ..strokeJoin = getStrokeJoin(lineJoin);
    }

    c.drawRect(rect, paint);

    if (clipRect != null) {
      c.restore();
    }
  }

  @override
  String toString() =>
      '${isFilled ? "Fill" : ""}Rect $rect${isFilled ? "" : " (width: $strokeWidth)"}${clipRect != null ? " [clipped]" : ""}';
}

class GradientRectShape extends Shape {
  GradientRectShape(this.rect, this.fromColor, this.toColor, this.vertical,
      [this.clipRect]);

  final Rect rect;
  final Color fromColor;
  final Color toColor;
  final bool vertical;
  @override
  final Rect? clipRect;

  @override
  void draw(Canvas c) {
    if (clipRect != null) {
      c.save();
      c.clipRect(clipRect!);
    }

    final gradient = LinearGradient(
      begin: vertical ? Alignment.topCenter : Alignment.centerLeft,
      end: vertical ? Alignment.bottomCenter : Alignment.centerRight,
      colors: [fromColor, toColor],
    );

    final shader = gradient.createShader(rect);

    c.drawRect(
      rect,
      Paint()
        ..shader = shader
        ..style = PaintingStyle.fill,
    );

    if (clipRect != null) {
      c.restore();
    }
  }

  @override
  String toString() =>
      'GradientRect $rect (${vertical ? 'vertical' : 'horizontal'})${clipRect != null ? " [clipped]" : ""}';
}

class PolygonShape extends Shape {
  PolygonShape(
      this.points, this.color, this.strokeWidth, this.lineCap, this.lineJoin,
      {this.isFilled = false, this.clipRect});

  final List<int> points;
  final Color color;
  final double strokeWidth;
  final int lineCap;
  final int lineJoin;
  final bool isFilled;
  @override
  final Rect? clipRect;

  @override
  void draw(Canvas c) {
    if (points.length < 6) return;

    if (clipRect != null) {
      c.save();
      c.clipRect(clipRect!);
    }

    final paint = Paint()
      ..color = color
      ..style = isFilled ? PaintingStyle.fill : PaintingStyle.stroke
      ..strokeWidth = strokeWidth
      ..strokeCap = getStrokeCap(lineCap)
      ..strokeJoin = getStrokeJoin(lineJoin);

    final path = Path();
    path.moveTo(points[0].toDouble(), points[1].toDouble());

    for (int i = 2; i < points.length; i += 2) {
      path.lineTo(points[i].toDouble(), points[i + 1].toDouble());
    }

    path.close();
    c.drawPath(path, paint);

    if (clipRect != null) {
      c.restore();
    }
  }

  @override
  String toString() =>
      '${isFilled ? "Fill" : ""}Polygon ${points.length ~/ 2} points${clipRect != null ? " [clipped]" : ""}';
}

class PolylineShape extends Shape {
  PolylineShape(
      this.points, this.color, this.strokeWidth, this.lineCap, this.lineJoin,
      {this.isFilled = false, this.clipRect});

  final List<int> points;
  final Color color;
  final double strokeWidth;
  final int lineCap;
  final int lineJoin;
  final bool isFilled;
  @override
  final Rect? clipRect;

  @override
  void draw(Canvas c) {
    if (points.length < 4) return;

    if (clipRect != null) {
      c.save();
      c.clipRect(clipRect!);
    }

    final paint = Paint()
      ..color = color
      ..style = isFilled ? PaintingStyle.fill : PaintingStyle.stroke
      ..strokeWidth = strokeWidth
      ..strokeCap = getStrokeCap(lineCap)
      ..strokeJoin = getStrokeJoin(lineJoin);

    final path = Path();
    path.moveTo(points[0].toDouble(), points[1].toDouble());

    for (int i = 2; i < points.length; i += 2) {
      path.lineTo(points[i].toDouble(), points[i + 1].toDouble());
    }

    c.drawPath(path, paint);

    if (clipRect != null) {
      c.restore();
    }
  }

  @override
  String toString() =>
      '${isFilled ? "Fill" : ""}Polyline ${points.length ~/ 2} points${clipRect != null ? " [clipped]" : ""}';
}

class ArcShape extends Shape {
  ArcShape(this.rect, this.startAngle, this.sweepAngle, this.color,
      this.strokeWidth, this.lineCap, this.lineJoin,
      {this.isFilled = false, this.clipRect});

  final Rect rect;
  final double startAngle;
  final double sweepAngle;
  final Color color;
  final double strokeWidth;
  final int lineCap;
  final int lineJoin;
  final bool isFilled;
  @override
  final Rect? clipRect;

  @override
  void draw(Canvas c) {
    if (clipRect != null) {
      c.save();
      c.clipRect(clipRect!);
    }

    final paint = Paint()
      ..color = color
      ..style = isFilled ? PaintingStyle.fill : PaintingStyle.stroke;

    if (!isFilled) {
      paint
        ..strokeWidth = strokeWidth
        ..strokeCap = getStrokeCap(lineCap)
        ..strokeJoin = getStrokeJoin(lineJoin);
    }

    if (isFilled) {
      final path = Path();
      final center = rect.center;
      path.moveTo(center.dx, center.dy);
      path.arcTo(rect, startAngle, sweepAngle, false);
      path.close();
      c.drawPath(path, paint);
    } else {
      c.drawArc(rect, startAngle, sweepAngle, false, paint);
    }

    if (clipRect != null) {
      c.restore();
    }
  }

  @override
  String toString() =>
      '${isFilled ? "Fill" : "Draw"}Arc $rect (${_radToDeg(startAngle)}° + ${_radToDeg(sweepAngle)}°)${clipRect != null ? " [clipped]" : ""}';

  double _radToDeg(double rad) => rad * 180 / 3.14159;
}

class RoundRectShape extends Shape {
  RoundRectShape(this.rect, this.radiusX, this.radiusY, this.color,
      this.strokeWidth, this.lineCap, this.lineJoin,
      {this.isFilled = false, this.clipRect});

  final Rect rect;
  final double radiusX;
  final double radiusY;
  final Color color;
  final double strokeWidth;
  final int lineCap;
  final int lineJoin;
  final bool isFilled;
  @override
  final Rect? clipRect;

  @override
  void draw(Canvas c) {
    if (clipRect != null) {
      c.save();
      c.clipRect(clipRect!);
    }

    final rrect = RRect.fromRectAndCorners(
      rect,
      topLeft: Radius.elliptical(radiusX, radiusY),
      topRight: Radius.elliptical(radiusX, radiusY),
      bottomLeft: Radius.elliptical(radiusX, radiusY),
      bottomRight: Radius.elliptical(radiusX, radiusY),
    );

    c.drawRRect(
        rrect,
        Paint()
          ..color = color
          ..style = isFilled ? PaintingStyle.fill : PaintingStyle.stroke
          ..strokeWidth = strokeWidth
          ..strokeCap = getStrokeCap(lineCap)
          ..strokeJoin = getStrokeJoin(lineJoin));

    if (clipRect != null) {
      c.restore();
    }
  }

  @override
  String toString() =>
      '${isFilled ? "Fill" : ""}RoundRect $rect (radius: $radiusX x $radiusY)${clipRect != null ? " [clipped]" : ""}';
}

class PointShape extends Shape {
  PointShape(this.point, this.color, [this.clipRect]);

  final Offset point;
  final Color color;
  @override
  final Rect? clipRect;

  @override
  void draw(Canvas c) {
    if (clipRect != null) {
      c.save();
      c.clipRect(clipRect!);
    }

    c.drawRect(
        Rect.fromLTWH(point.dx, point.dy, 1, 1),
        Paint()
          ..color = color
          ..style = PaintingStyle.fill);

    if (clipRect != null) {
      c.restore();
    }
  }

  @override
  String toString() => 'Point $point${clipRect != null ? " [clipped]" : ""}';
}

class FocusRectShape extends Shape {
  FocusRectShape(this.rect, this.color, [this.clipRect]);

  final Rect rect;
  final Color color;
  @override
  final Rect? clipRect;

  @override
  void draw(Canvas c) {
    if (clipRect != null) {
      c.save();
      c.clipRect(clipRect!);
    }

    final paint = Paint()
      ..color = color
      ..style = PaintingStyle.stroke
      ..strokeWidth = 1;

    final Path path = Path();
    _addDottedLine(
        path, Offset(rect.left, rect.top), Offset(rect.right, rect.top));
    _addDottedLine(
        path, Offset(rect.right, rect.top), Offset(rect.right, rect.bottom));
    _addDottedLine(
        path, Offset(rect.right, rect.bottom), Offset(rect.left, rect.bottom));
    _addDottedLine(
        path, Offset(rect.left, rect.bottom), Offset(rect.left, rect.top));

    c.drawPath(path, paint);

    if (clipRect != null) {
      c.restore();
    }
  }

  void _addDottedLine(Path path, Offset start, Offset end) {
    final dx = end.dx - start.dx;
    final dy = end.dy - start.dy;
    final distance = math.sqrt(dx * dx + dy * dy);
    final dashLength = 2.0;
    final gapLength = 2.0;
    final steps = (distance / (dashLength + gapLength)).floor();

    final stepX = dx / steps;
    final stepY = dy / steps;

    double x = start.dx;
    double y = start.dy;

    for (int i = 0; i < steps; i++) {
      path.moveTo(x, y);
      x += stepX * dashLength / (dashLength + gapLength);
      y += stepY * dashLength / (dashLength + gapLength);
      path.lineTo(x, y);

      x += stepX * gapLength / (dashLength + gapLength);
      y += stepY * gapLength / (dashLength + gapLength);
    }

    if (x < end.dx || y < end.dy) {
      path.moveTo(x, y);
      path.lineTo(end.dx, end.dy);
    }
  }

  @override
  String toString() => 'FocusRect $rect${clipRect != null ? " [clipped]" : ""}';
}

enum ImageType { raster, svg }

class ImageShape extends Shape {
  ImageShape._({
    required this.type,
    required this.destRect,
    this.image,
    this.srcRect,
    this.pictureInfo,
    this.clipRect,
  });

  factory ImageShape.raster(ui.Image image, Rect srcRect, Rect destRect,
      {Rect? clipRect}) {
    return ImageShape._(
      type: ImageType.raster,
      image: image,
      srcRect: srcRect,
      destRect: destRect,
      clipRect: clipRect,
    );
  }

  factory ImageShape.svg(PictureInfo pictureInfo, Rect destRect,
      {Rect? clipRect}) {
    return ImageShape._(
      type: ImageType.svg,
      pictureInfo: pictureInfo,
      destRect: destRect,
      clipRect: clipRect,
    );
  }

  static Future<ImageShape> fromVImage(
      VImage vImage, VGCDrawImage opArgs, Rect? clipRect) async {
    Object? replacement;
    if (vImage.filename != null && vImage.filename!.isNotEmpty) {
      replacement = await AssetsManager.loadReplacement(vImage.filename!);
    }

    if (replacement is String) {
      final pictureInfo =
          await vg.loadPicture(SvgStringLoader(replacement), null);
      final destRect = Rect.fromLTWH(
        (opArgs.destX ?? 0).toDouble(),
        (opArgs.destY ?? 0).toDouble(),
        (opArgs.destWidth == -1)
            ? pictureInfo.size.width
            : opArgs.destWidth!.toDouble(),
        (opArgs.destHeight == -1)
            ? pictureInfo.size.height
            : opArgs.destHeight!.toDouble(),
      );
      return ImageShape.svg(pictureInfo, destRect, clipRect: clipRect);
    }

    ui.Image? uiImage = replacement as ui.Image? ??
        await ImageUtils.decodeVImageToUIImage(vImage);
    if (uiImage == null) throw Exception('Failed to load image');

    final destRect = Rect.fromLTWH(
      (opArgs.destX ?? 0).toDouble(),
      (opArgs.destY ?? 0).toDouble(),
      (opArgs.destWidth == -1)
          ? uiImage.width.toDouble()
          : opArgs.destWidth!.toDouble(),
      (opArgs.destHeight == -1)
          ? uiImage.height.toDouble()
          : opArgs.destHeight!.toDouble(),
    );

    final srcRect = replacement != null
        ? Rect.fromLTWH(
            0, 0, uiImage.width.toDouble(), uiImage.height.toDouble())
        : Rect.fromLTWH(
            (opArgs.srcX ?? 0).toDouble(),
            (opArgs.srcY ?? 0).toDouble(),
            (opArgs.srcWidth == -1)
                ? uiImage.width.toDouble()
                : opArgs.srcWidth!.toDouble(),
            (opArgs.srcHeight == -1)
                ? uiImage.height.toDouble()
                : opArgs.srcHeight!.toDouble(),
          );

    return ImageShape.raster(uiImage, srcRect, destRect, clipRect: clipRect);
  }

  static Future<ui.Image> _scaleImage(
      ui.Image image, int targetWidth, int targetHeight) async {
    if (image.width == targetWidth && image.height == targetHeight) {
      return image;
    }
    final recorder = ui.PictureRecorder();
    final canvas = Canvas(recorder);
    final srcRect =
        Rect.fromLTWH(0, 0, image.width.toDouble(), image.height.toDouble());
    final destRect =
        Rect.fromLTWH(0, 0, targetWidth.toDouble(), targetHeight.toDouble());

    canvas.drawImageRect(
        image,
        srcRect,
        destRect,
        Paint()
          ..filterQuality = FilterQuality.high
          ..isAntiAlias = true);

    final picture = recorder.endRecording();
    return picture.toImage(targetWidth, targetHeight);
  }

  final ImageType type;
  final Rect destRect;
  final ui.Image? image;
  final Rect? srcRect;
  final PictureInfo? pictureInfo;
  @override
  final Rect? clipRect;

  @override
  void draw(Canvas c) {
    if (clipRect != null) {
      c.save();
      c.clipRect(clipRect!);
    }

    switch (type) {
      case ImageType.raster:
        _drawRaster(c);
      case ImageType.svg:
        _drawSvg(c);
    }

    if (clipRect != null) {
      c.restore();
    }
  }

  void _drawRaster(Canvas c) {
    if (image == null || srcRect == null) return;

    final paint = Paint()
      ..filterQuality = FilterQuality.high
      ..isAntiAlias = true;

    c.drawImageRect(image!, srcRect!, destRect, paint);
  }

  void _drawSvg(Canvas c) {
    if (pictureInfo == null) return;

    final double scaleX = destRect.width / pictureInfo!.size.width;
    final double scaleY = destRect.height / pictureInfo!.size.height;

    c.save();
    c.translate(destRect.left, destRect.top);
    c.scale(scaleX, scaleY);
    c.drawPicture(pictureInfo!.picture);
    c.restore();
  }

  @override
  String toString() {
    return switch (type) {
      ImageType.raster =>
        'ImageShape(raster) ${image?.width}x${image?.height} dest:$destRect${clipRect != null ? " [clipped]" : ""}',
      ImageType.svg =>
        'ImageShape(svg) dest:$destRect${clipRect != null ? " [clipped]" : ""}',
    };
  }
}

StrokeCap getStrokeCap(int swtCap) {
  switch (swtCap) {
    case SWT.CAP_FLAT:
      return StrokeCap.butt;
    case SWT.CAP_ROUND:
      return StrokeCap.round;
    case SWT.CAP_SQUARE:
      return StrokeCap.square;
    default:
      return StrokeCap.butt;
  }
}

StrokeJoin getStrokeJoin(int swtJoin) {
  switch (swtJoin) {
    case SWT.JOIN_MITER:
      return StrokeJoin.miter;
    case SWT.JOIN_ROUND:
      return StrokeJoin.round;
    case SWT.JOIN_BEVEL:
      return StrokeJoin.bevel;
    default:
      return StrokeJoin.miter;
  }
}
