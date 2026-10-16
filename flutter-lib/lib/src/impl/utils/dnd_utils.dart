import 'package:flutter/foundation.dart' show TargetPlatform, defaultTargetPlatform, kIsWeb;
import 'package:flutter/material.dart';
import '../../gen/control.dart';
import '../../gen/droptarget.dart';
import '../../gen/event.dart';
import '../../gen/widget.dart';
import 'dnd_session.dart';
import 'swt_zoom_scale.dart';

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

/// The operation the drop target under an SWT drag last answered, which is what the drag cursor
/// shows: native SWT draws copy/move/link from the target's `event.detail`, and "not allowed"
/// over a target that answered DROP_NONE or over anything that is not a drop target.
class DndDropAnswer {
  DndDropAnswer._();

  static final ValueNotifier<int> operation = ValueNotifier<int>(Dnd.dropNone);
  static Object? _owner;

  /// Flutter enters every DragTarget on the hit path, innermost first; the innermost one keeps
  /// the answer, as only the deepest drop target receives the drag natively.
  static void enter(Object target, int op) {
    if (_owner != null) return;
    _owner = target;
    operation.value = op;
  }

  static void update(Object target, int op) {
    if (identical(_owner, target)) operation.value = op;
  }

  static void leave(Object target) {
    if (!identical(_owner, target)) return;
    _owner = null;
    operation.value = Dnd.dropNone;
  }

  @visibleForTesting
  static void reset() {
    _owner = null;
    operation.value = Dnd.dropNone;
  }

  /// The Windows embedder only maps the standard IDC_* cursors, so copy and link would show as the
  /// plain arrow and move as the window-move cross. There the cursor stays the arrow and a badge
  /// next to it shows the operation, as the OLE drag cursor does.
  static bool get drawsOperationBadge => !kIsWeb && defaultTargetPlatform == TargetPlatform.windows;

  static bool _isDrop(int op) => op & (Dnd.dropCopy | Dnd.dropMove | Dnd.dropLink | Dnd.dropDefault) != 0;

  static MouseCursor cursorFor(int op) {
    if (drawsOperationBadge) return _isDrop(op) ? SystemMouseCursors.basic : SystemMouseCursors.noDrop;
    if (op & Dnd.dropCopy != 0) return SystemMouseCursors.copy;
    if (op & Dnd.dropLink != 0) return SystemMouseCursors.alias;
    if (op & (Dnd.dropMove | Dnd.dropDefault) != 0) return SystemMouseCursors.move;
    return SystemMouseCursors.noDrop;
  }
}

/// The DND.FEEDBACK_* a drop target draws over one of its items.
class DropFeedbackBox extends StatelessWidget {
  const DropFeedbackBox({super.key, required this.decoration, required this.child});

  final BoxDecoration decoration;
  final Widget child;

  @override
  Widget build(BuildContext context) => DecoratedBox(
        position: DecorationPosition.foreground,
        decoration: decoration,
        child: child,
      );
}

/// Draws the feedback a drop target's listener asked for over one of its items, as SWT's
/// Tree/TableDropTargetEffect does: an insertion line before or after it, or it highlighted.
Widget decorateDropFeedback({
  required int feedback,
  required Color color,
  required double lineWidth,
  required int highlightAlpha,
  required Widget child,
}) {
  final BoxDecoration decoration;
  if (feedback & Dnd.feedbackInsertBefore != 0) {
    decoration = BoxDecoration(border: Border(top: BorderSide(color: color, width: lineWidth)));
  } else if (feedback & Dnd.feedbackInsertAfter != 0) {
    decoration = BoxDecoration(border: Border(bottom: BorderSide(color: color, width: lineWidth)));
  } else if (feedback & Dnd.feedbackSelect != 0) {
    decoration = BoxDecoration(color: color.withAlpha(highlightAlpha), border: Border.all(color: color));
  } else {
    return child;
  }
  return DropFeedbackBox(decoration: decoration, child: child);
}

/// The operation glyph drawn next to the arrow while dragging on Windows: a plus for copy, an
/// arrow for link, an empty box for move.
class DropOperationBadge extends StatelessWidget {
  const DropOperationBadge({super.key, required this.operation});

  static const double size = 12.0;

  final int operation;

  @override
  Widget build(BuildContext context) {
    final colors = Theme.of(context).colorScheme;
    final IconData? glyph = operation & Dnd.dropCopy != 0
        ? Icons.add
        : operation & Dnd.dropLink != 0
            ? Icons.north_east
            : null;
    return Container(
      width: size,
      height: size,
      decoration: BoxDecoration(
        color: colors.surface,
        border: Border.all(color: colors.onSurface),
      ),
      child: glyph == null ? null : Icon(glyph, size: size - 2, color: colors.onSurface),
    );
  }
}

/// Where the pointer sits inside the drag feedback: the cursor region is centred on it, and the
/// dragged image keeps its top-left corner at the pointer.
const double _cursorSlop = 8.0;

/// The drag image with a region on top that carries the drag cursor. The region lets hit testing
/// through, so the drop targets underneath are still found.
Widget _withDropCursor(Widget visual) {
  return ConstrainedBox(
    constraints: const BoxConstraints(minWidth: 2 * _cursorSlop, minHeight: 2 * _cursorSlop),
    child: Stack(
      clipBehavior: Clip.none,
      children: [
        Padding(
          padding: const EdgeInsets.only(left: _cursorSlop, top: _cursorSlop),
          child: IgnorePointer(child: visual),
        ),
        Positioned(
          left: 0,
          top: 0,
          width: 2 * _cursorSlop,
          height: 2 * _cursorSlop,
          child: ValueListenableBuilder<int>(
            valueListenable: DndDropAnswer.operation,
            builder: (_, op, __) => MouseRegion(
              opaque: false,
              cursor: DndDropAnswer.cursorFor(op),
              child: const SizedBox.expand(),
            ),
          ),
        ),
        if (DndDropAnswer.drawsOperationBadge)
          // Below and right of the arrow's tip, clear of the arrow itself.
          Positioned(
            left: _cursorSlop + 10,
            top: _cursorSlop + 16,
            child: IgnorePointer(
              child: ValueListenableBuilder<int>(
                valueListenable: DndDropAnswer.operation,
                builder: (_, op, __) => DndDropAnswer._isDrop(op)
                    ? DropOperationBadge(operation: op)
                    : const SizedBox.shrink(),
              ),
            ),
          ),
      ],
    ),
  );
}

class DragDetectGate {
  static const double thresholdPx = 4.0;

  Offset _moved = Offset.zero;
  bool _sent = false;

  void reset() {
    _moved = Offset.zero;
    _sent = false;
  }

  bool passed(Offset delta) {
    if (_sent) return false;
    _moved += delta;
    if (_moved.distance < thresholdPx) return false;
    _sent = true;
    return true;
  }
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
  /// The DragDetect to raise for this drag, or null to raise none.
  VEvent? Function()? dragDetectEvent,
  DragDetectGate? dragDetectGate,
}) {
  // alwaysDraggable: a CTabFolder tab is draggable for the folder's own tab reordering even when
  // the application installed no DragSource.
  final hasDragSource = state.dragSource == true;
  if (!hasDragSource && !alwaysDraggable) return child;

  void sendDragDetect() {
    final event = dragDetectEvent == null ? VEvent() : dragDetectEvent();
    if (event != null) widget.sendDragDetectDragDetect(state, event);
  }

  void onUpdate(DragUpdateDetails d) {
    if (dragDetectGate!.passed(d.delta)) sendDragDetect();
  }

  void fireDragDetect() {
    if (hasDragSource) {
      DragStartVeto.begin(state.swt, state.id);
    } else {
      DragStartVeto.reset();
    }
    dragDetectGate?.reset();
    // DragDetect goes out either way: native SWT raises it for any drag over a control, and an
    // application can listen for it without a DragSource. The Eclipse workbench does exactly that
    // to start moving a view, so withholding it left every view stack immovable.
    //
    // Before whatever this control does about the drag, because the far side handles the two in the
    // order they arrive: a CTabFolder selects the dragged tab, and selecting one a workbench has
    // not rendered makes it render the view, which takes seconds of a busy UI thread.
    if (dragDetectGate == null) sendDragDetect();
    onDragStarted?.call();
  }

  final visual = feedbackBuilder != null
      ? feedbackBuilder(child)
      : Opacity(
          opacity: 0.85,
          child: Material(type: MaterialType.transparency, child: child),
        );
  // A drag with no SWT DragSource behind it (a CTabFolder reordering its own tabs, a workbench
  // moving a view) is not an SWT DnD operation and has no drop answer to show.
  // Drawn shifted back by the slop rather than anchored away from the pointer: a drag target reads
  // the feedback's offset as where the drop happens, so its origin has to stay at the pointer.
  final feedback = hasDragSource
      ? Transform.translate(
          offset: const Offset(-_cursorSlop, -_cursorSlop),
          child: _withDropCursor(visual),
        )
      : visual;
  final childWhenDragging = childWhenDraggingBuilder != null
      ? childWhenDraggingBuilder(child)
      : Opacity(opacity: 0.3, child: child);

  return useLongPress
      ? LongPressDraggable<T>(
          data: data,
          dragAnchorStrategy: pointerDragAnchorStrategy,
          ignoringFeedbackPointer: !hasDragSource,
          feedback: feedback,
          childWhenDragging: childWhenDragging,
          onDragStarted: fireDragDetect,
          onDragUpdate: dragDetectGate == null ? null : onUpdate,
          child: child,
        )
      : Draggable<T>(
          data: data,
          dragAnchorStrategy: pointerDragAnchorStrategy,
          ignoringFeedbackPointer: !hasDragSource,
          feedback: feedback,
          childWhenDragging: childWhenDragging,
          onDragStarted: fireDragDetect,
          onDragUpdate: dragDetectGate == null ? null : onUpdate,
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
    DndDropAnswer.update(this, state.detail);
    if (mounted) setState(() {});
  }

  @override
  void deactivate() {
    DndDropAnswer.leave(this);
    super.deactivate();
  }

  Offset _resolvePosition(DragTargetDetails<T> details) => widget.resolvePosition != null
      ? widget.resolvePosition!(details)
      : logicalToSwt(details.offset);

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
        final id = widget.dropTargetId;
        if (id == null) {
          // Accepted for the widget's own use, but not an SWT drop target: "not allowed".
          if (widget.alwaysAccepts) DndDropAnswer.enter(this, Dnd.dropNone);
          return widget.alwaysAccepts;
        }
        // A previous drag's answer would otherwise show until Java answers this one.
        DndSession.clear(id);
        DndDropAnswer.enter(this, Dnd.dropDefault);
        _sendNegotiation("dragEnter", details);
        return true;
      },
      onMove: (details) => _sendNegotiation("dragOver", details),
      onLeave: (_) {
        DndDropAnswer.leave(this);
        if (widget.dropTargetId == null) return;
        _sendNegotiation("dragLeave", null);
      },
      onAcceptWithDetails: (details) {
        DndDropAnswer.leave(this);
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
