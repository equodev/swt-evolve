import 'package:flutter/material.dart';
import '../gen/menuitem.dart';
import '../gen/swt.dart';
import '../gen/widget.dart';
import '../impl/item_evolve.dart';
import '../styles.dart';
import '../theme/theme_extensions/menu_theme_extension.dart';
import '../theme/theme_extensions/menuitem_theme_extension.dart';
import '../theme/theme_settings/menuitem_theme_settings.dart';
import 'menu_evolve.dart';

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
      return _buildCascadeMenuItem(context, widgetTheme, menuTheme, isEnabled);
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

  Widget _buildCascadeMenuItem(
    BuildContext context,
    MenuItemThemeExtension widgetTheme,
    MenuThemeExtension menuTheme,
    bool isEnabled,
  ) {
    final textStyle = getMenuItemTextStyle(widgetTheme, isEnabled: isEnabled);

    return Opacity(
      opacity: isEnabled ? 1.0 : widgetTheme.disabledOpacity,
      child: SubmenuButton(
        style: ButtonStyle(
          backgroundColor: WidgetStateProperty.resolveWith((states) {
            if (states.contains(WidgetState.hovered)) {
              return widgetTheme.hoverBackgroundColor;
            }
            return widgetTheme.backgroundColor;
          }),
          padding: WidgetStateProperty.all(widgetTheme.itemPadding),
          minimumSize: WidgetStateProperty.all(Size(widgetTheme.minItemWidth, widgetTheme.itemHeight)),
          shape: WidgetStateProperty.all(
            RoundedRectangleBorder(borderRadius: BorderRadius.circular(widgetTheme.borderRadius)),
          ),
          animationDuration: widgetTheme.animationDuration,
        ),
        menuStyle: MenuStyle(
          backgroundColor: WidgetStateProperty.all(menuTheme.popupBackgroundColor),
          elevation: WidgetStateProperty.all(menuTheme.popupElevation),
          padding: WidgetStateProperty.all(menuTheme.popupPadding),
          shape: WidgetStateProperty.all(
            RoundedRectangleBorder(borderRadius: BorderRadius.circular(menuTheme.borderRadius)),
          ),
        ),
        menuChildren: _buildSubMenuChildren(),
        child: Row(
          children: [
            Expanded(child: Text(state.text ?? '', style: textStyle)),
          ],
        ),
      ),
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
      child: Text(state.text ?? '', style: textStyle),
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
      child: Text(state.text ?? '', style: textStyle),
    );
  }

  Widget _buildPushMenuItem(
    BuildContext context,
    MenuItemThemeExtension widgetTheme,
    bool isEnabled,
  ) {
    final textStyle = getMenuItemTextStyle(widgetTheme, isEnabled: isEnabled);
    final acceleratorText = state.accelerator != null ? formatAccelerator(state.accelerator!) : '';
    final acceleratorStyle = getMenuItemAcceleratorTextStyle(widgetTheme, isEnabled: isEnabled);

    return _MenuItemRow(
      widgetTheme: widgetTheme,
      isEnabled: isEnabled,
      onTap: isEnabled ? _onPushPressed : null,
      trailing: acceleratorText.isNotEmpty
          ? Text(acceleratorText, style: acceleratorStyle)
          : null,
      child: Text(state.text ?? '', style: textStyle),
    );
  }

  List<Widget> _buildSubMenuChildren() {
    return (state.menu?.items ?? []).map((item) => MenuItemSwt(value: item)).toList();
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

  void _onPushPressed() {
    widget.sendSelectionSelection(state, null);
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
        child: GestureDetector(
          onTap: widget.onTap,
          child: AnimatedContainer(
            duration: widget.widgetTheme.animationDuration,
            constraints: BoxConstraints(
              minHeight: widget.widgetTheme.itemHeight,
            ),
            padding: widget.widgetTheme.itemPadding,
            decoration: BoxDecoration(
              color: widget.isEnabled && _isHovered
                  ? widget.widgetTheme.hoverBackgroundColor
                  : widget.widgetTheme.backgroundColor,
              borderRadius: BorderRadius.circular(widget.widgetTheme.borderRadius),
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

    if (widget.isSelected) {
      backgroundColor = widget.widgetTheme.checkboxSelectedColor;
      borderColor = widget.widgetTheme.checkboxSelectedColor;
    } else if (_isHovered && widget.isEnabled) {
      backgroundColor = widget.widgetTheme.checkboxHoverColor;
      borderColor = widget.widgetTheme.checkboxBorderColor;
    } else {
      backgroundColor = widget.widgetTheme.checkboxColor;
      borderColor = widget.widgetTheme.checkboxBorderColor;
    }

    return MouseRegion(
      onEnter: (_) => setState(() => _isHovered = true),
      onExit: (_) => setState(() => _isHovered = false),
      child: AnimatedContainer(
        duration: widget.widgetTheme.animationDuration,
        width: size,
        height: size,
        decoration: BoxDecoration(
          color: backgroundColor,
          borderRadius: BorderRadius.circular(widget.widgetTheme.checkboxBorderRadius),
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

    if (widget.isSelected) {
      if (_isHovered && widget.isEnabled) {
        backgroundColor = widget.widgetTheme.radioButtonSelectedHoverColor;
      } else {
        backgroundColor = widget.widgetTheme.radioButtonSelectedColor;
      }
      borderColor = backgroundColor;
    } else if (_isHovered && widget.isEnabled) {
      backgroundColor = widget.widgetTheme.radioButtonHoverColor;
      borderColor = widget.widgetTheme.radioButtonBorderColor;
    } else {
      backgroundColor = widget.widgetTheme.radioButtonColor;
      borderColor = widget.widgetTheme.radioButtonBorderColor;
    }

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
