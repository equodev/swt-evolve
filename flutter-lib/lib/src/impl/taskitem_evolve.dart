import 'package:flutter/widgets.dart';
import '../gen/taskitem.dart';
import '../gen/widget.dart';
import '../impl/item_evolve.dart';

class TaskItemImpl<T extends TaskItemSwt, V extends VTaskItem>
    extends ItemImpl<T, V> {
  @override
  Widget build(BuildContext context) {
    // TODO: implement the widget, remove this line to stop regenerating
    return const Text("TaskItem");
  }
}
