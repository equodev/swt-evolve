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
      case 349: // data
        data = r.bytes();
        return true;
      case 350: // depth
        depth = r.int32();
        return true;
      case 351: // height
        height = r.int32();
        return true;
      case 352: // width
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
        w.key(349);
        w.bytes(v_);
      }
    }
    {
      final v_ = depth;
      if (v_ != null) {
        w.keyInt32(350, v_);
      }
    }
    {
      final v_ = height;
      if (v_ != null) {
        w.keyInt32(351, v_);
      }
    }
    {
      final v_ = width;
      if (v_ != null) {
        w.keyInt32(352, v_);
      }
    }
  }

  @override
  void visitWire(void Function(int key, Object? value) visit) {
    visit(349, data);
    visit(350, depth);
    visit(351, height);
    visit(352, width);
  }
}
