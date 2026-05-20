import 'package:flutter/widgets.dart';
import '../gen/scrollbar.dart';
import '../gen/widget.dart';

class ScrollBarImpl<T extends ScrollBarSwt, V extends VScrollBar>
    extends WidgetSwtState<T, V> {
  @override
  Widget build(BuildContext context) {
    return const SizedBox.shrink();
  }
}
