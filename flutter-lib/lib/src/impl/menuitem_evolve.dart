import 'dart:async';

import 'package:flutter/material.dart';
import '../theme/theme_settings/menu_theme_settings.dart';
import 'package:flutter/services.dart';
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
import 'utils/image_utils.dart';
import 'utils/pointer.dart';
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
      final split = splitMenuItemText(state.text);
      return tagSemantics(_CascadeMenuItemRow(
        widgetTheme: widgetTheme,
        menuTheme: menuTheme,
        isEnabled: isEnabled,
        text: split.label,
        leading: _buildMenuIcon(widgetTheme, isEnabled),
        trailing: _buildAcceleratorText(widgetTheme, isEnabled, split.shortcut),
        subMenu: state.menu!,
        onArm: _sendArm,
      ));
    }

    if (style.has(SWT.CHECK)) {
      return tagSemantics(_buildCheckMenuItem(context, widgetTheme, isEnabled));
    }

    if (style.has(SWT.RADIO)) {
      return tagSemantics(_buildRadioMenuItem(context, widgetTheme, isEnabled));
    }

    return tagSemantics(_buildPushMenuItem(context, widgetTheme, isEnabled));
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
    final notifier = MenuChangeNotifier.of(context);
    final isAutofocusTarget = notifier?.autofocusItem == state;
    final split = splitMenuItemText(state.text);

    return _MenuItemRow(
      widgetTheme: widgetTheme,
      isEnabled: isEnabled,
      autofocus: isAutofocusTarget,
      focusNode: isAutofocusTarget ? notifier?.autofocusItemFocusNode : null,
      onArm: _sendArm,
      onTap: isEnabled ? _onCheckPressed : null,
      leading: _buildToggleLeading(
        indicator: _MenuCheckbox(
          widgetTheme: widgetTheme,
          isEnabled: isEnabled,
          isSelected: isChecked,
        ),
        widgetTheme: widgetTheme,
        isEnabled: isEnabled,
      ),
      trailing: _buildAcceleratorText(widgetTheme, isEnabled, split.shortcut),
      child: Text(split.label, style: textStyle),
    );
  }

  Widget? _buildAcceleratorText(
    MenuItemThemeExtension widgetTheme,
    bool isEnabled,
    String? shortcut,
  ) {
    final text = shortcut ??
        (state.accelerator != null ? formatAccelerator(state.accelerator!) : null);
    if (text == null) return null;
    return Text(
      text,
      style: getMenuItemAcceleratorTextStyle(widgetTheme, isEnabled: isEnabled),
    );
  }

  Widget _buildRadioMenuItem(
      BuildContext context,
      MenuItemThemeExtension widgetTheme,
      bool isEnabled,
      ) {
    final textStyle = getMenuItemTextStyle(widgetTheme, isEnabled: isEnabled);
    final isSelected = _localSelection ?? false;
    final notifier = MenuChangeNotifier.of(context);
    final isAutofocusTarget = notifier?.autofocusItem == state;
    final split = splitMenuItemText(state.text);

    return _MenuItemRow(
      widgetTheme: widgetTheme,
      isEnabled: isEnabled,
      autofocus: isAutofocusTarget,
      focusNode: isAutofocusTarget ? notifier?.autofocusItemFocusNode : null,
      onArm: _sendArm,
      onTap: isEnabled ? _onRadioPressed : null,
      leading: _buildToggleLeading(
        indicator: _MenuRadioButton(
          widgetTheme: widgetTheme,
          isEnabled: isEnabled,
          isSelected: isSelected,
        ),
        widgetTheme: widgetTheme,
        isEnabled: isEnabled,
      ),
      trailing: _buildAcceleratorText(widgetTheme, isEnabled, split.shortcut),
      child: Text(split.label, style: textStyle),
    );
  }

  Widget _buildPushMenuItem(
      BuildContext context,
      MenuItemThemeExtension widgetTheme,
      bool isEnabled,
      ) {
    final textStyle = getMenuItemTextStyle(widgetTheme, isEnabled: isEnabled);

    final notifier = MenuChangeNotifier.of(context);
    final capturedState = state;
    final capturedWidget = widget;

    final split = splitMenuItemText(capturedState.text);
    final isAutofocusTarget = notifier?.autofocusItem == capturedState;

    return _MenuItemRow(
      widgetTheme: widgetTheme,
      isEnabled: isEnabled,
      autofocus: isAutofocusTarget,
      focusNode: isAutofocusTarget ? notifier?.autofocusItemFocusNode : null,
      onArm: _sendArm,
      onTap: isEnabled ? () {
        if (notifier == null) {
          capturedWidget.sendSelectionSelection(capturedState, null);
          return;
        }
        notifier.registerPendingChange(
            () => capturedWidget.sendSelectionSelection(capturedState, null));
        notifier.closeMenu();
      } : null,
      leading: _buildMenuIcon(widgetTheme, isEnabled),
      trailing: _buildAcceleratorText(widgetTheme, isEnabled, split.shortcut),
      child: Text(split.label, style: textStyle),
    );
  }

  Widget? _buildMenuIcon(MenuItemThemeExtension widgetTheme, bool isEnabled) {
    if (state.image == null) return null;
    return ImageUtils.buildVImage(
      state.image,
      size: widgetTheme.iconSize,
      color: isEnabled ? widgetTheme.iconColor : widgetTheme.disabledIconColor,
      enabled: isEnabled,
      useBinaryImage: true,
      renderAsIcon: true,
    );
  }

  Widget _buildToggleLeading({
    required Widget indicator,
    required MenuItemThemeExtension widgetTheme,
    required bool isEnabled,
  }) {
    final icon = _buildMenuIcon(widgetTheme, isEnabled);
    if (icon == null) return indicator;
    return Row(
      mainAxisSize: MainAxisSize.min,
      children: [
        indicator,
        SizedBox(width: widgetTheme.iconTextSpacing),
        icon,
      ],
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
    MenuChangeNotifier.of(context)?.closeMenu();
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
    notifier?.closeMenu();
  }

  void _sendArm() => widget.sendArmArm(state, null);

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
  final bool autofocus;
  // Externally-supplied so the owning MenuImpl can explicitly requestFocus() on this exact item
  // a frame after the menu opens (see MenuImpl._focusFirstItemNextFrame) instead of relying
  // solely on Focus(autofocus:)'s own timing. Null means "use an internal, unmanaged FocusNode".
  final FocusNode? focusNode;
  final VoidCallback? onArm;

  const _MenuItemRow({
    required this.widgetTheme,
    required this.isEnabled,
    this.onArm,
    this.onTap,
    this.leading,
    required this.child,
    this.trailing,
    this.autofocus = false,
    this.focusNode,
  });

  @override
  State<_MenuItemRow> createState() => _MenuItemRowState();
}

class _MenuItemRowState extends State<_MenuItemRow> {
  FocusNode? _ownFocusNode;
  bool _isFocused = false;

  FocusNode get _focusNode =>
      widget.focusNode ?? (_ownFocusNode ??= FocusNode(debugLabel: 'MenuItem'));

  @override
  void dispose() {
    _ownFocusNode?.dispose();
    super.dispose();
  }

  // Focus is the menu's single current item, so hover moves it here and the arrow keys continue from it.
  void _handleHover(PointerHoverEvent event) {
    if (!widget.isEnabled || _focusNode.hasFocus) return;
    _focusNode.requestFocus();
    FocusTraversalGroup.of(context).invalidateScopeData(FocusScope.of(context));
  }

  void _handleFocusChange(bool focused) {
    setState(() => _isFocused = focused);
    if (focused) widget.onArm?.call();
  }

  // Without this, _MenuItemRow is mouse-only: it has no FocusNode at all, so a freshly-opened
  // menu has nothing for arrow-key navigation to move between and nothing for Enter/Space to
  // activate -- unlike Flutter's stock MenuItemButton, this is a fully custom row widget.
  KeyEventResult _handleKeyEvent(FocusNode node, KeyEvent event) {
    if (event is! KeyDownEvent) {
      return KeyEventResult.ignored;
    }
    final key = event.logicalKey;
    if (widget.onTap != null &&
        (key == LogicalKeyboardKey.enter ||
            key == LogicalKeyboardKey.numpadEnter ||
            key == LogicalKeyboardKey.space)) {
      widget.onTap!();
      return KeyEventResult.handled;
    }
    return KeyEventResult.ignored;
  }

  @override
  Widget build(BuildContext context) {
    return Opacity(
      opacity: widget.isEnabled ? 1.0 : widget.widgetTheme.disabledOpacity,
      child: Focus(
        focusNode: _focusNode,
        autofocus: widget.isEnabled && widget.autofocus,
        canRequestFocus: widget.isEnabled,
        onKeyEvent: _handleKeyEvent,
        onFocusChange: _handleFocusChange,
        child: MouseRegion(
          onHover: _handleHover,
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
                color: getMenuItemRowBackgroundColor(widget.widgetTheme, widget.isEnabled, _isFocused),
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
      ),
    );
  }
}

final _cascadeOpenKeys = {
  LogicalKeyboardKey.arrowRight,
  LogicalKeyboardKey.arrowLeft,
  LogicalKeyboardKey.enter,
  LogicalKeyboardKey.numpadEnter,
  LogicalKeyboardKey.space,
};

/// Natively a cascade opens on hover, a click, Right, Enter or Space, never because Up or Down made it
/// the current item; Flutter opens one whenever it gets the focus.
class _CascadeMenuController extends MenuController {
  _CascadeMenuController(this._mayOpen);

  final bool Function() _mayOpen;

  @override
  void open({Offset? position}) {
    if (_mayOpen()) super.open(position: position);
  }
}

class _CascadeMenuItemRow extends StatefulWidget {
  final MenuItemThemeExtension widgetTheme;
  final MenuThemeExtension menuTheme;
  final bool isEnabled;
  final String text;
  final Widget? leading;
  final Widget? trailing;
  final VMenu subMenu;
  final VoidCallback onArm;

  const _CascadeMenuItemRow({
    required this.widgetTheme,
    required this.menuTheme,
    required this.isEnabled,
    required this.text,
    this.leading,
    this.trailing,
    required this.subMenu,
    required this.onArm,
  });

  @override
  State<_CascadeMenuItemRow> createState() => _CascadeMenuItemRowState();
}

class _CascadeMenuItemRowState extends State<_CascadeMenuItemRow> {
  late final MenuController _menuController = _CascadeMenuController(() =>
      _isHovered ||
      _isPressed ||
      HardwareKeyboard.instance.logicalKeysPressed.any(_cascadeOpenKeys.contains));
  late final FocusNode _buttonFocusNode =
      FocusNode(debugLabel: 'Cascade', onKeyEvent: _enterOpenSubmenu);
  final FocusNode _submenuFocusNode = FocusNode(debugLabel: 'Cascade.menu');
  // SizedBox.shrink() placeholder ensures SubmenuButton is always interactive
  List<Widget> _menuChildren = const [SizedBox.shrink()];
  bool _isHovered = false;
  // A touch has no hover, so the press itself has to let the tap open the submenu.
  bool _isPressed = false;
  bool _shown = false;

  @override
  void initState() {
    super.initState();
    // If items are already provided (non-lazy case), use them immediately
    final existingItems = widget.subMenu.items;
    if (existingItems != null && existingItems.isNotEmpty) {
      _menuChildren = existingItems
          .map((item) => MenuItemSwt(key: ValueKey(item.id), value: item))
          .toList();
    }
  }

  @override
  void didUpdateWidget(_CascadeMenuItemRow oldWidget) {
    super.didUpdateWidget(oldWidget);
    final items = widget.subMenu.items;
    if (items != null &&
        items.isNotEmpty &&
        !_sameItems(items, oldWidget.subMenu.items)) {
      _menuChildren = items
          .map((item) => MenuItemSwt(key: ValueKey(item.id), value: item))
          .toList();
    }
  }

  // Compares the item properties that affect rendering. Keep in sync with the
  // render-affecting fields of VMenuItem. Image is compared by presence only
  // (not its bytes) to avoid serializing icon data on every parent rebuild.
  bool _sameItems(List<VMenuItem>? a, List<VMenuItem>? b) {
    if (a == null || b == null) return a == b;
    if (a.length != b.length) return false;
    for (var i = 0; i < a.length; i++) {
      final x = a[i], y = b[i];
      if (x.id != y.id ||
          x.style != y.style ||
          x.text != y.text ||
          x.enabled != y.enabled ||
          x.selection != y.selection ||
          x.accelerator != y.accelerator ||
          x.toolTipText != y.toolTipText ||
          x.menu?.id != y.menu?.id ||
          (x.image == null) != (y.image == null)) {
        return false;
      }
    }
    return true;
  }

  @override
  void dispose() {
    EquoCommService.remove("Menu/${widget.subMenu.id}");
    _buttonFocusNode.dispose();
    _submenuFocusNode.dispose();
    super.dispose();
  }

  // Another item of this menu became current, from hover or the keyboard: the submenu goes, as natively.
  void _handleFocusChange(bool focused) {
    if (focused) widget.onArm();
    _closeSubmenuIfFocusLeft();
  }

  void _closeSubmenuIfFocusLeft() {
    if (_menuController.isOpen && !_buttonFocusNode.hasFocus && !_submenuFocusNode.hasFocus) {
      _menuController.close();
    }
  }

  // Natively a hover-opened submenu takes Up/Down; one the keyboard opened already holds the focus.
  KeyEventResult _enterOpenSubmenu(FocusNode node, KeyEvent event) {
    if (event is KeyUpEvent || !_menuController.isOpen || !_isHovered) {
      return KeyEventResult.ignored;
    }
    final key = event.logicalKey;
    if (key != LogicalKeyboardKey.arrowDown && key != LogicalKeyboardKey.arrowUp) {
      return KeyEventResult.ignored;
    }
    final items = _submenuFocusNode.traversalDescendants.toList();
    if (items.isEmpty) return KeyEventResult.ignored;
    (key == LogicalKeyboardKey.arrowDown ? items.first : items.last).requestFocus();
    return KeyEventResult.handled;
  }

  void _onHover(bool hovering) {
    _isHovered = hovering;
  }

  // Deferred past the focus change that opened the submenu, so Java hears the item's Arm first.
  void _onSubmenuOpen() {
    _shown = true;
    scheduleMicrotask(() {
      if (mounted && _shown) _requestItems();
    });
  }

  // Hide goes out when the submenu starts closing: onClose waits for the closing animation, by
  // which time the next cascade has already sent its Show.
  void _onSubmenuAnimation(AnimationStatus status) {
    if (status == AnimationStatus.reverse || status.isDismissed) _sendHide();
  }

  void _sendHide() {
    if (!_shown) return;
    _shown = false;
    MenuSwt<VMenu>(value: widget.subMenu).sendMenuHide(widget.subMenu, null);
  }

  void _requestItems() {
    final channelName = "Menu/${widget.subMenu.id}";
    EquoCommService.remove(channelName);
    EquoCommService.on<VMenu>(channelName, (VMenu updatedMenu) {
      EquoCommService.remove(channelName);
      if (!mounted) return;
      final items = (updatedMenu.items ?? [])
          .map((item) => MenuItemSwt(key: ValueKey(item.id), value: item))
          .toList();
      setState(() {
        _menuChildren = items.isEmpty ? const [SizedBox.shrink()] : items;
      });
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
        child: Listener(
          onPointerDown: (_) => _isPressed = true,
          onPointerUp: (_) => scheduleMicrotask(() => _isPressed = false),
          onPointerCancel: (_) => _isPressed = false,
          child: SubmenuButton(
          controller: _menuController,
          focusNode: _buttonFocusNode,
          onHover: _onHover,
          onOpen: _onSubmenuOpen,
          onClose: _sendHide,
          onAnimationStatusChanged: _onSubmenuAnimation,
          onFocusChange: _handleFocusChange,
          style: ButtonStyle(
            backgroundColor: WidgetStateProperty.resolveWith((states) {
              if (!widget.isEnabled) return widget.widgetTheme.backgroundColor;
              // SubmenuButton focuses itself on hover, so focus is the single current-item signal.
              if (states.contains(WidgetState.focused) || _menuController.isOpen) {
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
            side: getMenuPopupSide(widget.menuTheme),
            elevation: WidgetStateProperty.all(widget.menuTheme.popupElevation),
            padding: WidgetStateProperty.all(widget.menuTheme.popupPadding),
            shape: WidgetStateProperty.all(
              RoundedRectangleBorder(
                borderRadius: BorderRadius.circular(widget.menuTheme.borderRadius),
              ),
            ),
          ),
          menuChildren: [
            pointerInterceptor(
              Focus(
                focusNode: _submenuFocusNode,
                canRequestFocus: false,
                onFocusChange: (_) => _closeSubmenuIfFocusLeft(),
                child: Column(mainAxisSize: MainAxisSize.min, children: _menuChildren),
              ),
            ),
          ],
          child: Row(
            children: [
              if (widget.leading != null) ...[
                widget.leading!,
                SizedBox(width: widget.widgetTheme.iconTextSpacing),
              ],
              Expanded(
                child: Text(widget.text, style: textStyle),
              ),
              if (widget.trailing != null) ...[
                SizedBox(width: widget.widgetTheme.textAcceleratorSpacing),
                widget.trailing!,
              ],
            ],
          ),
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