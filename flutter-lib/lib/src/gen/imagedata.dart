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
      case 549: // data
        data = r.bytes();
        return true;
      case 550: // depth
        depth = r.int32();
        return true;
      case 551: // height
        height = r.int32();
        return true;
      case 552: // width
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
      if (v_ != null && w.wants(549)) {
        w.key(549);
        w.bytes(v_);
      }
    }
    {
      final v_ = depth;
      if (v_ != null && w.wants(550)) {
        w.key(550);
        w.int32(v_);
      }
    }
    {
      final v_ = height;
      if (v_ != null && w.wants(551)) {
        w.key(551);
        w.int32(v_);
      }
    }
    {
      final v_ = width;
      if (v_ != null && w.wants(552)) {
        w.key(552);
        w.int32(v_);
      }
    }
  }

  @override
  void visitWire(void Function(int key, Object? value) visit) {
    visit(549, data);
    visit(550, depth);
    visit(551, height);
    visit(552, width);
  }
}
