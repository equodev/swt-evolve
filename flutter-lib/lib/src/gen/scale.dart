import 'package:flutter/widgets.dart';
import 'package:json_annotation/json_annotation.dart';
import '../comm/comm.dart';
import '../gen/color.dart';
import '../gen/control.dart';
import '../gen/cursor.dart';
import '../gen/font.dart';
import '../gen/image.dart';
import '../gen/menu.dart';
import '../gen/rectangle.dart';
import '../gen/region.dart';
import '../gen/widget.dart';
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

  int? maximum;
  int? minimum;
  int? selection;

  @override
  void copyFrom(VWidget other) {
    super.copyFrom(other);
    if (other is VScale) {
      maximum = other.maximum;
      minimum = other.minimum;
      selection = other.selection;
    }
  }

  @override
  void readProperty(String key, Map<String, dynamic> json) {
    switch (key) {
      case 'maximum':
        maximum = (json['maximum'] as num?)?.toInt();
      case 'minimum':
        minimum = (json['minimum'] as num?)?.toInt();
      case 'selection':
        selection = (json['selection'] as num?)?.toInt();
      default:
        super.readProperty(key, json);
    }
  }

  factory VScale.fromJson(Map<String, dynamic> json) =>
      _$VScaleFromJson(json)..isReference = json.containsKey('_r');
  Map<String, dynamic> toJson() => _$VScaleToJson(this);
}
