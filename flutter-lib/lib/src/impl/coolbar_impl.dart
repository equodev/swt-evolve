import 'package:flutter/material.dart';
import 'package:swtflutter/src/styles.dart';
import 'package:swtflutter/src/swt/coolitem.dart';
import 'package:swtflutter/src/swt/swt.dart';
import 'package:swtflutter/src/swt/toolitem.dart';
import 'package:swtflutter/src/widgets.dart';

import '../swt/coolbar.dart';
import '../impl/composite_impl.dart';

class CoolBarImpl<T extends CoolBarSwt, V extends CoolBarValue>
    extends CompositeImpl<T, V> {

  final bool useDarkTheme;
  //todo que el tema se propague a los items

  CoolBarImpl({this.useDarkTheme = true});

  @override
  Widget build(BuildContext context) {
    final coolItems = getCoolItems();
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
            final buttonColor = isDark ? Color(0xFF2D2D2D) : Colors.white;
            final hoverColor = isDark ? Color(0xFF3D3D3D) : Color(0xFFE0E0E0);

            Widget bar;
            if (shouldWrap) {
              bar = Wrap(
                direction: isVertical ? Axis.vertical : Axis.horizontal,
                children: coolItems,
              );
            } else {
              bar = SingleChildScrollView(
                scrollDirection: isVertical ? Axis.vertical : Axis.horizontal,
                child: isVertical
                    ? Column(mainAxisSize: MainAxisSize.min, children: coolItems)
                    : Row(mainAxisSize: MainAxisSize.min, children: coolItems),
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

  List<Widget> getCoolItems() {
    if (state.children == null) {
      return [];
    }
    return state.children!
        .whereType<CoolItemValue>()
        .map((coolItem) => getWidgetForCoolItem(coolItem))
        .toList();
  }

  Widget getWidgetForCoolItem(CoolItemValue coolItem) {
    final itemWidget = CoolItemSwt(value: coolItem);

    return Builder(
        builder: (context) {
          final theme = Theme.of(context);
          final isDark = theme.brightness == Brightness.dark;


          final iconColor = isDark ? Colors.grey.shade400 : Colors.grey.shade700;
          final textColor = isDark ? Colors.grey.shade300 : Colors.grey.shade800;
          final hoverColor = isDark ? Color(0xFF3D3D3D) : Color(0xFFE0E0E0);

          Widget item = switch (coolItem.style) {
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
                onTap: () => itemWidget.sendSelectionSelection(coolItem, null),
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

          // Envolver con Tooltip si hay texto de ayuda
          if (coolItem.toolTipText != null) {
            item = Tooltip(
              message: coolItem.toolTipText!,
              child: item,
            );
          }

          // Añadir un pequeño espacio entre elementos
          return Padding(
            padding: const EdgeInsets.symmetric(horizontal: 2.0),
            child: item,
          );
        }
    );
  }
}