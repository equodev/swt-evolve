import 'package:json_annotation/json_annotation.dart';
import 'package:flutter/widgets.dart';
import 'composite.dart';
import 'widget.dart';
import 'layout.dart';
import '../widgets.dart';

part 'sashformlayout.g.dart';

abstract class SashFormLayoutSwt extends StatelessWidget {
  final CompositeValue composite;
  final SashFormLayoutValue value;
  final List<WidgetValue> children;

  const SashFormLayoutSwt(
      {super.key,
      required this.value,
      required this.children,
      required this.composite});
}

@JsonSerializable()
class SashFormLayoutValue extends LayoutValue {
  SashFormLayoutValue() : this.empty();
  SashFormLayoutValue.empty() {
    swt = "SashFormLayout";
  }

  factory SashFormLayoutValue.fromJson(Map<String, dynamic> json) =>
      _$SashFormLayoutValueFromJson(json);
  Map<String, dynamic> toJson() => _$SashFormLayoutValueToJson(this);
}
