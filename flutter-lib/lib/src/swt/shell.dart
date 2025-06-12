import 'package:json_annotation/json_annotation.dart';
import 'package:flutter/widgets.dart';
import '../swt/decorations.dart';
import '../impl/shell_impl.dart';
import '../comm/comm.dart';
import '../widgets.dart';
import 'layout.dart';
import 'menu.dart';
import 'widget.dart';
import 'rectangle.dart';

part 'shell.g.dart';

class ShellSwt<V extends ShellValue> extends DecorationsSwt<V> {
  const ShellSwt({super.key, required super.value});

  @override
  State createState() => ShellImpl<ShellSwt<ShellValue>, ShellValue>();

  void sendShellClose(V val, Object? payload) {
    sendEvent(val, "Shell/Close", payload);
  }

  void sendShellIconify(V val, Object? payload) {
    sendEvent(val, "Shell/Iconify", payload);
  }

  void sendShellDeiconify(V val, Object? payload) {
    sendEvent(val, "Shell/Deiconify", payload);
  }

  void sendShellActivate(V val, Object? payload) {
    sendEvent(val, "Shell/Activate", payload);
  }

  void sendShellDeactivate(V val, Object? payload) {
    sendEvent(val, "Shell/Deactivate", payload);
  }
}

@JsonSerializable()
class ShellValue extends DecorationsValue {
  ShellValue() : this.empty();
  ShellValue.empty() {
    swt = "Shell";
  }

  int? alpha;
  bool? fullScreen;
  bool? maximized;
  bool? modified;
  bool? visible;
  int? imeInputMode;

  factory ShellValue.fromJson(Map<String, dynamic> json) =>
      _$ShellValueFromJson(json);
  Map<String, dynamic> toJson() => _$ShellValueToJson(this);
}
