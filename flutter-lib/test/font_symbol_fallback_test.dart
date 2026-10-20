// The fonts SWT asks for are mapped to the bundled Liberation faces, which have no glyphs for the Mac
// key symbols (⌘ ⌥ ⇧ ↩) that JFace puts in shortcut hints and menus. Text falls back to the bundled
// Inter, which has them, rather than to a box or a font fetched from the network.

import 'dart:ui' as ui;

import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/gen/font.dart';
import 'package:swtflutter/src/gen/fontdata.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/styledtext.dart';
import 'package:swtflutter/src/gen/stylerange.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/impl/styledtext_evolve.dart';
import 'package:swtflutter/src/impl/utils/font_utils.dart';

void main() {
  TestWidgetsFlutterBinding.ensureInitialized();

  test('a bundled face without the symbols falls back to one that has them', () {
    final liberation = VFont()
      ..fontData = [VFontData()..name = 'Liberation Sans'..height = 12..style = 0];
    expect(FontUtils.textStyleFromVFont(liberation, null).fontFamilyFallback, contains('Inter'));
  });

  test('a family this side does not have keeps resolving to the default font', () {
    final unknown = VFont()..fontData = [VFontData()..name = 'System'..height = 12..style = 0];
    expect(FontUtils.textStyleFromVFont(unknown, null).fontFamilyFallback, isNull,
        reason: 'a fallback list would replace the default font Java measured the text with');
  });

  testWidgets('a StyledText range in a bundled face without the symbols falls back too',
      (tester) async {
    final key = GlobalKey<StyledTextImpl>();
    final value = VStyledText()
      ..swt = 'StyledText'
      ..id = 1
      ..style = SWT.NONE
      ..enabled = true
      ..text = 'Press \u21A9 to refactor'
      ..bounds = (VRectangle()
        ..x = 0
        ..y = 0
        ..width = 300
        ..height = 40);
    await tester.pumpWidget(EvolveApp(
      theme: ThemeMode.light,
      contentWidget: SizedBox(
        width: 300,
        height: 40,
        child: StyledTextSwt<VStyledText>(key: key, value: value),
      ),
    ));

    final range = VStyleRange()
      ..font = (VFont()..fontData = [VFontData()..name = 'Liberation Mono'..height = 12..style = SWT.BOLD]);
    expect(key.currentState!.debugRangeStyle(range).fontFamilyFallback,
        FontUtils.fallbackFamilies);
  });

  test('a font that finishes loading drops the StyledText lines laid out before it', () async {
    debugResetLineLayoutCache();
    // A new shape each time: a shape keeps its own lines, and what is shared between paints is the
    // cache across shapes.
    TextShape shape() => TextShape(
          'one\ntwo\nthree',
          Offset.zero,
          const TextStyle(fontSize: 10, fontFamilyFallback: ['Inter']),
          null, null, null, false, const Size(200, 100), true, null, null, null, null, 12.0, 4,
        );
    shape().draw(Canvas(ui.PictureRecorder()));
    debugLineLayoutHits = 0;
    shape().draw(Canvas(ui.PictureRecorder()));
    expect(debugLineLayoutHits, greaterThan(0), reason: 'a later paint is served from the cache');

    await TestDefaultBinaryMessengerBinding.instance.defaultBinaryMessenger.handlePlatformMessage(
        SystemChannels.system.name,
        SystemChannels.system.codec.encodeMessage(<String, dynamic>{'type': 'fontsChange'}),
        (_) {});
    debugLineLayoutHits = 0;
    shape().draw(Canvas(ui.PictureRecorder()));
    expect(debugLineLayoutHits, 0, reason: 'what was measured before the font arrived is laid out again');
  });
}
