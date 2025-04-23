import 'package:flutter/widgets.dart';
import 'package:swtflutter/src/styles.dart';
import 'package:swtflutter/src/swt/swt.dart';
import 'package:swtflutter/src/widgets.dart';

import '../swt/expandbar.dart';

import '../impl/composite_impl.dart';

class ExpandBarImpl<T extends ExpandBarSwt, V extends ExpandBarValue>
    extends CompositeImpl<T, V> {
  @override
  Widget build(BuildContext context) {
    final style = StyleBits(state.style);
    var height = state.bounds?.height.toDouble();

    List<Widget> children = [];
    if (state.children != null) {
      children =
          state.children!.map((widget) => mapWidgetFromValue(widget)).toList();
    }

    Widget expandbar = Column(
      children: children,
    );

    if (style.has(SWT.V_SCROLL)) {
      expandbar = SizedBox(
          height: height,
          child: SingleChildScrollView(
            scrollDirection: Axis.vertical,
            child: expandbar,
          ));
    }

    return super.wrap(expandbar);
  }
}
