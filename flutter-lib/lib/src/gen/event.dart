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
      case 184: // button
        button = r.int32();
        return true;
      case 185: // character
        character = r.char16();
        return true;
      case 186: // count
        count = r.int32();
        return true;
      case 187: // currentDataTypeId
        currentDataTypeId = r.int32();
        return true;
      case 188: // detail
        detail = r.int32();
        return true;
      case 189: // doit
        doit = r.boolean();
        return true;
      case 191: // end
        end = r.int32();
        return true;
      case 192: // feedback
        feedback = r.int32();
        return true;
      case 194: // height
        height = r.int32();
        return true;
      case 195: // index
        index = r.int32();
        return true;
      case 197: // itemId
        itemId = r.int64();
        return true;
      case 198: // keyCode
        keyCode = r.int32();
        return true;
      case 199: // keyLocation
        keyLocation = r.int32();
        return true;
      case 203: // segments
        segments = r.ints();
        return true;
      case 205: // start
        start = r.int32();
        return true;
      case 206: // stateMask
        stateMask = r.int32();
        return true;
      case 207: // text
        text = r.string();
        return true;
      case 208: // time
        time = r.int32();
        return true;
      case 210: // type
        type = r.int32();
        return true;
      case 212: // width
        width = r.int32();
        return true;
      case 213: // x
        x = r.int32();
        return true;
      case 215: // y
        y = r.int32();
        return true;
      default:
        return false;
    }
  }

  @override
  void visitWire(void Function(int key, Object? value) visit) {
    visit(184, button);
    visit(185, character);
    visit(186, count);
    visit(187, currentDataTypeId);
    visit(188, detail);
    visit(189, doit);
    visit(191, end);
    visit(192, feedback);
    visit(194, height);
    visit(195, index);
    visit(197, itemId);
    visit(198, keyCode);
    visit(199, keyLocation);
    visit(203, segments);
    visit(205, start);
    visit(206, stateMask);
    visit(207, text);
    visit(208, time);
    visit(210, type);
    visit(212, width);
    visit(213, x);
    visit(215, y);
  }

  @override
  void writeWire(WidgetWireWriter w) {
    {
      final v_ = button;
      if (v_ != null) {
        w.keyInt32(184, v_);
      }
    }
    {
      final v_ = character;
      if (v_ != null) {
        w.keyChar16(185, v_);
      }
    }
    {
      final v_ = count;
      if (v_ != null) {
        w.keyInt32(186, v_);
      }
    }
    {
      final v_ = currentDataTypeId;
      if (v_ != null) {
        w.keyInt32(187, v_);
      }
    }
    {
      final v_ = detail;
      if (v_ != null) {
        w.keyInt32(188, v_);
      }
    }
    {
      final v_ = doit;
      if (v_ != null) {
        w.keyBoolean(189, v_);
      }
    }
    {
      final v_ = end;
      if (v_ != null) {
        w.keyInt32(191, v_);
      }
    }
    {
      final v_ = feedback;
      if (v_ != null) {
        w.keyInt32(192, v_);
      }
    }
    {
      final v_ = height;
      if (v_ != null) {
        w.keyInt32(194, v_);
      }
    }
    {
      final v_ = index;
      if (v_ != null) {
        w.keyInt32(195, v_);
      }
    }
    {
      final v_ = itemId;
      if (v_ != null) {
        w.keyInt64(197, v_);
      }
    }
    {
      final v_ = keyCode;
      if (v_ != null) {
        w.keyInt32(198, v_);
      }
    }
    {
      final v_ = keyLocation;
      if (v_ != null) {
        w.keyInt32(199, v_);
      }
    }
    {
      final v_ = segments;
      if (v_ != null) {
        w.key(203);
        w.ints(v_);
      }
    }
    {
      final v_ = start;
      if (v_ != null) {
        w.keyInt32(205, v_);
      }
    }
    {
      final v_ = stateMask;
      if (v_ != null) {
        w.keyInt32(206, v_);
      }
    }
    {
      final v_ = text;
      if (v_ != null) {
        w.key(207);
        w.string(v_);
      }
    }
    {
      final v_ = time;
      if (v_ != null) {
        w.keyInt32(208, v_);
      }
    }
    {
      final v_ = type;
      if (v_ != null) {
        w.keyInt32(210, v_);
      }
    }
    {
      final v_ = width;
      if (v_ != null) {
        w.keyInt32(212, v_);
      }
    }
    {
      final v_ = x;
      if (v_ != null) {
        w.keyInt32(213, v_);
      }
    }
    {
      final v_ = y;
      if (v_ != null) {
        w.keyInt32(215, v_);
      }
    }
  }
}
