import 'package:flutter/widgets.dart';
import 'package:json_annotation/json_annotation.dart';
import '../comm/comm.dart';
import '../gen/widget.dart';
import '../impl/scrollbar_evolve.dart';
import 'event.dart';
import 'widgets.dart';

part 'scrollbar.g.dart';

class ScrollBarSwt<V extends VScrollBar> extends WidgetSwt<V> {
  const ScrollBarSwt({super.key, required super.value});

  @override
  State createState() => ScrollBarImpl<ScrollBarSwt<VScrollBar>, VScrollBar>();

  void sendSelectionDefaultSelection(V val, VEvent? payload) {
    sendEvent(val, "Selection/DefaultSelection", payload);
  }

  void sendSelectionSelection(V val, VEvent? payload) {
    sendEvent(val, "Selection/Selection", payload);
  }
}

@JsonSerializable()
class VScrollBar extends VWidget {
  VScrollBar() : this.empty();
  VScrollBar.empty() {
    swt = "ScrollBar";
  }

  bool? enabled;
  int? increment;
  int? maximum;
  int? minimum;
  int? selection;
  int? thumb;
  bool? visible;

  @override
  void copyFrom(VWidget other) {
    super.copyFrom(other);
    if (other is VScrollBar) {
      enabled = other.enabled;
      increment = other.increment;
      maximum = other.maximum;
      minimum = other.minimum;
      selection = other.selection;
      thumb = other.thumb;
      visible = other.visible;
    }
  }

  @override
  void readProperty(String key, Map<String, dynamic> json) {
    switch (key) {
      case 'enabled':
        enabled = json['enabled'] as bool?;
      case 'increment':
        increment = (json['increment'] as num?)?.toInt();
      case 'maximum':
        maximum = (json['maximum'] as num?)?.toInt();
      case 'minimum':
        minimum = (json['minimum'] as num?)?.toInt();
      case 'selection':
        selection = (json['selection'] as num?)?.toInt();
      case 'thumb':
        thumb = (json['thumb'] as num?)?.toInt();
      case 'visible':
        visible = json['visible'] as bool?;
      default:
        super.readProperty(key, json);
    }
  }

  factory VScrollBar.fromJson(Map<String, dynamic> json) =>
      _$VScrollBarFromJson(json)..isReference = json.containsKey('_r');
  Map<String, dynamic> toJson() => _$VScrollBarToJson(this);
}
