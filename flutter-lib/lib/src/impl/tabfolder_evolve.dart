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
  /// The widget holding the tabs, one keyed subtree per tab: where measure_tabfolder reads a tab.
  static const tabRow = '_TabRow';

  /// The tab Java holds selected, or -1 when it holds none: no tab is highlighted and no page shown.
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

  /// A primary press on a tab moves the client's focus to that tab, so keys and Tab start from it,
  /// as GTK focuses the folder (Java decides the SWT focus: ControlHelper.takesFocusOnPress), and
  /// selects the tab. A press on the strip past the tabs does neither.
  @override
  void takeFocusOnPress() {
    final index = _pressedTab;
    _pressedTab = null;
    if (index == null || state.enabled != true) return;
    _tabNode(index).requestFocus();
    _focusReport.update(true, focusIn: () => widget.sendFocusFocusIn(state, null), focusOut: () {});
    _selectPressedTab(index);
  }

  /// GTK and Win32 switch tabs on the press, ahead of the release, and report it with the primary
  /// button held: the mask a move with that button down carries.
  void _selectPressedTab(int index) {
    if (index != _selectedIndex) _handleTabSelection(index, stateMask: moveStateMask(kPrimaryButton));
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
    if (_selectedIndex >= 0) {
      var e = VEvent()..index = _selectedIndex;
      widget.sendSelectionSelection(state, e);
    }
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

  /// Java selects the first tab a folder gets, so a folder with tabs and no selection is one whose
  /// selection was cleared.
  int _getSelectedIndexFromState() {
    if (state.selection == null || state.selection!.isEmpty) {
      return -1;
    }

    final selectedItem = state.selection!.first;
    final items = state.items ?? [];
    return items.indexWhere((item) => item.id == selectedItem.id);
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

    final bar = buildTabBar(context, widgetTheme, tabItems);
    final body = hasValidBounds ? Expanded(child: tabContent) : tabContent;
    Widget content = Column(
      mainAxisSize: hasValidBounds ? MainAxisSize.max : MainAxisSize.min,
      children: _tabsAtBottom ? [body, bar] : [bar, body],
    );

    // SWT reads enablement down the parent chain - `isEnabled()` is this control's own flag and
    // every ancestor's - so a disabled folder disables the pages it holds: trackPointer's gate
    // covers them.
    if (hasValidBounds) {
      return inOwnLayer(SwtControlScope(
          swt: state.swt,
          child: tagSemantics(trackPointer(ParentForegroundScope(
            foreground: state.foreground,
            font: state.font,
            child: ConstrainedBox(constraints: constraints!, child: content),
          )))));
    }

    // The tabs letter and colour in the folder's own font/foreground: VTabItem has neither.
    return inOwnLayer(SwtControlScope(
        swt: state.swt,
        child: tagSemantics(trackPointer(ParentForegroundScope(
          foreground: state.foreground,
          font: state.font,
          child: content,
        )))));
  }

  /// SWT.BOTTOM: the strip sits below the page, with its line and the selected tab's accent on its
  /// top edge, next to the page, as a TOP folder has them on its bottom edge.
  bool get _tabsAtBottom => hasStyle(state.style, SWT.BOTTOM);

  Widget _buildTabContent(List<Widget> tabBodies) {
    return IndexedStack(
      index: _selectedIndex >= 0 && _selectedIndex < tabBodies.length ? _selectedIndex : null,
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
        border: _edgeBorder(BorderSide(
          color: widgetTheme.tabBarBorderColor,
          width: widgetTheme.tabBorderWidth,
        )),
      ),
      child: _TabRow(
        child: Row(
          mainAxisSize: hasValidBounds ? MainAxisSize.max : MainAxisSize.min,
          mainAxisAlignment: MainAxisAlignment.start,
          children: [
            ...tabItems.asMap().entries.map((entry) {
              final int index = entry.key;
              final TabItem tab = entry.value;
              final vItem = state.items![index];

              return KeyedSubtree(
                key: ValueKey(vItem.id),
                child: _buildTab(
                  context: context,
                  widgetTheme: widgetTheme,
                  isSelected: index == _selectedIndex,
                  tab: tab,
                  vItem: vItem,
                  index: index,
                ),
              );
            }).toList(),
          ],
        ),
      ),
    );
  }

  Widget _buildTab({
    required BuildContext context,
    required TabFolderThemeExtension widgetTheme,
    required bool isSelected,
    required TabItem tab,
    required VTabItem vItem,
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
          onTap: enabled ? () => _handleTabTap(index) : null,
          onFocusChange: (focused) => _onTabFocusChange(index, focused),
          child: _Overhang(
            amount: isSelected && enabled ? widgetTheme.tabSelectedBorderWidth : 0,
            upward: _tabsAtBottom,
            child: Container(
            padding: EdgeInsets.symmetric(horizontal: widgetTheme.tabPadding),
            constraints: const BoxConstraints(minHeight: 0),
            foregroundDecoration: getTabFocusRingDecoration(widgetTheme, focused: _focusedTab == index, enabled: enabled),
            decoration: BoxDecoration(
              color: backgroundColor,
              border: _edgeBorder(
                isSelected && enabled
                    ? BorderSide(
                        color: bottomBorderColor,
                        width: widgetTheme.tabSelectedBorderWidth,
                      )
                    : BorderSide(
                        color: borderColor,
                        width: widgetTheme.tabBorderWidth,
                      ),
                right: BorderSide(
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

  /// [edge] on the side the page is on: the bottom of the strip, or the top when the tabs are at the bottom.
  Border _edgeBorder(BorderSide edge, {BorderSide right = BorderSide.none}) =>
      _tabsAtBottom
          ? Border(top: edge, right: right)
          : Border(bottom: edge, right: right);

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

  void _handleTabSelection(int index, {int stateMask = 0}) {
    if (state.enabled != true) return;
    _updateSelection(index);
    var e = VEvent()
      ..index = index
      ..stateMask = stateMask;
    widget.sendSelectionSelection(state, e);
  }

  /// A pointer press has already selected the tab; a tap with no press before it (the keyboard, or
  /// an accessibility action) selects it here. A second click on the tab is the folder's
  /// DefaultSelection.
  void _handleTabTap(int index) {
    if (_tabTap.registerTap(key: index) == 2) {
      _handleDefaultSelection(index);
      return;
    }
    if (index != _selectedIndex) _handleTabSelection(index);
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

/// Marks the row of tabs for the measurement; lays out as its child.
class _TabRow extends StatelessWidget {
  const _TabRow({required this.child});

  final Widget child;

  @override
  Widget build(BuildContext context) => child;
}

class TabItem {
  final String label;
  final Widget? customContent;
  final String? toolTipText;

  TabItem({required this.label, this.customContent, this.toolTipText});
}

/// Lays its child out at full height and reports [amount] less, so the child hangs over the strip's
/// line next to the page: below it, or above it when [upward]. A negative margin does the same and
/// is refused in debug.
class _Overhang extends SingleChildRenderObjectWidget {
  final double amount;
  final bool upward;

  const _Overhang({required this.amount, required this.upward, required super.child});

  @override
  RenderObject createRenderObject(BuildContext context) => _RenderOverhang(amount, upward);

  @override
  void updateRenderObject(BuildContext context, _RenderOverhang renderObject) {
    renderObject
      ..amount = amount
      ..upward = upward;
  }
}

class _RenderOverhang extends RenderShiftedBox {
  _RenderOverhang(this._amount, this._upward) : super(null);

  double _amount;
  double get amount => _amount;
  set amount(double value) {
    if (value == _amount) return;
    _amount = value;
    markNeedsLayout();
  }

  bool _upward;
  bool get upward => _upward;
  set upward(bool value) {
    if (value == _upward) return;
    _upward = value;
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
      maxHeight: constraints.maxHeight.isFinite ? constraints.maxHeight + _amount : null,
    );
    child.layout(inner, parentUsesSize: true);
    (child.parentData as BoxParentData).offset = Offset(0, _upward ? -_amount : 0);
    size = constraints.constrain(Size(child.size.width, child.size.height - _amount));
  }
}
