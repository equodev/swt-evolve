import 'dart:async';

import 'package:flutter/material.dart';

import '../gen/event.dart';
import '../gen/gc.dart';
import '../gen/widget.dart';
import 'control_evolve.dart';
import 'utils/widget_utils.dart' show appPaintsColors;
import '../gen/widgets.dart' show WireChannel;

/// What an owner-drawing Table or Tree paints into one row: its SWT.EraseItem and SWT.PaintItem
/// drawing, addressed to the row's item id and laid over the row.
///
/// Java paints a row on screen as soon as it sends it, and any other once the overlay reports
/// listening. Until the overlay shows what Java painted, the row draws its own text and images; then
/// it leaves the cells its `paintedTexts` and `paintedImages` name to the overlay ([isShown]).
class OwnerDrawnRowOverlay extends StatefulWidget {
  final int itemId;

  /// The opaque colour Evolve paints the row on (theme row, band or selection), which the
  /// listeners' text has to read on wherever they lay no fill of their own.
  final Color ground;

  /// The Table or Tree theme's minimum text contrast.
  final double minContrast;

  const OwnerDrawnRowOverlay(
      {super.key, required this.itemId, required this.ground, required this.minContrast});

  /// How many overlays of each row (by item id) show what Java painted: an overlay remounted under a
  /// new parent can show before the one it replaces is gone.
  static final Map<int, int> _shown = {};

  /// Whether row [itemId]'s overlay shows its painting, so the row leaves those cells to it.
  static bool isShown(int itemId) => _shown.containsKey(itemId);

  @override
  State<OwnerDrawnRowOverlay> createState() => OwnerDrawnRowOverlayState();
}

class OwnerDrawnRowOverlayState extends State<OwnerDrawnRowOverlay> {
  final GlobalKey gcKey = GlobalKey();
  bool _ready = false;
  bool _subscribed = false;

  /// What the row's GC keeps its text legible on, or null when the application's colours are
  /// painted as given (use_swt_colors).
  ({Color ground, double minContrast})? get textGround => appPaintsColors(context)
      ? null
      : (ground: widget.ground, minContrast: widget.minContrast);

  /// Called by the row's GC once its channels are registered.
  void onGCSubscribed() {
    _subscribed = true;
    _requestPaint();
  }

  void _requestPaint() {
    final parent = context.findAncestorStateOfType<ControlImpl>();
    if (parent != null) _OwnerDrawRequests.add(parent, widget.itemId);
  }

  @override
  void didUpdateWidget(OwnerDrawnRowOverlay oldWidget) {
    super.didUpdateWidget(oldWidget);
    // The text was made legible on the old ground (a row just selected or deselected).
    if (_subscribed &&
        (oldWidget.ground != widget.ground || oldWidget.minContrast != widget.minContrast)) {
      _requestPaint();
    }
  }

  /// Called by the row's GC once it has something to show. The row is rebuilt with it, so it stops
  /// drawing what the overlay draws in the same frame the overlay starts.
  void onGCReady() {
    if (_ready || !mounted) return;
    setState(() => _ready = true);
    OwnerDrawnRowOverlay._shown.update(widget.itemId, (n) => n + 1, ifAbsent: () => 1);
    final owner = context.findAncestorStateOfType<WidgetSwtState>();
    if (owner != null && owner.mounted) (owner.context as Element).markNeedsBuild();
  }

  @override
  void dispose() {
    if (_ready) {
      final left = (OwnerDrawnRowOverlay._shown[widget.itemId] ?? 1) - 1;
      if (left > 0) {
        OwnerDrawnRowOverlay._shown[widget.itemId] = left;
      } else {
        OwnerDrawnRowOverlay._shown.remove(widget.itemId);
      }
    }
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    final gc = GCSwt<VGC>(key: gcKey, value: VGC()..id = widget.itemId);
    // The row itself carries the item's semantics; the drawing would only repeat its text.
    return _ready
        ? IgnorePointer(child: ExcludeSemantics(child: gc))
        : Offstage(child: gc);
  }
}

/// The rows whose overlay started listening this frame, sent to Java once per Table or Tree.
class _OwnerDrawRequests {
  static final Map<ControlImpl, Set<int>> _pending = {};

  static void add(ControlImpl parent, int itemId) {
    // GCs report listening from post-frame callbacks; a microtask runs once they all have, where
    // another post-frame callback would wait for a frame an idle UI never schedules.
    final ids = _pending.putIfAbsent(parent, () {
      scheduleMicrotask(() => _flush(parent));
      return <int>{};
    });
    ids.add(itemId);
  }

  static void _flush(ControlImpl parent) {
    final ids = _pending.remove(parent);
    if (ids == null || ids.isEmpty || !parent.mounted) return;
    (parent.widget as WidgetSwt).sendEvent(
      parent.state,
      WireChannel.paintItemPaintItem,
      VEvent()..segments = ids.toList(),
    );
  }
}
