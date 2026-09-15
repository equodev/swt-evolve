import 'package:flutter/widgets.dart';
import 'package:json_annotation/json_annotation.dart';
import '../comm/comm.dart';
import '../gen/color.dart';
import '../gen/control.dart';
import '../gen/cursor.dart';
import '../gen/decorations.dart';
import '../gen/dialog.dart';
import '../gen/dialogs.dart';
import '../gen/font.dart';
import '../gen/image.dart';
import '../gen/menu.dart';
import '../gen/point.dart';
import '../gen/rectangle.dart';
import '../gen/region.dart';
import '../gen/scrollbar.dart';
import '../gen/widget.dart';
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

  void sendShellSetBounds(V val, VEvent? payload) {
    sendEvent(val, "Shell/SetBounds", payload);
  }
}

@JsonSerializable()
class VShell extends VDecorations {
  VShell() : this.empty();
  VShell.empty() {
    swt = "Shell";
  }

  @JsonKey(fromJson: parseDialogs)
  List<VDialog>? dialogs;
  int? alpha;
  bool? fullScreen;
  VPoint? maximumSize;
  VPoint? minimumSize;
  bool? modified;

  @override
  void copyFrom(VWidget other) {
    super.copyFrom(other);
    if (other is VShell) {
      alpha = other.alpha;
      fullScreen = other.fullScreen;
      maximumSize = other.maximumSize;
      minimumSize = other.minimumSize;
      modified = other.modified;
      dialogs = other.dialogs;
    }
  }

  @override
  void readProperty(String key, Map<String, dynamic> json) {
    switch (key) {
      case 'alpha':
        alpha = (json['alpha'] as num?)?.toInt();
      case 'fullScreen':
        fullScreen = json['fullScreen'] as bool?;
      case 'maximumSize':
        maximumSize = json['maximumSize'] == null
            ? null
            : VPoint.fromJson(json['maximumSize'] as Map<String, dynamic>);
      case 'minimumSize':
        minimumSize = json['minimumSize'] == null
            ? null
            : VPoint.fromJson(json['minimumSize'] as Map<String, dynamic>);
      case 'modified':
        modified = json['modified'] as bool?;
      case 'dialogs':
        dialogs = parseDialogs(json['dialogs']);
      default:
        super.readProperty(key, json);
    }
  }

  factory VShell.fromJson(Map<String, dynamic> json) =>
      _$VShellFromJson(json)..isReference = json.containsKey('_r');
  Map<String, dynamic> toJson() => _$VShellToJson(this);
}
