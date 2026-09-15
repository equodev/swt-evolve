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
import '../gen/widget.dart';
import '../impl/scrolledcomposite_evolve.dart';
import 'widgets.dart';

part 'scrolledcomposite.g.dart';

class ScrolledCompositeSwt<V extends VScrolledComposite>
    extends CompositeSwt<V> {
  const ScrolledCompositeSwt({super.key, required super.value});

  @override
  State createState() =>
      ScrolledCompositeImpl<
        ScrolledCompositeSwt<VScrolledComposite>,
        VScrolledComposite
      >();
}

@JsonSerializable()
class VScrolledComposite extends VComposite {
  VScrolledComposite() : this.empty();
  VScrolledComposite.empty() {
    swt = "ScrolledComposite";
  }

  bool? alwaysShowScrollBars;
  VControl? content;
  bool? expandHorizontal;
  bool? expandVertical;
  int? minHeight;
  int? minWidth;

  @override
  void copyFrom(VWidget other) {
    super.copyFrom(other);
    if (other is VScrolledComposite) {
      alwaysShowScrollBars = other.alwaysShowScrollBars;
      content = other.content;
      expandHorizontal = other.expandHorizontal;
      expandVertical = other.expandVertical;
      minHeight = other.minHeight;
      minWidth = other.minWidth;
    }
  }

  @override
  void readProperty(String key, Map<String, dynamic> json) {
    switch (key) {
      case 'alwaysShowScrollBars':
        alwaysShowScrollBars = json['alwaysShowScrollBars'] as bool?;
      case 'content':
        content = json['content'] == null
            ? null
            : VControl.fromJson(json['content'] as Map<String, dynamic>);
      case 'expandHorizontal':
        expandHorizontal = json['expandHorizontal'] as bool?;
      case 'expandVertical':
        expandVertical = json['expandVertical'] as bool?;
      case 'minHeight':
        minHeight = (json['minHeight'] as num?)?.toInt();
      case 'minWidth':
        minWidth = (json['minWidth'] as num?)?.toInt();
      default:
        super.readProperty(key, json);
    }
  }

  @override
  void adoptChildren(VWidget Function(VWidget) adopt) {
    super.adoptChildren(adopt);
    content = VWidget.adoptOne(content, adopt);
  }

  factory VScrolledComposite.fromJson(Map<String, dynamic> json) =>
      _$VScrolledCompositeFromJson(json)..isReference = json.containsKey('_r');
  Map<String, dynamic> toJson() => _$VScrolledCompositeToJson(this);
}
