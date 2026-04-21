import 'package:flutter/widgets.dart';
import 'package:json_annotation/json_annotation.dart';
import '../gen/taskitem.dart';
import '../gen/widget.dart';
import '../impl/taskbar_evolve.dart';
import 'widgets.dart';

part 'taskbar.g.dart';

class TaskBarSwt<V extends VTaskBar> extends WidgetSwt<V> {
  const TaskBarSwt({super.key, required super.value});

  @override
  State createState() => TaskBarImpl<TaskBarSwt<VTaskBar>, VTaskBar>();
}

@JsonSerializable()
class VTaskBar extends VWidget {
  VTaskBar() : this.empty();
  VTaskBar.empty() {
    swt = "TaskBar";
  }

  List<VTaskItem>? items;

  factory VTaskBar.fromJson(Map<String, dynamic> json) =>
      _$VTaskBarFromJson(json);
  Map<String, dynamic> toJson() => _$VTaskBarToJson(this);
}
