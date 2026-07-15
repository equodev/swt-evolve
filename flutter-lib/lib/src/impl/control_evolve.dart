import 'dart:async';

import 'package:flutter/gestures.dart';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:flutter/widgets.dart';
import '../gen/control.dart';
import '../gen/event.dart';
import '../gen/menu.dart';
import '../gen/swt.dart';
import '../gen/widget.dart';
import '../styles.dart';
import '../impl/key_mapping.dart';
import '../impl/menu_evolve.dart';
import '../theme/theme_extensions/tooltip_theme_extension.dart';
import 'widget_config.dart';

abstract class ControlImpl<T extends ControlSwt, V extends VControl>
    extends WidgetSwtState<T, V> {

  final GlobalKey<State<MenuSwt>> _menuKey = GlobalKey<State<MenuSwt>>();
  int _lastButton = 1;
  int _lastMouseMoveMs = 0;
  int _lastDragMoveMs = 0;
  static const int _mouseMoveThrottleMs = 15;
  static const int _dragMoveThrottleMs = 15;
  // Matches DartDisplay.getToolTipTime() — fires SWT.MouseHover after stillness
  static const int _hoverDelayMs = 560;
  Timer? _hoverTimer;

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

  Widget wrap(Widget widget) {
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
      return Opacity(opacity: 0.35, child: widget);
    }

    if (state.menu != null) {
      widget = applyMenu(widget);
    }

    // Wrap with Tooltip if toolTipText is set
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

    widget = Listener(
      onPointerDown: (e) {
        _lastButton = e.buttons == kSecondaryMouseButton ? 3 : e.buttons == kMiddleMouseButton ? 2 : 1;
        if (forwardsControlMouseDown) {
          final event = VEvent()
            ..button = _lastButton
            ..x = e.localPosition.dx.round()
            ..y = e.localPosition.dy.round()
            ..count = 2;
          this.widget.sendMouseMouseDown(state, event);
        }
      },
      onPointerUp: (e) {
        final event = VEvent()
          ..button = _lastButton
          ..x = e.localPosition.dx.round()
          ..y = e.localPosition.dy.round()
          ..count = 1;
        this.widget.sendMouseMouseUp(state, event);
      },
      onPointerMove: (e) {
        final event = VEvent()
          ..x = e.localPosition.dx.round()
          ..y = e.localPosition.dy.round();
        sendThrottledDragMove(state, event);
      },
      child: MouseRegion(
        onEnter: (_) => this.widget.sendMouseTrackMouseEnter(state, null),
        onExit: (_) {
          _hoverTimer?.cancel();
          this.widget.sendMouseTrackMouseExit(state, null);
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

    return widget;
  }

  Widget applyMenu(Widget child) {
    final menu = state.menu;
    if (menu == null) {
      return child;
    }

    return GestureDetector(
      onSecondaryTapUp: (details) {
        final menuState = _menuKey.currentState;
        if (menuState != null && menuState is MenuImpl) {
          menuState.openContextMenuAt(context, details.localPosition);
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
