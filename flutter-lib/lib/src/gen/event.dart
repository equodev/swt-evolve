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
      case 193: // button
        button = r.int32();
        return true;
      case 194: // character
        character = r.char16();
        return true;
      case 195: // count
        count = r.int32();
        return true;
      case 196: // currentDataTypeId
        currentDataTypeId = r.int32();
        return true;
      case 197: // detail
        detail = r.int32();
        return true;
      case 198: // doit
        doit = r.boolean();
        return true;
      case 200: // end
        end = r.int32();
        return true;
      case 201: // feedback
        feedback = r.int32();
        return true;
      case 203: // height
        height = r.int32();
        return true;
      case 204: // index
        index = r.int32();
        return true;
      case 206: // itemId
        itemId = r.int64();
        return true;
      case 207: // keyCode
        keyCode = r.int32();
        return true;
      case 208: // keyLocation
        keyLocation = r.int32();
        return true;
      case 212: // segments
        segments = r.ints();
        return true;
      case 214: // start
        start = r.int32();
        return true;
      case 215: // stateMask
        stateMask = r.int32();
        return true;
      case 216: // text
        text = r.string();
        return true;
      case 217: // time
        time = r.int32();
        return true;
      case 219: // type
        type = r.int32();
        return true;
      case 221: // width
        width = r.int32();
        return true;
      case 222: // x
        x = r.int32();
        return true;
      case 224: // y
        y = r.int32();
        return true;
      default:
        return false;
    }
  }

  @override
  void visitWire(void Function(int key, Object? value) visit) {
    visit(193, button);
    visit(194, character);
    visit(195, count);
    visit(196, currentDataTypeId);
    visit(197, detail);
    visit(198, doit);
    visit(200, end);
    visit(201, feedback);
    visit(203, height);
    visit(204, index);
    visit(206, itemId);
    visit(207, keyCode);
    visit(208, keyLocation);
    visit(212, segments);
    visit(214, start);
    visit(215, stateMask);
    visit(216, text);
    visit(217, time);
    visit(219, type);
    visit(221, width);
    visit(222, x);
    visit(224, y);
  }

  @override
  void writeWire(WidgetWireWriter w) {
    {
      final v_ = button;
      if (v_ != null) {
        w.keyInt32(193, v_);
      }
    }
    {
      final v_ = character;
      if (v_ != null) {
        w.keyChar16(194, v_);
      }
    }
    {
      final v_ = count;
      if (v_ != null) {
        w.keyInt32(195, v_);
      }
    }
    {
      final v_ = currentDataTypeId;
      if (v_ != null) {
        w.keyInt32(196, v_);
      }
    }
    {
      final v_ = detail;
      if (v_ != null) {
        w.keyInt32(197, v_);
      }
    }
    {
      final v_ = doit;
      if (v_ != null) {
        w.keyBoolean(198, v_);
      }
    }
    {
      final v_ = end;
      if (v_ != null) {
        w.keyInt32(200, v_);
      }
    }
    {
      final v_ = feedback;
      if (v_ != null) {
        w.keyInt32(201, v_);
      }
    }
    {
      final v_ = height;
      if (v_ != null) {
        w.keyInt32(203, v_);
      }
    }
    {
      final v_ = index;
      if (v_ != null) {
        w.keyInt32(204, v_);
      }
    }
    {
      final v_ = itemId;
      if (v_ != null) {
        w.keyInt64(206, v_);
      }
    }
    {
      final v_ = keyCode;
      if (v_ != null) {
        w.keyInt32(207, v_);
      }
    }
    {
      final v_ = keyLocation;
      if (v_ != null) {
        w.keyInt32(208, v_);
      }
    }
    {
      final v_ = segments;
      if (v_ != null) {
        w.key(212);
        w.ints(v_);
      }
    }
    {
      final v_ = start;
      if (v_ != null) {
        w.keyInt32(214, v_);
      }
    }
    {
      final v_ = stateMask;
      if (v_ != null) {
        w.keyInt32(215, v_);
      }
    }
    {
      final v_ = text;
      if (v_ != null) {
        w.key(216);
        w.string(v_);
      }
    }
    {
      final v_ = time;
      if (v_ != null) {
        w.keyInt32(217, v_);
      }
    }
    {
      final v_ = type;
      if (v_ != null) {
        w.keyInt32(219, v_);
      }
    }
    {
      final v_ = width;
      if (v_ != null) {
        w.keyInt32(221, v_);
      }
    }
    {
      final v_ = x;
      if (v_ != null) {
        w.keyInt32(222, v_);
      }
    }
    {
      final v_ = y;
      if (v_ != null) {
        w.keyInt32(224, v_);
      }
    }
  }
}
