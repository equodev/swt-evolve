// A window shaped like an IDE workbench, as Java describes one: a tool bar, a tree and an editor
// side by side across a sash, a table under them and a status line. Changes reach it the way Java
// sends them, as change records naming the properties that moved, one frame per Java flush.

import 'package:swtflutter/src/gen/composite.dart';
import 'package:swtflutter/src/gen/control.dart';
import 'package:swtflutter/src/gen/ctabfolder.dart';
import 'package:swtflutter/src/gen/ctabitem.dart';
import 'package:swtflutter/src/gen/label.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/sash.dart';
import 'package:swtflutter/src/gen/sashform.dart';
import 'package:swtflutter/src/gen/styledtext.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/gen/table.dart';
import 'package:swtflutter/src/gen/tablecolumn.dart';
import 'package:swtflutter/src/gen/tableitem.dart';
import 'package:swtflutter/src/gen/toolbar.dart';
import 'package:swtflutter/src/gen/toolitem.dart';
import 'package:swtflutter/src/gen/tree.dart';
import 'package:swtflutter/src/gen/treeitem.dart';
import 'package:swtflutter/src/gen/widget.dart';
import 'package:swtflutter/src/gen/widgets.dart' show WireKey;

import '../../delivery/support/deliver.dart';

const double windowWidth = 1200;
const double windowHeight = 800;

const int rootId = 1;
const int toolBarId = 10;
const int sashFormId = 30;
const int leftPaneId = 31;
const int treeId = 32;
const int sashId = 33;
const int folderId = 35;
const int editorId = 36;
const int tableId = 41;
const int statusId = 50;

const int _toolItems = 12;
const int _treeRoots = 40;
const int _treeChildren = 8;
const int _tableRows = 40;
const int _editorLines = 400;
const int sashWidth = 4;
const int _sashFormHeight = 560;
const int _sashFormY = 32;

VRectangle rect(int x, int y, int w, int h) => VRectangle()
  ..x = x
  ..y = y
  ..width = w
  ..height = h;

T _control<T extends VControl>(T v, int id, VRectangle bounds) => v
  ..id = id
  ..seq = 1
  ..enabled = true
  ..visible = true
  ..bounds = bounds;

int toolItemId(int i) => toolBarId + 1 + i;
int treeItemId(int root, [int? child]) => 1000 + root * 100 + (child == null ? 0 : child + 1);
int tableItemId(int row) => 5000 + row;
int statusLabelId(int i) => statusId + 1 + i;

String editorText(int lines) => List.generate(
  lines,
  (i) => i % 7 == 0
      ? '  /** Line $i explains what follows. */'
      : '    final value$i = compute(input$i, ${i * 3}) + offset; // note $i',
).join('\n');

VToolBar _toolBar() => _control(
  VToolBar()
    ..style = SWT.FLAT
    ..items = [
      for (var i = 0; i < _toolItems; i++)
        VToolItem()
          ..id = toolItemId(i)
          ..seq = 1
          ..style = SWT.PUSH
          ..enabled = true
          ..text = 'Tool $i',
    ],
  toolBarId,
  rect(0, 0, windowWidth.toInt(), _sashFormY),
);

VTreeItem _treeItem(int root, [int? child]) => VTreeItem()
  ..id = treeItemId(root, child)
  ..seq = 1
  ..text = child == null ? 'package.number$root' : 'Type${root}_$child.java'
  ..expanded = child == null && root < 3
  ..items = child == null && root < 3 ? [for (var c = 0; c < _treeChildren; c++) _treeItem(root, c)] : [];

VTree _tree(int width) => _control(
  VTree()
    ..style = SWT.SINGLE | SWT.V_SCROLL | SWT.H_SCROLL
    ..items = [for (var r = 0; r < _treeRoots; r++) _treeItem(r)],
  treeId,
  rect(0, 0, width, _sashFormHeight),
);

VStyledText _editor(int width) => _control(
  VStyledText()
    ..style = SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL
    ..editable = true
    ..text = editorText(_editorLines)
    ..caretOffset = 0,
  editorId,
  _editorBounds(width),
);

VRectangle _editorBounds(int folderWidth) => rect(1, 33, folderWidth - 2, _sashFormHeight - 34);

VCTabFolder _folder(int x, int width) => _control(
  VCTabFolder()
    ..style = SWT.BORDER
    ..selection = 0
    ..items = [
      for (var i = 0; i < 3; i++)
        VCTabItem()
          ..id = folderId * 10 + i
          ..seq = 1
          ..text = 'Editor$i.java'
          ..control = i == 0 ? _editor(width) : null,
    ],
  folderId,
  rect(x, 0, width, _sashFormHeight),
);

VSashForm _sashForm(int left) => _control(
  VSashForm()
    ..style = SWT.HORIZONTAL
    ..sashWidth = sashWidth
    ..weights = [left, windowWidth.toInt() - left - sashWidth]
    ..children = [
      _control(
        VComposite()
          ..style = SWT.NONE
          ..children = [_tree(left)],
        leftPaneId,
        rect(0, 0, left, _sashFormHeight),
      ),
      _control(VSash()..style = SWT.VERTICAL, sashId, rect(left, 0, sashWidth, _sashFormHeight)),
      _folder(left + sashWidth, windowWidth.toInt() - left - sashWidth),
    ],
  sashFormId,
  rect(0, _sashFormY, windowWidth.toInt(), _sashFormHeight),
);

VTable _table() => _control(
  VTable()
    ..style = SWT.MULTI | SWT.FULL_SELECTION | SWT.V_SCROLL
    ..headerVisible = true
    ..linesVisible = true
    ..columns = [
      VTableColumn()
        ..id = tableId * 10 + 1
        ..text = 'Description'
        ..width = 600,
      VTableColumn()
        ..id = tableId * 10 + 2
        ..text = 'Resource'
        ..width = 300,
      VTableColumn()
        ..id = tableId * 10 + 3
        ..text = 'Line'
        ..width = 100,
    ]
    ..items = [for (var r = 0; r < _tableRows; r++) tableRow(r)]
    ..itemCount = _tableRows,
  tableId,
  rect(0, _sashFormY + _sashFormHeight + 4, windowWidth.toInt(), 180),
);

VTableItem tableRow(int r, {String? text}) => VTableItem()
  ..id = tableItemId(r)
  ..seq = 1
  ..texts = [text ?? 'Problem number $r in the code', 'Type$r.java', '${r * 10}'];

VComposite _status() => _control(
  VComposite()
    ..style = SWT.NONE
    ..children = [
      for (var i = 0; i < 4; i++)
        _control(
          VLabel()
            ..style = SWT.NONE
            ..text = 'Status $i',
          statusLabelId(i),
          rect(i * 300, 0, 290, 20),
        ),
    ],
  statusId,
  rect(0, (windowHeight - 20).toInt(), windowWidth.toInt(), 20),
);

/// The whole window, its sash [left] pixels from the left edge.
VComposite workbench({int left = 300}) => _control(
  VComposite()
    ..style = SWT.NONE
    ..children = [_toolBar(), _sashForm(left), _table(), _status()],
  rootId,
  rect(0, 0, windowWidth.toInt(), windowHeight.toInt()),
);

/// Sends the changes Java makes to the window, each widget's stamp following the last one sent.
class JavaSide {
  final Map<int, int> _stamps = {};

  /// The changes of one Java flush: each value names the properties in its list.
  void flush(List<(VWidget, List<int>)> changes) {
    for (final (value, keys) in changes) {
      final base = _stamps[value.id] ?? 1;
      value.seq = base + 1;
      _stamps[value.id] = base + 1;
      applyChange(value, keys: keys, base: base);
    }
  }

  /// Moving the sash to [left]: SashForm lays out its panes and Java's layouts follow down to the
  /// tree and the editor.
  void sashAt(int left) {
    final right = windowWidth.toInt() - left - sashWidth;
    flush([
      (
        VSash()
          ..id = sashId
          ..bounds = rect(left, 0, sashWidth, _sashFormHeight),
        [WireKey.k_Control_bounds],
      ),
      (
        VComposite()
          ..id = leftPaneId
          ..bounds = rect(0, 0, left, _sashFormHeight),
        [WireKey.k_Control_bounds],
      ),
      (
        VTree()
          ..id = treeId
          ..bounds = rect(0, 0, left, _sashFormHeight),
        [WireKey.k_Control_bounds],
      ),
      (
        VCTabFolder()
          ..id = folderId
          ..bounds = rect(left + sashWidth, 0, right, _sashFormHeight),
        [WireKey.k_Control_bounds],
      ),
      (
        VStyledText()
          ..id = editorId
          ..bounds = _editorBounds(right),
        [WireKey.k_Control_bounds],
      ),
    ]);
  }

  /// The window resized to [width] x [height]: every control along the edges grows.
  void windowSize(int width, int height, {int left = 300}) {
    final formHeight = height - _sashFormY - 204;
    final right = width - left - sashWidth;
    flush([
      (
        VComposite()
          ..id = rootId
          ..bounds = rect(0, 0, width, height),
        [WireKey.k_Control_bounds],
      ),
      (
        VToolBar()
          ..id = toolBarId
          ..bounds = rect(0, 0, width, _sashFormY),
        [WireKey.k_Control_bounds],
      ),
      (
        VSashForm()
          ..id = sashFormId
          ..bounds = rect(0, _sashFormY, width, formHeight),
        [WireKey.k_Control_bounds],
      ),
      (
        VSash()
          ..id = sashId
          ..bounds = rect(left, 0, sashWidth, formHeight),
        [WireKey.k_Control_bounds],
      ),
      (
        VComposite()
          ..id = leftPaneId
          ..bounds = rect(0, 0, left, formHeight),
        [WireKey.k_Control_bounds],
      ),
      (
        VTree()
          ..id = treeId
          ..bounds = rect(0, 0, left, formHeight),
        [WireKey.k_Control_bounds],
      ),
      (
        VCTabFolder()
          ..id = folderId
          ..bounds = rect(left + sashWidth, 0, right, formHeight),
        [WireKey.k_Control_bounds],
      ),
      (
        VStyledText()
          ..id = editorId
          ..bounds = rect(1, 33, right - 2, formHeight - 34),
        [WireKey.k_Control_bounds],
      ),
      (
        VTable()
          ..id = tableId
          ..bounds = rect(0, _sashFormY + formHeight + 4, width, 180),
        [WireKey.k_Control_bounds],
      ),
      (
        VComposite()
          ..id = statusId
          ..bounds = rect(0, height - 20, width, 20),
        [WireKey.k_Control_bounds],
      ),
    ]);
  }
}
