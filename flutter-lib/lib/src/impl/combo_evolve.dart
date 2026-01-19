import 'package:flutter/material.dart';
import '../gen/combo.dart';
import '../gen/event.dart';
import '../gen/swt.dart';
import '../gen/widget.dart';
import '../impl/composite_evolve.dart';
import '../styles.dart';
import '../theme/theme_extensions/combo_theme_extension.dart';
import 'utils/text_utils.dart';
import 'widget_config.dart';
import 'utils/widget_utils.dart';

class ComboImpl<T extends ComboSwt, V extends VCombo>
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
    final widgetTheme = Theme.of(context).extension<ComboThemeExtension>()!;
    final styleBits = StyleBits(state.style);
    final bool isReadOnly = styleBits.has(SWT.READ_ONLY);
    final bool isSimple = styleBits.has(SWT.SIMPLE);
    final bool isEnabled = state.enabled ?? true;

    final bool isActive = _isFocused || _isHovered;
    final Color currentBg = isEnabled 
        ? widgetTheme.backgroundColor 
        : widgetTheme.disabledBackgroundColor;
    
    final Color currentBorderColor = isActive 
        ? widgetTheme.borderColor 
        : Colors.transparent; 

    final textColor = isEnabled ? widgetTheme.textColor : widgetTheme.disabledTextColor;
    
    final textStyle = getTextStyle(
      context: context, 
      font: state.font, 
      textColor: textColor, 
      baseTextStyle: widgetTheme.textStyle
    );

    if (isSimple) {
      return _StyledSimpleCombo(
        state: state,
        widgetTheme: widgetTheme,
        controller: _controller,
        focusNode: _focusNode,
        items: state.items ?? [],
        value: state.text,
        enabled: isEnabled,
        readOnly: isReadOnly,
        textStyle: textStyle,
        onChanged: isEnabled ? onChanged : null,
        onTextChanged: onTextChanged,
        onTextSubmitted: onTextSubmitted,
        onMouseEnter: handleMouseEnter,
        onMouseExit: handleMouseExit,
      );
    }

    return MouseRegion(
      onEnter: (_) => setState(() { _isHovered = true; handleMouseEnter(); }),
      onExit: (_) => setState(() { _isHovered = false; handleMouseExit(); }),
      child: AnimatedContainer(
        duration: widgetTheme.animationDuration,
        decoration: BoxDecoration(
          color: currentBg,
          borderRadius: BorderRadius.circular(widgetTheme.borderRadius),
          border: Border.all(
            color: currentBorderColor, 
            width: isActive ? widgetTheme.borderWidth : widgetTheme.borderWidth
          ),
        ),
        child: _StyledDropdownMenu(
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

class _StyledDropdownMenu extends StatelessWidget {
  final VCombo state;
  final ComboThemeExtension widgetTheme;
  final TextEditingController controller;
  final FocusNode? focusNode;
  final List<String> items;
  final bool enabled;
  final bool isReadOnly;
  final TextStyle textStyle;
  final ValueChanged<String?>? onSelected;

  const _StyledDropdownMenu({
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
        return DropdownMenu<String>(
          enabled: enabled,
          focusNode: focusNode,
          controller: controller,
          width: constraints.maxWidth, 
          initialSelection: state.text,
          requestFocusOnTap: !isReadOnly, 
          enableSearch: !isReadOnly,
          textStyle: textStyle,
          inputDecorationTheme: InputDecorationTheme(
            border: InputBorder.none,
            isDense: true,
            contentPadding: widgetTheme.textFieldPadding,
          ),
          menuStyle: MenuStyle(
            backgroundColor: WidgetStateProperty.all(widgetTheme.backgroundColor),
          ),
          trailingIcon: Icon(Icons.arrow_drop_down, color: widgetTheme.iconColor),
          onSelected: onSelected,
          dropdownMenuEntries: items.map<DropdownMenuEntry<String>>((item) {
            final bool isSelected = item == state.text;
            return DropdownMenuEntry<String>(
              value: item,
              label: item,
              style: MenuItemButton.styleFrom(
                foregroundColor: textStyle.color,
                minimumSize: Size(constraints.maxWidth, widgetTheme.itemHeight),
                padding: widgetTheme.textFieldPadding,
                overlayColor: widgetTheme.hoverBackgroundColor,
                backgroundColor: isSelected 
                    ? widgetTheme.selectedItemBackgroundColor 
                    : Colors.transparent,
              ),
            );
          }).toList(),
        );
      }
    );
  }
}

class _StyledSimpleCombo extends StatelessWidget {
  final VCombo state;
  final ComboThemeExtension widgetTheme;
  final TextEditingController controller;
  final FocusNode? focusNode;
  final List<String> items;
  final String? value;
  final bool enabled;
  final bool readOnly;
  final TextStyle textStyle;
  final ValueChanged<String?>? onChanged;
  final ValueChanged<String>? onTextChanged;
  final ValueChanged<String>? onTextSubmitted;
  final VoidCallback? onMouseEnter;
  final VoidCallback? onMouseExit;

  const _StyledSimpleCombo({
    required this.state,
    required this.widgetTheme,
    required this.controller,
    this.focusNode,
    required this.items,
    this.value,
    required this.textStyle,
    this.onChanged,
    this.onTextChanged,
    this.onTextSubmitted,
    required this.enabled,
    required this.readOnly,
    this.onMouseEnter,
    this.onMouseExit,
  });

  @override
  Widget build(BuildContext context) {
    return MouseRegion(
      onEnter: (_) => onMouseEnter?.call(),
      onExit: (_) => onMouseExit?.call(),
      child: Column(
        mainAxisSize: MainAxisSize.min,
        crossAxisAlignment: CrossAxisAlignment.stretch,
        children: [
          Padding(
            padding: widgetTheme.textFieldPadding,
            child: TextField(
              controller: controller,
              focusNode: focusNode,
              readOnly: readOnly,
              style: textStyle,
              decoration: const InputDecoration.collapsed(hintText: ''),
              onChanged: readOnly ? null : onTextChanged,
              onSubmitted: readOnly ? null : onTextSubmitted,
            ),
          ),
          if (items.isNotEmpty)
            Divider(height: widgetTheme.dividerHeight, thickness: widgetTheme.dividerThickness, color: widgetTheme.dividerColor),
          ...items.map((item) {
            final bool isSelected = item == value;
            return InkWell(
              hoverColor: widgetTheme.hoverBackgroundColor.withOpacity(0.1),
              onTap: enabled ? () => onChanged?.call(item) : null,
              child: Container(
                width: double.infinity,
                color: isSelected ? widgetTheme.selectedItemBackgroundColor : Colors.transparent,
                alignment: Alignment.centerLeft,
                child: Text(item, style: textStyle),
              ),
            );
          }),
        ],
      ),
    );
  }
}