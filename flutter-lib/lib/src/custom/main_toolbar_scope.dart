import 'package:flutter/widgets.dart';

/// Marks the subtree rendered by the main toolbar -- the e4 top trim, whose value arrives with
/// `swt == "MainToolbar"`.
///
/// The custom tooltip and the notification popup are confined to it on purpose: a toolbar item is
/// the one place where a card-sized tooltip has room and an action worth announcing, and keeping
/// them out of the rest of the tree means turning either on cannot change how the application's own
/// widgets look. A ToolItem is the same code wherever it is mounted, so this is how it finds out
/// where that is.
class MainToolbarScope extends InheritedWidget {
  const MainToolbarScope({super.key, required super.child});

  /// True when [context] is inside the main toolbar. For use from `build`: it registers a
  /// dependency, so the widget rebuilds if it is ever moved in or out.
  static bool of(BuildContext context) =>
      context.dependOnInheritedWidgetOfExactType<MainToolbarScope>() != null;

  /// The same answer without registering a dependency, for a one-off read from an event handler --
  /// where taking a dependency would be both useless and outside the build it belongs to.
  static bool isInside(BuildContext context) =>
      context.getInheritedWidgetOfExactType<MainToolbarScope>() != null;

  @override
  bool updateShouldNotify(MainToolbarScope oldWidget) => false;
}
