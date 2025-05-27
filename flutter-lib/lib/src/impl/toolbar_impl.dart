import 'package:flutter/material.dart';
import 'package:swtflutter/src/impl/widget_config.dart';
import 'package:swtflutter/src/swt/toolitem.dart';
import '../impl/composite_impl.dart';
import '../styles.dart';
import '../swt/swt.dart';
import '../swt/toolbar.dart';


class ToolBarImpl<T extends ToolBarSwt, V extends ToolBarValue>
    extends CompositeImpl<T, V> {

  final bool useDarkTheme = getCurrentTheme();

  @override
  Widget build(BuildContext context) {
    final toolItems = getToolItems();
    final style = StyleBits(state.style);
    final isVertical = style.has(SWT.VERTICAL);
    final shouldWrap = style.has(SWT.WRAP);

    final darkTheme = ThemeData.dark().copyWith(
      primaryColor: Color(0xFF6366F1),
      scaffoldBackgroundColor: Color(0xFF2C2C2C),
    );

    return Theme(
      data: useDarkTheme ? darkTheme : Theme.of(context),
      child: Builder(
          builder: (context) {
            final theme = Theme.of(context);
            final isDark = theme.brightness == Brightness.dark;

            final backgroundColor = isDark ? Color(0xFF1A1A1A) : Color(0xFFFFFFFF);
            final borderColor = isDark ? Color(0xFF333333) : Color(0xFFDDDDDD);
            final buttonColor = isDark ? Color(0xFF2D2D2D) : Color(0xFFFFFFFF);
            final hoverColor = isDark ? Color(0xFF3D3D3D) : Color(0xFFE0E0E0);

            Widget bar;
            if (shouldWrap) {
              bar = Wrap(
                direction: isVertical ? Axis.vertical : Axis.horizontal,
                children: toolItems,
              );
            } else {
              bar = SingleChildScrollView(
                scrollDirection: isVertical ? Axis.vertical : Axis.horizontal,
                child: isVertical
                    ? Column(mainAxisSize: MainAxisSize.min, children: toolItems)
                    : Row(mainAxisSize: MainAxisSize.min, children: toolItems),
              );
            }

            return super.wrap(
              Container(
                decoration: BoxDecoration(
                  color: backgroundColor,
                  border: Border(
                    bottom: BorderSide(color: borderColor, width: 1),
                  ),
                  boxShadow: isDark ? [
                    BoxShadow(
                      color: Colors.black.withOpacity(0.2),
                      blurRadius: 2,
                      offset: Offset(0, 1),
                    )
                  ] : null,
                ),
                child: Padding(
                  padding: const EdgeInsets.symmetric(vertical: 5.0, horizontal: 4.0),
                  child: bar,
                ),
              ),
            );
          }
      ),
    );
  }


  List<Widget> getToolItems() {
    if (state.children == null) {
      return [];
    }
    return state.children!
        .whereType<ToolItemValue>()
        .map((toolItem) => getWidgetForToolItem(toolItem))
        .toList();
  }


  Widget getWidgetForToolItem(ToolItemValue toolItem) {

    final itemWidget = ToolItemSwt(value: toolItem);

    return Builder(
        builder: (context) {
          final theme = Theme.of(context);
          final isDark = theme.brightness == Brightness.dark;

          final iconColor = isDark ? Colors.grey.shade400 : Colors.grey.shade700;
          final textColor = isDark ? Colors.grey.shade300 : Colors.grey.shade800;
          final hoverColor = isDark ? Color(0xFF3D3D3D) : Color(0xFFE0E0E0);

          Widget item = switch (toolItem.style) {
            SWT.SEPARATOR => VerticalDivider(
              width: 16,
              thickness: 1,
              indent: 8,
              endIndent: 8,
              color: isDark ? Color(0xFF444444) : Color(0xFFDDDDDD),
            ),
            _ => Material(
              color: Colors.transparent,
              child: InkWell(
                onTap: () => itemWidget.sendSelectionSelection(toolItem, null),
                hoverColor: hoverColor,
                borderRadius: BorderRadius.circular(4.0),
                child: Container(
                  height: 28,
                  padding: const EdgeInsets.symmetric(horizontal: 8.0),
                  child: Center(
                    child: DefaultTextStyle(
                      style: TextStyle(
                        color: textColor,
                        fontSize: 12,
                      ),
                      child: itemWidget,
                    ),
                  ),
                ),
              ),
            )
          };


          if (toolItem.toolTipText != null) {
            item = Tooltip(
              message: toolItem.toolTipText!,
              child: item,
            );
          }

          return Padding(
            padding: const EdgeInsets.symmetric(horizontal: 2.0),
            child: item,
          );
        }
    );
  }
}
