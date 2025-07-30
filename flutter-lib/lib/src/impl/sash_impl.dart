import 'package:flutter/widgets.dart';

import '../swt/sash.dart';
import '../swt/widget.dart';

import '../impl/control_impl.dart';

class SashImpl<T extends SashSwt, V extends SashValue>
    extends ControlImpl<T, V> {
  @override
  Widget build(BuildContext context) {
    // TODO: implement the widget, remove this line to stop regenerating
    return const Text("Sash");
  }
}
