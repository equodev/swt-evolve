// The chevron's tab list belongs to its folder: disposing the folder while the list is open takes
// the list down too, as a native disposes the folder's list menu with it.

import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/gen/ctabfolder.dart';
import 'package:swtflutter/src/gen/ctabitem.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/impl/config_flags.dart';
import 'package:swtflutter/src/impl/widget_config.dart';
// ignore: unused_import
import 'package:swtflutter/src/impl/ctabfolder_evolve.dart';

import 'delivery/support/deliver.dart';

VCTabFolder _folder({int seq = 0, int? showListPopupSeq}) => VCTabFolder()
  ..id = 1000
  ..seq = seq
  ..showListPopupSeq = showListPopupSeq
  ..style = SWT.NONE
  ..selection = 0
  ..items = List.generate(
    6,
    (i) => VCTabItem()
      ..id = 10 + i
      ..text = 'A rather long view name ${i + 1}'
      ..showing = true,
  );

Widget _host({required bool withFolder}) => EvolveApp(
      theme: ThemeMode.light,
      contentWidget: SizedBox(
        width: 300,
        height: 200,
        child: withFolder ? CTabFolderSwt<VCTabFolder>(value: _folder()) : const SizedBox.shrink(),
      ),
    );

void _drain(WidgetTester tester) {
  while (tester.takeException() != null) {}
}

void main() {
  setUp(resetConfigFlags);
  tearDown(resetConfigFlags);

  testWidgets('disposing the folder closes the chevron list it opened', (WidgetTester tester) async {
    tester.view.physicalSize = const Size(1600, 2400);
    tester.view.devicePixelRatio = 1.0;
    addTearDown(tester.view.reset);

    await tester.pumpWidget(_host(withFolder: true));
    _drain(tester);
    for (var i = 0; i < 3; i++) {
      await tester.pump();
    }
    _drain(tester);

    await tester.tap(find.byIcon(Icons.expand_more));
    await tester.pumpAndSettle();
    await deliverWhole(_folder(seq: 10, showListPopupSeq: 1));
    await tester.pumpAndSettle();
    _drain(tester);
    expect(find.byType(PopupMenuItem<int>), findsWidgets);

    await tester.pumpWidget(_host(withFolder: false));
    await tester.pumpAndSettle();
    _drain(tester);

    expect(find.byType(PopupMenuItem<int>), findsNothing);
  });
}
