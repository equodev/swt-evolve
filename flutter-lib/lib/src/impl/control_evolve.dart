import 'dart:async';

import 'package:flutter/foundation.dart'
    show defaultTargetPlatform, TargetPlatform;
import 'package:flutter/gestures.dart';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:flutter/widgets.dart';
import '../gen/control.dart';
import '../gen/rectangle.dart';
import '../live_bounds.dart';
import '../gen/droptarget.dart';
import '../gen/event.dart';
import '../gen/menu.dart';
import '../gen/swt.dart';
import '../gen/widget.dart';
import '../styles.dart';
import '../impl/key_forwarding.dart';
import '../impl/key_mapping.dart';
import '../impl/menu_evolve.dart';
import '../theme/theme_extensions/tooltip_theme_extension.dart';
import 'utils/dnd_utils.dart';
import 'utils/hover_arbiter.dart';
import 'utils/widget_utils.dart';
import 'widget_config.dart';

abstract class ControlImpl<T extends ControlSwt, V extends VControl>
    extends WidgetSwtState<T, V> {

  final GlobalKey<State<MenuSwt>> _menuKey = GlobalKey<State<MenuSwt>>();
  final GlobalKey _tooltipContentKey = GlobalKey();
  int _lastButton = 1;
  // SWT click count: consecutive same-button downs within the double-click
  // window chain (1, 2, 3…), like a native Display reports them. Java-side
  // consumers key real behavior off it — StyledText's word/line selection
  // takes the double-click branch whenever count > 1, and JFace's
  // PaintManager expects the caret set by a count==1 down — so the count
  // must be truthful, never assumed.
  int _clickCount = 0;
  Duration? _lastClickStamp;
  Offset? _lastClickPosition;
  int _lastMouseMoveMs = 0;
  int _lastDragMoveMs = 0;
  static const int _mouseMoveThrottleMs = 15;
  static const int _dragMoveThrottleMs = 15;
  // Matches DartDisplay.getToolTipTime() — fires SWT.MouseHover after stillness
  static const int _hoverDelayMs = 560;
  Timer? _hoverTimer;

  VRectangle? _lastRealBounds;

  void _resolveSentinelBounds(V value) {
    final b = value.bounds;
    if (b == null) return;
    if (b.width < 0 || b.height < 0) {
      if (_lastRealBounds != null) {
        value.bounds = _lastRealBounds;
      } else {
        b.width = 0;
        b.height = 0;
      }
    } else {
      _lastRealBounds = b;
    }
  }

  /// Publishes the bounds this control believes it has, so its parent's layout can prefer them
  /// over a stale copy of this child (see [LiveBounds]).
  void _publishBounds() {
    LiveBounds.publish(state.id, state.bounds, this);
  }

  @override
  void initState() {
    super.initState();
    _resolveSentinelBounds(state);
    HoverExclusivityArbiter.instance.register(this, (hovering) {
      if (hovering) {
        widget.sendMouseTrackMouseEnter(state, null);
      } else {
        widget.sendMouseTrackMouseExit(state, null);
      }
    });
    _publishBounds();
  }

  @override
  void setValue(V value) {
    _resolveSentinelBounds(value);
    super.setValue(value);
    _publishBounds();
  }

  @override
  void didUpdateWidget(covariant T oldWidget) {
    _resolveSentinelBounds(widget.value as V);
    super.didUpdateWidget(oldWidget);
    _publishBounds();
  }

  void sendThrottledMouseMove(V state, VEvent event) {
    final now = DateTime.now().millisecondsSinceEpoch;
    if (now - _lastMouseMoveMs >= _mouseMoveThrottleMs) {
      _lastMouseMoveMs = now;
      widget.sendMouseMoveMouseMove(state, event);
    }
  }

  void _resetHoverTimer(V state, VEvent event) {
    _hoverTimer?.cancel();
    _hoverTimer = Timer(const Duration(milliseconds: _hoverDelayMs), () {
      if (mounted) widget.sendMouseTrackMouseHover(state, event);
    });
  }

  @override
  void dispose() {
    _hoverTimer?.cancel();
    HoverExclusivityArbiter.instance.unregister(this);
    LiveBounds.forget(state.id, this);
    super.dispose();
  }

  void sendThrottledDragMove(V state, VEvent event) {
    final now = DateTime.now().millisecondsSinceEpoch;
    if (now - _lastDragMoveMs >= _dragMoveThrottleMs) {
      _lastDragMoveMs = now;
      widget.sendMouseMoveMouseMove(state, event);
    }
  }

  void openContextMenu(Offset globalPosition) {
    final menuState = _menuKey.currentState;
    final menuContext = _menuKey.currentContext;
    if (menuState is MenuImpl && menuContext != null) {
      final box = menuContext.findRenderObject() as RenderBox?;
      final localPos = box != null ? box.globalToLocal(globalPosition) : globalPosition;
      menuState.openContextMenuAt(context, localPos);
    }
  }

  @override
  Widget build(BuildContext context) {
    return const Text("Control");
  }

  Color? getSwtBackgroundColor(BuildContext context) {
    // Same guard as clabel_evolve.dart's stayTransparentForImage: an ancestor's own
    // backgroundImage is already painted by that ancestor, so paint nothing here and let
    // it show through -- otherwise state.background (an explicit color set somewhere up
    // the chain, resolved regardless of any image) would flatly paint over it.
    if (state.backgroundImage == null &&
        ParentBackgroundScope.backgroundImageOf(context) != null) {
      return null;
    }
    var swtBackground = state.background;
    if (swtBackground != null) {
      return Color.fromARGB(
        swtBackground.alpha,
        swtBackground.red,
        swtBackground.green,
        swtBackground.blue,
      );
    }

    // If it has no color of its own, use the parent color
    int? parentColor = getCurrentParentBackgroundColor();
    if (parentColor != null) {
      return Color(0xFF000000 | parentColor);
    }

    return null;
  }

  Color? getSwtForegroundColor(BuildContext context) {
    var swtForeground = state.foreground;
    if (swtForeground != null) {
      return Color.fromARGB(
        swtForeground.alpha,
        swtForeground.red,
        swtForeground.green,
        swtForeground.blue,
      );
    }

    return null;
  }

  MouseCursor swtCursorToFlutter(int style) {
    switch (style) {
      case 0: return SystemMouseCursors.basic;           // CURSOR_ARROW
      case 1: return SystemMouseCursors.wait;            // CURSOR_WAIT
      case 2: return SystemMouseCursors.precise;         // CURSOR_CROSS
      case 5: return SystemMouseCursors.move;            // CURSOR_SIZEALL
      case 6: return SystemMouseCursors.resizeUpLeftDownRight;  // CURSOR_SIZENESW
      case 7: return SystemMouseCursors.resizeUpDown;    // CURSOR_SIZENS
      case 8: return SystemMouseCursors.resizeUpRightDownLeft;  // CURSOR_SIZENWSE
      case 9: return SystemMouseCursors.resizeLeftRight; // CURSOR_SIZEWE
      case 19: return SystemMouseCursors.text;           // CURSOR_IBEAM
      case 20: return SystemMouseCursors.forbidden;      // CURSOR_NO
      case 21: return SystemMouseCursors.click;          // CURSOR_HAND
      default: return MouseCursor.defer;
    }
  }

  /// Whether this control forwards MouseDown from the interaction chrome
  /// (the Listener added in wrap() and wrapCompositeInteractionChrome).
  /// TableImpl overrides this to false because each cell has its own
  /// GestureDetector that already sends MouseDown with cell-center coordinates;
  /// the chrome Listener would fire a second event with different coordinates.
  bool get forwardsControlMouseDown => true;

  /// Whether [wrap] forwards KeyDown events to Java. Widgets that run their own keyboard
  /// pipeline and send Key events themselves (e.g. StyledText) override this to false —
  /// otherwise every keystroke reaches SWT KeyDown listeners twice.
  bool get forwardsKeysFromWrap => true;

  bool get wrapsWholeWidgetForDnd => true;

  Widget wrapDnd(Widget content) {
    if (state.dragSource != true && state.dropTargetId == null) return content;
    content = wrapDropTarget<DndDragPayload>(
      child: content,
      state: state,
      onDrop: (_, __, ___, position) => sendControlDrop(position),
      builder: (context, child, negotiation, isHovering) => isHovering
          ? DecoratedBox(
              decoration: BoxDecoration(border: Border.all(color: Colors.blue, width: 2)),
              child: child,
            )
          : child,
    );
    return wrapDraggable<DndDragPayload>(
      child: content,
      data: DndDragPayload(sourceControlId: state.id),
      widget: widget,
      state: state,
    );
  }

  void sendControlDrop(Offset position) {
    final dropTargetId = state.dropTargetId;
    if (dropTargetId == null) return;
    final dropTargetValue = VDropTarget()..id = dropTargetId;
    DropTargetSwt<VDropTarget>(value: dropTargetValue).sendDropdrop(
      dropTargetValue,
      VEvent()
        ..x = position.dx.round()
        ..y = position.dy.round(),
    );
  }

  Widget blockWhenDisabled(Widget child) {
    if (state.enabledEffective != false) return child;
    return ExcludeFocus(child: IgnorePointer(child: child));
  }

  Widget wrap(Widget widget) {
    final hoverDepth = ControlNestingScope.depthOf(context);
    if (wrapsWholeWidgetForDnd) {
      widget = wrapDnd(widget);
    }
    widget = tagSemantics(widget);

    if (state.cursor?.cursorStyle != null) {
      widget = MouseRegion(
        cursor: swtCursorToFlutter(state.cursor!.cursorStyle!),
        child: widget,
      );
    }

    if (state.style.has(SWT.BORDER)) {
      widget = Container(
        decoration: BoxDecoration(
          border: Border.all(width: 0.5, color: Colors.grey),
        ),
        child: widget,
      );
    }

    if (state.visible != null && !state.visible!) {
      return Visibility(visible: false, child: widget);
    }
    if (state.enabled != null && !state.enabled!) {
      return blockWhenDisabled(Opacity(opacity: 0.35, child: widget));
    }

    if (state.menu != null) {
      widget = applyMenu(widget);
    }

    widget = KeyedSubtree(key: _tooltipContentKey, child: widget);
    if (state.toolTipText != null && state.toolTipText!.isNotEmpty) {
      final tooltipTheme = Theme.of(context).extension<TooltipThemeExtension>();
      widget = Tooltip(
        message: state.toolTipText!,
        waitDuration: tooltipTheme?.waitDuration!,
        decoration: tooltipTheme != null
            ? BoxDecoration(
                color: tooltipTheme.backgroundColor,
                borderRadius: BorderRadius.circular(tooltipTheme.borderRadius),
              )
            : null,
        textStyle: tooltipTheme?.messageTextStyle,
        child: widget,
      );
    }

    // Wrap with GC overlay if needed
    widget = wrapWithGCOverlay(widget);

    // Skipped when a whole-tree Display forwards keys from a single top-level handler (see
    // [displayLevelKeyForwardingActive]) — otherwise the same key would reach Java twice. Still
    // the sole path in the embedded backend, which has no whole-tree Display.
    if (forwardsKeysFromWrap && !displayLevelKeyForwardingActive) {
      widget = Focus(
        onKeyEvent: (node, event) {
          if (event is KeyDownEvent) {
            final vEvent = mapNewKeyEventToSwt(event);
            if (vEvent.keyCode != 0 || vEvent.character != 0) {
              this.widget.sendKeyKeyDown(state, vEvent);
            }
          }
          return KeyEventResult.ignored;
        },
        child: widget,
      );
    }

    widget = Listener(
      onPointerDown: (e) {
        final button = e.buttons == kSecondaryMouseButton ? 3 : e.buttons == kMiddleMouseButton ? 2 : 1;
        final chained = _lastClickStamp != null &&
            _lastClickPosition != null &&
            button == _lastButton &&
            e.timeStamp - _lastClickStamp! <= kDoubleTapTimeout &&
            (e.position - _lastClickPosition!).distance <= kDoubleTapSlop;
        _clickCount = chained ? _clickCount + 1 : 1;
        _lastClickStamp = e.timeStamp;
        _lastClickPosition = e.position;
        _lastButton = button;
        if (forwardsControlMouseDown) {
          final event = VEvent()
            ..button = _lastButton
            ..x = e.localPosition.dx.round()
            ..y = e.localPosition.dy.round()
            ..count = _clickCount;
          this.widget.sendMouseMouseDown(state, event);
        }
      },
      onPointerUp: (e) {
        final event = VEvent()
          ..button = _lastButton
          ..x = e.localPosition.dx.round()
          ..y = e.localPosition.dy.round()
          ..count = _clickCount == 0 ? 1 : _clickCount;
        this.widget.sendMouseMouseUp(state, event);
      },
      onPointerMove: (e) {
        final event = VEvent()
          ..x = e.localPosition.dx.round()
          ..y = e.localPosition.dy.round();
        sendThrottledDragMove(state, event);
      },
      child: MouseRegion(
        onEnter: (_) =>
            HoverExclusivityArbiter.instance.setActive(this, hoverDepth, true),
        onExit: (_) {
          _hoverTimer?.cancel();
          HoverExclusivityArbiter.instance.setActive(this, hoverDepth, false);
        },
        onHover: (e) {
          final event = VEvent()
            ..x = e.localPosition.dx.round()
            ..y = e.localPosition.dy.round();
          sendThrottledMouseMove(state, event);
          _resetHoverTimer(state, event);
        },
        child: widget,
      ),
    );

    return ControlNestingScope(
      depth: hoverDepth + 1,
      child: blockWhenDisabled(widget),
    );
  }

  Widget applyMenu(Widget child) {
    final menu = state.menu;
    if (menu == null) {
      return child;
    }

    void openMenuAt(Offset localPosition) {
      final menuState = _menuKey.currentState;
      if (menuState != null && menuState is MenuImpl) {
        menuState.openContextMenuAt(context, localPosition);
      }
    }

    return GestureDetector(
      onSecondaryTapUp: (details) => openMenuAt(details.localPosition),
      onTapUp: (details) {
        // macOS treats Ctrl+Click as a secondary click.
        if (defaultTargetPlatform == TargetPlatform.macOS &&
            HardwareKeyboard.instance.isControlPressed) {
          openMenuAt(details.localPosition);
        }
      },
      child: Stack(
        children: [
          child,
          MenuSwt(key: _menuKey, value: menu),
        ],
      ),
    );
  }
}
