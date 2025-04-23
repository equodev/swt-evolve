import 'package:fluent_ui/fluent_ui.dart';

import '../impl/composite_impl.dart';
import '../styles.dart';
import '../swt/combo.dart';
import '../swt/swt.dart';

class ComboImpl<T extends ComboSwt, V extends ComboValue>
    extends CompositeImpl<T, V> {
  @override
  Widget build(BuildContext context) {
    Widget combo = switch (StyleBits(state.style)) {
      < SWT.READ_ONLY => ComboBox<String>(
          value: state.text,
          items: builder(context),
          onChanged: state.enabled ?? true ? onChanged : null,
        ),
      _ => EditableComboBox<String>(
          autofocus: true,
          value: state.text,
          items: builder(context),
          onChanged: state.enabled ?? true ? onChanged : null,
          onTextChanged: onTextChanged,
          onFieldSubmitted: onTextSubmited,
        )
    };
    return super.wrap(combo);
  }

  void onChanged(String? value) {
    widget.sendSelectionSelection(state, value);
    setState(() {
      state.text = value;
      state.selectionIndex = state.items?.indexOf(value!);
    });
  }

  void onTextChanged(String value) {
    widget.sendModifyModify(state, null);
  }

  String onTextSubmited(String text) {
    widget.sendVerifyVerify(state, null);
    return text;
  }

  List<ComboBoxItem<String>> builder(BuildContext context) {
    if (state.items != null) {
      return state.items!
          .map((e) => ComboBoxItem(value: e, child: Text(e)))
          .toList();
    }
    return List.empty();
  }
}
