import 'dart:async';
import 'dart:convert';
import 'dart:math' as math;
import 'dart:typed_data';
import 'dart:ui' as ui;

import 'package:flutter/foundation.dart' show kIsWasm, kIsWeb;
import 'package:flutter/material.dart';
import 'package:flutter/rendering.dart' show RenderRepaintBoundary;
import 'package:flutter_svg/flutter_svg.dart';

import '../comm/channel_header.dart';
import '../comm/comm.dart';
import '../gen/widgets.dart' show WireChannel, WireType;
import 'utils/perf_marks.dart';
import 'utils/text_utils.dart';
import '../gen/color.dart';
import '../gen/font.dart';
import '../gen/gc.dart';
import '../gen/gcdrawer.dart';
import '../gen/image.dart';
import '../gen/path.dart';
import '../gen/pathdata.dart';
import '../gen/pattern.dart';
import '../gen/rgb.dart';
import '../gen/swt.dart';
import '../theme/theme_extensions/canvas_theme_extension.dart';
import 'assets_manager.dart';
import 'color_utils.dart';
import 'utils/font_utils.dart';
import 'utils/glyph_tone.dart';
import 'utils/image_utils.dart';
import 'utils/widget_utils.dart';
import 'styledtext_evolve.dart';
import 'widget_config.dart';
import '../comm/widget_wire.dart' show WidgetWireReader, WidgetWireWriter;

// ─────────────────────────────────────────────
// GCDrawer — concrete shape engine
// ─────────────────────────────────────────────

class GCDrawer extends GCDrawerBase {
  final List<Shape> shapes = [];
  final void Function(List<Shape>)? onShapesUpdated;

  /// What this drawer does with its own channels' frames, by mode: a standalone (image) drawer and an
  /// embedded (control) one answer differently. Null for a channel the mode does not answer.
  FutureOr<void> Function(Uint8List frame, int start)? _imageInit, _gcDispose, _renderSnapshot, _nextFrame;

  /// Stops answering this drawer's own channels: what arrives on them from now on is held for the
  /// drawer that takes its id next.
  void _unregisterImageListeners() {
    _imageInit = _gcDispose = _renderSnapshot = _nextFrame = null;
  }

  /// Registers what every GC answers (see `registerWidgetOps`): its paint frames and ops, and the
  /// channels a drawer answers by [_imageInit] and the rest.
  static void registerOps() {
    final type = WireType.gc;
    GCDrawerBase.registerOps(type);
    EquoCommService.onWidgetOp(type, WireChannel.imageInit, _imageInitOp);
    EquoCommService.onWidgetOp(type, WireChannel.gcDispose, _gcDisposeOp);
    EquoCommService.onWidgetOp(type, WireChannel.renderSnapshot, _renderSnapshotOp);
    EquoCommService.onWidgetOp(type, WireChannel.nextFrame, _nextFrameOp);
  }

  static FutureOr<bool> _imageInitOp(int id, Uint8List frame, int start, int end) =>
      _apply((GCDrawerBase.live[id] as GCDrawer?)?._imageInit, frame, start);

  static FutureOr<bool> _gcDisposeOp(int id, Uint8List frame, int start, int end) =>
      _apply((GCDrawerBase.live[id] as GCDrawer?)?._gcDispose, frame, start);

  static FutureOr<bool> _renderSnapshotOp(int id, Uint8List frame, int start, int end) =>
      _apply((GCDrawerBase.live[id] as GCDrawer?)?._renderSnapshot, frame, start);

  static FutureOr<bool> _nextFrameOp(int id, Uint8List frame, int start, int end) =>
      _apply((GCDrawerBase.live[id] as GCDrawer?)?._nextFrame, frame, start);

  static FutureOr<bool> _apply(
      FutureOr<void> Function(Uint8List, int)? handler, Uint8List frame, int start) {
    if (handler == null) return false;
    final applied = handler(frame, start);
    return applied is Future ? applied.then((_) => true) : true;
  }

  final List<Shape> _lateLoadedImages = [];


  // BuildContext for text style resolution (widget mode only, null in image mode).
  // Updated by GCImpl before each build via syncContext().
  BuildContext? _context;
  CanvasThemeExtension? _canvasTheme;

  // Standalone image mode state.
  ui.Image? _baseImage;

  /// The base as a display list while it never had to become pixels (a web readback is costly).
  ui.Picture? _basePicture;

  /// Nesting depth of [_basePicture]; at [_maxBaseDepth] it is flattened to pixels.
  int _baseDepth = 0;
  static const int _maxBaseDepth = 8;

  int _imgWidth = 0;
  int _imgHeight = 0;
  final List<Future<Shape>> _pendingImages = [];
  Completer<void>? _baseImageCompleter;

  /// The control this GC draws on, set by GCImpl; null for an Image's GC.
  BuildContext? widgetContext;

  /// The control's pixels as they are on screen, read from the nearest repaint boundary that holds
  /// it, with the control's offset inside that capture. [ui.Picture.toImageSync]-backed: it reads
  /// the frame already rasterised, so it is safe while the SWT thread waits for the answer.
  ({ui.Image image, Offset offset})? _captureWidget() {
    final context = widgetContext;
    final box = context != null && context.mounted ? context.findRenderObject() : null;
    if (box is! RenderBox || !box.attached || !box.hasSize) return null;
    RenderObject? boundary = box;
    while (boundary != null && boundary is! RenderRepaintBoundary) {
      boundary = boundary.parent;
    }
    if (boundary is! RenderRepaintBoundary) return null;
    final offset = MatrixUtils.transformPoint(box.getTransformTo(boundary), Offset.zero);
    return (image: boundary.toImageSync(pixelRatio: 1.0), offset: offset);
  }

  List<Shape> _staging = [];

  // Standalone mode is one-shot: the drawer tears itself down after the cycle it renders, so a
  // second cycle overtaking the first has nothing to reconcile and simply discards itself.
  int _gcDisposeGeneration = 0;

  // Two gcDispose cycles can be in flight at once (e.g. a hover-driven redraw firing again before
  // a previous cycle's image decode resolves), and finish order doesn't track start order — a
  // cycle with no pending images completes before an older, slower one. Committing on completion
  // would let the older cycle clobber newer, correct shapes; the commit phase is therefore chained
  // so cycles apply in the order they started. Discarding the superseded cycle instead is not an
  // option: an incremental cycle composites onto the previous frame, so its content is never
  // resent and the region it covered would stay unpainted until an unrelated full repaint.
  Future<void> _commitChain = Future<void>.value();

  /// Standalone mode: registers comm listeners for state + all draw ops + imageInit/gcDispose.
  /// Used for headless image rendering (new GC(image)). [onDisposed] fires once the render is sent.
  GCDrawer.standalone(VGC state, {void Function()? onDisposed})
      : onShapesUpdated = null,
        onGCDispose = null,
        onFullRepaintNeeded = null,
        super(state) {
    // The Image's properties as the widget wire writes an object's fields.
    _imageInit = (frame, start) {
      final completer = Completer<void>();
      _baseImageCompleter = completer;
      // Only awaited when a cycle ends; the frame queue reports a failure instead.
      completer.future.catchError((Object _) {});
      final ready = _handleImageInit((WidgetWireReader(frame)..pos = start).fields(VImage())).then(
        (_) => completer.complete(),
        onError: (Object e) => completer.completeError(e),
      );
      // Returned so the next frame waits: ops such as copyArea read the base, not just draw over it.
      return ready;
    };
    _gcDispose = (frame, start) async {
      _settleNormalFill();
      final ending = _Dispose(frame, start);
      final ref = ending.ref;
      final wantPixels = ending.pixels;
      // Java keeps one drawer per Image: the next GC on it draws on top of this render.
      final retain = ending.retain;
      final myGeneration = ++_gcDisposeGeneration;
      if (_baseImageCompleter != null) await _baseImageCompleter!.future;
      await Future.wait(_pendingImages);
      if (myGeneration != _gcDisposeGeneration) return;
      if (retain && ref == 0) {
        // Abandoned: the Image keeps its previous render. Staged images belong to
        // _lateLoadedImages, so they are not disposed here.
        _staging = [];
        return;
      }
      shapes
        ..clear()
        ..addAll(_staging);
      _staging = [];
      await _renderAndSend(ref, wantPixels, retain: retain);
      if (retain) return;
      onDisposed?.call();
      // Nothing reuses this gcId after this point, so tear down now.
      dispose();
    };
    // Non-terminal: renders the current draw state (staged + already-committed shapes) without
    // clearing staging or unregistering listeners, so more ops (and a later real gcDispose, or
    // another snapshot) still work afterward. Used when Java asks for the image mid-drawing —
    // this backend only paints in response to an explicit signal, unlike real SWT where GC draws
    // are immediately visible in the image.
    _renderSnapshot = (frame, start) async {
      if (_baseImageCompleter != null) await _baseImageCompleter!.future;
      await Future.wait(_pendingImages);
      await _renderSnapshotAndSend();
    };
  }

  final void Function(List<Shape>)? onGCDispose;

  /// Called when scoped repaints have stacked past [_maxRegionLayers]; the Canvas answers with a
  /// full-area Paint, which collapses them back into one display list.
  final void Function()? onFullRepaintNeeded;

  // Layers inside a later damage are dropped, so only damage that keeps shifting by less than its
  // own size (a drag) stacks this deep. Every layer is replayed on every frame, so it stays low.
  static const int _maxRegionLayers = 16;

  // Set once the full-area Paint is asked for, so the cycles before it arrives don't ask again.
  bool _fullRepaintRequested = false;

  /// Embedded mode: registers comm listeners for state + all draw ops + gcDispose.
  /// onShapesUpdated triggers GCImpl.setState().
  GCDrawer.embedded(VGC state,
      {this.onShapesUpdated, this.onGCDispose, this.onFullRepaintNeeded})
      : super(state) {
    _gcDispose = (frame, start) {
      final ending = _Dispose.read(frame, start);
      // Whether this GC mirrors a real SWT.Paint (ControlHelper.firePaint()) versus one
      // the app opened outside any Paint dispatch (e.g. GEF/draw2d drag/hover feedback on
      // a FigureCanvas).
      final fullRepaint = ending.fullRepaint;
      final damage = ending.damage;
      final cycleStaging = _staging;
      _staging = _spareStaging ?? [];
      _spareStaging = null;
      _settleNormalFill();
      // Nothing to wait for: committed now, with nothing made for the wait.
      if (_pendingImages.isEmpty && _commitsInFlight == 0) {
        _commit(cycleStaging, fullRepaint, damage);
        _spareStaging = cycleStaging..clear();
        return null;
      }
      return _commitWhenReady(cycleStaging, fullRepaint, damage);
    };
    // A repaint of what is already shown is not resent; Java still waits for the frame it is due in.
    _nextFrame = (frame, start) => _reportInFrame();
  }

  /// Commits a cycle that has images still decoding, or that an earlier one is still committing
  /// ahead of: decoding runs concurrently across cycles; only the commit is serialized.
  Future<void> _commitWhenReady(List<Shape> cycleStaging, bool fullRepaint, Rect? damage) async {
    final pending = List<Future<Shape>>.from(_pendingImages);
    _pendingImages.clear();

    final previousCommit = _commitChain;
    final thisCommit = Completer<void>();
    _commitChain = thisCommit.future;
    _commitsInFlight++;
    try {
      if (pending.isNotEmpty) {
        try {
          await Future.wait(pending);
        } catch (e) {
          // Failed images will be replaced with fallback shapes
        }
      }
      await previousCommit;
      _commit(cycleStaging, fullRepaint, damage);
    } finally {
      _commitsInFlight--;
      thisCommit.complete();
    }
  }

  /// Commits that are waiting on images or on an earlier commit.
  int _commitsInFlight = 0;

  /// The staging list of the cycle last committed, emptied for the next one to fill.
  List<Shape>? _spareStaging;

  /// Images a commit must not dispose: those a shape still draws. Reused from commit to commit.
  final Set<ui.Image> _keep = {};

  /// Makes [cycleStaging] what the control shows: over the last frame within [damage], instead
  /// of it when [fullRepaint], else over what it does not cover.
  void _commit(List<Shape> cycleStaging, bool fullRepaint, Rect? damage) {
    var kept = 0;
    for (var k = 0; k < cycleStaging.length; k++) {
      final shape = cycleStaging[k];
      if (shape is! _PlaceholderShape) cycleStaging[kept++] = shape;
    }
    cycleStaging.length = kept;
    _settleScreenCopies(cycleStaging, fullRepaint, damage);

    final keep = _keep..clear();
    collectShapeImages(cycleStaging, keep);
    collectShapeImages(_lateLoadedImages, keep);
    if (_droppedCaptures.isNotEmpty) {
      for (final image in _droppedCaptures) {
        if (!keep.contains(image)) image.dispose();
      }
      _droppedCaptures.clear();
    }
    if (damage != null) {
      // A scoped Paint leaves every pixel outside its rectangle alone, so the previous frame
      // stays and this cycle becomes a layer over it. It erases its rectangle first, so
      // whatever was painted entirely inside it is gone; anything whose area is unknown stays.
      _dropInside(damage, keep);
      shapes.add(RegionShape(damage, [...cycleStaging, ..._lateLoadedImages]));
      _lateLoadedImages.clear();
      if (!_fullRepaintRequested && _regionLayers() > _maxRegionLayers) {
        _fullRepaintRequested = true;
        onFullRepaintNeeded?.call();
      }
    } else {
      // A full-area Paint repaints the whole client area, so nothing the previous cycle left
      // survives it; a GC opened outside one (draw2d/GEF feedback) composites on top instead.
      if (fullRepaint) {
        _fullRepaintRequested = false;
        disposeShapeImages(shapes, keep: keep);
        shapes.clear();
      } else {
        _dropWhatThisCycleCovers(cycleStaging, keep);
      }
      shapes.addAll(cycleStaging);
      shapes.addAll(_lateLoadedImages);
      _lateLoadedImages.clear();
    }

    // The committed frame, empty included: a cycle that drew nothing has to blank the control,
    // not leave the owner rendering the last frame that had content.
    onGCDispose?.call(List.of(shapes, growable: false));
    onShapesUpdated?.call(shapes);
    _reportInFrame();
  }

  /// Captures [_settleScreenCopies] replaced by the background, disposed once nothing draws them.
  final Set<ui.Image> _droppedCaptures = {};

  /// Turns the copies of the screen this cycle made into what they copied, now that its end says
  /// what its Paint erased first: the screen as captured where the Paint left it, the background
  /// where it erased the source. A GC outside a Paint erased nothing.
  void _settleScreenCopies(List<Shape> staging, bool fullRepaint, Rect? damage) {
    final erased = damage ?? (fullRepaint ? Rect.largest : null);
    for (var k = 0; k < staging.length; k++) {
      final s = staging[k];
      if (s is! _ScreenCopy) continue;
      final clip = s.base.clipRect ?? s.base.destRect;
      final fromErased =
          erased == null ? Rect.zero : erased.intersect(s.screenSource).shift(s.shift).intersect(clip);
      if (fromErased.isEmpty) {
        staging[k] = s.base;
      } else if (_containsRect(fromErased, clip)) {
        staging[k] = EraseShape(clip);
        final image = s.base.image;
        if (image != null) _droppedCaptures.add(image);
      } else {
        staging[k] = s.base;
        staging.insert(++k, EraseShape(fromErased));
      }
    }
  }

  int _regionLayers() {
    var n = 0;
    for (var k = 0; k < shapes.length; k++) {
      if (shapes[k] is RegionShape) n++;
    }
    return n;
  }

  /// Tells Java the frame that shows this paint has begun, which is when it sends the control's next
  /// Paint: that one is built while this frame renders and lands in the next, so repaints come at
  /// the rate frames are shown, as a native toolkit delivers them, without waiting out the render.
  void _reportInFrame() {
    // Java counts the reports, one per paint it sent: paints committed before the frame begins each
    // get theirs, from one callback.
    if (_inFrameDue++ == 0) WidgetsBinding.instance.scheduleFrameCallback(_inFrame);
    WidgetsBinding.instance.scheduleFrame();
  }

  /// The reports the next frame owes Java.
  int _inFrameDue = 0;

  /// [_sendInFrame], torn off once: scheduling it allocates nothing.
  late final void Function(Duration) _inFrame = _sendInFrame;

  void _sendInFrame(Duration _) {
    final due = _inFrameDue;
    _inFrameDue = 0;
    for (var k = 0; k < due; k++) {
      EquoCommService.sendWritten(WidgetWireWriter.startOn(WireChannel.gcInFrame)..int64(state.id));
    }
  }

  /// Drops what [cycle] paints over, so a control redrawn outside a Paint does not retain every
  /// superseded cycle for its lifetime. Only *opaque* coverage counts, and only shapes whose own
  /// area is known are dropped: anything unsure is kept, which costs retention, never correctness.
  void _dropWhatThisCycleCovers(List<Shape> cycle, Set<ui.Image> keep) {
    final covered = _covered..clear();
    for (var k = 0; k < cycle.length; k++) {
      final area = cycle[k].opaqueCoverage;
      if (area != null && !area.isEmpty) covered.add(area);
    }
    if (covered.isEmpty) return;
    _dropShapes(null, keep);
    covered.clear();
  }

  /// What the cycle being committed paints opaquely: reused from commit to commit.
  final List<Rect> _covered = [];

  /// Drops the shapes painted entirely inside [damage], which a scoped Paint erases.
  void _dropInside(Rect damage, Set<ui.Image> keep) => _dropShapes(damage, keep);

  /// Whether [s] is hidden: painted entirely inside [damage] when there is one, else inside one of
  /// [_covered].
  bool _hidden(Shape s, Rect? damage) {
    final area = s.paintedBounds;
    if (area == null) return false;
    if (damage != null) return _containsRect(damage, area);
    for (var k = 0; k < _covered.length; k++) {
      if (_containsRect(_covered[k], area)) return true;
    }
    return false;
  }

  /// Removes the shapes [hidden] selects and releases their images, except those a surviving shape
  /// still draws: copyArea gives its copy the original's image.
  void _dropShapes(Rect? damage, Set<ui.Image> keep) {
    // Kept shapes move down in place; dropped ones are listed only when one holds an image.
    final dropped = _dropped..clear();
    var kept = 0;
    var droppedImages = false;
    for (var k = 0; k < shapes.length; k++) {
      final s = shapes[k];
      if (_hidden(s, damage)) {
        if (s.holdsImages) {
          dropped.add(s);
          droppedImages = true;
        }
      } else {
        shapes[kept++] = s;
      }
    }
    if (kept == shapes.length) return;
    shapes.length = kept;
    if (!droppedImages) return;
    collectShapeImages(shapes, keep);
    disposeShapeImages(dropped, keep: keep);
    dropped.clear();
  }

  /// The dropped shapes that hold images, to release them: reused from commit to commit.
  final List<Shape> _dropped = [];

  /// Called by GCImpl.build() to provide context for text style resolution.
  void syncContext(BuildContext ctx) {
    _context = ctx;
    _canvasTheme = Theme.of(ctx).extension<CanvasThemeExtension>();
  }

  Object? _matrixOf;
  Float64List? _matrix;

  // SWT {m11,m12,m21,m22,dx,dy} → Flutter column-major 4×4, or null for identity. Worked out once
  // per transform the GC is given: every shape drawn under it asks, and nothing writes to it.
  Float64List? get currentTransform {
    final transform = state.transform;
    if (identical(transform, _matrixOf)) return _matrix;
    _matrixOf = transform;
    return _matrix = _matrixFrom(transform?.elements);
  }

  static Float64List? _matrixFrom(List<double>? e) {
    if (e == null || e.length < 6) return null;
    final m11 = e[0], m12 = e[1];
    final m21 = e[2], m22 = e[3];
    final dx  = e[4], dy  = e[5];
    if (m11 == 1 && m12 == 0 && m21 == 0 && m22 == 1 && dx == 0 && dy == 0) return null;
    return Float64List(16)
      ..[0] = m11
      ..[1] = m12
      ..[4] = m21
      ..[5] = m22
      ..[10] = 1
      ..[12] = dx
      ..[13] = dy
      ..[15] = 1;
  }

  // ── State getters (read from VGC) ──────────────────────────────────────

  // Drawing keeps the colors the application painted with. disable_swt_canvas_colors drops them
  // for the Canvas theme's, the rule getBackgroundColor applies to widgets; use_swt_colors, which
  // asks for the application's colors everywhere, wins over it.
  bool get _useThemeColors => gcUsesThemeColors(state.themedCanvas);

  Color _themed(VColor? swtColor, Color? themeColor, Color fallback) {
    if (_useThemeColors) return themeColor ?? fallback;
    if (swtColor == null) return fallback;
    // Every op asks for its colour; the last two made are kept, by value, since a paint alternates
    // between its background and its foreground.
    final argb = swtColor.alpha * 0x1000000 + (swtColor.red << 16) + (swtColor.green << 8) + swtColor.blue;
    if (argb == _argb0) return _color0!;
    if (argb == _argb1) return _color1!;
    final color = colorFromVColor(swtColor, defaultColor: fallback);
    _argb1 = _argb0;
    _color1 = _color0;
    _argb0 = argb;
    _color0 = color;
    return color;
  }

  int _argb0 = -1, _argb1 = -1;
  Color? _color0, _color1;

  // Used until a theme is resolved: the standalone image drawer renders offscreen, never builds,
  // and so never gets a context (_canvasTheme stays null). Falls back to the global dark/light
  // setting (getCurrentTheme(), no BuildContext needed) instead of a fixed white/dark-grey pair,
  // so GEF/draw2d's offscreen `new GC(image)` double-buffering still matches the app's theme.
  Color get _swtBackground => getBackground();
  Color get _swtForeground => getForeground();

  // One getter per painted role, differing only in the theme slot it reads. The GC hands the
  // drawing side a background or a foreground; the role says what it is being used for.
  Color get bg =>
      _themed(state.background, _canvasTheme?.backgroundColor, _swtBackground);
  Color get fillColor => _keepingContrast(
      _themed(state.background, _canvasTheme?.fillColor, _swtBackground));
  Color get textBackgroundColor =>
      _hostGround ??
      _keepingContrast(_themed(
          state.background, _canvasTheme?.textBackgroundColor, _swtBackground));

  // L* a fill that differs from the normal one moves away from the theme's fill: a little more than
  // its own L* distance to the normal fill while that is small, approaching the maximum as it grows.
  static const double _maxOffset = 40;
  static const double _offsetScale = 20;
  // How far the most chromatic fill goes toward highlightColor.
  static const double _maxHighlight = 0.6;
  // Below both, a fill reads as the normal one and takes the theme fill as it is.
  static const double _minLstarDifference = 1;
  static const double _minTint = 0.01;
  // Mid-grey L*: a theme fill darker than it has its room above, a lighter one below.
  static const double _midLstar = 50;
  static const int _maxTrackedFills = 256;

  // Keeps a hover or selection apart from the normal fill under the theme, moving it toward the side
  // with room (a light app darkens its highlights, which on a dark fill would sink into black). The
  // hue it adds tints it toward highlightColor and its lightness, or a light fill shows a pastel.
  Color _keepingContrast(Color themed) {
    final highlight = _canvasTheme?.highlightColor;
    final swtColor = state.background;
    if (!_useThemeColors || highlight == null || swtColor == null) return themed;
    final app = colorFromVColor(swtColor);
    final declared = _declaredRoleFill(app);
    if (declared != null) return declared;
    final normal = _normalFill(app);
    final key = (app.toARGB32(), normal.toARGB32(), themed.toARGB32(), highlight.toARGB32());
    if (_contrasted.length >= _maxTrackedFills) _contrasted.clear();
    return _contrasted.putIfAbsent(key, () => _contrastedFill(app, normal, themed, highlight));
  }

  // Fills already moved apart, keyed by everything the result depends on.
  final Map<(int, int, int, int), Color> _contrasted = {};

  static Color _contrastedFill(Color app, Color normal, Color themed, Color highlight) {
    final difference = (lstar(app) - lstar(normal)).abs();
    final tint = (chroma(app) - chroma(normal)).clamp(0.0, 1.0) * _maxHighlight;
    if (difference < _minLstarDifference && tint < _minTint) return themed;
    final fill = lstar(themed);
    final offset = _maxOffset * (1 - math.exp(-difference / _offsetScale));
    final apart = fill < _midLstar ? fill + offset : fill - offset;
    return withLstar(Color.lerp(themed, highlight, tint)!, apart + (lstar(highlight) - apart) * tint);
  }

  // The theme's color for a fill the integrator declared a hover or a selection
  // (VGC.hoverFills / VGC.selectionFills), or null when the color has no declared role.
  Color? _declaredRoleFill(Color app) {
    if (_isAnyOf(state.hoverFills, app)) return _canvasTheme?.hoverFillColor;
    if (_isAnyOf(state.selectionFills, app)) return _canvasTheme?.selectionFillColor;
    return null;
  }

  static bool _isAnyOf(List<VRGB?>? fills, Color app) =>
      fills?.any((c) => c != null && (c.red << 16 | c.green << 8 | c.blue) == app.toARGB32() & 0xFFFFFF) ??
      false;

  // The control's own background or, when it sets none, the fill this GC has painted with most.
  // The counts span the control's paints; within one paint the normal is what the paints before it
  // settled on, so one application color never paints two ways in the same frame.
  final Map<int, int> _fillCounts = {};
  int? _commonestFill;
  int? _settledNormalFill;

  Color _normalFill(Color app) {
    final ground = _controlBackground;
    if (ground != null) return colorFromVColor(ground);
    final key = app.toARGB32();
    if (_fillCounts.length < _maxTrackedFills || _fillCounts.containsKey(key)) {
      final count = _fillCounts.update(key, (n) => n + 1, ifAbsent: () => 1);
      if (count > (_fillCounts[_commonestFill] ?? 0)) _commonestFill = key;
    }
    return Color(_settledNormalFill ?? _commonestFill ?? key);
  }

  void _settleNormalFill() => _settledNormalFill = _commonestFill;

  VColor? get _controlBackground {
    final host = widgetContext;
    if (host is! StatefulElement || !host.mounted) return null;
    final ground = host.state;
    return ground is GCGround ? (ground as GCGround).swtGround : null;
  }

  Color? get _hostGround {
    final host = widgetContext;
    if (_useThemeColors || host is! StatefulElement || !host.mounted) return null;
    final ground = host.state;
    if (ground is! GCGround) return null;
    final GCGround hostGround = ground as GCGround;
    final rendered = hostGround.renderedGround;
    final swtGround = hostGround.swtGround;
    if (rendered == null || swtGround == null || state.background == null) return null;
    return colorFromVColor(state.background) == colorFromVColor(swtGround) ? rendered : null;
  }

  Color get strokeColor =>
      _themed(state.foreground, _canvasTheme?.strokeColor, _swtForeground);
  Color get lineColor =>
      _themed(state.foreground, _canvasTheme?.lineColor, _swtForeground);
  Color get pointColor =>
      _themed(state.foreground, _canvasTheme?.pointColor, _swtForeground);
  Color get textColor =>
      _themed(state.foreground, _canvasTheme?.textColor, _swtForeground);
  Color get focusColor =>
      _themed(state.foreground, _canvasTheme?.focusColor, _swtForeground);
  // fillGradientRectangle ramps foreground -> background.
  Color get gradientStartColor =>
      _themed(state.foreground, _canvasTheme?.gradientStartColor, _swtForeground);
  Color get gradientEndColor =>
      _themed(state.background, _canvasTheme?.gradientEndColor, _swtBackground);
  // An icon has no color of the application's to keep, so opting out leaves the tint that
  // preserve_icon_colors=false already used.
  Color get imageTintColor => _useThemeColors
      ? (_canvasTheme?.imageTintColor ?? AppColors.getColor(true))
      : AppColors.getColor(true);
  GlyphTintLimits get glyphTintLimits => _canvasTheme == null
      ? GlyphTintLimits.fallback
      : GlyphTintLimits(
          maxSide: _canvasTheme!.glyphTintMaxSide,
          channelTolerance: _canvasTheme!.glyphTintChannelTolerance);
  double get lineWidth => (state.lineWidth ?? 1).toDouble();
  int get lineCap => state.lineCap ?? 1;
  int get lineJoin => state.lineJoin ?? 1;

  PathFillType get _fillType =>
      (state.fillRule ?? SWT.FILL_EVEN_ODD) == SWT.FILL_WINDING
          ? PathFillType.nonZero
          : PathFillType.evenOdd;

  /// The dash lengths of the current line style, or null when it strokes solid.
  ///
  /// A built-in style is a pattern in line-width units, with a fixed one standing in at width 0;
  /// LINE_CUSTOM carries its own lengths and is taken as given.
  /// Dash patterns by line style and width. A custom pattern is not kept: it is the GC's own list.
  static final Map<(int, num), List<double>?> _dashes = {};

  /// The line style's dash pattern, worked out once per style and width: every line drawn asks for
  /// it, and a drawing alternates between a few of them.
  List<double>? get _dashPattern {
    final style = state.lineStyle ?? SWT.LINE_SOLID;
    if (style == SWT.LINE_SOLID) return null;
    if (style == SWT.LINE_CUSTOM) return _patternOfLineStyle();
    final key = (style, state.lineWidth ?? 0);
    final held = _dashes[key];
    if (held != null || _dashes.containsKey(key)) return held;
    return _dashes[key] = _patternOfLineStyle();
  }

  List<double>? _patternOfLineStyle() {
    final width = (state.lineWidth ?? 0).toDouble();
    final hairline = width == 0;
    final List<double>? pattern = switch (state.lineStyle ?? SWT.LINE_SOLID) {
      SWT.LINE_DASH => hairline ? const [18.0, 6.0] : const [3.0, 1.0],
      SWT.LINE_DOT => hairline ? const [3.0, 3.0] : const [1.0, 1.0],
      SWT.LINE_DASHDOT =>
        hairline ? const [9.0, 6.0, 3.0, 6.0] : const [3.0, 1.0, 1.0, 1.0],
      SWT.LINE_DASHDOTDOT => hairline
          ? const [9.0, 3.0, 3.0, 3.0, 3.0, 3.0]
          : const [3.0, 1.0, 1.0, 1.0, 1.0, 1.0],
      SWT.LINE_CUSTOM => state.lineDash?.map((d) => d.toDouble()).toList(),
      _ => null,
    };
    if (pattern == null || pattern.isEmpty) return null;
    final scaled = state.lineStyle == SWT.LINE_CUSTOM || hairline
        ? pattern
        : pattern.map((d) => d * width).toList();
    return scaled.fold(0.0, (sum, d) => sum + d) > 0 ? scaled : null;
  }

  /// Emits [outline] stroked as the current line style, and answers whether it did: a solid style
  /// has no pattern to walk, and the caller draws its own shape instead.
  bool _addDashedStroke(Path Function() outline, Color color) {
    final pattern = _dashPattern;
    if (pattern == null) return false;
    _addShape(PathShape(_dashed(outline(), pattern), color, lineWidth, lineCap,
        lineJoin,
        clipRect: _childClip));
    return true;
  }

  /// [_addDashedStroke] for an outline of straight segments, which is walked directly: the same
  /// runs as through path metrics, at a fraction of the cost.
  bool _addDashedPolyline(List<Offset> points, Color color, {bool close = false}) {
    final pattern = _dashPattern;
    if (pattern == null) return false;
    _addShape(PathShape(_dashedPolyline(points, pattern, close: close), color, lineWidth, lineCap,
        lineJoin,
        clipRect: _childClip));
    return true;
  }

  static List<Offset> _offsets(List<int> points) => [
        for (var i = 0; i + 1 < points.length; i += 2)
          Offset(points[i].toDouble(), points[i + 1].toDouble())
      ];

  /// [_dashed] along the polyline through [points], closed back to the first when [close].
  Path _dashedPolyline(List<Offset> points, List<double> pattern, {required bool close}) {
    final out = Path();
    final vertices = close && points.isNotEmpty ? [...points, points.first] : points;
    if (vertices.length < 2) return out;
    final along = <double>[0];
    for (var i = 1; i < vertices.length; i++) {
      along.add(along.last + (vertices[i] - vertices[i - 1]).distance);
    }
    final total = along.last;
    var segment = 1;
    Offset at(double distance) {
      while (segment < vertices.length - 1 && along[segment] < distance) {
        segment++;
      }
      final start = along[segment - 1];
      final length = along[segment] - start;
      return Offset.lerp(vertices[segment - 1], vertices[segment],
          length > 0 ? (distance - start) / length : 0)!;
    }

    final period = pattern.fold(0.0, (sum, d) => sum + d);
    var distance = -(period > 0 ? (state.lineDashOffset ?? 0) % period : 0.0);
    var index = 0;
    while (distance < total) {
      final length = pattern[index % pattern.length];
      final end = math.min(distance + length, total);
      if (index.isEven && end > 0) {
        final first = at(math.max(distance, 0));
        out.moveTo(first.dx, first.dy);
        for (var v = segment; v < vertices.length && along[v] < end; v++) {
          out.lineTo(vertices[v].dx, vertices[v].dy);
        }
        final last = at(end);
        out.lineTo(last.dx, last.dy);
      }
      distance += length;
      index++;
    }
    return out;
  }

  /// The "on" runs of [pattern] along the straight line [from]-[to], as the endpoint pairs
  /// [SegmentsShape] strokes.
  Float32List _dashSegments(Offset from, Offset to, List<double> pattern) {
    final total = (to - from).distance;
    var period = 0.0;
    for (final d in pattern) {
      period += d;
    }
    final start = -(period > 0 ? (state.lineDashOffset ?? 0) % period : 0.0);
    // Counted first, so the ends go straight into a list of their size.
    var count = 0;
    var distance = start;
    for (var index = 0; distance < total; index++) {
      final length = pattern[index % pattern.length];
      if (index.isEven && math.min(distance + length, total) > 0) count++;
      distance += length;
    }
    final out = Float32List(count * 4);
    final dx = total > 0 ? (to.dx - from.dx) / total : 0.0;
    final dy = total > 0 ? (to.dy - from.dy) / total : 0.0;
    var o = 0;
    distance = start;
    for (var index = 0; distance < total; index++) {
      final length = pattern[index % pattern.length];
      final end = math.min(distance + length, total);
      if (index.isEven && end > 0) {
        final begin = math.max(distance, 0);
        out[o++] = from.dx + dx * begin;
        out[o++] = from.dy + dy * begin;
        out[o++] = from.dx + dx * end;
        out[o++] = from.dy + dy * end;
      }
      distance += length;
    }
    return out;
  }

  /// The outline [points] describe, as a path: pairs of device-space coordinates.
  Path _polygonPath(List<int> points, {required bool close}) {
    final path = Path()..moveTo(points[0].toDouble(), points[1].toDouble());
    for (var i = 2; i < points.length; i += 2) {
      path.lineTo(points[i].toDouble(), points[i + 1].toDouble());
    }
    if (close) path.close();
    return path;
  }

  /// A path holding the "on" runs of [pattern] along [source] - Flutter strokes a path solid, so
  /// the gaps have to be left out of the geometry rather than asked for on the Paint.
  ///
  /// The walk starts [LineAttributes.dashOffset] into the pattern, as SWT's dash phase does.
  Path _dashed(Path source, List<double> pattern) {
    final out = Path();
    final period = pattern.fold(0.0, (sum, d) => sum + d);
    final phase = period > 0 ? (state.lineDashOffset ?? 0) % period : 0.0;
    for (final metric in source.computeMetrics()) {
      var distance = -phase;
      var index = 0;
      while (distance < metric.length) {
        final length = pattern[index % pattern.length];
        if (index.isEven && distance + length > 0) {
          _addRun(out, metric, math.max(distance, 0), math.min(distance + length, metric.length));
        }
        distance += length;
        index++;
      }
    }
    return out;
  }

  /// Adds the stretch of [metric] from [start] to [end] as plain segments. [ui.PathMetric.extractPath]
  /// would build the same run, but as a path that keeps the source's metrics alive for as long as
  /// the shape holding it: on the web every draw of it rebuilds native geometry that is not freed.
  static void _addRun(Path out, ui.PathMetric metric, double start, double end) {
    final first = metric.getTangentForOffset(start);
    if (first == null) return;
    out.moveTo(first.position.dx, first.position.dy);
    // Short steps follow a curve closely; a straight contour ends up as its two ends.
    const step = 2.0;
    for (var at = start + step; at < end; at += step) {
      final t = metric.getTangentForOffset(at);
      if (t != null) out.lineTo(t.position.dx, t.position.dy);
    }
    final last = metric.getTangentForOffset(end);
    if (last != null) out.lineTo(last.position.dx, last.position.dy);
  }

  Rect? get clipping {
    if (state.clipping == null) return null;
    final width = state.clipping!.width?.toDouble() ?? 0;
    final height = state.clipping!.height?.toDouble() ?? 0;
    // An empty region discards every op, where null means unclipped. Compositions that confine a
    // child by clipping to its intersection with the damaged area rely on the difference.
    if (width <= 0 || height <= 0) return Rect.zero;
    return Rect.fromLTWH(
      state.clipping!.x?.toDouble() ?? 0,
      state.clipping!.y?.toDouble() ?? 0,
      width,
      height,
    );
  }

  // SWT reports the clip in device space, so under a transform it rides on the wrapper _addShape
  // builds; clipping again inside the matrix would scale the clip rectangle along with the shape.
  Rect? get _childClip => currentTransform != null ? null : clipping;

  VPathData? _clipShapePathKey;
  List<int>? _clipShapeRectsKey;
  int? _clipShapeFillRuleKey;
  Path? _clipShape;

  /// The clip set from a Path or a Region, or null when the clip is rectangular (or unset).
  ///
  /// [clipping] carries only the bounding box of such a clip, which for anything but a rectangle
  /// lets drawing through where the shape does not reach. Rebuilt when the GC state that describes
  /// it changes, not per op: one clip serves every op drawn under it.
  Path? get clipShape {
    final clipPath = state.clippingPath;
    final rects = state.clippingRects;
    final fillRule = state.fillRule;
    if (identical(clipPath, _clipShapePathKey) &&
        identical(rects, _clipShapeRectsKey) &&
        fillRule == _clipShapeFillRuleKey) {
      return _clipShape;
    }
    _clipShapePathKey = clipPath;
    _clipShapeRectsKey = rects;
    _clipShapeFillRuleKey = fillRule;
    // A Path clip and a Region clip never arrive together — each setClipping overload replaces the
    // other. A region is the union of its rectangles and they may overlap, so it fills by winding
    // whatever rule the GC carries: even-odd would punch holes where two of them meet. A path clip
    // keeps the GC's rule, the way the platforms clip with the path's own winding rule.
    if (rects != null && rects.length >= 4) {
      final path = Path()..fillType = PathFillType.nonZero;
      for (var i = 0; i + 3 < rects.length; i += 4) {
        path.addRect(Rect.fromLTWH(rects[i].toDouble(), rects[i + 1].toDouble(),
            rects[i + 2].toDouble(), rects[i + 3].toDouble()));
      }
      _clipShape = path;
    } else {
      _clipShape = _buildPathData(clipPath);
    }
    return _clipShape;
  }

  /// Confines [shape] to a non-rectangular clip, if one is in force. The rectangular part of the
  /// clip already rides on the shape itself (or on its transform wrapper) as a clipRect.
  Shape _clipped(Shape shape) {
    final mask = textClipMask;
    if (mask != null) return MaskShape(mask, [shape]);
    final shapeClip = clipShape;
    return shapeClip == null ? shape : ClipPathShape(shapeClip, [shape]);
  }

  Color applyAlpha(Color color) {
    final alpha = state.alpha ?? 255;
    if (alpha == 255) return color;
    return color.withOpacity(alpha / 255.0);
  }

  /// Fills [path] with the GC's background pattern. False when no pattern is set.
  /// Whether fills draw with a pattern: checked before building the path [_addPatternFill] takes,
  /// which nearly every fill would otherwise build for nothing.
  bool get _patterned => state.backgroundPattern != null;

  bool _addPatternFill(Path path) {
    final pattern = state.backgroundPattern;
    if (pattern == null) return false;
    final alpha = state.alpha ?? 255;
    final clip = _childClip;
    final vImage = pattern.image;
    if (vImage != null) {
      _stageAsync(ImageUtils.decodeVImageToUIImage(vImage).then((image) =>
          image == null
              ? _PlaceholderShape()
              : PatternFillShape.image(path, image, alpha, clip, tileMatrix(vImage, image))));
      return true;
    }
    final from = _themed(pattern.color1, _canvasTheme?.patternStartColor,
        colorFromVColor(pattern.color1));
    final to = _themed(pattern.color2, _canvasTheme?.patternEndColor,
        colorFromVColor(pattern.color2));
    final start = Offset(pattern.startX ?? 0, pattern.startY ?? 0);
    final end = Offset(pattern.endX ?? 0, pattern.endY ?? 0);
    // A zero-length gradient has no direction; the native backends paint its first color.
    _addShape(start == end
        ? PathShape(path, applyAlpha(from), 0, lineCap, lineJoin, isFilled: true, clipRect: clip)
        : PatternFillShape(path,
            ui.Gradient.linear(start, end, [from, to], null, TileMode.repeated), alpha, clip));
    return true;
  }

  // ── Shape helpers ───────────────────────────────────────────────────────

  void clearShapes() {
    shapes.clear();
    _staging = [];
  }

  bool get _antiAlias => state.antialias != SWT.OFF;

  FilterQuality? get _filterQuality => switch (state.interpolation) {
        SWT.NONE => FilterQuality.none,
        SWT.LOW => FilterQuality.low,
        SWT.HIGH => FilterQuality.high,
        _ => null,
      };

  /// [shape] under the GC's anti-aliasing and interpolation, when either differs from the default.
  Shape _hinted(Shape shape) {
    final antiAlias = _antiAlias;
    final filter = _filterQuality;
    return antiAlias && filter == null ? shape : RenderHintsShape(antiAlias, filter, [shape]);
  }

  /// Whether [shape] is drawn with the foreground: those take the foreground pattern.
  static bool _isStroke(Shape shape) => switch (shape) {
        LineShape() || PointShape() || FocusRectShape() || TextShape() => true,
        RectShape(:final isFilled) ||
        OvalShape(:final isFilled) ||
        PathShape(:final isFilled) ||
        PolygonShape(:final isFilled) ||
        PolylineShape(:final isFilled) ||
        ArcShape(:final isFilled) ||
        RoundRectShape(:final isFilled) =>
          !isFilled,
        _ => false,
      };

  /// A gradient pattern's shader, laid out in GC coordinates. A zero-length gradient has no
  /// direction; the native backends paint its first colour.
  ui.Shader _gradientShader(VPattern pattern) {
    final from = _themed(pattern.color1, _canvasTheme?.patternStartColor,
        colorFromVColor(pattern.color1));
    final to = _themed(pattern.color2, _canvasTheme?.patternEndColor,
        colorFromVColor(pattern.color2));
    final start = Offset(pattern.startX ?? 0, pattern.startY ?? 0);
    final end = Offset(pattern.endX ?? 0, pattern.endY ?? 0);
    return start == end
        ? ui.Gradient.linear(start, start + const Offset(1, 0), [from, from])
        : ui.Gradient.linear(start, end, [from, to], null, TileMode.repeated);
  }

  void _addShape(Shape shape) {
    final pattern = state.foregroundPattern;
    if (pattern != null && _isStroke(shape)) {
      final alpha = state.alpha ?? 255;
      final vImage = pattern.image;
      if (vImage != null) {
        final stroke = _hinted(shape);
        _stageAsync(ImageUtils.decodeVImageToUIImage(vImage).then((image) => image == null
            ? _PlaceholderShape()
            : PatternStrokeShape(
                ui.ImageShader(image, TileMode.repeated, TileMode.repeated,
                    tileMatrix(vImage, image)),
                alpha, [stroke], image: image)));
        return;
      }
      shape = PatternStrokeShape(_gradientShader(pattern), alpha, [shape]);
    }
    shape = _hinted(shape);
    final transform = currentTransform;
    _staging.add(_clipped(_xored(transform == null
        ? shape
        : TransformShape(transform, [shape], clipping))));
  }

  /// Wraps [shape] when the GC is in XOR mode, which is how draw2d and GEF draw every piece of
  /// transient feedback: the marquee rubber band, for one, is painted in *white* and relies on the
  /// XOR to invert it against whatever is underneath (`MarqueeSelectionTool.MarqueeRectangleFigure`
  /// does `setXORMode(true)` then `setForegroundColor(white)`). Drawn without it, that feedback is
  /// opaque white on a light canvas — invisible.
  Shape _xored(Shape shape) =>
      state.XORMode == true ? XorShape([shape]) : shape;

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
    if (isFilled &&
        _patterned &&
        _addPatternFill(
            Path()..addRRect(RRect.fromRectXY(rect, arcWidth / 2.0, arcHeight / 2.0)))) {
      return;
    }
    final color = isFilled ? applyAlpha(fillColor) : applyAlpha(strokeColor);
    final strokeWidth = isFilled ? 0.0 : lineWidth;
    if (!isFilled &&
        _addDashedStroke(
            () => Path()
              ..addRRect(RRect.fromRectXY(rect, arcWidth / 2.0, arcHeight / 2.0)),
            color)) {
      return;
    }
    _addShape(RoundRectShape(
        rect, arcWidth / 2.0, arcHeight / 2.0, color, strokeWidth, lineCap, lineJoin,
        isFilled: isFilled, clipRect: _childClip));
  }

  void _addOvalShape({
    required int x,
    required int y,
    required int width,
    required int height,
    required bool isFilled,
  }) {
    final rect = _getRectFromArgs(x, y, width, height);
    if (isFilled && _patterned && _addPatternFill(Path()..addOval(rect))) return;
    final color = isFilled ? applyAlpha(fillColor) : applyAlpha(strokeColor);
    final strokeWidth = isFilled ? 0.0 : lineWidth;
    if (!isFilled && _addDashedStroke(() => Path()..addOval(rect), color)) return;
    _addShape(OvalShape(rect, color, strokeWidth,
        isFilled: isFilled, clipRect: _childClip));
  }

  void _addRectShape({
    required int x,
    required int y,
    required int width,
    required int height,
    required bool isFilled,
  }) {
    final rect = _getRectFromArgs(x, y, width, height);
    if (isFilled && _patterned && _addPatternFill(Path()..addRect(rect))) return;
    final color = isFilled ? applyAlpha(fillColor) : applyAlpha(strokeColor);
    final strokeWidth = isFilled ? 0.0 : lineWidth;
    if (!isFilled &&
        _addDashedPolyline(
            [rect.topLeft, rect.topRight, rect.bottomRight, rect.bottomLeft], color,
            close: true)) {
      return;
    }
    _addShape(RectShape(rect, color, strokeWidth, lineCap, lineJoin,
        isFilled: isFilled, clipRect: _childClip));
  }

  void _addPolygonShape({
    required List<int> points,
    required bool isFilled,
    int minPoints = 6,
  }) {
    if (points.length.isOdd) points = points.sublist(0, points.length - 1);
    if (points.length >= minPoints) {
      if (isFilled &&
          _patterned &&
          _addPatternFill(_polygonPath(points, close: true)..fillType = _fillType)) {
        return;
      }
      final color = isFilled ? applyAlpha(fillColor) : applyAlpha(strokeColor);
      final strokeWidth = isFilled ? 0.0 : lineWidth;
      if (!isFilled && _addDashedPolyline(_offsets(points), color, close: true)) {
        return;
      }
      _addShape(PolygonShape(points, color, strokeWidth, lineCap, lineJoin,
          isFilled: isFilled, fillType: _fillType, clipRect: _childClip));
    }
  }

  void _addPolylineShape({
    required List<int> points,
    required bool isFilled,
    int minPoints = 2,
  }) {
    if (points.length.isOdd) points = points.sublist(0, points.length - 1);
    if (points.length >= minPoints) {
      final color = isFilled ? applyAlpha(fillColor) : applyAlpha(strokeColor);
      final strokeWidth = isFilled ? 0.0 : lineWidth;
      if (!isFilled && _addDashedPolyline(_offsets(points), color)) {
        return;
      }
      _addShape(PolylineShape(points, color, strokeWidth, lineCap, lineJoin,
          isFilled: isFilled, clipRect: _childClip));
    }
  }

  void _addPathShape(VPath? path, {required bool isFilled}) {
    final built = _buildPath(path);
    // One fill op, so the strings take the geometry's color rather than resolving it again.
    final color = built != null ? _addPathGeometry(built, isFilled: isFilled) : null;
    for (final text in _pathTexts(path, isFilled: isFilled, color: color)) {
      _addShape(text);
    }
  }

  /// The color the geometry was drawn with, or null when a pattern filled it.
  Color? _addPathGeometry(Path built, {required bool isFilled}) {
    if (isFilled && _addPatternFill(built)) return null;
    final color = isFilled ? applyAlpha(fillColor) : applyAlpha(strokeColor);
    if (!isFilled && _addDashedStroke(() => built, color)) return color;
    _addShape(PathShape(built, color, isFilled ? 0.0 : lineWidth, lineCap, lineJoin,
        isFilled: isFilled, clipRect: _childClip));
    return color;
  }

  /// The strings a Path holds, filled or outlined as its geometry would be. Path.addString has no
  /// outline on this backend, so the glyphs are drawn from the font rather than from path data.
  List<TextShape> _pathTexts(VPath? path, {required bool isFilled, Color? color}) {
    final strings = path?.textStrings;
    final origins = path?.textOrigins;
    final fonts = path?.textFonts;
    if (strings == null || origins == null || fonts == null) return const [];
    final paint = Paint()
      ..color = color ?? applyAlpha(isFilled ? fillColor : strokeColor)
      ..isAntiAlias = _antiAlias;
    if (!isFilled) {
      paint
        ..style = PaintingStyle.stroke
        ..strokeWidth = math.max(lineWidth, 1);
    }
    return [
      for (var i = 0; i < strings.length && i < fonts.length && 2 * i + 1 < origins.length; i++)
        if (strings[i] != null)
          TextShape(strings[i]!, Offset(origins[2 * i], origins[2 * i + 1]),
              _foregroundStyle(fonts[i], paint), _childClip),
    ];
  }

  /// The font's style drawn with [paint]; a colour on the style would conflict with it.
  TextStyle _foregroundStyle(VFont? font, Paint paint) {
    final base = FontUtils.textStyleFromVFont(font, _context, applyDpiScaling: true);
    return TextStyle(
      fontFamily: base.fontFamily,
      fontFamilyFallback: base.fontFamilyFallback,
      fontSize: base.fontSize,
      fontWeight: base.fontWeight,
      fontStyle: base.fontStyle,
      foreground: paint,
    );
  }

  VPath? _textClipKey;
  List<Shape>? _textClipMask;

  /// The coverage of a clip set from a Path holding text: its geometry and its glyphs, opaque.
  List<Shape>? get textClipMask {
    final clip = state.clippingText;
    if (clip == null) return null;
    if (identical(clip, _textClipKey)) return _textClipMask;
    _textClipKey = clip;
    const opaque = Color(0xFF000000);
    final geometry = _buildPath(clip);
    return _textClipMask = [
      if (geometry != null) PathShape(geometry, opaque, 0, lineCap, lineJoin, isFilled: true),
      ..._pathTexts(clip, isFilled: true, color: opaque),
    ];
  }

  /// A Dart-backed Path has no handle, so this points/types pair is the whole geometry.
  ///
  /// [fillType] overrides the GC's fill rule, for a caller whose shape fills by a rule of its own.
  Path? _buildPath(VPath? vPath, {PathFillType? fillType}) =>
      _buildPathData(vPath?.pathData, fillType: fillType);

  Path? _buildPathData(VPathData? pathData, {PathFillType? fillType}) {
    final types = pathData?.types;
    final points = pathData?.points;
    if (types == null || points == null || types.isEmpty) return null;
    final path = Path()
      ..fillType = fillType ?? _fillType;
    var i = 0;
    bool has(int n) => i + n <= points.length;
    for (final type in types) {
      switch (type) {
        case SWT.PATH_MOVE_TO:
          if (!has(2)) return path;
          path.moveTo(points[i++], points[i++]);
        case SWT.PATH_LINE_TO:
          if (!has(2)) return path;
          path.lineTo(points[i++], points[i++]);
        case SWT.PATH_QUAD_TO:
          if (!has(4)) return path;
          path.quadraticBezierTo(
              points[i++], points[i++], points[i++], points[i++]);
        case SWT.PATH_CUBIC_TO:
          if (!has(6)) return path;
          path.cubicTo(points[i++], points[i++], points[i++], points[i++],
              points[i++], points[i++]);
        case SWT.PATH_CLOSE:
          path.close();
      }
    }
    return path;
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
    // A negative width or height mirrors the bounding box, as upstream normalises it.
    final rect = Rect.fromPoints(Offset(x.toDouble(), y.toDouble()),
        Offset((x + width).toDouble(), (y + height).toDouble()));
    if (isFilled &&
        _patterned &&
        _addPatternFill(
            piePath(rect, _degToRad(-startAngle.toDouble()), _degToRad(-arcAngle.toDouble())))) {
      return;
    }
    final color = isFilled ? applyAlpha(fillColor) : applyAlpha(strokeColor);
    final strokeWidth = isFilled ? 0.0 : lineWidth;
    if (!isFilled &&
        _addDashedStroke(
            () => Path()
              ..addArc(rect, _degToRad(-startAngle.toDouble()),
                  _degToRad(-arcAngle.toDouble())),
            color)) {
      return;
    }
    _addShape(ArcShape(
        rect,
        _degToRad(-startAngle.toDouble()),
        _degToRad(-arcAngle.toDouble()),
        color,
        strokeWidth,
        lineCap,
        lineJoin,
        isFilled: isFilled,
        clipRect: _childClip));
  }

  VFont? _styledFont;
  BuildContext? _styledContext;
  int _styledColor = 0;
  TextStyle? _style;

  /// The style text is drawn in, the same object while the GC's font and colour stay the same: the
  /// layout cache compares styles, and an identical one is the cheapest to compare.
  TextStyle _textStyle(VFont? font, Color color) {
    final argb = color.toARGB32();
    final held = _style;
    if (held != null && identical(font, _styledFont) && identical(_context, _styledContext) && argb == _styledColor) {
      return held;
    }
    _styledFont = font;
    _styledContext = _context;
    _styledColor = argb;
    return _style = FontUtils.textStyleFromVFont(font, _context, color: color, applyDpiScaling: true);
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

    final textStyle = _textStyle(state.font, applyAlpha(textColor));

    final childClip = _childClip;
    final textShape = TextShape(processedText, Offset(x, y), textStyle, childClip);

    if (!transparent) {
      final tp = textShape.painter;
      _addShape(RectShape(Rect.fromLTWH(x, y, tp.width, tp.height),
          applyAlpha(textBackgroundColor), 0, lineCap, lineJoin,
          isFilled: true, clipRect: childClip));
    }
    _addShape(textShape);
  }

  // Tabs and mnemonics as GCHelper.textExtent measures them, so text is drawn as wide as it measures.
  String _processTextFlags(String text, int flags) {
    final mnemonic =
        (flags & SWT.DRAW_MNEMONIC) != 0 ? stripAccelerators(text) : text;
    return mnemonic
        .replaceAll('\r\n', (flags & SWT.DRAW_DELIMITER) != 0 ? '\n' : ' ')
        .replaceAll('\r', (flags & SWT.DRAW_DELIMITER) != 0 ? '\n' : ' ')
        .replaceAll('\n', (flags & SWT.DRAW_DELIMITER) != 0 ? '\n' : ' ')
        .replaceAll('\t', (flags & SWT.DRAW_TAB) != 0 ? '        ' : ' ');
  }

  // ── Image handling ──────────────────────────────────────────────────────

  Future<void> _handleImageInit(VImage vImage) async {
    final ref = vImage.remoteRef;
    final render = ref == null ? null : ImageUtils.remoteRender(ref);
    final picture = render?.picture;
    if (render != null && picture != null && !render.hasPixels && render.depth < _maxBaseDepth) {
      _adoptBasePicture(picture, render.depth);
    } else {
      _adoptBaseImage(await ImageUtils.decodeVImageToUIImage(vImage));
    }
    // Size comes from the Image's own metadata, which rides every push. The pixel payload is
    // omitted whenever Flutter already owns the content (remoteRef), so it cannot be the source of
    // truth for how big the canvas is.
    _imgWidth = vImage.width ?? vImage.imageData?.width ?? _baseImage?.width ?? 0;
    _imgHeight = vImage.height ?? vImage.imageData?.height ?? _baseImage?.height ?? 0;
  }

  void _adoptBaseImage(ui.Image? image) {
    final previous = _baseImage;
    _baseImage = image;
    _basePicture = null;
    _baseDepth = 0;
    previous?.dispose();
  }

  /// Not disposed here: committed display lists may still draw it (see [RemoteRender.dispose]).
  void _adoptBasePicture(ui.Picture picture, int depth) {
    _baseImage?.dispose();
    _baseImage = null;
    _basePicture = picture;
    _baseDepth = depth;
  }

  /// Pixels per GC unit of the Image being drawn on: its buffer may be denser than the size the
  /// application gave it, and the ops are in the application's units.
  double get _bufferScale => state.bufferScale ?? 1;

  void _scaleToBuffer(ui.Canvas canvas) {
    final scale = _bufferScale;
    if (scale != 1) canvas.scale(scale);
  }

  /// Whether an opaque op in [shapesToRender] paints over the whole image, hiding the base.
  bool _hidesBase(List<Shape> shapesToRender) {
    final scale = _bufferScale;
    final whole = ui.Rect.fromLTWH(0, 0, (_imgWidth > 0 ? _imgWidth : 1) / scale,
        (_imgHeight > 0 ? _imgHeight : 1) / scale);
    for (final shape in shapesToRender) {
      final covered = shape.opaqueCoverage;
      if (covered != null && _containsRect(covered, whole)) return true;
    }
    return false;
  }

  void _drawBase(ui.Canvas canvas, int w, int h) {
    final picture = _basePicture;
    if (picture != null) {
      canvas.drawPicture(picture);
    } else if (_baseImage != null) {
      canvas.drawImage(_baseImage!, ui.Offset.zero, ui.Paint());
    } else {
      canvas.drawRect(
        ui.Rect.fromLTWH(0, 0, w.toDouble(), h.toDouble()),
        ui.Paint()..color = const ui.Color(0xFFFFFFFF),
      );
    }
  }

  Future<ui.Image> _paintToImage(List<Shape> shapesToRender) async {
    final w = _imgWidth > 0 ? _imgWidth : 1;
    final h = _imgHeight > 0 ? _imgHeight : 1;

    final picture = perfMarkValueDetail('GC.recordPicture', () {
      final recorder = ui.PictureRecorder();
      final canvas = ui.Canvas(recorder);
      _drawBase(canvas, w, h);
      _scaleToBuffer(canvas);
      for (final shape in shapesToRender) {
        shape.draw(canvas);
      }
      return recorder.endRecording();
    });
    return perfMarkValue('GC.rasterise', () => picture.toImage(w, h));
  }

  ui.Picture _paintToPicture(List<Shape> shapesToRender, {bool drawBase = true}) {
    final w = _imgWidth > 0 ? _imgWidth : 1;
    final h = _imgHeight > 0 ? _imgHeight : 1;
    return perfMarkValueDetail('GC.recordPicture', () {
      final recorder = ui.PictureRecorder();
      final canvas = ui.Canvas(recorder);
      if (drawBase) _drawBase(canvas, w, h);
      _scaleToBuffer(canvas);
      for (final shape in shapesToRender) {
        shape.draw(canvas);
      }
      return recorder.endRecording();
    });
  }


  Future<Uint8List?> _paintToPngBytes(List<Shape> shapesToRender) async {
    final image = await _paintToImage(shapesToRender);
    final bytes = await ImageUtils.toBytes(image, ui.ImageByteFormat.png);
    image.dispose();
    return bytes;
  }

  /// Renders the drawn state and keeps it here, registered under the ref Java minted. Nothing
  /// crosses back unless [wantPixels] — the pixels are fetched later, and only if some Java caller
  /// actually reads them (see `Image/requestPixels` in main.dart).
  Future<void> _renderAndSend(int ref, bool wantPixels, {bool retain = false}) async {
    // Abandoned draw: nobody will ever read this render, so skip painting it. A hot path, since
    // JFace rebuilds its buffer Image on every repaint.
    if (ref == 0 && !wantPixels) {
      if (!retain) _unregisterImageListeners();
      return;
    }
    // Registered as a picture; only a reader that needs pixels rasterises it.
    final hidesBase = _hidesBase(shapes);
    final picture = _paintToPicture(shapes, drawBase: !hidesBase);
    final depth = hidesBase || _basePicture == null ? 0 : _baseDepth + 1;
    if (ref != 0) {
      ImageUtils.registerRemotePicture(ref, picture, _imgWidth > 0 ? _imgWidth : 1,
          _imgHeight > 0 ? _imgHeight : 1, depth: depth);
    }
    // A retained drawer's next base stays a picture until it nests [_maxBaseDepth] deep.
    final flatten = retain && depth >= _maxBaseDepth;
    final needsPixels = wantPixels || flatten || ref == 0;
    final image = needsPixels
        ? (ref != 0
            ? ImageUtils.remoteRender(ref)!.rasteriseSync()
            : perfMarkValue('GC.rasterise', () => picture.toImageSync(
                _imgWidth > 0 ? _imgWidth : 1, _imgHeight > 0 ? _imgHeight : 1)))
        : null;
    if (wantPixels) {
      final pngBytes = await ImageUtils.toBytes(image!, ui.ImageByteFormat.png);
      // Binary path: raw PNG bytes, no base64 (the comm channel carries them verbatim).
      if (pngBytes != null) {
        EquoCommService.sendTo(state.wireType, state.id, WireChannel.imageResult, pngBytes);
      }
    }
    // Unregistered: nothing here owns it, so nothing would ever dispose it.
    if (ref == 0) image!.dispose();
    if (retain) {
      // This render becomes the next base and already contains its ops, so they are dropped.
      if (ref == 0) {
        _adoptBaseImage(null);
      } else if (flatten) {
        _adoptBaseImage(image!.clone());
      } else {
        _adoptBasePicture(picture, depth);
      }
      shapes.clear();
      return;
    }
    _unregisterImageListeners();
  }

  /// Non-terminal counterpart to [_renderAndSend]: paints the current staged + committed shapes
  /// and sends the result on its own channel, without clearing state or unregistering listeners.
  Future<void> _renderSnapshotAndSend() async {
    final bytes = await _paintToPngBytes([...shapes, ..._staging]);
    if (bytes == null) return;
    EquoCommService.sendTo(state.wireType, state.id, WireChannel.imageSnapshotResult, bytes);
  }

  // ── CopyArea helpers ────────────────────────────────────────────────────

  // SWT's GC#copyArea copies the pixels the GC's drawable already shows. A GC opened over
  // an existing Image starts with that image's pixels as its content and an empty shape
  // list, so translating shapes alone copies nothing — the drawable's own content, the
  // base image, has to be copied too. JFace's scrolling double-buffers depend on that:
  // LineNumberRulerColumn re-uses one buffer Image across paints and shifts it by the
  // scroll delta, redrawing only the newly exposed band, so without the base copy the
  // retained band keeps its pre-scroll content.
  /// A picture-backed blit that [clip] has to bound. The web renderers ignore a clip around a
  /// nested picture -- it draws in full, past the clip (see [_drawPicture]) -- so the picture goes
  /// to pixels first. [ui.Picture.toImageSync] keeps them on the GPU, so nothing is read back, and
  /// [disposeShapeImages] owns the image the shape carries.
  /// [sourceScale] is the picture's units per unit of [srcRect], as [ImageShape.sourceScale].
  static ImageShape _boundedPicture(
      ui.Picture picture, Rect srcRect, Rect destRect, Rect clip,
      {int alpha = 255, double sourceScale = 1}) {
    final k = sourceScale;
    final source = Rect.fromLTRB(srcRect.left * k, srcRect.top * k, srcRect.right * k, srcRect.bottom * k);
    final raster = picture.toImageSync(source.right.ceil(), source.bottom.ceil());
    return ImageShape.raster(raster, source, destRect, clipRect: clip, alpha: alpha);
  }

  @visibleForTesting
  static List<Shape> copyAreaShapes({
    required ui.Image? baseImage,
    ui.Picture? basePicture,
    required List<Shape> painted,
    required Rect srcRect,
    required Offset destOffset,
    Offset baseOffset = Offset.zero,
    double bufferScale = 1,
  }) {
    final destRect = srcRect.shift(destOffset);
    // The base is in the buffer's pixels, [bufferScale] to a unit of the area copied.
    final s = bufferScale;
    return [
      // Under the shapes drawn through this GC, so the copy keeps their z-order.
      if (basePicture != null)
        _boundedPicture(basePicture, srcRect, destRect, destRect, sourceScale: s)
      else if (baseImage != null)
        ImageShape.raster(baseImage.clone(),
            Rect.fromLTRB(srcRect.left * s, srcRect.top * s, srcRect.right * s, srcRect.bottom * s)
                .shift(baseOffset),
            destRect, clipRect: destRect),
      for (final shape in painted.where((s) => _shapeIntersects(s, srcRect)))
        _translateShapeWithClip(shape, destOffset, srcRect),
    ];
  }

  static bool _shapeIntersects(Shape shape, Rect area) {
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
      _ScreenCopy s => s.base.destRect.overlaps(area),
      EraseShape s => s.rect.overlaps(area),
      _ => true,
    };
  }

  static Shape _translateShapeWithClip(Shape shape, Offset offset, Rect srcArea) {
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
      TextShape s => s.translated(offset, clipArea),
      PointShape s => PointShape(s.point + offset, s.color, clipArea),
      PolygonShape s => PolygonShape(
          _translatePoints(s.points, offset), s.color, s.strokeWidth,
          s.lineCap, s.lineJoin,
          isFilled: s.isFilled, fillType: s.fillType, clipRect: clipArea),
      ArcShape s => ArcShape(
          s.rect.translate(offset.dx, offset.dy), s.startAngle, s.sweepAngle,
          s.color, s.strokeWidth, s.lineCap, s.lineJoin,
          isFilled: s.isFilled, clipRect: clipArea),
      RoundRectShape s => RoundRectShape(
          s.rect.translate(offset.dx, offset.dy), s.radiusX, s.radiusY,
          s.color, s.strokeWidth, s.lineCap, s.lineJoin,
          isFilled: s.isFilled, clipRect: clipArea),
      PathShape s => PathShape(
          s.path.shift(offset), s.color, s.strokeWidth, s.lineCap, s.lineJoin,
          isFilled: s.isFilled, clipRect: clipArea),
      PatternFillShape s => s.translated(offset, clipArea),
      ClipPathShape s => ClipPathShape(
          s.path.shift(offset)..fillType = s.path.fillType,
          s.children.map((c) => _translateShapeWithClip(c, offset, srcArea)).toList()),
      GradientRectShape s => GradientRectShape.aligned(
          s.rect.translate(offset.dx, offset.dy),
          s.fromColor, s.toColor, s.begin, s.end, clipArea),
      FocusRectShape s => FocusRectShape(
          s.rect.translate(offset.dx, offset.dy), s.color, clipArea),
      _ScreenCopy s => _ScreenCopy(
          _translateShapeWithClip(s.base, offset, srcArea) as ImageShape, s.screenSource, s.shift + offset),
      EraseShape s => EraseShape(s.rect.shift(offset).intersect(clipArea)),
      ImageShape s when s.type == ImageType.raster => ImageShape.raster(
          s.image!, s.srcRect!, s.destRect.translate(offset.dx, offset.dy),
          clipRect: clipArea, colorFilter: s.colorFilter, alpha: s.alpha,
          opaqueSource: s.opaqueSource, inkMask: s.inkMask),
      ImageShape s when s.type == ImageType.picture => _boundedPicture(
          s.remotePicture!, s.srcRect!, s.destRect.translate(offset.dx, offset.dy),
          clipArea, alpha: s.alpha, sourceScale: s.sourceScale),
      ImageShape s when s.type == ImageType.svg => ImageShape.svg(
          s.pictureInfo!, s.destRect.translate(offset.dx, offset.dy),
          clipRect: clipArea, colorFilter: s.colorFilter, alpha: s.alpha),
      // copyArea shifts by a device-space offset, so it goes on the matrix's translation column
      // rather than on children that are drawing in the transform's own coordinates.
      TransformShape s =>
        TransformShape(_translatedMatrix(s.matrix, offset), s.children, clipArea),
      _ => shape,
    };
  }

  static Float64List _translatedMatrix(Float64List matrix, Offset offset) {
    return Float64List.fromList(matrix)
      ..[12] += offset.dx
      ..[13] += offset.dy;
  }

  static List<int> _translatePoints(List<int> points, Offset offset) {
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
      _addArcShape(x: o.x, y: o.y, width: o.width,
          height: o.height, startAngle: o.startAngle,
          arcAngle: o.arcAngle, isFilled: false);

  @override
  void onFillArcintintintintintint(VGCFillArcintintintintintint o) =>
      _addArcShape(x: o.x, y: o.y, width: o.width,
          height: o.height, startAngle: o.startAngle,
          arcAngle: o.arcAngle, isFilled: true);

  @override
  void onDrawFocusintintintint(VGCDrawFocusintintintint o) {
    final rect = _getRectFromArgs(o.x, o.y, o.width, o.height);
    _addShape(FocusRectShape(rect, applyAlpha(focusColor), _childClip));
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
    _processImageAsync(o.image!, opArgs);
  }

  @override
  void onDrawImageImageintintintint(VGCDrawImageImageintintintint o) {
    if (o.image == null) return;
    final opArgs = VGCDrawImageImageintintintintintintintint(
      destX: o.destX, destY: o.destY, destWidth: o.destWidth, destHeight: o.destHeight,
      srcX: 0, srcY: 0, srcWidth: -1, srcHeight: -1,
    );
    opArgs.image = o.image;
    _processImageAsync(o.image!, opArgs);
  }

  @override
  void onDrawImageImageintintintintintintintint(
      VGCDrawImageImageintintintintintintintint o) {
    if (o.image == null) return;
    // Copied: the image resolves after this op, and the op's object is refilled by the next one.
    final opArgs = VGCDrawImageImageintintintintintintintint(
      destX: o.destX, destY: o.destY, destWidth: o.destWidth, destHeight: o.destHeight,
      srcX: o.srcX, srcY: o.srcY, srcWidth: o.srcWidth, srcHeight: o.srcHeight,
    );
    opArgs.image = o.image;
    _processImageAsync(o.image!, opArgs);
  }

  void _processImageAsync(
      VImage vImage, VGCDrawImageImageintintintintintintintint opArgs) {
    _stageAsync(ImageShape.fromVImageDetailed(vImage, opArgs, _childClip,
        tint: imageTintColor, glyphLimits: glyphTintLimits,
        alpha: state.alpha ?? 255, useThemeColors: _useThemeColors));
  }

  /// Stages a shape that resolves after its op, in that op's slot.
  void _stageAsync(Future<Shape> f) {
    // Capture the staging list this op belongs to: gcDispose() may reassign
    // `_staging` to a fresh list for the next paint cycle before this image
    // finishes decoding, and _onImageLoaded must write back into the list it
    // was scheduled against, not whatever `_staging` points to when it fires.
    final stagingList = _staging;
    // The placeholder stays unwrapped so gcDispose still finds it; the transform in force at op
    // time wraps the decoded image, since state may have moved on by the time it resolves.
    final transform = currentTransform;
    final deviceClip = transform == null ? null : clipping;
    final idx = stagingList.length;
    stagingList.add(_PlaceholderShape());
    _pendingImages.add(f);
    // The clip is read at op time too: the state may have moved on by the time the image resolves.
    final shapeClip = clipShape;
    final textMask = textClipMask;
    final antiAlias = _antiAlias;
    final filter = _filterQuality;
    f.then((decoded) {
      final Shape imageShape = antiAlias && filter == null
          ? decoded
          : RenderHintsShape(antiAlias, filter, [decoded]);
      final Shape shape = transform == null
          ? imageShape
          : TransformShape(transform, [imageShape], deviceClip);
      _onImageLoaded(stagingList, idx,
          textMask != null
              ? MaskShape(textMask, [shape])
              : shapeClip == null ? shape : ClipPathShape(shapeClip, [shape]));
    });
  }

  void _onImageLoaded(List<Shape> stagingList, int idx, Shape shape) {
    if (idx < stagingList.length) {
      stagingList[idx] = shape;
    } else {
      _lateLoadedImages.add(shape);
    }
  }

  @override
  void onDrawLineintintintint(VGCDrawLineintintintint o) {
    final from = Offset(o.x1.toDouble(), o.y1.toDouble());
    final to = Offset(o.x2.toDouble(), o.y2.toDouble());
    final pattern = _dashPattern;
    if (pattern != null) {
      _addShape(SegmentsShape(_dashSegments(from, to, pattern), applyAlpha(lineColor), lineWidth,
          lineCap, _childClip));
      return;
    }
    _addShape(LineShape(
        from, to, applyAlpha(lineColor), lineWidth, lineCap, lineJoin, _childClip));
  }

  @override
  void onDrawOvalintintintint(VGCDrawOvalintintintint o) =>
      _addOvalShape(x: o.x, y: o.y, width: o.width,
          height: o.height, isFilled: false);

  @override
  void onFillOvalintintintint(VGCFillOvalintintintint o) =>
      _addOvalShape(x: o.x, y: o.y, width: o.width,
          height: o.height, isFilled: true);

  @override
  void onDrawPathPath(VGCDrawPathPath o) => _addPathShape(o.path, isFilled: false);

  @override
  void onFillPathPath(VGCFillPathPath o) => _addPathShape(o.path, isFilled: true);

  @override
  void onDrawPointintint(VGCDrawPointintint o) {
    _addShape(PointShape(
        Offset(o.x.toDouble(), o.y.toDouble()),
        applyAlpha(pointColor), _childClip));
  }

  @override
  void onDrawPolygonint(VGCDrawPolygonint o) =>
      _addPolygonShape(points: o.pointArray ?? [], isFilled: false, minPoints: 4);

  @override
  void onFillPolygonint(VGCFillPolygonint o) =>
      _addPolygonShape(points: o.pointArray ?? [], isFilled: true, minPoints: 6);

  @override
  void onDrawPolylineint(VGCDrawPolylineint o) =>
      _addPolylineShape(points: o.pointArray ?? [], isFilled: false, minPoints: 2);

  @override
  void onDrawRectangleintintintint(VGCDrawRectangleintintintint o) =>
      _addRectShape(x: o.x, y: o.y, width: o.width,
          height: o.height, isFilled: false);

  @override
  void onFillRectangleintintintint(VGCFillRectangleintintintint o) =>
      _addRectShape(x: o.x, y: o.y, width: o.width,
          height: o.height, isFilled: true);

  @override
  void onDrawRoundRectangleintintintintintint(
      VGCDrawRoundRectangleintintintintintint o) =>
      _addRoundRectShape(x: o.x, y: o.y, width: o.width,
          height: o.height, arcWidth: o.arcWidth,
          arcHeight: o.arcHeight, isFilled: false);

  @override
  void onFillRoundRectangleintintintintintint(
      VGCFillRoundRectangleintintintintintint o) =>
      _addRoundRectShape(x: o.x, y: o.y, width: o.width,
          height: o.height, arcWidth: o.arcWidth,
          arcHeight: o.arcHeight, isFilled: true);

  @override
  void onDrawTextStringintintint(VGCDrawTextStringintintint o) =>
      _drawText(text: o.string, x: o.x.toDouble(),
          y: o.y.toDouble(), flags: o.flags);

  @override
  void onFillGradientRectangleintintintintboolean(
      VGCFillGradientRectangleintintintintboolean o) {
    final rect = _getRectFromArgs(o.x, o.y, o.width, o.height);
    _addShape(GradientRectShape(
        rect, applyAlpha(gradientStartColor), applyAlpha(gradientEndColor),
        o.vertical, _childClip));
  }

  @override
  void onCopyAreaintintintintintintboolean(
      VGCCopyAreaintintintintintintboolean o) =>
      _copyArea(o.srcX, o.srcY, o.width, o.height, o.destX, o.destY);

  @override
  void onCopyAreaImageintint(VGCCopyAreaImageintint o) async {
    // Java waits for an answer to every read-back: with none it would wait out its whole budget.
    // No pixels answers with none, and the Image is left as it was.
    Uint8List? pngBytes;
    try {
      final imgW = o.image?.width ?? o.image?.imageData?.width ?? 0;
      final imgH = o.image?.height ?? o.image?.imageData?.height ?? 0;
      if (imgW <= 0 || imgH <= 0) return;
      final x = o.x.toDouble();
      final y = o.y.toDouble();

      // A page that has rasterised no frame yet has no layers to capture: on Skwasm drawing a
      // capture taken then traps and leaves the page unable to draw. Bounded, since a page whose
      // view is never shown rasterises none.
      final rasterized = await WidgetsBinding.instance.waitUntilFirstFrameRasterized
          .then((_) => true)
          .timeout(const Duration(seconds: 5), onTimeout: () => false);
      // The raster read below is the last painted frame; state that arrived before this request
      // is only on screen once its frame is.
      if (WidgetsBinding.instance.hasScheduledFrame) await WidgetsBinding.instance.endOfFrame;

      // Determine the canvas size needed to include the area being captured.
      if (_baseImageCompleter != null) await _baseImageCompleter!.future;
      final renderW = math.max(
          _imgWidth > 0 ? _imgWidth : (x + imgW).toInt(), (x + imgW).toInt());
      final renderH = math.max(
          _imgHeight > 0 ? _imgHeight : (y + imgH).toInt(),
          (y + imgH).toInt());

      // Render: widget pixels > base image > white background.
      // Then apply any shapes drawn via this GC on top.
      final recorder = ui.PictureRecorder();
      final canvas = ui.Canvas(recorder);
      final captured = rasterized ? _captureWidget() : null;
      if (captured != null) {
        canvas.drawImage(captured.image, -captured.offset, ui.Paint());
        captured.image.dispose();
      } else {
        _drawBase(canvas, renderW, renderH);
      }
      for (final shape in [...shapes, ..._staging]) {
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

      pngBytes = await ImageUtils.toBytes(result, ui.ImageByteFormat.png);
    } catch (e, stack) {
      print('[GC copyArea] Error: $e\n$stack');
    } finally {
      // Binary path: raw PNG bytes, no base64 (see imageResult above).
      EquoCommService.sendTo(
          state.wireType, state.id, WireChannel.copyAreaImageintintResponse, pngBytes ?? Uint8List(0));
    }
  }

  void _copyArea(int srcX, int srcY, int width, int height, int destX, int destY) {
    // A GC on a control has no base of its own: what it copies is what the control shows, which
    // earlier paints left there and this GC has not drawn.
    final captured =
        _baseImage == null && _basePicture == null ? _captureWidget() : null;
    final srcRect = _getRectFromArgs(srcX, srcY, width, height);
    final destOffset = Offset((destX - srcX).toDouble(), (destY - srcY).toDouble());
    final copied = copyAreaShapes(
      baseImage: captured?.image ?? _baseImage,
      basePicture: _basePicture,
      painted: _staging,
      srcRect: srcRect,
      destOffset: destOffset,
      baseOffset: captured?.offset ?? Offset.zero,
      // A control's GC captures what the control shows, at its own units.
      bufferScale: captured == null ? _bufferScale : 1,
    );
    // Whether the screen as captured is what the copy starts from is known when the cycle ends.
    if (captured != null) copied[0] = _ScreenCopy(copied[0] as ImageShape, srcRect, destOffset);
    _staging.addAll(copied);
    captured?.image.dispose();
  }

  @override
  void dispose() {
    super.dispose();
    _baseImage?.dispose();
    _baseImage = null;

    final seen = <ui.Image>{};
    disposeShapeImages(shapes, seen: seen);
    disposeShapeImages(_staging, seen: seen);
    disposeShapeImages(_lateLoadedImages, seen: seen);
  }
}

// Each image is an independently disposable clone; track [seen] since copyArea can duplicate
// a shape while reusing the same ui.Image reference, and double-disposing throws.
void disposeShapeImages(List<Shape> shapes, {Set<ui.Image>? seen, Set<ui.Image>? keep}) {
  // Most shapes hold no image, and a walk that meets none makes no set.
  Set<ui.Image>? made = seen;
  for (var k = 0; k < shapes.length; k++) {
    final s = shapes[k];
    if (!s.holdsImages) continue;
    final disposed = made ??= <ui.Image>{};
    if (s is ImageShape && s.type == ImageType.raster && s.image != null) {
      final img = s.image!;
      if (keep != null && keep.contains(img)) continue;
      if (disposed.add(img)) {
        img.dispose();
      }
      s.image = null;
    } else if (s is PatternFillShape && s.image != null) {
      final img = s.image!;
      if (keep != null && keep.contains(img)) continue;
      if (disposed.add(img)) {
        img.dispose();
      }
      s.image = null;
    } else if (s is _ScreenCopy && s.base.image != null) {
      final img = s.base.image!;
      if (keep != null && keep.contains(img)) continue;
      if (disposed.add(img)) {
        img.dispose();
      }
      s.base.image = null;
    } else if (s is TransformShape) {
      disposeShapeImages(s.children, seen: disposed, keep: keep);
    } else if (s is RegionShape) {
      disposeShapeImages(s.ops, seen: disposed, keep: keep);
    } else if (s is XorShape) {
      disposeShapeImages(s.children, seen: disposed, keep: keep);
    }
  }
}

void collectShapeImages(List<Shape> shapes, Set<ui.Image> out) {
  for (var k = 0; k < shapes.length; k++) {
    final s = shapes[k];
    if (!s.holdsImages) continue;
    if (s is ImageShape && s.type == ImageType.raster && s.image != null) {
      out.add(s.image!);
    } else if (s is PatternFillShape && s.image != null) {
      out.add(s.image!);
    } else if (s is _ScreenCopy && s.base.image != null) {
      out.add(s.base.image!);
    } else if (s is TransformShape) {
      collectShapeImages(s.children, out);
    } else if (s is RegionShape) {
      collectShapeImages(s.ops, out);
    } else if (s is XorShape) {
      collectShapeImages(s.children, out);
    }
  }
}

class _PlaceholderShape extends Shape {
  @override
  void draw(ui.Canvas c) {}
  @override
  String toString() => 'Placeholder';

  @override
  String describe() => 'Placeholder';
}

// ─────────────────────────────────────────────
// ScenePainter
// ─────────────────────────────────────────────

class ScenePainter extends CustomPainter {
  ScenePainter(this.bg, this.shapes, {this.originY = 0});
  final Color bg;
  final List<Shape> shapes;

  /// Content y of the painted slice; shapes stay in content coordinates and the canvas is shifted.
  final double originY;

  @override
  void paint(Canvas canvas, Size size) {
    // A transparent erase (the Canvas shows an ancestor's backgroundImage through) still has to
    // clear the retained frame under a scoped repaint; BlendMode.clear would punch through
    // whatever is painted below unless this scene sits in its own layer.
    final ownLayer = bg.alpha == 0 && shapes.any((s) => s is RegionShape || s is EraseShape);
    if (ownLayer) canvas.saveLayer(Offset.zero & size, Paint());
    canvas.drawRect(Offset.zero & size, Paint()..color = bg);
    canvas.save();
    if (originY != 0) canvas.translate(0, -originY);
    drawShapeRun(canvas, shapes, background: bg);
    canvas.restore();
    if (ownLayer) canvas.restore();
  }

  @override
  bool shouldRepaint(ScenePainter o) {
    // Compare shapes, not the list: every build makes a fresh list.
    if (o.bg != bg || o.originY != originY) return true;
    if (o.shapes.length != shapes.length) return true;
    for (int i = 0; i < shapes.length; i++) {
      final was = o.shapes[i];
      final now = shapes[i];
      if (identical(was, now)) continue;
      // Sound only because the painted slice is pinned by originY, not by the live clip.
      if (now.paintsSameAs(was)) continue;
      return true;
    }
    return false;
  }
}

// ─────────────────────────────────────────────
// Shape classes
// ─────────────────────────────────────────────

/// Formatting helpers for [Shape.describe]. Coordinates are rounded: SWT hands the GC integers,
/// and a fractional value here would only carry rasterization noise into the description.
String _dr(Rect? r) => r == null
    ? '-'
    : '${r.left.round()},${r.top.round()} ${r.width.round()}x${r.height.round()}';
String _do(Offset o) => '${o.dx.round()},${o.dy.round()}';
// ignore: deprecated_member_use
String _dc(Color c) => '#${c.value.toRadixString(16).padLeft(8, '0')}';
String _dk(Rect? c) => c == null ? '' : ' clip=${_dr(c)}';
String _dn(List<Shape> children) => children.map((s) => s.describe()).join('; ');

/// Draws [shape]'s body under its own clip.
void _drawClipped(ui.Canvas c, Shape shape) {
  final clip = shape.clipRect;
  if (clip != null) { c.save(); c.clipRect(clip); }
  shape.paintBody(c);
  if (clip != null) c.restore();
}

/// Draws [shapes] as drawing each in order would, in fewer canvas calls: shapes that share a clip are
/// clipped once, and consecutive straight strokes and convex fills go as one triangle list.
/// [background] is the scene's, for the scoped repaints ([RegionShape]) a scene holds.
void drawShapeRun(ui.Canvas c, List<Shape> shapes, {ui.Color? background}) {
  // No closures here: a call would allocate what they capture, on every frame that repaints.
  // One device pixel, read from the canvas the first time a run needs it; 0 until then.
  var px = 0.0;
  Rect? open;
  var clipped = false;

  var i = 0;
  while (i < shapes.length) {
    final s = shapes[i];
    if (s is RegionShape && background != null) {
      if (clipped) c.restore();
      clipped = false;
      open = null;
      s.drawOver(c, background);
      i++;
      continue;
    }
    if (s is EraseShape) {
      if (background != null) {
        if (clipped) c.restore();
        clipped = false;
        open = null;
        s.drawOver(c, background);
      }
      i++;
      continue;
    }
    if (s is ClipPathShape) {
      if (clipped) c.restore();
      clipped = false;
      open = null;
      // The GC keeps its clip as one path while the clip holds, so consecutive shapes under it share
      // it: clipped once instead of once each, clipping to a path being the costly part on a GPU.
      var end = i + 1;
      while (end < shapes.length) {
        final next = shapes[end];
        if (next is! ClipPathShape || !identical(next.path, s.path)) break;
        end++;
      }
      c.save();
      c.clipPath(s.path);
      drawShapeRun(c, [for (var k = i; k < end; k++) ...(shapes[k] as ClipPathShape).children]);
      c.restore();
      i = end;
      continue;
    }
    if (!s.clipsBody) {
      if (clipped) c.restore();
      clipped = false;
      open = null;
      s.draw(c);
      i++;
      continue;
    }
    final clip = s.clipRect;
    if (clip != open || (clip == null && clipped)) {
      if (clipped) c.restore();
      clipped = false;
      open = null;
      if (clip != null) {
        c.save();
        c.clipRect(clip);
        clipped = true;
        open = clip;
      }
    }
    // A run needs a second shape to join it: one alone draws as itself.
    if (!_mayJoinRun(s) ||
        i + 1 == shapes.length ||
        !_mayJoinRun(shapes[i + 1]) ||
        shapes[i + 1].clipRect != clip) {
      s.paintBody(c);
      i++;
      continue;
    }
    if (px == 0) px = _devicePixel(c);
    final count = _trianglesFrom(shapes, i, px);
    if (count > 1) {
      final run = _run;
      var vertices = 0, indices = 0;
      for (var k = 0; k < count; k++) {
        vertices += run[k]!.vertices;
        indices += run[k]!.indices;
      }
      final list = _TriangleList(vertices, indices, px);
      for (var k = 0; k < count; k++) {
        run[k]!.addTo(list);
      }
      c.drawVertices(list.build(), BlendMode.dst, _runPaint);
      i += count;
    } else {
      s.paintBody(c);
      i++;
    }
  }
  if (clipped) c.restore();
}

/// What a triangle run draws with: the colours are the vertices', so the paint carries nothing.
final Paint _runPaint = Paint();

/// What a triangle run draws for one shape: its geometry, prepared once.
abstract class _Piece {
  int get vertices;
  int get indices;
  void addTo(_TriangleList list);
}

/// Straight segments with flat or square caps. A round cap is not a rectangle, so it stays a stroke.
class _Strokes implements _Piece {
  _Strokes(this.points, this.strokeWidth, this.lineCap, this.color);
  final Float32List points;
  final double strokeWidth;
  final int lineCap;
  final Color color;

  @override
  int get vertices => points.length ~/ 4 * _TriangleList.verticesFor(4);
  @override
  int get indices => points.length ~/ 4 * _TriangleList.indicesFor(4);
  @override
  void addTo(_TriangleList list) => list.addSegments(points, strokeWidth, lineCap, color);
}

/// A filled convex polygon as its outline inset and outset by half a device pixel, corner by corner.
class _ConvexFill implements _Piece {
  _ConvexFill(this.inner, this.outer, this.color);
  final Float32List inner, outer;
  final Color color;

  @override
  int get vertices => _TriangleList.verticesFor(inner.length ~/ 2);
  @override
  int get indices => _TriangleList.indicesFor(inner.length ~/ 2);
  @override
  void addTo(_TriangleList list) => list.addPolygon(inner, outer, color);

  /// [s] as a convex fill, or null when it is not a simple convex polygon whose outline can be moved
  /// half a pixel in and out: those stay paths. Turning the same way at every corner is not enough
  /// alone, since a star turns the same way at every point; it also has to turn once in all.
  static _ConvexFill? of(PolygonShape s, double px) {
    if (!s.isFilled || s.points.length < 6) return null;
    final xs = Float64List(s.points.length ~/ 2), ys = Float64List(s.points.length ~/ 2);
    var n = 0;
    for (var i = 0; i + 1 < s.points.length; i += 2) {
      final x = s.points[i].toDouble(), y = s.points[i + 1].toDouble();
      if (n > 0 && xs[n - 1] == x && ys[n - 1] == y) continue;
      xs[n] = x;
      ys[n] = y;
      n++;
    }
    if (n > 1 && xs[0] == xs[n - 1] && ys[0] == ys[n - 1]) n--;
    if (n < 3) return null;
    var sign = 0.0, turning = 0.0;
    for (var k = 0; k < n; k++) {
      final p = (k + n - 1) % n, q = (k + 1) % n;
      final ax = xs[k] - xs[p], ay = ys[k] - ys[p], bx = xs[q] - xs[k], by = ys[q] - ys[k];
      final cross = ax * by - ay * bx;
      if (cross != 0) {
        if (sign == 0) {
          sign = cross.sign;
        } else if (cross.sign != sign) {
          return null;
        }
      }
      turning += math.atan2(cross, ax * bx + ay * by);
    }
    if (sign == 0 || (turning.abs() - 2 * math.pi).abs() > 0.01) return null;
    // Outward normal of the edge from k to k+1: the interior lies on the side the corners turn to.
    final nx = Float64List(n), ny = Float64List(n);
    for (var k = 0; k < n; k++) {
      final q = (k + 1) % n;
      final ex = xs[q] - xs[k], ey = ys[q] - ys[k];
      final len = math.sqrt(ex * ex + ey * ey);
      nx[k] = sign * ey / len;
      ny[k] = -sign * ex / len;
    }
    final f = _RenderHints.antiAlias ? px / 2 : 0.0;
    final inner = Float32List(n * 2), outer = Float32List(n * 2);
    for (var k = 0; k < n; k++) {
      final p = (k + n - 1) % n;
      final denom = 1 + nx[p] * nx[k] + ny[p] * ny[k];
      // A corner this sharp would push its offset outline far past the shape.
      if (denom < 0.2) return null;
      final mx = (nx[p] + nx[k]) / denom * f, my = (ny[p] + ny[k]) / denom * f;
      inner[k * 2] = xs[k] - mx;
      inner[k * 2 + 1] = ys[k] - my;
      outer[k * 2] = xs[k] + mx;
      outer[k * 2 + 1] = ys[k] + my;
    }
    // Too small to inset: an edge of the inset outline would run backwards.
    for (var k = 0; k < n; k++) {
      final q = (k + 1) % n;
      if ((inner[q * 2] - inner[k * 2]) * (xs[q] - xs[k]) + (inner[q * 2 + 1] - inner[k * 2 + 1]) * (ys[q] - ys[k]) <= 0) {
        return null;
      }
    }
    return _ConvexFill(inner, outer, s.color);
  }
}

/// Whether [s] is of a kind [_pieceOf] may make a piece of, decided without the device pixel: a
/// shape that cannot join a run is drawn without reading the canvas transform back.
bool _mayJoinRun(Shape s) =>
    (s is LineShape && s.lineCap != SWT.CAP_ROUND) ||
    (s is SegmentsShape && s.lineCap != SWT.CAP_ROUND && s.points.isNotEmpty) ||
    (s is PolygonShape && s.isFilled && s.points.length >= 6);

/// What a triangle run can draw [s] as, or null when it is drawn on its own.
_Piece? _pieceOf(Shape s, double px) {
  if (s is LineShape && s.lineCap != SWT.CAP_ROUND) return s;
  if (s is SegmentsShape && s.lineCap != SWT.CAP_ROUND && s.points.isNotEmpty) {
    return _Strokes(s.points, s.strokeWidth, s.lineCap, s.color);
  }
  if (s is PolygonShape) return _ConvexFill.of(s, px);
  return null;
}

/// Puts in [_run] the pieces of the shapes from [start] under the same clip that one triangle list
/// can hold, and returns how many. Paint may change along the run: triangles in one list are blended
/// in the order they are listed, as separate draws would be, so the run paints what drawing the
/// shapes one by one does.
int _trianglesFrom(List<Shape> shapes, int start, double px) {
  // Kept, and read before the next run is made: no run is made while another is being drawn.
  var count = 0;
  var vertices = 0;
  final clip = shapes[start].clipRect;
  for (var i = start; i < shapes.length; i++) {
    final s = shapes[i];
    if (s.clipRect != clip) break;
    final piece = _pieceOf(s, px);
    if (piece == null || vertices + piece.vertices > _TriangleList.maxVertices) break;
    vertices += piece.vertices;
    if (count == _run.length) _run = List.filled(count * 2, null)..setRange(0, count, _run);
    _run[count++] = piece;
  }
  return count;
}

List<_Piece?> _run = List.filled(64, null);

/// One device pixel in [c]'s current coordinates.
double _devicePixel(ui.Canvas c) {
  final m = c.getTransform();
  final scale = math.sqrt(m[0] * m[0] + m[1] * m[1]);
  return scale > 0 ? 1 / scale : 1;
}

abstract class Shape {
  void draw(ui.Canvas c);

  /// Whether this shape, or one it holds, owns a decoded image that is disposed with it. Most
  /// shapes hold none, so the walks that dispose and keep images skip them on this alone.
  bool get holdsImages => false;

  /// What this shape paints, without its clip. Only meaningful when [clipsBody].
  void paintBody(ui.Canvas c) => draw(c);

  /// Whether [draw] is [paintBody] under [clipRect], so [drawShapeRun] can clip a run of shapes
  /// that share one clip once instead of once per shape.
  bool get clipsBody => false;

  /// Whether [other] would paint exactly what this shape paints. Identity by default, so a shape
  /// that does not compare all its inputs never keeps stale pixels.
  bool paintsSameAs(Shape other) => identical(this, other);
  @override
  String toString();

  /// A complete description of what this shape paints: geometry, colour, and the flags that
  /// change the result.
  ///
  /// Separate from [toString], which is shaped for reading in a debugger and leaves out colour
  /// entirely. This one is an assertion surface — a test compares these strings to decide whether
  /// two paints are the same, so anything that would make the result look different has to appear
  /// here, and nothing that only reflects how it was rasterized may.
  String describe();

  Rect? get clipRect => null;

  /// The area this shape paints into, or null when it cannot be described cheaply. Null is always
  /// the safe answer: a shape of unknown area is never treated as hidden.
  Rect? get paintedBounds => null;

  /// The area this shape paints *opaquely*, or null. Whatever falls entirely inside it is invisible
  /// and can be dropped: a control repainted through a GC opened outside a Paint draws over what the
  /// last cycle left, and keeping both means redrawing every cycle the control ever took.
  ///
  /// Conservative by construction — a shape only claims coverage when it is a filled, fully opaque,
  /// unclipped (or clip-containing) paint of a known rectangle.
  Rect? get opaqueCoverage => null;
}

/// Whether [outer] wholly contains [inner], edges included. Rect.contains excludes the right and
/// bottom edges, so it never reports a rectangle as holding itself.
bool _containsRect(Rect outer, Rect inner) =>
    inner.left >= outer.left &&
    inner.top >= outer.top &&
    inner.right <= outer.right &&
    inner.bottom <= outer.bottom;

/// One scoped Paint: the ops it emitted, and the rectangle it was allowed to touch.
///
/// SWT erases the damaged rectangle before the application paints it and keeps every pixel outside
/// untouched; a composite that reproduces both is indistinguishable from a full repaint.
class RegionShape extends Shape {
  RegionShape(this.rect, this.ops);
  final Rect rect;
  final List<Shape> ops;

  @override
  bool get holdsImages => ops.any((s) => s.holdsImages);

  // A scoped Paint erases its rectangle before painting it, so it covers it outright.
  @override
  Rect? get paintedBounds => rect;

  @override
  Rect? get opaqueCoverage => rect;

  void drawOver(ui.Canvas c, ui.Color background) {
    c.save();
    // Hard-edged: an antialiased clip leaves the boundary pixels a blend of both frames.
    c.clipRect(rect, doAntiAlias: false);
    final erase = ui.Paint()..color = background;
    if (background.alpha == 0) erase.blendMode = ui.BlendMode.clear;
    c.drawRect(rect, erase);
    drawShapeRun(c, ops, background: background);
    c.restore();
  }

  // Headless rendering (copyArea snapshots) has no canvas background to erase to.
  @override
  void draw(ui.Canvas c) {
    c.save();
    c.clipRect(rect, doAntiAlias: false);
    drawShapeRun(c, ops);
    c.restore();
  }

  @override
  String toString() => 'Region $rect [${ops.length} shapes]';

  @override
  String describe() => 'Region ${_dr(rect)} [${_dn(ops)}]';
}

/// A copy of the control's pixels as they were on screen, [shift]ed from [screenSource], until the
/// cycle that made it ends and says what its Paint erased of them first.
class _ScreenCopy extends Shape {
  _ScreenCopy(this.base, this.screenSource, this.shift);
  final ImageShape base;
  final Rect screenSource;
  final Offset shift;

  @override
  bool get holdsImages => base.holdsImages;

  @override
  Rect? get clipRect => base.clipRect;

  @override
  void draw(ui.Canvas c) => base.draw(c);

  @override
  String toString() => 'ScreenCopy $base';

  @override
  String describe() => base.describe();
}

/// Where a copy's source was erased by its Paint before the copy: the scene's background, which
/// only the scene knows.
class EraseShape extends Shape {
  EraseShape(this.rect);
  final Rect rect;

  static final ui.Paint _erase = ui.Paint();

  void drawOver(ui.Canvas c, ui.Color background) {
    c.drawRect(
        rect,
        _erase
          ..color = background
          ..blendMode = background.alpha == 0 ? ui.BlendMode.clear : ui.BlendMode.srcOver);
  }

  // Headless rendering (copyArea snapshots) has no canvas background to erase to.
  @override
  void draw(ui.Canvas c) {}

  @override
  Rect? get paintedBounds => rect;

  @override
  String toString() => 'Erase $rect';

  @override
  String describe() => 'Erase ${_dr(rect)}';
}

/// Ops drawn while `GC.setXORMode(true)` was in force.
///
/// SWT's XOR combines source and destination bitwise. Flutter has no bitwise blend, but
/// [BlendMode.difference] is `|dst - src|`, which is *exactly* XOR for a pure black or white
/// source — and black and white are what the idiom uses, because the point of drawing in XOR is to
/// invert. Anything in between only approximates.
///
/// The layer is what makes it correct: the blend has to combine with the scene already painted,
/// not with this shape's own backdrop.
class XorShape extends Shape {
  XorShape(this.children);

  final List<Shape> children;

  @override
  bool get holdsImages => children.any((s) => s.holdsImages);

  @override
  void draw(ui.Canvas c) {
    c.saveLayer(null, Paint()..blendMode = BlendMode.difference);
    drawShapeRun(c, children);
    c.restore();
  }

  // No paintedBounds and no opaqueCoverage on purpose: XOR feedback never hides what is under it,
  // so a cycle carrying it must not be treated as covering an earlier one.

  @override
  String toString() => 'Xor [${children.length} shapes]';

  @override
  String describe() => 'Xor [${_dn(children)}]';
}

/// Lays a pattern's tile out at the image's size in GC coordinates. The decoded pixels may be denser
/// than that - an image a GC drew is rendered at the device pixel ratio - and would tile too large.
Float64List tileMatrix(VImage vImage, ui.Image image) {
  final sx = (vImage.width ?? image.width) / image.width;
  final sy = (vImage.height ?? image.height) / image.height;
  return Matrix4.diagonal3Values(sx, sy, 1).storage;
}

/// What a GC asks of every Paint it draws with: anti-aliasing (`setAntialias`) and image filtering
/// (`setInterpolation`). Held for the duration of a [RenderHintsShape]'s draw, so the shapes read it
/// without carrying it; outside one the defaults apply.
class _RenderHints {
  static bool antiAlias = true;
  static FilterQuality? filterQuality;
}

Paint _paint() => Paint()..isAntiAlias = _RenderHints.antiAlias;

/// Ops drawn under a non-default anti-aliasing or interpolation setting.
class RenderHintsShape extends Shape {
  RenderHintsShape(this.antiAlias, this.filterQuality, this.children);

  final bool antiAlias;
  final FilterQuality? filterQuality;
  final List<Shape> children;

  @override
  void draw(ui.Canvas c) {
    final antiAliasBefore = _RenderHints.antiAlias;
    final filterBefore = _RenderHints.filterQuality;
    _RenderHints.antiAlias = antiAlias;
    _RenderHints.filterQuality = filterQuality;
    try {
      drawShapeRun(c, children);
    } finally {
      _RenderHints.antiAlias = antiAliasBefore;
      _RenderHints.filterQuality = filterBefore;
    }
  }

  @override
  String toString() => 'Hints(aa: $antiAlias, filter: $filterQuality) [${children.length} shapes]';

  @override
  String describe() => 'Hints aa=$antiAlias filter=$filterQuality [${_dn(children)}]';
}

/// A stroke drawn with the GC's foreground pattern: the stroke's coverage, painted with the pattern.
class PatternStrokeShape extends Shape {
  PatternStrokeShape(this.shader, this.alpha, this.children, {this.image});

  final ui.Shader shader;
  final int alpha;
  final List<Shape> children;

  /// The tile of an image pattern, released with the shape.
  ui.Image? image;

  @override
  void draw(ui.Canvas c) {
    if (image == null && shader is ui.ImageShader) return;
    c.saveLayer(null, Paint());
    for (final s in children) {
      s.draw(c);
    }
    c.drawPaint(Paint()
      ..shader = shader
      ..color = Color.fromARGB(alpha, 0, 0, 0)
      ..blendMode = BlendMode.srcIn);
    c.restore();
  }

  @override
  String toString() => 'PatternStroke [${children.length} shapes]';

  /// A shader does not describe itself: its kind and, for an image pattern, the tile's size.
  @override
  String describe() => 'PatternStroke ${shader.runtimeType}'
      '${image == null ? '' : ' tile=${image!.width}x${image!.height}'} alpha=$alpha [${_dn(children)}]';
}

/// Drawing confined to the coverage of [mask]: a clip whose shape is text, which Flutter cannot
/// turn into a path. The mask is drawn in a layer of its own so its parts add up before they cut.
class MaskShape extends Shape {
  MaskShape(this.mask, this.children);

  final List<Shape> mask;
  final List<Shape> children;

  @override
  void draw(ui.Canvas c) {
    c.saveLayer(null, Paint());
    for (final s in children) {
      s.draw(c);
    }
    c.saveLayer(null, Paint()..blendMode = BlendMode.dstIn);
    for (final s in mask) {
      s.draw(c);
    }
    c.restore();
    c.restore();
  }

  @override
  String toString() => 'Mask [${children.length} shapes]';

  @override
  String describe() => 'Mask [${_dn(mask)}] over [${_dn(children)}]';
}

/// Drawing confined to a clip that is not a rectangle — a GC clipped to a Path or a Region.
///
/// A container rather than a field on every shape: the clip has to be applied around the ops, and
/// a Region's several rectangles or a Path's curves have no place in the single clipRect a shape
/// carries.
class ClipPathShape extends Shape {
  ClipPathShape(this.path, this.children);

  final Path path;
  final List<Shape> children;

  @override
  void draw(ui.Canvas c) {
    c.save();
    c.clipPath(path);
    for (final s in children) {
      s.draw(c);
    }
    c.restore();
  }

  @override
  String toString() => 'ClipPath [${children.length} shapes]';

  @override
  String describe() => 'ClipPath [${_dn(children)}]';
}

/// Strokes where SWT does: an odd-width or hairline stroke sits on pixel centres, so a 1-pixel
/// line covers one row of pixels instead of half of two.
/// The half-pixel shift [_strokeAligned] applies, for a shape that moves its own geometry by it
/// instead: a colour-only paint draws the same either way, without three more canvas calls.
Offset _strokeShift(double strokeWidth, bool stroked) {
  final width = strokeWidth.round();
  return !stroked || width.isEven && width != 0 ? Offset.zero : const Offset(0.5, 0.5);
}

/// [r] moved by [_strokeShift]: [r] itself when that is no move, which is every fill.
Rect _alignedRect(Rect r, double strokeWidth, bool stroked) {
  final shift = _strokeShift(strokeWidth, stroked);
  return identical(shift, Offset.zero) ? r : r.shift(shift);
}

void _strokeAligned(ui.Canvas c, double strokeWidth, bool stroked, void Function() draw) {
  final width = strokeWidth.round();
  if (!stroked || width.isEven && width != 0) {
    draw();
    return;
  }
  c.save();
  c.translate(0.5, 0.5);
  draw();
  c.restore();
}

class TransformShape extends Shape {
  TransformShape(this.matrix, this.children, [this.clipRect]);
  final Float64List matrix;
  final List<Shape> children;

  @override
  bool get holdsImages => children.any((s) => s.holdsImages);

  @override
  final Rect? clipRect;

  @override
  void draw(ui.Canvas c) {
    c.save();
    // Clip in device space BEFORE rotating so the rect coords are correct.
    if (clipRect != null) c.clipRect(clipRect!);
    c.transform(matrix);
    drawShapeRun(c, children);
    c.restore();
  }

  @override
  String toString() => 'Transform [${children.length} shapes]';

  @override
  String describe() => 'Transform ${matrix.map((v) => v.toStringAsFixed(3)).join(",")} [${_dn(children)}]${_dk(clipRect)}';
}

class TextShape extends Shape {
  TextShape(this.text, this.off, this.style, [this.clipRect]);
  final String text;
  final Offset off;
  final TextStyle style;
  @override
  final Rect? clipRect;

  // Looked up rather than held: the shared cache disposes what it evicts.
  TextPainter get painter => _TextLayouts.of(text, style);

  TextShape translated(Offset offset, Rect clipArea) =>
      TextShape(text, off + offset, style, clipArea);

  @override
  bool get clipsBody => true;

  @override
  void draw(ui.Canvas c) => _drawClipped(c, this);

  @override
  void paintBody(ui.Canvas c) {
    painter.paint(c, off);
  }

  @override
  String toString() => 'Text "$text" @ $off${clipRect != null ? " [clipped]" : ""}';

  @override
  String describe() => 'Text "$text" @ ${_do(off)} '
      // The laid-out extent, so a reader of these ops knows which pixels this text owns without
      // measuring the font itself. Comes from the same cached layout draw() uses.
      'size=${painter.width.round()}x${painter.height.round()} '
      'font=${style.fontFamily}/${style.fontSize}/${style.fontWeight?.value}/${style.fontStyle?.name} '
      'color=${style.color == null ? "-" : _dc(style.color!)}${_dk(clipRect)}';
}

/// Laid-out text for [TextShape], kept by second chance; a system font change clears it.
///
/// A hit only marks its entry, so the text drawn every frame costs one lookup. When the cache is
/// full, the oldest entry goes unless it was used since it last came round, in which case it moves
/// to the back and loses its mark.
class _TextLayouts {
  static final Map<_LayoutKey, _Layout> _layouts = {};
  static const int _capacity = 512;
  static bool _watchingFonts = false;

  static TextPainter of(String text, TextStyle style) {
    if (!_watchingFonts) _watchingFonts = FontUtils.listenToSystemFonts(_clear);
    final key = _LayoutKey(text, style);
    final held = _layouts[key];
    if (held != null) {
      held.used = true;
      return held.painter;
    }
    final painter = TextPainter(
      text: TextSpan(text: text, style: style),
      textDirection: TextDirection.ltr,
    )..layout();
    _layouts[key] = _Layout(painter);
    // Evicted, not disposed: a picture recorded with it may be rasterised later -- an Image's
    // render is read back only when asked for -- and on the web a disposed paragraph draws as
    // garbage there. The engine frees it once nothing references it.
    while (_layouts.length > _capacity) {
      final oldest = _layouts.keys.first;
      final layout = _layouts.remove(oldest)!;
      if (layout.used) {
        layout.used = false;
        _layouts[oldest] = layout;
      }
    }
    return painter;
  }

  static void _clear() => _layouts.clear();
}

class _Layout {
  _Layout(this.painter);
  final TextPainter painter;
  bool used = false;
}

/// A text and its style, hashed once: a style's own hash goes over every field it has.
final class _LayoutKey {
  _LayoutKey(this.text, this.style) : hashCode = Object.hash(text, _styleHashes[style] ??= style.hashCode);

  static final Expando<int> _styleHashes = Expando();

  final String text;
  final TextStyle style;
  @override
  final int hashCode;

  @override
  bool operator ==(Object other) =>
      other is _LayoutKey && other.hashCode == hashCode && other.text == text &&
      (identical(other.style, style) || other.style == style);
}

class LineShape extends Shape implements _Piece {
  /// What every line draws with: a canvas call takes what a paint holds when it is made, and a line
  /// sets everything it draws with, so one paint serves them all instead of one per line.
  static final Paint _linePaint = Paint();

  /// The ends as drawn, moved by [_strokeShift] once rather than per draw.
  late final Offset _from = _aligned(p1), _to = _aligned(p2);

  Offset _aligned(Offset p) {
    final shift = _strokeShift(strokeWidth, true);
    return identical(shift, Offset.zero) ? p : p + shift;
  }

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
  bool get clipsBody => true;

  @override
  void draw(ui.Canvas c) => _drawClipped(c, this);

  @override
  void paintBody(ui.Canvas c) {
    c.drawLine(_from, _to, _linePaint
      ..isAntiAlias = _RenderHints.antiAlias
      ..color = color
      ..strokeWidth = strokeWidth
      ..strokeCap = getStrokeCap(lineCap)
      ..strokeJoin = getStrokeJoin(lineJoin));
  }

  // As a piece of a triangle run: one straight segment with a flat or square cap.
  @override
  int get vertices => _TriangleList.verticesFor(4);
  @override
  int get indices => _TriangleList.indicesFor(4);
  @override
  void addTo(_TriangleList list) =>
      list.addSegment(p1.dx, p1.dy, p2.dx, p2.dy, strokeWidth, lineCap, color);

  @override
  String toString() => 'Line $p1 → $p2${clipRect != null ? " [clipped]" : ""}';

  @override
  String describe() => 'Line ${_do(p1)}->${_do(p2)} ${_dc(color)} w=$strokeWidth cap=$lineCap join=$lineJoin${_dk(clipRect)}';
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
  bool get clipsBody => true;

  @override
  void draw(ui.Canvas c) => _drawClipped(c, this);

  @override
  void paintBody(ui.Canvas c) {
    c.drawOval(_alignedRect(rect, strokeWidth, !isFilled), _paint()
      ..color = color
      ..style = isFilled ? PaintingStyle.fill : PaintingStyle.stroke
      ..strokeWidth = strokeWidth);
  }

  @override
  String toString() => '${isFilled ? "Fill" : ""}Oval $rect';

  @override
  String describe() => '${isFilled ? "Fill" : "Draw"}Oval ${_dr(rect)} ${_dc(color)} w=$strokeWidth${_dk(clipRect)}';
}

abstract interface class GCGround {
  VColor? get swtGround;

  Color? get renderedGround;
}

class RectShape extends Shape {
  /// What every rectangle draws with, as [LineShape] has one: a rectangle sets its style, and its
  /// stroke when it strokes, so nothing another one left behind shows.
  static final Paint _rectPaint = Paint();

  /// [rect] as drawn: moved by [_strokeShift] once rather than per draw.
  late final Rect _drawn = _alignedRect(rect, strokeWidth, !isFilled);

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

  // A hairline still covers a pixel, on the centres past the rectangle's right and bottom edges.
  @override
  Rect? get paintedBounds => isFilled ? rect : rect.inflate(math.max(strokeWidth, 1));

  @override
  Rect? get opaqueCoverage =>
      isFilled && color.alpha == 0xFF && (clipRect == null || _containsRect(clipRect!, rect))
          ? rect
          : null;

  @override
  bool get clipsBody => true;

  @override
  void draw(ui.Canvas c) => _drawClipped(c, this);

  @override
  void paintBody(ui.Canvas c) {
    final paint = _rectPaint
      ..isAntiAlias = _RenderHints.antiAlias
      ..color = color;
    if (isFilled) {
      paint.style = PaintingStyle.fill;
    } else {
      paint
        ..style = PaintingStyle.stroke
        ..strokeWidth = strokeWidth
        ..strokeCap = getStrokeCap(lineCap)
        ..strokeJoin = getStrokeJoin(lineJoin);
    }
    c.drawRect(_drawn, paint);
  }

  @override
  String toString() => '${isFilled ? "Fill" : ""}Rect $rect';

  @override
  String describe() => '${isFilled ? "Fill" : "Draw"}Rect ${_dr(rect)} ${_dc(color)} w=$strokeWidth cap=$lineCap join=$lineJoin${_dk(clipRect)}';
}

class GradientRectShape extends Shape {
  GradientRectShape(this.rect, this.fromColor, this.toColor, bool vertical,
      [this.clipRect])
      : begin = vertical ? Alignment.topCenter : Alignment.centerLeft,
        end = vertical ? Alignment.bottomCenter : Alignment.centerRight;

  GradientRectShape.aligned(this.rect, this.fromColor, this.toColor,
      this.begin, this.end, [this.clipRect]);

  final Rect rect;
  final Color fromColor;
  final Color toColor;
  final Alignment begin;
  final Alignment end;
  @override
  final Rect? clipRect;

  @override
  bool get clipsBody => true;

  @override
  void draw(ui.Canvas c) => _drawClipped(c, this);

  @override
  void paintBody(ui.Canvas c) {
    final gradient = LinearGradient(
      begin: begin,
      end: end,
      colors: [fromColor, toColor],
    );
    c.drawRect(rect, _paint()
      ..shader = gradient.createShader(rect)
      ..style = PaintingStyle.fill);
  }

  @override
  String toString() => 'GradientRect $rect';

  @override
  String describe() => 'GradientRect ${_dr(rect)} ${_dc(fromColor)}->${_dc(toColor)} $begin->$end${_dk(clipRect)}';
}

/// A fill painted with a GC pattern. The shader is laid out in GC coordinates, as the native
/// backends lay out theirs, so the pattern does not move with the shape it fills.
class PatternFillShape extends Shape {
  @override
  String describe() => 'PatternFill ${_dr(path.getBounds())} @ ${_do(offset)} '
      'alpha=$alpha${_dk(clipRect)}';

  PatternFillShape(this.path, this.shader, this.alpha, this.clipRect,
      {this.image, this.offset = Offset.zero});

  @override
  bool get holdsImages => image != null;

  PatternFillShape.image(Path path, ui.Image image, int alpha, Rect? clipRect, Float64List matrix)
      : this(path, ui.ImageShader(image, TileMode.repeated, TileMode.repeated, matrix),
            alpha, clipRect, image: image);

  final Path path;
  final ui.Shader shader;
  final int alpha;
  @override
  final Rect? clipRect;

  /// The tile of an image pattern, released with the shape.
  ui.Image? image;

  /// A copyArea shift, applied to the pattern along with the path.
  final Offset offset;

  PatternFillShape translated(Offset by, Rect clipArea) =>
      PatternFillShape(path, shader, alpha, clipArea, image: image, offset: offset + by);

  @override
  Rect? get paintedBounds => path.getBounds().shift(offset);

  @override
  void draw(ui.Canvas c) {
    // Released with its commit cycle: the tile it would sample is gone.
    if (image == null && shader is ui.ImageShader) return;
    c.save();
    if (clipRect != null) c.clipRect(clipRect!);
    c.translate(offset.dx, offset.dy);
    c.drawPath(path, _paint()
      ..shader = shader
      ..color = Color.fromARGB(alpha, 0, 0, 0));
    c.restore();
  }

  @override
  String toString() => 'PatternFill ${path.getBounds()}';
}

/// Separate straight segments stroked with one paint: [points] holds their endpoint pairs.
class SegmentsShape extends Shape {
  SegmentsShape(this.points, this.color, this.strokeWidth, this.lineCap, [this.clipRect]);
  final Float32List points;
  final Color color;
  final double strokeWidth;
  final int lineCap;
  @override
  final Rect? clipRect;

  @override
  bool get clipsBody => true;

  @override
  void draw(ui.Canvas c) => _drawClipped(c, this);

  @override
  void paintBody(ui.Canvas c) {
    if (points.isEmpty) return;
    if (lineCap != SWT.CAP_ROUND) {
      final px = _devicePixel(c);
      final chunk = _TriangleList.maxSegments * 4;
      for (var from = 0; from < points.length; from += chunk) {
        final part = Float32List.sublistView(points, from, math.min(points.length, from + chunk));
        final segments = part.length ~/ 4;
        c.drawVertices(
            (_TriangleList(segments * _TriangleList.verticesFor(4), segments * _TriangleList.indicesFor(4), px)
                  ..addSegments(part, strokeWidth, lineCap, color))
                .build(),
            BlendMode.dst, _runPaint);
      }
      return;
    }
    _strokeAligned(c, strokeWidth, true, () => c.drawRawPoints(ui.PointMode.lines, points, _paint()
      ..color = color
      ..strokeWidth = strokeWidth
      ..strokeCap = getStrokeCap(lineCap)));
  }

  @override
  String toString() => 'Segments ${points.length ~/ 4}${clipRect != null ? " [clipped]" : ""}';

  @override
  String describe() {
    final ends = [
      for (var i = 0; i + 3 < points.length; i += 4)
        '${_do(Offset(points[i], points[i + 1]))}->${_do(Offset(points[i + 2], points[i + 3]))}'
    ];
    return 'Segments ${ends.join(' ')} ${_dc(color)} w=$strokeWidth cap=$lineCap${_dk(clipRect)}';
  }
}

/// Triangles that carry their own antialiasing, for straight segments and convex fills: each shape
/// is a core at full coverage inside a ring that fades to none across one device pixel. A GPU
/// renderer draws them as they are, where a stroke or a path is tessellated or drawn one call at a
/// time, and without multisampling plain triangles would lose the edges a thin dot is made of.
/// Each shape carries its own colour, so shapes of any paint share one list.
class _TriangleList {
  /// A list of [vertices] and [indices], written into buffers kept for every list: one is drawn
  /// before the next is made, and the canvas takes what it draws when it is drawn.
  _TriangleList(int vertices, int indices, this.px)
      : positions = _positionsBuffer.length >= vertices * 2
            ? _positionsBuffer
            : (_positionsBuffer = Float32List(vertices * 2)),
        colors = _colorsBuffer.length >= vertices ? _colorsBuffer : (_colorsBuffer = Int32List(vertices)),
        indices = strip
            ? null
            : _indicesBuffer.length >= indices
                ? _indicesBuffer
                : (_indicesBuffer = Uint16List(indices));

  static Float32List _positionsBuffer = Float32List(0);
  static Int32List _colorsBuffer = Int32List(0);
  static Uint16List _indicesBuffer = Uint16List(0);

  /// Whether lists go as one strip with no indices rather than indexed triangles. The CanvasKit
  /// build checks every index of an indexed list in Dart before drawing it, which costs more than
  /// the strip's extra vertices; elsewhere the indexed list is the cheaper one to draw.
  static const bool strip = kIsWeb && !kIsWasm;

  /// Vertices a shape of [corners] corners takes. As a strip: its ring, its core, and the repeated
  /// vertices that join it to its neighbours with triangles of no area.
  static int verticesFor(int corners) => strip ? 3 * corners + 4 : 2 * corners;

  /// Indices a shape of [corners] corners takes: a fan over its core and two triangles per side of
  /// its ring. None as a strip.
  static int indicesFor(int corners) => strip ? 0 : (corners - 2) * 3 + corners * 6;

  /// Vertices per list. Under Skwasm (the wasm build) a list is copied into scratch memory of
  /// limited size and fails beyond it, so there a long run goes as several lists of about 20 KB.
  /// Indexed elsewhere, a list holds as many as 16-bit indices reach; a strip is bounded only to cap
  /// one list's memory, since every split costs a call.
  static const int maxVertices = kIsWasm ? 1024 : (strip ? 1 << 20 : 0xFFFF);

  /// Segments per list.
  static final int maxSegments = maxVertices ~/ verticesFor(4);

  /// One device pixel.
  final double px;
  final Float32List positions;
  final Int32List colors;
  final Uint16List? indices;
  int _v = 0, _n = 0;

  /// One shape: its [inner] outline of [n] corners at full coverage [core], and a ring out to its
  /// [outer] outline fading to [edge].
  void _shape(Float32List inner, Float32List outer, int n, int core, int edge) {
    if (strip) {
      _stripShape(inner, outer, n, core, edge);
      return;
    }
    final base = _v;
    final positions = this.positions, colors = this.colors;
    for (var k = 0; k < n; k++) {
      positions[(base + k) * 2] = inner[k * 2];
      positions[(base + k) * 2 + 1] = inner[k * 2 + 1];
      colors[base + k] = core;
      positions[(base + n + k) * 2] = outer[k * 2];
      positions[(base + n + k) * 2 + 1] = outer[k * 2 + 1];
      colors[base + n + k] = edge;
    }
    _v = base + 2 * n;
    final indices = this.indices!;
    for (var k = 1; k + 1 < n; k++) {
      indices[_n++] = base;
      indices[_n++] = base + k;
      indices[_n++] = base + k + 1;
    }
    for (var k = 0; k < n; k++) {
      final q = k + 1 == n ? 0 : k + 1;
      indices[_n++] = base + k;
      indices[_n++] = base + n + k;
      indices[_n++] = base + n + q;
      indices[_n++] = base + k;
      indices[_n++] = base + n + q;
      indices[_n++] = base + q;
    }
  }

  /// [_shape] as a run of the strip: from its first outer corner (repeated, to start a new run)
  /// round the ring, then across the core zigzagging between its two ends, ending on a repeated
  /// vertex. Shapes follow one another without overlapping, since the triangles between them have
  /// no area.
  void _stripShape(Float32List inner, Float32List outer, int n, int core, int edge) {
    final positions = this.positions, colors = this.colors;
    var v = _v;
    positions[v * 2] = outer[0];
    positions[v * 2 + 1] = outer[1];
    colors[v++] = edge;
    for (var k = 0; k <= n; k++) {
      final q = k == n ? 0 : k;
      positions[v * 2] = outer[q * 2];
      positions[v * 2 + 1] = outer[q * 2 + 1];
      colors[v++] = edge;
      positions[v * 2] = inner[q * 2];
      positions[v * 2 + 1] = inner[q * 2 + 1];
      colors[v++] = core;
    }
    positions[v * 2] = inner[0];
    positions[v * 2 + 1] = inner[1];
    colors[v++] = core;
    for (var lo = 1, hi = n - 1; lo <= hi; lo++, hi--) {
      positions[v * 2] = inner[lo * 2];
      positions[v * 2 + 1] = inner[lo * 2 + 1];
      colors[v++] = core;
      if (lo != hi) {
        positions[v * 2] = inner[hi * 2];
        positions[v * 2 + 1] = inner[hi * 2 + 1];
        colors[v++] = core;
      }
    }
    positions[v * 2] = positions[(v - 1) * 2];
    positions[v * 2 + 1] = positions[(v - 1) * 2 + 1];
    colors[v] = colors[v - 1];
    _v = v + 1;
  }


  final Float32List _inner = Float32List(8), _outer = Float32List(8);

  /// Adds the endpoint pairs in [points], stroked [strokeWidth] wide in [color].
  void addSegments(Float32List points, double strokeWidth, int lineCap, Color color) {
    for (var i = 0; i + 3 < points.length; i += 4) {
      addSegment(points[i], points[i + 1], points[i + 2], points[i + 3], strokeWidth, lineCap, color);
    }
  }

  /// Adds the segment from (x1, y1) to (x2, y2), stroked [strokeWidth] wide in [color]. A hairline
  /// is one device pixel; an odd width is shifted half a pixel onto the pixel grid, as a stroke is.
  void addSegment(double x1, double y1, double x2, double y2, double strokeWidth, int lineCap, Color color) {
    final segment = math.sqrt((x2 - x1) * (x2 - x1) + (y2 - y1) * (y2 - y1));
    if (segment == 0) return;
    final rgb = color.toARGB32() & 0xFFFFFF;
    final f = _RenderHints.antiAlias ? px / 2 : 0.0;
    final half = (strokeWidth > 0 ? strokeWidth : px) / 2;
    final cap = lineCap == SWT.CAP_SQUARE ? half : 0.0;
    final width = strokeWidth.round();
    final shift = width.isEven && width != 0 ? 0.0 : 0.5;
    final hi = math.max(half - f, 0.0), ho = half + f;
    final ux = (x2 - x1) / segment, uy = (y2 - y1) / segment;
    final along = segment / 2 + cap;
    // Inset half a pixel for the core and outset half for the ring; a dash thinner or shorter
    // than a pixel keeps an empty core, and its coverage goes into the core's alpha.
    final li = math.max(along - f, 0.0), lo = along + f;
    // The linear ramp lays down a frustum of coverage: scale it to the dash's true area.
    final innerArea = 4 * hi * li, outerArea = 4 * ho * lo;
    final laid = (innerArea + outerArea + math.sqrt(innerArea * outerArea)) / 3;
    final coverage = laid > 0 ? math.min(1.0, 4 * half * along / laid) : 1.0;
    final core = ((color.a * coverage * 255).round() << 24) | rgb;
    final mx = (x1 + x2) / 2 + shift, my = (y1 + y2) / 2 + shift;
    final iax = ux * li, iay = uy * li, ibx = -uy * hi, iby = ux * hi;
    final oax = ux * lo, oay = uy * lo, obx = -uy * ho, oby = ux * ho;
    final inner = _inner, outer = _outer;
    // Corners go round the rectangle (-l,-h), (l,-h), (l,h), (-l,h) in the segment's own frame.
    inner[0] = mx - iax - ibx;
    inner[1] = my - iay - iby;
    inner[2] = mx + iax - ibx;
    inner[3] = my + iay - iby;
    inner[4] = mx + iax + ibx;
    inner[5] = my + iay + iby;
    inner[6] = mx - iax + ibx;
    inner[7] = my - iay + iby;
    outer[0] = mx - oax - obx;
    outer[1] = my - oay - oby;
    outer[2] = mx + oax - obx;
    outer[3] = my + oay - oby;
    outer[4] = mx + oax + obx;
    outer[5] = my + oay + oby;
    outer[6] = mx - oax + obx;
    outer[7] = my - oay + oby;
    _shape(inner, outer, 4, core, rgb);
  }

  /// Adds a convex fill: its [inner] outline at full coverage, and a ring out to its [outer] one
  /// fading to none.
  void addPolygon(Float32List inner, Float32List outer, Color color) {
    final rgb = color.toARGB32() & 0xFFFFFF;
    _shape(inner, outer, inner.length ~/ 2, ((color.a * 255).round() << 24) | rgb, rgb);
  }

  ui.Vertices build() => strip
      ? ui.Vertices.raw(ui.VertexMode.triangleStrip, Float32List.sublistView(positions, 0, _v * 2),
          colors: Int32List.sublistView(colors, 0, _v))
      : ui.Vertices.raw(ui.VertexMode.triangles, Float32List.sublistView(positions, 0, _v * 2),
          colors: Int32List.sublistView(colors, 0, _v), indices: Uint16List.sublistView(indices!, 0, _n));
}

class PathShape extends Shape {
  PathShape(this.path, this.color, this.strokeWidth, this.lineCap, this.lineJoin,
      {this.isFilled = false, this.clipRect});
  final Path path;
  final Color color;
  final double strokeWidth;
  final int lineCap;
  final int lineJoin;
  final bool isFilled;
  @override
  final Rect? clipRect;

  @override
  bool get clipsBody => true;

  @override
  void draw(ui.Canvas c) => _drawClipped(c, this);

  @override
  void paintBody(ui.Canvas c) {
    _strokeAligned(c, strokeWidth, !isFilled, () => c.drawPath(path, _paint()
      ..color = color
      ..style = isFilled ? PaintingStyle.fill : PaintingStyle.stroke
      ..strokeWidth = strokeWidth
      ..strokeCap = getStrokeCap(lineCap)
      ..strokeJoin = getStrokeJoin(lineJoin)));
  }

  @override
  String toString() => '${isFilled ? "Fill" : "Draw"}Path';

  @override
  String describe() => '${isFilled ? "Fill" : "Draw"}Path ${_dr(path.getBounds())} ${_dc(color)} w=$strokeWidth${_dk(clipRect)}';
}

class PolygonShape extends Shape {
  PolygonShape(this.points, this.color, this.strokeWidth, this.lineCap,
      this.lineJoin,
      {this.isFilled = false,
      this.fillType = PathFillType.evenOdd,
      this.clipRect});
  final List<int> points;
  final Color color;
  final double strokeWidth;
  final int lineCap;
  final int lineJoin;
  final bool isFilled;
  final PathFillType fillType;
  @override
  final Rect? clipRect;

  @override
  bool get clipsBody => true;

  @override
  void draw(ui.Canvas c) => _drawClipped(c, this);

  @override
  void paintBody(ui.Canvas c) {
    if (points.length < (isFilled ? 6 : 4)) return;
    final path = Path()
      ..fillType = fillType
      ..moveTo(points[0].toDouble(), points[1].toDouble());
    for (int i = 2; i < points.length; i += 2) {
      path.lineTo(points[i].toDouble(), points[i + 1].toDouble());
    }
    path.close();
    _strokeAligned(c, strokeWidth, !isFilled, () => c.drawPath(path, _paint()
      ..color = color
      ..style = isFilled ? PaintingStyle.fill : PaintingStyle.stroke
      ..strokeWidth = strokeWidth
      ..strokeCap = getStrokeCap(lineCap)
      ..strokeJoin = getStrokeJoin(lineJoin)));
  }

  @override
  String toString() => '${isFilled ? "Fill" : ""}Polygon ${points.length ~/ 2} pts';

  @override
  String describe() => '${isFilled ? "Fill" : "Draw"}Polygon ${points.join(",")} ${_dc(color)} w=$strokeWidth${_dk(clipRect)}';
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
  bool get clipsBody => true;

  @override
  void draw(ui.Canvas c) => _drawClipped(c, this);

  @override
  void paintBody(ui.Canvas c) {
    if (points.length < 4) return;
    final path = Path()
      ..moveTo(points[0].toDouble(), points[1].toDouble());
    for (int i = 2; i < points.length; i += 2) {
      path.lineTo(points[i].toDouble(), points[i + 1].toDouble());
    }
    _strokeAligned(c, strokeWidth, !isFilled, () => c.drawPath(path, _paint()
      ..color = color
      ..style = isFilled ? PaintingStyle.fill : PaintingStyle.stroke
      ..strokeWidth = strokeWidth
      ..strokeCap = getStrokeCap(lineCap)
      ..strokeJoin = getStrokeJoin(lineJoin)));
  }

  @override
  String toString() => 'Polyline ${points.length ~/ 2} pts';

  @override
  String describe() => 'Polyline ${points.join(",")} ${_dc(color)} w=$strokeWidth${_dk(clipRect)}';
}

/// The pie fillArc covers. A sweep of a full turn is the whole oval: arcTo draws nothing for it.
Path piePath(Rect rect, double startAngle, double sweepAngle) {
  if (sweepAngle.abs() >= 2 * math.pi) return Path()..addOval(rect);
  return Path()
    ..moveTo(rect.center.dx, rect.center.dy)
    ..arcTo(rect, startAngle, sweepAngle, false)
    ..close();
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
  bool get clipsBody => true;

  @override
  void draw(ui.Canvas c) => _drawClipped(c, this);

  @override
  void paintBody(ui.Canvas c) {
    final paint = _paint()
      ..color = color
      ..style = isFilled ? PaintingStyle.fill : PaintingStyle.stroke;
    if (!isFilled) {
      paint
        ..strokeWidth = strokeWidth
        ..strokeCap = getStrokeCap(lineCap)
        ..strokeJoin = getStrokeJoin(lineJoin);
    }
    if (isFilled) {
      final path = piePath(rect, startAngle, sweepAngle);
      _strokeAligned(c, strokeWidth, !isFilled, () => c.drawPath(path, paint));
    } else {
      c.drawArc(_alignedRect(rect, strokeWidth, true), startAngle, sweepAngle, false, paint);
    }
  }

  @override
  String toString() => '${isFilled ? "Fill" : "Draw"}Arc $rect';

  @override
  String describe() => '${isFilled ? "Fill" : "Draw"}Arc ${_dr(rect)} ${startAngle.toStringAsFixed(4)}+${sweepAngle.toStringAsFixed(4)} ${_dc(color)} w=$strokeWidth${_dk(clipRect)}';
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
  bool get clipsBody => true;

  @override
  void draw(ui.Canvas c) => _drawClipped(c, this);

  @override
  void paintBody(ui.Canvas c) {
    final rrect = RRect.fromRectAndCorners(rect,
        topLeft: Radius.elliptical(radiusX, radiusY),
        topRight: Radius.elliptical(radiusX, radiusY),
        bottomLeft: Radius.elliptical(radiusX, radiusY),
        bottomRight: Radius.elliptical(radiusX, radiusY));
    final shift = _strokeShift(strokeWidth, !isFilled);
    c.drawRRect(identical(shift, Offset.zero) ? rrect : rrect.shift(shift), _paint()
      ..color = color
      ..style = isFilled ? PaintingStyle.fill : PaintingStyle.stroke
      ..strokeWidth = strokeWidth
      ..strokeCap = getStrokeCap(lineCap)
      ..strokeJoin = getStrokeJoin(lineJoin));
  }

  @override
  String toString() => '${isFilled ? "Fill" : ""}RoundRect $rect';

  @override
  String describe() => '${isFilled ? "Fill" : "Draw"}RoundRect ${_dr(rect)} r=$radiusX,$radiusY ${_dc(color)} w=$strokeWidth${_dk(clipRect)}';
}

class PointShape extends Shape {
  PointShape(this.point, this.color, [this.clipRect]);
  final Offset point;
  final Color color;
  @override
  final Rect? clipRect;

  @override
  bool get clipsBody => true;

  @override
  void draw(ui.Canvas c) => _drawClipped(c, this);

  @override
  void paintBody(ui.Canvas c) {
    c.drawRect(Rect.fromLTWH(point.dx, point.dy, 1, 1),
        _paint()..color = color..style = PaintingStyle.fill);
  }

  @override
  String toString() => 'Point $point';

  @override
  String describe() => 'Point ${_do(point)} ${_dc(color)}${_dk(clipRect)}';
}

class FocusRectShape extends Shape {
  FocusRectShape(this.rect, this.color, [this.clipRect]);
  final Rect rect;
  final Color color;
  @override
  final Rect? clipRect;

  @override
  bool get clipsBody => true;

  @override
  void draw(ui.Canvas c) => _drawClipped(c, this);

  @override
  void paintBody(ui.Canvas c) {
    final paint = _paint()..color = color..style = PaintingStyle.stroke..strokeWidth = 1;
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

  @override
  String describe() => 'FocusRect ${_dr(rect)} ${_dc(color)}${_dk(clipRect)}';
}

enum ImageType { raster, svg, picture }

class ImageShape extends Shape {
  @override
  bool get holdsImages => type == ImageType.raster && image != null;

  ImageShape._({
    required this.type,
    required this.destRect,
    this.image,
    this.srcRect,
    this.pictureInfo,
    this.clipRect,
    this.colorFilter,
    this.alpha = 255,
    this.opaqueSource = false,
    this.inkMask,
    this.remotePicture,
    this.sourceScale = 1,
  });

  /// Units of [remotePicture] per unit of [srcRect] and [destRect]: the render of an Image held
  /// denser than its size is in its buffer's pixels, while a copy of it is placed in the Image's
  /// units.
  final double sourceScale;

  /// Masks a tinted glyph by its darkness, so a light fill it carries is not flooded with the tint.
  final ColorFilter? inkMask;

  /// Set when this draws a render that never had to become pixels.
  final ui.Picture? remotePicture;

  /// Whether every pixel of [image] is known opaque. Only set for a picture this side rendered
  /// itself, which always starts from an opaque fill — an application bitmap may have alpha and
  /// nothing here can tell cheaply.
  final bool opaqueSource;

  factory ImageShape.raster(ui.Image image, Rect srcRect, Rect destRect,
      {Rect? clipRect, ColorFilter? colorFilter, int alpha = 255, bool opaqueSource = false,
      ColorFilter? inkMask}) {
    return ImageShape._(
        type: ImageType.raster, image: image, srcRect: srcRect,
        destRect: destRect, clipRect: clipRect, colorFilter: colorFilter,
        alpha: alpha, opaqueSource: opaqueSource, inkMask: inkMask);
  }

  @override
  Rect? get paintedBounds => destRect;

  @override
  Rect? get opaqueCoverage => type == ImageType.raster &&
          opaqueSource &&
          alpha == 255 &&
          colorFilter == null &&
          (clipRect == null || _containsRect(clipRect!, destRect))
      ? destRect
      : null;

  factory ImageShape.picture(ui.Picture picture, Rect srcRect, Rect destRect,
      {Rect? clipRect, int alpha = 255, double sourceScale = 1}) {
    return ImageShape._(
        type: ImageType.picture, remotePicture: picture, srcRect: srcRect,
        destRect: destRect, clipRect: clipRect, alpha: alpha, opaqueSource: true,
        sourceScale: sourceScale);
  }

  factory ImageShape.svg(PictureInfo pictureInfo, Rect destRect,
      {Rect? clipRect, ColorFilter? colorFilter, int alpha = 255}) {
    return ImageShape._(
        type: ImageType.svg, pictureInfo: pictureInfo,
        destRect: destRect, clipRect: clipRect, colorFilter: colorFilter,
        alpha: alpha);
  }

  // Whether a blitted image reads as a glyph, by pixels — nothing here matches an image by name.
  // A null verdict means the image is not a glyph.
  static final Map<String, GlyphTone?> _monochromeGlyphs = {};

  // Keyed on the whole payload, and on the limits that produced the verdict: two same-sized PNGs share their leading bytes, and a name may be
  // re-registered with different pixels. Only reached past the size gate, so the payload is small.
  static String? _glyphCacheKey(VImage vImage, GlyphTintLimits limits) {
    final suffix = '${limits.maxSide}x${limits.channelTolerance}';
    final remoteRef = vImage.remoteRef;
    if (remoteRef != null) return 'ref-$remoteRef-$suffix';
    final data = vImage.imageData?.data;
    if (data == null || data.isEmpty) return null;
    return '${GlyphTone.contentKey(ImageUtils.asBytes(data))}-$suffix';
  }

  static Future<GlyphTone?> _monochromeGlyphTone(
      ui.Image image, VImage vImage, GlyphTintLimits limits) async {
    if (image.width > limits.maxSide || image.height > limits.maxSide) {
      return null;
    }
    final key = _glyphCacheKey(vImage, limits);
    if (key != null && _monochromeGlyphs.containsKey(key)) return _monochromeGlyphs[key];

    final data = await ImageUtils.toBytes(image, ui.ImageByteFormat.rawRgba);
    final tone = data == null
        ? null
        : GlyphTone.scan(data, limits.channelTolerance,
            premultiplied: true);
    if (key != null) {
      // The key is content-derived, so the map would otherwise grow without bound.
      if (_monochromeGlyphs.length > 512) _monochromeGlyphs.clear();
      _monochromeGlyphs[key] = tone;
    }
    return tone;
  }

  static Future<ImageShape> fromVImageDetailed(VImage vImage,
      VGCDrawImageImageintintintintintintintint opArgs, Rect? clipRect,
      {Color? tint, GlyphTintLimits? glyphLimits, int alpha = 255,
      required bool useThemeColors}) async {
    final preserveColors = preserveIconColors;
    final colorFilter = preserveColors
        ? null
        : ColorFilter.mode(tint ?? AppColors.getColor(true), BlendMode.srcIn);
    try {
      // Bundled icons resolve by bare filename, so an application image sharing a name with one of
      // ours is replaced by it — off unless the application asks for it. Java-side svgContent
      // (an assets_path override) is explicit and applies either way.
      Object? replacement;
      if (vImage.svgContent?.isNotEmpty ?? false) {
        replacement = vImage.svgContent!;
      } else if (gcIconsReplacement &&
          vImage.filename != null &&
          vImage.filename!.isNotEmpty) {
        try {
          replacement = await AssetsManager.loadReplacement(vImage.filename!);
        } catch (e) {
          // Asset not found or failed to load
        }
      }

      if (replacement is String) {
        try {
          final pictureInfo = await vg.loadPicture(SvgStringLoader(replacement), null);
          final destRect = Rect.fromLTWH(
            (opArgs.destX).toDouble(),
            (opArgs.destY).toDouble(),
            opArgs.destWidth == -1 ? pictureInfo.size.width : (opArgs.destWidth).toDouble(),
            opArgs.destHeight == -1 ? pictureInfo.size.height : (opArgs.destHeight).toDouble(),
          );
          return ImageShape.svg(pictureInfo, destRect,
              clipRect: clipRect, colorFilter: colorFilter, alpha: alpha);
        } catch (e) {
          // SVG failed to load, fall through to raster
        }
      }

      // Plain blits only: a tint, an alpha or a bundled-icon replacement needs a real image.
      final remoteRef = vImage.remoteRef;
      if (replacement == null && remoteRef != null && !useThemeColors) {
        final render = ImageUtils.remoteRender(remoteRef);
        // Once rasterised, blit the texture: replaying the list re-runs every op on every frame.
        final asPicture = render == null || render.hasPixels ? null : render.picture;
        if (render != null && asPicture != null) {
          final src = Rect.fromLTWH(
              (opArgs.srcX).toDouble(),
              (opArgs.srcY).toDouble(),
              opArgs.srcWidth == -1 ? render.width.toDouble() : (opArgs.srcWidth).toDouble(),
              opArgs.srcHeight == -1 ? render.height.toDouble() : (opArgs.srcHeight).toDouble());
          final dest = Rect.fromLTWH(
              (opArgs.destX).toDouble(),
              (opArgs.destY).toDouble(),
              opArgs.destWidth == -1 ? src.width : (opArgs.destWidth).toDouble(),
              opArgs.destHeight == -1 ? src.height : (opArgs.destHeight).toDouble());
          // A scaled blit has to resample, which a picture draw would not do the same way.
          if (src.width == dest.width && src.height == dest.height) {
            // The whole render with nothing bounding it is the only blit a picture survives (see
            // [_drawPicture]); every other 1:1 blit has to be bounded, so it takes the pixels. A
            // source rectangle may ask for more than the render holds, hence >= and not ==.
            final unbounded = clipRect == null &&
                src.left == 0 &&
                src.top == 0 &&
                src.width >= render.width &&
                src.height >= render.height;
            if (unbounded) {
              return ImageShape.picture(asPicture, src, dest, alpha: alpha);
            }
            return ImageShape.raster(render.rasteriseSync().clone(), src, dest,
                clipRect: clipRect, alpha: alpha);
          }
        }
      }

      ui.Image? uiImage;
      try {
        uiImage = replacement as ui.Image? ?? await ImageUtils.decodeVImageToUIImage(vImage);
      } catch (e) {
        uiImage = null;
      }

      if (uiImage == null) {
        return ImageShape._(
          type: ImageType.raster,
          destRect: Rect.fromLTWH(opArgs.destX.toDouble(), opArgs.destY.toDouble(), 16, 16),
          image: null,
          srcRect: null,
          clipRect: clipRect,
          alpha: alpha,
        );
      }

      final destRect = Rect.fromLTWH(
        (opArgs.destX).toDouble(),
        (opArgs.destY).toDouble(),
        opArgs.destWidth == -1 ? uiImage.width.toDouble() : (opArgs.destWidth).toDouble(),
        opArgs.destHeight == -1 ? uiImage.height.toDouble() : (opArgs.destHeight).toDouble(),
      );

      final srcRect = replacement != null
          ? Rect.fromLTWH(0, 0, uiImage.width.toDouble(), uiImage.height.toDouble())
          : Rect.fromLTWH(
              (opArgs.srcX).toDouble(),
              (opArgs.srcY).toDouble(),
              opArgs.srcWidth == -1 ? uiImage.width.toDouble() : (opArgs.srcWidth).toDouble(),
              opArgs.srcHeight == -1 ? uiImage.height.toDouble() : (opArgs.srcHeight).toDouble(),
            );

      // A resolved icon is tinted; an already-rendered bitmap keeps its colors, unless the Canvas
      // itself is themed and the bitmap is a monochrome glyph.
      final glyphTone = replacement == null && useThemeColors
          ? await _monochromeGlyphTone(uiImage, vImage, glyphLimits ?? GlyphTintLimits.fallback)
          : null;
      final rasterFilter = replacement != null
          ? colorFilter
          : (glyphTone != null
              ? ColorFilter.mode(tint ?? AppColors.getColor(true), BlendMode.srcIn)
              : null);
      return ImageShape.raster(uiImage, srcRect, destRect,
          clipRect: clipRect, colorFilter: rasterFilter, alpha: alpha,
          inkMask: glyphTone?.inkMask,
          // A remoteRef names a picture this side rendered, and those start from an opaque fill.
          // An application bitmap carries no such promise.
          opaqueSource: vImage.remoteRef != null);
    } catch (e) {
      return ImageShape._(
        type: ImageType.raster,
        destRect: Rect.fromLTWH(opArgs.destX.toDouble(), opArgs.destY.toDouble(), 16, 16),
        image: null,
        srcRect: null,
        clipRect: clipRect,
        alpha: alpha,
      );
    }
  }

  final ImageType type;
  final Rect destRect;
  ui.Image? image;
  final Rect? srcRect;
  final PictureInfo? pictureInfo;
  final ColorFilter? colorFilter;
  final int alpha;
  @override
  final Rect? clipRect;

  @override
  bool get clipsBody => true;

  @override
  void draw(ui.Canvas c) => _drawClipped(c, this);

  @override
  void paintBody(ui.Canvas c) {
    switch (type) {
      case ImageType.raster: _drawRaster(c);
      case ImageType.svg: _drawSvg(c);
      case ImageType.picture: _drawPicture(c);
    }
  }

  void _drawRaster(ui.Canvas c) {
    if (image == null || srcRect == null) return;
    if (inkMask != null) {
      _drawMaskedGlyph(c);
      return;
    }
    // A 1:1 blit (no real scaling) must be pixel-exact, matching real SWT's GC#drawImage
    // contract for same-size draws (a plain blit on every native backend, no resampling).
    // Bilinear filtering has no benefit when there's nothing to scale, and instead bleeds
    // sharp single-pixel features into their neighbors — e.g. a single bright pixel against
    // a dark background loses intensity and smears into adjacent pixels even though src and
    // dest are the same size (see Test_org_eclipse_swt_graphics_Image's
    // ImageData-constructor tests, which draw exactly that).
    // Scaled with no interpolation set: bilinear, as the native backends' default is. A cubic
    // filter (FilterQuality.high) blends neighbouring pixels in even at a pixel's centre.
    final isScaled = srcRect!.width != destRect.width || srcRect!.height != destRect.height;
    c.drawImageRect(image!, srcRect!, destRect,
        Paint()
          ..filterQuality = _RenderHints.filterQuality ?? (isScaled ? FilterQuality.medium : FilterQuality.none)
          ..isAntiAlias = isScaled
          // Skia scales the blit by the paint's alpha; the RGB channels are unused for an image.
          ..color = Color.fromRGBO(0, 0, 0, alpha / 255.0)
          ..colorFilter = colorFilter);
  }

  void _drawMaskedGlyph(ui.Canvas c) {
    final isScaled = srcRect!.width != destRect.width || srcRect!.height != destRect.height;
    Paint blit() => Paint()
      ..filterQuality = _RenderHints.filterQuality ?? (isScaled ? FilterQuality.medium : FilterQuality.none)
      ..isAntiAlias = isScaled;
    c.saveLayer(destRect, Paint()..color = Color.fromRGBO(0, 0, 0, alpha / 255.0));
    c.drawImageRect(image!, srcRect!, destRect, blit()..colorFilter = colorFilter);
    c.drawImageRect(image!, srcRect!, destRect,
        blit()
          ..colorFilter = inkMask
          ..blendMode = BlendMode.dstIn);
    c.restore();
  }

  /// Deliberately unbounded: the web renderers ignore a clip around a nested picture, so a clip
  /// here would not hold it and the picture would draw past it. A picture reaches here only for a
  /// draw that needs no bounding; a blit that needs one goes to pixels ([fromVImageDetailed]) or
  /// through [_boundedPicture].
  void _drawPicture(ui.Canvas c) {
    final picture = remotePicture;
    if (picture == null) return;
    c.save();
    c.translate(destRect.left - (srcRect?.left ?? 0), destRect.top - (srcRect?.top ?? 0));
    if (sourceScale != 1) c.scale(1 / sourceScale);
    c.drawPicture(picture);
    c.restore();
  }

  void _drawSvg(ui.Canvas c) {
    if (pictureInfo == null) return;
    final scaleX = destRect.width / pictureInfo!.size.width;
    final scaleY = destRect.height / pictureInfo!.size.height;
    c.save();
    final layered = colorFilter != null || alpha != 255;
    if (layered) {
      c.saveLayer(
          destRect,
          Paint()
            ..color = Color.fromRGBO(0, 0, 0, alpha / 255.0)
            ..colorFilter = colorFilter);
    }
    c.translate(destRect.left, destRect.top);
    c.scale(scaleX, scaleY);
    c.drawPicture(pictureInfo!.picture);
    if (layered) c.restore();
    c.restore();
  }

  @override
  String toString() => 'ImageShape($type) dest:$destRect';

  @override
  String describe() => 'Image(${type.name}) dest=${_dr(destRect)} src=${_dr(srcRect)}'
      '${sourceScale != 1 ? ' scale=$sourceScale' : ''} alpha=$alpha tinted=${colorFilter != null}${_dk(clipRect)}';
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

/// What ends a GC's paint (Java's `GcWire.Dispose`), positionally: a flags byte (1, the paint
/// replaces what was drawn; 2, a damage rectangle follows; 4, the render's pixels go back to Java;
/// 8, the drawer keeps drawing on the render), the image's render ref, then the damage as zigzag x,
/// y, width and height.
class _Dispose {
  _Dispose(Uint8List frame, int start) {
    _read(frame, start);
  }

  _Dispose._();

  /// The fields at [start] in [frame], read into one instance every call reuses: for a caller that
  /// takes what it needs before the next.
  static _Dispose read(Uint8List frame, int start) => _shared.._read(frame, start);

  static final _Dispose _shared = _Dispose._();
  static final WidgetWireReader _reader = WidgetWireReader(Uint8List(0));

  void _read(Uint8List frame, int start) {
    final r = _reader..reset(frame, start, frame.length);
    final flags = r.u8();
    fullRepaint = flags & 1 != 0;
    pixels = flags & 4 != 0;
    retain = flags & 8 != 0;
    ref = r.varint();
    damage = null;
    if (flags & 2 != 0) {
      final x = r.zigzag().toDouble(), y = r.zigzag().toDouble();
      final width = r.zigzag().toDouble(), height = r.zigzag().toDouble();
      // The rectangle a Paint was scoped to; an empty one covers nothing to scope to.
      if (width > 0 && height > 0) damage = Rect.fromLTWH(x, y, width, height);
    }
  }

  bool fullRepaint = false;
  bool pixels = false;
  bool retain = false;
  int ref = 0;
  Rect? damage;
}
