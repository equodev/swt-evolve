import 'package:fluent_ui/fluent_ui.dart';
// import 'package:flutter/material.dart';

import '../swt/swt.dart';
import '../swt/button.dart';
import '../impl/control_impl.dart';

class ButtonImpl<T extends ButtonSwt<V>, V extends ButtonValue>
    extends ControlImpl<T, V> {
  @override
  Widget build(BuildContext context) {
    var bits = SWT.ARROW | SWT.TOGGLE | SWT.CHECK | SWT.RADIO | SWT.PUSH;
    var enabled = state.enabled ?? true;
    return switch (state.style & bits) {
      // SWT.ARROW => ,
      // SWT.TOGGLE => .
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
      _ => Button(
          onPressed: enabled ? onPressed : null,
          child: Text(state.text ?? ""),
        )
    };

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
  }

  void onPressed() {
    widget.sendSelectionSelection(state, null);
  }
}
