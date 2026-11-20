// A DateTime(SWT.DATE | SWT.DROP_DOWN) field is edited by its calendar button, as on Win32 and Cocoa:
// it has no up/down buttons, and its date follows the locale's short-date order and separator.

import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/gen/datetime.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/impl/config_flags.dart';
import 'package:swtflutter/src/impl/widget_config.dart';
import 'package:swtflutter/src/impl/utils/test_anchors.dart';

const double _width = 110;
const double _height = 22;

VDateTime _date(int style, Size size) => VDateTime()
  ..swt = 'DateTime'
  ..id = 94
  ..seq = 1
  ..style = style
  ..enabled = true
  ..year = 2026
  ..month = 9
  ..day = 24
  ..bounds = (VRectangle()
    ..x = 0
    ..y = 0
    ..width = size.width.toInt()
    ..height = size.height.toInt());

Future<void> _pump(WidgetTester tester, int style, {Size size = const Size(_width, _height)}) async {
  await tester.pumpWidget(EvolveApp(
    theme: ThemeMode.light,
    contentWidget: Align(
      alignment: Alignment.topLeft,
      child: SizedBox(
        width: size.width,
        height: size.height,
        child: DateTimeSwt<VDateTime>(value: _date(style, size)),
      ),
    ),
  ));
  await tester.pumpAndSettle();
}

const int _dropDown = SWT.DATE | SWT.MEDIUM | SWT.DROP_DOWN;

void main() {
  // Widgets are located as a test driver finds them, through the anchor registry.
  setUpAll(() => TestAnchors.enabled = true);
  tearDownAll(() => TestAnchors.enabled = false);

  testWidgets('a drop-down date has no up/down buttons', (tester) async {
    await _pump(tester, _dropDown);

    // The calendar button's arrow is the only one: up/down buttons would add a second down arrow.
    expect(find.byIcon(Icons.arrow_drop_down), findsOneWidget);
  });

  testWidgets('a date without DROP_DOWN keeps its up/down buttons', (tester) async {
    await _pump(tester, SWT.DATE | SWT.MEDIUM);

    expect(find.byIcon(Icons.arrow_drop_up), findsOneWidget);
  });

  testWidgets('a click 15 px from the right edge opens the calendar and leaves the date alone',
      (tester) async {
    await _pump(tester, _dropDown);

    await tester.tapAt(const Offset(_width - 15, _height / 2));
    await tester.pumpAndSettle();

    // The calendar has a day 10 too, but it is drawn in an overlay, outside the field.
    expect(find.descendant(of: find.byType(DateTimeSwt<VDateTime>), matching: find.text('10')),
        findsOneWidget,
        reason: 'the month was not stepped');
    expect(find.text('October 2026'), findsOneWidget, reason: 'the calendar is open on the date');
  });

  Rect drawnBox(WidgetTester tester) => tester.getRect(find
      .descendant(
          of: find.byType(DateTimeSwt<VDateTime>),
          matching: find.byWidgetPredicate((w) => w is Container && w.decoration is BoxDecoration))
      .first);

  for (final (name, style) in [
    ('drop-down date', _dropDown),
    ('date', SWT.DATE | SWT.MEDIUM),
    ('time', SWT.TIME | SWT.MEDIUM),
  ]) {
    testWidgets('a $name fills the bounds it is given, as Win32 draws it', (tester) async {
      await _pump(tester, style);
      expect(drawnBox(tester), const Rect.fromLTWH(0, 0, _width, _height));

      await _pump(tester, style, size: const Size(200, 40));
      expect(drawnBox(tester), const Rect.fromLTWH(0, 0, 200, 40));
    });
  }

  testWidgets('the calendar button is two scroll bars wide and as tall as the field, as Win32 draws it',
      (tester) async {
    await _pump(tester, _dropDown);

    final at = TestAnchors.locate('dropdown:DateTime/94', hit: false)!;
    final button = Rect.fromLTWH(at['x'] as double, at['y'] as double, at['width'] as double, at['height'] as double);
    expect(button.width, 2 * 17, reason: 'two Win32 scroll bars (SM_CXVSCROLL) wide');
    expect(button.right, closeTo(_width, 1), reason: 'it ends the field, inside the border');
    expect(button.height, closeTo(_height, 2), reason: 'it fills the field\'s height, inside the border');
  });

  testWidgets('a German locale shows the date as day.month.year', (tester) async {
    tester.platformDispatcher.localeTestValue = const Locale('de', 'DE');
    tester.platformDispatcher.localesTestValue = const [Locale('de', 'DE')];
    addTearDown(tester.platformDispatcher.clearAllTestValues);
    await _pump(tester, _dropDown);

    expect(find.text('/'), findsNothing);
    final texts = tester.widgetList<Text>(find.byType(Text)).map((t) => t.data).whereType<String>();
    expect(texts.join(), contains('24.10.2026'));
  });

  testWidgets('a US English locale shows the date as month/day/year', (tester) async {
    tester.platformDispatcher.localeTestValue = const Locale('en', 'US');
    addTearDown(tester.platformDispatcher.clearAllTestValues);
    await _pump(tester, _dropDown);

    final texts = tester.widgetList<Text>(find.byType(Text)).map((t) => t.data).whereType<String>();
    expect(texts.join(), contains('10/24/2026'));
  });

  testWidgets('on desktop the locale of the JVM decides the order, whatever the platform says', (tester) async {
    tester.platformDispatcher.localeTestValue = const Locale('en', 'US');
    addTearDown(tester.platformDispatcher.clearAllTestValues);
    setConfigFlags(ConfigFlags()..locale = 'de-DE');
    addTearDown(resetConfigFlags);
    await _pump(tester, _dropDown);

    final texts = tester.widgetList<Text>(find.byType(Text)).map((t) => t.data).whereType<String>();
    expect(texts.join(), contains('24.10.2026'));
  });
}
