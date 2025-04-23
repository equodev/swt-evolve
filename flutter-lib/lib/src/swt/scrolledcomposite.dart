import 'package:json_annotation/json_annotation.dart';
import 'package:flutter/widgets.dart';
import '../swt/composite.dart';
import '../impl/scrolledcomposite_impl.dart';
import '../widgets.dart';
import 'layout.dart';
import 'menu.dart';
import 'widget.dart';
import 'rectangle.dart';

part 'scrolledcomposite.g.dart';

class ScrolledCompositeSwt<V extends ScrolledCompositeValue>
    extends CompositeSwt<V> {
  const ScrolledCompositeSwt({super.key, required super.value});

  @override
  State createState() => ScrolledCompositeImpl<
      ScrolledCompositeSwt<ScrolledCompositeValue>, ScrolledCompositeValue>();
}

@JsonSerializable()
class ScrolledCompositeValue extends CompositeValue {
  ScrolledCompositeValue() : this.empty();
  ScrolledCompositeValue.empty() {
    swt = "ScrolledComposite";
  }

  bool? alwaysShowScrollBars;
  bool? expandHorizontal;
  bool? expandVertical;
  int? minWidth;
  int? minHeight;
  bool? showFocusedControl;

  factory ScrolledCompositeValue.fromJson(Map<String, dynamic> json) =>
      _$ScrolledCompositeValueFromJson(json);
  Map<String, dynamic> toJson() => _$ScrolledCompositeValueToJson(this);
}
