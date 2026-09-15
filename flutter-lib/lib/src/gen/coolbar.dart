import 'package:flutter/widgets.dart';
import 'package:json_annotation/json_annotation.dart';
import '../gen/color.dart';
import '../gen/composite.dart';
import '../gen/control.dart';
import '../gen/coolitem.dart';
import '../gen/cursor.dart';
import '../gen/font.dart';
import '../gen/image.dart';
import '../gen/menu.dart';
import '../gen/point.dart';
import '../gen/rectangle.dart';
import '../gen/region.dart';
import '../gen/scrollbar.dart';
import '../gen/widget.dart';
import '../impl/coolbar_evolve.dart';
import 'widgets.dart';

part 'coolbar.g.dart';

class CoolBarSwt<V extends VCoolBar> extends CompositeSwt<V> {
  const CoolBarSwt({super.key, required super.value});

  @override
  State createState() => CoolBarImpl<CoolBarSwt<VCoolBar>, VCoolBar>();
}

@JsonSerializable()
class VCoolBar extends VComposite {
  VCoolBar() : this.empty();
  VCoolBar.empty() {
    swt = "CoolBar";
  }

  List<int>? itemOrder;
  List<VPoint>? itemSizes;
  List<VCoolItem>? items;
  List<int>? wrapIndices;

  @override
  void copyFrom(VWidget other) {
    super.copyFrom(other);
    if (other is VCoolBar) {
      itemOrder = other.itemOrder;
      itemSizes = other.itemSizes;
      items = other.items;
      wrapIndices = other.wrapIndices;
    }
  }

  @override
  void readProperty(String key, Map<String, dynamic> json) {
    switch (key) {
      case 'itemOrder':
        itemOrder = (json['itemOrder'] as List<dynamic>?)
            ?.map((e) => (e as num).toInt())
            .toList();
      case 'itemSizes':
        itemSizes = (json['itemSizes'] as List<dynamic>?)
            ?.map((e) => VPoint.fromJson(e as Map<String, dynamic>))
            .toList();
      case 'items':
        items = (json['items'] as List<dynamic>?)
            ?.map((e) => VCoolItem.fromJson(e as Map<String, dynamic>))
            .toList();
      case 'wrapIndices':
        wrapIndices = (json['wrapIndices'] as List<dynamic>?)
            ?.map((e) => (e as num).toInt())
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

  factory VCoolBar.fromJson(Map<String, dynamic> json) =>
      _$VCoolBarFromJson(json)..isReference = json.containsKey('_r');
  Map<String, dynamic> toJson() => _$VCoolBarToJson(this);
}
