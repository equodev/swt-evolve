import 'package:flutter/widgets.dart';
import '../gen/button.dart';
import '../gen/canvas.dart';
import '../gen/caret.dart';
import '../gen/clabel.dart';
import '../gen/combo.dart';
import '../gen/composite.dart';
import '../gen/ctabfolder.dart';
import '../gen/ctabitem.dart';
import '../gen/label.dart';
import '../gen/list.dart';
import '../gen/scrollbar.dart';
import '../gen/toolbar.dart';
import '../gen/toolitem.dart';
import '../gen/widget.dart';

VWidget mapWidgetValue(Map<String, dynamic> child) {
  var type = child['swt'];
  // print("value: $type");
  return switch (type) {
    "CLabel" => VCLabel.fromJson(child),
    "CTabFolder" => VCTabFolder.fromJson(child),
    "Combo" => VCombo.fromJson(child),
    "Canvas" => VCanvas.fromJson(child),
    "ToolBar" => VToolBar.fromJson(child),
    "Composite" => VComposite.fromJson(child),
    "List" => VList.fromJson(child),
    "Label" => VLabel.fromJson(child),
    "Button" => VButton.fromJson(child),
    "CTabItem" => VCTabItem.fromJson(child),
    "ToolItem" => VToolItem.fromJson(child),
    "ScrollBar" => VScrollBar.fromJson(child),
    "Caret" => VCaret.fromJson(child),
    _ => throw "Unknown Widget Value $type"
  };
}

Widget mapWidgetFromValue(VWidget child) {
  var type = child.swt;
  var id = child.id;
  return switch (child) {
    VCLabel() => CLabelSwt(key: ValueKey(id), value: child),
    VCTabFolder() => CTabFolderSwt(key: ValueKey(id), value: child),
    VCombo() => ComboSwt(key: ValueKey(id), value: child),
    VCanvas() => CanvasSwt(key: ValueKey(id), value: child),
    VToolBar() => ToolBarSwt(key: ValueKey(id), value: child),
    VComposite() => CompositeSwt(key: ValueKey(id), value: child),
    VList() => ListSwt(key: ValueKey(id), value: child),
    VLabel() => LabelSwt(key: ValueKey(id), value: child),
    VButton() => ButtonSwt(key: ValueKey(id), value: child),
    VCTabItem() => CTabItemSwt(key: ValueKey(id), value: child),
    VToolItem() => ToolItemSwt(key: ValueKey(id), value: child),
    VScrollBar() => ScrollBarSwt(key: ValueKey(id), value: child),
    VCaret() => CaretSwt(key: ValueKey(id), value: child),
    _ => throw "No widget for Value $type"
  };
}

Widget mapWidget(Map<String, dynamic> child) {
  var value = mapWidgetValue(child);
  return mapWidgetFromValue(value);
}
