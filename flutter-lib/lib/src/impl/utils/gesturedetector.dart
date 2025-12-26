import 'package:flutter/material.dart';

import '../../gen/menu.dart';
import '../../gen/widget.dart';
import '../../gen/widgets.dart';

GestureDetector createGestureDetector(
    List<VWidget>? items, Widget widget, BuildContext context, state) {
  final contextAttachKey = GlobalKey();

  return GestureDetector(
    onSecondaryTapUp: (clickDetails) {
      _generateMenu(context, state, contextAttachKey,
          clickDetails, items);
    },
    child: Container(
      key: contextAttachKey,
      child: widget,
    ),
  );
}

void _generateMenu(
    BuildContext context,
    state,
    GlobalKey contextAttachKey,
    TapUpDetails clickDetails,
    List<VWidget>? items) {
  // This calculates the position of the menu according to the parent navigator
  final targetContext = contextAttachKey.currentContext;
  if (targetContext == null) return;
  final box = targetContext.findRenderObject() as RenderBox;
  final position = box.localToGlobal(
    clickDetails.localPosition,
    ancestor: Navigator.of(context).context.findRenderObject(),
  );

  // Use showMenu with proper RelativeRect
  final RenderBox overlay = Overlay.of(context).context.findRenderObject() as RenderBox;
  showMenu(
    context: context,
    position: RelativeRect.fromRect(
      Rect.fromLTWH(position.dx, position.dy, 0, 0),
      Rect.fromLTWH(0, 0, overlay.size.width, overlay.size.height),
    ),
    items: [
      PopupMenuItem(
        child: mapWidgetFromValue(state.menu as VMenu),
      ),
    ],
    color: Colors.white,
  );
}
