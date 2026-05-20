import 'package:flutter/widgets.dart';
import 'package:json_annotation/json_annotation.dart';
import '../gen/color.dart';
import '../gen/composite.dart';
import '../gen/control.dart';
import '../gen/cursor.dart';
import '../gen/font.dart';
import '../gen/image.dart';
import '../gen/menu.dart';
import '../gen/rectangle.dart';
import '../gen/region.dart';
import '../gen/scrollbar.dart';
import '../impl/viewform_evolve.dart';
import 'widgets.dart';

part 'viewform.g.dart';

class ViewFormSwt<V extends VViewForm> extends CompositeSwt<V> {
  const ViewFormSwt({super.key, required super.value});

  @override
  State createState() => ViewFormImpl<ViewFormSwt<VViewForm>, VViewForm>();
}

@JsonSerializable()
class VViewForm extends VComposite {
  VViewForm() : this.empty();
  VViewForm.empty() {
    swt = "ViewForm";
  }

  bool? borderVisible;
  VControl? content;
  VControl? topCenter;
  bool? topCenterSeparate;
  VControl? topLeft;
  VControl? topRight;

  factory VViewForm.fromJson(Map<String, dynamic> json) =>
      _$VViewFormFromJson(json);
  Map<String, dynamic> toJson() => _$VViewFormToJson(this);
}
