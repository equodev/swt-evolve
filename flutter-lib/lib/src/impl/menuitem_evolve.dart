import 'package:flutter/material.dart';
import 'package:flutter/widgets.dart';
import '../gen/menuitem.dart';
import '../gen/widget.dart';
import '../impl/item_evolve.dart';
import '../styles.dart';
import 'color_utils.dart';
import '../gen/swt.dart';
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
    final style = StyleBits(state.style);
    final foregroundColor = getForeground();
    final enabled = state.enabled ?? true;

    if (_isRadio) {
      final notifier = MenuChangeNotifier.of(context);
      if (notifier != null) {
        WidgetsBinding.instance.addPostFrameCallback((_) {
          notifier.menuState.registerRadioCallback(state, _setLocalSelection);
        });
      }
    }

    if (style.has(SWT.SEPARATOR)) {
      return Container(
        height: 1,
        margin: const EdgeInsets.symmetric(vertical: 4, horizontal: 8),
        color: getBorderColor(),
      );
    }

    if (style.has(SWT.CASCADE) && state.menu != null) {
      return SubmenuButton(
        menuStyle: MenuStyle(
          backgroundColor: MaterialStateProperty.all(getBackground()),
          elevation: MaterialStateProperty.all(8.0),
        ),
        menuChildren: _buildSubMenuChildren(),
        child: Row(
          mainAxisSize: MainAxisSize.min,
          children: [
            if (state.image != null)
              Padding(
                padding: const EdgeInsets.only(right: 8.0),
                child: Icon(Icons.image, size: 16, color: foregroundColor),
              ),
            Text(
              state.text ?? '',
              style: TextStyle(color: foregroundColor, fontSize: 14),
            ),
          ],
        ),
      );
    }

    if (style.has(SWT.CHECK)) {
      return MenuItemButton(
        leadingIcon: Icon(
          _localSelection ?? false ? Icons.check_box : Icons.check_box_outline_blank,
          size: 18,
          color: foregroundColor,
        ),
        onPressed: enabled
            ? () {
                setState(() {
                  _localSelection = !(_localSelection ?? false);
                });
                final notifier = MenuChangeNotifier.of(context);
                if (notifier != null) {
                  notifier.registerPendingChange(() {
                    widget.sendSelectionSelection(state, null);
                  });
                } else {
                  widget.sendSelectionSelection(state, null);
                }
              }
            : null,
        closeOnActivate: false,
        child: Text(
          state.text ?? '',
          style: TextStyle(color: foregroundColor, fontSize: 14),
        ),
      );
    }

    if (style.has(SWT.RADIO)) {
      return MenuItemButton(
        leadingIcon: Icon(
          _localSelection ?? false ? Icons.radio_button_checked : Icons.radio_button_unchecked,
          size: 18,
          color: foregroundColor,
        ),
        onPressed: enabled
            ? () {
                final notifier = MenuChangeNotifier.of(context);
                if (notifier != null) {
                  notifier.menuState.notifyRadioSelected(state);
                }
                setState(() {
                  _localSelection = true;
                });
                if (notifier != null) {
                  notifier.registerPendingChange(() {
                    widget.sendSelectionSelection(state, null);
                  });
                } else {
                  widget.sendSelectionSelection(state, null);
                }
              }
            : null,
        closeOnActivate: false,
        child: Text(
          state.text ?? '',
          style: TextStyle(color: foregroundColor, fontSize: 14),
        ),
      );
    }

    return MenuItemButton(
      leadingIcon: state.image != null
          ? Icon(Icons.image, size: 16, color: foregroundColor)
          : null,
      trailingIcon: state.accelerator != null
          ? Text(
              _formatAccelerator(state.accelerator!),
              style: TextStyle(
                color: foregroundColor.withOpacity(0.6),
                fontSize: 12,
              ),
            )
          : null,
      onPressed: enabled
          ? () {
              widget.sendSelectionSelection(state, null);
            }
          : null,
      child: Text(
        state.text ?? '',
        style: TextStyle(color: foregroundColor, fontSize: 14),
      ),
    );
  }

  List<Widget> _buildSubMenuChildren() {
    return (state.menu?.items ?? []).map((item) => MenuItemSwt(value: item)).toList();
  }

  String _formatAccelerator(int accelerator) {
    return '';
  }
}
