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
      case 28: // alignment
        alignment = r.int32();
        return true;
      case 29: // backgroundImage
        backgroundImage = r.image();
        return true;
      case 30: // bottomMargin
        bottomMargin = r.int32();
        return true;
      case 31: // gradientColors
        gradientColors = r.list((r) => r.color())?.whereType<VColor>().toList();
        return true;
      case 32: // gradientPercents
        gradientPercents = r.ints();
        return true;
      case 33: // gradientVertical
        gradientVertical = r.boolean();
        return true;
      case 34: // image
        image = r.image();
        return true;
      case 35: // leftMargin
        leftMargin = r.int32();
        return true;
      case 36: // rightMargin
        rightMargin = r.int32();
        return true;
      case 37: // text
        text = r.string();
        return true;
      case 38: // topMargin
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
        w.keyInt32(28, v_);
      }
    }
    {
      final v_ = backgroundImage;
      if (v_ != null) {
        w.keyRef(29, v_.id);
      }
    }
    {
      final v_ = bottomMargin;
      if (v_ != null) {
        w.keyInt32(30, v_);
      }
    }
    {
      final v_ = gradientColors;
      if (v_ != null) {
        w.key(31);
        w.list(v_, (e) => w.colorValue(e));
      }
    }
    {
      final v_ = gradientPercents;
      if (v_ != null) {
        w.key(32);
        w.ints(v_);
      }
    }
    {
      final v_ = gradientVertical;
      if (v_ != null) {
        w.keyBoolean(33, v_);
      }
    }
    {
      final v_ = image;
      if (v_ != null) {
        w.keyRef(34, v_.id);
      }
    }
    {
      final v_ = leftMargin;
      if (v_ != null) {
        w.keyInt32(35, v_);
      }
    }
    {
      final v_ = rightMargin;
      if (v_ != null) {
        w.keyInt32(36, v_);
      }
    }
    {
      final v_ = text;
      if (v_ != null) {
        w.key(37);
        w.string(v_);
      }
    }
    {
      final v_ = topMargin;
      if (v_ != null) {
        w.keyInt32(38, v_);
      }
    }
  }

  @override
  void visitWire(void Function(int key, Object? value) visit) {
    super.visitWire(visit);
    visit(28, alignment);
    visit(29, backgroundImage);
    visit(30, bottomMargin);
    visit(31, gradientColors);
    visit(32, gradientPercents);
    visit(33, gradientVertical);
    visit(34, image);
    visit(35, leftMargin);
    visit(36, rightMargin);
    visit(37, text);
    visit(38, topMargin);
  }

  @override
  String? get semanticsText => text;
}
