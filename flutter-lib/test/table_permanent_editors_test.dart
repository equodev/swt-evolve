// Regression test: a Table with a permanent per-row editor control (e.g. a
// checkbox Button placed in a column via TableEditor.setEditor(control, item,
// col)) must render that control on EVERY row, not only the selected one.
//
// A preference page that puts a Button(SWT.CHECK) in column 1 of every row via
// a TableEditor is the motivating case: with native SWT every row shows its
// checkbox regardless of selection. Our web (whole-tree Flutter) impl used to
// gate editor overlays on `itemIndex == _selectedRowIndex`, so with no row
// selected the whole checkbox column rendered empty -- the reported bug.
//
// In SWT a TableEditor control is visible as soon as setEditor is called and
// stays visible until the editor is disposed; it does not follow selection.

import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/gen/table.dart';
import 'package:swtflutter/src/gen/tablecolumn.dart';
import 'package:swtflutter/src/gen/tableitem.dart';
import 'package:swtflutter/src/gen/tableeditor.dart';
import 'package:swtflutter/src/gen/button.dart';
import 'package:swtflutter/src/gen/rectangle.dart';

VTableColumn _col(int id, {int? width}) => VTableColumn()
  ..id = id
  ..width = width
  ..text = 'Col$id';

VTableItem _item(int id, String text) => VTableItem()
  ..id = id
  ..texts = [text, null];

/// A permanent checkbox editor placed in column 1 of [item], mirroring the
/// real-world pattern: new Button(table, SWT.CHECK), setSelection(true),
/// TableEditor.setEditor(button, item, 1).
VTableEditor _checkboxEditor(int editorId, int buttonId, VTableItem item) =>
    VTableEditor()
      ..id = editorId
      ..column = 1
      ..item = item
      ..editor = (VButton()
        ..id = buttonId
        ..style = SWT.CHECK
        ..enabled = true
        ..visible = true
        ..selection = true
        ..text = 'chk-${item.id}');

void main() {
  testWidgets(
    'permanent per-row checkbox editors render on every row with no selection',
    (tester) async {
      final items = [
        _item(10, 'analyzeTestFailure'),
        _item(11, 'executeTest'),
        _item(12, 'readFile'),
      ];

      final value = VTable()
        ..id = 1
        ..style = SWT.NONE
        ..enabled = true
        ..headerVisible = true
        ..linesVisible = true
        ..columns = [_col(1, width: 305), _col(2, width: 80)]
        ..items = items
        // The reported state: the checkbox column exists and every row has an
        // editor, but nothing is selected (live selection is []).
        ..selection = <int>[]
        ..editors = [
          _checkboxEditor(101, 201, items[0]),
          _checkboxEditor(102, 202, items[1]),
          _checkboxEditor(103, 203, items[2]),
        ]
        ..bounds = (VRectangle()
          ..x = 0
          ..y = 0
          ..width = 500
          ..height = 300);

      await tester.pumpWidget(EvolveApp(
        theme: ThemeMode.light,
        contentWidget: SizedBox(width: 500, height: 300, child: TableSwt<VTable>(value: value)),
      ));
      await tester.pumpAndSettle();

      // Every row's checkbox must be on screen even though no row is selected.
      expect(find.text('chk-10'), findsOneWidget,
          reason: 'row 0 checkbox editor must render with no selection');
      expect(find.text('chk-11'), findsOneWidget,
          reason: 'row 1 checkbox editor must render with no selection');
      expect(find.text('chk-12'), findsOneWidget,
          reason: 'row 2 checkbox editor must render with no selection');
    },
  );

  testWidgets(
    'a large table (30 permanent checkbox editors, editors also present as '
    'table children) renders every checkbox without a key collision',
    (tester) async {
      // 30 rows, each with a Button(SWT.CHECK) placed in column 1 via a
      // TableEditor. The editor Button is a child of the Table, so the same
      // VButton also appears in the Table's `children` -- exactly as the live
      // payload serializes it. This guards against building the same editor
      // twice / a GlobalKey collision.
      final items = <VTableItem>[];
      final editors = <VTableEditor>[];
      final children = <VButton>[];
      for (int i = 0; i < 30; i++) {
        final item = _item(1000 + i, 'tool_$i');
        items.add(item);
        final editor = _checkboxEditor(2000 + i, 3000 + i, item);
        editors.add(editor);
        children.add(editor.editor as VButton);
      }

      final value = VTable()
        ..id = 1
        ..style = SWT.NONE
        ..enabled = true
        ..headerVisible = true
        ..linesVisible = true
        ..columns = [_col(1, width: 305), _col(2, width: 80)]
        ..items = items
        ..children = children
        ..selection = <int>[]
        ..editors = editors
        ..bounds = (VRectangle()
          ..x = 0
          ..y = 0
          ..width = 500
          ..height = 760);

      await tester.pumpWidget(EvolveApp(
        theme: ThemeMode.light,
        contentWidget:
            SizedBox(width: 500, height: 760, child: TableSwt<VTable>(value: value)),
      ));
      await tester.pumpAndSettle();

      // No uncaught build exception (flutter_test fails the test on one) and
      // every row's checkbox is rendered.
      expect(tester.takeException(), isNull,
          reason: 'rendering 30 permanent editors must not throw '
              '(e.g. a duplicate GlobalKey)');
      for (int i = 0; i < 30; i++) {
        expect(find.text('chk-${1000 + i}'), findsOneWidget,
            reason: 'row $i checkbox editor must render');
      }
    },
  );
}
