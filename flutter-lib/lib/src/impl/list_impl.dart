import 'package:flutter/material.dart';
import '../swt/swt.dart';
import '../swt/list.dart';
import '../impl/scrollable_impl.dart';
import 'widget_config.dart';
import 'package:swtflutter/src/styles.dart';
import 'package:swtflutter/src/impl/widget_config.dart';
import 'styledlist.dart';

class ListImpl<T extends ListSwt, V extends ListValue>
    extends ScrollableImpl<T, V> {
  final bool useDarkTheme = getCurrentTheme();

  @override
  Widget build(BuildContext context) {
    state.selection ??= <String>[];
    state.items ??= <String>[];

    return wrap(
      StyledList(
        items: state.items!,
        selection: state.selection!,
        enabled: state.enabled ?? true,
        useDarkTheme: useDarkTheme,
        isMultiSelect: state.style.has(SWT.MULTI),
        onSelectionChanged: (selectedItems) {
          setState(() {
            state.selection = selectedItems;
          });
          widget.sendSelectionSelection(state, null);
        },
        onItemDoubleClick: (item) {
          widget.sendSelectionDefaultSelection(state, null);
        },
        onMouseEnter: () => widget.sendMouseTrackMouseEnter(state, null),
        onMouseExit: () => widget.sendMouseTrackMouseExit(state, null),
        onFocusIn: () => widget.sendFocusFocusIn(state, null),
        onFocusOut: () => widget.sendFocusFocusOut(state, null),
      ),
    );
  }
}