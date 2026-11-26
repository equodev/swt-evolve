// A primary click that closes an open menu does not reach a control that answers the press itself
// (a row that selects on the press, a part that fires on its release): it only closes the menu.
// The next click, with no menu open, is delivered as usual.

import 'package:flutter/gestures.dart';
import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/comm/comm_frame.dart';
import 'package:swtflutter/src/comm/comm_ws.dart';
import 'package:swtflutter/src/gen/ccombo.dart';
import 'package:swtflutter/src/gen/combo.dart';
import 'package:swtflutter/src/gen/composite.dart';
import 'package:swtflutter/src/gen/link.dart';
import 'package:swtflutter/src/gen/list.dart';
import 'package:swtflutter/src/gen/menu.dart';
import 'package:swtflutter/src/gen/menuitem.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/sash.dart';
import 'package:swtflutter/src/gen/scale.dart';
import 'package:swtflutter/src/gen/spinner.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/gen/tabfolder.dart';
import 'package:swtflutter/src/gen/tabitem.dart';
import 'package:swtflutter/src/gen/table.dart';
import 'package:swtflutter/src/gen/tablecolumn.dart';
import 'package:swtflutter/src/gen/tableitem.dart';
import 'package:swtflutter/src/gen/text.dart';
import 'package:swtflutter/src/gen/tree.dart';
import 'package:swtflutter/src/gen/treeitem.dart';

import 'delivery/support/deliver.dart';
import 'support/menu_shown_ack.dart';
import 'support/sent_events.dart';

const _treeId = 1;
const _menuId = 100;
const _id = 5;

VRectangle _rect(int x, int y, int w, int h) => VRectangle()
  ..x = x
  ..y = y
  ..width = w
  ..height = h;

VTree _tree() => VTree()
  ..id = _treeId
  ..style = SWT.SINGLE
  ..enabled = true
  ..menu = (VMenu()
    ..id = _menuId
    ..style = SWT.POP_UP
    ..enabled = true
    ..items = [
      VMenuItem()
        ..id = 101
        ..style = SWT.PUSH
        ..enabled = true
        ..text = 'Reload',
    ])
  ..items = [
    VTreeItem()
      ..id = 10
      ..text = 'alpha',
    VTreeItem()
      ..id = 11
      ..text = 'beta',
  ]
  ..bounds = _rect(0, 0, 200, 300);

List<String> _sent() => sentEvents().map((f) => f.$1).toList();

List<String> _sentBy(int id) => _sent().where((channel) => channel.contains('/$id/')).toList();

void main() {
  setUp(() => EquoCommBase.recordSentFrames = true);
  tearDown(() => EquoCommBase.recordSentFrames = false);

  /// Shows the tree and [target] side by side and opens the tree's context menu from the tree's
  /// empty bottom, so the menu hangs clear of the rows and of [target] at the top.
  Future<void> openMenuBeside(WidgetTester tester, [Widget? target]) async {
    freshClient();
    await tester.pumpWidget(EvolveApp(
      theme: ThemeMode.light,
      contentWidget: Row(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          SizedBox(width: 200, height: 300, child: TreeSwt<VTree>(value: _tree())),
          SizedBox(width: 400, height: 300, child: target ?? const SizedBox()),
        ],
      ),
    ));
    await tester.pumpAndSettle();
    await tester.tapAt(tester.getTopLeft(find.byType(TreeSwt<VTree>)) + const Offset(20, 260),
        buttons: kSecondaryButton);
    await tester.pump();
    await ackMenuShown(tester, _menuId);
    await tester.pumpAndSettle();
    expect(find.text('Reload'), findsOneWidget, reason: 'the right click opens the menu');
    EquoCommService.commForTesting.clearSentFrames();
  }

  Future<void> clickAt(WidgetTester tester, Offset at) async {
    await tester.tapAt(at);
    await tester.pumpAndSettle();
    expect(_sent(), isNot(contains('MenuItem/101/Selection/Selection')), reason: 'the click lands outside the menu');
  }

  /// Opens the menu, clicks [at] [target], and checks that click only closed the menu and that
  /// the next one sends [expected].
  Future<void> dismissThenClick(WidgetTester tester, Widget target, Finder at, String expected) async {
    await openMenuBeside(tester, target);

    await clickAt(tester, tester.getCenter(at));
    expect(find.text('Reload'), findsNothing, reason: 'the click closes the menu');
    expect(_sentBy(_id), isEmpty, reason: 'the click that closes the menu reaches nothing below');

    await clickAt(tester, tester.getCenter(at));
    expect(_sentBy(_id), contains(expected), reason: 'the next click, with no menu open, is delivered');
  }

  testWidgets('a Tree row does not select on the click that closes a menu', (tester) async {
    await openMenuBeside(tester);

    await clickAt(tester, tester.getCenter(find.text('beta')));
    expect(find.text('Reload'), findsNothing, reason: 'the click closes the menu');
    // The tree's FocusIn is the focus coming back to it as the menu closes, not the click's.
    expect(_sentBy(_treeId).where((channel) => !channel.contains('/Focus/')), isEmpty,
        reason: 'no MouseDown, Selection or MouseUp from that click');

    await clickAt(tester, tester.getCenter(find.text('beta')));
    expect(_sentBy(_treeId), contains('Tree/$_treeId/Selection/Selection'));
  });

  testWidgets('a Table row does not take the click that closes a menu', (tester) async {
    final table = VTable()
      ..swt = 'Table'
      ..id = _id
      ..style = SWT.SINGLE
      ..enabled = true
      ..columns = [VTableColumn()
        ..id = 6
        ..width = 300
        ..text = 'Name']
      ..items = [VTableItem()
        ..id = 7
        ..texts = ['row']]
      ..bounds = _rect(0, 0, 400, 300);
    await dismissThenClick(tester, TableSwt<VTable>(value: table), find.text('row'), 'Table/$_id/Mouse/MouseDown');
  });

  testWidgets('a List item does not select on the click that closes a menu', (tester) async {
    final list = VList()
      ..id = _id
      ..style = SWT.SINGLE
      ..enabled = true
      ..items = ['one', 'two']
      ..selection = <int>[]
      ..bounds = _rect(0, 0, 400, 300);
    await dismissThenClick(tester, ListSwt<VList>(value: list), find.text('two'), 'List/$_id/Selection/Selection');
  });

  testWidgets('a Link anchor does not fire on the click that closes a menu', (tester) async {
    final link = VLink()
      ..id = _id
      ..style = SWT.NONE
      ..enabled = true
      ..visible = true
      ..text = '<a>anchor</a>'
      ..bounds = _rect(0, 0, 400, 20);
    await dismissThenClick(tester, SizedBox(height: 20, child: LinkSwt<VLink>(value: link)), find.textContaining('anchor', findRichText: true),
        'Link/$_id/Selection/Selection');
  });

  testWidgets('a Scale does not report the click that closes a menu', (tester) async {
    final scale = VScale()
      ..id = _id
      ..style = SWT.HORIZONTAL
      ..enabled = true
      ..minimum = 0
      ..maximum = 100
      ..selection = 0
      ..bounds = _rect(0, 0, 300, 40);
    await dismissThenClick(tester, Align(alignment: Alignment.topLeft, child: ScaleSwt<VScale>(value: scale)),
        find.byType(ScaleSwt<VScale>), 'Scale/$_id/Selection/Selection');
  });

  testWidgets('a Sash does not report the click that closes a menu', (tester) async {
    final sash = VSash()
      ..id = _id
      ..style = SWT.VERTICAL
      ..enabled = true
      ..bounds = _rect(0, 0, 10, 300);
    await dismissThenClick(tester, Align(alignment: Alignment.topLeft, child: SizedBox(width: 10, child: SashSwt<VSash>(value: sash))),
        find.byType(SashSwt<VSash>), 'Sash/$_id/Selection/Selection');
  });

  testWidgets('a Spinner arrow does not step on the click that closes a menu', (tester) async {
    final spinner = VSpinner()
      ..id = _id
      ..style = SWT.BORDER
      ..enabled = true
      ..minimum = 0
      ..maximum = 10
      ..selection = 0
      ..increment = 1
      ..digits = 0
      ..bounds = _rect(0, 0, 120, 30);
    await dismissThenClick(
        tester,
        Align(alignment: Alignment.topLeft, child: SizedBox(width: 120, height: 30, child: SpinnerSwt<VSpinner>(value: spinner))),
        find.byIcon(Icons.arrow_drop_up),
        'Spinner/$_id/Selection/Selection');
  });

  testWidgets('a read-only Combo does not open its list on the click that closes a menu', (tester) async {
    final combo = VCombo()
      ..swt = 'Combo'
      ..id = _id
      ..style = SWT.READ_ONLY
      ..enabled = true
      ..items = ['first', 'second']
      ..text = 'first'
      ..bounds = _rect(0, 0, 200, 30);
    await openMenuBeside(tester,
        Align(alignment: Alignment.topLeft, child: SizedBox(width: 200, height: 30, child: ComboSwt<VCombo>(value: combo))));

    await clickAt(tester, tester.getCenter(find.byType(ComboSwt<VCombo>)));
    expect(find.text('Reload'), findsNothing, reason: 'the click closes the menu');
    expect(find.text('second'), findsNothing, reason: 'the list stays closed');

    await clickAt(tester, tester.getCenter(find.byType(ComboSwt<VCombo>)));
    expect(find.text('second'), findsOneWidget, reason: 'the next click opens the list');
  });

  testWidgets('a read-only CCombo does not open its list on the click that closes a menu', (tester) async {
    final combo = VCCombo()
      ..swt = 'CCombo'
      ..id = _id
      ..style = SWT.BORDER | SWT.READ_ONLY
      ..enabled = true
      ..visible = true
      ..text = 'first'
      ..items = ['first', 'second']
      ..bounds = _rect(0, 0, 200, 30);
    await openMenuBeside(tester,
        Align(alignment: Alignment.topLeft, child: SizedBox(width: 200, height: 30, child: CComboSwt<VCCombo>(value: combo))));

    await clickAt(tester, tester.getCenter(find.byType(CComboSwt<VCCombo>)));
    expect(find.text('Reload'), findsNothing, reason: 'the click closes the menu');
    expect(find.text('second'), findsNothing, reason: 'the list stays closed');

    await clickAt(tester, tester.getCenter(find.byType(CComboSwt<VCCombo>)));
    expect(find.text('second'), findsOneWidget, reason: 'the next click opens the list');
  });

  testWidgets('a Text at rest does not start editing on the click that closes a menu', (tester) async {
    final text = VText()
      ..id = _id
      ..style = SWT.SINGLE | SWT.BORDER
      ..text = 'words'
      ..visible = true
      ..enabled = true
      ..bounds = _rect(0, 0, 200, 30);
    await openMenuBeside(tester,
        Align(alignment: Alignment.topLeft, child: SizedBox(width: 200, height: 30, child: TextSwt<VText>(value: text))));

    await clickAt(tester, tester.getCenter(find.byType(TextSwt<VText>)));
    expect(find.text('Reload'), findsNothing, reason: 'the click closes the menu');
    expect(find.byType(EditableText), findsNothing, reason: 'the field stays at rest');

    await clickAt(tester, tester.getCenter(find.byType(TextSwt<VText>)));
    expect(find.byType(EditableText), findsOneWidget, reason: 'the next click starts editing');
  });

  testWidgets('a TabFolder tab is not selected later by the click that closed a menu', (tester) async {
    VTabItem tab(int id, String text) => VTabItem()
      ..id = id
      ..text = text
      ..control = (VComposite()
        ..id = id + 10
        ..style = SWT.NONE
        ..bounds = _rect(0, 24, 400, 200));
    final folder = VTabFolder()
      ..id = _id
      ..style = SWT.NONE
      ..enabled = true
      ..bounds = _rect(0, 0, 400, 224)
      ..items = [tab(6, 'One'), tab(7, 'Two')];
    await openMenuBeside(tester,
        Align(alignment: Alignment.topLeft, child: SizedBox(width: 400, height: 224, child: TabFolderSwt<VTabFolder>(value: folder))));

    await clickAt(tester, tester.getCenter(find.text('Two')));
    expect(find.text('Reload'), findsNothing, reason: 'the click closes the menu');
    // Past the tabs, on the strip: a press there selects no tab.
    await clickAt(tester, Offset(tester.getTopRight(find.byType(TabFolderSwt<VTabFolder>)).dx - 20, tester.getCenter(find.text('Two')).dy));
    expect(_sentBy(_id).where((channel) => channel.contains('Selection')), isEmpty);
  });
}
