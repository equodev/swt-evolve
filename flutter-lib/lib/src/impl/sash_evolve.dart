import 'package:flutter/material.dart';
import '../gen/event.dart';
import '../gen/sash.dart';
import '../gen/swt.dart';
import '../gen/widget.dart';
import '../impl/control_evolve.dart';
import '../impl/composite_evolve.dart';
import '../impl/utils/widget_utils.dart';
import '../theme/theme_extensions/sash_theme_extension.dart';
import 'package:swtflutter/src/styles.dart';
import 'package:swtflutter/src/impl/widget_config.dart';

class SashImpl<T extends SashSwt, V extends VSash> extends ControlImpl<T, V> {
  int? lastAppliedX;
  int? lastAppliedY;
  late final initialX = state.bounds?.x;
  late final initialY = state.bounds?.y;
  bool _isDragging = false;
  int _startX = 0;
  int _startY = 0;
  int _lastX = 0;
  int _lastY = 0;
  bool _isInLocalEditMode = false;
  int? _localX;
  int? _localY;
  Size? _parentDimensions;

  bool hasStyle(int style) => state.style & style != 0;

  @override
  void didUpdateWidget(covariant T oldWidget) {
    super.didUpdateWidget(oldWidget);

    if (state.bounds != null) {
      if (_localX != null && _localY != null) {
        final bool positionConfirmed = state.bounds!.x == _localX && state.bounds!.y == _localY;

        if (positionConfirmed && !_isInLocalEditMode) {
          setState(() {
            _localX = null;
            _localY = null;
            lastAppliedX = state.bounds!.x;
            lastAppliedY = state.bounds!.y;
          });
        }
      }
    }
  }

  @override
  void initState() {
    super.initState();

    if (lastAppliedX == null && state.bounds != null) {
      lastAppliedX = state.bounds!.x;
      lastAppliedY = state.bounds!.y;
    }
  }

  Size _getParentDimensions() {
    Size defaultSize = Size(
        (initialX != null) ? initialX! * 2.0 : 400.0,
        (initialY != null) ? initialY! * 2.0 : 300.0
    );

    try {
      final parentState = context.findAncestorStateOfType<CompositeImpl>();

      if (parentState != null) {
        final parentBounds = parentState.state.bounds;
        if (parentBounds != null) {
          final parentWidth = parentBounds.width?.toDouble() ?? defaultSize.width;
          final parentHeight = parentBounds.height?.toDouble() ?? defaultSize.height;
          return Size(parentWidth, parentHeight);
        }
      }
    } catch (e) {
      // Silent error handling
    }

    return defaultSize;
  }

  @override
  Widget build(BuildContext context) {
    final bounds = state.bounds;

    _parentDimensions = _getParentDimensions();

    if (lastAppliedX == null && bounds != null) {
      lastAppliedX = bounds.x;
      lastAppliedY = bounds.y;
    }

    final isHorizontal = hasStyle(SWT.HORIZONTAL);
    final enabled = state.enabled ?? true;

    final widgetTheme = Theme.of(context).extension<SashThemeExtension>()!;
    Color sashColor = getBackgroundColor(
      background: state.background,
      defaultColor: widgetTheme.backgroundColor,
    ) ?? widgetTheme.backgroundColor;

    final boundsWidth = bounds?.width?.toDouble() ?? 0;
    final boundsHeight = bounds?.height?.toDouble() ?? 0;
    final width = boundsWidth > 0 ? boundsWidth : widgetTheme.defaultSize;
    final height = boundsHeight > 0 ? boundsHeight : widgetTheme.defaultSize;

    Widget sashContent = MouseRegion(
      onEnter: (_) => handleMouseEnter(),
      onExit: (_) => handleMouseExit(),
      cursor: isHorizontal ? SystemMouseCursors.resizeRow : SystemMouseCursors.resizeColumn,
      child: Focus(
        onFocusChange: (hasFocus) {
          if (hasFocus) {
            handleFocusIn();
          } else {
            handleFocusOut();
          }
        },
        child: GestureDetector(
          onPanStart: enabled ? (details) {
            _isDragging = true;
            handleSelectionStart(details);
          } : null,
          onPanUpdate: enabled ? (details) {
            if (_isDragging) {
              handleSelectionDrag(details);
            }
          } : null,
          onPanEnd: enabled ? (details) {
            _isDragging = false;
            handleSelectionEnd(details);
          } : null,
          child: Container(
            width: width,
            height: height,
            constraints: BoxConstraints(
              maxHeight: height,
              maxWidth: width,
            ),
            color: sashColor,
          ),
        ),
      ),
    );

    if (_localX != null) {
      lastAppliedX = _localX;
      lastAppliedY = _localY;
    }

    return sashContent;
  }

  void _enterLocalEditMode() {
    final bounds = state.bounds;
    if (bounds != null) {
      _isInLocalEditMode = true;
      _localX = bounds.x;
      _localY = bounds.y;
    }
  }

  void _exitLocalEditMode() {
    if (_isInLocalEditMode && _localX != null && _localY != null) {
      final bounds = state.bounds;
      if (bounds != null) {
        final event = VEvent()
          ..x = _localX
          ..y = _localY
          ..width = bounds.width
          ..height = bounds.height
          ..detail = 0
          ..stateMask = 0;

        widget.sendSelectionSelection(state, event);

        _isInLocalEditMode = false;
        return;
      }
    }

    _isInLocalEditMode = false;
    _localX = null;
    _localY = null;
  }

  void handleSelectionStart(DragStartDetails details) {
    final bounds = state.bounds;
    if (bounds != null) {
      _enterLocalEditMode();

      _lastX = _localX ?? bounds.x ?? 0;
      _lastY = _localY ?? bounds.y ?? 0;

      _startX = details.globalPosition.dx.round() - _lastX;
      _startY = details.globalPosition.dy.round() - _lastY;
    }
  }

  void handleSelectionDrag(DragUpdateDetails details) {
    final isHorizontal = hasStyle(SWT.HORIZONTAL);

    final bounds = state.bounds;
    if (bounds == null) return;

    final width = bounds.width;
    final height = bounds.height;

    int newX = _lastX;
    int newY = _lastY;

    if (isHorizontal) {
      newY = details.globalPosition.dy.round() - _startY;

      if (newY < 0) {
        newY = 0;
      }

      final parentHeight = _parentDimensions?.height.round() ??
          ((initialY != null) ? initialY! * 2 : 0);

      final maxY = parentHeight - height;
      if (newY > maxY) {
        newY = maxY;
      }

    } else {
      newX = details.globalPosition.dx.round() - _startX;

      if (newX < 0) {
        newX = 0;
      }

      final parentWidth = _parentDimensions?.width.round() ??
          ((initialX != null) ? initialX! * 2 : 0);

      final maxX = parentWidth - width;
      if (newX > maxX) {
        newX = maxX;
      }
    }

    if (newX == _lastX && newY == _lastY) return;

    if (_isInLocalEditMode) {
      setState(() {
        _localX = newX;
        _localY = newY;
      });
    }

    _lastX = newX;
    _lastY = newY;
  }

  void handleSelectionEnd(DragEndDetails details) {
    if (_isInLocalEditMode && _localX != null && _localY != null) {
      lastAppliedX = _localX;
      lastAppliedY = _localY;

      _exitLocalEditMode();
    }
  }

  void handleMouseEnter() {
    widget.sendMouseTrackMouseEnter(state, null);
  }

  void handleMouseExit() {
    widget.sendMouseTrackMouseExit(state, null);
  }

  void handleFocusIn() {
    widget.sendFocusFocusIn(state, null);
  }

  void handleFocusOut() {
    widget.sendFocusFocusOut(state, null);
  }
}