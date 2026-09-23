
import 'package:flutter/foundation.dart';
import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/scrollbar.dart';
import 'package:swtflutter/src/gen/styledtext.dart';
import 'package:swtflutter/src/gen/swt.dart';

const double _width = 300;
const double _height = 200;

final String _text = List.generate(200, (i) => 'line $i').join('\n');

VScrollBar _bar(int id, {required bool visible}) => VScrollBar()
  ..swt = 'ScrollBar'
  ..id = id
  ..style = SWT.V_SCROLL
  ..visible = visible
  ..enabled = true
  ..minimum = 0
  ..maximum = 3015
  ..thumb = 414
  ..selection = 0;

VStyledText _styledText({required bool barVisible}) => VStyledText()
  ..swt = 'StyledText'
  ..id = 1
  ..style = SWT.V_SCROLL
  ..enabled = true
  ..visible = true
  ..editable = false
  ..caretOffset = 0
  ..text = _text
  ..alwaysShowScrollBars = true
  ..verticalBar = _bar(2, visible: barVisible)
  ..bounds = (VRectangle()
    ..x = 0
    ..y = 0
    ..width = _width.toInt()
    ..height = _height.toInt());

final Finder _anyScrollbar =
    find.byWidgetPredicate((w) => w is RawScrollbar, skipOffstage: false);

Future<void> _pump(WidgetTester tester, VStyledText value) async {
  await tester.pumpWidget(EvolveApp(
    theme: ThemeMode.light,
    contentWidget: SizedBox(
      width: _width,
      height: _height,
      child: StyledTextSwt<VStyledText>(value: value),
    ),
  ));
  await tester.pump();
}

Future<void> _onMacOS(Future<void> Function() body) async {
  debugDefaultTargetPlatformOverride = TargetPlatform.macOS;
  try {
    await body();
  } finally {
    debugDefaultTargetPlatformOverride = null;
  }
}

void main() {
  testWidgets('a StyledText whose vertical bar is hidden draws no scroll bar',
      (WidgetTester tester) async {
    await _onMacOS(() async {
      await _pump(tester, _styledText(barVisible: false));

      expect(_anyScrollbar, findsNothing,
          reason: 'the application hid the bar and scrolls the widget itself; the style only says '
              'the bar exists. Covers the ScrollBehavior one too, which hover brings back.');
    });
  });

  testWidgets('a StyledText whose vertical bar is visible still draws one',
      (WidgetTester tester) async {
    await _onMacOS(() async {
      await _pump(tester, _styledText(barVisible: true));

      expect(_anyScrollbar, findsOneWidget,
          reason: 'one bar, not the widget\'s own stacked on the behaviour\'s');
    });
  });

  testWidgets('a bar whose visibility never arrived is drawn, as SWT defaults to visible',
      (WidgetTester tester) async {
    await _onMacOS(() async {
      await _pump(tester, _styledText(barVisible: true)..verticalBar!.visible = null);

      expect(_anyScrollbar, findsOneWidget,
          reason: 'absent is not hidden: an older peer that never sends the flag '
              'must keep the bar it always had');
    });
  });
}
