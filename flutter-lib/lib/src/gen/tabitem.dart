import 'package:flutter/widgets.dart';
import 'package:json_annotation/json_annotation.dart';
import '../gen/control.dart';
import '../gen/image.dart';
import '../gen/item.dart';
import '../gen/widget.dart';
import '../impl/tabitem_evolve.dart';
import 'widgets.dart';

part 'tabitem.g.dart';

class TabItemSwt<V extends VTabItem> extends ItemSwt<V> {
  const TabItemSwt({super.key, required super.value});

  @override
  State createState() => TabItemImpl<TabItemSwt<VTabItem>, VTabItem>();
}

@JsonSerializable()
class VTabItem extends VItem {
  VTabItem() : this.empty();
  VTabItem.empty() {
    swt = "TabItem";
  }

  VControl? control;
  String? toolTipText;

  @override
  void copyFrom(VWidget other) {
    super.copyFrom(other);
    if (other is VTabItem) {
      control = other.control;
      toolTipText = other.toolTipText;
    }
  }

  @override
  void readProperty(String key, Map<String, dynamic> json) {
    switch (key) {
      case 'control':
        control = json['control'] == null
            ? null
            : VControl.fromJson(json['control'] as Map<String, dynamic>);
      case 'toolTipText':
        toolTipText = json['toolTipText'] as String?;
      default:
        super.readProperty(key, json);
    }
  }

  @override
  void adoptChildren(VWidget Function(VWidget) adopt) {
    super.adoptChildren(adopt);
    control = VWidget.adoptOne(control, adopt);
  }

  factory VTabItem.fromJson(Map<String, dynamic> json) =>
      _$VTabItemFromJson(json)..isReference = json.containsKey('_r');
  Map<String, dynamic> toJson() => _$VTabItemToJson(this);
}
