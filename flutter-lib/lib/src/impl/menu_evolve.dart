import 'dart:async';

import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import '../theme/theme_settings/menu_theme_settings.dart';
import '../comm/comm.dart';
import '../comm/v_registry.dart';
import '../gen/menu.dart';
import 'utils/window_origin.dart';
import '../gen/menuitem.dart';
import '../gen/swt.dart';
import '../gen/widget.dart';
import '../styles.dart';
import '../theme/theme_extensions/menu_theme_extension.dart';
import 'decorations_align.dart';
import 'decorations_evolve.dart';
import 'widget_config.dart';
import 'utils/hosted_context_menu.dart';
import 'utils/menu_mnemonics.dart';
import 'utils/mnemonic_text.dart';
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

/// Tracks whichever standalone popup Menu (POP_UP or DROP_DOWN style, built by
/// [MenuImpl._buildPopupMenu]) is currently open, so a control that re-triggers its own "show"
/// action can close it directly instead of asking Java to show it again.
///
/// Needed because [MenuAnchor.consumeOutsideTap] dismissal is built on [TapRegion], which only
/// sees real pointer routing. Under forced accessibility (every E2E run), a tap on a
/// semantics-tagged control -- including the arrow that opened this same popup -- is delivered as
/// a [SemanticsAction] and never reaches [TapRegion] at all, so the popup never sees that tap as
/// "outside" and never closes on its own; the arrow's own handler asks Java to show it again,
/// which is a no-op because it is already visible.
class OpenPopupMenuTracker {
  static MenuImpl? _current;

  /// Closes the currently tracked popup, if one is open. Returns whether it did.
  static bool closeIfOpen() {
    final current = _current;
    if (current == null || !current.mounted) return false;
    if (!current._menuController.isOpen) return false;
    current._menuController.close();
    return true;
  }
}

/// Follows the items of a cascade's menu, which no widget of its own renders. A cascade asks for
/// its items by sending Show, and JFace fills or changes them in answer; that answer is a delivery
/// to the menu, so whoever draws the cascade has to listen for it. [onItems] is called once the
/// delivery has been applied whole, because the items it names are described after it.
class MenuItemsWatch {
  MenuItemsWatch(VMenu menu, this.onItems) : _channel = VRegistry.channelOf(menu) {
    VRegistry.instance.watch(_channel, _changed);
  }

  final String _channel;
  final void Function(List<VMenuItem> items) onItems;
  bool _disposed = false;

  bool watches(VMenu menu) => _channel == VRegistry.channelOf(menu);

  void _changed(VChange change) {
    scheduleMicrotask(() {
      if (_disposed) return;
      final menu = VRegistry.instance.valueOn(_channel);
      if (menu is! VMenu) return;
      onItems([
        for (final item in menu.items ?? const <VMenuItem>[])
          switch (VRegistry.instance.valueOn(VRegistry.channelOf(item))) {
            final VMenuItem held => held,
            _ => item,
          },
      ]);
    });
  }

  void dispose() {
    _disposed = true;
    VRegistry.instance.unwatch(_channel, _changed);
  }
}

/// Every mounted menu anchor's controller, so the Display can tell when a menu holds the keyboard.
class OpenMenus {
  static final Set<MenuController> _controllers = {};
  // Menu bars in keyboard mode: they hold the keyboard as an open menu does.
  static final Set<Object> _activeBars = {};

  static bool get any => _activeBars.isNotEmpty || _anyOpen;

  static bool get _anyOpen => _controllers.any((controller) => controller.isOpen);

  /// Counts [controller]'s menu from its anchor's mount to its [remove].
  static void add(MenuController controller) => _controllers.add(controller);

  static void remove(MenuController controller) => _controllers.remove(controller);

  static final Set<Object> _opening = {};
  static final List<KeyEvent> _held = [];

  /// Where a held key goes when no menu took it: the Display's forwarding to Java.
  static bool Function(KeyEvent event)? forwardHeld;

  /// True from a pop-up's request to Java until it is open and focused. Natively the menu owns the
  /// keyboard from the moment it shows; here a key in that window would reach the focused control.
  static bool get opening => _opening.isNotEmpty;

  /// True while keys wait for a pop-up: while it opens, and until the keys held meanwhile have been
  /// played. A key that comes in that time waits behind them, so each reaches the menu, or Java once
  /// the menu is down, in the order it was typed.
  static bool get holding => _opening.isNotEmpty || _held.isNotEmpty;

  static void startOpening(Object menu) => _opening.add(menu);

  static void hold(KeyEvent event) => _held.add(event);

  /// Ends [menu]'s opening and plays the keys held meanwhile, in order, to the open menu's focus or,
  /// once none is open (an Escape among them closed it), to Java.
  static void endOpening(Object menu) {
    if (!_opening.remove(menu) || _opening.isNotEmpty || _held.isEmpty) return;
    // A focus requested this frame is applied in a microtask, which runs before this one.
    scheduleMicrotask(_playNextHeld);
  }

  /// One key a frame, as typed keys arrive: what the menu does with one (moving the focus, closing)
  /// settles before the next is read.
  static void _playNextHeld() {
    if (_held.isEmpty || opening) return;
    final event = _held.removeAt(0);
    if (!any) {
      forwardHeld?.call(event);
      _playNextHeld();
      return;
    }
    ServicesBinding.instance.keyEventManager.keyMessageHandler?.call(KeyMessage([event], null));
    if (_held.isEmpty) return;
    WidgetsBinding.instance.addPostFrameCallback((_) => _playNextHeld());
    WidgetsBinding.instance.ensureVisualUpdate();
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
  // Holds the open menu's items, so a menu made only of cascades can focus its first one.
  final FocusNode _itemsFocusNode = FocusNode(debugLabel: 'Menu.items');
  // True only while the open menu was opened by Java (the `visible && !isOpen` branch below). A
  // context menu opened imperatively from Dart (openContextMenuAt, on right-click) keeps
  // state.visible == false the whole time, so the symmetric close must NOT read that steady false
  // as a Java-driven setVisible(false) — otherwise the first state push after SWT.Show closes the
  // menu the instant it appears (the context-menu flicker regression).
  bool _openedFromVisibleFlag = false;
  // Set while the pending fill was asked for by Java's visible flag rather than by a right-click,
  // so the symmetric Java-driven close still recognises the popup as its own.
  bool _pendingOpenFromVisibleFlag = false;
  // A Show is already on its way to Java for this opening. The anchor reports onOpen once we act on
  // the answer to that Show, and reporting it again made the application empty and refill a menu
  // that was already on screen -- the second fill is what left the redundant separators visible.
  bool _showSent = false;
  // One setVisible(true) puts a popup up once. It stays up until something takes it down, and
  // showing it again means putting it up again — which arrives as a fresh mount, because Java
  // drops a hidden popup from the display's popup set and re-adds it on the next show. Latching
  // that here keeps a state push composed before Java saw our dismissal from re-showing the menu
  // the user just closed: without it the reopen races the round trip and the popup flickers back.
  bool _shownOnce = false;
  Offset? _pendingContextMenuPosition;

  // A menu bar's keyboard mode, as on Windows: F10, a lone Alt or Escape out of a drop-down make the
  // bar active with one title current; null while it is not.
  int? _currentTitle;
  final FocusNode _barFocusNode = FocusNode(debugLabel: 'MenuBar', skipTraversal: true);
  // Where the keyboard was before the bar took it, given back when the bar leaves keyboard mode.
  FocusNode? _focusBeforeBar;
  final Map<int, GlobalKey<_MenuBarItemState>> _titleKeys = {};
  int? _activatedBarId;

  // Every mounted popup per menu: a control's own copy and the one its window renders once shown.
  static final Map<String, Set<MenuImpl>> _mounted = {};

  /// The other mounted copy of this menu that is already open, or about to be.
  MenuImpl? get _presentingPeer {
    for (final peer in _mounted[_rawChannel] ?? const <MenuImpl>{}) {
      if (identical(peer, this) || !peer.mounted) continue;
      if (peer._menuController.isOpen || peer._pendingContextMenuPosition != null) return peer;
    }
    return null;
  }

  String? _rawChannel;

  @override
  void initState() {
    super.initState();
    OpenMenus.add(_menuController);
    _subscribeRawChannels();
  }

  @override
  void didUpdateWidget(covariant T oldWidget) {
    super.didUpdateWidget(oldWidget);
    if ("${state.swt}/${state.id}" != _rawChannel) {
      _unsubscribeRawChannels();
      _subscribeRawChannels();
      _pendingContextMenuPosition = null;
      _pendingOpenFromVisibleFlag = false;
    }
  }

  void _subscribeRawChannels() {
    final channel = "${state.swt}/${state.id}";
    _rawChannel = channel;
    if ((state.style & SWT.BAR) != 0) {
      _activatedBarId = state.id;
      MenuBarActivators.add(state.id, _activateBar);
    }
    final copies = _mounted.putIfAbsent(channel, () => {});
    copies.add(this);
    if (copies.length > 1) return;
    // A channel holds one handler, so the copies share it: a second subscription would silence the first.
    EquoCommService.onRaw("$channel/shown", (_) {
      for (final copy in List.of(_mounted[channel] ?? const <MenuImpl>{})) {
        copy._onMenuShown();
      }
    });
  }

  void _unsubscribeRawChannels() {
    final channel = _rawChannel;
    if (channel == null) return;
    _rawChannel = null;
    final barId = _activatedBarId;
    if (barId != null) {
      _activatedBarId = null;
      MenuBarActivators.remove(barId, _activateBar);
    }
    final copies = _mounted[channel];
    if (copies == null || !copies.remove(this) || copies.isNotEmpty) return;
    _mounted.remove(channel);
    EquoCommService.remove("$channel/shown");
  }

  @override
  void dispose() {
    if (identical(OpenPopupMenuTracker._current, this)) {
      OpenPopupMenuTracker._current = null;
    }
    OpenMenus.remove(_menuController);
    OpenMenus.endOpening(this);
    OpenMenus._activeBars.remove(this);
    _unsubscribeRawChannels();
    _barFocusNode.dispose();
    _popupAnchorFocusNode.dispose();
    _firstItemFocusNode.dispose();
    _itemsFocusNode.dispose();
    super.dispose();
  }

  void openContextMenuAt(BuildContext context, Offset position) {
    if (_menuController.isOpen || _pendingContextMenuPosition != null) return;
    if (_presentingPeer != null) return;
    _pendingContextMenuPosition = position;
    _showSent = true;
    OpenMenus.startOpening(this);
    widget.sendMenuShow(state, null);
  }

  void _onMenuShown() {
    final position = _pendingContextMenuPosition;
    if (position == null || _menuController.isOpen || !mounted) {
      OpenMenus.endOpening(this);
      return;
    }
    _pendingContextMenuPosition = null;
    WidgetsBinding.instance.addPostFrameCallback((_) {
      if (!mounted || _menuController.isOpen) {
        OpenMenus.endOpening(this);
        return;
      }
      _openedFromVisibleFlag = _pendingOpenFromVisibleFlag;
      _pendingOpenFromVisibleFlag = false;
      _menuController.open(position: position);
      _focusFirstItemNextFrame();
    });
    // The ack arrives in a microtask, so nothing has scheduled the frame this callback needs.
    // Without this the menu silently never opens.
    WidgetsBinding.instance.ensureVisualUpdate();
  }

  // Runs after the overlay's first frame (build + layout + paint) so it always has the final
  // word over both RawMenuAnchor's own (now-unused) anchor-focus logic and _MenuItemRow's
  // Focus(autofocus:) -- relying on either of those alone was a race with no guaranteed winner.
  void _focusFirstItemNextFrame() {
    WidgetsBinding.instance.addPostFrameCallback((_) {
      if (mounted && _menuController.isOpen) _focusFirstItem();
      OpenMenus.endOpening(this);
    });
  }

  void _focusFirstItem() {
    if (_firstItemFocusNode.context != null) {
      _firstItemFocusNode.requestFocus();
      return;
    }
    final items = _itemsFocusNode.traversalDescendants;
    if (items.isNotEmpty) {
      items.first.requestFocus();
    } else {
      _popupAnchorFocusNode.requestFocus();
    }
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
    // Nothing to show: no background strip and no bottom border either, so an itemless menu bar
    // leaves no trace of itself above the toolbar.
    if (menuItems.isEmpty) {
      return const SizedBox.shrink();
    }
    _titleKeys.removeWhere((id, _) => !menuItems.any((item) => item.id == id));
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

    final stripHeight = DecorationsMenuData.of(context)?.stripHeight;
    Widget menuBar = AnimatedContainer(
      duration: widgetTheme.animationDuration,
      // The height Java laid the strip out at, which follows the monitor's scale.
      height: stripHeight != null && stripHeight > 0
          ? stripHeight.toDouble()
          : widgetTheme.menuBarHeight,
      decoration: BoxDecoration(
        color: backgroundColor,
        border: Border(
          bottom: BorderSide(
            color: borderColor,
            width: enabled ? widgetTheme.borderWidth : 0.0,
          ),
        ),
      ),
      child: Focus(
        focusNode: _barFocusNode,
        onKeyEvent: _onBarKey,
        onFocusChange: (focused) {
          if (!focused && !OpenMenus._anyOpen) _leaveBar();
        },
        child: Row(
          mainAxisAlignment:
          alignRight ? MainAxisAlignment.end : MainAxisAlignment.start,
          children: [
            for (final (index, item) in menuItems.indexed)
              _MenuBarItem(
                key: _titleKeys.putIfAbsent(item.id, GlobalKey.new),
                item: item,
                widgetTheme: widgetTheme,
                textStyle: textStyle,
                textColor: textColor,
                borderColor: borderColor,
                current: _currentTitle == index,
                onOpening: _noteFocusBeforeBar,
                onOpened: () => _setCurrentTitle(index),
                onClosed: (byKeyboard) => _onTitleClosed(index, byKeyboard),
                onMoveTitle: (step) => _moveFromOpenTitle(index, step),
                registerPendingChange: _registerPendingChange,
                menuState: _menuState,
              ),
          ],
        ),
      ),
    );

    return tagSemantics(menuBar);
  }

  _MenuBarItemState? _title(int index) {
    final items = _getMenuItems();
    if (index < 0 || index >= items.length) return null;
    return _titleKeys[items[index].id]?.currentState;
  }

  /// F10 or a lone Alt, resolved by Java: the bar takes the keyboard with its first title current.
  void _activateBar() {
    if (!mounted || OpenMenus._anyOpen) return;
    final first = _nextTitle(-1, 1);
    if (first == null) return;
    _noteFocusBeforeBar();
    _enterBar(first);
  }

  void _noteFocusBeforeBar() {
    if (_currentTitle == null && !_barFocusNode.hasFocus) {
      _focusBeforeBar = FocusManager.instance.primaryFocus;
    }
  }

  void _enterBar(int index) {
    _setCurrentTitle(index);
    _barFocusNode.requestFocus();
  }

  void _setCurrentTitle(int index) {
    if (!mounted) return;
    OpenMenus._activeBars.add(this);
    setState(() => _currentTitle = index);
  }

  void _leaveBar() {
    OpenMenus._activeBars.remove(this);
    if (_currentTitle != null && mounted) setState(() => _currentTitle = null);
    final before = _focusBeforeBar;
    _focusBeforeBar = null;
    _giveFocusBack(before);
    // A closing drop-down that took the keyboard from the bar hands it back to the bar as it goes.
    WidgetsBinding.instance.addPostFrameCallback((_) {
      if (mounted && _currentTitle == null) _giveFocusBack(before);
    });
  }

  void _giveFocusBack(FocusNode? before) {
    if (!_barFocusNode.hasPrimaryFocus) return;
    if (before != null && before.context != null) {
      before.requestFocus();
    } else {
      _barFocusNode.unfocus();
    }
  }

  // Escape out of a drop-down leaves the bar active on its title, as natively; any other close ends it.
  void _onTitleClosed(int index, bool byKeyboard) {
    if (byKeyboard) {
      _enterBar(index);
    } else {
      _leaveBar();
    }
    // A chosen item is selected after its menus are hidden, as natively.
    _sendPendingChanges();
  }

  void _moveFromOpenTitle(int index, int step) {
    final next = _nextTitle(index, step);
    if (next == null || next == index) return;
    _title(index)?._closeForNextTitle();
    _setCurrentTitle(next);
    _title(next)?._open(focusFirstItem: true);
  }

  int? _nextTitle(int from, int step) {
    final items = _getMenuItems();
    final count = items.length;
    for (var i = 1; i <= count; i++) {
      final index = ((from + step * i) % count + count) % count;
      if ((items[index].style & SWT.SEPARATOR) == 0) return index;
    }
    return null;
  }

  KeyEventResult _onBarKey(FocusNode node, KeyEvent event) {
    final current = _currentTitle;
    if (current == null) return KeyEventResult.ignored;
    final key = event.logicalKey;
    final horizontal = key == LogicalKeyboardKey.arrowLeft || key == LogicalKeyboardKey.arrowRight;
    final step = key == LogicalKeyboardKey.arrowRight ? 1 : -1;
    if (!node.hasPrimaryFocus) return KeyEventResult.ignored;
    if (event is KeyUpEvent) return KeyEventResult.handled;
    if (horizontal) {
      final next = _nextTitle(current, step);
      if (next != null) setState(() => _currentTitle = next);
    } else if (key == LogicalKeyboardKey.arrowDown ||
        key == LogicalKeyboardKey.arrowUp ||
        key == LogicalKeyboardKey.enter ||
        key == LogicalKeyboardKey.numpadEnter ||
        key == LogicalKeyboardKey.space) {
      _title(current)?._open(focusFirstItem: true);
    } else if (key == LogicalKeyboardKey.escape ||
        key == LogicalKeyboardKey.f10 ||
        key == LogicalKeyboardKey.altLeft ||
        key == LogicalKeyboardKey.altRight) {
      if (event is KeyDownEvent) _leaveBar();
    } else {
      final letter = event.character?.toLowerCase() ?? '';
      final items = _getMenuItems();
      for (final (index, item) in items.indexed) {
        final mnemonic = parseMnemonic(item.text);
        if (letter.isEmpty || mnemonic.index < 0) continue;
        if (!mnemonic.text.substring(mnemonic.index).toLowerCase().startsWith(letter)) continue;
        setState(() => _currentTitle = index);
        _title(index)?._open(focusFirstItem: true);
        break;
      }
    }
    // Every other key stays in the bar, as in the platform's menu loop.
    return KeyEventResult.handled;
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

    if (!visible && !_menuController.isOpen) {
      // Settled back to hidden: the next setVisible(true) is a new showing, not a re-assertion.
      _shownOnce = false;
    }

    // A right-click already put this menu up through the control's own copy: one menu, one panel.
    if (visible && !_menuController.isOpen && !_shownOnce &&
        !HostedContextMenu.wraps(context) && _presentingPeer == null) {
      _shownOnce = true;
      // Ask Java to fill the menu before opening it. SWT.Show is what a MenuManager with
      // setRemoveAllWhenShown empties and refills the menu on, and only the client fires it --
      // so opening straight from the flag paints whatever the last fill left behind, or a fill
      // still in progress: too many separators, or nothing at all when the removeAll has landed
      // and the refill has not. Natively the platform fires Show as part of showing the menu
      // (win32 does it from WM_INITMENUPOPUP, inside TrackPopupMenu), which is the order this
      // restores. The right-click path already worked this way.
      // location is a SCREEN point; the anchor below is filled to this window, so what it wants is
      // a point inside it. Without the shift a popup opened over a window that is not at the screen
      // origin lands off by exactly where that window sits.
      final windowOrigin = WindowOriginScope.of(context);
      _pendingContextMenuPosition = location != null
          ? Offset(location.x.toDouble(), location.y.toDouble()) - windowOrigin
          : const Offset(100, 100);
      _pendingOpenFromVisibleFlag = true;
      _showSent = true;
      widget.sendMenuShow(state, null);
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
        side: getMenuPopupSide(widgetTheme),
        elevation: WidgetStateProperty.all(widgetTheme.popupElevation),
        padding: WidgetStateProperty.all(widgetTheme.popupPadding),
        shape: WidgetStateProperty.all(
          RoundedRectangleBorder(
            borderRadius: BorderRadius.circular(widgetTheme.borderRadius),
          ),
        ),
      ),
      alignmentOffset: Offset.zero,
      consumeOutsideTap: true,
      onOpen: () {
        OpenPopupMenuTracker._current = this;
        if (_showSent) return;
        _showSent = true;
        widget.sendMenuShow(state, null);
      },
      onClose: () {
        if (identical(OpenPopupMenuTracker._current, this)) {
          OpenPopupMenuTracker._current = null;
        }
        _openedFromVisibleFlag = false;
        _pendingContextMenuPosition = null;
        _pendingOpenFromVisibleFlag = false;
        _shownOnce = false;
        _showSent = false;
        if (state.visible ?? false) {
          // Sync the SERIALIZED state too: build() reads state.visible, and with it stuck true
          // the open guard re-opens the menu on every rebuild (Java can't help — its own field
          // already went false on the Hide event, so a later setVisible(false) is a no-op that
          // never serializes).
          visible = false;
          state.visible = false;
        }
        // Sent whichever path opened it. A right-click leaves state.visible false, so gating this
        // on it left Java believing the popup was still up — and the Display skips re-showing a
        // popup it still counts as shown, which killed every later opening of that menu.
        widget.sendMenuHide(state, null);
        // A chosen item is selected after its menus are hidden, as natively.
        _sendPendingChanges();
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
          child: MenuMnemonics(
            focusNode: _itemsFocusNode,
            close: _menuController.close,
            child: Column(
              mainAxisSize: MainAxisSize.min,
              children: menuItems
                  .map((item) => MenuItemSwt(key: ValueKey(item.id), value: item))
                  .toList(),
            ),
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
    final items = state.items ?? const <VMenuItem>[];
    // On macOS the application menu belongs to the system menu bar, which is out of reach in a
    // browser tab; show it here instead, in the position the OS would give it.
    if ((state.style & SWT.BAR) != 0) {
      return [...applicationMenuItems(), ...items];
    }
    return _withoutRedundantSeparators(items);
  }

  /// Drops separators that separate nothing: leading, trailing, and runs of them.
  ///
  /// A menu assembled from contributions routinely ends up with them -- each contributor adds its own
  /// trailing separator, and whichever groups turn out empty leave theirs behind. A platform menu does
  /// not draw those, so neither does this one: the same menu on native SWT shows four lines where the
  /// item list carries seven.
  static List<VMenuItem> _withoutRedundantSeparators(List<VMenuItem> items) {
    final out = <VMenuItem>[];
    VMenuItem? pending;
    for (final item in items) {
      if ((item.style & SWT.SEPARATOR) != 0) {
        // Held rather than dropped: a separator earns its place only once a real item follows it.
        // Keeping the first of a run keeps the ids the client renders stable.
        if (out.isNotEmpty) pending ??= item;
        continue;
      }
      if (pending != null) {
        out.add(pending);
        pending = null;
      }
      out.add(item);
    }
    return out;
  }
}

class _MenuBarItem extends StatefulWidget {
  final VMenuItem item;
  final MenuThemeExtension widgetTheme;
  final TextStyle textStyle;
  final Color textColor;
  final Color borderColor;

  /// The bar is in keyboard mode with this title current.
  final bool current;
  final VoidCallback onOpening;
  final VoidCallback onOpened;

  /// The drop-down closed; [byKeyboard] when Escape closed it.
  final void Function(bool byKeyboard) onClosed;

  /// Left (-1) or Right (1) in the open drop-down: the neighbouring title's drop-down takes its place.
  final void Function(int step) onMoveTitle;
  final void Function(void Function()) registerPendingChange;
  final MenuState menuState;

  const _MenuBarItem({
    super.key,
    required this.item,
    required this.widgetTheme,
    required this.textStyle,
    required this.textColor,
    required this.borderColor,
    required this.current,
    required this.onOpening,
    required this.onOpened,
    required this.onClosed,
    required this.onMoveTitle,
    required this.registerPendingChange,
    required this.menuState,
  });

  @override
  State<_MenuBarItem> createState() => _MenuBarItemState();
}

class _MenuBarItemState extends State<_MenuBarItem> {
  bool _isHovered = false;
  List<Widget> _menuChildren = const [SizedBox.shrink()];
  final MenuController _subMenuController = MenuController();
  MenuItemsWatch? _itemsWatch;
  final FocusNode _itemsFocusNode = FocusNode(debugLabel: 'MenuBar.items');
  // Opened from the keyboard: the keyboard moves onto the first item once the menu is up.
  bool _focusFirstItemWhenOpen = false;
  // The drop-down whose Show is on its way to Java; it opens on Java's answer.
  int? _showingMenuId;
  // Java was told the drop-down is shown, so closing it owes a Hide.
  bool _shown = false;
  bool _closingFromKeyboard = false;
  bool _closingForNextTitle = false;

  @override
  void initState() {
    super.initState();
    OpenMenus.add(_subMenuController);
    if (widget.item.menu != null) MenuBarOpeners.add(widget.item, _openFromMnemonic);
    _menuChildren = _childrenOf(widget.item.menu?.items);
    _watchItems();
  }

  void _watchItems() {
    final menu = widget.item.menu;
    if (menu != null && _itemsWatch?.watches(menu) == true) return;
    _itemsWatch?.dispose();
    _itemsWatch = menu == null ? null : MenuItemsWatch(menu, _itemsArrived);
  }

  static List<Widget> _childrenOf(List<VMenuItem>? items) => items == null || items.isEmpty
      ? const [SizedBox.shrink()]
      : items.map((item) => MenuItemSwt(key: ValueKey(item.id), value: item)).toList();

  @override
  void didUpdateWidget(_MenuBarItem oldWidget) {
    super.didUpdateWidget(oldWidget);
    _watchItems();
    // Java sends no answer for a drop-down disposed during its Show.
    if (_showingMenuId != null && widget.item.menu?.id != _showingMenuId) _stopShowing();
    if (oldWidget.item.id == widget.item.id && (oldWidget.item.menu == null) == (widget.item.menu == null)) {
      return;
    }
    if (oldWidget.item.menu != null) MenuBarOpeners.remove(oldWidget.item, _openFromMnemonic);
    if (widget.item.menu != null) MenuBarOpeners.add(widget.item, _openFromMnemonic);
  }

  @override
  void dispose() {
    OpenMenus.remove(_subMenuController);
    if (widget.item.menu != null) MenuBarOpeners.remove(widget.item, _openFromMnemonic);
    _stopShowing();
    _itemsFocusNode.dispose();
    _itemsWatch?.dispose();
    super.dispose();
  }

  void _itemsArrived(List<VMenuItem> arrived) {
    if (mounted) setState(() => _menuChildren = _childrenOf(arrived));
  }

  // Every opening asks Java first, as the platform sends SWT.Show before it shows a menu: an
  // application fills its menu on Show, and the items it sends back are what opens.
  void _open({required bool focusFirstItem}) {
    final menu = widget.item.menu;
    if (!mounted || menu == null || !(widget.item.enabled ?? false)) return;
    if (_subMenuController.isOpen || _showingMenuId != null) return;
    widget.onOpening();
    _focusFirstItemWhenOpen = focusFirstItem;
    final channel = 'Menu/${menu.id}';
    _showingMenuId = menu.id;
    EquoCommService.onRaw('$channel/shown', (_) => _onShown());
    MenuSwt<VMenu>(value: menu).sendMenuShow(menu, null);
  }

  void _stopShowing() {
    final id = _showingMenuId;
    if (id == null) return;
    _showingMenuId = null;
    EquoCommService.remove('Menu/$id/shown');
  }

  void _onShown() {
    if (_showingMenuId == null) return;
    _stopShowing();
    if (!mounted) return;
    _shown = true;
    WidgetsBinding.instance.addPostFrameCallback((_) {
      if (!mounted || _subMenuController.isOpen) return;
      _subMenuController.open();
      widget.onOpened();
      _focusFirstItemNextFrame();
    });
    // The ack arrives in a microtask, so nothing has scheduled the frame this callback needs.
    WidgetsBinding.instance.ensureVisualUpdate();
  }

  void _onClose() {
    _focusFirstItemWhenOpen = false;
    final byKeyboard = _closingFromKeyboard;
    final forNextTitle = _closingForNextTitle;
    _closingFromKeyboard = false;
    _closingForNextTitle = false;
    final menu = widget.item.menu;
    if (_shown && menu != null) MenuSwt<VMenu>(value: menu).sendMenuHide(menu, null);
    _shown = false;
    if (!forNextTitle) widget.onClosed(byKeyboard);
  }

  void _closeFromKeyboard() {
    _closingFromKeyboard = true;
    _subMenuController.close();
  }

  void _closeForNextTitle() {
    if (!_subMenuController.isOpen) return;
    _closingForNextTitle = true;
    _subMenuController.close();
  }

  // Keys in the drop-down itself, as the Windows menu loop answers them; a cascade's submenu
  // answers its own Escape before this sees it.
  KeyEventResult _onDropDownKey(FocusNode node, KeyEvent event) {
    if (event is! KeyDownEvent) return KeyEventResult.ignored;
    final key = event.logicalKey;
    if (key == LogicalKeyboardKey.escape) {
      _closeFromKeyboard();
      return KeyEventResult.handled;
    }
    if (key != LogicalKeyboardKey.arrowLeft && key != LogicalKeyboardKey.arrowRight) {
      return KeyEventResult.ignored;
    }
    // Left and Right on or inside a cascade open and close its submenu.
    final focused = FocusManager.instance.primaryFocus?.context;
    if (focused == null || focused.findAncestorWidgetOfExactType<SubmenuButton>() != null) {
      return KeyEventResult.ignored;
    }
    widget.onMoveTitle(key == LogicalKeyboardKey.arrowRight ? 1 : -1);
    return KeyEventResult.handled;
  }

  // Alt+letter on this title, resolved by Java: the menu opens with its first item current, as natively.
  void _openFromMnemonic() => _open(focusFirstItem: true);

  void _focusFirstItemNextFrame() {
    WidgetsBinding.instance.addPostFrameCallback((_) {
      if (!mounted || !_focusFirstItemWhenOpen || !_subMenuController.isOpen) return;
      _focusFirstItemWhenOpen = false;
      final items = _itemsFocusNode.traversalDescendants;
      if (items.isNotEmpty) items.first.requestFocus();
    });
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
    final highlighted = (_isHovered || widget.current) && enabled;

    if (widget.item.menu != null) {
      return MenuAnchor(
        controller: _subMenuController,
        style: MenuStyle(
          backgroundColor: WidgetStateProperty.all(
            widget.widgetTheme.popupBackgroundColor,
          ),
          side: getMenuPopupSide(widget.widgetTheme),
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
        onClose: _onClose,
        menuChildren: [
          pointerInterceptor(MenuChangeNotifier(
            registerPendingChange: widget.registerPendingChange,
            menuState: widget.menuState,
            closeMenu: _subMenuController.close,
            child: Focus(
              canRequestFocus: false,
              skipTraversal: true,
              onKeyEvent: _onDropDownKey,
              child: MenuMnemonics(
                focusNode: _itemsFocusNode,
                close: _closeFromKeyboard,
                child: Column(
                  mainAxisSize: MainAxisSize.min,
                  children: _menuChildren,
                ),
              ),
            ),
          )),
        ],
        builder: (context, controller, child) {
          return MouseRegion(
            onEnter: (_) => setState(() => _isHovered = true),
            onExit: (_) => setState(() => _isHovered = false),
            child: AnimatedContainer(
              duration: widget.widgetTheme.animationDuration,
              color: highlighted
                  ? widget.widgetTheme.hoverBackgroundColor
                  : Colors.transparent,
              child: Listener(
                onPointerUp: (e) {
                  if (enabled && e.buttons == 0) {
                    if (controller.isOpen) {
                      controller.close();
                    } else {
                      _open(focusFirstItem: false);
                    }
                  }
                },
                child: GestureDetector(
                  child: Padding(
                    padding: widget.widgetTheme.menuBarItemPadding,
                    child: MnemonicText(
                      widget.item.text,
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
        color: highlighted
            ? widget.widgetTheme.hoverBackgroundColor
            : Colors.transparent,
        child: Listener(
          onPointerUp: (e) {},
          child: GestureDetector(
            child: Padding(
              padding: widget.widgetTheme.menuBarItemPadding,
              child: MnemonicText(
                widget.item.text,
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
