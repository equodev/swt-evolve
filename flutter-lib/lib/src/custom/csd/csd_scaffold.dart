import 'package:flutter/foundation.dart' show kIsWeb;
import 'package:flutter/gestures.dart';
import 'package:flutter/material.dart';

import '../../impl/widget_config.dart';
import '../../theme/named_themes.dart';
import '../../theme/theme.dart' show parseThemeColorFromHex;
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

    // The desktop-native macOS window keeps its real frame — we only hide the title bar chrome
    // and draw our own controls — so its resizable edges are still the OS's. Mounting the
    // Flutter resize handles there would sit them over those edges and swallow the resize.
    // Scoped to non-web on purpose: the Chromium standalone window IS frameless on macOS too,
    // and does need the handles.
    final nativeFrameOwnsResize = !kIsWeb && os == 'mac';

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
        if (csdEnabled && !nativeFrameOwnsResize) const CsdResizeEdges(),
        if (csdEnabled && !nativeFrameOwnsResize) ..._csdWindowBorderEdges(),
      ],
    );
  }
}

/// A thin 1px window border so the frameless window's edges (and the resize zone) are
/// visible. Hidden while maximized. Isolated behind an [IgnorePointer] + its own
/// [ValueListenableBuilder] so toggling it never disturbs the resize-edge MouseRegions.
/// The four 1px edges, each its own [Positioned] in the CSD [Stack] rather than one
/// window-filling box. A filling overlay sits above the whole scene, and the Browser is a
/// [Texture] on Windows: with anything layered over it the compositor has to blend on every
/// frame, which shows as the texture blanking on each repaint (a click inside the page was
/// enough). Edge strips leave the content area clear.
///
/// The left/right strips are inset by 1px vertically so they do not overlap the top/bottom
/// ones -- the colour is translucent, and doubling it would darken the corners.
List<Widget> _csdWindowBorderEdges() {
  Widget edge({double? top, double? bottom, double? left, double? right,
      double? width, double? height}) {
    return Positioned(
      top: top,
      bottom: bottom,
      left: left,
      right: right,
      width: width,
      height: height,
      child: IgnorePointer(
        child: ValueListenableBuilder<bool>(
          valueListenable: csdMaximized,
          builder: (context, maximized, _) {
            if (maximized) return const SizedBox.shrink();
            final dark = Theme.of(context).brightness == Brightness.dark;
            final color =
                dark ? const Color(0x40FFFFFF) : const Color(0x26000000);
            return ColoredBox(color: color);
          },
        ),
      ),
    );
  }

  return [
    edge(top: 0, left: 0, right: 0, height: 1),
    edge(bottom: 0, left: 0, right: 0, height: 1),
    edge(top: 1, bottom: 1, left: 0, width: 1),
    edge(top: 1, bottom: 1, right: 0, width: 1),
  ];
}

/// Slim draggable title strip used by the `overlay` placement when the app has no
/// MainToolbar. Window controls (leading on macOS, trailing elsewhere), the window title,
/// and a [CsdDragView] filling the rest so the strip drags the window.
/// Height of the [CsdOverlayStrip]. The strip takes this off the top of the content, so the
/// viewport reported to the SWT side has to exclude it (see csdContentInsetTop) -- otherwise
/// SWT lays the shell out for the whole window and its bottom trim falls off the screen.
const double kCsdOverlayStripHeight = 32;

/// Vertical space the CSD chrome takes away from the app content. Only the `overlay` placement
/// puts the strip in the layout; `toolbar` hosts the controls inside the MainToolbar and
/// `floating` draws over the content, so neither costs any.
double csdContentInsetTop() =>
    (getConfigFlags().csd_placement ?? 'toolbar') == 'overlay'
        ? kCsdOverlayStripHeight
        : 0;

class CsdOverlayStrip extends StatelessWidget {
  final bool controlsLeading;
  final double height;
  const CsdOverlayStrip({
    super.key,
    required this.controlsLeading,
    this.height = kCsdOverlayStripHeight,
  });

  /// [preferred] when it is legible against [background], else plain white/black. The 4.5:1
  /// threshold is WCAG AA for text this size (12px).
  static Color _readableOn(Color background, Color? preferred) {
    if (preferred != null && _contrastRatio(preferred, background) >= 4.5) {
      return preferred;
    }
    return ThemeData.estimateBrightnessForColor(background) == Brightness.dark
        ? Colors.white
        : Colors.black87;
  }

  static double _contrastRatio(Color a, Color b) {
    final la = a.computeLuminance();
    final lb = b.computeLuminance();
    final lighter = la > lb ? la : lb;
    final darker = la > lb ? lb : la;
    return (lighter + 0.05) / (darker + 0.05);
  }

  /// The title-bar colour set explicitly — by the `csd_titlebar_color` flag, else by the active
  /// named theme. Null means nothing was specified and the colour scheme should decide.
  Color? _explicitBarColor(Brightness brightness) {
    final flags = getConfigFlags();
    final fromFlag = parseThemeColorFromHex(flags.csd_titlebar_color);
    if (fromFlag != null) return fromFlag;
    final name = flags.theme_name?.trim();
    if (name == null || name.isEmpty) return null;
    return kNamedThemes[name]?.titleBarColor(brightness == Brightness.dark);
  }

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    // Title-bar colour, most specific first: the explicit flag, then a colour the active named
    // theme declares for its title bar, then the scheme's `primaryContainer`. That last one is
    // the tinted-surface role — clearly the theme's hue, yet dark in a dark theme and light in
    // a light one, unlike `primary`, which would paint a bright pastel bar over a dark app.
    final explicit = _explicitBarColor(theme.brightness);
    // Neutral surface roles, not an accent pair: a title bar is a surface, and the named themes
    // customise their accents without touching primaryContainer -- so reading that role painted
    // the *base* scheme's colour rather than the active theme's (a red theme got a blue title).
    // A theme that wants a branded bar declares one, or the flag sets it.
    final bg = explicit ?? theme.colorScheme.surfaceContainerHigh;
    // Prefer the scheme's matching "on" role, but only when it actually reads against the bar:
    // these schemes are hand-written, so the pairing is not guaranteed to contrast the way a
    // generated scheme's would, and a title that fails is unreadable rather than off-palette.
    final titleColor =
        _readableOn(bg, explicit == null ? theme.colorScheme.onSurface : null);
    // The Windows/GNOME controls pick their glyph colour from the ambient brightness, which is
    // the *app's*, not the bar's. Hand them the bar's own brightness so the glyphs keep
    // contrast whatever hue the theme paints here.
    final barBrightness = ThemeData.estimateBrightnessForColor(bg);
    final barTheme = theme.copyWith(
      brightness: barBrightness,
      colorScheme: theme.colorScheme.copyWith(brightness: barBrightness),
    );

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
        child: Theme(data: barTheme, child: Row(children: children)),
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
