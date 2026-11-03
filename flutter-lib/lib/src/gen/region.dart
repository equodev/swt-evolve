import '../comm/widget_wire.dart';
import '../gen/widgets.dart';

class VRegion implements WireReadable, WireWritable, WireVisitable {
  VRegion() : this.empty();
  VRegion.empty();

  /// The region's rectangles, flattened to `x, y, width, height` runs. A region is a set
  /// of disjoint rectangles, so its bounds are not its shape -- the outlines it is used
  /// for are mostly hole.
  List<int>? rects;

  @override
  bool readWire(int key, WidgetWireReader r) {
    switch (key) {
      case 423: // rects
        rects = r.ints();
        return true;
      default:
        return false;
    }
  }

  @override
  void writeWire(WidgetWireWriter w) {
    {
      final v_ = rects;
      if (v_ != null) {
        w.key(423);
        w.ints(v_);
      }
    }
  }

  @override
  void visitWire(void Function(int key, Object? value) visit) {
    visit(423, rects);
  }
}
