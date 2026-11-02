import 'package:flutter/widgets.dart';
import '../comm/widget_wire.dart';
import '../gen/canvas.dart';
import '../gen/caret.dart';
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
import '../gen/widgets.dart';
import '../impl/animatedprogress_evolve.dart';
import 'widgets.dart';

class AnimatedProgressSwt<V extends VAnimatedProgress> extends CanvasSwt<V> {
  const AnimatedProgressSwt({super.key, required super.value});

  @override
  State createState() =>
      AnimatedProgressImpl<
        AnimatedProgressSwt<VAnimatedProgress>,
        VAnimatedProgress
      >();
}

class VAnimatedProgress extends VCanvas {
  VAnimatedProgress() : this.empty();
  VAnimatedProgress.empty() {
    swt = "AnimatedProgress";
  }
}
