import 'package:fluent_ui/fluent_ui.dart';

import '../impl/scrollable_impl.dart';
import '../styles.dart';
import '../swt/list.dart';
import '../swt/swt.dart';

class ListImpl<T extends ListSwt, V extends ListValue>
    extends ScrollableImpl<T, V> {
  @override
  Widget build(BuildContext context) {
    state.selection ??= <String>[];

    final list = ListView.builder(
      shrinkWrap: true,
      itemBuilder: itemBuilder,
      itemExtent: 24,
      itemCount: state.items != null ? state.items!.length : 0,
    );

    return wrap(list);
  }

  Widget? itemBuilder(BuildContext context, int index) {
    Typography typography = FluentTheme.of(context).typography;
    final item = state.items![index];
    return ListTile.selectable(
      contentPadding: EdgeInsets.zero,
      selectionMode: state.enabled ?? true
          ? ListTileSelectionMode.single
          : ListTileSelectionMode.none,
      selected: state.selection!.contains(item),
      onPressed: () => setState(() {
        final selected = state.selection!.contains(item);
        if (!StyleBits(state.style).has(SWT.MULTI)) {
          state.selection!.clear();
        }
        if (selected) {
          state.selection!.remove(item);
        } else {
          state.selection!.add(item);
        }
      }),
      title: Text(
        item,
        style: typography.body,
      ),
    );
  }
}
