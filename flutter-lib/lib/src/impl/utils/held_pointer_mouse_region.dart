import 'package:flutter/rendering.dart';
import 'package:flutter/widgets.dart';

/// A [MouseRegion] whose [cursor] shows over the whole of its child while [held] answers true,
/// whatever the controls inside it show: the control holding a press keeps its cursor until the
/// release, wherever the pointer goes, as a captured pointer does natively.
///
/// [held] is read on every hit test, so the press needs no rebuild to take effect. The region is
/// the one the control already has, so holding adds no render object to paint.
class HeldPointerMouseRegion extends MouseRegion {
  const HeldPointerMouseRegion({
    super.key,
    super.onEnter,
    super.onExit,
    super.onHover,
    super.cursor,
    required this.held,
    super.child,
  });

  final bool Function() held;

  @override
  RenderMouseRegion createRenderObject(BuildContext context) => _RenderHeldPointerMouseRegion(
        onEnter: onEnter,
        onHover: onHover,
        onExit: onExit,
        cursor: cursor,
        opaque: opaque,
        hitTestBehavior: hitTestBehavior,
        held: held,
      );

  @override
  void updateRenderObject(BuildContext context, RenderMouseRegion renderObject) {
    super.updateRenderObject(context, renderObject);
    (renderObject as _RenderHeldPointerMouseRegion).held = held;
  }
}

class _RenderHeldPointerMouseRegion extends RenderMouseRegion {
  _RenderHeldPointerMouseRegion({
    super.onEnter,
    super.onHover,
    super.onExit,
    super.cursor,
    super.opaque,
    super.hitTestBehavior,
    required this.held,
  });

  bool Function() held;

  /// While held, entered ahead of the children, so the mouse tracker takes this cursor before any
  /// of theirs. Its enter and exit then come in another order against the children's, which
  /// [HoverExclusivityArbiter] ignores: it settles the hover by depth once all have come.
  @override
  bool hitTest(BoxHitTestResult result, {required Offset position}) {
    if (cursor == MouseCursor.defer || !held()) return super.hitTest(result, position: position);
    if (!size.contains(position)) return false;
    result.add(BoxHitTestEntry(this, position));
    return (hitTestChildren(result, position: position) || hitTestSelf(position)) && opaque;
  }
}
