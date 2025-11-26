import 'package:flutter/widgets.dart';
import 'package:json_annotation/json_annotation.dart';
import '../gen/color.dart';
import '../gen/control.dart';
import '../gen/font.dart';
import '../gen/image.dart';
import '../gen/item.dart';
import '../impl/ctabitem_evolve.dart';
import 'widgets.dart';

part 'ctabitem.g.dart';

class CTabItemSwt<V extends VCTabItem> extends ItemSwt<V> {
  
  const CTabItemSwt({super.key, required super.value});

  
  @override
  State createState() => CTabItemImpl<CTabItemSwt<VCTabItem>, VCTabItem>();

  

  
}


@JsonSerializable() class VCTabItem extends VItem {
  VCTabItem() : this.empty();
  VCTabItem.empty()  { swt = "CTabItem"; }
  
  VControl? control;
  VImage? disabledImage;
  VFont? font;
  VColor? foreground;
  VColor? selectionForeground;
  bool? showClose;
  String? toolTipText;
  
  factory VCTabItem.fromJson(Map<String, dynamic> json) => _$VCTabItemFromJson(json);
  Map<String, dynamic> toJson() => _$VCTabItemToJson(this);
  
}