import 'package:flutter/widgets.dart';
import 'package:json_annotation/json_annotation.dart';
import '../comm/comm.dart';
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

  bool? autoHide;
  String? message;
  String? text;
  bool? visible;

  factory VToolTip.fromJson(Map<String, dynamic> json) =>
      _$VToolTipFromJson(json);
  Map<String, dynamic> toJson() => _$VToolTipToJson(this);
}
