// The strip highlights the tab Java holds selected and the body shows its page. Java selects the
// first tab a folder gets, so a folder with tabs and an empty selection is one whose selection was
// cleared (setSelection(new TabItem[0])): no tab is highlighted and no page is shown.

import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/gen/event.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/gen/tabfolder.dart';
import 'package:swtflutter/src/gen/tabitem.dart';
import 'package:swtflutter/src/gen/widgets.dart' show wireChannelNames;
import 'package:swtflutter/src/impl/ctabfolder_evolve.dart';

class _RecordingTabFolderSwt extends TabFolderSwt<VTabFolder> {
  final List<String> calls;
  const _RecordingTabFolderSwt({required VTabFolder value, required this.calls}) : super(value: value);

  @override
  void sendEvent(VTabFolder val, int channel, VEvent? payload) => calls.add(wireChannelNames[channel]!);
}

final _one = VTabItem()
  ..id = 11
  ..text = 'One';
final _two = VTabItem()
  ..id = 12
  ..text = 'Two';

VTabFolder _folder(List<VTabItem> selection) => VTabFolder()
  ..id = 10
  ..style = SWT.TOP
  ..enabled = true
  ..bounds = (VRectangle()
    ..x = 0
    ..y = 0
    ..width = 300
    ..height = 120)
  ..items = [_one, _two]
  ..selection = selection;

Future<List<String>> _pump(WidgetTester tester, VTabFolder value) async {
  final calls = <String>[];
  await tester.pumpWidget(EvolveApp(
    theme: ThemeMode.light,
    contentWidget: Align(
      alignment: Alignment.topLeft,
      child: SizedBox(width: 300, height: 120, child: _RecordingTabFolderSwt(value: value, calls: calls)),
    ),
  ));
  await tester.pump();
  while (tester.takeException() != null) {}
  return calls;
}

List<bool> _highlighted(WidgetTester tester) => tester
    .widgetList<TabItemContextProvider>(find.byType(TabItemContextProvider))
    .map((p) => p.context.isSelected)
    .toList();

int? _shownPage(WidgetTester tester) => tester.widget<IndexedStack>(find.byType(IndexedStack)).index;

void main() {
  testWidgets('the selected tab is the only one highlighted and its page is shown', (tester) async {
    await _pump(tester, _folder([_two]));
    expect(_highlighted(tester), [false, true]);
    expect(_shownPage(tester), 1);
  });

  testWidgets('an empty selection highlights no tab and shows no page', (tester) async {
    final calls = await _pump(tester, _folder([]));
    expect(_highlighted(tester), [false, false]);
    expect(_shownPage(tester), isNull);
    expect(calls, isNot(contains('Selection/Selection')),
        reason: 'the folder does not pick a tab for Java when Java holds none');
  });
}
