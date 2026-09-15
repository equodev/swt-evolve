import 'package:json_annotation/json_annotation.dart';
import '../gen/image.dart';
import '../gen/widget.dart';
import 'widgets.dart';

part 'item.g.dart';

abstract class ItemSwt<V extends VItem> extends WidgetSwt<V> {
  const ItemSwt({super.key, required super.value});
}

@JsonSerializable()
class VItem extends VWidget {
  VItem() : this.empty();
  VItem.empty() {
    swt = "Item";
  }

  VImage? image;
  String? text;

  @override
  void copyFrom(VWidget other) {
    super.copyFrom(other);
    if (other is VItem) {
      image = other.image;
      text = other.text;
    }
  }

  @override
  void readProperty(String key, Map<String, dynamic> json) {
    switch (key) {
      case 'image':
        image = json['image'] == null
            ? null
            : VImage.fromJson(json['image'] as Map<String, dynamic>);
      case 'text':
        text = json['text'] as String?;
      default:
        super.readProperty(key, json);
    }
  }

  factory VItem.fromJson(Map<String, dynamic> json) =>
      _$VItemFromJson(json)..isReference = json.containsKey('_r');
  Map<String, dynamic> toJson() => _$VItemToJson(this);
}
