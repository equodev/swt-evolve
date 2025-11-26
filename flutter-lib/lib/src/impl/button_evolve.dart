import 'package:flutter/material.dart';
import '../gen/button.dart';
import '../gen/swt.dart';
import '../gen/widget.dart';
import '../impl/control_evolve.dart';
import '../styles.dart';
import 'styled_buttons.dart';
import 'widget_config.dart';
import 'utils/text_utils.dart';
import 'utils/font_utils.dart';

class ButtonImpl<T extends ButtonSwt, V extends VButton>
    extends ControlImpl<T, V> {
  final bool useDarkTheme = getCurrentTheme();

  @override
  Widget build(BuildContext context) {
    var text = state.text;
    String? image;
    var enabled = state.enabled ?? true;
    var backgroundColor = getSwtBackgroundColor(context);

    if (state.style.has(SWT.TOGGLE)) {
      return wrap(SelectableButton(
        text: stripAccelerators(state.text),
        isSelected: state.selection ?? false,
        enabled: enabled,
        useDarkTheme: useDarkTheme,
        backgroundColor: backgroundColor,
        vFont: state.font,
        textColor: state.foreground,
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
      ));
    } else if (state.style.has(SWT.CHECK)) {
      return wrap(MaterialCheckBox(
        text: stripAccelerators(state.text),
        checked: state.selection ?? false,
        useDarkTheme: useDarkTheme,
        backgroundColor: backgroundColor,
        vFont: state.font,
        textColor: state.foreground,
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
      ));
    } else if (state.style.has(SWT.RADIO) || state.style.has(SWT.NONE)) {
      return wrap(MaterialRadioButton(
        text: stripAccelerators(state.text),
        checked: state.selection ?? false,
        useDarkTheme: useDarkTheme,
        backgroundColor: backgroundColor,
        vFont: state.font,
        textColor: state.foreground,
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
      ));
    } else if (state.style.has(SWT.DROP_DOWN)) {
      return wrap(MaterialDropdownButton(
        text: stripAccelerators(text) ?? "",
        height: 50.0,
        enabled: enabled,
        useDarkTheme: useDarkTheme,
        vFont: state.font,
        textColor: state.foreground,
        onPressed: () {
          if (enabled) {
            onPressed();
          }
        },
      ));
    } else if (state.style.has(SWT.PUSH)) {
      return wrap(PushButton(
        text: stripAccelerators(state.text),
        image: image,
        enabled: enabled,
        useDarkTheme: useDarkTheme,
        vFont: state.font,
        textColor: state.foreground,
        onPressed: () {
          onPressed();
          setState(() => state.selection = !(state.selection ?? false));
        },
        onMouseEnter: () => handleMouseEnter(),
        onMouseExit: () => handleMouseExit(),
        onFocusIn: () => handleFocusIn(),
        onFocusOut: () => handleFocusOut(),
      ));
    } else {
      return wrap(Material(
        child: ElevatedButton(
          onPressed: enabled ? onPressed : null,
          child: Text(stripAccelerators(state.text) ?? ""),
        ),
      ));
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
