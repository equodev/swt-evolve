import 'package:flutter/widgets.dart';
import 'package:json_annotation/json_annotation.dart';
import '../gen/button.dart';
import '../gen/canvas.dart';
import '../gen/caret.dart';
import '../gen/color.dart';
import '../gen/control.dart';
import '../gen/cursor.dart';
import '../gen/font.dart';
import '../gen/image.dart';
import '../gen/ime.dart';
import '../gen/menu.dart';
import '../gen/rectangle.dart';
import '../gen/region.dart';
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

  VButton? defaultButton;
  VImage? image;
  List<VImage>? images;
  bool? maximized;
  VMenu? menuBar;
  bool? minimized;
  String? text;

  factory VDecorations.fromJson(Map<String, dynamic> json) =>
      _$VDecorationsFromJson(json);
  Map<String, dynamic> toJson() => _$VDecorationsToJson(this);
}
