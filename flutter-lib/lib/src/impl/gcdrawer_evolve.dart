import 'dart:async';
import 'dart:convert';
import 'dart:math' as math;
import 'dart:ui' as ui;
import 'dart:typed_data';

import 'package:flutter/material.dart';
import 'package:flutter/rendering.dart' show RenderRepaintBoundary;
import 'package:flutter_svg/flutter_svg.dart';

import '../comm/comm.dart';
import '../gen/gc.dart';
import '../gen/gcdrawer.dart';
import '../gen/image.dart';
import '../gen/swt.dart';
import 'assets_manager.dart';
import 'color_utils.dart';
import 'utils/font_utils.dart';
import 'utils/image_utils.dart';

// ─────────────────────────────────────────────
// GCDrawer — concrete shape engine
// ─────────────────────────────────────────────

class GCDrawer extends GCDrawerBase {
  final List<Shape> shapes = [];
  final void Function(List<Shape>)? onShapesUpdated;

  // BuildContext for text style resolution (widget mode only, null in image mode).
  // Updated by GCImpl before each build via syncContext().
  BuildContext? _context;

  // Standalone image mode state
  ui.Image? _baseImage;
  int _imgWidth = 0;
  int _imgHeight = 0;
  final List<Future<ImageShape>> _pendingImages = [];
  Completer<void>? _baseImageCompleter;

  // Set by GCImpl when the parent WidgetSwtState provides a RepaintBoundary key.
  // Used in copyArea to capture the widget's actual rendered pixels via toImageSync().
  GlobalKey? widgetBoundaryKey;

  /// Standalone mode: registers comm listeners for state + all draw ops + imageInit/gcDispose.
  /// Used for headless image rendering (new GC(image)).
  GCDrawer.standalone(VGC state) : onShapesUpdated = null, super(state) {
    EquoCommService.onRaw("${state.swt}/${state.id}/imageInit", (payload) {
      _baseImageCompleter = Completer<void>();
      _handleImageInit(payload).then(
        (_) => _baseImageCompleter!.complete(),
        onError: (e) => _baseImageCompleter!.completeError(e),
      );
    });
    EquoCommService.onRaw("${state.swt}/${state.id}/gcDispose", (_) async {
      if (_baseImageCompleter != null) await _baseImageCompleter!.future;
      await Future.wait(_pendingImages);
      await _renderAndSend();
    });
  }

  /// Embedded mode: registers comm listeners for state + all draw ops + gcDispose.
  /// onShapesUpdated triggers GCImpl.setState().
  GCDrawer.embedded(VGC state, {this.onShapesUpdated}) : super(state) {
    EquoCommService.onRaw("${state.swt}/${state.id}/gcDispose", (_) {
      shapes.clear();
      onShapesUpdated?.call(shapes);
    });
  }

  /// Called by GCImpl.build() to provide context for text style resolution.
  void syncContext(BuildContext ctx) => _context = ctx;

  // ── State getters (read from VGC) ──────────────────────────────────────

  Color get bg => colorFromVColor(state.background, defaultColor: const Color(0xFFFFFFFF));
  Color get fg => colorFromVColor(state.foreground, defaultColor: const Color(0xFF333232));
  double get lineWidth => (state.lineWidth ?? 1).toDouble();
  int get lineCap => state.lineCap ?? 1;
  int get lineJoin => state.lineJoin ?? 1;

  Rect? get clipping {
    if (state.clipping == null) return null;
    final width = state.clipping!.width?.toDouble() ?? 0;
    final height = state.clipping!.height?.toDouble() ?? 0;
    if (width <= 0 || height <= 0) return null;
    return Rect.fromLTWH(
      state.clipping!.x?.toDouble() ?? 0,
      state.clipping!.y?.toDouble() ?? 0,
      width,
      height,
    );
  }

  Color applyAlpha(Color color) {
    final alpha = state.alpha ?? 255;
    if (alpha == 255) return color;
    return color.withOpacity(alpha / 255.0);
  }

  // ── Shape helpers ───────────────────────────────────────────────────────

  void clearShapes() => shapes.clear();

  void _addShape(Shape shape) {
    shapes.add(shape);
    onShapesUpdated?.call(shapes);
  }

  Rect _getRectFromArgs(int? x, int? y, int? w, int? h) => Rect.fromLTWH(
        (x ?? 0).toDouble(),
        (y ?? 0).toDouble(),
        (w ?? 0).toDouble(),
        (h ?? 0).toDouble(),
      );

  double _degToRad(double deg) => deg * math.pi / 180;

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
    final color = isFilled ? applyAlpha(bg) : applyAlpha(fg);
    final strokeWidth = isFilled ? 0.0 : lineWidth;
    _addShape(RoundRectShape(
        rect, arcWidth / 2.0, arcHeight / 2.0, color, strokeWidth, lineCap, lineJoin,
        isFilled: isFilled, clipRect: clipping));
  }

  void _addOvalShape({
    required int x,
    required int y,
    required int width,
    required int height,
    required bool isFilled,
  }) {
    final rect = _getRectFromArgs(x, y, width, height);
    final color = isFilled ? applyAlpha(bg) : applyAlpha(fg);
    final strokeWidth = isFilled ? 0.0 : lineWidth;
    _addShape(OvalShape(rect, color, strokeWidth,
        isFilled: isFilled, clipRect: clipping));
  }

  void _addRectShape({
    required int x,
    required int y,
    required int width,
    required int height,
    required bool isFilled,
  }) {
    final rect = _getRectFromArgs(x, y, width, height);
    final color = isFilled ? applyAlpha(bg) : applyAlpha(fg);
    final strokeWidth = isFilled ? 0.0 : lineWidth;
    _addShape(RectShape(rect, color, strokeWidth, lineCap, lineJoin,
        isFilled: isFilled, clipRect: clipping));
  }

  void _addPolygonShape({
    required List<int> points,
    required bool isFilled,
    int minPoints = 6,
  }) {
    if (points.length >= minPoints && points.length % 2 == 0) {
      final color = isFilled ? applyAlpha(bg) : applyAlpha(fg);
      final strokeWidth = isFilled ? 0.0 : lineWidth;
      _addShape(PolygonShape(points, color, strokeWidth, lineCap, lineJoin,
          isFilled: isFilled, clipRect: clipping));
    }
  }

  void _addPolylineShape({
    required List<int> points,
    required bool isFilled,
    int minPoints = 2,
  }) {
    if (points.length >= minPoints && points.length % 2 == 0) {
      final color = isFilled ? applyAlpha(bg) : applyAlpha(fg);
      final strokeWidth = isFilled ? 0.0 : lineWidth;
      _addShape(PolylineShape(points, color, strokeWidth, lineCap, lineJoin,
          isFilled: isFilled, clipRect: clipping));
    }
  }

  void _addArcShape({
    required int x,
    required int y,
    required int width,
    required int height,
    required int startAngle,
    required int arcAngle,
    required bool isFilled,
  }) {
    final rect = _getRectFromArgs(x, y, width, height);
    final color = isFilled ? applyAlpha(bg) : applyAlpha(fg);
    final strokeWidth = isFilled ? 0.0 : lineWidth;
    _addShape(ArcShape(
        rect,
        _degToRad(-startAngle.toDouble()),
        _degToRad(-arcAngle.toDouble()),
        color,
        strokeWidth,
        lineCap,
        lineJoin,
        isFilled: isFilled,
        clipRect: clipping));
  }

  void _drawText({
    required String text,
    required double x,
    required double y,
    int flags = 0,
    bool? isTransparent,
  }) {
    final processedText = _processTextFlags(text, flags);
    final transparent = isTransparent ?? (flags & SWT.DRAW_TRANSPARENT) != 0;

    final textStyle = FontUtils.textStyleFromVFont(
      state.font,
      _context,
      color: applyAlpha(fg),
      applyDpiScaling: true,
    );

    if (!transparent) {
      final textPainter = TextPainter(
        text: TextSpan(text: processedText, style: textStyle),
        textDirection: TextDirection.ltr,
      )..layout();
      final rect = Rect.fromLTWH(x, y, textPainter.width, textPainter.height);
      _addShape(RectShape(rect, applyAlpha(bg), 0, lineCap, lineJoin,
          isFilled: true, clipRect: clipping));
    }

    _addShape(TextShape(processedText, Offset(x, y), textStyle, clipping));
  }

  String _processTextFlags(String text, int flags) {
    return text
        .replaceAll('\r\n', (flags & SWT.DRAW_DELIMITER) != 0 ? '\n' : ' ')
        .replaceAll('\r', (flags & SWT.DRAW_DELIMITER) != 0 ? '\n' : ' ')
        .replaceAll('\n', (flags & SWT.DRAW_DELIMITER) != 0 ? '\n' : ' ')
        .replaceAll('\t', (flags & SWT.DRAW_TAB) != 0 ? '    ' : ' ');
  }

  // ── Image handling ──────────────────────────────────────────────────────

  Future<void> _handleImageInit(dynamic payload) async {
    final vImage = VImage.fromJson(
        jsonDecode(payload as String) as Map<String, dynamic>);
    _imgWidth = vImage.imageData?.width ?? 0;
    _imgHeight = vImage.imageData?.height ?? 0;
    _baseImage = await ImageUtils.decodeVImageToUIImage(vImage);
  }

  Future<void> _renderAndSend() async {
    final w = _imgWidth > 0 ? _imgWidth : 1;
    final h = _imgHeight > 0 ? _imgHeight : 1;

    final recorder = ui.PictureRecorder();
    final canvas = ui.Canvas(recorder);

    if (_baseImage != null) {
      canvas.drawImage(_baseImage!, ui.Offset.zero, ui.Paint());
    } else {
      canvas.drawRect(
        ui.Rect.fromLTWH(0, 0, w.toDouble(), h.toDouble()),
        ui.Paint()..color = const ui.Color(0xFFFFFFFF),
      );
    }

    for (final shape in shapes) {
      shape.draw(canvas);
    }

    final picture = recorder.endRecording();
    final image = await picture.toImage(w, h);
    final byteData = await image.toByteData(format: ui.ImageByteFormat.png);
    if (byteData == null) return;

    final encoded = base64Encode(byteData.buffer.asUint8List());
    EquoCommService.sendPayload('${state.swt}/${state.id}/imageResult', encoded);

    _unregisterImageListeners();
  }

  void _unregisterImageListeners() {
    EquoCommService.remove('${state.swt}/${state.id}/imageInit');
    EquoCommService.remove('${state.swt}/${state.id}/gcDispose');
  }

  // ── CopyArea helpers ────────────────────────────────────────────────────

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
      PolygonShape s => s.points.indexed
          .where((e) => e.$1 % 2 == 0)
          .any((e) => area.contains(
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
      RectShape s => RectShape(
          s.rect.translate(offset.dx, offset.dy), s.color, s.strokeWidth,
          s.lineCap, s.lineJoin,
          isFilled: s.isFilled, clipRect: clipArea),
      OvalShape s => OvalShape(
          s.rect.translate(offset.dx, offset.dy), s.color, s.strokeWidth,
          isFilled: s.isFilled, clipRect: clipArea),
      TextShape s => TextShape(s.text, s.off + offset, s.style, clipArea),
      PointShape s => PointShape(s.point + offset, s.color, clipArea),
      PolygonShape s => PolygonShape(
          _translatePoints(s.points, offset), s.color, s.strokeWidth,
          s.lineCap, s.lineJoin,
          isFilled: s.isFilled, clipRect: clipArea),
      ArcShape s => ArcShape(
          s.rect.translate(offset.dx, offset.dy), s.startAngle, s.sweepAngle,
          s.color, s.strokeWidth, s.lineCap, s.lineJoin,
          isFilled: s.isFilled, clipRect: clipArea),
      RoundRectShape s => RoundRectShape(
          s.rect.translate(offset.dx, offset.dy), s.radiusX, s.radiusY,
          s.color, s.strokeWidth, s.lineCap, s.lineJoin,
          isFilled: s.isFilled, clipRect: clipArea),
      GradientRectShape s => GradientRectShape(
          s.rect.translate(offset.dx, offset.dy),
          s.fromColor, s.toColor, s.vertical, clipArea),
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

  Future<ui.Image> _renderShapesToUiImage(List<Shape> shapes, Size size) async {
    final recorder = ui.PictureRecorder();
    final canvas = ui.Canvas(recorder);
    canvas.drawRect(
        Offset.zero & size, ui.Paint()..color = const Color(0xFFFFFFFF));
    for (final shape in shapes) {
      shape.draw(canvas);
    }
    final picture = recorder.endRecording();
    return picture.toImage(size.width.toInt(), size.height.toInt());
  }

  // ── Op implementations ──────────────────────────────────────────────────

  @override
  void onDrawArcintintintintintint(VGCDrawArcintintintintintint o) =>
      _addArcShape(x: o.x ?? 0, y: o.y ?? 0, width: o.width ?? 0,
          height: o.height ?? 0, startAngle: o.startAngle ?? 0,
          arcAngle: o.arcAngle ?? 0, isFilled: false);

  @override
  void onFillArcintintintintintint(VGCFillArcintintintintintint o) =>
      _addArcShape(x: o.x ?? 0, y: o.y ?? 0, width: o.width ?? 0,
          height: o.height ?? 0, startAngle: o.startAngle ?? 0,
          arcAngle: o.arcAngle ?? 0, isFilled: true);

  @override
  void onDrawFocusintintintint(VGCDrawFocusintintintint o) {
    final rect = _getRectFromArgs(o.x, o.y, o.width, o.height);
    _addShape(FocusRectShape(rect, applyAlpha(fg), clipping));
  }

  @override
  void onDrawImageImageintint(VGCDrawImageImageintint o) {
    if (o.image == null) return;
    // Use -1 as sentinel for "use natural image size"
    final opArgs = VGCDrawImageImageintintintintintintintint(
      destX: o.x, destY: o.y, destWidth: -1, destHeight: -1,
      srcX: 0, srcY: 0, srcWidth: -1, srcHeight: -1,
    );
    opArgs.image = o.image;
    _processImageAsync(o.image!, opArgs, clipping);
  }

  @override
  void onDrawImageImageintintintint(VGCDrawImageImageintintintint o) {
    if (o.image == null) return;
    final opArgs = VGCDrawImageImageintintintintintintintint(
      destX: o.destX, destY: o.destY, destWidth: o.destWidth, destHeight: o.destHeight,
      srcX: 0, srcY: 0, srcWidth: -1, srcHeight: -1,
    );
    opArgs.image = o.image;
    _processImageAsync(o.image!, opArgs, clipping);
  }

  @override
  void onDrawImageImageintintintintintintintint(
      VGCDrawImageImageintintintintintintintint o) {
    if (o.image == null) return;
    final capturedClipping = clipping;
    final idx = shapes.length;
    shapes.add(_PlaceholderShape());
    final f = ImageShape.fromVImageDetailed(o.image!, o, capturedClipping);
    _pendingImages.add(f);
    f.then((s) {
      shapes[idx] = s;
      onShapesUpdated?.call(shapes);
    });
  }

  void _processImageAsync(
    VImage vImage,
    VGCDrawImageImageintintintintintintintint opArgs,
    Rect? capturedClipping,
  ) async {
    final imageShape =
        await ImageShape.fromVImageDetailed(vImage, opArgs, capturedClipping);
    _addShape(imageShape);
  }

  @override
  void onDrawLineintintintint(VGCDrawLineintintintint o) {
    _addShape(LineShape(
        Offset((o.x1 ?? 0).toDouble(), (o.y1 ?? 0).toDouble()),
        Offset((o.x2 ?? 0).toDouble(), (o.y2 ?? 0).toDouble()),
        applyAlpha(fg), lineWidth, lineCap, lineJoin, clipping));
  }

  @override
  void onDrawOvalintintintint(VGCDrawOvalintintintint o) =>
      _addOvalShape(x: o.x ?? 0, y: o.y ?? 0, width: o.width ?? 0,
          height: o.height ?? 0, isFilled: false);

  @override
  void onFillOvalintintintint(VGCFillOvalintintintint o) =>
      _addOvalShape(x: o.x ?? 0, y: o.y ?? 0, width: o.width ?? 0,
          height: o.height ?? 0, isFilled: true);

  @override
  void onDrawPointintint(VGCDrawPointintint o) {
    _addShape(PointShape(
        Offset((o.x ?? 0).toDouble(), (o.y ?? 0).toDouble()),
        applyAlpha(fg), clipping));
  }

  @override
  void onDrawPolygonint(VGCDrawPolygonint o) =>
      _addPolygonShape(points: o.pointArray ?? [], isFilled: false, minPoints: 6);

  @override
  void onFillPolygonint(VGCFillPolygonint o) =>
      _addPolygonShape(points: o.pointArray ?? [], isFilled: true, minPoints: 6);

  @override
  void onDrawPolylineint(VGCDrawPolylineint o) =>
      _addPolylineShape(points: o.pointArray ?? [], isFilled: false, minPoints: 2);

  @override
  void onDrawRectangleRectangle(VGCDrawRectangleRectangle o) {
    if (o.rect == null) return;
    _addRectShape(x: o.rect!.x, y: o.rect!.y, width: o.rect!.width,
        height: o.rect!.height, isFilled: false);
  }

  @override
  void onDrawRectangleintintintint(VGCDrawRectangleintintintint o) =>
      _addRectShape(x: o.x ?? 0, y: o.y ?? 0, width: o.width ?? 0,
          height: o.height ?? 0, isFilled: false);

  @override
  void onFillRectangleintintintint(VGCFillRectangleintintintint o) =>
      _addRectShape(x: o.x ?? 0, y: o.y ?? 0, width: o.width ?? 0,
          height: o.height ?? 0, isFilled: true);

  @override
  void onFillRectangleRectangle(VGCFillRectangleRectangle o) {
    if (o.rect == null) return;
    _addRectShape(x: o.rect!.x, y: o.rect!.y, width: o.rect!.width,
        height: o.rect!.height, isFilled: true);
  }

  @override
  void onDrawRoundRectangleintintintintintint(
      VGCDrawRoundRectangleintintintintintint o) =>
      _addRoundRectShape(x: o.x ?? 0, y: o.y ?? 0, width: o.width ?? 0,
          height: o.height ?? 0, arcWidth: o.arcWidth ?? 0,
          arcHeight: o.arcHeight ?? 0, isFilled: false);

  @override
  void onFillRoundRectangleintintintintintint(
      VGCFillRoundRectangleintintintintintint o) =>
      _addRoundRectShape(x: o.x ?? 0, y: o.y ?? 0, width: o.width ?? 0,
          height: o.height ?? 0, arcWidth: o.arcWidth ?? 0,
          arcHeight: o.arcHeight ?? 0, isFilled: true);

  @override
  void onDrawStringStringintint(VGCDrawStringStringintint o) =>
      _drawText(text: o.string ?? '', x: (o.x ?? 0).toDouble(),
          y: (o.y ?? 0).toDouble());

  @override
  void onDrawStringStringintintboolean(VGCDrawStringStringintintboolean o) =>
      _drawText(text: o.string ?? '', x: (o.x ?? 0).toDouble(),
          y: (o.y ?? 0).toDouble(), isTransparent: o.isTransparent ?? false);

  @override
  void onDrawTextStringintint(VGCDrawTextStringintint o) =>
      _drawText(text: o.string ?? '', x: (o.x ?? 0).toDouble(),
          y: (o.y ?? 0).toDouble());

  @override
  void onDrawTextStringintintboolean(VGCDrawTextStringintintboolean o) =>
      _drawText(text: o.string ?? '', x: (o.x ?? 0).toDouble(),
          y: (o.y ?? 0).toDouble(), isTransparent: o.isTransparent ?? false);

  @override
  void onDrawTextStringintintint(VGCDrawTextStringintintint o) =>
      _drawText(text: o.string ?? '', x: (o.x ?? 0).toDouble(),
          y: (o.y ?? 0).toDouble(), flags: o.flags ?? 0);

  @override
  void onFillGradientRectangleintintintintboolean(
      VGCFillGradientRectangleintintintintboolean o) {
    final rect = _getRectFromArgs(o.x, o.y, o.width, o.height);
    _addShape(GradientRectShape(
        rect, applyAlpha(fg), applyAlpha(bg), o.vertical ?? false, clipping));
  }

  @override
  void onCopyAreaintintintintintintboolean(
      VGCCopyAreaintintintintintintboolean o) {
    final srcRect = _getRectFromArgs(o.srcX, o.srcY, o.width, o.height);
    final destOffset = Offset(
      (o.destX ?? 0) - (o.srcX ?? 0).toDouble(),
      (o.destY ?? 0) - (o.srcY ?? 0).toDouble(),
    );
    final copiedShapes = shapes
        .where((shape) => _shapeIntersects(shape, srcRect))
        .map((shape) => _translateShapeWithClip(shape, destOffset, srcRect))
        .toList();
    for (final s in copiedShapes) {
      _addShape(s);
    }
  }

  @override
  void onCopyAreaImageintint(VGCCopyAreaImageintint o) async {
    try {
      final imgW = o.image?.imageData?.width ?? 0;
      final imgH = o.image?.imageData?.height ?? 0;
      if (imgW <= 0 || imgH <= 0) return;
      final x = (o.x ?? 0).toDouble();
      final y = (o.y ?? 0).toDouble();

      // Determine the canvas size needed to include the area being captured.
      if (_baseImageCompleter != null) await _baseImageCompleter!.future;
      final renderW = math.max(
          _imgWidth > 0 ? _imgWidth : (x + imgW).toInt(), (x + imgW).toInt());
      final renderH = math.max(
          _imgHeight > 0 ? _imgHeight : (y + imgH).toInt(),
          (y + imgH).toInt());

      // Render: widget pixels (via RepaintBoundary) > base image > white background.
      // Then apply any shapes drawn via this GC on top.
      final recorder = ui.PictureRecorder();
      final canvas = ui.Canvas(recorder);
      if (widgetBoundaryKey != null) {
        // Capture the widget's current raster cache synchronously.
        // toImageSync() reads the already-rasterized frame — no GPU pass needed,
        // so it is safe to call while the SWT main thread is blocked.
        final ro = widgetBoundaryKey!.currentContext?.findRenderObject();
        if (ro is RenderRepaintBoundary) {
          final widgetImage = ro.toImageSync(pixelRatio: 1.0);
          canvas.drawImage(widgetImage, ui.Offset.zero, ui.Paint());
          widgetImage.dispose();
        }
      } else if (_baseImage != null) {
        canvas.drawImage(_baseImage!, ui.Offset.zero, ui.Paint());
      } else {
        canvas.drawRect(
          Rect.fromLTWH(0, 0, renderW.toDouble(), renderH.toDouble()),
          ui.Paint()..color = const Color(0xFFFFFFFF),
        );
      }
      for (final shape in shapes) {
        shape.draw(canvas);
      }
      final fullImage =
      await recorder.endRecording().toImage(renderW, renderH);

      // Crop to the requested area: (x, y, imgW, imgH) → (0, 0, imgW, imgH).
      final cropRecorder = ui.PictureRecorder();
      final cropCanvas = ui.Canvas(cropRecorder);
      cropCanvas.drawImageRect(
        fullImage,
        Rect.fromLTWH(x, y, imgW.toDouble(), imgH.toDouble()),
        Rect.fromLTWH(0, 0, imgW.toDouble(), imgH.toDouble()),
        ui.Paint(),
      );
      final result =
      await cropRecorder.endRecording().toImage(imgW, imgH);

      final byteData = await result.toByteData(format: ui.ImageByteFormat.png);
      if (byteData == null) return;
      EquoCommService.sendPayload(
        '${state.swt}/${state.id}/copyAreaImageintintResponse',
        jsonEncode(base64Encode(byteData.buffer.asUint8List())),
      );
    } catch (e, stack) {
      print('[GC copyArea] Error: $e\n$stack');
    }
  }

  @override
  void onCopyAreaintintintintintint(VGCCopyAreaintintintintintint o) {
    final srcRect = _getRectFromArgs(o.srcX, o.srcY, o.width, o.height);
    final destOffset = Offset(
      (o.destX).toDouble() - (o.srcX).toDouble(),
      (o.destY).toDouble() - (o.srcY).toDouble(),
    );
    final copiedShapes = shapes
        .where((shape) => _shapeIntersects(shape, srcRect))
        .map((shape) => _translateShapeWithClip(shape, destOffset, srcRect))
        .toList();
    for (final s in copiedShapes) {
      _addShape(s);
    }
  }

  @override
  void dispose() {
    super.dispose();
    _unregisterImageListeners();
  }
}

class _PlaceholderShape extends Shape {
  @override
  void draw(ui.Canvas c) {}
  @override
  String toString() => 'Placeholder';
}

// ─────────────────────────────────────────────
// ScenePainter
// ─────────────────────────────────────────────

class ScenePainter extends CustomPainter {
  ScenePainter(this.bg, this.shapes);
  final Color bg;
  final List<Shape> shapes;

  @override
  void paint(Canvas canvas, Size size) {
    canvas.drawRect(Offset.zero & size, Paint()..color = bg);
    canvas.save();
    for (final s in shapes) {
      s.draw(canvas);
    }
    canvas.restore();
  }

  @override
  bool shouldRepaint(ScenePainter o) => o.bg != bg || o.shapes != shapes;
}

// ─────────────────────────────────────────────
// Shape classes
// ─────────────────────────────────────────────

abstract class Shape {
  void draw(ui.Canvas c);
  @override
  String toString();
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
  void draw(ui.Canvas c) {
    if (clipRect != null) { c.save(); c.clipRect(clipRect!); }
    final tp = TextPainter(
        text: TextSpan(text: text, style: style),
        textDirection: TextDirection.ltr)
      ..layout();
    tp.paint(c, off);
    if (clipRect != null) c.restore();
  }

  @override
  String toString() => 'Text "$text" @ $off${clipRect != null ? " [clipped]" : ""}';
}

class LineShape extends Shape {
  LineShape(this.p1, this.p2, this.color, this.strokeWidth, this.lineCap,
      this.lineJoin, [this.clipRect]);
  final Offset p1, p2;
  final Color color;
  final double strokeWidth;
  final int lineCap;
  final int lineJoin;
  @override
  final Rect? clipRect;

  @override
  void draw(ui.Canvas c) {
    if (clipRect != null) { c.save(); c.clipRect(clipRect!); }
    c.drawLine(p1, p2, Paint()
      ..color = color
      ..strokeWidth = strokeWidth
      ..strokeCap = getStrokeCap(lineCap)
      ..strokeJoin = getStrokeJoin(lineJoin));
    if (clipRect != null) c.restore();
  }

  @override
  String toString() => 'Line $p1 → $p2${clipRect != null ? " [clipped]" : ""}';
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
  void draw(ui.Canvas c) {
    if (clipRect != null) { c.save(); c.clipRect(clipRect!); }
    c.drawOval(rect, Paint()
      ..color = color
      ..style = isFilled ? PaintingStyle.fill : PaintingStyle.stroke
      ..strokeWidth = strokeWidth);
    if (clipRect != null) c.restore();
  }

  @override
  String toString() => '${isFilled ? "Fill" : ""}Oval $rect';
}

class RectShape extends Shape {
  RectShape(this.rect, this.color, this.strokeWidth, this.lineCap, this.lineJoin,
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
  void draw(ui.Canvas c) {
    if (clipRect != null) { c.save(); c.clipRect(clipRect!); }
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
    if (clipRect != null) c.restore();
  }

  @override
  String toString() => '${isFilled ? "Fill" : ""}Rect $rect';
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
  void draw(ui.Canvas c) {
    if (clipRect != null) { c.save(); c.clipRect(clipRect!); }
    final gradient = LinearGradient(
      begin: vertical ? Alignment.topCenter : Alignment.centerLeft,
      end: vertical ? Alignment.bottomCenter : Alignment.centerRight,
      colors: [fromColor, toColor],
    );
    c.drawRect(rect, Paint()
      ..shader = gradient.createShader(rect)
      ..style = PaintingStyle.fill);
    if (clipRect != null) c.restore();
  }

  @override
  String toString() => 'GradientRect $rect';
}

class PolygonShape extends Shape {
  PolygonShape(this.points, this.color, this.strokeWidth, this.lineCap,
      this.lineJoin, {this.isFilled = false, this.clipRect});
  final List<int> points;
  final Color color;
  final double strokeWidth;
  final int lineCap;
  final int lineJoin;
  final bool isFilled;
  @override
  final Rect? clipRect;

  @override
  void draw(ui.Canvas c) {
    if (points.length < 6) return;
    if (clipRect != null) { c.save(); c.clipRect(clipRect!); }
    final path = Path()
      ..moveTo(points[0].toDouble(), points[1].toDouble());
    for (int i = 2; i < points.length; i += 2) {
      path.lineTo(points[i].toDouble(), points[i + 1].toDouble());
    }
    path.close();
    c.drawPath(path, Paint()
      ..color = color
      ..style = isFilled ? PaintingStyle.fill : PaintingStyle.stroke
      ..strokeWidth = strokeWidth
      ..strokeCap = getStrokeCap(lineCap)
      ..strokeJoin = getStrokeJoin(lineJoin));
    if (clipRect != null) c.restore();
  }

  @override
  String toString() => '${isFilled ? "Fill" : ""}Polygon ${points.length ~/ 2} pts';
}

class PolylineShape extends Shape {
  PolylineShape(this.points, this.color, this.strokeWidth, this.lineCap,
      this.lineJoin, {this.isFilled = false, this.clipRect});
  final List<int> points;
  final Color color;
  final double strokeWidth;
  final int lineCap;
  final int lineJoin;
  final bool isFilled;
  @override
  final Rect? clipRect;

  @override
  void draw(ui.Canvas c) {
    if (points.length < 4) return;
    if (clipRect != null) { c.save(); c.clipRect(clipRect!); }
    final path = Path()
      ..moveTo(points[0].toDouble(), points[1].toDouble());
    for (int i = 2; i < points.length; i += 2) {
      path.lineTo(points[i].toDouble(), points[i + 1].toDouble());
    }
    c.drawPath(path, Paint()
      ..color = color
      ..style = isFilled ? PaintingStyle.fill : PaintingStyle.stroke
      ..strokeWidth = strokeWidth
      ..strokeCap = getStrokeCap(lineCap)
      ..strokeJoin = getStrokeJoin(lineJoin));
    if (clipRect != null) c.restore();
  }

  @override
  String toString() => 'Polyline ${points.length ~/ 2} pts';
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
  void draw(ui.Canvas c) {
    if (clipRect != null) { c.save(); c.clipRect(clipRect!); }
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
      final path = Path()
        ..moveTo(rect.center.dx, rect.center.dy)
        ..arcTo(rect, startAngle, sweepAngle, false)
        ..close();
      c.drawPath(path, paint);
    } else {
      c.drawArc(rect, startAngle, sweepAngle, false, paint);
    }
    if (clipRect != null) c.restore();
  }

  @override
  String toString() => '${isFilled ? "Fill" : "Draw"}Arc $rect';
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
  void draw(ui.Canvas c) {
    if (clipRect != null) { c.save(); c.clipRect(clipRect!); }
    final rrect = RRect.fromRectAndCorners(rect,
        topLeft: Radius.elliptical(radiusX, radiusY),
        topRight: Radius.elliptical(radiusX, radiusY),
        bottomLeft: Radius.elliptical(radiusX, radiusY),
        bottomRight: Radius.elliptical(radiusX, radiusY));
    c.drawRRect(rrect, Paint()
      ..color = color
      ..style = isFilled ? PaintingStyle.fill : PaintingStyle.stroke
      ..strokeWidth = strokeWidth
      ..strokeCap = getStrokeCap(lineCap)
      ..strokeJoin = getStrokeJoin(lineJoin));
    if (clipRect != null) c.restore();
  }

  @override
  String toString() => '${isFilled ? "Fill" : ""}RoundRect $rect';
}

class PointShape extends Shape {
  PointShape(this.point, this.color, [this.clipRect]);
  final Offset point;
  final Color color;
  @override
  final Rect? clipRect;

  @override
  void draw(ui.Canvas c) {
    if (clipRect != null) { c.save(); c.clipRect(clipRect!); }
    c.drawRect(Rect.fromLTWH(point.dx, point.dy, 1, 1),
        Paint()..color = color..style = PaintingStyle.fill);
    if (clipRect != null) c.restore();
  }

  @override
  String toString() => 'Point $point';
}

class FocusRectShape extends Shape {
  FocusRectShape(this.rect, this.color, [this.clipRect]);
  final Rect rect;
  final Color color;
  @override
  final Rect? clipRect;

  @override
  void draw(ui.Canvas c) {
    if (clipRect != null) { c.save(); c.clipRect(clipRect!); }
    final paint = Paint()..color = color..style = PaintingStyle.stroke..strokeWidth = 1;
    final path = Path();
    _addDottedLine(path, Offset(rect.left, rect.top), Offset(rect.right, rect.top));
    _addDottedLine(path, Offset(rect.right, rect.top), Offset(rect.right, rect.bottom));
    _addDottedLine(path, Offset(rect.right, rect.bottom), Offset(rect.left, rect.bottom));
    _addDottedLine(path, Offset(rect.left, rect.bottom), Offset(rect.left, rect.top));
    c.drawPath(path, paint);
    if (clipRect != null) c.restore();
  }

  void _addDottedLine(Path path, Offset start, Offset end) {
    final dx = end.dx - start.dx;
    final dy = end.dy - start.dy;
    final distance = math.sqrt(dx * dx + dy * dy);
    const dashLength = 2.0;
    const gapLength = 2.0;
    final steps = (distance / (dashLength + gapLength)).floor();
    final stepX = dx / steps;
    final stepY = dy / steps;
    double x = start.dx, y = start.dy;
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
  String toString() => 'FocusRect $rect';
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
        type: ImageType.raster, image: image, srcRect: srcRect,
        destRect: destRect, clipRect: clipRect);
  }

  factory ImageShape.svg(PictureInfo pictureInfo, Rect destRect,
      {Rect? clipRect}) {
    return ImageShape._(
        type: ImageType.svg, pictureInfo: pictureInfo,
        destRect: destRect, clipRect: clipRect);
  }

  static Future<ImageShape> fromVImageDetailed(VImage vImage,
      VGCDrawImageImageintintintintintintintint opArgs, Rect? clipRect) async {
    Object? replacement;
    if (vImage.filename != null && vImage.filename!.isNotEmpty) {
      replacement = await AssetsManager.loadReplacement(vImage.filename!);
    }

    if (replacement is String) {
      final pictureInfo = await vg.loadPicture(SvgStringLoader(replacement), null);
      final destRect = Rect.fromLTWH(
        (opArgs.destX ?? 0).toDouble(),
        (opArgs.destY ?? 0).toDouble(),
        opArgs.destWidth == -1 ? pictureInfo.size.width : (opArgs.destWidth ?? 0).toDouble(),
        opArgs.destHeight == -1 ? pictureInfo.size.height : (opArgs.destHeight ?? 0).toDouble(),
      );
      return ImageShape.svg(pictureInfo, destRect, clipRect: clipRect);
    }

    ui.Image? uiImage =
        replacement as ui.Image? ?? await ImageUtils.decodeVImageToUIImage(vImage);
    if (uiImage == null) throw Exception('Failed to load image');

    final destRect = Rect.fromLTWH(
      (opArgs.destX ?? 0).toDouble(),
      (opArgs.destY ?? 0).toDouble(),
      opArgs.destWidth == -1 ? uiImage.width.toDouble() : (opArgs.destWidth ?? 0).toDouble(),
      opArgs.destHeight == -1 ? uiImage.height.toDouble() : (opArgs.destHeight ?? 0).toDouble(),
    );

    final srcRect = replacement != null
        ? Rect.fromLTWH(0, 0, uiImage.width.toDouble(), uiImage.height.toDouble())
        : Rect.fromLTWH(
            (opArgs.srcX ?? 0).toDouble(),
            (opArgs.srcY ?? 0).toDouble(),
            opArgs.srcWidth == -1 ? uiImage.width.toDouble() : (opArgs.srcWidth ?? 0).toDouble(),
            opArgs.srcHeight == -1 ? uiImage.height.toDouble() : (opArgs.srcHeight ?? 0).toDouble(),
          );

    return ImageShape.raster(uiImage, srcRect, destRect, clipRect: clipRect);
  }

  final ImageType type;
  final Rect destRect;
  final ui.Image? image;
  final Rect? srcRect;
  final PictureInfo? pictureInfo;
  @override
  final Rect? clipRect;

  @override
  void draw(ui.Canvas c) {
    if (clipRect != null) { c.save(); c.clipRect(clipRect!); }
    switch (type) {
      case ImageType.raster: _drawRaster(c);
      case ImageType.svg: _drawSvg(c);
    }
    if (clipRect != null) c.restore();
  }

  void _drawRaster(ui.Canvas c) {
    if (image == null || srcRect == null) return;
    c.drawImageRect(image!, srcRect!, destRect,
        Paint()..filterQuality = FilterQuality.high..isAntiAlias = true);
  }

  void _drawSvg(ui.Canvas c) {
    if (pictureInfo == null) return;
    final scaleX = destRect.width / pictureInfo!.size.width;
    final scaleY = destRect.height / pictureInfo!.size.height;
    c.save();
    c.translate(destRect.left, destRect.top);
    c.scale(scaleX, scaleY);
    c.drawPicture(pictureInfo!.picture);
    c.restore();
  }

  @override
  String toString() => 'ImageShape($type) dest:$destRect';
}

StrokeCap getStrokeCap(int swtCap) {
  switch (swtCap) {
    case SWT.CAP_FLAT: return StrokeCap.butt;
    case SWT.CAP_ROUND: return StrokeCap.round;
    case SWT.CAP_SQUARE: return StrokeCap.square;
    default: return StrokeCap.butt;
  }
}

StrokeJoin getStrokeJoin(int swtJoin) {
  switch (swtJoin) {
    case SWT.JOIN_MITER: return StrokeJoin.miter;
    case SWT.JOIN_ROUND: return StrokeJoin.round;
    case SWT.JOIN_BEVEL: return StrokeJoin.bevel;
    default: return StrokeJoin.miter;
  }
}