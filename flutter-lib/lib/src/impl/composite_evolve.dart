import 'package:flutter/material.dart';
import 'package:flutter/widgets.dart';
import '../nolayout.dart';
import '../gen/composite.dart';
import '../gen/event.dart';
import '../gen/gc.dart';
import '../gen/widget.dart';
import '../impl/gc_evolve.dart';
import '../impl/scrollable_evolve.dart';
import '../custom/toolbar_composite.dart';
import 'utils/double_tap_detector.dart';
import 'utils/widget_utils.dart';
import '../theme/theme_extensions/composite_theme_extension.dart';
import '../theme/theme_settings/composite_theme_settings.dart';


/// Returns true if [pos] falls inside the bounds of any child control.
/// Used to replicate SWT behaviour where mouse events on a child widget do
/// not propagate to the parent composite.
bool _hitsAnyChild(VComposite state, Offset pos) {
  final children = state.children;
  if (children == null || children.isEmpty) return false;
  for (final child in children) {
    final b = child.bounds;
    if (b == null) continue;
    final rect = Rect.fromLTWH(
      b.x.toDouble(), b.y.toDouble(),
      b.width.toDouble(), b.height.toDouble(),
    );
    if (rect.contains(pos)) return true;
  }
  return false;
}

Widget wrapCompositeInteractionChrome(CompositeImpl impl, Widget content) {
  final state = impl.state;

  // Cursor is merged into the existing MouseRegion instead of adding a new
  // outer wrapper. Adding/removing a wrapper widget on cursor-change would
  // change the root widget type returned by build(), deactivating all child
  // elements (CTabFolder, Canvas, Tree, …) and destroying their state.
  final cursor = state.cursor?.cursorStyle != null
      ? impl.swtCursorToFlutter(state.cursor!.cursorStyle!)
      : MouseCursor.defer;

  Widget listener = MouseRegion(
    cursor: cursor,
    onHover: (e) {
      final event = VEvent()
        ..x = e.localPosition.dx.round()
        ..y = e.localPosition.dy.round();
      impl.sendThrottledMouseMove(state, event);
    },
    child: Listener(
      behavior: HitTestBehavior.translucent,
      onPointerDown: (e) {
        if (_hitsAnyChild(state, e.localPosition)) return;
        final pos = e.localPosition;
        impl.widget.sendMouseMouseDown(
          state,
          VEvent()
            ..x = pos.dx.round()
            ..y = pos.dy.round()
            ..button = 1,
        );
        if (!impl.forwardsCompositeDoubleClick) return;
        if (impl.dblTap.registerTap(position: pos) == 2) {
          impl.widget.sendMouseMouseDoubleClick(
            state,
            VEvent()
              ..x = pos.dx.round()
              ..y = pos.dy.round()
              ..button = 1
              ..count = 2,
          );
        }
      },
      onPointerUp: (e) {
        if (_hitsAnyChild(state, e.localPosition)) return;
        final event = VEvent()
          ..x = e.localPosition.dx.round()
          ..y = e.localPosition.dy.round();
        impl.widget.sendMouseMouseUp(state, event);
      },
      onPointerMove: (e) {
        final event = VEvent()
          ..x = e.localPosition.dx.round()
          ..y = e.localPosition.dy.round();
        impl.sendThrottledDragMove(state, event);
      },
      child: content,
    ),
  );

  return impl.gcOverlay != null ? impl.wrapWithGCOverlay(listener) : listener;
}

class CompositeImpl<T extends CompositeSwt, V extends VComposite>
    extends ScrollableImpl<T, V> {
  final DoubleTapDetector dblTap = DoubleTapDetector();

  /// Whether this composite forwards MouseDoubleClick from the interaction
  /// chrome. CanvasImpl overrides this to false because it forwards its own
  /// double-click in build(); forwarding here too would double-fire.
  bool get forwardsCompositeDoubleClick => true;

  @override
  Widget build(BuildContext context) {
    return buildComposite();
  }

  @override
  Widget wrapWithGCOverlay(Widget child) {
    final gc = gcOverlay ?? (VGC()..id = state.id);
    final gcWidget = GCSwt<VGC>(key: gcOverlayKey, value: gc);

    return Stack(
      children: [
        child,
        if (gcOverlay != null)
          Positioned.fill(child: IgnorePointer(child: gcWidget))
        else
          Offstage(child: gcWidget),
      ],
    );
  }

  Widget buildComposite() {
    final isPanelChild = SashPanelMarker.of(context);
    final widgetTheme = Theme.of(context).extension<CompositeThemeExtension>()!;
    final enabled = state.enabled ?? true;
    final children = state.children;

    final backgroundColor = ParentBackgroundScope.backgroundOf(context) ??
        getCompositeBackgroundColor(state, widgetTheme, isEnabled: enabled);

    if (children == null || children.isEmpty) {
      final content = wrap(
        ColoredBox(color: backgroundColor, child: const SizedBox.expand()),
      );
      return wrapCompositeInteractionChrome(this, content);
    }

    final rawLayout = NoLayout(children: children, composite: state);
    if (state.visible != null && !state.visible!) {
      return Visibility(visible: false, child: rawLayout);
    }

    final Widget inner;
    if (isPanelChild) {
      inner = ColoredBox(
        color: backgroundColor,
        child: SashPanelMarker(active: false, child: rawLayout),
      );
    } else {
      inner = ColoredBox(color: backgroundColor, child: rawLayout);
    }

    return wrapCompositeInteractionChrome(this, inner);
  }
}
