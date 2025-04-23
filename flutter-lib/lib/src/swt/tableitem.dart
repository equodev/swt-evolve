import 'package:json_annotation/json_annotation.dart';
import 'package:flutter/widgets.dart';
import '../swt/item.dart';
import '../impl/tableitem_impl.dart';
import '../widgets.dart';
import 'layout.dart';
import 'widget.dart';
import 'rectangle.dart';

part 'tableitem.g.dart';

class TableItemSwt<V extends TableItemValue> extends ItemSwt<V> {
  const TableItemSwt({super.key, required super.value});

  @override
  State createState() =>
      TableItemImpl<TableItemSwt<TableItemValue>, TableItemValue>();
}

@JsonSerializable()
class TableItemValue extends ItemValue {
  TableItemValue() : this.empty();
  TableItemValue.empty() {
    swt = "TableItem";
  }

  bool? checked;
  bool? grayed;
  int? imageIndent;
  List<String?>? texts;

  factory TableItemValue.fromJson(Map<String, dynamic> json) =>
      _$TableItemValueFromJson(json);
  Map<String, dynamic> toJson() => _$TableItemValueToJson(this);
}
