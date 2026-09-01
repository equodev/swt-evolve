// A secondary (right) click must not destroy the selection the context menu is about to act on.
//
// SWT's handleMouseDown returns before any selection or caret work unless event.button == 1,
// so a secondary click leaves both untouched wherever it lands -- inside the selection or
// outside it. (handleMenuDetect only relocates a keyboard-triggered menu; handleMouseUp acts
// on button 1 only.) Our pointer handlers were wired without a button filter, so a right-click
// ran the primary path and collapsed the selection on both sides of the bridge before the
// context menu was built.

import 'package:flutter/gestures.dart';
import 'package:flutter/foundation.dart';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:flutter/widgets.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/gen/event.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/styledtext.dart';
import 'package:swtflutter/src/impl/gcdrawer_evolve.dart' show ScenePainter;
import 'package:swtflutter/src/impl/styledtext_evolve.dart';

const double _kWidth = 300;
const double _kHeight = 120;

/// The generated StyledTextSwt with sendEvent captured instead of handed to EquoCommService.
class _CapturingStyledTextSwt extends StyledTextSwt<VStyledText> {
  const _CapturingStyledTextSwt({required super.value, required this.onEvent});

  final void Function(String ev, VEvent? payload) onEvent;

  @override
  void sendEvent(VStyledText val, String ev, VEvent? payload) => onEvent(ev, payload);
}

void main() {
  VStyledText value() => VStyledText()
    ..swt = 'StyledText'
    ..id = 1
    ..style = 0
    ..enabled = true
    ..editable = true
    ..caretOffset = 0
    ..wordWrap = false
    ..text = 'alpha beta\ngamma delta\nepsilon'
    ..bounds = (VRectangle()
      ..x = 0
      ..y = 0
      ..width = _kWidth.toInt()
      ..height = _kHeight.toInt());

  late List<VEvent?> selectionEvents;

  Widget appWith(VStyledText v) => EvolveApp(
        theme: ThemeMode.light,
        contentWidget: SizedBox(
          width: _kWidth,
          height: _kHeight,
          child: _CapturingStyledTextSwt(
            value: v,
            onEvent: (ev, payload) {
              if (ev == 'Selection/Selection') selectionEvents.add(payload);
            },
          ),
        ),
      );

  Finder editor() => find.byType(_CapturingStyledTextSwt);

  List<TextShape> paintedText(WidgetTester tester) => tester
      .widgetList<CustomPaint>(
        find.descendant(of: editor(), matching: find.byType(CustomPaint)),
      )
      .map((w) => w.painter)
      .whereType<ScenePainter>()
      .expand((p) => p.shapes)
      .whereType<TextShape>()
      .toList();

  SelectionInfo? liveSelection(WidgetTester tester) => paintedText(tester)
      .map((s) => s.selectionInfo)
      .firstWhere((s) => s?.hasSelection == true, orElse: () => null);

  int? liveCaret(WidgetTester tester) => paintedText(tester)
      .map((s) => s.caretInfo?.offset)
      .firstWhere((o) => o != null, orElse: () => null);

  Future<void> pumpEditor(WidgetTester tester) async {
    selectionEvents = <VEvent?>[];
    await tester.pumpWidget(appWith(value()));
    // initState doesn't run extraSetState; the second push is what builds the text shape.
    await tester.pumpWidget(appWith(value()));
  }

  /// Drag across the first line to select part of it.
  Future<void> dragSelect(WidgetTester tester) async {
    final origin = tester.getTopLeft(editor());
    final gesture = await tester.startGesture(origin + const Offset(6, 4));
    await gesture.moveTo(origin + const Offset(60, 4));
    await tester.pump();
    await gesture.up();
    await tester.pump();
  }

  Future<void> secondaryClickAt(WidgetTester tester, Offset local) async {
    final origin = tester.getTopLeft(editor());
    final gesture =
        await tester.startGesture(origin + local, buttons: kSecondaryButton);
    await gesture.up();
    await tester.pump();
  }

  testWidgets('a right-click inside the selection keeps it', (tester) async {
    await pumpEditor(tester);
    await dragSelect(tester);

    final before = liveSelection(tester);
    expect(before, isNotNull,
        reason: 'sanity: dragging must select text before the right-click');
    final beforeRange = (before!.normalizedStart, before.normalizedEnd);

    selectionEvents.clear();
    // Land inside the dragged range, which spans roughly x=6..60 on the first line.
    await secondaryClickAt(tester, const Offset(30, 4));

    final after = liveSelection(tester);
    expect(after, isNotNull,
        reason: 'the context menu acts on this selection; it must survive the right-click');
    expect((after!.normalizedStart, after.normalizedEnd), beforeRange,
        reason: 'a secondary click inside the selection must not change it');
  });

  testWidgets('a right-click inside the selection pushes no collapse to Java',
      (tester) async {
    await pumpEditor(tester);
    await dragSelect(tester);

    selectionEvents.clear();
    await secondaryClickAt(tester, const Offset(30, 4));

    final collapses = selectionEvents
        .where((e) => e != null && e.start == e.end)
        .toList();
    expect(collapses, isEmpty,
        reason: "Java's own selection collapses too if the cleared push is sent");
  });

  testWidgets('a right-click outside the selection leaves selection and caret untouched',
      (tester) async {
    await pumpEditor(tester);
    await dragSelect(tester);

    final before = liveSelection(tester);
    expect(before, isNotNull, reason: 'sanity: the drag must select first');
    final beforeRange = (before!.normalizedStart, before.normalizedEnd);
    final beforeCaret = liveCaret(tester);

    selectionEvents.clear();
    // Third line, well clear of the dragged range on line one.
    await secondaryClickAt(tester, const Offset(20, 40));

    final after = liveSelection(tester);
    expect(after, isNotNull,
        reason: 'SWT ignores a non-primary button entirely, so the selection survives here too');
    expect((after!.normalizedStart, after.normalizedEnd), beforeRange);
    expect(liveCaret(tester), beforeCaret,
        reason: 'handleMouseDown returns before doMouseLocationChange for button != 1');
    expect(selectionEvents, isEmpty,
        reason: 'nothing about the selection changed, so nothing is pushed to Java');
  });

  testWidgets('macOS Ctrl+Click keeps the selection too', (tester) async {
    debugDefaultTargetPlatformOverride = TargetPlatform.macOS;
    await pumpEditor(tester);
    await dragSelect(tester);

    final before = liveSelection(tester);
    expect(before, isNotNull, reason: 'sanity: the drag must select first');
    final beforeRange = (before!.normalizedStart, before.normalizedEnd);

    selectionEvents.clear();
    // A primary click with Control held: the macOS context-menu gesture, and the case
    // SwtStyledText spells out as IS_MAC && (stateMask & SWT.MOD4).
    await tester.sendKeyDownEvent(LogicalKeyboardKey.controlLeft);
    final origin = tester.getTopLeft(editor());
    final gesture = await tester.startGesture(origin + const Offset(30, 4));
    await gesture.up();
    await tester.pump();
    await tester.sendKeyUpEvent(LogicalKeyboardKey.controlLeft);

    final after = liveSelection(tester);
    expect(after, isNotNull, reason: 'Ctrl+Click on macOS must not collapse the selection');
    expect((after!.normalizedStart, after.normalizedEnd), beforeRange);
    expect(selectionEvents, isEmpty);

    // Must be cleared inside the test body: the binding asserts on it before tearDown runs.
    debugDefaultTargetPlatformOverride = null;
  });
}
