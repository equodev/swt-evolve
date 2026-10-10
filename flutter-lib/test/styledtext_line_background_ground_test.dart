// A line background is a shade of the background the application gave the editor, as a C editor shades
// inactive code. Without use_swt_colors the theme's ground replaces that background, and the line's
// own colour, painted as is, stands out as a band of another colour across the editor.

import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/gen/color.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/styledtext.dart';
import 'package:swtflutter/src/gen/styledtextrenderer.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/impl/styledtext_evolve.dart';
import 'package:swtflutter/src/impl/config_flags.dart';
import 'package:swtflutter/src/impl/widget_config.dart';

VColor _rgb(int r, int g, int b) => VColor()
  ..alpha = 0xFF
  ..red = r
  ..green = g
  ..blue = b;

/// An editor in a dark scheme of its own, line 1 shaded a little lighter than its background.
VStyledText _editor({VColor? background}) => VStyledText()
  ..swt = 'StyledText'
  ..id = 1
  ..style = SWT.MULTI
  ..enabled = true
  ..visible = true
  ..editable = true
  ..caretOffset = 0
  ..text = 'one\ntwo\nthree'
  ..background = background
  ..renderer = (VStyledTextRenderer()
    ..lines = [null, VLineInfo()..background = _rgb(64, 68, 78), null])
  ..bounds = (VRectangle()
    ..x = 0
    ..y = 0
    ..width = 300
    ..height = 100);

Future<StyledTextImpl> _pump(WidgetTester tester, VStyledText value, ThemeMode theme) async {
  await tester.pumpWidget(EvolveApp(
    theme: theme,
    contentWidget: SizedBox(
      width: 300,
      height: 100,
      child: StyledTextSwt<VStyledText>(value: value),
    ),
  ));
  await tester.pump();
  return tester.state<StyledTextImpl>(find.byType(StyledTextSwt<VStyledText>));
}

Color _lineBackground(StyledTextImpl impl) {
  final facts = impl.debugRenderFacts();
  final backgrounds = facts['lineBackgrounds'] as List;
  expect(backgrounds, hasLength(1));
  final hex = (backgrounds.single as Map)['color'] as String;
  return Color(int.parse(hex.replaceFirst('#', ''), radix: 16) | 0xFF000000);
}

int _channel(double v) => (v * 255).round();

void main() {
  setUp(resetConfigFlags);
  tearDown(resetConfigFlags);

  for (final theme in [ThemeMode.light, ThemeMode.dark]) {
    testWidgets('a line keeps the shade it was of the background the theme replaced ($theme)',
        (tester) async {
      final impl = await _pump(tester, _editor(background: _rgb(40, 44, 52)), theme);
      final ground = impl.bg;
      expect(ground, isNot(const Color(0xFF282C34)), reason: 'sanity: the theme paints its own ground');

      final line = _lineBackground(impl);
      // 24, 24 and 26 above the application's background, as far as the channel allows.
      expect(_channel(line.r), (_channel(ground.r) + 24).clamp(0, 255));
      expect(_channel(line.g), (_channel(ground.g) + 24).clamp(0, 255));
      expect(_channel(line.b), (_channel(ground.b) + 26).clamp(0, 255));
    });
  }

  testWidgets('with no background of its own, the line is painted in its colour', (tester) async {
    final impl = await _pump(tester, _editor(), ThemeMode.dark);

    expect(_lineBackground(impl), const Color(0xFF40444E));
  });

  testWidgets('with the application\'s background painted, so is the line\'s', (tester) async {
    setConfigFlags(ConfigFlags()..use_swt_colors = true);
    final impl = await _pump(tester, _editor(background: _rgb(40, 44, 52)), ThemeMode.dark);

    expect(impl.bg, const Color(0xFF282C34));
    expect(_lineBackground(impl), const Color(0xFF40444E));
  });
}
