import 'package:flutter/widgets.dart';
import 'package:json_annotation/json_annotation.dart';
import '../comm/comm.dart';
import '../gen/color.dart';
import '../gen/composite.dart';
import '../gen/control.dart';
import '../gen/cursor.dart';
import '../gen/expanditem.dart';
import '../gen/font.dart';
import '../gen/image.dart';
import '../gen/menu.dart';
import '../gen/rectangle.dart';
import '../gen/region.dart';
import '../gen/scrollbar.dart';
import '../gen/widget.dart';
import '../impl/expandbar_evolve.dart';
import 'event.dart';
import 'widgets.dart';

part 'expandbar.g.dart';

class ExpandBarSwt<V extends VExpandBar> extends CompositeSwt<V> {
  const ExpandBarSwt({super.key, required super.value});

  @override
  State createState() => ExpandBarImpl<ExpandBarSwt<VExpandBar>, VExpandBar>();

  void sendExpandCollapse(V val, VEvent? payload) {
    sendEvent(val, "Expand/Collapse", payload);
  }

  void sendExpandExpand(V val, VEvent? payload) {
    sendEvent(val, "Expand/Expand", payload);
  }
}

@JsonSerializable()
class VExpandBar extends VComposite {
  VExpandBar() : this.empty();
  VExpandBar.empty() {
    swt = "ExpandBar";
  }

  List<VExpandItem>? items;
  int? spacing;

  @override
  void copyFrom(VWidget other) {
    super.copyFrom(other);
    if (other is VExpandBar) {
      items = other.items;
      spacing = other.spacing;
    }
  }

  @override
  void readProperty(String key, Map<String, dynamic> json) {
    switch (key) {
      case 'items':
        items = (json['items'] as List<dynamic>?)
            ?.map((e) => VExpandItem.fromJson(e as Map<String, dynamic>))
            .toList();
      case 'spacing':
        spacing = (json['spacing'] as num?)?.toInt();
      default:
        super.readProperty(key, json);
    }
  }

  @override
  void adoptChildren(VWidget Function(VWidget) adopt) {
    super.adoptChildren(adopt);
    VWidget.adoptEach(items, adopt);
  }

  factory VExpandBar.fromJson(Map<String, dynamic> json) =>
      _$VExpandBarFromJson(json)..isReference = json.containsKey('_r');
  Map<String, dynamic> toJson() => _$VExpandBarToJson(this);
}
