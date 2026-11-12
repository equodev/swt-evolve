import 'package:flutter/material.dart';

import '../widget_config.dart';
import 'text_utils.dart';

/// A Table or Tree column header with its `TableColumn`/`TreeColumn.setToolTipText`, shown after the
/// display's tooltip delay as a native header shows it. [child] as it is when the column has none.
Widget withColumnToolTip(String? toolTipText, Widget child) {
  final message = toolTipText == null ? '' : stripAccelerators(toolTipText);
  if (message.isEmpty) return child;
  return Tooltip(
    message: message,
    // The header carries its own semantics; the tooltip would add itself to the column's name.
    excludeFromSemantics: true,
    waitDuration: hoverDelay,
    child: child,
  );
}
