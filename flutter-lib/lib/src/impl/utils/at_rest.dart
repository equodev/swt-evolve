import 'package:flutter/widgets.dart';

import 'force_live.dart';

/// A control drawn at rest, without the input widget it mounts once anything could interact with it:
/// the pointer over it, or a press or focus arriving on the drawing, which hands over to the input
/// widget once it is built.
mixin AtRestHandover<T extends StatefulWidget> on State<T> {
  bool _hovered = false;
  bool _handingOver = false;

  /// Whether the pointer is over the control.
  bool get hovered => _hovered;

  /// Whether the input widget is being mounted to take over from the drawing at rest.
  bool get handingOver => _handingOver;

  /// Whether a [ForceLive] above keeps the input widget mounted.
  bool get forcedLive => mounted && ForceLive.isOn(context);

  void setHovered(bool hovered) {
    if (_hovered != hovered) setState(() => _hovered = hovered);
  }

  /// Mounts the input widget, and runs [then] once it is built.
  void handOver(VoidCallback then) {
    setState(() => _handingOver = true);
    WidgetsBinding.instance.addPostFrameCallback((_) {
      if (mounted) then();
    });
  }

  /// Ends the handover: whatever keeps the input widget mounted from now on is read at the next build.
  void handedOver() => _handingOver = false;
}
