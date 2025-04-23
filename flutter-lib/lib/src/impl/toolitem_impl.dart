import 'package:fluent_ui/fluent_ui.dart';

import '../impl/item_impl.dart';
import '../styles.dart';
import '../swt/swt.dart';
import '../swt/toolitem.dart';

class ToolItemImpl<T extends ToolItemSwt, V extends ToolItemValue>
    extends ItemImpl<T, V> {
  @override
  Widget build(BuildContext context) {
    var enabled = state.enabled ?? true;
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
          child: Text(state.text ?? ""),
        ),
      SWT.RADIO => ToggleButton(
          checked: state.selection ?? false,
          onChanged: !enabled
              ? null
              : (checked) {
                  // onPressed();
                  setState(() => state.selection = checked);
                },
          child: Text(state.text ?? ""),
        ),
      SWT.DROP_DOWN => SplitButton(
          flyout: const MenuFlyout(),
          child: Text(state.text ?? ""),
        ),
      _ => Text(state.text ?? "")
    };
  }
}
