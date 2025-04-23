import 'package:flutter/widgets.dart';

import '../swt/caret.dart';
import '../swt/widget.dart';

class CaretImpl<T extends CaretSwt, V extends CaretValue>
    extends WidgetSwtState<T, V> {
  @override
  Widget build(BuildContext context) {
    // TODO: implement the widget, remove this line to stop regenerating
    return const Text("Caret");
  }
}
