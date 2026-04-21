import 'dart:convert';
import 'package:flutter/material.dart';
import '../gen/display.dart';
import '../gen/shell.dart';
import '../gen/swt.dart';
import '../gen/widgets.dart' as gen;
import '../comm/comm.dart';
import '../theme/theme_extensions/display_theme_extension.dart';


class DisplaySwt extends StatefulWidget {
  final VDisplay value;

  const DisplaySwt({Key? key, required this.value}) : super(key: key);

  @override
  State<DisplaySwt> createState() => _DisplaySwtState();
}

class _DisplaySwtState extends State<DisplaySwt> {
  late VDisplay _display;

  @override
  void initState() {
    super.initState();
    _display = widget.value;
    EquoCommService.onRaw('Display/${widget.value.id}', _onUpdate);
  }

  void _onUpdate(dynamic raw) {
    print('DisplaySwt update');
    try {
      final map = jsonDecode(raw as String) as Map<String, dynamic>;
      final updated = VDisplay.fromJson(map);
      if (mounted) setState(() => _display = updated);
    } catch (e) {
      print('DisplaySwt update error: $e');
    }
  }

  bool _isMainShell(VShell s, BoxConstraints constraints) {
    final b = s.bounds;
    if (b == null || b.x != 0 || b.y != 0) return false;
    if (b.width == 1024 && b.height == 768) return true; // eclipse workbench default
    return b.width == constraints.maxWidth.toInt() && b.height == constraints.maxHeight.toInt();
  }

  static bool _isModal(VShell s) {
    final style = s.style;
    return (style & SWT.APPLICATION_MODAL) != 0 ||
        (style & SWT.SYSTEM_MODAL) != 0 ||
        (style & SWT.PRIMARY_MODAL) != 0;
  }

  @override
  Widget build(BuildContext context) {
    final shells = _display.shells ?? [];
    print("Dart Display.build shells: ${shells.length}");
    if (shells.isEmpty) return const SizedBox.shrink();

    return LayoutBuilder(builder: (context, constraints) {
      final mainShells = <VShell>[];
      final dialogShells = <VShell>[];
      for (var s in shells) {
        print("Shell text: ${s.text} ${s.bounds?.x},${s.bounds?.y},${s.bounds?.width},${s.bounds?.height}");
        if (_isMainShell(s, constraints)) {
          mainShells.add(s);
        } else {
          dialogShells.add(s);
        }
      }
      for (var s in mainShells) {
        s.bounds!.x = 0;
        s.bounds!.y = 0;
        s.bounds!.width = constraints.maxWidth.toInt();
        s.bounds!.height = constraints.maxHeight.toInt();
      }

      return Stack(children: [
        for (final s in mainShells) Positioned.fill(child: gen.mapWidgetFromValue(s)),
        if (dialogShells.any(_isModal))
          Positioned.fill(
            child: ColoredBox(
              color: Theme.of(context).extension<DisplayThemeExtension>()!.modalOverlayColor,
            ),
          ),
        for (final dialog in dialogShells) _DialogShell(shell: dialog, constraints: constraints),
      ]);
    });
  }
}

class _DialogShell extends StatefulWidget {
  final VShell shell;
  final BoxConstraints constraints;
  const _DialogShell({required this.shell, required this.constraints});

  @override
  State<_DialogShell> createState() => _DialogShellState();
}

class _DialogShellState extends State<_DialogShell> {
  Offset? _offset;
  Size? _size;
  bool _minimized = false;
  bool _maximized = false;

  int get _style     => widget.shell.style;
  bool get _hasTitle  => (_style & SWT.TITLE) != 0;
  bool get _hasClose  => (_style & SWT.CLOSE) != 0;
  bool get _hasMin    => (_style & SWT.MIN) != 0;
  bool get _hasMax    => (_style & SWT.MAX) != 0;
  bool get _hasResize => (_style & SWT.RESIZE) != 0;
  bool get _noTrim    => (_style & SWT.NO_TRIM) != 0;
  bool get _noMove    => (_style & SWT.NO_MOVE) != 0;
  bool get _hasBorder => (_style & SWT.BORDER) != 0;
  bool get _isTool    => (_style & SWT.TOOL) != 0;
  bool get _isSheet   => (_style & SWT.SHEET) != 0;

  bool get _showTitleBar => !_noTrim && (_hasTitle || _hasClose);
  bool get _showBorder   => !_noTrim && (_hasBorder || _showTitleBar);
  bool get _isDraggable  => _showTitleBar && !_noMove && !_isSheet && !_maximized && widget.shell.fullScreen != true;

  double get _baseW {
    if (_size != null) return _size!.width;
    final raw = (widget.shell.bounds?.width ?? 400).toDouble();
    return raw.clamp(200.0, widget.constraints.maxWidth * 0.9);
  }

  double get _baseH {
    if (_size != null) return _size!.height;
    final raw = (widget.shell.bounds?.height ?? 300).toDouble();
    return raw.clamp(150.0, widget.constraints.maxHeight * 0.9);
  }

  Offset _initialOffset(double w, double h) {
    final b = widget.shell.bounds;
    if (b != null && (b.x != 0 || b.y != 0)) return Offset(b.x.toDouble(), b.y.toDouble());
    return Offset((widget.constraints.maxWidth - w) / 2, (widget.constraints.maxHeight - h) / 2);
  }

  void _onMinimize() {
    final willMinimize = !_minimized;
    setState(() => _minimized = willMinimize);
    EquoCommService.send('Shell/${widget.shell.id}/Shell/${willMinimize ? 'Iconify' : 'Deiconify'}');
  }

  void _onMaximize() => setState(() {
    _maximized = !_maximized;
    if (_maximized) _minimized = false;
  });

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context).extension<DisplayThemeExtension>()!;
    final isFullScreen = widget.shell.fullScreen == true;
    final w = (isFullScreen || _maximized) ? widget.constraints.maxWidth : _baseW;
    final h = (isFullScreen || _maximized) ? widget.constraints.maxHeight : _baseH;
    final offset = (isFullScreen || _maximized) ? Offset.zero : (_offset ?? _initialOffset(w, h));
    final opacity = widget.shell.alpha != null ? (widget.shell.alpha! / 255.0).clamp(0.0, 1.0) : 1.0;
    final title = (widget.shell.modified == true ? '• ' : '') + (widget.shell.text ?? '');
    final borderRadius = _showBorder ? theme.dialogBorderRadius : 0.0;
    final titleBarH = _isTool ? theme.toolWindowTitleBarHeight : theme.titleBarHeight;

    Widget body = SizedBox(
      width: w,
      height: _minimized ? 0 : h,
      child: _minimized ? null : gen.mapWidgetFromValue(widget.shell),
    );

    if (!_minimized && _hasResize && !_maximized && !isFullScreen) {
      body = _ResizableWrapper(
        width: w, height: h,
        minWidth: (widget.shell.minimumSize?.x ?? 100).toDouble(),
        minHeight: (widget.shell.minimumSize?.y ?? 80).toDouble(),
        maxWidth: widget.shell.maximumSize?.x.toDouble() ?? widget.constraints.maxWidth * 0.95,
        maxHeight: widget.shell.maximumSize?.y.toDouble() ?? widget.constraints.maxHeight * 0.95,
        onResize: (s) => setState(() => _size = s),
        child: body,
      );
    }

    Widget dialog = Container(
      clipBehavior: _showBorder ? Clip.antiAlias : Clip.none,
      decoration: BoxDecoration(
        color: theme.dialogBackgroundColor,
        borderRadius: BorderRadius.circular(borderRadius),
        border: _showBorder
            ? Border.all(color: theme.dialogBorderColor, width: theme.dialogBorderWidth)
            : null,
        boxShadow: _showBorder
            ? [BoxShadow(color: theme.dialogShadowColor, blurRadius: theme.dialogShadowBlurRadius, spreadRadius: theme.dialogShadowSpreadRadius, offset: Offset(theme.dialogShadowOffsetX, theme.dialogShadowOffsetY))]
            : null,
      ),
      child: Column(
        mainAxisSize: MainAxisSize.min,
        children: [
          if (_showTitleBar)
            SizedBox(
              width: w,
              child: _TitleBar(
                title: title,
                isTool: _isTool,
                hasClose: _hasClose, hasMin: _hasMin, hasMax: _hasMax,
                isMinimized: _minimized, isMaximized: _maximized,
                isDraggable: _isDraggable,
                height: titleBarH, borderRadius: borderRadius,
                theme: theme,
                onDrag: (delta) => setState(() => _offset = (_offset ?? _initialOffset(w, h)) + delta),
                onClose: () => EquoCommService.send('Shell/${widget.shell.id}/Shell/Close'),
                onMinimize: _onMinimize,
                onMaximize: _onMaximize,
              ),
            ),
          body,
        ],
      ),
    );

    if (opacity < 1.0) dialog = Opacity(opacity: opacity, child: dialog);

    return Positioned(left: offset.dx, top: offset.dy, child: dialog);
  }
}

class _TitleBar extends StatelessWidget {
  final String title;
  final bool isTool, hasClose, hasMin, hasMax, isMinimized, isMaximized, isDraggable;
  final double height, borderRadius;
  final DisplayThemeExtension theme;
  final void Function(Offset) onDrag;
  final VoidCallback onClose, onMinimize, onMaximize;

  const _TitleBar({
    required this.title,
    required this.isTool,
    required this.hasClose, required this.hasMin, required this.hasMax,
    required this.isMinimized, required this.isMaximized, required this.isDraggable,
    required this.height, required this.borderRadius,
    required this.theme,
    required this.onDrag, required this.onClose, required this.onMinimize, required this.onMaximize,
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
              iconSize: btnSize, onTap: onMinimize,
            ),
          if (hasMax && !isTool)
            _TitleBarButton(
              icon: isMaximized ? Icons.close_fullscreen : Icons.crop_square,
              iconColor: theme.maximizeButtonColor,
              hoverBgColor: theme.maximizeButtonHoverColor,
              iconSize: btnSize, onTap: onMaximize,
            ),
          if (hasClose)
            _TitleBarButton(
              icon: Icons.close,
              iconColor: theme.closeButtonColor,
              hoverBgColor: theme.closeButtonHoverColor,
              hoverIconColor: theme.closeButtonHoverIconColor,
              iconSize: btnSize, onTap: onClose,
            ),
        ],
      ),
    );

    return isDraggable
        ? GestureDetector(onPanUpdate: (d) => onDrag(d.delta), child: bar)
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
    required this.iconColor, required this.hoverBgColor,
    this.hoverIconColor,
    required this.iconSize, required this.onTap,
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
          width: theme.titleBarButtonSize, height: theme.titleBarButtonSize,
          decoration: BoxDecoration(
            color: _hovering ? widget.hoverBgColor : Colors.transparent,
            borderRadius: BorderRadius.circular(theme.titleBarButtonBorderRadius),
          ),
          child: Icon(
            widget.icon,
            size: widget.iconSize,
            color: _hovering && widget.hoverIconColor != null ? widget.hoverIconColor : widget.iconColor,
          ),
        ),
      ),
    );
  }
}

class _ResizableWrapper extends StatelessWidget {
  final double width, height, minWidth, minHeight, maxWidth, maxHeight;
  final void Function(Size) onResize;
  final Widget child;

  static const double _h = 6.0;

  const _ResizableWrapper({
    required this.width, required this.height,
    required this.minWidth, required this.minHeight,
    required this.maxWidth, required this.maxHeight,
    required this.onResize, required this.child,
  });

  void _delta(double dx, double dy) => onResize(Size(
    (width + dx).clamp(minWidth, maxWidth),
    (height + dy).clamp(minHeight, maxHeight),
  ));

  Widget _handle(MouseCursor cursor, void Function(DragUpdateDetails) onPan, {
    double? top, double? bottom, double? left, double? right, double? w, double? h,
  }) =>
      Positioned(
        top: top, bottom: bottom, left: left, right: right, width: w, height: h,
        child: MouseRegion(cursor: cursor, child: GestureDetector(onPanUpdate: onPan)),
      );

  @override
  Widget build(BuildContext context) => Stack(
    clipBehavior: Clip.none,
    children: [
      child,
      _handle(SystemMouseCursors.resizeUpDown,           (d) => _delta(0, d.delta.dy),           bottom: 0, left: _h, right: _h, h: _h),
      _handle(SystemMouseCursors.resizeLeftRight,         (d) => _delta(d.delta.dx, 0),           right: 0, top: _h, bottom: _h, w: _h),
      _handle(SystemMouseCursors.resizeUpLeftDownRight,   (d) => _delta(d.delta.dx, d.delta.dy),  right: 0, bottom: 0, w: _h * 2, h: _h * 2),
    ],
  );
}
