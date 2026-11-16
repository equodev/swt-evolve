// The calendar of a DateTime -- SWT.CALENDAR and the drop-down of SWT.DATE | SWT.DROP_DOWN --
// names the months and weekdays and starts the week as the user's locale does, as Win32's does.

import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/gen/datetime.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/impl/config_flags.dart';
import 'package:swtflutter/src/impl/widget_config.dart';

const Size _calendarSize = Size(320, 320);
const Size _dropDownSize = Size(200, 24);

VDateTime _date(int style, Size size, {required int year, required int month}) => VDateTime()
  ..swt = 'DateTime'
  ..id = 96
  ..seq = 1
  ..style = style
  ..enabled = true
  ..year = year
  ..month = month
  ..day = 15
  ..bounds = (VRectangle()
    ..x = 0
    ..y = 0
    ..width = size.width.toInt()
    ..height = size.height.toInt());

/// The user's locale: the browser's on the web, the OS's regional settings on desktop.
void _setLocale(WidgetTester tester, Locale locale) {
  tester.platformDispatcher.localeTestValue = locale;
  tester.platformDispatcher.localesTestValue = [locale];
  addTearDown(tester.platformDispatcher.clearAllTestValues);
  _setConfigFlags(ConfigFlags()..regional_locale = locale.toLanguageTag());
}

void _setConfigFlags(ConfigFlags flags) {
  setConfigFlags(flags);
  addTearDown(resetConfigFlags);
}

Future<void> _pumpCalendar(WidgetTester tester, {required int year, required int month}) async {
  await tester.pumpWidget(EvolveApp(
    theme: ThemeMode.light,
    contentWidget: Align(
      alignment: Alignment.topLeft,
      child: SizedBox.fromSize(
        size: _calendarSize,
        child: DateTimeSwt<VDateTime>(
            value: _date(SWT.CALENDAR, _calendarSize, year: year, month: month)),
      ),
    ),
  ));
  await tester.pumpAndSettle();
}

Future<void> _pumpAndOpenDropDown(WidgetTester tester, {required int year, required int month}) async {
  await tester.pumpWidget(EvolveApp(
    theme: ThemeMode.light,
    contentWidget: Align(
      alignment: Alignment.topLeft,
      child: SizedBox.fromSize(
        size: _dropDownSize,
        child: DateTimeSwt<VDateTime>(
            value: _date(SWT.DATE | SWT.DROP_DOWN, _dropDownSize, year: year, month: month)),
      ),
    ),
  ));
  await tester.pumpAndSettle();
  await tester.tap(find.byIcon(Icons.arrow_drop_down).last);
  await tester.pumpAndSettle();
}

/// The calendar shows [header], the weekday labels [weekdays] left to right, and the 1st of the
/// month in the column of [weekdayOfFirst].
void _expectCalendar(WidgetTester tester,
    {required String header, required List<String> weekdays, required String weekdayOfFirst}) {
  expect(find.text(header), findsOneWidget, reason: 'the month header reads "$header"');
  final calendar = find.ancestor(of: find.text(header), matching: find.byType(Column)).first;
  Finder inCalendar(String text) =>
      find.descendant(of: calendar, matching: find.text(text, findRichText: true));

  for (final label in weekdays) {
    expect(inCalendar(label), findsOneWidget, reason: 'the weekday row has "$label"');
  }
  final xs = [for (final label in weekdays) tester.getCenter(inCalendar(label)).dx];
  for (var i = 1; i < xs.length; i++) {
    expect(xs[i], greaterThan(xs[i - 1]),
        reason: '"${weekdays[i]}" comes right of "${weekdays[i - 1]}"');
  }
  expect(tester.getCenter(inCalendar('1')).dx,
      moreOrLessEquals(tester.getCenter(inCalendar(weekdayOfFirst)).dx, epsilon: 0.5),
      reason: 'the 1st falls under "$weekdayOfFirst"');
}

void main() {
  group('SWT.CALENDAR', () {
    testWidgets('a German locale: German names, Monday first', (tester) async {
      _setLocale(tester, const Locale('de', 'DE'));
      await _pumpCalendar(tester, year: 2018, month: 9);

      // 1 October 2018 is a Monday.
      _expectCalendar(tester,
          header: 'Oktober 2018',
          weekdays: const ['Mo', 'Di', 'Mi', 'Do', 'Fr', 'Sa', 'So'],
          weekdayOfFirst: 'Mo');
    });

    testWidgets('an Argentine Spanish locale: Spanish names, Monday first', (tester) async {
      _setLocale(tester, const Locale('es', 'AR'));
      await _pumpCalendar(tester, year: 2026, month: 10);

      // 1 November 2026 is a Sunday.
      _expectCalendar(tester,
          header: 'noviembre de 2026',
          weekdays: const ['lun', 'mar', 'mié', 'jue', 'vie', 'sáb', 'dom'],
          weekdayOfFirst: 'dom');
    });

    testWidgets('a US English locale keeps today\'s calendar', (tester) async {
      _setLocale(tester, const Locale('en', 'US'));
      await _pumpCalendar(tester, year: 2026, month: 10);

      _expectCalendar(tester,
          header: 'November 2026',
          weekdays: const ['Su', 'Mo', 'Tu', 'We', 'Th', 'Fr', 'Sa'],
          weekdayOfFirst: 'Su');
    });
  });

  group('SWT.DROP_DOWN', () {
    testWidgets('a German locale: German names, Monday first', (tester) async {
      _setLocale(tester, const Locale('de', 'DE'));
      await _pumpAndOpenDropDown(tester, year: 2018, month: 9);

      _expectCalendar(tester,
          header: 'Oktober 2018',
          weekdays: const ['Mo', 'Di', 'Mi', 'Do', 'Fr', 'Sa', 'So'],
          weekdayOfFirst: 'Mo');
    });

    testWidgets('an Argentine Spanish locale: Spanish names, Monday first', (tester) async {
      _setLocale(tester, const Locale('es', 'AR'));
      await _pumpAndOpenDropDown(tester, year: 2026, month: 10);

      _expectCalendar(tester,
          header: 'noviembre de 2026',
          weekdays: const ['lun', 'mar', 'mié', 'jue', 'vie', 'sáb', 'dom'],
          weekdayOfFirst: 'dom');
    });

    testWidgets('a US English locale keeps today\'s calendar', (tester) async {
      _setLocale(tester, const Locale('en', 'US'));
      await _pumpAndOpenDropDown(tester, year: 2026, month: 10);

      _expectCalendar(tester,
          header: 'November 2026',
          weekdays: const ['Su', 'Mo', 'Tu', 'We', 'Th', 'Fr', 'Sa'],
          weekdayOfFirst: 'Su');
    });

    testWidgets('on desktop the OS\'s regional locale decides, whatever the JVM\'s and the display language',
        (tester) async {
      // Windows with a Mexican Spanish display language, whose week starts on Sunday, Argentine
      // regional settings, whose week starts on Monday, and an application that runs its JVM in English.
      tester.platformDispatcher.localeTestValue = const Locale('es', 'MX');
      tester.platformDispatcher.localesTestValue = const [Locale('es', 'MX')];
      addTearDown(tester.platformDispatcher.clearAllTestValues);
      _setConfigFlags(ConfigFlags()
        ..locale = 'en-US'
        ..regional_locale = 'es-AR');
      await _pumpAndOpenDropDown(tester, year: 2026, month: 10);

      _expectCalendar(tester,
          header: 'noviembre de 2026',
          weekdays: const ['lun', 'mar', 'mié', 'jue', 'vie', 'sáb', 'dom'],
          weekdayOfFirst: 'dom');
    });

    testWidgets('with no regional format from the OS, the JVM\'s locale decides', (tester) async {
      _setConfigFlags(ConfigFlags()..locale = 'de-DE');
      await _pumpAndOpenDropDown(tester, year: 2018, month: 9);

      _expectCalendar(tester,
          header: 'Oktober 2018',
          weekdays: const ['Mo', 'Di', 'Mi', 'Do', 'Fr', 'Sa', 'So'],
          weekdayOfFirst: 'Mo');
    });
  });
}
