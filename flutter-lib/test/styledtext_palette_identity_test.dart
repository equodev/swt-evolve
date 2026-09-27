// A highlighter resends the palette on most keystrokes; equal entries decoded afresh must not make
// every painted line look new.

import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/gen/color.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/styledtext.dart';
import 'package:swtflutter/src/gen/stylerange.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/impl/styledtext_evolve.dart';

import 'delivery/support/deliver.dart';

const _lines = 12;
final _text = List.generate(_lines, (i) => 'line $i: some text').join('\n');

VStyleRange _entry(int r, int g, int b) => VStyleRange()
  ..foreground = (VColor()
    ..alpha = 255
    ..red = r
    ..green = g
    ..blue = b);

/// One run per line, each drawn in entry 0 or 1.
List<int> _index(int paletteName) {
  final out = <int>[paletteName];
  var start = 0;
  for (var i = 0; i < _lines; i++) {
    out..add(i % 2)..add(start)..add(4);
    start += 'line $i: some text'.length + 1;
  }
  return out;
}

VStyledText _value(List<VStyleRange?> styles, int paletteName, {String? text}) => VStyledText()
  ..swt = 'StyledText'
  ..id = 1
  ..style = SWT.H_SCROLL | SWT.V_SCROLL
  ..enabled = true
  ..editable = true
  ..caretOffset = 0
  ..text = text ?? _text
  ..styles = styles
  ..styleIndex = _index(paletteName)
  ..bounds = (VRectangle()
    ..x = 0
    ..y = 0
    ..width = 400
    ..height = 400);

void main() {
  Widget app(GlobalKey<StyledTextImpl> key, VStyledText v) => EvolveApp(
        theme: ThemeMode.light,
        contentWidget: SizedBox(
          width: 400,
          height: 400,
          child: StyledTextSwt<VStyledText>(key: key, value: v),
        ),
      );

  testWidgets('a keystroke that brings a new palette re-lays out only the line it edited',
      (tester) async {
    final key = GlobalKey<StyledTextImpl>();
    final red = _entry(255, 0, 0), blue = _entry(0, 0, 255);
    await tester.pumpWidget(app(key, _value([red, blue], 1)));
    await tester.pumpWidget(app(key, _value([red, blue], 1)));
    await tester.pump(Duration.zero);
    expect(TextShape.debugPaintersLastDraw, greaterThan(0), reason: 'the first paint lays out');

    // Equal entries as new objects, plus one no run uses yet.
    TextShape.debugPainterLayouts = 0;
    await deliverWhole(_value([_entry(255, 0, 0), _entry(0, 0, 255), _entry(0, 128, 0)], 2,
        text: '${_text}x')
      ..seq = 2);
    await tester.pump(Duration.zero);

    expect(TextShape.debugPainterLayouts, 1,
        reason: 'every other line is drawn in styles it was already laid out in');
  });
}
