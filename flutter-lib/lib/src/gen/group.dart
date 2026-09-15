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
import '../impl/group_evolve.dart';
import 'widgets.dart';

part 'group.g.dart';

class GroupSwt<V extends VGroup> extends CompositeSwt<V> {
  const GroupSwt({super.key, required super.value});

  @override
  State createState() => GroupImpl<GroupSwt<VGroup>, VGroup>();
}

@JsonSerializable()
class VGroup extends VComposite {
  VGroup() : this.empty();
  VGroup.empty() {
    swt = "Group";
  }

  String? text;

  @override
  void copyFrom(VWidget other) {
    super.copyFrom(other);
    if (other is VGroup) {
      text = other.text;
    }
  }

  @override
  void readProperty(String key, Map<String, dynamic> json) {
    switch (key) {
      case 'text':
        text = json['text'] as String?;
      default:
        super.readProperty(key, json);
    }
  }

  factory VGroup.fromJson(Map<String, dynamic> json) =>
      _$VGroupFromJson(json)..isReference = json.containsKey('_r');
  Map<String, dynamic> toJson() => _$VGroupToJson(this);
}
