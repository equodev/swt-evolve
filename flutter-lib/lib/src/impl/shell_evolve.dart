import 'package:flutter/material.dart';
import 'package:flutter/widgets.dart';

import '../gen/dialog.dart';
import '../gen/event.dart';
import '../gen/messagebox.dart';
import '../gen/shell.dart';
import '../gen/swt.dart';
import '../impl/color_utils.dart';
import '../impl/decorations_evolve.dart';
import '../impl/messagebox_evolve.dart';
import '../impl/utils/widget_utils.dart';
import '../theme/theme_extensions/display_theme_extension.dart';
import 'utils/pointer.dart';

class FloatingShellChromeScope extends InheritedWidget {
  final BoxConstraints viewportConstraints;

  const FloatingShellChromeScope({
    super.key,
    required this.viewportConstraints,
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
      viewportConstraints != oldWidget.viewportConstraints;
}

class ShellImpl<T extends ShellSwt, V extends VShell> extends DecorationsImpl<T, V> {
  Offset? _offset;
  Size? _size;
  bool _minimized = false;
  bool _maximized = false;
  bool _interacting = false;
  bool _initialBoundsSent = false;
  final Set<int> _openedDialogIds = {};

  @override
  void setValue(V value) {
    final prevDialogs = state.dialogs ?? [];
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
  }

  @override
  void didUpdateWidget(covariant T oldWidget) {
    super.didUpdateWidget(oldWidget);
    if (_interacting) return;
    _size = null;
  }

  void _openDialog(VDialog d) {
    switch (d.swt) {
      case 'MessageBox':
        showMessageBoxDialog(context, d as VMessageBox, d.id);
    }
  }

  int get _style => state.style;
  bool get _hasTitle => (_style & SWT.TITLE) != 0;
  bool get _hasClose => (_style & SWT.CLOSE) != 0;
  bool get _hasMin => (_style & SWT.MIN) != 0;
  bool get _hasMax => (_style & SWT.MAX) != 0;
  bool get _hasResize => (_style & SWT.RESIZE) != 0;
  bool get _noTrim => (_style & SWT.NO_TRIM) != 0;
  bool get _noMove => (_style & SWT.NO_MOVE) != 0;
  bool get _hasBorder => (_style & SWT.BORDER) != 0;
  bool get _isTool => (_style & SWT.TOOL) != 0;
  bool get _isSheet => (_style & SWT.SHEET) != 0;

  bool get _showTitleBar => !_noTrim && (_hasTitle || _hasClose);
  bool get _showBorder => !_noTrim && (_hasBorder || _showTitleBar);
  bool get _isDraggable =>
      _showTitleBar && !_noMove && !_isSheet && !_maximized && state.fullScreen != true;

  @override
  Widget build(BuildContext context) {
    final scope = FloatingShellChromeScope.maybeOf(context);
    if (scope == null) return super.build(context);

    final viewport = scope.viewportConstraints;
    final theme = Theme.of(context).extension<DisplayThemeExtension>()!;
    final isFullScreen = state.fullScreen == true;
    final b = state.bounds;

    final rawW = (b?.width ?? 400).toDouble();
    final rawH = (b?.height ?? 300).toDouble();
    final bodyW = _size != null
        ? _size!.width
        : (!_showTitleBar && !_showBorder ? rawW : rawW.clamp(200.0, viewport.maxWidth * 0.9));
    final bodyH = _size != null
        ? _size!.height
        : (!_showTitleBar && !_showBorder ? rawH : rawH.clamp(150.0, viewport.maxHeight * 0.9));

    final titleBarH = _isTool ? theme.toolWindowTitleBarHeight : theme.titleBarHeight;
    final headerH = _showTitleBar ? titleBarH : 0.0;

    final w = (isFullScreen || _maximized) ? viewport.maxWidth : bodyW;
    final h = (isFullScreen || _maximized) ? viewport.maxHeight : bodyH;
    final frameH = _minimized ? headerH : headerH + h;

    Offset resolvedOffset() {
      if (isFullScreen || _maximized) return Offset.zero;
      if (_offset != null) return _offset!;
      if (b != null && (b.x != 0 || b.y != 0)) {
        return Offset(b.x.toDouble(), b.y.toDouble());
      }
      return Offset(
        (viewport.maxWidth - bodyW) / 2,
        (viewport.maxHeight - headerH - bodyH) / 2,
      );
    }

    final offset = resolvedOffset();

    void sendBoundsToJava() {
      final pos = (isFullScreen || _maximized) ? Offset.zero : (_offset ?? offset);
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

    void onMinimize() {
      final willMinimize = !_minimized;
      setState(() => _minimized = willMinimize);
      if (willMinimize) {
        widget.sendShellIconify(state, null);
      } else {
        widget.sendShellDeiconify(state, null);
      }
    }

    void onMaximize() {
      setState(() {
        _maximized = !_maximized;
        if (_maximized) _minimized = false;
      });
      sendBoundsToJava();
    }

    final opacity = state.alpha != null ? (state.alpha! / 255.0).clamp(0.0, 1.0) : 1.0;
    final title = (state.modified == true ? '• ' : '') + (state.text ?? '');
    final borderRadius = _showBorder ? theme.dialogBorderRadius : 0.0;

    Widget body = ClipRect(
      child: SizedBox(
        width: w,
        height: _minimized ? 0 : h,
        child: _minimized ? null : super.build(context),
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
              hasMin: _hasMin,
              hasMax: _hasMax,
              isMinimized: _minimized,
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
              onMinimize: onMinimize,
              onMaximize: onMaximize,
            ),
          ),
        body,
      ],
    );

    Widget framed = SizedBox(width: w, height: frameH, child: column);

    if (!_minimized && _hasResize && !_maximized && !isFullScreen) {
      final minBodyH = (state.minimumSize?.y ?? 80).toDouble();
      final maxBodyH = state.maximumSize?.y.toDouble() ?? viewport.maxHeight * 0.95;
      final minBodyW = (state.minimumSize?.x ?? 100).toDouble();
      final maxBodyW = state.maximumSize?.x.toDouble() ?? viewport.maxWidth * 0.95;
      final minFH = minBodyH + headerH;
      final maxFH = maxBodyH + headerH;
      framed = SizedBox(
        width: w,
        height: frameH,
        child: _ResizableWrapper(
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
          child: framed,
        ),
      );
    }

    final shellBg = state.background != null
        ? colorFromVColor(state.background, defaultColor: theme.dialogBackgroundColor)
        : null;
    if (shellBg != null) {
      framed = ParentBackgroundScope(background: shellBg, child: framed);
    }

    Widget dialog = Container(
      clipBehavior: Clip.none,
      decoration: BoxDecoration(
        color: shellBg ?? theme.dialogBackgroundColor,
        borderRadius: BorderRadius.circular(borderRadius),
        border: _showBorder
            ? Border.all(color: theme.dialogBorderColor, width: theme.dialogBorderWidth)
            : null,
        boxShadow: _showBorder
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

    if (opacity < 1.0) dialog = Opacity(opacity: opacity, child: dialog);

    return Positioned(left: offset.dx, top: offset.dy, child: pointerInterceptor(dialog));
  }
}

class _TitleBar extends StatelessWidget {
  final String title;
  final bool isTool, hasClose, hasMin, hasMax, isMinimized, isMaximized, isDraggable;
  final double height, borderRadius;
  final DisplayThemeExtension theme;
  final VoidCallback? onDragStart;
  final void Function(Offset) onDrag;
  final VoidCallback? onDragEnd;
  final VoidCallback? onDragCancel;
  final VoidCallback onClose, onMinimize, onMaximize;

  const _TitleBar({
    required this.title,
    required this.isTool,
    required this.hasClose,
    required this.hasMin,
    required this.hasMax,
    required this.isMinimized,
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
    required this.onMinimize,
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
          if (hasMin && !isTool)
            _TitleBarButton(
              icon: isMinimized ? Icons.open_in_full : Icons.remove,
              iconColor: theme.minimizeButtonColor,
              hoverBgColor: theme.minimizeButtonHoverColor,
              iconSize: btnSize,
              onTap: onMinimize,
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

    return isDraggable
        ? GestureDetector(
            onPanStart: (_) => onDragStart?.call(),
            onPanUpdate: (d) => onDrag(d.delta),
            onPanEnd: (_) => onDragEnd?.call(),
            onPanCancel: onDragCancel ?? onDragEnd,
            child: bar,
          )
        : bar;
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
  final double width, height;
  final VoidCallback? onResizeStart;
  final void Function(Offset delta, _ResizeEdge edge) onResize;
  final VoidCallback? onResizeEnd;
  final Widget child;

  static const double _h = 8.0;

  const _ResizableWrapper({
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
      );
}

enum _ResizeEdge { top, bottom, left, right, nw, ne, sw, se }
