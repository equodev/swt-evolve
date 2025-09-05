import 'package:swtflutter/src/impl/widget_config.dart';
import 'package:flutter/material.dart';
import '../swt/expanditem.dart';
import '../impl/item_impl.dart';
import '../widgets.dart';

class ExpandItemImpl<T extends ExpandItemSwt, V extends ExpandItemValue>
    extends ItemImpl<T, V> {
  final bool useDarkTheme = getCurrentTheme();

  @override
  Widget build(BuildContext context) {
    final textColor = useDarkTheme ? Colors.white : Color(0xFF595858);

    Widget? content;
    if (state.children != null && state.children!.isNotEmpty) {
      content = Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children:
            state.children!.map((child) => mapWidgetFromValue(child)).toList(),
      );
    }

    return Container(
      height: (state.height != null && state.height! > 0)
          ? state.height!.toDouble()
          : null, // Si es null o -1, dejamos que el contenido determine la altura
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        mainAxisSize:
            MainAxisSize.min, // Para que solo tome el espacio necesario
        children: [
          if (state.text != null)
            Padding(
              padding: EdgeInsets.only(bottom: 8),
              child: Text(
                state.text!,
                style: TextStyle(
                  fontSize: 14,
                  fontWeight: FontWeight.w500,
                  color: textColor,
                ),
              ),
            ),
          if (content != null) content,
        ],
      ),
    );
  }
}
