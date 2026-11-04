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
      case 344: // data
        data = r.bytes();
        return true;
      case 345: // depth
        depth = r.int32();
        return true;
      case 346: // height
        height = r.int32();
        return true;
      case 347: // width
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
        w.key(344);
        w.bytes(v_);
      }
    }
    {
      final v_ = depth;
      if (v_ != null) {
        w.keyInt32(345, v_);
      }
    }
    {
      final v_ = height;
      if (v_ != null) {
        w.keyInt32(346, v_);
      }
    }
    {
      final v_ = width;
      if (v_ != null) {
        w.keyInt32(347, v_);
      }
    }
  }

  @override
  void visitWire(void Function(int key, Object? value) visit) {
    visit(344, data);
    visit(345, depth);
    visit(346, height);
    visit(347, width);
  }
}
