import 'package:flutter/material.dart';
import '../comm/comm.dart';
import '../gen/menu.dart';
import '../gen/menuitem.dart';
import '../gen/swt.dart';
import '../gen/widget.dart';
import '../impl/item_evolve.dart';
import '../styles.dart';
import '../theme/theme_extensions/menu_theme_extension.dart';
import '../theme/theme_extensions/menuitem_theme_extension.dart';
import '../theme/theme_settings/menuitem_theme_settings.dart';
import 'menu_evolve.dart';
import 'utils/text_utils.dart';

class MenuItemImpl<T extends MenuItemSwt, V extends VMenuItem>
    extends ItemImpl<T, V> {
  bool? _localSelection;
  bool _isRadio = false;

  @override
  void initState() {
    super.initState();
    _localSelection = state.selection;
    _isRadio = StyleBits(state.style).has(SWT.RADIO);
  }

  @override
  void didUpdateWidget(T oldWidget) {
    super.didUpdateWidget(oldWidget);
    _localSelection = state.selection;
  }

  void _setLocalSelection(bool selected) {
    if (mounted) {
      setState(() {
        _localSelection = selected;
      });
    }
  }

  @override
  Widget build(BuildContext context) {
    final widgetTheme = Theme.of(context).extension<MenuItemThemeExtension>()!;
    final menuTheme = Theme.of(context).extension<MenuThemeExtension>()!;
    final style = StyleBits(state.style);
    final isEnabled = state.enabled ?? false;

    if (_isRadio) {
      _registerRadioCallback(context);
    }

    if (style.has(SWT.SEPARATOR)) {
      return _buildSeparator(widgetTheme);
    }

    if (style.has(SWT.CASCADE) && state.menu != null) {
      return _CascadeMenuItemRow(
        widgetTheme: widgetTheme,
        menuTheme: menuTheme,
        isEnabled: isEnabled,
        text: state.text,
        subMenu: state.menu!,
      );
    }

    if (style.has(SWT.CHECK)) {
      return _buildCheckMenuItem(context, widgetTheme, isEnabled);
    }

    if (style.has(SWT.RADIO)) {
      return _buildRadioMenuItem(context, widgetTheme, isEnabled);
    }

    return _buildPushMenuItem(context, widgetTheme, isEnabled);
  }

  Widget _buildSeparator(MenuItemThemeExtension widgetTheme) {
    return Container(
      height: widgetTheme.separatorHeight,
      margin: EdgeInsets.symmetric(
        vertical: widgetTheme.separatorMargin,
        horizontal: widgetTheme.itemPadding.horizontal / 2,
      ),
      color: widgetTheme.separatorColor,
    );
  }

  Widget _buildCheckMenuItem(
    BuildContext context,
    MenuItemThemeExtension widgetTheme,
    bool isEnabled,
  ) {
    final textStyle = getMenuItemTextStyle(widgetTheme, isEnabled: isEnabled);
    final isChecked = _localSelection ?? false;

    return _MenuItemRow(
      widgetTheme: widgetTheme,
      isEnabled: isEnabled,
      onTap: isEnabled ? _onCheckPressed : null,
      leading: _MenuCheckbox(
        widgetTheme: widgetTheme,
        isEnabled: isEnabled,
        isSelected: isChecked,
      ),
      child: Text(stripAccelerators(state.text), style: textStyle),
    );
  }

  Widget _buildRadioMenuItem(
    BuildContext context,
    MenuItemThemeExtension widgetTheme,
    bool isEnabled,
  ) {
    final textStyle = getMenuItemTextStyle(widgetTheme, isEnabled: isEnabled);
    final isSelected = _localSelection ?? false;

    return _MenuItemRow(
      widgetTheme: widgetTheme,
      isEnabled: isEnabled,
      onTap: isEnabled ? _onRadioPressed : null,
      leading: _MenuRadioButton(
        widgetTheme: widgetTheme,
        isEnabled: isEnabled,
        isSelected: isSelected,
      ),
      child: Text(stripAccelerators(state.text), style: textStyle),
    );
  }

  Widget _buildPushMenuItem(
    BuildContext context,
    MenuItemThemeExtension widgetTheme,
    bool isEnabled,
  ) {
    final textStyle = getMenuItemTextStyle(widgetTheme, isEnabled: isEnabled);
    final acceleratorText = state.accelerator != null
        ? formatAccelerator(state.accelerator!)
        : '';
    final acceleratorStyle = getMenuItemAcceleratorTextStyle(
      widgetTheme,
      isEnabled: isEnabled,
    );

    final notifier = MenuChangeNotifier.of(context);
    final capturedState = state;
    final capturedWidget = widget;

    return _MenuItemRow(
      widgetTheme: widgetTheme,
      isEnabled: isEnabled,
      onTap: isEnabled ? () {
        capturedWidget.sendSelectionSelection(capturedState, null);
        if (notifier != null) {
          notifier.closeMenu();
        }
      } : null,
      trailing: acceleratorText.isNotEmpty
          ? Text(acceleratorText, style: acceleratorStyle)
          : null,
      child: Text(stripAccelerators(capturedState.text), style: textStyle),
    );
  }

  void _registerRadioCallback(BuildContext context) {
    final notifier = MenuChangeNotifier.of(context);
    if (notifier != null) {
      WidgetsBinding.instance.addPostFrameCallback((_) {
        notifier.menuState.registerRadioCallback(state, _setLocalSelection);
      });
    }
  }

  void _onCheckPressed() {
    setState(() {
      _localSelection = !(_localSelection ?? false);
    });
    _sendSelectionEvent();
  }

  void _onRadioPressed() {
    final notifier = MenuChangeNotifier.of(context);
    if (notifier != null) {
      notifier.menuState.notifyRadioSelected(state);
    }
    setState(() {
      _localSelection = true;
    });
    _sendSelectionEvent();
  }

  void _sendSelectionEvent() {
    final notifier = MenuChangeNotifier.of(context);
    if (notifier != null) {
      notifier.registerPendingChange(() {
        widget.sendSelectionSelection(state, null);
      });
    } else {
      widget.sendSelectionSelection(state, null);
    }
  }
}

class _MenuItemRow extends StatefulWidget {
  final MenuItemThemeExtension widgetTheme;
  final bool isEnabled;
  final VoidCallback? onTap;
  final Widget? leading;
  final Widget child;
  final Widget? trailing;

  const _MenuItemRow({
    required this.widgetTheme,
    required this.isEnabled,
    this.onTap,
    this.leading,
    required this.child,
    this.trailing,
  });

  @override
  State<_MenuItemRow> createState() => _MenuItemRowState();
}

class _MenuItemRowState extends State<_MenuItemRow> {
  bool _isHovered = false;

  @override
  Widget build(BuildContext context) {
    return Opacity(
      opacity: widget.isEnabled ? 1.0 : widget.widgetTheme.disabledOpacity,
      child: MouseRegion(
        onEnter: (_) => setState(() => _isHovered = true),
        onExit: (_) => setState(() => _isHovered = false),
        child: Listener(
          onPointerUp: (e) {
            if (e.buttons == 0 && widget.onTap != null) {
              widget.onTap!();
            }
          },
          child: GestureDetector(
          child: AnimatedContainer(
            duration: widget.widgetTheme.animationDuration,
            constraints: BoxConstraints(
              minHeight: widget.widgetTheme.itemHeight,
            ),
            padding: widget.widgetTheme.itemPadding,
            decoration: BoxDecoration(
              color: getMenuItemRowBackgroundColor(widget.widgetTheme, widget.isEnabled, _isHovered),
              borderRadius: BorderRadius.circular(
                widget.widgetTheme.borderRadius,
              ),
            ),
            child: Row(
              children: [
                if (widget.leading != null) ...[
                  widget.leading!,
                  SizedBox(width: widget.widgetTheme.iconTextSpacing),
                ],
                Expanded(child: widget.child),
                if (widget.trailing != null) ...[
                  SizedBox(width: widget.widgetTheme.textAcceleratorSpacing),
                  widget.trailing!,
                ],
              ],
            ),
          ),
        ),
        ),
      ),
    );
  }
}

class _CascadeMenuItemRow extends StatefulWidget {
  final MenuItemThemeExtension widgetTheme;
  final MenuThemeExtension menuTheme;
  final bool isEnabled;
  final String? text;
  final VMenu subMenu;

  const _CascadeMenuItemRow({
    required this.widgetTheme,
    required this.menuTheme,
    required this.isEnabled,
    required this.text,
    required this.subMenu,
  });

  @override
  State<_CascadeMenuItemRow> createState() => _CascadeMenuItemRowState();
}

class _CascadeMenuItemRowState extends State<_CascadeMenuItemRow> {
  final MenuController _menuController = MenuController();
  // SizedBox.shrink() placeholder ensures SubmenuButton is always interactive
  List<Widget> _menuChildren = const [SizedBox.shrink()];
  bool _itemsLoaded = false;
  bool _isHovered = false;

  @override
  void initState() {
    super.initState();
    // If items are already provided (non-lazy case), use them immediately
    final existingItems = widget.subMenu.items;
    if (existingItems != null && existingItems.isNotEmpty) {
      _itemsLoaded = true;
      _menuChildren = existingItems
          .map((item) => MenuItemSwt(value: item))
          .toList();
    }
  }

  @override
  void dispose() {
    EquoCommService.remove("Menu/${widget.subMenu.id}");
    super.dispose();
  }

  void _onHover(bool hovering) {
    _isHovered = hovering;
    if (hovering && widget.isEnabled && !_itemsLoaded) {
      _requestItems();
    }
  }

  void _requestItems() {
    final channelName = "Menu/${widget.subMenu.id}";
    EquoCommService.on<VMenu>(channelName, (VMenu updatedMenu) {
      EquoCommService.remove(channelName);
      if (!mounted) return;
      final items = (updatedMenu.items ?? [])
          .map((item) => MenuItemSwt(value: item))
          .toList();
      setState(() {
        _itemsLoaded = true;
        _menuChildren = items.isEmpty ? const [SizedBox.shrink()] : items;
      });
      // Open the submenu with the now-populated items if still hovered
      if (_isHovered) {
        WidgetsBinding.instance.addPostFrameCallback((_) {
          if (mounted && _isHovered && !_menuController.isOpen) {
            _menuController.open();
          }
        });
      }
    });
    MenuSwt<VMenu>(value: widget.subMenu).sendMenuShow(widget.subMenu, null);
  }

  @override
  Widget build(BuildContext context) {
    final textStyle = getMenuItemTextStyle(widget.widgetTheme, isEnabled: widget.isEnabled);
    return Opacity(
      opacity: widget.isEnabled ? 1.0 : widget.widgetTheme.disabledOpacity,
      child: ConstrainedBox(
        constraints: BoxConstraints(
          minWidth: double.infinity,
          minHeight: widget.widgetTheme.itemHeight,
        ),
        child: SubmenuButton(
        controller: _menuController,
        onHover: _onHover,
        style: ButtonStyle(
          backgroundColor: WidgetStateProperty.resolveWith((states) {
            if (!widget.isEnabled) return widget.widgetTheme.backgroundColor;
            if (states.contains(WidgetState.hovered)) {
              return widget.widgetTheme.hoverBackgroundColor;
            }
            return widget.widgetTheme.backgroundColor;
          }),
          overlayColor: WidgetStateProperty.all(Colors.transparent),
          padding: WidgetStateProperty.all(widget.widgetTheme.itemPadding),
          minimumSize: WidgetStateProperty.all(
            Size(widget.widgetTheme.minItemWidth, widget.widgetTheme.itemHeight),
          ),
          tapTargetSize: MaterialTapTargetSize.shrinkWrap,
          shape: WidgetStateProperty.all(
            RoundedRectangleBorder(
              borderRadius: BorderRadius.circular(widget.widgetTheme.borderRadius),
            ),
          ),
          animationDuration: widget.widgetTheme.animationDuration,
        ),
        menuStyle: MenuStyle(
          backgroundColor: WidgetStateProperty.all(widget.menuTheme.popupBackgroundColor),
          elevation: WidgetStateProperty.all(widget.menuTheme.popupElevation),
          padding: WidgetStateProperty.all(widget.menuTheme.popupPadding),
          shape: WidgetStateProperty.all(
            RoundedRectangleBorder(
              borderRadius: BorderRadius.circular(widget.menuTheme.borderRadius),
            ),
          ),
        ),
        menuChildren: _menuChildren,
        child: Row(
          children: [
            Expanded(
              child: Text(stripAccelerators(widget.text), style: textStyle),
            ),
          ],
        ),
      ),
      ),
    );
  }
}

class _MenuCheckbox extends StatefulWidget {
  final MenuItemThemeExtension widgetTheme;
  final bool isEnabled;
  final bool isSelected;

  const _MenuCheckbox({
    required this.widgetTheme,
    required this.isEnabled,
    required this.isSelected,
  });

  @override
  State<_MenuCheckbox> createState() => _MenuCheckboxState();
}

class _MenuCheckboxState extends State<_MenuCheckbox> {
  bool _isHovered = false;

  @override
  Widget build(BuildContext context) {
    final size = widget.widgetTheme.checkboxSize;

    Color backgroundColor;
    Color borderColor;

    backgroundColor = getMenuCheckboxBackgroundColor(widget.widgetTheme, widget.isSelected, _isHovered, widget.isEnabled);
    borderColor = getMenuCheckboxBorderColor(widget.widgetTheme, widget.isSelected, _isHovered, widget.isEnabled);

    return MouseRegion(
      onEnter: (_) => setState(() => _isHovered = true),
      onExit: (_) => setState(() => _isHovered = false),
      child: AnimatedContainer(
        duration: widget.widgetTheme.animationDuration,
        width: size,
        height: size,
        decoration: BoxDecoration(
          color: backgroundColor,
          borderRadius: BorderRadius.circular(
            widget.widgetTheme.checkboxBorderRadius,
          ),
          border: Border.all(
            color: borderColor,
            width: widget.widgetTheme.checkboxBorderWidth,
          ),
        ),
        child: widget.isSelected
            ? Icon(
                Icons.check,
                size: widget.widgetTheme.checkboxCheckmarkSize,
                color: widget.widgetTheme.checkboxCheckmarkColor,
              )
            : null,
      ),
    );
  }
}

class _MenuRadioButton extends StatefulWidget {
  final MenuItemThemeExtension widgetTheme;
  final bool isEnabled;
  final bool isSelected;

  const _MenuRadioButton({
    required this.widgetTheme,
    required this.isEnabled,
    required this.isSelected,
  });

  @override
  State<_MenuRadioButton> createState() => _MenuRadioButtonState();
}

class _MenuRadioButtonState extends State<_MenuRadioButton> {
  bool _isHovered = false;

  @override
  Widget build(BuildContext context) {
    final size = widget.widgetTheme.radioButtonSize;

    Color backgroundColor;
    Color borderColor;

    backgroundColor = getMenuRadioBackgroundColor(widget.widgetTheme, widget.isSelected, _isHovered, widget.isEnabled);
    borderColor = getMenuRadioBorderColor(widget.widgetTheme, widget.isSelected, _isHovered, widget.isEnabled);

    return MouseRegion(
      onEnter: (_) => setState(() => _isHovered = true),
      onExit: (_) => setState(() => _isHovered = false),
      child: AnimatedContainer(
        duration: widget.widgetTheme.animationDuration,
        width: size,
        height: size,
        decoration: BoxDecoration(
          color: backgroundColor,
          shape: BoxShape.circle,
          border: Border.all(
            color: borderColor,
            width: widget.widgetTheme.radioButtonBorderWidth,
          ),
        ),
        child: widget.isSelected
            ? Center(
                child: Container(
                  width: widget.widgetTheme.radioButtonInnerSize,
                  height: widget.widgetTheme.radioButtonInnerSize,
                  decoration: BoxDecoration(
                    color: widget.widgetTheme.radioButtonInnerColor,
                    shape: BoxShape.circle,
                  ),
                ),
              )
            : null,
      ),
    );
  }
}
