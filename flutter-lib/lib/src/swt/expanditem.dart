import 'package:json_annotation/json_annotation.dart';
import 'package:flutter/widgets.dart';
import '../swt/item.dart';
import '../impl/expanditem_impl.dart';
import '../widgets.dart';
import 'layout.dart';
import 'widget.dart';
import 'rectangle.dart';

part 'expanditem.g.dart';

class ExpandItemSwt<V extends ExpandItemValue> extends ItemSwt<V> {
  const ExpandItemSwt({super.key, required super.value});

  @override
  State createState() =>
      ExpandItemImpl<ExpandItemSwt<ExpandItemValue>, ExpandItemValue>();
}

@JsonSerializable()
class ExpandItemValue extends ItemValue {
  ExpandItemValue() : this.empty();
  ExpandItemValue.empty() {
    swt = "ExpandItem";
  }

  bool? expanded;
  int? height;

  factory ExpandItemValue.fromJson(Map<String, dynamic> json) =>
      _$ExpandItemValueFromJson(json);
  Map<String, dynamic> toJson() => _$ExpandItemValueToJson(this);
}
