import 'package:flutter/widgets.dart';
import '../comm/widget_wire.dart';
import '../gen/color.dart';
import '../gen/composite.dart';
import '../gen/control.dart';
import '../gen/cursor.dart';
import '../gen/font.dart';
import '../gen/image.dart';
import '../gen/menu.dart';
import '../gen/rectangle.dart';
import '../gen/region.dart';
import '../gen/scrollbar.dart';
import '../gen/widget.dart';
import '../gen/widgets.dart';
import '../impl/cbanner_evolve.dart';
import 'widgets.dart';

class CBannerSwt<V extends VCBanner> extends CompositeSwt<V> {
  const CBannerSwt({super.key, required super.value});

  @override
  State createState() => CBannerImpl<CBannerSwt<VCBanner>, VCBanner>();
}

class VCBanner extends VComposite {
  VCBanner() : this.empty();
  VCBanner.empty() {
    swt = "CBanner";
  }
}
