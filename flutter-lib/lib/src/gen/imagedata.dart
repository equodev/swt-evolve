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
      case 339: // data
        data = r.bytes();
        return true;
      case 340: // depth
        depth = r.int32();
        return true;
      case 341: // height
        height = r.int32();
        return true;
      case 342: // width
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
        w.key(339);
        w.bytes(v_);
      }
    }
    {
      final v_ = depth;
      if (v_ != null) {
        w.keyInt32(340, v_);
      }
    }
    {
      final v_ = height;
      if (v_ != null) {
        w.keyInt32(341, v_);
      }
    }
    {
      final v_ = width;
      if (v_ != null) {
        w.keyInt32(342, v_);
      }
    }
  }

  @override
  void visitWire(void Function(int key, Object? value) visit) {
    visit(339, data);
    visit(340, depth);
    visit(341, height);
    visit(342, width);
  }
}
