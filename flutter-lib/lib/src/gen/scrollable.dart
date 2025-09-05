import 'package:json_annotation/json_annotation.dart';
import '../gen/color.dart';
import '../gen/control.dart';
import '../gen/image.dart';
import '../gen/rectangle.dart';
import 'widgets.dart';

part 'scrollable.g.dart';

abstract class ScrollableSwt<V extends VScrollable> extends ControlSwt<V> {
  const ScrollableSwt({super.key, required super.value});
}

@JsonSerializable()
class VScrollable extends VControl {
  VScrollable() : this.empty();
  VScrollable.empty() {
    swt = "Scrollable";
  }

  int? scrollbarsMode;

  factory VScrollable.fromJson(Map<String, dynamic> json) =>
      _$VScrollableFromJson(json);
  Map<String, dynamic> toJson() => _$VScrollableToJson(this);
}
