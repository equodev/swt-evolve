import 'package:fluent_ui/fluent_ui.dart';
import '../swt/swt.dart';
import '../swt/button.dart';
import '../impl/control_impl.dart';
import 'selectable_buttton.dart';


class ButtonImpl<T extends ButtonSwt<V>, V extends ButtonValue>
    extends ControlImpl<T, V> {
  final bool useDarkTheme;

  ButtonImpl({this.useDarkTheme = false});

  @override
  Widget build(BuildContext context) {
    var bits = SWT.ARROW | SWT.TOGGLE | SWT.CHECK | SWT.RADIO | SWT.PUSH | SWT.DROP_DOWN;
    var enabled = state.enabled ?? true;

    return switch (state.style & bits) {
    // SWT.ARROW => ,
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