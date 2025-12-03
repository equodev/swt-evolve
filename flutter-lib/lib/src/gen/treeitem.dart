import 'package:flutter/widgets.dart';
import 'package:json_annotation/json_annotation.dart';
import '../gen/color.dart';
import '../gen/font.dart';
import '../gen/image.dart';
import '../gen/item.dart';
import '../gen/treeitem.dart';
import '../impl/treeitem_evolve.dart';
import 'widgets.dart';

part 'treeitem.g.dart';

class TreeItemSwt<V extends VTreeItem> extends ItemSwt<V> {
  const TreeItemSwt({super.key, required super.value});

  @override
  State createState() => TreeItemImpl<TreeItemSwt<VTreeItem>, VTreeItem>();
}

@JsonSerializable()
class VTreeItem extends VItem {
  VTreeItem() : this.empty();
  VTreeItem.empty() {
    swt = "TreeItem";
  }

  VColor? background;
  bool? checked;
  bool? expanded;
  VFont? font;
  VColor? foreground;
  bool? grayed;
  List<VTreeItem>? items;
  List<String>? texts;

  factory VTreeItem.fromJson(Map<String, dynamic> json) =>
      _$VTreeItemFromJson(json);
  Map<String, dynamic> toJson() => _$VTreeItemToJson(this);
}
