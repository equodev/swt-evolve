// A range whose GlyphMetrics are 0 wide takes no horizontal space, as on SWT: a console hides
// ANSI escape sequences that way and colours only the text between them.

import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/gen/color.dart';
import 'package:swtflutter/src/gen/glyphmetrics.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/styledtext.dart';
import 'package:swtflutter/src/gen/stylerange.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/impl/styledtext_evolve.dart';

const _esc = '\u001b';
const _open = '$_esc[32m';
const _close = '$_esc[0;39m';
const _line1 = '${_open}INFO$_close started';
const _line2 = 'INFO started';

final _hidden = VStyleRange()
  ..metrics = (VGlyphMetrics()
    ..ascent = 0
    ..descent = 0
    ..width = 0);

final _green = VStyleRange()
  ..foreground = (VColor()
    ..alpha = 255
    ..red = 0
    ..green = 128
    ..blue = 0);

VStyledText _value() => VStyledText()
  ..swt = 'StyledText'
  ..id = 1
  ..style = SWT.H_SCROLL | SWT.V_SCROLL
  ..enabled = true
  ..editable = true
  ..caretOffset = 0
  ..text = '$_line1\n$_line2'
  ..styles = [_hidden, _green]
  ..styleIndex = [
    1,
    0, 0, _open.length,
    1, _open.length, 4,
    0, _open.length + 4, _close.length,
  ]
  ..bounds = (VRectangle()
    ..x = 0
    ..y = 0
    ..width = 500
    ..height = 200);

List<double> _charX(Map<String, dynamic> row) {
  if (row['cx'] != null) return (row['cx'] as List).cast<num>().map((v) => v.toDouble()).toList();
  final cxu = (row['cxu'] as List).cast<num>();
  final n = (row['e'] as int) - (row['s'] as int);
  return [for (var i = 0; i <= n; i++) cxu[0] + i * cxu[1].toDouble()];
}

void main() {
  setUp(() => StyledTextImpl.debugLastGeometry = null);

  testWidgets('characters under zero-width metrics take no horizontal space', (tester) async {
    final key = GlobalKey<StyledTextImpl>();
    await tester.pumpWidget(EvolveApp(
      theme: ThemeMode.light,
      contentWidget: SizedBox(
        width: 500,
        height: 200,
        child: StyledTextSwt<VStyledText>(key: key, value: _value()),
      ),
    ));
    await tester.pump(Duration.zero);

    final rows = (StyledTextImpl.debugLastGeometry!['lines'] as List).cast<Map<String, dynamic>>();
    final x1 = _charX(rows[0]);
    final x2 = _charX(rows[1]);
    for (var i = 0; i <= _open.length; i++) {
      expect(x1[i], closeTo(0, 0.5), reason: 'offset $i inside the opening escape is at the left edge');
    }
    for (var i = 0; i <= 4; i++) {
      expect(x1[_open.length + i], closeTo(x2[i], 0.5), reason: 'offset $i of "INFO" matches the plain line');
    }
    final infoEnd = x1[_open.length + 4];
    for (var i = 1; i <= _close.length; i++) {
      expect(x1[_open.length + 4 + i], closeTo(infoEnd, 0.5),
          reason: 'offset $i inside the closing escape stays at the end of "INFO"');
    }
    expect(x1[_line1.length], closeTo(x2[_line2.length], 0.5),
        reason: 'the line ends where the same text without escapes does');
  });
}
