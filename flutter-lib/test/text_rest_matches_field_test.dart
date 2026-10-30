// A Text nobody is interacting with is drawn without its TextField, which costs an update of its
// text a fraction of what the TextField does. It has to be indistinguishable from the TextField:
// this paints both and compares them pixel for pixel.

import 'dart:typed_data';
import 'dart:ui' as ui;

import 'package:flutter/material.dart';
import 'package:flutter/rendering.dart';
import 'package:flutter/services.dart' show FontLoader, rootBundle;
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/gen/color.dart';
import 'package:swtflutter/src/gen/font.dart';
import 'package:swtflutter/src/gen/fontdata.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/gen/text.dart';
import 'package:swtflutter/src/impl/text_evolve.dart';
import 'package:swtflutter/src/impl/utils/force_live.dart';

Future<void> _loadFonts() async {
  final inter = FontLoader('Inter')..addFont(rootBundle.load('assets/fonts/Inter_18pt-Medium.ttf'));
  await inter.load();
}

const _long = 'A value far too long to fit in the field it is shown in';

VText _text({
  int style = SWT.SINGLE | SWT.BORDER,
  int width = 160,
  int height = 24,
  String text = 'Value',
  bool enabled = true,
  bool editable = true,
  String? message,
  VColor? foreground,
  VFont? font,
  int? textLimit,
}) =>
    VText()
      ..id = 7
      ..style = style
      ..enabled = enabled
      ..editable = editable
      ..text = text
      ..message = message
      ..foreground = foreground
      ..font = font
      ..textLimit = textLimit
      ..bounds = (VRectangle()
        ..x = 0
        ..y = 0
        ..width = width
        ..height = height);

Future<Uint8List> _paint(WidgetTester tester, VText value, ThemeMode theme, {required bool live}) async {
  final key = GlobalKey();
  await tester.pumpWidget(ForceLive.when(live, EvolveApp(
    theme: theme,
    contentWidget: Align(
      alignment: Alignment.topLeft,
      child: RepaintBoundary(key: key, child: TextSwt<VText>(value: value)),
    ),
  )));
  await tester.pumpAndSettle();
  expect(find.byType(EditableText), live ? findsOneWidget : findsNothing);
  final boundary = key.currentContext!.findRenderObject()! as RenderRepaintBoundary;
  final bytes = await tester.runAsync(() async {
    final ui.Image image = await boundary.toImage();
    final data = await image.toByteData(format: ui.ImageByteFormat.rawRgba);
    return data!.buffer.asUint8List();
  });
  return bytes!;
}

void main() {

  final cases = <String, (ThemeMode, VText Function())>{
    'border': (ThemeMode.light, () => _text()),
    'no border': (ThemeMode.light, () => _text(style: SWT.SINGLE)),
    'read-only': (ThemeMode.light, () => _text(style: SWT.SINGLE | SWT.BORDER | SWT.READ_ONLY)),
    'not editable': (ThemeMode.light, () => _text(editable: false)),
    'disabled': (ThemeMode.light, () => _text(enabled: false)),
    'centered': (ThemeMode.light, () => _text(style: SWT.SINGLE | SWT.BORDER | SWT.CENTER)),
    'right': (ThemeMode.light, () => _text(style: SWT.SINGLE | SWT.BORDER | SWT.RIGHT)),
    'long text': (ThemeMode.light, () => _text(text: _long)),
    'long text, right': (ThemeMode.light, () => _text(style: SWT.SINGLE | SWT.BORDER | SWT.RIGHT, text: _long)),
    'empty': (ThemeMode.light, () => _text(text: '')),
    'empty, hint': (ThemeMode.light, () => _text(text: '', message: 'Search here')),
    'hint behind text': (ThemeMode.light, () => _text(message: 'Search here')),
    'search': (ThemeMode.light,
        () => _text(style: SWT.SINGLE | SWT.BORDER | SWT.SEARCH | SWT.ICON_SEARCH | SWT.ICON_CANCEL)),
    'transparent': (ThemeMode.light, () => _text(style: SWT.SINGLE | SWT.TRANSPARENT)),
    'text limit': (ThemeMode.light, () => _text(textLimit: 10)),
    'tall': (ThemeMode.light, () => _text(width: 200, height: 40)),
    'short': (ThemeMode.light, () => _text(height: 16)),
    'narrow': (ThemeMode.light, () => _text(width: 40)),
    'foreground': (ThemeMode.light,
        () => _text(foreground: VColor()..red = 200..green = 30..blue = 30..alpha = 255)),
    'font': (ThemeMode.light,
        () => _text(height: 28, font: VFont()..fontData = [VFontData()..name = 'Inter'..height = 14..style = SWT.BOLD])),
    'dark': (ThemeMode.dark, () => _text()),
    'dark, disabled': (ThemeMode.dark, () => _text(enabled: false)),
  };

  for (final entry in cases.entries) {
    testWidgets('at rest it paints what the TextField paints: ${entry.key}', (tester) async {
      await _loadFonts();
      final (theme, value) = entry.value;
      final field = await _paint(tester, value(), theme, live: true);
      final rest = await _paint(tester, value(), theme, live: false);
      expect(rest.length, field.length, reason: 'the two are not the same size');
      var differing = 0;
      for (var i = 0; i < field.length; i += 4) {
        if (field[i] != rest[i] || field[i + 1] != rest[i + 1] || field[i + 2] != rest[i + 2] ||
            field[i + 3] != rest[i + 3]) {
          differing++;
        }
      }
      expect(differing, 0, reason: '$differing of ${field.length ~/ 4} pixels differ');
    });
  }
}
