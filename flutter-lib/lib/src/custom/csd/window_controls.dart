import 'package:flutter/material.dart';

import '../../impl/widget_config.dart';
import 'csd_state.dart';

/// The window operations the controls invoke. Defaults to the host [EquoWindow]; tests
/// inject a fake to assert taps without a browser. [onMaxRestore] is given the desired
/// next state (true => maximize, false => restore) so the parent owns the toggle.
class WindowControlActions {
  final VoidCallback onMinimize;
  final void Function(bool maximize) onMaxRestore;
  final VoidCallback onClose;

  const WindowControlActions({
    required this.onMinimize,
    required this.onMaxRestore,
    required this.onClose,
  });

  /// Drives the real host window, keeping [csdMaximized] in sync. Maximize/close route
  /// through the SWT bridge (see csd_state.dart) to avoid the mac window-pump deadlock.
  factory WindowControlActions.equo() => WindowControlActions(
    onMinimize: csdMinimize,
    onMaxRestore: csdSetMaximized,
    onClose: csdClose,
  );
}

/// Which glyph a control button paints.
enum WindowGlyph { minimize, maximize, restore, close }

/// Host-OS-native window controls (close / minimize / maximize). Picks the variant from
/// [ConfigFlags.csd_os] and owns the maximize↔restore toggle state.
class WindowControls extends StatefulWidget {
  /// Overrides the host OS detection (mainly for tests/goldens): "mac" | "windows" | "linux".
  final String? osOverride;
  final WindowControlActions? actions;

  const WindowControls({super.key, this.osOverride, this.actions});

  @override
  State<WindowControls> createState() => _WindowControlsState();
}

class _WindowControlsState extends State<WindowControls> {
  WindowControlActions get _actions =>
      widget.actions ?? WindowControlActions.equo();

  void _onMaxRestore() => _actions.onMaxRestore(!csdMaximized.value);

  @override
  Widget build(BuildContext context) {
    final os = widget.osOverride ?? getConfigFlags().csd_os ?? 'linux';
    return ValueListenableBuilder<bool>(
      valueListenable: csdMaximized,
      builder: (context, maximized, _) {
        switch (os) {
          case 'mac':
            return MacTrafficLights(
              isMaximized: maximized,
              onMinimize: _actions.onMinimize,
              onMaxRestore: _onMaxRestore,
              onClose: _actions.onClose,
            );
          case 'windows':
            return WindowsControls(
              isMaximized: maximized,
              onMinimize: _actions.onMinimize,
              onMaxRestore: _onMaxRestore,
              onClose: _actions.onClose,
            );
          default:
            return GnomeControls(
              isMaximized: maximized,
              onMinimize: _actions.onMinimize,
              onMaxRestore: _onMaxRestore,
              onClose: _actions.onClose,
            );
        }
      },
    );
  }
}

// ─────────────────────────────────────────────────────────────── macOS traffic lights

/// Three 12px circles (close / minimize / maximize) at the leading edge. Glyphs appear on
/// all three together while the pointer is anywhere over the cluster, like macOS.
class MacTrafficLights extends StatefulWidget {
  final bool isMaximized;
  final VoidCallback onMinimize, onMaxRestore, onClose;

  const MacTrafficLights({
    super.key,
    required this.isMaximized,
    required this.onMinimize,
    required this.onMaxRestore,
    required this.onClose,
  });

  @override
  State<MacTrafficLights> createState() => _MacTrafficLightsState();
}

class _MacTrafficLightsState extends State<MacTrafficLights> {
  bool _hovering = false;

  static const _close = Color(0xFFFF5F57);
  static const _min = Color(0xFFFEBC2E);
  static const _max = Color(0xFF28C840);
  static const _glyph = Color(0xCC000000); // dark, shown only on hover
  static const _diameter = 13.0;
  static const _gap = 8.0;

  Widget _light(
    Color color,
    WindowGlyph glyph,
    VoidCallback onTap,
    String label, {
    required bool showGlyph,
  }) {
    // No visual tooltip (macOS doesn't show one), but keep a Semantics label so the controls
    // stay accessible to screen readers.
    return Semantics(
      label: label,
      button: true,
      child: _HoverScale(
        onTap: onTap,
        child: Container(
          width: _diameter,
          height: _diameter,
          decoration: BoxDecoration(
            color: color,
            shape: BoxShape.circle,
            border: Border.all(color: const Color(0x1A000000)),
          ),
          child: showGlyph
              ? CustomPaint(
                  painter: _GlyphPainter(
                    glyph,
                    _glyph,
                    strokeWidth: 1.0,
                    inset: 3.5,
                  ),
                )
              : null,
        ),
      ),
    );
  }

  @override
  Widget build(BuildContext context) {
    // Grey out when the window is inactive, like macOS — but reveal the colors + glyphs on
    // hover (matching macOS's behavior over a background window's traffic lights).
    return ValueListenableBuilder<bool>(
      valueListenable: csdWindowActive,
      builder: (context, active, _) {
        final dark = Theme.of(context).brightness == Brightness.dark;
        final inactiveColor = dark
            ? const Color(0xFF565656)
            : const Color(0xFFCDCDCD);
        final showColor = active || _hovering;
        Color c(Color on) => showColor ? on : inactiveColor;

        // Column(min) centers the fixed-size circles vertically when the host Row stretches
        // (bounds toolbar) without expanding horizontally.
        return Column(
          mainAxisSize: MainAxisSize.min,
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            MouseRegion(
              onEnter: (_) => setState(() => _hovering = true),
              onExit: (_) => setState(() => _hovering = false),
              child: Padding(
                padding: const EdgeInsets.symmetric(horizontal: 8),
                child: Row(
                  mainAxisSize: MainAxisSize.min,
                  children: [
                    _light(
                      c(_close),
                      WindowGlyph.close,
                      widget.onClose,
                      'Close',
                      showGlyph: _hovering,
                    ),
                    const SizedBox(width: _gap),
                    _light(
                      c(_min),
                      WindowGlyph.minimize,
                      widget.onMinimize,
                      'Minimize',
                      showGlyph: _hovering,
                    ),
                    const SizedBox(width: _gap),
                    _light(
                      c(_max),
                      widget.isMaximized
                          ? WindowGlyph.restore
                          : WindowGlyph.maximize,
                      widget.onMaxRestore,
                      widget.isMaximized ? 'Restore' : 'Zoom',
                      showGlyph: _hovering,
                    ),
                  ],
                ),
              ),
            ),
          ],
        );
      },
    );
  }
}

// ───────────────────────────────────────────────────────────────── Windows controls

/// Three 46px-wide full-height buttons (minimize / maximize-restore / close) at the
/// trailing edge. Close hovers red; the others hover a subtle wash.
class WindowsControls extends StatelessWidget {
  final bool isMaximized;
  final VoidCallback onMinimize, onMaxRestore, onClose;

  const WindowsControls({
    super.key,
    required this.isMaximized,
    required this.onMinimize,
    required this.onMaxRestore,
    required this.onClose,
  });

  @override
  Widget build(BuildContext context) {
    final dark = Theme.of(context).brightness == Brightness.dark;
    final hover = dark ? const Color(0x14FFFFFF) : const Color(0x0D000000);
    // Like the Windows title bar, the glyphs lighten when the window is inactive.
    return ValueListenableBuilder<bool>(
      valueListenable: csdWindowActive,
      builder: (context, active, _) {
        final glyph = active
            ? (dark ? Colors.white : const Color(0xFF1A1A1A))
            : (dark ? const Color(0xFF6E6E6E) : const Color(0xFF9B9B9B));
        return Row(
          mainAxisSize: MainAxisSize.min,
          children: [
            _WinButton(
              glyph: WindowGlyph.minimize,
              glyphColor: glyph,
              hoverColor: hover,
              onTap: onMinimize,
              tip: 'Minimize',
            ),
            _WinButton(
              glyph: isMaximized ? WindowGlyph.restore : WindowGlyph.maximize,
              glyphColor: glyph,
              hoverColor: hover,
              onTap: onMaxRestore,
              tip: isMaximized ? 'Restore' : 'Maximize',
            ),
            _WinButton(
              glyph: WindowGlyph.close,
              glyphColor: glyph,
              hoverColor: const Color(0xFFE81123),
              hoverGlyphColor: Colors.white,
              onTap: onClose,
              tip: 'Close',
            ),
          ],
        );
      },
    );
  }
}

class _WinButton extends StatefulWidget {
  final WindowGlyph glyph;
  final Color glyphColor, hoverColor;
  final Color? hoverGlyphColor;
  final VoidCallback onTap;
  final String tip;

  const _WinButton({
    required this.glyph,
    required this.glyphColor,
    required this.hoverColor,
    this.hoverGlyphColor,
    required this.onTap,
    required this.tip,
  });

  @override
  State<_WinButton> createState() => _WinButtonState();
}

class _WinButtonState extends State<_WinButton> {
  bool _hovering = false;

  @override
  Widget build(BuildContext context) {
    final color = _hovering && widget.hoverGlyphColor != null
        ? widget.hoverGlyphColor!
        : widget.glyphColor;
    return MouseRegion(
      onEnter: (_) => setState(() => _hovering = true),
      onExit: (_) => setState(() => _hovering = false),
      child: GestureDetector(
        onTap: widget.onTap,
        child: Tooltip(
          message: widget.tip,
          waitDuration: const Duration(milliseconds: 600),
          // No fixed height: a stretch Row (bounds toolbar) gives tight height so the
          // button fills the bar; loose contexts (floating/overlay) fall back to minHeight.
          child: ConstrainedBox(
            constraints: const BoxConstraints(minWidth: 46, minHeight: 30),
            child: Container(
              width: 46,
              color: _hovering ? widget.hoverColor : Colors.transparent,
              alignment: Alignment.center,
              child: CustomPaint(
                size: const Size(10, 10),
                painter: _GlyphPainter(
                  widget.glyph,
                  color,
                  strokeWidth: 1.0,
                  inset: 0.5,
                ),
              ),
            ),
          ),
        ),
      ),
    );
  }
}

// ─────────────────────────────────────────────────────────────── GNOME / Ubuntu controls

/// Three 24px round buttons (minimize / maximize-restore / close) at the trailing edge,
/// Adwaita-style grey fills.
class GnomeControls extends StatelessWidget {
  final bool isMaximized;
  final VoidCallback onMinimize, onMaxRestore, onClose;

  const GnomeControls({
    super.key,
    required this.isMaximized,
    required this.onMinimize,
    required this.onMaxRestore,
    required this.onClose,
  });

  @override
  Widget build(BuildContext context) {
    final dark = Theme.of(context).brightness == Brightness.dark;
    final glyph = dark ? const Color(0xFFEEEEEE) : const Color(0xFF2E2E2E);
    final idle = dark ? const Color(0x1AFFFFFF) : const Color(0x14000000);
    final hover = dark ? const Color(0x33FFFFFF) : const Color(0x26000000);
    // Column(min) centers the fixed 24px circles vertically under a stretch Row without
    // expanding horizontally.
    return Column(
      mainAxisSize: MainAxisSize.min,
      mainAxisAlignment: MainAxisAlignment.center,
      children: [
        Padding(
          padding: const EdgeInsets.symmetric(horizontal: 8),
          child: Row(
            mainAxisSize: MainAxisSize.min,
            children: [
              _GnomeButton(
                glyph: WindowGlyph.minimize,
                glyphColor: glyph,
                idleColor: idle,
                hoverColor: hover,
                onTap: onMinimize,
                tip: 'Minimize',
              ),
              const SizedBox(width: 8),
              _GnomeButton(
                glyph: isMaximized ? WindowGlyph.restore : WindowGlyph.maximize,
                glyphColor: glyph,
                idleColor: idle,
                hoverColor: hover,
                onTap: onMaxRestore,
                tip: isMaximized ? 'Restore' : 'Maximize',
              ),
              const SizedBox(width: 8),
              _GnomeButton(
                glyph: WindowGlyph.close,
                glyphColor: glyph,
                idleColor: idle,
                hoverColor: hover,
                onTap: onClose,
                tip: 'Close',
              ),
            ],
          ),
        ),
      ],
    );
  }
}

class _GnomeButton extends StatefulWidget {
  final WindowGlyph glyph;
  final Color glyphColor, idleColor, hoverColor;
  final VoidCallback onTap;
  final String tip;

  const _GnomeButton({
    required this.glyph,
    required this.glyphColor,
    required this.idleColor,
    required this.hoverColor,
    required this.onTap,
    required this.tip,
  });

  @override
  State<_GnomeButton> createState() => _GnomeButtonState();
}

class _GnomeButtonState extends State<_GnomeButton> {
  bool _hovering = false;

  @override
  Widget build(BuildContext context) {
    return MouseRegion(
      onEnter: (_) => setState(() => _hovering = true),
      onExit: (_) => setState(() => _hovering = false),
      child: GestureDetector(
        onTap: widget.onTap,
        child: Tooltip(
          message: widget.tip,
          waitDuration: const Duration(milliseconds: 600),
          child: Container(
            width: 24,
            height: 24,
            decoration: BoxDecoration(
              color: _hovering ? widget.hoverColor : widget.idleColor,
              shape: BoxShape.circle,
            ),
            child: CustomPaint(
              painter: _GlyphPainter(
                widget.glyph,
                widget.glyphColor,
                strokeWidth: 1.2,
                inset: 7.0,
              ),
            ),
          ),
        ),
      ),
    );
  }
}

// ───────────────────────────────────────────────────────────────────── shared bits

/// Tiny tap target that scales slightly on press — used for the macOS lights.
class _HoverScale extends StatelessWidget {
  final Widget child;
  final VoidCallback onTap;
  const _HoverScale({required this.child, required this.onTap});

  @override
  Widget build(BuildContext context) {
    return GestureDetector(onTap: onTap, child: child);
  }
}

/// Paints the four window-control glyphs with thin strokes, centered with [inset]
/// padding on each side. Resolution-independent (no icon font needed).
class _GlyphPainter extends CustomPainter {
  final WindowGlyph glyph;
  final Color color;
  final double strokeWidth;

  /// Padding from each edge to the glyph box. When null, scales with the canvas.
  final double? inset;

  _GlyphPainter(this.glyph, this.color, {this.strokeWidth = 1.0, this.inset});

  @override
  void paint(Canvas canvas, Size size) {
    final pad = inset ?? size.width * 0.25;
    final rect = Rect.fromLTRB(pad, pad, size.width - pad, size.height - pad);
    final paint = Paint()
      ..color = color
      ..strokeWidth = strokeWidth
      ..style = PaintingStyle.stroke
      ..strokeCap = StrokeCap.square
      ..isAntiAlias = true;

    switch (glyph) {
      case WindowGlyph.minimize:
        final y = rect.center.dy;
        canvas.drawLine(Offset(rect.left, y), Offset(rect.right, y), paint);
      case WindowGlyph.maximize:
        canvas.drawRect(rect, paint);
      case WindowGlyph.restore:
        // Two overlapping squares (the back one peeking top-right).
        final off = rect.width * 0.22;
        final front = Rect.fromLTRB(
          rect.left,
          rect.top + off,
          rect.right - off,
          rect.bottom,
        );
        canvas.drawRect(front, paint);
        final backTop = rect.top;
        canvas.drawLine(
          Offset(rect.left + off, backTop),
          Offset(rect.right, backTop),
          paint,
        );
        canvas.drawLine(
          Offset(rect.right, backTop),
          Offset(rect.right, rect.bottom - off),
          paint,
        );
        canvas.drawLine(
          Offset(rect.left + off, backTop),
          Offset(rect.left + off, rect.top + off),
          paint,
        );
      case WindowGlyph.close:
        canvas.drawLine(rect.topLeft, rect.bottomRight, paint);
        canvas.drawLine(rect.topRight, rect.bottomLeft, paint);
    }
  }

  @override
  bool shouldRepaint(_GlyphPainter old) =>
      old.glyph != glyph ||
      old.color != color ||
      old.strokeWidth != strokeWidth ||
      old.inset != inset;
}
