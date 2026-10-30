import 'dart:async';
import 'dart:typed_data';

import 'package:flutter/gestures.dart';
import 'package:meta/meta.dart';

import '../../comm/comm.dart';
import '../../comm/channel_header.dart';
import '../../comm/tagged_values.dart';
import '../../gen/widgets.dart' show WireChannel, WireType;
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

  /// How long the pointer must stay put before a position the threshold held back is reported
  /// anyway. A drop target can be smaller than the threshold — a tab strip is — so the pointer can
  /// come to rest on one without ever having been reported there.
  static const _settleDelay = Duration(milliseconds: 100);

  static Timer? _settle;


  /// Whether a Tracker currently holds the pointer. Widgets that run their own drag gesture check
  /// this so two mechanisms don't both act on one drag.
  static bool get isTracking => _activeId != null;

  /// The Shells a Tracker can be opened on, by id, each with what turns a pointer position into the
  /// coordinate space the rest of the tree is expressed in: only the Shell knows where its content
  /// sits inside the window. A Shell is in it from mount to dispose.
  static final Map<int, Offset Function(Offset)?> _hosts = {};

  /// The host whose Tracker is open, so a position is converted by the Shell it belongs to.
  static int? _activeHost;

  static final int _itemIdKey = TaggedValues.keyNumber('itemId');

  /// Registers what every Shell answers for its Trackers (see `registerWidgetOps`).
  static void registerOps() {
    final type = WireType.shell;
    EquoCommService.onWidgetOp(type, WireChannel.trackerOpen, _openOp);
    EquoCommService.onWidgetOp(type, WireChannel.trackerClose, _closeOp);
  }

  static FutureOr<bool> _openOp(int id, Uint8List frame, int start, int end) {
    if (!_hosts.containsKey(id)) return false;
    _activeHost = id;
    _begin(TaggedValues.intField(frame, start, _itemIdKey));
    return true;
  }

  static FutureOr<bool> _closeOp(int id, Uint8List frame, int start, int end) {
    if (!_hosts.containsKey(id)) return false;
    _release();
    return true;
  }

  /// Converts on the way out, not when a position is recorded: which Shell a gesture belongs to is
  /// only known once its Tracker opens, and the gesture starts before that. Converting earlier makes
  /// [_begin]'s replay report window coordinates, off by the Shell's own header.
  static Offset _convert(Offset viewPosition) {
    final convert = _hosts[_activeHost];
    return convert == null ? viewPosition : convert(viewPosition);
  }

  /// Listens for the Trackers opened on the Shell [id]. A Tracker is not a Control and never
  /// appears in the widget tree, so it has no State of its own to listen from. [swt] is the Shell's
  /// type, which the channels' numbers do not need.
  static void attachHost(String swt, int id, {Offset Function(Offset)? toDisplay}) {
    final known = _hosts.containsKey(id);
    // The converter is re-registered even for a host already attached: it closes over the Shell's
    // current layout, so a stale one would answer with the geometry of a previous frame.
    if (toDisplay != null || !known) _hosts[id] = toDisplay ?? _hosts[id];
    if (known) return;
    // Watching starts here, not when a Tracker opens: the button goes down well before Java has
    // answered DragDetect, so a route installed at open time would already have missed it.
    if (!_routeInstalled) {
      _routeInstalled = true;
      GestureBinding.instance.pointerRouter.addGlobalRoute(_onPointer);
    }
    EquoCommService.replayWidgetOps(id);
  }

  @visibleForTesting
  static int get hosts => _hosts.length;

  /// Stops listening for the Shell [id]'s Trackers, when it goes away.
  static void detachHost(int id) {
    _hosts.remove(id);
    if (_activeHost == id) {
      _activeHost = null;
      _release();
    }
  }

  static void _begin(int? trackerId) {
    if (trackerId == null || trackerId == 0) return;
    _activeId = trackerId;

    // The Tracker only exists once Java has answered DragDetect, which is a round trip: by now the
    // pointer has moved, and on a quick gesture it is already up. Replay what the route recorded in
    // the meantime, so the loop sees a drag it can resolve rather than one jump to the end -- the
    // workbench decides where a view lands from the path, not from the final point alone.
    for (final point in _path) {
      _send(trackerId, WireChannel.controlMove, point);
    }
    final position = _lastPosition;
    if (position != null && (_path.isEmpty || _path.last != position)) {
      _send(trackerId, WireChannel.controlMove, position);
    }
    // Released before the Tracker even opened: nothing more is coming, so end it on the position it
    // ended at. Waiting instead means the loop sits idle until Java's silence timeout and gives up,
    // and a Tracker that times out is reported as cancelled -- the workbench discards the drop.
    // Only on a press this route actually saw: ending a gesture that is still live would be worse
    // than waiting for it.
    if (_sawPointerDown && !_pointerDown) {
      _finish(trackerId, WireChannel.trackerClose);
      return;
    }
  }

  static void _release() {
    _activeId = null;
    _settle?.cancel();
    _settle = null;
  }

  static void _onPointer(PointerEvent event) {
    // Bookkeeping first, and whether a Tracker is open or not: the gesture starts well before Java
    // has answered DragDetect, so where the pointer is -- and whether it is still down -- has to be
    // known by the time the Tracker arrives. See _begin.
    // Kept as the window position it arrives as, and converted into the Shell's coordinates only on
    // the way out: see _convert.
    final position = event.position;
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
        if (_activeId != null) _scheduleSettle();
        return;
      }
    }
    _settle?.cancel();
    final unreported = _lastReported != position;
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
      _send(id, WireChannel.controlMove, position);
    } else if (event is PointerUpEvent || event is PointerCancelEvent) {
      _send(id, WireChannel.controlMove, position);
      // The workbench only enters a target on the first move there and accepts the drop on the
      // next; a release where nothing was reported yet would otherwise be resolved as "no drop".
      if (unreported && event is PointerUpEvent) _send(id, WireChannel.controlMove, position);
      _finish(id, event is PointerCancelEvent ? WireChannel.trackerCancel : WireChannel.trackerClose);
    }
  }

  /// Reports where the pointer came to rest when the threshold held that position back, twice for
  /// the same reason a release is: the first move enters the target, the second shows the drop there.
  static void _scheduleSettle() {
    _settle?.cancel();
    _settle = Timer(_settleDelay, () {
      _settle = null;
      final id = _activeId;
      final position = _lastPosition;
      if (id == null || position == null || !_pointerDown || position == _lastReported) return;
      _lastReported = position;
      _send(id, WireChannel.controlMove, position);
      _send(id, WireChannel.controlMove, position);
    });
  }

  static void _finish(int id, int action) {
    _release();
    _sendEvent(id, action, VEvent());
  }

  static void _send(int id, int action, Offset position) {
    final converted = _convert(position);
    _sendEvent(
      id,
      action,
      VEvent()
        ..x = converted.dx.round()
        ..y = converted.dy.round(),
    );
  }

  /// Stands in for the transport in a widget test, which has no live one. A Tracker is not a
  /// Control, so unlike every other widget there is no `*Swt` instance a test could subclass.
  @visibleForTesting
  static void Function(int id, int action, VEvent event)? sendForTesting;

  static void _sendEvent(int id, int action, VEvent event) {
    final override = sendForTesting;
    if (override != null) {
      override(id, action, event);
      return;
    }
    final value = VTracker()..id = id;
    TrackerSwt<VTracker>(value: value).sendEvent(value, action, event);
  }
}
