import 'package:fluent_ui/fluent_ui.dart';
import 'package:swtflutter/src/impl/separator.dart';
import '../impl/item_impl.dart';
import '../swt/swt.dart';
import '../swt/toolitem.dart';
import 'styled_buttons.dart';

class ToolItemImpl<T extends ToolItemSwt, V extends ToolItemValue>
    extends ItemImpl<T, V> {

  final bool useDarkTheme;

  ToolItemImpl({this.useDarkTheme = false});

  @override
  Widget build(BuildContext context) {

    var text = state.text;
    var image = state.image;
    var enabled = state.enabled?? false;

    var bits = SWT.PUSH | SWT.CHECK | SWT.RADIO | SWT.SEPARATOR | SWT.DROP_DOWN;

    return switch (state.style & bits) {
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
        text: text,
        image: image,
        enabled: enabled,
        useDarkTheme: useDarkTheme,
        onPressed: () {
          onPressed();
          setState(() => state.selection = !(state.selection ?? false));
        },
      ),
    //todo manejar los size
      SWT.SEPARATOR => MaterialSeparator(
        height: 10,        // Alto personalizado
        width: 10,          // Ancho total (incluyendo márgenes)
        thickness: 1.5,     // Grosor de la línea
        color: Colors.grey, // Color personalizado
      ),
      _ => Text(state.text ?? "")
    };
  }

  void onPressed() {
    widget.sendSelectionSelection(state, null);
  }
}
