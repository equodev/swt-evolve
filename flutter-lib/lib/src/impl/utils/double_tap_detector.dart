import 'package:flutter/gestures.dart' show kDoubleTapTimeout;
import 'package:flutter/widgets.dart';

/// Arena-free double-click/tap detection.
///
/// Flutter's [DoubleTapGestureRecognizer] -- created by any `GestureDetector`
/// or `InkWell` that sets `onDoubleTap` -- holds the gesture arena for
/// [kDoubleTapTimeout] (300ms) on every tap, delaying every single tap by that
/// much. Instead of paying that cost, hold a [DoubleTapDetector] and compare
/// timestamps in your existing `onTap`/`onPointerDown` handler: single taps stay
/// instant and a quick second tap is reported as a double.
///
/// Used directly at sites that already own a `State`/impl or run inside a raw
/// `Listener` (treeitem, tabfolder, tableitem, canvas, composite). For stateless
/// leaf widgets, prefer the [InstantDoubleTapDetector] wrapper below.
class DoubleTapDetector {
  DoubleTapDetector({this.timeout = kDoubleTapTimeout, this.slop = 5.0});

  /// Maximum gap between two taps for them to count as a double-click.
  final Duration timeout;

  /// Maximum distance (in logical pixels) between two taps when [registerTap] is
  /// given a [position]. Ignored when no position is supplied.
  final double slop;

  DateTime? _lastTime;
  Object? _lastKey;
  Offset? _lastPos;

  /// Records a tap and returns `true` when it forms a double-click with the
  /// previous tap: same [key] (when given), within [timeout], and within [slop]
  /// pixels (when both taps supply a [position]). After a positive result the
  /// internal state is cleared so a third tap starts a fresh sequence.
  bool registerTap({Object? key, Offset? position}) {
    final now = DateTime.now();
    final lastTime = _lastTime;
    final lastKey = _lastKey;
    final lastPos = _lastPos;

    _lastTime = now;
    _lastKey = key;
    _lastPos = position;

    if (lastTime == null || now.difference(lastTime) >= timeout) return false;
    if (lastKey != key) return false;
    if (position != null && lastPos != null) {
      final dx = position.dx - lastPos.dx;
      final dy = position.dy - lastPos.dy;
      if (dx * dx + dy * dy > slop * slop) return false;
    }

    reset();
    return true;
  }

  /// Forgets the previous tap so the next [registerTap] cannot pair with it.
  void reset() {
    _lastTime = null;
    _lastKey = null;
    _lastPos = null;
  }
}

/// Drop-in replacement for a `GestureDetector` that has both `onTap` and
/// `onDoubleTap`, without the [kDoubleTapTimeout] (300ms) arena hold that delays
/// every single tap.
///
/// Internally it wires only a single-tap recognizer (which resolves the arena
/// immediately) and uses a [DoubleTapDetector] to route a quick second tap to
/// [onDoubleTap]. The first tap of a double still fires [onTap] -- matching SWT,
/// which sends a Selection per physical click before DefaultSelection.
///
/// For widgets rebuilt across logical rows (e.g. inside `ListView.builder`),
/// pass an [identity] (the item or index): when the slot is recycled to a
/// different row the stale timestamp is discarded, so two taps on what is now a
/// different row never register as a double.
class InstantDoubleTapDetector extends StatefulWidget {
  const InstantDoubleTapDetector({
    super.key,
    this.onTap,
    this.onDoubleTap,
    this.behavior,
    this.identity,
    required this.child,
  });

  final VoidCallback? onTap;
  final VoidCallback? onDoubleTap;
  final HitTestBehavior? behavior;
  final Object? identity;
  final Widget child;

  @override
  State<InstantDoubleTapDetector> createState() =>
      _InstantDoubleTapDetectorState();
}

class _InstantDoubleTapDetectorState extends State<InstantDoubleTapDetector> {
  final DoubleTapDetector _detector = DoubleTapDetector();

  @override
  void didUpdateWidget(InstantDoubleTapDetector oldWidget) {
    super.didUpdateWidget(oldWidget);
    if (widget.identity != oldWidget.identity) {
      _detector.reset();
    }
  }

  void _handleTap() {
    if (widget.onDoubleTap != null && _detector.registerTap()) {
      widget.onDoubleTap!();
    } else {
      widget.onTap?.call();
    }
  }

  @override
  Widget build(BuildContext context) {
    return GestureDetector(
      behavior: widget.behavior,
      // Always wire onTap so the detector sees every tap, even when the caller
      // only supplied onDoubleTap.
      onTap: _handleTap,
      child: widget.child,
    );
  }
}
