import 'package:flutter/widgets.dart';
import 'package:json_annotation/json_annotation.dart';
import '../gen/color.dart';
import '../gen/composite.dart';
import '../gen/control.dart';
import '../gen/rectangle.dart';
import '../gen/toolitem.dart';
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

  factory VToolBar.fromJson(Map<String, dynamic> json) =>
      _$VToolBarFromJson(json);
  Map<String, dynamic> toJson() => _$VToolBarToJson(this);
}
