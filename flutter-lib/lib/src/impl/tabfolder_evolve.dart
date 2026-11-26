import 'dart:async';

import 'package:flutter/foundation.dart';
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
import 'utils/strip_scroll.dart';
import 'utils/widget_utils.dart';
import 'widget_config.dart';
import 'utils/test_anchors.dart';
import 'menu_evolve.dart' show OwnPressListener;

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

  /// A strip narrower than its tabs scrolls, keeps the selected tab in view and shows a pair of
  /// arrows at its end, as Win32's tab control does.
  final ScrollController _strip = ScrollController();
  final GlobalKey _stripView = GlobalKey();
  bool _overflow = false;
  double? _stripHeight;
  late final StripReveal _reveal =
      StripReveal(strip: _strip, selectedTab: () => _tabBox(_selectedIndex), mounted: () => mounted);

  /// The strip last reported to Java as "scroll,viewport", or null while the tabs fit.
  String? _reportedStrip;

  /// The row of tabs the folder last built, handed to [_TabRow] so a rebuilt folder rebuilds the
  /// row and not the scroll view around it.
  final ValueNotifier<Widget> _tabs = ValueNotifier(const SizedBox.shrink());
  late final Widget _tabRow = _TabRow(tabs: _tabs);
  Widget? _stripShell;
  Object? _stripShellInputs;

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
    _strip.dispose();
    _tabs.dispose();
    super.dispose();
  }

  @override
  void initState() {
    super.initState();
    _selectedIndex = _getSelectedIndexFromState();
    _strip.addListener(_reportStrip);
    _reveal.schedule();

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
      _reveal.schedule();
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
    if (hidden) return const SizedBox.shrink();
    final widgetTheme = Theme.of(context).extension<TabFolderThemeExtension>()!;
    final tabItems = getTabItems();
    final tabBodies = getTabBodies();

    final hasValidBounds = hasBounds(state.bounds);
    final constraints = getConstraintsFromBounds(state.bounds);

    // A folder with no tabs is still a control in its bounds: the pointer still enters it.
    if (tabItems.isEmpty) {
      return SwtControlScope(
          swt: state.swt,
          child: tagSemantics(state, trackPointer(hasValidBounds
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
          child: tagSemantics(state, trackPointer(ParentForegroundScope(
            foreground: state.foreground,
            font: state.font,
            child: ConstrainedBox(constraints: constraints!, child: content),
          )))));
    }

    // The tabs letter and colour in the folder's own font/foreground: VTabItem has neither.
    return inOwnLayer(SwtControlScope(
        swt: state.swt,
        child: tagSemantics(state, trackPointer(ParentForegroundScope(
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
    _tabs.value = Row(
      mainAxisSize: MainAxisSize.min,
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
    );

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
      child: hasValidBounds ? _scrollingStrip(context, widgetTheme) : _tabRow,
    );
  }

  /// The tabs in a horizontal viewport, with the arrows after it while they overflow. The viewport
  /// clips only sideways: the selected tab hangs over the strip's line. Built again only when the
  /// arrows change: a tab change rebuilds the row of tabs alone.
  Widget _scrollingStrip(BuildContext context, TabFolderThemeExtension widgetTheme) {
    final arrows = _overflow && _stripHeight != null;
    final behavior = ScrollConfiguration.of(context);
    final inputs = (widgetTheme, behavior, arrows ? _stripHeight : null, arrows ? state.enabled : null);
    if (_stripShell == null || inputs != _stripShellInputs) {
      _stripShellInputs = inputs;
      _stripShell = _buildStripShell(widgetTheme, behavior, arrows);
    }
    return _stripShell!;
  }

  Widget _buildStripShell(TabFolderThemeExtension widgetTheme, ScrollBehavior behavior, bool arrows) {
    // Keyed so the scroll view keeps its position when the arrows come or go.
    final viewport = Listener(
      key: _stripView,
      onPointerSignal: (event) => wheelStrip(_strip, event),
      child: ClipRect(
        clipper: const _SidewaysClip(),
        // The web maps the arrow keys to ScrollIntent; on a tab they move the selection only.
        child: Actions(
          actions: {ScrollIntent: DoNothingAction(consumesKey: false)},
          // A tab strip neither stretches nor shows a scrollbar, on any platform.
          child: ScrollConfiguration(
            behavior: behavior.copyWith(overscroll: false, scrollbars: false),
            child: SingleChildScrollView(
              controller: _strip,
              scrollDirection: Axis.horizontal,
              clipBehavior: Clip.none,
              child: _tabRow,
            ),
          ),
        ),
      ),
    );
    return NotificationListener<ScrollMetricsNotification>(
      onNotification: (notification) {
        if (notification.depth == 0) _stripMetricsChanged();
        return false;
      },
      child: arrows
          ? Row(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [Expanded(child: viewport), _scrollArrows(widgetTheme, _stripHeight!)],
            )
          : viewport,
    );
  }

  /// The arrows take the strip's height and the width of two squares of it.
  double get _arrowsWidth => _overflow && _stripHeight != null ? 2 * _stripHeight! : 0;

  /// Shows or hides the arrows as the tabs come to overflow the strip or fit it again, and keeps
  /// the selected tab in view when the strip or its tabs change size; a scroll changes neither.
  void _stripMetricsChanged() {
    if (!mounted || !_strip.hasClients) return;
    final position = _strip.position;
    if (!position.hasContentDimensions || !position.hasViewportDimension) return;
    final height = _stripView.currentContext?.size?.height;
    // Measured against the strip without the arrows, so showing them doesn't keep them shown.
    final overflow = position.maxScrollExtent > _arrowsWidth + 0.5;
    if (overflow != _overflow || height != _stripHeight) {
      setState(() {
        _overflow = overflow;
        _stripHeight = height;
      });
    }
    _reveal.metricsChanged();
    _reportStrip();
  }

  /// Tells Java which part of an overflowing strip is shown, so TabItem.getBounds and
  /// TabFolder.getItem(Point) follow the scroll (TabFolderHelper.handleStripLaidOut). A strip
  /// whose tabs fit again sends one empty report.
  void _reportStrip() {
    if (!mounted || !_strip.hasClients) return;
    final position = _strip.position;
    if (!position.hasContentDimensions || !position.hasViewportDimension) return;
    final report = position.maxScrollExtent > 0.5
        ? '${position.pixels.round()},${position.viewportDimension.round()}'
        : null;
    if (report == _reportedStrip) return;
    _reportedStrip = report;
    widget.sendTabFolderstripLaidOut(state, VEvent()..text = report ?? '');
  }

  RenderObject? _tabBox(int index) {
    if (index < 0 || index >= _tabNodes.length || index >= (state.items?.length ?? 0)) return null;
    final box = _tabNodes[index].context?.findRenderObject();
    return box != null && box.attached ? box : null;
  }

  /// Scrolls the strip by one tab: to the start of the first tab past its scrolled edge, or of the
  /// last one before it.
  void _stepStrip(int direction) {
    if (!_strip.hasClients) return;
    final position = _strip.position;
    double? target;
    for (var i = 0; i < (state.items?.length ?? 0); i++) {
      final tab = _tabBox(i);
      final viewport = tab == null ? null : RenderAbstractViewport.maybeOf(tab);
      if (viewport == null) continue;
      final start = viewport.getOffsetToReveal(tab!, 0).offset;
      if (direction > 0 && start > position.pixels + 0.5) {
        target = start;
        break;
      }
      if (direction < 0 && start < position.pixels - 0.5) target = start;
    }
    target ??= direction > 0 ? position.maxScrollExtent : position.minScrollExtent;
    _strip.jumpTo(target.clamp(position.minScrollExtent, position.maxScrollExtent));
  }

  Widget _scrollArrows(TabFolderThemeExtension widgetTheme, double size) {
    final enabled = state.enabled ?? false;
    return ListenableBuilder(
      listenable: _strip,
      builder: (context, _) {
        final position = _strip.hasClients ? _strip.position : null;
        final known = position != null && position.hasContentDimensions;
        final back = enabled && known && position.pixels > position.minScrollExtent + 0.5;
        final forward = enabled && known && position.pixels < position.maxScrollExtent - 0.5;
        return Row(
          mainAxisSize: MainAxisSize.min,
          children: [
            _scrollArrow(widgetTheme, size, Icons.chevron_left, back ? () => _stepStrip(-1) : null),
            _scrollArrow(widgetTheme, size, Icons.chevron_right, forward ? () => _stepStrip(1) : null),
          ],
        );
      },
    );
  }

  Widget _scrollArrow(TabFolderThemeExtension widgetTheme, double size, IconData icon, VoidCallback? onStep) {
    return MouseRegion(
      cursor: onStep != null ? SystemMouseCursors.click : SystemMouseCursors.basic,
      child: GestureDetector(
        behavior: HitTestBehavior.opaque,
        onTap: onStep,
        child: Container(
          width: size,
          height: size,
          decoration: getTabScrollArrowDecoration(widgetTheme),
          child: Icon(
            icon,
            size: size * widgetTheme.tabScrollArrowIconScale,
            color: getTabScrollArrowIconColor(widgetTheme, enabled: onStep != null),
          ),
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

    return tagSemantics(vItem, MouseRegion(
      cursor: enabled ? SystemMouseCursors.click : SystemMouseCursors.basic,
      child: OwnPressListener(
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
    _reveal.schedule();
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

/// Marks the row of tabs for the measurement; shows the row the folder last built.
class _TabRow extends StatefulWidget {
  const _TabRow({required this.tabs});

  final ValueListenable<Widget> tabs;

  @override
  State<_TabRow> createState() => _TabRowState();
}

class _TabRowState extends State<_TabRow> {
  @override
  void initState() {
    super.initState();
    widget.tabs.addListener(_changed);
  }

  @override
  void didUpdateWidget(_TabRow oldWidget) {
    super.didUpdateWidget(oldWidget);
    if (oldWidget.tabs != widget.tabs) {
      oldWidget.tabs.removeListener(_changed);
      widget.tabs.addListener(_changed);
    }
  }

  @override
  void dispose() {
    widget.tabs.removeListener(_changed);
    super.dispose();
  }

  void _changed() => setState(() {});

  @override
  Widget build(BuildContext context) => widget.tabs.value;
}

/// Clips at the left and right edges only.
class _SidewaysClip extends CustomClipper<Rect> {
  const _SidewaysClip();

  @override
  Rect getClip(Size size) => Rect.fromLTRB(0, -size.height, size.width, 2 * size.height);

  @override
  bool shouldReclip(_SidewaysClip oldClipper) => false;
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
