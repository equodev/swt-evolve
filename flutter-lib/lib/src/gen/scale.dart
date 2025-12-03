import 'package:flutter/widgets.dart';
import 'package:json_annotation/json_annotation.dart';
import '../comm/comm.dart';
import '../gen/color.dart';
import '../gen/control.dart';
import '../gen/font.dart';
import '../gen/image.dart';
import '../gen/menu.dart';
import '../gen/rectangle.dart';
import '../impl/scale_evolve.dart';
import 'event.dart';
import 'widgets.dart';

part 'scale.g.dart';

class ScaleSwt<V extends VScale> extends ControlSwt<V> {
  const ScaleSwt({super.key, required super.value});

  @override
  State createState() => ScaleImpl<ScaleSwt<VScale>, VScale>();

  void sendSelectionDefaultSelection(V val, VEvent? payload) {
    sendEvent(val, "Selection/DefaultSelection", payload);
  }

  void sendSelectionSelection(V val, VEvent? payload) {
    sendEvent(val, "Selection/Selection", payload);
  }
}

@JsonSerializable()
class VScale extends VControl {
  VScale() : this.empty();
  VScale.empty() {
    swt = "Scale";
  }

  int? increment;
  int? maximum;
  int? minimum;
  int? pageIncrement;
  int? selection;

  factory VScale.fromJson(Map<String, dynamic> json) => _$VScaleFromJson(json);
  Map<String, dynamic> toJson() => _$VScaleToJson(this);
}
