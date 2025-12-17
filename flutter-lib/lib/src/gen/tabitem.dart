import 'package:flutter/widgets.dart';
import 'package:json_annotation/json_annotation.dart';
import '../gen/control.dart';
import '../gen/image.dart';
import '../gen/item.dart';
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

  factory VTabItem.fromJson(Map<String, dynamic> json) =>
      _$VTabItemFromJson(json);
  Map<String, dynamic> toJson() => _$VTabItemToJson(this);
}
