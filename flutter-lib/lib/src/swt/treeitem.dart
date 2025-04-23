import 'package:json_annotation/json_annotation.dart';
import 'package:flutter/widgets.dart';
import '../swt/item.dart';
import '../impl/treeitem_impl.dart';
import '../widgets.dart';
import 'layout.dart';
import 'widget.dart';
import 'rectangle.dart';

part 'treeitem.g.dart';

class TreeItemSwt<V extends TreeItemValue> extends ItemSwt<V> {
  const TreeItemSwt({super.key, required super.value});

  @override
  State createState() =>
      TreeItemImpl<TreeItemSwt<TreeItemValue>, TreeItemValue>();
}

@JsonSerializable()
class TreeItemValue extends ItemValue {
  TreeItemValue() : this.empty();
  TreeItemValue.empty() {
    swt = "TreeItem";
  }

  bool? checked;
  bool? expanded;
  bool? grayed;
  int? itemCount;
  List<String>? texts;
  bool? selected;

  factory TreeItemValue.fromJson(Map<String, dynamic> json) =>
      _$TreeItemValueFromJson(json);
  Map<String, dynamic> toJson() => _$TreeItemValueToJson(this);
}
