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
      case 341: // data
        data = r.bytes();
        return true;
      case 342: // depth
        depth = r.int32();
        return true;
      case 343: // height
        height = r.int32();
        return true;
      case 344: // width
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
        w.key(341);
        w.bytes(v_);
      }
    }
    {
      final v_ = depth;
      if (v_ != null) {
        w.keyInt32(342, v_);
      }
    }
    {
      final v_ = height;
      if (v_ != null) {
        w.keyInt32(343, v_);
      }
    }
    {
      final v_ = width;
      if (v_ != null) {
        w.keyInt32(344, v_);
      }
    }
  }

  @override
  void visitWire(void Function(int key, Object? value) visit) {
    visit(341, data);
    visit(342, depth);
    visit(343, height);
    visit(344, width);
  }
}
