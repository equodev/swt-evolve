import 'package:flutter/widgets.dart';

import '../swt/treecolumn.dart';
import '../swt/widget.dart';

import '../impl/item_impl.dart';

class TreeColumnImpl<T extends TreeColumnSwt, V extends TreeColumnValue>
    extends ItemImpl<T, V> {
  @override
  Widget build(BuildContext context) {
    // TODO: implement the widget, remove this line to stop regenerating
    return const Text("TreeColumn");
  }
}
