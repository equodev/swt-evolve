import 'package:flutter/widgets.dart';
import '../comm/widget_wire.dart';
import '../gen/image.dart';
import '../gen/item.dart';
import '../gen/widget.dart';
import '../gen/widgets.dart';
import '../impl/taskitem_evolve.dart';
import 'widgets.dart';

class TaskItemSwt<V extends VTaskItem> extends ItemSwt<V> {
  const TaskItemSwt({super.key, required super.value});

  @override
  State createState() => TaskItemImpl<TaskItemSwt<VTaskItem>, VTaskItem>();
}

class VTaskItem extends VItem {
  VTaskItem() : this.empty();
  VTaskItem.empty() {
    swt = "TaskItem";
  }
}
