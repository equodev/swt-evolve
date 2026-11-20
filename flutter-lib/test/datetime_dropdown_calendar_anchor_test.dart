// The drop-down button and the open calendar of a DateTime(SWT.DATE | SWT.DROP_DOWN) are tagged
// on their own, so a test reaches them without coordinates.

import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/gen/datetime.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/impl/utils/test_anchors.dart';

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
    ..width = 120
    ..height = 24);

Offset _centreOf(String identifier) {
  final at = TestAnchors.locate(identifier)!;
  return Rect.fromLTWH(at['x'] as double, at['y'] as double, at['width'] as double, at['height'] as double).center;
}

void main() {
  setUpAll(() => TestAnchors.enabled = true);
  tearDownAll(() => TestAnchors.enabled = false);

  testWidgets('the drop-down button and the open calendar are found by identifier', (tester) async {
    await tester.pumpWidget(EvolveApp(
      theme: ThemeMode.light,
      contentWidget: Align(
        alignment: Alignment.topLeft,
        child: SizedBox(
          width: 200,
          height: 24,
          child: DateTimeSwt<VDateTime>(value: _dropDownDate()),
        ),
      ),
    ));
    await tester.pumpAndSettle();

    const button = 'dropdown:DateTime/95';
    const calendar = 'calendar:DateTime/95';
    expect(TestAnchors.locate(button), isNotNull);
    expect(TestAnchors.locate(calendar), isNull, reason: 'the calendar starts closed');

    await tester.tapAt(_centreOf(button));
    await tester.pumpAndSettle();
    expect(TestAnchors.locate(calendar), isNotNull, reason: 'the button opens the calendar');
    expect(TestAnchors.texts('January 2026', exact: true, within: 'calendar:DateTime/'), hasLength(1),
        reason: 'the identifier sits on the calendar itself');

    await tester.tapAt(_centreOf(button));
    await tester.pumpAndSettle();
    expect(TestAnchors.locate(calendar), isNull, reason: 'the button closes the calendar again');
  });
}
