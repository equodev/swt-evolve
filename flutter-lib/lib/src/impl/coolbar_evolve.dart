import 'package:flutter/material.dart';
import 'package:flutter/widgets.dart';
import 'package:swtflutter/src/gen/coolitem.dart';
import '../gen/coolbar.dart';
import '../gen/swt.dart';
import '../gen/widget.dart';
import '../impl/composite_evolve.dart';
import 'package:swtflutter/src/impl/widget_config.dart';
import '../styles.dart';
import 'color_utils.dart';

class CoolBarImpl<T extends CoolBarSwt, V extends VCoolBar>
    extends CompositeImpl<T, V> {
  @override
  Widget build(BuildContext context) {
    final coolItems = getCoolItems();
    final style = StyleBits(state.style);
    final isVertical = style.has(SWT.VERTICAL);
    final isLocked = state.locked ?? false;
    final wrapIndices = state.wrapIndices ?? [];

    final boundsHeight = state.bounds?.height ?? 0;
    final boundsWidth = state.bounds?.width ?? 0;

    // For horizontal: use height, for vertical: use width
    final crossAxisSize = isVertical
        ? ((boundsWidth > 0) ? boundsWidth.toDouble() : 25.0)
        : ((boundsHeight > 0) ? boundsHeight.toDouble() : 25.0);

    Widget coolBarContent;

    // Use wrapped layout if wrapIndices are provided
    if (wrapIndices.isNotEmpty) {
      coolBarContent = _buildWrappedCoolBar(coolItems, wrapIndices, isVertical);
    } else {
      // Simple single row/column layout
      coolBarContent = isVertical
          ? Column(
              mainAxisSize: MainAxisSize.max,
              crossAxisAlignment: CrossAxisAlignment.center,
              mainAxisAlignment: MainAxisAlignment.start,
              children: coolItems,
            )
          : Row(
              mainAxisSize: MainAxisSize.max,
              crossAxisAlignment: CrossAxisAlignment.center,
              mainAxisAlignment: MainAxisAlignment.start,
              children: coolItems,
            );
    }

    return MouseRegion(
      onEnter: (_) => widget.sendMouseTrackMouseEnter(state, null),
      onExit: (_) => widget.sendMouseTrackMouseExit(state, null),
      child: Focus(
        onFocusChange: (hasFocus) {
          if (hasFocus) {
            widget.sendFocusFocusIn(state, null);
          } else {
            widget.sendFocusFocusOut(state, null);
          }
        },
        child: Builder(builder: (context) {
          return super.wrap(
            Container(
              width: isVertical ? crossAxisSize : double.infinity,
              height: isVertical ? double.infinity : crossAxisSize,
              decoration: BoxDecoration(
                color: getBackground(),
                border: Border.all(
                  color: getBorderColor(),
                  width: 1,
                ),
              ),
              child: coolBarContent,
            ),
          );
        }),
      ),
    );
  }

  Widget _buildWrappedCoolBar(
    List<Widget> coolItems,
    List<int> wrapIndices,
    bool isVertical,
  ) {
    List<List<Widget>> wrappedItems = [];
    int currentIndex = 0;

    for (int i = 0; i < wrapIndices.length; i++) {
      int endIndex = wrapIndices[i];
      if (endIndex > currentIndex && endIndex <= coolItems.length) {
        wrappedItems.add(coolItems.sublist(currentIndex, endIndex));
        currentIndex = endIndex;
      }
    }

    if (currentIndex < coolItems.length) {
      wrappedItems.add(coolItems.sublist(currentIndex));
    }

    return SingleChildScrollView(
      scrollDirection: isVertical ? Axis.horizontal : Axis.vertical,
      child: isVertical
          ? Row(
              mainAxisSize: MainAxisSize.min,
              crossAxisAlignment: CrossAxisAlignment.start,
              children: wrappedItems.map((items) {
                return Column(
                  mainAxisSize: MainAxisSize.min,
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: items,
                );
              }).toList(),
            )
          : Column(
              mainAxisSize: MainAxisSize.min,
              crossAxisAlignment: CrossAxisAlignment.start,
              children: wrappedItems.map((items) {
                return Row(
                  mainAxisSize: MainAxisSize.min,
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: items,
                );
              }).toList(),
            ),
    );
  }

  List<Widget> getCoolItems() {
    if (state.items == null) {
      return [];
    }

    final itemOrder = state.itemOrder;
    final items = state.items!;
    final itemSizes = state.itemSizes;

    List<VCoolItem> orderedItems;
    if (itemOrder != null && itemOrder.isNotEmpty) {
      orderedItems = [];
      for (int index in itemOrder) {
        if (index >= 0 && index < items.length) {
          orderedItems.add(items[index]);
        }
      }
    } else {
      orderedItems = items;
    }

    return orderedItems.asMap().entries.map((entry) {
      final index = entry.key;
      final coolItem = entry.value;

      if (itemSizes != null && index < itemSizes.length) {
        final size = itemSizes[index];
        coolItem.preferredSize ??= size;
      }

      return getWidgetForCoolItem(coolItem);
    }).toList();
  }

  Widget getWidgetForCoolItem(VCoolItem coolItem) {
    final itemWidget = CoolItemSwt(value: coolItem);

    return Padding(
      padding: const EdgeInsets.symmetric(horizontal: 4.0, vertical: 2.0),
      child: itemWidget,
    );
  }
}
