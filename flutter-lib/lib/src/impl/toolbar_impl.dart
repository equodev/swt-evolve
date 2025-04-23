import 'package:fluent_ui/fluent_ui.dart';
import 'package:swtflutter/src/swt/toolitem.dart';

import '../impl/composite_impl.dart';
import '../styles.dart';
import '../swt/swt.dart';
import '../swt/toolbar.dart';

class ToolBarImpl<T extends ToolBarSwt, V extends ToolBarValue>
    extends CompositeImpl<T, V> {
  @override
  Widget build(BuildContext context) {
    final toolItems = getToolItems();
    final style = StyleBits(state.style);
    final bar = CommandBar(
      direction: style.has(SWT.VERTICAL) ? Axis.vertical : Axis.horizontal,
      overflowBehavior: style.has(SWT.WRAP)
          ? CommandBarOverflowBehavior.dynamicOverflow
          : CommandBarOverflowBehavior.scrolling,
      primaryItems: toolItems,
      isCompact: true,
    );
    return super.wrap(bar);
  }

  List<CommandBarItem> getToolItems() {
    if (state.children == null) {
      return [];
    }
    return state.children!
        .whereType<ToolItemValue>()
        .map((toolItem) => getWidgetForToolItem(toolItem))
        .toList();
  }

  CommandBarItem getWidgetForToolItem(ToolItemValue toolItem) {
    //var toolItemBits = SWT.PUSH | SWT.CHECK | SWT.RADIO | SWT.SEPARATOR | SWT.DROP_DOWN;
    final itemWidget = ToolItemSwt(value: toolItem);
    CommandBarItem item = switch (toolItem.style) {
      < SWT.SEPARATOR => const CommandBarSeparator(),
      _ => CommandBarButton(
          // icon: Icon(FluentIcons.accept),
          // label: Text(toolItem.text ?? ""),
          label: itemWidget,
          onPressed: () => itemWidget.sendSelectionSelection(toolItem, null),
        )
    };

    if (toolItem.toolTipText != null) {
      item = CommandBarBuilderItem(
        builder: (context, mode, w) => Tooltip(
          message: toolItem.toolTipText!,
          child: w,
        ),
        wrappedItem: item,
      );
    }
    return item;
  }
}
