// What a single-line label shows of its text, font and foreground is its Text alone, so a change of
// those that keeps it on one line rebuilds only that Text, not the label, and draws it as a label
// mounted with them does. A text that leaves one line - a line delimiter, or no text at all -
// changes how the label is laid out, and rebuilds the label; a rebuild for anything else leaves the
// Text as it was.

import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/gen/color.dart';
import 'package:swtflutter/src/gen/font.dart';
import 'package:swtflutter/src/gen/fontdata.dart';
import 'package:swtflutter/src/gen/label.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/impl/config_flags.dart';
import 'package:swtflutter/src/impl/widget_config.dart';
import 'package:swtflutter/src/gen/widgets.dart' show WireKey;

import 'delivery/support/deliver.dart';

VLabel _label(String text, {int seq = 10, VFont? font, VColor? foreground}) => VLabel()
  ..id = 1
  ..style = SWT.NONE
  ..seq = seq
  ..text = text
  ..font = font
  ..foreground = foreground
  ..bounds = (VRectangle()
    ..width = 160
    ..height = 40);

Future<void> _mount(WidgetTester tester, [VLabel? value]) async {
  await tester.pumpWidget(const SizedBox());
  freshClient();
  await tester.pumpWidget(EvolveApp(
    theme: ThemeMode.light,
    contentWidget: SizedBox(width: 160, height: 40, child: LabelSwt<VLabel>(value: value ?? _label('Before'))),
  ));
  await tester.pumpAndSettle();
}

final _font = VFont()
  ..fontData = [
    VFontData()
      ..name = 'Inter'
      ..height = 15
      ..style = SWT.BOLD
  ];

final _red = VColor()
  ..red = 200
  ..green = 10
  ..blue = 10
  ..alpha = 255;

/// Counts the label's own rebuilds from here on.
int Function() _countLabelBuilds() {
  var builds = 0;
  debugOnRebuildDirtyWidget = (element, _) {
    if (element.widget is LabelSwt) builds++;
  };
  addTearDown(() => debugOnRebuildDirtyWidget = null);
  return () => builds;
}

void main() {
  testWidgets('a new text on one line is shown without rebuilding the label', (tester) async {
    await _mount(tester);
    final builds = _countLabelBuilds();

    await deliverChange(_label('After &it', seq: 11), keys: [WireKey.k_Label_text], base: 10);
    await tester.pumpAndSettle();

    expect(find.text('After it'), findsOneWidget, reason: 'the accelerator marker is stripped as before');
    expect(find.text('Before'), findsNothing);
    expect(builds(), 0);
  });

  testWidgets('a text that leaves one line rebuilds the label', (tester) async {
    await _mount(tester);
    final builds = _countLabelBuilds();

    await deliverChange(_label('Two\nlines', seq: 11), keys: [WireKey.k_Label_text], base: 10);
    await tester.pumpAndSettle();

    expect(find.text('Two\nlines'), findsOneWidget);
    expect(builds(), 1);
  });

  testWidgets('a change of more than the text rebuilds the label', (tester) async {
    await _mount(tester);
    final builds = _countLabelBuilds();

    await deliverChange(_label('After', seq: 11)..enabled = false, keys: [WireKey.k_Label_text, WireKey.k_Control_enabled], base: 10);
    await tester.pumpAndSettle();

    expect(find.text('After'), findsOneWidget);
    expect(builds(), 1);
  });

  for (final (name, changed, after, labelColors) in [
    ('font', [WireKey.k_Control_font], _label('Before', seq: 11, font: _font), false),
    ('foreground', [WireKey.k_Control_foreground], _label('Before', seq: 11, foreground: _red), false),
    // The application's colours switched on for labels alone.
    ('foreground, label colours on', [WireKey.k_Control_foreground], _label('Before', seq: 11, foreground: _red), true),
  ]) {
    testWidgets('a new $name is drawn as a label mounted with it, without rebuilding the label', (tester) async {
      if (labelColors) {
        setConfigFlags(ConfigFlags()..use_swt_font_colors_by_widget = {'label': true});
        addTearDown(resetConfigFlags);
      }
      await _mount(tester, after);
      final expected = tester.widget<Text>(find.text('Before')).style;

      await _mount(tester);
      final builds = _countLabelBuilds();
      await deliverChange(after, keys: changed, base: 10);
      await tester.pumpAndSettle();

      expect(tester.widget<Text>(find.text('Before')).style, expected);
      expect(builds(), 0);
    });
  }

  testWidgets('a new font after a new text is drawn as a label mounted with both', (tester) async {
    await _mount(tester, _label('After', seq: 12, font: _font));
    final expected = tester.widget<Text>(find.text('After')).style;

    await _mount(tester);
    await deliverChange(_label('After', seq: 11), keys: [WireKey.k_Label_text], base: 10);
    await tester.pumpAndSettle();
    await deliverChange(_label('After', seq: 12, font: _font), keys: [WireKey.k_Control_font], base: 11);
    await tester.pumpAndSettle();

    expect(tester.widget<Text>(find.text('After')).style, expected);
  });

  testWidgets('a change of something else leaves the Text as it was', (tester) async {
    await _mount(tester);
    final before = tester.widget<Text>(find.text('Before'));

    await deliverChange(_label('Before', seq: 11)..background = _red, keys: [WireKey.k_Control_background], base: 10);
    await tester.pumpAndSettle();

    expect(tester.widget<Text>(find.text('Before')), same(before));
  });

  testWidgets('a new text arriving with something else is still shown', (tester) async {
    await _mount(tester);

    await deliverChange(_label('After', seq: 11)..background = _red, keys: [WireKey.k_Label_text, WireKey.k_Control_background], base: 10);
    await tester.pumpAndSettle();

    expect(find.text('After'), findsOneWidget);
    expect(find.text('Before'), findsNothing);
  });
}
