import 'package:flutter/material.dart';
import '../gen/menu.dart';
import '../gen/menuitem.dart';
import '../gen/swt.dart';
import '../gen/widget.dart';
import '../styles.dart';
import '../theme/theme_extensions/menu_theme_extension.dart';

class MenuState {
  final Map<VMenuItem, void Function(bool)> radioItemCallbacks = {};

  void registerRadioCallback(VMenuItem item, void Function(bool) callback) {
    radioItemCallbacks[item] = callback;
  }

  void notifyRadioSelected(VMenuItem selectedItem) {
    for (final entry in radioItemCallbacks.entries) {
      if (entry.key != selectedItem) {
        entry.value(false);
      }
    }
  }
}

class MenuChangeNotifier extends InheritedWidget {
  final void Function(void Function()) registerPendingChange;
  final MenuState menuState;

  const MenuChangeNotifier({
    Key? key,
    required this.registerPendingChange,
    required this.menuState,
    required Widget child,
  }) : super(key: key, child: child);

  static MenuChangeNotifier? of(BuildContext context) {
    return context.dependOnInheritedWidgetOfExactType<MenuChangeNotifier>();
  }

  @override
  bool updateShouldNotify(MenuChangeNotifier oldWidget) => false;
}

class MenuImpl<T extends MenuSwt, V extends VMenu>
    extends WidgetSwtState<T, V> {
  final MenuController _menuController = MenuController();
  final List<void Function()> _pendingChanges = [];
  final MenuState _menuState = MenuState();

  void openContextMenuAt(BuildContext context, Offset position) {
    if (_menuController.isOpen) {
      _menuController.close();
    } else {
      _menuController.open(position: position);
    }
  }

  @override
  Widget build(BuildContext context) {
    final widgetTheme = Theme.of(context).extension<MenuThemeExtension>()!;

    final enabled = state.enabled ?? true;
    final visible = state.visible ?? false;
    final orientation = state.orientation ?? SWT.LEFT_TO_RIGHT;

    final style = StyleBits(state.style);
    final isBar = style.has(SWT.BAR);
    final isDropDown = style.has(SWT.DROP_DOWN);
    final isPopUp = style.has(SWT.POP_UP);

    if (isPopUp) {
      return _buildPopupMenu(context, widgetTheme, enabled, visible);
    } else if (isDropDown) {
      return _buildPopupMenu(context, widgetTheme, enabled, visible);
    }

    if (!visible) {
      return const SizedBox.shrink();
    }

    if (isBar) {
      return _buildMenuBar(context, widgetTheme, enabled, visible);
    }

    return const SizedBox.shrink();
  }

  Widget _buildMenuBar(BuildContext context, MenuThemeExtension widgetTheme, bool enabled, bool visible) {
    final menuItems = _getMenuItems();
    final backgroundColor = enabled ? widgetTheme.menuBarBackgroundColor : widgetTheme.disabledBackgroundColor;
    final textColor = enabled ? widgetTheme.textColor : widgetTheme.disabledTextColor;
    final borderColor = enabled ? widgetTheme.menuBarBorderColor : widgetTheme.disabledBorderColor;

    final textStyle = widgetTheme.textStyle?.copyWith(color: textColor) ?? TextStyle(color: textColor);

    Widget menuBar = AnimatedContainer(
      duration: widgetTheme.animationDuration,
      height: widgetTheme.menuBarHeight,
      decoration: BoxDecoration(
        color: backgroundColor,
        border: Border(
          bottom: BorderSide(color: borderColor, width: enabled ? widgetTheme.borderWidth : 0.0),
        ),
      ),
      child: Row(
        children: menuItems.map((item) => _MenuBarItem(
          item: item,
          widgetTheme: widgetTheme,
          textStyle: textStyle,
          textColor: textColor,
          borderColor: borderColor,
          onMenuShow: () => widget.sendMenuShow(state, null),
          onMenuHide: () => _sendPendingChanges(),
          registerPendingChange: _registerPendingChange,
          menuState: _menuState,
        )).toList(),
      ),
    );

    return menuBar;
  }

  Widget _buildPopupMenu(BuildContext context, MenuThemeExtension widgetTheme, bool enabled, bool visible) {
    final menuItems = _getMenuItems();
    final backgroundColor = enabled ? widgetTheme.popupBackgroundColor : widgetTheme.disabledBackgroundColor;
    final location = state.location;

    if (visible && !_menuController.isOpen) { 
      WidgetsBinding.instance.addPostFrameCallback((_) {
        if (mounted && !_menuController.isOpen && visible) {
          final menuPosition = location != null
              ? Offset(location.x.toDouble(), location.y.toDouble())
              : const Offset(100, 100);
          _menuController.open(position: menuPosition);
        }
      });
    }

    return MenuAnchor(
      controller: _menuController,
      style: MenuStyle(
        backgroundColor: WidgetStateProperty.all(backgroundColor),
        elevation: WidgetStateProperty.all(widgetTheme.popupElevation),
        padding: WidgetStateProperty.all(widgetTheme.popupPadding),
        shape: WidgetStateProperty.all(
          RoundedRectangleBorder(
            borderRadius: BorderRadius.circular(widgetTheme.borderRadius),
          ),
        ),
      ),
      alignmentOffset: Offset.zero,
      anchorTapClosesMenu: false,
      onOpen: () => widget.sendMenuShow(state, null),
      onClose: () {
        _sendPendingChanges();
        if (visible) {
          visible = false;
          widget.sendMenuHide(state, null);
        }
      },
      menuChildren: [
        MenuChangeNotifier(
          registerPendingChange: _registerPendingChange,
          menuState: _menuState,
          child: Column(
            mainAxisSize: MainAxisSize.min,
            children: menuItems.map((item) => MenuItemSwt(value: item)).toList(),
          ),
        ),
      ],
      builder: (context, controller, child) => const SizedBox.shrink(),
    );
  }

  void _registerPendingChange(void Function() callback) {
    _pendingChanges.add(callback);
  }

  void _sendPendingChanges() {
    for (final callback in _pendingChanges) {
      callback();
    }
    _pendingChanges.clear();
  }

  List<VMenuItem> _getMenuItems() {
    return state.items ?? [];
  }
}

class _MenuBarItem extends StatefulWidget {
  final VMenuItem item;
  final MenuThemeExtension widgetTheme;
  final TextStyle textStyle;
  final Color textColor;
  final Color borderColor;
  final VoidCallback onMenuShow;
  final VoidCallback onMenuHide;
  final void Function(void Function()) registerPendingChange;
  final MenuState menuState;

  const _MenuBarItem({
    required this.item,
    required this.widgetTheme,
    required this.textStyle,
    required this.textColor,
    required this.borderColor,
    required this.onMenuShow,
    required this.onMenuHide,
    required this.registerPendingChange,
    required this.menuState,
  });

  @override
  State<_MenuBarItem> createState() => _MenuBarItemState();
}

class _MenuBarItemState extends State<_MenuBarItem> {
  bool _isHovered = false;

  @override
  Widget build(BuildContext context) {
    final style = StyleBits(widget.item.style);
    final enabled = widget.item.enabled ?? true;
    final widgetTheme = Theme.of(context).extension<MenuThemeExtension>()!;
    if (style.has(SWT.SEPARATOR)) {
      return Container(
        width: 1,
        margin: widgetTheme.menuBarItemMargin,
        color: widgetTheme.borderColor,
      );
    }

    if (widget.item.menu != null) {
      return MenuAnchor(
        style: MenuStyle(
          backgroundColor: WidgetStateProperty.all(widget.widgetTheme.popupBackgroundColor),
          elevation: WidgetStateProperty.all(widget.widgetTheme.popupElevation),
          padding: WidgetStateProperty.all(widget.widgetTheme.popupPadding),
          shape: WidgetStateProperty.all(
            RoundedRectangleBorder(
              borderRadius: BorderRadius.circular(widget.widgetTheme.borderRadius),
            ),
          ),
        ),
        onOpen: widget.onMenuShow,
        onClose: widget.onMenuHide,
        menuChildren: [
          MenuChangeNotifier(
            registerPendingChange: widget.registerPendingChange,
            menuState: widget.menuState,
            child: Column(
              mainAxisSize: MainAxisSize.min,
              children: (widget.item.menu!.items ?? [])
                  .map((item) => MenuItemSwt(value: item))
                  .toList(),
            ),
          ),
        ],
        builder: (context, controller, child) {
          return MouseRegion(
            onEnter: (_) => setState(() => _isHovered = true),
            onExit: (_) => setState(() => _isHovered = false),
            child: AnimatedContainer(
              duration: widget.widgetTheme.animationDuration,
              color: _isHovered && enabled
                  ? widget.widgetTheme.hoverBackgroundColor
                  : Colors.transparent,
              child: InkWell(
                onTap: enabled
                    ? () {
                        if (controller.isOpen) {
                          controller.close();
                        } else {
                          controller.open();
                        }
                      }
                    : null,
                child: Padding(
                  padding: widget.widgetTheme.menuBarItemPadding,
                  child: Text(
                    widget.item.text ?? '',
                    style: widget.textStyle.copyWith(
                      color: enabled
                          ? widget.textColor
                          : widget.widgetTheme.disabledTextColor,
                    ),
                  ),
                ),
              ),
            ),
          );
        },
      );
    }

    return MouseRegion(
      onEnter: (_) => setState(() => _isHovered = true),
      onExit: (_) => setState(() => _isHovered = false),
      child: AnimatedContainer(
        duration: widget.widgetTheme.animationDuration,
        color: _isHovered && enabled
            ? widget.widgetTheme.hoverBackgroundColor
            : Colors.transparent,
        child: InkWell(
          onTap: enabled ? () {} : null,
          child: Padding(
            padding: widget.widgetTheme.menuBarItemPadding,
            child: Text(
              widget.item.text ?? '',
              style: widget.textStyle.copyWith(
                color: enabled
                    ? widget.textColor
                    : widget.widgetTheme.disabledTextColor,
              ),
            ),
          ),
        ),
      ),
    );
  }
}
