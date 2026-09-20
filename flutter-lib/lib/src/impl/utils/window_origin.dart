import 'package:flutter/widgets.dart';

/// Where the window rendering this subtree starts on screen.
///
/// Java works in screen coordinates -- a popup's `location`, a drag's drop target -- while a Flutter
/// client positions from its own window's top-left. In the Display's window those two agreed only
/// because the main shell was pinned to the origin; with more than one window they never do. Whoever
/// roots a window publishes its origin here, and anything consuming a screen coordinate subtracts it.
class WindowOriginScope extends InheritedWidget {
  final Offset origin;

  const WindowOriginScope({super.key, required this.origin, required super.child});

  /// The origin of the enclosing window, or [Offset.zero] when nobody published one -- which is the
  /// right answer for a window that does sit at the screen origin, and harmless otherwise.
  static Offset of(BuildContext context) =>
      context.dependOnInheritedWidgetOfExactType<WindowOriginScope>()?.origin ?? Offset.zero;

  @override
  bool updateShouldNotify(WindowOriginScope oldWidget) => origin != oldWidget.origin;
}
