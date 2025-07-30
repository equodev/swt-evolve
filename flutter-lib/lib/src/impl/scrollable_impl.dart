import 'package:flutter/widgets.dart';

import '../swt/scrollable.dart';
import '../swt/widget.dart';

import '../impl/control_impl.dart';

abstract class ScrollableImpl<T extends ScrollableSwt,
    V extends ScrollableValue> extends ControlImpl<T, V> {
  @override
  Widget build(BuildContext context) {
    // TODO: implement the widget, remove this line to stop regenerating
    return const Text("Scrollable");
  }
}
