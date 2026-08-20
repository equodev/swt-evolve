// ShellImpl.buildComposite() paints state.background as an opaque backdrop whenever it is
// non-null -- but a Shell's background is very often just the SWT default widget-background gray
// (e.g. JFace's COLOR_WIDGET_BACKGROUND on dialog Shells), set incidentally rather than for a
// visual effect. Composite already gates its own background color on the use_swt_colors flag
// (getCompositeBackgroundColor -> getBackgroundColor); Shell's backdrop must follow the same rule.
// backgroundImage has no such "incidental default" case (nothing sets it by accident), so it must
// keep painting unconditionally regardless of the flag.

import 'dart:convert';

import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/gen/color.dart';
import 'package:swtflutter/src/gen/composite.dart';
import 'package:swtflutter/src/gen/display.dart';
import 'package:swtflutter/src/gen/image.dart';
import 'package:swtflutter/src/gen/imagedata.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/shell.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/impl/config_flags.dart';
import 'package:swtflutter/src/impl/display_evolve.dart';
import 'package:swtflutter/src/impl/widget_config.dart';

// A 1x1 transparent PNG -- just enough for MemoryImage to decode successfully.
final _tinyPngBytes = base64Decode(
    'iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mNk+A8AAQUBAScY'
    '42YAAAAASUVORK5CYII=');

const _swtDefaultWidgetGray = Color(0xFFF0F0F0);

VRectangle _rect(int x, int y, int w, int h) => VRectangle()
  ..x = x
  ..y = y
  ..width = w
  ..height = h;

VColor _vColor(int r, int g, int b) => VColor()
  ..alpha = 0xFF
  ..red = r
  ..green = g
  ..blue = b;

// A childless Shell/Composite/Canvas never reaches ShellImpl.buildComposite() at all --
// CanvasImpl.build() takes an early, different painting path when it has no children (see
// canvas_evolve.dart). A dialog Shell always hosts content (buttons, labels, ...), so give the
// test Shell one too, or the assertions below would pass/fail for the wrong reason.
VShell _shellValue({VColor? background, VImage? backgroundImage}) => VShell()
  ..id = 1
  ..style = SWT.SHELL_TRIM
  ..text = 'shell'
  ..bounds = _rect(0, 0, 800, 600)
  ..background = background
  ..backgroundImage = backgroundImage
  ..children = [
    VComposite()
      ..id = 2
      ..style = 0
      ..bounds = _rect(0, 0, 100, 100)
  ];

bool _hasColoredBackdrop(WidgetTester tester, Color color) => tester
    .widgetList<DecoratedBox>(find.byType(DecoratedBox))
    .any((w) => (w.decoration as BoxDecoration?)?.color == color);

bool _hasImageBackdrop(WidgetTester tester) => tester
    .widgetList<DecoratedBox>(find.byType(DecoratedBox))
    .any((w) => (w.decoration as BoxDecoration?)?.image != null);

void main() {
  setUp(resetConfigFlags);
  tearDown(resetConfigFlags);

  testWidgets(
      'a Shell background color set to the SWT default widget gray is not painted as a backdrop by default',
      (tester) async {
    await tester.pumpWidget(EvolveApp(
      theme: ThemeMode.light,
      contentWidget: DisplaySwt(
          value: VDisplay()
            ..shells = [_shellValue(background: _vColor(0xF0, 0xF0, 0xF0))]),
    ));
    await tester.pumpAndSettle();

    expect(_hasColoredBackdrop(tester, _swtDefaultWidgetGray), isFalse,
        reason: 'an incidental SWT background color must not paint an opaque backdrop '
            'unless use_swt_colors opts in -- matches how Composite treats its own background');
  });

  testWidgets('use_swt_colors opts a Shell into painting its own background color',
      (tester) async {
    setConfigFlags(ConfigFlags()..use_swt_colors = true);

    await tester.pumpWidget(EvolveApp(
      theme: ThemeMode.light,
      contentWidget: DisplaySwt(
          value: VDisplay()
            ..shells = [_shellValue(background: _vColor(0xF0, 0xF0, 0xF0))]),
    ));
    await tester.pumpAndSettle();

    expect(_hasColoredBackdrop(tester, _swtDefaultWidgetGray), isTrue,
        reason: 'with use_swt_colors on, a Shell should honor its explicit background color');
  });

  testWidgets(
      'a Shell backgroundImage still paints even though its background color is the SWT default gray',
      (tester) async {
    // Mirrors LoginShell.java: setBackground(240,240,240) + setBackgroundImage(...), where the
    // background color is incidental but the image is the deliberate visual effect.
    final image = VImage()..imageData = (VImageData()..data = _tinyPngBytes);

    await tester.pumpWidget(EvolveApp(
      theme: ThemeMode.light,
      contentWidget: DisplaySwt(
          value: VDisplay()
            ..shells = [
              _shellValue(background: _vColor(0xF0, 0xF0, 0xF0), backgroundImage: image)
            ]),
    ));
    await tester.pumpAndSettle();

    expect(_hasImageBackdrop(tester), isTrue,
        reason: 'backgroundImage has no incidental-default case, so it must keep painting '
            'unconditionally -- this is the case LoginShell relies on');
  });
}
