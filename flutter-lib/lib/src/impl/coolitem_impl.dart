import 'package:fluent_ui/fluent_ui.dart';
import 'package:flutter/material.dart';
import 'package:flutter/widgets.dart';
import 'package:swtflutter/src/swt/button.dart';
import 'package:swtflutter/src/swt/swt.dart';
import '../swt/coolitem.dart';
import '../impl/item_impl.dart';
import 'dropdown_button.dart';
import 'selectable_buttton.dart';


class CoolItemImpl<T extends CoolItemSwt, V extends CoolItemValue>
    extends ItemImpl<T, V> {

  final bool useDarkTheme;

  CoolItemImpl({this.useDarkTheme = true});

  @override
  Widget build(BuildContext context) {
    var enabled = state.enabled ?? true;
    var text = state.children!.whereType<ButtonValue>().first.text ?? "";
    var image = state.children!.whereType<ButtonValue>().first.image;
    var buttonStyle = state.children!.whereType<ButtonValue>().first.style ?? SWT.NONE;
    var bits = SWT.ARROW | SWT.TOGGLE | SWT.CHECK | SWT.RADIO | SWT.PUSH | SWT.DROP_DOWN ;
    print('button image $image');

    return switch (buttonStyle & bits) {
      SWT.TOGGLE => SelectableButton(
        text: text,
        image: image,
        height: 50.0,
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
      _ => Button(
        child: Text(text),
        onPressed: () {
          print("click onPressed Button $text");
        },
      )
    };
  }

  void onPressed() {
    widget.sendSelectionSelection(state, null);
  }
}
