import 'package:flutter/widgets.dart';
import 'package:json_annotation/json_annotation.dart';
import '../comm/comm.dart';
import '../gen/button.dart';
import '../gen/caret.dart';
import '../gen/color.dart';
import '../gen/control.dart';
import '../gen/cursor.dart';
import '../gen/decorations.dart';
import '../gen/font.dart';
import '../gen/image.dart';
import '../gen/ime.dart';
import '../gen/menu.dart';
import '../gen/point.dart';
import '../gen/rectangle.dart';
import '../gen/region.dart';
import '../gen/shell.dart';
import '../impl/shell_evolve.dart';
import 'event.dart';
import 'widgets.dart';

part 'shell.g.dart';

class ShellSwt<V extends VShell> extends DecorationsSwt<V> {
  const ShellSwt({super.key, required super.value});

  @override
  State createState() => ShellImpl<ShellSwt<VShell>, VShell>();

  void sendShellActivate(V val, VEvent? payload) {
    sendEvent(val, "Shell/Activate", payload);
  }

  void sendShellClose(V val, VEvent? payload) {
    sendEvent(val, "Shell/Close", payload);
  }

  void sendShellDeactivate(V val, VEvent? payload) {
    sendEvent(val, "Shell/Deactivate", payload);
  }

  void sendShellDeiconify(V val, VEvent? payload) {
    sendEvent(val, "Shell/Deiconify", payload);
  }

  void sendShellIconify(V val, VEvent? payload) {
    sendEvent(val, "Shell/Iconify", payload);
  }
}

@JsonSerializable()
class VShell extends VDecorations {
  VShell() : this.empty();
  VShell.empty() {
    swt = "Shell";
  }

  int? alpha;
  bool? fullScreen;
  int? imeInputMode;
  VPoint? maximumSize;
  VPoint? minimumSize;
  bool? modified;
  List<VShell>? shells;

  factory VShell.fromJson(Map<String, dynamic> json) => _$VShellFromJson(json);
  Map<String, dynamic> toJson() => _$VShellToJson(this);
}
