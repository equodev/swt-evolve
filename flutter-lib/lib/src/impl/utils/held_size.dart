import 'dart:async';

import 'package:flutter/widgets.dart';

/// Lays [child] out at the size it was last given while that size keeps changing, and at the new one
/// once it has stayed put for [settle]; meanwhile the child is placed where its parent puts it and
/// clipped to the space it is given.
///
/// For a child whose own resize costs a frame, such as a page embedded from another process: the
/// browser waits for that page to draw itself at a new size before it shows a frame of the window,
/// so resizing it on every frame of a drag halves the frame rate of everything around it.
class HeldSize extends StatefulWidget {
  const HeldSize({super.key, required this.child, this.settle = const Duration(milliseconds: 100)});

  final Widget child;
  final Duration settle;

  @override
  State<HeldSize> createState() => _HeldSizeState();
}

class _HeldSizeState extends State<HeldSize> {
  Size? _held;
  Size? _latest;
  Timer? _settling;

  @override
  void dispose() {
    _settling?.cancel();
    super.dispose();
  }

  void _settled() {
    if (!mounted) return;
    setState(() => _held = _latest);
  }

  @override
  Widget build(BuildContext context) {
    return LayoutBuilder(builder: (context, constraints) {
      if (!constraints.hasBoundedWidth || !constraints.hasBoundedHeight) return widget.child;
      final size = constraints.biggest;
      final held = _held;
      if (held == null) {
        _held = size;
      } else if (size != held) {
        _latest = size;
        _settling?.cancel();
        _settling = Timer(widget.settle, _settled);
      } else {
        _settling?.cancel();
        _settling = null;
      }
      final shown = _held!;
      return ClipRect(
        child: OverflowBox(
          alignment: Alignment.topLeft,
          minWidth: shown.width,
          maxWidth: shown.width,
          minHeight: shown.height,
          maxHeight: shown.height,
          child: widget.child,
        ),
      );
    });
  }
}
