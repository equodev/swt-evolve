import 'package:swtflutter/src/impl/widget_config.dart';
import '../styles.dart';
import 'package:flutter/material.dart';
import '../swt/swt.dart';
import '../swt/expandbar.dart';
import '../impl/composite_impl.dart';
import '../swt/expanditem.dart';

class ExpandBarImpl<T extends ExpandBarSwt, V extends ExpandBarValue>
    extends CompositeImpl<T, V> {
  final bool useDarkTheme = getCurrentTheme();

  @override
  Widget build(BuildContext context) {
    final style = StyleBits(state.style);
    final expandItems = getExpandItems();

    final backgroundColor = useDarkTheme ? Color(0xFF2C2C2C) : Colors.white;
    final textColor = useDarkTheme ? Colors.white : Color(0xFF595858);

    // Esta es la clave: NUNCA debemos anidar widgets de scroll
    Widget expandBar;

    if (style.has(SWT.V_SCROLL)) {
      // Si queremos scroll, usamos un ListView directamente
      expandBar = ListView.builder(
        itemCount: expandItems.length,
        itemBuilder: (context, index) => expandItems[index],
      );
    } else {
      // Si no queremos scroll, usamos un Column dentro de un Container
      expandBar = Column(
        children: expandItems,
        mainAxisSize: MainAxisSize.min,
        crossAxisAlignment: CrossAxisAlignment.stretch,
      );
    }

    return Theme(
      data: useDarkTheme ? ThemeData.dark() : ThemeData.light(),
      child: Container(
        // Es importante dar una altura limitada al ExpandBar
        height: MediaQuery.of(context).size.height - 100, // O cualquier altura apropiada
        color: backgroundColor,
        child: expandBar,
      ),
    );
  }

  // El resto del código permanece igual
  List<Widget> getExpandItems() {
    if (state.children == null) {
      return [];
    }
    return state.children!
        .whereType<ExpandItemValue>()
        .map((expandItem) => getWidgetForExpandItem(expandItem))
        .toList();
  }

  Widget getWidgetForExpandItem(ExpandItemValue expandItem) {
    // Código sin cambios
    final itemWidget = ExpandItemSwt(value: expandItem);

    return Builder(
      builder: (context) {
        final textColor = useDarkTheme ? Colors.white : Color(0xFF595858);
        final expandedColor = useDarkTheme ? Color(0xFF3D3D3D) : Color(0xFFF3F4F6);
        final borderColor = useDarkTheme ? Color(0xFF444444) : Color(0xFFE5E7EB);

        return Container(
          decoration: BoxDecoration(
            border: Border(
              bottom: BorderSide(color: borderColor, width: 1),
            ),
          ),
          child: Theme(
            data: Theme.of(context).copyWith(
              dividerColor: Colors.transparent,
              textTheme: Theme.of(context).textTheme.apply(
                bodyColor: textColor,
                displayColor: textColor,
              ),
            ),
            child: ExpansionTile(
              title: Text(
                expandItem.text ?? '',
                style: TextStyle(
                  fontSize: 14,
                  fontWeight: FontWeight.w500,
                  color: textColor,
                ),
              ),
              onExpansionChanged: (expanded) {
                if (expanded) {
                  widget.sendExpandExpand(state, null);
                } else {
                  widget.sendExpandCollapse(state, null);
                }
              },
              initiallyExpanded: expandItem.expanded ?? false,
              children: [
                Container(
                  color: expandedColor,
                  padding: EdgeInsets.all(16),
                  child: itemWidget,
                ),
              ],
            ),
          ),
        );
      },
    );
  }
}