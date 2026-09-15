import 'package:json_annotation/json_annotation.dart';
import '../gen/color.dart';
import '../gen/control.dart';
import '../gen/cursor.dart';
import '../gen/font.dart';
import '../gen/image.dart';
import '../gen/menu.dart';
import '../gen/rectangle.dart';
import '../gen/region.dart';
import '../gen/scrollbar.dart';
import '../gen/widget.dart';
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

  VScrollBar? horizontalBar;
  VScrollBar? verticalBar;

  @override
  void copyFrom(VWidget other) {
    super.copyFrom(other);
    if (other is VScrollable) {
      horizontalBar = other.horizontalBar;
      verticalBar = other.verticalBar;
    }
  }

  @override
  void readProperty(String key, Map<String, dynamic> json) {
    switch (key) {
      case 'horizontalBar':
        horizontalBar = json['horizontalBar'] == null
            ? null
            : VScrollBar.fromJson(
                json['horizontalBar'] as Map<String, dynamic>,
              );
      case 'verticalBar':
        verticalBar = json['verticalBar'] == null
            ? null
            : VScrollBar.fromJson(json['verticalBar'] as Map<String, dynamic>);
      default:
        super.readProperty(key, json);
    }
  }

  @override
  void adoptChildren(VWidget Function(VWidget) adopt) {
    super.adoptChildren(adopt);
    horizontalBar = VWidget.adoptOne(horizontalBar, adopt);
    verticalBar = VWidget.adoptOne(verticalBar, adopt);
  }

  factory VScrollable.fromJson(Map<String, dynamic> json) =>
      _$VScrollableFromJson(json)..isReference = json.containsKey('_r');
  Map<String, dynamic> toJson() => _$VScrollableToJson(this);
}
