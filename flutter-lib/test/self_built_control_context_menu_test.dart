// A control that builds its own tree calls ControlImpl.trackPointer() instead of routing through
// wrap(), and wrap() was the only place that attached the control's context menu. A right-click on
// such a control (a List, a Combo, a Group...) opened nothing, even though Shift+F10 opened the
// same menu through Java.

import 'package:flutter/gestures.dart';
import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/gen/ccombo.dart';
import 'package:swtflutter/src/gen/combo.dart';
import 'package:swtflutter/src/gen/control.dart';
import 'package:swtflutter/src/gen/ctabfolder.dart';
import 'package:swtflutter/src/gen/expandbar.dart';
import 'package:swtflutter/src/gen/group.dart';
import 'package:swtflutter/src/gen/list.dart';
import 'package:swtflutter/src/gen/menu.dart';
import 'package:swtflutter/src/gen/menuitem.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/sash.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/gen/tabfolder.dart';
import 'package:swtflutter/src/gen/text.dart';

import 'support/menu_shown_ack.dart';

const _menuId = 100;
const _width = 200;
const _height = 120;

VMenu _menu() => VMenu()
  ..id = _menuId
  ..style = SWT.POP_UP
  ..enabled = true
  ..items = [
    VMenuItem()
      ..id = _menuId + 1
      ..style = SWT.PUSH
      ..enabled = true
      ..text = 'Context Item',
  ];

T _withMenu<T extends VControl>(T value, int style) => value
  ..id = 1
  ..style = style
  ..enabled = true
  ..menu = _menu()
  ..bounds = (VRectangle()
    ..x = 0
    ..y = 0
    ..width = _width
    ..height = _height);

final _controls = <String, Widget Function()>{
  'List': () => ListSwt<VList>(
      value: _withMenu(VList(), SWT.SINGLE | SWT.BORDER)
        ..items = ['Alpha', 'Bravo']
        ..selection = <int>[]),
  'Combo': () => ComboSwt<VCombo>(
      value: _withMenu(VCombo(), SWT.DROP_DOWN | SWT.READ_ONLY)..items = ['Alpha', 'Bravo']),
  'CCombo': () => CComboSwt<VCCombo>(
      value: _withMenu(VCCombo(), SWT.BORDER)..items = ['Alpha', 'Bravo']),
  'Group': () => GroupSwt<VGroup>(value: _withMenu(VGroup(), SWT.NONE)..text = 'Group'),
  'Sash': () => SashSwt<VSash>(value: _withMenu(VSash(), SWT.VERTICAL)),
  'TabFolder': () => TabFolderSwt<VTabFolder>(value: _withMenu(VTabFolder(), SWT.TOP)),
  'CTabFolder': () => CTabFolderSwt<VCTabFolder>(value: _withMenu(VCTabFolder(), SWT.TOP)),
  'ExpandBar': () => ExpandBarSwt<VExpandBar>(value: _withMenu(VExpandBar(), SWT.NONE)),
  'Text': () => TextSwt<VText>(value: _withMenu(VText(), SWT.SINGLE | SWT.BORDER)..text = 'abc'),
};

void main() {
  for (final entry in _controls.entries) {
    testWidgets('right-clicking a ${entry.key} opens the menu set on it', (tester) async {
      await tester.pumpWidget(EvolveApp(
        theme: ThemeMode.light,
        contentWidget: SizedBox(
          width: _width.toDouble(),
          height: _height.toDouble(),
          child: entry.value(),
        ),
      ));
      await tester.pump();

      await tester.tapAt(const Offset(_width / 2, _height / 2), buttons: kSecondaryButton);
      await tester.pump();
      await tester.pump();
      await ackMenuShown(tester, _menuId);

      expect(find.text('Context Item'), findsOneWidget);
    });
  }
}
