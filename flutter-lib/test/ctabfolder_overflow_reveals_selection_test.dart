// With more tabs than the strip can show, a native CTabFolder keeps the selected tab in view, and
// its chevron asks the application for a list (CTabFolder2Listener.showList) before falling back to
// its own menu. The workbench answers that event with its part list, so the chevron has to reach
// Java first; the folder's own list opens only when Java says no listener took the event.

import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/gen/ctabfolder.dart';
import 'package:swtflutter/src/gen/ctabitem.dart';
import 'package:swtflutter/src/gen/event.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/impl/config_flags.dart';
// ignore: unused_import
import 'package:swtflutter/src/impl/ctabfolder_evolve.dart';
import 'package:swtflutter/src/impl/widget_config.dart';

import 'delivery/support/deliver.dart';

const _folderId = 1000;

const _labels = [
  'Hello.java',
  'Long.java',
  'note01.txt',
  'note02.txt',
  'note03.txt',
  'note04.txt',
  'note05.txt',
  'note06.txt',
  'note07.txt',
  'note08.txt',
  'note09.txt',
  'note10.txt',
];

class _CapturingCTabFolderSwt extends CTabFolderSwt<VCTabFolder> {
  const _CapturingCTabFolderSwt({required super.value, required this.onEvent});

  final void Function(String ev, VEvent? payload) onEvent;

  @override
  void sendEvent(VCTabFolder val, String ev, VEvent? payload) => onEvent(ev, payload);
}

VCTabFolder _folder({int selection = 0, int? showListPopupSeq, int seq = 0}) => VCTabFolder()
  ..id = _folderId
  ..seq = seq
  ..style = SWT.NONE
  ..enabled = true
  ..visible = true
  ..selection = selection
  ..showListPopupSeq = showListPopupSeq
  ..items = List.generate(
    _labels.length,
    (i) => VCTabItem()
      ..id = 100 + i
      ..text = _labels[i]
      ..showing = true,
  );

final _events = <(String, VEvent?)>[];

Future<void> _pump(WidgetTester tester, VCTabFolder value) async {
  await tester.pumpWidget(EvolveApp(
    theme: ThemeMode.light,
    contentWidget: Align(
      alignment: Alignment.topLeft,
      child: SizedBox(
        width: 600,
        height: 200,
        child: _CapturingCTabFolderSwt(
          value: value,
          onEvent: (ev, payload) => _events.add((ev, payload)),
        ),
      ),
    ),
  ));
  await _settle(tester);
}

Future<void> _settle(WidgetTester tester) async {
  await tester.pumpAndSettle();
  while (tester.takeException() != null) {}
}

Finder get _folderWidget => find.byWidgetPredicate((w) => w is CTabFolderSwt<VCTabFolder>);

/// The part of the strip the tabs are drawn in: the scroll view's viewport.
Rect _strip(WidgetTester tester) => tester.getRect(
    find.descendant(of: _folderWidget, matching: find.byType(SingleChildScrollView)).first);

Rect _tab(WidgetTester tester, String label) => tester.getRect(find
    .ancestor(
      of: find.descendant(of: _folderWidget, matching: find.text(label)),
      matching: find.byType(AnimatedContainer),
    )
    .first);

bool _shown(WidgetTester tester, String label) {
  final strip = _strip(tester);
  final tab = _tab(tester, label);
  return tab.left >= strip.left - 0.5 && tab.right <= strip.right + 0.5;
}

Finder get _chevron => find.byIcon(Icons.expand_more);

Finder _menuEntry(String label) => find.widgetWithText(PopupMenuItem<int>, label);

Iterable<VEvent?> _sent(String ev) => _events.where((e) => e.$1 == ev).map((e) => e.$2);

void main() {
  setUp(() {
    resetConfigFlags();
    _events.clear();
  });
  tearDown(resetConfigFlags);

  testWidgets('a folder that opens on a tab past the strip shows that tab',
      (WidgetTester tester) async {
    await _pump(tester, _folder(selection: 11));

    expect(_chevron, findsOneWidget, reason: 'sanity: twelve tabs overflow a 600 px strip');
    expect(_shown(tester, 'note10.txt'), isTrue,
        reason: 'the selected tab is scrolled into the strip, not left behind the chevron');
  });

  testWidgets('a tab Java selects is scrolled into the strip', (WidgetTester tester) async {
    await _pump(tester, _folder());
    expect(_shown(tester, 'note10.txt'), isFalse, reason: 'sanity: the last tab starts out of view');

    await deliverWhole(_folder(selection: 11, seq: 2));
    await _settle(tester);

    expect(_shown(tester, 'note10.txt'), isTrue);

    await deliverWhole(_folder(selection: 0, seq: 3));
    await _settle(tester);

    expect(_shown(tester, 'Hello.java'), isTrue,
        reason: 'selecting the first tab again scrolls back to it');
  });

  testWidgets('the chevron asks Java for the list, with its bounds in the folder',
      (WidgetTester tester) async {
    await _pump(tester, _folder());
    expect(_chevron, findsOneWidget, reason: 'sanity: the strip overflows');

    await tester.tap(_chevron);
    await _settle(tester);

    final requests = _sent('CTabFolder2/showList').toList();
    expect(requests, hasLength(1), reason: 'the chevron is CTabFolder2Listener.showList');
    final event = requests.single!;
    final folder = tester.getTopLeft(_folderWidget);
    final button = tester.getRect(
        find.ancestor(of: _chevron, matching: find.byType(GestureDetector)).first);
    expect(event.x, closeTo(button.left - folder.dx, 1));
    expect(event.y, closeTo(button.top - folder.dy, 1));
    expect(event.width, closeTo(button.width, 1));
    expect(event.height, closeTo(button.height, 1));

    expect(find.byType(PopupMenuItem<int>), findsNothing,
        reason: 'a listener may still take the event (doit=false), so nothing opens yet');
  });

  testWidgets('when no listener takes the event, the folder opens its own list',
      (WidgetTester tester) async {
    await _pump(tester, _folder());

    await tester.tap(_chevron);
    await _settle(tester);
    expect(find.byType(PopupMenuItem<int>), findsNothing);

    // Java ran the listeners, doit stayed true, and it asks for the default list.
    await deliverWhole(_folder(showListPopupSeq: 1, seq: 2));
    await _settle(tester);

    expect(_menuEntry('note10.txt'), findsOneWidget);
  });

  testWidgets('a tab picked from the list is scrolled into the strip',
      (WidgetTester tester) async {
    await _pump(tester, _folder());
    await tester.tap(_chevron);
    await _settle(tester);
    await deliverWhole(_folder(showListPopupSeq: 1, seq: 2));
    await _settle(tester);

    await tester.tap(_menuEntry('note10.txt'));
    await _settle(tester);

    expect(_sent('Selection/Selection').map((e) => e?.index), [11]);
    expect(_shown(tester, 'note10.txt'), isTrue);
  });
}
