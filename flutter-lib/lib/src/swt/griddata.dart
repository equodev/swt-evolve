import 'package:json_annotation/json_annotation.dart';
import 'swt.dart';

part 'griddata.g.dart';

@JsonSerializable()
class GridDataValue {
  GridDataValue() : this.empty();
  GridDataValue.empty();

  int verticalAlignment = 0;
  int horizontalAlignment = 0;
  int widthHint = 0;
  int heightHint = 0;
  int horizontalIndent = 0;
  int verticalIndent = 0;
  int horizontalSpan = 0;
  int verticalSpan = 0;
  bool grabExcessHorizontalSpace = false;
  bool grabExcessVerticalSpace = false;
  int minimumWidth = 0;
  int minimumHeight = 0;
  bool exclude = false;
  static const int BEGINNING = SWT.BEGINNING;
  static const int CENTER = 2;
  static const int END = 3;
  static const int FILL = SWT.FILL;
  static const int VERTICAL_ALIGN_BEGINNING = 1 << 1;
  static const int VERTICAL_ALIGN_CENTER = 1 << 2;
  static const int VERTICAL_ALIGN_END = 1 << 3;
  static const int VERTICAL_ALIGN_FILL = 1 << 4;
  static const int HORIZONTAL_ALIGN_BEGINNING = 1 << 5;
  static const int HORIZONTAL_ALIGN_CENTER = 1 << 6;
  static const int HORIZONTAL_ALIGN_END = 1 << 7;
  static const int HORIZONTAL_ALIGN_FILL = 1 << 8;
  static const int GRAB_HORIZONTAL = 1 << 9;
  static const int GRAB_VERTICAL = 1 << 10;
  static const int FILL_VERTICAL = VERTICAL_ALIGN_FILL | GRAB_VERTICAL;
  static const int FILL_HORIZONTAL = HORIZONTAL_ALIGN_FILL | GRAB_HORIZONTAL;
  static const int FILL_BOTH = FILL_VERTICAL | FILL_HORIZONTAL;

  factory GridDataValue.fromJson(Map<String, dynamic> json) =>
      _$GridDataValueFromJson(json);
  Map<String, dynamic> toJson() => _$GridDataValueToJson(this);
}
