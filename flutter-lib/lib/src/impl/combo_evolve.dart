import 'package:flutter/material.dart';
import '../gen/combo.dart';
import '../gen/event.dart';
import '../gen/swt.dart';
import '../gen/widget.dart';
import '../impl/composite_evolve.dart';
import '../styles.dart';
import 'color_utils.dart';
import 'widget_config.dart';

class ComboImpl<T extends ComboSwt, V extends VCombo>
    extends CompositeImpl<T, V> {
  late TextEditingController _controller;
  FocusNode? _focusNode;

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
      if (state.textLimit != null) {
        _controller.value = _controller.value.copyWith(
          text: _controller.text,
          selection: TextSelection.collapsed(offset: _controller.text.length),
          composing: TextRange.empty,
        );
      }
    }
  }

  @override
  Widget build(BuildContext context) {
    return buildCombo();
  }

  Widget buildCombo() {
    final styleBits = StyleBits(state.style);
    final bool isReadOnly = styleBits.has(SWT.READ_ONLY);
    final bool isSimple = styleBits.has(SWT.SIMPLE);
    final bool isEnabled = state.enabled ?? true;

    // READ_ONLY: no editable, solo selección desde lista
    if (isReadOnly) {
      if (isSimple) {
        // READ_ONLY + SIMPLE: lista visible, no editable
        return StyledSimpleCombo(
          controller: _controller,
          focusNode: _focusNode,
          items: state.items ?? [],
          value: state.text,
          onChanged: isEnabled ? onChanged : null,
          onTextChanged: null, // No text changes allowed in READ_ONLY
          onTextSubmitted: null, // No text submission in READ_ONLY
          textLimit: state.textLimit,
          readOnly: true,
          onMouseEnter: handleMouseEnter,
          onMouseExit: handleMouseExit,
          onFocusIn: handleFocusIn,
          onFocusOut: handleFocusOut,
        );
      } else {
        // READ_ONLY + DROP_DOWN (o solo READ_ONLY por defecto): dropdown, no editable
        return StyledDropdownButton(
          items: state.items ?? [],
          value: state.text,
          onChanged: isEnabled ? onChanged : null,
          onMouseEnter: handleMouseEnter,
          onMouseExit: handleMouseExit,
          onFocusIn: handleFocusIn,
          onFocusOut: handleFocusOut,
        );
      }
    } else {
      // Editable combos
      if (isSimple) {
        // SIMPLE: lista visible, editable
        return StyledSimpleCombo(
          controller: _controller,
          focusNode: _focusNode,
          items: state.items ?? [],
          value: state.text,
          onChanged: isEnabled ? onChanged : null,
          onTextChanged: onTextChanged,
          onTextSubmitted: onTextSubmitted,
          textLimit: state.textLimit,
          readOnly: false,
          onMouseEnter: handleMouseEnter,
          onMouseExit: handleMouseExit,
          onFocusIn: handleFocusIn,
          onFocusOut: handleFocusOut,
        );
      } else {
        // DROP_DOWN (o por defecto): dropdown, editable
        return StyledEditableCombo(
          controller: _controller,
          focusNode: _focusNode,
          items: state.items ?? [],
          value: state.text,
          onChanged: isEnabled ? onChanged : null,
          onTextChanged: onTextChanged,
          onTextSubmitted: onTextSubmitted,
          textLimit: state.textLimit,
          onMouseEnter: handleMouseEnter,
          onMouseExit: handleMouseExit,
          onFocusIn: handleFocusIn,
          onFocusOut: handleFocusOut,
        );
      }
    }
  }

  void onChanged(String? value) {
    setState(() {
      state.text = value;
      _controller.text = value ?? "";
    });
    var e = VEvent()..text = value;
    widget.sendSelectionSelection(state, e);
  }

  void onTextChanged(String value) {
    state.text = value;
    var e = VEvent()..text = value;
    widget.sendModifyModify(state, e);
  }

  void onTextSubmitted(String text) {
    var e = VEvent()..text = text;
    widget.sendSelectionDefaultSelection(state, e);
    widget.sendVerifyVerify(state, e);
  }

  void handleMouseEnter() {
    widget.sendMouseTrackMouseEnter(state, null);
  }

  void handleMouseExit() {
    widget.sendMouseTrackMouseExit(state, null);
  }

  void handleFocusIn() {
    widget.sendFocusFocusIn(state, null);
  }

  void handleFocusOut() {
    widget.sendFocusFocusOut(state, null);
  }

  void _handleFocusChange() {
    if (_focusNode!.hasFocus) {
      handleFocusIn();
    } else {
      handleFocusOut();
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

// Styled Combo Widgets

class StyledDropdownButton extends StatelessWidget {
  final List<String> items;
  final String? value;
  final ValueChanged<String?>? onChanged;
  final VoidCallback? onMouseEnter;
  final VoidCallback? onMouseExit;
  final VoidCallback? onFocusIn;
  final VoidCallback? onFocusOut;

  const StyledDropdownButton({
    Key? key,
    required this.items,
    this.value,
    this.onChanged,
    this.onMouseEnter,
    this.onMouseExit,
    this.onFocusIn,
    this.onFocusOut,
  }) : super(key: key);

  @override
  Widget build(BuildContext context) {
    final Color backgroundColor = getBackground();
    final Color textColor = getForeground();
    final Color borderColor = getBorderColor();
    final Color iconColor = getIconColor();

    final String? dropdownValue = (value != null && items.isNotEmpty && items.contains(value)) ? value : null;

    if (items.isEmpty) {
      return Container(
        height: 32,
        decoration: BoxDecoration(
          color: backgroundColor,
          borderRadius: BorderRadius.circular(4),
          border: Border.all(color: borderColor),
        ),
        alignment: Alignment.centerLeft,
        padding: const EdgeInsets.symmetric(horizontal: 12),
        child: Text(value ?? '', style: TextStyle(color: textColor, fontSize: 12)),
      );
    }

    return MouseRegion(
      onEnter: (_) => onMouseEnter?.call(),
      onExit: (_) => onMouseExit?.call(),
      child: Focus(
        onFocusChange: (hasFocus) {
          if (hasFocus) {
            onFocusIn?.call();
          } else {
            onFocusOut?.call();
          }
        },
        child: Material(
          color: Colors.transparent,
          child: Container(
            height: 32,
            decoration: BoxDecoration(
              color: backgroundColor,
              borderRadius: BorderRadius.circular(4),
              border: Border.all(color: borderColor),
            ),
            padding: const EdgeInsets.symmetric(horizontal: 8),
            child: DropdownButtonHideUnderline(
              child: DropdownButton<String>(
                value: dropdownValue,
                isExpanded: true,
                items: items.map((String item) {
                  final bool isSelected = item == value;
                  return DropdownMenuItem<String>(
                    value: item,
                    child: ColoredBox(
                      color: isSelected ? getBackgroundSelected() : Colors.transparent,
                      child: Container(
                        padding: EdgeInsets.symmetric(horizontal: 8, vertical: 4),
                        child: Text(item, style: TextStyle(color: textColor, fontSize: 12)),
                      ),
                    ),
                  );
                }).toList(),
                isDense: true,
                itemHeight: 32,
                onChanged: onChanged,
                style: TextStyle(color: textColor, fontSize: 12),
                dropdownColor: backgroundColor,
                icon: Icon(Icons.arrow_drop_down, color: iconColor, size: 20),
              ),
            ),
          ),
        ),
      ),
    );
  }
}

class StyledSimpleCombo extends StatelessWidget {
  final TextEditingController controller;
  final FocusNode? focusNode;
  final List<String> items;
  final String? value;
  final ValueChanged<String?>? onChanged;
  final ValueChanged<String>? onTextChanged;
  final ValueChanged<String>? onTextSubmitted;
  final int? textLimit;
  final bool readOnly;
  final VoidCallback? onMouseEnter;
  final VoidCallback? onMouseExit;
  final VoidCallback? onFocusIn;
  final VoidCallback? onFocusOut;

  const StyledSimpleCombo({
    Key? key,
    required this.controller,
    this.focusNode,
    required this.items,
    this.value,
    this.onChanged,
    this.onTextChanged,
    this.onTextSubmitted,
    this.textLimit,
    this.readOnly = false,
    this.onMouseEnter,
    this.onMouseExit,
    this.onFocusIn,
    this.onFocusOut,
  }) : super(key: key);

  @override
  Widget build(BuildContext context) {
    final Color backgroundColor = getBackground();
    final Color textColor = getForeground();
    final Color borderColor = getBorderColor();

    // SIMPLE combo: TextField arriba, lista visible permanentemente abajo
    return MouseRegion(
      onEnter: (_) => onMouseEnter?.call(),
      onExit: (_) => onMouseExit?.call(),
      child: Material(
        color: Colors.transparent,
        child: IntrinsicHeight(
          child: Container(
            decoration: BoxDecoration(
              color: backgroundColor,
              borderRadius: BorderRadius.circular(4),
              border: Border.all(color: borderColor),
            ),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.stretch,
              children: [
                // TextField editable
                SizedBox(
                  height: 32,
                  child: Padding(
                    padding: EdgeInsets.symmetric(horizontal: 12, vertical: 6),
                    child: TextField(
                      controller: controller,
                      focusNode: focusNode,
                      readOnly: readOnly,
                      style: TextStyle(color: textColor, fontSize: 12),
                      decoration: InputDecoration.collapsed(
                        hintText: '',
                      ),
                      maxLines: 1,
                      maxLength: textLimit,
                      onChanged: readOnly ? null : onTextChanged,
                      onSubmitted: readOnly ? null : onTextSubmitted,
                    ),
                  ),
                ),
                // Separador
                if (items.isNotEmpty)
                  Divider(height: 1, thickness: 1, color: borderColor),
                // Lista visible permanentemente
                if (items.isNotEmpty)
                  ...items.asMap().entries.map((entry) {
                    final index = entry.key;
                    final item = entry.value;
                    final bool isSelected = item == value;
                    return InkWell(
                      onTap: () => onChanged?.call(item),
                      child: Container(
                        height: 32,
                        padding: EdgeInsets.symmetric(horizontal: 12, vertical: 8),
                        color: isSelected ? getBackgroundSelected() : Colors.transparent,
                        alignment: Alignment.centerLeft,
                        child: Text(
                          item,
                          style: TextStyle(color: textColor, fontSize: 12),
                        ),
                      ),
                    );
                  }).toList(),
              ],
            ),
          ),
        ),
      ),
    );
  }
}

class StyledEditableCombo extends StatelessWidget {
  final TextEditingController controller;
  final FocusNode? focusNode;
  final List<String> items;
  final String? value;
  final ValueChanged<String?>? onChanged;
  final ValueChanged<String>? onTextChanged;
  final ValueChanged<String>? onTextSubmitted;
  final int? textLimit;
  final VoidCallback? onMouseEnter;
  final VoidCallback? onMouseExit;
  final VoidCallback? onFocusIn;
  final VoidCallback? onFocusOut;

  const StyledEditableCombo({
    Key? key,
    required this.controller,
    this.focusNode,
    required this.items,
    this.value,
    this.onChanged,
    this.onTextChanged,
    this.onTextSubmitted,
    this.textLimit,
    this.onMouseEnter,
    this.onMouseExit,
    this.onFocusIn,
    this.onFocusOut,
  }) : super(key: key);

  @override
  Widget build(BuildContext context) {
    final Color backgroundColor = getBackground();
    final Color textColor = getForeground();
    final Color borderColor = getBorderColor();
    final Color iconColor = getIconColor();

    // Only use value if it exists in items, otherwise use null
    final String? dropdownValue = (value != null && items.isNotEmpty && items.contains(value)) ? value : null;

    return MouseRegion(
      onEnter: (_) => onMouseEnter?.call(),
      onExit: (_) => onMouseExit?.call(),
      child: Focus(
        onFocusChange: (hasFocus) {
          if (hasFocus) {
            onFocusIn?.call();
          } else {
            onFocusOut?.call();
          }
        },
        child: Material(
          color: Colors.transparent,
          child: Container(
            height: 32,
            decoration: BoxDecoration(
              color: backgroundColor,
              borderRadius: BorderRadius.circular(4),
              border: Border.all(color: borderColor),
            ),
            child: Row(
              children: [
                Expanded(
                  child: TextField(
                    controller: controller,
                    focusNode: focusNode,
                    style: TextStyle(color: textColor, fontSize: 12),
                    decoration: InputDecoration(
                      border: InputBorder.none,
                      contentPadding: EdgeInsets.symmetric(horizontal: 12, vertical: 8),
                      isDense: true,
                      counterText: '',
                    ),
                    maxLength: textLimit,
                    onChanged: onTextChanged,
                    onSubmitted: onTextSubmitted,
                  ),
                ),
                if (items.isNotEmpty)
                  DropdownButtonHideUnderline(
                    child: DropdownButton<String>(
                      value: dropdownValue,
                      items: items.map((String item) {
                        final bool isSelected = item == value;
                        return DropdownMenuItem<String>(
                          value: item,
                          child: ColoredBox(
                            color: isSelected ? getBackgroundSelected() : Colors.transparent,
                            child: Container(
                              padding: EdgeInsets.symmetric(horizontal: 12, vertical: 4),
                              child: Text(item, style: TextStyle(color: textColor, fontSize: 12)),
                            ),
                          ),
                        );
                      }).toList(),
                      isDense: true,
                      itemHeight: 32,
                      onChanged: onChanged,
                      style: TextStyle(color: textColor, fontSize: 12),
                      dropdownColor: backgroundColor,
                      icon: Padding(
                        padding: const EdgeInsets.only(right: 8),
                        child: Icon(Icons.arrow_drop_down, color: iconColor, size: 20),
                      ),
                    ),
                  ),
              ],
            ),
          ),
        ),
      ),
    );
  }
}
