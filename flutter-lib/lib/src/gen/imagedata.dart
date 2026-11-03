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
      case 342: // data
        data = r.bytes();
        return true;
      case 343: // depth
        depth = r.int32();
        return true;
      case 344: // height
        height = r.int32();
        return true;
      case 345: // width
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
        w.key(342);
        w.bytes(v_);
      }
    }
    {
      final v_ = depth;
      if (v_ != null) {
        w.keyInt32(343, v_);
      }
    }
    {
      final v_ = height;
      if (v_ != null) {
        w.keyInt32(344, v_);
      }
    }
    {
      final v_ = width;
      if (v_ != null) {
        w.keyInt32(345, v_);
      }
    }
  }

  @override
  void visitWire(void Function(int key, Object? value) visit) {
    visit(342, data);
    visit(343, depth);
    visit(344, height);
    visit(345, width);
  }
}
