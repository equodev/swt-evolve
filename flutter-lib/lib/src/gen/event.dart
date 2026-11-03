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
      case 186: // button
        button = r.int32();
        return true;
      case 187: // character
        character = r.char16();
        return true;
      case 188: // count
        count = r.int32();
        return true;
      case 189: // currentDataTypeId
        currentDataTypeId = r.int32();
        return true;
      case 190: // detail
        detail = r.int32();
        return true;
      case 191: // doit
        doit = r.boolean();
        return true;
      case 193: // end
        end = r.int32();
        return true;
      case 194: // feedback
        feedback = r.int32();
        return true;
      case 196: // height
        height = r.int32();
        return true;
      case 197: // index
        index = r.int32();
        return true;
      case 199: // itemId
        itemId = r.int64();
        return true;
      case 200: // keyCode
        keyCode = r.int32();
        return true;
      case 201: // keyLocation
        keyLocation = r.int32();
        return true;
      case 205: // segments
        segments = r.ints();
        return true;
      case 207: // start
        start = r.int32();
        return true;
      case 208: // stateMask
        stateMask = r.int32();
        return true;
      case 209: // text
        text = r.string();
        return true;
      case 210: // time
        time = r.int32();
        return true;
      case 212: // type
        type = r.int32();
        return true;
      case 214: // width
        width = r.int32();
        return true;
      case 215: // x
        x = r.int32();
        return true;
      case 217: // y
        y = r.int32();
        return true;
      default:
        return false;
    }
  }

  @override
  void visitWire(void Function(int key, Object? value) visit) {
    visit(186, button);
    visit(187, character);
    visit(188, count);
    visit(189, currentDataTypeId);
    visit(190, detail);
    visit(191, doit);
    visit(193, end);
    visit(194, feedback);
    visit(196, height);
    visit(197, index);
    visit(199, itemId);
    visit(200, keyCode);
    visit(201, keyLocation);
    visit(205, segments);
    visit(207, start);
    visit(208, stateMask);
    visit(209, text);
    visit(210, time);
    visit(212, type);
    visit(214, width);
    visit(215, x);
    visit(217, y);
  }

  @override
  void writeWire(WidgetWireWriter w) {
    {
      final v_ = button;
      if (v_ != null) {
        w.keyInt32(186, v_);
      }
    }
    {
      final v_ = character;
      if (v_ != null) {
        w.keyChar16(187, v_);
      }
    }
    {
      final v_ = count;
      if (v_ != null) {
        w.keyInt32(188, v_);
      }
    }
    {
      final v_ = currentDataTypeId;
      if (v_ != null) {
        w.keyInt32(189, v_);
      }
    }
    {
      final v_ = detail;
      if (v_ != null) {
        w.keyInt32(190, v_);
      }
    }
    {
      final v_ = doit;
      if (v_ != null) {
        w.keyBoolean(191, v_);
      }
    }
    {
      final v_ = end;
      if (v_ != null) {
        w.keyInt32(193, v_);
      }
    }
    {
      final v_ = feedback;
      if (v_ != null) {
        w.keyInt32(194, v_);
      }
    }
    {
      final v_ = height;
      if (v_ != null) {
        w.keyInt32(196, v_);
      }
    }
    {
      final v_ = index;
      if (v_ != null) {
        w.keyInt32(197, v_);
      }
    }
    {
      final v_ = itemId;
      if (v_ != null) {
        w.keyInt64(199, v_);
      }
    }
    {
      final v_ = keyCode;
      if (v_ != null) {
        w.keyInt32(200, v_);
      }
    }
    {
      final v_ = keyLocation;
      if (v_ != null) {
        w.keyInt32(201, v_);
      }
    }
    {
      final v_ = segments;
      if (v_ != null) {
        w.key(205);
        w.ints(v_);
      }
    }
    {
      final v_ = start;
      if (v_ != null) {
        w.keyInt32(207, v_);
      }
    }
    {
      final v_ = stateMask;
      if (v_ != null) {
        w.keyInt32(208, v_);
      }
    }
    {
      final v_ = text;
      if (v_ != null) {
        w.key(209);
        w.string(v_);
      }
    }
    {
      final v_ = time;
      if (v_ != null) {
        w.keyInt32(210, v_);
      }
    }
    {
      final v_ = type;
      if (v_ != null) {
        w.keyInt32(212, v_);
      }
    }
    {
      final v_ = width;
      if (v_ != null) {
        w.keyInt32(214, v_);
      }
    }
    {
      final v_ = x;
      if (v_ != null) {
        w.keyInt32(215, v_);
      }
    }
    {
      final v_ = y;
      if (v_ != null) {
        w.keyInt32(217, v_);
      }
    }
  }
}
