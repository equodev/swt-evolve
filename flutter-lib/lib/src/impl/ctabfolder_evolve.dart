import 'dart:io';
import 'dart:ui';

import 'package:flutter/material.dart';
import 'package:flutter/widgets.dart';
import 'package:swtflutter/main.dart';
import '../gen/composite.dart';
import '../gen/control.dart';
import '../gen/ctabfolder.dart';
import '../gen/ctabitem.dart';
import '../gen/event.dart';
import '../gen/swt.dart';
import '../gen/toolbar.dart';
import '../gen/widget.dart';
import '../gen/widgets.dart';
import '../impl/composite_evolve.dart';
import 'icons_map.dart';
import 'widget_config.dart';

class CTabFolderImpl<T extends CTabFolderSwt, V extends VCTabFolder>
    extends CompositeImpl<T, V> {
  final bool useDarkTheme = getCurrentTheme();
  late int _selectedIndex;
  bool _hoveringTopBar = false;

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

    final useSimpleStyle = false;
    final isSingle = state.single ?? false;
    final isTabBottom = state.tabPosition == SWT.BOTTOM ?? false;

    final double tabHeight =
        (state.tabHeight != null && state.tabHeight != SWT.DEFAULT)
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

  Widget buildTabBar(List<CTabItem> tabItems, double height,
      bool useSimpleStyle, bool isSingle) {
    List<CTabItem> visibleTabs = isSingle
        ? (_selectedIndex < tabItems.length ? [tabItems[_selectedIndex]] : [])
        : tabItems;

    final topRightComposite = getTopRightComposite();
    if (useSimpleStyle) {
      return buildSimpleTabBar(visibleTabs, height);
    } else {
      return buildAdvancedTabBar(visibleTabs, height, topRightComposite);
    }
  }

  Widget buildSimpleTabBar(List<CTabItem> tabs, double height) {
    final theme = Theme.of(context);
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
          ...tabs.asMap().entries.map((entry) {
            final int index = entry.key;
            final CTabItem tab = entry.value;

            return _buildSimpleTab(
              context: context,
              isSelected: index == _selectedIndex,
              tab: tab,
              onTap: () => _handleTabSelection(index),
              onClose:
                  tab.showCloseButton ? () => _handleTabClose(index) : null,
            );
          }).toList(),
          Spacer(),
        ],
      ),
    );
  }

  Widget buildAdvancedTabBar(
      List<CTabItem> tabs, double height, VComposite? topRightComposite) {
    final theme = Theme.of(context);
    final backgroundColor = AppColors.getBackgroundColor();
    final borderColor = AppColors.getBorderColor();

    final showMinimizeButton = state.minimizeVisible ?? false;
    final showMaximizeButton = state.maximizeVisible ?? false;
    final isMinimized = state.minimized ?? false;
    final isMaximized = state.maximized ?? false;

    return MouseRegion(
      onEnter: (_) => setState(() => _hoveringTopBar = true),
      onExit: (_) => setState(() => _hoveringTopBar = false),
      child: Container(
        height: height,
        decoration: BoxDecoration(
          color: backgroundColor,
          border: Border(bottom: BorderSide(color: borderColor, width: 1)),
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
                onClose:
                    tab.showCloseButton ? () => _handleTabClose(index) : null,
              );
            }),
            const Spacer(),
            if (topRightComposite != null ||
                showMinimizeButton ||
                showMaximizeButton)
              _HoverReveal(
                visible: getConfigFlags().ctabfolder_visible_controls == true
                    ? true
                    : _hoveringTopBar,
                child: Row(
                  mainAxisSize: MainAxisSize.min,
                  children: [
                    if (topRightComposite != null)
                      _buildTopRightComposite(topRightComposite),
                    if (showMinimizeButton)
                      _buildControlButton(
                        icon: isMinimized ? Icons.maximize : Icons.minimize,
                        onTap: _toggleMinimize,
                      ),
                    if (showMaximizeButton)
                      _buildControlButton(
                        icon: isMaximized
                            ? Icons.fullscreen_exit
                            : Icons.fullscreen,
                        onTap: _toggleMaximize,
                      ),
                    const SizedBox(width: 4),
                  ],
                ),
              ),
          ],
        ),
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
    final isDark = useDarkTheme;

    final backgroundColor = AppColors.getBackgroundColor();
    final selectedColor = AppColors.getSelectedColor();
    final borderColor = AppColors.getBorderColor();

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
              tab.customContent ??
                  Text(
                    tab.label,
                    style: TextStyle(
                      fontSize: 12,
                      color: isSelected
                          ? AppColors.getSelectedTextColor()
                          : AppColors.getTextColor(),
                      fontWeight:
                          isSelected ? FontWeight.w600 : FontWeight.normal,
                    ),
                  ),
              if (onClose != null) ...[
                const SizedBox(width: 4),
                InkWell(
                  onTap: onClose,
                  child: Icon(
                    Icons.close,
                    size: AppSizes.tabCloseIconSize,
                    color: isSelected
                        ? AppColors.getSelectedTextColor()
                        : AppColors.getTextColor(),
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
    final isDark = useDarkTheme;

    final backgroundColor = AppColors.getBackgroundColor();
    final selectedColor = AppColors.getSelectedColor();
    final borderColor = AppColors.getBorderColor();

    final highlightColor = AppColors.highlight;

    final showUnselectedClose = state.unselectedCloseVisible ?? false;
    final shouldShowClose =
        (onClose != null) && (isSelected || showUnselectedClose);

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
                        color: isSelected
                            ? AppColors.getSelectedTextColor()
                            : AppColors.getTextColor(),
                        fontWeight:
                            isSelected ? FontWeight.w600 : FontWeight.normal,
                      ),
                      child: tab.customContent!,
                    )
                  : Row(
                      mainAxisSize: MainAxisSize.min,
                      children: [
                        if (tab.image != null && shouldShowImage)
                          Padding(
                            padding:
                                const EdgeInsets.only(bottom: 1.0, right: 3.0),
                            child: !iconMap.containsKey(tab.image)
                                ? Image.file(
                                    File(tab.image!),
                                    width: AppSizes.tabIconSize,
                                    height: AppSizes.tabIconSize,
                                  )
                                : Icon(
                                    getIconByName(tab.image!),
                                    size: AppSizes.tabIconSize,
                                    color: isSelected
                                        ? AppColors.getSelectedTextColor()
                                        : AppColors.getTextColor(),
                                  ),
                          ),
                        Padding(
                          padding: const EdgeInsets.only(bottom: 2.0),
                          child: Text(
                            tab.label,
                            style: TextStyle(
                              fontSize: AppSizes.tabTextSize,
                              color: isSelected
                                  ? AppColors.getSelectedTextColor()
                                  : AppColors.getTextColor(),
                              fontWeight: isSelected
                                  ? FontWeight.w600
                                  : FontWeight.normal,
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
                        size: AppSizes.tabCloseIconSize,
                        color: isSelected
                            ? AppColors.getSelectedTextColor().withOpacity(0.9)
                            : AppColors.getTextColor().withOpacity(0.7),
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

  Widget _buildTopRightComposite(VComposite composite) {
    return CompositeSwt<VComposite>(
      key: ValueKey(composite.id),
      value: composite,
    );
  }

  VComposite? getTopRightComposite() {
    if (state.topRight == null) {
      return null;
    }
    if (state.topRight is VComposite) {
      return state.topRight as VComposite;
    }
    return null;
  }

  Widget _buildControlButton({
    required IconData icon,
    required VoidCallback onTap,
  }) {
    final darkTheme = ThemeData.dark().copyWith(
      primaryColor: Color(0xFF6366F1),
      scaffoldBackgroundColor: Color(0xFF1A1A1A),
    );

    final toolbarTheme = useDarkTheme ? darkTheme : Theme.of(context);
    final iconColor = toolbarTheme.iconTheme.color;

    return Material(
      color: Colors.transparent,
      child: InkWell(
        onTap: onTap,
        child: Padding(
          padding: const EdgeInsets.symmetric(horizontal: 4),
          child: Icon(
            icon,
            size: AppSizes.controlButtonSize,
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
    var e = VEvent()..index = index;
    widget.sendCTabFolder2close(state, e);
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

class _HoverReveal extends StatelessWidget {
  final bool visible;
  final Widget child;

  const _HoverReveal({required this.visible, required this.child});

  @override
  Widget build(BuildContext context) {
    return Visibility(
      visible: true,
      maintainSize: true,
      maintainAnimation: true,
      maintainState: true,
      child: AnimatedOpacity(
        opacity: visible ? 1.0 : 0.0,
        duration: Duration(milliseconds: visible ? 250 : 500),
        curve: Curves.easeOut,
        child: IgnorePointer(
          ignoring: !visible,
          child: child,
        ),
      ),
    );
  }
}
