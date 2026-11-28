// The active tab's emphasis is the cue for which stack keyboard input will reach, so with several
// folders open only the active one marks its selected tab. Every folder here has highlightEnabled, and
// upstream's shouldHighlight also requires the folder to be the active one.

import 'package:flutter/material.dart';
import 'package:flutter/services.dart' show FontLoader, rootBundle;
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/gen/ctabfolder.dart';
import 'package:swtflutter/src/gen/ctabitem.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/impl/config_flags.dart';
import 'package:swtflutter/src/impl/widget_config.dart' show resetConfigFlags, setConfigFlags;
// ignore: unused_import
import 'package:swtflutter/src/impl/ctabfolder_evolve.dart';

VCTabFolder _folder({
  required int id,
  required String label,
  required bool highlight,
  List<String> after = const [],
  int selection = 0,
}) =>
    VCTabFolder()
      ..id = id
      ..style = SWT.NONE
      ..selection = selection
      ..enabled = true
      ..highlight = highlight
      ..highlightEnabled = true
      ..items = [
        VCTabItem()
          ..id = id + 1
          ..text = label,
        for (var i = 0; i < after.length; i++)
          VCTabItem()
            ..id = id + 2 + i
            ..text = after[i],
      ];

/// The box that holds one tab.
Finder _tabBox(String label) => find
    .ancestor(of: find.text(label), matching: find.byWidgetPredicate((w) => w is AnimatedContainer))
    .first;

Widget _stack(VCTabFolder value) => SizedBox(
      width: 300,
      height: 120,
      child: CTabFolderSwt<VCTabFolder>(value: value),
    );

Widget _host(List<Widget> children) => EvolveApp(
      theme: ThemeMode.light,
      contentWidget: Column(children: children),
    );

Text _label(WidgetTester tester, String label) => tester.widget<Text>(find.text(label).first);

/// Whether the selected tab's label is drawn with the active emphasis.
bool _emphasised(WidgetTester tester, String label) =>
    _label(tester, label).style?.fontWeight == FontWeight.w700;

void main() {
  testWidgets('only the folder that holds focus emphasises its selected tab',
      (WidgetTester tester) async {
    await tester.pumpWidget(_host([
      _stack(_folder(id: 1000, label: 'Active', highlight: true)),
      _stack(_folder(id: 2000, label: 'Idle', highlight: false)),
    ]));
    while (tester.takeException() != null) {}

    expect(_emphasised(tester, 'Active'), isTrue,
        reason: 'the folder that holds focus must mark its selected tab');
    expect(_emphasised(tester, 'Idle'), isFalse,
        reason: 'a folder that was never activated must not look focused');
    expect(_label(tester, 'Active').style?.color, _label(tester, 'Idle').style?.color,
        reason: 'focus changes the selected tab\'s weight, not its text colour');
  });

  for (final mode in [ThemeMode.light, ThemeMode.dark]) {
    testWidgets('the active tab keeps the unselected text colour in $mode',
        (WidgetTester tester) async {
      await tester.pumpWidget(EvolveApp(
        theme: mode,
        contentWidget: _stack(VCTabFolder()
          ..id = 4000
          ..style = SWT.NONE
          ..selection = 1
          ..enabled = true
          ..highlight = true
          ..highlightEnabled = true
          ..items = [
            VCTabItem()
              ..id = 4001
              ..text = 'Resources',
            VCTabItem()
              ..id = 4002
              ..text = 'Control Flow',
          ]),
      ));
      while (tester.takeException() != null) {}

      expect(_emphasised(tester, 'Control Flow'), isTrue);
      expect(_label(tester, 'Control Flow').style?.color, _label(tester, 'Resources').style?.color,
          reason: 'the active tab is marked by weight and frame, not by a brighter colour');
    });
  }

  testWidgets('highlightEnabled=false opts the folder out of the emphasis',
      (WidgetTester tester) async {
    await tester.pumpWidget(_host([
      _stack(_folder(id: 3000, label: 'Opted out', highlight: true)
        ..highlightEnabled = false),
    ]));
    while (tester.takeException() != null) {}

    expect(_emphasised(tester, 'Opted out'), isFalse,
        reason: 'setHighlightEnabled(false) opts the folder out of the '
            'active-folder rendering, active or not');
  });

  for (final mode in [ThemeMode.light, ThemeMode.dark]) {
    testWidgets('with highlightEnabled=false the selected tab keeps the unselected text colour in $mode',
        (WidgetTester tester) async {
      await tester.pumpWidget(EvolveApp(
        theme: mode,
        contentWidget: _stack(VCTabFolder()
          ..id = 5000
          ..style = SWT.NONE
          ..selection = 1
          ..enabled = true
          ..highlightEnabled = false
          ..items = [
            VCTabItem()
              ..id = 5001
              ..text = 'Outline',
            VCTabItem()
              ..id = 5002
              ..text = 'Problems',
          ]),
      ));
      while (tester.takeException() != null) {}

      expect(_label(tester, 'Problems').style?.color, _label(tester, 'Outline').style?.color);
    });

    testWidgets('the selected tab draws its dirty dot in the unselected text colour in $mode',
        (WidgetTester tester) async {
      VCTabItem dirtyItem(int id, String text) => VCTabItem()
        ..id = id
        ..text = text
        ..showClose = true
        ..showDirty = true
        ..showing = true;
      await tester.pumpWidget(EvolveApp(
        theme: mode,
        contentWidget: SizedBox(
          width: 600,
          height: 240,
          child: CTabFolderSwt<VCTabFolder>(
              value: VCTabFolder()
                ..id = 6000
                ..style = SWT.CLOSE
                ..enabled = true
                ..selection = 0
                ..dirtyIndicatorStyle = true
                ..items = [dirtyItem(6001, 'A.java'), dirtyItem(6002, 'B.java')]),
        ),
      ));
      await tester.pumpAndSettle();

      final dots = find
          .byWidgetPredicate((w) =>
              w is DecoratedBox &&
              w.decoration is BoxDecoration &&
              (w.decoration as BoxDecoration).shape == BoxShape.circle)
          .evaluate()
          .toList()
        ..sort((a, b) => tester.getCenter(find.byWidget(a.widget)).dx.compareTo(
            tester.getCenter(find.byWidget(b.widget)).dx));
      Color rgb(Element dot) =>
          ((dot.widget as DecoratedBox).decoration as BoxDecoration).color!.withAlpha(255);

      expect(dots, hasLength(2));
      expect(rgb(dots[0]), rgb(dots[1]),
          reason: 'the dot takes the tab text colour, and the selected tab no longer has its own');
    });
  }

  for (final tabScroll in [true, false]) {
    testWidgets('the selected tab keeps its width when its folder gains or loses focus '
        '(ctabfolder_tab_scroll=$tabScroll)', (WidgetTester tester) async {
      // The test font draws every weight at one width, which would hide the change.
      await tester.runAsync(() async {
        final loader = FontLoader('Inter');
        for (final face in ['Regular', 'Medium', 'SemiBold', 'Bold']) {
          loader.addFont(rootBundle.load('assets/fonts/Inter_18pt-$face.ttf'));
        }
        await loader.load();
      });
      setConfigFlags(ConfigFlags()..ctabfolder_tab_scroll = tabScroll);
      addTearDown(resetConfigFlags);

      Future<(double, double)> layout(bool highlight) async {
        await tester.pumpWidget(_host([
          _stack(_folder(
              id: highlight ? 4000 : 5000, label: 'Untitled 12', highlight: highlight, after: ['Next'])),
        ]));
        await tester.pump();
        while (tester.takeException() != null) {}
        expect(_emphasised(tester, 'Untitled 12'), highlight);
        return (tester.getSize(_tabBox('Untitled 12')).width, tester.getTopLeft(_tabBox('Next')).dx);
      }

      final (activeWidth, activeNext) = await layout(true);
      final (idleWidth, idleNext) = await layout(false);

      expect(idleWidth, activeWidth,
          reason: 'the label\'s weight follows focus, the tab\'s width must not');
      expect(idleNext, activeNext, reason: 'the tabs after it must not move');

      await tester.pumpWidget(_host([
        _stack(_folder(id: 6000, label: 'Untitled 12', highlight: true, after: ['Next'], selection: 1)),
      ]));
      await tester.pump();
      while (tester.takeException() != null) {}
      expect(tester.getSize(_tabBox('Untitled 12')).width, activeWidth,
          reason: 'nor does selection: SWT never resizes a tab when another one is selected');
    });
  }
}
