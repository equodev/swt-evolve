import 'package:flutter/gestures.dart' show kDoubleTapTimeout;
import 'package:flutter/widgets.dart';

/// True only when this session is driven by our E2E test harness -- set once
/// from `main()`, mirroring `getEnableTestSemantics` (see `main.dart` /
/// `web_platform.dart`). Always false in production.
///
/// [DoubleTapDetector] widens its window when this is set: a CI runner's
/// added event-processing latency between a Playwright-dispatched click pair
/// can push a genuine double-click past the window real users hit, even
/// though production timing (tuned per-widget -- see call sites below) is
/// unaffected. This keeps the E2E scenario deterministic without changing
/// what any real user sees.
bool e2eTestMode = false;

/// Only used in [e2eTestMode] -- generous on purpose since it never reaches
/// production; must comfortably clear CI's observed worst-case gap (~500ms).
const Duration _e2eDoubleClickTimeout = Duration(milliseconds: 1000);

/// Arena-free multi-click detection (single / double / triple).
///
/// Flutter's `DoubleTapGestureRecognizer` -- created by any `GestureDetector`
/// or `InkWell` that sets `onDoubleTap` -- holds the gesture arena for its
/// `kDoubleTapTimeout` (300ms) on every tap, delaying every single tap by that
/// much. Instead of paying that cost, hold a [DoubleTapDetector] and compare
/// timestamps in your existing `onTap`/`onPointerDown` handler: single taps stay
/// instant and a quick second tap is reported as a double.
///
/// Used directly at sites that already own a `State`/impl or run inside a raw
/// `Listener` (treeitem, tabfolder, tableitem, canvas, composite). For stateless
/// leaf widgets, prefer the [InstantDoubleTapDetector] wrapper below.
///
/// Defaults to Flutter's `kDoubleTapTimeout` (300ms), same as before -- widget
/// call sites that need a different window pass [timeout] explicitly (see
/// `styledtext_evolve.dart`). Do not widen the shared default here; per-widget
/// double-click timing is a deliberate, already-tuned product decision.
class DoubleTapDetector {
  DoubleTapDetector({Duration? timeout, this.slop = 5.0})
      : timeout = timeout ?? (e2eTestMode ? _e2eDoubleClickTimeout : kDoubleTapTimeout);

  /// Maximum gap between two taps for them to count as a double-click.
  final Duration timeout;

  /// Maximum distance (in logical pixels) between two taps when [registerTap] is
  /// given a [position]. Ignored when no position is supplied.
  final double slop;

  int _tapCount = 0;
  DateTime? _lastTime;
  Object? _lastKey;
  Offset? _lastPos;

  /// Records a tap and returns the consecutive tap count: 1 for a fresh tap,
  /// 2 for a double-click, 3 for a triple-click. Each tap must be within
  /// [timeout] and [slop] of the previous one to continue the sequence.
  /// After a triple (count == 3) the internal state is cleared so the next
  /// tap starts a fresh sequence.
  int registerTap({Object? key, Offset? position}) {
    final now = DateTime.now();
    final prevTime = _lastTime;
    final prevKey = _lastKey;
    final prevPos = _lastPos;

    _lastTime = now;
    _lastKey = key;
    _lastPos = position;

    bool consecutive = prevTime != null &&
        now.difference(prevTime) < timeout &&
        prevKey == key;
    if (consecutive && position != null && prevPos != null) {
      final dx = position.dx - prevPos.dx;
      final dy = position.dy - prevPos.dy;
      consecutive = dx * dx + dy * dy <= slop * slop;
    }

    if (!consecutive) {
      _tapCount = 1;
      return 1;
    }

    _tapCount++;
    if (_tapCount >= 3) {
      reset();
      return 3;
    }
    return _tapCount;
  }

  /// Forgets the previous tap so the next [registerTap] cannot pair with it.
  void reset() {
    _tapCount = 0;
    _lastTime = null;
    _lastKey = null;
    _lastPos = null;
  }
}

/// Drop-in replacement for a `GestureDetector` that has `onTap`, `onDoubleTap`,
/// and optionally `onTripleTap`, without the `kDoubleTapTimeout` (300ms) arena
/// hold that delays every single tap.
///
/// Internally it wires only a single-tap recognizer (which resolves the arena
/// immediately) and uses a [DoubleTapDetector] to route quick successive taps
/// to [onDoubleTap] or [onTripleTap]. The first tap of a double/triple still
/// fires [onTap] -- matching SWT, which sends a Selection per physical click
/// before DefaultSelection.
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
    this.onTripleTap,
    this.behavior,
    this.identity,
    required this.child,
  });

  final VoidCallback? onTap;
  final VoidCallback? onDoubleTap;
  final VoidCallback? onTripleTap;
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
    final count = _detector.registerTap();
    if (count == 3 && widget.onTripleTap != null) {
      widget.onTripleTap!();
    } else if (count == 2 && widget.onDoubleTap != null) {
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
      // only supplied onDoubleTap or onTripleTap.
      onTap: _handleTap,
      child: widget.child,
    );
  }
}
