import 'package:flutter/gestures.dart';

import '../../comm/comm.dart';
import '../../gen/event.dart';
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
  static final Map<int, bool> _subscribed = {};
  static final Map<int, List<void Function(DndNegotiationState)>> _listeners = {};

  static DndNegotiationState stateFor(int dropTargetId) =>
      _state[dropTargetId] ?? DndNegotiationState.none;

  static void listen(int dropTargetId, void Function(DndNegotiationState) onUpdate) {
    (_listeners[dropTargetId] ??= []).add(onUpdate);
    if (_subscribed[dropTargetId] == true) return;
    _subscribed[dropTargetId] = true;
    EquoCommService.onRaw("DropTarget/$dropTargetId/Drop/negotiationResult", (payload) {
      final event = VEvent.fromJson(payload as Map<String, dynamic>);
      final state = DndNegotiationState(
        detail: event.detail ?? 0,
        feedback: event.feedback ?? 0,
        currentDataTypeId: event.currentDataTypeId ?? 0,
      );
      _state[dropTargetId] = state;
      for (final listener in List.of(_listeners[dropTargetId] ?? const [])) {
        listener(state);
      }
    });
  }

  static void unlisten(int dropTargetId, void Function(DndNegotiationState) onUpdate) {
    _listeners[dropTargetId]?.remove(onUpdate);
  }
}

/// Whether the drag in flight was vetoed by Java's `dragStart` listener.
///
/// The answer arrives on the *source* control's channel while the check happens on the *drop
/// target* — a different widget — so the link between them lives here for one gesture.
class DragStartVeto {
  DragStartVeto._();

  static final Map<String, VetoGate> _gates = {};
  static VetoGate? _inFlight;
  static bool _vetoed = false;

  static bool get isVetoed => _vetoed;

  static void begin(String controlSwt, int controlId) {
    _vetoed = false;
    final gate = _gates.putIfAbsent('$controlSwt/$controlId', () {
      final created = VetoGate.optimistic('DragDetect/dragStartResult');
      created.attach(controlSwt, controlId);
      return created;
    });
    _inFlight = gate;
    gate.propose((doit) {
      // Without this guard a late verdict rejects whatever drag is in flight next.
      if (!doit && identical(_inFlight, gate)) _vetoed = true;
    });
  }
}
