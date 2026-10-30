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
      case 185: // button
        button = r.int32();
        return true;
      case 186: // character
        character = r.char16();
        return true;
      case 187: // count
        count = r.int32();
        return true;
      case 188: // currentDataTypeId
        currentDataTypeId = r.int32();
        return true;
      case 189: // detail
        detail = r.int32();
        return true;
      case 190: // doit
        doit = r.boolean();
        return true;
      case 192: // end
        end = r.int32();
        return true;
      case 193: // feedback
        feedback = r.int32();
        return true;
      case 195: // height
        height = r.int32();
        return true;
      case 196: // index
        index = r.int32();
        return true;
      case 198: // itemId
        itemId = r.int64();
        return true;
      case 199: // keyCode
        keyCode = r.int32();
        return true;
      case 200: // keyLocation
        keyLocation = r.int32();
        return true;
      case 204: // segments
        segments = r.ints();
        return true;
      case 206: // start
        start = r.int32();
        return true;
      case 207: // stateMask
        stateMask = r.int32();
        return true;
      case 208: // text
        text = r.string();
        return true;
      case 209: // time
        time = r.int32();
        return true;
      case 211: // type
        type = r.int32();
        return true;
      case 213: // width
        width = r.int32();
        return true;
      case 214: // x
        x = r.int32();
        return true;
      case 216: // y
        y = r.int32();
        return true;
      default:
        return false;
    }
  }

  @override
  void visitWire(void Function(int key, Object? value) visit) {
    visit(185, button);
    visit(186, character);
    visit(187, count);
    visit(188, currentDataTypeId);
    visit(189, detail);
    visit(190, doit);
    visit(192, end);
    visit(193, feedback);
    visit(195, height);
    visit(196, index);
    visit(198, itemId);
    visit(199, keyCode);
    visit(200, keyLocation);
    visit(204, segments);
    visit(206, start);
    visit(207, stateMask);
    visit(208, text);
    visit(209, time);
    visit(211, type);
    visit(213, width);
    visit(214, x);
    visit(216, y);
  }

  @override
  void writeWire(WidgetWireWriter w) {
    {
      final v_ = button;
      if (v_ != null) {
        w.keyInt32(185, v_);
      }
    }
    {
      final v_ = character;
      if (v_ != null) {
        w.keyChar16(186, v_);
      }
    }
    {
      final v_ = count;
      if (v_ != null) {
        w.keyInt32(187, v_);
      }
    }
    {
      final v_ = currentDataTypeId;
      if (v_ != null) {
        w.keyInt32(188, v_);
      }
    }
    {
      final v_ = detail;
      if (v_ != null) {
        w.keyInt32(189, v_);
      }
    }
    {
      final v_ = doit;
      if (v_ != null) {
        w.keyBoolean(190, v_);
      }
    }
    {
      final v_ = end;
      if (v_ != null) {
        w.keyInt32(192, v_);
      }
    }
    {
      final v_ = feedback;
      if (v_ != null) {
        w.keyInt32(193, v_);
      }
    }
    {
      final v_ = height;
      if (v_ != null) {
        w.keyInt32(195, v_);
      }
    }
    {
      final v_ = index;
      if (v_ != null) {
        w.keyInt32(196, v_);
      }
    }
    {
      final v_ = itemId;
      if (v_ != null) {
        w.keyInt64(198, v_);
      }
    }
    {
      final v_ = keyCode;
      if (v_ != null) {
        w.keyInt32(199, v_);
      }
    }
    {
      final v_ = keyLocation;
      if (v_ != null) {
        w.keyInt32(200, v_);
      }
    }
    {
      final v_ = segments;
      if (v_ != null) {
        w.key(204);
        w.ints(v_);
      }
    }
    {
      final v_ = start;
      if (v_ != null) {
        w.keyInt32(206, v_);
      }
    }
    {
      final v_ = stateMask;
      if (v_ != null) {
        w.keyInt32(207, v_);
      }
    }
    {
      final v_ = text;
      if (v_ != null) {
        w.key(208);
        w.string(v_);
      }
    }
    {
      final v_ = time;
      if (v_ != null) {
        w.keyInt32(209, v_);
      }
    }
    {
      final v_ = type;
      if (v_ != null) {
        w.keyInt32(211, v_);
      }
    }
    {
      final v_ = width;
      if (v_ != null) {
        w.keyInt32(213, v_);
      }
    }
    {
      final v_ = x;
      if (v_ != null) {
        w.keyInt32(214, v_);
      }
    }
    {
      final v_ = y;
      if (v_ != null) {
        w.keyInt32(216, v_);
      }
    }
  }
}
