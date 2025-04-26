import 'package:fluent_ui/fluent_ui.dart';
import 'package:flutter/material.dart';
import 'package:flutter/widgets.dart';
import 'package:swtflutter/src/swt/button.dart';
import 'package:swtflutter/src/swt/swt.dart';
import '../swt/coolitem.dart';
import '../impl/item_impl.dart';
import '../swt/toolbar.dart';
import 'styled_buttons.dart';

class CoolItemImpl<T extends CoolItemSwt, V extends CoolItemValue>
    extends ItemImpl<T, V> {

  final bool useDarkTheme;

  CoolItemImpl({this.useDarkTheme = true});

  @override
  Widget build(BuildContext context) {

    final isButtonValue = state.children?.isNotEmpty == true && state.children?.first is ButtonValue;
    final buttonValue = isButtonValue ? state.children!.whereType<ButtonValue>().first : null;

    final bool enabled = isButtonValue ? (state.enabled ?? true) : true;
    final String? text = isButtonValue ? buttonValue?.text : null;
    final dynamic image = isButtonValue ? buttonValue?.image : null;

    var childStyle = state.children!.first.style;

    var bits = SWT.ARROW | SWT.TOGGLE | SWT.CHECK | SWT.RADIO | SWT.PUSH | SWT.DROP_DOWN | SWT.FLAT ;

    return switch (childStyle & bits) {
      SWT.FLAT => (() {
        final toolBarValue = state.children!.whereType<ToolBarValue>().first;
        return ToolBarSwt<ToolBarValue>(value: toolBarValue);
      })(),

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
        child: Text(text!),
      ),
      SWT.RADIO => ToggleButton(
        checked: state.selection ?? false,
        onChanged: !enabled
            ? null
            : (checked) {
          // onPressed();
          setState(() => state.selection = checked);
        },
        child: Text(text!),
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
        child: Text(text!),
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
