import 'package:flutter/widgets.dart';
import 'package:json_annotation/json_annotation.dart';
import '../gen/color.dart';
import '../gen/composite.dart';
import '../gen/control.dart';
import '../gen/cursor.dart';
import '../gen/font.dart';
import '../gen/image.dart';
import '../gen/menu.dart';
import '../gen/rectangle.dart';
import '../gen/region.dart';
import '../gen/scrollbar.dart';
import '../gen/toolitem.dart';
import '../gen/widget.dart';
import '../impl/toolbar_evolve.dart';
import 'widgets.dart';

part 'toolbar.g.dart';

class ToolBarSwt<V extends VToolBar> extends CompositeSwt<V> {
  const ToolBarSwt({super.key, required super.value});

  @override
  State createState() => ToolBarImpl<ToolBarSwt<VToolBar>, VToolBar>();
}

@JsonSerializable()
class VToolBar extends VComposite {
  VToolBar() : this.empty();
  VToolBar.empty() {
    swt = "ToolBar";
  }

  List<VToolItem>? items;

  @override
  void copyFrom(VWidget other) {
    super.copyFrom(other);
    if (other is VToolBar) {
      items = other.items;
    }
  }

  @override
  void readProperty(String key, Map<String, dynamic> json) {
    switch (key) {
      case 'items':
        items = (json['items'] as List<dynamic>?)
            ?.map((e) => VToolItem.fromJson(e as Map<String, dynamic>))
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

  factory VToolBar.fromJson(Map<String, dynamic> json) =>
      _$VToolBarFromJson(json)..isReference = json.containsKey('_r');
  Map<String, dynamic> toJson() => _$VToolBarToJson(this);
}
