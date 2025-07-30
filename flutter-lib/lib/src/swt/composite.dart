import 'package:json_annotation/json_annotation.dart';
import 'package:flutter/widgets.dart';
import '../swt/scrollable.dart';
import '../impl/composite_impl.dart';
import '../swt/layout.dart';
import '../widgets.dart';
import 'layout.dart';
import 'menu.dart';
import 'widget.dart';
import 'rectangle.dart';

part 'composite.g.dart';

class CompositeSwt<V extends CompositeValue> extends ScrollableSwt<V> {
  const CompositeSwt({super.key, required super.value});

  @override
  State createState() =>
      CompositeImpl<CompositeSwt<CompositeValue>, CompositeValue>();
}

@JsonSerializable()
class CompositeValue extends ScrollableValue {
  CompositeValue() : this.empty();
  CompositeValue.empty() {
    swt = "Composite";
  }

  int? backgroundMode;
  LayoutValue? layout;
  bool? layoutDeferred;

  factory CompositeValue.fromJson(Map<String, dynamic> json) =>
      _$CompositeValueFromJson(json);
  Map<String, dynamic> toJson() => _$CompositeValueToJson(this);
}
