import 'package:json_annotation/json_annotation.dart';
import 'package:flutter/widgets.dart';
import '../swt/item.dart';
import '../impl/tabitem_impl.dart';
import '../widgets.dart';
import 'layout.dart';
import 'widget.dart';
import 'rectangle.dart';

part 'tabitem.g.dart';

class TabItemSwt<V extends TabItemValue> extends ItemSwt<V> {
  const TabItemSwt({super.key, required super.value});

  @override
  State createState() => TabItemImpl<TabItemSwt<TabItemValue>, TabItemValue>();
}

@JsonSerializable()
class TabItemValue extends ItemValue {
  TabItemValue() : this.empty();
  TabItemValue.empty() {
    swt = "TabItem";
  }

  String? toolTipText;

  factory TabItemValue.fromJson(Map<String, dynamic> json) =>
      _$TabItemValueFromJson(json);
  Map<String, dynamic> toJson() => _$TabItemValueToJson(this);
}
