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
      case 352: // data
        data = r.bytes();
        return true;
      case 353: // depth
        depth = r.int32();
        return true;
      case 354: // height
        height = r.int32();
        return true;
      case 355: // width
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
        w.key(352);
        w.bytes(v_);
      }
    }
    {
      final v_ = depth;
      if (v_ != null) {
        w.keyInt32(353, v_);
      }
    }
    {
      final v_ = height;
      if (v_ != null) {
        w.keyInt32(354, v_);
      }
    }
    {
      final v_ = width;
      if (v_ != null) {
        w.keyInt32(355, v_);
      }
    }
  }

  @override
  void visitWire(void Function(int key, Object? value) visit) {
    visit(352, data);
    visit(353, depth);
    visit(354, height);
    visit(355, width);
  }
}
