import 'package:fluent_ui/fluent_ui.dart';
import '../swt/swt.dart';
import '../swt/button.dart';
import '../impl/control_impl.dart';
import 'styled_buttons.dart';


class ButtonImpl<T extends ButtonSwt<V>, V extends ButtonValue>
    extends ControlImpl<T, V> {
  final bool useDarkTheme;

  ButtonImpl({this.useDarkTheme = false});

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
      ),
      SWT.CHECK => Checkbox(
        checked: state.selection ?? false,
        onChanged: !enabled
            ? null
            : (checked) {
          onPressed();
          setState(() => state.selection = checked);
        },
        content: Text(state.text ?? ""),
      ),
      SWT.RADIO => RadioButton(
        checked: state.selection ?? false,
        onChanged: !enabled
            ? null
            : (checked) {
          onPressed();
          setState(() => state.selection = checked);
        },
        content: Text(state.text ?? ""),
      ),
      SWT.NONE => RadioButton(
        checked: state.selection ?? false,
        onChanged: !enabled
            ? null
            : (checked) {
          onPressed();
          setState(() => state.selection = checked);
        },
        content: Text(state.text ?? ""),
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
        enabled: enabled,
        useDarkTheme: useDarkTheme,
        onPressed: () {
          onPressed();
          setState(() => state.selection = !(state.selection ?? false));
        },
      ),
      _ => Button(
        onPressed: enabled ? onPressed : null,
        child: Text(state.text ?? ""),
      )
    };
  }



//    return ElevatedButton(onPressed: onPressed, child: Text(state.text ?? ""));
//     if (state.style & SWT.CHECK == 0)
//     return Button(
//       onPressed: state.enabled ? onPressed : null,
//       child: Text(state.text ?? ""),
//     );
//     bool checked = false;
  // Checkbox(
  //   checked: checked,
  //   onPressed: state.enabled ? null : (v) => setState(() => checked = v),
  // )


  void onPressed() {
    widget.sendSelectionSelection(state, null);
  }

}