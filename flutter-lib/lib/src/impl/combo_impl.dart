import 'package:flutter/material.dart';
import '../impl/composite_impl.dart';
import '../styles.dart';
import '../swt/combo.dart';
import '../swt/swt.dart';
import 'package:swtflutter/src/impl/widget_config.dart';
import 'styleddropdownbutton.dart';

class ComboImpl<T extends ComboSwt, V extends ComboValue>
    extends CompositeImpl<T, V> {
  final bool useDarkTheme = getCurrentTheme();

  @override
  Widget build(BuildContext context) {
    return super.wrap(buildCombo());
  }

  Widget buildCombo() {
    final styleBits = StyleBits(state.style);

    if (styleBits.has(SWT.READ_ONLY)) {
      return StyledDropdownButton(
        items: state.items ?? [],
        value: state.text,
        onChanged: state.enabled ?? true ? onChanged : null,
        useDarkTheme: useDarkTheme,
        onMouseEnter: () => handleMouseEnter(),
        onMouseExit: () => handleMouseExit(),
        onFocusIn: () => handleFocusIn(),
        onFocusOut: () => handleFocusOut(),
      );
    } else if (styleBits.has(SWT.SIMPLE)) {
      return StyledSimpleCombo(
        items: state.items ?? [],
        value: state.text,
        onChanged: state.enabled ?? true ? onChanged : null,
        onTextChanged: onTextChanged,
        onTextSubmitted: onTextSubmitted,
        useDarkTheme: useDarkTheme,
        onMouseEnter: () => handleMouseEnter(),
        onMouseExit: () => handleMouseExit(),
        onFocusIn: () => handleFocusIn(),
        onFocusOut: () => handleFocusOut(),
      );
    } else {
      // Default DROP_DOWN style
      return StyledEditableCombo(
        items: state.items ?? [],
        value: state.text,
        onChanged: state.enabled ?? true ? onChanged : null,
        onTextChanged: onTextChanged,
        onTextSubmitted: onTextSubmitted,
        useDarkTheme: useDarkTheme,
        onMouseEnter: () => handleMouseEnter(),
        onMouseExit: () => handleMouseExit(),
        onFocusIn: () => handleFocusIn(),
        onFocusOut: () => handleFocusOut(),
      );
    }
  }

  void onChanged(String? value) {
    widget.sendSelectionSelection(state, value);
    setState(() {
      state.text = value;
      state.selectionIndex = state.items?.indexOf(value!);
    });
  }

  void onTextChanged(String value) {
    widget.sendModifyModify(state, null);
  }

  String onTextSubmitted(String text) {
    widget.sendVerifyVerify(state, null);
    return text;
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
}