import 'package:flutter/widgets.dart';

/// Marks the [MenuSwt] a control mounts for its own context menu.
///
/// The Display renders every shown popup as well, so one menu has two mounted widgets. Only the
/// Display's acts on `Menu.setVisible(true)`; this one exists for the right-click that opens the
/// menu from the client. Without the split, one Java-driven show paints two overlapping popups.
class HostedContextMenu extends InheritedWidget {
  const HostedContextMenu({super.key, required super.child});

  static bool wraps(BuildContext context) =>
      context.dependOnInheritedWidgetOfExactType<HostedContextMenu>() != null;

  @override
  bool updateShouldNotify(HostedContextMenu oldWidget) => false;
}
