// An application that picked light colours (black text, a pale current-line band) while the theme
// paints dark: whatever ground each line ends up on, its text has to read on it.

import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/gen/color.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/styledtext.dart';
import 'package:swtflutter/src/gen/styledtextrenderer.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/impl/styledtext_evolve.dart';
import 'package:swtflutter/src/impl/utils/widget_utils.dart';

VColor _rgb(int r, int g, int b) => VColor()
  ..alpha = 255
  ..red = r
  ..green = g
  ..blue = b;

/// What Eclipse's light theme sends for a text editor whose second line holds the caret.
VStyledText _lightEditor() => VStyledText()
  ..swt = 'StyledText'
  ..id = 1
  ..background = _rgb(255, 255, 255)
  ..foreground = _rgb(0, 0, 0)
  ..renderer = (VStyledTextRenderer()
    ..ascent = 10
    ..descent = 4
    ..lines = [null, VLineInfo()..background = _rgb(232, 242, 254)])
  ..style = SWT.MULTI | SWT.V_SCROLL
  ..enabled = true
  ..editable = true
  ..caretOffset = 42
  ..text = 'first line of text\nsecond line, caret here'
  ..tabs = 4
  ..bounds = (VRectangle()
    ..width = 400
    ..height = 200);

Color _hex(String argb) => Color(int.parse(argb.substring(1), radix: 16));

void main() {
  // A Canvas hosting the editor (a SourceViewer's) publishes SwtColorScope.
  testWidgets('every line of a light-coloured editor reads on a dark theme', (tester) async {
    final key = GlobalKey<StyledTextImpl>();
    await tester.pumpWidget(EvolveApp(
      theme: ThemeMode.dark,
      contentWidget: SizedBox(
          width: 400,
          height: 200,
          child: SwtColorScope(child: StyledTextSwt<VStyledText>(key: key, value: _lightEditor()))),
    ));
    await tester.pump(Duration.zero);

    final impl = key.currentState!;
    final facts = impl.debugRenderFacts();
    final bands = {
      for (final band in facts['lineBackgrounds'] as List) (band['line'] as num).toInt(): _hex(band['color'] as String)
    };
    final runs = facts['runs'] as List;
    for (var line = 0; line < runs.length; line++) {
      final ground = bands[line] ?? impl.bg;
      for (final run in runs[line] as List) {
        final text = _hex(run['foreground'] as String);
        expect(contrastRatio(text, ground), greaterThanOrEqualTo(kMinTextContrast),
            reason: 'line $line: text $text on $ground');
      }
    }
  });
}
