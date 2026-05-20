import 'dart:async';
import 'package:flutter/gestures.dart';
import 'package:flutter/material.dart';
import '../comm/comm.dart';
import '../gen/canvas.dart';
import '../gen/composite.dart';
import '../gen/control.dart';
import '../gen/event.dart';
import '../gen/gc.dart';
import '../gen/scrollbar.dart';
import '../gen/swt.dart';
import '../gen/widgets.dart';
import '../nolayout.dart';
import 'composite_evolve.dart';
import '../custom/toolbar_composite.dart';
import 'color_utils.dart';
import 'utils/widget_utils.dart';
import '../theme/theme_extensions/canvas_theme_extension.dart';
import '../theme/theme_extensions/scrolledcomposite_theme_extension.dart';

const double _kBarSize = 17.0;

class CanvasImpl<T extends CanvasSwt, V extends VCanvas>
    extends CompositeImpl<T, V> {
  bool _scrollbarDragging = false;
  int _dragEndMs = 0;
  double _localVScrollPx = 0.0;
  double _localHScrollPx = 0.0;
  bool _isSnapping = false;
  double _snapFromV = 0.0;
  double _snapFromH = 0.0;
  int _snapStartMs = 0;
  int? _prevHBarSel;
  int? _prevVBarSel;

  CanvasThemeExtension get _theme =>
      Theme.of(context).extension<CanvasThemeExtension>()!;

  Color get bg =>
      colorFromVColor(state.background, defaultColor: _theme.backgroundColor);
  Color get fg => _theme.foregroundColor;
  Color gcBg = Colors.transparent;

  final int _alpha = 255;
  Rect? clipRect;
  Color applyAlpha(Color color) {
    if (_alpha == 255) return color;
    return color.withOpacity(_alpha / 255.0);
  }

  @override
  void initState() {
    super.initState();
    widget.sendPaintPaint(state, null);
  }

  @override
  Widget build(BuildContext context) {
    final hSel = state.horizontalBar?.selection;
    final vSel = state.verticalBar?.selection;
    if (hSel != _prevHBarSel) {
      if (_localHScrollPx != 0.0 && !_scrollbarDragging) {
        _localHScrollPx = 0.0;
        _isSnapping = false;
      }
      _prevHBarSel = hSel;
    }
    if (vSel != _prevVBarSel) {
      if (_localVScrollPx != 0.0 && !_scrollbarDragging) {
        _localVScrollPx = 0.0;
        _isSnapping = false;
      }
      _prevVBarSel = vSel;
    }

    if (!_scrollbarDragging && !_isSnapping) {
      widget.sendPaintPaint(state, null);
    }

    final widgetTheme = _theme;
    final hasValidBounds = hasBounds(state.bounds);
    final constraints = getConstraintsFromBounds(state.bounds);
    final children = state.children;

    Widget base;
    if (children != null && children.isNotEmpty) {
      base = buildComposite();
    } else {
      Widget content;
      if (hasValidBounds && constraints != null) {
        content = ConstrainedBox(constraints: constraints, child: const SizedBox.expand());
      } else {
        content = SizedBox(
          width: widgetTheme.defaultWidth,
          height: widgetTheme.defaultHeight,
        );
      }
      base = wrap(content);
    }

    if (_localVScrollPx != 0.0 || _localHScrollPx != 0.0) {
      base = ColoredBox(
        color: bg,
        child: ClipRect(
          child: Transform.translate(
            offset: Offset(_localHScrollPx, _localVScrollPx),
            child: base,
          ),
        ),
      );
    }

    return _wrapWithScrollbars(base);
  }

  Size getBounds() {
    if (hasBounds(state.bounds)) {
      return Size(
        state.bounds!.width.toDouble(),
        state.bounds!.height.toDouble(),
      );
    }
    return Size(_theme.defaultWidth, _theme.defaultHeight);
  }

  @override
  Widget buildComposite() {
    final children = state.children;
    if (children == null || children.isEmpty) {
      final content = wrap(ColoredBox(color: bg, child: const SizedBox.expand()));
      return wrapCompositeInteractionChrome(this, content);
    }
    final rawLayout = NoLayout(children: children, composite: state);
    return wrapCompositeInteractionChrome(
      this,
      ColoredBox(color: Colors.transparent, child: rawLayout),
    );
  }

  @override
  Widget wrapWithGCOverlay(Widget child) {
    final gc = gcOverlay ?? (VGC()..id = state.id);
    final gcWidget = GCSwt<VGC>(key: gcOverlayKey, value: gc);
    return Stack(
      children: [
        Positioned.fill(child: IgnorePointer(child: gcWidget)),
        child,
      ],
    );
  }

  Widget _wrapWithScrollbars(Widget base) {
    final hBar = state.horizontalBar;
    final vBar = state.verticalBar;
    final showHBar = hBar != null && hBar.visible != false && _isScrollable(hBar);
    final showVBar = vBar != null && vBar.visible != false && _isScrollable(vBar);

    if (!showVBar && !showHBar) return base;

    void onDragStateChanged(bool dragging) {
      _scrollbarDragging = dragging;
      if (!dragging) {
        _dragEndMs = DateTime.now().millisecondsSinceEpoch;

        if (_localVScrollPx != 0.0 || _localHScrollPx != 0.0) {
          _isSnapping = true;
          _snapFromV = _localVScrollPx;
          _snapFromH = _localHScrollPx;
          _snapStartMs = DateTime.now().millisecondsSinceEpoch;
          WidgetsBinding.instance.addPostFrameCallback((_) {
            if (mounted && _isSnapping) _tickSnap();
          });
        }
      }
    }

    final hasActiveChild = state.children != null && state.children!.isNotEmpty;
    return Listener(
      onPointerSignal: (event) {
        if (event is PointerScrollEvent) {
          final now = DateTime.now().millisecondsSinceEpoch;
          if (!_scrollbarDragging && !hasActiveChild && now - _dragEndMs > 300) {
            _handleMouseWheel(event);
          }
        }
      },
      child: Stack(
        children: [
          base,
          if (showVBar)
            Positioned(
              top: 0,
              right: 0,
              bottom: showHBar ? _kBarSize : 0,
              width: _kBarSize,
              child: _CanvasScrollBar(
                bar: vBar!,
                vertical: true,
                onSelection: _sendSelection,
                onDragStateChanged: onDragStateChanged,
                onDragPixelDelta: (px) {
                  _isSnapping = false;
                  setState(() => _localVScrollPx = px);
                },
              ),
            ),
          if (showHBar)
            Positioned(
              left: 0,
              right: showVBar ? _kBarSize : 0,
              bottom: 0,
              height: _kBarSize,
              child: _CanvasScrollBar(
                bar: hBar!,
                vertical: false,
                onSelection: _sendSelection,
                onDragStateChanged: onDragStateChanged,
                onDragPixelDelta: (px) {
                  _isSnapping = false;
                  setState(() => _localHScrollPx = px);
                },
              ),
            ),
          if (showVBar && showHBar)
            Positioned(
              right: 0,
              bottom: 0,
              width: _kBarSize,
              height: _kBarSize,
              child: Listener(
                behavior: HitTestBehavior.opaque,
                child: const SizedBox.expand(),
              ),
            ),
        ],
      ),
    );
  }

  void _tickSnap() {
    if (!mounted || !_isSnapping) return;
    const snapMs = 180;
    final elapsed = DateTime.now().millisecondsSinceEpoch - _snapStartMs;
    if (elapsed >= snapMs) {
      setState(() {
        _isSnapping = false;
        _localVScrollPx = 0.0;
        _localHScrollPx = 0.0;
      });
      return;
    }
    final t = elapsed / snapMs;
    final eased = 1.0 - (1.0 - t) * (1.0 - t);
    setState(() {
      _localVScrollPx = _snapFromV * (1.0 - eased);
      _localHScrollPx = _snapFromH * (1.0 - eased);
    });
    WidgetsBinding.instance.addPostFrameCallback((_) {
      if (mounted && _isSnapping) _tickSnap();
    });
  }

  bool _isScrollable(VScrollBar bar) {
    final min = bar.minimum ?? 0;
    final max = bar.maximum ?? 100;
    final thumb = bar.thumb ?? 0;
    return max - min > thumb;
  }

  void _sendSelection(VScrollBar bar, int newValue, int detail) {
    EquoCommService.sendPayload(
      "ScrollBar/${bar.id}/Selection/Selection",
      (VEvent()..index = newValue..detail = detail).toJson(),
    );
  }

  void _handleMouseWheel(PointerScrollEvent event) {
    final vBar = state.verticalBar;
    if (vBar != null && vBar.visible != false && vBar.enabled != false) {
      final increment = vBar.increment ?? 1;
      final minimum = vBar.minimum ?? 0;
      final maximum = vBar.maximum ?? 100;
      final thumb = vBar.thumb ?? 0;
      final selection = vBar.selection ?? minimum;
      final delta = event.scrollDelta.dy > 0 ? increment : -increment;
      final newSel = (selection + delta).clamp(minimum, maximum - thumb);
      _sendSelection(vBar, newSel, 0);
    }
  }

  Color rgbMapToColor(Map<String, dynamic> m) => Color.fromARGB(
    (m['alpha'] ?? 255) as int,
    (m['red'] ?? 0) as int,
    (m['green'] ?? 0) as int,
    (m['blue'] ?? 0) as int,
  );
}

class _CanvasScrollBar extends StatefulWidget {
  final VScrollBar bar;
  final bool vertical;
  final void Function(VScrollBar bar, int value, int detail) onSelection;
  final void Function(bool dragging)? onDragStateChanged;
  final void Function(double pixelDelta)? onDragPixelDelta;

  const _CanvasScrollBar({
    required this.bar,
    required this.vertical,
    required this.onSelection,
    this.onDragStateChanged,
    this.onDragPixelDelta,
  });

  @override
  State<_CanvasScrollBar> createState() => _CanvasScrollBarState();
}

class _CanvasScrollBarState extends State<_CanvasScrollBar> {
  double? _dragStartPointerOffset;
  int? _dragStartSelection;
  int? _currentDragSelection;
  bool _hovered = false;
  double _opacity = 0.0;
  Timer? _hideTimer;
  Timer? _dragThrottleTimer;

  @override
  void initState() {
    super.initState();
    _showBar();
    _scheduleHide();
  }

  @override
  void didUpdateWidget(_CanvasScrollBar oldWidget) {
    super.didUpdateWidget(oldWidget);
    if (oldWidget.bar.selection != widget.bar.selection) {
      _showBar();
      _scheduleHide();

      if (_dragStartPointerOffset == null) {
        if (_currentDragSelection == null || widget.bar.selection == _currentDragSelection) {
          _currentDragSelection = null;
          widget.onDragPixelDelta?.call(0.0);
        }
      }
    }
  }

  @override
  void dispose() {
    _hideTimer?.cancel();
    _dragThrottleTimer?.cancel();
    super.dispose();
  }

  void _showBar() {
    _hideTimer?.cancel();
    if (!mounted) return;
    setState(() => _opacity = 1.0);
  }

  void _scheduleHide() {
    _hideTimer?.cancel();
    _hideTimer = Timer(const Duration(milliseconds: 800), () {
      if (mounted && !_hovered) setState(() => _opacity = 0.0);
    });
  }

  int get _minimum => widget.bar.minimum ?? 0;
  int get _maximum => widget.bar.maximum ?? 100;
  int get _thumb => widget.bar.thumb ?? 10;
  int get _selection => widget.bar.selection ?? _minimum;

  int get _displaySelection => _currentDragSelection ?? _selection;

  double _thumbSize(double trackSize) {
    final range = (_maximum - _minimum).toDouble();
    if (range <= 0) return trackSize;
    return (trackSize * (_thumb / range)).clamp(16.0, trackSize);
  }

  double _thumbOffset(double trackSize) {
    final thumbSz = _thumbSize(trackSize);
    final scrollRange = (_maximum - _minimum - _thumb).toDouble();
    if (scrollRange <= 0) return 0;
    return ((_displaySelection - _minimum) / scrollRange * (trackSize - thumbSz))
        .clamp(0.0, trackSize - thumbSz);
  }

  void _onDragStart(DragStartDetails details) {
    _dragStartPointerOffset =
        widget.vertical ? details.localPosition.dy : details.localPosition.dx;
    _dragStartSelection = _selection;
    _currentDragSelection = _selection;
    widget.onDragPixelDelta?.call(0.0);
    widget.onDragStateChanged?.call(true);
    _showBar();
  }

  void _onDragUpdate(DragUpdateDetails details, double trackSize) {
    if (_dragStartPointerOffset == null || _dragStartSelection == null) return;
    final current =
        widget.vertical ? details.localPosition.dy : details.localPosition.dx;
    final delta = current - _dragStartPointerOffset!;
    final thumbSz = _thumbSize(trackSize);
    final scrollRange = _maximum - _minimum - _thumb;
    if (scrollRange <= 0 || trackSize - thumbSz <= 0) return;
    final selectionDelta =
        (delta / (trackSize - thumbSz) * scrollRange).round();
    final newSel =
        (_dragStartSelection! + selectionDelta).clamp(_minimum, _maximum - _thumb);
    _currentDragSelection = newSel;
    setState(() {});
    _dragThrottleTimer ??= Timer(const Duration(milliseconds: 50), () {
      _dragThrottleTimer = null;
      final sel = _currentDragSelection;
      if (sel != null) widget.onSelection(widget.bar, sel, 0);
    });
  }

  void _onDragEnd(DragEndDetails details) {
    _dragThrottleTimer?.cancel();
    _dragThrottleTimer = null;
    final finalSel = _currentDragSelection ?? _selection;
    _dragStartPointerOffset = null;
    _dragStartSelection = null;
    widget.onDragStateChanged?.call(false);
    widget.onSelection(widget.bar, finalSel, 0);
    _scheduleHide();
  }

  void _onDragCancel() {
    _dragThrottleTimer?.cancel();
    _dragThrottleTimer = null;
    _dragStartPointerOffset = null;
    _dragStartSelection = null;
    _currentDragSelection = null;
    widget.onDragStateChanged?.call(false);
    widget.onDragPixelDelta?.call(0.0);
    _scheduleHide();
  }

  @override
  Widget build(BuildContext context) {
    final scrollTheme =
        Theme.of(context).extension<ScrolledCompositeThemeExtension>()!;
    final thickness = scrollTheme.scrollbarThickness;
    final radius = scrollTheme.scrollbarRadius;

    return LayoutBuilder(
      builder: (context, constraints) {
        final trackSize =
            widget.vertical ? constraints.maxHeight : constraints.maxWidth;
        final thumbSz = _thumbSize(trackSize);
        final thumbOff = _thumbOffset(trackSize);
        final enabled = widget.bar.enabled != false;
        final thumbColor = (_hovered && enabled)
            ? scrollTheme.scrollbarThumbHoverColor
            : scrollTheme.scrollbarThumbColor;
        final trackColor = _hovered
            ? scrollTheme.scrollbarTrackColor.withOpacity(0.5)
            : Colors.transparent;

        final painter = _ScrollBarPainter(
          vertical: widget.vertical,
          thumbSize: thumbSz,
          thumbOffset: thumbOff,
          trackColor: trackColor,
          thumbColor: thumbColor,
          thickness: thickness,
          radius: radius,
        );

        return MouseRegion(
          onEnter: (_) {
            setState(() => _hovered = true);
            _showBar();
          },
          onExit: (_) {
            setState(() => _hovered = false);
            _scheduleHide();
          },
          child: GestureDetector(
            behavior: HitTestBehavior.opaque,
            onVerticalDragStart: (widget.vertical && enabled) ? _onDragStart : null,
            onVerticalDragUpdate: (widget.vertical && enabled)
                ? (d) => _onDragUpdate(d, trackSize)
                : null,
            onVerticalDragEnd:
                (widget.vertical && enabled) ? (d) => _onDragEnd(d) : null,
            onVerticalDragCancel:
                (widget.vertical && enabled) ? _onDragCancel : null,
            onHorizontalDragStart:
                (!widget.vertical && enabled) ? _onDragStart : null,
            onHorizontalDragUpdate: (!widget.vertical && enabled)
                ? (d) => _onDragUpdate(d, trackSize)
                : null,
            onHorizontalDragEnd:
                (!widget.vertical && enabled) ? (d) => _onDragEnd(d) : null,
            onHorizontalDragCancel:
                (!widget.vertical && enabled) ? _onDragCancel : null,
            child: AnimatedOpacity(
              opacity: _opacity,
              duration: const Duration(milliseconds: 200),
              child: CustomPaint(
                painter: painter,
                size: Size(constraints.maxWidth, constraints.maxHeight),
              ),
            ),
          ),
        );
      },
    );
  }
}

class _ScrollBarPainter extends CustomPainter {
  final bool vertical;
  final double thumbSize;
  final double thumbOffset;
  final Color trackColor;
  final Color thumbColor;
  final double thickness;
  final double radius;

  const _ScrollBarPainter({
    required this.vertical,
    required this.thumbSize,
    required this.thumbOffset,
    required this.trackColor,
    required this.thumbColor,
    required this.thickness,
    required this.radius,
  });

  @override
  void paint(Canvas canvas, Size size) {
    if (trackColor.alpha > 0) {
      canvas.drawRect(
        Rect.fromLTWH(0, 0, size.width, size.height),
        Paint()..color = trackColor,
      );
    }

    final Rect thumbRect;
    if (vertical) {
      final x = (size.width - thickness) / 2;
      thumbRect = Rect.fromLTWH(
        x,
        thumbOffset,
        thickness,
        thumbSize.clamp(8.0, size.height),
      );
    } else {
      final y = (size.height - thickness) / 2;
      thumbRect = Rect.fromLTWH(
        thumbOffset,
        y,
        thumbSize.clamp(8.0, size.width),
        thickness,
      );
    }

    canvas.drawRRect(
      RRect.fromRectAndRadius(thumbRect, Radius.circular(radius)),
      Paint()..color = thumbColor,
    );
  }

  @override
  bool shouldRepaint(_ScrollBarPainter old) =>
      old.thumbSize != thumbSize ||
      old.thumbOffset != thumbOffset ||
      old.thumbColor != thumbColor ||
      old.trackColor != trackColor ||
      old.thickness != thickness ||
      old.radius != radius;
}