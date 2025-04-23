import 'package:flutter/widgets.dart';

import '../swt/tabitem.dart';
import '../swt/widget.dart';

import '../impl/item_impl.dart';

class TabItemImpl<T extends TabItemSwt, V extends TabItemValue>
    extends ItemImpl<T, V> {
  @override
  Widget build(BuildContext context) {
    // TODO: implement the widget, remove this line to stop regenerating
    return const Text("TabItem");
  }
}
