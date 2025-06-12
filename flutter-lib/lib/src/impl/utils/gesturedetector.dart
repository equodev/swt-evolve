import 'package:fluent_ui/fluent_ui.dart';

import '../../swt/menu.dart';
import '../../swt/widget.dart';
import '../../widgets.dart';

GestureDetector createGestureDetector(List<WidgetValue>? items, Widget widget, BuildContext context, state) {
  final contextController = FlyoutController();
  final contextAttachKey = GlobalKey();

  return GestureDetector(
    onSecondaryTapUp: (clickDetails) {
      _generateMenu(context, state, contextAttachKey, contextController, clickDetails, items);
    },
    child: FlyoutTarget(
      key: contextAttachKey,
      controller: contextController,
      child: widget,
    ),
  );
}

void _generateMenu(BuildContext context, state, GlobalKey contextAttachKey,
              FlyoutController contextController, TapUpDetails clickDetails,
              List<WidgetValue>? items) {
  // This calculates the position of the flyout according to the parent navigator
  final targetContext = contextAttachKey.currentContext;
  if (targetContext == null) return;
  final box = targetContext.findRenderObject() as RenderBox;
  final position = box.localToGlobal(
    clickDetails.localPosition,
    ancestor: Navigator.of(context).context.findRenderObject(),
  );

  contextController.showFlyout(
    barrierColor: Colors.black.withOpacity(0.1),
    position: position,
    builder: (context) {
      return mapWidgetFromValue(state.menu as MenuValue);
    },
  );
}