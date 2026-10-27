import 'package:flutter/widgets.dart';
import '../comm/comm.dart';
import '../gen/event.dart';
import '../gen/scrollbar.dart';
import '../gen/widget.dart';

class ScrollBarImpl<T extends ScrollBarSwt, V extends VScrollBar>
    extends WidgetSwtState<T, V> {
  @override
  Widget build(BuildContext context) {
    return const SizedBox.shrink();
  }
}

/// Tells Java the user moved [bar] to [selection]; [detail] is the SWT scroll detail (`SWT.DRAG`, ...).
void sendScrollBarSelectionEvent(
  VScrollBar bar,
  int selection, {
  int detail = 0,
}) {
  EquoCommService.sendPayload(
    'ScrollBar/${bar.id}/Selection/Selection',
    (VEvent()
          ..index = selection
          ..detail = detail)
        .toJson(),
  );
}
