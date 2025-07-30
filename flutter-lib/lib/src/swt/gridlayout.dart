import 'package:json_annotation/json_annotation.dart';
import 'package:flutter/widgets.dart';
import 'composite.dart';
import 'widget.dart';
import 'layout.dart';
import '../widgets.dart';

part 'gridlayout.g.dart';

abstract class GridLayoutSwt extends StatelessWidget {
  final CompositeValue composite;
  final GridLayoutValue value;
  final List<WidgetValue> children;

  const GridLayoutSwt(
      {super.key,
      required this.value,
      required this.children,
      required this.composite});
}

@JsonSerializable()
class GridLayoutValue extends LayoutValue {
  GridLayoutValue() : this.empty();
  GridLayoutValue.empty() {
    swt = "GridLayout";
  }

  int numColumns = 0;
  bool makeColumnsEqualWidth = false;
  int marginWidth = 0;
  int marginHeight = 0;
  int marginLeft = 0;
  int marginTop = 0;
  int marginRight = 0;
  int marginBottom = 0;
  int horizontalSpacing = 0;
  int verticalSpacing = 0;

  factory GridLayoutValue.fromJson(Map<String, dynamic> json) =>
      _$GridLayoutValueFromJson(json);
  Map<String, dynamic> toJson() => _$GridLayoutValueToJson(this);
}
