import 'package:flutter/widgets.dart';

import 'swt/widget.dart';
import 'swt/control.dart';
import 'swt/scrollbar.dart';
import 'swt/scrollable.dart';
import 'swt/composite.dart';
import 'swt/caret.dart';
import 'swt/ime.dart';
import 'swt/canvas.dart';
import 'swt/decorations.dart';
import 'swt/shell.dart';
import 'swt/label.dart';
import 'swt/list.dart';
import 'swt/text.dart';
import 'swt/button.dart';
import 'swt/slider.dart';
import 'swt/item.dart';
import 'swt/progressbar.dart';
import 'swt/link.dart';
import 'swt/toolbar.dart';
import 'swt/toolitem.dart';
import 'swt/table.dart';
import 'swt/tableitem.dart';
import 'swt/tablecolumn.dart';
import 'swt/tree.dart';
import 'swt/treeitem.dart';
import 'swt/treecolumn.dart';
import 'swt/tabitem.dart';
import 'swt/menu.dart';
import 'swt/menuitem.dart';
import 'swt/tabfolder.dart';
import 'swt/group.dart';
import 'swt/spinner.dart';
import 'swt/scale.dart';
import 'swt/scrolledcomposite.dart';
import 'swt/expanditem.dart';
import 'swt/expandbar.dart';
import 'swt/coolitem.dart';
import 'swt/coolbar.dart';
import 'swt/browser.dart';
import 'swt/sashform.dart';
import 'swt/sash.dart';
import 'swt/layout.dart';

import 'swt/rowlayout.dart';
import 'impl/rowlayout_impl.dart';
import 'swt/gridlayout.dart';
import 'impl/gridlayout_impl.dart';
import 'swt/filllayout.dart';
import 'impl/filllayout_impl.dart';
import 'swt/sashformlayout.dart';
import 'impl/sashformlayout_impl.dart';
import 'nolayout.dart';

WidgetValue mapWidgetValue(Map<String, dynamic> child) {
  var type = child['swt'];
  // print("value: $type");
  return switch (type) {
    "ScrollBar" => ScrollBarValue.fromJson(child),
    "Composite" => CompositeValue.fromJson(child),
    "Caret" => CaretValue.fromJson(child),
    "IME" => IMEValue.fromJson(child),
    "Canvas" => CanvasValue.fromJson(child),
    "Decorations" => DecorationsValue.fromJson(child),
    "Shell" => ShellValue.fromJson(child),
    "Label" => LabelValue.fromJson(child),
    "List" => ListValue.fromJson(child),
    "Text" => TextValue.fromJson(child),
    "Button" => ButtonValue.fromJson(child),
   // "Combo" => ComboValue.fromJson(child),
    "Slider" => SliderValue.fromJson(child),
    "ProgressBar" => ProgressBarValue.fromJson(child),
    "Link" => LinkValue.fromJson(child),
    "ToolBar" => ToolBarValue.fromJson(child),
    "ToolItem" => ToolItemValue.fromJson(child),
    "Table" => TableValue.fromJson(child),
    "TableItem" => TableItemValue.fromJson(child),
    "TableColumn" => TableColumnValue.fromJson(child),
    "Tree" => TreeValue.fromJson(child),
    "TreeItem" => TreeItemValue.fromJson(child),
    "TreeColumn" => TreeColumnValue.fromJson(child),
    "TabItem" => TabItemValue.fromJson(child),
    "Menu" => MenuValue.fromJson(child),
    "MenuItem" => MenuItemValue.fromJson(child),
    "TabFolder" => TabFolderValue.fromJson(child),
    "Group" => GroupValue.fromJson(child),
    "Spinner" => SpinnerValue.fromJson(child),
    "Scale" => ScaleValue.fromJson(child),
    "ScrolledComposite" => ScrolledCompositeValue.fromJson(child),
    "ExpandItem" => ExpandItemValue.fromJson(child),
    "ExpandBar" => ExpandBarValue.fromJson(child),
    "CoolItem" => CoolItemValue.fromJson(child),
    "CoolBar" => CoolBarValue.fromJson(child),
    "Browser" => BrowserValue.fromJson(child),
    "SashForm" => SashFormValue.fromJson(child),
    "Sash" => SashValue.fromJson(child),
    _ => throw "Unknown Widget Value $type"
  };
}

Widget mapWidgetFromValue(WidgetValue child) {
  var type = child.swt;
  var id = child.id;
  return switch (type) {
    "ScrollBar" =>
      ScrollBarSwt(key: ValueKey(id), value: (child as ScrollBarValue)),
    "Composite" =>
      CompositeSwt(key: ValueKey(id), value: (child as CompositeValue)),
    "Caret" => CaretSwt(key: ValueKey(id), value: (child as CaretValue)),
    "IME" => IMESwt(key: ValueKey(id), value: (child as IMEValue)),
    "Canvas" => CanvasSwt(key: ValueKey(id), value: (child as CanvasValue)),
    "Decorations" =>
      DecorationsSwt(key: ValueKey(id), value: (child as DecorationsValue)),
    "Shell" => ShellSwt(key: ValueKey(id), value: (child as ShellValue)),
    "Label" => LabelSwt(key: ValueKey(id), value: (child as LabelValue)),
    "List" => ListSwt(key: ValueKey(id), value: (child as ListValue)),
    "Text" => TextSwt(key: ValueKey(id), value: (child as TextValue)),
    "Button" => ButtonSwt(key: ValueKey(id), value: (child as ButtonValue)),
   // "Combo" => ComboSwt(key: ValueKey(id), value: (child as ComboValue)),
    "Slider" => SliderSwt(key: ValueKey(id), value: (child as SliderValue)),
    "ProgressBar" =>
      ProgressBarSwt(key: ValueKey(id), value: (child as ProgressBarValue)),
    "Link" => LinkSwt(key: ValueKey(id), value: (child as LinkValue)),
    "ToolBar" => ToolBarSwt(key: ValueKey(id), value: (child as ToolBarValue)),
    "ToolItem" =>
      ToolItemSwt(key: ValueKey(id), value: (child as ToolItemValue)),
    "Table" => TableSwt(key: ValueKey(id), value: (child as TableValue)),
    "TableItem" =>
      TableItemSwt(key: ValueKey(id), value: (child as TableItemValue)),
    "TableColumn" =>
      TableColumnSwt(key: ValueKey(id), value: (child as TableColumnValue)),
    "Tree" => TreeSwt(key: ValueKey(id), value: (child as TreeValue)),
    "TreeItem" =>
      TreeItemSwt(key: ValueKey(id), value: (child as TreeItemValue)),
    "TreeColumn" =>
      TreeColumnSwt(key: ValueKey(id), value: (child as TreeColumnValue)),
    "TabItem" => TabItemSwt(key: ValueKey(id), value: (child as TabItemValue)),
    "Menu" => MenuSwt(key: ValueKey(id), value: (child as MenuValue)),
    "MenuItem" =>
      MenuItemSwt(key: ValueKey(id), value: (child as MenuItemValue)),
    "TabFolder" =>
      TabFolderSwt(key: ValueKey(id), value: (child as TabFolderValue)),
    "Group" => GroupSwt(key: ValueKey(id), value: (child as GroupValue)),
    "Spinner" => SpinnerSwt(key: ValueKey(id), value: (child as SpinnerValue)),
    "Scale" => ScaleSwt(key: ValueKey(id), value: (child as ScaleValue)),
    "ScrolledComposite" => ScrolledCompositeSwt(
        key: ValueKey(id), value: (child as ScrolledCompositeValue)),
    "ExpandItem" =>
      ExpandItemSwt(key: ValueKey(id), value: (child as ExpandItemValue)),
    "ExpandBar" =>
      ExpandBarSwt(key: ValueKey(id), value: (child as ExpandBarValue)),
    "CoolItem" =>
      CoolItemSwt(key: ValueKey(id), value: (child as CoolItemValue)),
    "CoolBar" => CoolBarSwt(key: ValueKey(id), value: (child as CoolBarValue)),
    "Browser" => BrowserSwt(key: ValueKey(id), value: (child as BrowserValue)),
    "SashForm" =>
      SashFormSwt(key: ValueKey(id), value: (child as SashFormValue)),
    "Sash" => SashSwt(key: ValueKey(id), value: (child as SashValue)),
    _ => throw "No widget for Value $type"
  };
}

Widget mapWidget(Map<String, dynamic> child) {
  var value = mapWidgetValue(child);
  return mapWidgetFromValue(value);
}

LayoutValue mapLayoutValue(Map<String, dynamic>? child) {
  if (child == null) {
    // print("no layout");
    return LayoutValue.empty();
  }
  var type = child['swt'];
  // print("layout: $type");
  return switch (type) {
    "RowLayout" => RowLayoutValue.fromJson(child),
    "GridLayout" => GridLayoutValue.fromJson(child),
    "FillLayout" => FillLayoutValue.fromJson(child),
    "SashFormLayout" => SashFormLayoutValue.fromJson(child),
    _ => throw "Unknown Layout Value $type"
  };
}

Widget mapLayout(
    CompositeValue composite, LayoutValue? layout, List<WidgetValue> children) {
  if (layout == null || layout.swt == "Layout") {
    // print("no layout");
    return Text("No Layout");
  }
  var type = layout.swt;
  // print("layout: $type");
  return switch (type) {
    "RowLayout" => RowLayoutImpl(
        composite: composite,
        value: (layout as RowLayoutValue),
        children: children),
    "GridLayout" => GridLayoutImpl(
        composite: composite,
        value: (layout as GridLayoutValue),
        children: children),
    "FillLayout" => FillLayoutImpl(
        composite: composite,
        value: (layout as FillLayoutValue),
        children: children),
    "SashFormLayout" => SashFormLayoutImpl(
        composite: composite,
        value: (layout as SashFormLayoutValue),
        children: children),
    _ => LabelSwt(value: LabelValue.empty())
  };
}
