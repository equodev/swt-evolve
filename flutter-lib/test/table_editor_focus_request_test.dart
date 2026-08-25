// Regression test: a cell editor Java opens programmatically must take keyboard focus.
//
// JFace's TableViewer.editElement(element, column) -- what an "Add row" toolbar item calls --
// creates the TextCellEditor's Text, places it with TableEditor.setEditor(...) and then calls
// setFocus() on it. Java tracking the focus holder is not enough on the whole-tree surface: the
// render side owns the real focus, so without a request crossing the bridge the new editor renders
// but swallows every keystroke until the user clicks the cell.
//
// The request also has to survive arriving before the editor exists on the client: Java sends it in
// the same event-loop pass that creates the control, and the control is only built one frame after
// its parent Table's state push.

import 'package:flutter/material.dart';
import 'package:flutter/widgets.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/gen/table.dart';
import 'package:swtflutter/src/gen/tablecolumn.dart';
import 'package:swtflutter/src/gen/tableeditor.dart';
import 'package:swtflutter/src/gen/tableitem.dart';
import 'package:swtflutter/src/gen/text.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/impl/focus_requests.dart';

const int editorTextId = 4242;

VTableColumn _col(int id, {int? width}) => VTableColumn()
  ..id = id
  ..width = width
  ..text = 'Col$id';

VTableItem _item(int id, String text) => VTableItem()
  ..id = id
  ..texts = [text, null];

/// The text cell editor JFace opens on the new row: a Text placed in column 0 of [item].
VTableEditor _textEditor(VTableItem item) => VTableEditor()
  ..id = 900
  ..column = 0
  ..item = item
  ..editor = (VText()
    ..id = editorTextId
    ..style = SWT.SINGLE
    ..enabled = true
    ..visible = true
    ..text = '');

VTable _tableWithNewRowEditor() {
  final existing = _item(10, 'name');
  final added = _item(11, '');
  return VTable()
    ..id = 1
    ..style = SWT.NONE
    ..enabled = true
    ..headerVisible = true
    ..columns = [_col(1, width: 200), _col(2, width: 120)]
    ..items = [existing, added]
    ..selection = <int>[1]
    ..editors = [_textEditor(added)]
    ..bounds = (VRectangle()
      ..x = 0
      ..y = 0
      ..width = 400
      ..height = 200);
}

Future<void> _pumpTable(WidgetTester tester, VTable value) async {
  await tester.pumpWidget(EvolveApp(
    theme: ThemeMode.light,
    contentWidget:
        SizedBox(width: 400, height: 200, child: TableSwt<VTable>(value: value)),
  ));
  await tester.pumpAndSettle();
}

bool _editorHasFocus(WidgetTester tester) =>
    tester.widget<EditableText>(find.byType(EditableText)).focusNode.hasFocus;

void main() {
  setUp(() => FocusRequests.instance.reset());
  tearDown(() => FocusRequests.instance.reset());

  testWidgets('a focus request that lands before the editor is built still focuses it',
      (tester) async {
    // Java's order on Add: create the Text, flush the Table (which carries it), ask for focus.
    // The client sees the request while the editor is still one frame away from existing.
    FocusRequests.instance.request(editorTextId);

    await _pumpTable(tester, _tableWithNewRowEditor());

    expect(_editorHasFocus(tester), isTrue,
        reason: 'the new row cell editor must take keyboard focus without a click');
  });

  testWidgets('a focus request for a mounted editor focuses it', (tester) async {
    await _pumpTable(tester, _tableWithNewRowEditor());
    expect(_editorHasFocus(tester), isFalse);

    FocusRequests.instance.request(editorTextId);
    await tester.pumpAndSettle();
    // The request is applied in a post-frame callback; FocusManager only publishes the change at
    // the start of the following frame.
    await tester.pump();

    expect(_editorHasFocus(tester), isTrue);
  });

  testWidgets('a request is honoured once and does not pull focus back on a rebuild',
      (tester) async {
    final value = _tableWithNewRowEditor();
    FocusRequests.instance.request(editorTextId);
    await _pumpTable(tester, value);
    expect(_editorHasFocus(tester), isTrue);

    // The user moves on; a later Table push must not drag focus back into the editor.
    tester.widget<EditableText>(find.byType(EditableText)).focusNode.unfocus();
    await tester.pumpAndSettle();
    value.seq = value.seq + 1;
    await _pumpTable(tester, value);

    expect(_editorHasFocus(tester), isFalse,
        reason: 'a consumed focus request must not be replayed');
  });
}
