import 'package:json_annotation/json_annotation.dart';
import 'package:flutter/widgets.dart';
import '../swt/item.dart';
import '../impl/toolitem_impl.dart';
import '../comm/comm.dart';
import '../widgets.dart';
import 'layout.dart';
import 'widget.dart';
import 'rectangle.dart';

part 'toolitem.g.dart';

class ToolItemSwt<V extends ToolItemValue> extends ItemSwt<V> {
  const ToolItemSwt({super.key, required super.value});

  @override
  State createState() =>
      ToolItemImpl<ToolItemSwt<ToolItemValue>, ToolItemValue>();

  void sendSelectionSelection(V val, Object? payload) {
    sendEvent(val, "Selection/Selection", payload);
  }

  void sendSelectionDefaultSelection(V val, Object? payload) {
    sendEvent(val, "Selection/DefaultSelection", payload);
  }
}

@JsonSerializable()
class ToolItemValue extends ItemValue {
  ToolItemValue() : this.empty();
  ToolItemValue.empty() {
    swt = "ToolItem";
  }

  bool? enabled;
  bool? selection;
  String? toolTipText;
  int? width;

  factory ToolItemValue.fromJson(Map<String, dynamic> json) =>
      _$ToolItemValueFromJson(json);
  Map<String, dynamic> toJson() => _$ToolItemValueToJson(this);
}
