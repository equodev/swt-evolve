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
      case 192: // button
        button = r.int32();
        return true;
      case 193: // character
        character = r.char16();
        return true;
      case 194: // count
        count = r.int32();
        return true;
      case 195: // currentDataTypeId
        currentDataTypeId = r.int32();
        return true;
      case 196: // detail
        detail = r.int32();
        return true;
      case 197: // doit
        doit = r.boolean();
        return true;
      case 199: // end
        end = r.int32();
        return true;
      case 200: // feedback
        feedback = r.int32();
        return true;
      case 202: // height
        height = r.int32();
        return true;
      case 203: // index
        index = r.int32();
        return true;
      case 205: // itemId
        itemId = r.int64();
        return true;
      case 206: // keyCode
        keyCode = r.int32();
        return true;
      case 207: // keyLocation
        keyLocation = r.int32();
        return true;
      case 211: // segments
        segments = r.ints();
        return true;
      case 213: // start
        start = r.int32();
        return true;
      case 214: // stateMask
        stateMask = r.int32();
        return true;
      case 215: // text
        text = r.string();
        return true;
      case 216: // time
        time = r.int32();
        return true;
      case 218: // type
        type = r.int32();
        return true;
      case 220: // width
        width = r.int32();
        return true;
      case 221: // x
        x = r.int32();
        return true;
      case 223: // y
        y = r.int32();
        return true;
      default:
        return false;
    }
  }

  @override
  void visitWire(void Function(int key, Object? value) visit) {
    visit(192, button);
    visit(193, character);
    visit(194, count);
    visit(195, currentDataTypeId);
    visit(196, detail);
    visit(197, doit);
    visit(199, end);
    visit(200, feedback);
    visit(202, height);
    visit(203, index);
    visit(205, itemId);
    visit(206, keyCode);
    visit(207, keyLocation);
    visit(211, segments);
    visit(213, start);
    visit(214, stateMask);
    visit(215, text);
    visit(216, time);
    visit(218, type);
    visit(220, width);
    visit(221, x);
    visit(223, y);
  }

  @override
  void writeWire(WidgetWireWriter w) {
    {
      final v_ = button;
      if (v_ != null) {
        w.keyInt32(192, v_);
      }
    }
    {
      final v_ = character;
      if (v_ != null) {
        w.keyChar16(193, v_);
      }
    }
    {
      final v_ = count;
      if (v_ != null) {
        w.keyInt32(194, v_);
      }
    }
    {
      final v_ = currentDataTypeId;
      if (v_ != null) {
        w.keyInt32(195, v_);
      }
    }
    {
      final v_ = detail;
      if (v_ != null) {
        w.keyInt32(196, v_);
      }
    }
    {
      final v_ = doit;
      if (v_ != null) {
        w.keyBoolean(197, v_);
      }
    }
    {
      final v_ = end;
      if (v_ != null) {
        w.keyInt32(199, v_);
      }
    }
    {
      final v_ = feedback;
      if (v_ != null) {
        w.keyInt32(200, v_);
      }
    }
    {
      final v_ = height;
      if (v_ != null) {
        w.keyInt32(202, v_);
      }
    }
    {
      final v_ = index;
      if (v_ != null) {
        w.keyInt32(203, v_);
      }
    }
    {
      final v_ = itemId;
      if (v_ != null) {
        w.keyInt64(205, v_);
      }
    }
    {
      final v_ = keyCode;
      if (v_ != null) {
        w.keyInt32(206, v_);
      }
    }
    {
      final v_ = keyLocation;
      if (v_ != null) {
        w.keyInt32(207, v_);
      }
    }
    {
      final v_ = segments;
      if (v_ != null) {
        w.key(211);
        w.ints(v_);
      }
    }
    {
      final v_ = start;
      if (v_ != null) {
        w.keyInt32(213, v_);
      }
    }
    {
      final v_ = stateMask;
      if (v_ != null) {
        w.keyInt32(214, v_);
      }
    }
    {
      final v_ = text;
      if (v_ != null) {
        w.key(215);
        w.string(v_);
      }
    }
    {
      final v_ = time;
      if (v_ != null) {
        w.keyInt32(216, v_);
      }
    }
    {
      final v_ = type;
      if (v_ != null) {
        w.keyInt32(218, v_);
      }
    }
    {
      final v_ = width;
      if (v_ != null) {
        w.keyInt32(220, v_);
      }
    }
    {
      final v_ = x;
      if (v_ != null) {
        w.keyInt32(221, v_);
      }
    }
    {
      final v_ = y;
      if (v_ != null) {
        w.keyInt32(223, v_);
      }
    }
  }
}
