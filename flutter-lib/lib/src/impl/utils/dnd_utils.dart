import 'dart:async';
import 'dart:typed_data';
import 'dart:ui' as ui;

import 'package:flutter/foundation.dart' show TargetPlatform, ValueListenable, defaultTargetPlatform, kIsWeb;
import 'package:flutter/gestures.dart';
import 'package:flutter/material.dart';
import 'package:flutter/rendering.dart';
import 'package:flutter/services.dart';
import '../../comm/comm.dart';
import '../../gen/control.dart';
import '../../gen/dragsource.dart';
import '../../gen/droptarget.dart';
import '../../gen/event.dart';
import '../../gen/widget.dart';
import '../shell_evolve.dart';
import 'dnd_session.dart';
import '../../gen/widgets.dart' show WireChannel;
import 'widget_utils.dart';

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

/// The row a drag is over and the DND.FEEDBACK_* bits the drop target's listener answered for it.
class DropRowFeedback extends ChangeNotifier {
  int? _row;
  int _feedback = Dnd.feedbackNone;

  int? get row => _row;
  int get feedback => _feedback;

  void hover(int? row) {
    if (row == _row) return;
    _row = row;
    notifyListeners();
  }

  void answer(int feedback) {
    if (feedback == _feedback) return;
    _feedback = feedback;
    notifyListeners();
  }
}

/// The drop feedback over the row a drag is on, painted in a layer of its own laid over the rows,
/// so a change of row or of answer repaints this layer and no row.
///
/// Rows are [rowHeight] tall from the top of the layer, less [scroll]'s offset when the rows scroll
/// under it; scrolling repaints the layer only while there is feedback on it.
class DropRowLayer extends StatelessWidget {
  const DropRowLayer({
    super.key,
    required this.rows,
    required this.rowHeight,
    required this.rowCount,
    required this.decoration,
    this.scroll,
  });

  final DropRowFeedback rows;
  final double rowHeight;
  final int rowCount;
  final BoxDecoration? Function(int feedback) decoration;
  final ScrollController? scroll;

  /// Where row [row] is drawn, in this layer's coordinates.
  Rect rowRect(int row, double width) {
    return Rect.fromLTWH(0, row * rowHeight - scroll.scrolledBy, width, rowHeight);
  }

  /// What is drawn over row [row]: nothing unless the drag is over it.
  BoxDecoration? decorationOver(int row) =>
      rows.row == row && row < rowCount ? decoration(rows.feedback) : null;

  @override
  Widget build(BuildContext context) => IgnorePointer(
        child: RepaintBoundary(
          child: CustomPaint(
            painter: _DropRowPainter(this, _WhileOverRows(rows, scroll)),
            child: const SizedBox.expand(),
          ),
        ),
      );
}

class _DropRowPainter extends CustomPainter {
  _DropRowPainter(this.layer, Listenable repaint) : super(repaint: repaint);

  final DropRowLayer layer;

  @override
  void paint(Canvas canvas, Size size) {
    final row = layer.rows.row;
    if (row == null) return;
    final decoration = layer.decorationOver(row);
    if (decoration == null) return;
    final rect = layer.rowRect(row, size.width);
    final painter = decoration.createBoxPainter();
    painter.paint(canvas, rect.topLeft, ImageConfiguration(size: rect.size));
    painter.dispose();
  }

  @override
  bool shouldRepaint(_DropRowPainter old) =>
      old.layer.rowHeight != layer.rowHeight ||
      old.layer.rowCount != layer.rowCount ||
      !identical(old.layer.rows, layer.rows) ||
      old.layer.decoration(layer.rows.feedback) != layer.decoration(layer.rows.feedback);
}

/// The feedback's changes, and the scroll's while there is feedback to scroll.
class _WhileOverRows implements Listenable {
  _WhileOverRows(this._rows, this._scroll);

  final DropRowFeedback _rows;
  final ScrollController? _scroll;
  final Map<VoidCallback, VoidCallback> _onScroll = {};

  @override
  void addListener(VoidCallback listener) {
    _rows.addListener(listener);
    final scroll = _scroll;
    if (scroll == null) return;
    void scrolled() {
      if (_rows.row != null) listener();
    }

    _onScroll[listener] = scrolled;
    scroll.addListener(scrolled);
  }

  @override
  void removeListener(VoidCallback listener) {
    _rows.removeListener(listener);
    final scrolled = _onScroll.remove(listener);
    if (scrolled != null) _scroll?.removeListener(scrolled);
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

/// The feedback a drop target's listener asked for over one of its items, as SWT's
/// Tree/TableDropTargetEffect draws it: an insertion line before or after it, or it highlighted.
/// Null when it asked for none.
BoxDecoration? dropFeedbackDecoration({
  required int feedback,
  required Color color,
  required double lineWidth,
  required int highlightAlpha,
}) {
  if (feedback & Dnd.feedbackInsertBefore != 0) {
    return BoxDecoration(border: Border(top: BorderSide(color: color, width: lineWidth)));
  }
  if (feedback & Dnd.feedbackInsertAfter != 0) {
    return BoxDecoration(border: Border(bottom: BorderSide(color: color, width: lineWidth)));
  }
  if (feedback & Dnd.feedbackSelect != 0) {
    return BoxDecoration(color: color.withAlpha(highlightAlpha), border: Border.all(color: color));
  }
  return null;
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

/// The drag image, with the drag cursor over the whole overlay for as long as the image is up.
Widget _withDropCursor(Widget visual) {
  return _DropCursor(
    child: Stack(
      clipBehavior: Clip.none,
      children: [
        Padding(
          padding: const EdgeInsets.only(left: _cursorSlop, top: _cursorSlop),
          child: _DragImageCapture(child: visual),
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

/// Shows the drop answer's cursor wherever the pointer is, not only over the drag image, which a
/// pointer moving faster than frames leaves behind. The region lets hit testing through, so the drop
/// targets underneath are still found.
class _DropCursor extends StatefulWidget {
  const _DropCursor({required this.child});

  final Widget child;

  @override
  State<_DropCursor> createState() => _DropCursorState();
}

class _DropCursorState extends State<_DropCursor> {
  final _portal = OverlayPortalController()..show();

  @override
  Widget build(BuildContext context) {
    return OverlayPortal(
      controller: _portal,
      overlayChildBuilder: (_) => RepaintBoundary(
        child: ValueListenableBuilder<int>(
          valueListenable: DndDropAnswer.operation,
          builder: (_, op, __) => MouseRegion(
            opaque: false,
            cursor: DndDropAnswer.cursorFor(op),
            child: const SizedBox.expand(),
          ),
        ),
      ),
      child: widget.child,
    );
  }
}

/// Sends the drag image once it is drawn, for another window the drag crosses into to draw it: the
/// image this window draws ends at its border.
class _DragImageCapture extends StatefulWidget {
  const _DragImageCapture({required this.child});

  final Widget child;

  @override
  State<_DragImageCapture> createState() => _DragImageCaptureState();
}

class _DragImageCaptureState extends State<_DragImageCapture> {
  @override
  void initState() {
    super.initState();
    final drag = _RemoteDragReporter.drag;
    WidgetsBinding.instance.addPostFrameCallback((_) => _capture(drag));
  }

  Future<void> _capture(int drag) async {
    if (!mounted || !_RemoteDragReporter.isReporting(drag)) return;
    final boundary = context.findRenderObject();
    // A drag with no image (a Canvas's) has none to send, as natively.
    if (boundary is! RenderRepaintBoundary || !boundary.hasSize || boundary.size.isEmpty) return;
    final size = boundary.size;
    final image = await boundary.toImage(pixelRatio: View.of(context).devicePixelRatio);
    try {
      final png = await image.toByteData(format: ui.ImageByteFormat.png);
      if (png != null) _RemoteDragReporter.image(drag, png.buffer.asUint8List(), size);
    } finally {
      image.dispose();
    }
  }

  @override
  Widget build(BuildContext context) => RepaintBoundary(child: widget.child);
}

/// The drag image a drag from another window carries, drawn here where the source window cannot:
/// at the pointer, as the source draws it, with the operation badge for what this window's drop
/// target answered.
class _RemoteDragImage extends StatelessWidget {
  const _RemoteDragImage({required this.at, required this.png, required this.size});

  final ValueListenable<Offset> at;
  final Uint8List png;
  final Size size;

  @override
  Widget build(BuildContext context) => ValueListenableBuilder<Offset>(
        valueListenable: at,
        builder: (_, at, __) => Positioned(
          left: at.dx,
          top: at.dy,
          child: IgnorePointer(
            child: Stack(
              clipBehavior: Clip.none,
              children: [
                Image.memory(png, width: size.width, height: size.height, gaplessPlayback: true),
                if (DndDropAnswer.drawsOperationBadge)
                  Positioned(
                    left: 10,
                    top: 16,
                    child: ValueListenableBuilder<int>(
                      valueListenable: DndDropAnswer.operation,
                      builder: (_, op, __) => DndDropAnswer._isDrop(op)
                          ? DropOperationBadge(operation: op)
                          : const SizedBox.shrink(),
                    ),
                  ),
              ],
            ),
          ),
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

/// Reports where a DragSource's drag goes, in display coordinates, so Java can carry it to a shell
/// another window draws: no other window sees a drag that leaves this one. One drag at a time per
/// engine, kept here because the Draggable's callbacks are rebuilt with the control.
class _RemoteDragReporter {
  /// At most one move a frame; the last one of a frame is sent when the frame ends.
  static const _frame = Duration(milliseconds: 16);

  static ControlSwt? _widget;
  static VControl? _state;
  static Offset Function(Offset)? _toDisplay;
  static ui.FlutterView? _view;
  static Offset? _last;
  static Offset? _lastReported;
  static Timer? _throttle;

  /// Counts drags, so an image captured for one that has ended is not sent for the next.
  static int drag = 0;

  /// What the drop target in another window under the pointer answered, which Java relays.
  static int _answer = Dnd.dropNone;
  static (int, int)? _answerOn;
  static Object? _answerToken;

  /// Whether the cursor was set here, outside the view, where Flutter's own cursor tracking finds
  /// nothing under the pointer and shows the arrow.
  static bool _cursorSet = false;

  static void start(ControlSwt widget, VControl state, Offset Function(Offset) toDisplay, ui.FlutterView view) {
    stop();
    drag++;
    _widget = widget;
    _state = state;
    _toDisplay = toDisplay;
    _view = view;
    final on = (state.wireType, state.id);
    _answerOn = on;
    _answerToken = EquoCommService.on(on.$1, on.$2, WireChannel.dragDetectRemoteAnswer, (payload) {
      if (_answerOn != on || payload is! Map) return;
      _answer = (payload['detail'] as num?)?.toInt() ?? Dnd.dropNone;
      _showCursor();
    });
  }

  static bool get reporting => _widget != null;

  static bool isReporting(int drag) => _widget != null && drag == _RemoteDragReporter.drag;

  static void image(int drag, Uint8List png, Size size) {
    if (!isReporting(drag)) return;
    _widget!.sendEvent(
        _state!,
        WireChannel.dragDetectDragImage,
        VEvent()
          ..segments = png
          ..width = size.width.round()
          ..height = size.height.round());
  }

  /// The window a drag starts in keeps the pointer until the release, so it sets the cursor over
  /// the other windows too.
  static void _showCursor() {
    final at = _last;
    final view = _view;
    if (at == null || view == null) return;
    final outside = !(Offset.zero & (view.physicalSize / view.devicePixelRatio)).contains(at);
    if (!outside && !_cursorSet) return;
    // Back in the view, Flutter takes the cursor over again, but it does not know it was changed.
    final op = outside ? _answer : DndDropAnswer.operation.value;
    _cursorSet = outside;
    final cursor = DndDropAnswer.cursorFor(op);
    if (cursor is! SystemMouseCursor) return;
    SystemChannels.mouseCursor.invokeMethod<void>(
        'activateSystemCursor', <String, dynamic>{'device': 0, 'kind': cursor.kind});
  }

  static void move(Offset global) {
    if (_widget == null) return;
    _last = global;
    _showCursor();
    if (_throttle != null) return;
    _report(WireChannel.dragDetectDragMove, global);
  }

  static void _endFrame() {
    _throttle = null;
    final last = _last;
    if (_widget != null && last != null && last != _lastReported) _report(WireChannel.dragDetectDragMove, last);
  }

  /// The drag ended with no drop target in this window taking it.
  static void release(Offset global) {
    if (_widget == null) return;
    _report(WireChannel.dragDetectDragRelease, global);
    stop();
  }

  /// Escape cancelled the drag: the window it is over hears it leave, and it ends with nothing dropped.
  static void cancel() {
    final widget = _widget;
    final state = _state;
    if (widget == null || state == null) return;
    stop();
    widget.sendEvent(state, WireChannel.dragDetectDragCancel, VEvent());
  }

  static void stop() {
    _throttle?.cancel();
    _throttle = null;
    _widget = null;
    _state = null;
    _toDisplay = null;
    _view = null;
    _last = null;
    _lastReported = null;
    _answer = Dnd.dropNone;
    _cursorSet = false;
    final on = _answerOn;
    if (on != null) EquoCommService.remove(on.$1, on.$2, WireChannel.dragDetectRemoteAnswer, _answerToken);
    _answerOn = null;
    _answerToken = null;
  }

  static void _report(int channel, Offset global) {
    _throttle?.cancel();
    _throttle = Timer(_frame, _endFrame);
    _lastReported = global;
    final display = _toDisplay!(global);
    _widget!.sendEvent(
        _state!,
        channel,
        VEvent()
          ..x = display.dx.round()
          ..y = display.dy.round());
  }
}

/// Escape cancels the DragSource drag in flight, as it cancels an OLE drag. The window the drag
/// started in keeps the keyboard until the release, and the Escape goes to no control.
class _EscapeCancelsDrag {
  static KeyEventCallback? _handler;

  static void arm(VoidCallback onEscape) {
    disarm();
    bool handler(KeyEvent event) {
      if (event.logicalKey != LogicalKeyboardKey.escape) return false;
      if (event is KeyDownEvent) onEscape();
      return true;
    }
    _handler = handler;
    HardwareKeyboard.instance.addHandler(handler);
  }

  static void disarm() {
    final handler = _handler;
    _handler = null;
    if (handler != null) HardwareKeyboard.instance.removeHandler(handler);
  }
}

/// A Draggable whose drag starts only once the pointer has moved [DragDetectGate.thresholdPx] from
/// the press. Flutter hands the pointer to a lone drag recognizer at the press, which made every
/// click on a control with nothing else competing for it a drag, entering and dropping on the drop
/// target under it.
class _ThresholdDraggable<T extends Object> extends Draggable<T> {
  const _ThresholdDraggable({
    required super.child,
    required super.feedback,
    super.data,
    super.dragAnchorStrategy,
    super.ignoringFeedbackPointer,
    super.childWhenDragging,
    super.onDragStarted,
    super.onDragUpdate,
    super.onDraggableCanceled,
    super.onDragCompleted,
    super.onDragEnd,
  });

  @override
  MultiDragGestureRecognizer createRecognizer(GestureMultiDragStartCallback onStart) =>
      _ThresholdMultiDragRecognizer(allowedButtonsFilter: allowedButtonsFilter)..onStart = onStart;
}

class _ThresholdMultiDragRecognizer extends MultiDragGestureRecognizer {
  _ThresholdMultiDragRecognizer({super.allowedButtonsFilter}) : super(debugOwner: null);

  @override
  MultiDragPointerState createNewPointerState(PointerDownEvent event) =>
      _ThresholdPointerState(event.position, event.kind, gestureSettings);

  @override
  String get debugDescription => 'threshold multidrag';
}

class _ThresholdPointerState extends MultiDragPointerState {
  _ThresholdPointerState(super.initialPosition, super.kind, super.gestureSettings);

  /// Starts the drag; held when the arena accepts the pointer before it has moved far enough.
  GestureMultiDragStartCallback? _starter;

  bool get _moved => (pendingDelta?.distance ?? 0) >= DragDetectGate.thresholdPx;

  @override
  void checkForResolutionAfterMove() {
    if (!_moved) return;
    resolve(GestureDisposition.accepted);
    final starter = _starter;
    _starter = null;
    starter?.call(initialPosition);
  }

  @override
  void accepted(GestureMultiDragStartCallback starter) {
    if (_moved) {
      starter(initialPosition);
    } else {
      _starter = starter;
    }
  }

  @override
  void dispose() {
    _starter = null;
    super.dispose();
  }
}

/// The Draggable (its context) whose drag Java's DragStart allowed, while that drag is in flight.
/// A DragSource's drag shows only then: one that DragStart refuses must look like no drag at all.
final ValueNotifier<Object?> _shownDrag = ValueNotifier<Object?>(null);

Widget wrapDraggable<T extends Object>({
  required Widget child,
  required T data,
  required ControlSwt widget,
  required VControl state,
  VoidCallback? onDragStarted,
  // Java's DragStart answer for the drag this Draggable started, once it arrives.
  ValueChanged<bool>? onDragStartVerdict,
  // What native SWT does on the mouse down that starts the drag, so DragStart listeners see it:
  // a Tree or Table selects the item under the pointer before DragDetect is raised.
  VoidCallback? beforeDragDetect,
  Widget Function(Widget child)? feedbackBuilder,
  bool useLongPress = false,
  bool alwaysDraggable = false,
  VEvent Function()? dragDetectEvent,
  DragDetectGate? dragDetectGate,
}) {
  // alwaysDraggable: a CTabFolder tab is draggable for the folder's own tab reordering even when
  // the application installed no DragSource.
  final hasDragSource = state.dragSource == true;
  if (!hasDragSource && !alwaysDraggable) return child;

  void sendDragDetect() {
    widget.sendDragDetectDragDetect(state, dragDetectEvent?.call() ?? VEvent());
  }

  // The context under which the drag starts, to find the shell it starts in.
  BuildContext? anchor;

  void onUpdate(DragUpdateDetails d) {
    if (dragDetectGate?.passed(d.delta) ?? false) sendDragDetect();
    if (hasDragSource) _RemoteDragReporter.move(d.globalPosition);
  }

  void startReporting() {
    final context = anchor;
    if (!hasDragSource || context == null || !context.mounted) return;
    final shell = context.findAncestorStateOfType<ShellImpl>();
    if (shell != null) _RemoteDragReporter.start(widget, state, shell.toDisplay, View.of(context));
  }

  void fireDragDetect() {
    if (hasDragSource) {
      final source = anchor;
      var shownHere = false;
      void show() {
        if (shownHere) return;
        shownHere = true;
        _shownDrag.value = source;
        startReporting();
        _EscapeCancelsDrag.arm(() {
          DragStartVeto.cancel();
          if (identical(_shownDrag.value, source)) _shownDrag.value = null;
          _RemoteDragReporter.cancel();
        });
      }

      DragStartVeto.begin(state.wireType, state.id, onVerdict: (doit) {
        if (doit) {
          show();
        } else if (shownHere) {
          // Refused after it was shown for want of an answer: it never started for Java.
          _EscapeCancelsDrag.disarm();
          if (identical(_shownDrag.value, source)) _shownDrag.value = null;
          _RemoteDragReporter.stop();
        }
        onDragStartVerdict?.call(doit);
      }, onNoVerdict: show);
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
    beforeDragDetect?.call();
    if (dragDetectGate == null) sendDragDetect();
    onDragStarted?.call();
  }

  // Natively the DragSource hears every drag end, a cancelled one or one dropped on nothing
  // included. Told of each, Java tells a drop on a DropTarget, which has ended the drag already,
  // from any other end.
  void sendDragFinished(DraggableDetails _) {
    final id = state.dragSourceId;
    if (!hasDragSource || id == null) return;
    final source = VDragSource()..id = id;
    DragSourceSwt<VDragSource>(value: source).sendDragdragFinished(source, null);
  }

  // The image under the pointer is a copy of the control and only a picture of it: nothing to
  // announce, and it must not stand in the semantics tree as a second node with the control's id.
  final visual = ExcludeSemantics(
    child: feedbackBuilder != null
        ? feedbackBuilder(child)
        : Opacity(
            opacity: 0.85,
            child: Material(type: MaterialType.transparency, child: child),
          ),
  );
  // A drag with no SWT DragSource behind it (a CTabFolder reordering its own tabs, a workbench
  // moving a view) is not an SWT DnD operation and has no drop answer to show.
  // Drawn shifted back by the slop rather than anchored away from the pointer: a drag target reads
  // the feedback's offset as where the drop happens, so its origin has to stay at the pointer. A
  // layer of its own: the image moves with every frame of the drag, and moving a layer does not
  // paint what is in it again.
  final dragFeedback = RepaintBoundary(
    child: hasDragSource
        ? Transform.translate(
            offset: const Offset(-_cursorSlop, -_cursorSlop),
            child: _withDropCursor(visual),
          )
        : visual,
  );

  bool shown(Object? drag) => drag != null && identical(drag, anchor);
  // A DragSource's Draggable keeps drawing its child as is until Java allows the drag. The same
  // widget dragged or not, so that what is dragged is dimmed rather than built again: rebuilt under
  // a new parent, the control would lose its state while it still holds the pointer.
  final Widget feedback;
  final Widget? childWhenDragging;
  final Widget draggableChild;
  if (hasDragSource) {
    feedback = ValueListenableBuilder<Object?>(
      valueListenable: _shownDrag,
      builder: (_, drag, __) => shown(drag) ? dragFeedback : const SizedBox.shrink(),
    );
    childWhenDragging = null;
    draggableChild = ValueListenableBuilder<Object?>(
      valueListenable: _shownDrag,
      builder: (_, drag, __) => _DimmedWhileDragged(dimmed: shown(drag), child: child),
    );
  } else {
    feedback = dragFeedback;
    childWhenDragging = _DimmedWhileDragged(dimmed: true, child: child);
    draggableChild = _DimmedWhileDragged(dimmed: false, child: child);
  }

  void endDrag() {
    if (!hasDragSource) return;
    _EscapeCancelsDrag.disarm();
    DragStartVeto.end();
    if (identical(_shownDrag.value, anchor)) _shownDrag.value = null;
  }

  final onUpdateOrNull = dragDetectGate == null && !hasDragSource ? null : onUpdate;
  void onCanceled(Velocity _, Offset offset) {
    if (hasDragSource) _RemoteDragReporter.release(offset);
    endDrag();
  }
  void onCompleted() {
    if (hasDragSource) _RemoteDragReporter.stop();
    endDrag();
  }
  // Runs before onCanceled. A drag the other windows hear about ends with its release, which Java
  // takes to the window under the pointer: a DragEnd sent now would end it before that drop.
  void onDragEnd(DraggableDetails details) {
    if (!details.wasAccepted && _RemoteDragReporter.reporting) return;
    sendDragFinished(details);
  }

  return Builder(builder: (context) {
    anchor = context;
    return useLongPress
        ? LongPressDraggable<T>(
            data: data,
            dragAnchorStrategy: pointerDragAnchorStrategy,
            feedback: feedback,
            childWhenDragging: childWhenDragging,
            onDragStarted: fireDragDetect,
            onDragUpdate: onUpdateOrNull,
            onDraggableCanceled: onCanceled,
            onDragCompleted: onCompleted,
            onDragEnd: onDragEnd,
            child: draggableChild,
          )
        : dragDetectGate != null
        ? _ThresholdDraggable<T>(
            data: data,
            dragAnchorStrategy: pointerDragAnchorStrategy,
            feedback: feedback,
            childWhenDragging: childWhenDragging,
            onDragStarted: fireDragDetect,
            onDragUpdate: onUpdateOrNull,
            onDraggableCanceled: onCanceled,
            onDragCompleted: onCompleted,
            onDragEnd: onDragEnd,
            child: draggableChild,
          )
        : Draggable<T>(
            data: data,
            dragAnchorStrategy: pointerDragAnchorStrategy,
            feedback: feedback,
            childWhenDragging: childWhenDragging,
            onDragStarted: fireDragDetect,
            onDragUpdate: onUpdateOrNull,
            onDraggableCanceled: onCanceled,
            onDragCompleted: onCompleted,
            onDragEnd: onDragEnd,
            child: draggableChild,
          );
  });
}

/// What a drag picked up, faded to [_alpha] while it is being dragged. Not an [Opacity]: one at
/// full opacity still gives the item a layer of its own, and every draggable item has one of these.
class _DimmedWhileDragged extends SingleChildRenderObjectWidget {
  const _DimmedWhileDragged({required this.dimmed, required super.child});

  static const int _alpha = 77;

  final bool dimmed;

  @override
  _RenderDimmedWhileDragged createRenderObject(BuildContext context) => _RenderDimmedWhileDragged(dimmed);

  @override
  void updateRenderObject(BuildContext context, _RenderDimmedWhileDragged renderObject) {
    renderObject.dimmed = dimmed;
  }
}

class _RenderDimmedWhileDragged extends RenderProxyBox {
  _RenderDimmedWhileDragged(this._dimmed);

  bool _dimmed;

  set dimmed(bool value) {
    if (value == _dimmed) return;
    _dimmed = value;
    markNeedsCompositingBitsUpdate();
    markNeedsPaint();
  }

  @override
  bool get alwaysNeedsCompositing => _dimmed && child != null;

  @override
  void paint(PaintingContext context, Offset offset) {
    final child = this.child;
    if (child == null) return;
    if (!_dimmed) {
      layer = null;
      context.paintChild(child, offset);
      return;
    }
    layer = context.pushOpacity(offset, _DimmedWhileDragged._alpha, super.paint,
        oldLayer: layer as OpacityLayer?);
  }
}

Widget wrapDropTarget<T extends Object>({
  required Widget child,
  required VControl state,
  required void Function(T data, int? index, int? itemId, Offset position) onDrop,
  int Function(DragTargetDetails<T> details)? resolveIndex,
  int Function(DragTargetDetails<T> details)? resolveItemId,
  /// The drop point in SWT units; when it is null, or answers null, the pointer's global position
  /// converted to SWT units.
  Offset? Function(DragTargetDetails<T> details)? resolvePosition,
  Widget Function(BuildContext context, Widget child, bool isHovering)? builder,
  void Function(DndNegotiationState answer)? onAnswer,
  VoidCallback? onLeave,
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
    onAnswer: onAnswer,
    onLeave: onLeave,
    alwaysAccepts: alwaysAccepts,
    alwaysResolves: alwaysResolves,
    child: child,
  );
}

class _DropTargetNegotiator<T extends Object> extends StatefulWidget {
  final int? dropTargetId;
  final int Function(DragTargetDetails<T> details)? resolveIndex;
  final int Function(DragTargetDetails<T> details)? resolveItemId;
  final Offset? Function(DragTargetDetails<T> details)? resolvePosition;
  final void Function(T data, int? index, int? itemId, Offset position) onDrop;
  final Widget Function(BuildContext context, Widget child, bool isHovering)? builder;

  /// What the drop target's listener answered for the drag over this widget.
  final void Function(DndNegotiationState answer)? onAnswer;

  /// The drag is no longer over this widget: it left, was dropped or was cancelled.
  final VoidCallback? onLeave;

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
    required this.onAnswer,
    required this.onLeave,
    required this.alwaysAccepts,
    required this.alwaysResolves,
    required this.child,
  });

  @override
  State<_DropTargetNegotiator<T>> createState() => _DropTargetNegotiatorState<T>();
}

/// A drag from another window, which Java carries to the shell under the pointer: it enters the
/// drop targets drawn here as a drag in this window would, the innermost one under the pointer.
class RemoteDragReceiver {
  /// A step's phase, as Java's RemoteDrag numbers it in the event's `detail`.
  static const over = 1;
  static const leave = 2;
  static const drop = 3;

  _DropTargetNegotiatorState? _current;
  OverlayEntry? _image;
  final ValueNotifier<Offset> _imageAt = ValueNotifier(Offset.zero);

  /// The drag image Java sent ahead of the first step into this window, until that step shows it.
  (Uint8List, Size)? _pendingImage;

  /// The drag image, [png] of [size], for the next [over] to draw.
  void image(Uint8List png, Size size) => _pendingImage = (png, size);

  /// Handles [over], [leave] and [drop] at [global], a position in [context]'s view. Answers false
  /// for a drop no drop target here takes.
  bool handle(BuildContext context, int phase, Offset? global) {
    if (phase == over && global != null) {
      final image = _pendingImage;
      _pendingImage = null;
      _showImage(context, global, image?.$1, image?.$2);
    } else {
      _pendingImage = null;
      _hideImage();
    }
    if (phase == leave || global == null) {
      _enter(null, null);
      return phase != drop;
    }
    final target = _targetAt(context, global);
    if (phase == over) {
      if (!identical(target, _current)) _enter(target, global);
      target?.remoteOver(global);
      return true;
    }
    if (phase != drop) return true;
    if (!identical(target, _current)) {
      _enter(target, global);
      target?.remoteOver(global);
    }
    _current = null;
    if (target == null) return false;
    target.remoteDrop(global);
    return true;
  }

  void _showImage(BuildContext context, Offset global, Uint8List? png, Size? size) {
    final overlay = Overlay.maybeOf(context, rootOverlay: true);
    if (overlay == null) return;
    final box = overlay.context.findRenderObject();
    _imageAt.value = box is RenderBox && box.hasSize ? box.globalToLocal(global) : global;
    if (png == null || size == null || size.isEmpty) return;
    _hideImage();
    final entry = OverlayEntry(builder: (_) => _RemoteDragImage(at: _imageAt, png: png, size: size));
    _image = entry;
    overlay.insert(entry);
  }

  void _hideImage() {
    final entry = _image;
    _image = null;
    entry?.remove();
    entry?.dispose();
  }

  void dispose() {
    _hideImage();
    _current = null;
    _imageAt.dispose();
  }

  void _enter(_DropTargetNegotiatorState? target, Offset? global) {
    final previous = _current;
    _current = target;
    if (previous != null && previous.mounted) previous.remoteLeave();
    if (target != null) target.remoteEnter(global!);
  }

  static _DropTargetNegotiatorState? _targetAt(BuildContext context, Offset global) {
    final result = HitTestResult();
    RendererBinding.instance.hitTestInView(result, global, View.of(context).viewId);
    for (final entry in result.path) {
      final target = entry.target;
      if (target is! RenderMetaData) continue;
      final negotiator = target.metaData;
      if (negotiator is _DropTargetNegotiatorState && negotiator.mounted && negotiator.acceptsRemote) {
        return negotiator;
      }
    }
    return null;
  }
}

class _DropTargetNegotiatorState<T extends Object> extends State<_DropTargetNegotiator<T>> {
  /// What a drag from another window carries: its data stays with the source, on the Java side.
  static const _remotePayload = DndDragPayload(sourceControlId: 0);

  /// Whether a drag from another window is over this target.
  bool _remoteHover = false;

  bool get acceptsRemote => widget.dropTargetId != null && _remotePayload is T;

  DragTargetDetails<T> _remoteDetails(Offset global) =>
      DragTargetDetails<T>(data: _remotePayload as T, offset: global);

  void remoteEnter(Offset global) {
    DndSession.clear(widget.dropTargetId!);
    DndDropAnswer.enter(this, Dnd.dropDefault);
    setState(() => _remoteHover = true);
    _sendNegotiation(WireChannel.dropDragEnter, _remoteDetails(global));
  }

  void remoteOver(Offset global) => _dragOver(_remoteDetails(global));

  void remoteLeave() {
    _stopRepeat();
    DndDropAnswer.leave(this);
    setState(() => _remoteHover = false);
    widget.onLeave?.call();
    _sendNegotiation(WireChannel.dropDragLeave, null);
  }

  void remoteDrop(Offset global) {
    _stopRepeat();
    DndDropAnswer.leave(this);
    setState(() => _remoteHover = false);
    widget.onLeave?.call();
    final details = _remoteDetails(global);
    final index = widget.resolveIndex?.call(details);
    final itemId = widget.resolveItemId?.call(details);
    widget.onDrop(details.data, index, itemId, _resolvePosition(details));
  }

  /// Whether Java heard this window's drag enter this target. A DragSource's drag reaches it only
  /// once DragStart allowed it, so DragEnter can come with a later move.
  bool _entered = false;

  void _enterOnce(DragTargetDetails<T> details) {
    if (_entered || widget.dropTargetId == null || !DragStartVeto.reachesTargets) return;
    _entered = true;
    _sendNegotiation(WireChannel.dropDragEnter, details);
  }

  /// Escape cancelled the drag: it leaves the target it is over.
  void _onCancel() {
    if (!_entered) return;
    _entered = false;
    _stopRepeat();
    DndDropAnswer.leave(this);
    widget.onLeave?.call();
    _sendNegotiation(WireChannel.dropDragLeave, null);
    if (mounted) setState(() {});
  }

  @override
  void initState() {
    super.initState();
    _subscribe(widget.dropTargetId);
    DragStartVeto.cancels.addListener(_onCancel);
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
    DragStartVeto.cancels.removeListener(_onCancel);
    _stopRepeat();
    _unsubscribe(widget.dropTargetId);
    super.dispose();
  }

  /// How often DragOver is sent again while the pointer rests, as the OLE drag loop does.
  static const _repeatInterval = Duration(milliseconds: 50);

  /// Where the drag over this target last was, while it is over it.
  DragTargetDetails<T>? _over;
  Timer? _repeat;

  void _dragOver(DragTargetDetails<T> details) {
    _over = details;
    _sendNegotiation(WireChannel.dropDragOver, details);
  }

  /// A drag resting over the target keeps hearing DragOver while the listener asks for
  /// FEEDBACK_SCROLL or FEEDBACK_EXPAND, which act after the drag has rested a while. Each one
  /// resolves the item again, as the rows under the pointer move when the target scrolls.
  void _syncRepeat() {
    final id = widget.dropTargetId;
    final rests = id != null &&
        _over != null &&
        DndSession.stateFor(id).feedback & (Dnd.feedbackScroll | Dnd.feedbackExpand) != 0;
    if (!rests) {
      _repeat?.cancel();
      _repeat = null;
      return;
    }
    _repeat ??= Timer.periodic(_repeatInterval, (_) {
      final over = _over;
      if (!mounted || over == null) return _stopRepeat();
      _sendNegotiation(WireChannel.dropDragOver, over);
    });
  }

  void _stopRepeat() {
    _over = null;
    _repeat?.cancel();
    _repeat = null;
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
    _syncRepeat();
    if (mounted) widget.onAnswer?.call(state);
  }

  @override
  void deactivate() {
    _stopRepeat();
    DndDropAnswer.leave(this);
    super.deactivate();
  }

  /// The drop position local to the control, which Java maps to display coordinates. The global
  /// offset is in the window's logical pixels, which match SWT coordinates only at a UI scale of 1
  /// in a window at the screen's origin.
  Offset _resolvePosition(DragTargetDetails<T> details) {
    final resolved = widget.resolvePosition?.call(details);
    if (resolved != null) return resolved;
    final box = context.findRenderObject();
    return box is RenderBox ? box.globalToLocal(details.offset) : Offset.zero;
  }

  void _sendNegotiation(int channel, DragTargetDetails<T>? details) {
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
        .sendEvent(dropTargetValue, channel, event);
  }

  @override
  Widget build(BuildContext context) {
    // Found by RemoteDragReceiver's hit test.
    return MetaData(
      metaData: this,
      behavior: HitTestBehavior.translucent,
      child: _dragTarget(context),
    );
  }

  Widget _dragTarget(BuildContext context) {
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
        _entered = false;
        _enterOnce(details);
        return true;
      },
      onMove: (details) {
        if (!DragStartVeto.reachesTargets) return;
        _enterOnce(details);
        _dragOver(details);
      },
      onLeave: (_) {
        _stopRepeat();
        DndDropAnswer.leave(this);
        widget.onLeave?.call();
        if (widget.dropTargetId == null || !_entered) return;
        _entered = false;
        _sendNegotiation(WireChannel.dropDragLeave, null);
      },
      onAcceptWithDetails: (details) {
        _stopRepeat();
        DndDropAnswer.leave(this);
        widget.onLeave?.call();
        _entered = false;
        if (widget.dropTargetId == null && !widget.alwaysAccepts) return;
        if (DragStartVeto.isVetoed) return;
        final index = widget.resolveIndex != null ? widget.resolveIndex!(details) : null;
        final itemId = widget.resolveItemId != null ? widget.resolveItemId!(details) : null;
        widget.onDrop(details.data, index, itemId, _resolvePosition(details));
      },
      builder: (context, candidateData, rejectedData) => widget.builder != null
          ? widget.builder!(context, widget.child, candidateData.isNotEmpty || _remoteHover)
          : widget.child,
    );
  }
}
