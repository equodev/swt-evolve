// The chevron's tab list is a Material menu, placed in the overlay the app's zoom Transform sits
// above. Its anchor has to be measured in that same space, or a zoomed app opens the list as far
// from the chevron again as the zoom magnifies.

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

VCTabFolder _folder() => VCTabFolder()
  ..id = 1000
  ..style = SWT.NONE
  ..selection = 0
  ..items = List.generate(
    6,
    (i) => VCTabItem()
      ..id = 10 + i
      ..text = 'A rather long view name ${i + 1}'
      ..showing = true,
  );

Widget _host() => EvolveApp(
      theme: ThemeMode.light,
      contentWidget: Padding(
        padding: const EdgeInsets.only(left: 20, top: 60),
        child: SizedBox(
          width: 300,
          height: 200,
          child: CTabFolderSwt<VCTabFolder>(value: _folder()),
        ),
      ),
    );

void _drain(WidgetTester tester) {
  while (tester.takeException() != null) {}
}

/// Opens the chevron's list at [scale] and answers how far, on screen, the list's top-left sits
/// from the chevron's bottom-left, in units of the unzoomed app.
Future<Offset> _listGapAt(WidgetTester tester, double scale) async {
  appScaleNotifier.value = scale;
  await tester.pumpWidget(_host());
  _drain(tester);
  for (var i = 0; i < 3; i++) {
    await tester.pump();
  }
  _drain(tester);

  final chevron = find.byIcon(Icons.expand_more);
  expect(chevron, findsOneWidget);
  await tester.tap(chevron);
  await tester.pumpAndSettle();
  _drain(tester);

  final menu = find.byType(PopupMenuItem<int>).first;
  final gap = tester.getTopLeft(menu) - tester.getBottomLeft(chevron);
  await tester.tapAt(Offset.zero);
  await tester.pumpAndSettle();
  return gap / scale;
}

void main() {
  setUp(resetConfigFlags);
  tearDown(() {
    resetConfigFlags();
    appScaleNotifier.value = 1.0;
  });

  testWidgets('a zoomed app opens the chevron list where the unzoomed one does',
      (WidgetTester tester) async {
    // Room below the chevron at both scales, so the menu is never pushed back on screen.
    tester.view.physicalSize = const Size(1600, 2400);
    tester.view.devicePixelRatio = 1.0;
    addTearDown(tester.view.reset);

    final plain = await _listGapAt(tester, 1.0);
    final zoomed = await _listGapAt(tester, 2.0);

    expect(zoomed.dx, closeTo(plain.dx, 1));
    expect(zoomed.dy, closeTo(plain.dy, 1));
  });
}
