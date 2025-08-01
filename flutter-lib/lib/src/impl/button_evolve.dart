import 'package:flutter/material.dart';
import '../gen/button.dart';
import '../gen/swt.dart';
import '../gen/widget.dart';
import '../impl/control_evolve.dart';
import '../styles.dart';
import 'styled_buttons.dart';
import 'widget_config.dart';

class ButtonImpl<T extends ButtonSwt, V extends VButton>
    extends ControlImpl<T, V> {
  final bool useDarkTheme = getCurrentTheme();

  @override
  Widget build(BuildContext context) {
    var text = state.text;
    // var image = state.image;
    String? image;
    var enabled = state.enabled?? true;

    if (state.style.has(SWT.TOGGLE)) {
      return SelectableButton(
        text: state.text,
        isSelected: state.selection ?? false,
        enabled: enabled,
        useDarkTheme: useDarkTheme,
        onPressed: () {
          if (enabled) {
            onPressed();
            setState(() => state.selection = !(state.selection ?? false));
          }
        },
        onMouseEnter: () => handleMouseEnter(),
        onMouseExit: () => handleMouseExit(),
        onFocusIn: () => handleFocusIn(),
        onFocusOut: () => handleFocusOut(),
      );
    } else if (state.style.has(SWT.CHECK)) {
      return MaterialCheckBox(
        text: state.text,
        checked: state.selection ?? false,
        useDarkTheme: useDarkTheme,
        onChanged: !enabled
            ? null
            : (checked) {
          onPressed();
          setState(() => state.selection = checked);
        },
        onMouseEnter: () => handleMouseEnter(),
        onMouseExit: () => handleMouseExit(),
        onFocusIn: () => handleFocusIn(),
        onFocusOut: () => handleFocusOut(),
      );
    } else if (state.style.has(SWT.RADIO) || state.style.has(SWT.NONE)) {
      return MaterialRadioButton(
        text: state.text,
        checked: state.selection ?? false,
        useDarkTheme: useDarkTheme,
        onChanged: !enabled
            ? null
            : (checked) {
          onPressed();
          setState(() => state.selection = checked);
        },
        onMouseEnter: () => handleMouseEnter(),
        onMouseExit: () => handleMouseExit(),
        onFocusIn: () => handleFocusIn(),
        onFocusOut: () => handleFocusOut(),
      );
    } else if (state.style.has(SWT.DROP_DOWN)) {
      return MaterialDropdownButton(
        text: text ?? "",
        height: 50.0,
        enabled: enabled,
        useDarkTheme: useDarkTheme,
        onPressed: () {
          if (enabled) {
            onPressed();
          }
        },
      );
    } else if (state.style.has(SWT.PUSH)) {
      return PushButton(
        text: state.text,
        image: image,
        enabled: enabled,
        useDarkTheme: useDarkTheme,
        onPressed: () {
          onPressed();
          setState(() => state.selection = !(state.selection ?? false));
        },
        onMouseEnter: () => handleMouseEnter(),
        onMouseExit: () => handleMouseExit(),
        onFocusIn: () => handleFocusIn(),
        onFocusOut: () => handleFocusOut(),
      );
    } else {
      // Default case
      return ElevatedButton(
        onPressed: enabled ? onPressed : null,
        child: Text(state.text ?? ""),
      );
    }
  }

  void onPressed() {
    widget.sendSelectionSelection(state, null);
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
