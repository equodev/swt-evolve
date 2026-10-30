// A CCombo nobody is interacting with is drawn without its DropdownMenu, which costs a CCombo update
// a small fraction of what the menu does. It has to be indistinguishable from the closed menu: this
// paints both and compares them pixel for pixel.

import 'dart:typed_data';
import 'dart:ui' as ui;

import 'package:flutter/material.dart';
import 'package:flutter/rendering.dart';
import 'package:flutter/services.dart' show FontLoader, rootBundle;
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/gen/ccombo.dart';
import 'package:swtflutter/src/gen/color.dart';
import 'package:swtflutter/src/gen/font.dart';
import 'package:swtflutter/src/gen/fontdata.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/impl/ccombo_evolve.dart';
import 'package:swtflutter/src/impl/utils/force_live.dart';

Future<void> _loadFonts() async {
  final inter = FontLoader('Inter')..addFont(rootBundle.load('assets/fonts/Inter_18pt-Medium.ttf'));
  await inter.load();
}

VCCombo _ccombo({
  required int style,
  int? width,
  int? height,
  int? alignment,
  bool enabled = true,
  String text = 'Medium',
  VColor? foreground,
  VFont? font,
}) =>
    VCCombo()
      ..id = 7
      ..style = style
      ..enabled = enabled
      ..alignment = alignment
      ..text = text
      ..items = ['Small', text, 'Large']
      ..foreground = foreground
      ..font = font
      ..bounds = width == null
          ? null
          : (VRectangle()
            ..x = 0
            ..y = 0
            ..width = width
            ..height = height!);

const _long = 'A value far too long to fit in the field';

Future<Uint8List> _paint(WidgetTester tester, VCCombo value, ThemeMode theme, {required bool live}) async {
  final key = GlobalKey();
  await tester.pumpWidget(ForceLive.when(live, EvolveApp(
    theme: theme,
    contentWidget: Align(
      alignment: Alignment.topLeft,
      child: RepaintBoundary(key: key, child: CComboSwt<VCCombo>(value: value)),
    ),
  )));
  await tester.pumpAndSettle();
  expect(find.byType(DropdownMenu<String>), live ? findsOneWidget : findsNothing);
  final boundary = key.currentContext!.findRenderObject()! as RenderRepaintBoundary;
  final bytes = await tester.runAsync(() async {
    final ui.Image image = await boundary.toImage();
    final data = await image.toByteData(format: ui.ImageByteFormat.rawRgba);
    return data!.buffer.asUint8List();
  });
  return bytes!;
}

void main() {

  final cases = <String, (ThemeMode, VCCombo Function())>{
    'read-only': (ThemeMode.light, () => _ccombo(style: SWT.READ_ONLY, width: 140, height: 24)),
    'read-only, border': (ThemeMode.light, () => _ccombo(style: SWT.READ_ONLY | SWT.BORDER, width: 140, height: 28)),
    'editable': (ThemeMode.light, () => _ccombo(style: SWT.NONE, width: 140, height: 24)),
    'editable, border': (ThemeMode.light, () => _ccombo(style: SWT.BORDER, width: 160, height: 30)),
    'centered': (ThemeMode.light, () => _ccombo(style: SWT.READ_ONLY, width: 140, height: 24, alignment: SWT.CENTER)),
    'right': (ThemeMode.light, () => _ccombo(style: SWT.READ_ONLY, width: 140, height: 24, alignment: SWT.RIGHT)),
    'narrow': (ThemeMode.light, () => _ccombo(style: SWT.READ_ONLY, width: 50, height: 22)),
    'tall': (ThemeMode.light, () => _ccombo(style: SWT.READ_ONLY | SWT.BORDER, width: 200, height: 40)),
    'disabled': (ThemeMode.light, () => _ccombo(style: SWT.READ_ONLY, width: 140, height: 24, enabled: false)),
    'long text': (ThemeMode.light, () => _ccombo(style: SWT.READ_ONLY, width: 140, height: 24, text: _long)),
    'long text, centered': (ThemeMode.light,
        () => _ccombo(style: SWT.READ_ONLY, width: 140, height: 24, text: _long, alignment: SWT.CENTER)),
    'long text, right': (ThemeMode.light,
        () => _ccombo(style: SWT.READ_ONLY, width: 140, height: 24, text: _long, alignment: SWT.RIGHT)),
    'foreground': (ThemeMode.light, () => _ccombo(style: SWT.READ_ONLY, width: 140, height: 24,
        foreground: VColor()..red = 200..green = 30..blue = 30..alpha = 255)),
    'font': (ThemeMode.light, () => _ccombo(style: SWT.READ_ONLY, width: 160, height: 28,
        font: VFont()..fontData = [VFontData()..name = 'Inter'..height = 14..style = SWT.BOLD])),
    'empty': (ThemeMode.light, () => _ccombo(style: SWT.READ_ONLY, width: 140, height: 24, text: '')),
    'dark': (ThemeMode.dark, () => _ccombo(style: SWT.READ_ONLY | SWT.BORDER, width: 140, height: 28)),
    'dark, disabled': (ThemeMode.dark, () => _ccombo(style: SWT.READ_ONLY, width: 140, height: 24, enabled: false)),
    'natural size, read-only': (ThemeMode.light, () => _ccombo(style: SWT.READ_ONLY)),
    'natural size, editable, border': (ThemeMode.light, () => _ccombo(style: SWT.BORDER)),
    'natural size, centered': (ThemeMode.light, () => _ccombo(style: SWT.READ_ONLY, alignment: SWT.CENTER)),
  };

  for (final entry in cases.entries) {
    testWidgets('at rest it paints what the closed menu paints: ${entry.key}', (tester) async {
      await _loadFonts();
      final (theme, value) = entry.value;
      final menu = await _paint(tester, value(), theme, live: true);
      final rest = await _paint(tester, value(), theme, live: false);
      expect(rest.length, menu.length, reason: 'the two are not the same size');
      var differing = 0;
      for (var i = 0; i < menu.length; i += 4) {
        if (menu[i] != rest[i] || menu[i + 1] != rest[i + 1] || menu[i + 2] != rest[i + 2] || menu[i + 3] != rest[i + 3]) {
          differing++;
        }
      }
      expect(differing, 0, reason: '$differing of ${menu.length ~/ 4} pixels differ');
    });
  }
}
