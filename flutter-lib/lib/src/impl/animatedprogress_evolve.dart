import '../gen/animatedprogress.dart';
import '../impl/canvas_evolve.dart';

/// SWT's AnimatedProgress is a Canvas that paints its own bar, so it is one here too.
class AnimatedProgressImpl<
  T extends AnimatedProgressSwt,
  V extends VAnimatedProgress
>
    extends CanvasImpl<T, V> {
  /// A progress bar takes no focus on a click, on any native.
  @override
  bool get focusesOnPress => false;

  @override
  void takeFocusOnPress() {}
}
