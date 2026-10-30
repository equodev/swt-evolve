import 'package:flutter/widgets.dart';

/// Keeps every field under it that is otherwise drawn at rest live, so a test can compare a field at
/// rest with the live one.
class ForceLive extends InheritedWidget {
  const ForceLive({super.key, required super.child});

  /// [child] under a [ForceLive] when [live], else [child] alone.
  static Widget when(bool live, Widget child) => live ? ForceLive(child: child) : child;

  /// Whether the field built in [context] has to stay live.
  static bool isOn(BuildContext context) => context.getInheritedWidgetOfExactType<ForceLive>() != null;

  @override
  bool updateShouldNotify(ForceLive oldWidget) => false;
}
