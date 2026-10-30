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
      case 541: // alignment
        alignment = r.int32();
        return true;
      case 542: // backgroundImage
        backgroundImage = r.image();
        return true;
      case 543: // bottomMargin
        bottomMargin = r.int32();
        return true;
      case 1357: // gradientColors
        gradientColors = r.list(() => r.color())?.whereType<VColor>().toList();
        return true;
      case 1358: // gradientPercents
        gradientPercents = r.ints();
        return true;
      case 1359: // gradientVertical
        gradientVertical = r.boolean();
        return true;
      case 544: // image
        image = r.image();
        return true;
      case 545: // leftMargin
        leftMargin = r.int32();
        return true;
      case 546: // rightMargin
        rightMargin = r.int32();
        return true;
      case 547: // text
        text = r.string();
        return true;
      case 548: // topMargin
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
      if (v_ != null && w.wants(541)) {
        w.key(541);
        w.int32(v_);
      }
    }
    {
      final v_ = backgroundImage;
      if (v_ != null && w.wants(542)) {
        w.key(542);
        w.ref(v_.id);
      }
    }
    {
      final v_ = bottomMargin;
      if (v_ != null && w.wants(543)) {
        w.key(543);
        w.int32(v_);
      }
    }
    {
      final v_ = gradientColors;
      if (v_ != null && w.wants(1357)) {
        w.key(1357);
        w.list(v_, (e) => w.colorValue(e));
      }
    }
    {
      final v_ = gradientPercents;
      if (v_ != null && w.wants(1358)) {
        w.key(1358);
        w.ints(v_);
      }
    }
    {
      final v_ = gradientVertical;
      if (v_ != null && w.wants(1359)) {
        w.key(1359);
        w.boolean(v_);
      }
    }
    {
      final v_ = image;
      if (v_ != null && w.wants(544)) {
        w.key(544);
        w.ref(v_.id);
      }
    }
    {
      final v_ = leftMargin;
      if (v_ != null && w.wants(545)) {
        w.key(545);
        w.int32(v_);
      }
    }
    {
      final v_ = rightMargin;
      if (v_ != null && w.wants(546)) {
        w.key(546);
        w.int32(v_);
      }
    }
    {
      final v_ = text;
      if (v_ != null && w.wants(547)) {
        w.key(547);
        w.string(v_);
      }
    }
    {
      final v_ = topMargin;
      if (v_ != null && w.wants(548)) {
        w.key(548);
        w.int32(v_);
      }
    }
  }

  @override
  void visitWire(void Function(int key, Object? value) visit) {
    super.visitWire(visit);
    visit(541, alignment);
    visit(542, backgroundImage);
    visit(543, bottomMargin);
    visit(1357, gradientColors);
    visit(1358, gradientPercents);
    visit(1359, gradientVertical);
    visit(544, image);
    visit(545, leftMargin);
    visit(546, rightMargin);
    visit(547, text);
    visit(548, topMargin);
  }

  @override
  String? get semanticsText => text;
}
