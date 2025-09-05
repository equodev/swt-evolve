import 'package:flutter/material.dart';
import 'package:flutter/widgets.dart';
import 'package:swtflutter/src/gen/item.dart';
import '../gen/swt.dart';
import '../gen/toolbar.dart';
import '../gen/toolitem.dart';
import '../gen/widget.dart';
import '../impl/composite_evolve.dart';
import 'package:swtflutter/src/impl/widget_config.dart';
import '../styles.dart';

class ToolBarImpl<T extends ToolBarSwt, V extends VToolBar>
    extends CompositeImpl<T, V> {
  @override
  Widget build(BuildContext context) {
    final toolItems = getToolItems();
    final style = StyleBits(state.style);
    final isVertical = style.has(SWT.VERTICAL);
    final shouldWrap = style.has(SWT.WRAP);

    return Builder(builder: (context) {
      Widget bar;
      if (shouldWrap) {
        bar = Wrap(
          direction: isVertical ? Axis.vertical : Axis.horizontal,
          children: toolItems,
        );
      } else {
        bar = SingleChildScrollView(
          scrollDirection: isVertical ? Axis.vertical : Axis.horizontal,
          child: isVertical
              ? Column(
                  mainAxisSize: MainAxisSize.min,
                  crossAxisAlignment: CrossAxisAlignment.center,
                  children: toolItems,
                )
              : Row(
                  mainAxisSize: MainAxisSize.min,
                  crossAxisAlignment: CrossAxisAlignment.center,
                  children: toolItems,
                ),
        );
      }

      return super.wrap(
        Container(
          decoration: BoxDecoration(
            color: AppColors.toolbarBackground,
          ),
          child: bar,
        ),
      );
    });
  }

  List<Widget> getToolItems() {
    if (state.items == null) {
      return [];
    }

    return state.items!
        .whereType<VToolItem>()
        .where((toolItem) => (toolItem.image != null || toolItem.text != null))
        .map((toolItem) => getWidgetForToolItem(toolItem))
        .toList();
  }

  Widget getWidgetForToolItem(VToolItem toolItem) {
    final itemWidget = ToolItemSwt(value: toolItem);

    return Padding(
      padding: const EdgeInsets.symmetric(horizontal: 2.0),
      child: itemWidget,
    );
  }
}
