// The frame around a CTabFolder is the only cue for which stack keyboard input will reach, so
// with several folders open exactly the active one draws it. highlightEnabled is true on every
// folder; upstream's shouldHighlight also requires the folder to be the active one.

import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/gen/ctabfolder.dart';
import 'package:swtflutter/src/gen/ctabitem.dart';
import 'package:swtflutter/src/gen/swt.dart';
// ignore: unused_import
import 'package:swtflutter/src/impl/ctabfolder_evolve.dart';

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

/// The frame the folder paints over itself, or null when it paints none.
BoxBorder? _frameAround(WidgetTester tester, String label) {
  final container = tester.widget<Container>(
    find.ancestor(of: find.text(label), matching: find.byType(Container)).last,
  );
  return (container.foregroundDecoration as BoxDecoration?)?.border;
}

void main() {
  testWidgets('only the folder that holds focus is framed',
      (WidgetTester tester) async {
    await tester.pumpWidget(_host([
      _stack(_folder(id: 1000, label: 'Active', highlight: true)),
      _stack(_folder(id: 2000, label: 'Idle', highlight: false)),
    ]));
    while (tester.takeException() != null) {}

    expect(_frameAround(tester, 'Active'), isNotNull,
        reason: 'the folder that holds focus must be framed');
    expect(_frameAround(tester, 'Idle'), isNull,
        reason: 'a folder that was never activated must not look focused');
  });

  testWidgets('highlightEnabled=false opts the folder out of the frame',
      (WidgetTester tester) async {
    await tester.pumpWidget(_host([
      _stack(_folder(id: 3000, label: 'Opted out', highlight: true)
        ..highlightEnabled = false),
    ]));
    while (tester.takeException() != null) {}

    expect(_frameAround(tester, 'Opted out'), isNull,
        reason: 'setHighlightEnabled(false) opts the folder out of the '
            'active-folder rendering, active or not');
  });
}
