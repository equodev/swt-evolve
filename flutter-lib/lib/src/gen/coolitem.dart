import 'package:flutter/widgets.dart';
import 'package:json_annotation/json_annotation.dart';
import '../comm/comm.dart';
import '../gen/control.dart';
import '../gen/image.dart';
import '../gen/item.dart';
import '../gen/point.dart';
import '../impl/coolitem_evolve.dart';
import 'event.dart';
import 'widgets.dart';

part 'coolitem.g.dart';

class CoolItemSwt<V extends VCoolItem> extends ItemSwt<V> {
  const CoolItemSwt({super.key, required super.value});

  @override
  State createState() => CoolItemImpl<CoolItemSwt<VCoolItem>, VCoolItem>();

  void sendSelectionDefaultSelection(V val, VEvent? payload) {
    sendEvent(val, "Selection/DefaultSelection", payload);
  }

  void sendSelectionSelection(V val, VEvent? payload) {
    sendEvent(val, "Selection/Selection", payload);
  }
}

@JsonSerializable()
class VCoolItem extends VItem {
  VCoolItem() : this.empty();
  VCoolItem.empty() {
    swt = "CoolItem";
  }

  VControl? control;
  VPoint? minimumSize;
  VPoint? preferredSize;

  factory VCoolItem.fromJson(Map<String, dynamic> json) =>
      _$VCoolItemFromJson(json);
  Map<String, dynamic> toJson() => _$VCoolItemToJson(this);
}
