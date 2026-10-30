import 'package:flutter/widgets.dart';
import '../comm/comm.dart';
import '../gen/event.dart';
import '../gen/scrollbar.dart';
import '../gen/widget.dart';
import '../gen/widgets.dart' show WireChannel, WireType;

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
  EquoCommService.sendTo(
    WireType.scrollBar,
    bar.id,
    WireChannel.selectionSelection,
    VEvent()
      ..index = selection
      ..detail = detail,
  );
}
