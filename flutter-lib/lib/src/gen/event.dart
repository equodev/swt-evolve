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
      case 187: // button
        button = r.int32();
        return true;
      case 188: // character
        character = r.char16();
        return true;
      case 189: // count
        count = r.int32();
        return true;
      case 190: // currentDataTypeId
        currentDataTypeId = r.int32();
        return true;
      case 191: // detail
        detail = r.int32();
        return true;
      case 192: // doit
        doit = r.boolean();
        return true;
      case 194: // end
        end = r.int32();
        return true;
      case 195: // feedback
        feedback = r.int32();
        return true;
      case 197: // height
        height = r.int32();
        return true;
      case 198: // index
        index = r.int32();
        return true;
      case 200: // itemId
        itemId = r.int64();
        return true;
      case 201: // keyCode
        keyCode = r.int32();
        return true;
      case 202: // keyLocation
        keyLocation = r.int32();
        return true;
      case 206: // segments
        segments = r.ints();
        return true;
      case 208: // start
        start = r.int32();
        return true;
      case 209: // stateMask
        stateMask = r.int32();
        return true;
      case 210: // text
        text = r.string();
        return true;
      case 211: // time
        time = r.int32();
        return true;
      case 213: // type
        type = r.int32();
        return true;
      case 215: // width
        width = r.int32();
        return true;
      case 216: // x
        x = r.int32();
        return true;
      case 218: // y
        y = r.int32();
        return true;
      default:
        return false;
    }
  }

  @override
  void visitWire(void Function(int key, Object? value) visit) {
    visit(187, button);
    visit(188, character);
    visit(189, count);
    visit(190, currentDataTypeId);
    visit(191, detail);
    visit(192, doit);
    visit(194, end);
    visit(195, feedback);
    visit(197, height);
    visit(198, index);
    visit(200, itemId);
    visit(201, keyCode);
    visit(202, keyLocation);
    visit(206, segments);
    visit(208, start);
    visit(209, stateMask);
    visit(210, text);
    visit(211, time);
    visit(213, type);
    visit(215, width);
    visit(216, x);
    visit(218, y);
  }

  @override
  void writeWire(WidgetWireWriter w) {
    {
      final v_ = button;
      if (v_ != null) {
        w.keyInt32(187, v_);
      }
    }
    {
      final v_ = character;
      if (v_ != null) {
        w.keyChar16(188, v_);
      }
    }
    {
      final v_ = count;
      if (v_ != null) {
        w.keyInt32(189, v_);
      }
    }
    {
      final v_ = currentDataTypeId;
      if (v_ != null) {
        w.keyInt32(190, v_);
      }
    }
    {
      final v_ = detail;
      if (v_ != null) {
        w.keyInt32(191, v_);
      }
    }
    {
      final v_ = doit;
      if (v_ != null) {
        w.keyBoolean(192, v_);
      }
    }
    {
      final v_ = end;
      if (v_ != null) {
        w.keyInt32(194, v_);
      }
    }
    {
      final v_ = feedback;
      if (v_ != null) {
        w.keyInt32(195, v_);
      }
    }
    {
      final v_ = height;
      if (v_ != null) {
        w.keyInt32(197, v_);
      }
    }
    {
      final v_ = index;
      if (v_ != null) {
        w.keyInt32(198, v_);
      }
    }
    {
      final v_ = itemId;
      if (v_ != null) {
        w.keyInt64(200, v_);
      }
    }
    {
      final v_ = keyCode;
      if (v_ != null) {
        w.keyInt32(201, v_);
      }
    }
    {
      final v_ = keyLocation;
      if (v_ != null) {
        w.keyInt32(202, v_);
      }
    }
    {
      final v_ = segments;
      if (v_ != null) {
        w.key(206);
        w.ints(v_);
      }
    }
    {
      final v_ = start;
      if (v_ != null) {
        w.keyInt32(208, v_);
      }
    }
    {
      final v_ = stateMask;
      if (v_ != null) {
        w.keyInt32(209, v_);
      }
    }
    {
      final v_ = text;
      if (v_ != null) {
        w.key(210);
        w.string(v_);
      }
    }
    {
      final v_ = time;
      if (v_ != null) {
        w.keyInt32(211, v_);
      }
    }
    {
      final v_ = type;
      if (v_ != null) {
        w.keyInt32(213, v_);
      }
    }
    {
      final v_ = width;
      if (v_ != null) {
        w.keyInt32(215, v_);
      }
    }
    {
      final v_ = x;
      if (v_ != null) {
        w.keyInt32(216, v_);
      }
    }
    {
      final v_ = y;
      if (v_ != null) {
        w.keyInt32(218, v_);
      }
    }
  }
}
