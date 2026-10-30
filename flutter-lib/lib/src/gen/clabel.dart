import 'package:flutter/widgets.dart';
import '../comm/widget_wire.dart';
import '../gen/canvas.dart';
import '../gen/caret.dart';
import '../gen/color.dart';
import '../gen/control.dart';
import '../gen/cursor.dart';
import '../gen/font.dart';
import '../gen/image.dart';
import '../gen/menu.dart';
import '../gen/rectangle.dart';
import '../gen/region.dart';
import '../gen/scrollbar.dart';
import '../gen/widget.dart';
import '../gen/widgets.dart';
import '../impl/clabel_evolve.dart';
import 'widgets.dart';

class CLabelSwt<V extends VCLabel> extends CanvasSwt<V> {
  const CLabelSwt({super.key, required super.value});

  @override
  State createState() => CLabelImpl<CLabelSwt<VCLabel>, VCLabel>();
}

class VCLabel extends VCanvas {
  VCLabel() : this.empty();
  VCLabel.empty() {
    swt = "CLabel";
  }

  int? alignment;
  VImage? backgroundImage;
  int? bottomMargin;
  List<VColor>? gradientColors;
  List<int>? gradientPercents;
  bool? gradientVertical;
  VImage? image;
  int? leftMargin;
  int? rightMargin;
  String? text;
  int? topMargin;

  @override
  void copyFrom(VWidget other) {
    super.copyFrom(other);
    if (other is VCLabel) {
      alignment = other.alignment;
      backgroundImage = other.backgroundImage;
      bottomMargin = other.bottomMargin;
      gradientColors = other.gradientColors;
      gradientPercents = other.gradientPercents;
      gradientVertical = other.gradientVertical;
      image = other.image;
      leftMargin = other.leftMargin;
      rightMargin = other.rightMargin;
      text = other.text;
      topMargin = other.topMargin;
    }
  }

  @override
  bool readWire(int key, WidgetWireReader r) {
    switch (key) {
      case 25: // alignment
        alignment = r.int32();
        return true;
      case 26: // backgroundImage
        backgroundImage = r.image();
        return true;
      case 27: // bottomMargin
        bottomMargin = r.int32();
        return true;
      case 28: // gradientColors
        gradientColors = r.list((r) => r.color())?.whereType<VColor>().toList();
        return true;
      case 29: // gradientPercents
        gradientPercents = r.ints();
        return true;
      case 30: // gradientVertical
        gradientVertical = r.boolean();
        return true;
      case 31: // image
        image = r.image();
        return true;
      case 32: // leftMargin
        leftMargin = r.int32();
        return true;
      case 33: // rightMargin
        rightMargin = r.int32();
        return true;
      case 34: // text
        text = r.string();
        return true;
      case 35: // topMargin
        topMargin = r.int32();
        return true;
      default:
        return super.readWire(key, r);
    }
  }

  @override
  void writeWire(WidgetWireWriter w) {
    super.writeWire(w);
    {
      final v_ = alignment;
      if (v_ != null) {
        w.keyInt32(25, v_);
      }
    }
    {
      final v_ = backgroundImage;
      if (v_ != null) {
        w.keyRef(26, v_.id);
      }
    }
    {
      final v_ = bottomMargin;
      if (v_ != null) {
        w.keyInt32(27, v_);
      }
    }
    {
      final v_ = gradientColors;
      if (v_ != null) {
        w.key(28);
        w.list(v_, (e) => w.colorValue(e));
      }
    }
    {
      final v_ = gradientPercents;
      if (v_ != null) {
        w.key(29);
        w.ints(v_);
      }
    }
    {
      final v_ = gradientVertical;
      if (v_ != null) {
        w.keyBoolean(30, v_);
      }
    }
    {
      final v_ = image;
      if (v_ != null) {
        w.keyRef(31, v_.id);
      }
    }
    {
      final v_ = leftMargin;
      if (v_ != null) {
        w.keyInt32(32, v_);
      }
    }
    {
      final v_ = rightMargin;
      if (v_ != null) {
        w.keyInt32(33, v_);
      }
    }
    {
      final v_ = text;
      if (v_ != null) {
        w.key(34);
        w.string(v_);
      }
    }
    {
      final v_ = topMargin;
      if (v_ != null) {
        w.keyInt32(35, v_);
      }
    }
  }

  @override
  void visitWire(void Function(int key, Object? value) visit) {
    super.visitWire(visit);
    visit(25, alignment);
    visit(26, backgroundImage);
    visit(27, bottomMargin);
    visit(28, gradientColors);
    visit(29, gradientPercents);
    visit(30, gradientVertical);
    visit(31, image);
    visit(32, leftMargin);
    visit(33, rightMargin);
    visit(34, text);
    visit(35, topMargin);
  }

  @override
  String? get semanticsText => text;
}
