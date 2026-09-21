import 'dart:convert';
import 'dart:math' as math;

import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:flutter/widgets.dart';

import '../comm/comm.dart';
import '../comm/v_registry.dart';
import '../gen/menu.dart';
import '../gen/widgets.dart' as gen;
import '../gen/colordialog.dart';
import '../gen/dialog.dart';
import '../gen/event.dart';
import '../gen/messagebox.dart';
import '../gen/shell.dart';
import '../gen/swt.dart';
import '../impl/colordialog_evolve.dart';
import '../impl/decorations_evolve.dart';
import '../impl/messagebox_evolve.dart';
import '../impl/utils/image_utils.dart';
import '../impl/utils/region_clip.dart';
import '../impl/utils/tracker_session.dart';
import '../impl/utils/window_origin.dart';
import '../impl/utils/widget_utils.dart';
import '../theme/theme_extensions/display_theme_extension.dart';
import 'utils/pointer.dart';

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

class ShellImpl<T extends ShellSwt, V extends VShell> extends DecorationsImpl<T, V> {
  Offset? _offset;

  /// How much frame this Shell draws above its content, which a pointer position has to be
  /// measured past: it arrives global to the window, and the bounds Java sends start at the
  /// content. Kept from the last build, which is where the layout decides it.
  double _headerH = 0;
  Size? _size;
  bool _maximized = false;
  bool _interacting = false;
  bool _initialBoundsSent = false;
  final Set<int> _openedDialogIds = {};

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
    TrackerSession.attachHost(state.swt, state.id, toDisplay: _toDisplay);
    _popupsToken = EquoCommService.onRaw(_popupsChannel, _receivePopups);
  }

  /// Popups opened over this shell, when this client is the window drawing it. Empty in every other
  /// client: Java sends them only to the window that owns the control they were opened for (see
  /// `DisplayBridge.syncShellPopups`), because a popup hangs off the Display rather than off this
  /// shell's widget tree and nothing else would carry it here.
  List<VMenu> _popups = const [];
  Object? _popupsToken;

  String get _popupsChannel => 'Shell/${state.id}/Popups';

  void _receivePopups(dynamic payload) {
    final menus = <VMenu>[];
    if (payload is Map && payload['popups'] is List) {
      for (final raw in payload['popups'] as List) {
        if (raw is Map<String, dynamic>) menus.add(VMenu.fromJson(raw));
      }
    }
    // Registering is what puts the menu and its items in the registry; holding is what lets a popup
    // that closed stop being held, instead of being kept for the life of the session.
    for (final menu in menus) {
      VRegistry.instance.register(menu);
    }
    VRegistry.instance.holds(_popupsChannel, menus);
    if (!mounted) return;
    setState(() => _popups = menus);
  }

  /// Where a window-global pointer position falls in the coordinate space Java works in: measured
  /// from the content's own origin, then shifted by where this Shell sits. Read from the render
  /// tree rather than derived from the frame's parts, so border, title bar and maximized state are
  /// all already accounted for.
  Offset _toDisplay(Offset windowPosition) {
    if (!mounted) return windowPosition;
    final box = context.findRenderObject() as RenderBox?;
    if (box == null || !box.hasSize) return windowPosition;
    // The Shell's own top-left, plus whatever frame it draws above its content: what is left is
    // measured from the same origin the bounds Java sends are measured from.
    final content = box.localToGlobal(Offset.zero) + Offset(0, _headerH);
    final local = windowPosition - content;
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
    // discarded. attachHost is a no-op for an id already subscribed.
    TrackerSession.attachHost(state.swt, state.id, toDisplay: _toDisplay);
  }

  @override
  void dispose() {
    EquoCommService.remove(_popupsChannel, _popupsToken);
    VRegistry.instance.holds(_popupsChannel, const []);
    _opacityNotifier.dispose();
    _focusScopeNode.removeListener(_handleFocusScopeChange);
    _focusScopeNode.dispose();
    super.dispose();
  }

  void _handleFocusScopeChange() {
    final hasFocusNow = _focusScopeNode.hasFocus;
    if (hasFocusNow == _hasFocus) return;
    _hasFocus = hasFocusNow;
    if (_hasFocus) {
      widget.sendShellActivate(state, null);
    } else {
      widget.sendShellDeactivate(state, null);
    }
  }

  KeyEventResult _handleShellKey(FocusNode node, KeyEvent event) {
    if (event is KeyDownEvent && event.logicalKey == LogicalKeyboardKey.escape) {
      widget.sendKeyKeyDown(state, VEvent()
        ..keyCode = SWT.ESC
        ..character = SWT.ESC);
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
    if (change != null && change.isOnly(const {'alpha'})) return;
    // A value is changed in place, so there is no earlier copy to compare against - what the
    // delivery named is all there is to reason with. A delivery that never mentioned the dialogs
    // did not change them; one that mentions nothing was a whole widget, and then anything may have.
    final prevDialogs =
        (change != null && !change.touches('dialogs')) ? (value.dialogs ?? []) : const <VDialog>[];
    super.setValue(value);
    final newDialogs = value.dialogs ?? [];
    _openedDialogIds.removeWhere((id) => !newDialogs.any((d) => d.id == id));
    for (final d in newDialogs) {
      if (!prevDialogs.any((old) => old.id == d.id) && !_openedDialogIds.contains(d.id)) {
        _openedDialogIds.add(d.id);
        WidgetsBinding.instance.addPostFrameCallback((_) {
          if (mounted) _openDialog(d);
        });
      }
    }
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

  void _openDialog(VDialog d) {
    switch (d.swt) {
      case 'MessageBox':
        showMessageBoxDialog(context, d as VMessageBox, d.id);
      case 'ColorDialog':
        showColorDialog(context, d as VColorDialog, d.id);
    }
  }

  int get _style => state.style;
  bool get _hasTitle => (_style & SWT.TITLE) != 0;
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
    final scope = FloatingShellChromeScope.maybeOf(context);
    if (scope == null) {
      // No chrome scope above: this shell is the root of its own window, so it is also the only
      // thing that can host a popup opened over it. Inside the Display's window the Display draws
      // the popups instead, and _popups stays empty there.
      Widget content = super.build(context);
      // This shell IS the window, so its bounds are where the window sits on screen: publish that
      // for anything consuming a screen coordinate inside it (a popup's location, a drop target).
      final b0 = state.bounds;
      final windowOrigin =
          b0 == null ? Offset.zero : Offset(b0.x.toDouble(), b0.y.toDouble());
      if (_popups.isNotEmpty) {
        content = Stack(children: [
          Positioned.fill(child: content),
          for (final popup in _popups)
            KeyedSubtree(
              key: ValueKey(popup.id),
              child: Positioned.fill(child: gen.mapWidgetFromValue(popup)),
            ),
        ]);
      }
      return WindowOriginScope(
        origin: windowOrigin,
        child: FocusScope(
          node: _focusScopeNode,
          onKeyEvent: _handleShellKey,
          child: Listener(
            behavior: HitTestBehavior.translucent,
            onPointerDown: (_) {
              if (!_focusScopeNode.hasFocus) _focusScopeNode.requestFocus();
            },
            child: content,
          ),
        ),
      );
    }

    final viewport = scope.viewportConstraints;
    final theme = Theme.of(context).extension<DisplayThemeExtension>()!;
    final isFullScreen = state.fullScreen == true;
    final b = state.bounds;

    final rawW = (b?.width ?? 400).toDouble();
    final rawH = (b?.height ?? 300).toDouble();
    final bodyW = _size != null
        ? _size!.width
        : (!_showTitleBar && !_showBorder
            ? rawW
            : rawW.clamp(200.0, math.max(200.0, viewport.maxWidth * 0.9)).toDouble());
    final bodyH = _size != null
        ? _size!.height
        : (!_showTitleBar && !_showBorder
            ? rawH
            : rawH.clamp(150.0, math.max(150.0, viewport.maxHeight * 0.9)).toDouble());

    final titleBarH = _isTool ? theme.toolWindowTitleBarHeight : theme.titleBarHeight;
    final headerH = _showTitleBar ? titleBarH : 0.0;
    _headerH = headerH;

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
      final maxX = math.max(0.0, viewport.maxWidth - w);
      final maxY = math.max(0.0, viewport.maxHeight - frameH);
      return Offset(wanted.dx.clamp(0.0, maxX), wanted.dy.clamp(0.0, maxY));
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
      return Offset(
        (viewport.maxWidth - bodyW) / 2,
        (viewport.maxHeight - headerH - bodyH) / 2,
      );
    }

    final offset = resolvedOffset();

    void sendBoundsToJava() {
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
              (isFullScreen || _maximized) ? viewport.maxHeight.round() : bodyH.round(),
      );
    }

    if (!_initialBoundsSent) {
      _initialBoundsSent = true;
      WidgetsBinding.instance.addPostFrameCallback((_) {
        if (mounted) sendBoundsToJava();
      });
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
    final maxBodyH = state.maximumSize?.y.toDouble() ?? viewport.maxHeight * 0.95;
    final minBodyW = (state.minimumSize?.x ?? 100).toDouble();
    final maxBodyW = state.maximumSize?.x.toDouble() ?? viewport.maxWidth * 0.95;
    final minFH = minBodyH + headerH;
    final maxFH = maxBodyH + headerH;

    Widget framed = SizedBox(
      width: w,
      height: frameH,
      child: _ResizableWrapper(
        enabled: _hasResize && !_maximized && !isFullScreen,
        width: w,
        height: frameH,
        onResizeStart: () => setState(() => _interacting = true),
        onResize: (delta, edge) => setState(() {
          final curW  = _size?.width ?? bodyW;
          final curFH = (_size?.height ?? bodyH) + headerH;

          double newW  = curW,  newFH = curFH;
          double odx   = 0,     ody   = 0;

          switch (edge) {
            case _ResizeEdge.right:
              newW  = (curW + delta.dx).clamp(minBodyW, maxBodyW);
            case _ResizeEdge.left:
              newW  = (curW - delta.dx).clamp(minBodyW, maxBodyW);
              odx   = -(newW - curW);
            case _ResizeEdge.bottom:
              newFH = (curFH + delta.dy).clamp(minFH, maxFH);
            case _ResizeEdge.top:
              newFH = (curFH - delta.dy).clamp(minFH, maxFH);
              ody   = -(newFH - curFH);
            case _ResizeEdge.se:
              newW  = (curW + delta.dx).clamp(minBodyW, maxBodyW);
              newFH = (curFH + delta.dy).clamp(minFH, maxFH);
            case _ResizeEdge.sw:
              newW  = (curW - delta.dx).clamp(minBodyW, maxBodyW);
              newFH = (curFH + delta.dy).clamp(minFH, maxFH);
              odx   = -(newW - curW);
            case _ResizeEdge.ne:
              newW  = (curW + delta.dx).clamp(minBodyW, maxBodyW);
              newFH = (curFH - delta.dy).clamp(minFH, maxFH);
              ody   = -(newFH - curFH);
            case _ResizeEdge.nw:
              newW  = (curW - delta.dx).clamp(minBodyW, maxBodyW);
              newFH = (curFH - delta.dy).clamp(minFH, maxFH);
              odx   = -(newW - curW);
              ody   = -(newFH - curFH);
          }
          _offset = (_offset ?? offset) + Offset(odx, ody);
          _size   = Size(newW, (newFH - headerH).clamp(minBodyH, maxBodyH));
        }),
        onResizeEnd: () {
          setState(() => _interacting = false);
          sendBoundsToJava();
        },
        child: column,
      ),
    );

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
          if (!_focusScopeNode.hasFocus) _focusScopeNode.requestFocus();
        },
        child: dialog,
      ),
    );

    // Only a shell SWT activated may take the keyboard. `Shell.setVisible(true)` does not activate:
    // a JFace `SWT.ON_TOP` popup (content assist) is shown that way precisely so the control that
    // opened it keeps focus and can drive the popup from its own VerifyKeyListener.
    if (!_autoFocusRequested && scope.activeShellId == state.id) {
      _autoFocusRequested = true;
      WidgetsBinding.instance.addPostFrameCallback((_) {
        if (mounted) _focusScopeNode.nextFocus();
      });
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
      child: pointerInterceptor(RegionClip.maybe(state.region, opacityWrapped)),
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
        child: AnimatedContainer(
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

class _ResizableWrapper extends StatelessWidget {
  final bool enabled;
  final double width, height;
  final VoidCallback? onResizeStart;
  final void Function(Offset delta, _ResizeEdge edge) onResize;
  final VoidCallback? onResizeEnd;
  final Widget child;

  static const double _h = 8.0;

  const _ResizableWrapper({
    this.enabled = true,
    required this.width,
    required this.height,
    this.onResizeStart,
    required this.onResize,
    this.onResizeEnd,
    required this.child,
  });

  void _bottom(DragUpdateDetails d) => onResize(d.delta, _ResizeEdge.bottom);
  void _top(DragUpdateDetails d)    => onResize(d.delta, _ResizeEdge.top);
  void _right(DragUpdateDetails d)  => onResize(d.delta, _ResizeEdge.right);
  void _left(DragUpdateDetails d)   => onResize(d.delta, _ResizeEdge.left);
  void _se(DragUpdateDetails d)     => onResize(d.delta, _ResizeEdge.se);
  void _sw(DragUpdateDetails d)     => onResize(d.delta, _ResizeEdge.sw);
  void _ne(DragUpdateDetails d)     => onResize(d.delta, _ResizeEdge.ne);
  void _nw(DragUpdateDetails d)     => onResize(d.delta, _ResizeEdge.nw);

  Widget _handle(
    MouseCursor cursor,
    void Function(DragUpdateDetails) onPan, {
    double? top,
    double? bottom,
    double? left,
    double? right,
    double? w,
    double? h,
  }) =>
      Positioned(
        top: top,
        bottom: bottom,
        left: left,
        right: right,
        width: w,
        height: h,
        child: MouseRegion(
          cursor: cursor,
          child: GestureDetector(
            behavior: HitTestBehavior.opaque,
            onPanStart: (_) => onResizeStart?.call(),
            onPanUpdate: onPan,
            onPanEnd: (_) => onResizeEnd?.call(),
            onPanCancel: onResizeEnd,
          ),
        ),
      );

  @override
  Widget build(BuildContext context) => Stack(
        clipBehavior: Clip.none,
        children: [
          child,
          if (enabled) ...[
            _handle(SystemMouseCursors.resizeUpDown, _top,
                top: 0, left: 0, right: 0, h: _h),
            _handle(SystemMouseCursors.resizeUpDown, _bottom,
                bottom: 0, left: _h, right: _h, h: _h),
            _handle(SystemMouseCursors.resizeLeftRight, _left,
                left: 0, top: 0, bottom: 0, w: _h),
            _handle(SystemMouseCursors.resizeLeftRight, _right,
                right: 0, top: _h, bottom: _h, w: _h),
            _handle(SystemMouseCursors.resizeUpLeftDownRight, _nw,
                left: 0, top: 0, w: _h * 2, h: _h * 2),
            _handle(SystemMouseCursors.resizeUpRightDownLeft, _ne,
                right: 0, top: 0, w: _h * 2, h: _h * 2),
            _handle(SystemMouseCursors.resizeUpRightDownLeft, _sw,
                left: 0, bottom: 0, w: _h * 2, h: _h * 2),
            _handle(SystemMouseCursors.resizeUpLeftDownRight, _se,
                right: 0, bottom: 0, w: _h * 2, h: _h * 2),
          ],
        ],
      );
}

enum _ResizeEdge { top, bottom, left, right, nw, ne, sw, se }
