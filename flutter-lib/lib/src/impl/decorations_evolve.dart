import 'package:flutter/material.dart';
import '../theme/theme_settings/menu_theme_settings.dart';
import '../gen/decorations.dart';
import '../gen/menu.dart';
import '../gen/menuitem.dart';
import '../gen/widget.dart';
import '../gen/widgets.dart';
import '../impl/canvas_evolve.dart';
import '../impl/menu_evolve.dart';
import '../impl/decorations_align.dart';
import '../impl/utils/menu_mnemonics.dart';
import '../impl/utils/pointer.dart';
import '../impl/widget_config.dart';
import '../theme/theme_extensions/composite_theme_extension.dart';
import '../theme/theme_extensions/menu_theme_extension.dart';

class DecorationsMenuData extends InheritedWidget {
  final VMenu? menuBar;
  final bool isAtStart;
  final bool isHorizontal;

  /// The menu strip's height wherever it is drawn, as Java lays it out (`Sizes.menuStripHeight`).
  final int? stripHeight;

  const DecorationsMenuData({
    super.key,
    required this.menuBar,
    required this.isAtStart,
    required this.isHorizontal,
    this.stripHeight,
    required super.child,
  });

  /// The Shell's own menus plus, ahead of them, the application menu -- on macOS that one lives in
  /// the system menu bar, which a browser tab cannot reach. Empty when the OS owns the menu bar:
  /// every menu is in the system bar there, so drawing any of them here would put the same menu in
  /// two places. `DartMainToolbar` mirrors this on the Java side.
  List<VMenuItem> get items => getConfigFlags().system_menu_bar == true
      ? const []
      : [...applicationMenuItems(), ...?menuBar?.items];

  /// A menu bar with nothing in it must not be shown at all -- neither as the horizontal strip
  /// above the toolbar nor as the vertical hamburger button inside it. `DartMainToolbar` mirrors
  /// this on the Java side so the hidden menu does not reserve layout space either.
  bool get hasItems => items.isNotEmpty;

  static DecorationsMenuData? of(BuildContext context) =>
      context.dependOnInheritedWidgetOfExactType<DecorationsMenuData>();

  @override
  bool updateShouldNotify(DecorationsMenuData old) =>
      menuBar != old.menuBar ||
      isAtStart != old.isAtStart ||
      isHorizontal != old.isHorizontal ||
      stripHeight != old.stripHeight;
}

class VerticalMenuIcon extends StatelessWidget {
  const VerticalMenuIcon({super.key, this.onPressed});

  final VoidCallback? onPressed;

  @override
  Widget build(BuildContext context) {
    return IconButton(
      tooltip: 'Menu',
      icon: const Icon(Icons.menu),
      onPressed: onPressed,
    );
  }
}

class VerticalMenuButton extends StatefulWidget {
  final bool atStart;

  const VerticalMenuButton({super.key, required this.atStart});

  @override
  State<VerticalMenuButton> createState() => _VerticalMenuButtonState();
}

class _VerticalMenuButtonState extends State<VerticalMenuButton> {
  final MenuController _controller = MenuController();
  final MenuState _menuState = MenuState();
  final List<void Function()> _pendingChanges = [];
  final FocusNode _itemsFocusNode = FocusNode(debugLabel: 'VerticalMenu.items');
  final MenuMnemonicItems _items = MenuMnemonicItems();
  // The menu bar titles this button opens on Alt+letter, by channel, while it is the one drawing the bar.
  final Map<String, ({VMenuItem item, VoidCallback open})> _openers = {};
  // The menu bar this button answers F10 and a lone Alt for, while it is the one drawing the bar.
  int? _activatedBarId;

  @override
  void initState() {
    super.initState();
    OpenMenus.add(_controller);
  }

  @override
  void dispose() {
    OpenMenus.remove(_controller);
    _syncOpeners(const []);
    _syncActivator(null);
    _itemsFocusNode.dispose();
    super.dispose();
  }

  void _syncOpeners(List<VMenuItem> items) {
    final wanted = {
      for (final item in items)
        if (item.menu != null) '${item.swt}/${item.id}': item,
    };
    for (final key in _openers.keys.toList()) {
      if (wanted.containsKey(key)) continue;
      final opener = _openers.remove(key)!;
      MenuBarOpeners.remove(opener.item, opener.open);
    }
    for (final MapEntry(:key, value: item) in wanted.entries) {
      if (_openers.containsKey(key)) continue;
      void open() => _openFromMnemonic(item.id);
      _openers[key] = (item: item, open: open);
      MenuBarOpeners.add(item, open);
    }
  }

  void _syncActivator(int? barId) {
    if (barId == _activatedBarId) return;
    final previous = _activatedBarId;
    if (previous != null) MenuBarActivators.remove(previous, _activate);
    _activatedBarId = barId;
    if (barId != null) MenuBarActivators.add(barId, _activate);
  }

  // F10 or a lone Alt, resolved by Java: the collapsed bar opens with its first title current.
  void _activate() {
    if (!mounted || _controller.isOpen) return;
    _items.activeBar = true;
    _controller.open();
    WidgetsBinding.instance.addPostFrameCallback((_) {
      if (!mounted || !_controller.isOpen) return;
      final titles = _itemsFocusNode.traversalDescendants;
      if (titles.isNotEmpty) titles.first.requestFocus();
    });
    WidgetsBinding.instance.ensureVisualUpdate();
  }

  // Alt+letter on a menu bar title, resolved by Java: the collapsed bar opens, and that title's menu in it.
  void _openFromMnemonic(int id) {
    if (!mounted) return;
    _items.activeBar = true;
    if (!_controller.isOpen) _controller.open();
    WidgetsBinding.instance.addPostFrameCallback((_) {
      if (!mounted || !_controller.isOpen) return;
      final items = DecorationsMenuData.of(context)?.items ?? const <VMenuItem>[];
      final item = items.where((item) => item.id == id).firstOrNull;
      if (item != null) _items.activate(item.text ?? '');
    });
    WidgetsBinding.instance.ensureVisualUpdate();
  }

  void _onClose() {
    _items.activeBar = false;
    _flushChanges();
  }

  void _flushChanges() {
    for (final cb in _pendingChanges) {
      cb();
    }
    _pendingChanges.clear();
  }

  @override
  Widget build(BuildContext context) {
    final data = DecorationsMenuData.of(context);
    if (data == null ||
        data.isHorizontal ||
        data.isAtStart != widget.atStart ||
        !data.hasItems) {
      _syncOpeners(const []);
      _syncActivator(null);
      return const SizedBox.shrink();
    }
    _syncOpeners(data.items);
    _syncActivator(data.menuBar?.id);
    final menuTheme = Theme.of(context).extension<MenuThemeExtension>()!;
    return MenuAnchor(
      controller: _controller,
      style: MenuStyle(
        backgroundColor: WidgetStateProperty.all(menuTheme.popupBackgroundColor),
        side: getMenuPopupSide(menuTheme),
        elevation: WidgetStateProperty.all(menuTheme.popupElevation),
        padding: WidgetStateProperty.all(menuTheme.popupPadding),
        shape: WidgetStateProperty.all(
          RoundedRectangleBorder(
            borderRadius: BorderRadius.circular(menuTheme.borderRadius),
          ),
        ),
      ),
      alignmentOffset: Offset.zero,
      anchorTapClosesMenu: false,
      onClose: _onClose,
      menuChildren: [
        pointerInterceptor(MenuChangeNotifier(
          registerPendingChange: (cb) => _pendingChanges.add(cb),
          menuState: _menuState,
          closeMenu: _controller.close,
          child: MenuMnemonics(
            focusNode: _itemsFocusNode,
            close: _controller.close,
            items: _items,
            child: Column(
              mainAxisSize: MainAxisSize.min,
              children: data.items
                  .map((item) => MenuItemSwt(key: ValueKey(item.id), value: item))
                  .toList(),
            ),
          ),
        )),
      ],
      builder: (context, controller, child) {
        return VerticalMenuIcon(
          onPressed: () {
            if (controller.isOpen) {
              controller.close();
            } else {
              controller.open();
            }
          },
        );
      },
    );
  }
}

class DecorationsImpl<T extends DecorationsSwt, V extends VDecorations>
    extends CanvasImpl<T, V> {
  @override
  bool get hostsAppColoredContent => false;

  /// A Decorations is a Canvas only by SWT's class hierarchy: it hosts controls, it is not a
  /// drawing surface. Falling back to the Canvas drawing surface would paint a colour no Composite
  /// inside ever falls back to, so every one of them would read as an opaque block on it.
  @override
  Color get themeSurfaceColor =>
      Theme.of(context).extension<CompositeThemeExtension>()!.backgroundColor;

  /// Keeps the content's state when the menu strip comes or goes above it.
  final GlobalKey _contentKey = GlobalKey();

  @override
  Widget build(BuildContext context) {
    final align = getConfigFlags().decorations_align ?? DecorationsAlign.hleft;
    final content = KeyedSubtree(key: _contentKey, child: super.build(context));

    return DecorationsMenuData(
      menuBar: state.menuBar,
      isAtStart: align.isAtStart,
      isHorizontal: !align.isVertical,
      stripHeight: state.menuStripHeight,
      // Java decides when the Shell draws the strip itself (`Sizes.ownMenuStripHeight`): no visible
      // main toolbar hosts it, so it goes above the client area.
      child: state.ownMenuStrip == true && state.menuBar != null
          ? Column(
              crossAxisAlignment: CrossAxisAlignment.stretch,
              children: [
                mapWidgetFromValue(state.menuBar!),
                Expanded(child: content),
              ],
            )
          : content,
    );
  }
}
