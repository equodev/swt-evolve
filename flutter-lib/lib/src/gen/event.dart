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
      case 195: // button
        button = r.int32();
        return true;
      case 196: // character
        character = r.char16();
        return true;
      case 197: // count
        count = r.int32();
        return true;
      case 198: // currentDataTypeId
        currentDataTypeId = r.int32();
        return true;
      case 199: // detail
        detail = r.int32();
        return true;
      case 200: // doit
        doit = r.boolean();
        return true;
      case 202: // end
        end = r.int32();
        return true;
      case 203: // feedback
        feedback = r.int32();
        return true;
      case 205: // height
        height = r.int32();
        return true;
      case 206: // index
        index = r.int32();
        return true;
      case 208: // itemId
        itemId = r.int64();
        return true;
      case 209: // keyCode
        keyCode = r.int32();
        return true;
      case 210: // keyLocation
        keyLocation = r.int32();
        return true;
      case 214: // segments
        segments = r.ints();
        return true;
      case 216: // start
        start = r.int32();
        return true;
      case 217: // stateMask
        stateMask = r.int32();
        return true;
      case 218: // text
        text = r.string();
        return true;
      case 219: // time
        time = r.int32();
        return true;
      case 221: // type
        type = r.int32();
        return true;
      case 223: // width
        width = r.int32();
        return true;
      case 224: // x
        x = r.int32();
        return true;
      case 226: // y
        y = r.int32();
        return true;
      default:
        return false;
    }
  }

  @override
  void visitWire(void Function(int key, Object? value) visit) {
    visit(195, button);
    visit(196, character);
    visit(197, count);
    visit(198, currentDataTypeId);
    visit(199, detail);
    visit(200, doit);
    visit(202, end);
    visit(203, feedback);
    visit(205, height);
    visit(206, index);
    visit(208, itemId);
    visit(209, keyCode);
    visit(210, keyLocation);
    visit(214, segments);
    visit(216, start);
    visit(217, stateMask);
    visit(218, text);
    visit(219, time);
    visit(221, type);
    visit(223, width);
    visit(224, x);
    visit(226, y);
  }

  @override
  void writeWire(WidgetWireWriter w) {
    {
      final v_ = button;
      if (v_ != null) {
        w.keyInt32(195, v_);
      }
    }
    {
      final v_ = character;
      if (v_ != null) {
        w.keyChar16(196, v_);
      }
    }
    {
      final v_ = count;
      if (v_ != null) {
        w.keyInt32(197, v_);
      }
    }
    {
      final v_ = currentDataTypeId;
      if (v_ != null) {
        w.keyInt32(198, v_);
      }
    }
    {
      final v_ = detail;
      if (v_ != null) {
        w.keyInt32(199, v_);
      }
    }
    {
      final v_ = doit;
      if (v_ != null) {
        w.keyBoolean(200, v_);
      }
    }
    {
      final v_ = end;
      if (v_ != null) {
        w.keyInt32(202, v_);
      }
    }
    {
      final v_ = feedback;
      if (v_ != null) {
        w.keyInt32(203, v_);
      }
    }
    {
      final v_ = height;
      if (v_ != null) {
        w.keyInt32(205, v_);
      }
    }
    {
      final v_ = index;
      if (v_ != null) {
        w.keyInt32(206, v_);
      }
    }
    {
      final v_ = itemId;
      if (v_ != null) {
        w.keyInt64(208, v_);
      }
    }
    {
      final v_ = keyCode;
      if (v_ != null) {
        w.keyInt32(209, v_);
      }
    }
    {
      final v_ = keyLocation;
      if (v_ != null) {
        w.keyInt32(210, v_);
      }
    }
    {
      final v_ = segments;
      if (v_ != null) {
        w.key(214);
        w.ints(v_);
      }
    }
    {
      final v_ = start;
      if (v_ != null) {
        w.keyInt32(216, v_);
      }
    }
    {
      final v_ = stateMask;
      if (v_ != null) {
        w.keyInt32(217, v_);
      }
    }
    {
      final v_ = text;
      if (v_ != null) {
        w.key(218);
        w.string(v_);
      }
    }
    {
      final v_ = time;
      if (v_ != null) {
        w.keyInt32(219, v_);
      }
    }
    {
      final v_ = type;
      if (v_ != null) {
        w.keyInt32(221, v_);
      }
    }
    {
      final v_ = width;
      if (v_ != null) {
        w.keyInt32(223, v_);
      }
    }
    {
      final v_ = x;
      if (v_ != null) {
        w.keyInt32(224, v_);
      }
    }
    {
      final v_ = y;
      if (v_ != null) {
        w.keyInt32(226, v_);
      }
    }
  }
}
