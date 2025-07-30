import 'dart:math' as math show min, max;

import 'package:fluent_ui/fluent_ui.dart';

import '../impl/control_impl.dart';
import '../styles.dart';
import '../swt/progressbar.dart';
import '../swt/swt.dart';

class ProgressBarImpl<T extends ProgressBarSwt, V extends ProgressBarValue>
    extends ControlImpl<T, V> {
  @override
  Widget build(BuildContext context) {
    final style = StyleBits(state.style);
    Widget bar = ProgressBar(
        value: style.has(SWT.INDETERMINATE)
            ? null
            : mapSelectedValue(state.minimum ?? 0, state.maximum ?? 100,
                state.selection ?? 0));
    if (style.has(SWT.VERTICAL)) {
      bar = RotatedBox(
          quarterTurns: 1, // Rotate 90 degrees clockwise
          child: bar);
    }
    return wrap(bar);
  }

  double mapSelectedValue(int min, int max, int selected) {
    selected = math.max(min, selected);
    selected = math.min(max, selected);
    return ((selected - min) * 100) / (max - min);
  }
}
