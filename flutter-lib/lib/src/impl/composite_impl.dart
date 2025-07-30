import 'package:flutter/material.dart';
import 'package:flutter/widgets.dart';

import '../swt/composite.dart';
import '../widgets.dart';

import '../impl/scrollable_impl.dart';

class CompositeImpl<T extends CompositeSwt, V extends CompositeValue>
    extends ScrollableImpl<T, V> {
  @override
  Widget build(BuildContext context) {
    if (state.children != null) {
      print("CompositeImpl.build ${state.children!.length}");
      var children = state.children!;
      return super.wrap(mapLayout(state, state.layout, children));
    }
    return super.wrap(SizedBox(
        width: state.bounds?.width.toDouble() ?? 10,
        height: state.bounds?.height.toDouble() ?? 100));
    // return Container(
    //     // width: 600,
    //     // height: 400,
    //     color: Colors.amber,
    //     child: mapLayout(state, state.layout, children));
  }
}
