import 'package:flutter/material.dart';
import '../gen/ccombo.dart';
import '../gen/event.dart';
import '../gen/swt.dart';
import '../gen/widget.dart';
import '../impl/composite_evolve.dart';
import '../styles.dart';
import '../theme/theme_extensions/ccombo_theme_extension.dart';
import 'utils/text_utils.dart';
import 'utils/widget_utils.dart';
import 'color_utils.dart';

class CComboImpl<T extends CComboSwt, V extends VCCombo>
    extends CompositeImpl<T, V> {
  late TextEditingController _controller;
  FocusNode? _focusNode;
  bool _isFocused = false;
  bool _isHovered = false;

  @override
  void initState() {
    super.initState();
    _controller = TextEditingController(text: state.text);
    _focusNode = FocusNode();
    _focusNode!.addListener(_handleFocusChange);
  }

  @override
  void extraSetState() {
    String newText = state.text ?? "";
    if (_controller.text != newText) {
      _controller.text = newText;
      _controller.selection = TextSelection.collapsed(offset: newText.length);
    }
  }

  @override
  Widget build(BuildContext context) {
    final widgetTheme = Theme.of(context).extension<CComboThemeExtension>()!;
    final styleBits = StyleBits(state.style);
    final bool isReadOnly = styleBits.has(SWT.READ_ONLY);
    final bool isSimple = styleBits.has(SWT.SIMPLE);
    final bool isEnabled = state.enabled ?? true;
    final bool listVisible = state.listVisible ?? false;

    // Custom colors support
    final customBg = state.background != null 
        ? colorFromVColor(state.background!, defaultColor: widgetTheme.backgroundColor)
        : null;
    final customFg = state.foreground != null
        ? colorFromVColor(state.foreground!, defaultColor: widgetTheme.textColor)
        : null;

    final bool isActive = _isFocused || _isHovered;
    final Color currentBg = !isEnabled
        ? widgetTheme.disabledBackgroundColor
        : customBg ?? widgetTheme.backgroundColor;
    
    final Color currentBorderColor = !isEnabled
        ? widgetTheme.disabledBorderColor
        : (isActive ? widgetTheme.borderColor : Colors.transparent);

    final textColor = !isEnabled
        ? widgetTheme.disabledTextColor
        : customFg ?? widgetTheme.textColor;
    
    final textStyle = getTextStyle(
      context: context,
      font: state.font,
      textColor: textColor,
      baseTextStyle: widgetTheme.textStyle,
    );

    final borderWidth = styleBits.has(SWT.BORDER) ? 3.0 : widgetTheme.borderWidth;

    // Simple mode with visible list
    if (isSimple || listVisible) {
      return _StyledSimpleCCombo(
        state: state,
        widgetTheme: widgetTheme,
        controller: _controller,
        focusNode: _focusNode,
        items: state.items ?? [],
        enabled: isEnabled,
        readOnly: isReadOnly,
        textStyle: textStyle,
        backgroundColor: currentBg,
        borderColor: currentBorderColor,
        borderWidth: borderWidth,
        onChanged: isEnabled ? onChanged : null,
        onTextChanged: onTextChanged,
        onTextSubmitted: onTextSubmitted,
        onMouseEnter: handleMouseEnter,
        onMouseExit: handleMouseExit,
      );
    }

    // Dropdown mode
    return MouseRegion(
      onEnter: (_) => setState(() { _isHovered = true; handleMouseEnter(); }),
      onExit: (_) => setState(() { _isHovered = false; handleMouseExit(); }),
      child: AnimatedContainer(
        duration: widgetTheme.animationDuration,
        decoration: BoxDecoration(
          color: currentBg,
          borderRadius: BorderRadius.circular(widgetTheme.borderRadius),
          border: Border.all(color: currentBorderColor, width: borderWidth),
        ),
        child: _StyledDropdownCCombo(
          state: state,
          widgetTheme: widgetTheme,
          controller: _controller,
          focusNode: _focusNode,
          items: state.items ?? [],
          enabled: isEnabled,
          isReadOnly: isReadOnly,
          textStyle: textStyle,
          onSelected: isEnabled ? onChanged : null,
        ),
      ),
    );
  }

  void onChanged(String? value) {
    setState(() {
      state.text = value;
      _controller.text = value ?? "";
    });
    widget.sendSelectionSelection(state, VEvent()..text = value);
  }

  void onTextChanged(String value) {
    state.text = value;
    widget.sendModifyModify(state, VEvent()..text = value);
  }

  void onTextSubmitted(String text) {
    widget.sendSelectionDefaultSelection(state, VEvent()..text = text);
    widget.sendVerifyVerify(state, VEvent()..text = text);
  }

  void handleMouseEnter() => widget.sendMouseTrackMouseEnter(state, null);
  void handleMouseExit() => widget.sendMouseTrackMouseExit(state, null);

  void _handleFocusChange() {
    if (!mounted) return;
    setState(() => _isFocused = _focusNode!.hasFocus);
    if (_focusNode!.hasFocus) {
      widget.sendFocusFocusIn(state, null);
    } else {
      widget.sendFocusFocusOut(state, null);
    }
  }

  @override
  void dispose() {
    _controller.dispose();
    _focusNode?.removeListener(_handleFocusChange);
    _focusNode?.dispose();
    super.dispose();
  }
}

// Dropdown mode widget (READ_ONLY or editable dropdown)
class _StyledDropdownCCombo extends StatelessWidget {
  final VCCombo state;
  final CComboThemeExtension widgetTheme;
  final TextEditingController controller;
  final FocusNode? focusNode;
  final List<String> items;
  final bool enabled;
  final bool isReadOnly;
  final TextStyle textStyle;
  final ValueChanged<String?>? onSelected;

  const _StyledDropdownCCombo({
    required this.state,
    required this.widgetTheme,
    required this.controller,
    this.focusNode,
    required this.items,
    required this.enabled,
    required this.isReadOnly,
    required this.textStyle,
    this.onSelected,
  });

  @override
  Widget build(BuildContext context) {
    if (isReadOnly && focusNode != null) {
      focusNode!.canRequestFocus = false;
    }

    return LayoutBuilder(
      builder: (context, constraints) {
        final hasConstraints = hasBounds(state.bounds);
        final width = hasConstraints ? constraints.maxWidth : null;

        return DropdownMenu<String>(
          enabled: enabled,
          focusNode: focusNode,
          controller: controller,
          width: width,
          initialSelection: state.text,
          requestFocusOnTap: !isReadOnly,
          enableSearch: !isReadOnly,
          textStyle: textStyle,
          textAlign: _getTextAlign(),
          inputDecorationTheme: InputDecorationTheme(
            border: InputBorder.none,
            isDense: true,
            contentPadding: widgetTheme.textFieldPadding,
          ),
          menuStyle: MenuStyle(
            backgroundColor: WidgetStateProperty.all(widgetTheme.backgroundColor),
            alignment: _getMenuAlignment(),
          ),
          trailingIcon: Icon(
            Icons.arrow_drop_down,
            color: enabled ? widgetTheme.iconColor : widgetTheme.disabledIconColor,
            size: widgetTheme.iconSize,
          ),
          onSelected: onSelected,
          dropdownMenuEntries: items.map<DropdownMenuEntry<String>>((item) {
            final bool isSelected = item == state.text;
            return DropdownMenuEntry<String>(
              value: item,
              label: item,
              labelWidget: Align(
                alignment: _getAlignment(),
                child: Text(item, style: textStyle),
              ),
              style: MenuItemButton.styleFrom(
                foregroundColor: textStyle.color,
                minimumSize: Size(width ?? 0, widgetTheme.itemHeight),
                padding: widgetTheme.textFieldPadding,
                overlayColor: widgetTheme.hoverBackgroundColor,
                backgroundColor: isSelected
                    ? widgetTheme.selectedItemBackgroundColor
                    : Colors.transparent,
              ),
            );
          }).toList(),
        );
      },
    );
  }

  TextAlign _getTextAlign() {
    if (state.alignment == SWT.CENTER) return TextAlign.center;
    if (state.alignment == SWT.RIGHT || state.alignment == SWT.TRAIL) {
      return TextAlign.right;
    }
    return TextAlign.left;
  }

  Alignment _getAlignment() {
    if (state.alignment == SWT.CENTER) return Alignment.center;
    if (state.alignment == SWT.RIGHT || state.alignment == SWT.TRAIL) {
      return Alignment.centerRight;
    }
    return Alignment.centerLeft;
  }

  AlignmentGeometry? _getMenuAlignment() {
    if (state.alignment == SWT.RIGHT || state.alignment == SWT.TRAIL) {
      return AlignmentDirectional.topEnd;
    }
    return null;
  }
}

// Simple mode widget (always visible list)
class _StyledSimpleCCombo extends StatelessWidget {
  final VCCombo state;
  final CComboThemeExtension widgetTheme;
  final TextEditingController controller;
  final FocusNode? focusNode;
  final List<String> items;
  final bool enabled;
  final bool readOnly;
  final TextStyle textStyle;
  final Color backgroundColor;
  final Color borderColor;
  final double borderWidth;
  final ValueChanged<String?>? onChanged;
  final ValueChanged<String>? onTextChanged;
  final ValueChanged<String>? onTextSubmitted;
  final VoidCallback? onMouseEnter;
  final VoidCallback? onMouseExit;

  const _StyledSimpleCCombo({
    required this.state,
    required this.widgetTheme,
    required this.controller,
    this.focusNode,
    required this.items,
    required this.enabled,
    required this.readOnly,
    required this.textStyle,
    required this.backgroundColor,
    required this.borderColor,
    required this.borderWidth,
    this.onChanged,
    this.onTextChanged,
    this.onTextSubmitted,
    this.onMouseEnter,
    this.onMouseExit,
  });

  @override
  Widget build(BuildContext context) {
    return MouseRegion(
      onEnter: (_) => onMouseEnter?.call(),
      onExit: (_) => onMouseExit?.call(),
      child: Container(
        decoration: BoxDecoration(
          color: backgroundColor,
          borderRadius: BorderRadius.circular(widgetTheme.borderRadius),
          border: Border.all(color: borderColor, width: borderWidth),
        ),
        child: Column(
          mainAxisSize: MainAxisSize.min,
          crossAxisAlignment: CrossAxisAlignment.stretch,
          children: [
            // Text field
            Padding(
              padding: widgetTheme.textFieldPadding,
              child: TextField(
                controller: controller,
                focusNode: focusNode,
                enabled: enabled,
                readOnly: readOnly,
                textAlign: _getTextAlign(),
                style: textStyle,
                decoration: const InputDecoration.collapsed(hintText: ''),
                maxLength: state.textLimit,
                onChanged: readOnly ? null : onTextChanged,
                onSubmitted: readOnly ? null : onTextSubmitted,
              ),
            ),
            // Divider
            if (items.isNotEmpty)
              Divider(
                height: widgetTheme.dividerHeight,
                thickness: widgetTheme.dividerThickness,
                color: widgetTheme.dividerColor,
              ),
            // Items list
            ...items.map((item) {
              final bool isSelected = item == state.text;
              return InkWell(
                hoverColor: widgetTheme.hoverBackgroundColor.withOpacity(0.1),
                onTap: enabled ? () => onChanged?.call(item) : null,
                child: Container(
                  height: widgetTheme.itemHeight,
                  padding: widgetTheme.textFieldPadding,
                  color: isSelected
                      ? widgetTheme.selectedItemBackgroundColor
                      : Colors.transparent,
                  alignment: _getAlignment(),
                  child: Text(item, style: textStyle),
                ),
              );
            }),
          ],
        ),
      ),
    );
  }

  TextAlign _getTextAlign() {
    if (state.alignment == SWT.CENTER) return TextAlign.center;
    if (state.alignment == SWT.RIGHT || state.alignment == SWT.TRAIL) {
      return TextAlign.right;
    }
    return TextAlign.left;
  }

  Alignment _getAlignment() {
    if (state.alignment == SWT.CENTER) return Alignment.center;
    if (state.alignment == SWT.RIGHT || state.alignment == SWT.TRAIL) {
      return Alignment.centerRight;
    }
    return Alignment.centerLeft;
  }
}