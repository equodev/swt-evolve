import 'package:json_annotation/json_annotation.dart';
import 'package:flutter/widgets.dart';
import '../swt/composite.dart';
import '../impl/group_impl.dart';
import '../widgets.dart';
import 'layout.dart';
import 'menu.dart';
import 'widget.dart';
import 'rectangle.dart';

part 'group.g.dart';

class GroupSwt<V extends GroupValue> extends CompositeSwt<V> {
  const GroupSwt({super.key, required super.value});

  @override
  State createState() => GroupImpl<GroupSwt<GroupValue>, GroupValue>();
}

@JsonSerializable()
class GroupValue extends CompositeValue {
  GroupValue() : this.empty();
  GroupValue.empty() {
    swt = "Group";
  }

  String? text;

  factory GroupValue.fromJson(Map<String, dynamic> json) =>
      _$GroupValueFromJson(json);
  Map<String, dynamic> toJson() => _$GroupValueToJson(this);
}
