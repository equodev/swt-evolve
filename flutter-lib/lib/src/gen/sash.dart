import 'package:flutter/widgets.dart';
import 'package:json_annotation/json_annotation.dart';
import '../comm/comm.dart';
import '../gen/color.dart';
import '../gen/control.dart';
import '../gen/font.dart';
import '../gen/image.dart';
import '../gen/menu.dart';
import '../gen/rectangle.dart';
import '../impl/sash_evolve.dart';
import 'event.dart';
import 'widgets.dart';

part 'sash.g.dart';

class SashSwt<V extends VSash> extends ControlSwt<V> {
  const SashSwt({super.key, required super.value});

  @override
  State createState() => SashImpl<SashSwt<VSash>, VSash>();

  void sendSelectionDefaultSelection(V val, VEvent? payload) {
    sendEvent(val, "Selection/DefaultSelection", payload);
  }

  void sendSelectionSelection(V val, VEvent? payload) {
    sendEvent(val, "Selection/Selection", payload);
  }
}

@JsonSerializable()
class VSash extends VControl {
  VSash() : this.empty();
  VSash.empty() {
    swt = "Sash";
  }

  factory VSash.fromJson(Map<String, dynamic> json) => _$VSashFromJson(json);
  Map<String, dynamic> toJson() => _$VSashToJson(this);
}
