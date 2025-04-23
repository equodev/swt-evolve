import 'package:fluent_ui/fluent_ui.dart';
import 'package:swtflutter/src/swt/expandbar.dart';
import 'package:swtflutter/src/widgets.dart';

import '../swt/expanditem.dart';

import '../impl/item_impl.dart';

class ExpandItemImpl<T extends ExpandItemSwt, V extends ExpandItemValue>
    extends ItemImpl<T, V> {
  @override
  Widget build(BuildContext context) {
    var parent = context.findAncestorWidgetOfExactType<ExpandBarSwt>();
    var height = state.height?.toDouble();

    List<Widget> children = [];
    if (state.children != null) {
      children =
          state.children!.map((widget) => mapWidgetFromValue(widget)).toList();
    }

    Widget content = Column(
      children: children,
    );

    if (height != null && height > 0) {
      content = SizedBox(
          height: height,
          child: SingleChildScrollView(
            scrollDirection: Axis.vertical,
            child: content,
          ));
    }

    return Expander(
        header: Text(state.text ?? ""),
        initiallyExpanded: state.expanded ?? false,
        onStateChanged: (expanded) {
          if (expanded) {
            parent?.sendExpandExpand(parent.value, null);
          } else {
            parent?.sendExpandCollapse(parent.value, null);
          }
        },
        content: content);
  }
}
