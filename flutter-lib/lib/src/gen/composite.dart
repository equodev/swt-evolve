import 'package:flutter/widgets.dart';
import 'package:json_annotation/json_annotation.dart';
import '../gen/color.dart';
import '../gen/control.dart';
import '../gen/cursor.dart';
import '../gen/font.dart';
import '../gen/image.dart';
import '../gen/menu.dart';
import '../gen/rectangle.dart';
import '../gen/region.dart';
import '../gen/scrollable.dart';
import '../gen/scrollbar.dart';
import '../gen/widget.dart';
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

  @override
  void copyFrom(VWidget other) {
    super.copyFrom(other);
    if (other is VComposite) {
      backgroundMode = other.backgroundMode;
      children = other.children;
    }
  }

  @override
  void readProperty(String key, Map<String, dynamic> json) {
    switch (key) {
      case 'backgroundMode':
        backgroundMode = (json['backgroundMode'] as num?)?.toInt();
      case 'children':
        children = (json['children'] as List<dynamic>?)
            ?.map((e) => VControl.fromJson(e as Map<String, dynamic>))
            .toList();
      default:
        super.readProperty(key, json);
    }
  }

  @override
  void adoptChildren(VWidget Function(VWidget) adopt) {
    super.adoptChildren(adopt);
    VWidget.adoptEach(children, adopt);
  }

  factory VComposite.fromJson(Map<String, dynamic> json) =>
      _$VCompositeFromJson(json)..isReference = json.containsKey('_r');
  Map<String, dynamic> toJson() => _$VCompositeToJson(this);
}
