import 'package:flutter/material.dart';
import 'package:flutter/widgets.dart';
import '../gen/control.dart';
import '../gen/menu.dart';
import '../gen/swt.dart';
import '../gen/widget.dart';
import '../styles.dart';
import '../impl/menu_evolve.dart';
import '../theme/theme_extensions/tooltip_theme_extension.dart';
import 'widget_config.dart';

abstract class ControlImpl<T extends ControlSwt, V extends VControl>
    extends WidgetSwtState<T, V> {

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

  Widget wrap(Widget widget) {
    if (state.style.has(SWT.BORDER)) {
      widget = Container(
          decoration:
              BoxDecoration(border: Border.all(width: 0.5, color: Colors.grey)),
          child: widget);
    }

    if (state.visible != null && !state.visible!) {
      return Visibility(
        visible: false,
        child: widget,
      );
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

    return widget;
  }

  Widget applyMenu(Widget child) {
    final menu = state.menu;
    if (menu == null) {
      return child;
    }

    final menuKey = GlobalKey<State<MenuSwt>>();
    final stackKey = GlobalKey();

    return Stack(
      key: stackKey,
      children: [
        GestureDetector(
          onSecondaryTapUp: (details) {
            final RenderBox? stackBox = stackKey.currentContext?.findRenderObject() as RenderBox?;
            if (stackBox != null) {
              final localPosition = stackBox.globalToLocal(details.globalPosition);
              final menuState = menuKey.currentState;
              if (menuState != null && menuState is MenuImpl) {
                menuState.openContextMenuAt(context, localPosition);
              }
            }
          },
          child: child,
        ),
        MenuSwt(key: menuKey, value: menu),
      ],
    );
  }
}
