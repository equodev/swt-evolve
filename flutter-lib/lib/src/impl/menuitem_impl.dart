import 'package:flutter/widgets.dart';

import '../swt/menuitem.dart';
import '../swt/widget.dart';

import '../impl/item_impl.dart';

class MenuItemImpl<T extends MenuItemSwt, V extends MenuItemValue>
    extends ItemImpl<T, V> {
  @override
  Widget build(BuildContext context) {
    // TODO: implement the widget, remove this line to stop regenerating
    return const Text("MenuItem");
  }
}
