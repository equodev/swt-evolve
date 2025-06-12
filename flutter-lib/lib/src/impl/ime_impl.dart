import 'package:flutter/widgets.dart';

import '../swt/ime.dart';
import '../swt/widget.dart';

class IMEImpl<T extends IMESwt, V extends IMEValue>
    extends WidgetSwtState<T, V> {
  @override
  Widget build(BuildContext context) {
    // TODO: implement the widget, remove this line to stop regenerating
    return const Text("IME");
  }
}
