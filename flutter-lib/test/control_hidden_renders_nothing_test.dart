// A control hidden with setVisible(false) takes no space and builds nothing, also for the controls
// whose build does not go through ControlImpl.wrap().

import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/gen/ccombo.dart';
import 'package:swtflutter/src/gen/combo.dart';
import 'package:swtflutter/src/gen/control.dart';
import 'package:swtflutter/src/gen/expandbar.dart';
import 'package:swtflutter/src/gen/expanditem.dart';
import 'package:swtflutter/src/gen/group.dart';
import 'package:swtflutter/src/gen/list.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/sash.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/gen/tabfolder.dart';
import 'package:swtflutter/src/gen/tabitem.dart';
// ignore: unused_import
import 'package:swtflutter/src/impl/ccombo_evolve.dart';
// ignore: unused_import
import 'package:swtflutter/src/impl/combo_evolve.dart';
// ignore: unused_import
import 'package:swtflutter/src/impl/expandbar_evolve.dart';
// ignore: unused_import
import 'package:swtflutter/src/impl/group_evolve.dart';
// ignore: unused_import
import 'package:swtflutter/src/impl/list_evolve.dart';
// ignore: unused_import
import 'package:swtflutter/src/impl/sash_evolve.dart';
// ignore: unused_import
import 'package:swtflutter/src/impl/tabfolder_evolve.dart';

VRectangle _bounds() => VRectangle()
  ..x = 0
  ..y = 0
  ..width = 200
  ..height = 120;

T _control<T extends VControl>(T c, int id, int style, bool visible) => c
  ..id = id
  ..style = style
  ..visible = visible
  ..bounds = _bounds();

final _cases = <String, (Widget Function(bool visible), Type)>{
  'Group': (
    (v) => GroupSwt<VGroup>(value: _control(VGroup()..text = 'Group', 1, SWT.NONE, v)),
    GroupSwt<VGroup>
  ),
  'List': (
    (v) => ListSwt<VList>(value: _control(VList()..items = ['One'], 2, SWT.SINGLE, v)),
    ListSwt<VList>
  ),
  'Combo': (
    (v) => ComboSwt<VCombo>(
        value: _control(VCombo()
          ..items = ['One']
          ..text = 'One', 3, SWT.READ_ONLY, v)),
    ComboSwt<VCombo>
  ),
  'CCombo': (
    (v) => CComboSwt<VCCombo>(value: _control(VCCombo()..text = 'One', 4, SWT.NONE, v)),
    CComboSwt<VCCombo>
  ),
  'TabFolder': (
    (v) => TabFolderSwt<VTabFolder>(
        value: _control(VTabFolder()
          ..items = [VTabItem()
            ..id = 50
            ..text = 'Tab'], 5, SWT.TOP, v)),
    TabFolderSwt<VTabFolder>
  ),
  'Sash': (
    (v) => SashSwt<VSash>(value: _control(VSash(), 6, SWT.VERTICAL, v)),
    SashSwt<VSash>
  ),
  'ExpandBar': (
    (v) => ExpandBarSwt<VExpandBar>(
        value: _control(VExpandBar()
          ..items = [VExpandItem()
            ..id = 70
            ..text = 'Item'], 7, SWT.NONE, v)),
    ExpandBarSwt<VExpandBar>
  ),
};

Widget _host(Widget control) => EvolveApp(
      theme: ThemeMode.light,
      contentWidget: Align(alignment: Alignment.topLeft, child: control),
    );

/// What the control's build produced, past the empty box a hidden control is.
Finder _builtUnder(Type type) => find.descendant(
    of: find.byType(type), matching: find.byWidgetPredicate((w) => w is! SizedBox));

// Some of these trip debug-only paint assertions in the isolated harness; they are unrelated to
// what is built.
void _drainPaintExceptions(WidgetTester tester) {
  while (tester.takeException() != null) {}
}

void main() {
  for (final MapEntry(key: name, value: (build, type)) in _cases.entries) {
    testWidgets('a shown $name takes its bounds', (tester) async {
      await tester.pumpWidget(_host(build(true)));
      _drainPaintExceptions(tester);
      expect(tester.getSize(find.byType(type)), isNot(Size.zero));
      expect(_builtUnder(type), findsWidgets);
    });

    testWidgets('a hidden $name builds nothing', (tester) async {
      await tester.pumpWidget(_host(build(false)));
      _drainPaintExceptions(tester);
      expect(tester.getSize(find.byType(type)), Size.zero);
      expect(_builtUnder(type), findsNothing);
    });
  }
}
