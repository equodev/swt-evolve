import 'package:flutter/widgets.dart';
import '../comm/widget_wire.dart';
import '../gen/widget.dart';
import '../gen/widgets.dart';
import '../impl/ime_evolve.dart';
import 'widgets.dart';

class IMESwt<V extends VIME> extends WidgetSwt<V> {
  const IMESwt({super.key, required super.value});

  @override
  State createState() => IMEImpl<IMESwt<VIME>, VIME>();
}

class VIME extends VWidget {
  VIME() : this.empty();
  VIME.empty() {
    swt = "IME";
  }
}
