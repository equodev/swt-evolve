import 'package:flutter/widgets.dart';
import 'package:json_annotation/json_annotation.dart';
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
import '../impl/animatedprogress_evolve.dart';
import 'widgets.dart';

part 'animatedprogress.g.dart';

class AnimatedProgressSwt<V extends VAnimatedProgress> extends CanvasSwt<V> {
  const AnimatedProgressSwt({super.key, required super.value});

  @override
  State createState() =>
      AnimatedProgressImpl<
        AnimatedProgressSwt<VAnimatedProgress>,
        VAnimatedProgress
      >();
}

@JsonSerializable()
class VAnimatedProgress extends VCanvas {
  VAnimatedProgress() : this.empty();
  VAnimatedProgress.empty() {
    swt = "AnimatedProgress";
  }

  factory VAnimatedProgress.fromJson(Map<String, dynamic> json) =>
      _$VAnimatedProgressFromJson(json);
  Map<String, dynamic> toJson() => _$VAnimatedProgressToJson(this);
}
