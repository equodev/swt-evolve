import 'package:flutter/gestures.dart';
import 'package:flutter/material.dart';

import '../../impl/widget_config.dart';
import 'csd_drag_view.dart';
import 'equo_window.dart';
import 'csd_state.dart';
import 'window_controls.dart';

/// Root wrapper that adds Client-Side-Decoration affordances around the whole app when
/// CSD is enabled: an optional title strip / floating controls and a window-edge resize
/// layer. A no-op passthrough when off.
///
/// Placement (`csd_placement`):
/// - `toolbar`  — controls live in the MainToolbar (see toolbar_composite.dart); here we
///                only add the global resize layer.
/// - `overlay`  — a slim draggable title strip with the controls above the content.
/// - `floating` — just the controls floating in the top corner over the content.
class CsdShell extends StatelessWidget {
  final Widget child;
  const CsdShell({super.key, required this.child});

  @override
  Widget build(BuildContext context) {
    final flags = getConfigFlags();
    final placement = flags.csd_placement ?? 'toolbar';
    // Do NOT early-return a bare `child` when CSD is off. Config flags arrive asynchronously
    // *after* the first frame (csd_placement is null -> 'toolbar' initially, then e.g. 'false'
    // in desk mode). Switching between a bare `child` and the Stack-wrapped form changes the
    // child's depth in the tree, so Flutter discards and recreates every descendant State on
    // that rebuild — which silently drops each widget's update subscription and leaves the whole
    // tree deaf. Keep `child` at a stable tree position and only gate the CSD affordances.
    final csdEnabled = placement != 'false';

    final os = flags.csd_os ?? 'linux';
    final controlsLeading = os == 'mac';

    Widget content = child;

    if (placement == 'overlay') {
      content = Column(
        children: [
          CsdOverlayStrip(controlsLeading: controlsLeading),
          Expanded(child: child),
        ],
      );
    }

    return Stack(
      children: [
        Positioned.fill(child: content),
        if (csdEnabled && placement == 'floating')
          Positioned(
            top: 4,
            left: controlsLeading ? 4 : null,
            right: controlsLeading ? null : 4,
            child: const WindowControls(),
          ),
        if (csdEnabled) const CsdResizeEdges(),
        if (csdEnabled)
          const Positioned.fill(child: IgnorePointer(child: _CsdWindowBorder())),
      ],
    );
  }
}

/// A thin 1px window border so the frameless window's edges (and the resize zone) are
/// visible. Hidden while maximized. Isolated behind an [IgnorePointer] + its own
/// [ValueListenableBuilder] so toggling it never disturbs the resize-edge MouseRegions.
class _CsdWindowBorder extends StatelessWidget {
  const _CsdWindowBorder();

  @override
  Widget build(BuildContext context) {
    return ValueListenableBuilder<bool>(
      valueListenable: csdMaximized,
      builder: (context, maximized, _) {
        if (maximized) return const SizedBox.shrink();
        final dark = Theme.of(context).brightness == Brightness.dark;
        final color = dark ? const Color(0x40FFFFFF) : const Color(0x26000000);
        return DecoratedBox(
          decoration: BoxDecoration(border: Border.all(color: color, width: 1)),
        );
      },
    );
  }
}

/// Slim draggable title strip used by the `overlay` placement when the app has no
/// MainToolbar. Window controls (leading on macOS, trailing elsewhere), the window title,
/// and a [CsdDragView] filling the rest so the strip drags the window.
class CsdOverlayStrip extends StatelessWidget {
  final bool controlsLeading;
  final double height;
  const CsdOverlayStrip({
    super.key,
    required this.controlsLeading,
    this.height = 32,
  });

  @override
  Widget build(BuildContext context) {
    final dark = Theme.of(context).brightness == Brightness.dark;
    final bg = dark ? const Color(0xFF2B2B2B) : const Color(0xFFF2F2F2);
    final titleColor = dark ? const Color(0xFFE0E0E0) : const Color(0xFF333333);

    final title = ValueListenableBuilder<String>(
      valueListenable: csdWindowTitle,
      builder: (context, text, _) => Padding(
        padding: const EdgeInsets.symmetric(horizontal: 8),
        child: Text(
          text,
          maxLines: 1,
          overflow: TextOverflow.ellipsis,
          style: TextStyle(fontSize: 12, color: titleColor),
        ),
      ),
    );

    // macOS: traffic lights then title at the left, drag fills the rest.
    // Windows/Linux: title at the left, drag fills the middle, controls at the right.
    final children = controlsLeading
        ? [const WindowControls(), title, const Expanded(child: CsdDragView())]
        : [title, const Expanded(child: CsdDragView()), const WindowControls()];

    return SizedBox(
      height: height,
      child: ColoredBox(
        color: bg,
        child: Row(children: children),
      ),
    );
  }
}

/// Eight transparent edge/corner handles around the whole window that initiate a native
/// resize via `window.equo.beginResize`. Hidden while maximized/fullscreen. Geometry and
/// cursors mirror the dialog resize wrapper in shell_evolve.dart.
class CsdResizeEdges extends StatelessWidget {
  const CsdResizeEdges({super.key});

  static const double _h = 8.0;

  void _begin(PointerDownEvent e, String edge) {
    if (e.buttons != kPrimaryButton) return;
    // No edge-resize while maximized — but the handles stay mounted (see build()).
    if (csdMaximized.value) return;
    EquoWindow.beginResize(
      EquoWindow.screenOriginX + e.position.dx,
      EquoWindow.screenOriginY + e.position.dy,
      edge,
    );
  }

  Widget _handle(
    MouseCursor cursor,
    String edge, {
    double? top,
    double? bottom,
    double? left,
    double? right,
    double? w,
    double? h,
  }) {
    return Positioned(
      top: top,
      bottom: bottom,
      left: left,
      right: right,
      width: w,
      height: h,
      child: MouseRegion(
        cursor: cursor,
        child: Listener(
          behavior: HitTestBehavior.translucent,
          onPointerDown: (e) => _begin(e, edge),
        ),
      ),
    );
  }

  @override
  Widget build(BuildContext context) {
    // Handles are ALWAYS mounted — never swapped to SizedBox.shrink on maximize. Tearing
    // these MouseRegions down while the window resizes corrupts Flutter-web's global mouse
    // tracker and kills input/hover. When maximized, _begin() no-ops instead.
    return Positioned.fill(
      child: Stack(
        children: [
          _handle(
            SystemMouseCursors.resizeUpDown,
            'TOP',
            top: 0,
            left: _h,
            right: _h,
            h: _h,
          ),
          _handle(
            SystemMouseCursors.resizeUpDown,
            'BOTTOM',
            bottom: 0,
            left: _h,
            right: _h,
            h: _h,
          ),
          _handle(
            SystemMouseCursors.resizeLeftRight,
            'LEFT',
            left: 0,
            top: _h,
            bottom: _h,
            w: _h,
          ),
          _handle(
            SystemMouseCursors.resizeLeftRight,
            'RIGHT',
            right: 0,
            top: _h,
            bottom: _h,
            w: _h,
          ),
          _handle(
            SystemMouseCursors.resizeUpLeftDownRight,
            'TOP_LEFT',
            left: 0,
            top: 0,
            w: _h,
            h: _h,
          ),
          _handle(
            SystemMouseCursors.resizeUpRightDownLeft,
            'TOP_RIGHT',
            right: 0,
            top: 0,
            w: _h,
            h: _h,
          ),
          _handle(
            SystemMouseCursors.resizeUpRightDownLeft,
            'BOTTOM_LEFT',
            left: 0,
            bottom: 0,
            w: _h,
            h: _h,
          ),
          _handle(
            SystemMouseCursors.resizeUpLeftDownRight,
            'BOTTOM_RIGHT',
            right: 0,
            bottom: 0,
            w: _h,
            h: _h,
          ),
        ],
      ),
    );
  }
}
