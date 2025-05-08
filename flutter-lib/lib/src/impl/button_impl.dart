import 'package:fluent_ui/fluent_ui.dart';
import '../swt/swt.dart';
import '../swt/button.dart';
import '../impl/control_impl.dart';
import 'styled_buttons.dart';
import 'widget_config.dart';


class ButtonImpl<T extends ButtonSwt<V>, V extends ButtonValue>
    extends ControlImpl<T, V> {
  final bool useDarkTheme = getCurrentTheme();

  @override
  Widget build(BuildContext context) {

    var bits = SWT.ARROW | SWT.TOGGLE | SWT.CHECK | SWT.RADIO | SWT.PUSH | SWT.DROP_DOWN;

    var text = state.text;
    var image = state.image;
    var enabled = state.enabled?? false;

    return switch (state.style & bits) {
      SWT.TOGGLE => SelectableButton(
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
      ),
      SWT.CHECK => MaterialCheckBox(
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
      ),
      SWT.RADIO => MaterialRadioButton(
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
      ),
      SWT.NONE => MaterialRadioButton(
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
      ),
      SWT.DROP_DOWN => MaterialDropdownButton(
        text: text ?? "",
        height: 50.0,
        enabled: enabled,
        useDarkTheme: useDarkTheme,
        onPressed: () {
          if (enabled) {
            onPressed();
          }
        },
      ),
      SWT.PUSH => PushButton(
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
      ),
      _ => Button(
        onPressed: enabled ? onPressed : null,
        child: Text(state.text ?? ""),
      )
    };
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