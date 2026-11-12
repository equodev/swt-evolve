import '../comm/widget_wire.dart';
import '../gen/imagedata.dart';
import '../gen/widgets.dart';

class VImage implements WireReadable, WireWritable, WireVisitable {
  VImage() : this.empty();
  VImage.empty();

  /// The id Java knows this image by. An image used to travel as pixels under whatever
  /// widget owned it, so the same icon arrived once per owner and again with every full
  /// description of one; with an id it can be named instead.
  int? id;

  /// True when this image arrived as a name rather than as a description.
  bool isReference = false;
  String? filename;
  int? height;
  VImageData? imageData;
  int? remoteRef;
  String? svgContent;
  int? width;

  @override
  bool readWire(int key, WidgetWireReader r) {
    switch (key) {
      case 343: // filename
        filename = r.string();
        return true;
      case 344: // height
        height = r.int32();
        return true;
      case 345: // imageData
        imageData = r.object(VImageData.new);
        return true;
      case 346: // remoteRef
        remoteRef = r.longBoxed();
        return true;
      case 347: // svgContent
        svgContent = r.string();
        return true;
      case 348: // width
        width = r.int32();
        return true;
      default:
        return false;
    }
  }

  @override
  void writeWire(WidgetWireWriter w) {
    {
      final v_ = filename;
      if (v_ != null) {
        w.key(343);
        w.string(v_);
      }
    }
    {
      final v_ = height;
      if (v_ != null) {
        w.keyInt32(344, v_);
      }
    }
    {
      final v_ = imageData;
      if (v_ != null) {
        w.key(345);
        w.object(v_.writeWire);
      }
    }
    {
      final v_ = remoteRef;
      if (v_ != null) {
        w.key(346);
        w.longBoxed(v_);
      }
    }
    {
      final v_ = svgContent;
      if (v_ != null) {
        w.key(347);
        w.string(v_);
      }
    }
    {
      final v_ = width;
      if (v_ != null) {
        w.keyInt32(348, v_);
      }
    }
  }

  @override
  void visitWire(void Function(int key, Object? value) visit) {
    visit(343, filename);
    visit(344, height);
    visit(345, imageData);
    visit(346, remoteRef);
    visit(347, svgContent);
    visit(348, width);
  }
}

/// Every image this client holds, by its Java id, until Java disposes it ([forgetImage]).
final Map<int, VImage> _imagesHeld = {};

/// Answers a named image with the one already held, and remembers a described one.
VImage rememberOrResolveImage(VImage value) {
  final id = value.id;
  if (id == null) return value;
  if (value.isReference) return _imagesHeld[id] ?? value;
  // An image with nothing to draw by is not worth holding. One rendered by its own GC is: it
  // is a size and a remote ref, and a frame names it by id, which only this map can turn
  // into the ref its render is cached under.
  if (value.imageData != null ||
      value.svgContent != null ||
      value.remoteRef != null) {
    _imagesHeld[id] = value;
  }
  return value;
}

/// Drops the image Java disposed under [id]. A frame applied in the same turn may still name
/// it, so it goes a turn later, and not if a newer image has taken the id by then.
void forgetImage(int id) {
  final held = _imagesHeld[id];
  if (held == null) return;
  Future(() {
    if (identical(_imagesHeld[id], held)) _imagesHeld.remove(id);
  });
}

int get heldImageCount => _imagesHeld.length;
