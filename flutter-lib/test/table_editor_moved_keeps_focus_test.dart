// A control the Table holds becomes its editor's control after it took the keyboard focus: the
// application focused its Text before Java's push placing the editor arrived. Moving it from the
// Table's child layer to the editor overlay builds it anew; the focus goes with it, rather than back
// to the Table, which would close an editor that commits on focus-out.

import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/gen/table.dart';
import 'package:swtflutter/src/gen/tablecolumn.dart';
import 'package:swtflutter/src/gen/tableeditor.dart';
import 'package:swtflutter/src/gen/tableitem.dart';
import 'package:swtflutter/src/gen/text.dart';
import 'package:swtflutter/src/impl/focus_requests.dart';

import 'delivery/support/deliver.dart';

const int tableId = 1;
const int textId = 4646;

VTableColumn _col(int id) => VTableColumn()
  ..id = id
  ..width = 150
  ..text = 'Col$id';

VTableItem _item(int id, String name) => VTableItem()
  ..id = id
  ..texts = [name, '1'];

final _rows = [_item(10, 'row0'), _item(11, 'row1')];

VText _text({bool visible = true}) => VText()
  ..swt = 'Text'
  ..id = textId
  ..style = SWT.SINGLE
  ..enabled = true
  ..visible = visible
  ..editable = true
  ..text = '1'
  ..bounds = (VRectangle()
    ..x = 150
    ..y = 25
    ..width = 150
    ..height = 25);

VTable _table({required bool editing, required int seq, bool shown = true}) => VTable()
  ..swt = 'Table'
  ..id = tableId
  ..seq = seq
  ..style = SWT.FULL_SELECTION
  ..enabled = true
  ..tabStop = true
  ..headerVisible = true
  ..columns = [_col(1), _col(2)]
  ..items = _rows
  ..selection = <int>[]
  ..children = [_text(visible: shown)]
  ..editors = editing
      ? [
          VTableEditor()
            ..id = 900
            ..column = 1
            ..item = _rows[1]
            ..editor = _text(visible: shown)
        ]
      : <VTableEditor>[]
  ..bounds = (VRectangle()
    ..x = 0
    ..y = 0
    ..width = 300
    ..height = 150);

bool _textFocused(WidgetTester tester) =>
    tester.widgetList<EditableText>(find.byType(EditableText)).any((e) => e.focusNode.hasPrimaryFocus);

void main() {
  setUp(() => FocusRequests.instance.reset());
  tearDown(() => FocusRequests.instance.reset());

  testWidgets('a focused control the Table holds keeps the focus when it becomes the editor', (tester) async {
    freshClient();
    await tester.pumpWidget(EvolveApp(
      theme: ThemeMode.light,
      contentWidget: Align(
        alignment: Alignment.topLeft,
        child: SizedBox(width: 300, height: 150, child: TableSwt<VTable>(value: _table(editing: false, seq: 10))),
      ),
    ));
    await tester.pumpAndSettle();
    FocusRequests.instance.request(tableId);
    await tester.pumpAndSettle();
    FocusRequests.instance.request(textId);
    await tester.pumpAndSettle();
    expect(_textFocused(tester), isTrue, reason: 'sanity: the Text took the focus in the child layer');

    await deliverWhole(_table(editing: true, seq: 11));
    await tester.pumpAndSettle();

    expect(_textFocused(tester), isTrue);
  });

  // A control that leaves its place hidden takes no focus with it: shown later, it is not focused
  // unless Java asks again.
  testWidgets('a focused control the Table holds, hidden as it becomes the editor, keeps no focus for later',
      (tester) async {
    freshClient();
    await tester.pumpWidget(EvolveApp(
      theme: ThemeMode.light,
      contentWidget: Align(
        alignment: Alignment.topLeft,
        child: SizedBox(width: 300, height: 150, child: TableSwt<VTable>(value: _table(editing: false, seq: 10))),
      ),
    ));
    await tester.pumpAndSettle();
    FocusRequests.instance.request(textId);
    await tester.pumpAndSettle();
    expect(_textFocused(tester), isTrue, reason: 'sanity: the Text took the focus in the child layer');

    await deliverWhole(_table(editing: true, seq: 11, shown: false));
    await tester.pumpAndSettle();
    await deliverWhole(_table(editing: true, seq: 12));
    await tester.pumpAndSettle();

    expect(_textFocused(tester), isFalse);
  });
}
