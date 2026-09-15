import 'package:flutter/widgets.dart';
import 'package:json_annotation/json_annotation.dart';
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

  factory VTaskBar.fromJson(Map<String, dynamic> json) =>
      _$VTaskBarFromJson(json)..isReference = json.containsKey('_r');
  Map<String, dynamic> toJson() => _$VTaskBarToJson(this);
}
