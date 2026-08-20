import 'package:flutter/material.dart';
import '../main.dart';
import 'gen/composite.dart';
import 'gen/control.dart';
import 'gen/rectangle.dart';
import 'live_bounds.dart';
import 'gen/widget.dart';
import 'gen/widgets.dart';
import 'gen/widgets.dart' as gen;
import 'theme/theme_extensions/composite_theme_extension.dart';


class NoLayout extends StatelessWidget {
  final VComposite? composite;
  final List<VControl> children;
  final Widget Function(VControl child)? childBuilder;

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
    return CustomMultiChildLayout(
        delegate: _AbsoluteLayoutDelegate(children, composite,
            relayout: LiveBounds.relayoutFor(children.map((child) => child.id))),
        children: [
          for (var child in children.reversed)
            LayoutId(
              // Stable widget key so Flutter preserves State across parent rebuilds.
              // Without this, a parent ONCHANGE causes child widgets to be disposed
              // and remounted, briefly producing an empty subtree and a visible flash.
              key: ValueKey(child.id),
              id: child.id,
              child: isPanelLayout
                  ? _wrapAsPanel(_buildChild(child), theme!)
                  : ClipRect(child: _buildChild(child)),
            )
        ]);
  }

  Widget _buildChild(VControl child) {
    if (childBuilder != null) {
      return childBuilder!(child);
    }
    return customWidgetFromValue(child) ?? gen.mapWidgetFromValue(child);
  }

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

class _AbsoluteLayoutDelegate extends MultiChildLayoutDelegate {
  List<VControl> children;
  VComposite? composite;

  _AbsoluteLayoutDelegate(this.children, this.composite, {super.relayout});

  /// The bounds to lay [child] out at: the parent's own copy, except when that copy is 0x0 and the
  /// child itself reports otherwise ([LiveBounds]) — a stale copy must not pin a live child at zero
  /// size. A child that is genuinely 0x0 publishes 0x0 too, so it stays 0x0.
  VRectangle? _boundsOf(VControl child) {
    final fromParent = child.bounds;
    if (fromParent != null && (fromParent.width != 0 || fromParent.height != 0)) {
      return fromParent;
    }
    final ownBounds = LiveBounds.of(child.id);
    if (ownBounds != null && (ownBounds.width != 0 || ownBounds.height != 0)) {
      return ownBounds;
    }
    return fromParent;
  }

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
