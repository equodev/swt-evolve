import 'package:flutter/widgets.dart';

import '../swt/scrolledcomposite.dart';
import '../swt/widget.dart';

import '../impl/composite_impl.dart';
import '../widgets.dart';

class ScrolledCompositeImpl<T extends ScrolledCompositeSwt,
    V extends ScrolledCompositeValue> extends CompositeImpl<T, V> {
  @override
  Widget build(BuildContext context) {
    if (state.children != null) {
      print("ScrolledCompositeImpl.build ${state.children!.length}");
      var children = state.children!;
      return super.wrap(mapLayout(state, state.layout, children));
    }
    return super.wrap(SizedBox(
        width: state.bounds?.width.toDouble() ?? 10,
        height: state.bounds?.height.toDouble() ?? 100));
  }
}
