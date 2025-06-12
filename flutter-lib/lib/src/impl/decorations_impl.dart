import 'package:flutter/widgets.dart';

import '../swt/decorations.dart';
import '../swt/widget.dart';

import '../impl/canvas_impl.dart';

class DecorationsImpl<T extends DecorationsSwt, V extends DecorationsValue>
    extends CanvasImpl<T, V> {
  @override
  Widget build(BuildContext context) {
    // TODO: implement the widget, remove this line to stop regenerating
    return const Text("Decorations");
  }
}
