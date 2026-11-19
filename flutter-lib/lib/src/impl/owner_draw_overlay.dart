import 'dart:async';

import 'package:flutter/material.dart';

import '../comm/comm.dart';
import '../gen/event.dart';
import '../gen/gc.dart';
import '../gen/widget.dart';
import 'control_evolve.dart';
import 'utils/widget_utils.dart' show appPaintsColors;
import '../gen/widgets.dart' show WireChannel, WireType;

/// What an owner-drawing Table or Tree paints into one row: its SWT.EraseItem and SWT.PaintItem
/// drawing, addressed to the row's item id and laid over the row.
///
/// Java paints a row on screen as soon as it sends it, and any other once the overlay reports
/// listening. Until the overlay shows what Java painted, the row draws its own text and images; then
/// it leaves the cells its `paintedTexts` and `paintedImages` name to the overlay ([isShown]). A row
/// whose painting arrived before it mounted leaves them from its first frame: its own text sits in
/// the theme font where Java's does not, so drawing it for the frame before the painting is applied
/// makes the row's content jump.
class OwnerDrawnRowOverlay extends StatefulWidget {
  final int itemId;

  /// The opaque colour Evolve paints the row on (theme row, band or selection), which the
  /// listeners' text has to read on wherever they lay no fill of their own.
  final Color ground;

  /// The row's ground as the application set it, which its text colours were picked for: the
  /// item's or the control's background, else white, SWT's default list background.
  final Color chosenOn;

  /// The Table or Tree theme's minimum text contrast.
  final double minContrast;

  /// The colour the row's own fill takes, shared by the rows of one Table; null keeps the fills.
  final OwnerDrawnRowFill? rowFill;

  /// Whether the row is selected: its fill then is no hover fill.
  final bool selected;

  const OwnerDrawnRowOverlay(
      {super.key,
      required this.itemId,
      required this.ground,
      this.chosenOn = Colors.white,
      required this.minContrast,
      this.rowFill,
      this.selected = false});

  /// How many overlays of each row (by item id) show what Java painted, or hold a painting received
  /// before they mounted and are about to show it: an overlay remounted under a new parent can count
  /// before the one it replaces is gone.
  static final Map<int, int> _shown = {};

  /// Whether row [itemId]'s overlay shows its painting, or is about to show one already received,
  /// so the row leaves those cells to it.
  static bool isShown(int itemId) => _shown.containsKey(itemId) || _paintHeld(itemId);

  /// Whether Java's painting of row [itemId] arrived before the row's GC could take it: the GC
  /// applies it once mounted, after the frame that mounts it. Only a painting counts: a GC's other
  /// frames held alone (a dispose, an image) give its overlay nothing to show.
  static bool _paintHeld(int itemId) =>
      EquoCommService.holdsWidgetOps(itemId, WireType.gc, WireChannel.b);

  static void _count(int itemId) => _shown.update(itemId, (n) => n + 1, ifAbsent: () => 1);

  @override
  State<OwnerDrawnRowOverlay> createState() => OwnerDrawnRowOverlayState();
}

class OwnerDrawnRowOverlayState extends State<OwnerDrawnRowOverlay> {
  final GlobalKey gcKey = GlobalKey();
  bool _ready = false;
  bool _subscribed = false;

  /// Whether this overlay counts in [OwnerDrawnRowOverlay._shown].
  bool _counted = false;

  @override
  void initState() {
    super.initState();
    // Counted before its GC mounts and takes the held painting, so the row keeps leaving its cells
    // in any rebuild until the painting shows.
    if (OwnerDrawnRowOverlay._paintHeld(widget.itemId)) _count();
    widget.rowFill?.addListener(_onRowFillChosen);
  }

  void _count() {
    _counted = true;
    OwnerDrawnRowOverlay._count(widget.itemId);
  }

  /// What the row's GC keeps its text legible on, or null when the application's colours are
  /// painted as given (use_swt_colors).
  ({Color ground, Color chosenOn, double minContrast})? get textGround => appPaintsColors(context)
      ? null
      : (ground: widget.ground, chosenOn: widget.chosenOn, minContrast: widget.minContrast);

  /// What the row's GC paints its own fill in, or null when it paints the application's fills.
  OwnerDrawnRowFill? get rowFill => appPaintsColors(context) ? null : widget.rowFill;

  /// Called by the row's GC once its channels are registered.
  void onGCSubscribed() {
    _subscribed = true;
    _requestPaint();
  }

  void _requestPaint() {
    final parent = context.findAncestorStateOfType<ControlImpl>();
    if (parent != null) _OwnerDrawRequests.add(parent, widget.itemId, widget.ground);
  }

  // The Table's rows now count another fill as their own: this row was painted with the old one.
  void _onRowFillChosen() {
    if (_subscribed && mounted) _requestPaint();
  }

  @override
  void didUpdateWidget(OwnerDrawnRowOverlay oldWidget) {
    super.didUpdateWidget(oldWidget);
    if (oldWidget.rowFill != widget.rowFill) {
      oldWidget.rowFill?.removeListener(_onRowFillChosen);
      widget.rowFill?.addListener(_onRowFillChosen);
    }
    // The row was painted on the old ground (a row just selected or deselected).
    if (_subscribed &&
        (oldWidget.ground != widget.ground ||
            oldWidget.minContrast != widget.minContrast ||
            oldWidget.rowFill != widget.rowFill)) {
      _requestPaint();
    }
  }

  /// Called by the row's GC once it has something to show. The row is rebuilt with it, so it stops
  /// drawing what the overlay draws in the same frame the overlay starts.
  void onGCReady() {
    if (_ready || !mounted) return;
    setState(() => _ready = true);
    if (!_counted) _count();
    final owner = context.findAncestorStateOfType<WidgetSwtState>();
    if (owner != null && owner.mounted) (owner.context as Element).markNeedsBuild();
  }

  @override
  void dispose() {
    widget.rowFill?.removeListener(_onRowFillChosen);
    if (_counted) {
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
    return _ready
        ? IgnorePointer(child: gc)
        : Offstage(child: gc);
  }
}

/// A theme's colour for the fill an owner-drawing Table lays under its rows' content. That fill is
/// the application colour covering the most area across the Table's rows, so a row filled in
/// another colour (a selection, a hover, a highlight) keeps it. Each row counts each colour once,
/// with the area of its latest paint in it, so repainting a row moves nothing; a tie keeps the
/// current choice. Listeners hear when the choice changes: rows painted before it repaint.
///
/// With a [hoverColor], the application's hover fill takes it: the first colour an unselected row
/// fills at least half of its own fill's area with, in a paint that has not used its own fill yet.
/// That colour stays the hover fill on every row and is no candidate for the own fill.
class OwnerDrawnRowFill extends ChangeNotifier {
  OwnerDrawnRowFill(this.color, {this.hoverColor});

  final Color color;
  final Color? hoverColor;

  static const int _maxTrackedFills = 256;
  // Per row, the area each colour covered in the latest paint that used it.
  final Map<int, Map<int, double>> _rowAreas = {};
  // Per row, the colours its current paint has used so far.
  final Map<int, Set<int>> _painting = {};
  final Map<int, double> _totals = {};
  int? _commonest;
  int? _hover;

  /// The colour to paint a fill of [rect] that row [row], [selected] or not, makes in [app].
  Color resolve(int row, Color app, Rect rect, {bool selected = false}) {
    final key = app.toARGB32();
    final area = rect.width.abs() * rect.height.abs();
    if (_hover == null && !selected && _replacesOwnFill(row, key, area)) {
      _hover = key;
      _totals.remove(key);
      for (final areas in _rowAreas.values) {
        areas.remove(key);
      }
    }
    if (key == _hover) return hoverColor!;
    if (_totals.length < _maxTrackedFills || _totals.containsKey(key)) {
      final areas = _rowAreas.putIfAbsent(row, () => {});
      final old = areas[key] ?? 0;
      final now = _painting.putIfAbsent(row, () => {}).add(key) ? area : old + area;
      areas[key] = now;
      _totals[key] = (_totals[key] ?? 0) + now - old;
      _choose();
    }
    return key == _commonest ? color : app;
  }

  bool _replacesOwnFill(int row, int key, double area) {
    final own = _commonest;
    if (hoverColor == null || own == null || key == own) return false;
    final ownArea = _rowAreas[row]?[own];
    return ownArea != null &&
        area * 2 >= ownArea &&
        !(_painting[row]?.contains(own) ?? false);
  }

  /// Row [row] has finished a paint: its next fills replace what this one counted.
  void painted(int row) => _painting.remove(row);

  void _choose() {
    var best = _commonest;
    for (final MapEntry(:key, :value) in _totals.entries) {
      if (value > (_totals[best] ?? 0)) best = key;
    }
    if (best == _commonest) return;
    _commonest = best;
    notifyListeners();
  }
}

/// The rows whose overlay started listening this frame, sent to Java once per Table or Tree, each
/// with the ground it is laid on: Java hands that colour to the listeners as the GC's background.
class _OwnerDrawRequests {
  static final Map<ControlImpl, Map<int, Color>> _pending = {};

  static void add(ControlImpl parent, int itemId, Color ground) {
    // GCs report listening from post-frame callbacks; a microtask runs once they all have, where
    // another post-frame callback would wait for a frame an idle UI never schedules.
    final rows = _pending.putIfAbsent(parent, () {
      scheduleMicrotask(() => _flush(parent));
      return <int, Color>{};
    });
    rows[itemId] = ground;
  }

  static void _flush(ControlImpl parent) {
    final rows = _pending.remove(parent);
    if (rows == null || rows.isEmpty || !parent.mounted) return;
    (parent.widget as WidgetSwt).sendEvent(
      parent.state,
      WireChannel.paintItemPaintItem,
      VEvent()
        ..segments = [
          for (final row in rows.entries) ...[row.key, row.value.toARGB32() & 0xFFFFFF],
        ],
    );
  }
}
