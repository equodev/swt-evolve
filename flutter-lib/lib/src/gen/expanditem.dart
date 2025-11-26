import 'package:flutter/widgets.dart';
import 'package:json_annotation/json_annotation.dart';
import '../gen/control.dart';
import '../gen/image.dart';
import '../gen/item.dart';
import '../impl/expanditem_evolve.dart';
import 'widgets.dart';

part 'expanditem.g.dart';

class ExpandItemSwt<V extends VExpandItem> extends ItemSwt<V> {
  
  const ExpandItemSwt({super.key, required super.value});

  
  @override
  State createState() => ExpandItemImpl<ExpandItemSwt<VExpandItem>, VExpandItem>();

  

  
}


@JsonSerializable() class VExpandItem extends VItem {
  VExpandItem() : this.empty();
  VExpandItem.empty()  { swt = "ExpandItem"; }
  
  VControl? control;
  bool? expanded;
  int? height;
  
  factory VExpandItem.fromJson(Map<String, dynamic> json) => _$VExpandItemFromJson(json);
  Map<String, dynamic> toJson() => _$VExpandItemToJson(this);
  
}