import 'package:json_annotation/json_annotation.dart';
import 'package:flutter/widgets.dart';
import '../swt/composite.dart';
import '../impl/expandbar_impl.dart';
import '../comm/comm.dart';
import '../widgets.dart';
import 'layout.dart';
import 'menu.dart';
import 'widget.dart';
import 'rectangle.dart';

part 'expandbar.g.dart';

class ExpandBarSwt<V extends ExpandBarValue> extends CompositeSwt<V> {
  const ExpandBarSwt({super.key, required super.value});

  @override
  State createState() =>
      ExpandBarImpl<ExpandBarSwt<ExpandBarValue>, ExpandBarValue>();

  void sendExpandExpand(V val, Object? payload) {
    sendEvent(val, "Expand/Expand", payload);
  }

  void sendExpandCollapse(V val, Object? payload) {
    sendEvent(val, "Expand/Collapse", payload);
  }
}

@JsonSerializable()
class ExpandBarValue extends CompositeValue {
  ExpandBarValue() : this.empty();
  ExpandBarValue.empty() {
    swt = "ExpandBar";
  }

  int? spacing;

  factory ExpandBarValue.fromJson(Map<String, dynamic> json) =>
      _$ExpandBarValueFromJson(json);
  Map<String, dynamic> toJson() => _$ExpandBarValueToJson(this);
}
