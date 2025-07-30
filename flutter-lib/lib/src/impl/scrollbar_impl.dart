import 'package:flutter/widgets.dart';

import '../swt/scrollbar.dart';
import '../swt/widget.dart';

class ScrollBarImpl<T extends ScrollBarSwt, V extends ScrollBarValue>
    extends WidgetSwtState<T, V> {
  @override
  Widget build(BuildContext context) {
    // TODO: implement the widget, remove this line to stop regenerating
    return const Text("ScrollBar");
  }
}
