// SWT parity: a JFace cell editor commits when it loses the keyboard, and in SWT a click
// anywhere outside it takes the keyboard away. `CellEditor.focusLost()` is the whole contract --
// `TextCellEditor` hooks a FocusListener on its Text and applies the value from there.
//
// Flutter does not move focus on a tap that lands on a non-focusable area, and the Text field
// suppresses Flutter's own tap-outside unfocus (see TextImpl's `onTapOutside`), so nothing took
// the keyboard off an open cell editor: the caret kept blinking and Java never saw FocusOut.
// The Table owns the editor overlay, so it is what watches for the outside tap.

import 'package:flutter/material.dart';
import 'package:flutter/widgets.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/gen/table.dart';
import 'package:swtflutter/src/gen/tablecolumn.dart';
import 'package:swtflutter/src/gen/tableeditor.dart';
import 'package:swtflutter/src/gen/tableitem.dart';
import 'package:swtflutter/src/gen/text.dart';
import 'package:swtflutter/src/gen/button.dart';
import 'package:swtflutter/src/impl/focus_requests.dart';

const int editorTextId = 4343;

VTableColumn _col(int id, int width) => VTableColumn()
  ..id = id
  ..width = width
  ..text = 'Col$id';

VTableItem _item(int id, String name) => VTableItem()
  ..id = id
  ..texts = [name, ''];

/// The Text cell editor JFace opens on the Value column of the selected row.
VTableEditor _valueEditor(VTableItem item) => VTableEditor()
  ..id = 900
  ..column = 1
  ..item = item
  ..editor = (VText()
    ..swt = 'Text'
    ..id = editorTextId
    ..style = SWT.SINGLE
    ..enabled = true
    ..visible = true
    ..editable = true
    ..text = 'typed');

VTable _tableWithOpenEditor() {
  final edited = _item(10, 'var1');
  final other = _item(11, 'var2');
  return VTable()
    ..swt = 'Table'
    ..id = 1
    ..style = SWT.NONE
    ..enabled = true
    ..headerVisible = true
    ..columns = [_col(1, 150), _col(2, 150)]
    ..items = [edited, other]
    ..selection = <int>[0]
    ..editors = [_valueEditor(edited)]
    ..bounds = (VRectangle()
      ..x = 0
      ..y = 0
      ..width = 300
      ..height = 120);
}

/// The table sits at the top; everything below it is the "outside" the report clicks on.
Future<void> _pumpEditingTable(WidgetTester tester) async {
  await tester.pumpWidget(EvolveApp(
    theme: ThemeMode.light,
    contentWidget: Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        SizedBox(
          width: 300,
          height: 120,
          child: TableSwt<VTable>(value: _tableWithOpenEditor()),
        ),
        const Expanded(child: SizedBox.expand()),
      ],
    ),
  ));
  await tester.pumpAndSettle();
}

bool _editorHasFocus(WidgetTester tester) =>
    tester.widget<EditableText>(find.byType(EditableText)).focusNode.hasFocus;

/// Puts the keyboard in the editor the way JFace does when it activates the cell.
Future<void> _activateEditor(WidgetTester tester) async {
  FocusRequests.instance.request(editorTextId);
  await tester.pumpAndSettle();
  await tester.pump();
  expect(_editorHasFocus(tester), isTrue,
      reason: 'sanity: the cell editor starts out holding the keyboard');
}

/// A Table that keeps a control on every row for good -- the SWT pattern this must not disturb:
/// `TableEditor.setEditor(new Button(SWT.CHECK), item, col)` per row, tied to neither selection
/// nor focus.
VTable _tableWithPermanentEditors() {
  final rows = [_item(20, 'a'), _item(21, 'b')];
  return VTable()
    ..swt = 'Table'
    ..id = 2
    ..style = SWT.NONE
    ..enabled = true
    ..headerVisible = true
    ..columns = [_col(1, 150), _col(2, 150)]
    ..items = rows
    ..selection = <int>[0]
    ..editors = [
      for (var i = 0; i < rows.length; i++)
        VTableEditor()
          ..id = 910 + i
          ..column = 1
          ..item = rows[i]
          ..editor = (VButton()
            ..swt = 'Button'
            ..id = 5050 + i
            ..style = SWT.CHECK
            ..enabled = true
            ..visible = true)
    ]
    ..bounds = (VRectangle()
      ..x = 0
      ..y = 0
      ..width = 300
      ..height = 120);
}

void main() {
  setUp(() => FocusRequests.instance.reset());
  tearDown(() => FocusRequests.instance.reset());

  testWidgets('a click outside the table takes the keyboard off the cell editor',
      (tester) async {
    await _pumpEditingTable(tester);
    await _activateEditor(tester);

    // Empty space well below the table -- nothing focusable under the pointer.
    await tester.tapAt(const Offset(150, 400));
    await tester.pumpAndSettle();

    expect(_editorHasFocus(tester), isFalse,
        reason: 'SWT parity: any click outside the cell editor ends the edit, which is what '
            'sends Focus/FocusOut and lets JFace apply the value');
  });

  testWidgets('a click on another row of the same table also releases the keyboard',
      (tester) async {
    await _pumpEditingTable(tester);
    await _activateEditor(tester);

    await tester.tapAt(const Offset(60, 100));
    await tester.pumpAndSettle();

    expect(_editorHasFocus(tester), isFalse);
  });

  testWidgets('a click inside the cell editor keeps the edit open', (tester) async {
    await _pumpEditingTable(tester);
    await _activateEditor(tester);

    await tester.tap(find.byType(EditableText));
    await tester.pumpAndSettle();

    expect(_editorHasFocus(tester), isTrue,
        reason: 'clicking the editor itself must not end the edit');
  });

  testWidgets('a permanent per-row editor does not pull focus off an unrelated field',
      (tester) async {
    final outside = VText()
      ..swt = 'Text'
      ..id = 6060
      ..style = SWT.SINGLE
      ..enabled = true
      ..editable = true
      ..text = 'elsewhere';

    await tester.pumpWidget(EvolveApp(
      theme: ThemeMode.light,
      contentWidget: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          SizedBox(
            width: 300,
            height: 120,
            child: TableSwt<VTable>(value: _tableWithPermanentEditors()),
          ),
          SizedBox(width: 300, height: 40, child: TextSwt<VText>(value: outside)),
          const Expanded(child: SizedBox.expand()),
        ],
      ),
    ));
    await tester.pumpAndSettle();

    await tester.tap(find.byType(EditableText));
    await tester.pumpAndSettle();
    expect(_editorHasFocus(tester), isTrue, reason: 'sanity: the field outside the table has focus');

    // A click on empty space: the table's permanent editors never held the keyboard, so nothing
    // of theirs may take it off the field that does.
    await tester.tapAt(const Offset(150, 400));
    await tester.pumpAndSettle();

    expect(_editorHasFocus(tester), isTrue,
        reason: 'only an editor that holds the keyboard releases it on an outside click');
  });
}
