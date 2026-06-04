import 'dart:async';
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
import '../gen/widget.dart';
import '../gen/widgets.dart';
import '../gen/color.dart';
import '../gen/image.dart';
import '../custom/toolbar_composite.dart';
import 'composite_evolve.dart';
import 'icons_map.dart';
import 'widget_config.dart';
import '../theme/theme_extensions/ctabfolder_theme_extension.dart';
import '../theme/theme_settings/ctabfolder_theme_settings.dart';
import 'utils/image_utils.dart';
import 'utils/widget_utils.dart';
import 'color_utils.dart';

class CTabFolderImpl<T extends CTabFolderSwt, V extends VCTabFolder>
    extends CompositeImpl<T, V> {
  late int _selectedIndex;

  @override
  void initState() {
    super.initState();
    _selectedIndex = state.selection ?? 0;
  }

  @override
  void extraSetState() {
    super.extraSetState();
    final itemCount = state.items?.length ?? 0;

    // If Java sent a valid selection, use it
    if (state.selection != null &&
        state.selection! >= 0 &&
        state.selection! < itemCount) {
      if (_selectedIndex != state.selection!) {
        _selectedIndex = state.selection!;
      }
    }
    // If state.selection is null (means 0 in Java due to skipDefaultValues) or out of range,
    // ensure _selectedIndex is valid
    else if (itemCount > 0) {
      if (_selectedIndex >= itemCount) {
        // _selectedIndex is out of range after tab removal, adjust it
        _selectedIndex = itemCount - 1;
      } else if (state.selection == null && _selectedIndex != 0) {
        // state.selection=null means Java has selection=0, update if different
        _selectedIndex = 0;
      }
    }

    if (state.minimized != null || state.maximized != null) {
      setState(() {});
    }
  }

  void _handleTabSecondaryTap(Offset globalPosition) {
    final box = context.findRenderObject() as RenderBox?;
    final localPos = box != null ? box.globalToLocal(globalPosition) : globalPosition;
    final e = VEvent()
      ..x = localPos.dx.round()
      ..y = localPos.dy.round();
    widget.sendMenuDetectMenuDetect(state, e);
  }

  @override
  Widget build(BuildContext context) {
    final tabItems = getTabItems();
    final tabBodies = getTabBodies();

    final isTabBottom = state.tabPosition == SWT.BOTTOM ?? false;
    final isMinimized = state.minimized ?? false;

    final double? tabHeight = 32;

    final constraints = getConstraintsFromBounds(state.bounds);

    Widget column = Column(
      children: [
        if (!isTabBottom)
          _CTabBar(
            state: state,
            selectedIndex: _selectedIndex,
            tabItems: tabItems,
            tabHeight: tabHeight,
            useSimpleStyle: false,
            topRightComposite: getTopRightComposite(),
            onTabSelected: _handleTabSelection,
            onTabClose: _handleTabClose,
            onMinimize: _toggleMinimize,
            onMaximize: _toggleMaximize,
            onSecondaryTap: _handleTabSecondaryTap,
            onTabReorder: _handleTabReorder,
          ),
        if (!isMinimized)
          Expanded(
            child: IndexedStack(
              index: _selectedIndex < tabBodies.length ? _selectedIndex : 0,
              children: tabBodies,
            ),
          ),
        if (isTabBottom)
          _CTabBar(
            state: state,
            selectedIndex: _selectedIndex,
            tabItems: tabItems,
            tabHeight: tabHeight,
            useSimpleStyle: false,
            topRightComposite: getTopRightComposite(),
            onTabSelected: _handleTabSelection,
            onTabClose: _handleTabClose,
            onMinimize: _toggleMinimize,
            onMaximize: _toggleMaximize,
            onSecondaryTap: _handleTabSecondaryTap,
            onTabReorder: _handleTabReorder,
          ),
      ],
    );

    if (constraints != null) {
      return ConstrainedBox(constraints: constraints, child: column);
    }

    return column;
  }

  void _handleTabSelection(int index) {
    if (state.enabled != true) return;
    setState(() {
      _selectedIndex = index;
      state.selection = index;
    });
    var e = VEvent()..index = index;
    widget.sendSelectionSelection(state, e);
  }

  void _toggleMinimize() {
    if (state.enabled != true) return;
    final isMinimized = state.minimized ?? false;
    var e = VEvent();
    if (isMinimized) {
      widget.sendCTabFolder2restore(state, e);
    } else {
      widget.sendCTabFolder2minimize(state, e);
    }
  }

  void _toggleMaximize() {
    if (state.enabled != true) return;
    final isMaximized = state.maximized ?? false;
    var e = VEvent();
    if (isMaximized) {
      widget.sendCTabFolder2restore(state, e);
    } else {
      widget.sendCTabFolder2maximize(state, e);
    }
  }

  VComposite? getTopRightComposite() {
    return state.topRight is VComposite ? state.topRight as VComposite : null;
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
      showCloseButton: tabItem.showClose ?? false,
      customContent: tabItemWidget,
      toolTipText: tabItem.toolTipText,
    );
  }

  List<Widget> getTabBodies() {
    if (state.items == null) {
      return <Widget>[];
    }
    return state.items!.whereType<VCTabItem>().map((e) => tabBody(e)).toList();
  }

  Widget tabBody(VCTabItem e) {
    if (e.control != null) {
      final control = e.control!;

      if (hasBounds(control.bounds)) {
        final bounds = control.bounds!;
        return SizedBox(
          width: bounds.width.toDouble(),
          height: bounds.height.toDouble(),
          child: mapWidgetFromValue(control),
        );
      } else {
        return SizedBox.expand(child: mapWidgetFromValue(control));
      }
    }
    return Container();
  }

  void _handleTabClose(int index) {
    if (state.enabled != true) return;
    var e = VEvent()..index = index;
    widget.sendCTabFolder2close(state, e);
    widget.sendCTabFolderitemClosed(state, e);
  }

  void _handleTabReorder(int fromIndex, int toIndex) {
    if (state.enabled != true) return;
    if (fromIndex == toIndex) return;
    final e = VEvent()
      ..index = fromIndex
      ..detail = toIndex;
    widget.sendCTabFolderreorderItems(state, e);
  }
}

class _CTabBar extends StatefulWidget {
  final VCTabFolder state;
  final int selectedIndex;
  final List<CTabItem> tabItems;
  final double? tabHeight;
  final bool useSimpleStyle;
  final VComposite? topRightComposite;
  final ValueChanged<int> onTabSelected;
  final ValueChanged<int> onTabClose;
  final VoidCallback onMinimize;
  final VoidCallback onMaximize;
  final void Function(Offset globalPosition)? onSecondaryTap;
  final void Function(int fromIndex, int toIndex)? onTabReorder;

  const _CTabBar({
    required this.state,
    required this.selectedIndex,
    required this.tabItems,
    required this.tabHeight,
    required this.useSimpleStyle,
    required this.topRightComposite,
    required this.onTabSelected,
    required this.onTabClose,
    required this.onMinimize,
    required this.onMaximize,
    this.onSecondaryTap,
    this.onTabReorder,
  });

  @override
  State<_CTabBar> createState() => _CTabBarState();
}

class _CTabBarState extends State<_CTabBar> {
  bool _hoveringTopBar = false;
  bool _scrollbarVisible = false;
  Timer? _scrollbarHideTimer;
  late final ScrollController _horizontalScrollController;
  bool _isMinimizeHovered = false;
  bool _isMaximizeHovered = false;
  int? _hoveredTabIndex;
  int? _pendingFrom;
  int? _pendingTo;


  @override
  void initState() {
    super.initState();
    _horizontalScrollController = ScrollController();
  }

  @override
  void didUpdateWidget(_CTabBar oldWidget) {
    super.didUpdateWidget(oldWidget);
    if (_pendingFrom != null) {
      _pendingFrom = null;
      _pendingTo = null;
    }
  }

  @override
  void dispose() {
    _scrollbarHideTimer?.cancel();
    _horizontalScrollController.dispose();
    super.dispose();
  }

  List<CTabItem> _applyPendingReorder(List<CTabItem> tabs) {
    if (_pendingFrom == null || _pendingTo == null) return tabs;
    if (_pendingFrom! >= tabs.length || _pendingTo! >= tabs.length) return tabs;
    final result = List<CTabItem>.from(tabs);
    result.insert(_pendingTo!, result.removeAt(_pendingFrom!));
    return result;
  }

  int _computeEffectiveSelected(int selected) {
    if (_pendingFrom == null || _pendingTo == null) return selected;
    final from = _pendingFrom!;
    final to = _pendingTo!;
    if (selected == from) return to;
    if (from < to && selected > from && selected <= to) return selected - 1;
    if (from > to && selected >= to && selected < from) return selected + 1;
    return selected;
  }

  @override
  Widget build(BuildContext context) {
    final widgetTheme = Theme.of(
      context,
    ).extension<CTabFolderThemeExtension>()!;
    final isSingle = widget.state.single ?? false;

    List<CTabItem> visibleTabs = isSingle
        ? (widget.selectedIndex < widget.tabItems.length
            ? [widget.tabItems[widget.selectedIndex]]
            : [])
        : widget.tabItems;

    if (widget.useSimpleStyle) {
      return _buildSimpleTabBar(context, widgetTheme, visibleTabs, widget.tabHeight);
    } else {
      return _buildAdvancedTabBar(
        context,
        widgetTheme,
        visibleTabs,
        widget.tabHeight,
        widget.topRightComposite,
      );
    }
  }

  Widget _buildSimpleTabBar(
    BuildContext context,
    CTabFolderThemeExtension widgetTheme,
    List<CTabItem> tabs,
    double? height,
  ) {
    final isTabBottom = widget.state.tabPosition == SWT.BOTTOM ?? false;
    Widget tabBarContent = Row(
      children: [
        Expanded(
          child: _buildHorizontalScrollableTabs(
            widgetTheme: widgetTheme,
            child: Row(
              children: tabs.asMap().entries.map((entry) {
                final int index = entry.key;
                final CTabItem tab = entry.value;

                return Expanded(
                  child: _buildSimpleTab(
                    context: context,
                    widgetTheme: widgetTheme,
                    isSelected: index == widget.selectedIndex,
                    isHovered: index == _hoveredTabIndex,
                    tab: tab,
                    onTap: () => widget.onTabSelected(index),
                    onClose: tab.showCloseButton
                        ? () => widget.onTabClose(index)
                        : null,
                    isTabBottom: isTabBottom,
                    onHoverEnter: () => setState(() => _hoveredTabIndex = index),
                    onHoverExit: () => setState(() {
                      if (_hoveredTabIndex == index) _hoveredTabIndex = null;
                    }),
                  ),
                );
              }).toList(),
            ),
          ),
        ),
      ],
    );

    return _buildTabBarContainer(
      widgetTheme: widgetTheme,
      height: height,
      child: tabBarContent,
      isTabBottom: isTabBottom,
    );
  }

  Widget _buildAdvancedTabBar(
    BuildContext context,
    CTabFolderThemeExtension widgetTheme,
    List<CTabItem> tabs,
    double? height,
    VComposite? topRightComposite,
  ) {
    final isTabBottom = widget.state.tabPosition == SWT.BOTTOM ?? false;
    final showMinimizeButton = widget.state.minimizeVisible ?? false;
    final showMaximizeButton = widget.state.maximizeVisible ?? false;
    final isMinimized = widget.state.minimized ?? false;
    final isMaximized = widget.state.maximized ?? false;
    final topRightAlignment = widget.state.topRightAlignment ?? SWT.RIGHT;

    final displayTabs = _applyPendingReorder(tabs);
    final effectiveSelected = _computeEffectiveSelected(widget.selectedIndex);

    final tabChildren = displayTabs.asMap().entries.map((entry) {
      final index = entry.key;
      final tab = entry.value;
      return _buildAdvancedTab(
        context: context,
        widgetTheme: widgetTheme,
        isSelected: index == effectiveSelected,
        isHovered: index == _hoveredTabIndex,
        tab: tab,
        onTap: () => widget.onTabSelected(index),
        onClose: tab.showCloseButton ? () => widget.onTabClose(index) : null,
        isTabBottom: isTabBottom,
        onHoverEnter: () => setState(() => _hoveredTabIndex = index),
        onHoverExit: () => setState(() {
          if (_hoveredTabIndex == index) _hoveredTabIndex = null;
        }),
      );
    }).toList();

    final tabRowWithDrag = DragReorderRow(
      children: tabChildren,
      overlayBuilder: (index) => _buildAdvancedTab(
        context: context,
        widgetTheme: widgetTheme,
        isSelected: index == effectiveSelected,
        isHovered: false,
        tab: displayTabs[index],
        onTap: () {},
        onClose: null,
        isTabBottom: isTabBottom,
        onHoverEnter: () {},
        onHoverExit: () {},
      ),
      onReorder: (from, to) {
        setState(() {
          _pendingFrom = from;
          _pendingTo = to;
        });
        widget.onTabReorder?.call(from, to);
      },
      onDragStart: () => setState(() => _hoveredTabIndex = null),
      dragThreshold: widgetTheme.tabDragThreshold,
    );

    Widget tabBarContent = Row(
      children: [
        Expanded(
          child: _buildHorizontalScrollableTabs(
            widgetTheme: widgetTheme,
            child: tabRowWithDrag,
          ),
        ),
      ],
    );

    final topRightControls = _buildTopRightControls(
      context: context,
      widgetTheme: widgetTheme,
      topRightComposite: topRightComposite,
      showMinimizeButton: showMinimizeButton,
      showMaximizeButton: showMaximizeButton,
      isMinimized: isMinimized,
      isMaximized: isMaximized,
      alignment: topRightAlignment,
    );

    return MouseRegion(
      onEnter: (_) => setState(() => _hoveringTopBar = true),
      onExit: (_) => setState(() => _hoveringTopBar = false),
      child: _buildTabBarContainer(
        widgetTheme: widgetTheme,
        height: height,
        child: Stack(
          children: [
            tabBarContent,
            Positioned(right: 0, top: 0, bottom: 0, child: topRightControls),
          ],
        ),
        isTabBottom: isTabBottom,
      ),
    );
  }

  Widget _buildSimpleTab({
    required BuildContext context,
    required CTabFolderThemeExtension widgetTheme,
    required bool isSelected,
    required bool isHovered,
    required CTabItem tab,
    required VoidCallback onTap,
    VoidCallback? onClose,
    required bool isTabBottom,
    required VoidCallback onHoverEnter,
    required VoidCallback onHoverExit,
  }) {
    final enabled = widget.state.enabled ?? false;

    final resolvedSelectionForeground = getForegroundColor(
      foreground: widget.state.selectionForeground,
      defaultColor: widgetTheme.tabSelectedTextColor,
    );
    final resolvedSelectionBackground = getBackgroundColor(
      background: widget.state.selectionBackground,
      defaultColor: widgetTheme.tabSelectedBackgroundColor,
    ) ?? widgetTheme.tabSelectedBackgroundColor;

    final textColor = getCTabTextColor(
      widgetTheme,
      isSelected,
      enabled,
      resolvedSelectionForeground: resolvedSelectionForeground,
      isHovered: isHovered,
    );
    final backgroundColor = getCTabBackgroundColor(
      widgetTheme,
      isSelected,
      enabled,
      resolvedSelectionBackground: resolvedSelectionBackground,
      isHovered: isHovered,
    );
    final borderColor = getCTabBorderColor(widgetTheme, enabled, isHovered: isHovered);
    final textStyle = getCTabTextStyle(widgetTheme, isSelected, enabled);

    return MouseRegion(
      cursor: enabled ? SystemMouseCursors.click : SystemMouseCursors.basic,
      onEnter: (_) => onHoverEnter(),
      onExit: (_) => onHoverExit(),
      child: GestureDetector(
        onTap: onTap,
        onSecondaryTapDown: (details) =>
            widget.onSecondaryTap?.call(details.globalPosition),
        child: AnimatedContainer(
          duration: widgetTheme.hoverRevealDuration,
          curve: Curves.easeOut,
          height: double.infinity,
          padding: EdgeInsets.symmetric(
            horizontal: widgetTheme.tabHorizontalPadding,
            vertical: widgetTheme.tabVerticalPadding,
          ),
          decoration: BoxDecoration(
            color: backgroundColor,
            borderRadius: isTabBottom
                ? BorderRadius.only(
                    bottomLeft: Radius.circular(widgetTheme.tabBorderRadius),
                    bottomRight: Radius.circular(widgetTheme.tabBorderRadius),
                  )
                : BorderRadius.only(
                    topLeft: Radius.circular(widgetTheme.tabBorderRadius),
                    topRight: Radius.circular(widgetTheme.tabBorderRadius),
                  ),
            border: Border(
              right: BorderSide(
                color: borderColor,
                width: widgetTheme.tabBorderWidth,
              ),
              bottom: !isTabBottom && isSelected && enabled
                  ? BorderSide(
                      color: backgroundColor,
                      width:
                          (widget.state.selectionBarThickness != null &&
                              widget.state.selectionBarThickness! > 0)
                          ? widget.state.selectionBarThickness!.toDouble()
                          : widgetTheme.tabSelectedBorderWidth,
                    )
                  : !isTabBottom
                  ? BorderSide(
                      color: borderColor,
                      width: widgetTheme.tabBorderWidth,
                    )
                  : BorderSide.none,
              top: isTabBottom && isSelected && enabled
                  ? BorderSide(
                      color: backgroundColor,
                      width:
                          (widget.state.selectionBarThickness != null &&
                              widget.state.selectionBarThickness! > 0)
                          ? widget.state.selectionBarThickness!.toDouble()
                          : widgetTheme.tabSelectedBorderWidth,
                    )
                  : isTabBottom
                  ? BorderSide(
                      color: borderColor,
                      width: widgetTheme.tabBorderWidth,
                    )
                  : BorderSide.none,
            ),
          ),
          child: Row(
            mainAxisSize: MainAxisSize.min,
            children: [
              tab.customContent ??
                  Text(
                    _getTabText(tab.label),
                    style:
                        textStyle?.copyWith(color: textColor) ??
                        TextStyle(color: textColor),
                  ),
              if (onClose != null) ...[
                SizedBox(width: widgetTheme.tabCloseButtonSpacing),
                GestureDetector(
                  onTap: onClose,
                  child: Icon(
                    Icons.close,
                    size: widgetTheme.tabCloseIconSize,
                    color: widgetTheme.tabCloseButtonColor,
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
    required CTabFolderThemeExtension widgetTheme,
    required bool isSelected,
    required bool isHovered,
    required CTabItem tab,
    required VoidCallback onTap,
    VoidCallback? onClose,
    required bool isTabBottom,
    required VoidCallback onHoverEnter,
    required VoidCallback onHoverExit,
  }) {
    final showUnselectedClose = widget.state.unselectedCloseVisible ?? false;
    final shouldShowClose =
        (onClose != null) && (isSelected || showUnselectedClose);

    final showUnselectedImage = widget.state.unselectedImageVisible ?? false;
    final showSelectedImage = widget.state.selectedImageVisible ?? true;
    final shouldShowImage =
        (isSelected && showSelectedImage) ||
        (!isSelected && showUnselectedImage);

    final showHighlight = widget.state.highlightEnabled ?? false;
    final enabled = widget.state.enabled ?? false;
    final useDefaultTheme = getConfigFlags().theme_name == null;

    final resolvedSelectionForeground = getForegroundColor(
      foreground: widget.state.selectionForeground,
      defaultColor: widgetTheme.tabSelectedTextColor,
    );
    final resolvedSelectionBackground = getBackgroundColor(
      background: widget.state.selectionBackground,
      defaultColor: widgetTheme.tabSelectedBackgroundColor,
    ) ?? widgetTheme.tabSelectedBackgroundColor;

    final textColor = getCTabTextColor(
      widgetTheme,
      isSelected,
      enabled,
      resolvedSelectionForeground: resolvedSelectionForeground,
      isHovered: isHovered,
    );
    final backgroundColor = getCTabBackgroundColor(
      widgetTheme,
      isSelected,
      enabled,
      resolvedSelectionBackground: resolvedSelectionBackground,
      useDefaultTheme: useDefaultTheme,
      isHovered: isHovered,
    );
    final borderColor = getCTabBorderColor(widgetTheme, enabled, isHovered: isHovered);
    final textStyle = getCTabTextStyle(widgetTheme, isSelected, enabled);

    return MouseRegion(
      cursor: enabled ? SystemMouseCursors.click : SystemMouseCursors.basic,
      onEnter: (_) => onHoverEnter(),
      onExit: (_) => onHoverExit(),
      child: GestureDetector(
        onTap: onTap,
        onSecondaryTapDown: (details) =>
            widget.onSecondaryTap?.call(details.globalPosition),
        child: AnimatedContainer(
          duration: widgetTheme.hoverRevealDuration,
          curve: Curves.easeOut,
          height: double.infinity,
          padding: EdgeInsets.symmetric(
            horizontal: widgetTheme.tabHorizontalPadding,
            vertical: widgetTheme.tabVerticalPadding,
          ),
          decoration: BoxDecoration(
            color: backgroundColor,
            borderRadius: isTabBottom
                ? BorderRadius.only(
                    bottomLeft: Radius.circular(widgetTheme.tabBorderRadius),
                    bottomRight: Radius.circular(widgetTheme.tabBorderRadius),
                  )
                : BorderRadius.only(
                    topLeft: Radius.circular(widgetTheme.tabBorderRadius),
                    topRight: Radius.circular(widgetTheme.tabBorderRadius),
                  ),
            image: isSelected && enabled ? _buildSelectionBgImage() : null,
            border: Border(
              top: !isTabBottom && isSelected && enabled && showHighlight
                  ? BorderSide(
                      color: widgetTheme.tabHighlightColor,
                      width: widgetTheme.tabHighlightBorderWidth,
                    )
                  : isTabBottom && isSelected && enabled
                  ? BorderSide(
                      color: backgroundColor,
                      width:
                          (widget.state.selectionBarThickness != null &&
                              widget.state.selectionBarThickness! > 0)
                          ? widget.state.selectionBarThickness!.toDouble()
                          : widgetTheme.tabSelectedBorderWidth,
                    )
                  : isTabBottom
                  ? BorderSide(
                      color: borderColor,
                      width: widgetTheme.tabBorderWidth,
                    )
                  : BorderSide.none,
              right: BorderSide(
                color: borderColor,
                width: widgetTheme.tabBorderWidth,
              ),
              left: isSelected && enabled
                  ? BorderSide(
                      color: borderColor,
                      width: widgetTheme.tabBorderWidth,
                    )
                  : BorderSide.none,
              bottom: !isTabBottom && isSelected && enabled
                  ? BorderSide(
                      color: backgroundColor,
                      width:
                          (widget.state.selectionBarThickness != null &&
                              widget.state.selectionBarThickness! > 0)
                          ? widget.state.selectionBarThickness!.toDouble()
                          : widgetTheme.tabSelectedBorderWidth,
                    )
                  : !isTabBottom
                  ? BorderSide(
                      color: borderColor,
                      width: widgetTheme.tabBorderWidth,
                    )
                  : isTabBottom && isSelected && enabled && showHighlight
                  ? BorderSide(
                      color: widgetTheme.tabHighlightColor,
                      width: widgetTheme.tabHighlightBorderWidth,
                    )
                  : BorderSide.none,
            ),
          ),
          child: Row(
            mainAxisSize: MainAxisSize.min,
            children: [
              tab.customContent != null
                  ? TabItemContextProvider(
                      isSelected: isSelected,
                      isEnabled: enabled,
                      child: tab.customContent!,
                    )
                  : Row(
                      mainAxisSize: MainAxisSize.min,
                      children: [
                        if (tab.image != null && shouldShowImage)
                          Padding(
                            padding: EdgeInsets.only(
                              bottom: widgetTheme.tabIconBottomPadding,
                              right: widgetTheme.tabIconTextSpacing,
                            ),
                            child: !iconMap.containsKey(tab.image)
                                ? Image.file(
                                    File(tab.image!),
                                    width: widgetTheme.tabIconSize,
                                    height: widgetTheme.tabIconSize,
                                    fit: BoxFit.contain,
                                  )
                                : Icon(
                                    getIconByName(tab.image!),
                                    size: widgetTheme.tabIconSize,
                                    color: textColor,
                                  ),
                          ),
                        Padding(
                          padding: EdgeInsets.only(
                            bottom: widgetTheme.tabTextBottomPadding,
                          ),
                          child: Text(
                            _getTabText(tab.label),
                            style:
                                textStyle?.copyWith(color: textColor) ??
                                TextStyle(color: textColor),
                          ),
                        ),
                      ],
                    ),
              if (shouldShowClose) ...[
                SizedBox(width: widgetTheme.tabCloseButtonSpacing),
                MouseRegion(
                  cursor: enabled
                      ? SystemMouseCursors.click
                      : SystemMouseCursors.basic,
                  child: GestureDetector(
                    onTap: onClose,
                    child: Padding(
                      padding: EdgeInsets.only(
                        bottom: widgetTheme.tabCloseIconBottomPadding,
                      ),
                      child: Icon(
                        Icons.close,
                        size: widgetTheme.tabCloseIconSize,
                        color: getCTabCloseButtonColor(widgetTheme, isSelected),
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

  Widget _buildTopRightComposite(VComposite composite, {Color? backgroundColor}) {
    return ToolbarComposite(value: composite, backgroundColor: backgroundColor);
  }

  Widget _buildControlButton({
    required BuildContext context,
    required CTabFolderThemeExtension widgetTheme,
    required IconData icon,
    required VoidCallback onTap,
    required bool isHovered,
    required ValueChanged<bool> onHoverChanged,
    bool enabled = true,
  }) {
    return MouseRegion(
      onEnter: (_) {
        onHoverChanged(true);
      },
      onExit: (_) {
        onHoverChanged(false);
      },
      cursor: enabled ? SystemMouseCursors.click : SystemMouseCursors.basic,
      child: Material(
        color: Colors.transparent,
        child: InkWell(
          onTap: enabled ? onTap : null,
          splashColor: widgetTheme.controlButtonHoverColor.withOpacity(0.3),
          highlightColor: widgetTheme.controlButtonHoverColor.withOpacity(0.2),
          borderRadius: BorderRadius.circular(4),
          child: Container(
            height: double.infinity,
            padding: EdgeInsets.symmetric(
              horizontal: widgetTheme.controlButtonHorizontalPadding,
            ),
            alignment: Alignment.center,
            decoration: BoxDecoration(
              color: isHovered
                  ? widgetTheme.controlButtonHoverColor.withOpacity(0.1)
                  : Colors.transparent,
              borderRadius: BorderRadius.circular(4),
            ),
            child: Icon(
              icon,
              size: widgetTheme.controlButtonSize,
              color: widgetTheme.controlButtonColor,
            ),
          ),
        ),
      ),
    );
  }

  Widget _buildHorizontalScrollableTabs({
    required CTabFolderThemeExtension widgetTheme,
    required Widget child,
  }) {
    return LayoutBuilder(
      builder: (context, constraints) {
        return NotificationListener<ScrollUpdateNotification>(
          onNotification: (notification) {
            _showScrollbar();
            _hideScrollbarAfterDelay(widgetTheme.scrollbarHideDelay);
            return false;
          },
          child: MouseRegion(
            onEnter: (_) {
              _showScrollbar();
            },
            onExit: (_) {
              _hideScrollbarAfterDelay(widgetTheme.scrollbarHideDelay);
            },
            child: Listener(
              onPointerDown: (_) {
                _showScrollbar();
              },
              child: Scrollbar(
                controller: _horizontalScrollController,
                thumbVisibility: _scrollbarVisible,
                thickness: widgetTheme.tabScrollbarThickness,
                child: SingleChildScrollView(
                  controller: _horizontalScrollController,
                  scrollDirection: Axis.horizontal,
                  child: ConstrainedBox(
                    constraints: BoxConstraints(minWidth: constraints.maxWidth),
                    child: child,
                  ),
                ),
              ),
            ),
          ),
        );
      },
    );
  }

  void _hideScrollbarAfterDelay(Duration delay) {
    _scrollbarHideTimer?.cancel();
    _scrollbarHideTimer = Timer(delay, () {
      if (mounted && _scrollbarVisible) {
        setState(() {
          _scrollbarVisible = false;
        });
      }
    });
  }

  void _showScrollbar() {
    _scrollbarHideTimer?.cancel();
    if (!_scrollbarVisible) {
      setState(() {
        _scrollbarVisible = true;
      });
    }
  }

  Widget _buildTabBarContainer({
    required CTabFolderThemeExtension widgetTheme,
    required double? height,
    required Widget child,
    required bool isTabBottom,
  }) {
    final borderVisible = widget.state.borderVisible ?? true;
    final decoration = BoxDecoration(
      color: widgetTheme.tabBarBackgroundColor,
      border: borderVisible
          ? (isTabBottom
                ? Border(
                    top: BorderSide(
                      color: widgetTheme.tabBarBorderColor,
                      width: widgetTheme.tabBorderWidth,
                    ),
                  )
                : Border(
                    bottom: BorderSide(
                      color: widgetTheme.tabBarBorderColor,
                      width: widgetTheme.tabBorderWidth,
                    ),
                  ))
          : null,
    );

    if (height != null) {
      return Container(height: height, decoration: decoration, child: child);
    }

    return IntrinsicHeight(
      child: Container(decoration: decoration, child: child),
    );
  }

  String _getTabText(String label) {
    final minChars = widget.state.minimumCharacters;
    if (minChars != null && minChars > 0 && label.length > minChars) {
      return '${label.substring(0, minChars)}...';
    }
    return label;
  }

  DecorationImage? _buildSelectionBgImage() {
    final image = widget.state.selectionBgImage;
    if (image == null) return null;

    if (image.filename != null && image.filename!.isNotEmpty) {
      try {
        return DecorationImage(
          image: FileImage(File(image.filename!)),
          fit: BoxFit.cover,
        );
      } catch (e) {
        return null;
      }
    }

    return null;
  }

  Widget _buildTopRightControls({
    required BuildContext context,
    required CTabFolderThemeExtension widgetTheme,
    required VComposite? topRightComposite,
    required bool showMinimizeButton,
    required bool showMaximizeButton,
    required bool isMinimized,
    required bool isMaximized,
    required int alignment,
  }) {
    final hasControls =
        topRightComposite != null || showMinimizeButton || showMaximizeButton;
    if (!hasControls) return const SizedBox.shrink();

    final controls = Row(
      mainAxisSize: MainAxisSize.min,
      crossAxisAlignment: CrossAxisAlignment.stretch,
      children: [
        if (topRightComposite != null)
          _buildTopRightComposite(
            topRightComposite,
            backgroundColor: widgetTheme.tabBarBackgroundColor,
          ),
        if (showMinimizeButton)
          Builder(
            builder: (context) {
              final isVisible =
                  getConfigFlags().ctabfolder_visible_controls == true
                  ? true
                  : _hoveringTopBar;
              return _buildControlButton(
                context: context,
                widgetTheme: widgetTheme,
                icon: isMinimized ? Icons.maximize : Icons.minimize,
                onTap: isVisible ? widget.onMinimize : () {},
                isHovered: _isMinimizeHovered,
                onHoverChanged: (hovered) {
                  setState(() {
                    _isMinimizeHovered = hovered;
                  });
                },
                enabled: true,
              );
            },
          ),
        if (showMaximizeButton)
          Builder(
            builder: (context) {
              final isVisible =
                  getConfigFlags().ctabfolder_visible_controls == true
                  ? true
                  : _hoveringTopBar;
              return _buildControlButton(
                context: context,
                widgetTheme: widgetTheme,
                icon: isMaximized ? Icons.fullscreen_exit : Icons.fullscreen,
                onTap: isVisible ? widget.onMaximize : () {},
                isHovered: _isMaximizeHovered,
                onHoverChanged: (hovered) {
                  setState(() {
                    _isMaximizeHovered = hovered;
                  });
                },
                enabled: true,
              );
            },
          ),
      ],
    );

    final isControlsVisible =
        getConfigFlags().ctabfolder_visible_controls == true
        ? true
        : _hoveringTopBar;

    final revealWidget = _HoverReveal(
      visible: isControlsVisible,
      revealDuration: widgetTheme.hoverRevealDuration,
      hideDuration: widgetTheme.hoverHideDuration,
      child: Align(
        alignment: Alignment.topRight,
        child: Container(
          height: double.infinity,
          decoration: BoxDecoration(
            color: widgetTheme.tabBarBackgroundColor,
            boxShadow: [
              BoxShadow(
                color: widgetTheme.topRightControlsShadowColor.withOpacity(
                  widgetTheme.topRightControlsShadowOpacity,
                ),
                blurRadius: widgetTheme.topRightControlsShadowBlurRadius,
                offset: widgetTheme.topRightControlsShadowOffset,
              ),
            ],
          ),
          child: controls,
        ),
      ),
    );

    final scaledWidget = Transform.scale(
      scale: widgetTheme.controlButtonScale,
      alignment: Alignment.topRight,
      child: revealWidget,
    );

    if (alignment == SWT.LEFT) {
      return scaledWidget;
    } else if (alignment == SWT.CENTER) {
      return Expanded(child: Center(child: scaledWidget));
    } else {
      return scaledWidget;
    }
  }
}

class DragReorderRow extends StatefulWidget {
  final List<Widget> children;
  final Widget Function(int index)? overlayBuilder;
  final void Function(int from, int to)? onReorder;
  final VoidCallback? onDragStart;
  final double dragThreshold;

  const DragReorderRow({
    super.key,
    required this.children,
    this.overlayBuilder,
    this.onReorder,
    this.onDragStart,
    this.dragThreshold = 8.0,
  });

  @override
  State<DragReorderRow> createState() => _DragReorderRowState();
}

class _DragReorderRowState extends State<DragReorderRow> {
  final GlobalKey _rowKey = GlobalKey();
  final List<GlobalKey> _itemKeys = [];
  List<GlobalKey> _orderedItemKeys = [];

  int? _draggingIndex;
  int? _draggedToIndex;
  double? _dragStartX;
  bool _dragActive = false;
  List<double>? _cachedItemRights;
  double? _dragCurrentX;
  double? _dragOffsetX;

  @override
  void didUpdateWidget(DragReorderRow oldWidget) {
    super.didUpdateWidget(oldWidget);
    if (widget.children.length != oldWidget.children.length &&
        _draggingIndex != null) {
      WidgetsBinding.instance.addPostFrameCallback((_) {
        if (mounted) _resetDrag();
      });
    }
  }

  void _ensureItemKeys(int count) {
    while (_itemKeys.length < count) {
      _itemKeys.add(GlobalKey());
    }
  }

  RenderBox? _itemBox(int displayIndex) {
    if (displayIndex >= _orderedItemKeys.length) return null;
    return _orderedItemKeys[displayIndex].currentContext
        ?.findRenderObject() as RenderBox?;
  }

  int? _hitTest(double localX, int count) {
    final row = _rowKey.currentContext?.findRenderObject() as RenderBox?;
    if (row == null) return null;
    for (int i = 0; i < count; i++) {
      final box = _itemBox(i);
      if (box == null) continue;
      final left = row.globalToLocal(box.localToGlobal(Offset.zero)).dx;
      if (localX >= left && localX < left + box.size.width) return i;
    }
    return null;
  }

  int _destFromX(double localX, int count) {
    if (count == 0) return 0;
    final rights = _cachedItemRights;
    if (rights == null || rights.length != count) return _draggedToIndex ?? 0;
    for (int i = 0; i < count; i++) {
      if (localX < rights[i]) return i;
    }
    return count - 1;
  }

  int _originalFromDisplay(int d) {
    final from = _draggingIndex;
    final to = _draggedToIndex;
    if (from == null || to == null || from == to) return d;
    if (d == to) return from;
    if (from < to && d >= from && d < to) return d + 1;
    if (from > to && d > to && d <= from) return d - 1;
    return d;
  }

  void _onPointerDown(PointerDownEvent event) {
    if (event.buttons != 1) return;
    final count = widget.children.length;
    final index = _hitTest(event.localPosition.dx, count);
    if (index == null) return;
    final row = _rowKey.currentContext?.findRenderObject() as RenderBox?;
    if (row != null) {
      _cachedItemRights = List.generate(count, (i) {
        final box = _itemBox(i);
        if (box == null) return 0.0;
        return row.globalToLocal(box.localToGlobal(Offset.zero)).dx +
            box.size.width;
      });
      final box = _itemBox(index);
      if (box != null) {
        _dragOffsetX = event.localPosition.dx -
            row.globalToLocal(box.localToGlobal(Offset.zero)).dx;
      }
    }
    setState(() {
      _draggingIndex = index;
      _draggedToIndex = index;
      _dragStartX = event.localPosition.dx;
      _dragActive = false;
    });
  }

  void _onPointerMove(PointerMoveEvent event) {
    if (_draggingIndex == null) return;
    final count = widget.children.length;
    final dx =
        event.localPosition.dx - (_dragStartX ?? event.localPosition.dx);
    if (!_dragActive && dx.abs() >= widget.dragThreshold) {
      _dragActive = true;
      widget.onDragStart?.call();
    }
    if (_dragActive) {
      setState(() {
        _dragCurrentX = event.localPosition.dx;
        _draggedToIndex = _destFromX(event.localPosition.dx, count);
      });
    }
  }

  void _onPointerUp(PointerUpEvent event) {
    if (_draggingIndex == null) return;
    if (_dragActive) {
      final from = _draggingIndex!;
      final to = _draggedToIndex ?? from;
      if (from != to) widget.onReorder?.call(from, to);
    }
    _resetDrag();
  }

  void _onPointerCancel(PointerCancelEvent event) {
    if (_draggingIndex == null) return;
    _resetDrag();
  }

  void _resetDrag() {
    setState(() {
      _draggingIndex = null;
      _draggedToIndex = null;
      _dragStartX = null;
      _dragActive = false;
      _cachedItemRights = null;
      _dragCurrentX = null;
      _dragOffsetX = null;
    });
  }

  @override
  Widget build(BuildContext context) {
    final count = widget.children.length;
    _ensureItemKeys(count);

    final isDragging =
        _dragActive && _draggingIndex != null && _draggedToIndex != null;

    _orderedItemKeys = List.generate(count, (d) {
      final original = isDragging ? _originalFromDisplay(d) : d;
      return _itemKeys[original.clamp(0, _itemKeys.length - 1)];
    });

    final rowItems = List.generate(count, (d) {
      if (isDragging && d == _draggedToIndex) {
        // Invisible placeholder — preserves the tab's natural height so the
        // Stack doesn't collapse to zero (which would hide the overlay too).
        final original = _originalFromDisplay(d);
        return KeyedSubtree(
          key: _orderedItemKeys[d],
          child: Opacity(opacity: 0, child: widget.children[original]),
        );
      }
      final original = isDragging ? _originalFromDisplay(d) : d;
      return KeyedSubtree(
        key: _orderedItemKeys[d],
        child: widget.children[original],
      );
    });

    Widget? dragOverlay;
    if (isDragging &&
        _dragCurrentX != null &&
        _draggingIndex! < count &&
        widget.overlayBuilder != null) {
      dragOverlay = Positioned(
        left: _dragCurrentX! - (_dragOffsetX ?? 0),
        top: 0,
        bottom: 0,
        child: DecoratedBox(
          decoration: const BoxDecoration(
            boxShadow: [
              BoxShadow(
                color: Colors.black26,
                blurRadius: 6,
                offset: Offset(2, 2),
              ),
            ],
          ),
          child: widget.overlayBuilder!(_draggingIndex!),
        ),
      );
    }

    return MouseRegion(
      cursor: _dragActive
          ? SystemMouseCursors.grabbing
          : _draggingIndex != null
              ? SystemMouseCursors.grab
              : MouseCursor.defer,
      child: Listener(
        onPointerDown: _onPointerDown,
        onPointerMove: _onPointerMove,
        onPointerUp: _onPointerUp,
        onPointerCancel: _onPointerCancel,
        child: Stack(
          clipBehavior: Clip.none,
          children: [
            AbsorbPointer(
              absorbing: _dragActive,
              child: Row(key: _rowKey, children: rowItems),
            ),
            if (dragOverlay != null) dragOverlay,
          ],
        ),
      ),
    );
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
  final Duration revealDuration;
  final Duration hideDuration;

  const _HoverReveal({
    required this.visible,
    required this.child,
    required this.revealDuration,
    required this.hideDuration,
  });

  @override
  Widget build(BuildContext context) {
    return Visibility(
      visible: true,
      maintainSize: true,
      maintainAnimation: true,
      maintainState: true,
      child: AnimatedOpacity(
        opacity: visible ? 1.0 : 0.0,
        duration: visible ? revealDuration : hideDuration,
        curve: Curves.easeOut,
        child: child,
      ),
    );
  }
}

class TabItemContext {
  final bool isSelected;
  final bool isEnabled;

  TabItemContext({required this.isSelected, required this.isEnabled});

  static TabItemContext? of(BuildContext context) {
    final provider = context
        .dependOnInheritedWidgetOfExactType<TabItemContextProvider>();
    return provider?.context;
  }
}

class TabItemContextProvider extends InheritedWidget {
  final TabItemContext context;

  TabItemContextProvider({
    Key? key,
    required bool isSelected,
    required bool isEnabled,
    required Widget child,
  }) : context = TabItemContext(isSelected: isSelected, isEnabled: isEnabled),
       super(key: key, child: child);

  @override
  bool updateShouldNotify(TabItemContextProvider oldWidget) {
    return context.isSelected != oldWidget.context.isSelected ||
        context.isEnabled != oldWidget.context.isEnabled;
  }
}