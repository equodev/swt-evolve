import 'package:flutter/material.dart';
import '../comm/comm.dart';
import '../gen/menu.dart';
import '../gen/menuitem.dart';
import '../gen/swt.dart';
import '../gen/widget.dart';
import '../styles.dart';
import '../theme/theme_extensions/menu_theme_extension.dart';
import 'decorations_align.dart';
import 'widget_config.dart';
import 'utils/text_utils.dart';
import 'utils/pointer.dart';

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
  final VoidCallback closeMenu;
  // The item MenuItemImpl should build with this FocusNode (and autofocus: true) -- the menu's
  // content FocusScope is built with skipTraversal: true (Flutter's own
  // material/menu_anchor.dart), so arrow keys never auto-enter it on their own. Without one item
  // actively claiming focus, nothing inside the menu is ever focusable at all (see
  // _MenuItemRow), so neither arrow-key navigation nor Enter-to-activate has anything to act on.
  // MenuImpl also explicitly re-requests focus on this exact node a frame after opening (see
  // MenuImpl._focusFirstItemNextFrame) rather than relying on Focus(autofocus:)'s own timing,
  // which was observed to lose a race against the menu's anchor focus node.
  final VMenuItem? autofocusItem;
  final FocusNode? autofocusItemFocusNode;

  const MenuChangeNotifier({
    Key? key,
    required this.registerPendingChange,
    required this.menuState,
    required this.closeMenu,
    this.autofocusItem,
    this.autofocusItemFocusNode,
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
  // Fallback target if the menu has no focusable item at all (e.g. every item disabled) --
  // RawMenuAnchor.open() only requests focus for an anchor when a childFocusNode is supplied
  // (widgets/raw_menu_anchor.dart: `if (_isRootOverlayAnchor) { widget.childFocusNode?.requestFocus(); }`),
  // but this is deliberately NOT wired up as that childFocusNode: doing so raced against
  // _firstItemFocusNode's own autofocus (whichever requested focus later won, nondeterministically),
  // so instead nothing auto-requests it and _focusFirstItemNextFrame() below is the only thing
  // that ever calls requestFocus() on either node, after the frame settles.
  final FocusNode _popupAnchorFocusNode = FocusNode(debugLabel: 'MenuAnchor.popup');
  // The actual keyboard-entry point into the menu: see MenuChangeNotifier.autofocusItemFocusNode.
  final FocusNode _firstItemFocusNode = FocusNode(debugLabel: 'MenuItem.autofocus');
  // True only while the open menu was opened by Java (the `visible && !isOpen` branch below). A
  // context menu opened imperatively from Dart (openContextMenuAt, on right-click) keeps
  // state.visible == false the whole time, so the symmetric close must NOT read that steady false
  // as a Java-driven setVisible(false) — otherwise the first state push after SWT.Show closes the
  // menu the instant it appears (the context-menu flicker regression).
  bool _openedFromVisibleFlag = false;

  @override
  void initState() {
    super.initState();
    EquoCommService.onRaw(
      "${state.swt}/${state.id}/closeMenu",
          (_) {
        if (_menuController.isOpen) {
          _menuController.close();
        }
      },
    );
  }

  @override
  void dispose() {
    _popupAnchorFocusNode.dispose();
    _firstItemFocusNode.dispose();
    super.dispose();
  }

  void openContextMenuAt(BuildContext context, Offset position) {
    if (!_menuController.isOpen) {
      _openedFromVisibleFlag = false;
      _menuController.open(position: position);
      _focusFirstItemNextFrame();
    }
  }

  // Runs after the overlay's first frame (build + layout + paint) so it always has the final
  // word over both RawMenuAnchor's own (now-unused) anchor-focus logic and _MenuItemRow's
  // Focus(autofocus:) -- relying on either of those alone was a race with no guaranteed winner.
  void _focusFirstItemNextFrame() {
    WidgetsBinding.instance.addPostFrameCallback((_) {
      if (!mounted || !_menuController.isOpen) return;
      if (_firstItemFocusNode.context != null) {
        _firstItemFocusNode.requestFocus();
      } else {
        _popupAnchorFocusNode.requestFocus();
      }
    });
  }

  @override
  Widget build(BuildContext context) {
    final widgetTheme = Theme.of(context).extension<MenuThemeExtension>()!;

    final enabled = state.enabled ?? false;
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

    if (!isBar && !visible) {
      return const SizedBox.shrink();
    }

    if (isBar) {
      return _buildMenuBar(context, widgetTheme, enabled, visible);
    }

    return const SizedBox.shrink();
  }

  Widget _buildMenuBar(
      BuildContext context,
      MenuThemeExtension widgetTheme,
      bool enabled,
      bool visible,
      ) {
    final menuItems = _getMenuItems();
    final align = getConfigFlags().decorations_align ?? DecorationsAlign.hleft;
    final alignRight = align == DecorationsAlign.hright;
    final backgroundColor = enabled
        ? widgetTheme.menuBarBackgroundColor
        : widgetTheme.disabledBackgroundColor;
    final textColor = enabled
        ? widgetTheme.textColor
        : widgetTheme.disabledTextColor;
    final borderColor = enabled
        ? widgetTheme.menuBarBorderColor
        : widgetTheme.disabledBorderColor;

    final textStyle =
        widgetTheme.textStyle?.copyWith(color: textColor) ??
            TextStyle(color: textColor);

    Widget menuBar = AnimatedContainer(
      duration: widgetTheme.animationDuration,
      height: widgetTheme.menuBarHeight,
      decoration: BoxDecoration(
        color: backgroundColor,
        border: Border(
          bottom: BorderSide(
            color: borderColor,
            width: enabled ? widgetTheme.borderWidth : 0.0,
          ),
        ),
      ),
      child: Row(
        mainAxisAlignment:
        alignRight ? MainAxisAlignment.end : MainAxisAlignment.start,
        children: menuItems
            .map(
              (item) => _MenuBarItem(
            item: item,
            widgetTheme: widgetTheme,
            textStyle: textStyle,
            textColor: textColor,
            borderColor: borderColor,
            onMenuShow: () => widget.sendMenuShow(state, null),
            onMenuHide: () => _sendPendingChanges(),
            registerPendingChange: _registerPendingChange,
            menuState: _menuState,
          ),
        )
            .toList(),
      ),
    );

    return tagSemantics(menuBar);
  }

  Widget _buildPopupMenu(
      BuildContext context,
      MenuThemeExtension widgetTheme,
      bool enabled,
      bool visible,
      ) {
    final menuItems = _getMenuItems();
    final backgroundColor = enabled
        ? widgetTheme.popupBackgroundColor
        : widgetTheme.disabledBackgroundColor;
    final location = state.location;

    if (visible && !_menuController.isOpen) {
      WidgetsBinding.instance.addPostFrameCallback((_) {
        if (mounted && !_menuController.isOpen && visible) {
          final menuPosition = location != null
              ? Offset(location.x.toDouble(), location.y.toDouble())
              : const Offset(100, 100);
          _menuController.open(position: menuPosition);
          _openedFromVisibleFlag = true;
          _focusFirstItemNextFrame();
        }
      });
    } else if (_openedFromVisibleFlag && !visible && _menuController.isOpen) {
      // Symmetric Java-driven close (Menu.setVisible(false)): the open above is imperative, so
      // the close must be too — without it a popup opened from Java can never be closed from
      // Java, and its modal barrier keeps swallowing every later click.
      WidgetsBinding.instance.addPostFrameCallback((_) {
        if (mounted && _menuController.isOpen && !(state.visible ?? false)) {
          _menuController.close();
        }
      });
    }

    // The popup ANCHOR itself is deliberately untagged: its semantics node serves no locator
    // (items carry their own MenuItem/ identifiers; state queries don't need the DOM), and the
    // engine reuses a stale full-overlay rect for it across open/close cycles — the node then
    // intercepts pointer events over the page (after a close) or over the menu's own items
    // (after a reopen). No node, no stale rect.
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
        _openedFromVisibleFlag = false;
        if (visible) {
          visible = false;
          // Sync the SERIALIZED state too: build() reads state.visible, and with it stuck true
          // the open guard re-opens the menu on every rebuild (Java can't help — its own field
          // already went false on the Hide event, so a later setVisible(false) is a no-op that
          // never serializes).
          state.visible = false;
          widget.sendMenuHide(state, null);
        }
      },
      menuChildren: [
        pointerInterceptor(MenuChangeNotifier(
          registerPendingChange: _registerPendingChange,
          menuState: _menuState,
          closeMenu: _menuController.close,
          autofocusItem: _firstFocusableMenuItem(menuItems),
          autofocusItemFocusNode: _firstItemFocusNode,
          // No extra Semantics wrapper: with the anchor untagged, each item's own tagSemantics
          // materializes on its own (a tagged anchor merges them away; an added wrapper here
          // duplicates every item node instead).
          child: Column(
            mainAxisSize: MainAxisSize.min,
            children: menuItems
                .map((item) => MenuItemSwt(key: ValueKey(item.id), value: item))
                .toList(),
          ),
        )),
      ],
      // _popupAnchorFocusNode only needs to be attached (not auto-requested -- see
      // _focusFirstItemNextFrame) so it is a valid fallback target if the menu ends up with no
      // focusable item at all.
      builder: (context, controller, child) =>
          Focus(focusNode: _popupAnchorFocusNode, child: const SizedBox.shrink()),
    );
  }

  void _registerPendingChange(void Function() callback) {
    _pendingChanges.add(callback);
  }

  /// The item MenuChangeNotifier.autofocusItem should point at: the first enabled, non-separator,
  /// non-cascade item, so a freshly-opened menu always has something keyboard-focused to
  /// navigate from. Cascade (SWT.CASCADE, has a submenu) items render through
  /// _CascadeMenuItemRow, a separate widget that uses Flutter's own SubmenuButton -- already
  /// natively focusable -- rather than our custom _MenuItemRow, so it would never honor
  /// autofocusItemFocusNode at all if picked here.
  VMenuItem? _firstFocusableMenuItem(List<VMenuItem> menuItems) {
    for (final item in menuItems) {
      final style = StyleBits(item.style);
      if (style.has(SWT.SEPARATOR)) continue;
      if (style.has(SWT.CASCADE) && item.menu != null) continue;
      if (item.enabled == false) continue;
      return item;
    }
    return null;
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
  bool _itemsLoaded = false;
  bool _pendingOpen = false;
  List<Widget> _menuChildren = const [SizedBox.shrink()];
  final MenuController _subMenuController = MenuController();

  @override
  void initState() {
    super.initState();
    final preItems = widget.item.menu?.items;
    if (preItems != null && preItems.isNotEmpty) {
      _itemsLoaded = true;
      _menuChildren = preItems
          .map((item) => MenuItemSwt(key: ValueKey(item.id), value: item))
          .toList();
    }
  }

  @override
  void dispose() {
    if (widget.item.menu != null) {
      EquoCommService.remove("Menu/${widget.item.menu!.id}");
    }
    super.dispose();
  }

  void _requestItems() {
    if (_itemsLoaded || widget.item.menu == null) return;
    final subMenu = widget.item.menu!;
    final channelName = "Menu/${subMenu.id}";
    EquoCommService.on<VMenu>(channelName, (VMenu updatedMenu) {
      EquoCommService.remove(channelName);
      if (!mounted) return;
      final items = (updatedMenu.items ?? [])
          .map((item) => MenuItemSwt(key: ValueKey(item.id), value: item))
          .toList();
      setState(() {
        _itemsLoaded = true;
        _menuChildren = items.isEmpty ? const [SizedBox.shrink()] : items;
      });
      if (_pendingOpen) {
        _pendingOpen = false;
        WidgetsBinding.instance.addPostFrameCallback((_) {
          if (mounted && !_subMenuController.isOpen) {
            _subMenuController.open();
          }
        });
      }
    });
    MenuSwt<VMenu>(value: subMenu).sendMenuShow(subMenu, null);
  }

  @override
  Widget build(BuildContext context) {
    final style = StyleBits(widget.item.style);
    final enabled = widget.item.enabled ?? false;
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
        controller: _subMenuController,
        style: MenuStyle(
          backgroundColor: WidgetStateProperty.all(
            widget.widgetTheme.popupBackgroundColor,
          ),
          elevation: WidgetStateProperty.all(widget.widgetTheme.popupElevation),
          padding: WidgetStateProperty.all(widget.widgetTheme.popupPadding),
          shape: WidgetStateProperty.all(
            RoundedRectangleBorder(
              borderRadius: BorderRadius.circular(
                widget.widgetTheme.borderRadius,
              ),
            ),
          ),
        ),
        onOpen: widget.onMenuShow,
        onClose: widget.onMenuHide,
        menuChildren: [
          pointerInterceptor(MenuChangeNotifier(
            registerPendingChange: widget.registerPendingChange,
            menuState: widget.menuState,
            closeMenu: _subMenuController.close,
            child: Column(
              mainAxisSize: MainAxisSize.min,
              children: _menuChildren,
            ),
          )),
        ],
        builder: (context, controller, child) {
          return MouseRegion(
            onEnter: (_) {
              setState(() => _isHovered = true);
              _requestItems();
            },
            onExit: (_) => setState(() => _isHovered = false),
            child: AnimatedContainer(
              duration: widget.widgetTheme.animationDuration,
              color: _isHovered && enabled
                  ? widget.widgetTheme.hoverBackgroundColor
                  : Colors.transparent,
              child: Listener(
                onPointerUp: (e) {
                  if (enabled && e.buttons == 0) {
                    if (controller.isOpen) {
                      controller.close();
                    } else if (_itemsLoaded) {
                      controller.open();
                    } else {
                      _pendingOpen = true;
                      _requestItems();
                    }
                  }
                },
                child: GestureDetector(
                  child: Padding(
                    padding: widget.widgetTheme.menuBarItemPadding,
                    child: Text(
                      stripAccelerators(widget.item.text),
                      style: widget.textStyle.copyWith(
                        color: enabled
                            ? widget.textColor
                            : widget.widgetTheme.disabledTextColor,
                      ),
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
        child: Listener(
          onPointerUp: (e) {},
          child: GestureDetector(
            child: Padding(
              padding: widget.widgetTheme.menuBarItemPadding,
              child: Text(
                stripAccelerators(widget.item.text),
                style: widget.textStyle.copyWith(
                  color: enabled
                      ? widget.textColor
                      : widget.widgetTheme.disabledTextColor,
                ),
              ),
            ),
          ),
        ),
      ),
    );
  }
}