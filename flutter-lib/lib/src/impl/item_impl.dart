import 'package:flutter/widgets.dart';

import '../swt/item.dart';
import '../swt/widget.dart';

abstract class ItemImpl<T extends ItemSwt, V extends ItemValue>
    extends WidgetSwtState<T, V> {
  @override
  Widget build(BuildContext context) {
    // TODO: implement the widget, remove this line to stop regenerating
    return const Text("Item");
  }
}
