import 'package:flutter/widgets.dart';

import '../swt/tableitem.dart';
import '../swt/widget.dart';

import '../impl/item_impl.dart';

class TableItemImpl<T extends TableItemSwt, V extends TableItemValue>
    extends ItemImpl<T, V> {
  @override
  Widget build(BuildContext context) {
    // TODO: implement the widget, remove this line to stop regenerating
    return const Text("TableItem");
  }
}
