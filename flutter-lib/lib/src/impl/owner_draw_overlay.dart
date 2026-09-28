import 'dart:async';

import 'package:flutter/material.dart';

import '../gen/event.dart';
import '../gen/gc.dart';
import '../gen/widget.dart';
import 'control_evolve.dart';

/// What an owner-drawing Table or Tree paints into one row: its SWT.EraseItem and SWT.PaintItem
/// drawing, addressed to the row's item id and laid over the row.
///
/// Java paints a row only once the overlay reports listening: ops that reach a channel before its
/// listener exists are replayed out of order across the GC's state and op channels.
class OwnerDrawnRowOverlay extends StatefulWidget {
  final int itemId;

  const OwnerDrawnRowOverlay({super.key, required this.itemId});

  @override
  State<OwnerDrawnRowOverlay> createState() => OwnerDrawnRowOverlayState();
}

class OwnerDrawnRowOverlayState extends State<OwnerDrawnRowOverlay> {
  final GlobalKey gcKey = GlobalKey();
  bool _ready = false;

  /// Called by the row's GC once its channels are registered.
  void onGCSubscribed() {
    final parent = context.findAncestorStateOfType<ControlImpl>();
    if (parent != null) _OwnerDrawRequests.add(parent, widget.itemId);
  }

  /// Called by the row's GC once it has something to show.
  void onGCReady() {
    if (!_ready && mounted) setState(() => _ready = true);
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
      "PaintItem/PaintItem",
      VEvent()..segments = ids.toList(),
    );
  }
}
