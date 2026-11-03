import '../comm/widget_wire.dart';
import '../gen/color.dart';
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
import 'widgets.dart';

abstract class ScrollableSwt<V extends VScrollable> extends ControlSwt<V> {
  const ScrollableSwt({super.key, required super.value});
}

class VScrollable extends VControl {
  VScrollable() : this.empty();
  VScrollable.empty() {
    swt = "Scrollable";
  }

  VScrollBar? horizontalBar;
  VScrollBar? verticalBar;

  @override
  void copyFrom(VWidget other) {
    super.copyFrom(other);
    if (other is VScrollable) {
      horizontalBar = other.horizontalBar;
      verticalBar = other.verticalBar;
    }
  }

  @override
  bool readWire(int key, WidgetWireReader r) {
    switch (key) {
      case 440: // horizontalBar
        horizontalBar = r.ref<VScrollBar>();
        return true;
      case 441: // verticalBar
        verticalBar = r.ref<VScrollBar>();
        return true;
      default:
        return super.readWire(key, r);
    }
  }

  @override
  void writeWire(WidgetWireWriter w) {
    super.writeWire(w);
    {
      final v_ = horizontalBar;
      if (v_ != null) {
        w.keyRef(440, v_.id);
      }
    }
    {
      final v_ = verticalBar;
      if (v_ != null) {
        w.keyRef(441, v_.id);
      }
    }
  }

  @override
  void visitWire(void Function(int key, Object? value) visit) {
    super.visitWire(visit);
    visit(440, horizontalBar);
    visit(441, verticalBar);
  }

  @override
  void adoptChildren(VWidget Function(VWidget) adopt) {
    super.adoptChildren(adopt);
    horizontalBar = VWidget.adoptOne(horizontalBar, adopt);
    verticalBar = VWidget.adoptOne(verticalBar, adopt);
  }
}
