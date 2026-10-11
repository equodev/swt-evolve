import 'dart:async';

import 'package:flutter/material.dart';
import '../gen/caret.dart';
import '../gen/widget.dart';
import '../theme/theme_extensions/canvas_theme_extension.dart';

/// The caret of a Canvas, drawn over it at its bounds while it is visible and its display's focus
/// caret, blinking at its blink rate, as SWT's Canvas draws its caret. StyledText draws its own.
class CaretImpl<T extends CaretSwt, V extends VCaret>
    extends WidgetSwtState<T, V> {
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
    final color = Theme.of(context).extension<CanvasThemeExtension>()!.foregroundColor;
    return Stack(children: [
      Positioned(
        left: bounds.x.toDouble(),
        top: bounds.y.toDouble(),
        width: bounds.width.toDouble(),
        height: bounds.height.toDouble(),
        child: ColoredBox(color: color),
      ),
    ]);
  }
}
