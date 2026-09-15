import 'package:flutter/widgets.dart';
import 'package:json_annotation/json_annotation.dart';
import '../comm/comm.dart';
import '../gen/control.dart';
import '../gen/image.dart';
import '../gen/item.dart';
import '../gen/point.dart';
import '../gen/widget.dart';
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
  VPoint? preferredSize;

  @override
  void copyFrom(VWidget other) {
    super.copyFrom(other);
    if (other is VCoolItem) {
      control = other.control;
      preferredSize = other.preferredSize;
    }
  }

  @override
  void readProperty(String key, Map<String, dynamic> json) {
    switch (key) {
      case 'control':
        control = json['control'] == null
            ? null
            : VControl.fromJson(json['control'] as Map<String, dynamic>);
      case 'preferredSize':
        preferredSize = json['preferredSize'] == null
            ? null
            : VPoint.fromJson(json['preferredSize'] as Map<String, dynamic>);
      default:
        super.readProperty(key, json);
    }
  }

  @override
  void adoptChildren(VWidget Function(VWidget) adopt) {
    super.adoptChildren(adopt);
    control = VWidget.adoptOne(control, adopt);
  }

  factory VCoolItem.fromJson(Map<String, dynamic> json) =>
      _$VCoolItemFromJson(json)..isReference = json.containsKey('_r');
  Map<String, dynamic> toJson() => _$VCoolItemToJson(this);
}
