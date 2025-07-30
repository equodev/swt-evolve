import 'package:flutter/widgets.dart';

import '../swt/canvas.dart';
import '../swt/widget.dart';

import '../impl/composite_impl.dart';

class CanvasImpl<T extends CanvasSwt, V extends CanvasValue>
    extends CompositeImpl<T, V> {
  @override
  Widget build(BuildContext context) {
    // TODO: implement the widget, remove this line to stop regenerating
    return const Text("Canvas");
  }
}
