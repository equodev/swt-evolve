import 'package:flutter/material.dart';
import 'package:flutter/widgets.dart';
import '../gen/event.dart';
import '../gen/swt.dart';
import '../gen/tabfolder.dart';
import '../gen/tabitem.dart';
import '../gen/widget.dart';
import '../gen/widgets.dart';
import '../impl/composite_evolve.dart';
import '../impl/ctabfolder_evolve.dart';
import '../theme/theme_extensions/tabfolder_theme_extension.dart';
import 'utils/widget_utils.dart';
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
    final index = items.indexWhere((item) => item.id == selectedItem.id);
    
    return index >= 0 ? index : 0;
  }

  @override
  Widget build(BuildContext context) {
    final widgetTheme = Theme.of(context).extension<TabFolderThemeExtension>()!;
    final tabItems = getTabItems();
    final tabBodies = getTabBodies();

    if (tabItems.isEmpty) {
      return Container();
    }

    final hasValidBounds = hasBounds(state.bounds);
    final constraints = getConstraintsFromBounds(state.bounds);

    final tabContent = _buildTabContent(tabBodies);
    
    Widget content = Column(
      mainAxisSize: hasValidBounds ? MainAxisSize.max : MainAxisSize.min,
      children: [
        buildTabBar(context, widgetTheme, tabItems),
        if (hasValidBounds) Expanded(child: tabContent) else tabContent,
      ],
    );

    if (hasValidBounds) {
      return ConstrainedBox(
        constraints: constraints!,
        child: content,
      );
    }

    return content;
  }

  Widget _buildTabContent(List<Widget> tabBodies) {
    return IndexedStack(
      index: _selectedIndex < tabBodies.length ? _selectedIndex : 0,
      children: tabBodies,
    );
  }

  Widget buildTabBar(BuildContext context, TabFolderThemeExtension widgetTheme, List<TabItem> tabItems) {
    final hasValidBounds = hasBounds(state.bounds);
    
    return Container(
      width: hasValidBounds ? double.infinity : null,
      decoration: BoxDecoration(
        color: widgetTheme.tabBarBackgroundColor,
        border: Border(
          bottom: BorderSide(color: widgetTheme.tabBarBorderColor, width: widgetTheme.tabBorderWidth),
        ),
      ),
      child: Row(
        mainAxisSize: hasValidBounds ? MainAxisSize.max : MainAxisSize.min,
        mainAxisAlignment: MainAxisAlignment.start,
        children: [
          ...tabItems.asMap().entries.map((entry) {
            final int index = entry.key;
            final TabItem tab = entry.value;

            return _buildTab(
              context: context,
              widgetTheme: widgetTheme,
              isSelected: index == _selectedIndex,
              tab: tab,
              onTap: () => _handleTabSelection(index),
              index: index,
            );
          }).toList(),
        ],
      ),
    );
  }

  Widget _buildTab({
    required BuildContext context,
    required TabFolderThemeExtension widgetTheme,
    required bool isSelected,
    required TabItem tab,
    required VoidCallback onTap,
    required int index,
  }) {
    final enabled = state.enabled ?? false;

    // Determine colors based on enabled state
    final backgroundColor = !enabled
        ? widgetTheme.tabDisabledBackgroundColor
        : (isSelected ? widgetTheme.tabSelectedBackgroundColor : widgetTheme.tabBackgroundColor);
    final borderColor = !enabled
        ? widgetTheme.tabDisabledBorderColor
        : widgetTheme.tabBorderColor;
    final bottomBorderColor = !enabled
        ? widgetTheme.tabDisabledBorderColor
        : (isSelected ? widgetTheme.tabSelectedBorderColor : widgetTheme.tabBorderColor);
    final textColor = !enabled
        ? widgetTheme.tabDisabledTextColor
        : (isSelected ? widgetTheme.tabSelectedTextColor : widgetTheme.tabTextColor);

    return MouseRegion(
      cursor: enabled ? SystemMouseCursors.click : SystemMouseCursors.basic,
      child: Material(
      color: Colors.transparent,
      child: InkWell(
        onTap: enabled ? onTap : null,
        onDoubleTap: enabled ? () => _handleDefaultSelection(index) : null,
        child: Container(
          padding: EdgeInsets.symmetric(horizontal: widgetTheme.tabPadding),
          margin: isSelected && enabled ? EdgeInsets.only(bottom: -widgetTheme.tabSelectedBorderWidth) : EdgeInsets.zero,
          constraints: const BoxConstraints(minHeight: 0),
          decoration: BoxDecoration(
            color: backgroundColor,
            border: Border(
              right: BorderSide(color: borderColor, width: widgetTheme.tabBorderWidth),
              bottom: isSelected && enabled
                  ? BorderSide(color: bottomBorderColor, width: widgetTheme.tabSelectedBorderWidth)
                  : BorderSide(color: borderColor, width: widgetTheme.tabBorderWidth),
            ),
          ),
          child: Center(
            child: tab.customContent != null
                ? TabItemContextProvider(
                    isSelected: isSelected,
                    isEnabled: enabled,
                    child: tab.customContent!,
                  )
                : Text(
                    tab.label,
                    style: isSelected && enabled
                        ? (widgetTheme.tabSelectedTextStyle ?? const TextStyle()).copyWith(color: textColor)
                        : (widgetTheme.tabTextStyle ?? const TextStyle()).copyWith(color: textColor),
                  ),
          ),
        ),
      ),
    ),
    );
  }

  void _updateSelection(int index) {
    setState(() {
      _selectedIndex = index;
      if (state.items != null && index < state.items!.length) {
        state.selection = [state.items![index]];
      }
    });
  }

  void _handleTabSelection(int index) {
    if (state.enabled != true) return;
    _updateSelection(index);
    var e = VEvent()..index = index;
    widget.sendSelectionSelection(state, e);
  }

  void _handleDefaultSelection(int index) {
    if (state.enabled != true) return;
    _updateSelection(index);
    var e = VEvent()..index = index;
    widget.sendSelectionDefaultSelection(state, e);
  }

  List<TabItem> getTabItems() {
    return state.items?.map((tabItem) => getWidgetForTabItem(tabItem)).toList() ?? [];
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
    return state.items?.map((e) => tabBody(e)).toList() ?? [];
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
