import 'package:json_annotation/json_annotation.dart';
import '../gen/image.dart';
import '../gen/widget.dart';
import 'widgets.dart';

part 'item.g.dart';

abstract class ItemSwt<V extends VItem> extends WidgetSwt<V> {
  
  const ItemSwt({super.key, required super.value});

  

  

  
}


@JsonSerializable() class VItem extends VWidget {
  VItem() : this.empty();
  VItem.empty()  { swt = "Item"; }
  
  VImage? image;
  String? text;
  
  factory VItem.fromJson(Map<String, dynamic> json) => _$VItemFromJson(json);
  Map<String, dynamic> toJson() => _$VItemToJson(this);
  
}