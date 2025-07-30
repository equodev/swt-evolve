import 'package:json_annotation/json_annotation.dart';
import 'package:flutter/widgets.dart';
import 'composite.dart';
import 'widget.dart';
import 'layout.dart';
import '../widgets.dart';

part 'filllayout.g.dart';

abstract class FillLayoutSwt extends StatelessWidget {
  final CompositeValue composite;
  final FillLayoutValue value;
  final List<WidgetValue> children;

  const FillLayoutSwt(
      {super.key,
      required this.value,
      required this.children,
      required this.composite});
}

@JsonSerializable()
class FillLayoutValue extends LayoutValue {
  FillLayoutValue() : this.empty();
  FillLayoutValue.empty() {
    swt = "FillLayout";
  }

  int type = 0;
  int marginWidth = 0;
  int marginHeight = 0;
  int spacing = 0;

  factory FillLayoutValue.fromJson(Map<String, dynamic> json) =>
      _$FillLayoutValueFromJson(json);
  Map<String, dynamic> toJson() => _$FillLayoutValueToJson(this);
}
