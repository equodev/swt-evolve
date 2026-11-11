import 'package:flutter/material.dart';
import '../gen/event.dart';
import '../gen/expandbar.dart';
import '../gen/expanditem.dart';
import '../gen/widget.dart';
import '../gen/widgets.dart';
import '../gen/swt.dart';
import '../gen/composite.dart';
import '../styles.dart';
import '../impl/composite_evolve.dart';
import '../theme/theme_extensions/expandbar_theme_extension.dart';
import '../theme/theme_extensions/expanditem_theme_extension.dart';
import 'expanditem_header.dart';
import 'utils/widget_utils.dart';
import 'utils/image_utils.dart';

class ExpandBarImpl<T extends ExpandBarSwt, V extends VExpandBar>
    extends CompositeImpl<T, V> {
  /// A click never focuses an ExpandBar natively, empty or not.
  @override
  void takeFocusOnPress() {}

  @override
  Widget buildComposite() {
    final children = state.children;

    if (children == null || children.isEmpty) {
      return SizedBox.shrink();
    }

    return Column(
      mainAxisSize: MainAxisSize.min,
      crossAxisAlignment: CrossAxisAlignment.stretch,
      children: children.map((child) => mapWidgetFromValue(child)).toList(),
    );
  }

  @override
  Widget build(BuildContext context) {
    if (hidden) return const SizedBox.shrink();
    final widgetTheme = Theme.of(context).extension<ExpandBarThemeExtension>()!;
    final itemTheme = Theme.of(context).extension<ExpandItemThemeExtension>()!;
    final style = StyleBits(state.style);
    final expandItems = getExpandItems(context, widgetTheme, itemTheme);
    final hasValidBounds = hasBounds(state.bounds);
    final constraints = getConstraintsFromBounds(state.bounds);

    final backgroundColor = getBackgroundColor(
      background: state.background,
      defaultColor: widgetTheme.backgroundColor,
    );
    final borderColor = widgetTheme.borderColor;

    // Java lays the items out `spacing` from every edge and from each other.
    final spacing = _spacing();
    final padding = widgetTheme.containerPadding.add(EdgeInsets.all(spacing));

    Widget expandBar;
    // A bar with no bounds has nothing to scroll, and a ListView cannot be asked how wide it is.
    if (style.has(SWT.V_SCROLL) && hasValidBounds) {
      expandBar = ListView.separated(
        itemCount: expandItems.length,
        padding: padding,
        separatorBuilder: (context, index) => SizedBox(height: spacing),
        itemBuilder: (context, index) => expandItems[index],
      );
    } else {
      expandBar = SingleChildScrollView(
        padding: padding,
        child: Column(
          mainAxisSize: MainAxisSize.min,
          crossAxisAlignment: CrossAxisAlignment.stretch,
          children: [
            for (int i = 0; i < expandItems.length; i++) ...[
              expandItems[i],
              if (i < expandItems.length - 1) SizedBox(height: spacing),
            ],
          ],
        ),
      );
    }

    // Drawn over the bar rather than around it: a border that took room would move every item off Java's coordinates.
    Widget result = Container(
      constraints: constraints,
      decoration: BoxDecoration(
        color: backgroundColor,
        borderRadius: BorderRadius.circular(widgetTheme.borderRadius),
      ),
      foregroundDecoration: BoxDecoration(
        border: Border.all(color: borderColor, width: widgetTheme.borderWidth),
        borderRadius: BorderRadius.circular(widgetTheme.borderRadius),
      ),
      child: ClipRRect(
        borderRadius: BorderRadius.circular(
          widgetTheme.borderRadius > widgetTheme.borderWidth
              ? widgetTheme.borderRadius - widgetTheme.borderWidth
              : 0,
        ),
        child: expandBar,
      ),
    );

    // With no bounds from Java the bar packs as SWT's computeSize does: as wide as its widest header.
    if (!hasValidBounds) {
      result = IntrinsicWidth(child: IntrinsicHeight(child: result));
    }

    // Tab stops on the bar (VControl.tabStop), whose keys Java applies to the header holding its focus;
    // with no item there is no header to stop on.
    result = wrapSurfaceFocus(result, tabStop: (state.tabStop ?? false) && expandItems.isNotEmpty);

    return inOwnLayer(SwtControlScope(swt: state.swt, child: tagSemantics(trackPointer(result))));
  }

  List<Widget> getExpandItems(
    BuildContext context,
    ExpandBarThemeExtension widgetTheme,
    ExpandItemThemeExtension itemTheme,
  ) {
    if (state.items == null) {
      return [];
    }

    return [
      for (final expandItem in state.items!)
        getWidgetForExpandItem(context, expandItem, widgetTheme, itemTheme),
    ];
  }

  /// The space Java keeps around and between the items. Java does not send its default, 0.
  double _spacing() => (state.spacing ?? 0).toDouble();

  Widget getWidgetForExpandItem(
    BuildContext context,
    VExpandItem expandItem,
    ExpandBarThemeExtension widgetTheme,
    ExpandItemThemeExtension itemTheme,
  ) {
    return tagItemSemantics(
      expandItem,
      _ExpandItemWidget(
        expandItem: expandItem,
        widgetTheme: widgetTheme,
        itemTheme: itemTheme,
        mapWidgetFromValue: mapWidgetFromValue,
        onExpansionChanged: (expanded) {
          expandItem.expanded = expanded;
          // The index lets Java resolve WHICH item changed (ExpandBarHelper.sendExpand
          // updates the item's expanded state before firing the SWT event).
          final event = VEvent()
            ..index = state.items?.indexOf(expandItem) ?? -1;
          if (expanded) {
            widget.sendExpandExpand(state, event);
          } else {
            widget.sendExpandCollapse(state, event);
          }
        },
        parentBackground: state.background,
        parentForeground: state.foreground,
        parentFont: state.font,
      ),
    );
  }
}

class _ExpandItemWidget extends StatefulWidget {
  final VExpandItem expandItem;
  final ExpandBarThemeExtension widgetTheme;
  final ExpandItemThemeExtension itemTheme;
  final Widget Function(VWidget) mapWidgetFromValue;
  final ValueChanged<bool> onExpansionChanged;
  final dynamic parentBackground;
  final dynamic parentForeground;
  final dynamic parentFont;

  const _ExpandItemWidget({
    required this.expandItem,
    required this.widgetTheme,
    required this.itemTheme,
    required this.mapWidgetFromValue,
    required this.onExpansionChanged,
    this.parentBackground,
    this.parentForeground,
    this.parentFont,
  });

  @override
  State<_ExpandItemWidget> createState() => _ExpandItemWidgetState();
}

class _ExpandItemWidgetState extends State<_ExpandItemWidget>
    with SingleTickerProviderStateMixin {
  late AnimationController _animationController;
  late Animation<double> _expandAnimation;
  late Animation<double> _iconRotationAnimation;
  bool _isExpanded = false;
  bool _isHovering = false;

  @override
  void initState() {
    super.initState();
    _isExpanded = widget.expandItem.expanded ?? false;
    _animationController = AnimationController(
      duration: widget.itemTheme.animationDuration,
      vsync: this,
    );

    _expandAnimation = CurvedAnimation(
      parent: _animationController,
      curve: widget.itemTheme.animationCurve,
    );

    _iconRotationAnimation = Tween<double>(
      begin: 0.0,
      end: 0.5,
    ).animate(_expandAnimation);

    if (_isExpanded) {
      _animationController.value = 1.0;
    }
  }

  @override
  void didUpdateWidget(covariant _ExpandItemWidget oldWidget) {
    super.didUpdateWidget(oldWidget);
    final newExpanded = widget.expandItem.expanded ?? false;
    if (newExpanded != _isExpanded) {
      _isExpanded = newExpanded;
      if (_isExpanded) {
        _animationController.forward();
      } else {
        _animationController.reverse();
      }
    }
  }

  @override
  void dispose() {
    _animationController.dispose();
    super.dispose();
  }

  void _handleTap() {
    setState(() {
      _isExpanded = !_isExpanded;
    });
    if (_isExpanded) {
      _animationController.forward();
    } else {
      _animationController.reverse();
    }
    widget.onExpansionChanged(_isExpanded);
  }

  @override
  Widget build(BuildContext context) {
    final itemTheme = widget.itemTheme;
    final expandItem = widget.expandItem;

    // Both states take the application's colour when it set one; only the theme default differs.
    final textColor = getForegroundColor(
      context: context,
      foreground: widget.parentForeground,
      defaultColor: _isExpanded
          ? itemTheme.foregroundExpandedColor
          : itemTheme.foregroundColor,
      minContrast: itemTheme.minTextContrast,
    );

    final iconColor = _isExpanded
        ? itemTheme.iconExpandedColor
        : itemTheme.iconColor;

    final headerBackgroundColor = _isExpanded
        ? itemTheme.headerBackgroundExpandedColor
        : (_isHovering
              ? itemTheme.headerBackgroundHoveredColor
              : itemTheme.headerBackgroundColor);

    final contentBackgroundColor = itemTheme.contentBackgroundColor;
    final borderColor = itemTheme.borderColor;

    final textStyle = getTextStyle(
      context: context,
      font: widget.parentFont,
      textColor: textColor,
      baseTextStyle: itemTheme.headerTextStyle,
    );

    Widget? contentWidget;
    if (expandItem.control != null) {
      if (expandItem.control is VComposite) {
        final composite = expandItem.control as VComposite;
        if (composite.children != null && composite.children!.isNotEmpty) {
          List<Widget> childWidgets = [];
          for (int i = 0; i < composite.children!.length; i++) {
            childWidgets.add(widget.mapWidgetFromValue(composite.children![i]));
            if (i < composite.children!.length - 1) {
              childWidgets.add(SizedBox(height: 8));
            }
          }

          contentWidget = Column(
            mainAxisSize: MainAxisSize.min,
            crossAxisAlignment: CrossAxisAlignment.stretch,
            children: childWidgets,
          );
        } else {
          contentWidget = widget.mapWidgetFromValue(expandItem.control!);
        }
      } else {
        contentWidget = widget.mapWidgetFromValue(expandItem.control!);
      }
    }

    // The header is exactly as tall as Java's getHeaderHeight, which sizes the bar and places the body and the items
    // below. 0 is a platform that measures no band: the header then takes the height its content gives it.
    final javaHeaderHeight = (expandItem.headerHeight ?? 0) > 0
        ? expandItem.headerHeight!.toDouble()
        : null;
    // The body is as tall as the height Java sizes it to, inset by the item's border where Java places the
    // control. With no height from Java the control is as tall as it is, inside the theme's content padding.
    final javaHeight = (expandItem.height ?? 0) > 0 ? expandItem.height!.toDouble() : null;
    final contentInsets = javaHeight == null
        ? itemTheme.contentPadding
        : EdgeInsets.fromLTRB(
            itemTheme.borderWidth,
            0,
            itemTheme.borderWidth,
            itemTheme.borderWidth,
          );

    return Semantics(
      identifier: '${expandItem.swt}/${expandItem.id}',
      child: Container(
        // The border is drawn over the item for the same reason as the bar's.
        decoration: BoxDecoration(
          borderRadius: BorderRadius.circular(itemTheme.borderRadius),
        ),
        foregroundDecoration: BoxDecoration(
          border: Border.all(color: borderColor, width: itemTheme.borderWidth),
          borderRadius: BorderRadius.circular(itemTheme.borderRadius),
        ),
        clipBehavior: Clip.antiAlias,
        child: Column(
          mainAxisSize: MainAxisSize.min,
          crossAxisAlignment: CrossAxisAlignment.stretch,
          children: [
            // Header. Its own semantics node, so the item's node has a child - and therefore a
            // settled role - from the first frame. A tappable node with no children is a button to
            // Flutter Web, and it turns into a plain container as soon as the expanded body brings
            // children; the engine answers that role change by rebuilding the node's DOM element
            // and writing back only the properties that changed with it, silently dropping the
            // `flt-semantics-identifier` this item is found by.
            Semantics(
              container: true,
              child: MouseRegion(
                onEnter: (_) => setState(() => _isHovering = true),
                onExit: (_) => setState(() => _isHovering = false),
                child: GestureDetector(
                  onTap: _handleTap,
                  child: ExpandItemHeader(
                    item: expandItem,
                    theme: itemTheme,
                    textStyle: textStyle,
                    height: javaHeaderHeight,
                    color: headerBackgroundColor,
                    chevron: RotationTransition(
                      turns: _iconRotationAnimation,
                      child: Icon(
                        Icons.expand_more,
                        size: itemTheme.iconSize,
                        color: iconColor,
                      ),
                    ),
                  ),
                ),
              ),
            ),
            // The body starts where the header ends.
            SizeTransition(
              sizeFactor: _expandAnimation,
              axisAlignment: -1.0,
              child: javaHeight == null && contentWidget == null
                  ? const SizedBox.shrink()
                  : Container(
                      color: contentBackgroundColor,
                      padding: contentInsets,
                      constraints: javaHeight == null
                          ? null
                          : BoxConstraints.tightFor(height: javaHeight),
                      alignment: javaHeight == null ? null : Alignment.topLeft,
                      child: contentWidget,
                    ),
            ),
          ],
        ),
      ),
    );
  }
}
