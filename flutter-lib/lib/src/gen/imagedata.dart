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
      case 340: // data
        data = r.bytes();
        return true;
      case 341: // depth
        depth = r.int32();
        return true;
      case 342: // height
        height = r.int32();
        return true;
      case 343: // width
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
        w.key(340);
        w.bytes(v_);
      }
    }
    {
      final v_ = depth;
      if (v_ != null) {
        w.keyInt32(341, v_);
      }
    }
    {
      final v_ = height;
      if (v_ != null) {
        w.keyInt32(342, v_);
      }
    }
    {
      final v_ = width;
      if (v_ != null) {
        w.keyInt32(343, v_);
      }
    }
  }

  @override
  void visitWire(void Function(int key, Object? value) visit) {
    visit(340, data);
    visit(341, depth);
    visit(342, height);
    visit(343, width);
  }
}
