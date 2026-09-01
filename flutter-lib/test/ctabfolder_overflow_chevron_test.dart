// Java marks every CTabItem showing=true in web mode (it can't measure the Flutter
// viewport), so the overflow chevron must be driven from the live layout: it appears
// whenever the tab row overflows, regardless of that flag.

import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/gen/ctabfolder.dart';
import 'package:swtflutter/src/gen/ctabitem.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/impl/config_flags.dart';
// ignore: unused_import
import 'package:swtflutter/src/impl/ctabfolder_evolve.dart';
import 'package:swtflutter/src/impl/widget_config.dart';

VCTabFolder _folder(int tabCount) => VCTabFolder()
  ..id = 1000
  ..style = SWT.NONE
  ..selection = 0
  ..items = List.generate(
    tabCount,
    (i) => VCTabItem()
      ..id = 10 + i
      ..text = 'A rather long view name ${i + 1}'
      // Java marks every item showing=true in web mode: it cannot know which fit.
      ..showing = true,
  );

Widget _host(VCTabFolder value, double width) => EvolveApp(
      theme: ThemeMode.light,
      contentWidget: SizedBox(
        width: width,
        height: 200,
        child: CTabFolderSwt<VCTabFolder>(value: value),
      ),
    );

void _drain(WidgetTester tester) {
  while (tester.takeException() != null) {}
}

Future<void> _settle(WidgetTester tester) async {
  // Overflow is detected via a ScrollMetricsNotification -> post-frame setState, so a couple
  // of pumps are needed for it to land.
  await tester.pump();
  await tester.pump();
  await tester.pump();
  _drain(tester);
}

void main() {
  setUp(resetConfigFlags);
  tearDown(resetConfigFlags);

  testWidgets('overflowing tabs surface the overflow chevron',
      (WidgetTester tester) async {
    await tester.pumpWidget(_host(_folder(15), 300));
    _drain(tester);
    await _settle(tester);

    // Even though Java marked every item showing=true, the chevron appears because the
    // row overflows its 300px width -- so the off-view tabs are reachable through it.
    expect(find.byIcon(Icons.expand_more), findsOneWidget);
  });

  testWidgets('a tab row that fits shows no overflow chevron (guard)',
      (WidgetTester tester) async {
    await tester.pumpWidget(_host(_folder(2), 1200));
    _drain(tester);
    await _settle(tester);

    // Two short tabs fit comfortably in 1200px: no overflow, no chevron.
    expect(find.byIcon(Icons.expand_more), findsNothing);
  });

  testWidgets('chevronVisible=false suppresses the chevron even when overflowing',
      (WidgetTester tester) async {
    final folder = _folder(15)..chevronVisible = false;
    await tester.pumpWidget(_host(folder, 300));
    _drain(tester);
    await _settle(tester);

    // The row still overflows, but the folder's chevronVisible flag forbids the chevron --
    // so it must stay hidden (matching native SWT's showChevron = chevronVisible && overflow).
    expect(find.byIcon(Icons.expand_more), findsNothing);
  });
}
