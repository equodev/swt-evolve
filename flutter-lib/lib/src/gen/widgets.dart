import 'package:flutter/widgets.dart';
import '../gen/animatedprogress.dart';
import '../gen/browser.dart';
import '../gen/button.dart';
import '../gen/canvas.dart';
import '../gen/caret.dart';
import '../gen/cbanner.dart';
import '../gen/ccombo.dart';
import '../gen/clabel.dart';
import '../gen/combo.dart';
import '../gen/composite.dart';
import '../gen/controleditor.dart';
import '../gen/coolbar.dart';
import '../gen/coolitem.dart';
import '../gen/ctabfolder.dart';
import '../gen/ctabitem.dart';
import '../gen/datetime.dart';
import '../gen/decorations.dart';
import '../gen/dragsource.dart';
import '../gen/droptarget.dart';
import '../gen/expandbar.dart';
import '../gen/expanditem.dart';
import '../gen/gc.dart';
import '../gen/glcanvas.dart';
import '../gen/group.dart';
import '../gen/ime.dart';
import '../gen/label.dart';
import '../gen/link.dart';
import '../gen/list.dart';
import '../gen/menu.dart';
import '../gen/menuitem.dart';
import '../gen/progressbar.dart';
import '../gen/sash.dart';
import '../gen/sashform.dart';
import '../gen/scale.dart';
import '../gen/scrollbar.dart';
import '../gen/scrolledcomposite.dart';
import '../gen/shell.dart';
import '../gen/slider.dart';
import '../gen/spinner.dart';
import '../gen/styledtext.dart';
import '../gen/tabfolder.dart';
import '../gen/tabitem.dart';
import '../gen/table.dart';
import '../gen/tablecolumn.dart';
import '../gen/tablecursor.dart';
import '../gen/tableeditor.dart';
import '../gen/tableitem.dart';
import '../gen/taskbar.dart';
import '../gen/taskitem.dart';
import '../gen/text.dart';
import '../gen/toolbar.dart';
import '../gen/toolitem.dart';
import '../gen/tooltip.dart';
import '../gen/tracker.dart';
import '../gen/tray.dart';
import '../gen/trayitem.dart';
import '../gen/tree.dart';
import '../gen/treecolumn.dart';
import '../gen/treecursor.dart';
import '../gen/treeeditor.dart';
import '../gen/treeitem.dart';
import '../gen/viewform.dart';
import '../gen/widget.dart';

VWidget mapWidgetValue(Map<String, dynamic> child) {
  var type = child['swt'];
  return switch (type) {
    "Shell" => VShell.fromJson(child),
    "AnimatedProgress" => VAnimatedProgress.fromJson(child),
    "CLabel" => VCLabel.fromJson(child),
    "Decorations" => VDecorations.fromJson(child),
    "GLCanvas" => VGLCanvas.fromJson(child),
    "StyledText" => VStyledText.fromJson(child),
    "TableCursor" => VTableCursor.fromJson(child),
    "TreeCursor" => VTreeCursor.fromJson(child),
    "Browser" => VBrowser.fromJson(child),
    "CBanner" => VCBanner.fromJson(child),
    "CCombo" => VCCombo.fromJson(child),
    "CTabFolder" => VCTabFolder.fromJson(child),
    "Canvas" => VCanvas.fromJson(child),
    "Combo" => VCombo.fromJson(child),
    "CoolBar" => VCoolBar.fromJson(child),
    "DateTime" => VDateTime.fromJson(child),
    "ExpandBar" => VExpandBar.fromJson(child),
    "Group" => VGroup.fromJson(child),
    "SashForm" => VSashForm.fromJson(child),
    "ScrolledComposite" => VScrolledComposite.fromJson(child),
    "Spinner" => VSpinner.fromJson(child),
    "TabFolder" => VTabFolder.fromJson(child),
    "Table" => VTable.fromJson(child),
    "ToolBar" => VToolBar.fromJson(child),
    "Tree" => VTree.fromJson(child),
    "ViewForm" => VViewForm.fromJson(child),
    "Composite" => VComposite.fromJson(child),
    "List" => VList.fromJson(child),
    "Text" => VText.fromJson(child),
    "Button" => VButton.fromJson(child),
    "Label" => VLabel.fromJson(child),
    "Link" => VLink.fromJson(child),
    "ProgressBar" => VProgressBar.fromJson(child),
    "Sash" => VSash.fromJson(child),
    "Scale" => VScale.fromJson(child),
    "Slider" => VSlider.fromJson(child),
    "CTabItem" => VCTabItem.fromJson(child),
    "CoolItem" => VCoolItem.fromJson(child),
    "ExpandItem" => VExpandItem.fromJson(child),
    "MenuItem" => VMenuItem.fromJson(child),
    "TabItem" => VTabItem.fromJson(child),
    "TableColumn" => VTableColumn.fromJson(child),
    "TableItem" => VTableItem.fromJson(child),
    "TaskItem" => VTaskItem.fromJson(child),
    "ToolItem" => VToolItem.fromJson(child),
    "TrayItem" => VTrayItem.fromJson(child),
    "TreeColumn" => VTreeColumn.fromJson(child),
    "TreeItem" => VTreeItem.fromJson(child),
    "Caret" => VCaret.fromJson(child),
    "DragSource" => VDragSource.fromJson(child),
    "DropTarget" => VDropTarget.fromJson(child),
    "GC" => VGC.fromJson(child),
    "IME" => VIME.fromJson(child),
    "Menu" => VMenu.fromJson(child),
    "ScrollBar" => VScrollBar.fromJson(child),
    "TableEditor" => VTableEditor.fromJson(child),
    "TaskBar" => VTaskBar.fromJson(child),
    "ToolTip" => VToolTip.fromJson(child),
    "Tracker" => VTracker.fromJson(child),
    "Tray" => VTray.fromJson(child),
    "TreeEditor" => VTreeEditor.fromJson(child),
    "ControlEditor" => VControlEditor.fromJson(child),
    "MainComposite" => VComposite.fromJson(child),
    "SideBar" => VComposite.fromJson(child),
    "MainToolbar" => VComposite.fromJson(child),
    "StatusBar" => VComposite.fromJson(child),
    _ => throw "Unknown Widget Value $type",
  };
}

Widget mapWidgetFromValue(VWidget child) {
  var type = child.swt;
  var id = child.id;
  return switch (child) {
    VShell() => ShellSwt(key: ValueKey(id), value: child),
    VAnimatedProgress() => AnimatedProgressSwt(key: ValueKey(id), value: child),
    VCLabel() => CLabelSwt(key: ValueKey(id), value: child),
    VDecorations() => DecorationsSwt(key: ValueKey(id), value: child),
    VGLCanvas() => GLCanvasSwt(key: ValueKey(id), value: child),
    VStyledText() => StyledTextSwt(key: ValueKey(id), value: child),
    VTableCursor() => TableCursorSwt(key: ValueKey(id), value: child),
    VTreeCursor() => TreeCursorSwt(key: ValueKey(id), value: child),
    VBrowser() => BrowserSwt(key: ValueKey(id), value: child),
    VCBanner() => CBannerSwt(key: ValueKey(id), value: child),
    VCCombo() => CComboSwt(key: ValueKey(id), value: child),
    VCTabFolder() => CTabFolderSwt(key: ValueKey(id), value: child),
    VCanvas() => CanvasSwt(key: ValueKey(id), value: child),
    VCombo() => ComboSwt(key: ValueKey(id), value: child),
    VCoolBar() => CoolBarSwt(key: ValueKey(id), value: child),
    VDateTime() => DateTimeSwt(key: ValueKey(id), value: child),
    VExpandBar() => ExpandBarSwt(key: ValueKey(id), value: child),
    VGroup() => GroupSwt(key: ValueKey(id), value: child),
    VSashForm() => SashFormSwt(key: ValueKey(id), value: child),
    VScrolledComposite() => ScrolledCompositeSwt(
      key: ValueKey(id),
      value: child,
    ),
    VSpinner() => SpinnerSwt(key: ValueKey(id), value: child),
    VTabFolder() => TabFolderSwt(key: ValueKey(id), value: child),
    VTable() => TableSwt(key: ValueKey(id), value: child),
    VToolBar() => ToolBarSwt(key: ValueKey(id), value: child),
    VTree() => TreeSwt(key: ValueKey(id), value: child),
    VViewForm() => ViewFormSwt(key: ValueKey(id), value: child),
    VComposite() => CompositeSwt(key: ValueKey(id), value: child),
    VList() => ListSwt(key: ValueKey(id), value: child),
    VText() => TextSwt(key: ValueKey(id), value: child),
    VButton() => ButtonSwt(key: ValueKey(id), value: child),
    VLabel() => LabelSwt(key: ValueKey(id), value: child),
    VLink() => LinkSwt(key: ValueKey(id), value: child),
    VProgressBar() => ProgressBarSwt(key: ValueKey(id), value: child),
    VSash() => SashSwt(key: ValueKey(id), value: child),
    VScale() => ScaleSwt(key: ValueKey(id), value: child),
    VSlider() => SliderSwt(key: ValueKey(id), value: child),
    VCTabItem() => CTabItemSwt(key: ValueKey(id), value: child),
    VCoolItem() => CoolItemSwt(key: ValueKey(id), value: child),
    VExpandItem() => ExpandItemSwt(key: ValueKey(id), value: child),
    VMenuItem() => MenuItemSwt(key: ValueKey(id), value: child),
    VTabItem() => TabItemSwt(key: ValueKey(id), value: child),
    VTableColumn() => TableColumnSwt(key: ValueKey(id), value: child),
    VTableItem() => TableItemSwt(key: ValueKey(id), value: child),
    VTaskItem() => TaskItemSwt(key: ValueKey(id), value: child),
    VToolItem() => ToolItemSwt(key: ValueKey(id), value: child),
    VTrayItem() => TrayItemSwt(key: ValueKey(id), value: child),
    VTreeColumn() => TreeColumnSwt(key: ValueKey(id), value: child),
    VTreeItem() => TreeItemSwt(key: ValueKey(id), value: child),
    VCaret() => CaretSwt(key: ValueKey(id), value: child),
    VDragSource() => DragSourceSwt(key: ValueKey(id), value: child),
    VDropTarget() => DropTargetSwt(key: ValueKey(id), value: child),
    VGC() => GCSwt(key: ValueKey(id), value: child),
    VIME() => IMESwt(key: ValueKey(id), value: child),
    VMenu() => MenuSwt(key: ValueKey(id), value: child),
    VScrollBar() => ScrollBarSwt(key: ValueKey(id), value: child),
    VTableEditor() => TableEditorSwt(key: ValueKey(id), value: child),
    VTaskBar() => TaskBarSwt(key: ValueKey(id), value: child),
    VToolTip() => ToolTipSwt(key: ValueKey(id), value: child),
    VTracker() => TrackerSwt(key: ValueKey(id), value: child),
    VTray() => TraySwt(key: ValueKey(id), value: child),
    VTreeEditor() => TreeEditorSwt(key: ValueKey(id), value: child),
    VControlEditor() => ControlEditorSwt(key: ValueKey(id), value: child),
    _ => throw "No widget for Value $type",
  };
}

Widget mapWidget(Map<String, dynamic> child) {
  var value = mapWidgetValue(child);
  return mapWidgetFromValue(value);
}
