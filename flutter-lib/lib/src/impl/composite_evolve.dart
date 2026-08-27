import 'package:flutter/material.dart';
import 'package:flutter/widgets.dart';
import '../nolayout.dart';
import '../gen/composite.dart';
import '../gen/decorations.dart';
import '../gen/event.dart';
import '../gen/gc.dart';
import '../gen/swt.dart';
import '../gen/widget.dart';
import '../styles.dart';
import '../impl/gc_evolve.dart';
import '../impl/scrollable_evolve.dart';
import '../custom/toolbar_composite.dart';
import 'utils/double_tap_detector.dart';
import 'utils/hover_arbiter.dart';
import 'utils/image_utils.dart';
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
  final hoverDepth = ControlNestingScope.depthOf(impl.context);

  if (state.menu != null && (state.children?.isNotEmpty ?? false)) {
    content = impl.applyMenu(content);
  }

  // Cursor is merged into the existing MouseRegion instead of adding a new
  // outer wrapper. Adding/removing a wrapper widget on cursor-change would
  // change the root widget type returned by build(), deactivating all child
  // elements (CTabFolder, Canvas, Tree, …) and destroying their state.
  final cursor = state.cursor?.cursorStyle != null
      ? impl.swtCursorToFlutter(state.cursor!.cursorStyle!)
      : MouseCursor.defer;

  Widget listener = MouseRegion(
    cursor: cursor,
    // A childless Composite reaches ControlImpl.wrap() (which already sends these); this
    // "has children" path bypasses it, so MouseEnter/MouseExit never fired here before.
    onEnter: (_) =>
        HoverExclusivityArbiter.instance.setActive(impl, hoverDepth, true),
    onExit: (_) =>
        HoverExclusivityArbiter.instance.setActive(impl, hoverDepth, false),
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
        if (!impl.forwardsControlMouseDown) return;
        // Captured so onPointerUp can forward it regardless of where the pointer ends up.
        impl.capturedPointerDowns.add(e.pointer);
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
        // Re-testing hitsAnyChild here would drop the MouseUp once a drag moves onto a sibling's rect.
        if (!impl.capturedPointerDowns.remove(e.pointer)) return;
        final event = VEvent()
          ..x = e.localPosition.dx.round()
          ..y = e.localPosition.dy.round();
        impl.widget.sendMouseMouseUp(state, event);
      },
      onPointerCancel: (e) {
        impl.capturedPointerDowns.remove(e.pointer);
      },
      onPointerMove: (e) {
        final event = VEvent()
          ..x = e.localPosition.dx.round()
          ..y = e.localPosition.dy.round();
        impl.sendThrottledDragMove(state, event);
      },
      child: ControlNestingScope(depth: hoverDepth + 1, child: content),
    ),
  );

  Widget bordered = listener;
  // A childless Composite reaches ControlImpl.wrap() (which already applies this); this
  // "has children" path bypasses it, so SWT.BORDER was never drawn here before -- a real, general
  // gap (SWT.BORDER is a universal Control style, not Text-specific), so this applies to any
  // bordered Composite, not just this app's password-field container. No fill: a
  // TRANSPARENT-styled Composite paints nothing of its own, same convention as Text's
  // SWT.TRANSPARENT handling -- just the outline. Uses CompositeThemeExtension's own border
  // tokens (not Text's) so a generic Composite never depends on a different widget type's theme.
  // Excludes Decorations (Shell): there SWT.BORDER means a resizable OS window frame, already
  // handled by ShellImpl's own chrome -- not an internal outline to draw on top of it.
  if (state.style.has(SWT.BORDER) && state is! VDecorations) {
    final widgetTheme = Theme.of(impl.context).extension<CompositeThemeExtension>()!;
    // Plain, static outline -- not focus-aware. Text's focusedBorder swap belongs to Text alone:
    // its FocusNode is the literal caret target. A Composite's border sits on an *ancestor* of
    // whatever descendant controls it contains, and Flutter's Focus.of(context).hasFocus is true
    // whenever ANY descendant holds focus -- so a focus-aware swap here would light up blue for
    // as long as focus is anywhere inside the panel (a button click, a nested field), not because
    // the panel itself is "focused" the way a Text field is.
    bordered = Container(
      clipBehavior: Clip.antiAlias,
      decoration: BoxDecoration(
        border: Border.fromBorderSide(
          BorderSide(color: widgetTheme.borderColor, width: widgetTheme.borderWidth),
        ),
        borderRadius: BorderRadius.circular(widgetTheme.borderRadius),
      ),
      child: bordered,
    );
  }

  // The GC wrap is applied by each build path exactly once, never here: choosing it on
  // gcOverlay moved the Stack across this chrome the moment the overlay mounted and set it,
  // which remounted every descendant.
  return bordered;
}

class CompositeImpl<T extends CompositeSwt, V extends VComposite>
    extends ScrollableImpl<T, V> {
  final DoubleTapDetector dblTap = DoubleTapDetector();

  /// Pointer ids whose MouseDown this composite (not a descendant) forwarded to Java.
  final Set<int> capturedPointerDowns = {};

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

    // state.background already resolves inheritance (DartControl.getBackground(), Java side)
    // and always wins when set; ParentBackgroundScope.backgroundOf only fills in the default
    // for a Composite with no color of its own (e.g. a control hosted on a ToolBar band).
    final backgroundColor = getCompositeBackgroundColor(
      context,
      state,
      widgetTheme,
      isEnabled: enabled,
      parentBackground: ParentBackgroundScope.backgroundOf(context),
    );
    final decorationImage = ImageUtils.buildTiledBackgroundImage(state.backgroundImage);

    Widget paintBackground(Widget child) {
      // Inherited backgroundImage is already painted once by the ancestor that owns it
      // (ShellImpl.buildComposite); stay transparent so it isn't occluded by our own fill.
      if (state.backgroundImage == null &&
          ParentBackgroundScope.backgroundImageOf(context) != null) {
        return child;
      }
      if (decorationImage == null) return ColoredBox(color: backgroundColor, child: child);
      return DecoratedBox(
        decoration: BoxDecoration(color: backgroundColor, image: decorationImage),
        child: child,
      );
    }

    if (children == null || children.isEmpty) {
      final content = wrap(paintBackground(const SizedBox.expand()));
      return wrapCompositeInteractionChrome(this, content);
    }

    // Re-scopes background inheritance for this composite's own children instead of leaving a
    // distant ancestor's ParentBackgroundScope (e.g. a Shell's) visible unchanged straight
    // through -- matches SWT's per-Composite backgroundMode, where INHERIT_NONE (the default)
    // stops a parent's color from reaching grandchildren that never opted in.
    final rawLayout = wrapBackgroundInheritanceScope(
      context: context,
      backgroundMode: state.backgroundMode,
      effectiveBackground: backgroundColor,
      backgroundImage: state.backgroundImage,
      child: NoLayout(children: children, composite: state),
    );
    if (state.visible != null && !state.visible!) {
      return Visibility(visible: false, maintainState: true, child: rawLayout);
    }

    final Widget inner;
    if (isPanelChild) {
      inner = paintBackground(SashPanelMarker(active: false, child: rawLayout));
    } else {
      inner = paintBackground(rawLayout);
    }

    // wrapCompositeInteractionChrome(), unlike wrap() above, never applies wrapDnd() —
    // apply it explicitly so a Composite with children can still be a Draggable/DragTarget.
    final gcWrapped = wrapWithGCOverlay(inner);
    final dndWrapped = wrapsWholeWidgetForDnd ? wrapDnd(gcWrapped) : gcWrapped;
    return blockWhenDisabled(wrapCompositeInteractionChrome(this, dndWrapped));
  }
}
