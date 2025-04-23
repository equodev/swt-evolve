import 'package:json_annotation/json_annotation.dart';
import 'package:flutter/widgets.dart';
import '../swt/item.dart';
import '../impl/ctabitem_impl.dart';
import '../widgets.dart';
import 'layout.dart';
import 'widget.dart';
import 'rectangle.dart';

part 'ctabitem.g.dart';

class CTabItemSwt<V extends CTabItemValue> extends ItemSwt<V> {
  const CTabItemSwt({super.key, required super.value});

  @override
  State createState() =>
      CTabItemImpl<CTabItemSwt<CTabItemValue>, CTabItemValue>();
}

@JsonSerializable()
class CTabItemValue extends ItemValue {
  CTabItemValue() : this.empty();
  CTabItemValue.empty() {
    swt = "CTabItem";
  }

  bool? showClose;
  String? toolTipText;

  factory CTabItemValue.fromJson(Map<String, dynamic> json) =>
      _$CTabItemValueFromJson(json);
  Map<String, dynamic> toJson() => _$CTabItemValueToJson(this);
}
