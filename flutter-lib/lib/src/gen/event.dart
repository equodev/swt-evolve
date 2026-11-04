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
      case 188: // button
        button = r.int32();
        return true;
      case 189: // character
        character = r.char16();
        return true;
      case 190: // count
        count = r.int32();
        return true;
      case 191: // currentDataTypeId
        currentDataTypeId = r.int32();
        return true;
      case 192: // detail
        detail = r.int32();
        return true;
      case 193: // doit
        doit = r.boolean();
        return true;
      case 195: // end
        end = r.int32();
        return true;
      case 196: // feedback
        feedback = r.int32();
        return true;
      case 198: // height
        height = r.int32();
        return true;
      case 199: // index
        index = r.int32();
        return true;
      case 201: // itemId
        itemId = r.int64();
        return true;
      case 202: // keyCode
        keyCode = r.int32();
        return true;
      case 203: // keyLocation
        keyLocation = r.int32();
        return true;
      case 207: // segments
        segments = r.ints();
        return true;
      case 209: // start
        start = r.int32();
        return true;
      case 210: // stateMask
        stateMask = r.int32();
        return true;
      case 211: // text
        text = r.string();
        return true;
      case 212: // time
        time = r.int32();
        return true;
      case 214: // type
        type = r.int32();
        return true;
      case 216: // width
        width = r.int32();
        return true;
      case 217: // x
        x = r.int32();
        return true;
      case 219: // y
        y = r.int32();
        return true;
      default:
        return false;
    }
  }

  @override
  void visitWire(void Function(int key, Object? value) visit) {
    visit(188, button);
    visit(189, character);
    visit(190, count);
    visit(191, currentDataTypeId);
    visit(192, detail);
    visit(193, doit);
    visit(195, end);
    visit(196, feedback);
    visit(198, height);
    visit(199, index);
    visit(201, itemId);
    visit(202, keyCode);
    visit(203, keyLocation);
    visit(207, segments);
    visit(209, start);
    visit(210, stateMask);
    visit(211, text);
    visit(212, time);
    visit(214, type);
    visit(216, width);
    visit(217, x);
    visit(219, y);
  }

  @override
  void writeWire(WidgetWireWriter w) {
    {
      final v_ = button;
      if (v_ != null) {
        w.keyInt32(188, v_);
      }
    }
    {
      final v_ = character;
      if (v_ != null) {
        w.keyChar16(189, v_);
      }
    }
    {
      final v_ = count;
      if (v_ != null) {
        w.keyInt32(190, v_);
      }
    }
    {
      final v_ = currentDataTypeId;
      if (v_ != null) {
        w.keyInt32(191, v_);
      }
    }
    {
      final v_ = detail;
      if (v_ != null) {
        w.keyInt32(192, v_);
      }
    }
    {
      final v_ = doit;
      if (v_ != null) {
        w.keyBoolean(193, v_);
      }
    }
    {
      final v_ = end;
      if (v_ != null) {
        w.keyInt32(195, v_);
      }
    }
    {
      final v_ = feedback;
      if (v_ != null) {
        w.keyInt32(196, v_);
      }
    }
    {
      final v_ = height;
      if (v_ != null) {
        w.keyInt32(198, v_);
      }
    }
    {
      final v_ = index;
      if (v_ != null) {
        w.keyInt32(199, v_);
      }
    }
    {
      final v_ = itemId;
      if (v_ != null) {
        w.keyInt64(201, v_);
      }
    }
    {
      final v_ = keyCode;
      if (v_ != null) {
        w.keyInt32(202, v_);
      }
    }
    {
      final v_ = keyLocation;
      if (v_ != null) {
        w.keyInt32(203, v_);
      }
    }
    {
      final v_ = segments;
      if (v_ != null) {
        w.key(207);
        w.ints(v_);
      }
    }
    {
      final v_ = start;
      if (v_ != null) {
        w.keyInt32(209, v_);
      }
    }
    {
      final v_ = stateMask;
      if (v_ != null) {
        w.keyInt32(210, v_);
      }
    }
    {
      final v_ = text;
      if (v_ != null) {
        w.key(211);
        w.string(v_);
      }
    }
    {
      final v_ = time;
      if (v_ != null) {
        w.keyInt32(212, v_);
      }
    }
    {
      final v_ = type;
      if (v_ != null) {
        w.keyInt32(214, v_);
      }
    }
    {
      final v_ = width;
      if (v_ != null) {
        w.keyInt32(216, v_);
      }
    }
    {
      final v_ = x;
      if (v_ != null) {
        w.keyInt32(217, v_);
      }
    }
    {
      final v_ = y;
      if (v_ != null) {
        w.keyInt32(219, v_);
      }
    }
  }
}
