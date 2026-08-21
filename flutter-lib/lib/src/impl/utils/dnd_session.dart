import 'package:flutter/gestures.dart';

import '../../comm/comm.dart';
import '../../gen/event.dart';

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

class DragStartVeto {
  DragStartVeto._();

  static bool _vetoed = false;
  static final Map<int, bool> _subscribed = {};

  static bool get isVetoed => _vetoed;

  static void reset() {
    _vetoed = false;
  }

  static void listenFor(String controlSwt, int controlId) {
    if (_subscribed[controlId] == true) return;
    _subscribed[controlId] = true;
    EquoCommService.onRaw("$controlSwt/$controlId/DragDetect/dragStartResult", (payload) {
      final event = VEvent.fromJson(payload as Map<String, dynamic>);
      if (event.doit == false) _vetoed = true;
    });
  }
}
