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
  bool alwaysDraggable = false,
  VEvent Function()? dragDetectEvent,
}) {
  // alwaysDraggable: a CTabFolder tab is draggable for the folder's own tab reordering even when
  // the application installed no DragSource.
  final hasDragSource = state.dragSource == true;
  if (!hasDragSource && !alwaysDraggable) return child;

  void fireDragDetect() {
    if (hasDragSource) {
      DragStartVeto.begin(state.swt, state.id);
    } else {
      DragStartVeto.reset();
    }
    onDragStarted?.call();
    // DragDetect goes out either way: native SWT raises it for any drag over a control, and an
    // application can listen for it without a DragSource. The Eclipse workbench does exactly that
    // to start moving a view, so withholding it left every view stack immovable.
    widget.sendDragDetectDragDetect(state, dragDetectEvent?.call() ?? VEvent());
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
  bool alwaysAccepts = false,
  bool alwaysResolves = false,
}) {
  // Always return _DropTargetNegotiator, even while dropTargetId is still null (it can
  // resolve later, e.g. a Composite whose SWT DropTarget attaches after first serialize).
  // Switching widget type at this tree position would force Flutter to tear down and
  // rebuild the whole subtree the instant a long-lived ancestor first gets a real id.
  return _DropTargetNegotiator<T>(
    dropTargetId: state.dropTargetId,
    resolveIndex: resolveIndex,
    resolveItemId: resolveItemId,
    resolvePosition: resolvePosition,
    onDrop: onDrop,
    builder: builder,
    alwaysAccepts: alwaysAccepts,
    alwaysResolves: alwaysResolves,
    child: child,
  );
}

class _DropTargetNegotiator<T extends Object> extends StatefulWidget {
  final int? dropTargetId;
  final int Function(DragTargetDetails<T> details)? resolveIndex;
  final int Function(DragTargetDetails<T> details)? resolveItemId;
  final Offset Function(DragTargetDetails<T> details)? resolvePosition;
  final void Function(T data, int? index, int? itemId, Offset position) onDrop;
  final Widget Function(BuildContext context, Widget child, DndNegotiationState negotiation, bool isHovering)? builder;

  /// Accept the drag and call [onDrop] even with no SWT DropTarget attached, for a widget that
  /// handles some drops itself (a CTabFolder reordering its own tabs).
  final bool alwaysAccepts;

  /// Resolve the drop position on every move, not only when an SWT DropTarget is attached — for a
  /// widget that draws its own feedback from it. The resolvers are free to have side effects, so
  /// this stays opt-in: a widget that only feeds Java must not run them for a drag it can never
  /// receive, or it paints drop feedback for gestures that are not headed its way.
  final bool alwaysResolves;
  final Widget child;

  const _DropTargetNegotiator({
    super.key,
    required this.dropTargetId,
    required this.resolveIndex,
    required this.resolveItemId,
    required this.resolvePosition,
    required this.onDrop,
    required this.builder,
    required this.alwaysAccepts,
    required this.alwaysResolves,
    required this.child,
  });

  @override
  State<_DropTargetNegotiator<T>> createState() => _DropTargetNegotiatorState<T>();
}

class _DropTargetNegotiatorState<T extends Object> extends State<_DropTargetNegotiator<T>> {
  @override
  void initState() {
    super.initState();
    _subscribe(widget.dropTargetId);
  }

  @override
  void didUpdateWidget(covariant _DropTargetNegotiator<T> oldWidget) {
    super.didUpdateWidget(oldWidget);
    if (oldWidget.dropTargetId != widget.dropTargetId) {
      _unsubscribe(oldWidget.dropTargetId);
      _subscribe(widget.dropTargetId);
    }
  }

  @override
  void dispose() {
    _unsubscribe(widget.dropTargetId);
    super.dispose();
  }

  void _subscribe(int? id) {
    if (id == null) return;
    DndSession.listen(id, _onNegotiationUpdate);
  }

  void _unsubscribe(int? id) {
    if (id == null) return;
    DndSession.unlisten(id, _onNegotiationUpdate);
  }

  void _onNegotiationUpdate(DndNegotiationState state) {
    if (mounted) setState(() {});
  }

  Offset _resolvePosition(DragTargetDetails<T> details) =>
      widget.resolvePosition != null ? widget.resolvePosition!(details) : details.offset;

  void _sendNegotiation(String ev, DragTargetDetails<T>? details) {
    final id = widget.dropTargetId;
    // Nothing to tell Java, and the resolvers have side effects, so run them only for a widget
    // that asked to draw its own feedback without a DropTarget of its own.
    if (id == null && !widget.alwaysResolves) return;
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
    if (id == null) return;
    final dropTargetValue = VDropTarget()..id = id;
    DropTargetSwt<VDropTarget>(value: dropTargetValue)
        .sendEvent(dropTargetValue, "Drop/$ev", event);
  }

  @override
  Widget build(BuildContext context) {
    return DragTarget<T>(
      onWillAcceptWithDetails: (details) {
        if (widget.dropTargetId == null) return widget.alwaysAccepts;
        _sendNegotiation("dragEnter", details);
        return true;
      },
      onMove: (details) => _sendNegotiation("dragOver", details),
      onLeave: (_) {
        if (widget.dropTargetId == null) return;
        _sendNegotiation("dragLeave", null);
      },
      onAcceptWithDetails: (details) {
        if (widget.dropTargetId == null && !widget.alwaysAccepts) return;
        if (DragStartVeto.isVetoed) return;
        final index = widget.resolveIndex != null ? widget.resolveIndex!(details) : null;
        final itemId = widget.resolveItemId != null ? widget.resolveItemId!(details) : null;
        widget.onDrop(details.data, index, itemId, _resolvePosition(details));
      },
      builder: (context, candidateData, rejectedData) {
        final id = widget.dropTargetId;
        final negotiation = id != null ? DndSession.stateFor(id) : DndNegotiationState.none;
        return widget.builder != null
            ? widget.builder!(context, widget.child, negotiation, candidateData.isNotEmpty)
            : widget.child;
      },
    );
  }
}
