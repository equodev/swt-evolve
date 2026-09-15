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
import '../gen/widget.dart';
import '../impl/sashform_evolve.dart';
import 'widgets.dart';

part 'sashform.g.dart';

class SashFormSwt<V extends VSashForm> extends CompositeSwt<V> {
  const SashFormSwt({super.key, required super.value});

  @override
  State createState() => SashFormImpl<SashFormSwt<VSashForm>, VSashForm>();
}

@JsonSerializable()
class VSashForm extends VComposite {
  VSashForm() : this.empty();
  VSashForm.empty() {
    swt = "SashForm";
  }

  VControl? maximizedControl;
  int? sashWidth;
  List<int>? weights;

  @override
  void copyFrom(VWidget other) {
    super.copyFrom(other);
    if (other is VSashForm) {
      maximizedControl = other.maximizedControl;
      sashWidth = other.sashWidth;
      weights = other.weights;
    }
  }

  @override
  void readProperty(String key, Map<String, dynamic> json) {
    switch (key) {
      case 'maximizedControl':
        maximizedControl = json['maximizedControl'] == null
            ? null
            : VControl.fromJson(
                json['maximizedControl'] as Map<String, dynamic>,
              );
      case 'sashWidth':
        sashWidth = (json['sashWidth'] as num?)?.toInt();
      case 'weights':
        weights = (json['weights'] as List<dynamic>?)
            ?.map((e) => (e as num).toInt())
            .toList();
      default:
        super.readProperty(key, json);
    }
  }

  @override
  void adoptChildren(VWidget Function(VWidget) adopt) {
    super.adoptChildren(adopt);
    maximizedControl = VWidget.adoptOne(maximizedControl, adopt);
  }

  factory VSashForm.fromJson(Map<String, dynamic> json) =>
      _$VSashFormFromJson(json)..isReference = json.containsKey('_r');
  Map<String, dynamic> toJson() => _$VSashFormToJson(this);
}
