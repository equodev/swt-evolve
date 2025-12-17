import 'package:flutter/material.dart';
import 'package:flutter/widgets.dart';
import '../gen/event.dart';
import '../gen/swt.dart';
import '../gen/tabfolder.dart';
import '../gen/tabitem.dart';
import '../gen/widget.dart';
import '../gen/widgets.dart';
import '../impl/composite_evolve.dart';
import 'widget_config.dart';

class TabFolderImpl<T extends TabFolderSwt, V extends VTabFolder>
    extends CompositeImpl<T, V> {
  late int _selectedIndex;

  @override
  void initState() {
    super.initState();
    _selectedIndex = _getSelectedIndexFromState();

    // Send initial selection event
    var e = VEvent()..index = _selectedIndex;
    widget.sendSelectionSelection(state, e);
  }

  @override
  void extraSetState() {
    super.extraSetState();
    final newIndex = _getSelectedIndexFromState();
    if (newIndex != _selectedIndex) {
      setState(() {
        _selectedIndex = newIndex;
      });
    }
  }

  int _getSelectedIndexFromState() {
    if (state.selection == null || state.selection!.isEmpty) {
      return 0;
    }

    final selectedItem = state.selection!.first;
    final items = state.items ?? [];

    for (int i = 0; i < items.length; i++) {
      if (items[i].id == selectedItem.id) {
        return i;
      }
    }

    return 0;
  }

  @override
  Widget build(BuildContext context) {
    final tabItems = getTabItems();
    final tabBodies = getTabBodies();

    if (tabItems.isEmpty) {
      return Container();
    }

    final double tabHeight = 28.0;

    return Column(
      children: [
        buildTabBar(tabItems, tabHeight),
        Expanded(
          child: IndexedStack(
            index: _selectedIndex < tabBodies.length ? _selectedIndex : 0,
            children: tabBodies,
          ),
        ),
      ],
    );
  }

  Widget buildTabBar(List<TabItem> tabItems, double height) {
    final backgroundColor = AppColors.getBackgroundColor();
    final borderColor = AppColors.getBorderColor();

    return Container(
      height: height,
      decoration: BoxDecoration(
        color: backgroundColor,
        border: Border(
          bottom: BorderSide(color: borderColor, width: 1),
        ),
      ),
      child: Row(
        children: [
          ...tabItems.asMap().entries.map((entry) {
            final int index = entry.key;
            final TabItem tab = entry.value;

            return _buildTab(
              context: context,
              isSelected: index == _selectedIndex,
              tab: tab,
              onTap: () => _handleTabSelection(index),
              index: index,
            );
          }).toList(),
          Spacer(),
        ],
      ),
    );
  }

  Widget _buildTab({
    required BuildContext context,
    required bool isSelected,
    required TabItem tab,
    required VoidCallback onTap,
    required int index,
  }) {
    final backgroundColor = AppColors.getBackgroundColor();
    final selectedColor = AppColors.getSelectedColor();
    final borderColor = AppColors.getBorderColor();

    return Material(
      color: Colors.transparent,
      child: InkWell(
        onTap: onTap,
        onDoubleTap: () => _handleDefaultSelection(index),
        child: Container(
          padding: const EdgeInsets.symmetric(horizontal: 8),
          margin: isSelected ? const EdgeInsets.only(bottom: -1) : EdgeInsets.zero,
          decoration: BoxDecoration(
            color: isSelected ? selectedColor : backgroundColor,
            border: Border(
              right: BorderSide(color: borderColor, width: 1),
              bottom: isSelected
                  ? BorderSide(color: selectedColor, width: 2)
                  : BorderSide(color: borderColor, width: 1),
            ),
          ),
          child: Center(
            child: tab.customContent ??
                Text(
                  tab.label,
                  style: TextStyle(
                    fontSize: 12,
                    color: isSelected
                        ? AppColors.getSelectedTextColor()
                        : AppColors.getTextColor(),
                    fontWeight: isSelected ? FontWeight.w600 : FontWeight.normal,
                  ),
                ),
          ),
        ),
      ),
    );
  }

  void _handleTabSelection(int index) {
    setState(() {
      _selectedIndex = index;

      // Update state.selection to match the selected item
      if (state.items != null && index < state.items!.length) {
        state.selection = [state.items![index]];
      }
    });

    var e = VEvent()..index = index;
    widget.sendSelectionSelection(state, e);
  }

  void _handleDefaultSelection(int index) {
    // First select the tab
    setState(() {
      _selectedIndex = index;

      // Update state.selection to match the selected item
      if (state.items != null && index < state.items!.length) {
        state.selection = [state.items![index]];
      }
    });

    // Then send the default selection event
    var e = VEvent()..index = index;
    widget.sendSelectionDefaultSelection(state, e);
  }

  List<TabItem> getTabItems() {
    if (state.items == null) {
      return [];
    }
    return state.items!
        .map((tabItem) => getWidgetForTabItem(tabItem))
        .toList();
  }

  TabItem getWidgetForTabItem(VTabItem tabItem) {
    final tabItemWidget = TabItemSwt(value: tabItem);

    return TabItem(
      label: tabItem.text ?? "",
      customContent: tabItemWidget,
      toolTipText: tabItem.toolTipText,
    );
  }

  List<Widget> getTabBodies() {
    if (state.items == null) {
      return <Widget>[];
    }
    return state.items!.map((e) => tabBody(e)).toList();
  }

  Widget tabBody(VTabItem e) {
    if (e.control != null) {
      return mapWidgetFromValue(e.control!);
    }
    return Container();
  }
}

class TabItem {
  final String label;
  final Widget? customContent;
  final String? toolTipText;

  TabItem({
    required this.label,
    this.customContent,
    this.toolTipText,
  });
}
