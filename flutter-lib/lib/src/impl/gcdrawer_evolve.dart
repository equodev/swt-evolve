import 'utils/test_anchors.dart';
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
import 'utils/svg_tint.dart';
import 'utils/image_utils.dart';
import 'widget_config.dart' show getConfigFlags;
import 'utils/widget_utils.dart';
import 'owner_draw_overlay.dart' show OwnerDrawnRowFill;
import 'styledtext_evolve.dart';
import 'widget_config.dart';
import '../testing/gc_stats.dart';
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
    _SdfLineAtlas.prepare();
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

  /// For the GC of an owner-drawn Table or Tree row: the ground Evolve paints the row on and the
  /// widget theme's minimum text contrast; null for any other GC, or when the application's colours
  /// are painted as given. Text is kept legible ([legibleOn]) on what is under it: the row's own
  /// fills this paint, else this ground, but never more than on what the application set there:
  /// the colour it filled, else `chosenOn`, the row's ground as it set it.
  ({Color ground, Color chosenOn, double minContrast})? Function()? rowGround;

  /// For the GC of an owner-drawn Table row whose theme declares the colour of the row's own fill.
  OwnerDrawnRowFill? Function()? rowFill;

  /// Whether the owner-drawn row this GC paints is selected.
  bool Function()? rowSelected;

  // A rectangle filled on an owner-drawn row in the application colour that is the rows' own fill,
  // or their hover fill, takes the theme's colour for it.
  Color _asRowFill(Rect rect, Color color) {
    final fill = _useThemeColors ? null : rowFill?.call();
    return fill == null
        ? color
        : fill.resolve(state.id, color, _onRow(rect), selected: rowSelected?.call() ?? false);
  }

  // The opaque fills this paint has laid on an owner-drawn row, in row coordinates, the last on top.
  // Each with the colour the application filled it in, which differs when Evolve recoloured it.
  final List<(Rect, Color, Color)> _rowFills = [];
  static const int _maxRowFills = 256;

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
      await _renderAndSend(ref, wantPixels, retain: retain, contentKey: ending.contentKey);
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

  /// Called when scoped repaints have stacked past what a full-area Paint would replay (see
  /// [_maxRegionLayers]), or when the scene was drawn under another Canvas theme; the Canvas answers
  /// with a full-area Paint, which redraws the scene as one display list.
  final void Function()? onFullRepaintNeeded;

  // Every held draw is replayed on every frame: past this many layers, once they replay more than
  // two full scenes, a full-area Paint (one Java Paint) is cheaper than keeping them.
  static const int _maxRegionLayers = 16;

  /// The draws the last full-area Paint left to replay: what one full scene costs a frame.
  int _fullSceneDraws = 0;

  // Set once the full-area Paint is asked for, so the cycles before it arrives don't ask again.
  bool _fullRepaintRequested = false;

  // The Canvas theme the last full-area cycle resolved its colors from.
  CanvasThemeExtension? _sceneTheme;

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
      _rowFills.clear();
      rowFill?.call()?.painted(state.id);
      _settleNormalFill();
      final cycleTheme = _canvasTheme;
      // Nothing to wait for: committed now, with nothing made for the wait.
      if (_pendingImages.isEmpty && _commitsInFlight == 0) {
        _commit(cycleStaging, fullRepaint, damage, cycleTheme);
        _spareStaging = cycleStaging..clear();
        return null;
      }
      return _commitWhenReady(cycleStaging, fullRepaint, damage, cycleTheme);
    };
    // A repaint of what is already shown is not resent; Java still waits for the frame it is due in.
    _nextFrame = (frame, start) => _reportInFrame();
  }

  /// Commits a cycle that has images still decoding, or that an earlier one is still committing
  /// ahead of: decoding runs concurrently across cycles; only the commit is serialized.
  Future<void> _commitWhenReady(List<Shape> cycleStaging, bool fullRepaint, Rect? damage,
      CanvasThemeExtension? cycleTheme) async {
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
      _commit(cycleStaging, fullRepaint, damage, cycleTheme);
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
  /// of it when [fullRepaint], else over what it does not cover. [cycleTheme] is the Canvas theme
  /// the cycle's colors were resolved from.
  void _commit(
      List<Shape> cycleStaging, bool fullRepaint, Rect? damage, CanvasThemeExtension? cycleTheme) {
    final startedAt = GcStats.on ? GcStats.now() : 0;
    _cycleStyles.clear();
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
      _dropErasedFromLayers(damage, keep);
      shapes.add(RegionShape(damage, [...cycleStaging, ..._lateLoadedImages]));
      _lateLoadedImages.clear();
      if (!_fullRepaintRequested && _pastLayerBudget()) {
        _fullRepaintRequested = true;
        onFullRepaintNeeded?.call();
      }
    } else {
      // A full-area Paint repaints the whole client area, so nothing the previous cycle left
      // survives it; a GC opened outside one (draw2d/GEF feedback) composites on top instead.
      if (fullRepaint) {
        _fullRepaintRequested = false;
        _sceneTheme = cycleTheme;
        disposeShapeImages(shapes, keep: keep);
        shapes.clear();
      } else {
        _dropWhatThisCycleCovers(cycleStaging, keep);
      }
      shapes.addAll(cycleStaging);
      shapes.addAll(_lateLoadedImages);
      _lateLoadedImages.clear();
      if (fullRepaint) _fullSceneDraws = shapes.length;
    }

    // The committed frame, empty included: a cycle that drew nothing has to blank the control,
    // not leave the owner rendering the last frame that had content.
    onGCDispose?.call(List.of(shapes, growable: false));
    onShapesUpdated?.call(shapes);
    _reportInFrame();
    if (GcStats.on) {
      GcStats.commits++;
      GcStats.commitMicros += GcStats.now() - startedAt;
      GcStats.shapesHeld += shapes.length;
    }
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

  /// Whether the held layers are past [_maxRegionLayers] and replay more than two full scenes: a
  /// layer is its erase and the ops it still holds.
  bool _pastLayerBudget() {
    var layers = 0, draws = 0;
    for (var k = 0; k < shapes.length; k++) {
      final s = shapes[k];
      if (s is RegionShape) {
        layers++;
        draws += 1 + s.ops.length;
      } else {
        draws++;
      }
    }
    return layers > _maxRegionLayers && draws > 2 * _fullSceneDraws;
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

  /// Drops from every held layer the ops [damage] erases: a layer only shows through its own
  /// rectangle, so an op whose part inside it lies wholly inside [damage] can never show again. A
  /// drag's damage covers the figure an earlier layer drew, and what that layer keeps is the band
  /// no later damage reaches.
  void _dropErasedFromLayers(Rect damage, Set<ui.Image> keep) {
    final dropped = _dropped..clear();
    for (var k = 0; k < shapes.length; k++) {
      final layer = shapes[k];
      if (layer is! RegionShape || !layer.rect.overlaps(damage)) continue;
      // A layer is replaced, never changed: the frame committed before still draws the one it holds.
      final ops = layer.ops;
      List<Shape>? kept;
      for (var j = 0; j < ops.length; j++) {
        final op = ops[j];
        if (_erasedIn(op, layer.rect, damage)) {
          kept ??= ops.sublist(0, j);
          if (op.holdsImages) dropped.add(op);
        } else {
          kept?.add(op);
        }
      }
      if (kept != null) shapes[k] = RegionShape(layer.rect, kept);
    }
    if (dropped.isEmpty) return;
    collectShapeImages(shapes, keep);
    disposeShapeImages(dropped, keep: keep);
    dropped.clear();
  }

  /// Whether what [op] paints inside [layer] lies wholly inside [damage].
  static bool _erasedIn(Shape op, Rect layer, Rect damage) {
    var area = op.paintedBounds;
    if (area == null) return false;
    final clip = op.clipRect;
    if (clip != null) area = area.intersect(clip);
    area = area.intersect(layer);
    return area.width <= 0 || area.height <= 0 || _containsRect(damage, area);
  }

  /// Called by GCImpl.build() to provide context for text style resolution.
  void syncContext(BuildContext ctx) {
    _context = ctx;
    _canvasTheme = Theme.of(ctx).extension<CanvasThemeExtension>();
    _repaintIfThemeChanged();
  }

  // Themed colors are resolved as the drawing arrives, so a scene drawn under another Canvas theme
  // changes only when Java draws it again. The theme is this side's, so this side asks, as a native
  // window is invalidated when the system theme changes. A switch animates, so an answer drawn
  // mid-animation asks again until one is drawn under the final theme.
  void _repaintIfThemeChanged() {
    if (_fullRepaintRequested || shapes.isEmpty || !_useThemeColors) return;
    final painted = _sceneTheme;
    if (painted == null || painted == _canvasTheme) return;
    _fullRepaintRequested = true;
    WidgetsBinding.instance.addPostFrameCallback((_) => onFullRepaintNeeded?.call());
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
    // Every op asks for its colour, and a paint draws with a handful: the last few made are kept,
    // by value.
    final argb = swtColor.alpha * 0x1000000 + (swtColor.red << 16) + (swtColor.green << 8) + swtColor.blue;
    for (var i = 0; i < _ColorSlots.count; i++) {
      if (_themedArgb[i] == argb) return _themedColor[i]!;
    }
    final color = colorFromVColor(swtColor, defaultColor: fallback);
    final slot = _themedNext;
    _themedNext = (slot + 1) % _ColorSlots.count;
    _themedArgb[slot] = argb;
    _themedColor[slot] = color;
    _argbOfKnown(color, argb);
    return color;
  }

  final List<int> _themedArgb = List.filled(_ColorSlots.count, -1);
  final List<Color?> _themedColor = List.filled(_ColorSlots.count, null);
  int _themedNext = 0;

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
  /// Whether strokes are dashed, read before a caller builds the outline it would dash.
  bool get _dashedStrokes => (state.lineStyle ?? SWT.LINE_SOLID) != SWT.LINE_SOLID;

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

  /// The dashes of lines drawn before, by ends, pattern and offset, emptied when full.
  static final Map<_DashKey, Float32List> _lineDashes = {};
  static const int _lineDashesCapacity = 1024;
  static final _DashKey _dashProbe = _DashKey();

  /// The "on" runs of [pattern] along the straight line [from]-[to], as the endpoint pairs
  /// [SegmentsShape] strokes. A line's dashes are kept, so drawing it again reuses them.
  Float32List _dashSegments(Offset from, Offset to, List<double> pattern) {
    if (state.lineStyle == SWT.LINE_CUSTOM) return _dashSegmentsOf(from, to, pattern);
    final key = _dashProbe.set(from, to, pattern, (state.lineDashOffset ?? 0).toDouble());
    final held = _lineDashes[key];
    if (held != null) return held;
    if (_lineDashes.length >= _lineDashesCapacity) _lineDashes.clear();
    return _lineDashes[key.held()] = _dashSegmentsOf(from, to, pattern);
  }

  Float32List _dashSegmentsOf(Offset from, Offset to, List<double> pattern) {
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
    // The colour with its alpha replaced, as withOpacity gives it, made once per colour and alpha:
    // withOpacity works the channels out of floats three times and makes a new colour each call.
    final rgb = _argbOf(color) & 0xFFFFFF;
    for (var i = 0; i < _ColorSlots.count; i++) {
      if (_alphaRgb[i] == rgb && _alphaOf[i] == alpha) return _alphaColor[i]!;
    }
    final argb = alpha * 0x1000000 + rgb;
    final made = Color(argb);
    final slot = _alphaNext;
    _alphaNext = (slot + 1) % _ColorSlots.count;
    _alphaRgb[slot] = rgb;
    _alphaOf[slot] = alpha;
    _alphaColor[slot] = made;
    _argbOfKnown(made, argb);
    return made;
  }

  final List<int> _alphaRgb = List.filled(_ColorSlots.count, -1);
  final List<int> _alphaOf = List.filled(_ColorSlots.count, -1);
  final List<Color?> _alphaColor = List.filled(_ColorSlots.count, null);
  int _alphaNext = 0;

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
    final moved = transform == null ? shape : _translated(shape, transform);
    if (moved != null) {
      _staging.add(_clipped(_xored(moved)));
      return;
    }
    final clip = clipping;
    // A shape under the transform and clip of the one staged before it goes in with it: one canvas
    // transform for both, and their run is drawn as it would be under two.
    final open = _openTransform;
    if (open != null && _staging.isNotEmpty && identical(_staging.last, open) && open.clipRect == clip &&
        _sameMatrix(open.matrix, transform!) && !_wrapsOwnTransform) {
      open.children.add(shape);
      return;
    }
    final wrapped = TransformShape(transform!, [shape], clip);
    final staged = _clipped(_xored(wrapped));
    _openTransform = identical(staged, wrapped) ? wrapped : null;
    _staging.add(staged);
  }

  /// The transform [_addShape] staged last, while nothing has been wrapped around it.
  TransformShape? _openTransform;

  bool get _wrapsOwnTransform => state.XORMode == true || textClipMask != null || clipShape != null;

  static bool _sameMatrix(Float64List a, Float64List b) {
    if (identical(a, b)) return true;
    for (var i = 0; i < 16; i++) {
      if (a[i] != b[i]) return false;
    }
    return true;
  }

  /// [shape] moved by [transform] when that is a translation by whole units, under the clip the
  /// transform would have drawn it under: it lands on the same pixels as drawn translated, and as a
  /// shape of its own a rectangle joins the runs of its neighbours and a curve is drawn without a
  /// transform of its own. Null for any other transform or shape: a line or polygon in a run is
  /// antialiased by the run's edges, so it keeps its transform.
  Shape? _translated(Shape shape, Float64List m) {
    if (m[0] != 1 || m[5] != 1 || m[10] != 1 || m[15] != 1 || m[1] != 0 || m[2] != 0 || m[3] != 0 ||
        m[4] != 0 || m[6] != 0 || m[7] != 0 || m[8] != 0 || m[9] != 0 || m[11] != 0 || m[14] != 0) {
      return null;
    }
    final dx = m[12], dy = m[13];
    if (dx != dx.roundToDouble() || dy != dy.roundToDouble()) return null;
    final by = Offset(dx, dy);
    final clip = clipping;
    return switch (shape) {
      RectShape s => RectShape(s.rect.shift(by), s.color, s.strokeWidth, s.lineCap, s.lineJoin,
          isFilled: s.isFilled, clipRect: clip),
      OvalShape s => OvalShape(s.rect.shift(by), s.color, s.strokeWidth, isFilled: s.isFilled, clipRect: clip),
      RoundRectShape s => RoundRectShape(s.rect.shift(by), s.radiusX, s.radiusY, s.color, s.strokeWidth, s.lineCap,
          s.lineJoin, isFilled: s.isFilled, clipRect: clip),
      ArcShape s => ArcShape(s.rect.shift(by), s.startAngle, s.sweepAngle, s.color, s.strokeWidth, s.lineCap,
          s.lineJoin, isFilled: s.isFilled, clipRect: clip),
      PathShape s => s.moved(by, clip),
      TextShape s => TextShape(s.text, s.off + by, s.style, clip),
      _ => null,
    };
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
    final color = isFilled ? applyAlpha(_asRowFill(rect, fillColor)) : applyAlpha(strokeColor);
    if (isFilled) _recordRowFill(rect, color, applyAlpha(fillColor));
    final strokeWidth = isFilled ? 0.0 : lineWidth;
    if (!isFilled &&
        _dashedStrokes && _addDashedStroke(
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
    if (!isFilled && _dashedStrokes && _addDashedStroke(() => Path()..addOval(rect), color)) return;
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
    final color = isFilled ? applyAlpha(_asRowFill(rect, fillColor)) : applyAlpha(strokeColor);
    if (isFilled) _recordRowFill(rect, color, applyAlpha(fillColor));
    final strokeWidth = isFilled ? 0.0 : lineWidth;
    if (!isFilled &&
        _dashedStrokes && _addDashedPolyline(
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
      if (!isFilled && _dashedStrokes && _addDashedPolyline(_offsets(points), color, close: true)) {
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
      if (!isFilled && _dashedStrokes && _addDashedPolyline(_offsets(points), color)) {
        return;
      }
      _addShape(PolylineShape(points, color, strokeWidth, lineCap, lineJoin,
          isFilled: isFilled, clipRect: _childClip));
    }
  }

  void _addPathShape(VPath? path, {required bool isFilled}) {
    final source = _PathSource.of(path?.pathData, _fillType);
    // One fill op, so the strings take the geometry's color rather than resolving it again.
    final color = source != null ? _addPathGeometry(source, isFilled: isFilled) : null;
    for (final text in _pathTexts(path, isFilled: isFilled, color: color)) {
      _addShape(_legiblePathText(text));
    }
  }

  TextShape _legiblePathText(TextShape text) {
    final paint = text.style.foreground;
    if (paint == null || rowGround == null) return text;
    final tp = text.painter;
    final legible =
        _legibleOnRow(paint.color, Rect.fromLTWH(text.off.dx, text.off.dy, tp.width, tp.height));
    if (legible == paint.color) return text;
    final adjusted = Paint()
      ..color = legible
      ..isAntiAlias = paint.isAntiAlias
      ..style = paint.style
      ..strokeWidth = paint.strokeWidth;
    return TextShape(text.text, text.off, text.style.copyWith(foreground: adjusted), text.clipRect);
  }

  /// The color the geometry was drawn with, or null when a pattern filled it.
  Color? _addPathGeometry(_PathSource source, {required bool isFilled}) {
    if (isFilled && state.backgroundPattern != null && _addPatternFill(source.build(Offset.zero))) return null;
    final color = isFilled ? applyAlpha(fillColor) : applyAlpha(strokeColor);
    if (!isFilled && _dashedStrokes && _addDashedStroke(() => source.build(Offset.zero), color)) return color;
    _addShape(PathShape.of(source, Offset.zero, color, isFilled ? 0.0 : lineWidth, lineCap, lineJoin,
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

  Path? _buildPathData(VPathData? pathData, {PathFillType? fillType}) =>
      _PathSource.of(pathData, fillType ?? _fillType)?.build(Offset.zero);

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
        _dashedStrokes && _addDashedStroke(
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
    final argb = _argbOf(color);
    final held = _style;
    if (held != null && identical(font, _styledFont) && identical(_context, _styledContext) && argb == _styledColor) {
      return held;
    }
    _styledFont = font;
    _styledContext = _context;
    _styledColor = argb;
    // Text that switches font or colour from one string to the next, as highlighting does, finds the
    // style of each switch built once per cycle. The font is decoded afresh with every change, so it
    // is told by its data; a style takes only the first.
    final data = font?.fontData;
    final first = data == null || data.isEmpty ? null : data.first;
    final key = (first?.name, first?.height, first?.style, argb, _context);
    final built = _cycleStyles[key];
    if (built != null) return _style = built;
    if (_cycleStyles.length >= 64) _cycleStyles.clear();
    return _style = _cycleStyles[key] =
        _sameStyle(FontUtils.textStyleFromVFont(font, _context, color: color, applyDpiScaling: true));
  }

  /// The styles built this cycle; cleared when it is committed, so a change of theme or zoom between
  /// cycles is built anew.
  final Map<(String?, int?, int?, int, BuildContext?), TextStyle> _cycleStyles = {};

  /// Equal styles as one object. Every cycle decodes the GC's font afresh, so the style built from it
  /// is new each cycle; the one kept from an earlier cycle is returned instead.
  static TextStyle _sameStyle(TextStyle style) {
    final kept = _styles[style];
    if (kept != null) return kept;
    if (_styles.length >= 256) _styles.clear();
    return _styles[style] = style;
  }

  static final Map<TextStyle, TextStyle> _styles = {};

  void _drawText({
    required String text,
    required double x,
    required double y,
    int flags = 0,
    bool? isTransparent,
  }) {
    final processedText = _processTextFlags(text, flags);
    final transparent = isTransparent ?? (flags & SWT.DRAW_TRANSPARENT) != 0;

    final color = applyAlpha(textColor);
    final childClip = _childClip;
    var textShape = TextShape(processedText, Offset(x, y), _textStyle(state.font, color), childClip);
    final tp = textShape.painter;
    final box = Rect.fromLTWH(x, y, tp.width, tp.height);

    if (!transparent) {
      final ground = applyAlpha(textBackgroundColor);
      _recordRowFill(box, ground);
      _addShape(RectShape(box, ground, 0, lineCap, lineJoin,
          isFilled: true, clipRect: childClip));
    }
    final legible = _legibleOnRow(color, box);
    if (legible != color) {
      textShape = TextShape(processedText, Offset(x, y), _textStyle(state.font, legible), childClip);
    }
    _addShape(textShape);
  }

  // The application chose its text colour against the ground it set, which Evolve may not paint:
  // on an owner-drawn row the text is kept legible on what is actually under it.
  Color _legibleOnRow(Color color, Rect box) {
    final row = rowGround?.call();
    if (row == null) return color;
    final (ground, chosenOn) = _groundAt(_onRow(box).center, row);
    return legibleOn(color, ground, minRatio: row.minContrast, chosenOn: chosenOn);
  }

  /// Records an opaque fill painted in [color] where the application filled in [chosen] (the
  /// same colour unless Evolve painted the fill in another).
  void _recordRowFill(Rect rect, Color color, [Color? chosen]) {
    final row = rowGround?.call();
    if (row == null || _rowFills.length >= _maxRowFills) return;
    final onRow = _onRow(rect);
    final (ground, chosenOn) = _groundAt(onRow.center, row);
    _rowFills.add((
      onRow,
      Color.alphaBlend(color, ground),
      Color.alphaBlend(chosen ?? color, chosenOn),
    ));
  }

  /// What is painted at [point] and what the application set there: its topmost fill, else the row.
  (Color, Color) _groundAt(Offset point, ({Color ground, Color chosenOn, double minContrast}) row) {
    for (final (rect, color, chosen) in _rowFills.reversed) {
      if (rect.contains(point)) return (color, chosen);
    }
    return (row.ground, row.chosenOn);
  }

  Rect _onRow(Rect rect) {
    final transform = currentTransform;
    return transform == null
        ? rect
        : MatrixUtils.transformRect(Matrix4.fromFloat64List(transform), rect);
  }

  // Tabs and mnemonics as GCHelper.textExtent measures them, so text is drawn as wide as it measures.
  String _processTextFlags(String text, int flags) {
    final mnemonic =
        (flags & SWT.DRAW_MNEMONIC) != 0 ? stripAccelerators(text) : text;
    if (!_hasBreakOrTab(mnemonic)) return mnemonic;
    return mnemonic
        .replaceAll('\r\n', (flags & SWT.DRAW_DELIMITER) != 0 ? '\n' : ' ')
        .replaceAll('\r', (flags & SWT.DRAW_DELIMITER) != 0 ? '\n' : ' ')
        .replaceAll('\n', (flags & SWT.DRAW_DELIMITER) != 0 ? '\n' : ' ')
        .replaceAll('\t', (flags & SWT.DRAW_TAB) != 0 ? '        ' : ' ');
  }

  static bool _hasBreakOrTab(String text) {
    for (var i = 0; i < text.length; i++) {
      final c = text.codeUnitAt(i);
      if (c == 0x0A || c == 0x0D || c == 0x09) return true;
    }
    return false;
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
  /// application gave it, and the ops are in the application's units. Each axis is rounded to
  /// pixels on its own, so the vertical scale can differ from the horizontal one.
  double get _bufferScale => state.bufferScale ?? 1;
  double get _bufferScaleY => state.bufferScaleY ?? _bufferScale;

  void _scaleToBuffer(ui.Canvas canvas) {
    final sx = _bufferScale, sy = _bufferScaleY;
    if (sx != 1 || sy != 1) canvas.scale(sx, sy);
  }

  /// Whether an opaque op in [shapesToRender] paints over the whole image, hiding the base.
  bool _hidesBase(List<Shape> shapesToRender) {
    final whole = ui.Rect.fromLTWH(0, 0, (_imgWidth > 0 ? _imgWidth : 1) / _bufferScale,
        (_imgHeight > 0 ? _imgHeight : 1) / _bufferScaleY);
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
  ///
  /// [contentKey], when not 0, is what Java names the render's content by: a render with the key of
  /// one already rasterised takes its pixels (see [RemoteRender.contentKey]).
  Future<void> _renderAndSend(int ref, bool wantPixels, {bool retain = false, int contentKey = 0}) async {
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
    // A retained drawer's render becomes the next one's base, which Java does not name.
    assert(!retain || contentKey == 0);
    if (ref != 0) {
      ImageUtils.registerRemotePicture(ref, picture, _imgWidth > 0 ? _imgWidth : 1,
          _imgHeight > 0 ? _imgHeight : 1,
          depth: depth, contentKey: contentKey);
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
      {int alpha = 255, double sourceScale = 1, double? sourceScaleY}) {
    final k = sourceScale, ky = sourceScaleY ?? sourceScale;
    final source = Rect.fromLTRB(srcRect.left * k, srcRect.top * ky, srcRect.right * k, srcRect.bottom * ky);
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
    double? bufferScaleY,
  }) {
    final destRect = srcRect.shift(destOffset);
    // The base is in the buffer's pixels, [bufferScale] (and [bufferScaleY] down) to a unit of
    // the area copied.
    final s = bufferScale, sy = bufferScaleY ?? bufferScale;
    return [
      // Under the shapes drawn through this GC, so the copy keeps their z-order.
      if (basePicture != null)
        _boundedPicture(basePicture, srcRect, destRect, destRect, sourceScale: s, sourceScaleY: sy)
      else if (baseImage != null)
        ImageShape.raster(baseImage.clone(),
            Rect.fromLTRB(srcRect.left * s, srcRect.top * sy, srcRect.right * s, srcRect.bottom * sy)
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
      PathShape s => s.moved(offset, clipArea),
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
    final alpha = state.alpha ?? 255;
    // An image already decoded is staged now: waiting on it would only delay the cycle's commit.
    final ready = _useThemeColors ? null : ImageShape.fromDecoded(vImage, opArgs, _childClip, alpha);
    if (ready != null) {
      final transform = currentTransform;
      _staging.add(_wrapImage(ready, transform, transform == null ? null : clipping, clipShape,
          textClipMask, _antiAlias, _filterQuality));
      return;
    }
    _stageAsync(ImageShape.fromVImageDetailed(vImage, opArgs, _childClip,
        tint: imageTintColor, glyphLimits: glyphTintLimits,
        alpha: alpha, useThemeColors: _useThemeColors));
  }

  /// [image] under the GC state its op was drawn in.
  static Shape _wrapImage(Shape image, Float64List? transform, Rect? deviceClip, Path? shapeClip,
      List<Shape>? textMask, bool antiAlias, FilterQuality? filter) {
    final Shape hinted =
        antiAlias && filter == null ? image : RenderHintsShape(antiAlias, filter, [image]);
    final Shape shape =
        transform == null ? hinted : TransformShape(transform, [hinted], deviceClip);
    return textMask != null
        ? MaskShape(textMask, [shape])
        : shapeClip == null ? shape : ClipPathShape(shapeClip, [shape]);
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
    f.then((decoded) => _onImageLoaded(stagingList, idx,
        _wrapImage(decoded, transform, deviceClip, shapeClip, textMask, antiAlias, filter)));
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
          lineCap, _childClip, true));
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
    final start = applyAlpha(gradientStartColor), end = applyAlpha(gradientEndColor);
    // Text over a gradient is read against its middle.
    _recordRowFill(rect, Color.lerp(start, end, 0.5)!);
    _addShape(GradientRectShape(rect, start, end, o.vertical, _childClip));
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
      bufferScaleY: captured == null ? _bufferScaleY : 1,
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

/// The text drawn by [shapes] (drawString/drawText), in drawing order: what a test reads from a
/// canvas.
String? paintedTextOf(List<Shape> shapes) {
  final parts = <String>[];
  void collect(List<Shape> from) {
    for (final shape in from) {
      if (shape is TextShape) {
        final text = shape.text.trim();
        if (text.isNotEmpty) parts.add(text);
      } else if (shape is RegionShape) {
        collect(shape.ops);
      } else if (shape is ClipPathShape) {
        collect(shape.children);
      }
    }
  }

  collect(shapes);
  return parts.isEmpty ? null : parts.join(' ');
}

class ScenePainter extends CustomPainter implements PaintedTextSource {
  @override
  String? get paintedText => paintedTextOf(shapes);

  // Repaints on a font change too: the text shapes lay out again in a font that loaded after them.
  // The app's zoom too: which rectangles a run draws depends on where their edges land on device
  // pixels, and the zoom is applied above this painter, out of the canvas transform.
  ScenePainter(this.bg, this.shapes, {this.originY = 0, required this.devicePixelRatio})
      : super(repaint: _repaint);
  final Color bg;
  final List<Shape> shapes;

  static Listenable? _merged;
  static Listenable get _repaint {
    final fonts = FontUtils.systemFonts;
    if (fonts == null) return appScaleNotifier;
    return _merged ??= Listenable.merge([fonts, appScaleNotifier]);
  }

  /// Device pixels per logical pixel of the view painted into, before the app's zoom: the canvas
  /// transform leaves both out.
  final double devicePixelRatio;

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
    _RenderHints.devicePixelRatio = devicePixelRatio * appScaleNotifier.value;
    final startedAt = GcStats.on ? GcStats.now() : 0;
    try {
      drawShapeRun(canvas, shapes, background: bg);
    } finally {
      _RenderHints.devicePixelRatio = 1;
    }
    if (GcStats.on) {
      GcStats.paints++;
      GcStats.recordMicros += GcStats.now() - startedAt;
    }
    canvas.restore();
    if (ownLayer) canvas.restore();
  }

  @override
  bool shouldRepaint(ScenePainter o) {
    // Compare shapes, not the list: every build makes a fresh list.
    if (o.bg != bg || o.originY != originY || o.devicePixelRatio != devicePixelRatio) return true;
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

/// A number as dart2js prints it, whichever compiler built the app: a whole number has no
/// fraction (dart2wasm prints 1.0 as "1.0").
String _dd(num v) => v == v.truncateToDouble() ? v.toInt().toString() : v.toString();

/// An alignment by its name, as [Alignment.toString] gives it: a release wasm build minifies that
/// to "Instance of 'minified:…'".
String _da(Alignment a) {
  final name = switch ((a.x, a.y)) {
    (-1.0, -1.0) => 'topLeft',
    (0.0, -1.0) => 'topCenter',
    (1.0, -1.0) => 'topRight',
    (-1.0, 0.0) => 'centerLeft',
    (0.0, 0.0) => 'center',
    (1.0, 0.0) => 'centerRight',
    (-1.0, 1.0) => 'bottomLeft',
    (0.0, 1.0) => 'bottomCenter',
    (1.0, 1.0) => 'bottomRight',
    _ => null,
  };
  return name != null
      ? 'Alignment.$name'
      : 'Alignment(${a.x.toStringAsFixed(1)}, ${a.y.toStringAsFixed(1)})';
}
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
  // A nested run (a shape that holds shapes) reads the grid of its own canvas state; this run's is
  // put back when it returns, and read once it is first needed.
  final outerScale = _grid.scale, outerDx = _grid.dx, outerDy = _grid.dy;
  var gridRead = false;
  _approximate = getConfigFlags().gc_approximate_batching != false;

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
      if (GcStats.on) GcStats.drawnAlone(s);
      s.draw(c);
      i++;
      continue;
    }
    if (!gridRead && s is RectShape && s.clipRect != null) {
      _grid.read(c);
      gridRead = true;
    }
    final clip = _runClip(s);
    if (clip != open || (clip == null && clipped)) {
      if (clipped) c.restore();
      clipped = false;
      open = null;
      if (clip != null) {
        c.save();
        c.clipRect(clip);
        clipped = true;
        open = clip;
        if (GcStats.on) GcStats.clips++;
      }
    }
    if (_approximate && s is OvalShape && i + 1 < shapes.length && shapes[i + 1] is OvalShape) {
      if (!gridRead) {
        _grid.read(c);
        gridRead = true;
      }
      final count = _OvalAtlas.mayStart(s, shapes[i + 1] as OvalShape) ? _OvalAtlas.drawRun(c, shapes, i) : 0;
      if (count > 0) {
        if (GcStats.on) {
          GcStats.runs++;
          GcStats.shapesInRuns += count;
        }
        i += count;
        continue;
      }
    }
    // A run needs a second shape to join it: one alone draws as itself. A polyline's triangles cost
    // more to build than it to draw, so they are not built when the next shape cannot join it.
    if (!_mayJoinRun(s) || i + 1 == shapes.length || (s is PolylineShape && !_mayJoinRun(shapes[i + 1]))) {
      if (GcStats.on) GcStats.drawnAlone(s);
      s.paintBody(c);
      i++;
      continue;
    }
    if (px == 0) px = _devicePixel(c);
    if (!gridRead) {
      _grid.read(c);
      gridRead = true;
    }
    final span = _trianglesFrom(shapes, i, px);
    final count = _runCount;
    if (count > 1) {
      final run = _run;
      if (_fewRectangles(run, count)) {
        // A rectangle in a run lies on whole device pixels, where drawing it paints what its
        // triangles do; a few are cheaper drawn than a triangle list is built.
        for (var k = 0; k < count; k++) {
          (run[k]! as RectShape).paintBody(c);
        }
      } else {
        var vertices = 0, indices = 0;
        for (var k = 0; k < count; k++) {
          vertices += run[k]!.vertices;
          indices += run[k]!.indices;
        }
        final list = _TriangleList(vertices, indices, px);
        for (var k = 0; k < count; k++) {
          run[k]!.addTo(list);
        }
        final built = list.build();
        if (list.textured) {
          c.drawVertices(built, BlendMode.modulate, _SdfLineAtlas.paint!);
        } else {
          c.drawVertices(built, BlendMode.dst, _runPaint);
        }
      }
      if (GcStats.on) {
        GcStats.runs++;
        GcStats.shapesInRuns += count;
        GcStats.drawnAfterRuns += _passedCount;
      }
      // The shapes the run reached past, after it and in their order: none overlaps what it moved.
      // One under a clip of its own is passed only by a run under none, and clips itself.
      final passed = _passed;
      for (var k = 0; k < _passedCount; k++) {
        final p = passed[k]!;
        if (p.clipRect == clip) {
          p.paintBody(c);
        } else {
          p.draw(c);
        }
      }
      i += span;
    } else {
      if (GcStats.on) GcStats.drawnAlone(s);
      s.paintBody(c);
      i++;
    }
  }
  if (clipped) c.restore();
  _grid
    ..scale = outerScale
    ..dx = outerDx
    ..dy = outerDy;
}

/// [color] as ARGB. A paint draws with a few colours, each the same object every time, and
/// [Color.toARGB32] works it out from floats on every call: the last few are kept.
int _argbOf(Color color) {
  for (var i = 0; i < _ColorSlots.count; i++) {
    if (identical(_argbColors[i], color)) return _argbValues[i];
  }
  final argb = color.toARGB32();
  _argbOfKnown(color, argb);
  return argb;
}

/// Records that [color] is [argb].
void _argbOfKnown(Color color, int argb) {
  final slot = _argbNext;
  _argbNext = (slot + 1) % _ColorSlots.count;
  _argbColors[slot] = color;
  _argbValues[slot] = argb;
}

final List<Color?> _argbColors = List.filled(_ColorSlots.count, null);
final List<int> _argbValues = List.filled(_ColorSlots.count, 0);
int _argbNext = 0;

/// How many colours a colour memo keeps, replaced oldest first.
abstract final class _ColorSlots {
  static const int count = 8;
}

/// What a triangle run draws with: the colours are the vertices', so the paint carries nothing.
final Paint _runPaint = Paint();

/// The most rectangles a run draws one by one instead of as a triangle list.
const int _fewestForTriangles = 4;

bool _fewRectangles(List<_Piece?> run, int count) {
  if (count >= _fewestForTriangles) return false;
  for (var k = 0; k < count; k++) {
    if (run[k] is! RectShape) return false;
  }
  return true;
}

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
  final Float32List inner;

  /// Where the canvas multisamples, null: the polygon is [inner] alone, its edges antialiased by the
  /// samples as a filled path's are.
  final Float32List? outer;
  final Color color;

  @override
  int get vertices => outer == null ? inner.length ~/ 2 : _TriangleList.verticesFor(inner.length ~/ 2);
  @override
  int get indices => outer == null ? (inner.length ~/ 2 - 2) * 3 : _TriangleList.indicesFor(inner.length ~/ 2);
  @override
  void addTo(_TriangleList list) {
    final outer = this.outer;
    if (outer == null) {
      list.addFan(inner, color);
    } else {
      list.addPolygon(inner, outer, color);
    }
  }

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
    if (_TriangleList.multisampled) {
      final corners = Float32List(n * 2);
      for (var k = 0; k < n; k++) {
        corners[k * 2] = xs[k];
        corners[k * 2 + 1] = ys[k];
      }
      return _ConvexFill(corners, null, s.color);
    }
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

/// Whether wide polylines join runs: on the web, where a wide stroke is a path tessellated every
/// frame. Impeller strokes them on the GPU, which costs less than building their triangles.
@visibleForTesting
bool polylinesJoinRuns = kIsWeb;

/// Forces whether runs take the canvas to multisample, in place of asking the engine.
@visibleForTesting
bool? debugMultisampled;

/// How many times [_OvalAtlas] has drawn its sprites anew.
@visibleForTesting
int ovalAtlasBuilds = 0;

/// Ovals drawn as sprites of one image: each distinct oval is drawn once, white, at its size and
/// position within a device pixel, by the same oval draw it gets on its own, and a run of ovals is
/// then one atlas draw, each sprite tinted by its colour. Each pixel is the oval's coverage times its
/// colour, as drawn on its own, but for the coverage being kept in eight bits. A GPU renderer issues
/// one draw per oval; this is one per run.
///
/// Impeller's oval shader bends an edge pixel's coverage towards the colour's luma, so where it draws,
/// a sprite is drawn in a grey of its oval's luma, to the nearest of [_lumaSteps], and the image keeps
/// only its coverage.
class _OvalAtlas {
  /// The largest sprite side, in device pixels; a larger oval is drawn on its own.
  static const int _maxSide = 128;

  /// How many greys a sprite is drawn in where Impeller draws: one luma step bends coverage by at most
  /// 0.414 / 62, under two levels.
  static const int _lumaSteps = 32;

  static int _lumaStep(Color c) => ((0.2126 * c.r + 0.7152 * c.g + 0.0722 * c.b) * (_lumaSteps - 1)).round();

  /// Makes a grey sprite white, keeping its coverage.
  static final Paint _coverageOnly = Paint()
    ..colorFilter = const ColorFilter.matrix(
        [0, 0, 0, 0, 255, 0, 0, 0, 0, 255, 0, 0, 0, 0, 255, 0, 0, 0, 1, 0]);

  /// The fewest ovals drawn as one atlas draw: a few cost less drawn one by one.
  static const int _fewest = 4;

  /// Whether a run may start at [first] followed by [second], told without working out their
  /// sprites: where a hairline outline follows every fill, no run forms, and none is looked for.
  static bool mayStart(OvalShape first, OvalShape second) =>
      _grid.scale != 0 && _wideEnough(first) && _wideEnough(second);

  static bool _wideEnough(OvalShape s) => s.isFilled || s.strokeWidth * _grid.scale >= 2;

  /// The image's size; once its sprites no longer fit, it is emptied and drawn anew for the ovals in
  /// hand.
  static const int _atlasWidth = 2048;
  static const int _atlasHeight = 2048;

  /// Each sprite's place in [_image], by [_key]: left, top, right, bottom.
  static final Map<int, int> _slots = {};
  static final List<double> _rects = [];
  /// The ovals the sprites are drawn from, with the device scale and the position within a device
  /// pixel each was keyed at, and the key.
  static final List<(OvalShape, double, double, double, int)> _sprites = [];
  static ui.Image? _image;

  static Float32List _transforms = Float32List(0), _sources = Float32List(0);
  static Int32List _colors = Int32List(0);
  static final Paint _atlasPaint = Paint()..filterQuality = FilterQuality.none;

  /// The sprite an oval is drawn as, as one number: whether it is filled, its device width, height and
  /// stroke width in quarter pixels, and where it starts within a device pixel, in quarters; -1 when
  /// it cannot be a sprite. Packed by arithmetic, not bit operations, which are 32-bit in JavaScript.
  /// A stroke under two device pixels is not one: a hairline is drawn by other means than a wide
  /// stroke, which a sprite does not reproduce.
  static int _key(OvalShape s) {
    final scale = _grid.scale;
    if (scale == 0 || !_RenderHints.antiAlias) return -1;
    final r = _alignedRect(s.rect, s.strokeWidth, !s.isFilled);
    final w = _quarters(r.width * scale), h = _quarters(r.height * scale);
    final stroke = s.isFilled ? 0 : _quarters(s.strokeWidth * scale);
    final fx = _quarters(r.left * scale + _grid.dx), fy = _quarters(r.top * scale + _grid.dy);
    if (w == null || h == null || stroke == null || fx == null || fy == null || w <= 0 || h <= 0) return -1;
    if (!s.isFilled && stroke < 8) return -1;
    if ((math.max(w, h) + stroke) ~/ 4 + 4 > _maxSide) return -1;
    final key = (s.isFilled ? 1 : 0) + 2 * (w + 1024 * (h + 1024 * (stroke + 1024 * (fx % 4 + 4 * (fy % 4)))));
    return _TriangleList.multisampled ? key * _lumaSteps + _lumaStep(s.color) : key;
  }

  /// [v] in quarter pixels when it is a whole number of them.
  static int? _quarters(double v) {
    final q = v * 4;
    final r = q.roundToDouble();
    return (q - r).abs() > 1e-6 ? null : r.toInt();
  }

  /// The keys of the ovals of the run being drawn.
  static List<int> _runKeys = List.filled(64, 0);

  /// Draws the ovals from [start] that share its clip as one atlas draw, and returns how many; 0 when
  /// fewer than [_fewest] can.
  static int drawRun(ui.Canvas c, List<Shape> shapes, int start) {
    final clip = shapes[start].clipRect;
    var end = start;
    var missing = false;
    while (end < shapes.length) {
      final s = shapes[end];
      if (s is! OvalShape || s.clipRect != clip) break;
      final key = _key(s);
      if (key < 0) break;
      final k = end - start;
      if (k == _runKeys.length) _runKeys = List.filled(k * 2, 0)..setRange(0, k, _runKeys);
      _runKeys[k] = key;
      if (!missing && !_slots.containsKey(key)) missing = true;
      end++;
    }
    if (end - start < _fewest) return 0;
    if (missing) end = _build(shapes, start, end);
    final count = end - start;
    if (count < _fewest) return 0;
    if (_transforms.length < count * 4) {
      _transforms = Float32List(count * 4);
      _sources = Float32List(count * 4);
      _colors = Int32List(count);
    }
    final scale = _grid.scale, inv = 1 / scale;
    for (var k = 0; k < count; k++) {
      final s = shapes[start + k] as OvalShape;
      final slot = _slots[_runKeys[k]]!;
      final r = _alignedRect(s.rect, s.strokeWidth, !s.isFilled);
      final pad = _pad(s, scale);
      // The sprite's top left lands on the device pixel its oval starts in, less the pad.
      final ox = (r.left * scale + _grid.dx).floorToDouble() - pad, oy = (r.top * scale + _grid.dy).floorToDouble() - pad;
      _transforms[k * 4] = inv;
      _transforms[k * 4 + 1] = 0;
      _transforms[k * 4 + 2] = (ox - _grid.dx) * inv;
      _transforms[k * 4 + 3] = (oy - _grid.dy) * inv;
      _sources[k * 4] = _rects[slot * 4];
      _sources[k * 4 + 1] = _rects[slot * 4 + 1];
      _sources[k * 4 + 2] = _rects[slot * 4 + 2];
      _sources[k * 4 + 3] = _rects[slot * 4 + 3];
      _colors[k] = _argbOf(s.color);
    }
    c.drawRawAtlas(_image!, Float32List.sublistView(_transforms, 0, count * 4),
        Float32List.sublistView(_sources, 0, count * 4), Int32List.sublistView(_colors, 0, count), BlendMode.modulate,
        null, _atlasPaint);
    return count;
  }

  /// Device pixels of room round an oval's box in its sprite: half its stroke, and its antialiasing.
  static double _pad(OvalShape s, double scale) =>
      (s.isFilled ? 0 : ((s.strokeWidth > 0 ? s.strokeWidth : 1 / scale) * scale / 2).ceilToDouble()) + 2;

  /// Draws the image anew with every sprite it holds and those of the ovals from [start] to [end],
  /// and returns where the ovals it holds sprites for end: before [end] when they do not all fit.
  static int _build(List<Shape> shapes, int start, int end) {
    _add(shapes, start, end);
    _tallestFirst();
    if (_pack() < _sprites.length) {
      _slots.clear();
      _sprites.clear();
      _add(shapes, start, end);
      _tallestFirst();
      var fitted = _pack();
      if (fitted < _sprites.length) {
        // The run's own ovals do not all fit: the first ones in drawing order go in this draw, and
        // the rest start the next.
        _slots.clear();
        _sprites.clear();
        _add(shapes, start, end);
        fitted = _pack();
      }
      if (fitted < _sprites.length) {
        _sprites.length = fitted;
        _slots.removeWhere((_, slot) => slot >= fitted);
        var k = start;
        while (k < end && _slots.containsKey(_runKeys[k - start])) {
          k++;
        }
        end = k;
      }
    }
    final recorder = ui.PictureRecorder();
    final canvas = ui.Canvas(recorder);
    final grey = _TriangleList.multisampled;
    if (grey) canvas.saveLayer(null, _coverageOnly);
    var bottom = 0.0;
    for (var n = 0; n < _sprites.length; n++) {
      final (s, scale, phaseX, phaseY, _) = _sprites[n];
      final r = _alignedRect(s.rect, s.strokeWidth, !s.isFilled);
      final pad = _pad(s, scale);
      final x = _rects[n * 4], y = _rects[n * 4 + 1];
      canvas.save();
      canvas.clipRect(Rect.fromLTRB(x, y, _rects[n * 4 + 2], _rects[n * 4 + 3]));
      canvas.translate(x + pad + phaseX, y + pad + phaseY);
      canvas.scale(scale);
      canvas.translate(-r.left, -r.top);
      final level = grey ? (_lumaStep(s.color) * 255 / (_lumaSteps - 1)).round() : 255;
      OvalShape(s.rect, Color.fromARGB(255, level, level, level), s.strokeWidth, isFilled: s.isFilled).paintBody(canvas);
      canvas.restore();
      bottom = math.max(bottom, _rects[n * 4 + 3]);
    }
    if (grey) canvas.restore();
    _image?.dispose();
    _image = recorder.endRecording().toImageSync(_atlasWidth, math.max(bottom.ceil(), 1));
    ovalAtlasBuilds++;
    return end;
  }

  /// Gives a sprite to each oval from [start] to [end] that has none.
  static void _add(List<Shape> shapes, int start, int end) {
    for (var k = start; k < end; k++) {
      final key = _runKeys[k - start];
      if (_slots.containsKey(key)) continue;
      _slots[key] = _sprites.length;
      final s = shapes[k] as OvalShape;
      final r = _alignedRect(s.rect, s.strokeWidth, !s.isFilled);
      final fx = r.left * _grid.scale + _grid.dx, fy = r.top * _grid.scale + _grid.dy;
      _sprites.add((s, _grid.scale, fx - fx.floorToDouble(), fy - fy.floorToDouble(), key));
    }
  }

  /// A sprite's width and height in the image.
  static double _spriteWidth((OvalShape, double, double, double, int) sprite) {
    final (s, scale, phaseX, _, _) = sprite;
    return (phaseX + _alignedRect(s.rect, s.strokeWidth, !s.isFilled).width * scale + 2 * _pad(s, scale))
        .ceilToDouble();
  }

  static double _spriteHeight((OvalShape, double, double, double, int) sprite) {
    final (s, scale, _, phaseY, _) = sprite;
    return (phaseY + _alignedRect(s.rect, s.strokeWidth, !s.isFilled).height * scale + 2 * _pad(s, scale))
        .ceilToDouble();
  }

  /// Orders the sprites tallest first, and their slots with them: rows of sprites of about one height
  /// waste little of the image, where sprites in drawing order leave each row as tall as its tallest
  /// and fill the image before every sprite is in.
  static void _tallestFirst() {
    _sprites.sort((a, b) => _spriteHeight(b).compareTo(_spriteHeight(a)));
    _slots.clear();
    for (var n = 0; n < _sprites.length; n++) {
      _slots[_sprites[n].$5] = n;
    }
  }

  /// Places the sprites in rows across the image, into [_rects], and returns how many fit in it.
  static int _pack() {
    var x = 0.0, y = 0.0, row = 0.0;
    _rects.clear();
    for (var n = 0; n < _sprites.length; n++) {
      final w = _spriteWidth(_sprites[n]), h = _spriteHeight(_sprites[n]);
      if (x + w > _atlasWidth) {
        x = 0;
        y += row;
        row = 0;
      }
      if (y + h > _atlasHeight) return n;
      _rects..add(x)..add(y)..add(x + w)..add(y + h);
      x += w;
      row = math.max(row, h);
    }
    return _sprites.length;
  }
}

/// How many polyline strokes [_PolylineStroke] has built rather than found kept.
@visibleForTesting
int polylineStrokesBuilt = 0;

/// A straight dashed line: its ends, pattern and dash offset. A pattern is the same list for a line
/// style and width, so it compares by identity. Looked up with one reused key, copied only when kept.
final class _DashKey {
  Offset from = Offset.zero, to = Offset.zero;
  List<double> pattern = const [];
  double offset = 0;
  @override
  int hashCode = 0;

  _DashKey set(Offset from, Offset to, List<double> pattern, double offset) {
    this.from = from;
    this.to = to;
    this.pattern = pattern;
    this.offset = offset;
    hashCode = Object.hash(from, to, identityHashCode(pattern), offset);
    return this;
  }

  _DashKey held() => _DashKey()
    ..from = from
    ..to = to
    ..pattern = pattern
    ..offset = offset
    ..hashCode = hashCode;

  @override
  bool operator ==(Object other) =>
      other is _DashKey && other.hashCode == hashCode && identical(other.pattern, pattern) &&
      other.offset == offset && other.from == from && other.to == to;
}

/// A polyline and its stroke. Looked up with one reused key, copied only when kept.
final class _PolylineKey {
  List<int> points = const [];
  double strokeWidth = 0, px = 0;
  int lineCap = 0, lineJoin = 0, argb = 0;
  bool antiAlias = false;

  @override
  int hashCode = 0;

  _PolylineKey set(List<int> points, double strokeWidth, int lineCap, int lineJoin, int argb, double px,
      bool antiAlias) {
    this.points = points;
    this.strokeWidth = strokeWidth;
    this.lineCap = lineCap;
    this.lineJoin = lineJoin;
    this.argb = argb;
    this.px = px;
    this.antiAlias = antiAlias;
    var h = points.length;
    for (var i = 0; i < points.length; i++) {
      h = (h * 31 + points[i]) & 0x3FFFFFFF;
    }
    hashCode = Object.hash(h, strokeWidth, lineCap, lineJoin, argb, px, antiAlias);
    return this;
  }

  _PolylineKey held() => _PolylineKey()
    ..points = points
    ..strokeWidth = strokeWidth
    ..lineCap = lineCap
    ..lineJoin = lineJoin
    ..argb = argb
    ..px = px
    ..antiAlias = antiAlias
    ..hashCode = hashCode;

  @override
  bool operator ==(Object other) {
    if (other is! _PolylineKey || other.hashCode != hashCode) return false;
    if (other.strokeWidth != strokeWidth || other.px != px || other.lineCap != lineCap ||
        other.lineJoin != lineJoin || other.argb != argb || other.antiAlias != antiAlias ||
        other.points.length != points.length) {
      return false;
    }
    for (var i = 0; i < points.length; i++) {
      if (other.points[i] != points[i]) return false;
    }
    return true;
  }
}

/// Whether runs also take the shapes whose triangles antialias their edges slightly differently from
/// the shape drawn on its own (`gc_approximate_batching`, on unless the application turns it off).
/// Without it a run takes only what it draws to the same pixels: rectangles and lines on whole device
/// pixels, dashes, which are drawn as these same triangles on their own too, and where the canvas
/// multisamples, convex fills. Read per run.
bool _approximate = true;

/// Whether [s] is of a kind [_pieceOf] may make a piece of, decided without the device pixel: a
/// shape that cannot join a run is drawn without reading the canvas transform back.
bool _mayJoinRun(Shape s) =>
    (s is LineShape && s.lineCap != SWT.CAP_ROUND) ||
    (s is SegmentsShape && s.lineCap != SWT.CAP_ROUND && s.points.isNotEmpty) ||
    (s is RectShape && s.mayJoinRun) ||
    ((_approximate || _TriangleList.multisampled) && s is PolygonShape && s.isFilled && s.points.length >= 6) ||
    (_approximate && s is GradientRectShape && s._alongAnAxis) ||
    (polylinesJoinRuns && _approximate && s is PolylineShape && !s.isFilled && s.points.length >= 4);

/// What a triangle run can draw [s] as, or null when it is drawn on its own.
_Piece? _pieceOf(Shape s, double px) {
  if (s is LineShape && s.lineCap != SWT.CAP_ROUND) {
    if (!_approximate) return s._fillsGridQuad() ? s : null;
    if (_TriangleList.sdfLines && !s._fillsGridQuad() && !_TriangleList.sdfFits(s.strokeWidth, s._length, s.lineCap, px)) {
      return null;
    }
    return s;
  }
  if (s is SegmentsShape && s.lineCap != SWT.CAP_ROUND && s.points.isNotEmpty) {
    return _Strokes(s.points, s.strokeWidth, s.lineCap, s.color);
  }
  if (s is RectShape) return s.joinsRun ? s : null;
  // Where the canvas multisamples, a convex fill is its own triangles and paints what its path does.
  if (s is PolygonShape) return _approximate || _TriangleList.multisampled ? _ConvexFill.of(s, px) : null;
  if (!_approximate) return null;
  if (s is GradientRectShape) return s._onGrid ? s : null;
  if (s is PolylineShape) return _PolylineStroke.of(s, px);
  return null;
}

/// A wide polyline's stroke as triangles that never overlap, so a translucent colour is laid once, as
/// the stroke lays it: each segment a quad cut where it meets its neighbour on the inside of a turn,
/// each join filled from its corner on the outside, the caps likewise, and a ring that fades across a
/// device pixel all round the outline. Only for a stroke that cannot reach over itself; any other is
/// drawn on its own.
class _PolylineStroke implements _Piece {
  _PolylineStroke._(this._positions, this._colors, this._indices);
  final Float32List _positions;
  final Int32List _colors;
  final Uint16List _indices;

  @override
  int get vertices => _colors.length;
  @override
  int get indices => _indices.length;
  @override
  void addTo(_TriangleList list) => list.addMesh(_positions, _colors, _indices);

  /// The most points a polyline drawn this way has: its segments are checked against each other.
  static const int _maxPoints = 64;

  /// A Paint's: beyond it a miter join is drawn as a bevel.
  static const double _miterLimit = 4;

  /// Strokes built, by polyline and device pixel, kept while the same polylines are drawn again: a
  /// GC repaints its polylines every frame, and building one costs more than looking it up. Emptied
  /// when full, so it holds more than a diagram's connectors. A polyline that cannot be drawn this way
  /// is kept too, as null.
  static final Map<_PolylineKey, _PolylineStroke?> _built = {};
  static const int _capacity = 1024;
  static final _PolylineKey _probe = _PolylineKey();

  static _PolylineStroke? of(PolylineShape s, double px) {
    if (_TriangleList.strip || s.isFilled || s.points.length < 4 || s.points.length > 2 * _maxPoints) return null;
    final key = _probe.set(s.points, s.strokeWidth, s.lineCap, s.lineJoin, _argbOf(s.color), px,
        _RenderHints.antiAlias);
    final kept = _built[key];
    if (kept != null || _built.containsKey(key)) return kept;
    if (_built.length >= _capacity) _built.clear();
    polylineStrokesBuilt++;
    return _built[key.held()] = _build(s, px);
  }

  static _PolylineStroke? _build(PolylineShape s, double px) {
    final f = _RenderHints.antiAlias ? px / 2 : 0.0;
    final h = s.strokeWidth / 2;
    if (h <= f) return null;
    final shift = _strokeShift(s.strokeWidth, true).dx;
    final xs = Float64List(s.points.length ~/ 2), ys = Float64List(s.points.length ~/ 2);
    var m = 0;
    for (var i = 0; i + 1 < s.points.length; i += 2) {
      final x = s.points[i] + shift, y = s.points[i + 1] + shift;
      if (m > 0 && xs[m - 1] == x && ys[m - 1] == y) continue;
      xs[m] = x;
      ys[m] = y;
      m++;
    }
    if (m < 2) return null;
    final ux = Float64List(m - 1), uy = Float64List(m - 1);
    for (var i = 0; i + 1 < m; i++) {
      final dx = xs[i + 1] - xs[i], dy = ys[i + 1] - ys[i], l = math.sqrt(dx * dx + dy * dy);
      ux[i] = dx / l;
      uy[i] = dy / l;
    }
    final ho = h + f;
    final cap = s.lineCap;
    // At each corner: which way it turns, how far the inside cut runs back along both segments, how
    // far the outside turns, and whether it is mitred.
    final turn = Float64List(m), trim = Float64List(m), angle = Float64List(m), dots = Float64List(m);
    final mitred = Uint8List(m);
    for (var j = 1; j + 1 < m; j++) {
      final a = j - 1;
      final dot = ux[a] * ux[j] + uy[a] * uy[j], cross = ux[a] * uy[j] - uy[a] * ux[j];
      // Near a reversal the inside cut runs far back along both segments.
      if (1 + dot < 0.05) return null;
      turn[j] = cross;
      dots[j] = dot;
      trim[j] = ho * cross.abs() / (1 + dot);
      angle[j] = math.atan2(cross.abs(), dot);
      mitred[j] = s.lineJoin == SWT.JOIN_MITER && 1 / math.sqrt((1 + dot) / 2) <= _miterLimit ? 1 : 0;
    }
    for (var i = 0; i + 1 < m; i++) {
      final dx = xs[i + 1] - xs[i], dy = ys[i + 1] - ys[i];
      if (trim[i] + trim[i + 1] > math.sqrt(dx * dx + dy * dy)) return null;
    }
    // Segments that do not meet stay further apart than the stroke is wide, caps included.
    final apart = 2 * ho + (cap == SWT.CAP_SQUARE ? 2 * h : 0);
    for (var i = 0; i + 2 < m - 1; i++) {
      final il = math.min(xs[i], xs[i + 1]) - apart, ir = math.max(xs[i], xs[i + 1]) + apart;
      final it = math.min(ys[i], ys[i + 1]) - apart, ib = math.max(ys[i], ys[i + 1]) + apart;
      for (var k = i + 2; k + 1 < m; k++) {
        if (math.max(xs[k], xs[k + 1]) < il || math.min(xs[k], xs[k + 1]) > ir) continue;
        if (math.max(ys[k], ys[k + 1]) < it || math.min(ys[k], ys[k + 1]) > ib) continue;
        if (!_segmentsApart(xs[i], ys[i], xs[i + 1], ys[i + 1], xs[k], ys[k], xs[k + 1], ys[k + 1], apart)) {
          return null;
        }
      }
    }
    final quarter = _TriangleList.quarterSteps(ho, px);
    if (quarter == 0) return null;
    final step = math.pi / 2 / quarter;
    final round = s.lineJoin == SWT.JOIN_ROUND;

    final hiLoop = <double>[], hoLoop = <double>[];
    final l0 = Int32List(m - 1), l1 = Int32List(m - 1), r0 = Int32List(m - 1), r1 = Int32List(m - 1);
    final inner = Int32List(m), outerFrom = Int32List(m), outerTo = Int32List(m);
    var endCapFrom = 0, endCapTo = 0, startCapFrom = 0;

    // The outline at [w] from the line, as a loop: along the left side, round the end, back along the
    // right side and round the start. Both loops have the same corners, one for one.
    void trace(double w, List<double> out, bool record) {
      int add(double x, double y) {
        out
          ..add(x)
          ..add(y);
        return out.length ~/ 2 - 1;
      }

      // Points on the side [side] of corner [j], turning from segment [from]'s normal by [sweep].
      void join(int j, double side, int from, double sweep) {
        if (round) {
          final steps = (angle[j] / step).ceil();
          final start = math.atan2(side * ux[from], -side * uy[from]);
          for (var k = 1; k < steps; k++) {
            final a = start + sweep * k / steps;
            add(xs[j] + w * math.cos(a), ys[j] + w * math.sin(a));
          }
        } else if (mitred[j] == 1) {
          final a = j - 1, q = side * w / (1 + dots[j]);
          add(xs[j] + (-uy[a] - uy[j]) * q, ys[j] + (ux[a] + ux[j]) * q);
        }
      }

      // How far past each end the outline runs: a flat end where the line ends, a square one half the
      // width further, a round one from where its arc starts.
      final s0 = cap == SWT.CAP_SQUARE ? -w : (cap == SWT.CAP_ROUND ? 0.0 : h - w), e0 = -s0;
      final last = m - 2;
      final first = add(xs[0] - uy[0] * w + ux[0] * s0, ys[0] + ux[0] * w + uy[0] * s0);
      if (record) l0[0] = first;
      for (var j = 1; j + 1 < m; j++) {
        final a = j - 1;
        if (turn[j] >= 0) {
          final q = w / (1 + dots[j]);
          final k = add(xs[j] + (-uy[a] - uy[j]) * q, ys[j] + (ux[a] + ux[j]) * q);
          if (record) l1[a] = l0[j] = inner[j] = k;
        } else {
          final from = add(xs[j] - uy[a] * w, ys[j] + ux[a] * w);
          join(j, 1, a, -angle[j]);
          final to = add(xs[j] - uy[j] * w, ys[j] + ux[j] * w);
          if (record) {
            l1[a] = outerFrom[j] = from;
            l0[j] = outerTo[j] = to;
          }
        }
      }
      final pe = m - 1;
      final leftEnd = add(xs[pe] - uy[last] * w + ux[last] * e0, ys[pe] + ux[last] * w + uy[last] * e0);
      if (cap == SWT.CAP_ROUND) {
        final start = math.atan2(ux[last], -uy[last]);
        for (var k = 1; k < 2 * quarter; k++) {
          final a = start - k * math.pi / (2 * quarter);
          add(xs[pe] + w * math.cos(a), ys[pe] + w * math.sin(a));
        }
      }
      final rightEnd = add(xs[pe] + uy[last] * w + ux[last] * e0, ys[pe] - ux[last] * w + uy[last] * e0);
      if (record) {
        l1[last] = endCapFrom = leftEnd;
        r1[last] = endCapTo = rightEnd;
      }
      for (var j = m - 2; j >= 1; j--) {
        final a = j - 1;
        if (turn[j] >= 0) {
          final from = add(xs[j] + uy[j] * w, ys[j] - ux[j] * w);
          join(j, -1, j, -angle[j]);
          final to = add(xs[j] + uy[a] * w, ys[j] - ux[a] * w);
          if (record) {
            r0[j] = outerFrom[j] = from;
            r1[a] = outerTo[j] = to;
          }
        } else {
          final q = w / (1 + dots[j]);
          final k = add(xs[j] + (uy[a] + uy[j]) * q, ys[j] - (ux[a] + ux[j]) * q);
          if (record) r0[j] = r1[a] = inner[j] = k;
        }
      }
      final rightStart = add(xs[0] + uy[0] * w + ux[0] * s0, ys[0] - ux[0] * w + uy[0] * s0);
      if (record) r0[0] = startCapFrom = rightStart;
      if (cap == SWT.CAP_ROUND) {
        final start = math.atan2(-ux[0], uy[0]);
        for (var k = 1; k < 2 * quarter; k++) {
          final a = start - k * math.pi / (2 * quarter);
          add(xs[0] + w * math.cos(a), ys[0] + w * math.sin(a));
        }
      }
    }

    trace(h - f, hiLoop, true);
    trace(ho, hoLoop, false);
    final n = hiLoop.length ~/ 2;
    final indices = <int>[];
    for (var i = 0; i + 1 < m; i++) {
      indices.addAll([l0[i], l1[i], r1[i], l0[i], r1[i], r0[i]]);
    }
    // Joins and caps are convex, so each is fanned from a corner of its own rather than from the
    // line's point, which an SWT polyline puts on a pixel centre: triangles meeting on a pixel centre
    // may each leave it uncovered.
    for (var j = 1; j + 1 < m; j++) {
      for (var k = outerFrom[j]; k < outerTo[j]; k++) {
        indices.addAll([inner[j], k, k + 1]);
      }
    }
    if (cap == SWT.CAP_ROUND) {
      for (var k = endCapFrom + 1; k < endCapTo; k++) {
        indices.addAll([endCapFrom, k, k + 1]);
      }
      for (var k = startCapFrom + 1; k < n - 1; k++) {
        indices.addAll([startCapFrom, k, k + 1]);
      }
      indices.addAll([startCapFrom, n - 1, 0]);
    }
    for (var k = 0; k < n; k++) {
      final q = k + 1 == n ? 0 : k + 1;
      indices.addAll([k, n + k, n + q, k, n + q, q]);
    }
    final rgb = _argbOf(s.color) & 0xFFFFFF, core = ((s.color.a * 255).round() << 24) | rgb;
    final colors = Int32List(2 * n);
    for (var k = 0; k < 2 * n; k++) {
      colors[k] = k < n ? core : rgb;
    }
    return _PolylineStroke._(Float32List.fromList([...hiLoop, ...hoLoop]), colors,
        Uint16List.fromList(indices));
  }

  /// Whether the segments from ([ax], [ay]) to ([bx], [by]) and from ([cx], [cy]) to ([dx], [dy]) are
  /// at least [distance] apart.
  static bool _segmentsApart(double ax, double ay, double bx, double by, double cx, double cy, double dx, double dy,
      double distance) {
    double side(double px, double py, double qx, double qy, double rx, double ry) =>
        (qx - px) * (ry - py) - (qy - py) * (rx - px);
    final d1 = side(ax, ay, bx, by, cx, cy), d2 = side(ax, ay, bx, by, dx, dy);
    final d3 = side(cx, cy, dx, dy, ax, ay), d4 = side(cx, cy, dx, dy, bx, by);
    if (d1 * d2 <= 0 && d3 * d4 <= 0) return false;
    double toSegment(double px, double py, double qx, double qy, double rx, double ry) {
      final ex = rx - qx, ey = ry - qy, l2 = ex * ex + ey * ey;
      final t = l2 == 0 ? 0.0 : (((px - qx) * ex + (py - qy) * ey) / l2).clamp(0.0, 1.0);
      final x = qx + t * ex - px, y = qy + t * ey - py;
      return math.sqrt(x * x + y * y);
    }

    return toSegment(ax, ay, cx, cy, dx, dy) >= distance &&
        toSegment(bx, by, cx, cy, dx, dy) >= distance &&
        toSegment(cx, cy, ax, ay, bx, by) >= distance &&
        toSegment(dx, dy, ax, ay, bx, by) >= distance;
  }
}

/// Puts in [_run] the pieces of the shapes from [start] under the same clip that one triangle list
/// can hold ([_runCount] of them), and returns how many shapes the run spans. Paint may change along
/// the run: triangles in one list are blended in the order they are listed, as separate draws would
/// be, so the run paints what drawing the shapes one by one does.
///
/// While the run holds only rectangles, a shape that cannot join is reached past while its area is
/// known and no piece taken after it overlaps it, and is put in [_passed] ([_passedCount]) to be drawn
/// after the run: only the order of shapes that share no pixel changes. Rectangles only, because a
/// rectangle draws the same pixels in a run as on its own; a line or polygon in a run is antialiased
/// by the run's own edges, so one that would have been drawn on its own must stay so.
int _trianglesFrom(List<Shape> shapes, int start, double px) {
  // Kept, and read before the next run is made: no run is made while another is being drawn.
  var count = 0, passed = 0, vertices = 0, end = start + 1, passedAtEnd = 0;
  var rectangles = true;
  final clip = _runClip(shapes[start]);
  for (var i = start; i < shapes.length; i++) {
    final s = shapes[i];
    if (!s.clipsBody) break;
    var piece = _mayJoinRun(s) ? _pieceOf(s, px) : null;
    if (piece != null && _runClip(s) != clip) piece = null;
    if (piece == null && s.clipRect != clip && clip != null) break;
    // Taken now it would paint before a shape it overlaps: the run ends, and it may start the next.
    if (piece != null && passed > 0 && (piece is! RectShape || !_clearOfPassed(s, passed))) break;
    if (piece != null) {
      if (vertices + piece.vertices > _TriangleList.maxVertices) break;
      vertices += piece.vertices;
      if (count == _run.length) _run = List.filled(count * 2, null)..setRange(0, count, _run);
      _run[count++] = piece;
      end = i + 1;
      passedAtEnd = passed;
      if (piece is! RectShape) rectangles = false;
      continue;
    }
    if (!rectangles || passed == _maxPassed) break;
    var area = s.paintedBounds;
    if (area == null) break;
    final own = s.clipRect;
    if (own != null) area = area.intersect(own);
    _passed[passed] = s;
    _passedAreas[passed++] = area;
  }
  _runCount = count;
  // Shapes passed after the last piece were not reached past: they draw as themselves.
  _passedCount = passedAtEnd;
  return end - start;
}

/// The clip [s] has to be drawn under: none for an on-grid rectangle wholly inside its clip, which then
/// clips away nothing; [_grid] has been read for the run's canvas.
Rect? _runClip(Shape s) {
  final clip = s.clipRect;
  if (clip == null || s is! RectShape || !s.joinsRun) return clip;
  return _containsRect(clip, s.exactArea) ? null : clip;
}

/// Pieces in [_run], and shapes in [_passed], of the last [_trianglesFrom].
int _runCount = 0, _passedCount = 0;

/// How many shapes a run reaches past; each piece after them is checked against all of them.
const int _maxPassed = 32;

List<Shape?> _passed = List.filled(_maxPassed, null);

/// The areas of the shapes in [_passed], read once each as they are passed.
final List<Rect?> _passedAreas = List.filled(_maxPassed, null);

/// Whether [s] shares no pixel with the first [passed] shapes in [_passed].
bool _clearOfPassed(Shape s, int passed) {
  final area = s.paintedBounds;
  if (area == null) return false;
  for (var k = 0; k < passed; k++) {
    if (_passedAreas[k]!.overlaps(area)) return false;
  }
  return true;
}

List<_Piece?> _run = List.filled(64, null);

/// How [c]'s coordinates land on device pixels, read for the run being drawn.
final _DeviceGrid _grid = _DeviceGrid();

class _DeviceGrid {
  /// Device pixels per canvas unit, or 0 when the canvas is rotated, skewed or scaled unevenly.
  double scale = 0;
  double dx = 0, dy = 0;

  void read(ui.Canvas c) {
    final m = c.getTransform();
    final ratio = _RenderHints.devicePixelRatio;
    final aligned = m[1] == 0 && m[4] == 0 && m[3] == 0 && m[7] == 0 && m[15] == 1 && m[0] == m[5] && m[0] > 0;
    scale = aligned ? m[0] * ratio : 0;
    dx = m[12] * ratio;
    dy = m[13] * ratio;
  }

  bool onX(double x) => _whole(x * scale + dx);
  bool onY(double y) => _whole(y * scale + dy);

  static bool _whole(double v) => (v - v.roundToDouble()).abs() < 1e-6;
}

/// The box around [points] (x, y pairs), grown by [margin]; null for none.
Rect? _pointsBounds(List<num> points, double margin) {
  if (points.length < 2) return null;
  var l = points[0].toDouble(), t = points[1].toDouble(), r = l, b = t;
  for (var i = 2; i + 1 < points.length; i += 2) {
    final x = points[i].toDouble(), y = points[i + 1].toDouble();
    l = math.min(l, x);
    r = math.max(r, x);
    t = math.min(t, y);
    b = math.max(b, y);
  }
  return Rect.fromLTRB(l - margin, t - margin, r + margin, b + margin);
}

/// One device pixel in [c]'s current coordinates.
double _devicePixel(ui.Canvas c) {
  final m = c.getTransform();
  // On screen the view's pixel ratio is applied after the scene is recorded, out of the transform.
  final scale = math.sqrt(m[0] * m[0] + m[1] * m[1]) * _RenderHints.devicePixelRatio;
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

  /// What the canvas transform is scaled by to reach device pixels: 1 where it already maps to them
  /// (an off-screen render), the view's ratio on screen.
  static double devicePixelRatio = 1;
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

  TextPainter? _painter;
  int _laidOutIn = -1;

  /// The layout, looked up again only once a system font change has cleared the cache.
  TextPainter get painter {
    final held = _painter;
    if (held != null && _laidOutIn == _TextLayouts.generation) return held;
    _laidOutIn = _TextLayouts.generation;
    return _painter = _TextLayouts.of(text, style);
  }

  TextShape translated(Offset offset, Rect clipArea) => TextShape(text, off + offset, style, clipArea)
    .._painter = _painter
    .._laidOutIn = _laidOutIn;

  @override
  bool get clipsBody => true;

  @override
  void draw(ui.Canvas c) => _drawClipped(c, this);

  /// Each layout's text, recorded once at the origin: recording a paragraph converts every glyph run
  /// it has, frame after frame, where drawing a picture of it records one reference. Not on the web,
  /// whose renderer spends more drawing a picture than recording the paragraph saves.
  static final Expando<ui.Picture> _recorded = Expando();

  @override
  void paintBody(ui.Canvas c) {
    final tp = painter;
    if (kIsWeb) {
      tp.paint(c, off);
      return;
    }
    final picture = _recorded[tp] ??= () {
      final recorder = ui.PictureRecorder();
      tp.paint(ui.Canvas(recorder), Offset.zero);
      return recorder.endRecording();
    }();
    c.save();
    c.translate(off.dx, off.dy);
    c.drawPicture(picture);
    c.restore();
  }

  @override
  late final Rect? paintedBounds = () {
    final tp = painter;
    return Rect.fromLTWH(off.dx, off.dy, tp.width, tp.height).inflate(tp.height);
  }();

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
  static final _LayoutKey _probe = _LayoutKey();
  static bool _watchingFonts = false;

  static TextPainter of(String text, TextStyle style) {
    if (!_watchingFonts) _watchingFonts = FontUtils.listenToSystemFonts(_clear);
    final key = _probe.set(text, style);
    final held = _layouts[key];
    if (held != null) {
      held.used = true;
      return held.painter;
    }
    final painter = TextPainter(
      text: TextSpan(text: text, style: style),
      textDirection: TextDirection.ltr,
    )..layout();
    _layouts[key.held()] = _Layout(painter);
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

  /// Counts the times the cache was cleared, so a shape knows its held layout is stale.
  static int generation = 0;

  static void _clear() {
    _layouts.clear();
    generation++;
  }
}

class _Layout {
  _Layout(this.painter);
  final TextPainter painter;
  bool used = false;
}

/// A text and its style, hashed once: a style's own hash goes over every field it has. Looked up with
/// one reused key, copied only when kept.
final class _LayoutKey {
  static final Expando<int> _styleHashes = Expando();

  String text = '';
  TextStyle style = const TextStyle();
  @override
  int hashCode = 0;

  _LayoutKey set(String text, TextStyle style) {
    this.text = text;
    this.style = style;
    hashCode = Object.hash(text, _styleHashes[style] ??= style.hashCode);
    return this;
  }

  _LayoutKey held() => _LayoutKey()
    ..text = text
    ..style = style
    ..hashCode = hashCode;

  @override
  bool operator ==(Object other) =>
      other is _LayoutKey && other.hashCode == hashCode && other.text == text &&
      (identical(other.style, style) || other.style == style);
}

class LineShape extends Shape implements _Piece {
  /// What every line draws with: a canvas call takes what a paint holds when it is made, and a line
  /// sets everything it draws with, so one paint serves them all instead of one per line.
  static final Paint _linePaint = Paint();

  late final double _length = (p2 - p1).distance;

  /// The ends as drawn, moved by [_strokeShift] once rather than per draw.
  late final Offset _from = _aligned(p1), _to = _aligned(p2);

  Offset _aligned(Offset p) {
    final shift = _strokeShift(strokeWidth, true);
    return identical(shift, Offset.zero) ? p : p + shift;
  }

  LineShape(this.p1, this.p2, this.color, this.strokeWidth, this.lineCap,
      this.lineJoin, [this.clipRect]);

  // A cap reaches half the width past an end, and the half-pixel shift of an odd width one more.
  @override
  late final Rect? paintedBounds = Rect.fromPoints(p1, p2).inflate(math.max(strokeWidth, 1) + 1);
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
  int get vertices => _TriangleList.sdfLines ? _TriangleList.sdfLineVertices : _TriangleList.verticesFor(4);
  @override
  int get indices => _TriangleList.sdfLines ? _TriangleList.sdfLineIndices : _TriangleList.indicesFor(4);
  @override
  void addTo(_TriangleList list) {
    if (_fillsGridQuad()) {
      list.addQuad(_quad[0], _quad[1], _quad[2], _quad[3], _argbOf(color));
      return;
    }
    if (_TriangleList.sdfLines) {
      list.addSdfLine(_from.dx, _from.dy, _to.dx, _to.dy, strokeWidth, lineCap, color);
      return;
    }
    list.addSegment(p1.dx, p1.dy, p2.dx, p2.dy, strokeWidth, lineCap, color);
  }

  /// The rectangle [_fillsGridQuad] found: left, top, right and bottom.
  static final Float64List _quad = Float64List(4);

  /// Whether this is a horizontal or vertical line whose edges land on whole device pixels, which it
  /// covers wholly, as the stroke drawn on its own does: one quad, with no ring to antialias it, put
  /// in [_quad]. [_grid] has been read for the run's canvas.
  bool _fillsGridQuad() {
    if (strokeWidth > 0 && (p1.dx == p2.dx || p1.dy == p2.dy) && _grid.scale != 0) {
      final half = strokeWidth / 2;
      final cap = lineCap == SWT.CAP_SQUARE ? half : 0.0;
      final width = strokeWidth.round();
      final shift = width.isEven && width != 0 ? 0.0 : 0.5;
      final double l, t, r, b;
      if (p1.dy == p2.dy) {
        l = math.min(p1.dx, p2.dx) - cap + shift;
        r = math.max(p1.dx, p2.dx) + cap + shift;
        t = p1.dy + shift - half;
        b = p1.dy + shift + half;
      } else {
        l = p1.dx + shift - half;
        r = p1.dx + shift + half;
        t = math.min(p1.dy, p2.dy) - cap + shift;
        b = math.max(p1.dy, p2.dy) + cap + shift;
      }
      if (r > l && b > t && _grid.onX(l) && _grid.onX(r) && _grid.onY(t) && _grid.onY(b)) {
        _quad
          ..[0] = l
          ..[1] = t
          ..[2] = r
          ..[3] = b;
        return true;
      }
    }
    return false;
  }

  @override
  String toString() => 'Line $p1 → $p2${clipRect != null ? " [clipped]" : ""}';

  @override
  String describe() => 'Line ${_do(p1)}->${_do(p2)} ${_dc(color)} w=${_dd(strokeWidth)} cap=$lineCap join=$lineJoin${_dk(clipRect)}';
}

class OvalShape extends Shape {
  @override
  late final Rect? paintedBounds =
      Rect.fromPoints(rect.topLeft, rect.bottomRight).inflate(isFilled ? 1 : math.max(strokeWidth, 1) + 1);

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
  String describe() => '${isFilled ? "Fill" : "Draw"}Oval ${_dr(rect)} ${_dc(color)} w=${_dd(strokeWidth)}${_dk(clipRect)}';
}

abstract interface class GCGround {
  VColor? get swtGround;

  Color? get renderedGround;
}

class RectShape extends Shape implements _Piece {
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

  /// Whether a triangle run draws it exactly as [paintBody] does: every edge on a whole device pixel,
  /// where an antialiased edge covers whole pixels. A hairline is one device pixel wide only at a
  /// device scale of 1, where it is drawn as the renderer antialiases it: each corner pixel covered
  /// at half strength once by each of the two edges that meet there.
  bool get joinsRun {
    if (!mayJoinRun) return false;
    final scale = _grid.scale;
    if (scale == 0) return false;
    final r = _drawn;
    final l = math.min(r.left, r.right), t = math.min(r.top, r.bottom);
    final rr = math.max(r.left, r.right), b = math.max(r.top, r.bottom);
    if (isFilled) return _grid.onX(l) && _grid.onY(t) && _grid.onX(rr) && _grid.onY(b);
    if (_hairline) {
      return scale == 1 && _RenderHints.antiAlias && _grid.onX(l - 0.5) && _grid.onY(t - 0.5);
    }
    // The outer corner on the grid, and the stroke and the box whole numbers of device pixels, put
    // every other edge on it too.
    final half = strokeWidth / 2;
    return _grid.onX(l - half) &&
        _grid.onY(t - half) &&
        _DeviceGrid._whole(strokeWidth * scale) &&
        _DeviceGrid._whole((rr - l) * scale) &&
        _DeviceGrid._whole((b - t) * scale);
  }

  /// What [joinsRun] asks of the rectangle alone, without the device grid: a fill, or a mitred
  /// outline round a rectangle with area.
  bool get mayJoinRun =>
      isFilled ||
      (lineJoin != SWT.JOIN_ROUND && lineJoin != SWT.JOIN_BEVEL && rect.width != 0 && rect.height != 0);

  bool get _hairline => !isFilled && strokeWidth <= 0;

  /// The pixels it paints while [joinsRun]: its edges then lie on whole device pixels, so nothing is
  /// antialiased past them.
  Rect get exactArea {
    final r = _drawn;
    final l = math.min(r.left, r.right), t = math.min(r.top, r.bottom);
    final rr = math.max(r.left, r.right), b = math.max(r.top, r.bottom);
    if (isFilled) return Rect.fromLTRB(l, t, rr, b);
    final half = _hairline ? 0.5 : strokeWidth / 2;
    return Rect.fromLTRB(l - half, t - half, rr + half, b + half);
  }

  // As a piece of a triangle run: the fill, or the outline as bands that do not overlap.
  int get _quads => isFilled ? 1 : (_hairline ? 12 : 4);
  @override
  int get vertices => !isFilled && !_hairline ? _TriangleList.ringVertices : _quads * _TriangleList.quadVertices;
  @override
  int get indices => !isFilled && !_hairline ? _TriangleList.ringIndices : _quads * _TriangleList.quadIndices;
  @override
  void addTo(_TriangleList list) {
    final argb = _argbOf(color);
    final r = _drawn;
    final l = math.min(r.left, r.right), t = math.min(r.top, r.bottom);
    final rr = math.max(r.left, r.right), b = math.max(r.top, r.bottom);
    if (isFilled) {
      if (rr > l && b > t) list.addQuad(l, t, rr, b, argb);
      return;
    }
    final half = _hairline ? 0.5 : strokeWidth / 2;
    final ol = l - half, ot = t - half, or = rr + half, ob = b + half;
    if (_hairline) {
      final corner = ((argb >>> 24) >> 1) << 24 | (argb & 0xFFFFFF);
      for (var pass = 0; pass < 2; pass++) {
        list.addQuad(ol, ot, ol + 1, ot + 1, corner);
        list.addQuad(or - 1, ot, or, ot + 1, corner);
        list.addQuad(ol, ob - 1, ol + 1, ob, corner);
        list.addQuad(or - 1, ob - 1, or, ob, corner);
      }
      if (or - ol > 2) {
        list.addQuad(ol + 1, ot, or - 1, ot + 1, argb);
        list.addQuad(ol + 1, ob - 1, or - 1, ob, argb);
      }
      if (ob - ot > 2) {
        list.addQuad(ol, ot + 1, ol + 1, ob - 1, argb);
        list.addQuad(or - 1, ot + 1, or, ob - 1, argb);
      }
      return;
    }
    final il = l + half, it = t + half, ir = rr - half, ib = b - half;
    if (ir <= il || ib <= it) {
      list.addQuad(ol, ot, or, ob, argb);
      return;
    }
    list.addRing(ol, ot, or, ob, il, it, ir, ib, argb);
  }

  @override
  String toString() => '${isFilled ? "Fill" : ""}Rect $rect';

  @override
  String describe() => '${isFilled ? "Fill" : "Draw"}Rect ${_dr(rect)} ${_dc(color)} w=${_dd(strokeWidth)} cap=$lineCap join=$lineJoin${_dk(clipRect)}';
}

class GradientRectShape extends Shape implements _Piece {
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

  /// The shaders made, by what they are made from: a gradient repainted frame after frame is the
  /// same shader, and making one is a native object each time.
  static final Map<_GradientKey, ui.Shader> _shaders = {};
  static const int _shadersCapacity = 256;
  static final _GradientKey _shaderProbe = _GradientKey();

  ui.Shader get _shader {
    final key = _shaderProbe.set(rect, fromColor, toColor, begin, end);
    final held = _shaders[key];
    if (held != null) return held;
    if (_shaders.length >= _shadersCapacity) _shaders.clear();
    return _shaders[key.held()] =
        LinearGradient(begin: begin, end: end, colors: [fromColor, toColor]).createShader(rect);
  }

  @override
  void paintBody(ui.Canvas c) {
    // Impeller draws a rect filled by a shader as an SDF blended with the shader in two offscreen
    // passes; the same pixels drawn as a clipped paint take none. The clip is the rect sorted, as a
    // rect is filled: a negative width or height swaps the gradient's colours, it does not empty it.
    c.save();
    c.clipRect(Rect.fromPoints(rect.topLeft, rect.bottomRight));
    c.drawPaint(_paint()..shader = _shader);
    c.restore();
  }

  /// Whether it runs top to bottom or left to right, as fillGradientRectangle's do.
  bool get _alongAnAxis =>
      (begin == Alignment.topCenter && end == Alignment.bottomCenter) ||
      (begin == Alignment.centerLeft && end == Alignment.centerRight);

  bool get _vertical => begin == Alignment.topCenter;

  /// Whether its edges land on whole device pixels: a run's quad then covers what its clip does.
  /// [_grid] has been read for the run's canvas.
  bool get _onGrid {
    final r = Rect.fromPoints(rect.topLeft, rect.bottomRight);
    return _grid.scale != 0 && !r.isEmpty && _grid.onX(r.left) && _grid.onX(r.right) && _grid.onY(r.top) &&
        _grid.onY(r.bottom);
  }

  // As a piece of a triangle run: one quad whose colours run from edge to edge. A gradient between two
  // colours along one axis is linear in it, which the quad's two triangles reproduce at every pixel.
  @override
  int get vertices => _TriangleList.quadVertices;
  @override
  int get indices => _TriangleList.quadIndices;
  @override
  void addTo(_TriangleList list) {
    final r = Rect.fromPoints(rect.topLeft, rect.bottomRight);
    // A negative width or height swaps which edge the gradient starts at.
    final reversed = _vertical ? rect.height < 0 : rect.width < 0;
    final first = _argbOf(reversed ? toColor : fromColor), last = _argbOf(reversed ? fromColor : toColor);
    list.addGradientQuad(r.left, r.top, r.right, r.bottom, first, last, _vertical);
  }

  @override
  String toString() => 'GradientRect $rect';

  @override
  String describe() => 'GradientRect ${_dr(rect)} ${_dc(fromColor)}->${_dc(toColor)} ${_da(begin)}->${_da(end)}${_dk(clipRect)}';
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
  @override
  late final Rect? paintedBounds = _pointsBounds(points, math.max(strokeWidth, 1) + 1);

  SegmentsShape(this.points, this.color, this.strokeWidth, this.lineCap, [this.clipRect, this.fixed = false]);
  final Float32List points;

  /// Whether [points] never changes once made, as the dashes a GC works out do: their triangles are
  /// then kept with the list itself rather than looked up by its coordinates.
  final bool fixed;
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
      for (final triangles in _SegmentTriangles.of(points, strokeWidth, lineCap, color, _devicePixel(c), fixed)) {
        c.drawVertices(triangles, BlendMode.dst, _runPaint);
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
    return 'Segments ${ends.join(' ')} ${_dc(color)} w=${_dd(strokeWidth)} cap=$lineCap${_dk(clipRect)}';
  }
}

/// [_SdfLineAtlas]'s texture, as it is made where Impeller draws.
@visibleForTesting
({Uint8List pixels, int width, int height}) sdfLineAtlas() =>
    (pixels: _SdfLineAtlas.pixels(), width: _SdfLineAtlas.width, height: _SdfLineAtlas.height);

/// The triangles a run gives the line from ([x1], [y1]) to ([x2], [y2]) where Impeller draws, at
/// [px] canvas units a device pixel; null for a line a run leaves to be drawn on its own there.
@visibleForTesting
({Float32List positions, Float32List textures, Int32List colors, Uint16List indices})? sdfLineMesh(
    double x1, double y1, double x2, double y2, double strokeWidth, int lineCap, Color color, double px) {
  final length = math.sqrt((x2 - x1) * (x2 - x1) + (y2 - y1) * (y2 - y1));
  if (!_TriangleList.sdfFits(strokeWidth, length, lineCap, px)) return null;
  final list = _TriangleList(_TriangleList.sdfLineVertices, _TriangleList.sdfLineIndices, px)
    ..addSdfLine(x1, y1, x2, y2, strokeWidth, lineCap, color);
  final v = list._v;
  return (
    positions: Float32List.fromList(list.positions.sublist(0, v * 2)),
    textures: Float32List.fromList(list._textures!.sublist(0, v * 2)),
    colors: Int32List.fromList(list.colors.sublist(0, v)),
    indices: Uint16List.fromList(list.indices!.sublist(0, list._n)),
  );
}

/// What Impeller's SDF shader lays on a line's pixels, as a texture: Impeller draws a line on its own
/// as a rectangle at least a device pixel each way, whose pixels take
/// `gamma(1 - smoothstep(-0.5, 0.5, sdf))`, `sdf` being the pixel centre's signed distance to the
/// rectangle in device pixels and `gamma` leaning towards the line colour's luma. A line in a run gives
/// each vertex where it lies relative to the rectangle's ends (x) and sides (y) as texture
/// coordinates, so every pixel samples the same value.
///
/// Per luma step, a tile of distances and one for hairlines, stacked down the texture. Along, a tile
/// holds distances past an end from -0.5 to 0.5 device pixels, and the texture's own edges clamp
/// any further, which is what the shader gives there: a line's coordinates run from end to middle.
/// Across, on a multisampled canvas a pixel only partly inside a triangle is shaded at its centre,
/// outside the triangle, so its coordinates run on past what the triangle's vertices hold, by up to
/// [reach] device pixels: each tile holds that far past what any vertex gives. Where a distance
/// folds, at a line's middle, it runs on the wrong way past it, so a fold is only where the line is
/// opaque that far round it; a line narrower than that is drawn on its own, but for a hairline.
class _SdfLineAtlas {
  static const int lumas = 64, steps = 16, width = steps + 1;

  /// How far a multisampled pixel's samples lie from its centre, in device pixels, at most: under the
  /// standard 4x pattern, 0.375 each way.
  static const double reach = 0.55;

  /// A tile of distances past a side, from -0.5 to 0.5 device pixels and [gutter] texels further
  /// either way: a line's vertices hold at most [reach] past those, and a pixel reaches that far again.
  static const int gutter = 18, tile = steps + 1 + 2 * gutter;

  /// A tile of positions across a line half a device pixel either side of its middle, which every
  /// line thinner than a pixel is, [hairlineHalf] texels either way: its vertices lie [reach] past half
  /// a pixel outside it, and a pixel reaches that far again.
  static const int hairlineHalf = 34, hairlineTile = 2 * hairlineHalf + 1;

  static const int height = (tile + hairlineTile) * lumas;

  /// What a run with SDF lines in it is drawn with, once the texture is made; null until then, and
  /// where the canvas is not Impeller's.
  static Paint? paint;
  static bool _started = false;

  /// Makes the texture, where Impeller draws. Its lines are drawn as before until it is made.
  static void prepare() {
    if (_started || kIsWeb || !ui.ImageFilter.isShaderFilterSupported) return;
    _started = true;
    ui.decodeImageFromPixels(pixels(), width, height, ui.PixelFormat.rgba8888, (image) {
      paint = Paint()
        ..shader = ImageShader(image, TileMode.clamp, TileMode.clamp, Matrix4.identity().storage,
            filterQuality: FilterQuality.low);
    });
  }

  /// The texture's premultiplied white texels, luma step after luma step.
  static Uint8List pixels() {
    final out = Uint8List(width * height * 4);
    var o = 0;
    void put(double a) {
      out[o] = out[o + 1] = out[o + 2] = out[o + 3] = (a * 255).round();
      o += 4;
    }

    for (var l = 0; l < lumas; l++) {
      final luma = l / (lumas - 1);
      for (var j = 0; j < tile; j++) {
        for (var i = 0; i < width; i++) {
          put(coverage(i / steps - 0.5, (j - gutter) / steps - 0.5, luma));
        }
      }
      for (var j = 0; j < hairlineTile; j++) {
        for (var i = 0; i < width; i++) {
          put(coverage(i / steps - 0.5, ((j - hairlineHalf) / steps).abs() - 0.5, luma));
        }
      }
    }
    return out;
  }

  /// The shader's alpha at [dx] past a rectangle's end and [dy] past its side, in device pixels.
  static double coverage(double dx, double dy, double luma) {
    final ox = math.max(dx, 0.0), oy = math.max(dy, 0.0);
    final sdf = math.sqrt(ox * ox + oy * oy) + math.min(math.max(dx, dy), 0.0);
    final t = (sdf + 0.5).clamp(0.0, 1.0);
    final c = 1 - t * t * (3 - 2 * t);
    return (1 - luma) * (1 - math.sqrt(1 - c)) + luma * math.sqrt(c);
  }

  static int _luma(int rgb) {
    final luma = (0.2126 * ((rgb >> 16) & 0xFF) + 0.7152 * ((rgb >> 8) & 0xFF) + 0.0722 * (rgb & 0xFF)) / 255;
    return (luma * (lumas - 1)).round();
  }

  /// The texture coordinate of a distance of -0.5 past a side, in the tile for [rgb]'s luma.
  static double rowOf(int rgb) => _luma(rgb) * (tile + hairlineTile) + gutter + 0.5;

  /// The texture coordinate of a hairline's middle, in the hairline tile for [rgb]'s luma.
  static double hairlineRowOf(int rgb) => _luma(rgb) * (tile + hairlineTile) + tile + hairlineHalf + 0.5;
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

  /// Whether the canvas antialiases by multisampling, as Impeller does: a filled path is then its
  /// triangles with no fringe of its own, and a fill in a run is the same triangles.
  static bool get multisampled => debugMultisampled ?? (_multisampled ??= !kIsWeb && ui.ImageFilter.isShaderFilterSupported);
  static bool? _multisampled;

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

  /// Whether a segment stroked [strokeWidth] wide is a hairline here: no wider than one device pixel
  /// [px] and antialiased, so its core has no width and it goes as [_hairline] does.
  static bool isHairline(double strokeWidth, double px) =>
      !strip && _RenderHints.antiAlias && (strokeWidth > 0 ? strokeWidth : px) <= px;

  /// Vertices and indices a segment stroked [strokeWidth] wide takes at device pixel [px].
  static int segmentVertices(double strokeWidth, double px) => isHairline(strokeWidth, px) ? 6 : verticesFor(4);
  static int segmentIndices(double strokeWidth, double px) => isHairline(strokeWidth, px) ? 18 : indicesFor(4);

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
    final rgb = _argbOf(color) & 0xFFFFFF;
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
    if (hi == 0 && !strip) {
      _hairline(mx - iax, my - iay, mx + iax, my + iay, mx - oax - obx, my - oay - oby, mx + oax - obx,
          my + oay - oby, mx + oax + obx, my + oay + oby, mx - oax + obx, my - oay + oby, core, rgb);
      return;
    }
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

  /// [_shape] for a segment whose core has no width: its inner corners meet in pairs at ([ax], [ay])
  /// and ([bx], [by]), so the core and the ring's two ends are triangles of no area, which cover no
  /// pixel. The rest of the ring is six triangles over six vertices.
  void _hairline(double ax, double ay, double bx, double by, double o0x, double o0y, double o1x, double o1y,
      double o2x, double o2y, double o3x, double o3y, int core, int edge) {
    final base = _v;
    _vertex(base, ax, ay, core);
    _vertex(base + 1, bx, by, core);
    _vertex(base + 2, o0x, o0y, edge);
    _vertex(base + 3, o1x, o1y, edge);
    _vertex(base + 4, o2x, o2y, edge);
    _vertex(base + 5, o3x, o3y, edge);
    _v = base + 6;
    final a = base, b = base + 1, o0 = base + 2, o1 = base + 3, o2 = base + 4, o3 = base + 5;
    final indices = this.indices!;
    var n = _n;
    indices[n++] = a;
    indices[n++] = o0;
    indices[n++] = o1;
    indices[n++] = a;
    indices[n++] = o1;
    indices[n++] = b;
    indices[n++] = b;
    indices[n++] = o1;
    indices[n++] = o2;
    indices[n++] = b;
    indices[n++] = o2;
    indices[n++] = o3;
    indices[n++] = b;
    indices[n++] = o3;
    indices[n++] = a;
    indices[n++] = a;
    indices[n++] = o3;
    indices[n++] = o0;
    _n = n;
  }

  /// Adds a convex fill: its [inner] outline at full coverage, and a ring out to its [outer] one
  /// fading to none.
  /// The convex polygon [corners] as a fan of triangles from its first corner, at full coverage.
  void addFan(Float32List corners, Color color) {
    final argb = ((color.a * 255).round() << 24) | (_argbOf(color) & 0xFFFFFF);
    final n = corners.length ~/ 2, base = _v, positions = this.positions, colors = this.colors;
    for (var k = 0; k < n; k++) {
      positions[(base + k) * 2] = corners[k * 2];
      positions[(base + k) * 2 + 1] = corners[k * 2 + 1];
      colors[base + k] = argb;
    }
    _v = base + n;
    final indices = this.indices!;
    for (var k = 1; k + 1 < n; k++) {
      indices[_n++] = base;
      indices[_n++] = base + k;
      indices[_n++] = base + k + 1;
    }
  }

  void addPolygon(Float32List inner, Float32List outer, Color color) {
    final rgb = _argbOf(color) & 0xFFFFFF;
    _shape(inner, outer, inner.length ~/ 2, ((color.a * 255).round() << 24) | rgb, rgb);
  }

  /// Chords per quarter turn of a curve of [radius], so that none strays from it by more than a tenth
  /// of device pixel [px]; 0 when that takes more than [maxQuarterSteps], and the shape goes alone.
  static int quarterSteps(double radius, double px) {
    final r = radius / px;
    if (r <= 0.1) return 2;
    final steps = (math.pi / 4 / math.acos(1 - 0.1 / r)).ceil();
    return steps > maxQuarterSteps ? 0 : math.max(steps, 2);
  }

  static const int maxQuarterSteps = 32;

  /// Adds triangles made elsewhere: [indices] count from this list's next vertex.
  void addMesh(Float32List positions, Int32List colors, Uint16List indices) {
    final base = _v;
    this.positions.setRange(base * 2, base * 2 + positions.length, positions);
    this.colors.setRange(base, base + colors.length, colors);
    final out = this.indices!;
    var n = _n;
    for (var i = 0; i < indices.length; i++) {
      out[n++] = base + indices[i];
    }
    _n = n;
    _v = base + colors.length;
  }

  /// Adds the rectangle from ([l], [t]) to ([r], [b]) in [argb], at full coverage with no ring.
  void addQuad(double l, double t, double r, double b, int argb) {
    final positions = this.positions, colors = this.colors;
    var v = _v;
    if (strip) {
      // Repeated first and last vertices join it to its neighbours with triangles of no area.
      _vertex(v++, l, t, argb);
      _vertex(v++, l, t, argb);
      _vertex(v++, r, t, argb);
      _vertex(v++, l, b, argb);
      _vertex(v++, r, b, argb);
      _vertex(v++, r, b, argb);
      _v = v;
      return;
    }
    positions[v * 2] = l;
    positions[v * 2 + 1] = t;
    positions[v * 2 + 2] = r;
    positions[v * 2 + 3] = t;
    positions[v * 2 + 4] = r;
    positions[v * 2 + 5] = b;
    positions[v * 2 + 6] = l;
    positions[v * 2 + 7] = b;
    colors[v] = argb;
    colors[v + 1] = argb;
    colors[v + 2] = argb;
    colors[v + 3] = argb;
    final indices = this.indices!;
    indices[_n++] = v;
    indices[_n++] = v + 1;
    indices[_n++] = v + 2;
    indices[_n++] = v;
    indices[_n++] = v + 2;
    indices[_n++] = v + 3;
    _v = v + 4;
  }

  void _vertex(int v, double x, double y, int argb) {
    positions[v * 2] = x;
    positions[v * 2 + 1] = y;
    colors[v] = argb;
  }

  static int get quadVertices => strip ? 6 : 4;
  static int get quadIndices => strip ? 0 : 6;

  /// What [addRing] takes: as a strip, round the ring and back to its start, joined to its
  /// neighbours by repeated vertices.
  static int get ringVertices => strip ? 12 : 8;
  static int get ringIndices => strip ? 0 : 24;

  /// Adds the band between the rectangle from ([ol], [ot]) to ([or], [ob]) and the one inside it
  /// from ([il], [it]) to ([ir], [ib]), in [argb], at full coverage with no ring to antialias it:
  /// two triangles per side, which meet on the corners' diagonals.
  void addRing(double ol, double ot, double or, double ob, double il, double it, double ir, double ib, int argb) {
    var v = _v;
    if (strip) {
      _vertex(v++, ol, ot, argb);
      _vertex(v++, ol, ot, argb);
      _vertex(v++, il, it, argb);
      _vertex(v++, or, ot, argb);
      _vertex(v++, ir, it, argb);
      _vertex(v++, or, ob, argb);
      _vertex(v++, ir, ib, argb);
      _vertex(v++, ol, ob, argb);
      _vertex(v++, il, ib, argb);
      _vertex(v++, ol, ot, argb);
      _vertex(v++, il, it, argb);
      _vertex(v++, il, it, argb);
      _v = v;
      return;
    }
    _vertex(v, ol, ot, argb);
    _vertex(v + 1, or, ot, argb);
    _vertex(v + 2, or, ob, argb);
    _vertex(v + 3, ol, ob, argb);
    _vertex(v + 4, il, it, argb);
    _vertex(v + 5, ir, it, argb);
    _vertex(v + 6, ir, ib, argb);
    _vertex(v + 7, il, ib, argb);
    final indices = this.indices!;
    var n = _n;
    for (var k = 0; k < 4; k++) {
      final q = k == 3 ? 0 : k + 1;
      indices[n++] = v + k;
      indices[n++] = v + q;
      indices[n++] = v + 4 + q;
      indices[n++] = v + k;
      indices[n++] = v + 4 + q;
      indices[n++] = v + 4 + k;
    }
    _n = n;
    _v = v + 8;
  }

  /// Adds the rectangle from ([l], [t]) to ([r], [b]) at full coverage, [first] along its top or left
  /// edge and [last] along the opposite one, as [vertical] says.
  void addGradientQuad(double l, double t, double r, double b, int first, int last, bool vertical) {
    final tr = vertical ? first : last, bl = vertical ? last : first;
    var v = _v;
    if (strip) {
      // Repeated first and last vertices join it to its neighbours with triangles of no area.
      _vertex(v++, l, t, first);
      _vertex(v++, l, t, first);
      _vertex(v++, r, t, tr);
      _vertex(v++, l, b, bl);
      _vertex(v++, r, b, last);
      _vertex(v++, r, b, last);
      _v = v;
      return;
    }
    _vertex(v, l, t, first);
    _vertex(v + 1, r, t, tr);
    _vertex(v + 2, r, b, last);
    _vertex(v + 3, l, b, bl);
    final indices = this.indices!;
    indices[_n++] = v;
    indices[_n++] = v + 1;
    indices[_n++] = v + 2;
    indices[_n++] = v;
    indices[_n++] = v + 2;
    indices[_n++] = v + 3;
    _v = v + 4;
  }

  ui.Vertices build() {
    if (strip) {
      return ui.Vertices.raw(ui.VertexMode.triangleStrip, Float32List.sublistView(positions, 0, _v * 2),
          colors: Int32List.sublistView(colors, 0, _v));
    }
    final textures = _textures;
    if (textures != null) _untextured(textures, _v);
    return ui.Vertices.raw(ui.VertexMode.triangles, Float32List.sublistView(positions, 0, _v * 2),
        textureCoordinates: textures == null ? null : Float32List.sublistView(textures, 0, _v * 2),
        colors: Int32List.sublistView(colors, 0, _v), indices: Uint16List.sublistView(indices!, 0, _n));
  }

  /// Whether a line in a run goes as [addSdfLine]: where Impeller draws, once [_SdfLineAtlas] is made,
  /// under a canvas scaled evenly. [_grid] has been read for the run's canvas.
  static bool get sdfLines => _SdfLineAtlas.paint != null && _grid.scale != 0;
  static const int sdfLineVertices = 12, sdfLineIndices = 36;

  static Float32List _texturesBuffer = Float32List(0);

  /// Texture coordinates, once a line has been shaded from [_SdfLineAtlas]: the list is then drawn
  /// with its paint, every other vertex sampling a texel at full coverage.
  Float32List? _textures;
  int _t = 0;

  bool get textured => _textures != null;

  void _untextured(Float32List textures, int to) {
    for (var v = _t; v < to; v++) {
      textures[v * 2] = 0.5;
      textures[v * 2 + 1] = 0.5;
    }
    _t = to;
  }

  /// Whether [addSdfLine] paints what Impeller does for a line [strokeWidth] wide and [length] long:
  /// one thinner than a device pixel, or at least two wide, and at least two long: where a distance
  /// folds, the line is opaque as far round as a multisampled pixel reaches.
  static bool sdfFits(double strokeWidth, double length, int lineCap, double px) {
    final e = px / 2, w = strokeWidth / 2, l = length / 2 + (lineCap == SWT.CAP_FLAT ? 0 : w);
    return (w <= e || w >= px) && l >= px;
  }

  /// The line from ([x1], [y1]) to ([x2], [y2]) as Impeller draws one on its own, for one [sdfFits]
  /// takes: a rectangle at least a device pixel each way, fainter by what that adds to a thinner one,
  /// its pixels shaded from [_SdfLineAtlas]. Drawn [_SdfLineAtlas.reach] past where it fades out, so
  /// a multisampled pixel has every sample covered. Across, its edges and two points that far inside
  /// where it fades in, the distance holding still between those, or its middle, where the line is too
  /// narrow for those, the distance folding there; across a hairline, its edges.
  void addSdfLine(double x1, double y1, double x2, double y2, double strokeWidth, int lineCap, Color color) {
    final length = math.sqrt((x2 - x1) * (x2 - x1) + (y2 - y1) * (y2 - y1));
    final ux = length > 0 ? (x2 - x1) / length : 1.0, uy = length > 0 ? (y2 - y1) / length : 0.0;
    final px = this.px, e = px / 2, pad = _SdfLineAtlas.reach * px;
    final w = strokeWidth / 2, l = length / 2 + (lineCap == SWT.CAP_FLAT ? 0 : w);
    final hw = math.max(w, e), hl = math.max(l, e);
    var alpha = color.a;
    if (strokeWidth != 0 && (hw != w || hl != l)) alpha *= w * l / (hw * hl);
    final rgb = _argbOf(color) & 0xFFFFFF;
    final argb = ((alpha * 255).round() << 24) | rgb;
    final along = _sdfAlong, alongTexels = _sdfAlongTexels, across = _sdfAcross, acrossTexels = _sdfAcrossTexels;
    // Along: its ends, past where it fades out, and its middle, the texture's edges clamping between.
    const steps = _SdfLineAtlas.steps;
    const columns = 3;
    along[0] = -hl - e - pad;
    along[1] = 0;
    along[2] = hl + e + pad;
    alongTexels[0] = alongTexels[2] = 0.5 + (1 + _SdfLineAtlas.reach) * steps;
    alongTexels[1] = 0.5 - (hl / px - 0.5) * steps;
    final int rows;
    if (hw == e) {
      rows = 2;
      final hairline = _SdfLineAtlas.hairlineRowOf(rgb), out = (e + e + pad) / px * steps;
      across[0] = -e - e - pad;
      across[1] = e + e + pad;
      acrossTexels[0] = hairline - out;
      acrossTexels[1] = hairline + out;
    } else {
      rows = _sdfStations(across, acrossTexels, hw, px, _SdfLineAtlas.rowOf(rgb));
    }
    final mx = (x1 + x2) / 2, my = (y1 + y2) / 2;
    final positions = this.positions, colors = this.colors;
    final textures = _textures ??= _texturesBuffer.length >= positions.length
        ? _texturesBuffer
        : (_texturesBuffer = Float32List(positions.length));
    final base = _v;
    _untextured(textures, base);
    var v = base;
    for (var i = 0; i < columns; i++) {
      final ax = mx + ux * along[i], ay = my + uy * along[i], tx = alongTexels[i];
      for (var j = 0; j < rows; j++) {
        final a = across[j];
        positions[v * 2] = ax - uy * a;
        positions[v * 2 + 1] = ay + ux * a;
        textures[v * 2] = tx;
        textures[v * 2 + 1] = acrossTexels[j];
        colors[v] = argb;
        v++;
      }
    }
    _v = _t = v;
    final indices = this.indices!;
    for (var i = 0; i + 1 < columns; i++) {
      for (var j = 0; j + 1 < rows; j++) {
        final a = base + i * rows + j;
        indices[_n++] = a;
        indices[_n++] = a + rows;
        indices[_n++] = a + rows + 1;
        indices[_n++] = a;
        indices[_n++] = a + rows + 1;
        indices[_n++] = a + 1;
      }
    }
  }

  /// The offsets from a line's middle, in one direction, at which a rectangle reaching [half] from it
  /// is cut, and the texture coordinate there in a tile whose distance of -0.5 lies at [origin]: its
  /// edges [_SdfLineAtlas.reach] past where it fades out, and that far inside where it fades in, or
  /// its middle where those meet. How many.
  static int _sdfStations(Float64List at, Float64List texels, double half, double px, double origin) {
    const steps = _SdfLineAtlas.steps, reach = _SdfLineAtlas.reach;
    final e = px / 2, pad = reach * px, deep = half - e - pad;
    final edge = origin + (1 + reach) * steps;
    at[0] = -half - e - pad;
    texels[0] = edge;
    if (deep <= 0) {
      at[1] = 0;
      texels[1] = origin - (half / px - 0.5) * steps;
      at[2] = half + e + pad;
      texels[2] = edge;
      return 3;
    }
    at[1] = -deep;
    at[2] = deep;
    texels[1] = texels[2] = origin - reach * steps;
    at[3] = half + e + pad;
    texels[3] = edge;
    return 4;
  }

  static final Float64List _sdfAlong = Float64List(3), _sdfAlongTexels = Float64List(3);
  static final Float64List _sdfAcross = Float64List(4), _sdfAcrossTexels = Float64List(4);
}

/// The triangles a [SegmentsShape] drawn on its own is stroked with, kept while the same segments are
/// drawn again: a dashed line is thousands of dashes, the same ones every frame. Built from the
/// segments, stroke and device pixel only, so an equal key builds the same triangles.
class _SegmentTriangles {
  static final Map<_SegmentsKey, List<ui.Vertices>> _built = {};
  static const int _capacity = 256;

  /// The triangles of a list that never changes, by the list.
  static final Expando<_HeldTriangles> _ofFixed = Expando();

  static List<ui.Vertices> of(Float32List points, double strokeWidth, int lineCap, Color color, double px,
      [bool fixed = false]) {
    final argb = _argbOf(color), antiAlias = _RenderHints.antiAlias;
    if (fixed) {
      final held = _ofFixed[points];
      if (held != null && held.strokeWidth == strokeWidth && held.lineCap == lineCap && held.argb == argb &&
          held.px == px && held.antiAlias == antiAlias) {
        return held.built;
      }
      final built = _buildAll(points, strokeWidth, lineCap, color, px);
      _ofFixed[points] = _HeldTriangles(strokeWidth, lineCap, argb, px, antiAlias, built);
      return built;
    }
    final key = _SegmentsKey.probe(points, strokeWidth, lineCap, argb, px, antiAlias);
    final held = _built[key];
    if (held != null) return held;
    // Dropped, not disposed: a picture recorded with them may still be drawn.
    if (_built.length >= _capacity) _built.clear();
    final built = _buildAll(points, strokeWidth, lineCap, color, px);
    _built[key.held()] = built;
    return built;
  }

  static List<ui.Vertices> _buildAll(Float32List points, double strokeWidth, int lineCap, Color color, double px) {
    final chunk = _TriangleList.maxVertices ~/ _TriangleList.segmentVertices(strokeWidth, px) * 4;
    return <ui.Vertices>[
      for (var from = 0; from < points.length; from += chunk)
        _build(Float32List.sublistView(points, from, math.min(points.length, from + chunk)), strokeWidth,
            lineCap, color, px),
    ];
  }

  static ui.Vertices _build(Float32List part, double strokeWidth, int lineCap, Color color, double px) {
    final segments = part.length ~/ 4;
    return (_TriangleList(segments * _TriangleList.segmentVertices(strokeWidth, px),
            segments * _TriangleList.segmentIndices(strokeWidth, px), px)
          ..addSegments(part, strokeWidth, lineCap, color))
        .build();
  }
}

class _HeldTriangles {
  _HeldTriangles(this.strokeWidth, this.lineCap, this.argb, this.px, this.antiAlias, this.built);
  final double strokeWidth;
  final int lineCap;
  final int argb;
  final double px;
  final bool antiAlias;
  final List<ui.Vertices> built;
}

/// Segments and their stroke, equal when every coordinate is the same bit for bit. Looked up with one
/// reused key over a reused copy of the coordinates, copied only when kept.
final class _SegmentsKey {
  _SegmentsKey._();

  static final _SegmentsKey _probe = _SegmentsKey._();
  static Float32List _scratch = Float32List(0);
  static Int32List _scratchBits = Int32List(0);

  static _SegmentsKey probe(Float32List points, double strokeWidth, int lineCap, int argb, double px, bool antiAlias) {
    if (_scratch.length < points.length) {
      _scratch = Float32List(points.length * 2);
      _scratchBits = Int32List.view(_scratch.buffer);
    }
    _scratch.setRange(0, points.length, points);
    return _probe
      ..bits = _scratchBits
      ..length = points.length
      ..strokeWidth = strokeWidth
      ..lineCap = lineCap
      ..argb = argb
      ..px = px
      ..antiAlias = antiAlias
      ..hashCode = Object.hash(_hashBits(_scratchBits, points.length), strokeWidth, lineCap, argb, px, antiAlias);
  }

  /// This key over a copy of the coordinates, so the points it was made from can change.
  _SegmentsKey held() => _SegmentsKey._()
    ..bits = Int32List.fromList(Int32List.sublistView(bits, 0, length))
    ..length = length
    ..strokeWidth = strokeWidth
    ..lineCap = lineCap
    ..argb = argb
    ..px = px
    ..antiAlias = antiAlias
    ..hashCode = hashCode;

  Int32List bits = Int32List(0);
  int length = 0;
  double strokeWidth = 0;
  int lineCap = 0;
  int argb = 0;
  double px = 0;
  bool antiAlias = false;
  @override
  int hashCode = 0;

  static int _hashBits(Int32List bits, int length) {
    var h = length;
    for (var i = 0; i < length; i++) {
      h = (h * 31 + bits[i]) & 0x3FFFFFFF;
    }
    return h;
  }

  @override
  bool operator ==(Object other) {
    if (other is! _SegmentsKey || other.hashCode != hashCode || other.strokeWidth != strokeWidth ||
        other.lineCap != lineCap || other.argb != argb || other.px != px || other.antiAlias != antiAlias ||
        other.length != length) {
      return false;
    }
    final a = bits, b = other.bits;
    for (var i = 0; i < length; i++) {
      if (a[i] != b[i]) return false;
    }
    return true;
  }
}

/// A gradient's rectangle, colours and direction. Looked up with one reused key, copied only when kept.
final class _GradientKey {
  Rect rect = Rect.zero;
  Color from = const Color(0x00000000), to = const Color(0x00000000);
  Alignment begin = Alignment.center, end = Alignment.center;
  @override
  int hashCode = 0;

  _GradientKey set(Rect rect, Color from, Color to, Alignment begin, Alignment end) {
    this.rect = rect;
    this.from = from;
    this.to = to;
    this.begin = begin;
    this.end = end;
    hashCode = Object.hash(rect, from, to, begin, end);
    return this;
  }

  _GradientKey held() => _GradientKey()
    ..rect = rect
    ..from = from
    ..to = to
    ..begin = begin
    ..end = end
    ..hashCode = hashCode;

  @override
  bool operator ==(Object other) =>
      other is _GradientKey && other.hashCode == hashCode && other.rect == rect && other.from == from &&
      other.to == to && other.begin == begin && other.end == end;
}

class PathShape extends Shape {
  PathShape(Path path, this.color, this.strokeWidth, this.lineCap, this.lineJoin,
      {this.isFilled = false, this.clipRect})
      : _path = path,
        _source = null,
        _by = Offset.zero;

  /// The path [source] describes, moved by [by], built the first time it is needed: moving the
  /// shape again builds no path in between.
  PathShape.of(_PathSource source, Offset by, this.color, this.strokeWidth, this.lineCap, this.lineJoin,
      {this.isFilled = false, this.clipRect})
      : _source = source,
        _by = by;

  final _PathSource? _source;
  final Offset _by;
  Path? _path;

  Path get path => _path ??= _KeptPaths.ofSource(_source!, _by);

  /// This shape moved by [by], under [clipRect].
  PathShape moved(Offset by, Rect? clipRect) {
    final source = _source;
    return source != null && _path == null
        ? PathShape.of(source, _by + by, color, strokeWidth, lineCap, lineJoin,
            isFilled: isFilled, clipRect: clipRect)
        : PathShape(path.shift(by), color, strokeWidth, lineCap, lineJoin, isFilled: isFilled, clipRect: clipRect);
  }

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
  String describe() => '${isFilled ? "Fill" : "Draw"}Path ${_dr(path.getBounds())} ${_dc(color)} w=${_dd(strokeWidth)}${_dk(clipRect)}';
}

/// How many paths have been built from a GC's Path data.
@visibleForTesting
int pathsBuilt = 0;

/// A Dart-backed Path's points and types, as the wire holds them, and the rule it fills by.
class _PathSource {
  _PathSource(this.types, this.points, this.fillType);

  final List<int> types;
  final List<double> points;
  final PathFillType fillType;

  static _PathSource? of(VPathData? data, PathFillType fillType) {
    final types = data?.types;
    final points = data?.points;
    if (types == null || points == null || types.isEmpty) return null;
    return _PathSource(types, points, fillType);
  }

  /// The path, every point moved by [by]. Points missing for a segment end it there.
  Path build(Offset by) {
    pathsBuilt++;
    final dx = by.dx, dy = by.dy;
    final path = Path()..fillType = fillType;
    var i = 0;
    bool has(int n) => i + n <= points.length;
    for (final type in types) {
      switch (type) {
        case SWT.PATH_MOVE_TO:
          if (!has(2)) return path;
          path.moveTo(points[i++] + dx, points[i++] + dy);
        case SWT.PATH_LINE_TO:
          if (!has(2)) return path;
          path.lineTo(points[i++] + dx, points[i++] + dy);
        case SWT.PATH_QUAD_TO:
          if (!has(4)) return path;
          path.quadraticBezierTo(points[i++] + dx, points[i++] + dy, points[i++] + dx, points[i++] + dy);
        case SWT.PATH_CUBIC_TO:
          if (!has(6)) return path;
          path.cubicTo(points[i++] + dx, points[i++] + dy, points[i++] + dx, points[i++] + dy,
              points[i++] + dx, points[i++] + dy);
        case SWT.PATH_CLOSE:
          path.close();
      }
    }
    return path;
  }
}

/// How many paths [_KeptPaths] has built to keep.
@visibleForTesting
int keptPathsBuilt = 0;

/// Paths kept by the geometry they are built from: a GC repaints the same shapes frame after frame,
/// and a path is a native object built with a call per segment. Geometry drawn once is built in the
/// shared path, as before; drawn again, it gets a path of its own, kept until the cache is full.
abstract final class _KeptPaths {
  static const int _capacity = 1024;
  static final Map<_PointsPathKey, Path?> _points = {};
  static final Map<_PiePathKey, Path?> _pies = {};
  static final Map<_SourcePathKey, Path> _sources = {};
  static final _PointsPathKey _pointsProbe = _PointsPathKey();
  static final _PiePathKey _pieProbe = _PiePathKey();
  static final _SourcePathKey _sourceProbe = _SourcePathKey();

  static Path ofPoints(List<int> points, double shift, bool closed, PathFillType fillType) {
    final key = _pointsProbe.set(points, shift, closed, fillType);
    final kept = _points[key];
    if (kept != null) return kept;
    final seen = _points.containsKey(key);
    if (!seen && _points.length >= _capacity) _points.clear();
    final path = seen ? _keep(Path()..fillType = fillType) : _pointsPath(fillType);
    path.moveTo(points[0] + shift, points[1] + shift);
    for (int i = 2; i < points.length; i += 2) {
      path.lineTo(points[i] + shift, points[i + 1] + shift);
    }
    if (closed) path.close();
    if (seen) {
      _points[key] = path;
    } else {
      _points[key.held()] = null;
    }
    return path;
  }

  static Path ofPie(Rect rect, double startAngle, double sweepAngle) {
    final key = _pieProbe.set(rect, startAngle, sweepAngle);
    final kept = _pies[key];
    if (kept != null) return kept;
    final seen = _pies.containsKey(key);
    if (!seen && _pies.length >= _capacity) _pies.clear();
    final path = piePath(rect, startAngle, sweepAngle, seen ? _keep(Path()) : _pointsPath(PathFillType.nonZero));
    if (seen) {
      _pies[key] = path;
    } else {
      _pies[key.held()] = null;
    }
    return path;
  }

  /// The path [source] describes moved by [by]: the one built before for the same data, or a new one.
  static Path ofSource(_PathSource source, Offset by) {
    final key = _sourceProbe.set(source, by);
    final kept = _sources[key];
    if (kept != null) return kept;
    if (_sources.length >= _capacity) _sources.clear();
    return _sources[key.held()] = _keep(source.build(by));
  }

  static Path _keep(Path path) {
    keptPathsBuilt++;
    return path;
  }
}

/// Points, the shift they are drawn at, whether they close and the rule they fill by. Looked up with
/// one reused key, copied only when kept; the points are the shape's own list, which nothing changes.
final class _PointsPathKey {
  List<int> points = const [];
  double shift = 0;
  bool closed = false;
  PathFillType fillType = PathFillType.nonZero;
  @override
  int hashCode = 0;

  _PointsPathKey set(List<int> points, double shift, bool closed, PathFillType fillType) {
    this.points = points;
    this.shift = shift;
    this.closed = closed;
    this.fillType = fillType;
    var h = points.length;
    for (var i = 0; i < points.length; i++) {
      h = (h * 31 + points[i]) & 0x3FFFFFFF;
    }
    hashCode = Object.hash(h, shift, closed, fillType);
    return this;
  }

  _PointsPathKey held() => _PointsPathKey()
    ..points = points
    ..shift = shift
    ..closed = closed
    ..fillType = fillType
    ..hashCode = hashCode;

  @override
  bool operator ==(Object other) {
    if (other is! _PointsPathKey || other.hashCode != hashCode || other.shift != shift || other.closed != closed ||
        other.fillType != fillType || other.points.length != points.length) {
      return false;
    }
    final a = points, b = other.points;
    for (var i = 0; i < a.length; i++) {
      if (a[i] != b[i]) return false;
    }
    return true;
  }
}

/// A pie slice's box and angles. Looked up with one reused key, copied only when kept.
final class _PiePathKey {
  Rect rect = Rect.zero;
  double start = 0, sweep = 0;
  @override
  int hashCode = 0;

  _PiePathKey set(Rect rect, double start, double sweep) {
    this.rect = rect;
    this.start = start;
    this.sweep = sweep;
    hashCode = Object.hash(rect, start, sweep);
    return this;
  }

  _PiePathKey held() => _PiePathKey()
    ..rect = rect
    ..start = start
    ..sweep = sweep
    ..hashCode = hashCode;

  @override
  bool operator ==(Object other) =>
      other is _PiePathKey && other.hashCode == hashCode && other.start == start && other.sweep == sweep &&
      other.rect == rect;
}

/// A Path's data, as the wire holds it, and the offset it is moved by. Looked up with one reused key,
/// copied only when kept; the lists are the decoded op's own, which nothing changes.
final class _SourcePathKey {
  List<int> types = const [];
  List<double> points = const [];
  PathFillType fillType = PathFillType.nonZero;
  double dx = 0, dy = 0;
  @override
  int hashCode = 0;

  _SourcePathKey set(_PathSource source, Offset by) {
    types = source.types;
    points = source.points;
    fillType = source.fillType;
    dx = by.dx;
    dy = by.dy;
    var h = types.length;
    for (var i = 0; i < types.length; i++) {
      h = (h * 31 + types[i]) & 0x3FFFFFFF;
    }
    for (var i = 0; i < points.length; i++) {
      h = (h * 31 + points[i].hashCode) & 0x3FFFFFFF;
    }
    hashCode = Object.hash(h, fillType, dx, dy);
    return this;
  }

  _SourcePathKey held() => _SourcePathKey()
    ..types = types
    ..points = points
    ..fillType = fillType
    ..dx = dx
    ..dy = dy
    ..hashCode = hashCode;

  @override
  bool operator ==(Object other) {
    if (other is! _SourcePathKey || other.hashCode != hashCode || other.fillType != fillType || other.dx != dx ||
        other.dy != dy || other.types.length != types.length || other.points.length != points.length) {
      return false;
    }
    for (var i = 0; i < types.length; i++) {
      if (other.types[i] != types[i]) return false;
    }
    for (var i = 0; i < points.length; i++) {
      if (other.points[i] != points[i]) return false;
    }
    return true;
  }
}

/// The one path a polygon, a polyline or a pie slice is built in and drawn from: drawing records the path's
/// geometry as it is then, so the next shape can reset and rebuild it.
final Path _sharedPointsPath = Path();

Path _pointsPath(PathFillType fillType) => _sharedPointsPath
  ..reset()
  ..fillType = fillType;

class PolygonShape extends Shape {
  @override
  late final Rect? paintedBounds = _pointsBounds(points, isFilled ? 1 : 2 * math.max(strokeWidth, 1) + 1);

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
    // The points carry the stroke's half-pixel shift; the canvas is not moved.
    final shift = _strokeShift(strokeWidth, !isFilled).dx;
    c.drawPath(_KeptPaths.ofPoints(points, shift, true, fillType), _paint()
      ..color = color
      ..style = isFilled ? PaintingStyle.fill : PaintingStyle.stroke
      ..strokeWidth = strokeWidth
      ..strokeCap = getStrokeCap(lineCap)
      ..strokeJoin = getStrokeJoin(lineJoin));
  }

  @override
  String toString() => '${isFilled ? "Fill" : ""}Polygon ${points.length ~/ 2} pts';

  @override
  String describe() => '${isFilled ? "Fill" : "Draw"}Polygon ${points.join(",")} ${_dc(color)} w=${_dd(strokeWidth)}${_dk(clipRect)}';
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
    // The points carry the stroke's half-pixel shift; the canvas is not moved.
    final shift = _strokeShift(strokeWidth, !isFilled).dx;
    c.drawPath(_KeptPaths.ofPoints(points, shift, false, PathFillType.nonZero), _paint()
      ..color = color
      ..style = isFilled ? PaintingStyle.fill : PaintingStyle.stroke
      ..strokeWidth = strokeWidth
      ..strokeCap = getStrokeCap(lineCap)
      ..strokeJoin = getStrokeJoin(lineJoin));
  }

  @override
  String toString() => 'Polyline ${points.length ~/ 2} pts';

  @override
  String describe() => 'Polyline ${points.join(",")} ${_dc(color)} w=${_dd(strokeWidth)}${_dk(clipRect)}';
}

/// The pie fillArc covers. A sweep of a full turn is the whole oval: arcTo draws nothing for it.
///
/// Built in [into] when given, which a shape drawing it at once passes so as to make no path of its own.
Path piePath(Rect rect, double startAngle, double sweepAngle, [Path? into]) {
  final path = into ?? Path();
  if (sweepAngle.abs() >= 2 * math.pi) return path..addOval(rect);
  return path
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
      c.drawPath(_KeptPaths.ofPie(rect, startAngle, sweepAngle), paint);
    } else {
      c.drawArc(_alignedRect(rect, strokeWidth, true), startAngle, sweepAngle, false, paint);
    }
  }

  @override
  String toString() => '${isFilled ? "Fill" : "Draw"}Arc $rect';

  @override
  String describe() => '${isFilled ? "Fill" : "Draw"}Arc ${_dr(rect)} ${startAngle.toStringAsFixed(4)}+${sweepAngle.toStringAsFixed(4)} ${_dc(color)} w=${_dd(strokeWidth)}${_dk(clipRect)}';
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
  String describe() => '${isFilled ? "Fill" : "Draw"}RoundRect ${_dr(rect)} r=${_dd(radiusX)},${_dd(radiusY)} ${_dc(color)} w=${_dd(strokeWidth)}${_dk(clipRect)}';
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

  /// The image's bounds in logical px: its own width and height (already divided by the zoom its
  /// pixels were loaded at), else its pixel data's size; null when it carries neither.
  static Size? _logicalBounds(VImage vImage) {
    int? positive(int? v) => v != null && v > 0 ? v : null;
    final w = positive(vImage.width) ?? positive(vImage.imageData?.width);
    final h = positive(vImage.height) ?? positive(vImage.imageData?.height);
    return w == null || h == null ? null : Size(w.toDouble(), h.toDouble());
  }

  static Rect _destOf(VGCDrawImageImageintintintintintintintint opArgs, Size naturalSize) =>
      Rect.fromLTWH(
        (opArgs.destX).toDouble(),
        (opArgs.destY).toDouble(),
        opArgs.destWidth == -1 ? naturalSize.width : (opArgs.destWidth).toDouble(),
        opArgs.destHeight == -1 ? naturalSize.height : (opArgs.destHeight).toDouble(),
      );

  static Rect _srcOf(VGCDrawImageImageintintintintintintintint opArgs, ui.Image image) =>
      Rect.fromLTWH(
        (opArgs.srcX).toDouble(),
        (opArgs.srcY).toDouble(),
        opArgs.srcWidth == -1 ? image.width.toDouble() : (opArgs.srcWidth).toDouble(),
        opArgs.srcHeight == -1 ? image.height.toDouble() : (opArgs.srcHeight).toDouble(),
      );

  /// The blit [fromVImageDetailed] resolves to, made now when that needs nothing it would wait for:
  /// an application bitmap already decoded, drawn as its own colors. Null otherwise.
  static ImageShape? fromDecoded(VImage vImage,
      VGCDrawImageImageintintintintintintintint opArgs, Rect? clipRect, int alpha) {
    if (vImage.remoteRef != null || (vImage.svgContent?.isNotEmpty ?? false)) return null;
    if (gcIconsReplacement && (vImage.filename?.isNotEmpty ?? false)) return null;
    final data = vImage.imageData?.data;
    if (data == null || data is String) return null;
    final image = ImageUtils.decodedNow(data);
    if (image == null) return null;
    return ImageShape.raster(image, _srcOf(opArgs, image),
        _destOf(opArgs, Size(image.width.toDouble(), image.height.toDouble())),
        clipRect: clipRect, alpha: alpha);
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

      // An unsized drawImage draws at the image's bounds, so a replacement takes the replaced
      // image's logical size, not its own.
      final replacedSize = replacement == null ? null : _logicalBounds(vImage);

      if (replacement is String) {
        try {
          final svgTint = !preserveColors || ImageUtils.svgNeedsTint(replacement)
              ? (tint ?? AppColors.getColor(true))
              : null;
          // Decoded once (SvgPictures), with the tint put into the picture where it can be: a
          // filtered picture costs a layer of its own on every frame (SvgTint).
          final (pictureInfo, filtered) = await SvgPictures.load(replacement, svgTint);
          final naturalSize = replacedSize ?? pictureInfo.size;
          final destRect = Rect.fromLTWH(
            (opArgs.destX).toDouble(),
            (opArgs.destY).toDouble(),
            opArgs.destWidth == -1 ? naturalSize.width : (opArgs.destWidth).toDouble(),
            opArgs.destHeight == -1 ? naturalSize.height : (opArgs.destHeight).toDouble(),
          );
          final svgFilter = filtered ? ColorFilter.mode(svgTint!, BlendMode.srcIn) : null;
          return ImageShape.svg(pictureInfo, destRect,
              clipRect: clipRect, colorFilter: svgFilter, alpha: alpha);
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

      final naturalSize =
          replacedSize ?? Size(uiImage.width.toDouble(), uiImage.height.toDouble());
      final destRect = _destOf(opArgs, naturalSize);
      final srcRect = replacement != null
          ? Rect.fromLTWH(0, 0, uiImage.width.toDouble(), uiImage.height.toDouble())
          : _srcOf(opArgs, uiImage);

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
    contentKey = flags & 16 != 0 ? r.varint() : 0;
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

  /// What the render shows, as Java names it: renders with the same key draw the same pixels. 0 when
  /// unnamed.
  int contentKey = 0;
  Rect? damage;
}
