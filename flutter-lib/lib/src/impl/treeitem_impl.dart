import 'package:flutter/widgets.dart';

import '../swt/treeitem.dart';
import '../swt/widget.dart';

import '../impl/item_impl.dart';

class TreeItemImpl<T extends TreeItemSwt, V extends TreeItemValue>
    extends ItemImpl<T, V> {
  @override
  Widget build(BuildContext context) {
    // TODO: implement the widget, remove this line to stop regenerating
    return const Text("TreeItem");
  }
}
