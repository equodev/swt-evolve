import '../comm/widget_wire.dart';
import '../gen/image.dart';
import '../gen/widget.dart';
import '../gen/widgets.dart';
import 'widgets.dart';

abstract class ItemSwt<V extends VItem> extends WidgetSwt<V> {
  const ItemSwt({super.key, required super.value});
}

class VItem extends VWidget {
  VItem() : this.empty();
  VItem.empty() {
    swt = "Item";
  }

  VImage? image;
  String? text;

  @override
  void copyFrom(VWidget other) {
    super.copyFrom(other);
    if (other is VItem) {
      image = other.image;
      text = other.text;
    }
  }

  @override
  bool readWire(int key, WidgetWireReader r) {
    switch (key) {
      case 305: // image
        image = r.image();
        return true;
      case 306: // text
        text = r.string();
        return true;
      default:
        return super.readWire(key, r);
    }
  }

  @override
  void writeWire(WidgetWireWriter w) {
    super.writeWire(w);
    {
      final v_ = image;
      if (v_ != null && w.wants(305)) {
        w.key(305);
        w.ref(v_.id);
      }
    }
    {
      final v_ = text;
      if (v_ != null && w.wants(306)) {
        w.key(306);
        w.string(v_);
      }
    }
  }

  @override
  void visitWire(void Function(int key, Object? value) visit) {
    super.visitWire(visit);
    visit(305, image);
    visit(306, text);
  }

  @override
  String? get semanticsText => text;
}
