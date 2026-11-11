import 'package:flutter/gestures.dart' show kPrimaryButton;
import 'package:flutter/material.dart';
import '../gen/event.dart';
import '../gen/sash.dart';
import '../gen/swt.dart';
import '../gen/widget.dart';
import '../impl/control_evolve.dart';
import '../impl/composite_evolve.dart';
import '../impl/utils/hover_arbiter.dart';
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

  /// The positions the drag's moves reported: an answer that brings back one of them, other than
  /// where the drag was released, is Java catching up with an earlier move.
  final Set<int> _movedTo = {};

  /// Whether the release has been reported and Java's answer to it has not arrived yet.
  bool _awaitingAnswer = false;

  static int _position(int x, int y) => x * 65536 + y;

  /// Draws the sash where the drag left it until Java answers the release (SashHelper always does),
  /// so the pointer that let go of it is still on it when the hover settles.
  @override
  void extraSetState() {
    super.extraSetState();
    final bounds = state.bounds, released = _releasedAt;
    if (!_awaitingAnswer || bounds == null || released == null) return;
    final atRelease = bounds.x == released.dx.round() && bounds.y == released.dy.round();
    if (!atRelease && _movedTo.contains(_position(bounds.x!, bounds.y!))) return;
    _awaitingAnswer = false;
    _localX = null;
    _localY = null;
    _answered();
  }

  /// Releases the hover the release holds, once Java has answered it.
  void Function()? _releaseHover;

  void _answered() {
    _releaseHover?.call();
    _releaseHover = null;
  }

  @override
  void dispose() {
    _answered();
    super.dispose();
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
        // reached first. Each move of the drag in between reports its own here too, ahead of its
        // MouseMove: a gesture recognizer would hear the move only after the control's Listener.
        // The second press of a double click reports neither, as on Win32 and Cocoa.
        child: Listener(
          onPointerDown: enabled
              ? (e) {
                  _primaryPress = e.buttons == kPrimaryButton;
                  if (!_primaryPress) return;
                  _releasedAt = null;
                  _dragPointer = e.pointer;
                  final at = state.bounds;
                  if (at != null) {
                    _parentOrigin = logicalToSwt(e.position) - e.localPosition - Offset(at.x!.toDouble(), at.y!.toDouble());
                  }
                  if (!continuesClickChain(e)) _sendPositionSelection(pressed: true);
                  _isDragging = true;
                  handleSelectionStart(e.position);
                }
              : null,
          onPointerMove: enabled
              ? (e) {
                  if (_isDragging && e.buttons & kPrimaryButton != 0) handleSelectionDrag(e.position);
                }
              : null,
          onPointerUp: enabled
              ? (_) {
                  if (_primaryPress && swtClickCount == 1) {
                    // Java's answer lays out the sash and the controls beside it where the drag
                    // left them; until then a sibling may still cover the pointer.
                    _answered();
                    if (state.bounds != null) _releaseHover = HoverExclusivityArbiter.instance.holdSettle();
                    _sendPositionSelection(pressed: false);
                  }
                  _primaryPress = false;
                  _endDrag();
                }
              : null,
          onPointerCancel: enabled
              ? (_) {
                  _primaryPress = false;
                  _endDrag();
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
    );

    if (_localX != null) {
      lastAppliedX = _localX;
      lastAppliedY = _localY;
    }

    // The parent places the sash at the bounds Java last sent; a drag draws it where the pointer
    // took it until Java's bounds catch up, so it stays under the pointer as a native one does.
    // Always mounted: a wrapper added mid-drag would remount the gesture that is moving it.
    var drift = Offset.zero;
    if (bounds != null && _localX != null && _localY != null) {
      drift = Offset((_localX! - bounds.x!).toDouble(), (_localY! - bounds.y!).toDouble());
    }

    return tagSemantics(Transform.translate(offset: drift, child: trackPointer(sashContent)));
  }

  /// The parent's origin on the view, in SWT units, taken at the press.
  Offset? _parentOrigin;

  /// Where the drag last reported the sash when the pointer let go, which its MouseUp lies on.
  Offset? _releasedAt;

  /// The pointer of the primary press the two above were taken for.
  int? _dragPointer;

  /// Relative to where the drag last reported the sash, read off the pointer rather than the
  /// layout, which lags it. Java moves a MouseUp onto where its listeners kept the sash
  /// (SashHelper.placeRelease). Any other pointer is the control's as usual.
  @override
  Offset heldLocal(PointerEvent e) {
    final origin = _parentOrigin, bounds = state.bounds;
    if (origin == null || bounds == null || e.pointer != _dragPointer) return super.heldLocal(e);
    final at = _localX != null && _localY != null
        ? Offset(_localX!.toDouble(), _localY!.toDouble())
        : _releasedAt ?? Offset(bounds.x!.toDouble(), bounds.y!.toDouble());
    return logicalToSwt(e.position) - origin - at;
  }

  void _enterLocalEditMode() {
    final bounds = state.bounds;
    if (bounds != null) {
      _isInLocalEditMode = true;
      _awaitingAnswer = false;
      _movedTo.clear();
      _localX = bounds.x;
      _localY = bounds.y;
    }
  }

  /// Once the pointer lets go, where the sash ends is Java's to say: its listeners may have kept it
  /// elsewhere than the drag took it.
  void _endDrag() {
    _isDragging = false;
    _isInLocalEditMode = false;
    if (_localX == null || _localY == null) return;
    _releasedAt = Offset(_localX!.toDouble(), _localY!.toDouble());
    lastAppliedX = _localX;
    lastAppliedY = _localY;
    _awaitingAnswer = true;
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
        ..stateMask = pressed ? SWT.BUTTON1 : 0
        // The release says it is one: Java reports it where its listeners kept the sash (SashHelper).
        ..button = pressed ? 0 : 1,
    );
  }

  void handleSelectionStart(Offset globalPosition) {
    final bounds = state.bounds;
    if (bounds != null) {
      _enterLocalEditMode();

      _lastX = _localX ?? bounds.x;
      _lastY = _localY ?? bounds.y;

      // The pointer is read against the view and the bounds are SWT units.
      final pointer = logicalToSwt(globalPosition);
      _startX = pointer.dx.round() - _lastX;
      _startY = pointer.dy.round() - _lastY;
    }
  }

  void handleSelectionDrag(Offset globalPosition) {
    final isHorizontal = hasStyle(SWT.HORIZONTAL);

    final bounds = state.bounds;
    if (bounds == null) return;

    final width = bounds.width;
    final height = bounds.height;

    int newX = _lastX;
    int newY = _lastY;
    final pointer = logicalToSwt(globalPosition);

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
    _movedTo.add(_position(newX, newY));

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

  void handleFocusIn() {
    widget.sendFocusFocusIn(state, null);
  }

  void handleFocusOut() {
    widget.sendFocusFocusOut(state, null);
  }
}
