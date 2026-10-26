import 'package:flutter/foundation.dart';
import 'package:flutter/services.dart';
import 'package:flutter/widgets.dart';

import '../../comm/comm.dart';
import '../../gen/menuitem.dart';
import 'text_utils.dart';

/// Letters typed in one open menu, answered the way the platform's menu loop answers them.
///
/// Win32 and GTK: an item's mnemonic letter activates it (a cascade opens and takes the keyboard),
/// and a letter several items share only moves the current item between them. macOS: Cocoa strips
/// mnemonics, and the menu selects the first item whose title starts with what was typed.
///
/// Wraps the column holding one menu's items, and each item row inside it registers through
/// [MenuMnemonicItem]. The nearest scope wins, so a cascade's submenu answers for its own items and
/// keeps every letter from reaching the menu that opened it.
///
/// It also answers the keyboard for the open menu that holds no focus; see [_onKeyOutsideMenus].
class MenuMnemonics extends StatefulWidget {
  const MenuMnemonics({
    super.key,
    required this.focusNode,
    required this.close,
    this.items,
    this.onFocusChange,
    required this.child,
  });

  final FocusNode focusNode;

  /// Closes this menu, and only this one: Escape takes down the innermost open menu.
  final VoidCallback close;

  /// Where the item rows register, when the menu's owner needs to reach them; else a registry of its own.
  final MenuMnemonicItems? items;
  final ValueChanged<bool>? onFocusChange;
  final Widget child;

  /// The items of the menu [context] is in, or null outside any menu.
  static MenuMnemonicItems? of(BuildContext context) =>
      context.getInheritedWidgetOfExactType<_MenuMnemonicsScope>()?.items;

  /// Closes the menu [context] is in, and only that one, as Escape there does.
  static void closeMenuOf(BuildContext context) =>
      context.findAncestorStateOfType<_MenuMnemonicsState>()?.widget.close();

  @override
  State<MenuMnemonics> createState() => _MenuMnemonicsState();
}

/// The item rows of one menu, by the focus node each row is built with.
class MenuMnemonicItems {
  final Map<FocusNode, ({String label, VoidCallback? activate})> _items = {};

  /// The items are the titles of a menu bar that has the keyboard: Down, Up, Enter or Space opens
  /// the current one, as on the strip.
  bool activeBar = false;

  /// Makes [node] reachable by letter. [label] is the item's SWT text, `&` marker included;
  /// [activate] is null while the item is disabled.
  void register(FocusNode node, String label, VoidCallback? activate) =>
      _items[node] = (label: label, activate: activate);

  void unregister(FocusNode node) => _items.remove(node);

  /// Activates the enabled item whose text is [label], as its mnemonic would. Returns whether one was.
  bool activate(String label) {
    for (final entry in _items.entries) {
      if (entry.value.label == label && entry.value.activate != null) {
        _activate(entry.key);
        return true;
      }
    }
    return false;
  }

  void _activate(FocusNode node) {
    node.requestFocus();
    _items[node]!.activate!();
  }
}

/// Opens a menu bar's menu when Java resolves Alt+letter to its title (`MnemonicHelper`), wherever the
/// bar is drawn: as a strip, or collapsed into the menu button. The latest opener of an item wins.
class MenuBarOpeners {
  static void add(VMenuItem item, VoidCallback open) => _latest.add(_channel(item), open);

  static void remove(VMenuItem item, VoidCallback open) => _latest.remove(_channel(item), open);

  static String _channel(VMenuItem item) => '${item.swt}/${item.id}/openMenu';
}

/// Makes a menu bar active when Java resolves F10 or a lone Alt to it, wherever the bar is drawn.
/// The latest activator of a bar wins.
class MenuBarActivators {
  static void add(int barId, VoidCallback activate) => _latest.add(_channel(barId), activate);

  static void remove(int barId, VoidCallback activate) => _latest.remove(_channel(barId), activate);

  static String _channel(int barId) => 'Menu/$barId/activate';
}

final _latest = _LatestHandlers();

/// One raw channel per key, answered by the handler added last.
class _LatestHandlers {
  final Map<String, List<VoidCallback>> _handlers = {};

  void add(String channel, VoidCallback handler) {
    final handlers = _handlers.putIfAbsent(channel, () => []);
    if (handlers.isEmpty) {
      EquoCommService.onRaw(channel, (_) => _handlers[channel]?.lastOrNull?.call());
    }
    handlers.add(handler);
  }

  void remove(String channel, VoidCallback handler) {
    final handlers = _handlers[channel];
    if (handlers == null || !handlers.remove(handler) || handlers.isNotEmpty) return;
    _handlers.remove(channel);
    EquoCommService.remove(channel);
  }
}

class _MenuMnemonicsState extends State<MenuMnemonics> {
  late final MenuMnemonicItems _registry = widget.items ?? MenuMnemonicItems();
  Map<FocusNode, ({String label, VoidCallback? activate})> get _items => _registry._items;
  String _typed = '';
  Duration? _lastTyped;

  // NSMenu starts a new search once the user pauses.
  static const _typeSelectPause = Duration(seconds: 1);

  // Every open menu, innermost last.
  static final List<_MenuMnemonicsState> _open = [];

  /// Keys while no menu holds the focus (opened with the mouse, or the web engine parked it at the
  /// root): the innermost menu takes them, Escape closing it, Up/Down entering it, letters picking.
  static bool _onKeyOutsideMenus(KeyEvent event) {
    if (_open.isEmpty || _open.any((menu) => menu.widget.focusNode.hasFocus)) return false;
    final menu = _open.last;
    if (event is KeyDownEvent) {
      final key = event.logicalKey;
      if (key == LogicalKeyboardKey.escape) {
        menu.widget.close();
        return true;
      }
      if (key == LogicalKeyboardKey.arrowDown || key == LogicalKeyboardKey.arrowUp) {
        final items = menu.widget.focusNode.traversalDescendants.toList();
        if (items.isEmpty) return false;
        (key == LogicalKeyboardKey.arrowDown ? items.first : items.last).requestFocus();
        return true;
      }
    }
    return menu._onKey(menu.widget.focusNode, event) == KeyEventResult.handled;
  }

  @override
  void initState() {
    super.initState();
    if (_open.isEmpty) HardwareKeyboard.instance.addHandler(_onKeyOutsideMenus);
    _open.add(this);
  }

  @override
  void dispose() {
    _open.remove(this);
    if (_open.isEmpty) HardwareKeyboard.instance.removeHandler(_onKeyOutsideMenus);
    super.dispose();
  }

  KeyEventResult _onKey(FocusNode node, KeyEvent event) {
    if (event is KeyUpEvent) return KeyEventResult.ignored;
    if (event is KeyDownEvent && event.logicalKey == LogicalKeyboardKey.escape) {
      widget.close();
      return KeyEventResult.handled;
    }
    final character = event.character;
    if (character == null ||
        character.trim().isEmpty ||
        character.codeUnitAt(0) < 0x20 ||
        character.codeUnitAt(0) == 0x7f) {
      return KeyEventResult.ignored;
    }
    final keyboard = HardwareKeyboard.instance;
    if (keyboard.isControlPressed || keyboard.isMetaPressed) return KeyEventResult.ignored;

    final items = [
      for (final item in widget.focusNode.traversalDescendants)
        if (_items[item]?.activate != null) item,
    ];
    if (defaultTargetPlatform == TargetPlatform.macOS ||
        defaultTargetPlatform == TargetPlatform.iOS) {
      _typeSelect(character, event.timeStamp, items);
    } else {
      _mnemonic(character, items);
    }
    // Unmatched letters stay in the menu, as natively: none reaches a parent menu or the control.
    return KeyEventResult.handled;
  }

  void _mnemonic(String character, List<FocusNode> items) {
    final letter = character.toLowerCase();
    final matches = [
      for (final item in items)
        if (_mnemonicOf(_items[item]!.label) == letter) item,
    ];
    if (matches.isEmpty) return;
    if (matches.length == 1) {
      _registry._activate(matches.single);
      return;
    }
    final focused = FocusManager.instance.primaryFocus;
    final current = focused == null ? -1 : items.indexOf(focused);
    matches
        .firstWhere((item) => items.indexOf(item) > current, orElse: () => matches.first)
        .requestFocus();
  }

  void _typeSelect(String character, Duration timeStamp, List<FocusNode> items) {
    final last = _lastTyped;
    if (last == null || timeStamp - last > _typeSelectPause) _typed = '';
    _lastTyped = timeStamp;
    _typed += character.toLowerCase();
    for (final item in items) {
      if (parseMnemonic(_items[item]!.label).text.toLowerCase().startsWith(_typed)) {
        item.requestFocus();
        return;
      }
    }
  }

  static String? _mnemonicOf(String label) {
    final mnemonic = parseMnemonic(label);
    if (mnemonic.index < 0) return null;
    return String.fromCharCode(mnemonic.text.substring(mnemonic.index).runes.first).toLowerCase();
  }

  @override
  Widget build(BuildContext context) => _MenuMnemonicsScope(
    items: _registry,
    child: Focus(
      focusNode: widget.focusNode,
      canRequestFocus: false,
      onFocusChange: widget.onFocusChange,
      onKeyEvent: _onKey,
      child: widget.child,
    ),
  );
}

class _MenuMnemonicsScope extends InheritedWidget {
  const _MenuMnemonicsScope({required this.items, required super.child});

  final MenuMnemonicItems items;

  @override
  bool updateShouldNotify(_MenuMnemonicsScope oldWidget) => false;
}

/// Keeps an item row registered in the menu it is built in, under the focus node it is built with.
mixin MenuMnemonicItem<T extends StatefulWidget> on State<T> {
  MenuMnemonicItems? _menu;
  FocusNode? _node;

  /// Call from build(): the row's node, label and activation can all change between builds.
  void registerMnemonic(FocusNode node, String label, VoidCallback? activate) {
    final menu = MenuMnemonics.of(context);
    if (!identical(menu, _menu) || !identical(node, _node)) {
      _menu?.unregister(_node!);
      _menu = menu;
      _node = node;
    }
    menu?.register(node, label, activate);
  }

  @override
  void dispose() {
    _menu?.unregister(_node!);
    super.dispose();
  }
}
