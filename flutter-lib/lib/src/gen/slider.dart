import 'package:flutter/widgets.dart';
import 'package:json_annotation/json_annotation.dart';
import '../comm/comm.dart';
import '../gen/color.dart';
import '../gen/control.dart';
import '../gen/cursor.dart';
import '../gen/font.dart';
import '../gen/image.dart';
import '../gen/menu.dart';
import '../gen/rectangle.dart';
import '../gen/region.dart';
import '../gen/widget.dart';
import '../impl/slider_evolve.dart';
import 'event.dart';
import 'widgets.dart';

part 'slider.g.dart';

class SliderSwt<V extends VSlider> extends ControlSwt<V> {
  const SliderSwt({super.key, required super.value});

  @override
  State createState() => SliderImpl<SliderSwt<VSlider>, VSlider>();

  void sendSelectionDefaultSelection(V val, VEvent? payload) {
    sendEvent(val, "Selection/DefaultSelection", payload);
  }

  void sendSelectionSelection(V val, VEvent? payload) {
    sendEvent(val, "Selection/Selection", payload);
  }
}

@JsonSerializable()
class VSlider extends VControl {
  VSlider() : this.empty();
  VSlider.empty() {
    swt = "Slider";
  }

  int? increment;
  int? maximum;
  int? minimum;
  int? pageIncrement;
  int? selection;
  int? thumb;

  @override
  void copyFrom(VWidget other) {
    super.copyFrom(other);
    if (other is VSlider) {
      increment = other.increment;
      maximum = other.maximum;
      minimum = other.minimum;
      pageIncrement = other.pageIncrement;
      selection = other.selection;
      thumb = other.thumb;
    }
  }

  @override
  void readProperty(String key, Map<String, dynamic> json) {
    switch (key) {
      case 'increment':
        increment = (json['increment'] as num?)?.toInt();
      case 'maximum':
        maximum = (json['maximum'] as num?)?.toInt();
      case 'minimum':
        minimum = (json['minimum'] as num?)?.toInt();
      case 'pageIncrement':
        pageIncrement = (json['pageIncrement'] as num?)?.toInt();
      case 'selection':
        selection = (json['selection'] as num?)?.toInt();
      case 'thumb':
        thumb = (json['thumb'] as num?)?.toInt();
      default:
        super.readProperty(key, json);
    }
  }

  factory VSlider.fromJson(Map<String, dynamic> json) =>
      _$VSliderFromJson(json)..isReference = json.containsKey('_r');
  Map<String, dynamic> toJson() => _$VSliderToJson(this);
}
