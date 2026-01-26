import 'package:flutter/material.dart';
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
import 'utils/widget_utils.dart';
import 'utils/image_utils.dart';

class ExpandBarImpl<T extends ExpandBarSwt, V extends VExpandBar>
    extends CompositeImpl<T, V> {
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

    Widget expandBar;
    if (style.has(SWT.V_SCROLL)) {
      expandBar = ListView.builder(
        itemCount: expandItems.length,
        padding: widgetTheme.containerPadding,
        shrinkWrap: !hasValidBounds,
        itemBuilder: (context, index) {
          return Padding(
            padding: EdgeInsets.only(
              bottom: index < expandItems.length - 1 ? widgetTheme.itemSpacing : 0,
            ),
            child: expandItems[index],
          );
        },
      );
    } else {
      expandBar = SingleChildScrollView(
        padding: widgetTheme.containerPadding,
        child: Column(
          mainAxisSize: MainAxisSize.min,
          crossAxisAlignment: CrossAxisAlignment.stretch,
          children: [
            for (int i = 0; i < expandItems.length; i++) ...[
              expandItems[i],
              if (i < expandItems.length - 1)
                SizedBox(height: widgetTheme.itemSpacing),
            ],
          ],
        ),
      );
    }

    Widget result = Container(
      constraints: constraints,
      decoration: BoxDecoration(
        color: backgroundColor,
        border: Border.all(
          color: borderColor,
          width: widgetTheme.borderWidth,
        ),
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

    if (!hasValidBounds) {
      result = IntrinsicHeight(child: result);
    }

    return result;
  }

  List<Widget> getExpandItems(
    BuildContext context,
    ExpandBarThemeExtension widgetTheme,
    ExpandItemThemeExtension itemTheme,
  ) {
    if (state.items == null) {
      return [];
    }

    final spacing = state.spacing ?? widgetTheme.itemSpacing.toInt();

    List<Widget> result = [];
    for (int i = 0; i < state.items!.length; i++) {
      var expandItem = state.items![i];
      result.add(getWidgetForExpandItem(context, expandItem, widgetTheme, itemTheme));

      if (i < state.items!.length - 1 && spacing > 0) {
        result.add(SizedBox(height: spacing.toDouble()));
      }
    }

    return result;
  }

  Widget getWidgetForExpandItem(
    BuildContext context,
    VExpandItem expandItem,
    ExpandBarThemeExtension widgetTheme,
    ExpandItemThemeExtension itemTheme,
  ) {
    return _ExpandItemWidget(
      expandItem: expandItem,
      widgetTheme: widgetTheme,
      itemTheme: itemTheme,
      mapWidgetFromValue: mapWidgetFromValue,
      onExpansionChanged: (expanded) {
        expandItem.expanded = expanded;
        if (expanded) {
          widget.sendExpandExpand(state, null);
        } else {
          widget.sendExpandCollapse(state, null);
        }
      },
      parentBackground: state.background,
      parentForeground: state.foreground,
      parentFont: state.font,
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

    final textColor = _isExpanded
        ? itemTheme.foregroundExpandedColor
        : getForegroundColor(
            foreground: widget.parentForeground,
            defaultColor: itemTheme.foregroundColor,
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

    // Build header with optional image
    Widget headerContent = Row(
      mainAxisSize: MainAxisSize.min,
      children: [
        if (expandItem.image != null && expandItem.image?.imageData != null) ...[
          _buildImage(expandItem, itemTheme),
          SizedBox(width: itemTheme.imageTextSpacing),
        ],
        Expanded(
          child: Text(
            expandItem.text ?? '',
            style: textStyle,
            overflow: TextOverflow.ellipsis,
          ),
        ),
        RotationTransition(
          turns: _iconRotationAnimation,
          child: Icon(
            Icons.expand_more,
            size: itemTheme.iconSize,
            color: iconColor,
          ),
        ),
      ],
    );

    return Container(
      decoration: BoxDecoration(
        border: Border.all(
          color: borderColor,
          width: itemTheme.borderWidth,
        ),
        borderRadius: BorderRadius.circular(itemTheme.borderRadius),
      ),
      clipBehavior: Clip.antiAlias,
      child: Column(
        mainAxisSize: MainAxisSize.min,
        crossAxisAlignment: CrossAxisAlignment.stretch,
        children: [
          // Header
          MouseRegion(
            onEnter: (_) => setState(() => _isHovering = true),
            onExit: (_) => setState(() => _isHovering = false),
            child: GestureDetector(
              onTap: _handleTap,
              child: AnimatedContainer(
                duration: itemTheme.animationDuration,
                curve: itemTheme.animationCurve,
                color: headerBackgroundColor,
                padding: itemTheme.headerPadding,
                child: headerContent,
              ),
            ),
          ),
          // Content
          SizeTransition(
            sizeFactor: _expandAnimation,
            axisAlignment: -1.0,
            child: contentWidget != null
                ? Container(
                    color: contentBackgroundColor,
                    padding: itemTheme.contentPadding,
                    constraints: (expandItem.height != null && expandItem.height! > 0)
                        ? BoxConstraints(
                            minHeight: expandItem.height!.toDouble(),
                            maxHeight: expandItem.height!.toDouble(),
                          )
                        : null,
                    child: contentWidget,
                  )
                : const SizedBox.shrink(),
          ),
        ],
      ),
    );
  }

  Widget _buildImage(VExpandItem expandItem, ExpandItemThemeExtension itemTheme) {
    final imageData = expandItem.image?.imageData;
    if (imageData == null) return const SizedBox.shrink();

    final double imageWidth = imageData.width?.toDouble() ?? itemTheme.iconSize;
    final double imageHeight = imageData.height?.toDouble() ?? itemTheme.iconSize;

    final builtImage = ImageUtils.buildVImage(
      expandItem.image,
      width: imageWidth,
      height: imageHeight,
      enabled: true,
      constraints: null,
      useBinaryImage: true,
      renderAsIcon: false,
    );

    if (builtImage != null) {
      return SizedBox(
        width: imageWidth,
        height: imageHeight,
        child: builtImage,
      );
    }

    return const SizedBox.shrink();
  }
}
