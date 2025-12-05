import 'package:flutter/widgets.dart';
import 'package:json_annotation/json_annotation.dart';
import '../gen/color.dart';
import '../gen/composite.dart';
import '../gen/control.dart';
import '../gen/coolitem.dart';
import '../gen/font.dart';
import '../gen/image.dart';
import '../gen/menu.dart';
import '../gen/point.dart';
import '../gen/rectangle.dart';
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
  bool? locked;
  List<int>? wrapIndices;

  factory VCoolBar.fromJson(Map<String, dynamic> json) =>
      _$VCoolBarFromJson(json);
  Map<String, dynamic> toJson() => _$VCoolBarToJson(this);
}
