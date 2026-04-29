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
import '../theme/theme_extensions/composite_theme_extension.dart';
import '../theme/theme_settings/composite_theme_settings.dart';


Widget wrapCompositeInteractionChrome(CompositeImpl impl, Widget content) {
  final state = impl.state;

  Widget listener = MouseRegion(
    onHover: (e) {
      final event = VEvent()
        ..x = e.localPosition.dx.round()
        ..y = e.localPosition.dy.round();
      impl.widget.sendMouseMoveMouseMove(state, event);
    },
    child: Listener(
      behavior: HitTestBehavior.translucent,
      onPointerDown: (e) {
        final event = VEvent()
          ..x = e.localPosition.dx.round()
          ..y = e.localPosition.dy.round()
          ..button = 1;
        impl.widget.sendMouseMouseDown(state, event);
      },
      onPointerUp: (e) {
        final event = VEvent()
          ..x = e.localPosition.dx.round()
          ..y = e.localPosition.dy.round();
        impl.widget.sendMouseMouseUp(state, event);
      },
      onPointerMove: (e) {
        final event = VEvent()
          ..x = e.localPosition.dx.round()
          ..y = e.localPosition.dy.round();
        impl.widget.sendMouseMoveMouseMove(state, event);
      },
      child: content,
    ),
  );

  Widget result = impl.gcOverlay != null ? impl.wrapWithGCOverlay(listener) : listener;

  if (state.cursor?.cursorStyle != null) {
    result = MouseRegion(
      cursor: impl.swtCursorToFlutter(state.cursor!.cursorStyle!),
      child: result,
    );
  }

  return result;
}

class CompositeImpl<T extends CompositeSwt, V extends VComposite>
    extends ScrollableImpl<T, V> {
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

    final backgroundColor = ToolbarAreaMarker.backgroundOf(context) ??
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
