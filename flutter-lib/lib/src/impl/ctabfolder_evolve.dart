import 'dart:io';
import 'dart:ui';

import 'package:flutter/material.dart';
import 'package:flutter/widgets.dart';
import '../gen/ctabfolder.dart';
import '../gen/ctabitem.dart';
import '../gen/event.dart';
import '../gen/swt.dart';
import '../gen/widget.dart';
import '../gen/widgets.dart';
import '../impl/composite_evolve.dart';
import 'icons_map.dart';
import 'widget_config.dart';

class CTabFolderImpl<T extends CTabFolderSwt, V extends VCTabFolder>
    extends CompositeImpl<T, V> {

  final bool useDarkTheme = getCurrentTheme();
  late int _selectedIndex;

  @override
  void initState() {
    super.initState();
    _selectedIndex = state.selection ?? 0;

    var e = VEvent()..index = _selectedIndex;
    widget.sendSelectionSelection(state, e);
  }

  @override
  void extraSetState() {
    super.extraSetState();
    if (state.selection != null && state.selection != _selectedIndex) {
      _selectedIndex = state.selection!;
    }
  }

  @override
  Widget build(BuildContext context) {
    final tabItems = getTabItems();
    final tabBodies = getTabBodies();

    // Decidir si usar una implementación simple o avanzada basada en el estado
    // final useSimpleStyle = state.simple ?? false;
    final useSimpleStyle = false;
    final isSingle = state.single ?? false;
    final isTabBottom = state.tabPosition == SWT.BOTTOM ?? false;

    // Configurar la altura de los tabs si está especificada
    final double tabHeight = (state.tabHeight != null && state.tabHeight != SWT.DEFAULT)
        ? state.tabHeight!.toDouble()
        : 28.0;

    return Column(
      children: [
        if (!isTabBottom)
          buildTabBar(tabItems, tabHeight, useSimpleStyle, isSingle),

        Expanded(
          child: IndexedStack(
            index: _selectedIndex < tabBodies.length ? _selectedIndex : 0,
            children: tabBodies,
          ),
        ),

        if (isTabBottom)
          buildTabBar(tabItems, tabHeight, useSimpleStyle, isSingle),
      ],
    );
  }

  Widget buildTabBar(List<CTabItem> tabItems, double height, bool useSimpleStyle, bool isSingle) {
    List<CTabItem> visibleTabs = isSingle
        ? (_selectedIndex < tabItems.length ? [tabItems[_selectedIndex]] : [])
        : tabItems;

    if (useSimpleStyle) {
      return buildSimpleTabBar(visibleTabs, height);
    } else {
      return buildAdvancedTabBar(visibleTabs, height);
    }
  }

  Widget buildSimpleTabBar(List<CTabItem> tabs, double height) {
    final theme = Theme.of(context);
    final isDark = theme.brightness == Brightness.dark;
    final backgroundColor = isDark ? Color(0xFF1E1E1E) : Color(0xFFF2F2F2);
    final borderColor = isDark ? Color(0xFF333333) : Color(0xFFDDDDDD);

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
          ...tabs.asMap().entries.map((entry) {
            final int index = entry.key;
            final CTabItem tab = entry.value;

            return _buildSimpleTab(
              context: context,
              isSelected: index == _selectedIndex,
              tab: tab,
              onTap: () => _handleTabSelection(index),
              onClose: tab.showCloseButton ? () => _handleTabClose(index) : null,
            );
          }).toList(),

          Spacer(),

        ],
      ),
    );
  }

  Widget buildAdvancedTabBar(List<CTabItem> tabs, double height) {
    final theme = Theme.of(context);
    final isDark = theme.brightness == Brightness.dark;
    final backgroundColor = isDark ? Color(0xFF1A1A1A) : Color(0xFFF2F2F2);
    final borderColor = isDark ? Color(0xFF333333) : Color(0xFFDDDDDD);

    final showMinimizeButton = state.minimizeVisible ?? false;
    final showMaximizeButton = state.maximizeVisible ?? false;
    final isMinimized = state.minimized ?? false;
    final isMaximized = state.maximized ?? false;

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
          ...tabs.asMap().entries.map((entry) {
            final int index = entry.key;
            final CTabItem tab = entry.value;

            return _buildAdvancedTab(
              context: context,
              isSelected: index == _selectedIndex,
              tab: tab,
              onTap: () => _handleTabSelection(index),
              onClose: tab.showCloseButton ? () => _handleTabClose(index) : null,
            );
          }).toList(),

          Spacer(),

          if (showMinimizeButton)
            _buildControlButton(
              icon: isMinimized ? Icons.keyboard_arrow_up : Icons.keyboard_arrow_down,
              onTap: _toggleMinimize,
            ),

          if (showMaximizeButton)
            _buildControlButton(
              icon: isMaximized ? Icons.fullscreen_exit : Icons.fullscreen,
              onTap: _toggleMaximize,
            ),

          SizedBox(width: 4),
        ],
      ),
    );
  }

  Widget _buildSimpleTab({
    required BuildContext context,
    required bool isSelected,
    required CTabItem tab,
    required VoidCallback onTap,
    VoidCallback? onClose,
  }) {
    final theme = Theme.of(context);
    final isDark = theme.brightness == Brightness.dark;

    final backgroundColor = isDark ? Color(0xFF1A1A1A) : Color(0xFFF2F2F2);
    final selectedColor = isDark ? Color(0xFF2D2D2D) : Colors.white;
    final borderColor = isDark ? Color(0xFF333333) : Color(0xFFDDDDDD);

    final textColor = isDark ? Colors.grey.shade400 : Colors.grey.shade700;
    final selectedTextColor = isDark ? Colors.white : Colors.grey.shade900;

    return Material(
      color: Colors.transparent,
      child: InkWell(
        onTap: onTap,
        child: Container(
          height: double.infinity,
          padding: const EdgeInsets.symmetric(horizontal: 8),
          decoration: BoxDecoration(
            color: isSelected ? selectedColor : backgroundColor,
            border: Border(
              right: BorderSide(color: borderColor, width: 1),
              bottom: isSelected
                  ? BorderSide(color: selectedColor, width: 1)
                  : BorderSide(color: borderColor, width: 1),
            ),
          ),
          child: Row(
            mainAxisSize: MainAxisSize.min,
            children: [
              // Contenido del tab
              tab.customContent ??
                  Text(
                    tab.label,
                    style: TextStyle(
                      fontSize: 12,
                      color: isSelected ? selectedTextColor : textColor,
                      fontWeight: isSelected ? FontWeight.w600 : FontWeight.normal,
                    ),
                  ),

              // Botón de cierre
              if (onClose != null) ...[
                const SizedBox(width: 4),
                InkWell(
                  onTap: onClose,
                  child: Icon(
                    Icons.close,
                    size: 14,
                    color: isSelected ? selectedTextColor : textColor,
                  ),
                ),
              ],
            ],
          ),
        ),
      ),
    );
  }

  Widget _buildAdvancedTab({
    required BuildContext context,
    required bool isSelected,
    required CTabItem tab,
    required VoidCallback onTap,
    VoidCallback? onClose,
  }) {
    final theme = Theme.of(context);
    final isDark = theme.brightness == Brightness.dark;

    final backgroundColor = isDark ? Color(0xFF1A1A1A) : Color(0xFFF2F2F2);
    final selectedColor = isDark ? Color(0xFF2D2D2D) : Colors.white;
    final borderColor = isDark ? Color(0xFF333333) : Color(0xFFDDDDDD);

    final textColor = isDark ? Colors.grey.shade400 : Colors.grey.shade700;
    final selectedTextColor = isDark ? Colors.white : Colors.grey.shade900;
    final highlightColor = isDark ? Color(0xFF6366F1) : theme.primaryColor;

    final showUnselectedClose = state.unselectedCloseVisible ?? false;
    final shouldShowClose = (onClose != null) && (isSelected || showUnselectedClose);

    final showUnselectedImage = state.unselectedImageVisible ?? false;
    final shouldShowImage = isSelected || showUnselectedImage;

    final showHighlight = state.highlightEnabled ?? true;

    return Material(
      color: Colors.transparent,
      child: InkWell(
        onTap: onTap,
        child: Container(
          height: double.infinity,
          padding: const EdgeInsets.symmetric(horizontal: 8),
          decoration: BoxDecoration(
            color: isSelected ? selectedColor : backgroundColor,
            border: Border(
              top: isSelected && showHighlight
                  ? BorderSide(color: highlightColor, width: 2)
                  : BorderSide.none,
              right: BorderSide(color: borderColor, width: 1),
              left: isSelected
                  ? BorderSide(color: borderColor, width: 1)
                  : BorderSide.none,
              bottom: isSelected
                  ? BorderSide(color: selectedColor, width: 1)
                  : BorderSide(color: borderColor, width: 1),
            ),
          ),
          child: Row(
            mainAxisSize: MainAxisSize.min,
            children: [
              tab.customContent != null
                  ? DefaultTextStyle(
                style: TextStyle(
                  fontSize: 12,
                  color: isSelected ? selectedTextColor : textColor,
                  fontWeight: isSelected ? FontWeight.w600 : FontWeight.normal,
                ),
                child: tab.customContent!,
              )
                  : Row(
                mainAxisSize: MainAxisSize.min,
                children: [
                  if (tab.image != null && shouldShowImage)
                    Padding(
                      padding: const EdgeInsets.only(bottom: 1.0, right: 3.0),
                      child: !materialIconMap.containsKey(tab.image)
                          ? Image.file(
                        File(tab.image!),
                        width: 16,
                        height: 16,
                      )
                          : Icon(
                        getMaterialIconByName(tab.image!),
                        size: 16,
                        color: isSelected ? selectedTextColor : textColor,
                      ),
                    ),
                  Padding(
                    padding: const EdgeInsets.only(bottom: 2.0),
                    child: Text(
                      tab.label,
                      style: TextStyle(
                        fontSize: 12,
                        color: isSelected ? selectedTextColor : textColor,
                        fontWeight: isSelected ? FontWeight.w600 : FontWeight.normal,
                      ),
                    ),
                  ),
                ],
              ),

              if (shouldShowClose) ...[
                const SizedBox(width: 6),
                MouseRegion(
                  cursor: SystemMouseCursors.click,
                  child: GestureDetector(
                    onTap: onClose,
                    child: Padding(
                      padding: const EdgeInsets.only(bottom: 1.0),
                      child: Icon(
                        Icons.close,
                        size: 14,
                        color: isSelected
                            ? selectedTextColor.withOpacity(0.9)
                            : textColor.withOpacity(0.7),
                      ),
                    ),
                  ),
                ),
              ],
            ],
          ),
        ),
      ),
    );
  }

  Widget _buildControlButton({
    required IconData icon,
    required VoidCallback onTap,
  }) {
    final theme = Theme.of(context);
    final isDark = theme.brightness == Brightness.dark;
    final iconColor = isDark ? Colors.grey.shade400 : Colors.grey.shade600;

    return Material(
      color: Colors.transparent,
      child: InkWell(
        onTap: onTap,
        child: Padding(
          padding: const EdgeInsets.symmetric(horizontal: 4),
          child: Icon(
            icon,
            size: 16,
            color: iconColor,
          ),
        ),
      ),
    );
  }

  void _handleTabSelection(int index) {
    setState(() {
      _selectedIndex = index;
      state.selection = index;
    });
    var e = VEvent()..index = index;
    widget.sendSelectionSelection(state, e);
  }

  void _handleTabClose(int index) {

  }

  void _toggleMinimize() {
    final isMinimized = state.minimized ?? false;
    setState(() {
      state.minimized = !isMinimized;
    });
  }

  void _toggleMaximize() {
    final isMaximized = state.maximized ?? false;
    setState(() {
      state.maximized = !isMaximized;
    });
  }

  List<CTabItem> getTabItems() {
    if (state.items == null) {
      return [];
    }
    return state.items!
        .whereType<VCTabItem>()
        .map((tabItem) => getWidgetForTabItem(tabItem))
        .toList();
  }

  CTabItem getWidgetForTabItem(VCTabItem tabItem) {
    final tabItemWidget = CTabItemSwt(value: tabItem);

    return CTabItem(
      label: tabItem.text ?? "",
      // image: tabItem.image,
      showCloseButton: tabItem.showClose ?? false,
      customContent: tabItemWidget,
      toolTipText: tabItem.toolTipText,
    );
  }

  List<Widget> getTabBodies() {
    if (state.children == null) {
      return <Widget>[];
    }
    return state.children!
        .whereType<VCTabItem>()
        .map((e) => tabBody(e))
        .toList();
  }

  Widget tabBody(VCTabItem e) {
    if (e.control != null) {
      return mapWidgetFromValue(e.control!!);
    }
    return Container();
  }
}

class CTabItem {
  final String label;
  final String? image;
  final bool showCloseButton;
  final VoidCallback? onClose;
  final bool alignRight;
  final Widget? customContent;
  final String? toolTipText;

  CTabItem({
    required this.label,
    this.image,
    this.showCloseButton = false,
    this.onClose,
    this.alignRight = false,
    this.customContent,
    this.toolTipText,
  });
}