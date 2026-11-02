import 'package:flutter/widgets.dart';
import '../comm/widget_wire.dart';
import '../gen/widget.dart';
import '../gen/widgets.dart';
import '../impl/taskbar_evolve.dart';
import 'widgets.dart';

class TaskBarSwt<V extends VTaskBar> extends WidgetSwt<V> {
  const TaskBarSwt({super.key, required super.value});

  @override
  State createState() => TaskBarImpl<TaskBarSwt<VTaskBar>, VTaskBar>();
}

class VTaskBar extends VWidget {
  VTaskBar() : this.empty();
  VTaskBar.empty() {
    swt = "TaskBar";
  }
}
