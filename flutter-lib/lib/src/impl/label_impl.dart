import 'package:fluent_ui/fluent_ui.dart';
import 'control_impl.dart';
import '../swt/swt.dart';
import '../swt/label.dart';
import '../styles.dart';

class LabelImpl<T extends LabelSwt<V>, V extends LabelValue>
    extends ControlImpl<T, V> {
  @override
  Widget build(BuildContext context) {
    var child = switch (StyleBits(state.style)) {
      < SWT.SEPARATOR => Divider(
          size: 10,
          direction:
              state.style.has(SWT.VERTICAL) ? Axis.vertical : Axis.horizontal,
        ),
      < SWT.VERTICAL => Column(children: [
          RotatedBox(
            quarterTurns: 1, // Rotate 90 degrees clockwise
            child: buildText(),
          )
        ]),
      _ => buildText(),
    };

    return super.wrap(child);
  }

  Widget buildText() {
    return Column(children: [
      Text(
        overflow: TextOverflow.ellipsis,
        state.text ?? "",
        softWrap: state.style.has(SWT.WRAP),
        textAlign: TextAlign.start,
      )
    ]);
  }
}
