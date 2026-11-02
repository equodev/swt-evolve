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
import '../impl/viewform_evolve.dart';
import 'widgets.dart';

class ViewFormSwt<V extends VViewForm> extends CompositeSwt<V> {
  const ViewFormSwt({super.key, required super.value});

  @override
  State createState() => ViewFormImpl<ViewFormSwt<VViewForm>, VViewForm>();
}

class VViewForm extends VComposite {
  VViewForm() : this.empty();
  VViewForm.empty() {
    swt = "ViewForm";
  }
}
