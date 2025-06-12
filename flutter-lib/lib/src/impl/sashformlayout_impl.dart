import 'package:flutter/widgets.dart';

import '../swt/sashformlayout.dart';
import '../swt/widget.dart';

//import '../impl/layout_impl.dart';

class SashFormLayoutImpl extends SashFormLayoutSwt {
  const SashFormLayoutImpl(
      {super.key,
      required super.value,
      required super.children,
      required super.composite});

  @override
  Widget build(BuildContext context) {
    // TODO: implement the widget, remove this line to stop regenerating
    return const Text("SashFormLayout");
  }
}
