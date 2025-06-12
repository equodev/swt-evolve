import 'package:json_annotation/json_annotation.dart';
import 'package:flutter/widgets.dart';
import 'composite.dart';
import 'widget.dart';
import 'layout.dart';
import '../widgets.dart';

part 'rowlayout.g.dart';

abstract class RowLayoutSwt extends StatelessWidget {
  final CompositeValue composite;
  final RowLayoutValue value;
  final List<WidgetValue> children;

  const RowLayoutSwt(
      {super.key,
      required this.value,
      required this.children,
      required this.composite});
}

@JsonSerializable()
class RowLayoutValue extends LayoutValue {
  RowLayoutValue() : this.empty();
  RowLayoutValue.empty() {
    swt = "RowLayout";
  }

  int type = 0;
  int marginWidth = 0;
  int marginHeight = 0;
  int spacing = 0;
  bool wrap = false;
  bool pack = false;
  bool fill = false;
  bool center = false;
  bool justify = false;
  int marginLeft = 0;
  int marginTop = 0;
  int marginRight = 0;
  int marginBottom = 0;

  factory RowLayoutValue.fromJson(Map<String, dynamic> json) =>
      _$RowLayoutValueFromJson(json);
  Map<String, dynamic> toJson() => _$RowLayoutValueToJson(this);
}
