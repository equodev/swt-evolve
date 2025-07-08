import 'package:flutter/widgets.dart';
import '../gen/scrollable.dart';
import '../gen/widget.dart';
import '../impl/control_evolve.dart';

abstract class ScrollableImpl<T extends ScrollableSwt, V extends VScrollable>
    extends ControlImpl<T, V> {
  @override
  Widget build(BuildContext context) {
    // TODO: implement the widget, remove this line to stop regenerating
    return const Text("Scrollable");
  }
}
