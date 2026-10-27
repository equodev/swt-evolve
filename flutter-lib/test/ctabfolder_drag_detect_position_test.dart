// The workbench reads DragDetect's x/y to work out which tab a drag picked up: it maps the cursor
// into the folder's own coordinates and asks CTabFolder.getItem(point). So the event has to carry
// the tab where it is DRAWN.
//
// Measured on a running workbench: with the strip scrolled, that first question arrived with the
// tab's unscrolled position - out past the folder's own width - found no tab there, and the
// workbench dragged the whole stack, carrying every view in it.

import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/gen/ctabfolder.dart';
import 'package:swtflutter/src/gen/ctabitem.dart';
import 'package:swtflutter/src/gen/event.dart';
import 'package:swtflutter/src/gen/swt.dart';

class _CapturingCTabFolderSwt extends CTabFolderSwt<VCTabFolder> {
  const _CapturingCTabFolderSwt({required super.value, required this.onEvent});

  final void Function(String ev, VEvent? payload) onEvent;

  @override
  void sendEvent(VCTabFolder val, String ev, VEvent? payload) => onEvent(ev, payload);
}

const _labels = [
  'Section 3 (TVDSS)',
  'Data Operations',
  'Stratigraphic Modeling',
  'Facies Trend Modeling',
  'Interpretation Set',
];

VCTabFolder _folder() => VCTabFolder()
  ..id = 1
  ..style = SWT.NONE
  ..enabled = true
  ..selection = 0
  ..dragSource = true
  ..items = List.generate(
    _labels.length,
    (i) => VCTabItem()
      ..id = 100 + i
      ..text = _labels[i]
      ..showing = true,
  );

void main() {
  testWidgets('a drag on a scrolled strip reports the tab where it is drawn',
      (WidgetTester tester) async {
    final dragDetects = <VEvent>[];

    await tester.pumpWidget(EvolveApp(
      theme: ThemeMode.light,
      contentWidget: SizedBox(
        // Too narrow for the five tabs, so the strip scrolls.
        width: 300,
        height: 240,
        child: _CapturingCTabFolderSwt(
          value: _folder(),
          onEvent: (ev, payload) {
            if (ev.endsWith('DragDetect') && payload != null) dragDetects.add(payload);
          },
        ),
      ),
    ));
    await tester.pumpAndSettle();
    while (tester.takeException() != null) {}

    tester.state<ScrollableState>(find.byType(Scrollable).first).position.jumpTo(150);
    await tester.pumpAndSettle();

    // The second tab, wherever the scroll has left it.
    final tab = find
        .ancestor(of: find.text(_labels[1]), matching: find.byType(AnimatedContainer))
        .first;
    final drawn = tester.getRect(tab);
    final folder = tester.getRect(
        find.byWidgetPredicate((w) => w is CTabFolderSwt<VCTabFolder>));

    final start = tester.getCenter(find.text(_labels[1]));
    final gesture = await tester.startGesture(start);
    await tester.pump(const Duration(milliseconds: 50));
    for (var i = 1; i <= 8; i++) {
      await gesture.moveTo(Offset.lerp(start, start + const Offset(0, 140), i / 8)!);
      await tester.pump(const Duration(milliseconds: 20));
    }

    expect(dragDetects, isNotEmpty, reason: 'the workbench is told a drag started on a tab');
    final reported = dragDetects.last;
    expect(reported.x, greaterThanOrEqualTo((drawn.left - folder.left).round()));
    expect(reported.x, lessThanOrEqualTo((drawn.right - folder.left).round()),
        reason: 'inside the tab where it is drawn, not where it would be without the scroll');
    expect(reported.x, lessThan(folder.width.round()),
        reason: 'a point past the folder itself finds no tab, and the whole stack is dragged');

    await gesture.up();
    await tester.pumpAndSettle();
    await tester.pump(const Duration(milliseconds: 400));
    while (tester.takeException() != null) {}
  });

  testWidgets('a sideways drag on a drawn tab is a drag, not a scroll of the strip',
      (WidgetTester tester) async {
    final dragDetects = <VEvent>[];

    await tester.pumpWidget(EvolveApp(
      theme: ThemeMode.light,
      contentWidget: SizedBox(
        width: 300,
        height: 240,
        child: _CapturingCTabFolderSwt(
          value: _folder(),
          onEvent: (ev, payload) {
            if (ev.endsWith('DragDetect') && payload != null) dragDetects.add(payload);
          },
        ),
      ),
    ));
    await tester.pumpAndSettle();
    while (tester.takeException() != null) {}

    // Scrolled off the start, so the strip has somewhere to go in both directions - which is when
    // a sideways drag is one the strip could take for itself.
    tester.state<ScrollableState>(find.byType(Scrollable).first).position.jumpTo(120);
    await tester.pumpAndSettle();

    // Pressed on a tab that is actually drawn: with the strip scrolled, several are not.
    final folder = tester.getRect(
        find.byWidgetPredicate((w) => w is CTabFolderSwt<VCTabFolder>));
    Rect? drawn;
    for (final label in _labels) {
      final finder = find
          .ancestor(of: find.text(label), matching: find.byType(AnimatedContainer))
          .first;
      if (finder.evaluate().isEmpty) continue;
      final rect = tester.getRect(finder).intersect(folder);
      if (rect.width > 40) {
        drawn = rect;
        break;
      }
    }
    expect(drawn, isNotNull, reason: 'the premise: some tab is drawn wide enough to press on');

    final start = drawn!.center;
    final gesture = await tester.startGesture(start);
    await tester.pump(const Duration(milliseconds: 50));
    for (var i = 1; i <= 8; i++) {
      await gesture.moveTo(Offset.lerp(start, start - const Offset(200, 0), i / 8)!);
      await tester.pump(const Duration(milliseconds: 20));
    }

    expect(dragDetects, isNotEmpty,
        reason: 'a view is moved to another stack by dragging its tab sideways, and the strip it '
            'sits in scrolls that way too - the tab has to win that gesture');

    await gesture.up();
    await tester.pumpAndSettle();
    await tester.pump(const Duration(milliseconds: 400));
    while (tester.takeException() != null) {}
  });

  testWidgets('a tab half scrolled out reports a point inside what is drawn of it',
      (WidgetTester tester) async {
    final dragDetects = <VEvent>[];

    await tester.pumpWidget(EvolveApp(
      theme: ThemeMode.light,
      contentWidget: SizedBox(
        width: 300,
        height: 240,
        child: _CapturingCTabFolderSwt(
          value: _folder(),
          onEvent: (ev, payload) {
            if (ev.endsWith('DragDetect') && payload != null) dragDetects.add(payload);
          },
        ),
      ),
    ));
    await tester.pumpAndSettle();
    while (tester.takeException() != null) {}

    final firstTab = find
        .ancestor(of: find.text(_labels.first), matching: find.byType(AnimatedContainer))
        .first;
    // Scrolled so most of the first tab is off the left edge, leaving a sliver drawn.
    final scrolled = tester.getRect(firstTab).width - 20;
    tester.state<ScrollableState>(find.byType(Scrollable).first).position.jumpTo(scrolled);
    await tester.pumpAndSettle();

    final folder = tester.getRect(
        find.byWidgetPredicate((w) => w is CTabFolderSwt<VCTabFolder>));
    expect(tester.getRect(firstTab).center.dx - folder.left, lessThan(0),
        reason: 'the premise: the tab is drawn, but its own centre is outside the folder');

    final start = tester.getRect(firstTab).centerRight - const Offset(8, 0);
    final gesture = await tester.startGesture(start);
    await tester.pump(const Duration(milliseconds: 50));
    for (var i = 1; i <= 8; i++) {
      await gesture.moveTo(Offset.lerp(start, start + const Offset(0, 140), i / 8)!);
      await tester.pump(const Duration(milliseconds: 20));
    }

    expect(dragDetects, isNotEmpty);
    expect(dragDetects.last.x, greaterThanOrEqualTo(0),
        reason: 'a point outside the folder finds no tab, and the whole stack is dragged instead');

    await gesture.up();
    await tester.pumpAndSettle();
    await tester.pump(const Duration(milliseconds: 400));
    while (tester.takeException() != null) {}
  });
}
