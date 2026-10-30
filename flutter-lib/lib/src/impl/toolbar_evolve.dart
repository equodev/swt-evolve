import 'package:flutter/material.dart';
import 'package:flutter/rendering.dart';
import 'package:flutter/scheduler.dart';
import 'package:flutter/services.dart';
import 'package:flutter/widgets.dart';
import 'package:swtflutter/src/gen/item.dart';
import '../gen/event.dart';
import '../gen/swt.dart';
import '../gen/toolbar.dart';
import '../gen/toolitem.dart';
import '../gen/widget.dart';
import '../gen/widgets.dart' show WireKey;
import '../impl/composite_evolve.dart';
import '../custom/toolbar_composite.dart';
import 'package:swtflutter/src/impl/widget_config.dart';
import '../styles.dart';
import '../theme/theme_extensions/toolbar_theme_extension.dart';
import '../theme/theme_extensions/toolitem_theme_extension.dart';
import '../theme/theme_settings/toolitem_theme_settings.dart';
import 'utils/font_utils.dart';
import 'utils/widget_utils.dart';
import 'utils/focus_report.dart';
import 'utils/tab_only_focus_node.dart';
import 'utils/layered_layout_builder.dart';
import 'utils/text_utils.dart';
import 'utils/cached_text.dart';
import 'utils/quiet_animations.dart';
import 'package:flutter/foundation.dart' show listEquals;
import '../comm/v_registry.dart';

VEvent? toolItemGeometryEvent(BuildContext context) {
  final itemBox = context.findRenderObject();
  if (itemBox is! RenderBox || !itemBox.hasSize) return null;
  final toolbarState = context.findAncestorStateOfType<ToolBarImpl>();
  final toolbarBox = toolbarState?.context.findRenderObject();
  if (toolbarBox is! RenderBox || !toolbarBox.hasSize) return null;
  final topLeft = itemBox.localToGlobal(Offset.zero, ancestor: toolbarBox);
  final bottomRight = itemBox.localToGlobal(
      Offset(itemBox.size.width, itemBox.size.height),
      ancestor: toolbarBox);
  return VEvent()
    ..x = topLeft.dx.round()
    ..y = topLeft.dy.round()
    ..width = (bottomRight.dx - topLeft.dx).round()
    ..height = (bottomRight.dy - topLeft.dy).round();
}

/// A tool item's focus stop. [arrow] marks a DROP_DOWN item's arrow, which belongs to the same
/// item as the stop before it.
class ToolItemFocusNode extends TabOnlyFocusNode {
  ToolItemFocusNode({super.debugLabel, this.arrow = false});

  final bool arrow;
}

/// Down on a focused DROP_DOWN item opens its menu, as on a native ToolBar.
Widget openMenuOnDown({required bool enabled, required VoidCallback openMenu, required Widget child}) =>
    Focus(
      canRequestFocus: false,
      skipTraversal: true,
      onKeyEvent: (node, event) {
        if (!enabled || event is! KeyDownEvent || event.logicalKey != LogicalKeyboardKey.arrowDown) {
          return KeyEventResult.ignored;
        }
        openMenu();
        return KeyEventResult.handled;
      },
      child: child,
    );

/// Carries toolbar layout config down to ToolItem children.
/// [textOnRight]: true when the ToolBar has SWT.RIGHT style (icon left, text right).
/// false when not (icon top, text bottom).
class ToolBarConfig extends InheritedWidget {
  final bool textOnRight;

  const ToolBarConfig({
    super.key,
    required this.textOnRight,
    required super.child,
  });

  static ToolBarConfig? of(BuildContext context) =>
      context.dependOnInheritedWidgetOfExactType<ToolBarConfig>();

  @override
  bool updateShouldNotify(ToolBarConfig oldWidget) =>
      textOnRight != oldWidget.textOnRight;
}

class ToolBarImpl<T extends ToolBarSwt, V extends VToolBar>
    extends CompositeImpl<T, V> {
  /// A click never focuses a ToolBar natively, empty or not.
  @override
  void takeFocusOnPress() {}

  final FocusReport _focusReport = FocusReport();
  final FocusNode _barNode = FocusNode(debugLabel: 'ToolBar', canRequestFocus: false, skipTraversal: true);

  /// The bar's stop is its entry item, not the Composite surface.
  @override
  bool get surfaceTakesTab => false;

  bool _ownsItem(FocusNode? node) =>
      node is ToolItemFocusNode && node.context?.findAncestorStateOfType<ToolBarImpl>() == this;

  /// The ToolBar holds the focus while one of its own items does; a control hosted in a separator
  /// item reports its own focus.
  void _onFocusChange() {
    final primary = FocusManager.instance.primaryFocus;
    if (_ownsItem(primary) && !(primary as ToolItemFocusNode).arrow) {
      if (_hotItem != primary) {
        _hotItem = primary;
        _updateTabStops();
      }
    }
    _focusReport.update(_ownsItem(primary),
        focusIn: () => widget.sendFocusFocusIn(state, null),
        focusOut: () => widget.sendFocusFocusOut(state, null));
  }

  /// The item Tab enters the bar on: the one last focused, as Win32's hot item.
  ToolItemFocusNode? _hotItem;

  /// One Tab stop per bar, when Java names the bar one (VControl.tabStop): Tab enters on the hot
  /// item, or the first enabled one, and the next Tab leaves the bar; Left/Right move between items.
  void _updateTabStops() {
    if (!mounted) return;
    final stops = _barNode.descendants.where(_ownsItem).cast<ToolItemFocusNode>().toList();
    final items = stops.where((s) => !s.arrow && s.focusableStop).toList();
    final entry = state.tabStop ?? false
        ? (items.contains(_hotItem) ? _hotItem : (items.isEmpty ? null : items.first))
        : null;
    for (final stop in stops) {
      stop.skipTraversal = stop != entry;
    }
  }

  /// With an item focused, Left/Right move to the previous/next enabled item, wrapping around as on
  /// Windows. The arrow keys never walk the focus out of the bar.
  KeyEventResult _onItemKey(FocusNode node, KeyEvent event) {
    final primary = FocusManager.instance.primaryFocus;
    if (!_ownsItem(primary)) return KeyEventResult.ignored;
    final key = event.logicalKey;
    final int step;
    if (key == LogicalKeyboardKey.arrowRight) {
      step = 1;
    } else if (key == LogicalKeyboardKey.arrowLeft) {
      step = -1;
    } else if (key == LogicalKeyboardKey.arrowUp || key == LogicalKeyboardKey.arrowDown) {
      return KeyEventResult.handled;
    } else {
      return KeyEventResult.ignored;
    }
    if (event is KeyUpEvent) return KeyEventResult.handled;
    final direction = StyleBits(state.style).has(SWT.RIGHT_TO_LEFT) ? -step : step;
    TabOnlyFocusNode.keyMove(() {
      final stops = _barNode.descendants.where(_ownsItem).cast<ToolItemFocusNode>().toList();
      var index = stops.indexOf(primary as ToolItemFocusNode);
      if (primary.arrow && index > 0) index--;
      for (var i = 1; i < stops.length; i++) {
        final next = stops[(index + direction * i) % stops.length];
        if (!next.arrow && next.canRequestFocus) {
          next.requestFocus();
          break;
        }
      }
    });
    return KeyEventResult.handled;
  }

  @override
  void initState() {
    super.initState();
    FocusManager.instance.addListener(_onFocusChange);
  }

  @override
  void dispose() {
    FocusManager.instance.removeListener(_onFocusChange);
    _barNode.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    _itemWidgets = _builtItemWidgets;
    _builtItemWidgets = Map<VToolItem, Widget>.identity();
    // The items' nodes are attached once this frame is laid out.
    WidgetsBinding.instance.addPostFrameCallback((_) => _updateTabStops());
    final widgetTheme = Theme.of(context).extension<ToolBarThemeExtension>()!;
    final style = StyleBits(state.style);
    final isVertical = style.has(SWT.VERTICAL);
    final shouldWrap = style.has(SWT.WRAP);
    final hasBorder = style.has(SWT.BORDER);
    final isFlat = style.has(SWT.FLAT);
    final hasShadowOut = style.has(SWT.SHADOW_OUT);
    final isRightToLeft = style.has(SWT.RIGHT_TO_LEFT);
    final textOnRight = style.has(SWT.RIGHT);
    final entries = _toolItemEntries(context);
    final toolItems = entries.map((e) => e.widget).nonNulls.toList();
    final wrappedRows =
        shouldWrap && !isVertical ? _wrappedRows(entries) : null;

    final isInToolbarArea = ToolbarAreaMarker.of(context);
    // A nested bar (e.g. the CTabFolder topRight slot) sits inside a composite that already
    // picked its own background -- often not this theme's generic toolbarBackgroundColor, but
    // the tab bar's accent color, so the composite and its own controls (minimize/maximize) read
    // as one surface. Falling back to the marker's color instead of the theme default keeps this
    // bar on that same surface rather than painting a mismatched slab over it.
    final Color? backgroundColor = getBackgroundColor(
      background: state.background,
      defaultColor: isInToolbarArea
          ? (ToolbarAreaMarker.backgroundOf(context) ?? widgetTheme.toolbarBackgroundColor)
          : Theme.of(context).colorScheme.surface,
    );

    return Builder(
      builder: (context) {
        final toolItemTheme = Theme.of(
          context,
        ).extension<ToolItemThemeExtension>();
        final iconSize = toolItemTheme?.defaultIconSize ?? 24.0;

        Widget bar;
        if (shouldWrap && isVertical) {
          final limitedToolItems = toolItems.map((item) {
            return ConstrainedBox(
              constraints: BoxConstraints(maxWidth: iconSize),
              child: item,
            );
          }).toList();

          bar = Wrap(
            direction: Axis.vertical,
            textDirection: isRightToLeft ? TextDirection.rtl : TextDirection.ltr,
            crossAxisAlignment: WrapCrossAlignment.center,
            children: limitedToolItems,
          );

          if (hasBounds(state.bounds)) {
            final allocatedHeight = state.bounds!.height.toDouble();
            final unscaledBar = bar;
            bar = LayeredLayoutBuilder(
              builder: (context, incoming) {
                if (incoming.maxWidth <= 0) return unscaledBar;
                return ConstrainedBox(
                  constraints: BoxConstraints(maxHeight: allocatedHeight),
                  child: FittedBox(
                    fit: BoxFit.scaleDown,
                    alignment: Alignment.topCenter,
                    child: unscaledBar,
                  ),
                );
              },
            );
          }
        } else if (wrappedRows != null && wrappedRows.length > 1) {
          // Each row fills its own share of the bar, so items sit where Java's toDisplay puts them.
          final rowCount = wrappedRows.length;
          final rowHeight = (state.bounds!.height - 2 * (rowCount - 1)) / rowCount;
          bar = Column(
            mainAxisSize: MainAxisSize.min,
            crossAxisAlignment: CrossAxisAlignment.start,
            spacing: 2,
            children: [
              for (final row in wrappedRows)
                SizedBox(
                  width: state.bounds!.width.toDouble(),
                  height: rowHeight > 0 ? rowHeight : 0,
                  child: FittedBox(
                    fit: BoxFit.scaleDown,
                    alignment: isRightToLeft
                        ? Alignment.centerRight
                        : Alignment.centerLeft,
                    child: Row(
                      mainAxisSize: MainAxisSize.min,
                      textDirection: isRightToLeft
                          ? TextDirection.rtl
                          : TextDirection.ltr,
                      crossAxisAlignment: CrossAxisAlignment.center,
                      children: row,
                    ),
                  ),
                ),
            ],
          );
        } else {
          final itemRow = isVertical
              ? Column(
                  mainAxisSize: MainAxisSize.min,
                  crossAxisAlignment: CrossAxisAlignment.center,
                  children: toolItems,
                )
              : Row(
                  mainAxisSize: MainAxisSize.min,
                  textDirection: isRightToLeft
                      ? TextDirection.rtl
                      : TextDirection.ltr,
                  crossAxisAlignment: CrossAxisAlignment.center,
                  children: toolItems,
                );

          // Items paint at fixed theme sizes, so the row only reaches the scale the size
          // model measured because this FittedBox squeezes it into the box the parent
          // allotted. A parent that sizes children to their content instead (the CTabFolder
          // topRight slot) leaves the axis unbounded, so on that axis fall back to the
          // measured bounds -- otherwise the items paint at the raw theme size.
          bar = _CapUnbounded(
            measured: getConstraintsFromBounds(state.bounds),
            child: FittedBox(
              fit: BoxFit.scaleDown,
              alignment:
                  isRightToLeft ? Alignment.centerRight : Alignment.centerLeft,
              child: itemRow,
            ),
          );
        }

        BoxDecoration? decoration;
        if (hasBorder || hasShadowOut || isFlat) {
          decoration = BoxDecoration(
            color: backgroundColor,
            border: hasBorder
                ? Border.all(
                    color: widgetTheme.borderColor,
                    width: widgetTheme.borderWidth,
                  )
                : null,
            boxShadow: hasShadowOut
                ? [
                    BoxShadow(
                      color: widgetTheme.shadowColor.withOpacity(
                        widgetTheme.shadowOpacity,
                      ),
                      blurRadius: widgetTheme.shadowBlurRadius,
                      offset: widgetTheme.shadowOffset,
                    ),
                  ]
                : null,
          );
        } else {
          decoration = BoxDecoration(color: backgroundColor);
        }

        return ToolBarConfig(
          textOnRight: textOnRight,
          child: super.wrap(
            Align(
              alignment: Alignment.centerLeft,
              child: Container(
                decoration: decoration,
                // Controls hosted by a separator item are ordinary Composites and would paint
                // themselves on the default surface, a white slab on top of the toolbar band.
                child: ParentBackgroundScope(
                  background: backgroundColor,
                  // The items letter and colour in the ToolBar's own font/foreground: VToolItem
                  // carries no font of its own, and an item without a colour takes the bar's.
                  child: ParentForegroundScope(
                    foreground: state.foreground,
                    font: state.font,
                    child: Focus(focusNode: _barNode, onKeyEvent: _onItemKey, child: bar),
                  ),
                ),
              ),
            ),
          ),
        );
      },
    );
  }

  List<Widget> getToolItems(BuildContext context) =>
      _toolItemEntries(context).map((e) => e.widget).nonNulls.toList();

  /// Every item with its widget, null for one that draws nothing but still takes room in Java's layout.
  List<({VToolItem item, Widget? widget})> _toolItemEntries(BuildContext context) {
    if (state.items == null) {
      return [];
    }

    final style = StyleBits(state.style);
    final isVertical = style.has(SWT.VERTICAL);
    final allItems = state.items!.whereType<VToolItem>().toList();
    final contentItems = allItems
        .where((t) =>
            (t.style & SWT.SEPARATOR) == 0 &&
            (t.image != null || t.text != null))
        .toList();

    final toolItemTheme = Theme.of(
      context,
    ).extension<ToolItemThemeExtension>()!;
    final keywordTextLower = toolItemTheme.segmentKeywordText.toLowerCase();
    final debugTextLower = toolItemTheme.segmentDebugText.toLowerCase();
    final specialDropdownTooltipText = toolItemTheme.specialDropdownTooltipText;
    final specialDropdownImageFilename =
        toolItemTheme.specialDropdownImageFilename;

    final contentWidgets = _buildGroupedItems(
      contentItems,
      context,
      keywordTextLower,
      debugTextLower,
      specialDropdownTooltipText,
      specialDropdownImageFilename,
    );

    final toolbarTheme = Theme.of(context).extension<ToolBarThemeExtension>()!;
    final result = <({VToolItem item, Widget? widget})>[];
    var contentIndex = 0;
    for (final item in allItems) {
      Widget? widget;
      if ((item.style & SWT.SEPARATOR) != 0) {
        // A SEPARATOR ToolItem carrying setControl() hosts a real control — combos,
        // text fields, nested toolbars. Collapsing it to a divider drops that whole
        // subtree, so let the item build itself instead.
        if (item.control != null) {
          widget = ToolItemSwt(value: item);
        } else {
          widget = _buildToolBarSeparator(isVertical, toolbarTheme);
        }
      } else if (item.image != null || item.text != null) {
        if (contentIndex < contentWidgets.length) {
          widget = contentWidgets[contentIndex++];
        }
      }
      result.add((item: item, widget: widget));
    }
    return result;
  }

  /// The rows Java's layout wraps a horizontal SWT.WRAP bar into, by each item's serialized width.
  List<List<Widget>>? _wrappedRows(
      List<({VToolItem item, Widget? widget})> entries) {
    final bounds = state.bounds;
    if (!hasBounds(bounds)) return null;
    final rows = <List<Widget>>[[]];
    var x = 0;
    for (var i = 0; i < entries.length; i++) {
      final item = entries[i].item;
      var width = item.width ?? 0;
      // Java's thickness for a separator the application gave no width.
      if ((item.style & SWT.SEPARATOR) != 0 && width <= 0) width = 8;
      if (i != 0 && x + width > bounds!.width) {
        rows.add([]);
        x = 0;
      }
      x += width;
      final widget = entries[i].widget;
      if (widget != null) rows.last.add(widget);
    }
    return rows;
  }

  Widget _buildToolBarSeparator(bool isVertical, ToolBarThemeExtension theme) {
    final thickness = theme.separatorThickness;
    final width = theme.separatorWidth;
    final height = theme.separatorHeight;
    final color = theme.borderColor;
    if (isVertical) {
      return Padding(
        padding: theme.itemPadding,
        child: Center(
          child: Container(
            width: height,
            height: thickness,
            color: color,
          ),
        ),
      );
    }
    return Padding(
      padding: theme.itemPadding,
      child: Center(
        child: VerticalDivider(
          width: width,
          thickness: thickness,
          color: color,
        ),
      ),
    );
  }

  List<Widget> _buildGroupedItems(
    List<VToolItem> items,
    BuildContext context,
    String keywordTextLower,
    String debugTextLower,
    String specialDropdownTooltipText,
    String? specialDropdownImageFilename,
  ) {
    final result = <Widget>[];
    final processedIndices = <int>{};
    final toolbarTheme = Theme.of(context).extension<ToolBarThemeExtension>()!;
    final toolItemTheme = Theme.of(
      context,
    ).extension<ToolItemThemeExtension>()!;

    for (int i = 0; i < items.length; i++) {
      if (processedIndices.contains(i)) continue;

      final item = items[i];
      final text = item.text?.trim().toLowerCase();
      final tooltipText = item.toolTipText?.trim();
      final imageFilename = item.image?.filename;

      final useSpecialDropdown =
          getConfigFlags().use_special_dropdown_button ?? true;

      if (useSpecialDropdown) {
        final matchesByText = text == specialDropdownTooltipText.toLowerCase();
        final matchesByTooltip = tooltipText == specialDropdownTooltipText;
        final matchesByImage =
            specialDropdownImageFilename != null &&
            imageFilename == specialDropdownImageFilename;

        if (matchesByText || matchesByTooltip || matchesByImage) {
          result.add(_buildSpecialDropdown(context, item));
          processedIndices.add(i);
          continue;
        }
      }

      if (text == keywordTextLower || text == debugTextLower) {
        VToolItem? otherItem;
        int? otherIndex;
        for (int j = i + 1; j < items.length; j++) {
          final otherText = items[j].text?.trim().toLowerCase();
          if ((text == keywordTextLower && otherText == debugTextLower) ||
              (text == debugTextLower && otherText == keywordTextLower)) {
            otherItem = items[j];
            otherIndex = j;
            break;
          }
        }

        if (otherItem != null && otherIndex != null) {
          final firstIsKeyword = text == keywordTextLower;
          final firstItem = firstIsKeyword ? item : otherItem;
          final secondItem = firstIsKeyword ? otherItem : item;

          result.add(_buildSegmentControl(context, firstItem, secondItem));
          processedIndices.add(i);
          processedIndices.add(otherIndex);
        } else {
          result.add(getWidgetForToolItem(item, toolbarTheme));
        }
      } else {
        result.add(getWidgetForToolItem(item, toolbarTheme));
      }
    }

    return result;
  }

  /// An item drawn by this bar rather than by a widget of its own changed in a way the bar lays out.
  void itemChanged() {
    if (mounted) setState(() {});
  }

  Widget _buildSegmentControl(
    BuildContext context,
    VToolItem keywordItem,
    VToolItem debugItem,
  ) {
    return _SegmentControlWidget(
      keywordItem: keywordItem,
      debugItem: debugItem,
    );
  }

  Widget _buildSpecialDropdown(BuildContext context, VToolItem toolItem) {
    return _SpecialDropdownWidget(toolItem: toolItem);
  }

  /// The widget of each item, kept while the item is the same value. What an item shows reaches it
  /// on its own channel, so the same widget lets a rebuild of the bar pass an item by.
  var _itemWidgets = Map<VToolItem, Widget>.identity();
  var _builtItemWidgets = Map<VToolItem, Widget>.identity();

  Widget _itemWidget(VToolItem item) =>
      _builtItemWidgets[item] = _itemWidgets[item] ?? ToolItemSwt(value: item);

  Widget getWidgetForToolItem(
    VToolItem toolItem,
    ToolBarThemeExtension widgetTheme, {
    bool shouldLimitSize = false,
    double? maxSize,
  }) {
    final itemWidget = _itemWidget(toolItem);

    Widget result = Padding(
      padding: widgetTheme.itemPadding,
      child: itemWidget,
    );

    if (shouldLimitSize && maxSize != null) {
      result = ConstrainedBox(
        constraints: BoxConstraints(maxWidth: maxSize, maxHeight: maxSize),
        child: result,
      );
    }

    // Shrunk into its own measured width: the bar's FittedBox factor is shared with hosted controls,
    // which already paint at their Java size, so alone it draws icons larger in a bar hosting one.
    final measuredWidth = toolItem.width;
    if (measuredWidth != null && measuredWidth > 0) {
      result = ConstrainedBox(
        constraints: BoxConstraints(maxWidth: measuredWidth.toDouble()),
        child: FittedBox(fit: BoxFit.scaleDown, child: result),
      );
    }

    return result;
  }
}

/// Lays its child out within the parent's constraints, except that an axis the parent leaves
/// unbounded is capped at [measured].
class _CapUnbounded extends SingleChildRenderObjectWidget {
  const _CapUnbounded({required this.measured, required super.child});

  final BoxConstraints? measured;

  @override
  _RenderCapUnbounded createRenderObject(BuildContext context) => _RenderCapUnbounded(measured);

  @override
  void updateRenderObject(BuildContext context, _RenderCapUnbounded renderObject) {
    renderObject.measured = measured;
  }
}

class _RenderCapUnbounded extends RenderProxyBox {
  _RenderCapUnbounded(this._measured);

  BoxConstraints? _measured;
  set measured(BoxConstraints? value) {
    if (value == _measured) return;
    _measured = value;
    markNeedsLayout();
  }

  BoxConstraints _childConstraints(BoxConstraints constraints) {
    final measured = _measured;
    if (measured == null) return constraints;
    return BoxConstraints(
      maxWidth: constraints.hasBoundedWidth ? double.infinity : measured.maxWidth,
      maxHeight: constraints.hasBoundedHeight ? double.infinity : measured.maxHeight,
    ).enforce(constraints);
  }

  @override
  void performLayout() {
    final child = this.child;
    if (child == null) {
      size = constraints.smallest;
      return;
    }
    child.layout(_childConstraints(constraints), parentUsesSize: true);
    size = child.size;
  }

  @override
  Size computeDryLayout(BoxConstraints constraints) =>
      child?.getDryLayout(_childConstraints(constraints)) ?? constraints.smallest;
}

class _SegmentControlWidget extends StatefulWidget {
  final VToolItem keywordItem;
  final VToolItem debugItem;

  const _SegmentControlWidget({
    required this.keywordItem,
    required this.debugItem,
  });

  @override
  State<_SegmentControlWidget> createState() => _SegmentControlWidgetState();
}

class _SegmentControlWidgetState extends State<_SegmentControlWidget> {
  /// The two items are drawn here rather than by widgets of their own, so this listens to them: a
  /// change of what only this control shows redraws it alone, and anything else rebuilds the bar.
  static const _drawnHere = {WireKey.k_ToolItem_selection, WireKey.k_ToolItem_enabled};
  static const _selectionOnly = {WireKey.k_ToolItem_selection};
  List<int> _watched = const [];

  /// Ticks on a change of selection alone: only the parts that show it rebuild.
  final _selectionChanges = ValueNotifier<int>(0);

  void _watch() {
    final keys = [
      VRegistry.keyOf(widget.keywordItem),
      VRegistry.keyOf(widget.debugItem),
    ];
    if (listEquals(keys, _watched)) return;
    _unwatch();
    for (final key in keys) {
      VRegistry.instance.watchAt(key, _itemChanged);
    }
    _watched = keys;
  }

  void _unwatch() {
    for (final key in _watched) {
      VRegistry.instance.unwatchAt(key, _itemChanged);
    }
    _watched = const [];
  }

  void _itemChanged(VChange change) {
    if (!mounted) return;
    if (change.isOnly(_selectionOnly)) {
      _selectionChanges.value++;
    } else if (change.isOnly(_drawnHere)) {
      setState(() {});
    } else {
      context.findAncestorStateOfType<ToolBarImpl>()?.itemChanged();
    }
  }

  @override
  void initState() {
    super.initState();
    _watch();
  }

  @override
  void didUpdateWidget(_SegmentControlWidget oldWidget) {
    super.didUpdateWidget(oldWidget);
    _watch();
  }

  @override
  void dispose() {
    _unwatch();
    _selectionChanges.dispose();
    super.dispose();
  }

  Widget _followingSelection(Widget Function() build) => ValueListenableBuilder<int>(
        valueListenable: _selectionChanges,
        builder: (context, _, __) => build(),
      );

  @override
  Widget build(BuildContext context) {
    final widgetTheme = Theme.of(context).extension<ToolItemThemeExtension>()!;

    // A selection animates the control for a while; the rest of the bar need not repaint with it.
    return RepaintBoundary(
      child: _buildSegmentControlContent(
        context: context,
        widgetTheme: widgetTheme,
        keywordItem: widget.keywordItem,
        debugItem: widget.debugItem,
      ),
    );
  }

  Widget _buildSegmentControlContent({
    required BuildContext context,
    required ToolItemThemeExtension widgetTheme,
    required VToolItem keywordItem,
    required VToolItem debugItem,
  }) {
    final keywordTextLower = widgetTheme.segmentKeywordText.toLowerCase();
    final debugTextLower = widgetTheme.segmentDebugText.toLowerCase();

    final keywordText =
        keywordItem.text?.trim().toLowerCase() == keywordTextLower
        ? keywordItem
        : debugItem;
    final debugText = debugItem.text?.trim().toLowerCase() == debugTextLower
        ? debugItem
        : keywordItem;

    final textStyle = getTextStyle(
      context: context,
      font: null,
      textColor: widgetTheme.segmentSelectedTextColor,
      baseTextStyle: widgetTheme.fontStyle,
    );

    final keywordTextSize = textSize(widgetTheme.segmentKeywordText, textStyle);
    final debugTextSize = textSize(widgetTheme.segmentDebugText, textStyle);

    final keywordWidth =
        keywordTextSize.width + widgetTheme.segmentPadding.horizontal;
    final debugWidth =
        debugTextSize.width + widgetTheme.segmentPadding.horizontal;

    final toolbarTheme = Theme.of(context).extension<ToolBarThemeExtension>();
    final backgroundColor =
        toolbarTheme?.compositeBackgroundColor ?? Colors.white;

    return Container(
      color: backgroundColor,
      child: IntrinsicHeight(
        child: Container(
          padding: EdgeInsets.symmetric(
            vertical: widgetTheme.segmentPadding.vertical,
          ),
          decoration: BoxDecoration(
            color: widgetTheme.segmentUnselectedBackgroundColor,
            borderRadius: BorderRadius.circular(
              widgetTheme.segmentBorderRadius,
            ),
          ),
          child: Stack(
            children: [
              Positioned.fill(
                child: _followingSelection(() {
                  final keywordSelected = keywordText.selection ?? false;
                  return SlidingBox(
                    duration: widgetTheme.segmentAnimationDuration,
                    curve: Curves.easeInOut,
                    left: keywordSelected ? 0.0 : keywordWidth,
                    width: keywordSelected ? keywordWidth : debugWidth,
                    decoration: BoxDecoration(
                      color: widgetTheme.segmentInnerColor,
                      borderRadius: BorderRadius.circular(
                        widgetTheme.segmentBorderRadius,
                      ),
                    ),
                  );
                }),
              ),
              Row(
                mainAxisSize: MainAxisSize.min,
                children: [
                  _buildSegmentButtonWrapper(
                    context: context,
                    widgetTheme: widgetTheme,
                    toolItem: keywordText,
                    text: widgetTheme.segmentKeywordText,
                  ),
                  _buildSegmentButtonWrapper(
                    context: context,
                    widgetTheme: widgetTheme,
                    toolItem: debugText,
                    text: widgetTheme.segmentDebugText,
                  ),
                ],
              ),
            ],
          ),
        ),
      ),
    );
  }

  Widget _buildSegmentButtonWrapper({
    required BuildContext context,
    required ToolItemThemeExtension widgetTheme,
    required VToolItem toolItem,
    required String text,
  }) {
    final enabled = toolItem.enabled ?? true;
    return GestureDetector(
      onTap: enabled
          ? () {
              ToolItemSwt(
                value: toolItem,
              ).sendSelectionSelection(toolItem, null);
            }
          : null,
      child: Container(
        padding: widgetTheme.segmentPadding,
        child: Center(
          child: _followingSelection(() => AnimatedStyledText(
                text,
                duration: widgetTheme.segmentAnimationDuration,
                style: getTextStyle(
                  context: context,
                  font: null,
                  textColor: getSegmentTextColor(widgetTheme, toolItem.selection ?? false),
                  baseTextStyle: widgetTheme.fontStyle,
                ),
                textAlign: TextAlign.center,
              )),
        ),
      ),
    );
  }
}

class _SpecialDropdownWidget extends StatefulWidget {
  final VToolItem toolItem;

  const _SpecialDropdownWidget({required this.toolItem});

  @override
  State<_SpecialDropdownWidget> createState() => _SpecialDropdownWidgetState();
}

class _SpecialDropdownWidgetState extends State<_SpecialDropdownWidget> {
  final _buttonFocus = ToolItemFocusNode(debugLabel: 'ToolItem');
  final _arrowFocus = ToolItemFocusNode(debugLabel: 'ToolItem arrow', arrow: true);

  @override
  void dispose() {
    _buttonFocus.dispose();
    _arrowFocus.dispose();
    super.dispose();
  }

  void _openMenu() =>
      ToolItemSwt(value: widget.toolItem).sendSelectionOpenMenu(widget.toolItem, toolItemGeometryEvent(context));

  @override
  Widget build(BuildContext context) {
    final widgetTheme = Theme.of(context).extension<ToolItemThemeExtension>()!;
    final enabled = widget.toolItem.enabled ?? true;
    final text = widgetTheme.specialDropdownText;

    final textStyle = getTextStyle(
      context: context,
      font: null,
      textColor: widgetTheme.specialDropdownTextColor,
      baseTextStyle: widgetTheme.fontStyle,
    );

    Widget mainContentButton = Material(
      color: Colors.transparent,
      child: InkWell(
        focusNode: _buttonFocus,
        onTap: enabled
            ? () {
                final itemWidget = ToolItemSwt(value: widget.toolItem);
                itemWidget.sendSelectionSelection(widget.toolItem, null);
              }
            : null,
        splashColor: widgetTheme.specialDropdownTextColor.withOpacity(
          widgetTheme.splashOpacity,
        ),
        highlightColor: widgetTheme.specialDropdownTextColor.withOpacity(
          widgetTheme.highlightOpacity,
        ),
        borderRadius: BorderRadius.only(
          topLeft: Radius.circular(widgetTheme.borderRadius),
          bottomLeft: Radius.circular(widgetTheme.borderRadius),
        ),
        child: Container(
          padding: widgetTheme.specialDropdownPadding,
          decoration: BoxDecoration(
            color: widgetTheme.specialDropdownBackgroundColor,
            borderRadius: BorderRadius.only(
              topLeft: Radius.circular(widgetTheme.borderRadius),
              bottomLeft: Radius.circular(widgetTheme.borderRadius),
            ),
          ),
          child: Padding(
            padding: widgetTheme.textPadding,
            child: Center(
              child: Text(text, style: textStyle, textAlign: TextAlign.center),
            ),
          ),
        ),
      ),
    );

    Widget separator = Container(
      width: widgetTheme.separatorBarWidth,
      color: widgetTheme.specialDropdownSeparatorColor,
    );

    Widget dropdownArrow = Material(
      color: Colors.transparent,
      child: InkWell(
        focusNode: _arrowFocus,
        onTap: enabled ? _openMenu : null,
        splashColor: widgetTheme.specialDropdownTextColor.withOpacity(
          widgetTheme.splashOpacity,
        ),
        highlightColor: widgetTheme.specialDropdownTextColor.withOpacity(
          widgetTheme.highlightOpacity,
        ),
        borderRadius: BorderRadius.only(
          topRight: Radius.circular(widgetTheme.borderRadius),
          bottomRight: Radius.circular(widgetTheme.borderRadius),
        ),
        child: Container(
          padding: widgetTheme.specialDropdownPadding,
          decoration: BoxDecoration(
            color: widgetTheme.specialDropdownBackgroundColor,
            borderRadius: BorderRadius.only(
              topRight: Radius.circular(widgetTheme.borderRadius),
              bottomRight: Radius.circular(widgetTheme.borderRadius),
            ),
          ),
          child: MouseRegion(
            cursor: enabled
                ? SystemMouseCursors.click
                : SystemMouseCursors.basic,
            child: Icon(
              Icons.arrow_drop_down,
              size: widgetTheme.dropdownArrowSize,
              color: widgetTheme.specialDropdownArrowColor,
            ),
          ),
        ),
      ),
    );

    return openMenuOnDown(
      enabled: enabled,
      openMenu: _openMenu,
      child: IntrinsicWidth(
        child: IntrinsicHeight(
          child: Row(
            mainAxisSize: MainAxisSize.min,
            crossAxisAlignment: CrossAxisAlignment.stretch,
            children: [mainContentButton, separator, dropdownArrow],
          ),
        ),
      ),
    );
  }
}

