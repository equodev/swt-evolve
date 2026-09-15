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
import '../impl/button_evolve.dart';
import 'event.dart';
import 'widgets.dart';

part 'button.g.dart';

class ButtonSwt<V extends VButton> extends ControlSwt<V> {
  const ButtonSwt({super.key, required super.value});

  @override
  State createState() => ButtonImpl<ButtonSwt<VButton>, VButton>();

  void sendSelectionDefaultSelection(V val, VEvent? payload) {
    sendEvent(val, "Selection/DefaultSelection", payload);
  }

  void sendSelectionSelection(V val, VEvent? payload) {
    sendEvent(val, "Selection/Selection", payload);
  }
}

@JsonSerializable()
class VButton extends VControl {
  VButton() : this.empty();
  VButton.empty() {
    swt = "Button";
  }

  int? alignment;
  bool? grayed;
  VImage? image;
  bool? primary;
  bool? selection;
  String? text;

  @override
  void copyFrom(VWidget other) {
    super.copyFrom(other);
    if (other is VButton) {
      alignment = other.alignment;
      grayed = other.grayed;
      image = other.image;
      primary = other.primary;
      selection = other.selection;
      text = other.text;
    }
  }

  @override
  void readProperty(String key, Map<String, dynamic> json) {
    switch (key) {
      case 'alignment':
        alignment = (json['alignment'] as num?)?.toInt();
      case 'grayed':
        grayed = json['grayed'] as bool?;
      case 'image':
        image = json['image'] == null
            ? null
            : VImage.fromJson(json['image'] as Map<String, dynamic>);
      case 'primary':
        primary = json['primary'] as bool?;
      case 'selection':
        selection = json['selection'] as bool?;
      case 'text':
        text = json['text'] as String?;
      default:
        super.readProperty(key, json);
    }
  }

  factory VButton.fromJson(Map<String, dynamic> json) =>
      _$VButtonFromJson(json)..isReference = json.containsKey('_r');
  Map<String, dynamic> toJson() => _$VButtonToJson(this);
}
