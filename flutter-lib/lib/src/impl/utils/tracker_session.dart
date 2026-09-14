import 'dart:async';

import 'package:flutter/gestures.dart';
import 'package:meta/meta.dart';

import '../../comm/comm.dart';
import '../../gen/event.dart';
import '../../gen/tracker.dart';


/// Drives an SWT `Tracker` from Flutter.
///
/// A Tracker owns the pointer for a whole gesture and blocks the Java UI thread in its own event
/// loop, so nothing on the Java side can advance or end it: the loop sees the pointer only through
/// the events sent from here, and stops only when told to. That is how the Eclipse workbench moves
/// a view between stacks — it opens a Tracker on `DragDetect` and reads the cursor on every
/// `SWT.Move`.
///
/// Nothing is painted. An application parks the band off-screen and draws its own feedback (the
/// workbench does), so this only has to report where the pointer is and when the gesture ended.
class TrackerSession {
  TrackerSession._();

  static final Set<String> _hosts = {};
  static int? _activeId;
  static bool _routeInstalled = false;

  /// Where the pointer was last seen, and whether it is still down — kept from the global route
  /// whether or not a Tracker is open, because a Tracker always arrives mid-gesture at the earliest.
  static Offset? _lastPosition;
  static bool _pointerDown = false;

  /// Whether this gesture's press was actually observed. The route is installed when a Shell
  /// mounts, so in the application it always is; a caller that attaches mid-gesture has not seen
  /// one, and must not be told the pointer is up on that basis.
  static bool _sawPointerDown = false;

  /// Where the pointer has been since it went down. A Tracker only exists once Java has answered
  /// DragDetect, so the start of every gesture happens before there is anything to report it to;
  /// this is what the loop is given to catch up with, instead of a single jump to the end.
  static final List<Offset> _path = [];

  /// Bounded so a long gesture cannot grow it without limit — the workbench only needs enough of
  /// the path to resolve a drop, not every frame of it.
  static const _maxPathPoints = 64;

  /// How far the pointer must travel before its new position is worth reporting. Java answers every
  /// position with a Display snapshot that carries every Shell, so the cost of a gesture is set by
  /// how many times it is sampled, not by how long it lasts. An application re-resolves the drop on
  /// each one, and its answer only changes when the pointer reaches a different target, so sampling
  /// finer than that is paid for and thrown away.
  static const _moveThresholdPx = 40.0;

  /// The last position actually reported, which is what the threshold measures from — not the last
  /// position seen, or a slow drag would never report at all.
  static Offset? _lastReported;


  /// Whether a Tracker currently holds the pointer. Widgets that run their own drag gesture check
  /// this so two mechanisms don't both act on one drag.
  static bool get isTracking => _activeId != null;

  /// Turns a pointer position into the coordinate space the rest of the tree is expressed in.
  /// Registered per Shell, because only the Shell knows where its content sits inside the window.
  static final Map<String, Offset Function(Offset)> _toDisplay = {};

  /// The host whose Tracker is open, so a position is converted by the Shell it belongs to.
  static String? _activeHost;

  static Offset _convert(Offset viewPosition) {
    final convert = _toDisplay[_activeHost];
    return convert == null ? viewPosition : convert(viewPosition);
  }

  /// Subscribes to the Tracker channel of the Shell a Tracker is opened on. A Tracker is not a
  /// Control and never appears in the widget tree, so it has no State of its own to listen from.
  static void attachHost(String swt, int id, {Offset Function(Offset)? toDisplay}) {
    final key = '$swt/$id';
    // The converter is re-registered even for a host already subscribed: it closes over the Shell's
    // current layout, so a stale one would answer with the geometry of a previous frame.
    if (toDisplay != null) _toDisplay[key] = toDisplay;
    if (!_hosts.add(key)) return;
    // Watching starts here, not when a Tracker opens: the button goes down well before Java has
    // answered DragDetect, so a route installed at open time would already have missed it.
    if (!_routeInstalled) {
      _routeInstalled = true;
      GestureBinding.instance.pointerRouter.addGlobalRoute(_onPointer);
    }
    EquoCommService.onRaw('$key/Tracker/open', (payload) {
      _activeHost = key;
      _begin(VEvent.fromJson(payload as Map<String, dynamic>).itemId);
    });
    EquoCommService.onRaw('$key/Tracker/close', (_) => _release());
  }

  static void _begin(int? trackerId) {
    if (trackerId == null || trackerId == 0) return;
    _activeId = trackerId;

    // The Tracker only exists once Java has answered DragDetect, which is a round trip: by now the
    // pointer has moved, and on a quick gesture it is already up. Replay what the route recorded in
    // the meantime, so the loop sees a drag it can resolve rather than one jump to the end -- the
    // workbench decides where a view lands from the path, not from the final point alone.
    for (final point in _path) {
      _send(trackerId, 'Control/Move', point);
    }
    final position = _lastPosition;
    if (position != null && (_path.isEmpty || _path.last != position)) {
      _send(trackerId, 'Control/Move', position);
    }
    // Released before the Tracker even opened: nothing more is coming, so end it on the position it
    // ended at. Waiting instead means the loop sits idle until Java's silence timeout and gives up,
    // and a Tracker that times out is reported as cancelled -- the workbench discards the drop.
    // Only on a press this route actually saw: ending a gesture that is still live would be worse
    // than waiting for it.
    if (_sawPointerDown && !_pointerDown) {
      _finish(trackerId, 'Tracker/close');
      return;
    }
  }

  static void _release() {
    _activeId = null;
  }

  static void _onPointer(PointerEvent event) {
    // Bookkeeping first, and whether a Tracker is open or not: the gesture starts well before Java
    // has answered DragDetect, so where the pointer is -- and whether it is still down -- has to be
    // known by the time the Tracker arrives. See _begin.
    // A pointer position is global to the window, which includes whatever frame the client draws
    // around the Shell; everything the workbench compares it against is in the Shell's own
    // coordinates. Converting here keeps that difference out of the rest of the session.
    final position = _convert(event.position);
    _lastPosition = position;
    final ended = event is PointerUpEvent || event is PointerCancelEvent;
    if (event is PointerDownEvent) {
      _pointerDown = true;
      _sawPointerDown = true;
      _lastReported = position;
      _path
        ..clear()
        ..add(position);
    } else if (ended) {
      _pointerDown = false;
    }
    // The end of the gesture always goes out -- it is the position the drop resolves on. Everything
    // between is sampled, so a position too close to the last one reported is dropped here rather
    // than costing a round trip that cannot change the answer.
    if (!ended) {
      final reported = _lastReported;
      if (reported != null &&
          (position - reported).distance < _moveThresholdPx) {
        return;
      }
    }
    _lastReported = position;
    if (_pointerDown) {
      // A rolling window, dropping the oldest: what the workbench needs is how the gesture
      // arrived where it ended, and keeping the first points of a long drag instead would replay
      // the approach to a place the pointer left seconds ago.
      _path.add(position);
      if (_path.length > _maxPathPoints) _path.removeAt(0);
    }

    final id = _activeId;
    if (id == null) return;
    if (event is PointerMoveEvent || event is PointerHoverEvent) {
      _send(id, 'Control/Move', position);
    } else if (event is PointerUpEvent || event is PointerCancelEvent) {
      _send(id, 'Control/Move', position);
      _finish(id, event is PointerCancelEvent ? 'Tracker/cancel' : 'Tracker/close');
    }
  }

  static void _finish(int id, String action) {
    _release();
    _sendEvent(id, action, VEvent());
  }

  static void _send(int id, String action, Offset position) {
    _sendEvent(
      id,
      action,
      VEvent()
        ..x = position.dx.round()
        ..y = position.dy.round(),
    );
  }

  /// Stands in for the transport in a widget test, which has no live one. A Tracker is not a
  /// Control, so unlike every other widget there is no `*Swt` instance a test could subclass.
  @visibleForTesting
  static void Function(int id, String action, VEvent event)? sendForTesting;

  static void _sendEvent(int id, String action, VEvent event) {
    final override = sendForTesting;
    if (override != null) {
      override(id, action, event);
      return;
    }
    final value = VTracker()..id = id;
    TrackerSwt<VTracker>(value: value).sendEvent(value, action, event);
  }
}
