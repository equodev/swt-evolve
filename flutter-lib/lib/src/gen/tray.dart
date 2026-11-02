import 'package:flutter/widgets.dart';
import '../comm/widget_wire.dart';
import '../gen/widget.dart';
import '../gen/widgets.dart';
import '../impl/tray_evolve.dart';
import 'widgets.dart';

class TraySwt<V extends VTray> extends WidgetSwt<V> {
  const TraySwt({super.key, required super.value});

  @override
  State createState() => TrayImpl<TraySwt<VTray>, VTray>();
}

class VTray extends VWidget {
  VTray() : this.empty();
  VTray.empty() {
    swt = "Tray";
  }
}
