import 'package:fluent_ui/fluent_ui.dart';
import 'package:swtflutter/src/styles.dart';
import 'package:swtflutter/src/swt/coolitem.dart';
import 'package:swtflutter/src/swt/swt.dart';
import 'package:swtflutter/src/swt/toolitem.dart';
import 'package:swtflutter/src/widgets.dart';

import '../swt/coolbar.dart';

import '../impl/composite_impl.dart';

class CoolBarImpl<T extends CoolBarSwt, V extends CoolBarValue>
    extends CompositeImpl<T, V> {
  @override
  Widget build(BuildContext context) {
    final coolItems = getCoolItems();
    final style = StyleBits(state.style);
    final bar = CommandBar(
      direction: style.has(SWT.VERTICAL) ? Axis.vertical : Axis.horizontal,
      overflowBehavior: style.has(SWT.WRAP)
          ? CommandBarOverflowBehavior.dynamicOverflow
          : CommandBarOverflowBehavior.scrolling,
      primaryItems: coolItems,
      isCompact: true,
    );
    return super.wrap(bar);
  }

  List<CommandBarItem> getCoolItems() {
    if (state.children == null) {
      return [];
    }
    return state.children!
        .whereType<CoolItemValue>()
        .map((coolItem) => getWidgetForCoolItem(coolItem))
        .toList();
  }

  CommandBarItem getWidgetForCoolItem(CoolItemValue coolItem) {
    //var coolItemBits = SWT.PUSH | SWT.CHECK | SWT.RADIO | SWT.SEPARATOR | SWT.DROP_DOWN;
    final itemWidget = CoolItemSwt(value: coolItem);
    CommandBarItem item = switch (coolItem.style) {
      // < SWT.SEPARATOR => const CommandBarSeparator(),
      _ => CommandBarButton(
        // icon: Icon(FluentIcons.accept),
        // label: Text(coolItem.text ?? ""),
        label: itemWidget,
        onPressed: () => itemWidget.sendSelectionSelection(coolItem, null),
      )
    };

    if (coolItem.toolTipText != null) {
      item = CommandBarBuilderItem(
        builder: (context, mode, w) => Tooltip(
          message: coolItem.toolTipText!,
          child: w,
        ),
        wrappedItem: item,
      );
    }
    return item;
  }
}
