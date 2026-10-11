// A CLabel paints the gradient set with setBackground(Color[], int[], boolean) the way SWT's
// CLabel.onPaint does: colour i runs into colour i + 1 over the span that ends at percents[i] of
// the width (the height when vertical), and whatever lies past the last percent is the widget's
// background, not the last colour. A tiled background image is painted as well.

import 'dart:convert';

import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/gen/clabel.dart';
import 'package:swtflutter/src/gen/color.dart';
import 'package:swtflutter/src/gen/image.dart';
import 'package:swtflutter/src/gen/imagedata.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/impl/utils/widget_utils.dart' show ParentBackgroundScope;

// chevron_right_16.png, a 16x16 PNG.
const String _pngBase64 =
    'iVBORw0KGgoAAAANSUhEUgAAABAAAAAQCAYAAAAf8/9hAAAAAXNSR0IArs4c6QAAAIlJ'
    'REFUOBFjYBgFtAuBromzNvdNnaNCyAYmXAoYGRi8//z+d7V74qy27kWLuHGpw2nAfyBg'
    'YPjPBqQqGd59vwk0KAKbITgNYGRkBBoAAf8ZGb4x/md8C+Mj0yzIHGQ22AWMjD8YGf63'
    'sTPKduXle/1EloexcRoAVLCFlZWluDAr6R5M8ShNoxAAAAyMK0qpGOZNAAAAAElFTkSu'
    'QmCC';

VColor _rgb(int r, int g, int b) => VColor()
  ..alpha = 255
  ..red = r
  ..green = g
  ..blue = b;

const Color _darkBlue = Color(0xFF000080);
const Color _blue = Color(0xFF0000FF);
const Color _white = Color(0xFFFFFFFF);
const Color _red = Color(0xFFFF0000);
const Color _green = Color(0xFF00FF00);

VCLabel _label({
  List<VColor>? colors,
  List<int>? percents,
  bool? vertical,
  VImage? backgroundImage,
}) =>
    VCLabel()
      ..id = 21
      ..style = SWT.NONE
      ..enabled = true
      ..text = 'Banner'
      ..bounds = (VRectangle()
        ..x = 0
        ..y = 0
        ..width = 200
        ..height = 40)
      ..gradientColors = colors
      ..gradientPercents = percents
      ..gradientVertical = vertical
      ..backgroundImage = backgroundImage;

/// The decoration the label paints behind its text, or null when it paints none. [background] is
/// the colour the label takes from its surroundings, which is what its own background resolves to
/// under the default theme.
Future<BoxDecoration?> _paint(
  WidgetTester tester,
  VCLabel label, {
  Color? background,
}) async {
  Widget content = Align(
    alignment: Alignment.topLeft,
    child: CLabelSwt<VCLabel>(value: label),
  );
  if (background != null) {
    content = ParentBackgroundScope(background: background, child: content);
  }
  await tester.pumpWidget(EvolveApp(theme: ThemeMode.light, contentWidget: content));
  await tester.pumpAndSettle();
  final decorations = tester
      .widgetList<DecoratedBox>(find.descendant(
        of: find.byType(CLabelSwt<VCLabel>),
        matching: find.byType(DecoratedBox),
      ))
      .map((box) => box.decoration)
      .whereType<BoxDecoration>()
      .where((d) => d.gradient != null || d.image != null || d.color != null);
  return decorations.isEmpty ? null : decorations.first;
}

Future<LinearGradient> _gradient(
  WidgetTester tester,
  VCLabel label, {
  Color? background,
}) async {
  final decoration = await _paint(tester, label, background: background);
  expect(decoration?.gradient, isA<LinearGradient>(),
      reason: 'the label paints no gradient');
  return decoration!.gradient! as LinearGradient;
}

void main() {
  testWidgets('a horizontal gradient spreads its colours at the given percents',
      (tester) async {
    // The example in CLabel's own documentation: dark blue to blue to white, white to the end.
    final gradient = await _gradient(
      tester,
      _label(
        colors: [_rgb(0, 0, 128), _rgb(0, 0, 255), _rgb(255, 255, 255), _rgb(255, 255, 255)],
        percents: [25, 50, 100],
        vertical: false,
      ),
    );
    expect(gradient.begin, Alignment.centerLeft);
    expect(gradient.end, Alignment.centerRight);
    expect(gradient.colors, [_darkBlue, _blue, _white, _white]);
    expect(gradient.stops, [0.0, 0.25, 0.5, 1.0]);
  });

  testWidgets('a vertical gradient runs from the top to the bottom', (tester) async {
    final gradient = await _gradient(
      tester,
      _label(
        colors: [_rgb(0, 0, 128), _rgb(255, 255, 255)],
        percents: [100],
        vertical: true,
      ),
    );
    expect(gradient.begin, Alignment.topCenter);
    expect(gradient.end, Alignment.bottomCenter);
    expect(gradient.colors, [_darkBlue, _white]);
    expect(gradient.stops, [0.0, 1.0]);
  });

  testWidgets('the part past the last percent is the widget background, not the last colour',
      (tester) async {
    const background = Color(0xFFFFFF99);
    final gradient = await _gradient(
      tester,
      _label(
        colors: [_rgb(255, 0, 0), _rgb(0, 255, 0)],
        percents: [50],
        vertical: false,
      ),
      background: background,
    );
    // A hard edge at 50%: the green ends there and the background starts and pads to the end.
    expect(gradient.stops, [0.0, 0.5, 0.5]);
    expect(gradient.colors, [_red, _green, background]);
  });

  testWidgets('a gradient with percents out of step still paints the colours it has a percent for',
      (tester) async {
    final gradient = await _gradient(
      tester,
      _label(
        colors: [_rgb(255, 0, 0), _rgb(0, 255, 0), _rgb(0, 0, 255)],
        percents: [40],
        vertical: false,
      ),
    );
    expect(gradient.stops!.length, gradient.colors.length);
    expect(gradient.colors.sublist(0, 2), [_red, _green]);
    expect(gradient.stops!.sublist(0, 2), [0.0, 0.4]);
  });

  testWidgets('a single colour fills the whole label', (tester) async {
    final decoration = await _paint(
      tester,
      _label(colors: [_rgb(255, 0, 0)], percents: [], vertical: false),
    );
    expect(decoration?.gradient, isNull);
    expect(decoration?.color, _red);
  });

  testWidgets('a label without a gradient paints none', (tester) async {
    final decoration = await _paint(tester, _label(), background: _white);
    expect(decoration?.gradient, isNull);
    expect(decoration?.color, _white);
  });

  testWidgets('the gradient is gone once Java sends it as cleared', (tester) async {
    final label = _label(
      colors: [_rgb(255, 0, 0), _rgb(0, 255, 0)],
      percents: [100],
      vertical: true,
    );
    expect((await _paint(tester, label))?.gradient, isNotNull);

    label
      ..gradientColors = null
      ..gradientPercents = null;
    expect((await _paint(tester, label))?.gradient, isNull);
  });

  testWidgets('the gradient is read from the wire form Java writes', (tester) async {
    final label = VCLabel()
      ..id = 22
      ..style = SWT.NONE
      ..text = 'Wire';
    label.readProperty('gradientColors', {
      'gradientColors': [
        {'r': 255, 'g': 0, 'b': 0, 'a': 255},
        0xFF00FF00,
      ],
    });
    label.readProperty('gradientPercents', {'gradientPercents': [100]});
    label.readProperty('gradientVertical', {'gradientVertical': true});

    final gradient = await _gradient(tester, label);
    expect(gradient.colors, [_red, _green]);
    expect(gradient.begin, Alignment.topCenter);
  });

  testWidgets('a background image is tiled from the top left', (tester) async {
    final decoration = await _paint(
      tester,
      _label(
        backgroundImage: VImage()
          ..filename = 'tile.png'
          ..imageData = (VImageData()
            ..width = 16
            ..height = 16
            ..data = base64Decode(_pngBase64)),
      ),
    );
    expect(decoration?.image, isNotNull, reason: 'the label paints no background image');
    expect(decoration!.image!.repeat, ImageRepeat.repeat);
    expect(decoration.image!.alignment, Alignment.topLeft);
    expect(decoration.gradient, isNull);
  });
}
