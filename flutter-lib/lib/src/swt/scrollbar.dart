import 'package:json_annotation/json_annotation.dart';
import 'package:flutter/widgets.dart';
import '../swt/widget.dart';
import '../impl/scrollbar_impl.dart';
import '../comm/comm.dart';
import '../widgets.dart';
import 'layout.dart';
import 'widget.dart';
import 'rectangle.dart';

part 'scrollbar.g.dart';

class ScrollBarSwt<V extends ScrollBarValue> extends WidgetSwt<V> {
  const ScrollBarSwt({super.key, required super.value});

  @override
  State createState() =>
      ScrollBarImpl<ScrollBarSwt<ScrollBarValue>, ScrollBarValue>();

  void sendSelectionSelection(V val, Object? payload) {
    sendEvent(val, "Selection/Selection", payload);
  }

  void sendSelectionDefaultSelection(V val, Object? payload) {
    sendEvent(val, "Selection/DefaultSelection", payload);
  }
}

@JsonSerializable()
class ScrollBarValue extends WidgetValue {
  ScrollBarValue() : this.empty();
  ScrollBarValue.empty() {
    swt = "ScrollBar";
  }

  bool? enabled;
  int? increment;
  int? maximum;
  int? minimum;
  int? pageIncrement;
  int? selection;
  int? thumb;
  bool? visible;

  factory ScrollBarValue.fromJson(Map<String, dynamic> json) =>
      _$ScrollBarValueFromJson(json);
  Map<String, dynamic> toJson() => _$ScrollBarValueToJson(this);
}
