import 'package:flutter/material.dart';
import '../main.dart';
import 'gen/composite.dart';
import 'gen/control.dart';
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
        delegate: _AbsoluteLayoutDelegate(children, composite),
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

  _AbsoluteLayoutDelegate(this.children, this.composite);

  @override
  Size getSize(BoxConstraints constraints) {
    final bounds = composite?.bounds;
    if (bounds != null && bounds.width >= 0 && bounds.height >= 0) {
      return Size(bounds.width.toDouble(), bounds.height.toDouble());
    }
    return super.getSize(constraints);
  }

  @override
  void performLayout(Size size) {
    for (var child in children.whereType<VControl>()) {
      var w = child.bounds?.width.toDouble();
      var h = child.bounds?.height.toDouble();
      var x = child.bounds?.x ?? 0;
      var y = child.bounds?.y ?? 0;
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

      final currentBounds = currentChild.bounds;
      final oldBounds = oldChild.bounds;

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
