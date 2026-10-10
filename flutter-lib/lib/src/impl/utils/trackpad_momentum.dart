import 'package:flutter/gestures.dart';
import 'package:flutter/physics.dart';
import 'package:flutter/scheduler.dart';
import 'package:flutter/widgets.dart';

/// The scroll a two-finger trackpad swipe keeps after the fingers lift.
///
/// The desktop embedders deliver the swipe as pan/zoom updates and drop the system's momentum
/// events, leaving momentum to the framework the way a Flutter scrollable makes it: a fling with the
/// platform's scroll physics at the fingers' release velocity. A control that turns the swipe into
/// wheel deltas therefore has to make it too, or the content stops dead where native keeps gliding.
class TrackpadMomentum {
  TrackpadMomentum(this._scroll);

  /// Receives each step of the glide, as the wheel delta a swipe of that distance gives.
  final void Function(Offset wheelDelta) _scroll;

  VelocityTracker? _tracker;
  Ticker? _ticker;
  Simulation? _x;
  Simulation? _y;
  Offset _travelled = Offset.zero;

  bool get isGliding => _ticker?.isActive ?? false;

  void panStart(PointerPanZoomStartEvent event) {
    stop();
    _tracker = VelocityTracker.withKind(PointerDeviceKind.trackpad);
  }

  void panUpdate(PointerPanZoomUpdateEvent event) {
    _tracker?.addPosition(event.timeStamp, event.pan);
  }

  /// Starts the glide at the fingers' release velocity, with [physics] (the platform's).
  void panEnd(ScrollPhysics physics, double devicePixelRatio) {
    final tracker = _tracker;
    _tracker = null;
    if (tracker == null) return;
    final estimate = tracker.getVelocityEstimate();
    if (estimate == null) return;
    final fingers = estimate.pixelsPerSecond;
    if (fingers.distance < physics.minFlingVelocity) return;
    final capped = fingers.distance > physics.maxFlingVelocity
        ? fingers * (physics.maxFlingVelocity / fingers.distance)
        : fingers;
    // The content follows the fingers, so the scroll runs against them.
    _x = _glide(physics, -capped.dx, devicePixelRatio);
    _y = _glide(physics, -capped.dy, devicePixelRatio);
    if (_x == null && _y == null) return;
    _travelled = Offset.zero;
    _ticker = Ticker(_tick)..start();
  }

  void stop() {
    _ticker?.dispose();
    _ticker = null;
    _x = null;
    _y = null;
  }

  void dispose() {
    stop();
    _tracker = null;
  }

  /// No extent: where the content ends is Java's to say, and it clamps what it is sent.
  static Simulation? _glide(ScrollPhysics physics, double velocity, double devicePixelRatio) {
    if (velocity == 0) return null;
    final unbounded = FixedScrollMetrics(
      minScrollExtent: double.negativeInfinity,
      maxScrollExtent: double.infinity,
      pixels: 0,
      viewportDimension: 0,
      axisDirection: AxisDirection.down,
      devicePixelRatio: devicePixelRatio,
    );
    return physics.createBallisticSimulation(unbounded, velocity);
  }

  void _tick(Duration elapsed) {
    final t = elapsed.inMicroseconds / Duration.microsecondsPerSecond;
    final x = _x, y = _y;
    final at = Offset(x?.x(t) ?? 0, y?.x(t) ?? 0);
    final step = at - _travelled;
    _travelled = at;
    final done = (x?.isDone(t) ?? true) && (y?.isDone(t) ?? true);
    if (done) stop();
    if (step != Offset.zero) _scroll(step);
  }
}
