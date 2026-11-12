import 'dart:async';
import 'dart:math' as math;

import 'package:flutter/foundation.dart'
    show defaultTargetPlatform, TargetPlatform;
import 'package:flutter/gestures.dart';
import 'package:flutter/material.dart';
import 'package:flutter/rendering.dart' show RenderOpacity, RenderProxyBox;
import 'package:flutter/semantics.dart' show SemanticsBinding;
import 'package:flutter/services.dart';
import 'package:flutter/widgets.dart';
import '../comm/v_registry.dart';
import '../gen/control.dart';
import '../gen/cursor.dart';
import '../gen/rectangle.dart';
import '../gen/droptarget.dart';
import '../gen/event.dart';
import '../gen/menu.dart';
import '../gen/swt.dart';
import '../gen/widget.dart';
import '../gen/widgets.dart' show WireKey;
import '../nolayout.dart' show BoundsSlot;
import '../styles.dart';
import '../impl/focus_requests.dart';
import '../impl/image_mouse_cursor.dart';
import '../impl/key_forwarding.dart';
import '../impl/key_mapping.dart';
import '../impl/menu_evolve.dart';
import 'utils/frame_interval.dart';
import 'utils/pending_move.dart';
import 'utils/perf_marks.dart';
import 'utils/round_trip_timing.dart';
import 'utils/text_utils.dart';
import 'utils/hosted_context_menu.dart';
import '../custom/rich_tooltip.dart';
import '../theme/theme_extensions/display_theme_extension.dart';
import '../theme/theme_extensions/tooltip_theme_extension.dart';
import 'utils/dnd_session.dart';
import 'utils/dnd_utils.dart';
import 'utils/hover_arbiter.dart';
import 'utils/region_clip.dart';
import 'utils/kept_rebuild.dart';
import 'utils/wheel_input.dart';
import 'utils/widget_utils.dart';
import 'widget_config.dart';

abstract class ControlImpl<T extends ControlSwt, V extends VControl>
    extends WidgetSwtState<T, V> implements HeldMove {

  final GlobalKey<State<MenuSwt>> _menuKey = GlobalKey<State<MenuSwt>>();
  int _lastButton = 1;
  // SWT click count: consecutive same-button downs within the double-click
  // window chain (1, 2, 3…), like a native Display reports them. Java-side
  // consumers key real behavior off it — StyledText's word/line selection
  // takes the double-click branch whenever count > 1, and JFace's
  // PaintManager expects the caret set by a count==1 down — so the count
  // must be truthful, never assumed.
  int _clickCount = 0;
  Duration? _lastClickStamp;
  Offset? _lastClickPosition;
  /// On [nowMicros]; far enough back that the first move is never held.
  int _lastMouseMoveMicros = -(1 << 40);

  Offset? _lastPointerCrossing;

  /// Where in the view the held pointer was last reported, by its press or a move, in whole
  /// pixels. In view coordinates because a control moved along with the pointer reports the same
  /// local position for a pointer that did move.
  Offset? _lastHeld;
  int? _dragDetectPointer;
  Offset? _dragDetectOrigin;
  bool _dragDetectSent = false;
  // Shared by every ControlImpl: the pointer whose MenuDetect walk up the parent chain has
  // already been stopped by a control owning a menu (see [handleMenuDetect]).
  static int? _menuDetectHandledPointer;
  /// The shortest time between two moves sent to Java: just under the display's frame interval, so
  /// a display drawing more often than 60 Hz draws Java's answer to a move in every frame, and no
  /// more than 15 ms, nor less than 4.
  static int get _moveWindowMicros => math.max(4000, math.min(15000, FrameInterval.micros * 9 ~/ 10));
  // Win32's SM_CXDRAG/SM_CYDRAG default, which is what SWT's own drag detection uses.
  static const double _dragThresholdPx = 4.0;
  /// How long the pointer rests before a control reports MouseHover and shows its tooltip:
  /// Display.getToolTipTime(), configurable ([hoverDelay]).
  static int get hoverDelayMs => hoverDelay.inMilliseconds;
  Timer? _hoverTimer;

  // The control's own tooltip is placed by hand, in an overlay, at the pointer. See
  // [_showTooltipAtPointer].
  OverlayEntry? _tooltipEntry;
  Offset? _lastHoverPosition;

  VRectangle? _lastRealBounds;

  void _resolveSentinelBounds(V value) {
    final b = value.bounds;
    if (b == null) return;
    if (b.width < 0 || b.height < 0) {
      if (_lastRealBounds != null) {
        value.bounds = _lastRealBounds;
      } else {
        b.width = 0;
        b.height = 0;
      }
    } else {
      _lastRealBounds = b;
    }
  }

  /// The mounted controls, which build again when semantics are turned on or off: what a control
  /// builds and tags depends on whether they are on.
  static final Set<ControlImpl> _mounted = {};
  static bool _watchingSemantics = false;

  static void _semanticsToggled() {
    for (final control in _mounted) {
      if (control.mounted) control.setState(() {});
    }
  }

  @override
  void initState() {
    super.initState();
    _mounted.add(this);
    if (!_watchingSemantics) {
      SemanticsBinding.instance.addSemanticsEnabledListener(_semanticsToggled);
      _watchingSemantics = true;
    }
    _resolveSentinelBounds(state);
    HoverExclusivityArbiter.instance.register(this, (hovering) {
      final global = HoverExclusivityArbiter.instance.pointerPosition;
      final box = context.findRenderObject();
      final at = global != null && box is RenderBox && box.attached
          ? box.globalToLocal(global)
          : _lastPointerCrossing;
      final event = at == null
          ? null
          : (VEvent()
            ..x = at.dx.round()
            ..y = at.dy.round());
      if (hovering) {
        widget.sendMouseTrackMouseEnter(state, event);
        // A pointer that enters and comes to rest sends no hover event after the one that brought
        // it in, so the stillness that makes a MouseHover due has to start counting here.
        if (event != null) _resetHoverTimer(state, event);
      } else {
        _hoverTimer?.cancel();
        widget.sendMouseTrackMouseExit(state, event);
      }
    }, onLeftWhileHeld: () => _hoverTimer?.cancel());
    _watchFocusRequests();
    _applyFocusRequest();
    FocusRequests.instance.register(state.id, context);
    ActiveDragTracker.ensureInitialized();
  }

  /// The node that takes keyboard focus when Java focuses this control ([FocusRequests]). Null for
  /// controls that hold no keyboard focus of their own; those simply ignore the request.
  ///
  /// It must be readable from [initState] -- the request is usually already waiting by the time the
  /// control is built -- so it has to be a field initializer, not something [initState] assigns.
  FocusNode? get swtFocusNode => null;

  int? _focusRequestId;

  void _watchFocusRequests() {
    final id = state.id;
    if (id == _focusRequestId) return;
    _unwatchFocusRequests();
    FocusRequests.instance.watch(id, _applyFocusRequest);
    _focusRequestId = id;
  }

  void _unwatchFocusRequests() {
    final id = _focusRequestId;
    if (id != null) FocusRequests.instance.unwatch(id, _applyFocusRequest);
    _focusRequestId = null;
  }

  void _applyFocusRequest() {
    final node = swtFocusNode;
    if (node == null) return;
    if (!FocusRequests.instance.claim(state.id)) return;
    _focusClaimed = true;
    // After the next frame: Java often creates or moves the control in the same pass that focuses it,
    // and focusing a text field opens the platform input connection at its current layout position.
    afterNextFrame(() {
      if (!mounted || !_focusClaimed) return;
      _focusClaimed = false;
      // A hidden control's node is detached: it would keep the request and take the focus whenever
      // the control is shown again, with no request from Java behind it.
      if (state.visible == false) return;
      swtFocusNode?.requestFocus();
    });
  }

  /// A focus request this element claimed and has not applied yet.
  bool _focusClaimed = false;

  /// Properties this control's build does not read, so a change naming only these needs no
  /// rebuild. The tooltip is read when it opens.
  Set<int> get unrenderedProperties => const {WireKey.k_Control_toolTipText};

  /// Whether this control draws anything of the widgets it holds. A control that mirrors its parts
  /// in its own state (a CCombo's text field and list) needs no rebuild when only one of them changed.
  bool get drawsHeldWidgets => true;

  @override
  void setValue(V value) {
    _resolveSentinelBounds(value);
    if (!drawsHeldWidgets && (lastChange?.ofHeld ?? false)) return;
    if (_onlyPlaced()) return;
    if (lastChange?.isOnly(unrenderedProperties) ?? false) {
      // A tooltip on screen shows the new text, as the platform's does.
      if (_tooltipEntry != null) _showTooltipAtPointer();
      return;
    }
    _settleBlockNode();
    super.setValue(value);
    // Bounds arrive here, not through didUpdateWidget: a parent's copy of a child carries identity
    // only, so a changed value reaches the widget on its own channel.
    if (_gcOverlaySubscribed || awaitsGC) _requestInitialPaint();
    // Hiding drops the MouseRegion without an exit, so nothing else would end the hover.
    if (state.visible == false) {
      _hoverTimer?.cancel();
      _removeTooltip();
      // Hiding a control supersedes a focus request it has not applied yet.
      _focusClaimed = false;
    }
  }

  @override
  void didUpdateWidget(covariant T oldWidget) {
    // The parent's copy is identity only now; the bounds to resolve are the ones this widget
    // actually holds.
    _resolveSentinelBounds(state);
    super.didUpdateWidget(oldWidget);
    _watchFocusRequests();
  }

  // Java schedules every later repaint; it cannot schedule the first, having no mount event. Every
  // Control carries a GC overlay, not just a Canvas, so every Control has to make that first
  // request -- an application that draws a plain Composite from a Paint listener otherwise stays
  // blank until some unrelated damage happens to make Java paint it.
  bool _sentInitialPaintRequest = false;

  // Java drops a Paint request while the control still has 0x0 bounds, so retry once they arrive.
  bool _requestedWithValidBounds = false;

  // Subclasses push what the Java-side SWT.Paint listeners must already have when they run (e.g.
  // StyledText's text geometry); the same ordered channel carries both, so it lands first.
  void beforePaintRequest() {}

  // An overlay mounted from the start registers the GC op channels in its own initState, a build
  // after this one, and the request waits until it says it is listening.
  bool _gcOverlaySubscribed = false;

  /// A control whose GC waits for its first message asks for its paint without it: Java answers
  /// only a control that has something to paint, and what it sends mounts the GC.
  @override
  void onGCAwaited() {
    WidgetsBinding.instance.addPostFrameCallback((_) {
      if (mounted && awaitsGC) _requestInitialPaint();
    });
  }

  void onGCOverlaySubscribed() {
    // A second subscription is a new, empty overlay: the previous one was dropped along with a
    // hidden subtree, and Java does not know its drawing went with it. One mounted by what an
    // earlier request brought is painted once more, so what it shows does not rest on what was held
    // for it before it listened.
    if (_gcOverlaySubscribed || _sentInitialPaintRequest) {
      _gcOverlaySubscribed = true;
      requestFullRepaint();
      return;
    }
    _gcOverlaySubscribed = true;
    _requestInitialPaint();
  }

  /// Asks Java for a full-area Paint. The GC drawer calls this when scoped repaints have stacked
  /// deep enough to be worth collapsing back into a single display list, or when the scene was
  /// drawn under another Canvas theme.
  void requestFullRepaint() {
    if (!mounted) return;
    beforePaintRequest();
    widget.sendPaintPaint(state, null);
  }

  void _requestInitialPaint() {
    final boundsValid = hasBounds(state.bounds);
    if (_sentInitialPaintRequest && (!boundsValid || _requestedWithValidBounds)) return;
    _sentInitialPaintRequest = true;
    if (boundsValid) _requestedWithValidBounds = true;
    beforePaintRequest();
    widget.sendPaintPaint(state, null);
  }

  /// SWT's `stateMask` for a pointer event: the buttons held during the move, plus the live
  /// modifier keys.
  ///
  /// draw2d and GEF both decide drag-vs-hover from this field alone —
  /// `SWTEventDispatcher.dispatchMouseMoved` reaches `handleMouseDragged`, and
  /// `DomainEventDispatcher.dispatchMouseMoved` reaches `EditDomain.mouseDrag`, only when a button
  /// bit is set. A move sent without it is delivered as a hover, so no diagram tool ever sees a
  /// drag and no drag feedback is ever drawn.
  ///
  /// A move presses and releases nothing, so it carries the buttons held unadjusted: neither of
  /// [swtStateMask]'s own-button corrections applies to it.
  int moveStateMask(int buttons) {
    final keys = HardwareKeyboard.instance;
    var mask = _swtButtonsMask(buttons);
    if (keys.isAltPressed) mask |= SWT.ALT;
    if (keys.isShiftPressed) mask |= SWT.SHIFT;
    if (keys.isControlPressed) mask |= SWT.CTRL;
    if (keys.isMetaPressed) mask |= SWT.COMMAND;
    return mask;
  }

  void takeFocusOnPress() {}

  /// The pointer crossed this control's bounds at [depth], [entering] or leaving it.
  void crossPointer(PointerEvent event, int depth, bool entering) {
    _lastPointerCrossing = event.localPosition;
    HoverExclusivityArbiter.instance.setActive(this, depth, entering, event.position);
  }

  void sendThrottledMouseMove(V state, VEvent event, int buttons) {
    HoverExclusivityArbiter.instance.flushPending();
    event.stateMask = moveStateMask(buttons);
    final wait = _lastMouseMoveMicros + _moveWindowMicros - nowMicros();
    if (wait > 0) {
      // The pointer may stop here: the move is sent when the window ends unless another replaces it.
      _heldState = state;
      _heldEvent = event;
      PendingMove.hold(this, wait);
      return;
    }
    _sendMouseMove(state, event);
  }

  /// The move [PendingMove] holds for this control.
  V? _heldState;
  VEvent? _heldEvent;

  @override
  void sendHeldMove() {
    final state = _heldState, event = _heldEvent;
    _heldState = null;
    _heldEvent = null;
    if (state != null && event != null) _sendMouseMove(state, event);
  }

  /// A move's position, reused from move to move: it is written to the wire as it is sent, and a
  /// held one is only ever replaced by this control's next.
  VEvent moveEvent(int x, int y) => (_moveEvent ??= VEvent())
    ..x = x
    ..y = y;
  VEvent? _moveEvent;

  static final Stopwatch _clock = Stopwatch()..start();

  /// What the throttle measures moves by: monotonic, and read without making a date. A test sets it
  /// to its own clock, so the window does not depend on how fast the machine runs the test.
  @visibleForTesting
  static int Function() nowMicros = () => _clock.elapsedMicroseconds;

  void _sendMouseMove(V state, VEvent event) {
    _lastMouseMoveMicros = nowMicros();
    widget.sendMouseMoveMouseMove(state, event);
    // After the send, not around it: this times the round trip the move starts.
    RoundTripTiming.trace();
  }

  void _resetHoverTimer(V state, VEvent event) {
    _hoverTimer?.cancel();
    _hoverTimer = Timer(hoverDelay, () {
      if (!mounted) return;
      widget.sendMouseTrackMouseHover(state, event);
      _showTooltipAtPointer();
    });
  }

  /// Whether this control's tooltip is drawn as a card instead of the plain panel, when the custom
  /// tooltip is on. Off for a control that already says what it is through a label or an item; a
  /// control that paints its own content overrides it, since its tooltip is the only thing that can
  /// name what the pointer is over.
  bool get usesCustomTooltipCard => false;

  /// Opens the control's tooltip at the pointer, the way the platform places one: its top-left
  /// corner just below and right of the cursor. Flutter's Tooltip centres the panel on its child
  /// instead, which straddles the pointer and, on a control as tall as a Tree, lands nowhere near
  /// the row under the mouse -- so this does not use it.
  void _showTooltipAtPointer() {
    final pointer = _lastHoverPosition;
    final message = state.toolTipText;
    if (pointer == null || message == null || message.isEmpty) return;
    final overlay = Overlay.maybeOf(context, rootOverlay: true);
    if (overlay == null) return;
    _removeTooltip();
    // The hover arrives in the view's coordinates and the panel is laid out inside the overlay,
    // which the app's zoom Transform sits above -- the two only coincide at zoom 1.
    final overlayBox = overlay.context.findRenderObject() as RenderBox?;
    final anchor = overlayBox?.globalToLocal(pointer) ?? pointer;
    final entry = OverlayEntry(
      builder: (overlayContext) => _tooltipOverlay(overlayContext, message, anchor),
    );
    _tooltipEntry = entry;
    overlay.insert(entry);
  }

  /// Any movement takes the tooltip down, as a native one goes down; the hover timer reopens it
  /// wherever the pointer came to rest.
  void _removeTooltip() {
    _tooltipEntry?.remove();
    _tooltipEntry = null;
  }

  /// Whether this control held the keyboard focus when it left the tree.
  bool _focusedWhenDeactivated = false;

  @override
  void deactivate() {
    _focusedWhenDeactivated = swtFocusNode?.hasFocus ?? false;
    super.deactivate();
  }

  @override
  void activate() {
    super.activate();
    _focusedWhenDeactivated = false;
  }

  /// A control moved to another place in the tree -- a Table's child becoming its editor's control --
  /// is built anew there. The focus it held goes to that new element, ahead of the fallback its
  /// unmounted node leaves, which would hand it to the previously focused control. Not to one that
  /// shows nothing that takes the focus, as while it is hidden: the request would wait and take the
  /// focus whenever the control is shown again.
  ///
  /// With no element showing it yet, as when a part restored or moved builds the control again later,
  /// the element that shows it next takes the keys: Java, which still has it focused, hears nothing
  /// of this.
  void _handFocusToSuccessor() {
    for (final other in _mounted) {
      if (!identical(other, this) && other.mounted && other.state.id == state.id) {
        final node = other.swtFocusNode;
        if (node != null && node.parent != null) node.requestFocus();
        return;
      }
    }
    FocusRequests.instance.request(state.id);
  }

  @override
  void dispose() {
    if (_focusedWhenDeactivated) _handFocusToSuccessor();
    _hoverTimer?.cancel();
    PendingMove.drop(this);
    _heldState = null;
    _heldEvent = null;
    _removeTooltip();
    _unwatchFocusRequests();
    if (_focusClaimed) {
      _focusClaimed = false;
      FocusRequests.instance.giveBack(state.id);
    }
    DragStartVeto.forget(state.id);
    if (identical(_rawMoveTarget, this)) _rawMoveTarget = null;
    FocusRequests.instance.unregister(state.id, context);
    HoverExclusivityArbiter.instance.unregister(this);
    _mounted.remove(this);
    super.dispose();
    _blockNode?.dispose();
  }

  /// Whether this control's DragSource drag is in flight. Natively the drag owns the pointer until
  /// the release: the control sees no MouseMove, and a table that selects rows from moves with the
  /// button held (NatTable) would otherwise change what is being dragged.
  bool _dndDragging = false;

  /// Java's DragStart answer for this press's drag, null until it arrives, however late. Natively a
  /// refused drag never starts, so the moves held until the answer go to the control then, and the
  /// rest follow. Kept until the release, so an answer that comes before the drag starts still holds.
  bool? _dndDragAllowed;
  final List<VEvent> _movesBeforeDragStart = [];

  void _startDndDrag() {
    _dndDragging = state.dragSource == true && state.dragDetect != false;
    _movesBeforeDragStart.clear();
  }

  void _dragStartAnswered(bool doit) {
    _dndDragAllowed = doit;
    final held = List.of(_movesBeforeDragStart);
    _movesBeforeDragStart.clear();
    if (doit || !mounted) return;
    for (final move in held) {
      _sendMouseMove(state, move);
    }
  }

  /// Every position the held pointer reaches is sent, unthrottled: natively each one is a
  /// MouseMove, and a drag-select fires a Selection for each, even when a busy frame delivers
  /// several back to back.
  void sendDragMove(V state, VEvent event, PointerMoveEvent e) {
    _rawMoveTarget = this;
    _rawMovePointer = e.pointer;
    _sendDragMove(state, event, e.position, e.buttons);
  }

  /// The control the held pointer's moves go to, and that pointer: where a move the browser reports
  /// ahead of its frame is sent ([rawMove]).
  static ControlImpl? _rawMoveTarget;
  static int? _rawMovePointer;

  /// A move of the held pointer as the browser reports it, ahead of the frame that delivers it (at
  /// [x], [y] in the view): sent to Java now, so that Java's answer can be drawn in that frame. The
  /// move the frame then delivers is at the same place, and is not reported again.
  static void rawMove(double x, double y, int buttons) {
    final target = _rawMoveTarget;
    if (target == null || buttons == 0 || !target.mounted) return;
    final box = target._ownBox();
    if (box == null || !box.attached || !box.hasSize) return;
    final global = Offset(x, y);
    // Relative to where the control is now, unlike a held pointer's localPosition (see [heldLocal]).
    final local = box.globalToLocal(global);
    final event = VEvent()
      ..x = local.dx.round()
      ..y = local.dy.round();
    target._sendDragMove(target.state, event, global, buttons);
  }

  void _sendDragMove(V state, VEvent event, Offset position, int buttons) {
    if (_dndDragging && _dndDragAllowed == true) return;
    // A move that does not move is not reported: Win32 SWT drops it, and some engines deliver one
    // with the press.
    final held = Offset(position.dx.roundToDouble(), position.dy.roundToDouble());
    if (_lastHeld == held) return;
    _lastHeld = held;
    event.stateMask = moveStateMask(buttons);
    if (_dndDragging && _dndDragAllowed == null) {
      _movesBeforeDragStart.add(event);
      return;
    }
    _sendMouseMove(state, event);
  }

  /// Delivers SWT.MenuDetect for a context-menu request at [localPosition].
  ///
  /// SWT sends MenuDetect to the control under the pointer whether or not that control has
  /// a Menu attached, and applications rely on it to build the menu lazily: a diagram
  /// editor paints its whole scene into a single Canvas and only populates its menu once
  /// MenuDetect has run, so a Canvas that never sends it has a dead right-click.
  ///
  /// A control that owns a menu ends the walk up the parent chain, because natively the
  /// parent is only reached when nothing below handled the request. Flutter delivers a
  /// pointer to every control under it, innermost first, so the walk itself comes for free
  /// and this only has to stop it.
  void handleMenuDetect(PointerDownEvent press) {
    if (_menuDetectHandledPointer == press.pointer) return;
    if (state.menu != null) _menuDetectHandledPointer = press.pointer;
    // The press a control's own parts report (a Table's cells and header, which carry the
    // coordinates of the part that was hit) gets its MenuDetect from them too.
    if (!forwardsMouseDownOf(press)) return;
    widget.sendMenuDetectMenuDetect(
      state,
      VEvent()
        ..x = press.localPosition.dx.round()
        ..y = press.localPosition.dy.round(),
    );
  }

  /// Whether a press of [button] is the platform's context-menu trigger: the secondary
  /// button anywhere, plus Ctrl+Click on macOS — the same pair [applyMenu] opens on.
  static bool isMenuDetectTrigger(int button) =>
      button == 3 ||
      (defaultTargetPlatform == TargetPlatform.macOS &&
          HardwareKeyboard.instance.isControlPressed);

  void openContextMenu(Offset globalPosition) {
    final menuState = _menuKey.currentState;
    final menuContext = _menuKey.currentContext;
    if (menuState is MenuImpl && menuContext != null) {
      final box = menuContext.findRenderObject() as RenderBox?;
      final localPos = box != null ? box.globalToLocal(globalPosition) : globalPosition;
      menuState.openContextMenuAt(context, localPos);
    }
  }

  @override
  Widget build(BuildContext context) {
    return const Text("Control");
  }

  Color? getSwtBackgroundColor(BuildContext context) {
    // Same guard as clabel_evolve.dart's stayTransparentForImage: an ancestor's own
    // backgroundImage is already painted by that ancestor, so paint nothing here and let
    // it show through -- otherwise state.background (an explicit color set somewhere up
    // the chain, resolved regardless of any image) would flatly paint over it.
    if (state.backgroundImage == null &&
        ParentBackgroundScope.backgroundImageOf(context) != null) {
      return null;
    }
    var swtBackground = state.background;
    if (swtBackground != null) {
      return Color.fromARGB(
        swtBackground.alpha,
        swtBackground.red,
        swtBackground.green,
        swtBackground.blue,
      );
    }

    // If it has no color of its own, use the parent color
    int? parentColor = getCurrentParentBackgroundColor();
    if (parentColor != null) {
      return Color(0xFF000000 | parentColor);
    }

    return null;
  }

  Color? getSwtForegroundColor(BuildContext context) {
    var swtForeground = state.foreground;
    if (swtForeground != null) {
      return Color.fromARGB(
        swtForeground.alpha,
        swtForeground.red,
        swtForeground.green,
        swtForeground.blue,
      );
    }

    return null;
  }

  /// Pointers pressed on this control's own scroll bars, which are not forwarded as mouse events:
  /// a native scroll bar is outside the client area.
  final Set<int> scrollBarPointers = {};

  /// Whether this press continues the click chain [registerPointerDown] counts: same button, inside
  /// the double-click window and distance. Answers before the press is registered, for a handler
  /// nested inside the control's own, which is reached first.
  bool continuesClickChain(PointerDownEvent e) =>
      _lastClickStamp != null &&
      _lastClickPosition != null &&
      swtButtonOf(e.buttons) == _lastButton &&
      e.timeStamp - _lastClickStamp! <= doubleClickTimeout &&
      // A finger keeps Flutter's double-tap slop: two taps rarely land [doubleClickSlop] apart.
      (e.position - _lastClickPosition!).distance <=
          (e.kind == PointerDeviceKind.touch ? kDoubleTapSlop : doubleClickSlop);

  /// Records the SWT button/count pair a pointer-down carries, chaining consecutive same-button
  /// downs inside the double-click window. Both interaction chromes funnel through here — [wrap]
  /// below and wrapCompositeInteractionChrome() in composite_evolve.dart, which serves a Composite
  /// or Canvas that has children — so a control reports the same pair whichever one owns its
  /// pointer.
  void registerPointerDown(PointerDownEvent e) {
    final button = swtButtonOf(e.buttons);
    _clickCount = continuesClickChain(e) ? _clickCount + 1 : 1;
    _lastClickStamp = e.timeStamp;
    _lastClickPosition = e.position;
    _lastButton = button;
    _dragDetectPointer = e.pointer;
    _dragDetectOrigin = e.localPosition;
    _lastHeld = Offset(e.position.dx.roundToDouble(), e.position.dy.roundToDouble());
    _dragDetectSent = false;
    final box = _ownBox();
    if (box != null) _pressOrigins[e.pointer] = box.localToGlobal(Offset.zero);
  }

  /// Where this control's top-left was in the view when each held pointer went down.
  final Map<int, Offset> _pressOrigins = {};

  RenderBox? _ownBox() {
    if (!mounted) return null;
    final box = context.findRenderObject();
    return box is RenderBox && box.hasSize && box.attached ? box : null;
  }

  /// [e]'s position relative to where this control is now, as SWT reports MouseMove and MouseUp.
  ///
  /// Flutter delivers every event of a held pointer through the transforms it hit-tested at the
  /// press, so [PointerEvent.localPosition] stays relative to where the control was then. A
  /// listener that moves the control under the pointer (a pane resized from its own edge) would
  /// otherwise read the whole distance it moved it a second time, on every move.
  Offset heldLocal(PointerEvent e) {
    final origin = _pressOrigins[e.pointer];
    final box = origin == null ? null : _ownBox();
    if (box == null) return e.localPosition;
    return e.localPosition + box.globalToLocal(origin!);
  }

  /// Forgets the press of [e]'s pointer once it is released or cancelled.
  void releasePointer(PointerEvent e) {
    _pressOrigins.remove(e.pointer);
    if (identical(_rawMoveTarget, this) && e.pointer == _rawMovePointer) _rawMoveTarget = null;
  }

  /// Whether a drag over this control is announced from its own pointer handling. False for a
  /// control that runs its own Draggable and raises DragDetect from there, or the gesture would be
  /// announced twice.
  bool get raisesDragDetectFromPointer => true;

  /// Raises SWT.DragDetect once the pointer has moved past the platform drag threshold.
  ///
  /// SWT.DragDetect is a Control-level event, not a DnD one: a Control with no DragSource still
  /// receives it, and an application starts its own drag from it -- typically by opening a Tracker,
  /// which is how a hand-drawn toolbar is dragged out of its dock. A Control that is itself a
  /// DragSource is served by the DnD Draggable instead, which raises it with the payload that path
  /// needs.
  void maybeSendDragDetect(PointerMoveEvent e) {
    if (_dragDetectSent) return;
    if (e.pointer != _dragDetectPointer) return;
    if (!raisesDragDetectFromPointer) return;
    if (state.dragDetect == false) return;
    if (state.dragSource == true) return;
    final origin = _dragDetectOrigin;
    if (origin == null) return;
    if ((e.localPosition - origin).distance < _dragThresholdPx) return;
    _dragDetectSent = true;
    widget.sendDragDetectDragDetect(
      state,
      VEvent()
        ..x = origin.dx.round()
        ..y = origin.dy.round()
        ..button = swtButton
        ..stateMask = swtStateMask(released: false, flutterButtons: e.buttons),
    );
  }

  final DragDetectGate _dragDetectGate = DragDetectGate();

  VEvent dragDetectPayload() {
    final at = _dragDetectOrigin ?? _lastPointerCrossing;
    return VEvent()
      ..x = (at?.dx ?? 0).round()
      ..y = (at?.dy ?? 0).round()
      ..button = swtButton
      // The pressed button is in `button` and left out of stateMask, as GTK does.
      ..stateMask = swtStateMask(released: false);
  }

  void cancelHover() => _hoverTimer?.cancel();

  /// The pointer rests on this control's own area at [position]: its hover counts from there.
  void restartHover(V state, VEvent event, Offset position) {
    _lastHoverPosition = position;
    _resetHoverTimer(state, event);
  }

  void forgetDragDetect(int pointer) {
    _dndDragging = false;
    _dndDragAllowed = null;
    _movesBeforeDragStart.clear();
    if (pointer != _dragDetectPointer) return;
    _dragDetectPointer = null;
    _dragDetectOrigin = null;
    _dragDetectSent = false;
  }

  /// SWT's button number for a Flutter button bitfield: 1 left, 2 middle, 3 right, 4/5 the
  /// side buttons — the numbering every native Control.sendMouseEvent produces.
  static int swtButtonOf(int flutterButtons) => switch (flutterButtons) {
        kSecondaryMouseButton => 3,
        kMiddleMouseButton => 2,
        kBackMouseButton => 4,
        kForwardMouseButton => 5,
        _ => 1,
      };

  static int _swtButtonBit(int swtButton) => switch (swtButton) {
        1 => SWT.BUTTON1,
        2 => SWT.BUTTON2,
        3 => SWT.BUTTON3,
        4 => SWT.BUTTON4,
        5 => SWT.BUTTON5,
        _ => 0,
      };

  static int _swtButtonsMask(int flutterButtons) {
    var mask = 0;
    if (flutterButtons & kPrimaryMouseButton != 0) mask |= SWT.BUTTON1;
    if (flutterButtons & kMiddleMouseButton != 0) mask |= SWT.BUTTON2;
    if (flutterButtons & kSecondaryMouseButton != 0) mask |= SWT.BUTTON3;
    if (flutterButtons & kBackMouseButton != 0) mask |= SWT.BUTTON4;
    if (flutterButtons & kForwardMouseButton != 0) mask |= SWT.BUTTON5;
    return mask;
  }

  /// The stateMask a native Control puts on a mouse event: the modifiers held plus the buttons
  /// down, and the platform asymmetry around the event's own button — cleared on the press
  /// (the state *before* it), set on the release. Win32, Cocoa and GTK all do exactly this in
  /// `Widget.setInputState`, so an application testing `stateMask & SWT.BUTTON1` on a mouseUp
  /// gets the same answer here as it does natively.
  int swtStateMask({required bool released, int flutterButtons = 0}) {
    final keys = HardwareKeyboard.instance;
    var mask = _swtButtonsMask(flutterButtons);
    if (keys.isAltPressed) mask |= SWT.ALT;
    if (keys.isShiftPressed) mask |= SWT.SHIFT;
    if (keys.isControlPressed) mask |= SWT.CTRL;
    if (keys.isMetaPressed) mask |= SWT.COMMAND;
    final own = _swtButtonBit(_lastButton);
    return released ? (mask | own) : (mask & ~own);
  }

  /// The SWT button number of the pointer-down still in flight. Held across the release so MouseUp
  /// names the same button as MouseDown did, the way a native Display reports it — a
  /// PointerUpEvent's own `buttons` is already 0 by then.
  int get swtButton => _lastButton;

  /// The click count of the pointer-down still in flight, never 0 — an application gating on
  /// `count == 1` must see a plain click as one.
  int get swtClickCount => _clickCount == 0 ? 1 : _clickCount;

  /// Whether the press in flight continues a click chain: the second press of a double click, or a
  /// later one. Natively it reports no Selection of its own on a Scale or Slider.
  bool get isRepeatPress => swtClickCount > 1;

  /// The second press of a chain delivers MouseDoubleClick right after its MouseDown, with the
  /// same position and button, as every native does; a third press is a plain MouseDown again.
  void sendDoubleClickOnSecondPress(VEvent press) {
    if (swtClickCount != 2) return;
    widget.sendMouseMouseDoubleClick(
      state,
      VEvent()
        ..button = press.button
        ..x = press.x
        ..y = press.y
        ..count = 2
        ..stateMask = press.stateMask,
    );
  }

  MouseCursor swtCursorToFlutter(int style) {
    switch (style) {
      case 0: return SystemMouseCursors.basic;           // CURSOR_ARROW
      case 1: return SystemMouseCursors.wait;            // CURSOR_WAIT
      case 2: return SystemMouseCursors.precise;         // CURSOR_CROSS
      case 5: return SystemMouseCursors.move;            // CURSOR_SIZEALL
      case 6: return SystemMouseCursors.resizeUpLeftDownRight;  // CURSOR_SIZENESW
      case 7: return SystemMouseCursors.resizeUpDown;    // CURSOR_SIZENS
      case 8: return SystemMouseCursors.resizeUpRightDownLeft;  // CURSOR_SIZENWSE
      case 9: return SystemMouseCursors.resizeLeftRight; // CURSOR_SIZEWE
      case 19: return SystemMouseCursors.text;           // CURSOR_IBEAM
      case 20: return SystemMouseCursors.forbidden;      // CURSOR_NO
      case 21: return SystemMouseCursors.click;          // CURSOR_HAND
      default: return MouseCursor.defer;
    }
  }

  /// The cursor [cursor] shows, or null when it leaves it to the controls around this one.
  MouseCursor? mouseCursorOf(VCursor? cursor) {
    if (cursor == null) return null;
    final style = cursor.cursorStyle;
    if (style != null && style >= 0) return swtCursorToFlutter(style);
    if (cursor.image != null) return imageMouseCursorOf(cursor) ?? SystemMouseCursors.basic;
    return null;
  }

  /// Whether this control forwards MouseDown from the interaction chrome
  /// (the Listener added in wrap() and wrapCompositeInteractionChrome).
  /// TableImpl overrides this to false because each cell has its own
  /// GestureDetector that already sends MouseDown with cell-center coordinates;
  /// the chrome Listener would fire a second event with different coordinates.
  bool get forwardsControlMouseDown => true;

  /// Whether [wrap] sends the MouseDown of this press: [forwardsControlMouseDown], unless the
  /// control tells its own parts' presses apart from the rest of its area.
  bool forwardsMouseDownOf(PointerDownEvent event) => forwardsControlMouseDown;

  /// Whether [wrap] forwards KeyDown events to Java. Widgets that run their own keyboard
  /// pipeline and send Key events themselves (e.g. StyledText) override this to false —
  /// otherwise every keystroke reaches SWT KeyDown listeners twice.
  bool get forwardsKeysFromWrap => true;

  /// Whether [wrap] reports the mouse wheel. A control that reports it from its own handler (Canvas,
  /// a StyledText with scroll bars) overrides this to false.
  bool get forwardsWheelFromWrap => true;

  /// Whether a wheel over this control ends with it: a List, Table or Tree keeps it (Win32 and
  /// Cocoa), and a Scale or Combo (Win32); any other control passes it on, and its parents report it
  /// too.
  bool get keepsWheel => false;

  /// The wheel event a control below has kept, by its untransformed original: every listener on the
  /// pointer's path receives its own transformed copy of the same event.
  static PointerEvent? _keptWheel;

  /// Whether [e] is within this control's own bounds.
  bool isOverSelf(PointerEvent e) {
    final size = context.size;
    return size != null && (Offset.zero & size).contains(e.localPosition);
  }

  /// Whether a control below the pointer already kept [event].
  static bool wheelKeptBelow(PointerEvent event) =>
      identical(event.original ?? event, _keptWheel) ||
      (swipeBeingReported != null && identical(swipeBeingReported, _keptWheel));

  /// Marks [event] as kept by a control on the pointer's path, so the controls above it do not
  /// report it again.
  static void markWheelKept(PointerEvent event) => _keptWheel = swipeBeingReported ?? event.original ?? event;

  /// The pointer event the deepest control on its path has taken, by its untransformed original.
  static PointerEvent? _takenPointer;

  /// Whether a control below the pointer already took [event]; when none has, this one takes it.
  /// SWT reports a press, a move, a release or a hover to the one control under the pointer, and
  /// every control on the path receives its own copy of the event, the deepest first.
  static bool pointerTakenBelow(PointerEvent event) {
    final original = event.original ?? event;
    if (identical(original, _takenPointer)) return true;
    _takenPointer = original;
    return false;
  }

  /// Reports the mouse wheel to Java at [localPosition]: SWT.MouseWheel for its vertical part,
  /// positive up, and SWT.MouseHorizontalWheel for its horizontal part, positive left.
  void reportWheel(PointerSignalEvent event, Offset localPosition) {
    if (event is! PointerScrollEvent || wheelKeptBelow(event)) return;
    for (final wheel in wheelEvents(event, stateMask: moveStateMask(0))) {
      widget.sendMouseWheelMouseWheel(
        state,
        wheel
          ..x = localPosition.dx.round()
          ..y = localPosition.dy.round(),
      );
    }
    if (keepsWheel) markWheelKept(event);
  }

  bool get wrapsWholeWidgetForDnd => true;

  /// Whether [wrap] puts this control in [inOwnLayer]: every Composite does, so that a change to
  /// one of its controls repaints the Composite rather than the shell. A control that builds its own
  /// top instead of going through [wrap] puts that top there itself.
  bool get drawsInOwnLayer => false;

  /// [top], the outermost thing this control builds, in a layer of its own. Painting restarts from
  /// the nearest layer above what changed, so without one here a change repaints the shell and
  /// every control in it. A relayout inside a control (a LayoutBuilder lays itself out again for any
  /// change below it) climbs to the control's top, where its bounds fix its size, and repaints all
  /// it passes: outermost, or the layer is climbed past.
  Widget inOwnLayer(Widget top) => RepaintBoundary(child: top);

  Widget _ownLayerIfDrawn(Widget control, bool ownLayer) =>
      ownLayer && drawsInOwnLayer ? inOwnLayer(control) : control;

  Widget wrapDnd(Widget content) {
    if (state.dragSource != true && state.dropTargetId == null) return content;
    // A DragSource drags from anywhere inside the control. What a self-drawn control renders under
    // the Draggable (a Canvas's empty box; its GC paint sits outside) does not hit-test, so without
    // this the press never reaches the Draggable.
    if (state.dragSource == true) {
      content = MetaData(behavior: HitTestBehavior.opaque, child: content);
    }
    content = wrapDropTarget<DndDragPayload>(
      child: content,
      state: state,
      onDrop: (_, __, ___, position) => sendControlDrop(position),
      // Painted under the control in a layer of its own, so the border coming and going repaints
      // nothing else, and the control is not built again.
      builder: (context, child, isHovering) => Stack(
        fit: StackFit.passthrough,
        children: [
          Positioned.fill(
            child: IgnorePointer(
              child: RepaintBoundary(child: CustomPaint(painter: _DropBorderPainter(isHovering))),
            ),
          ),
          child,
        ],
      ),
    );
    return wrapDraggable<DndDragPayload>(
      child: content,
      data: DndDragPayload(sourceControlId: state.id),
      widget: widget,
      state: state,
      // Native SWT gives a plain control's drag no image, only the cursor.
      feedbackBuilder: (_) => const SizedBox.shrink(),
      dragDetectEvent: dragDetectPayload,
      dragDetectGate: _dragDetectGate,
      onDragStarted: _startDndDrag,
      onDragStartVerdict: _dragStartAnswered,
    );
  }

  void sendControlDrop(Offset position) {
    final dropTargetId = state.dropTargetId;
    if (dropTargetId == null) return;
    final dropTargetValue = VDropTarget()..id = dropTargetId;
    DropTargetSwt<VDropTarget>(value: dropTargetValue).sendDropdrop(
      dropTargetValue,
      VEvent()
        ..x = position.dx.round()
        ..y = position.dy.round(),
    );
  }

  /// This control's own flag combined with its ancestors', which is what SWT's `isEnabled()` means.
  ///
  /// Read from the tree rather than from the payload: it depends on where the widget sits, and the
  /// only place that knows is here. [EnabledScope.of] reads the ancestors' answer — this control's
  /// own scope is published below this context, so this cannot see itself.
  bool get enabledEffective =>
      (state.enabled ?? true) && EnabledScope.of(context);

  /// Stops this control taking input while it is disabled, and publishes the same answer for
  /// everything below it — a control that refuses input owes its subtree the reason, or a
  /// descendant would go on rendering itself as enabled inside something that is not. Keeping the
  /// two in one wrapper is also what keeps every build() path honest: several of them return early
  /// and would otherwise each have to remember to tell the subtree.
  ///
  /// The wrappers stay mounted and only their flags change: adding or removing one would change
  /// the widget type at the root of the subtree, deactivating every descendant element, which is
  /// then rebuilt from the copy of its state nested in the parent's payload — an older snapshot
  /// than what the descendant may already have applied from its own channel.
  ///
  /// With [onKeyEvent], the focus node that excludes the subtree is also the control's own, which
  /// forwards the keys reaching it: one node instead of two nested ones.
  ///
  /// Its focus node is this control's, so that the control's own enablement reaches it as the change
  /// arrives (see [setValue]): a focus node tells the Focus widgets depending on it in a microtask,
  /// and one changed during the build makes them rebuild in a frame of their own, which lays out and
  /// repaints everything around them again.
  Widget blockWhenDisabled(Widget child, {bool? effective, FocusOnKeyEventCallback? onKeyEvent}) {
    effective ??= enabledEffective;
    final node = _blockNode ??= FocusNode();
    _blockForwardsKeys = onKeyEvent != null;
    _configureBlockNode(node, effective);
    node.onKeyEvent = onKeyEvent;
    return EnabledScope(
      enabled: effective,
      child: Focus(
        focusNode: node,
        includeSemantics: onKeyEvent != null,
        child: IgnorePointer(ignoring: !effective, child: child),
      ),
    );
  }

  FocusNode? _blockNode;
  bool _blockForwardsKeys = false;

  /// What [ExcludeFocus] makes of its node, or with keys to forward, what a [Focus] that takes focus
  /// while enabled does.
  void _configureBlockNode(FocusNode node, bool effective) {
    node
      ..canRequestFocus = _blockForwardsKeys && effective
      ..skipTraversal = !_blockForwardsKeys
      ..descendantsAreFocusable = effective
      ..descendantsAreTraversable = effective;
  }

  /// [blockWhenDisabled]'s node set for the enablement that just arrived, before the build. The
  /// ancestors' part is read without depending on it: the build that follows does.
  void _settleBlockNode() {
    final node = _blockNode;
    if (node == null || !mounted) return;
    final ancestors = context.getInheritedWidgetOfExactType<EnabledScope>()?.enabled ?? true;
    final effective = (state.enabled ?? true) && ancestors;
    if (node.descendantsAreFocusable == effective) return;
    _configureBlockNode(node, effective);
    // The Focus widgets below depend on the node through the scope its Focus publishes, which tells
    // them in the middle of the build; marked now, they are already waiting to rebuild by then.
    void mark(Element e) {
      final widget = e.widget;
      if (widget is Focus && !identical(widget.focusNode, node)) {
        e.markNeedsBuild();
        return;
      }
      e.visitChildren(mark);
    }

    final scope = _blockScopeOf(node);
    if (scope != null) {
      scope.visitChildren(mark);
    } else {
      context.visitChildElements(mark);
    }
  }

  /// The scope the Focus of [blockWhenDisabled] publishes for [node], kept once found.
  InheritedElement? _blockScope;

  InheritedElement? _blockScopeOf(FocusNode node) {
    final kept = _blockScope;
    if (kept != null && kept.mounted && identical((kept.widget as InheritedNotifier).notifier, node)) {
      return kept;
    }
    InheritedElement? found;
    void find(Element e) {
      if (found != null) return;
      final widget = e.widget;
      if (e is InheritedElement && widget is InheritedNotifier && identical(widget.notifier, node)) {
        found = e;
        return;
      }
      e.visitChildren(find);
    }

    context.visitChildElements(find);
    return _blockScope = found;
  }

  KeyEventResult _forwardKeyDown(FocusNode node, KeyEvent event) {
    if (event is KeyDownEvent) {
      final vEvent = mapNewKeyEventToSwt(event);
      if (vEvent.keyCode != 0 || vEvent.character != 0) {
        widget.sendKeyKeyDown(state, vEvent);
      }
    }
    return KeyEventResult.ignored;
  }

  /// `setVisible(false)`: the control renders nothing. [wrap] answers it; a build that does not go
  /// through [wrap] returns [SizedBox.shrink] first when this holds.
  bool get hidden => state.visible == false;

  /// The control's own chrome around [widget]. [opacity] is the impl's own fade, folded into the
  /// one layer the control already has. [ownLayer] false for a control whose own build puts this in
  /// [inOwnLayer] further out.
  Widget wrap(Widget widget, {double opacity = 1, bool ownLayer = true}) {
    // Every Control build ends here, so this counts real rebuilds, which a duration mark cannot see.
    if (perfMarksEnabled) perfCount('${state.swt}.build');
    final hoverDepth = ControlNestingScope.depthOf(context);
    if (wrapsWholeWidgetForDnd) {
      widget = wrapDnd(widget);
    }
    if (hidden) {
      _wrapped = null;
      _inner = null;
      return const SizedBox.shrink();
    }
    builtAtBounds();

    // The chain below is the same for most rebuilds, which change only what the control shows: the
    // previous chain is returned as it is and only its innermost slot rebuilds, with [widget].
    _slotChild = widget;
    final mouseCursor = mouseCursorOf(state.cursor);
    final forwardKeys = forwardsKeysFromWrap && !displayLevelKeyForwardingActive;
    final menu = state.menu;
    final semantics = WidgetsBinding.instance.semanticsEnabled;
    final disabled = state.enabled == false;
    final effective = enabledEffective;
    final innerKey = (
      hoverDepth,
      mouseCursor,
      state.style.has(SWT.BORDER),
      menu == null ? null : VRegistry.keyOf(menu),
      gcOverlay,
      paintsGCOverlay,
      gcMounted,
      state.id,
      semantics,
      semantics ? state.semanticsTooltip : null,
      semantics ? state.semanticsEnabled : null,
      forwardKeys,
      opacity,
    );
    final key = (innerKey, disabled, effective, ownLayer);
    // The slot is normally marked with this control (see [setState]); marking it during the build
    // makes the build owner re-sort its dirty list, once per control.
    _slotRebuild.mark();
    final wrapped = _wrapped;
    final reusable = _slotRebuild.built && state.region == null;
    if (wrapped != null && reusable && key == _wrappedKey) return wrapped;
    _wrappedKey = key;
    // Enabled and disabled share one structure, so toggling enablement rebuilds rather than
    // remounts the control, and rebuilds only the outer wrappers: what they wrap is the same
    // instance. A disabled control still paints (Java dispatches SWT.Paint whatever its enablement,
    // so its GC stays mounted); [blockWhenDisabled] keeps the input below from it.
    var inner = _inner;
    if (inner == null || !reusable || innerKey != _innerKey) {
      _innerKey = innerKey;
      inner = _inner = _innerChain(KeptSlot(_slotRebuild, () => _slotChild), hoverDepth, mouseCursor);
    }
    return _wrapped = _ownLayerIfDrawn(
        SwtControlScope(
          swt: state.swt,
          child: ControlNestingScope(
            depth: hoverDepth + 1,
            child: RegionClip.maybe(
                state.region,
                blockWhenDisabled(
                    _FadeOpacity(
                        opacity: (disabled ? AppOpacities.disabled : 1) * opacity,
                        child: semantics ? _SemanticsGeneration(generation: effective, child: inner) : inner),
                    effective: effective,
                    onKeyEvent: forwardKeys ? _forwardKeyDown : null)),
          ),
        ),
        ownLayer);
  }

  Widget _innerChain(Widget widget, int hoverDepth, MouseCursor? mouseCursor) {
    if (mouseCursor != null) {
      widget = MouseRegion(cursor: mouseCursor, child: widget);
    }

    if (state.style.has(SWT.BORDER)) {
      widget = Container(
        decoration: BoxDecoration(
          border: Border.all(width: 0.5, color: Colors.grey),
        ),
        child: widget,
      );
    }

    if (state.menu != null) {
      widget = applyMenu(widget);
    }

    // Wrap with GC overlay if needed
    widget = wrapWithGCOverlay(widget);
    // Tagged outside the GC overlay: the text the GC draws is the node's name only as a descendant.
    widget = tagSemantics(widget);

    // [forwardKeys] is off when a whole-tree Display forwards keys from a single top-level handler
    // (see [displayLevelKeyForwardingActive]) — otherwise the same key would reach Java twice. Still
    // the sole path in the embedded backend, which has no whole-tree Display.
    return _pointerListener(_pointerRegion(hoverDepth, widget));
  }

  /// The pointer events a native control reports on its own: MouseDown and MouseUp, MouseEnter and
  /// MouseExit settled by [HoverExclusivityArbiter], MouseMove while the pointer is over it,
  /// MouseHover once it rests -- none while it is disabled, and none for a child's pointer -- and
  /// the right-click that opens its Menu. [wrap] applies it; a control that builds its own tree
  /// instead has to call this, or it reports none of them.
  Widget trackPointer(Widget child) {
    final hoverDepth = ControlNestingScope.depthOf(context);
    return ControlNestingScope(
      depth: hoverDepth + 1,
      child: blockWhenDisabled(_pointerListener(_pointerRegion(hoverDepth, applyMenu(child)))),
    );
  }

  Widget _pointerListener(Widget child) {
    return Listener(
      onPointerSignal: (e) {
        if (forwardsWheelFromWrap) reportWheel(e, e.localPosition);
      },
      onPointerPanZoomUpdate: (e) {
        if (!forwardsWheelFromWrap || wheelKeptBelow(e)) return;
        panAsScroll(e, (wheel) => reportWheel(wheel, wheel.localPosition));
        if (keepsWheel) markWheelKept(e);
      },
      onPointerDown: (e) {
        if (pointerTakenBelow(e) || scrollBarPointers.contains(e.pointer)) return;
        registerPointerDown(e);
        if (forwardsMouseDownOf(e)) {
          final event = VEvent()
            ..button = swtButton
            ..x = e.localPosition.dx.round()
            ..y = e.localPosition.dy.round()
            ..count = swtClickCount
            ..stateMask = swtStateMask(released: false, flutterButtons: e.buttons);
          widget.sendMouseMouseDown(state, event);
          sendDoubleClickOnSecondPress(event);
        }
        // After MouseDown: Java orders the press's activation and focus ahead of the MouseDown it
        // delivers (ControlHelper.pressed), and a focus reported first would arrive as a separate,
        // earlier change. Only the primary button focuses this way, as natively; an edit field's
        // own text takes a right press's focus (DoubleClickWordSelector).
        if (e.buttons == kPrimaryButton) {
          takeFocusOnPress();
        }
        // After MouseDown, never before: a tool that tracks the secondary press picks the
        // item under the cursor there, and a menu built ahead of it describes the previous
        // selection. Still ahead of the menu opening, which applyMenu does on the release.
        if (isMenuDetectTrigger(swtButton)) {
          handleMenuDetect(e);
        }
      },
      onPointerUp: (e) {
        if (pointerTakenBelow(e)) return;
        forgetDragDetect(e.pointer);
        final pos = heldLocal(e);
        releasePointer(e);
        if (scrollBarPointers.remove(e.pointer)) return;
        final event = VEvent()
          ..button = swtButton
          ..x = pos.dx.round()
          ..y = pos.dy.round()
          ..count = swtClickCount
          ..stateMask = swtStateMask(released: true, flutterButtons: e.buttons);
        widget.sendMouseMouseUp(state, event);
        // A mouse rests where the release left it; the hover starts counting from there. A touch
        // leaves nothing under the pointer.
        if (e.kind == PointerDeviceKind.mouse && isOverSelf(e)) {
          restartHover(state, VEvent()
            ..x = event.x
            ..y = event.y, e.position);
        }
      },
      onPointerCancel: (e) {
        forgetDragDetect(e.pointer);
        releasePointer(e);
        scrollBarPointers.remove(e.pointer);
      },
      onPointerMove: (e) {
        if (pointerTakenBelow(e) || scrollBarPointers.contains(e.pointer)) return;
        // No hover while a button is held: the pointer is moving, not resting.
        _hoverTimer?.cancel();
        maybeSendDragDetect(e);
        final pos = heldLocal(e);
        final event = VEvent()
          ..x = pos.dx.round()
          ..y = pos.dy.round();
        sendDragMove(state, event, e);
      },
      child: child,
    );
  }

  /// Whether this control's build reads where it stands and not only its size. Where a control
  /// stands is otherwise its parent's layout, which follows the move on its own.
  bool get buildsFromPosition => false;

  /// Whether a build made for a [built] size draws this control as a new build would at a
  /// [width] x [height] its parent's layout gives it: true where the build reads the size only as
  /// constraints that layout imposes anyway.
  bool sizeIsLayoutOnly(Size built, int width, int height) => false;

  /// The size the last build was made for, recorded by [builtAtBounds].
  Size? _builtSize;

  /// Records that the build under way draws the control at its current bounds. [wrap] does it; a
  /// control that builds its own tree calls it from its build.
  @protected
  void builtAtBounds() {
    final bounds = state.bounds;
    _builtSize = bounds == null ? null : Size(bounds.width.toDouble(), bounds.height.toDouble());
  }

  /// Whether the change is to the bounds alone, and the last build still draws them: a move, or
  /// a new size that the parent's layout applies by itself.
  bool _onlyPlaced() {
    final built = _builtSize;
    final b = state.bounds;
    if (built == null || b == null || buildsFromPosition) return false;
    if (!(lastChange?.isOnly(const {WireKey.k_Control_bounds}) ?? false)) return false;
    if (built.width == b.width && built.height == b.height) return true;
    return built.width > 0 &&
        built.height > 0 &&
        b.width > 0 &&
        b.height > 0 &&
        BoundsSlot.places(context, state.id) &&
        sizeIsLayoutOnly(built, b.width, b.height);
  }

  /// The chain [wrap] last built, what it was built from, and the content its slot shows.
  Widget? _wrapped;
  Object? _wrappedKey;
  Widget? _inner;
  Object? _innerKey;
  Widget? _slotChild;
  final KeptRebuild _slotRebuild = KeptRebuild();

  @override
  void setState(VoidCallback fn) {
    super.setState(fn);
    // Set again by the build if it goes through wrap.
    _builtSize = null;
    _slotRebuild.mark();
  }

  Widget _pointerRegion(int hoverDepth, Widget child) {
    return MouseRegion(
      onEnter: (e) {
        _lastHoverPosition = e.position;
        crossPointer(e, hoverDepth, true);
      },
      onExit: (e) {
        _hoverTimer?.cancel();
        _removeTooltip();
        crossPointer(e, hoverDepth, false);
      },
      onHover: (e) {
        if (pointerTakenBelow(e)) return;
        final event = moveEvent(e.localPosition.dx.round(), e.localPosition.dy.round());
        _removeTooltip();
        sendThrottledMouseMove(state, event, e.buttons);
        restartHover(state, event, e.position);
      },
      child: child,
    );
  }

  Widget _tooltipOverlay(BuildContext context, String message, Offset pointer) {
    final tooltipTheme = Theme.of(context).extension<TooltipThemeExtension>();
    final displayTheme = Theme.of(context).extension<DisplayThemeExtension>();
    // A control that paints its own content has no ToolItem to read, so the card is built here from
    // the tooltip the application set and placed by the same pointer layout as the plain panel.
    final card =
        usesCustomTooltipCard ? EvolveToolTip.maybeCard(context, toolTipText: message) : null;
    return Positioned.fill(
      child: IgnorePointer(
        child: CustomSingleChildLayout(
          delegate: _PointerTooltipLayout(
            pointer: pointer,
            cursorOffset: Offset(
              tooltipTheme?.pointerOffsetX ?? 0,
              tooltipTheme?.pointerOffsetY ?? 0,
            ),
            screenMargin: tooltipTheme?.screenMargin ?? 0,
          ),
          child: card ??
              Container(
            padding: tooltipTheme?.hoverPadding,
            decoration: tooltipTheme == null
                ? null
                // The info background, as a JFace viewer's popup-Shell tooltip is painted.
                // tooltipTheme.backgroundColor is colorScheme.surface, which Tree and Table paint
                // their own background from: over one of those the panel would be the exact colour
                // of what is behind it, and without the border and shadow nothing would be left to
                // see.
                : BoxDecoration(
                    color: displayTheme?.tooltipShellBackgroundColor ??
                        tooltipTheme.backgroundColor,
                    borderRadius: BorderRadius.circular(tooltipTheme.borderRadius),
                    border: Border.all(
                      color: tooltipTheme.borderColor,
                      width: tooltipTheme.borderWidth,
                    ),
                    boxShadow: [
                      BoxShadow(
                        color: tooltipTheme.shadowColor,
                        blurRadius: tooltipTheme.shadowBlurRadius,
                        offset: Offset(0, tooltipTheme.shadowOffsetY),
                      ),
                    ],
                  ),
            // An OverlayEntry builds outside the app's DefaultTextStyle, and the fallback style
            // paints text underlined in yellow. Set the whole style here rather than merging.
            child: DefaultTextStyle(
              style: (tooltipTheme?.messageTextStyle ?? const TextStyle())
                  .copyWith(decoration: TextDecoration.none),
              child: Text(stripAccelerators(message), maxLines: tooltipTheme?.messageMaxLines),
            ),
              ),
        ),
      ),
    );
  }

  Widget applyMenu(Widget child) {
    final menu = state.menu;
    if (menu == null) {
      return child;
    }

    void openMenuAt(Offset localPosition) {
      final menuState = _menuKey.currentState;
      if (menuState != null && menuState is MenuImpl) {
        menuState.openContextMenuAt(context, localPosition);
      }
    }

    return GestureDetector(
      // A control that paints itself rather than composing hit-testable children -- a Canvas
      // draws its whole scene through the GC overlay, which is an IgnorePointer over an empty
      // box -- offers nothing for the default deferToChild to land on, so the detector never
      // joined the gesture arena and its menu could not open. Translucent keeps the hit passing
      // through to whatever is behind, so a nested control still wins the arena over its parent.
      behavior: HitTestBehavior.translucent,
      // The tap callbacks below are pointer plumbing for the menu. Left in the semantics tree they
      // give the control a tap action, and the web engine then answers every click on it with a
      // position-less semantic tap, so the control never sees its MouseDown/MouseUp.
      excludeFromSemantics: true,
      onSecondaryTapUp: (details) => openMenuAt(details.localPosition),
      onTapUp: (details) {
        // macOS treats Ctrl+Click as a secondary click.
        if (defaultTargetPlatform == TargetPlatform.macOS &&
            HardwareKeyboard.instance.isControlPressed) {
          openMenuAt(details.localPosition);
        }
      },
      child: Stack(
        children: [
          child,
          _contextMenuFor(menu),
        ],
      ),
    );
  }

  /// Reuses the widget for the same menu: rebuilding the anchor re-lays out its overlay.
  Widget _contextMenuFor(VMenu menu) {
    final key = VRegistry.keyOf(menu);
    final held = _contextMenu;
    if (held != null && held.key == key) return held.widget;
    final widget = HostedContextMenu(child: MenuSwt(key: _menuKey, value: menu));
    _contextMenu = (key: key, widget: widget);
    return widget;
  }

  ({int key, Widget widget})? _contextMenu;
}


/// Gives the semantics nodes of [child] new ids whenever [generation] changes, without remounting it.
///
/// Enablement decides whether a control's node is tappable, and with that its role on the web, where
/// the engine builds a new element for a node whose role changes without the identifier the node
/// already had. A new node is created with it.
class _SemanticsGeneration extends SingleChildRenderObjectWidget {
  const _SemanticsGeneration({required this.generation, required super.child});

  final Object generation;

  @override
  _RenderSemanticsGeneration createRenderObject(BuildContext context) => _RenderSemanticsGeneration(generation);

  @override
  void updateRenderObject(BuildContext context, _RenderSemanticsGeneration renderObject) =>
      renderObject.generation = generation;
}

class _RenderSemanticsGeneration extends RenderProxyBox {
  _RenderSemanticsGeneration(this._generation);

  Object _generation;

  set generation(Object value) {
    if (value == _generation) return;
    _generation = value;
    if (!attached) return;
    clearSemantics();
    markNeedsSemanticsUpdate();
  }
}

/// Places a tooltip the way the platform does: top-left corner just below and right of the
/// pointer, pulled back inside the window rather than clipped, and flipped above the pointer when
/// there is no room underneath.
class _PointerTooltipLayout extends SingleChildLayoutDelegate {
  _PointerTooltipLayout({
    required this.pointer,
    required this.cursorOffset,
    required this.screenMargin,
  });

  final Offset pointer;
  final Offset cursorOffset;
  final double screenMargin;

  @override
  BoxConstraints getConstraintsForChild(BoxConstraints constraints) => constraints.loosen();

  @override
  Offset getPositionForChild(Size size, Size childSize) {
    double x = pointer.dx + cursorOffset.dx;
    if (x + childSize.width > size.width - screenMargin) {
      x = size.width - childSize.width - screenMargin;
    }
    double y = pointer.dy + cursorOffset.dy;
    if (y + childSize.height > size.height - screenMargin) {
      y = pointer.dy - childSize.height - screenMargin;
    }
    return Offset(
      x.clamp(screenMargin, math.max(screenMargin, size.width)),
      y.clamp(screenMargin, math.max(screenMargin, size.height)),
    );
  }

  @override
  bool shouldRelayout(_PointerTooltipLayout oldDelegate) =>
      oldDelegate.pointer != pointer ||
      oldDelegate.cursorOffset != cursorOffset ||
      oldDelegate.screenMargin != screenMargin;
}

/// The border a plain control's drop target shows while a drag is over it.
/// The control's [Opacity]: a layer of its own only while it is see-through, so that a fade or a
/// change of enablement updates that layer without repainting, while a control at full opacity
/// paints into its parent's layer and many of them changing at once are not composited one by one.
class _FadeOpacity extends Opacity {
  const _FadeOpacity({required super.opacity, super.child});

  @override
  RenderOpacity createRenderObject(BuildContext context) =>
      _RenderFadeOpacity(opacity: opacity, alwaysIncludeSemantics: alwaysIncludeSemantics);
}

class _RenderFadeOpacity extends RenderOpacity {
  _RenderFadeOpacity({super.opacity, super.alwaysIncludeSemantics});

  @override
  bool get alwaysNeedsCompositing {
    final alpha = Color.getAlphaFromOpacity(opacity);
    return child != null && alpha > 0 && alpha < 255;
  }
}

class _DropBorderPainter extends CustomPainter {
  const _DropBorderPainter(this.hovering);

  final bool hovering;

  static final BoxDecoration _border = BoxDecoration(border: Border.all(color: Colors.blue, width: 2));

  @override
  void paint(Canvas canvas, Size size) {
    if (!hovering) return;
    final painter = _border.createBoxPainter();
    painter.paint(canvas, Offset.zero, ImageConfiguration(size: size));
    painter.dispose();
  }

  @override
  bool shouldRepaint(_DropBorderPainter old) => old.hovering != hovering;
}
