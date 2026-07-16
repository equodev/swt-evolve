import 'package:flutter/material.dart';
import '../../gen/control.dart';
import '../../gen/droptarget.dart';
import '../../gen/event.dart';
import '../../gen/widget.dart';
import 'dnd_session.dart';

class DndDragPayload {
  final int sourceControlId;
  final int? index;
  final int? itemId;
  const DndDragPayload({required this.sourceControlId, this.index, this.itemId});
}

class HoverTracker<T> {
  final ValueNotifier<T?> notifier = ValueNotifier<T?>(null);
  T? get value => notifier.value;

  void update(T? newValue) {
    if (notifier.value == newValue) return;
    notifier.value = newValue;
  }
}

class Dnd {
  Dnd._();

  static const dropNone = 0;
  static const dropCopy = 1 << 0;
  static const dropMove = 1 << 1;
  static const dropLink = 1 << 2;
  static const dropDefault = 1 << 4;

  static const feedbackNone = 0;
  static const feedbackSelect = 1;
  static const feedbackInsertBefore = 2;
  static const feedbackInsertAfter = 4;
  static const feedbackScroll = 8;
  static const feedbackExpand = 16;
}

Widget wrapDraggable<T extends Object>({
  required Widget child,
  required T data,
  required ControlSwt widget,
  required VControl state,
  VoidCallback? onDragStarted,
  Widget Function(Widget child)? feedbackBuilder,
  Widget Function(Widget child)? childWhenDraggingBuilder,
  bool useLongPress = false,
}) {
  if (state.dragSource != true) return child;

  void fireDragDetect() {
    DragStartVeto.reset();
    DragStartVeto.listenFor(state.swt, state.id);
    onDragStarted?.call();
    widget.sendDragDetectDragDetect(state, VEvent());
  }

  final feedback = feedbackBuilder != null
      ? feedbackBuilder(child)
      : Opacity(
          opacity: 0.85,
          child: Material(type: MaterialType.transparency, child: child),
        );
  final childWhenDragging = childWhenDraggingBuilder != null
      ? childWhenDraggingBuilder(child)
      : Opacity(opacity: 0.3, child: child);

  return useLongPress
      ? LongPressDraggable<T>(
          data: data,
          dragAnchorStrategy: pointerDragAnchorStrategy,
          feedback: feedback,
          childWhenDragging: childWhenDragging,
          onDragStarted: fireDragDetect,
          child: child,
        )
      : Draggable<T>(
          data: data,
          dragAnchorStrategy: pointerDragAnchorStrategy,
          feedback: feedback,
          childWhenDragging: childWhenDragging,
          onDragStarted: fireDragDetect,
          child: child,
        );
}

Widget wrapDropTarget<T extends Object>({
  required Widget child,
  required VControl state,
  required void Function(T data, int? index, int? itemId, Offset position) onDrop,
  int Function(DragTargetDetails<T> details)? resolveIndex,
  int Function(DragTargetDetails<T> details)? resolveItemId,
  Offset Function(DragTargetDetails<T> details)? resolvePosition,
  Widget Function(BuildContext context, Widget child, DndNegotiationState negotiation, bool isHovering)? builder,
}) {
  final dropTargetId = state.dropTargetId;
  if (dropTargetId == null) return child;

  return _DropTargetNegotiator<T>(
    dropTargetId: dropTargetId,
    resolveIndex: resolveIndex,
    resolveItemId: resolveItemId,
    resolvePosition: resolvePosition,
    onDrop: onDrop,
    builder: builder,
    child: child,
  );
}

class _DropTargetNegotiator<T extends Object> extends StatefulWidget {
  final int dropTargetId;
  final int Function(DragTargetDetails<T> details)? resolveIndex;
  final int Function(DragTargetDetails<T> details)? resolveItemId;
  final Offset Function(DragTargetDetails<T> details)? resolvePosition;
  final void Function(T data, int? index, int? itemId, Offset position) onDrop;
  final Widget Function(BuildContext context, Widget child, DndNegotiationState negotiation, bool isHovering)? builder;
  final Widget child;

  const _DropTargetNegotiator({
    super.key,
    required this.dropTargetId,
    required this.resolveIndex,
    required this.resolveItemId,
    required this.resolvePosition,
    required this.onDrop,
    required this.builder,
    required this.child,
  });

  @override
  State<_DropTargetNegotiator<T>> createState() => _DropTargetNegotiatorState<T>();
}

class _DropTargetNegotiatorState<T extends Object> extends State<_DropTargetNegotiator<T>> {
  @override
  void initState() {
    super.initState();
    DndSession.listen(widget.dropTargetId, _onNegotiationUpdate);
  }

  @override
  void dispose() {
    DndSession.unlisten(widget.dropTargetId, _onNegotiationUpdate);
    super.dispose();
  }

  void _onNegotiationUpdate(DndNegotiationState state) {
    if (mounted) setState(() {});
  }

  Offset _resolvePosition(DragTargetDetails<T> details) =>
      widget.resolvePosition != null ? widget.resolvePosition!(details) : details.offset;

  void _sendNegotiation(String ev, DragTargetDetails<T>? details) {
    final dropTargetValue = VDropTarget()..id = widget.dropTargetId;
    final event = VEvent();
    if (details != null) {
      final position = _resolvePosition(details);
      event.x = position.dx.round();
      event.y = position.dy.round();
      if (widget.resolveIndex != null) {
        event.index = widget.resolveIndex!(details);
      }
      if (widget.resolveItemId != null) {
        event.itemId = widget.resolveItemId!(details);
      }
    }
    DropTargetSwt<VDropTarget>(value: dropTargetValue)
        .sendEvent(dropTargetValue, "Drop/$ev", event);
  }

  @override
  Widget build(BuildContext context) {
    return DragTarget<T>(
      onWillAcceptWithDetails: (details) {
        _sendNegotiation("dragEnter", details);
        return true;
      },
      onMove: (details) => _sendNegotiation("dragOver", details),
      onLeave: (_) => _sendNegotiation("dragLeave", null),
      onAcceptWithDetails: (details) {
        if (DragStartVeto.isVetoed) return;
        final index = widget.resolveIndex != null ? widget.resolveIndex!(details) : null;
        final itemId = widget.resolveItemId != null ? widget.resolveItemId!(details) : null;
        widget.onDrop(details.data, index, itemId, _resolvePosition(details));
      },
      builder: (context, candidateData, rejectedData) {
        final negotiation = DndSession.stateFor(widget.dropTargetId);
        return widget.builder != null
            ? widget.builder!(context, widget.child, negotiation, candidateData.isNotEmpty)
            : widget.child;
      },
    );
  }
}
