import 'package:flutter/widgets.dart';
import 'package:json_annotation/json_annotation.dart';
import '../gen/image.dart';
import '../gen/item.dart';
import '../gen/widget.dart';
import '../impl/taskitem_evolve.dart';
import 'widgets.dart';

part 'taskitem.g.dart';

class TaskItemSwt<V extends VTaskItem> extends ItemSwt<V> {
  const TaskItemSwt({super.key, required super.value});

  @override
  State createState() => TaskItemImpl<TaskItemSwt<VTaskItem>, VTaskItem>();
}

@JsonSerializable()
class VTaskItem extends VItem {
  VTaskItem() : this.empty();
  VTaskItem.empty() {
    swt = "TaskItem";
  }

  factory VTaskItem.fromJson(Map<String, dynamic> json) =>
      _$VTaskItemFromJson(json)..isReference = json.containsKey('_r');
  Map<String, dynamic> toJson() => _$VTaskItemToJson(this);
}
