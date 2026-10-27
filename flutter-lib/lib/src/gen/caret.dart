import 'package:flutter/widgets.dart';
import 'package:json_annotation/json_annotation.dart';
import '../gen/rectangle.dart';
import '../gen/widget.dart';
import '../impl/caret_evolve.dart';
import 'widgets.dart';

part 'caret.g.dart';

class CaretSwt<V extends VCaret> extends WidgetSwt<V> {
  const CaretSwt({super.key, required super.value});

  @override
  State createState() => CaretImpl<CaretSwt<VCaret>, VCaret>();
}

@JsonSerializable()
class VCaret extends VWidget {
  VCaret() : this.empty();
  VCaret.empty() {
    swt = "Caret";
  }

  VRectangle? bounds;

  @override
  void copyFrom(VWidget other) {
    super.copyFrom(other);
    if (other is VCaret) {
      bounds = other.bounds;
    }
  }

  @override
  void readProperty(String key, Map<String, dynamic> json) {
    switch (key) {
      case 'bounds':
        bounds = json['bounds'] == null
            ? null
            : VRectangle.fromJson(json['bounds'] as Map<String, dynamic>);
      default:
        super.readProperty(key, json);
    }
  }

  factory VCaret.fromJson(Map<String, dynamic> json) =>
      _$VCaretFromJson(json)..isReference = json.containsKey('_r');
  Map<String, dynamic> toJson() => _$VCaretToJson(this);
}
