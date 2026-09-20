import 'package:json_annotation/json_annotation.dart';
import '../impl/config_flags.dart';
import 'menu.dart';
import 'shell.dart';
import 'tooltip.dart';

part 'display.g.dart';

@JsonSerializable()
class VDisplay {
  String? swt;
  int? id;
  List<VShell>? shells;
  List<VMenu>? popups;
  VMenu? systemMenu;
  List<VToolTip>? tooltips;
  ConfigFlags? config;

  /// The shell SWT considers active, or null/0 when none is. A shell takes keyboard focus only
  /// when it is named here — see [ShellImpl].
  int? activeShellId;

  /// The shell that drives (and is slaved to) the viewport, or null/0 when Java named none. Only
  /// this shell renders full-bleed; every other one is a window with its own chrome.
  int? mainShellId;

  /// The shells drawn in a window of their own — a second native window, a second browser window —
  /// each by its own Flutter client rooted at that shell. They are left out of this client's stack;
  /// drawing them here as well would put them on screen twice. See [WindowPolicy] on the Java side.
  List<int>? windowedShellIds;

  VDisplay();

  factory VDisplay.fromJson(Map<String, dynamic> json) =>
      _$VDisplayFromJson(json);

  Map<String, dynamic> toJson() => _$VDisplayToJson(this);
}
