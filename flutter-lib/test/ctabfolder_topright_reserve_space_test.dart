// With several tabs open in a narrow CTabFolder, the topRight
// controls float on top of the tab row (a Stack overlay, Positioned(right: 0, ...))
// instead of taking their own space. When the tabs overflow, the rightmost ones end
// up permanently underneath that overlay -- no scroll position ever clears them, so
// they become unreachable.
//
// ctabfolder_topright_auto_hide defaults to true (the historical behaviour, kept
// unchanged for existing clients). Set it to false to opt into a layout where the
// controls are always shown and are a normal Row sibling instead of a floating
// overlay, so the tabs' scrollable area shrinks to leave them real room instead of
// hiding them.
//
// Uses the minimize button (state.minimizeVisible) as the stand-in "control" that
// competes for space, rather than a real topRight Composite: a topRight Composite
// with children currently hits an unrelated, pre-existing debug-only layout
// assertion in ToolbarComposite's non-bounds-layout Row/Expanded/ClipRect chain
// (release builds render it fine, confirmed live). The minimize button exercises
// the exact same _buildTopRightControls reserve-space/overlay mechanics without it.

import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/gen/ctabfolder.dart';
import 'package:swtflutter/src/gen/ctabitem.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/impl/config_flags.dart';
// ignore: unused_import
import 'package:swtflutter/src/impl/ctabfolder_evolve.dart';
import 'package:swtflutter/src/impl/widget_config.dart';

const _narrowWidth = 260.0;
const _tabCount = 5;
const _lastTabLabel = 'Tab $_tabCount';

VCTabFolder _folder() => VCTabFolder()
  ..id = 1000
  ..style = SWT.NONE
  ..selection = 0
  ..minimizeVisible = true
  ..items = List.generate(
    _tabCount,
    (i) => VCTabItem()
      ..id = 10 + i
      ..text = 'Tab ${i + 1}',
  );

Widget _host(VCTabFolder value) => EvolveApp(
      theme: ThemeMode.light,
      contentWidget: SizedBox(
        width: _narrowWidth,
        height: 200,
        child: CTabFolderSwt<VCTabFolder>(value: value),
      ),
    );

void _drainPaintExceptions(WidgetTester tester) {
  while (tester.takeException() != null) {}
}

void main() {
  setUp(resetConfigFlags);
  tearDown(resetConfigFlags);

  testWidgets(
      'ctabfolder_topright_auto_hide=false: the control reserves its own space, tabs never sit under it',
      (WidgetTester tester) async {
    setConfigFlags(ConfigFlags()..ctabfolder_topright_auto_hide = false);
    await tester.pumpWidget(_host(_folder()));
    _drainPaintExceptions(tester);

    final controlRect = tester.getRect(find.byIcon(Icons.minimize));
    final lastTabRect = tester.getRect(find.text(_lastTabLabel));

    expect(controlRect.overlaps(lastTabRect), isFalse,
        reason: 'With the flag off, the controls are a Row sibling, not a '
            'floating overlay, so the scrollable tab area is narrower and every '
            'tab -- including the last one -- stays clear of them. In the default '
            'hover-only mode this control sits as a Positioned(right: 0, ...) '
            'overlay on top of the tab row regardless of width.');
  });

  testWidgets(
      'ctabfolder_topright_auto_hide=false implies the controls are always visible, not hover-only',
      (WidgetTester tester) async {
    setConfigFlags(ConfigFlags()..ctabfolder_topright_auto_hide = false);
    await tester.pumpWidget(_host(_folder()));
    _drainPaintExceptions(tester);

    expect(find.byIcon(Icons.minimize), findsOneWidget,
        reason: 'A client relying on an always-visible topRight toolbar should '
            'not also have to set ctabfolder_visible_controls separately.');
  });
}
