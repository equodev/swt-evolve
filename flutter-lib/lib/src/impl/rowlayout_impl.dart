import 'package:flutter/widgets.dart';

import '../swt/rowlayout.dart';
import '../widgets.dart';

class RowLayoutImpl extends RowLayoutSwt {
  const RowLayoutImpl(
      {super.key,
      required super.value,
      required super.children,
      required super.composite});

  @override
  Widget build(BuildContext context) {
    if (value.type == 1 << 8) {
      return Row(
          // mainAxisAlignment:MainAxisAlignment.start,
          // verticalDirection: VerticalDirection.down,
          children: children
              .map(mapWidgetFromValue)
              .map((e) => Expanded(child: e))
              .toList());
    } else {
      return Column(
          children: children
              .map(mapWidgetFromValue)
              .map((e) => Expanded(child: e))
              .toList());
    }
  }
}
