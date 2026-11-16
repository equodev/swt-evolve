// The calendar of a DateTime(SWT.DATE | SWT.DROP_DOWN) stays fully visible, as Win32's does: it
// opens below the field when it fits there and above it when it does not. When it fits on
// neither side it keeps its size, on the side with more room, moved inside the window.

import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/gen/datetime.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/swt.dart';

const Size _surface = Size(800, 600);
const double _fieldWidth = 200;
const double _fieldHeight = 24;

VDateTime _dropDownDate() => VDateTime()
  ..swt = 'DateTime'
  ..id = 95
  ..seq = 1
  ..style = SWT.DATE | SWT.DROP_DOWN
  ..enabled = true
  ..year = 2026
  ..month = 0
  ..day = 15
  ..bounds = (VRectangle()
    ..x = 0
    ..y = 0
    ..width = _fieldWidth.toInt()
    ..height = _fieldHeight.toInt());

/// Opens the calendar of a field placed at [alignment] and returns the rects of the field and
/// of the calendar.
Future<({Rect field, Rect calendar})> _open(WidgetTester tester, Alignment alignment,
    {Size surface = _surface}) async {
  tester.view.physicalSize = surface;
  tester.view.devicePixelRatio = 1;
  addTearDown(tester.view.reset);
  await tester.pumpWidget(const SizedBox());
  await tester.pumpWidget(EvolveApp(
    theme: ThemeMode.light,
    contentWidget: Align(
      alignment: alignment,
      child: SizedBox(
        width: _fieldWidth,
        height: _fieldHeight,
        child: DateTimeSwt<VDateTime>(value: _dropDownDate()),
      ),
    ),
  ));
  await tester.pumpAndSettle();

  await tester.tap(find.byIcon(Icons.arrow_drop_down).last);
  await tester.pumpAndSettle();
  final header = find.text('January 2026');
  expect(header, findsOneWidget, reason: 'sanity: the calendar is open');

  final calendar = find.ancestor(of: header, matching: find.byType(Material)).first;
  return (
    field: tester.getRect(find.byType(DateTimeSwt<VDateTime>)),
    calendar: tester.getRect(calendar),
  );
}

bool _inside(Rect outer, Rect inner) =>
    inner.left >= outer.left &&
    inner.top >= outer.top &&
    inner.right <= outer.right &&
    inner.bottom <= outer.bottom;

void main() {
  testWidgets('with room below, the calendar opens below the field', (tester) async {
    final rects = await _open(tester, Alignment.topLeft);

    expect(rects.calendar.top, rects.field.bottom);
    expect(rects.calendar.left, rects.field.left);
  });

  testWidgets('at the bottom of the window, the calendar opens above the field', (tester) async {
    final rects = await _open(tester, Alignment.bottomLeft);

    expect(_inside(Offset.zero & _surface, rects.calendar), isTrue,
        reason: 'the calendar ${rects.calendar} is fully visible');
    expect(rects.calendar.bottom, rects.field.top, reason: 'it sits right above the field');
    expect(rects.calendar.left, rects.field.left);
  });

  testWidgets('at the right edge of the window, the calendar stays inside it', (tester) async {
    final rects = await _open(tester, Alignment.topRight);

    expect(_inside(Offset.zero & _surface, rects.calendar), isTrue,
        reason: 'the calendar ${rects.calendar} is fully visible');
    expect(rects.calendar.top, rects.field.bottom);
  });

  testWidgets('near the top of a short window, the calendar opens below the field', (tester) async {
    final rects = await _open(tester, const Alignment(-1, -0.9), surface: const Size(800, 300));

    expect(_inside(const Offset(0, 0) & const Size(800, 300), rects.calendar), isTrue,
        reason: 'the calendar ${rects.calendar} is fully visible');
    expect(rects.calendar.top, rects.field.bottom, reason: 'it sits right below the field');
  });

  for (final (name, alignment, opensAbove) in [
    ('more room above', const Alignment(-1, 0.3), true),
    ('more room below', const Alignment(-1, -0.3), false),
  ]) {
    testWidgets('fitting on neither side, with $name, the calendar keeps its size inside the window',
        (tester) async {
      const window = Size(800, 300);
      final roomy = await _open(tester, Alignment.topLeft);
      final rects = await _open(tester, alignment, surface: window);

      expect(rects.calendar.size, roomy.calendar.size, reason: 'it is not cut to the room it has');
      expect(_inside(Offset.zero & window, rects.calendar), isTrue,
          reason: 'the calendar ${rects.calendar} is fully visible');
      if (opensAbove) {
        expect(rects.calendar.top, 0, reason: 'it opens upwards and stops at the top edge');
      } else {
        expect(rects.calendar.bottom, window.height,
            reason: 'it opens downwards and stops at the bottom edge');
      }
    });
  }

  testWidgets('the calendar is as wide as its month, whatever the window\'s width', (tester) async {
    final narrow = await _open(tester, Alignment.topLeft, surface: const Size(800, 600));
    final wide = await _open(tester, Alignment.topLeft, surface: const Size(1600, 600));

    expect(wide.calendar.width, narrow.calendar.width);
    expect(narrow.calendar.width, lessThan(_surface.width / 2));
  });
}
