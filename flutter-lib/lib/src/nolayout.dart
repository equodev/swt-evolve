import 'package:flutter/material.dart';
import 'package:flutter/rendering.dart' show PipelineOwner, RenderProxyBox;
import '../main.dart';
import 'gen/composite.dart';
import 'gen/control.dart';
import 'gen/rectangle.dart';
import 'comm/v_registry.dart';
import 'gen/widget.dart';
import 'gen/widgets.dart';
import 'gen/widgets.dart' as gen;
import 'impl/utils/live_layer.dart';
import 'impl/utils/widget_utils.dart';
import 'theme/theme_extensions/composite_theme_extension.dart';


class NoLayout extends StatelessWidget {
  final VComposite? composite;
  final List<VControl> children;
  /// Builds [child]; null leaves it to the widget its value maps to.
  final Widget? Function(VControl child)? childBuilder;

  const NoLayout({
    super.key,
    required this.children,
    required this.composite,
    this.childBuilder,
  });

  @override
  Widget build(BuildContext context) {
    final isPanelLayout = SashPanelMarker.of(context);
    final theme = isPanelLayout
        ? Theme.of(context).extension<CompositeThemeExtension>()!
        : null;
    if (composite != null) PanelEdge.setGap(composite!.id, theme?.panelChildGap, context);
    // Where a composite's children stand. A container whose build() never reaches
    // ControlImpl.blockWhenDisabled (Group lays its children out itself) would otherwise leave
    // them reading an enclosing answer that skips the container's own flag.
    return EnabledScope(
      enabled: (composite?.enabled ?? true) && EnabledScope.of(context),
      child: CustomMultiChildLayout(
        delegate: _AbsoluteLayoutDelegate(children, composite,
            relayout: VRegistry.instance.boundsChangesAt(children.map(VRegistry.keyOf))),
        children: [
          for (var child in children.reversed)
            LayoutId(
              // Stable widget key so Flutter preserves State across parent rebuilds.
              // Without this, a parent ONCHANGE causes child widgets to be disposed
              // and remounted, briefly producing an empty subtree and a visible flash.
              key: ValueKey(child.id),
              id: child.id,
              // A panel's child is sized by its own build and then fitted, so only a plain slot
              // lays its child out by the bounds.
              child: isPanelLayout
                  ? _wrapAsPanel(_AtBounds(control: child, child: BoundsSlot(id: child.id, child: _buildChild(child))),
                      theme!)
                  : BoundsSlot(
                      id: child.id,
                      child: LiveLayer(
                        leavesPointerAt: (position) => PanelEdge.gapAt(child, position) != null,
                        child: ClipRect(child: _buildChild(child)),
                      ),
                    ),
            )
        ],
      ),
    );
  }

  /// Each child under a TickerMode of its own: every animation listens to its nearest one, and
  /// leaving a shared one takes as long as it has listeners, so a thousand siblings would leave it in
  /// quadratic time.
  Widget _buildChild(VControl child) => TickerMode(
        enabled: true,
        child: childBuilder?.call(child) ?? customWidgetFromValue(child) ?? gen.mapWidgetFromValue(child),
      );

  static Widget _wrapAsPanel(Widget child, CompositeThemeExtension theme) {
    final radius = BorderRadius.all(Radius.circular(theme.panelBorderRadius));
    return Container(
      margin: EdgeInsets.all(theme.panelChildGap),
      decoration: BoxDecoration(
        borderRadius: radius,
        boxShadow: [
          BoxShadow(
            color: theme.panelShadowColor,
            blurRadius: theme.panelShadowBlurRadius,
            offset: Offset(theme.panelShadowDx, theme.panelShadowDy),
          ),
        ],
      ),
      foregroundDecoration: BoxDecoration(
        borderRadius: radius,
        border: Border.fromBorderSide(
          BorderSide(color: theme.panelBorderColor, width: theme.panelBorderWidth),
        ),
      ),
      child: ClipRRect(
        borderRadius: radius,
        child: FittedBox(fit: BoxFit.fill, child: child),
      ),
    );
  }
}

/// [child] laid out at [control]'s bounds, read when it is laid out and again whenever they change:
/// a panel fits its child into its slot from the size the child takes, and the child's own build
/// would otherwise have to be the one to size it, building it again on every resize.
class _AtBounds extends SingleChildRenderObjectWidget {
  const _AtBounds({required this.control, super.child});

  final VControl control;

  @override
  RenderObject createRenderObject(BuildContext context) => _RenderAtBounds(control);

  @override
  void updateRenderObject(BuildContext context, _RenderAtBounds renderObject) => renderObject.control = control;
}

class _RenderAtBounds extends RenderProxyBox {
  _RenderAtBounds(this._control);

  VControl _control;
  Listenable? _changes;

  set control(VControl value) {
    if (identical(value, _control)) return;
    _control = value;
    if (attached) _listen();
    markNeedsLayout();
  }

  void _listen() {
    _changes?.removeListener(markNeedsLayout);
    _changes = VRegistry.instance.boundsChangesAt([VRegistry.keyOf(_control)])..addListener(markNeedsLayout);
  }

  @override
  void attach(PipelineOwner owner) {
    super.attach(owner);
    _listen();
  }

  @override
  void detach() {
    _changes?.removeListener(markNeedsLayout);
    _changes = null;
    super.detach();
  }

  BoxConstraints _at(BoxConstraints constraints) {
    final b = _control.bounds;
    if (b == null || b.width <= 0 || b.height <= 0) return constraints;
    return BoxConstraints.tightFor(width: b.width.toDouble(), height: b.height.toDouble()).enforce(constraints);
  }

  @override
  void performLayout() {
    final child = this.child;
    if (child == null) {
      size = _at(constraints).constrain(Size.zero);
      return;
    }
    child.layout(_at(constraints), parentUsesSize: true);
    size = constraints.constrain(child.size);
  }

  @override
  Size computeDryLayout(BoxConstraints constraints) =>
      constraints.constrain(child?.getDryLayout(_at(constraints)) ?? Size.zero);
}

class _AbsoluteLayoutDelegate extends MultiChildLayoutDelegate {
  List<VControl> children;
  VComposite? composite;

  _AbsoluteLayoutDelegate(this.children, this.composite, {super.relayout});

  /// The bounds to lay [child] out at.
  ///
  /// [child] is the widget itself, not a copy of it: the list this delegate was built from holds
  /// the objects the registry holds, so what it reads here is whatever that child was last told —
  /// including by an update the composite never heard about. Keeping up with those is what
  /// [VRegistry.boundsChangesOn] is passed as `relayout` for.
  VRectangle? _boundsOf(VControl child) => child.bounds;

  /// The declared bounds, grown to at least cover every child's own bottom-right
  /// corner. A parent's declared size can understate what it actually needs (e.g.
  /// SWT's `ScrolledComposite` forcing a "fill" size onto content whose real layout
  /// is taller) -- reporting the smaller size here would silently clip children
  /// positioned past it instead of letting an ancestor scroll view reach them.
  @override
  Size getSize(BoxConstraints constraints) {
    final bounds = composite?.bounds;
    if (bounds == null || bounds.width < 0 || bounds.height < 0) {
      return super.getSize(constraints);
    }
    double width = bounds.width.toDouble();
    double height = bounds.height.toDouble();
    for (final child in children) {
      final b = _boundsOf(child);
      if (b == null) continue;
      if (b.width > 0 && b.x + b.width > width) width = (b.x + b.width).toDouble();
      if (b.height > 0 && b.y + b.height > height) height = (b.y + b.height).toDouble();
    }
    return Size(width, height);
  }

  @override
  void performLayout(Size size) {
    for (var child in children.whereType<VControl>()) {
      final bounds = _boundsOf(child);
      // Never-set bounds (SWT.DEFAULT/-1) must render at zero size, not an invalid tight constraint.
      final w = (bounds != null && bounds.width > 0) ? bounds.width.toDouble() : 0.0;
      final h = (bounds != null && bounds.height > 0) ? bounds.height.toDouble() : 0.0;
      final x = bounds?.x ?? 0;
      final y = bounds?.y ?? 0;
      layoutChild(child.id, BoxConstraints.tightFor(width: w, height: h));
      positionChild(child.id, Offset(x.toDouble(), y.toDouble()));
    }
  }

  @override
  bool shouldRelayout(covariant _AbsoluteLayoutDelegate oldDelegate) {
    if (children.length != oldDelegate.children.length) return true;

    for (int i = 0; i < children.length; i++) {
      final currentChild = children[i];
      final oldChild = oldDelegate.children[i];

      if (currentChild.id != oldChild.id) return true;

      // Compare what the layout will actually use, not the raw parent copy — otherwise a rebuild
      // that carries the same stale copy reports "nothing moved" while the resolved bounds did.
      final currentBounds = _boundsOf(currentChild);
      final oldBounds = oldDelegate._boundsOf(oldChild);

      if (currentBounds == null && oldBounds != null) return true;
      if (currentBounds != null && oldBounds == null) return true;

      if (currentBounds != null && oldBounds != null) {
        if (currentBounds.x != oldBounds.x ||
            currentBounds.y != oldBounds.y ||
            currentBounds.width != oldBounds.width ||
            currentBounds.height != oldBounds.height) {
          return true;
        }
      }
    }

    return false;
  }
}

/// The gap a composite that lays its children out as panels paints around each of them, which runs
/// along its own edge too. Where that edge meets a divider of an enclosing container, the strip
/// reads as part of the divider: the pointer there belongs to the nearest enclosing control whose
/// own edge the strip does not run along, as the gap between two panels belongs to their parent.
abstract final class PanelEdge {
  /// Per composite id, its gap and the [NoLayout] that laid it out: the entry lasts while that
  /// layout is mounted, so a disposed composite's gap claims no pointer.
  static final Map<int, (double, BuildContext)> _gaps = {};
  static double _widest = 0;

  /// Records that the composite numbered [id] panels its children with [gap], or does not (null),
  /// as the [NoLayout] built in [layout] lays it out.
  static void setGap(int id, double? gap, BuildContext layout) {
    if (gap == null || gap <= 0) {
      _gaps.remove(id);
      return;
    }
    if (!_gaps.containsKey(id)) _gaps.removeWhere((_, entry) => !entry.$2.mounted);
    _gaps[id] = (gap, layout);
    if (gap > _widest) _widest = gap;
  }

  static double? _gapOf(int id) {
    final entry = _gaps[id];
    return entry != null && entry.$2.mounted ? entry.$1 : null;
  }

  /// The width of the panel gap [pos] lies in, in [control]'s coordinates, when that gap runs along
  /// [control]'s own edge; null when [pos] is on the surface of [control] or of a control in it.
  static double? gapAt(VControl control, Offset pos) {
    if (_gaps.isEmpty || control is! VComposite) return null;
    final bounds = control.bounds;
    final children = control.children;
    if (bounds == null || children == null || children.isEmpty) return null;
    bool alongEdge(double gap) =>
        pos.dx < gap || pos.dy < gap || pos.dx >= bounds.width - gap || pos.dy >= bounds.height - gap;
    if (!alongEdge(_widest)) return null;
    final gap = _gapOf(control.id);
    // The first child is the one painted on top.
    for (final child in children) {
      final b = child.bounds;
      if (child.visible == false || b == null) continue;
      final rect = Rect.fromLTWH(b.x.toDouble(), b.y.toDouble(), b.width.toDouble(), b.height.toDouble())
          .deflate(gap ?? 0);
      if (!rect.contains(pos)) continue;
      // A panel is fitted into its slot, which takes the pointer anywhere inside it.
      if (gap != null) return null;
      final inner = gapAt(child, pos - rect.topLeft);
      return inner != null && alongEdge(inner) ? inner : null;
    }
    return gap != null && alongEdge(gap) ? gap : null;
  }
}

/// Marks where [NoLayout] lays out the control numbered [id]: at exactly its bounds, read at layout
/// time, so a change of its size alone needs a new layout of the control and not a new build.
class BoundsSlot extends InheritedWidget {
  const BoundsSlot({super.key, required this.id, required super.child});

  final int id;

  /// Whether the control numbered [id], built in [context], is laid out by its bounds.
  static bool places(BuildContext context, int id) =>
      context.getInheritedWidgetOfExactType<BoundsSlot>()?.id == id;

  @override
  bool updateShouldNotify(BoundsSlot oldWidget) => false;
}

class SashPanelMarker extends InheritedWidget {
  final bool active;

  const SashPanelMarker({
    super.key,
    required this.active,
    required super.child,
  });

  static bool of(BuildContext context) {
    final marker =
    context.dependOnInheritedWidgetOfExactType<SashPanelMarker>();
    return marker?.active ?? false;
  }

  @override
  bool updateShouldNotify(SashPanelMarker oldWidget) =>
      active != oldWidget.active;
}
