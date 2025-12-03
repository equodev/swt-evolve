import 'package:flutter/widgets.dart';
import 'package:json_annotation/json_annotation.dart';
import '../gen/color.dart';
import '../gen/composite.dart';
import '../gen/control.dart';
import '../gen/font.dart';
import '../gen/image.dart';
import '../gen/point.dart';
import '../gen/rectangle.dart';
import '../impl/scrolledcomposite_evolve.dart';
import 'widgets.dart';

part 'scrolledcomposite.g.dart';

class ScrolledCompositeSwt<V extends VScrolledComposite>
    extends CompositeSwt<V> {
  const ScrolledCompositeSwt({super.key, required super.value});

  @override
  State createState() => ScrolledCompositeImpl<
      ScrolledCompositeSwt<VScrolledComposite>, VScrolledComposite>();
}

@JsonSerializable()
class VScrolledComposite extends VComposite {
  VScrolledComposite() : this.empty();
  VScrolledComposite.empty() {
    swt = "ScrolledComposite";
  }

  bool? alwaysShowScrollBars;
  VControl? content;
  bool? expandHorizontal;
  bool? expandVertical;
  int? minHeight;
  int? minSize;
  int? minWidth;
  VPoint? origin;
  bool? showFocusedControl;

  factory VScrolledComposite.fromJson(Map<String, dynamic> json) =>
      _$VScrolledCompositeFromJson(json);
  Map<String, dynamic> toJson() => _$VScrolledCompositeToJson(this);
}
