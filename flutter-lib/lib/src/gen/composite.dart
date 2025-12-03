import 'package:flutter/widgets.dart';
import 'package:json_annotation/json_annotation.dart';
import '../gen/color.dart';
import '../gen/control.dart';
import '../gen/font.dart';
import '../gen/image.dart';
import '../gen/menu.dart';
import '../gen/rectangle.dart';
import '../gen/scrollable.dart';
import '../impl/composite_evolve.dart';
import 'widgets.dart';

part 'composite.g.dart';

class CompositeSwt<V extends VComposite> extends ScrollableSwt<V> {
  const CompositeSwt({super.key, required super.value});

  @override
  State createState() => CompositeImpl<CompositeSwt<VComposite>, VComposite>();
}

@JsonSerializable()
class VComposite extends VScrollable {
  VComposite() : this.empty();
  VComposite.empty() {
    swt = "Composite";
  }

  int? backgroundMode;
  List<VControl>? children;
  bool? layoutDeferred;
  List<VControl>? tabList;

  factory VComposite.fromJson(Map<String, dynamic> json) =>
      _$VCompositeFromJson(json);
  Map<String, dynamic> toJson() => _$VCompositeToJson(this);
}
