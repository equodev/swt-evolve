import 'dart:async';
import 'dart:convert';
import 'dart:math' as math;
import 'dart:typed_data';

import 'package:flutter/material.dart';
import 'menu_evolve.dart';
import 'package:flutter/rendering.dart';
import 'package:flutter/services.dart';
import 'package:flutter/widgets.dart';

import '../comm/comm.dart';
import '../comm/tagged_values.dart';
import '../comm/v_registry.dart';
import '../comm/widget_key.dart';
import '../comm/widget_wire.dart';
import '../gen/menu.dart';
import '../gen/widgets.dart' as gen;
import '../gen/colordialog.dart';
import '../gen/dialog.dart';
import '../gen/event.dart';
import '../gen/messagebox.dart';
import '../gen/shell.dart';
import '../gen/swt.dart';
import '../gen/tooltip.dart';
import '../impl/colordialog_evolve.dart';
import '../impl/decorations_evolve.dart';
import '../impl/dialog_evolve.dart';
import '../impl/key_forwarding.dart';
import '../impl/messagebox_evolve.dart';
import '../impl/utils/image_utils.dart';
import '../impl/utils/dnd_utils.dart';
import '../impl/utils/region_clip.dart';
import '../impl/utils/swt_zoom_scale.dart';
import '../impl/utils/tracker_session.dart';
import '../impl/utils/window_origin.dart';
import '../impl/utils/widget_utils.dart';
import '../theme/theme_extensions/display_theme_extension.dart';
import 'utils/pointer.dart';
import 'utils/quiet_animations.dart';

/// `VEvent.detail` of the bounds a shell reports once it is first drawn (Java's
/// `DisplayBridge.INITIAL_CLIENT_BOUNDS`).
const int initialClientBounds = 1;

class FloatingShellChromeScope extends InheritedWidget {
  final BoxConstraints viewportConstraints;

  /// The shell SWT currently considers active (`VDisplay.activeShellId`), or null when none is.
  final int? activeShellId;

  const FloatingShellChromeScope({
    super.key,
    required this.viewportConstraints,
    required this.activeShellId,
    required super.child,
  });

  static FloatingShellChromeScope? maybeOf(BuildContext context) {
    return context.dependOnInheritedWidgetOfExactType<FloatingShellChromeScope>();
  }

  static FloatingShellChromeScope? peekOf(BuildContext context) {
    final el = context.getElementForInheritedWidgetOfExactType<FloatingShellChromeScope>();
    return el?.widget as FloatingShellChromeScope?;
  }

  @override
  bool updateShouldNotify(FloatingShellChromeScope oldWidget) =>
      viewportConstraints != oldWidget.viewportConstraints ||
      activeShellId != oldWidget.activeShellId;
}

bool isModalShell(VShell shell) =>
    (shell.style & (SWT.APPLICATION_MODAL | SWT.SYSTEM_MODAL | SWT.PRIMARY_MODAL)) != 0;

/// [shells], bottom first, as the panes drawn inside a window over the shell that fills it, with the
/// overlay that dims what the topmost modal one blocks. For a [Stack] that fills [viewport].
List<Widget> floatingShells(
    BuildContext context, List<VShell> shells, BoxConstraints viewport, int? activeShellId) {
  final topmostModal = shells.lastIndexWhere(isModalShell);
  return [
    for (var i = 0; i < shells.length; i++) ...[
      if (i == topmostModal)
        Positioned.fill(
          child: ColoredBox(
            color: Theme.of(context).extension<DisplayThemeExtension>()!.modalOverlayColor,
          ),
        ),
      KeyedSubtree(
        key: ValueKey(shells[i].id),
        child: FloatingShellChromeScope(
          viewportConstraints: viewport,
          activeShellId: activeShellId,
          child: gen.mapWidgetFromValue(shells[i]),
        ),
      ),
    ],
  ];
}

/// A shell is placed on whole pixels: its position goes to Java as integers and comes back as the
/// shell's bounds, so a half pixel here would move the shell once Java repeats it.
Offset wholePixels(Offset offset) => Offset(offset.dx.roundToDouble(), offset.dy.roundToDouble());

class ShellImpl<T extends ShellSwt, V extends VShell> extends DecorationsImpl<T, V> {
  Offset? _offset;
  Size? _size;
  bool _maximized = false;
  bool _interacting = false;
  bool _initialBoundsSent = false;
  /// The bounds Java last set that the drawn size was reported against (see build).
  (int, int, int, int)? _drawnSizeReportedFor;

  final ValueNotifier<double> _opacityNotifier = ValueNotifier(1.0);
  final FocusScopeNode _focusScopeNode = FocusScopeNode(debugLabel: 'ShellFocusScope');
  bool _hasFocus = false;
  bool _autoFocusRequested = false;

  /// Whether the Shell was visible as of the last state rendered here. Remembered rather than read
  /// off the arriving value: a value is changed in place, so by the time an update is handled the
  /// object no longer holds what it held before and there is no earlier copy to compare against.
  late bool _wasVisible;

  @override
  void initState() {
    super.initState();
    _wasVisible = state.visible ?? true;
    _opacityNotifier.value = (state.alpha ?? 255) / 255.0;
    _focusScopeNode.addListener(_handleFocusScopeChange);
    // A Tracker is opened on a Shell but is not a Control, so it has no State of its own to receive
    // on -- the Shell it belongs to carries its channel.
    _attachTrackerHost();
    // A shell that reaches the client with its dialogs gets no setValue for them.
    openNewDialogs(context, state);
    _stopPopupRecords = VRegistry.instance.onHolder<VShellPopups>('ShellPopups', VShellPopups.new, (id, record) {
      if (id == state.id) _showPopups(record.popups ?? const [], record.tooltips ?? const []);
    });
    _stopWindowShells = VRegistry.instance.onHolder<VWindowShells>('WindowShells', VWindowShells.new, (id, record) {
      if (id == state.id) _showWindowShells(record);
    });
  }

  void Function()? _stopWindowShells;

  /// The shells drawn inside this one's window, when this client is that window. Empty in every other
  /// client: Java sends them only to the window drawing them (see `DisplayBridge.syncWindowShells`).
  List<VShell> _windowShells = const [];
  int? _activeShellId;

  /// What holds [_windowShells]: the window's record, keyed apart from the shell.
  int get _windowShellsKey => WidgetKey.of(gen.WireType.windowShells, state.id);

  void _showWindowShells(VWindowShells record) {
    final shells = record.shells ?? const <VShell>[];
    for (final shell in shells) {
      VRegistry.instance.register(shell);
    }
    VRegistry.instance.holdsAt(_windowShellsKey, shells);
    if (!mounted) return;
    setState(() {
      _windowShells = shells;
      _activeShellId = record.activeShellId;
    });
  }

  /// A drag from another window over this shell, which Java sends here with its position relative
  /// to the shell's bounds.
  final RemoteDragReceiver _remoteDrag = RemoteDragReceiver();
  int? _remoteDragId;

  /// The shell drawing each id, for the drag steps and images Java sends it.
  static final Map<int, ShellImpl> _remoteDragTargets = {};

  /// Registers what every Shell answers (see `registerWidgetOps`).
  static void registerOps() {
    EquoCommService.onWidgetOp(gen.WireType.shell, gen.WireChannel.dnDRemoteDrag, _remoteDragOp);
    EquoCommService.onWidgetOp(gen.WireType.shell, gen.WireChannel.dragDetectDragImage, _remoteDragImageOp);
  }

  /// A drag step: an event's fields. One for a shell nothing draws is not kept for a later one.
  static FutureOr<bool> _remoteDragOp(int id, Uint8List frame, int start, int end) {
    _remoteDragTargets[id]?._receiveRemoteDrag((WidgetWireReader(frame)..pos = start).fields(VEvent()));
    return true;
  }

  /// The drag image, as PNG bytes, ahead of the first step into this window.
  static FutureOr<bool> _remoteDragImageOp(int id, Uint8List frame, int start, int end) {
    final shell = _remoteDragTargets[id];
    final payload = TaggedValues.decodeAt(frame, start);
    if (shell == null || payload is! Map) return true;
    final png = payload['segments'];
    if (png is List<int> && png.isNotEmpty) {
      shell._remoteDrag.image(Uint8List.fromList(png), Size(_num(payload['width']), _num(payload['height'])),
          Offset(_num(payload['x']), _num(payload['y'])));
    }
    return true;
  }

  void _listenRemoteDrag(int id) {
    _unlistenRemoteDrag();
    _remoteDragId = id;
    _remoteDragTargets[id] = this;
  }

  void _unlistenRemoteDrag() {
    final id = _remoteDragId;
    if (id != null && identical(_remoteDragTargets[id], this)) _remoteDragTargets.remove(id);
    _remoteDragId = null;
  }

  void _receiveRemoteDrag(VEvent step) {
    if (!mounted) return;
    final x = step.x, y = step.y;
    final box = context.findRenderObject() as RenderBox?;
    final global = x != null && y != null && box != null && box.hasSize
        ? box.localToGlobal(Offset(x.toDouble(), y.toDouble()))
        : null;
    if (!_remoteDrag.handle(context, step.detail ?? 0, global, operation: step.feedback ?? Dnd.dropNone)) {
      widget.sendEvent(state, gen.WireChannel.dnDRemoteDropNone, VEvent());
    }
  }

  static double _num(Object? value) => (value as num?)?.toDouble() ?? 0;

  void Function()? _stopPopupRecords;

  /// Popups opened over this shell, when this client is the window drawing it. Empty in every other
  /// client: Java sends them only to the window that owns the control they were opened for (see
  /// `DisplayBridge.syncShellPopups`), because a popup hangs off the Display rather than off this
  /// shell's widget tree and nothing else would carry it here.
  List<VMenu> _popups = const [];

  /// What holds this shell's popup menus: the shell's popups record, keyed apart from the shell.
  int get _popupsKey => WidgetKey.of(gen.WireType.shellPopups, state.id);

  /// Tooltips of the shells this window draws, sent with [_popups] for the same reason.
  List<VToolTip> _tooltips = const [];

  void _showPopups(List<VMenu> menus, List<VToolTip> tooltips) {
    VRegistry.instance.holdsAt(_popupsKey, [...menus, ...tooltips]);
    if (!mounted) return;
    setState(() {
      _popups = menus;
      _tooltips = tooltips;
    });
  }

  /// Where a window-global pointer position falls in the coordinate space Java works in: measured
  /// from this Shell's own top-left, then shifted by where this Shell sits. Read from the render
  /// tree rather than derived from the frame's parts, so maximized state is already accounted for,
  /// and through the Shell's own transform, so the result is in SWT units at any zoom.
  ///
  /// The top-left is the frame's, title bar included: Java's bounds for a shell start there too, and
  /// its toDisplay adds the title bar to reach the client area.
  Offset toDisplay(Offset windowPosition) => _toDisplay(windowPosition);

  Offset _toDisplay(Offset windowPosition) {
    if (!mounted) return logicalToSwt(windowPosition);
    final box = context.findRenderObject() as RenderBox?;
    if (box == null || !box.hasSize) return logicalToSwt(windowPosition);
    final local = box.globalToLocal(windowPosition);
    final b = state.bounds;
    if (b == null) return local;
    return Offset(local.dx + b.x.toDouble(), local.dy + b.y.toDouble());
  }

  @override
  void extraSetState() {
    super.extraSetState();
    // Again on every update, because the channel is keyed by id and this State outlives a change
    // of it: subscribing once at mount leaves the Shell listening on an id Java no longer sends
    // to, and a Tracker opened after that is never driven -- it just times out and its drop is
    // discarded. The remote-drag channel is keyed by id the same way.
    _attachTrackerHost();
  }

  int? _trackerHostId;

  void _attachTrackerHost() {
    final id = state.id;
    if (_trackerHostId != null && _trackerHostId != id) TrackerSession.detachHost(_trackerHostId!);
    _trackerHostId = id;
    TrackerSession.attachHost(state.swt, id, toDisplay: _toDisplay);
    if (_remoteDragId != id) _listenRemoteDrag(id);
  }

  @override
  void dispose() {
    final trackerHost = _trackerHostId;
    if (trackerHost != null) TrackerSession.detachHost(trackerHost);
    _stopPopupRecords?.call();
    VRegistry.instance.holdsAt(_popupsKey, const []);
    _stopWindowShells?.call();
    VRegistry.instance.holdsAt(_windowShellsKey, const []);
    _unlistenRemoteDrag();
    _remoteDrag.dispose();
    _opacityNotifier.dispose();
    _focusScopeNode.removeListener(_handleFocusScopeChange);
    _focusScopeNode.dispose();
    super.dispose();
  }

  void _handleFocusScopeChange() {
    final hasFocusNow = _focusScopeNode.hasFocus;
    if (hasFocusNow == _hasFocus) return;
    // A popup menu the Display shows is drawn outside every shell; it takes the keyboard without
    // deactivating the shell it opened over, as natively.
    if (!hasFocusNow && OpenMenus.any) return;
    _hasFocus = hasFocusNow;
    if (_hasFocus) {
      widget.sendShellActivate(state, null);
    } else {
      widget.sendShellDeactivate(state, null);
    }
  }

  KeyEventResult _handleShellKey(FocusNode node, KeyEvent event) {
    if (event is KeyDownEvent && event.logicalKey == LogicalKeyboardKey.escape) {
      // The Display already forwards every key in the whole-tree backend; a second KeyDown here
      // would deliver the Escape to Java twice.
      if (!displayLevelKeyForwardingActive) {
        widget.sendKeyKeyDown(state, VEvent()
          ..keyCode = SWT.ESC
          ..character = SWT.ESC);
      }
      return KeyEventResult.handled;
    }
    return KeyEventResult.ignored;
  }

  /// Alpha-only updates bypass setState/rebuild and just touch this notifier.
  @override
  void setValue(V value) {
    _opacityNotifier.value = (value.alpha ?? 255) / 255.0;
    final change = lastChange;
    // An alpha-only delivery is an animation frame: the notifier above has already carried it, and
    // rebuilding the shell for each one is the cost this bypass exists to avoid.
    if (change != null && change.isOnly(const {gen.WireKey.k_Shell_alpha})) return;
    super.setValue(value);
    openNewDialogs(context, value);
    _noteVisibility(value.visible ?? true);
  }

  /// A Shell shown after being hidden is opened again, so it takes its initial focus again. Only
  /// that transition may re-arm it: re-arming on any update the Shell happens to receive would
  /// send [nextFocus] round the Shell's controls on every delivery, moving the keyboard off
  /// whatever the user is typing into.
  void _noteVisibility(bool visible) {
    if (!_wasVisible && visible) _autoFocusRequested = false;
    _wasVisible = visible;
  }

  @override
  void didUpdateWidget(covariant T oldWidget) {
    super.didUpdateWidget(oldWidget);
    _opacityNotifier.value = (state.alpha ?? 255) / 255.0;
    _noteVisibility(state.visible ?? true);
    if (_interacting) return;
    _size = null;
  }

  int get _style => state.style;
  bool get _hasTitle => (_style & SWT.TITLE) != 0;
  /// An ON_TOP shell is a popup its owner shows without handing over activation. Whether a press
  /// inside it activates it is Java's call (`DisplayBridge.activateShellOnPress`), made before the
  /// focus moves, so the client does not take the focus for it on pointer down.
  bool get _isOnTop => (_style & SWT.ON_TOP) != 0;
  bool get _hasClose => (_style & SWT.CLOSE) != 0;
  bool get _hasMax => (_style & SWT.MAX) != 0;
  bool get _hasResize => (_style & SWT.RESIZE) != 0;
  bool get _noTrim => (_style & SWT.NO_TRIM) != 0;
  bool get _noMove => (_style & SWT.NO_MOVE) != 0;
  bool get _hasBorder => (_style & SWT.BORDER) != 0;
  bool get _isTool => (_style & SWT.TOOL) != 0;
  bool get _isSheet => (_style & SWT.SHEET) != 0;

  bool get _showTitleBar => !_noTrim && (_hasTitle || _hasClose);
  bool get _showBorder => !_noTrim && (_hasBorder || _showTitleBar);
  bool get _showFloatingBorder => !_noTrim;
  bool get _isDraggable =>
      _showTitleBar && !_noMove && !_isSheet && !_maximized && state.fullScreen != true;

  /// Paints the shell's own background/backgroundImage, and publishes it via
  /// ParentBackgroundScope for INHERIT_DEFAULT/FORCE descendants that don't set their own.
  @override
  Widget buildComposite() {
    final built = super.buildComposite();

    // Gated the same way Composite's own background is (getCompositeBackgroundColor ->
    // getBackgroundColor): a bare state.background is often just the SWT default widget-background
    // gray (e.g. JFace's COLOR_WIDGET_BACKGROUND on dialog Shells) rather than a deliberate color,
    // so only paint it when use_swt_colors opts in. backgroundImage has no such "default" case, so
    // it stays unconditional.
    final ownColor = getBackgroundColor(background: state.background, defaultColor: null);
    final tiledImage = ImageUtils.buildTiledBackgroundImage(state.backgroundImage);

    Widget painted = built;
    if (ownColor != null || tiledImage != null) {
      painted = DecoratedBox(
        decoration: BoxDecoration(color: ownColor, image: tiledImage),
        child: built,
      );
    }

    final inheritable = (state.backgroundMode ?? SWT.INHERIT_NONE) != SWT.INHERIT_NONE;
    if (!inheritable || (ownColor == null && tiledImage == null)) return painted;

    return ParentBackgroundScope(
      background: ownColor,
      backgroundImage: state.backgroundImage,
      child: painted,
    );
  }

  @override
  Widget build(BuildContext context) {
    // Shell builds its own tree and never routes through ControlImpl.wrap(), so a hidden shell
    // would keep painting and swallowing pointer input over whatever it covers.
    final scope = FloatingShellChromeScope.maybeOf(context);
    if (state.visible != null && !state.visible!) {
      // Positioned inside the Display's stack, as a shown one is: a child that is not would size
      // the stack, and every shell filling it, to nothing. Its own record hides it ahead of the
      // display's, so the display can still hold it.
      return scope == null ? const SizedBox.shrink() : const Positioned(left: 0, top: 0, child: SizedBox.shrink());
    }
    if (scope == null) {
      // No chrome scope above: this shell fills the window it is drawn in. In one of its own it
      // draws the popups, shells and tooltips opened over it; in the Display's window the Display
      // does, and Java sends this shell none.
      final content = super.build(context);
      // This shell IS the window, so its bounds are where the window sits on screen: publish that
      // for anything consuming a screen coordinate inside it (a popup's location, a drop target).
      final b0 = state.bounds;
      final windowOrigin =
          b0 == null ? Offset.zero : Offset(b0.x.toDouble(), b0.y.toDouble());
      final own = FocusScope(
        node: _focusScopeNode,
        onKeyEvent: _handleShellKey,
        child: Listener(
          behavior: HitTestBehavior.translucent,
          onPointerDown: (_) {
            if (!_isOnTop && !_focusScopeNode.hasFocus) _focusScopeNode.requestFocus();
          },
          child: content,
        ),
      );
      // Beside the focus scope, not in it: the keyboard in a shell drawn here deactivates this one,
      // and in the popup of such a shell does not activate it. The stack is there with none too, or
      // the first one would rebuild everything in the shell anew.
      return WindowOriginScope(
        origin: windowOrigin,
        child: Stack(children: [
          Positioned.fill(child: own),
          for (final popup in _popups)
            KeyedSubtree(
              key: ValueKey(popup.id),
              child: Positioned.fill(child: gen.mapWidgetFromValue(popup)),
            ),
          if (_windowShells.isNotEmpty)
            Positioned.fill(
              child: LayoutBuilder(
                builder: (context, viewport) => Stack(
                  children: floatingShells(context, _windowShells, viewport, _activeShellId),
                ),
              ),
            ),
          // Above the shells: one of them can be the tooltip's own.
          for (final tooltip in _tooltips)
            KeyedSubtree(
              key: ValueKey(tooltip.id),
              child: Positioned.fill(child: gen.mapWidgetFromValue(tooltip)),
            ),
        ]),
      );
    }

    final viewport = scope.viewportConstraints;
    final theme = Theme.of(context).extension<DisplayThemeExtension>()!;
    final isFullScreen = state.fullScreen == true;
    final b = state.bounds;

    final titleBarH = _isTool ? theme.toolWindowTitleBarHeight : theme.titleBarHeight;
    final headerH = _showTitleBar ? titleBarH : 0.0;
    // What the frame adds around the body on each axis: the border drawn around a floating pane.
    final chromeEdge =
        _showFloatingBorder && state.region == null ? 2 * theme.dialogBorderWidth : 0.0;

    final rawW = (b?.width ?? 400).toDouble();
    final rawH = (b?.height ?? 300).toDouble();
    // A trimmed shell is drawn at the size the application set, as a native one is; a floor is the
    // application's to set (setMinimumSize). Only a dimension Java has not set yet (0, or -1 before
    // the shell is sized) gets one here, since drawn at that size the shell would not be there.
    // The ceiling is what fits in the viewport under the title bar and inside the border: Java sizes
    // a shell against the monitor, which on this surface is the viewport, and its bounds include
    // neither.
    double bodySize(double raw, double fallback, int? minimum, double fits) {
      final wanted = math.max(raw > 0 ? raw : fallback, (minimum ?? 0).toDouble());
      return math.min(wanted, math.max(fallback, fits));
    }

    final bodyW = _size != null
        ? _size!.width
        : (!_showTitleBar && !_showBorder
            ? rawW
            : bodySize(rawW, 200, state.minimumSize?.x, viewport.maxWidth - chromeEdge));
    final bodyH = _size != null
        ? _size!.height
        : (!_showTitleBar && !_showBorder
            ? rawH
            : bodySize(
                rawH, 150, state.minimumSize?.y, viewport.maxHeight - headerH - chromeEdge));

    final w = (isFullScreen || _maximized) ? viewport.maxWidth : bodyW;
    final h = (isFullScreen || _maximized) ? viewport.maxHeight : bodyH;
    final frameH = headerH + h;

    /// Where a pane asked to sit at [wanted] is actually drawn.
    ///
    /// The position comes from the application, and an application built for real windows measures
    /// one against the monitor — which on the desktop surface is the screen, several times larger
    /// than the window every shell is drawn inside. The e4 workbench detaching a view is exactly
    /// that: it places the new shell at `Control.toDisplay` of the part stack, then keeps it on the
    /// *monitor*. A pane laid out past the edge is clipped away by the stack that holds it, so it
    /// is not drawn small or half — it is not on screen at all. Moved inside, the way a window
    /// manager places an off-screen window. A pane that already fits is left exactly where it is.
    Offset insideViewport(Offset wanted) {
      if (!viewport.maxWidth.isFinite || !viewport.maxHeight.isFinite) return wanted;
      final maxX = math.max(0.0, viewport.maxWidth - w - chromeEdge);
      final maxY = math.max(0.0, viewport.maxHeight - frameH - chromeEdge);
      return wholePixels(Offset(wanted.dx.clamp(0.0, maxX), wanted.dy.clamp(0.0, maxY)));
    }

    // A floating shell's bounds are a SCREEN position; this offset is measured from the window
    // drawing it, so the window's own origin comes off.
    final windowOrigin = WindowOriginScope.of(context);

    Offset resolvedOffset() {
      if (isFullScreen || _maximized) return Offset.zero;
      if (_offset != null) return _offset!;
      // An untrimmed shell counts as placed wherever its bounds are: the application anchors it to
      // a control and calls setLocation, and anchored at the window's top-left that location IS the
      // origin -- which the (0,0) test below would otherwise read as never having been placed.
      if (b != null && (b.x != 0 || b.y != 0 || _noTrim)) {
        final wanted = Offset(b.x.toDouble(), b.y.toDouble()) - windowOrigin;
        // A region is read against the shell's own origin (see below), so moving the shell moves
        // what the region marks: that one is placed where it was asked, wherever that is.
        return state.region != null ? wanted : insideViewport(wanted);
      }
      // A shell clipped to a region means its position, including the origin: the region's
      // rectangles are given in this shell's own coordinates and describe areas of the window
      // behind it. Centring one larger than the viewport -- the workbench sizes its drop feedback
      // to its own window, not to the client's -- moves those rectangles off the zone they mark.
      if (b != null && state.region != null) {
        return Offset(b.x.toDouble(), b.y.toDouble()) - windowOrigin;
      }
      // What is left is a shell nobody placed: a dialog opened at the platform's default.
      return wholePixels(Offset(
        (viewport.maxWidth - bodyW) / 2,
        (viewport.maxHeight - headerH - bodyH) / 2,
      ));
    }

    final offset = resolvedOffset();

    void sendBoundsToJava({bool initial = false}) {
      // Back to SCREEN space, where Java keeps a shell's bounds: reporting the drawn offset would
      // have the window's origin taken off a second time on the next update.
      final drawn = (isFullScreen || _maximized) ? Offset.zero : (_offset ?? offset);
      final pos = drawn + windowOrigin;
      widget.sendShellSetBounds(
        state,
        VEvent()
          ..x = pos.dx.round()
          ..y = pos.dy.round()
          ..width =
              (isFullScreen || _maximized) ? viewport.maxWidth.round() : bodyW.round()
          ..height =
              (isFullScreen || _maximized) ? viewport.maxHeight.round() : bodyH.round()
          // Java ignores the position in this one for a shell it moved after this frame's bounds were sent.
          ..detail = initial ? initialClientBounds : null,
      );
    }

    if (!_initialBoundsSent) {
      _initialBoundsSent = true;
      WidgetsBinding.instance.addPostFrameCallback((_) {
        if (mounted) sendBoundsToJava(initial: true);
      });
    } else if (_size == null &&
        !_interacting &&
        !(isFullScreen || _maximized) &&
        b != null &&
        b.width > 0 &&
        b.height > 0 &&
        (bodyW.round() != b.width || bodyH.round() != b.height)) {
      // Java set a size that is not drawn as such (taller than fits, say): it lays the shell out
      // in that size unless told the drawn one. Once per size it set, so an application that keeps
      // the size it asked for is told once and left alone.
      final set = (b.x, b.y, b.width, b.height);
      if (_drawnSizeReportedFor != set) {
        _drawnSizeReportedFor = set;
        WidgetsBinding.instance.addPostFrameCallback((_) {
          if (mounted) sendBoundsToJava();
        });
      }
    }

    void onMaximize() {
      setState(() {
        _maximized = !_maximized;
      });
      sendBoundsToJava();
    }

    final title = (state.modified == true ? '• ' : '') + (state.text ?? '');
    final borderRadius = _showBorder ? theme.dialogBorderRadius : 0.0;

    Widget body = ClipRect(
      child: SizedBox(
        width: w,
        height: h,
        child: super.build(context),
      ),
    );

    Widget column = Column(
      mainAxisSize: MainAxisSize.min,
      children: [
        if (_showTitleBar)
          SizedBox(
            width: w,
            child: _TitleBar(
              title: title,
              isTool: _isTool,
              hasClose: _hasClose,
              hasMax: _hasMax,
              isMaximized: _maximized,
              isDraggable: _isDraggable,
              height: titleBarH,
              borderRadius: borderRadius,
              theme: theme,
              onDragStart: () => setState(() => _interacting = true),
              onDrag: (delta) => setState(() => _offset = (_offset ?? offset) + delta),
              onDragEnd: () {
                setState(() => _interacting = false);
                sendBoundsToJava();
              },
              onDragCancel: () => setState(() => _interacting = false),
              onClose: () => widget.sendShellClose(state, null),
              onMaximize: onMaximize,
            ),
          ),
        body,
      ],
    );

    final minBodyH = (state.minimumSize?.y ?? 80).toDouble();
    final maxBodyH =
        math.max(minBodyH,
            state.maximumSize?.y.toDouble() ?? viewport.maxHeight - headerH - chromeEdge);
    final minBodyW = (state.minimumSize?.x ?? 100).toDouble();
    final maxBodyW =
        math.max(minBodyW, state.maximumSize?.x.toDouble() ?? viewport.maxWidth - chromeEdge);
    final minFH = minBodyH + headerH;
    final maxFH = maxBodyH + headerH;

    Widget framed = SizedBox(width: w, height: frameH, child: column);

    void resize(Offset delta, _ResizeEdge edge) => setState(() {
          final curW  = _size?.width ?? bodyW;
          final curFH = (_size?.height ?? bodyH) + headerH;
          final loW  = math.min(minBodyW, curW);
          final loFH = math.min(minFH, curFH);

          double newW  = curW,  newFH = curFH;
          double odx   = 0,     ody   = 0;

          switch (edge) {
            case _ResizeEdge.right:
              newW  = (curW + delta.dx).clamp(loW, maxBodyW);
            case _ResizeEdge.left:
              newW  = (curW - delta.dx).clamp(loW, maxBodyW);
              odx   = -(newW - curW);
            case _ResizeEdge.bottom:
              newFH = (curFH + delta.dy).clamp(loFH, maxFH);
            case _ResizeEdge.top:
              newFH = (curFH - delta.dy).clamp(loFH, maxFH);
              ody   = -(newFH - curFH);
            case _ResizeEdge.se:
              newW  = (curW + delta.dx).clamp(loW, maxBodyW);
              newFH = (curFH + delta.dy).clamp(loFH, maxFH);
            case _ResizeEdge.sw:
              newW  = (curW - delta.dx).clamp(loW, maxBodyW);
              newFH = (curFH + delta.dy).clamp(loFH, maxFH);
              odx   = -(newW - curW);
            case _ResizeEdge.ne:
              newW  = (curW + delta.dx).clamp(loW, maxBodyW);
              newFH = (curFH - delta.dy).clamp(loFH, maxFH);
              ody   = -(newFH - curFH);
            case _ResizeEdge.nw:
              newW  = (curW - delta.dx).clamp(loW, maxBodyW);
              newFH = (curFH - delta.dy).clamp(loFH, maxFH);
              odx   = -(newW - curW);
              ody   = -(newFH - curFH);
          }
          _offset = (_offset ?? offset) + Offset(odx, ody);
          _size   = Size(newW, (newFH - headerH).clamp(loFH - headerH, maxBodyH));
        });

    final isTooltipLike = (_style & SWT.ON_TOP) != 0 && (_style & SWT.TOOL) != 0;
    final shellBg = isTooltipLike ? theme.tooltipShellBackgroundColor : null;
    if (shellBg != null) {
      framed = ParentBackgroundScope(background: shellBg, child: framed);
    }

    // A region is the window's shape, so the frame it describes is all there is to this shell: a
    // border, a shadow or a rounded corner would be drawn around the full rectangle the region
    // carves that frame out of, not around the frame.
    final isShaped = state.region != null;
    Widget dialog = Container(
      clipBehavior: Clip.none,
      decoration: BoxDecoration(
        color: shellBg ?? theme.dialogBackgroundColor,
        borderRadius: BorderRadius.circular(isShaped ? 0 : borderRadius),
        border: _showFloatingBorder && !isShaped
            ? Border.all(color: theme.dialogBorderColor, width: theme.dialogBorderWidth)
            : null,
        boxShadow: _showFloatingBorder && !isShaped
            ? [
                BoxShadow(
                  color: theme.dialogShadowColor,
                  blurRadius: theme.dialogShadowBlurRadius,
                  spreadRadius: theme.dialogShadowSpreadRadius,
                  offset: Offset(theme.dialogShadowOffsetX, theme.dialogShadowOffsetY),
                ),
              ]
            : null,
      ),
      child: framed,
    );

    final content = FocusScope(
      node: _focusScopeNode,
      onKeyEvent: _handleShellKey,
      child: Listener(
        behavior: HitTestBehavior.translucent,
        onPointerDown: (_) {
          if (!_isOnTop && !_focusScopeNode.hasFocus) _focusScopeNode.requestFocus();
        },
        child: dialog,
      ),
    );

    // Only a shell SWT activated may take the keyboard. `Shell.setVisible(true)` does not activate:
    // a JFace `SWT.ON_TOP` popup (content assist) is shown that way precisely so the control that
    // opened it keeps focus and can drive the popup from its own VerifyKeyListener.
    if (!_autoFocusRequested && scope.activeShellId == state.id) {
      _autoFocusRequested = true;
      // Two frames: a control Java focused lands at the end of the first, and nextFocus would skip it.
      WidgetsBinding.instance.addPostFrameCallback((_) => afterNextFrame(() {
            // Only when no control in the shell holds the keyboard: the scope itself or nothing.
            final noControlFocused = !_focusScopeNode.hasFocus || _focusScopeNode.hasPrimaryFocus;
            if (mounted && noControlFocused) _focusScopeNode.nextFocus();
          }));
    }

    // Always wrap in Opacity, never conditionally on opacity < 1.0 -- branching the widget
    // TYPE at this position tears down and remounts the whole content subtree on the first
    // alpha tick, wiping any already-committed GC shapes inside it.
    final opacityWrapped = ValueListenableBuilder<double>(
      valueListenable: _opacityNotifier,
      child: content,
      builder: (context, opacity, child) => Opacity(opacity: opacity, child: child),
    );

    // Clipped out here rather than around the shell's content: a region shapes the window itself,
    // and the shell's own background is what fills the frame, so clipping only what the shell
    // contains would still paint that background over everything the frame is meant to reveal.
    return Positioned(
      left: offset.dx,
      top: offset.dy,
      child: _ResizableWrapper(
        enabled: _hasResize && !_maximized && !isFullScreen,
        room: EdgeInsets.fromLTRB(
          offset.dx,
          offset.dy,
          viewport.maxWidth - offset.dx - w - chromeEdge,
          viewport.maxHeight - offset.dy - frameH - chromeEdge,
        ),
        onResizeStart: () => setState(() => _interacting = true),
        onResize: resize,
        onResizeEnd: () {
          setState(() => _interacting = false);
          sendBoundsToJava();
        },
        child: RepaintBoundary(
          child: pointerInterceptor(RegionClip.maybe(state.region, opacityWrapped)),
        ),
      ),
    );
  }
}

class _TitleBar extends StatelessWidget {
  final String title;
  final bool isTool, hasClose, hasMax, isMaximized, isDraggable;
  final double height, borderRadius;
  final DisplayThemeExtension theme;
  final VoidCallback? onDragStart;
  final void Function(Offset) onDrag;
  final VoidCallback? onDragEnd;
  final VoidCallback? onDragCancel;
  final VoidCallback onClose, onMaximize;

  const _TitleBar({
    required this.title,
    required this.isTool,
    required this.hasClose,
    required this.hasMax,
    required this.isMaximized,
    required this.isDraggable,
    required this.height,
    required this.borderRadius,
    required this.theme,
    this.onDragStart,
    required this.onDrag,
    this.onDragEnd,
    this.onDragCancel,
    required this.onClose,
    required this.onMaximize,
  });

  @override
  Widget build(BuildContext context) {
    final btnSize = isTool ? 10.0 : 12.0;

    final bar = Container(
      height: height,
      padding: const EdgeInsets.symmetric(horizontal: 8),
      decoration: BoxDecoration(
        color: theme.titleBarColor,
        borderRadius: BorderRadius.vertical(
          top: Radius.circular(borderRadius > 0 ? borderRadius - 1 : 0),
        ),
      ),
      child: Row(
        children: [
          Expanded(
            child: Text(
              title,
              style: isTool ? theme.toolWindowTitleTextStyle : theme.titleTextStyle,
              overflow: TextOverflow.ellipsis,
              maxLines: 1,
            ),
          ),
          if (hasMax && !isTool)
            _TitleBarButton(
              icon: isMaximized ? Icons.close_fullscreen : Icons.crop_square,
              iconColor: theme.maximizeButtonColor,
              hoverBgColor: theme.maximizeButtonHoverColor,
              iconSize: btnSize,
              onTap: onMaximize,
            ),
          if (hasClose)
            _TitleBarButton(
              icon: Icons.close,
              iconColor: theme.closeButtonColor,
              hoverBgColor: theme.closeButtonHoverColor,
              hoverIconColor: theme.closeButtonHoverIconColor,
              iconSize: btnSize,
              onTap: onClose,
            ),
        ],
      ),
    );

    return GestureDetector(
      onPanStart: isDraggable ? (_) => onDragStart?.call() : null,
      onPanUpdate: isDraggable ? (d) => onDrag(d.delta) : null,
      onPanEnd: isDraggable ? (_) => onDragEnd?.call() : null,
      onPanCancel: isDraggable ? (onDragCancel ?? onDragEnd) : null,
      child: bar,
    );
  }
}

class _TitleBarButton extends StatefulWidget {
  final IconData icon;
  final Color iconColor, hoverBgColor;
  final Color? hoverIconColor;
  final double iconSize;
  final VoidCallback onTap;

  const _TitleBarButton({
    required this.icon,
    required this.iconColor,
    required this.hoverBgColor,
    this.hoverIconColor,
    required this.iconSize,
    required this.onTap,
  });

  @override
  State<_TitleBarButton> createState() => _TitleBarButtonState();
}

class _TitleBarButtonState extends State<_TitleBarButton> {
  bool _hovering = false;

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context).extension<DisplayThemeExtension>()!;
    return MouseRegion(
      onEnter: (_) => setState(() => _hovering = true),
      onExit: (_) => setState(() => _hovering = false),
      child: GestureDetector(
        onTap: widget.onTap,
        child: QuietAnimatedContainer(
          duration: theme.titleBarButtonAnimationDuration,
          width: theme.titleBarButtonSize,
          height: theme.titleBarButtonSize,
          decoration: BoxDecoration(
            color: _hovering ? widget.hoverBgColor : Colors.transparent,
            borderRadius: BorderRadius.circular(theme.titleBarButtonBorderRadius),
          ),
          child: Icon(
            widget.icon,
            size: widget.iconSize,
            color: _hovering && widget.hoverIconColor != null
                ? widget.hoverIconColor
                : widget.iconColor,
          ),
        ),
      ),
    );
  }
}

/// A resizable shell's resize border, laid in a band just outside its frame, as a native window's
/// is: a RESIZE shell adds no trim, its client area is the whole frame, and every point of the frame
/// belongs to the controls in it. The frame stays this widget's own box, so the shell's render box
/// keeps the origin and size Java's bounds describe.
class _ResizableWrapper extends StatelessWidget {
  final bool enabled;
  /// The space between each side of the frame and the viewport's edge.
  final EdgeInsets room;
  final VoidCallback? onResizeStart;
  final void Function(Offset delta, _ResizeEdge edge) onResize;
  final VoidCallback? onResizeEnd;
  final Widget child;

  static const double _h = 8.0;

  static const _frameKey = ValueKey('frame');

  const _ResizableWrapper({
    this.enabled = true,
    required this.room,
    this.onResizeStart,
    required this.onResize,
    this.onResizeEnd,
    required this.child,
  });

  // Keyed by edge: a handle that moves between the frame's sides of the stack mid-drag keeps it, so
  // its subtree is the same on either side. Outside the frame it is outside the frame's pointer
  // interceptor, so it takes its own: on web a platform view (Browser) under it takes the press
  // otherwise.
  Widget _handle(
    MouseCursor cursor,
    _ResizeEdge edge, {
    double? top,
    double? bottom,
    double? left,
    double? right,
    double? w,
    double? h,
  }) =>
      Positioned(
        key: ValueKey(edge),
        top: top,
        bottom: bottom,
        left: left,
        right: right,
        width: w,
        height: h,
        child: pointerInterceptor(MouseRegion(
          cursor: cursor,
          child: GestureDetector(
            behavior: HitTestBehavior.opaque,
            onPanStart: (_) => onResizeStart?.call(),
            onPanUpdate: (d) => onResize(d.delta, edge),
            onPanEnd: (_) => onResizeEnd?.call(),
            onPanCancel: onResizeEnd,
          ),
        )),
      );

  @override
  // The frame wins every hit inside it unless a handle comes after it. Outside the viewport a band
  // can't be reached, so on such a side it lies inside the frame, over it: a shell as big as the
  // viewport keeps its border, as a window as big as the screen does.
  Widget build(BuildContext context) {
    if (!enabled) {
      return _OutsetHitStack(outset: _h, children: [KeyedSubtree(key: _frameKey, child: child)]);
    }
    final inL = room.left < _h, inT = room.top < _h, inR = room.right < _h, inB = room.bottom < _h;
    final under = <Widget>[], over = <Widget>[];
    void side(bool inset, Widget handle) => (inset ? over : under).add(handle);
    side(inT, _handle(SystemMouseCursors.resizeUpDown, _ResizeEdge.top,
        top: inT ? 0 : -_h, left: 0, right: 0, h: _h));
    side(inB, _handle(SystemMouseCursors.resizeUpDown, _ResizeEdge.bottom,
        bottom: inB ? 0 : -_h, left: 0, right: 0, h: _h));
    side(inL, _handle(SystemMouseCursors.resizeLeftRight, _ResizeEdge.left,
        left: inL ? 0 : -_h, top: 0, bottom: 0, w: _h));
    side(inR, _handle(SystemMouseCursors.resizeLeftRight, _ResizeEdge.right,
        right: inR ? 0 : -_h, top: 0, bottom: 0, w: _h));
    // A corner straddles each outside edge, reaching _h into the frame under it for an L-shaped grip
    // outside; against an inset edge it covers only that edge's band.
    void corner(MouseCursor cursor, _ResizeEdge edge,
        {required bool x0, required bool y0, required bool inX, required bool inY}) {
      final dx = inX ? 0.0 : -_h, dy = inY ? 0.0 : -_h;
      final cw = inX ? _h : _h * 2, ch = inY ? _h : _h * 2;
      side(inX || inY, _handle(cursor, edge,
          left: x0 ? dx : null, right: x0 ? null : dx,
          top: y0 ? dy : null, bottom: y0 ? null : dy, w: cw, h: ch));
    }
    corner(SystemMouseCursors.resizeUpLeftDownRight, _ResizeEdge.nw,
        x0: true, y0: true, inX: inL, inY: inT);
    corner(SystemMouseCursors.resizeUpRightDownLeft, _ResizeEdge.ne,
        x0: false, y0: true, inX: inR, inY: inT);
    corner(SystemMouseCursors.resizeUpRightDownLeft, _ResizeEdge.sw,
        x0: true, y0: false, inX: inL, inY: inB);
    corner(SystemMouseCursors.resizeUpLeftDownRight, _ResizeEdge.se,
        x0: false, y0: false, inX: inR, inY: inB);
    return _OutsetHitStack(
      outset: _h,
      children: [...under, KeyedSubtree(key: _frameKey, child: child), ...over],
    );
  }
}

/// A [Stack] that also takes hits up to [outset] outside its own box, where the children it lays
/// past its edges are. A [RenderBox] takes only the hits inside its size.
class _OutsetHitStack extends Stack {
  final double outset;

  const _OutsetHitStack({required this.outset, super.children}) : super(clipBehavior: Clip.none);

  @override
  RenderStack createRenderObject(BuildContext context) => _RenderOutsetHitStack(
        outset: outset,
        alignment: alignment,
        textDirection: textDirection ?? Directionality.maybeOf(context),
        fit: fit,
        clipBehavior: clipBehavior,
      );

  @override
  void updateRenderObject(BuildContext context, RenderStack renderObject) {
    super.updateRenderObject(context, renderObject);
    (renderObject as _RenderOutsetHitStack).outset = outset;
  }
}

class _RenderOutsetHitStack extends RenderStack {
  double outset;

  _RenderOutsetHitStack({
    required this.outset,
    super.alignment,
    super.textDirection,
    super.fit,
    super.clipBehavior,
  });

  @override
  bool hitTest(BoxHitTestResult result, {required Offset position}) {
    if (!(Offset.zero & size).inflate(outset).contains(position)) return false;
    if (!hitTestChildren(result, position: position)) return false;
    result.add(BoxHitTestEntry(this, position));
    return true;
  }
}

enum _ResizeEdge { top, bottom, left, right, nw, ne, sw, se }
