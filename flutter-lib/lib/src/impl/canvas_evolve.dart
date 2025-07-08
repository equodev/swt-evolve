import 'package:flutter/widgets.dart';
import '../gen/canvas.dart';
import '../gen/widget.dart';
import '../impl/composite_evolve.dart';

class CanvasImpl<T extends CanvasSwt, V extends VCanvas>
    extends CompositeImpl<T, V> {
  @override
  Widget build(BuildContext context) {
    // TODO: implement the widget, remove this line to stop regenerating
    return const Text("Canvas");
  }
}
