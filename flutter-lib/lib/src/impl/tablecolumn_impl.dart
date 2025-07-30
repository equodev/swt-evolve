import 'package:flutter/widgets.dart';

import '../swt/tablecolumn.dart';
import '../swt/widget.dart';

import '../impl/item_impl.dart';

class TableColumnImpl<T extends TableColumnSwt, V extends TableColumnValue>
    extends ItemImpl<T, V> {
  @override
  Widget build(BuildContext context) {
    // TODO: implement the widget, remove this line to stop regenerating
    return const Text("TableColumn");
  }
}
