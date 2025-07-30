import 'package:fluent_ui/fluent_ui.dart';
import 'package:swtflutter/src/swt/swt.dart';

import '../impl/control_impl.dart';
import '../styles.dart';
import '../swt/scale.dart';

class ScaleImpl<T extends ScaleSwt, V extends ScaleValue>
    extends ControlImpl<T, V> {
  @override
  Widget build(BuildContext context) {
    final min = state.minimum ?? 0;
    final max = state.maximum ?? 100;
    final step = state.increment ?? 1;
    final divs = (max - min + 1) / step;
    final scale = Slider(
      value: (state.selection ?? 0).toDouble(),
      min: min.toDouble(),
      max: max.toDouble(),
      divisions: divs.toInt(),
      label: (state.selection ?? 0).toString(),
      vertical: StyleBits(state.style).has(SWT.VERTICAL),
      onChanged: state.enabled ?? true
          ? (v) {
              if (v.toInt() != state.selection) {
                setState(() => state.selection = v.toInt());
                widget.sendSelectionSelection(state, v.toInt());
              }
            }
          : null,
    );
    return super.wrap(scale);
  }
}
