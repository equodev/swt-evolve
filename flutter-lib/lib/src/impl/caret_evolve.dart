import 'dart:async';

import 'package:flutter/rendering.dart';
import 'package:flutter/widgets.dart';
import '../gen/caret.dart';
import '../gen/image.dart';
import '../gen/widget.dart';
import 'utils/image_utils.dart';

/// The caret of a Canvas, drawn over it at its bounds while it is visible and its display's focus
/// caret, blinking at its blink rate, as SWT's Canvas draws its caret. StyledText draws its own.
///
/// Like SWT's on every platform, the caret inverts what is under it: a bar of white, or its image,
/// drawn in difference mode, so it shows on any background.
class CaretImpl<T extends CaretSwt, V extends VCaret> extends WidgetSwtState<T, V> {
  Timer? _blink;
  bool _on = true;
  String? _blinkKey;

  bool get _shown => (state.visible ?? true) && (state.focused ?? false);

  @override
  void initState() {
    super.initState();
    _restartBlink();
  }

  @override
  void extraSetState() {
    super.extraSetState();
    _restartBlink();
  }

  /// A change of place, visibility, focus or rate shows the caret again and starts the blink over,
  /// as SWT does when the caret moves; a rebuild that changes none of them leaves the blink alone.
  void _restartBlink() {
    final b = state.bounds;
    final key = '${b?.x},${b?.y},${b?.width},${b?.height},$_shown,${state.blinkRate}';
    if (key == _blinkKey) return;
    _blinkKey = key;
    _blink?.cancel();
    _blink = null;
    _on = true;
    final rate = state.blinkRate ?? 0;
    if (!_shown || rate <= 0) return;
    _blink = Timer.periodic(Duration(milliseconds: rate), (_) {
      if (mounted) setState(() => _on = !_on);
    });
  }

  @override
  void dispose() {
    _blink?.cancel();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    final bounds = state.bounds;
    if (!_shown || !_on || bounds == null || bounds.width <= 0 || bounds.height <= 0) {
      return const SizedBox.shrink();
    }
    final width = bounds.width.toDouble();
    final height = bounds.height.toDouble();
    final image = state.image;
    return Stack(
      children: [
        Positioned(
          left: bounds.x.toDouble(),
          top: bounds.y.toDouble(),
          width: width,
          height: height,
          child: image != null ? InvertingCaret(child: _imageOf(image, width, height)) : const InvertingCaret(),
        ),
      ],
    );
  }
}

/// The caret's image, as the application's own bits: drawn straight onto the canvas, so the blend
/// reaches what is under it.
Widget _imageOf(VImage image, double width, double height) {
  final bytes = image.imageData?.data;
  if (bytes != null) {
    return Image.memory(
      ImageUtils.asBytes(bytes),
      width: width,
      height: height,
      fit: BoxFit.fill,
      gaplessPlayback: true,
    );
  }
  return ImageUtils.buildVImage(image, width: width, height: height, constraints: null, renderAsIcon: false) ??
      const SizedBox.shrink();
}

/// Draws [child] onto what is under it in difference mode, or, without a child, a white box: either
/// way, inverting what is under the caret.
class InvertingCaret extends SingleChildRenderObjectWidget {
  const InvertingCaret({super.key, super.child});

  @override
  RenderObject createRenderObject(BuildContext context) => _RenderInvertingCaret();
}

class _RenderInvertingCaret extends RenderProxyBox {
  static final Paint _difference = Paint()..blendMode = BlendMode.difference;
  static final Paint _white = Paint()
    ..blendMode = BlendMode.difference
    ..color = const Color(0xFFFFFFFF);

  @override
  void paint(PaintingContext context, Offset offset) {
    final rect = offset & size;
    if (child == null) {
      context.canvas.drawRect(rect, _white);
      return;
    }
    // A layer of the child's own would leave the blend; such an image is drawn as it is.
    if (child!.needsCompositing) {
      super.paint(context, offset);
      return;
    }
    context.canvas.saveLayer(rect, _difference);
    super.paint(context, offset);
    context.canvas.restore();
  }
}
