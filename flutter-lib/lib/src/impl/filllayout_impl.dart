import 'package:fluent_ui/fluent_ui.dart';
import 'package:flutter/widgets.dart';

import '../widgets.dart';
import '../swt/filllayout.dart';

class FillLayoutImpl extends FillLayoutSwt {
  const FillLayoutImpl(
      {super.key,
      required super.value,
      required super.children,
      required super.composite});

  @override
  Widget build(BuildContext context) {
    if (value.type == 1 << 9) {
      return Column(
        mainAxisSize: MainAxisSize.max,
        crossAxisAlignment: CrossAxisAlignment.stretch,
        children: children
            .map(mapWidgetFromValue)
            .map((w) => Expanded(child: w))
            .toList(),
      );
    } else {
      return Row(
        mainAxisSize: MainAxisSize.max,
        crossAxisAlignment: CrossAxisAlignment.stretch,
        children: children
            .map(mapWidgetFromValue)
            .map((w) => Expanded(child: w))
            .toList(),
      );
    }
  }
}
