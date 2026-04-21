import 'package:json_annotation/json_annotation.dart';

part 'griddata.g.dart';

@JsonSerializable()
class VGridData {
  VGridData() : this.empty();
  VGridData.empty();

  int? BEGINNING;
  int? CENTER;
  int? END;
  int? FILL;
  int? FILL_BOTH;
  int? FILL_HORIZONTAL;
  int? FILL_VERTICAL;
  int? GRAB_HORIZONTAL;
  int? GRAB_VERTICAL;
  int? HORIZONTAL_ALIGN_BEGINNING;
  int? HORIZONTAL_ALIGN_CENTER;
  int? HORIZONTAL_ALIGN_END;
  int? HORIZONTAL_ALIGN_FILL;
  int? VERTICAL_ALIGN_BEGINNING;
  int? VERTICAL_ALIGN_CENTER;
  int? VERTICAL_ALIGN_END;
  int? VERTICAL_ALIGN_FILL;
  bool? exclude;
  bool? grabExcessHorizontalSpace;
  bool? grabExcessVerticalSpace;
  int? heightHint;
  int? horizontalAlignment;
  int? horizontalIndent;
  int? horizontalSpan;
  int? minimumHeight;
  int? minimumWidth;
  int? verticalAlignment;
  int? verticalIndent;
  int? verticalSpan;
  int? widthHint;

  factory VGridData.fromJson(Map<String, dynamic> json) =>
      _$VGridDataFromJson(json);
  Map<String, dynamic> toJson() => _$VGridDataToJson(this);
}
