import 'package:flutter/gestures.dart';
import 'package:flutter/widgets.dart';

/// Scrolls a strip of tabs so all of [tab] is in view: the end first, then the start, so a tab
/// wider than the strip shows its leading edge.
void revealInStrip(ScrollPosition position, RenderObject tab) {
  position.ensureVisible(tab, alignmentPolicy: ScrollPositionAlignmentPolicy.keepVisibleAtEnd);
  position.ensureVisible(tab, alignmentPolicy: ScrollPositionAlignmentPolicy.keepVisibleAtStart);
}

/// Where a wheel over a horizontal strip takes [position], or null when it stays: a vertical wheel
/// scrolls the strip too, which a horizontal viewport does not do by itself.
double? stripWheelTarget(ScrollPosition position, PointerScrollEvent event) {
  if (!position.hasContentDimensions || position.maxScrollExtent <= 0) return null;
  final delta = event.scrollDelta.dy != 0 ? event.scrollDelta.dy : event.scrollDelta.dx;
  final target = (position.pixels + delta).clamp(0.0, position.maxScrollExtent);
  return target == position.pixels ? null : target;
}

/// Scrolls [strip] by a wheel over it. The strip claims the wheel through the pointer-signal
/// resolver, so its own viewport and any scrollable around it don't scroll as well.
void wheelStrip(ScrollController strip, PointerSignalEvent event) {
  if (event is! PointerScrollEvent || !strip.hasClients) return;
  if (stripWheelTarget(strip.position, event) == null) return;
  GestureBinding.instance.pointerSignalResolver.register(event, (event) {
    if (!strip.hasClients) return;
    final target = stripWheelTarget(strip.position, event as PointerScrollEvent);
    if (target != null) strip.jumpTo(target);
  });
}

/// Keeps a strip's selected tab in view, as a native tab folder does: after the selection
/// changes, and when the strip or its tabs change size. A scroll changes neither, so the user can
/// scroll the selected tab out of view.
class StripReveal {
  StripReveal({required this.strip, required this.selectedTab, required this.mounted});

  final ScrollController strip;

  /// The selected tab, or null when there is none or it is not laid out.
  final RenderObject? Function() selectedTab;

  final bool Function() mounted;

  bool _pending = false;
  double? _viewport;
  double? _extent;

  /// Whether the strip is scrolling to the selected tab right now.
  bool get revealing => _revealing;
  bool _revealing = false;

  /// Reveals the selected tab once the frame has laid it out; at most one reveal is pending.
  void schedule() {
    if (_pending) return;
    _pending = true;
    WidgetsBinding.instance.addPostFrameCallback((_) {
      _pending = false;
      if (mounted()) _reveal();
    });
    WidgetsBinding.instance.ensureVisualUpdate();
  }

  /// Call on a change of the strip's metrics: reveals again when the tabs overflow and the
  /// viewport or the extent changed since the last reveal.
  void metricsChanged() {
    if (!strip.hasClients) return;
    final position = strip.position;
    if (!position.hasContentDimensions || !position.hasViewportDimension) return;
    if (position.maxScrollExtent <= 0.5) return;
    if (position.viewportDimension != _viewport || position.maxScrollExtent != _extent) schedule();
  }

  void _reveal() {
    if (!strip.hasClients) return;
    final position = strip.position;
    if (!position.hasContentDimensions || !position.hasViewportDimension) return;
    _viewport = position.viewportDimension;
    _extent = position.maxScrollExtent;
    final tab = selectedTab();
    if (tab == null || !tab.attached || (tab is RenderBox && !tab.hasSize)) return;
    _revealing = true;
    try {
      revealInStrip(position, tab);
    } finally {
      _revealing = false;
    }
  }
}
