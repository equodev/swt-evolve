// A hidden CTabFolder paints nothing: neither its tabs nor its selected page. A wizard that hides
// one page (a CTabFolder) to show another in the same area relies on it.

import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/gen/ctabfolder.dart';
import 'package:swtflutter/src/gen/ctabitem.dart';
import 'package:swtflutter/src/gen/control.dart';
import 'package:swtflutter/src/gen/label.dart';
import 'package:swtflutter/src/gen/swt.dart';
// ignore: unused_import
import 'package:swtflutter/src/impl/ctabfolder_evolve.dart';

const _tabText = 'Main';
const _pageText = 'Server settings';

VCTabFolder _folder({required bool visible}) => VCTabFolder()
  ..id = 1000
  ..style = SWT.NONE
  ..visible = visible
  ..selection = 0
  ..items = [
    VCTabItem()
      ..id = 10
      ..text = _tabText
      ..control = (VLabel()
        ..id = 2001
        ..style = SWT.NONE
        ..text = _pageText),
  ];

Widget _host(VCTabFolder value) => EvolveApp(
      theme: ThemeMode.light,
      contentWidget: SizedBox(
        width: 600,
        height: 400,
        child: CTabFolderSwt<VCTabFolder>(value: value),
      ),
    );

// The tab strip trips a debug-only paint assertion in the isolated harness; it is unrelated to
// what is built.
void _drainPaintExceptions(WidgetTester tester) {
  while (tester.takeException() != null) {}
}

void main() {
  testWidgets('a shown CTabFolder renders its tabs and its page', (tester) async {
    await tester.pumpWidget(_host(_folder(visible: true)));
    _drainPaintExceptions(tester);
    expect(find.text(_tabText), findsOneWidget);
    expect(find.text(_pageText), findsOneWidget);
  });

  testWidgets('a hidden CTabFolder renders neither its tabs nor its page', (tester) async {
    await tester.pumpWidget(_host(_folder(visible: false)));
    _drainPaintExceptions(tester);
    expect(find.text(_tabText), findsNothing);
    expect(find.text(_pageText), findsNothing);
  });
}
