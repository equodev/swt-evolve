import 'package:flutter/widgets.dart';

import '../swt/ctabitem.dart';
import '../swt/widget.dart';

import '../impl/item_impl.dart';

class CTabItemImpl<T extends CTabItemSwt, V extends CTabItemValue>
    extends ItemImpl<T, V> {
  @override
  Widget build(BuildContext context) {
    // TODO: implement the widget, remove this line to stop regenerating
    return const Text("CTabItem");
  }
}
