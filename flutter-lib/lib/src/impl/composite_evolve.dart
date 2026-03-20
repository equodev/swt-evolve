import 'package:flutter/material.dart';
import 'package:flutter/widgets.dart';
import '../nolayout.dart';
import '../gen/composite.dart';
import '../gen/control.dart';
import '../gen/event.dart';
import '../gen/gc.dart';
import '../gen/toolbar.dart';
import '../gen/toolitem.dart';
import '../gen/canvas.dart';
import '../gen/widget.dart';
import '../gen/widgets.dart';
import '../impl/gc_evolve.dart';
import '../impl/scrollable_evolve.dart';

class CompositeImpl<T extends CompositeSwt, V extends VComposite>
    extends ScrollableImpl<T, V> {
  @override
  Widget build(BuildContext context) {
    return buildComposite();
  }

  /// Composites don't use RepaintBoundary because it causes layout/update
  /// issues when Java sends frequent layout changes to children.
  /// copyArea on composites is not a common use case.
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
    final children = state.children;

    Widget content;
    if (children == null || children.isEmpty) {
      content = wrap(const SizedBox.expand());
    } else {
      final layout = NoLayout(children: children, composite: state);
      if (state.visible != null && !state.visible!) {
        return Visibility(visible: false, child: layout);
      }
      content = layout;
    }

    Widget listener = MouseRegion(
      onHover: (e) {
        final event = VEvent()
          ..x = e.localPosition.dx.round()
          ..y = e.localPosition.dy.round();
        widget.sendMouseMoveMouseMove(state, event);
      },
      child: Listener(
        behavior: HitTestBehavior.translucent,
        onPointerDown: (e) {
          final event = VEvent()
            ..x = e.localPosition.dx.round()
            ..y = e.localPosition.dy.round()
            ..button = 1;
          widget.sendMouseMouseDown(state, event);
        },
        onPointerUp: (e) {
          final event = VEvent()
            ..x = e.localPosition.dx.round()
            ..y = e.localPosition.dy.round();
          widget.sendMouseMouseUp(state, event);
        },
        onPointerMove: (e) {
          final event = VEvent()
            ..x = e.localPosition.dx.round()
            ..y = e.localPosition.dy.round();
          widget.sendMouseMoveMouseMove(state, event);
        },
        child: content,
      ),
    );

    Widget result = gcOverlay != null ? wrapWithGCOverlay(listener) : listener;

    if (state.cursor?.cursorStyle != null) {
      result = MouseRegion(
        cursor: swtCursorToFlutter(state.cursor!.cursorStyle!),
        child: result,
      );
    }

    return result;
  }
}