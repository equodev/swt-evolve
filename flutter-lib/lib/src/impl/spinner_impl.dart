import 'package:fluent_ui/fluent_ui.dart';
import 'package:swtflutter/src/swt/swt.dart';

import '../impl/composite_impl.dart';
import '../styles.dart';
import '../swt/spinner.dart';

class SpinnerImpl<T extends SpinnerSwt, V extends SpinnerValue>
    extends CompositeImpl<T, V> {
  @override
  Widget build(BuildContext context) {
    final style = StyleBits(state.style);
    final spinner = NumberBox(
      value: toFloat(state.selection),
      onChanged: style.has(SWT.READ_ONLY)
          ? null
          : (num? value) {
              if (style.has(SWT.WRAP)) {
                value = wrapValue(value);
              }
              final intValue = toInt(value);
              if (state.selection != intValue) {
                setState(() {
                  state.selection = intValue;
                });
                widget.sendSelectionSelection(state, state.selection);
              }
            },
      smallChange: toFloat(state.increment) ?? 1,
      largeChange: toFloat(state.pageIncrement) ?? 10,
      precision: state.digits ?? 0,
      mode: SpinButtonPlacementMode.inline,
      clearButton: false,
      allowExpressions: true,
      max: toFloat(state.maximum),
      min: toFloat(state.minimum),
    );
    return super.wrap(spinner);
  }

  num? toFloat(int? value) {
    if (value == null) {
      return null;
    }
    if (state.digits == null || state.digits == 0) {
      return value;
    }
    double newValue = value.toDouble();
    for (int i = 0; i < state.digits!; i++) {
      newValue /= 10;
    }
    return newValue;
  }

  int toInt(num? value) {
    if (value == null) {
      return 0;
    }
    if (state.digits == null || state.digits == 0) {
      return value.toInt();
    }
    num newValue = value;
    for (int i = 0; i < state.digits!; i++) {
      newValue *= 10;
    }
    return newValue.toInt();
  }

  num? wrapValue(num? value) {
    int previousValue = state.selection ?? 0;
    if (value != null &&
        state.maximum != null &&
        state.minimum != null &&
        value >= state.maximum! &&
        previousValue == state.maximum) {
      value = state.minimum;
    } else if (value != null &&
        state.minimum != null &&
        state.minimum != null &&
        value <= state.minimum! &&
        previousValue == state.minimum) {
      value = state.maximum;
    }
    return value;
  }
}
