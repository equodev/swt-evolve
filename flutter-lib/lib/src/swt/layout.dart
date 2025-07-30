import 'package:json_annotation/json_annotation.dart';
import 'package:flutter/widgets.dart';
import 'composite.dart';
import 'widget.dart';
import 'layout.dart';
import '../widgets.dart';

abstract class LayoutSwt extends StatelessWidget {
  final CompositeValue composite;
  final LayoutValue value;
  final List<WidgetValue> children;

  const LayoutSwt(
      {super.key,
      required this.value,
      required this.children,
      required this.composite});
}

class LayoutValue {
  LayoutValue() : this.empty();
  LayoutValue.empty() : swt = "Layout";

  String swt;

  factory LayoutValue.fromJson(Map<String, dynamic> json) =>
      mapLayoutValue(json);
  Map<String, dynamic> toJson() =>
      throw UnsupportedError("Unsupported toJson in Layout");
}
