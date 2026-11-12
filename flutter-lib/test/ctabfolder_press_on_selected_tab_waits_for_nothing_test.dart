// Pressing the tab that is already selected changes nothing in Java, so Java sends no selection back.
// The folder must not then wait for one: a later selection Java makes on its own, such as a
// workbench selecting a tab it moved within the folder, is the one to show.

import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/gen/ctabfolder.dart';
import 'package:swtflutter/src/gen/ctabitem.dart';
import 'package:swtflutter/src/gen/event.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/gen/widgets.dart' show WireKey;

import 'delivery/support/deliver.dart';

class _QuietCTabFolderSwt extends CTabFolderSwt<VCTabFolder> {
  const _QuietCTabFolderSwt({required super.value});

  @override
  void sendEvent(VCTabFolder val, int channel, VEvent? payload) {}
}

VCTabFolder _folder({required int selection, int seq = 10}) => VCTabFolder()
  ..id = 1
  ..style = SWT.NONE
  ..seq = seq
  ..enabled = true
  ..selection = selection
  ..items = List.generate(
    2,
    (i) => VCTabItem()
      ..id = 100 + i
      ..text = 'Tab $i'
      ..showing = true,
  );

int? _clientSelection(WidgetTester tester) =>
    (tester.state(find.byType(_QuietCTabFolderSwt)) as dynamic).state.selection as int?;

void main() {
  testWidgets('after a press on the selected tab, the folder takes the selection Java makes next', (tester) async {
    freshClient();
    await tester.pumpWidget(EvolveApp(
      theme: ThemeMode.light,
      contentWidget: SizedBox(width: 600, height: 240, child: _QuietCTabFolderSwt(value: _folder(selection: 1))),
    ));
    await tester.pumpAndSettle();
    while (tester.takeException() != null) {}

    await tester.tap(find.text('Tab 1'));
    await tester.pumpAndSettle();
    expect(_clientSelection(tester), 1, reason: 'sanity: the pressed tab was the selected one');

    await deliverChange(_folder(selection: 0, seq: 11), keys: [WireKey.k_CTabFolder_selection], base: 10);
    await tester.pumpAndSettle();

    expect(_clientSelection(tester), 0, reason: 'nothing was pending: Java selected the other tab itself');
    await tester.pump(const Duration(milliseconds: 400));
    while (tester.takeException() != null) {}
  });
}
