import 'package:json_annotation/json_annotation.dart';
import 'package:flutter/widgets.dart';
import '../swt/composite.dart';
import '../impl/toolbar_impl.dart';
import '../widgets.dart';
import 'layout.dart';
import 'menu.dart';
import 'widget.dart';
import 'rectangle.dart';

part 'toolbar.g.dart';

class ToolBarSwt<V extends ToolBarValue> extends CompositeSwt<V> {
  const ToolBarSwt({super.key, required super.value});

  @override
  State createState() => ToolBarImpl<ToolBarSwt<ToolBarValue>, ToolBarValue>();
}

@JsonSerializable()
class ToolBarValue extends CompositeValue {
  ToolBarValue() : this.empty();
  ToolBarValue.empty() {
    swt = "ToolBar";
  }

  factory ToolBarValue.fromJson(Map<String, dynamic> json) =>
      _$ToolBarValueFromJson(json);
  Map<String, dynamic> toJson() => _$ToolBarValueToJson(this);
}
