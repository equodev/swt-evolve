import 'package:flutter/widgets.dart';

/// Lets a host keep what it built around a widget across its own rebuilds: the widget at the bottom
/// of the kept part is rebuilt through this when the host's state changes, and nothing above it is.
class KeptRebuild {
  Element? _element;
  bool _marked = false;

  /// Whether the kept widget is built, and so can be rebuilt through this.
  bool get built => _element != null;

  /// Whether the kept widget has been told to build again from the host's latest state.
  bool get marked => _marked;

  /// Has the kept widget build again, once until it does.
  void mark() {
    final element = _element;
    if (element == null || _marked) return;
    element.markNeedsBuild();
    _marked = true;
  }

  /// Called by the kept widget whenever it builds.
  void builds(BuildContext context) {
    _element = context as Element;
    _marked = false;
  }

  /// Called by the kept widget when it goes.
  void disposed(BuildContext context) {
    if (!identical(_element, context)) return;
    _element = null;
    _marked = false;
  }
}

/// The bottom of a kept part: shows what [content] gives at the time, rebuilt through [rebuild].
class KeptSlot extends StatefulWidget {
  // Not const: a slot built anew rebuilds with its parent.
  // ignore: prefer_const_constructors_in_immutables
  KeptSlot(this.rebuild, this.content, {super.key});

  final KeptRebuild rebuild;
  final Widget? Function() content;

  @override
  State<KeptSlot> createState() => _KeptSlotState();
}

class _KeptSlotState extends State<KeptSlot> {
  @override
  Widget build(BuildContext context) {
    widget.rebuild.builds(context);
    return widget.content() ?? const SizedBox.shrink();
  }

  @override
  void dispose() {
    widget.rebuild.disposed(context);
    super.dispose();
  }
}
