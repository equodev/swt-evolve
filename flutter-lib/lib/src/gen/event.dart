import '../comm/widget_wire.dart';
import '../gen/widgets.dart';

class VEvent implements WireReadable, WireWritable, WireVisitable {
  VEvent() : this.empty();
  VEvent.empty();

  int? button;
  int? character;
  int? count;
  int? currentDataTypeId;
  int? detail;
  bool? doit;
  int? end;
  int? feedback;
  int? height;
  int? index;
  int? itemId;
  int? keyCode;
  int? keyLocation;
  List<int>? segments;
  int? start;
  int? stateMask;
  String? text;
  int? time;
  int? type;
  int? width;
  int? x;
  int? y;

  @override
  bool readWire(int key, WidgetWireReader r) {
    switch (key) {
      case 1856: // button
        button = r.int32();
        return true;
      case 1857: // character
        character = r.char16();
        return true;
      case 1858: // count
        count = r.int32();
        return true;
      case 1859: // currentDataTypeId
        currentDataTypeId = r.int32();
        return true;
      case 1860: // detail
        detail = r.int32();
        return true;
      case 1861: // doit
        doit = r.boolean();
        return true;
      case 1863: // end
        end = r.int32();
        return true;
      case 1864: // feedback
        feedback = r.int32();
        return true;
      case 1866: // height
        height = r.int32();
        return true;
      case 1867: // index
        index = r.int32();
        return true;
      case 1869: // itemId
        itemId = r.int64();
        return true;
      case 1870: // keyCode
        keyCode = r.int32();
        return true;
      case 1871: // keyLocation
        keyLocation = r.int32();
        return true;
      case 1875: // segments
        segments = r.ints();
        return true;
      case 1877: // start
        start = r.int32();
        return true;
      case 1878: // stateMask
        stateMask = r.int32();
        return true;
      case 1879: // text
        text = r.string();
        return true;
      case 1880: // time
        time = r.int32();
        return true;
      case 1882: // type
        type = r.int32();
        return true;
      case 1884: // width
        width = r.int32();
        return true;
      case 1885: // x
        x = r.int32();
        return true;
      case 1887: // y
        y = r.int32();
        return true;
      default:
        return false;
    }
  }

  @override
  void visitWire(void Function(int key, Object? value) visit) {
    visit(1856, button);
    visit(1857, character);
    visit(1858, count);
    visit(1859, currentDataTypeId);
    visit(1860, detail);
    visit(1861, doit);
    visit(1863, end);
    visit(1864, feedback);
    visit(1866, height);
    visit(1867, index);
    visit(1869, itemId);
    visit(1870, keyCode);
    visit(1871, keyLocation);
    visit(1875, segments);
    visit(1877, start);
    visit(1878, stateMask);
    visit(1879, text);
    visit(1880, time);
    visit(1882, type);
    visit(1884, width);
    visit(1885, x);
    visit(1887, y);
  }

  @override
  void writeWire(WidgetWireWriter w) {
    {
      final v_ = button;
      if (v_ != null) {
        w.keyInt32(1856, v_);
      }
    }
    {
      final v_ = character;
      if (v_ != null) {
        w.keyChar16(1857, v_);
      }
    }
    {
      final v_ = count;
      if (v_ != null) {
        w.keyInt32(1858, v_);
      }
    }
    {
      final v_ = currentDataTypeId;
      if (v_ != null) {
        w.keyInt32(1859, v_);
      }
    }
    {
      final v_ = detail;
      if (v_ != null) {
        w.keyInt32(1860, v_);
      }
    }
    {
      final v_ = doit;
      if (v_ != null) {
        w.keyBoolean(1861, v_);
      }
    }
    {
      final v_ = end;
      if (v_ != null) {
        w.keyInt32(1863, v_);
      }
    }
    {
      final v_ = feedback;
      if (v_ != null) {
        w.keyInt32(1864, v_);
      }
    }
    {
      final v_ = height;
      if (v_ != null) {
        w.keyInt32(1866, v_);
      }
    }
    {
      final v_ = index;
      if (v_ != null) {
        w.keyInt32(1867, v_);
      }
    }
    {
      final v_ = itemId;
      if (v_ != null) {
        w.keyInt64(1869, v_);
      }
    }
    {
      final v_ = keyCode;
      if (v_ != null) {
        w.keyInt32(1870, v_);
      }
    }
    {
      final v_ = keyLocation;
      if (v_ != null) {
        w.keyInt32(1871, v_);
      }
    }
    {
      final v_ = segments;
      if (v_ != null) {
        w.key(1875);
        w.ints(v_);
      }
    }
    {
      final v_ = start;
      if (v_ != null) {
        w.keyInt32(1877, v_);
      }
    }
    {
      final v_ = stateMask;
      if (v_ != null) {
        w.keyInt32(1878, v_);
      }
    }
    {
      final v_ = text;
      if (v_ != null) {
        w.key(1879);
        w.string(v_);
      }
    }
    {
      final v_ = time;
      if (v_ != null) {
        w.keyInt32(1880, v_);
      }
    }
    {
      final v_ = type;
      if (v_ != null) {
        w.keyInt32(1882, v_);
      }
    }
    {
      final v_ = width;
      if (v_ != null) {
        w.keyInt32(1884, v_);
      }
    }
    {
      final v_ = x;
      if (v_ != null) {
        w.keyInt32(1885, v_);
      }
    }
    {
      final v_ = y;
      if (v_ != null) {
        w.keyInt32(1887, v_);
      }
    }
  }
}
