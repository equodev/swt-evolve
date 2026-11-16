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
      case 196: // button
        button = r.int32();
        return true;
      case 197: // character
        character = r.char16();
        return true;
      case 198: // count
        count = r.int32();
        return true;
      case 199: // currentDataTypeId
        currentDataTypeId = r.int32();
        return true;
      case 200: // detail
        detail = r.int32();
        return true;
      case 201: // doit
        doit = r.boolean();
        return true;
      case 203: // end
        end = r.int32();
        return true;
      case 204: // feedback
        feedback = r.int32();
        return true;
      case 206: // height
        height = r.int32();
        return true;
      case 207: // index
        index = r.int32();
        return true;
      case 209: // itemId
        itemId = r.int64();
        return true;
      case 210: // keyCode
        keyCode = r.int32();
        return true;
      case 211: // keyLocation
        keyLocation = r.int32();
        return true;
      case 215: // segments
        segments = r.ints();
        return true;
      case 217: // start
        start = r.int32();
        return true;
      case 218: // stateMask
        stateMask = r.int32();
        return true;
      case 219: // text
        text = r.string();
        return true;
      case 220: // time
        time = r.int32();
        return true;
      case 222: // type
        type = r.int32();
        return true;
      case 224: // width
        width = r.int32();
        return true;
      case 225: // x
        x = r.int32();
        return true;
      case 227: // y
        y = r.int32();
        return true;
      default:
        return false;
    }
  }

  @override
  void visitWire(void Function(int key, Object? value) visit) {
    visit(196, button);
    visit(197, character);
    visit(198, count);
    visit(199, currentDataTypeId);
    visit(200, detail);
    visit(201, doit);
    visit(203, end);
    visit(204, feedback);
    visit(206, height);
    visit(207, index);
    visit(209, itemId);
    visit(210, keyCode);
    visit(211, keyLocation);
    visit(215, segments);
    visit(217, start);
    visit(218, stateMask);
    visit(219, text);
    visit(220, time);
    visit(222, type);
    visit(224, width);
    visit(225, x);
    visit(227, y);
  }

  @override
  void writeWire(WidgetWireWriter w) {
    {
      final v_ = button;
      if (v_ != null) {
        w.keyInt32(196, v_);
      }
    }
    {
      final v_ = character;
      if (v_ != null) {
        w.keyChar16(197, v_);
      }
    }
    {
      final v_ = count;
      if (v_ != null) {
        w.keyInt32(198, v_);
      }
    }
    {
      final v_ = currentDataTypeId;
      if (v_ != null) {
        w.keyInt32(199, v_);
      }
    }
    {
      final v_ = detail;
      if (v_ != null) {
        w.keyInt32(200, v_);
      }
    }
    {
      final v_ = doit;
      if (v_ != null) {
        w.keyBoolean(201, v_);
      }
    }
    {
      final v_ = end;
      if (v_ != null) {
        w.keyInt32(203, v_);
      }
    }
    {
      final v_ = feedback;
      if (v_ != null) {
        w.keyInt32(204, v_);
      }
    }
    {
      final v_ = height;
      if (v_ != null) {
        w.keyInt32(206, v_);
      }
    }
    {
      final v_ = index;
      if (v_ != null) {
        w.keyInt32(207, v_);
      }
    }
    {
      final v_ = itemId;
      if (v_ != null) {
        w.keyInt64(209, v_);
      }
    }
    {
      final v_ = keyCode;
      if (v_ != null) {
        w.keyInt32(210, v_);
      }
    }
    {
      final v_ = keyLocation;
      if (v_ != null) {
        w.keyInt32(211, v_);
      }
    }
    {
      final v_ = segments;
      if (v_ != null) {
        w.key(215);
        w.ints(v_);
      }
    }
    {
      final v_ = start;
      if (v_ != null) {
        w.keyInt32(217, v_);
      }
    }
    {
      final v_ = stateMask;
      if (v_ != null) {
        w.keyInt32(218, v_);
      }
    }
    {
      final v_ = text;
      if (v_ != null) {
        w.key(219);
        w.string(v_);
      }
    }
    {
      final v_ = time;
      if (v_ != null) {
        w.keyInt32(220, v_);
      }
    }
    {
      final v_ = type;
      if (v_ != null) {
        w.keyInt32(222, v_);
      }
    }
    {
      final v_ = width;
      if (v_ != null) {
        w.keyInt32(224, v_);
      }
    }
    {
      final v_ = x;
      if (v_ != null) {
        w.keyInt32(225, v_);
      }
    }
    {
      final v_ = y;
      if (v_ != null) {
        w.keyInt32(227, v_);
      }
    }
  }
}
