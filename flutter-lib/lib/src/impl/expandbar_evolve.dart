import 'package:swtflutter/src/impl/widget_config.dart';
import '../gen/expandbar.dart';
import '../gen/expanditem.dart';
import '../gen/widgets.dart';
import '../styles.dart';
import 'package:flutter/material.dart';
import '../gen/swt.dart';
import '../impl/composite_evolve.dart';
import 'color_utils.dart';
import '../gen/composite.dart';

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
    final style = StyleBits(state.style);
    final expandItems = getExpandItems();
    final backgroundColor = getBackground();
    final borderColor = getBorderColor();

    Widget expandBar;
    if (style.has(SWT.V_SCROLL)) {
      expandBar = ListView.builder(
        itemCount: expandItems.length,
        padding: EdgeInsets.zero,
        itemBuilder: (context, index) {
          return Padding(
            padding: EdgeInsets.symmetric(vertical: 2),
            child: expandItems[index],
          );
        },
      );
    } else {
      expandBar = SingleChildScrollView(
        child: Column(
          children: expandItems
              .map((item) => Padding(
            padding: EdgeInsets.symmetric(vertical: 2),
            child: item,
          ))
              .toList(),
          mainAxisSize: MainAxisSize.min,
          crossAxisAlignment: CrossAxisAlignment.stretch,
        ),
      );
    }

    return Material(
      color: Colors.transparent,
      child: Theme(
        data: ThemeData(
          useMaterial3: true,
          canvasColor: Colors.transparent,
        ),
        child: SizedBox(
          width: state.bounds?.width.toDouble(),
          height: state.bounds?.height.toDouble(),
          child: Container(
            decoration: BoxDecoration(
              color: backgroundColor,
              border: Border.all(
                color: borderColor,
                width: 1,
              ),
            ),
            child: expandBar,
          ),
        ),
      ),
    );
  }

  List<Widget> getExpandItems() {
    if (state.items == null) {
      return [];
    }

    final spacing = state.spacing ?? 0;

    List<Widget> result = [];
    for (int i = 0; i < state.items!.length; i++) {
      var expandItem = state.items![i];

      result.add(getWidgetForExpandItem(expandItem));

      if (i < state.items!.length - 1 && spacing > 0) {
        result.add(SizedBox(height: spacing.toDouble()));
      }
    }

    return result;
  }

  Widget getWidgetForExpandItem(VExpandItem expandItem) {
    final textColor = getForeground();
    final expandedColor = getBackgroundSelected();
    final borderColor = getBorderColor();
    final backgroundColor = getBackground();

    Widget? contentWidget;
    if (expandItem.control != null) {
      if (expandItem.control is VComposite) {
        final composite = expandItem.control as VComposite;
        if (composite.children != null && composite.children!.isNotEmpty) {
          List<Widget> childWidgets = [];
          for (int i = 0; i < composite.children!.length; i++) {
            childWidgets.add(mapWidgetFromValue(composite.children![i]));
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
          contentWidget = mapWidgetFromValue(expandItem.control!);
        }
      } else {
        contentWidget = mapWidgetFromValue(expandItem.control!);
      }
    }

    return Material(
      color: Colors.transparent,
      child: Container(
        decoration: BoxDecoration(
          border: Border.all(
            color: borderColor,
            width: 1,
          ),
        ),
        child: Material(
          color: Colors.transparent,
          child: ExpansionTile(
            title: Text(
              expandItem.text ?? 'No title',
              style: TextStyle(
                fontSize: 14,
                fontWeight: FontWeight.normal,
                color: textColor,
              ),
              overflow: TextOverflow.ellipsis,
            ),
            initiallyExpanded: expandItem.expanded ?? false,
            maintainState: true,
            childrenPadding: EdgeInsets.zero,
            expandedCrossAxisAlignment: CrossAxisAlignment.start,
            tilePadding: EdgeInsets.symmetric(horizontal: 8.0, vertical: 0.0),
            collapsedBackgroundColor: backgroundColor,
            backgroundColor: expandedColor,
            iconColor: textColor,
            collapsedIconColor: textColor,
            onExpansionChanged: (expanded) {
              expandItem.expanded = expanded;
              if (expanded) {
                widget.sendExpandExpand(state, null);
              } else {
                widget.sendExpandCollapse(state, null);
              }
            },
            children: [
              if (contentWidget != null)
                Material(
                  color: expandedColor,
                  child: Container(
                    padding: EdgeInsets.symmetric(horizontal: 8, vertical: 4),
                    width: double.infinity,
                    height: (expandItem.height != null && expandItem.height! > 0)
                        ? expandItem.height!.toDouble()
                        : null,
                    child: contentWidget,
                  ),
                ),
            ],
          ),
        ),
      ),
    );
  }
}