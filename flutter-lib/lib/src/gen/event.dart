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
      case 331: // button
        button = r.int32();
        return true;
      case 332: // character
        character = r.char16();
        return true;
      case 333: // count
        count = r.int32();
        return true;
      case 334: // currentDataTypeId
        currentDataTypeId = r.int32();
        return true;
      case 335: // detail
        detail = r.int32();
        return true;
      case 336: // doit
        doit = r.boolean();
        return true;
      case 338: // end
        end = r.int32();
        return true;
      case 339: // feedback
        feedback = r.int32();
        return true;
      case 341: // height
        height = r.int32();
        return true;
      case 342: // index
        index = r.int32();
        return true;
      case 344: // itemId
        itemId = r.int64();
        return true;
      case 345: // keyCode
        keyCode = r.int32();
        return true;
      case 346: // keyLocation
        keyLocation = r.int32();
        return true;
      case 350: // segments
        segments = r.ints();
        return true;
      case 352: // start
        start = r.int32();
        return true;
      case 353: // stateMask
        stateMask = r.int32();
        return true;
      case 354: // text
        text = r.string();
        return true;
      case 355: // time
        time = r.int32();
        return true;
      case 357: // type
        type = r.int32();
        return true;
      case 359: // width
        width = r.int32();
        return true;
      case 360: // x
        x = r.int32();
        return true;
      case 362: // y
        y = r.int32();
        return true;
      default:
        return false;
    }
  }

  @override
  void visitWire(void Function(int key, Object? value) visit) {
    visit(331, button);
    visit(332, character);
    visit(333, count);
    visit(334, currentDataTypeId);
    visit(335, detail);
    visit(336, doit);
    visit(338, end);
    visit(339, feedback);
    visit(341, height);
    visit(342, index);
    visit(344, itemId);
    visit(345, keyCode);
    visit(346, keyLocation);
    visit(350, segments);
    visit(352, start);
    visit(353, stateMask);
    visit(354, text);
    visit(355, time);
    visit(357, type);
    visit(359, width);
    visit(360, x);
    visit(362, y);
  }

  @override
  void writeWire(WidgetWireWriter w) {
    {
      final v_ = button;
      if (v_ != null) {
        w.key(331);
        w.int32(v_);
      }
    }
    {
      final v_ = character;
      if (v_ != null) {
        w.key(332);
        w.char16(v_);
      }
    }
    {
      final v_ = count;
      if (v_ != null) {
        w.key(333);
        w.int32(v_);
      }
    }
    {
      final v_ = currentDataTypeId;
      if (v_ != null) {
        w.key(334);
        w.int32(v_);
      }
    }
    {
      final v_ = detail;
      if (v_ != null) {
        w.key(335);
        w.int32(v_);
      }
    }
    {
      final v_ = doit;
      if (v_ != null) {
        w.key(336);
        w.boolean(v_);
      }
    }
    {
      final v_ = end;
      if (v_ != null) {
        w.key(338);
        w.int32(v_);
      }
    }
    {
      final v_ = feedback;
      if (v_ != null) {
        w.key(339);
        w.int32(v_);
      }
    }
    {
      final v_ = height;
      if (v_ != null) {
        w.key(341);
        w.int32(v_);
      }
    }
    {
      final v_ = index;
      if (v_ != null) {
        w.key(342);
        w.int32(v_);
      }
    }
    {
      final v_ = itemId;
      if (v_ != null) {
        w.key(344);
        w.int64(v_);
      }
    }
    {
      final v_ = keyCode;
      if (v_ != null) {
        w.key(345);
        w.int32(v_);
      }
    }
    {
      final v_ = keyLocation;
      if (v_ != null) {
        w.key(346);
        w.int32(v_);
      }
    }
    {
      final v_ = segments;
      if (v_ != null) {
        w.key(350);
        w.ints(v_);
      }
    }
    {
      final v_ = start;
      if (v_ != null) {
        w.key(352);
        w.int32(v_);
      }
    }
    {
      final v_ = stateMask;
      if (v_ != null) {
        w.key(353);
        w.int32(v_);
      }
    }
    {
      final v_ = text;
      if (v_ != null) {
        w.key(354);
        w.string(v_);
      }
    }
    {
      final v_ = time;
      if (v_ != null) {
        w.key(355);
        w.int32(v_);
      }
    }
    {
      final v_ = type;
      if (v_ != null) {
        w.key(357);
        w.int32(v_);
      }
    }
    {
      final v_ = width;
      if (v_ != null) {
        w.key(359);
        w.int32(v_);
      }
    }
    {
      final v_ = x;
      if (v_ != null) {
        w.key(360);
        w.int32(v_);
      }
    }
    {
      final v_ = y;
      if (v_ != null) {
        w.key(362);
        w.int32(v_);
      }
    }
  }
}
