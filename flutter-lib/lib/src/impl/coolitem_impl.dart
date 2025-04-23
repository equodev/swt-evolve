import 'package:fluent_ui/fluent_ui.dart';
import 'package:flutter/widgets.dart';
import 'package:swtflutter/src/styles.dart';
import 'package:swtflutter/src/swt/button.dart';
import 'package:swtflutter/src/swt/swt.dart';

import '../swt/coolitem.dart';
import '../swt/widget.dart';

import '../impl/item_impl.dart';

class CoolItemImpl<T extends CoolItemSwt, V extends CoolItemValue>
    extends ItemImpl<T, V> {
  @override
  Widget build(BuildContext context) {
    var enabled = state.enabled ?? true;
    var text = state.children!.whereType<ButtonValue>().first.text ?? "";
    return switch (StyleBits(state.style)) {
      SWT.CHECK => ToggleButton(
          checked: state.selection ?? false,
          // style: ToggleButtonThemeData(
          //     uncheckedButtonStyle: ButtonStyle(
          //         padding: ButtonState.all(EdgeInsets.zero),
          //         shape: ButtonState.all(RoundedRectangleBorder(
          //           side: BorderSide.none,
          //           borderRadius: BorderRadius.circular(0),
          //         ))),
          //     checkedButtonStyle: ButtonStyle(
          //         padding: ButtonState.all(EdgeInsets.zero),
          //         shape: ButtonState.all(RoundedRectangleBorder(
          //           side: BorderSide.none,
          //           borderRadius: BorderRadius.circular(0),
          //         )))),
          onChanged: !enabled
              ? null
              : (checked) {
                  // onPressed();
                  setState(() => state.selection = checked);
                },
          child: Text(text),
        ),
      SWT.RADIO => ToggleButton(
          checked: state.selection ?? false,
          onChanged: !enabled
              ? null
              : (checked) {
                  // onPressed();
                  setState(() => state.selection = checked);
                },
          child: Text(text),
        ),
      SWT.DROP_DOWN => SplitButton(
          flyout: const MenuFlyout(),
          child: Text(text),
        ),
      _ => Button(
          child: Text(text),
          onPressed: () {
            print("click onPressed Button $text");
          },
        )
    };
  }
}
