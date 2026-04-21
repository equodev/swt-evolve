import 'package:flutter/widgets.dart';
import 'package:json_annotation/json_annotation.dart';
import '../gen/trayitem.dart';
import '../gen/widget.dart';
import '../impl/tray_evolve.dart';
import 'widgets.dart';

part 'tray.g.dart';

class TraySwt<V extends VTray> extends WidgetSwt<V> {
  const TraySwt({super.key, required super.value});

  @override
  State createState() => TrayImpl<TraySwt<VTray>, VTray>();
}

@JsonSerializable()
class VTray extends VWidget {
  VTray() : this.empty();
  VTray.empty() {
    swt = "Tray";
  }

  List<VTrayItem>? items;

  factory VTray.fromJson(Map<String, dynamic> json) => _$VTrayFromJson(json);
  Map<String, dynamic> toJson() => _$VTrayToJson(this);
}
