import 'dart:async';
import 'dart:convert';
import 'dart:io';
import 'dart:math' as math;
import 'dart:ui';

import 'package:flutter/gestures.dart';
import 'package:flutter/material.dart';
import 'package:flutter/widgets.dart';
import 'package:swtflutter/main.dart';
import '../comm/comm.dart';
import '../gen/composite.dart';
import '../gen/control.dart';
import '../gen/ctabfolder.dart';
import '../gen/ctabitem.dart';
import '../gen/droptarget.dart';
import '../gen/event.dart';
import '../gen/swt.dart';
import '../gen/widget.dart';
import '../gen/widgets.dart';
import '../gen/color.dart';
import '../gen/image.dart';
import '../custom/toolbar_composite.dart';
import 'composite_evolve.dart';
import 'widget_config.dart';
import '../theme/theme_extensions/ctabfolder_theme_extension.dart';
import '../theme/theme_settings/ctabfolder_theme_settings.dart';
import 'utils/dnd_utils.dart';
import 'utils/tracker_session.dart';
import 'utils/image_utils.dart';
import 'utils/widget_utils.dart';
import 'color_utils.dart';

class CTabFolderImpl<T extends CTabFolderSwt, V extends VCTabFolder>
    extends CompositeImpl<T, V> {
  late int _selectedIndex;
  int? _lastShowListPopupSeq;
  int? _pendingTabIndex;
  Object? _activationToken;

  String get _activationChannel => "${state.swt}/${state.id}/activation";

  @override
  void initState() {
    super.initState();
    // -1 means genuinely unselected (not Java's skipDefaultValues null=0); fall back to item 0.
    final selection = state.selection;
    _selectedIndex = (selection != null && selection >= 0) ? selection : 0;
    _activationToken = EquoCommService.onRaw(_activationChannel, (args) {
      final decoded = args is String ? jsonDecode(args) : args;
      if (mounted && decoded is Map && decoded['active'] is bool) {
        // Kept on the state too, or a rebuild re-reads the last push's stale flag in extraSetState.
        state.highlight = decoded['active'] as bool;
        // The active tab's label and ring belong to the tab bar, so the folder rebuilds.
        setState(() {});
      }
    });
  }

  @override
  void dispose() {
    EquoCommService.remove(_activationChannel, _activationToken);
    super.dispose();
  }

  @override
  void extraSetState() {
    super.extraSetState();
    final itemCount = state.items?.length ?? 0;

    if (_pendingTabIndex != null) {
      // Java selection caught up (null means 0 due to skipDefaultValues)
      final javaSelection = state.selection ?? 0;
      if (javaSelection == _pendingTabIndex) {
        _pendingTabIndex = null;
      } else {
        // Java sent old state — keep our pending selection to avoid flicker
        _selectedIndex = _pendingTabIndex!;
        state.selection = _pendingTabIndex;
        return;
      }
    }

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
      } else if ((state.selection == null || state.selection! < 0) &&
          _selectedIndex != 0) {
        // See initState: null (skipDefaultValues) and -1 (genuine) both fall back to item 0.
        _selectedIndex = 0;
      }
    }

    if (state.showListPopupSeq != null) {
      final seq = state.showListPopupSeq!;
      if (_lastShowListPopupSeq == null) {
        _lastShowListPopupSeq = seq;
      } else if (seq != _lastShowListPopupSeq) {
        _lastShowListPopupSeq = seq;
        WidgetsBinding.instance.addPostFrameCallback((_) {
          if (mounted) _doShowChevronPopup();
        });
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

    final isTabBottom = state.tabPosition == SWT.BOTTOM;

    final double? tabHeight = 32;

    final constraints = getConstraintsFromBounds(state.bounds);

    Widget column = Column(
      children: [
        if (!isTabBottom)
          _CTabBar(
            state: state,
            folderWidget: widget,
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
            onTabDrop: _handleTabDrop,
            onTabDragStarted: _handleTabDragStarted,
            onChevronShowList: _handleChevronShowList,
          ),
        // Render the body regardless of `minimized`: native SWT's `minimized` only
        // shrinks computeSize, it never hides the selected control. A collapsed folder
        // gets a tab-strip-height bounds (Expanded → ~0 height); an E4 minimized-stack
        // fly-out gets a full client area and must show its content.
        Expanded(
          child: IndexedStack(
            index: _selectedIndex < tabBodies.length ? _selectedIndex : 0,
            children: tabBodies,
          ),
        ),
        if (isTabBottom)
          _CTabBar(
            state: state,
            folderWidget: widget,
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
            onTabDrop: _handleTabDrop,
            onTabDragStarted: _handleTabDragStarted,
            onChevronShowList: _handleChevronShowList,
          ),
      ],
    );

    // SWT reads enablement down the parent chain - `isEnabled()` is this control's own flag and
    // every ancestor's - so a disabled folder disables the pages it holds. This build does not go
    // through `wrap()`, so it owes them that answer itself.
    if (constraints != null) {
      return tagSemantics(blockWhenDisabled(ParentForegroundScope(
        foreground: state.foreground,
        font: state.font,
        selectionForeground: state.selectionForeground,
        child: ConstrainedBox(constraints: constraints, child: column),
      )));
    }

    // The tabs letter and colour in the folder's own font/foreground when they carry none.
    return tagSemantics(blockWhenDisabled(ParentForegroundScope(
      foreground: state.foreground,
      font: state.font,
      selectionForeground: state.selectionForeground,
      child: column,
    )));
  }

  void _handleTabSelection(int index) {
    if (state.enabled != true) return;
    _pendingTabIndex = index;
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
      vItem: tabItem,
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

  Rect? _chevronButtonRect;

  void _handleChevronShowList(BuildContext buttonContext, CTabFolderThemeExtension widgetTheme) {
    final RenderBox? box = buttonContext.findRenderObject() as RenderBox?;
    if (box != null && box.hasSize) {
      final Offset origin = box.localToGlobal(Offset.zero);
      _chevronButtonRect = origin & box.size;
    } else {
      _chevronButtonRect = null;
    }
    // Web-mode Java can't measure the viewport (marks every item showing=true), so drive
    // the overflow menu client-side rather than round-tripping through Java.
    _doShowChevronPopup();
  }

  void _doShowChevronPopup() {
    if (!mounted) return;
    final ctx = context;
    final widgetTheme = Theme.of(ctx).extension<CTabFolderThemeExtension>()!;

    final allItems = state.items?.whereType<VCTabItem>().toList() ?? [];
    if (allItems.isEmpty) return;

    // Java marks every item showing=true in web mode, so when none is flagged hidden fall
    // back to listing every tab -- otherwise the off-view tabs are unreachable.
    var hiddenEntries = allItems.asMap().entries
        .where((e) => e.value.showing == false)
        .toList();
    if (hiddenEntries.isEmpty) {
      hiddenEntries = allItems.asMap().entries.toList();
    }

    final screenSize = MediaQuery.of(ctx).size;
    final buttonRect = _chevronButtonRect;
    final RelativeRect position = buttonRect != null
        ? RelativeRect.fromLTRB(
            buttonRect.left,
            buttonRect.bottom,
            screenSize.width - buttonRect.right,
            0,
          )
        : RelativeRect.fromLTRB(
            screenSize.width - 60,
            40,
            0,
            0,
          );

    showMenu<int>(
      context: ctx,
      position: position,
      items: hiddenEntries.map((entry) => PopupMenuItem<int>(
        value: entry.key,
        height: widgetTheme.chevronMenuItemHeight,
        padding: widgetTheme.chevronMenuItemPadding,
        child: Text(entry.value.text ?? ''),
      )).toList(),
    ).then((selectedIndex) {
      if (selectedIndex != null) {
        _handleTabSelection(selectedIndex);
      }
    });
  }

  void _handleTabClose(int index) {
    if (state.enabled != true) return;
    var e = VEvent()..index = index;
    widget.sendCTabFolder2close(state, e);
    widget.sendCTabFolderitemClosed(state, e);
  }

  /// An application's `dragSetData` identifies the tab being moved through
  /// `folder.getSelection()` — the same idiom Table/Tree DND uses — so the drag has to select
  /// it first. Without an application DragSource there is no such listener and the folder's
  /// own reordering must not steal the selection.
  void _handleTabDragStarted(int index) {
    if (state.dragSource != true) return;
    _handleTabSelection(index);
  }

  void _handleTabDrop(int insertIndex, int? targetItemId, Offset position) {
    final dropTargetId = state.dropTargetId;
    if (dropTargetId == null) return;
    final dropTargetValue = VDropTarget()..id = dropTargetId;
    final e = VEvent()
      ..x = position.dx.round()
      ..y = position.dy.round()
      ..index = insertIndex;
    if (targetItemId != null) e.itemId = targetItemId;
    DropTargetSwt<VDropTarget>(value: dropTargetValue)
        .sendDropdrop(dropTargetValue, e);
  }

  void _handleTabReorder(int fromIndex, int toIndex) {
    if (state.enabled != true) return;
    if (fromIndex == toIndex) return;
    final e = VEvent()
      ..index = fromIndex
      ..detail = toIndex;
    widget.sendCTabFolderreorderItems(state, e);

    final current = _selectedIndex;
    int newSelected = current;
    if (current == fromIndex) {
      newSelected = toIndex;
    } else if (fromIndex < toIndex && current > fromIndex && current <= toIndex) {
      newSelected = current - 1;
    } else if (fromIndex > toIndex && current >= toIndex && current < fromIndex) {
      newSelected = current + 1;
    }
    if (newSelected != current) {
      setState(() {
        _selectedIndex = newSelected;
        _pendingTabIndex = newSelected;
        state.selection = newSelected;
      });
    }
  }
}

class _CTabBar extends StatefulWidget {
  final VCTabFolder state;
  final CTabFolderSwt folderWidget;
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
  final void Function(int insertIndex, int? targetItemId, Offset position)? onTabDrop;
  final ValueChanged<int>? onTabDragStarted;
  final void Function(BuildContext, CTabFolderThemeExtension widgetTheme)? onChevronShowList;

  const _CTabBar({
    required this.state,
    required this.folderWidget,
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
    this.onTabDrop,
    this.onTabDragStarted,
    this.onChevronShowList,
  });

  @override
  State<_CTabBar> createState() => _CTabBarState();
}

class _CTabBarState extends State<_CTabBar> {
  bool _hoveringTopBar = false;
  bool _scrollbarVisible = false;
  // True when the tab row overflows its width (some tabs scroll off-view). Drives the chevron.
  bool _hasOverflow = false;
  Timer? _scrollbarHideTimer;
  late final ScrollController _horizontalScrollController;
  bool _isMinimizeHovered = false;
  bool _isMaximizeHovered = false;
  bool _isChevronHovered = false;
  int? _hoveredTabIndex;
  int? _pendingFrom;
  int? _pendingTo;

  // ctabfolder_topright_auto_hide (default true, matches the historical behaviour):
  // the topRight/chevron/minimize/maximize block auto-hides until hovered and floats
  // on top of the tab row without reserving space. Set to false to keep it always
  // shown and reserving its own width in the tab row instead -- a client relying on
  // an always-visible topRight toolbar needs both effects together, so this is one
  // flag, not two: ctabfolder_visible_controls alone only removes the hover-fade, it
  // does not reserve space.
  bool get _autoHide => getConfigFlags().ctabfolder_topright_auto_hide != false;
  bool get _reserveTopRightSpace => !_autoHide;
  bool get _controlsAlwaysVisible =>
      getConfigFlags().ctabfolder_visible_controls == true ||
      _reserveTopRightSpace ||
      // While overflowing, the chevron is the only way to reach the hidden tabs -- keep it visible.
      _hasOverflow;

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
    final isTabBottom = widget.state.tabPosition == SWT.BOTTOM;
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
    final isTabBottom = widget.state.tabPosition == SWT.BOTTOM;
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

    final tabRowWithDrag = TabDragRow(
      children: tabChildren,
      tabs: displayTabs,
      folderState: widget.state,
      folderWidget: widget.folderWidget,
      tabHeight: widget.tabHeight,
      rowWrapper: (row) => _buildHorizontalScrollableTabs(
        widgetTheme: widgetTheme,
        child: row,
      ),
      onReorder: (from, to) {
        setState(() {
          _pendingFrom = from;
          _pendingTo = to;
        });
        widget.onTabReorder?.call(from, to);
      },
      onDrop: widget.onTabDrop,
      onTabDragStarted: widget.onTabDragStarted,
      onDragStart: () => setState(() => _hoveredTabIndex = null),
    );

    final scrollableTabs = Expanded(child: tabRowWithDrag);

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

    // ctabfolder_topright_auto_hide=false: the controls sit as a normal Row
    // sibling, so the scrollable tab area shrinks to make room for them -- no tab
    // ever ends up hidden underneath a floating overlay. Default (true): the
    // original Stack overlay, full-width tabs with the (auto-hidden, hover-revealed)
    // controls on top.
    // While overflowing, lay the controls out as a Row sibling so the chevron reserves space
    // and stays clickable, instead of floating over the tab row where clicks get swallowed.
    final Widget header = (_reserveTopRightSpace || _hasOverflow)
        ? Row(children: [scrollableTabs, topRightControls])
        : Stack(
            children: [
              Row(children: [scrollableTabs]),
              Positioned(right: 0, top: 0, bottom: 0, child: topRightControls),
            ],
          );

    return MouseRegion(
      onEnter: (_) => setState(() => _hoveringTopBar = true),
      onExit: (_) => setState(() => _hoveringTopBar = false),
      child: _buildTabBarContainer(
        widgetTheme: widgetTheme,
        height: height,
        child: header,
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

    final themeTextColor = getCTabTextColor(
      widgetTheme,
      isSelected,
      enabled,
      resolvedSelectionForeground: resolvedSelectionForeground,
      isHovered: isHovered,
    );
    // The folder's own colours win over the theme's when the application set them.
    final textColor = getForegroundColor(
      foreground: widget.state.foreground,
      defaultColor: themeTextColor,
      context: context,
    );
    final themeBackground = getCTabBackgroundColor(
      widgetTheme,
      isSelected,
      enabled,
      resolvedSelectionBackground: resolvedSelectionBackground,
      isHovered: isHovered,
    );
    final backgroundColor = isSelected
        ? themeBackground
        : (getBackgroundColor(
              background: widget.state.background,
              defaultColor: themeBackground,
              context: context,
            ) ??
            themeBackground);
    final borderColor = getCTabBorderColor(widgetTheme, enabled, isHovered: isHovered);
    final textStyle = getTextStyle(
      context: context,
      font: widget.state.font,
      textColor: textColor,
      baseTextStyle: getCTabTextStyle(widgetTheme, isSelected, enabled),
    );

    final border = Border(
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
    );

    // The tab body gets a semantics node of its own, so the tab's node has a child - and
    // therefore a settled role - from the first frame. A tappable node with no children is a
    // button to Flutter Web, and it turns into a plain container once children turn up; the
    // engine answers that role change by rebuilding the node's DOM element and writing back only
    // the properties that changed with it, silently dropping the identifier the tab is found by.
    return Semantics(identifier: '${tab.vItem.swt}/${tab.vItem.id}', child: Semantics(container: true, child: MouseRegion(
      cursor: enabled ? SystemMouseCursors.click : SystemMouseCursors.basic,
      onEnter: (_) => onHoverEnter(),
      onExit: (_) => onHoverExit(),
      child: GestureDetector(
        onTap: onTap,
        onSecondaryTapDown: (details) =>
            widget.onSecondaryTap?.call(details.globalPosition),
        child: CustomPaint(
          foregroundPainter: _RectangularSides(border),
          child: AnimatedContainer(
          duration: widgetTheme.hoverRevealDuration,
          curve: Curves.easeOut,
          height: double.infinity,
          padding: EdgeInsets.symmetric(
            horizontal: widgetTheme.tabHorizontalPadding,
            vertical: widgetTheme.tabVerticalPadding,
          ).add(border.dimensions),
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
      ),
    )));
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
    final active = showHighlight && (widget.state.highlight ?? false);

    final resolvedSelectionForeground = getForegroundColor(
      foreground: widget.state.selectionForeground,
      defaultColor: widgetTheme.tabSelectedTextColor,
    );
    final resolvedSelectionBackground = getBackgroundColor(
      background: widget.state.selectionBackground,
      defaultColor: widgetTheme.tabSelectedBackgroundColor,
    ) ?? widgetTheme.tabSelectedBackgroundColor;

    final themeTextColor = getCTabTextColor(
      widgetTheme,
      isSelected,
      enabled,
      resolvedSelectionForeground: resolvedSelectionForeground,
      isHovered: isHovered,
    );
    // The folder's own colours win over the theme's when the application set them.
    final textColor = getForegroundColor(
      foreground: widget.state.foreground,
      defaultColor: themeTextColor,
      context: context,
    );
    final themeBackground = getCTabBackgroundColor(
      widgetTheme,
      isSelected,
      enabled,
      resolvedSelectionBackground: resolvedSelectionBackground,
      useDefaultTheme: useDefaultTheme,
      isHovered: isHovered,
    );
    final backgroundColor = isSelected
        ? themeBackground
        : (getBackgroundColor(
              background: widget.state.background,
              defaultColor: themeBackground,
              context: context,
            ) ??
            themeBackground);
    final borderColor = getCTabBorderColor(widgetTheme, enabled, isHovered: isHovered);
    final textStyle = getTextStyle(
      context: context,
      font: widget.state.font,
      textColor: textColor,
      baseTextStyle: getCTabTextStyle(widgetTheme, isSelected, enabled),
    );

    final border = Border(
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
    );

    // The tab body gets a semantics node of its own, so the tab's node has a child - and
    // therefore a settled role - from the first frame. A tappable node with no children is a
    // button to Flutter Web, and it turns into a plain container once children turn up; the
    // engine answers that role change by rebuilding the node's DOM element and writing back only
    // the properties that changed with it, silently dropping the identifier the tab is found by.
    return Semantics(identifier: '${tab.vItem.swt}/${tab.vItem.id}', child: Semantics(container: true, child: MouseRegion(
      cursor: enabled ? SystemMouseCursors.click : SystemMouseCursors.basic,
      onEnter: (_) => onHoverEnter(),
      onExit: (_) => onHoverExit(),
      child: GestureDetector(
        onTap: onTap,
        onSecondaryTapDown: (details) =>
            widget.onSecondaryTap?.call(details.globalPosition),
        child: CustomPaint(
          foregroundPainter: _RectangularSides(
            border,
            ring: getCTabFocusRingSide(
              widgetTheme,
              selected: isSelected,
              enabled: enabled,
              active: active,
            ),
          ),
          child: AnimatedContainer(
          duration: widgetTheme.hoverRevealDuration,
          curve: Curves.easeOut,
          height: double.infinity,
          padding: EdgeInsets.symmetric(
            horizontal: widgetTheme.tabHorizontalPadding,
            vertical: widgetTheme.tabVerticalPadding,
          ).add(border.dimensions),
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
          ),
          child: Row(
            mainAxisSize: MainAxisSize.min,
            children: [
              tab.customContent != null
                  ? TabItemContextProvider(
                      isSelected: isSelected,
                      isEnabled: enabled,
                      isActive: isSelected && enabled && active,
                      isDimmed: isSelected && enabled && showHighlight && !active,
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
                            child: FutureBuilder<Widget?>(
                              future: ImageUtils.buildVImageAsync(
                                VImage()..filename = tab.image,
                                size: widgetTheme.tabIconSize,
                                color: textColor,
                              ),
                              builder: (context, snapshot) {
                                final resolved = snapshot.data;
                                if (resolved != null) return resolved;
                                if (snapshot.connectionState !=
                                    ConnectionState.done) {
                                  return SizedBox(
                                    width: widgetTheme.tabIconSize,
                                    height: widgetTheme.tabIconSize,
                                  );
                                }
                                return Image.file(
                                  File(tab.image!),
                                  width: widgetTheme.tabIconSize,
                                  height: widgetTheme.tabIconSize,
                                  fit: BoxFit.contain,
                                );
                              },
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
      ),
    )));
  }

  Widget _buildChevronButton(CTabFolderThemeExtension widgetTheme) {
    return MouseRegion(
      onEnter: (_) => setState(() => _isChevronHovered = true),
      onExit: (_) => setState(() => _isChevronHovered = false),
      cursor: SystemMouseCursors.click,
      child: Builder(builder: (buttonContext) => GestureDetector(
        onTap: () => _handleChevronTap(buttonContext, widgetTheme),
        child: Container(
          height: double.infinity,
          padding: EdgeInsets.symmetric(
            horizontal: widgetTheme.controlButtonHorizontalPadding,
          ),
          alignment: Alignment.center,
          decoration: BoxDecoration(
            color: _isChevronHovered
                ? widgetTheme.controlButtonHoverColor.withOpacity(0.1)
                : Colors.transparent,
            borderRadius: BorderRadius.circular(4),
          ),
          child: Icon(
            Icons.expand_more,
            size: widgetTheme.controlButtonSize,
            color: widgetTheme.controlButtonColor,
          ),
        ),
      )),
    );
  }

  void _handleChevronTap(BuildContext buttonContext, CTabFolderThemeExtension widgetTheme) {
    widget.onChevronShowList?.call(buttonContext, widgetTheme);
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
        // Re-measure overflow after each frame (a toggled _hasOverflow rebuilds this subtree).
        WidgetsBinding.instance.addPostFrameCallback((_) => _checkOverflow());
        return NotificationListener<SizeChangedLayoutNotification>(
          // The row widens as tab icons load async without rebuilding this ancestor, so
          // re-measure on each size change instead of relying on a one-shot check.
          onNotification: (notification) {
            WidgetsBinding.instance.addPostFrameCallback((_) => _checkOverflow());
            return false;
          },
          child: NotificationListener<ScrollUpdateNotification>(
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
                // A vertical wheel doesn't scroll a horizontal viewport by default; map it
                // to horizontal offset.
                onPointerSignal: (event) {
                  if (event is! PointerScrollEvent) return;
                  if (!_horizontalScrollController.hasClients) return;
                  final position = _horizontalScrollController.position;
                  if (!position.hasContentDimensions ||
                      position.maxScrollExtent <= 0) {
                    return;
                  }
                  final delta = event.scrollDelta.dy != 0
                      ? event.scrollDelta.dy
                      : event.scrollDelta.dx;
                  final target = (position.pixels + delta)
                      .clamp(0.0, position.maxScrollExtent);
                  if (target != position.pixels) {
                    _horizontalScrollController.jumpTo(target);
                    _showScrollbar();
                    _hideScrollbarAfterDelay(widgetTheme.scrollbarHideDelay);
                  }
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
                      child: SizeChangedLayoutNotifier(child: child),
                    ),
                  ),
                ),
              ),
            ),
          ),
        );
      },
    );
  }

  // Surface the chevron once the tab row is wider than its viewport (read from the scroll
  // controller, since web-mode Java can't measure the viewport).
  void _checkOverflow() {
    if (!mounted) return;
    final position = _horizontalScrollController.hasClients
        ? _horizontalScrollController.position
        : null;
    final overflow = position != null &&
        position.hasContentDimensions &&
        position.maxScrollExtent > 0.5;
    if (overflow != _hasOverflow) {
      setState(() => _hasOverflow = overflow);
    }
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
      // The band the tabs sit on is the folder's own ground: an application that coloured the
      // CTabFolder expects it here, not just behind the selected tab.
      color: getBackgroundColor(
            background: widget.state.background,
            defaultColor: widgetTheme.tabBarBackgroundColor,
            context: context,
          ) ??
          widgetTheme.tabBarBackgroundColor,
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
    // Show the chevron on overflow, but honour the folder's chevronVisible permission flag
    // (default true) -- matching native SWT's showChevron = chevronVisible && overflow.
    final chevronAllowed = widget.state.chevronVisible ?? true;
    final showChevron =
        chevronAllowed && ((widget.state.showChevron ?? false) || _hasOverflow);
    final hasControls =
        topRightComposite != null || showMinimizeButton || showMaximizeButton ||
        showChevron;
    if (!hasControls) return const SizedBox.shrink();

    final controls = Row(
      mainAxisSize: MainAxisSize.min,
      crossAxisAlignment: CrossAxisAlignment.stretch,
      children: [
        if (showChevron)
          _buildChevronButton(widgetTheme),
        if (topRightComposite != null)
          _buildTopRightComposite(
            topRightComposite,
            backgroundColor: widgetTheme.tabBarBackgroundColor,
          ),
        if (showMinimizeButton)
          Builder(
            builder: (context) {
              final isVisible = _controlsAlwaysVisible ? true : _hoveringTopBar;
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
              final isVisible = _controlsAlwaysVisible ? true : _hoveringTopBar;
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

    final isControlsVisible = _controlsAlwaysVisible ? true : _hoveringTopBar;

    final revealWidget = _HoverReveal(
      visible: isControlsVisible,
      revealDuration: widgetTheme.hoverRevealDuration,
      hideDuration: widgetTheme.hoverHideDuration,
      child: Align(
        alignment: Alignment.topRight,
        // Opacity alone doesn't stop hit-testing: while faded out (not hovering)
        // this box still sat on top of the Stack and swallowed clicks meant for
        // a tab hidden underneath it. Only block pointers while genuinely hidden.
        child: IgnorePointer(
          ignoring: !isControlsVisible,
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
      ),
    );

    final scaledWidget = Transform.scale(
      scale: widgetTheme.controlButtonScale,
      alignment: Alignment.topRight,
      child: revealWidget,
    );

    if (alignment == SWT.CENTER) {
      return Expanded(child: Center(child: scaledWidget));
    } else {
      return scaledWidget;
    }
  }
}

/// Where a dragged tab would land, drawn on the folder that would receive it.
///
/// Caret heads, not a bare rule: the mark sits in the gap between two tabs, and a plain line there
/// reads as a tab border as easily as an insertion point. The heads overhang the row, so the width
/// is wider than the bar and the marker is positioned by its bar rather than by its box.
class CTabInsertionMarker extends StatelessWidget {
  const CTabInsertionMarker({super.key, required this.color});

  final Color color;

  static const double barWidth = 3;
  static const double caretWidth = 4;
  static const double caretHeight = 4;

  /// How far left of the insertion point the widget starts, its bar being what must land there.
  static const double overhang = caretWidth + barWidth / 2;

  @override
  Widget build(BuildContext context) => SizedBox(
        width: caretWidth * 2 + barWidth,
        child: CustomPaint(painter: _CTabInsertionMarkerPainter(color)),
      );
}

class _CTabInsertionMarkerPainter extends CustomPainter {
  const _CTabInsertionMarkerPainter(this.color);

  final Color color;

  @override
  void paint(Canvas canvas, Size size) {
    final paint = Paint()..color = color;
    final centre = size.width / 2;
    final half = CTabInsertionMarker.barWidth / 2;
    canvas.drawRect(
      Rect.fromLTRB(centre - half, 0, centre + half, size.height),
      paint,
    );
    const caret = CTabInsertionMarker.caretWidth;
    const height = CTabInsertionMarker.caretHeight;
    canvas.drawPath(
      Path()
        ..moveTo(centre - caret - half, 0)
        ..lineTo(centre + caret + half, 0)
        ..lineTo(centre, height)
        ..close(),
      paint,
    );
    canvas.drawPath(
      Path()
        ..moveTo(centre - caret - half, size.height)
        ..lineTo(centre + caret + half, size.height)
        ..lineTo(centre, size.height - height)
        ..close(),
      paint,
    );
  }

  @override
  bool shouldRepaint(_CTabInsertionMarkerPainter oldDelegate) =>
      oldDelegate.color != color;
}

/// A CTabFolder's tab row. Every tab is a Flutter [Draggable] carrying a [DndDragPayload] and
/// the row itself is a drop target, so a tab dropped on a *different* folder's row resolves
/// there and reaches Java as a real `DND.Drop`.
///
/// A folder that has an application `DropTarget` hands the drop to it and does not reorder
/// itself: native CTabFolder has no built-in reordering, so running both would move the tab
/// twice and diverge from the native backend.
class TabDragRow extends StatefulWidget {
  final List<Widget> children;
  final List<CTabItem> tabs;
  final VCTabFolder folderState;
  final CTabFolderSwt folderWidget;
  final double? tabHeight;

  /// Wraps the tab row in the folder's scroll view. The drop target goes *outside* it, so the
  /// whole tab strip accepts a drop and not just the width the tabs happen to occupy —
  /// otherwise a tab released over a folder's empty strip resolves to no target at all.
  final Widget Function(Widget row) rowWrapper;
  final void Function(int from, int to)? onReorder;
  final void Function(int insertIndex, int? targetItemId, Offset position)? onDrop;
  final ValueChanged<int>? onTabDragStarted;
  final VoidCallback? onDragStart;

  const TabDragRow({
    super.key,
    required this.children,
    required this.tabs,
    required this.folderState,
    required this.folderWidget,
    required this.rowWrapper,
    this.tabHeight,
    this.onReorder,
    this.onDrop,
    this.onTabDragStarted,
    this.onDragStart,
  });

  @override
  State<TabDragRow> createState() => _TabDragRowState();
}

class _TabDragRowState extends State<TabDragRow> {
  final GlobalKey _rowKey = GlobalKey();
  final List<GlobalKey> _itemKeys = [];
  final HoverTracker<int> _insertHover = HoverTracker<int>();

  void _ensureItemKeys(int count) {
    while (_itemKeys.length < count) {
      _itemKeys.add(GlobalKey());
    }
  }

  RenderBox? get _row => _rowKey.currentContext?.findRenderObject() as RenderBox?;

  RenderBox? _itemBox(int index) => index < _itemKeys.length
      ? _itemKeys[index].currentContext?.findRenderObject() as RenderBox?
      : null;

  double? _itemLeft(RenderBox row, int index) {
    final box = _itemBox(index);
    if (box == null) return null;
    return row.globalToLocal(box.localToGlobal(Offset.zero)).dx;
  }

  /// The gap the pointer sits at, in `[0, tab count]`.
  int _insertIndexAt(Offset globalPosition) {
    final count = widget.children.length;
    final row = _row;
    if (row == null) return count;
    final x = row.globalToLocal(globalPosition).dx;
    for (int i = 0; i < count; i++) {
      final box = _itemBox(i);
      final left = _itemLeft(row, i);
      if (box == null || left == null) continue;
      if (x < left + box.size.width / 2) return i;
    }
    return count;
  }

  /// The tab under the pointer, or null past the last one.
  int? _itemIdAt(Offset globalPosition) {
    final row = _row;
    if (row == null) return null;
    final x = row.globalToLocal(globalPosition).dx;
    final count = math.min(widget.children.length, widget.tabs.length);
    for (int i = 0; i < count; i++) {
      final box = _itemBox(i);
      final left = _itemLeft(row, i);
      if (box == null || left == null) continue;
      if (x >= left && x < left + box.size.width) return widget.tabs[i].vItem.id;
    }
    return null;
  }

  void _handleDrop(DndDragPayload payload, int? index, int? itemId, Offset position) {
    _insertHover.update(null);
    // An application that answered DragDetect by opening a Tracker owns this gesture -- the Eclipse
    // workbench moves the whole view that way. Reordering here as well would move the tab twice.
    if (TrackerSession.isTracking) return;
    final count = widget.children.length;
    final insertIndex = (index ?? count).clamp(0, count);
    if (widget.folderState.dropTargetId != null) {
      widget.onDrop?.call(insertIndex, (itemId ?? 0) != 0 ? itemId : null, position);
      return;
    }
    if (payload.sourceControlId != widget.folderState.id) return;
    final from = payload.index;
    if (from == null || from >= count) return;
    final to = insertIndex > from ? insertIndex - 1 : insertIndex;
    if (to != from) widget.onReorder?.call(from, to);
  }

  Widget _draggableTab(int index) {
    return wrapDraggable<DndDragPayload>(
      child: widget.children[index],
      data: DndDragPayload(
        sourceControlId: widget.folderState.id,
        index: index,
        itemId: index < widget.tabs.length ? widget.tabs[index].vItem.id : null,
      ),
      widget: widget.folderWidget,
      state: widget.folderState,
      alwaysDraggable: true,
      // The workbench reads DragDetect's x/y to work out which tab is being dragged, so the event
      // has to carry the tab's own position rather than an empty one. The tab strip starts at the
      // folder's own origin, so the row's coordinates are the folder's for this purpose.
      dragDetectEvent: () {
        final box = _itemBox(index);
        final row = _row;
        final centre = (box != null && row != null)
            ? row.globalToLocal(box.localToGlobal(box.size.center(Offset.zero)))
            : Offset.zero;
        return VEvent()
          ..x = centre.dx.round()
          ..y = centre.dy.round();
      },
      onDragStarted: () {
        widget.onDragStart?.call();
        widget.onTabDragStarted?.call(index);
      },
      // Built lazily: the drag feedback renders in the Overlay, which gives it unbounded
      // height, while a tab's own container asks for `double.infinity`. The measurement is
      // only available once the row has laid out, which is the case by the time a drag starts.
      feedbackBuilder: (child) => Builder(builder: (_) {
        final size = _itemBox(index)?.size;
        return Material(
          elevation: 4,
          child: SizedBox(
            width: size?.width,
            height: size?.height ?? widget.tabHeight,
            child: Opacity(opacity: 0.85, child: child),
          ),
        );
      }),
      childWhenDraggingBuilder: (child) => Opacity(opacity: 0.3, child: child),
    );
  }

  Widget _insertionMarker(int index, Color color) {
    double left = 0;
    final row = _row;
    if (row != null && widget.children.isNotEmpty) {
      if (index < widget.children.length) {
        left = _itemLeft(row, index) ?? 0;
      } else {
        final last = widget.children.length - 1;
        final lastLeft = _itemLeft(row, last);
        final box = _itemBox(last);
        if (lastLeft != null && box != null) left = lastLeft + box.size.width;
      }
    }
    // Animated so the mark slides between the gaps it can occupy: dragging along a row of tabs
    // otherwise reads as the marker blinking out and back rather than as one thing moving.
    return AnimatedPositioned(
      key: _insertionMarkerKey,
      duration: const Duration(milliseconds: 90),
      curve: Curves.easeOut,
      left: left - CTabInsertionMarker.overhang,
      top: 0,
      bottom: 0,
      child: CTabInsertionMarker(color: color),
    );
  }

  static const Key _insertionMarkerKey = ValueKey('ctab-insertion-marker');

  @override
  Widget build(BuildContext context) {
    final count = widget.children.length;
    _ensureItemKeys(count);

    // mainAxisSize.min so the row takes its natural width and the scroll view overflows when
    // tabs don't fit; the default (max) fills and clips instead.
    final row = Row(
      key: _rowKey,
      mainAxisSize: MainAxisSize.min,
      children: List.generate(
        count,
        (i) => KeyedSubtree(key: _itemKeys[i], child: _draggableTab(i)),
      ),
    );

    return wrapDropTarget<DndDragPayload>(
      state: widget.folderState,
      alwaysAccepts: true,
      // The insertion mark is drawn from the resolved index, and the folders that most need it —
      // the Eclipse workbench's view stacks — have no application DropTarget: the workbench moves
      // a view with a Tracker rather than through DND.
      alwaysResolves: true,
      resolveIndex: (details) {
        final index = _insertIndexAt(details.offset);
        _insertHover.update(index);
        return index;
      },
      resolveItemId: (details) => _itemIdAt(details.offset) ?? 0,
      resolvePosition: (details) =>
          _row?.globalToLocal(details.offset) ?? details.offset,
      onDrop: _handleDrop,
      builder: (context, child, negotiation, isHovering) {
        if (!isHovering) _insertHover.update(null);
        return child;
      },
      child: widget.rowWrapper(
        // A Row with no children is zero-high, which would stop the strip hit-testing and leave
        // a folder that lost its last tab unable to be given one back. Hold the strip's height.
        ConstrainedBox(
          constraints: BoxConstraints(minHeight: widget.tabHeight ?? 0),
          child: ValueListenableBuilder<int?>(
            valueListenable: _insertHover.notifier,
            builder: (context, index, child) {
              final marker = Theme.of(context)
                  .extension<CTabFolderThemeExtension>()!
                  .tabHighlightColor;
              return Stack(
                clipBehavior: Clip.none,
                children: [
                  // Tinted while this folder is the one under the pointer: with several folders
                  // on screen the marker alone says where in a row the tab lands, not which row.
                  AnimatedContainer(
                    duration: const Duration(milliseconds: 90),
                    color: index == null
                        ? null
                        : marker.withValues(alpha: _receivingStripTint),
                    child: child,
                  ),
                  if (index != null) _insertionMarker(index, marker),
                ],
              );
            },
            child: row,
          ),
        ),
      ),
    );
  }

  /// Enough to read as "this row", not enough to fight the tabs drawn on it.
  static const double _receivingStripTint = 0.12;
}

class CTabItem {
  final String label;
  final String? image;
  final bool showCloseButton;
  final VoidCallback? onClose;
  final bool alignRight;
  final Widget? customContent;
  final String? toolTipText;
  final VCTabItem vItem;

  CTabItem({
    required this.label,
    this.image,
    this.showCloseButton = false,
    this.onClose,
    this.alignRight = false,
    this.customContent,
    this.toolTipText,
    required this.vItem,
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
  // The selected tab of the folder that holds focus.
  final bool isActive;
  // The selected tab of a folder that takes part in activation but does not hold focus.
  final bool isDimmed;

  TabItemContext({
    required this.isSelected,
    required this.isEnabled,
    this.isActive = false,
    this.isDimmed = false,
  });

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
    bool isActive = false,
    bool isDimmed = false,
    required Widget child,
  }) : context = TabItemContext(
         isSelected: isSelected,
         isEnabled: isEnabled,
         isActive: isActive,
         isDimmed: isDimmed,
       ),
       super(key: key, child: child);

  @override
  bool updateShouldNotify(TabItemContextProvider oldWidget) {
    return context.isSelected != oldWidget.context.isSelected ||
        context.isEnabled != oldWidget.context.isEnabled ||
        context.isActive != oldWidget.context.isActive ||
        context.isDimmed != oldWidget.context.isDimmed;
  }
}

/// The tab's sides, drawn as straight strips the way Flutter's release build draws a
/// non-uniform Border under a rounded corner (it ignores the radius for the sides); the debug
/// build refuses that combination in a BoxDecoration, so the sides are painted here instead.
class _RectangularSides extends CustomPainter {
  final Border border;
  // Painted after the sides, which would otherwise cover it.
  final BorderSide? ring;

  const _RectangularSides(this.border, {this.ring});

  @override
  void paint(Canvas canvas, Size size) {
    paintBorder(
      canvas,
      Offset.zero & size,
      top: border.top,
      right: border.right,
      bottom: border.bottom,
      left: border.left,
    );
    final ring = this.ring;
    if (ring != null) {
      paintBorder(canvas, Offset.zero & size, top: ring, right: ring, bottom: ring, left: ring);
    }
  }

  @override
  bool shouldRepaint(_RectangularSides oldDelegate) =>
      oldDelegate.border != border || oldDelegate.ring != ring;
}
