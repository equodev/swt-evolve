import 'package:flutter/widgets.dart';
import 'package:json_annotation/json_annotation.dart';
import '../gen/image.dart';
import '../gen/item.dart';
import '../gen/menu.dart';
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

  VMenu? menu;
  VImage? overlayImage;
  String? overlayText;
  int? progress;
  int? progressState;

  factory VTaskItem.fromJson(Map<String, dynamic> json) =>
      _$VTaskItemFromJson(json);
  Map<String, dynamic> toJson() => _$VTaskItemToJson(this);
}
