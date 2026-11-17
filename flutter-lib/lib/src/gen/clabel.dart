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
      case 448: // alignment
        alignment = r.int32();
        return true;
      case 449: // backgroundImage
        backgroundImage = r.image();
        return true;
      case 450: // bottomMargin
        bottomMargin = r.int32();
        return true;
      case 451: // gradientColors
        gradientColors = r.list((r) => r.color())?.whereType<VColor>().toList();
        return true;
      case 452: // gradientPercents
        gradientPercents = r.ints();
        return true;
      case 453: // gradientVertical
        gradientVertical = r.boolean();
        return true;
      case 454: // image
        image = r.image();
        return true;
      case 455: // leftMargin
        leftMargin = r.int32();
        return true;
      case 456: // rightMargin
        rightMargin = r.int32();
        return true;
      case 457: // text
        text = r.string();
        return true;
      case 458: // topMargin
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
        w.keyInt32(448, v_);
      }
    }
    {
      final v_ = backgroundImage;
      if (v_ != null) {
        w.keyRef(449, v_.id);
      }
    }
    {
      final v_ = bottomMargin;
      if (v_ != null) {
        w.keyInt32(450, v_);
      }
    }
    {
      final v_ = gradientColors;
      if (v_ != null) {
        w.key(451);
        w.list(v_, (e) => w.colorValue(e));
      }
    }
    {
      final v_ = gradientPercents;
      if (v_ != null) {
        w.key(452);
        w.ints(v_);
      }
    }
    {
      final v_ = gradientVertical;
      if (v_ != null) {
        w.keyBoolean(453, v_);
      }
    }
    {
      final v_ = image;
      if (v_ != null) {
        w.keyRef(454, v_.id);
      }
    }
    {
      final v_ = leftMargin;
      if (v_ != null) {
        w.keyInt32(455, v_);
      }
    }
    {
      final v_ = rightMargin;
      if (v_ != null) {
        w.keyInt32(456, v_);
      }
    }
    {
      final v_ = text;
      if (v_ != null) {
        w.key(457);
        w.string(v_);
      }
    }
    {
      final v_ = topMargin;
      if (v_ != null) {
        w.keyInt32(458, v_);
      }
    }
  }

  @override
  void visitWire(void Function(int key, Object? value) visit) {
    super.visitWire(visit);
    visit(448, alignment);
    visit(449, backgroundImage);
    visit(450, bottomMargin);
    visit(451, gradientColors);
    visit(452, gradientPercents);
    visit(453, gradientVertical);
    visit(454, image);
    visit(455, leftMargin);
    visit(456, rightMargin);
    visit(457, text);
    visit(458, topMargin);
  }

  @override
  String? get semanticsText => text;
}
