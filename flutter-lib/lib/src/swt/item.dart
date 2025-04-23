import 'package:json_annotation/json_annotation.dart';
import '../swt/widget.dart';
import '../widgets.dart';
import 'layout.dart';
import 'widget.dart';
import 'rectangle.dart';

part 'item.g.dart';

abstract class ItemSwt<V extends ItemValue> extends WidgetSwt<V> {
  const ItemSwt({super.key, required super.value});
}

@JsonSerializable()
class ItemValue extends WidgetValue {
  ItemValue() : this.empty();
  ItemValue.empty() {
    swt = "Item";
  }

  String? text;

  factory ItemValue.fromJson(Map<String, dynamic> json) =>
      _$ItemValueFromJson(json);
  Map<String, dynamic> toJson() => _$ItemValueToJson(this);
}
