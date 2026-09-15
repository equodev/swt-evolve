import 'package:flutter/widgets.dart';
import 'package:json_annotation/json_annotation.dart';
import '../gen/canvas.dart';
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
import '../impl/decorations_evolve.dart';
import 'widgets.dart';

part 'decorations.g.dart';

class DecorationsSwt<V extends VDecorations> extends CanvasSwt<V> {
  const DecorationsSwt({super.key, required super.value});

  @override
  State createState() =>
      DecorationsImpl<DecorationsSwt<VDecorations>, VDecorations>();
}

@JsonSerializable()
class VDecorations extends VCanvas {
  VDecorations() : this.empty();
  VDecorations.empty() {
    swt = "Decorations";
  }

  VMenu? menuBar;
  String? text;

  @override
  void copyFrom(VWidget other) {
    super.copyFrom(other);
    if (other is VDecorations) {
      menuBar = other.menuBar;
      text = other.text;
    }
  }

  @override
  void readProperty(String key, Map<String, dynamic> json) {
    switch (key) {
      case 'menuBar':
        menuBar = json['menuBar'] == null
            ? null
            : VMenu.fromJson(json['menuBar'] as Map<String, dynamic>);
      case 'text':
        text = json['text'] as String?;
      default:
        super.readProperty(key, json);
    }
  }

  @override
  void adoptChildren(VWidget Function(VWidget) adopt) {
    super.adoptChildren(adopt);
    menuBar = VWidget.adoptOne(menuBar, adopt);
  }

  factory VDecorations.fromJson(Map<String, dynamic> json) =>
      _$VDecorationsFromJson(json)..isReference = json.containsKey('_r');
  Map<String, dynamic> toJson() => _$VDecorationsToJson(this);
}
