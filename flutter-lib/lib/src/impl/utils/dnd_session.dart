import '../../comm/comm.dart';
import '../../gen/event.dart';

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
