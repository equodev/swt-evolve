import 'package:flutter/widgets.dart';
import 'package:json_annotation/json_annotation.dart';
import '../gen/control.dart';
import '../gen/image.dart';
import '../gen/item.dart';
import '../gen/widget.dart';
import '../impl/expanditem_evolve.dart';
import 'widgets.dart';

part 'expanditem.g.dart';

class ExpandItemSwt<V extends VExpandItem> extends ItemSwt<V> {
  const ExpandItemSwt({super.key, required super.value});

  @override
  State createState() =>
      ExpandItemImpl<ExpandItemSwt<VExpandItem>, VExpandItem>();
}

@JsonSerializable()
class VExpandItem extends VItem {
  VExpandItem() : this.empty();
  VExpandItem.empty() {
    swt = "ExpandItem";
  }

  VControl? control;
  bool? expanded;
  int? height;

  @override
  void copyFrom(VWidget other) {
    super.copyFrom(other);
    if (other is VExpandItem) {
      control = other.control;
      expanded = other.expanded;
      height = other.height;
    }
  }

  @override
  void readProperty(String key, Map<String, dynamic> json) {
    switch (key) {
      case 'control':
        control = json['control'] == null
            ? null
            : VControl.fromJson(json['control'] as Map<String, dynamic>);
      case 'expanded':
        expanded = json['expanded'] as bool?;
      case 'height':
        height = (json['height'] as num?)?.toInt();
      default:
        super.readProperty(key, json);
    }
  }

  @override
  void adoptChildren(VWidget Function(VWidget) adopt) {
    super.adoptChildren(adopt);
    control = VWidget.adoptOne(control, adopt);
  }

  factory VExpandItem.fromJson(Map<String, dynamic> json) =>
      _$VExpandItemFromJson(json)..isReference = json.containsKey('_r');
  Map<String, dynamic> toJson() => _$VExpandItemToJson(this);
}
