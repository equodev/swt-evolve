import 'package:flutter/widgets.dart';
import '../gen/composite.dart';
import '../gen/widget.dart';
import '../impl/scrollable_evolve.dart';

class CompositeImpl<T extends CompositeSwt, V extends VComposite>
    extends ScrollableImpl<T, V> {
  @override
  Widget build(BuildContext context) {
    // TODO: implement the widget, remove this line to stop regenerating
    return const Text("Composite");
  }
}
