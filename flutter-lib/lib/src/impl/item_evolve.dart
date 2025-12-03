import 'package:flutter/widgets.dart';
import '../gen/item.dart';
import '../gen/widget.dart';

abstract class ItemImpl<T extends ItemSwt, V extends VItem>
    extends WidgetSwtState<T, V> {
  @override
  Widget build(BuildContext context) {
    // TODO: implement the widget, remove this line to stop regenerating
    return const Text("Item");
  }
}
