import 'package:flutter/material.dart';
import '../gen/decorations.dart';
import '../gen/menu.dart';
import '../gen/menuitem.dart';
import '../gen/widget.dart';
import '../impl/canvas_evolve.dart';
import '../impl/menu_evolve.dart';
import '../impl/widget_config.dart';
import '../theme/theme_extensions/menu_theme_extension.dart';

class DecorationsMenuData extends InheritedWidget {
  final VMenu? menuBar;
  final bool isAtStart;

  const DecorationsMenuData({
    super.key,
    required this.menuBar,
    required this.isAtStart,
    required super.child,
  });

  static DecorationsMenuData? of(BuildContext context) =>
      context.dependOnInheritedWidgetOfExactType<DecorationsMenuData>();

  @override
  bool updateShouldNotify(DecorationsMenuData old) =>
      menuBar != old.menuBar || isAtStart != old.isAtStart;
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

  void _flushChanges() {
    for (final cb in _pendingChanges) {
      cb();
    }
    _pendingChanges.clear();
  }

  @override
  Widget build(BuildContext context) {
    final data = DecorationsMenuData.of(context);
    final menuBar = data?.menuBar;
    if (menuBar == null || data!.isAtStart != widget.atStart) {
      return const SizedBox.shrink();
    }

    final menuTheme = Theme.of(context).extension<MenuThemeExtension>()!;
    return MenuAnchor(
      controller: _controller,
      style: MenuStyle(
        backgroundColor: WidgetStateProperty.all(menuTheme.popupBackgroundColor),
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
      onClose: _flushChanges,
      menuChildren: [
        MenuChangeNotifier(
          registerPendingChange: (cb) => _pendingChanges.add(cb),
          menuState: _menuState,
          closeMenu: _controller.close,
          child: Column(
            mainAxisSize: MainAxisSize.min,
            children: (menuBar.items ?? [])
                .map((item) => MenuItemSwt(value: item))
                .toList(),
          ),
        ),
      ],
      builder: (context, controller, child) {
        return IconButton(
          tooltip: 'Menu',
          icon: const Icon(Icons.menu),
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
  Widget build(BuildContext context) {
    final menuMode = (getConfigFlags().decorations_align ?? "hleft").toLowerCase();
    final isVertical = menuMode == "vleft" || menuMode == "vright";
    final isAtStart = menuMode == "vleft";

    final Widget content;
    if (!isVertical && state.menuBar != null) {
      content = Column(
        children: [
          MenuSwt(value: state.menuBar!),
          Expanded(child: super.build(context)),
        ],
      );
    } else {
      content = super.build(context);
    }

    return DecorationsMenuData(
      menuBar: isVertical ? state.menuBar : null,
      isAtStart: isAtStart,
      child: content,
    );
  }
}
