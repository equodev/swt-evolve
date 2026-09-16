// The active tab's emphasis is the cue for which stack keyboard input will reach, so with several
// folders open only the active one marks its selected tab. Every folder here has highlightEnabled, and
// upstream's shouldHighlight also requires the folder to be the active one.

import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/gen/ctabfolder.dart';
import 'package:swtflutter/src/gen/ctabitem.dart';
import 'package:swtflutter/src/gen/swt.dart';
// ignore: unused_import
import 'package:swtflutter/src/impl/ctabfolder_evolve.dart';
import 'package:swtflutter/src/theme/theme_extensions/ctabitem_theme_extension.dart';

VCTabFolder _folder({
  required int id,
  required String label,
  required bool highlight,
}) =>
    VCTabFolder()
      ..id = id
      ..style = SWT.NONE
      ..selection = 0
      ..enabled = true
      ..highlight = highlight
      ..highlightEnabled = true
      ..items = [
        VCTabItem()
          ..id = id + 1
          ..text = label,
      ];

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

CTabItemThemeExtension _itemTheme(WidgetTester tester, String label) =>
    Theme.of(tester.element(find.text(label).first)).extension<CTabItemThemeExtension>()!;

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
    expect(_label(tester, 'Active').style?.color, _itemTheme(tester, 'Active').tabItemActiveTextColor);
    expect(_label(tester, 'Idle').style?.color, _itemTheme(tester, 'Idle').tabItemTextColor,
        reason: 'a selected tab in a folder without focus is dimmed, so only the active one stands out');
  });

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
    expect(_label(tester, 'Opted out').style?.color,
        _itemTheme(tester, 'Opted out').tabItemSelectedTextColor,
        reason: 'a folder outside activation keeps its selected tab in the selected colour');
  });
}
