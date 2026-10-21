import 'package:flutter/gestures.dart' show kPrimaryButton;
import 'package:flutter/material.dart';
import '../gen/event.dart';
import '../gen/sash.dart';
import '../gen/swt.dart';
import '../gen/widget.dart';
import '../impl/control_evolve.dart';
import '../impl/composite_evolve.dart';
import '../impl/utils/swt_zoom_scale.dart';
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
        final bool positionConfirmed =
            state.bounds!.x == _localX && state.bounds!.y == _localY;

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
      (initialY != null) ? initialY! * 2.0 : 300.0,
    );

    try {
      final parentState = context.findAncestorStateOfType<CompositeImpl>();

      if (parentState != null) {
        final parentBounds = parentState.state.bounds;
        if (parentBounds != null) {
          final parentWidth =
              parentBounds.width?.toDouble() ?? defaultSize.width;
          final parentHeight =
              parentBounds.height?.toDouble() ?? defaultSize.height;
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
    Color sashColor =
        getBackgroundColor(
          background: state.background,
          defaultColor: widgetTheme.backgroundColor,
        ) ??
        widgetTheme.backgroundColor;

    final boundsWidth = bounds?.width?.toDouble() ?? 0;
    final boundsHeight = bounds?.height?.toDouble() ?? 0;
    final width = boundsWidth > 0 ? boundsWidth : widgetTheme.defaultSize;
    final height = boundsHeight > 0 ? boundsHeight : widgetTheme.defaultSize;

    Widget sashContent = MouseRegion(
      cursor: isHorizontal
          ? SystemMouseCursors.resizeRow
          : SystemMouseCursors.resizeColumn,
      child: Focus(
        // Not a tab stop: Tab passes over it, as on every native; focus can still be set.
        skipTraversal: true,
        onFocusChange: (hasFocus) {
          if (hasFocus) {
            handleFocusIn();
          } else {
            handleFocusOut();
          }
        },
        // A press and a release each report the sash's position, ahead of their MouseDown and
        // MouseUp, as a native Sash does; this Listener sits inside the control's own, so it is
        // reached first. The drag in between reports its moves from the pan below. The second
        // press of a double click reports neither, as on Win32 and Cocoa.
        child: Listener(
          onPointerDown: enabled
              ? (e) {
                  _primaryPress = e.buttons == kPrimaryButton;
                  if (_primaryPress && !continuesClickChain(e)) {
                    _sendPositionSelection(pressed: true);
                  }
                }
              : null,
          onPointerUp: enabled
              ? (_) {
                  if (_primaryPress && swtClickCount == 1) _sendPositionSelection(pressed: false);
                  _primaryPress = false;
                }
              : null,
          child: GestureDetector(
          onPanStart: enabled
              ? (details) {
                  _isDragging = true;
                  handleSelectionStart(details);
                }
              : null,
          onPanUpdate: enabled
              ? (details) {
                  if (_isDragging) {
                    handleSelectionDrag(details);
                  }
                }
              : null,
          onPanEnd: enabled
              ? (details) {
                  _isDragging = false;
                  handleSelectionEnd(details);
                }
              : null,
          child: Container(
            width: width,
            height: height,
            constraints: BoxConstraints(maxHeight: height, maxWidth: width),
            color: sashColor,
          ),
        ),
        ),
      ),
    );

    if (_localX != null) {
      lastAppliedX = _localX;
      lastAppliedY = _localY;
    }

    return tagSemantics(trackPointer(sashContent));
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
    // The release already reported where the drag ended (_sendPositionSelection); the local
    // position stays until Java's bounds catch up.
    if (_isInLocalEditMode && _localX != null && _localY != null && state.bounds != null) {
      _isInLocalEditMode = false;
      return;
    }

    _isInLocalEditMode = false;
    _localX = null;
    _localY = null;
  }

  /// Whether the press in flight is the primary button's: only that one moves a sash.
  bool _primaryPress = false;

  /// The Selection a press or a release reports: where the sash is now, with the button still
  /// held on the press.
  void _sendPositionSelection({required bool pressed}) {
    final bounds = state.bounds;
    if (bounds == null) return;
    widget.sendSelectionSelection(
      state,
      VEvent()
        ..x = _localX ?? bounds.x
        ..y = _localY ?? bounds.y
        ..width = bounds.width
        ..height = bounds.height
        ..detail = 0
        ..stateMask = pressed ? SWT.BUTTON1 : 0,
    );
  }

  void handleSelectionStart(DragStartDetails details) {
    final bounds = state.bounds;
    if (bounds != null) {
      _enterLocalEditMode();

      _lastX = _localX ?? bounds.x;
      _lastY = _localY ?? bounds.y;

      // The pointer is read against the view and the bounds are SWT units.
      final pointer = logicalToSwt(details.globalPosition);
      _startX = pointer.dx.round() - _lastX;
      _startY = pointer.dy.round() - _lastY;
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
    final pointer = logicalToSwt(details.globalPosition);

    if (isHorizontal) {
      newY = pointer.dy.round() - _startY;

      if (newY < 0) {
        newY = 0;
      }

      final parentHeight =
          _parentDimensions?.height.round() ??
          ((initialY != null) ? initialY! * 2 : 0);

      final maxY = parentHeight - height;
      if (newY > maxY) {
        newY = maxY;
      }
    } else {
      newX = pointer.dx.round() - _startX;

      if (newX < 0) {
        newX = 0;
      }

      final parentWidth =
          _parentDimensions?.width.round() ??
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

    // Native SWT fires Selection continuously while the sash is dragged
    // (SwtSash.mouseDragged) so apps can resize the surrounding composites
    // live — without this nothing relayouts until the drag ends.
    final event = VEvent()
      ..x = newX
      ..y = newY
      ..width = width
      ..height = height
      ..detail = 0
      ..stateMask = 0;
    widget.sendSelectionSelection(state, event);
  }

  void handleSelectionEnd(DragEndDetails details) {
    if (_isInLocalEditMode && _localX != null && _localY != null) {
      lastAppliedX = _localX;
      lastAppliedY = _localY;

      _exitLocalEditMode();
    }
  }

  void handleFocusIn() {
    widget.sendFocusFocusIn(state, null);
  }

  void handleFocusOut() {
    widget.sendFocusFocusOut(state, null);
  }
}
