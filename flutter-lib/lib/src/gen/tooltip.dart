import 'package:flutter/widgets.dart';
import 'package:json_annotation/json_annotation.dart';
import '../comm/comm.dart';
import '../gen/point.dart';
import '../gen/widget.dart';
import '../impl/tooltip_evolve.dart';
import 'event.dart';
import 'widgets.dart';

part 'tooltip.g.dart';

class ToolTipSwt<V extends VToolTip> extends WidgetSwt<V> {
  const ToolTipSwt({super.key, required super.value});

  @override
  State createState() => ToolTipImpl<ToolTipSwt<VToolTip>, VToolTip>();

  void sendSelectionDefaultSelection(V val, VEvent? payload) {
    sendEvent(val, "Selection/DefaultSelection", payload);
  }

  void sendSelectionSelection(V val, VEvent? payload) {
    sendEvent(val, "Selection/Selection", payload);
  }
}

@JsonSerializable()
class VToolTip extends VWidget {
  VToolTip() : this.empty();
  VToolTip.empty() {
    swt = "ToolTip";
  }

  VPoint? location;
  String? message;
  String? text;
  bool? visible;

  @override
  void copyFrom(VWidget other) {
    super.copyFrom(other);
    if (other is VToolTip) {
      location = other.location;
      message = other.message;
      text = other.text;
      visible = other.visible;
    }
  }

  @override
  void readProperty(String key, Map<String, dynamic> json) {
    switch (key) {
      case 'location':
        location = json['location'] == null
            ? null
            : VPoint.fromJson(json['location'] as Map<String, dynamic>);
      case 'message':
        message = json['message'] as String?;
      case 'text':
        text = json['text'] as String?;
      case 'visible':
        visible = json['visible'] as bool?;
      default:
        super.readProperty(key, json);
    }
  }

  factory VToolTip.fromJson(Map<String, dynamic> json) =>
      _$VToolTipFromJson(json)..isReference = json.containsKey('_r');
  Map<String, dynamic> toJson() => _$VToolTipToJson(this);
}
