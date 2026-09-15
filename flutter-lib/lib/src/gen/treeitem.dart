import 'package:flutter/widgets.dart';
import 'package:json_annotation/json_annotation.dart';
import '../gen/color.dart';
import '../gen/font.dart';
import '../gen/image.dart';
import '../gen/item.dart';
import '../gen/treeitem.dart';
import '../gen/widget.dart';
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
  List<VImage?>? images;
  List<VTreeItem>? items;
  List<String?>? texts;

  @override
  void copyFrom(VWidget other) {
    super.copyFrom(other);
    if (other is VTreeItem) {
      background = other.background;
      checked = other.checked;
      expanded = other.expanded;
      font = other.font;
      foreground = other.foreground;
      grayed = other.grayed;
      images = other.images;
      items = other.items;
      texts = other.texts;
    }
  }

  @override
  void readProperty(String key, Map<String, dynamic> json) {
    switch (key) {
      case 'background':
        background = json['background'] == null
            ? null
            : VColor.fromJson(json['background'] as Map<String, dynamic>);
      case 'checked':
        checked = json['checked'] as bool?;
      case 'expanded':
        expanded = json['expanded'] as bool?;
      case 'font':
        font = json['font'] == null
            ? null
            : VFont.fromJson(json['font'] as Map<String, dynamic>);
      case 'foreground':
        foreground = json['foreground'] == null
            ? null
            : VColor.fromJson(json['foreground'] as Map<String, dynamic>);
      case 'grayed':
        grayed = json['grayed'] as bool?;
      case 'images':
        images = (json['images'] as List<dynamic>?)
            ?.map(
              (e) =>
                  e == null ? null : VImage.fromJson(e as Map<String, dynamic>),
            )
            .toList();
      case 'items':
        items = (json['items'] as List<dynamic>?)
            ?.map((e) => VTreeItem.fromJson(e as Map<String, dynamic>))
            .toList();
      case 'texts':
        texts = (json['texts'] as List<dynamic>?)
            ?.map((e) => e as String?)
            .toList();
      default:
        super.readProperty(key, json);
    }
  }

  @override
  void adoptChildren(VWidget Function(VWidget) adopt) {
    super.adoptChildren(adopt);
    VWidget.adoptEach(items, adopt);
  }

  factory VTreeItem.fromJson(Map<String, dynamic> json) =>
      _$VTreeItemFromJson(json)..isReference = json.containsKey('_r');
  Map<String, dynamic> toJson() => _$VTreeItemToJson(this);
}
