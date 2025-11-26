import 'package:flutter/material.dart';
import '../gen/menu.dart';
import '../gen/menuitem.dart';
import '../gen/point.dart';
import '../gen/swt.dart';
import '../gen/widget.dart';
import '../styles.dart';
import 'color_utils.dart';

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

  void _registerPendingChange(void Function() callback) {
    _pendingChanges.add(callback);
  }

  void _sendPendingChanges() {
    for (final callback in _pendingChanges) {
      callback();
    }
    _pendingChanges.clear();
  }

  void openContextMenuAt(BuildContext context, Offset position) {
    if (_menuController.isOpen) {
      _menuController.close();
    } else {
      _menuController.open(position: position);
    }
  }

  @override
  Widget build(BuildContext context) {
    state.enabled ??= true;
    state.visible ??= false;
    state.orientation ??= SWT.LEFT_TO_RIGHT;

    final style = StyleBits(state.style);
    final isBar = style.has(SWT.BAR);
    final isDropDown = style.has(SWT.DROP_DOWN);
    final isPopUp = style.has(SWT.POP_UP);

    if (isPopUp) {
      return _buildPopupMenu();
    } else if (isDropDown) {
      return _buildDropDownMenu();
    }

    if (!state.visible!) {
      return const SizedBox.shrink();
    }

    if (isBar) {
      return _buildMenuBar();
    }

    return const SizedBox.shrink();
  }

  Widget _buildMenuBar() {
    final menuItems = getMenuItems();
    final backgroundColor = getBackground();
    final foregroundColor = getForeground();
    final borderColor = getBorderColor();

    return Container(
      height: 28,
      decoration: BoxDecoration(
        color: backgroundColor,
        border: Border(
          bottom: BorderSide(color: borderColor, width: 1),
        ),
      ),
      child: Row(
        children: menuItems.map((item) => _buildMenuBarItem(item, foregroundColor)).toList(),
      ),
    );
  }

  Widget _buildMenuBarItem(VMenuItem item, Color foregroundColor) {
    final style = StyleBits(item.style);

    if (style.has(SWT.SEPARATOR)) {
      return Container(
        width: 1,
        margin: const EdgeInsets.symmetric(horizontal: 4, vertical: 4),
        color: getBorderColor(),
      );
    }

    if (item.menu != null) {
      return MenuAnchor(
        style: MenuStyle(
          backgroundColor: MaterialStateProperty.all(getBackground()),
        ),
        onOpen: () {
          widget.sendMenuShow(state, null);
        },
        onClose: () {
          _sendPendingChanges();
        },
        menuChildren: [
          MenuChangeNotifier(
            registerPendingChange: _registerPendingChange,
            menuState: _menuState,
            child: Column(
              mainAxisSize: MainAxisSize.min,
              children: _buildMenuChildren(item.menu!.items ?? []),
            ),
          ),
        ],
        builder: (context, controller, child) {
          return InkWell(
            onTap: (item.enabled ?? true)
                ? () {
                    if (controller.isOpen) {
                      controller.close();
                    } else {
                      controller.open();
                    }
                  }
                : null,
            child: Padding(
              padding: const EdgeInsets.symmetric(horizontal: 12.0, vertical: 4.0),
              child: Text(
                item.text ?? '',
                style: TextStyle(
                  color: foregroundColor,
                  fontSize: 14,
                ),
              ),
            ),
          );
        },
      );
    }

    return InkWell(
      onTap: (item.enabled ?? true) ? () {} : null,
      child: Padding(
        padding: const EdgeInsets.symmetric(horizontal: 12.0, vertical: 4.0),
        child: Text(
          item.text ?? '',
          style: TextStyle(
            color: foregroundColor,
            fontSize: 14,
          ),
        ),
      ),
    );
  }

  Widget _buildPopupMenu() {
    final menuItems = getMenuItems();
    final backgroundColor = getBackground();

    if (state.visible == true && !_menuController.isOpen) {
      WidgetsBinding.instance.addPostFrameCallback((_) {
        if (mounted && !_menuController.isOpen && state.visible == true) {
          Offset menuPosition;
          final location = state.location;
          if (location != null) {
            menuPosition = Offset(location.x.toDouble(), location.y.toDouble());
          } else {
            menuPosition = const Offset(100, 100);
          }
          _menuController.open(position: menuPosition);
        }
      });
    }

    return MenuAnchor(
      controller: _menuController,
      style: MenuStyle(
        backgroundColor: MaterialStateProperty.all(backgroundColor),
        elevation: MaterialStateProperty.all(8.0),
      ),
      alignmentOffset: const Offset(0, 0),
      anchorTapClosesMenu: true,
      onOpen: () {
        widget.sendMenuShow(state, null);
      },
      onClose: () {
        _sendPendingChanges();
        if (state.visible == true) {
          state.visible = false;
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
      builder: (context, controller, child) {
        return const SizedBox.shrink();
      },
    );
  }

  Widget _buildDropDownMenu() {
    return _buildPopupMenu();
  }

  List<Widget> _buildMenuChildren(List<VMenuItem> items) {
    return items.map((item) => MenuItemSwt(value: item)).toList();
  }

  String _formatAccelerator(int accelerator) {
    return '';
  }

  List<VMenuItem> getMenuItems() {
    return state.items ?? [];
  }
}
