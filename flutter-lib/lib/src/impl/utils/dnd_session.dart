import 'dart:async';
import 'dart:typed_data';

import 'package:flutter/gestures.dart';
import 'package:meta/meta.dart';
import 'package:flutter/scheduler.dart';

import '../../comm/comm.dart';
import '../../comm/channel_header.dart';
import '../../comm/widget_wire.dart' show WidgetWireReader;
import '../../gen/event.dart';
import '../../gen/widgets.dart' show WireChannel, WireType;
import 'hover_arbiter.dart';
import 'veto_gate.dart';

/// Tracks whether a pointer button is held down anywhere in the app, to suppress
/// MouseTrack enter/exit forwarding to Java during a drag-like gesture.
///
/// On the web, Flutter keeps updating [MouseRegion] membership while a button is
/// held, unlike real OS-level DND, so an unsuppressed onExit mid-drag would forward
/// a spurious SWT MouseExit. Listens on the pointer router directly rather than
/// [Draggable]'s onDragStarted, which only fires after the drag distance threshold
/// is crossed — by then the origin's onExit may already have fired.
class ActiveDragTracker {
  ActiveDragTracker._();

  static bool _pointerDown = false;
  static bool _initialized = false;

  static bool get isSuppressingHover => _pointerDown;

  static void ensureInitialized() {
    if (_initialized) return;
    _initialized = true;
    GestureBinding.instance.pointerRouter.addGlobalRoute(_onPointerEvent);
  }

  static void _onPointerEvent(PointerEvent event) {
    if (event is PointerDownEvent) {
      _pointerDown = true;
    } else if (event is PointerUpEvent || event is PointerCancelEvent) {
      _pointerDown = false;
      // Settled once the frame the release is in has been laid out and hit-tested again: a drag's
      // last step (a sash, a scroll) moves what is under the pointer only in that frame.
      final scheduler = SchedulerBinding.instance;
      scheduler.addPostFrameCallback((_) => HoverExclusivityArbiter.instance.settle());
      scheduler.scheduleFrame();
    }
  }
}

class DndNegotiationState {
  final int detail;
  final int feedback;
  final int currentDataTypeId;

  const DndNegotiationState({
    required this.detail,
    required this.feedback,
    required this.currentDataTypeId,
  });

  static const none = DndNegotiationState(detail: 0, feedback: 0, currentDataTypeId: 0);
}

class DndSession {
  DndSession._();

  static final Map<int, DndNegotiationState> _state = {};

  /// Who listens for each drop target's answers, by its id: a drop target is in it while something
  /// shows it, and leaves with the last of them.
  static final Map<int, List<void Function(DndNegotiationState)>> _listeners = {};

  /// Registers what every DropTarget answers (see `registerWidgetOps`).
  static void registerOps() => EquoCommService.onWidgetOp(
      WireType.dropTarget, WireChannel.dropNegotiationResult, _negotiationOp);

  static DndNegotiationState stateFor(int dropTargetId) =>
      _state[dropTargetId] ?? DndNegotiationState.none;

  /// Java's answer to a drag over a drop target: an event's fields, sent only when it changes.
  static FutureOr<bool> _negotiationOp(int id, Uint8List frame, int start, int end) {
    final listeners = _listeners[id];
    if (listeners == null) return false;
    final event = (WidgetWireReader(frame)..pos = start).fields(VEvent());
    final state = DndNegotiationState(
      detail: event.detail ?? 0,
      feedback: event.feedback ?? 0,
      currentDataTypeId: event.currentDataTypeId ?? 0,
    );
    _state[id] = state;
    for (final listener in List.of(listeners)) {
      listener(state);
    }
    return true;
  }

  static void listen(int dropTargetId, void Function(DndNegotiationState) onUpdate) {
    final listeners = _listeners[dropTargetId] ??= [];
    listeners.add(onUpdate);
    if (listeners.length == 1) EquoCommService.replayWidgetOps(dropTargetId);
  }

  static void clear(int dropTargetId) => _state.remove(dropTargetId);

  static void unlisten(int dropTargetId, void Function(DndNegotiationState) onUpdate) {
    final listeners = _listeners[dropTargetId];
    if (listeners == null || !listeners.remove(onUpdate) || listeners.isNotEmpty) return;
    _listeners.remove(dropTargetId);
    _state.remove(dropTargetId);
  }

  @visibleForTesting
  static int get listenedDropTargets => _listeners.length;
}

/// Whether the drag in flight was vetoed by Java's `dragStart` listener.
///
/// The answer arrives on the *source* control's channel while the check happens on the *drop
/// target* — a different widget — so the link between them lives here for one gesture.
class DragStartVeto {
  DragStartVeto._();

  /// One gate per control that starts drags, by id, from its first drag until it goes away.
  static final Map<int, VetoGate> _gates = {};
  static VetoGate? _inFlight;
  static bool _vetoed = false;

  static bool get isVetoed => _vetoed;

  /// Clears the verdict for a gesture that has none to wait for — a drag started on a control
  /// with no SWT DragSource (a CTabFolder reordering its own tabs). Without this it would
  /// inherit whatever the previous drag was told.
  static void reset() {
    _vetoed = false;
    _inFlight = null;
  }

  static void begin(int controlType, int controlId) {
    _vetoed = false;
    final gate = _gates.putIfAbsent(controlId, () {
      final created = VetoGate.optimistic(WireChannel.dragDetectDragStartResult);
      created.attach(controlType, controlId);
      return created;
    });
    _inFlight = gate;
    gate.propose((doit) {
      // Without this guard a late verdict rejects whatever drag is in flight next.
      if (!doit && identical(_inFlight, gate)) _vetoed = true;
    });
  }

  /// Drops the gate of the control [controlId], when it goes away.
  static void forget(int controlId) {
    final gate = _gates.remove(controlId);
    if (gate == null) return;
    if (identical(_inFlight, gate)) _inFlight = null;
    gate.detach();
  }

  @visibleForTesting
  static int get gates => _gates.length;
}
