import 'dart:async';

import 'package:flutter/gestures.dart';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import '../theme/theme_settings/tabfolder_theme_settings.dart';
import 'package:flutter/rendering.dart';
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
import 'utils/double_tap_detector.dart';
import 'utils/focus_report.dart';
import 'utils/widget_utils.dart';
import 'widget_config.dart';

class TabFolderImpl<T extends TabFolderSwt, V extends VTabFolder>
    extends CompositeImpl<T, V> {
  late int _selectedIndex;
  int? _focusedTab;
  final DoubleTapDetector _tabTap = DoubleTapDetector();

  final FocusReport _focusReport = FocusReport();
  final List<FocusNode> _tabNodes = [];

  /// The tab a primary press landed on, until the folder's own press handling reads it.
  int? _pressedTab;

  FocusNode _tabNode(int index) {
    while (_tabNodes.length <= index) {
      _tabNodes.add(FocusNode(debugLabel: 'TabItem'));
    }
    return _tabNodes[index];
  }

  /// A press that switches tabs focuses the folder, as on GTK: the pressed tab takes the client's
  /// focus, so keys and Tab start from it. A press on the strip or on the selected tab does not.
  @override
  void takeFocusOnPress() {
    final index = _pressedTab;
    _pressedTab = null;
    if (index == null || index == _selectedIndex || state.enabled != true) return;
    _tabNode(index).requestFocus();
    _focusReport.update(true, focusIn: () => widget.sendFocusFocusIn(state, null), focusOut: () {});
  }

  @override
  void dispose() {
    for (final node in _tabNodes) {
      node.dispose();
    }
    super.dispose();
  }

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

    final hasValidBounds = hasBounds(state.bounds);
    final constraints = getConstraintsFromBounds(state.bounds);

    // A folder with no tabs is still a control in its bounds: the pointer still enters it.
    if (tabItems.isEmpty) {
      return SwtControlScope(
          swt: state.swt,
          child: tagSemantics(trackPointer(hasValidBounds
              ? ConstrainedBox(constraints: constraints!, child: const SizedBox.expand())
              : const SizedBox.shrink())));
    }

    final tabContent = _buildTabContent(tabBodies);

    Widget content = Column(
      mainAxisSize: hasValidBounds ? MainAxisSize.max : MainAxisSize.min,
      children: [
        buildTabBar(context, widgetTheme, tabItems),
        if (hasValidBounds) Expanded(child: tabContent) else tabContent,
      ],
    );

    // SWT reads enablement down the parent chain - `isEnabled()` is this control's own flag and
    // every ancestor's - so a disabled folder disables the pages it holds: trackPointer's gate
    // covers them.
    if (hasValidBounds) {
      return SwtControlScope(
          swt: state.swt,
          child: tagSemantics(trackPointer(ParentForegroundScope(
            foreground: state.foreground,
            font: state.font,
            child: ConstrainedBox(constraints: constraints!, child: content),
          ))));
    }

    // The tabs letter and colour in the folder's own font/foreground: VTabItem has neither.
    return SwtControlScope(
        swt: state.swt,
        child: tagSemantics(trackPointer(ParentForegroundScope(
          foreground: state.foreground,
          font: state.font,
          child: content,
        ))));
  }

  Widget _buildTabContent(List<Widget> tabBodies) {
    return IndexedStack(
      index: _selectedIndex < tabBodies.length ? _selectedIndex : 0,
      children: tabBodies,
    );
  }

  Widget buildTabBar(
    BuildContext context,
    TabFolderThemeExtension widgetTheme,
    List<TabItem> tabItems,
  ) {
    final hasValidBounds = hasBounds(state.bounds);

    return Container(
      width: hasValidBounds ? double.infinity : null,
      decoration: BoxDecoration(
        // The band the tabs sit on is the folder's own ground.
        color: getBackgroundColor(
              background: state.background,
              defaultColor: widgetTheme.tabBarBackgroundColor,
              context: context,
            ) ??
            widgetTheme.tabBarBackgroundColor,
        border: Border(
          bottom: BorderSide(
            color: widgetTheme.tabBarBorderColor,
            width: widgetTheme.tabBorderWidth,
          ),
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
              vItem: state.items![index],
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
    required VTabItem vItem,
    required VoidCallback onTap,
    required int index,
  }) {
    final enabled = state.enabled ?? false;

    // Determine colors based on enabled state
    final backgroundColor = !enabled
        ? widgetTheme.tabDisabledBackgroundColor
        : (isSelected
              ? widgetTheme.tabSelectedBackgroundColor
              : widgetTheme.tabBackgroundColor);
    final borderColor = !enabled
        ? widgetTheme.tabDisabledBorderColor
        : widgetTheme.tabBorderColor;
    final bottomBorderColor = !enabled
        ? widgetTheme.tabDisabledBorderColor
        : (isSelected
              ? widgetTheme.tabSelectedBorderColor
              : widgetTheme.tabBorderColor);
    final textColor = !enabled
        ? widgetTheme.tabDisabledTextColor
        : (isSelected
              ? widgetTheme.tabSelectedTextColor
              : widgetTheme.tabTextColor);

    return tagItemSemantics(vItem, MouseRegion(
      cursor: enabled ? SystemMouseCursors.click : SystemMouseCursors.basic,
      child: Listener(
        onPointerDown: (e) {
          if (e.buttons == kPrimaryButton) _pressedTab = index;
        },
        child: Material(
        color: Colors.transparent,
        child: InkWell(
          focusNode: _tabNode(index),
          onTap: enabled ? () => _handleTabTap(index, onTap) : null,
          onFocusChange: (focused) => _onTabFocusChange(index, focused),
          child: _Overhang(
            bottom: isSelected && enabled ? widgetTheme.tabSelectedBorderWidth : 0,
            child: Container(
            padding: EdgeInsets.symmetric(horizontal: widgetTheme.tabPadding),
            constraints: const BoxConstraints(minHeight: 0),
            foregroundDecoration: getTabFocusRingDecoration(widgetTheme, focused: _focusedTab == index, enabled: enabled),
            decoration: BoxDecoration(
              color: backgroundColor,
              border: Border(
                right: BorderSide(
                  color: borderColor,
                  width: widgetTheme.tabBorderWidth,
                ),
                bottom: isSelected && enabled
                    ? BorderSide(
                        color: bottomBorderColor,
                        width: widgetTheme.tabSelectedBorderWidth,
                      )
                    : BorderSide(
                        color: borderColor,
                        width: widgetTheme.tabBorderWidth,
                      ),
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
                          ? (widgetTheme.tabSelectedTextStyle ??
                                    const TextStyle())
                                .copyWith(color: textColor)
                          : (widgetTheme.tabTextStyle ?? const TextStyle())
                                .copyWith(color: textColor),
                    ),
            ),
          ),
          ),
        ),
      ),
      ),
    ));
  }

  /// The folder holds the focus while any of its tabs does: Tab moving onto a tab focuses it, and
  /// only the focus leaving every tab is a FocusOut -- moving between tabs is neither.
  void _onTabFocusChange(int index, bool focused) {
    // SWT moves the focus between controls by Tab only; an arrow key's directional focus walking
    // into the strip from outside the folder is undone, as natively the arrows stay where they were.
    if (focused && !_focusReport.reported && _arrowKeyDown()) {
      FocusManager.instance.primaryFocus?.unfocus(disposition: UnfocusDisposition.previouslyFocusedChild);
      return;
    }
    setState(() {
      if (focused) {
        _focusedTab = index;
      } else if (_focusedTab == index) {
        _focusedTab = null;
      }
    });
    if (focused) {
      _focusReport.update(true, focusIn: () => widget.sendFocusFocusIn(state, null), focusOut: () {});
    } else {
      // Flutter takes the focus off one tab before it gives it to the next.
      scheduleMicrotask(() {
        if (!mounted || _focusedTab != null) return;
        _focusReport.update(false, focusIn: () {}, focusOut: () => widget.sendFocusFocusOut(state, null));
      });
    }
  }

  static bool _arrowKeyDown() => HardwareKeyboard.instance.logicalKeysPressed.any((k) =>
      k == LogicalKeyboardKey.arrowLeft || k == LogicalKeyboardKey.arrowRight
      || k == LogicalKeyboardKey.arrowUp || k == LogicalKeyboardKey.arrowDown);

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

  void _handleTabTap(int index, VoidCallback onSingle) {
    if (_tabTap.registerTap(key: index) == 2) {
      _handleDefaultSelection(index);
      return;
    }
    onSingle();
  }

  void _handleDefaultSelection(int index) {
    if (state.enabled != true) return;
    _updateSelection(index);
    var e = VEvent()..index = index;
    widget.sendSelectionDefaultSelection(state, e);
  }

  List<TabItem> getTabItems() {
    return state.items
            ?.map((tabItem) => getWidgetForTabItem(tabItem))
            .toList() ??
        [];
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

  TabItem({required this.label, this.customContent, this.toolTipText});
}

/// Lays its child out at full height and reports [bottom] less, so the child hangs over whatever
/// is laid out below it: the selected tab's bottom border covering the bar's border line. A
/// negative margin does the same and is refused in debug.
class _Overhang extends SingleChildRenderObjectWidget {
  final double bottom;

  const _Overhang({required this.bottom, required super.child});

  @override
  RenderObject createRenderObject(BuildContext context) => _RenderOverhang(bottom);

  @override
  void updateRenderObject(BuildContext context, _RenderOverhang renderObject) {
    renderObject.bottom = bottom;
  }
}

class _RenderOverhang extends RenderShiftedBox {
  _RenderOverhang(this._bottom) : super(null);

  double _bottom;
  double get bottom => _bottom;
  set bottom(double value) {
    if (value == _bottom) return;
    _bottom = value;
    markNeedsLayout();
  }

  @override
  void performLayout() {
    final child = this.child;
    if (child == null) {
      size = constraints.smallest;
      return;
    }
    final inner = constraints.copyWith(
      minHeight: 0,
      maxHeight: constraints.maxHeight.isFinite ? constraints.maxHeight + _bottom : null,
    );
    child.layout(inner, parentUsesSize: true);
    (child.parentData as BoxParentData).offset = Offset.zero;
    size = constraints.constrain(Size(child.size.width, child.size.height - _bottom));
  }
}
