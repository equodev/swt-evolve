import 'package:flutter/material.dart';
import '../gen/ccombo.dart';
import '../gen/event.dart';
import '../gen/swt.dart';
import '../gen/widget.dart';
import '../impl/composite_evolve.dart';
import '../styles.dart';
import 'color_utils.dart';
import 'widget_config.dart';

// Helper functions to get colors with custom color support
Color getCComboBackgroundColor(BuildContext context) {
  final parentState = context.findAncestorStateOfType<CComboImpl>();
  if (parentState != null) {
    return colorFromVColor(parentState.state.background, defaultColor: getBackground());
  }
  return getBackground();
}

Color getCComboForegroundColor(BuildContext context) {
  final parentState = context.findAncestorStateOfType<CComboImpl>();
  if (parentState != null) {
    return colorFromVColor(parentState.state.foreground, defaultColor: getForeground());
  }
  return getForeground();
}

class CComboImpl<T extends CComboSwt, V extends VCCombo>
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
    return IntrinsicWidth(
        child: buildCCombo(),
      );
  }

  Widget buildCCombo() {
    final styleBits = StyleBits(state.style);
    final bool isReadOnly = styleBits.has(SWT.READ_ONLY);
    final bool isSimple = styleBits.has(SWT.SIMPLE);
    final bool isEnabled = state.enabled ?? true;
    final bool listVisible = state.listVisible ?? false;

    // CCombo behavior: in READ_ONLY mode, acts like a dropdown selector
    // In editable mode, allows free text input with optional list selection
    if (isReadOnly) {
      if (isSimple || listVisible) {
        // READ_ONLY + SIMPLE or list visible: show list always
        return StyledSimpleCCombo(
          controller: _controller,
          focusNode: _focusNode,
          items: state.items ?? [],
          value: state.text,
          onChanged: isEnabled ? onChanged : null,
          onTextChanged: null, // No text changes in READ_ONLY
          onTextSubmitted: null,
          textLimit: state.textLimit,
          readOnly: true,
          alignment: state.alignment,
          onMouseEnter: handleMouseEnter,
          onMouseExit: handleMouseExit,
          onFocusIn: handleFocusIn,
          onFocusOut: handleFocusOut,
        );
      } else {
        // READ_ONLY + DROP_DOWN: standard dropdown
        return StyledCComboDropdown(
          items: state.items ?? [],
          value: state.text,
          onChanged: isEnabled ? onChanged : null,
          alignment: state.alignment,
          listVisible: listVisible,
          style: state.style,
          onMouseEnter: handleMouseEnter,
          onMouseExit: handleMouseExit,
          onFocusIn: handleFocusIn,
          onFocusOut: handleFocusOut,
        );
      }
    } else {
      // Editable CCombo modes
      if (isSimple || listVisible) {
        // Editable with visible list
        return StyledSimpleCCombo(
          controller: _controller,
          focusNode: _focusNode,
          items: state.items ?? [],
          value: state.text,
          onChanged: isEnabled ? onChanged : null,
          onTextChanged: onTextChanged,
          onTextSubmitted: onTextSubmitted,
          textLimit: state.textLimit,
          readOnly: false,
          alignment: state.alignment,
          onMouseEnter: handleMouseEnter,
          onMouseExit: handleMouseExit,
          onFocusIn: handleFocusIn,
          onFocusOut: handleFocusOut,
        );
      } else {
        // Editable dropdown (default)
        return StyledEditableCCombo(
          controller: _controller,
          focusNode: _focusNode,
          items: state.items ?? [],
          value: state.text,
          onChanged: isEnabled ? onChanged : null,
          onTextChanged: onTextChanged,
          onTextSubmitted: onTextSubmitted,
          textLimit: state.textLimit,
          alignment: state.alignment,
          listVisible: listVisible,
          enabled: isEnabled,
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

// Styled CCombo Widgets

/// Dropdown for READ_ONLY CCombo
class StyledCComboDropdown extends StatelessWidget {
  final List<String> items;
  final String? value;
  final ValueChanged<String?>? onChanged;
  final int? alignment;
  final bool listVisible;
  final int? style;
  final VoidCallback? onMouseEnter;
  final VoidCallback? onMouseExit;
  final VoidCallback? onFocusIn;
  final VoidCallback? onFocusOut;

  const StyledCComboDropdown({
    Key? key,
    required this.items,
    this.value,
    this.onChanged,
    this.alignment,
    this.listVisible = false,
    this.style,
    this.onMouseEnter,
    this.onMouseExit,
    this.onFocusIn,
    this.onFocusOut,
  }) : super(key: key);

  BoxBorder? _getBorder() {
    final styleBits = StyleBits(style ?? 0);
    if (!styleBits.has(SWT.BORDER) && !styleBits.has(SWT.FLAT)) {
      return null;
    }

    final Color visibleBorderColor = getCurrentTheme()
        ? const Color(0xFF808080)
        : const Color(0xFF404040);

    if (styleBits.has(SWT.FLAT)) {
      return Border.all(
        color: visibleBorderColor.withOpacity(0.7),
        width: 1.5
      );
    }

    return Border.all(color: visibleBorderColor, width: 3.0);
  }

  @override
  Widget build(BuildContext context) {
    final Color backgroundColor = getCComboBackgroundColor(context);
    final Color textColor = getCComboForegroundColor(context);
    final Color iconColor = getIconColor();

    final String? dropdownValue =
        (value != null && items.isNotEmpty && items.contains(value))
            ? value
            : null;

    final textAlign = _getTextAlign();

    if (items.isEmpty) {
      return Container(
        height: 32,
        decoration: BoxDecoration(
          color: backgroundColor,
          borderRadius: BorderRadius.circular(4),
          border: _getBorder(),
        ),
        alignment: _getAlignment(),
        padding: const EdgeInsets.symmetric(horizontal: 12),
        child: Text(value ?? '',
            textAlign: textAlign,
            style: TextStyle(color: textColor, fontSize: 12)),
      );
    }

    return IntrinsicWidth(
      child: MouseRegion(
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
                border: _getBorder(),
              ),
              padding: const EdgeInsets.symmetric(horizontal: 8),
              child: DropdownButtonHideUnderline(
                child: DropdownButton<String>(
                  value: dropdownValue,
                  isExpanded: false,
                  items: items.map((String item) {
                    final bool isSelected = item == value;
                    return DropdownMenuItem<String>(
                      value: item,
                      child: ColoredBox(
                        color: isSelected
                            ? getBackgroundSelected()
                            : Colors.transparent,
                        child: Container(
                          padding:
                              EdgeInsets.symmetric(horizontal: 8, vertical: 4),
                          alignment: _getAlignment(),
                          child: Text(item,
                              textAlign: textAlign,
                              style: TextStyle(color: textColor, fontSize: 12)),
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
      ),
    );
  }

  TextAlign _getTextAlign() {
    if (alignment == SWT.CENTER) return TextAlign.center;
    if (alignment == SWT.RIGHT || alignment == SWT.TRAIL) return TextAlign.right;
    return TextAlign.left;
  }

  Alignment _getAlignment() {
    if (alignment == SWT.CENTER) return Alignment.center;
    if (alignment == SWT.RIGHT || alignment == SWT.TRAIL) {
      return Alignment.centerRight;
    }
    return Alignment.centerLeft;
  }
}

/// Simple CCombo with always visible list
class StyledSimpleCCombo extends StatelessWidget {
  final TextEditingController controller;
  final FocusNode? focusNode;
  final List<String> items;
  final String? value;
  final ValueChanged<String?>? onChanged;
  final ValueChanged<String>? onTextChanged;
  final ValueChanged<String>? onTextSubmitted;
  final int? textLimit;
  final bool readOnly;
  final int? alignment;
  final VoidCallback? onMouseEnter;
  final VoidCallback? onMouseExit;
  final VoidCallback? onFocusIn;
  final VoidCallback? onFocusOut;

  const StyledSimpleCCombo({
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
    this.alignment,
    this.onMouseEnter,
    this.onMouseExit,
    this.onFocusIn,
    this.onFocusOut,
  }) : super(key: key);

  @override
  Widget build(BuildContext context) {
    final Color backgroundColor = getCComboBackgroundColor(context);
    final Color textColor = getCComboForegroundColor(context);
    final Color borderColor = getBorderColor();

    final textAlign = _getTextAlign();

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
                // Text field at top
                SizedBox(
                  height: 32,
                  child: Padding(
                    padding: EdgeInsets.symmetric(horizontal: 12, vertical: 6),
                    child: TextField(
                      controller: controller,
                      focusNode: focusNode,
                      readOnly: readOnly,
                      textAlign: textAlign,
                      textAlignVertical: TextAlignVertical.center,
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
                // Separator
                if (items.isNotEmpty)
                  Divider(height: 1, thickness: 1, color: borderColor),
                // Visible list
                if (items.isNotEmpty)
                  ...items.asMap().entries.map((entry) {
                    final item = entry.value;
                    final bool isSelected = item == value;
                    return InkWell(
                      onTap: () => onChanged?.call(item),
                      child: Container(
                        height: 32,
                        padding:
                            EdgeInsets.symmetric(horizontal: 12, vertical: 8),
                        color: isSelected
                            ? getBackgroundSelected()
                            : Colors.transparent,
                        alignment: _getAlignment(),
                        child: Text(
                          item,
                          textAlign: textAlign,
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

  TextAlign _getTextAlign() {
    if (alignment == SWT.CENTER) return TextAlign.center;
    if (alignment == SWT.RIGHT || alignment == SWT.TRAIL) return TextAlign.right;
    return TextAlign.left;
  }

  Alignment _getAlignment() {
    if (alignment == SWT.CENTER) return Alignment.center;
    if (alignment == SWT.RIGHT || alignment == SWT.TRAIL) {
      return Alignment.centerRight;
    }
    return Alignment.centerLeft;
  }
}

/// Editable CCombo with dropdown
class StyledEditableCCombo extends StatelessWidget {
  final TextEditingController controller;
  final FocusNode? focusNode;
  final List<String> items;
  final String? value;
  final ValueChanged<String?>? onChanged;
  final ValueChanged<String>? onTextChanged;
  final ValueChanged<String>? onTextSubmitted;
  final int? textLimit;
  final int? alignment;
  final bool listVisible;
  final bool enabled;
  final VoidCallback? onMouseEnter;
  final VoidCallback? onMouseExit;
  final VoidCallback? onFocusIn;
  final VoidCallback? onFocusOut;

  const StyledEditableCCombo({
    Key? key,
    required this.controller,
    this.focusNode,
    required this.items,
    this.value,
    this.onChanged,
    this.onTextChanged,
    this.onTextSubmitted,
    this.textLimit,
    this.alignment,
    this.listVisible = false,
    this.enabled = true,
    this.onMouseEnter,
    this.onMouseExit,
    this.onFocusIn,
    this.onFocusOut,
  }) : super(key: key);

  @override
  Widget build(BuildContext context) {
    final Color backgroundColor = getCComboBackgroundColor(context);
    final Color textColor = getCComboForegroundColor(context);
    final Color borderColor = getBorderColor();
    final Color iconColor = getIconColor();

    final String? dropdownValue =
        (value != null && items.isNotEmpty && items.contains(value))
            ? value
            : null;

    final textAlign = _getTextAlign();

    return IntrinsicWidth(
      child: MouseRegion(
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
                constraints: BoxConstraints(minWidth: 120),
                decoration: BoxDecoration(
                  color: backgroundColor,
                  borderRadius: BorderRadius.circular(4),
                  border: Border.all(color: borderColor),
                ),
                child: Row(
                  mainAxisSize: MainAxisSize.min,
                  children: [
                    Flexible(
                      child: IntrinsicWidth(
                        child: TextField(
                          controller: controller,
                          focusNode: focusNode,
                          enabled: enabled,
                          textAlign: textAlign,
                          textAlignVertical: TextAlignVertical.center,
                          style: TextStyle(color: textColor, fontSize: 12),
                          decoration: InputDecoration(
                            border: InputBorder.none,
                            contentPadding:
                                EdgeInsets.symmetric(horizontal: 12, vertical: 8),
                            isDense: true,
                            counterText: '',
                          ),
                          maxLines: 1,
                          maxLength: textLimit,
                          onChanged: enabled ? onTextChanged : null,
                          onSubmitted: enabled ? onTextSubmitted : null,
                        ),
                      ),
                    ),
                    if (items.isNotEmpty)
                      SizedBox(
                        height: 32,
                        child: DropdownButtonHideUnderline(
                          child: DropdownButton<String>(
                            value: dropdownValue,
                            items: items.map((String item) {
                              final bool isSelected = item == value;
                              return DropdownMenuItem<String>(
                                value: item,
                                child: ColoredBox(
                                  color: isSelected
                                      ? getBackgroundSelected()
                                      : Colors.transparent,
                                  child: Container(
                                    padding: EdgeInsets.symmetric(
                                        horizontal: 12, vertical: 4),
                                    alignment: _getAlignment(),
                                    child: Text(item,
                                        textAlign: textAlign,
                                        style: TextStyle(
                                            color: textColor, fontSize: 12)),
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
                              child: Icon(Icons.arrow_drop_down,
                                  color: iconColor, size: 20),
                            ),
                          ),
                        ),
                      ),
                  ],
                ),
              ),
            ),
          ),
      ),
    );
  }

  TextAlign _getTextAlign() {
    if (alignment == SWT.CENTER) return TextAlign.center;
    if (alignment == SWT.RIGHT || alignment == SWT.TRAIL) return TextAlign.right;
    return TextAlign.left;
  }

  Alignment _getAlignment() {
    if (alignment == SWT.CENTER) return Alignment.center;
    if (alignment == SWT.RIGHT || alignment == SWT.TRAIL) {
      return Alignment.centerRight;
    }
    return Alignment.centerLeft;
  }
}
