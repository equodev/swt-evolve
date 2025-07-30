import 'package:fluent_ui/fluent_ui.dart';
import 'package:swtflutter/src/impl/utils/gesturedetector.dart';
import 'package:swtflutter/src/styles.dart';

import '../swt/control.dart';
import '../swt/menuitem.dart';
import '../swt/swt.dart';
import '../swt/widget.dart';

abstract class ControlImpl<T extends ControlSwt, V extends ControlValue>
    extends WidgetSwtState<T, V> {
  @override
  Widget build(BuildContext context) {
    return const Text("Control");
  }

  Widget wrap(Widget widget) {
    if (state.style.has(SWT.BORDER)) {
      widget = Container(
          decoration:
              BoxDecoration(border: Border.all(width: 0.5, color: Colors.grey)),
          child: widget);
    }

    if (state.visible != null && !state.visible!) {
      return Visibility(
        visible: false,
        child: widget,
      );
    }
    if (state.enabled != null && !state.enabled!) {
      return Opacity(opacity: 0.35, child: widget);
    }

    if (state.menu != null) {
      return applyMenu(widget);
    }

    return widget;
  }

  GestureDetector applyMenu(Widget widget) {
    var items = state.menu?.children?.whereType<MenuItemValue>().toList();
    return createGestureDetector(items, widget, context, state);
  }
}
