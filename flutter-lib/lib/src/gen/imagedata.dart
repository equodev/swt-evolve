import '../comm/widget_wire.dart';
import '../gen/widgets.dart';

class VImageData implements WireReadable, WireWritable, WireVisitable {
  VImageData() : this.empty();
  VImageData.empty();

  List<int>? data;
  int? depth;
  int? height;
  int? width;

  @override
  bool readWire(int key, WidgetWireReader r) {
    switch (key) {
      case 2816: // data
        data = r.bytes();
        return true;
      case 2817: // depth
        depth = r.int32();
        return true;
      case 2818: // height
        height = r.int32();
        return true;
      case 2819: // width
        width = r.int32();
        return true;
      default:
        return false;
    }
  }

  @override
  void writeWire(WidgetWireWriter w) {
    {
      final v_ = data;
      if (v_ != null) {
        w.key(2816);
        w.bytes(v_);
      }
    }
    {
      final v_ = depth;
      if (v_ != null) {
        w.keyInt32(2817, v_);
      }
    }
    {
      final v_ = height;
      if (v_ != null) {
        w.keyInt32(2818, v_);
      }
    }
    {
      final v_ = width;
      if (v_ != null) {
        w.keyInt32(2819, v_);
      }
    }
  }

  @override
  void visitWire(void Function(int key, Object? value) visit) {
    visit(2816, data);
    visit(2817, depth);
    visit(2818, height);
    visit(2819, width);
  }
}
