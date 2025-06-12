import 'package:json_annotation/json_annotation.dart';
import '../swt/control.dart';
import '../widgets.dart';
import 'layout.dart';
import 'menu.dart';
import 'widget.dart';
import 'rectangle.dart';

part 'scrollable.g.dart';

abstract class ScrollableSwt<V extends ScrollableValue> extends ControlSwt<V> {
  const ScrollableSwt({super.key, required super.value});
}

@JsonSerializable()
class ScrollableValue extends ControlValue {
  ScrollableValue() : this.empty();
  ScrollableValue.empty() {
    swt = "Scrollable";
  }

  factory ScrollableValue.fromJson(Map<String, dynamic> json) =>
      _$ScrollableValueFromJson(json);
  Map<String, dynamic> toJson() => _$ScrollableValueToJson(this);
}
