import 'package:flutter/widgets.dart';
import '../gen/list.dart';
import '../gen/widget.dart';
import '../impl/scrollable_evolve.dart';

class ListImpl<T extends ListSwt, V extends VList>
    extends ScrollableImpl<T, V> {
  @override
  Widget build(BuildContext context) {
    // TODO: implement the widget, remove this line to stop regenerating
    return const Text("List");
  }
}
