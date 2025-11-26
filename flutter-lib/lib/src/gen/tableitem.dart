import 'package:flutter/widgets.dart';
import 'package:json_annotation/json_annotation.dart';
import '../gen/color.dart';
import '../gen/font.dart';
import '../gen/image.dart';
import '../gen/item.dart';
import '../impl/tableitem_evolve.dart';
import 'widgets.dart';

part 'tableitem.g.dart';

class TableItemSwt<V extends VTableItem> extends ItemSwt<V> {
  
  const TableItemSwt({super.key, required super.value});

  
  @override
  State createState() => TableItemImpl<TableItemSwt<VTableItem>, VTableItem>();

  

  
}


@JsonSerializable() class VTableItem extends VItem {
  VTableItem() : this.empty();
  VTableItem.empty()  { swt = "TableItem"; }
  
  VColor? background;
  bool? checked;
  VFont? font;
  VColor? foreground;
  bool? grayed;
  int? imageIndent;
  List<String>? texts;
  
  factory VTableItem.fromJson(Map<String, dynamic> json) => _$VTableItemFromJson(json);
  Map<String, dynamic> toJson() => _$VTableItemToJson(this);
  
}