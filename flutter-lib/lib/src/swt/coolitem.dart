import 'package:json_annotation/json_annotation.dart';
import 'package:flutter/widgets.dart';
import '../swt/item.dart';
import '../impl/coolitem_impl.dart';
import '../comm/comm.dart';
import '../widgets.dart';
import 'layout.dart';
import 'widget.dart';
import 'rectangle.dart';

part 'coolitem.g.dart';

class CoolItemSwt<V extends CoolItemValue> extends ItemSwt<V> {
  const CoolItemSwt({super.key, required super.value});

  @override
  State createState() =>
      CoolItemImpl<CoolItemSwt<CoolItemValue>, CoolItemValue>();

  void sendSelectionSelection(V val, Object? payload) {
    sendEvent(val, "Selection/Selection", payload);
  }

  void sendSelectionDefaultSelection(V val, Object? payload) {
    sendEvent(val, "Selection/DefaultSelection", payload);
  }
}

@JsonSerializable()
class CoolItemValue extends ItemValue {
  CoolItemValue() : this.empty();
  CoolItemValue.empty() {
    swt = "CoolItem";
  }

  bool? enabled;
  bool? selection;
  String? toolTipText;
  int? width;

  factory CoolItemValue.fromJson(Map<String, dynamic> json) =>
      _$CoolItemValueFromJson(json);
  Map<String, dynamic> toJson() => _$CoolItemValueToJson(this);
}
