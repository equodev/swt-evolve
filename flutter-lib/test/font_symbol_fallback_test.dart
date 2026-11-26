// The fonts SWT asks for are mapped to the bundled Liberation faces, which have no glyphs for the Mac
// key symbols (⌘ ⌥ ⇧ ↩) that JFace puts in shortcut hints and menus. Text falls back to the bundled
// Inter, which has them, rather than to a box or a font fetched from the network.

import 'dart:ui' as ui;

import 'package:flutter/foundation.dart';
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

  test('where the engine default has no name, a family this side does not have gets no fallback', () {
    FontUtils.debugEngineDefaultOverride = (family: null);
    addTearDown(() => FontUtils.debugEngineDefaultOverride = null);
    final unknown = VFont()..fontData = [VFontData()..name = 'System'..height = 12..style = 0];
    expect(FontUtils.textStyleFromVFont(unknown, null).fontFamilyFallback, isNull,
        reason: 'a fallback list with no default ahead of it would replace the font Java measured');
  });

  test('with a default font it can name, a family this side does not have falls back behind it', () {
    FontUtils.debugEngineDefaultOverride = (family: 'Roboto');
    addTearDown(() => FontUtils.debugEngineDefaultOverride = null);
    final system = VFont()..fontData = [VFontData()..name = 'system'..height = 11..style = 0];
    expect(FontUtils.textStyleFromVFont(system, null).fontFamilyFallback, ['Roboto', 'Inter'],
        reason: 'the default font still draws the text, and a symbol such as \u25B6 comes from Inter '
            'rather than from the colour emoji font the web engine tries next');
    final inter = VFont()..fontData = [VFontData()..name = 'Inter'..height = 11..style = 0];
    expect(FontUtils.textStyleFromVFont(inter, null).fontFamilyFallback, isNull);
  });

  group('the face "System" is drawn in on the desktop', () {
    final system = VFont()..fontData = [VFontData()..name = 'System'..height = 12..style = 0];
    tearDown(() => debugDefaultTargetPlatformOverride = null);

    test('is the bundled Inter on Linux, where the engine default depends on the fonts installed', () {
      debugDefaultTargetPlatformOverride = TargetPlatform.linux;
      expect(FontUtils.textStyleFromVFont(system, null).fontFamilyFallback, ['Inter']);
      final installed = VFont()..fontData = [VFontData()..name = 'DejaVu Sans'..height = 12..style = 0];
      expect(FontUtils.textStyleFromVFont(installed, null).fontFamily, 'DejaVu Sans',
          reason: 'a family the host has is still asked for by its own name');
    });

    test('stays the OS default on Windows and macOS', () {
      for (final os in [TargetPlatform.windows, TargetPlatform.macOS]) {
        debugDefaultTargetPlatformOverride = os;
        expect(FontUtils.textStyleFromVFont(system, null).fontFamilyFallback, isNull, reason: '$os');
      }
    });

    test('is Roboto on the web, whatever the OS', () {
      for (final os in [TargetPlatform.linux, TargetPlatform.windows, TargetPlatform.macOS]) {
        expect(FontUtils.defaultFaceFor(web: true, platform: os), 'Roboto', reason: '$os');
      }
    });

    // DesktopSystemFontMetricsTest.java holds Java's measure of System to the width checked here,
    // the one this face is laid out at, so the two sides agree on the text that was clipped.
    test('on Linux is a face laid out at the width Java measures it with', () async {
      final inter = FontLoader('Inter')..addFont(rootBundle.load('assets/fonts/Inter_18pt-Regular.ttf'));
      await inter.load();
      debugDefaultTargetPlatformOverride = TargetPlatform.linux;
      final face = FontUtils.textStyleFromVFont(system, null).fontFamilyFallback?.first;
      expect(face, 'Inter', reason: 'System is drawn in the bundled Inter');
      // The test engine draws a family it does not have in its own test font instead of falling
      // back, so the face the fallback names is laid out directly.
      final painter = TextPainter(
        text: TextSpan(
          text: 'Create a new C or C++ project',
          style: TextStyle(fontFamily: face, fontSize: 12),
        ),
        textDirection: TextDirection.ltr,
      )..layout();
      addTearDown(painter.dispose);
      expect(painter.width, closeTo(170.21, 0.05));
    });
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
          null, null, false, const Size(200, 100), true, null, null, null, null, 12.0, 4,
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
