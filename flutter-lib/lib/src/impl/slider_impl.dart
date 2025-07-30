import 'package:fluent_ui/fluent_ui.dart';

import '../impl/control_impl.dart';
import '../styles.dart';
import '../swt/slider.dart';
import '../swt/swt.dart';

class SliderImpl<T extends SliderSwt, V extends SliderValue>
    extends ControlImpl<T, V> {
  @override
  Widget build(BuildContext context) {
    final theme = FluentTheme.of(context);
    final slider = Slider(
      value: (state.selection ?? 0).toDouble(),
      vertical: StyleBits(state.style).has(SWT.VERTICAL),
      max: (state.maximum ?? 100).toDouble(),
      min: (state.minimum ?? 0).toDouble(),
      style: SliderThemeData(
          inactiveColor: SliderThemeData.standard(theme).activeColor,
          thumbRadius: ButtonState.all(6)),
      onChanged: state.enabled ?? true
          ? (double value) {
              setState(() {
                state.selection = value.toInt();
              });
              widget.sendSelectionSelection(state, state.selection);
            }
          : null,
    );
    return super.wrap(slider);
  }
}
