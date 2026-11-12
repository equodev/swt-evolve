import 'package:flutter/rendering.dart';
import 'package:flutter/scheduler.dart';
import 'package:flutter/widgets.dart';

/// A layer of its own only while what it holds repaints frame after frame (a ripple, a focus
/// transition, a moving thumb), so each of those frames redraws the control instead of its parent's
/// layer and every control in it. A control that changes once paints into its parent's layer, so a
/// window of controls is not composited one layer each. It must be the control's outermost box: a
/// relayout inside the control repaints from there.
class LiveLayer extends SingleChildRenderObjectWidget {
  const LiveLayer({super.key, super.child});

  @override
  RenderObject createRenderObject(BuildContext context) => _RenderLiveLayer();
}

class _RenderLiveLayer extends RenderProxyBox {
  /// Consecutive frames repainted before the control gets a layer.
  static const int _framesToPromote = 3;

  /// More controls than this repainting in one frame is an update to many of them, which their
  /// parent's layer draws more cheaply than a layer each: none of them counts that frame.
  static const int _manyInOneFrame = 8;

  /// How long a layer outlives the last frame that repainted it.
  static const Duration _idleToDemote = Duration(seconds: 1);

  /// Frames drawn so far, counted once the first live layer is made.
  static int _frame = 0;
  static bool _counting = false;

  /// The layers marked for paint in the frame under way.
  static final List<_RenderLiveLayer> _marked = [];

  static void _countFrames() {
    if (_counting) return;
    _counting = true;
    SchedulerBinding.instance.addPersistentFrameCallback((_) => _endFrame());
  }

  static void _endFrame() {
    final many = _marked.length > _manyInOneFrame;
    for (final layer in _marked) {
      layer._counted(many);
    }
    _marked.clear();
    _frame++;
  }

  bool _live = false;
  int _streak = 0;
  int _markedIn = -1;
  int _lastCounted = -2;
  Duration _lastStamp = Duration.zero;

  @override
  bool get isRepaintBoundary => _live;

  /// Settles the frame this layer repainted in, once it is known how many others did.
  void _counted(bool many) {
    _streak = many ? 0 : (_lastCounted == _frame - 1 ? _streak + 1 : 1);
    _lastCounted = many ? -2 : _frame;
    if (!_live && _streak >= _framesToPromote && attached) {
      _live = true;
      markNeedsCompositingBitsUpdate();
      // Flutter repaints a new boundary only when its compositing changes; one whose child already
      // composited has to be painted into its new layer the next time its parent paints.
      super.markNeedsPaint();
    }
  }

  @override
  void markNeedsPaint() {
    _countFrames();
    if (_markedIn != _frame) {
      _markedIn = _frame;
      _marked.add(this);
      final stamp = SchedulerBinding.instance.currentSystemFrameTimeStamp;
      if (_live && stamp - _lastStamp > _idleToDemote) {
        _live = false;
        _streak = 0;
        markNeedsCompositingBitsUpdate();
      }
      _lastStamp = stamp;
    }
    super.markNeedsPaint();
  }
}
