// The same mouse clicks and drags given to a multi-line TextField and to a LineEditor select the
// same text. A secondary click is the exception: Text builds its TextField without Flutter's toolbar
// and puts back the caret the click moved, so the LineEditor opens no menu and moves nothing.

import 'package:flutter/gestures.dart';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:swtflutter/src/impl/utils/line_editor.dart';

const _text = 'first line here\nsecond line, with words\n\nfourth word word word word word word word word word word word\nlast';
const _style = TextStyle(fontSize: 13, color: Color(0xFF202020));
const _size = Size(240, 120);

final _mac = TargetPlatformVariant.only(TargetPlatform.macOS);

typedef _Gesture = Future<void> Function(WidgetTester tester, Offset origin);

_Gesture _clicks(Offset at, int count, {bool shift = false}) => (tester, origin) async {
      if (shift) await tester.sendKeyDownEvent(LogicalKeyboardKey.shiftLeft);
      for (var i = 0; i < count; i++) {
        final gesture = await tester.startGesture(origin + at, kind: PointerDeviceKind.mouse);
        await tester.pump(const Duration(milliseconds: 20));
        await gesture.up();
        await tester.pump(const Duration(milliseconds: 50));
      }
      if (shift) await tester.sendKeyUpEvent(LogicalKeyboardKey.shiftLeft);
      await tester.pump(const Duration(milliseconds: 600));
    };

/// A secondary click, and what any menu it opens offers.
_Gesture _secondaryClick(Offset at, List<String> menu) => (tester, origin) async {
      final gesture = await tester.startGesture(origin + at, kind: PointerDeviceKind.mouse, buttons: kSecondaryButton);
      await tester.pump(const Duration(milliseconds: 20));
      await gesture.up();
      await tester.pump(const Duration(milliseconds: 600));
      for (final label in const ['Cut', 'Copy', 'Paste', 'Select All', 'Look Up', 'Search Web', 'Share']) {
        if (find.text(label).evaluate().isNotEmpty) menu.add(label);
      }
    };

_Gesture _drag(Offset from, Offset to) => (tester, origin) async {
      final gesture = await tester.startGesture(origin + from, kind: PointerDeviceKind.mouse);
      await tester.pump(const Duration(milliseconds: 20));
      for (var i = 1; i <= 5; i++) {
        await gesture.moveTo(origin + Offset.lerp(from, to, i / 5)!);
        await tester.pump(const Duration(milliseconds: 20));
      }
      await gesture.up();
      await tester.pump(const Duration(milliseconds: 600));
    };

Future<(TextSelection, double)> _run(WidgetTester tester, bool editor, TextSelection start, List<_Gesture> gestures) async {
  final field = TextEditingController(text: _text);
  final lines = LineEditorController(_text);
  final focus = FocusNode();
  const key = Key('box');
  await tester.pumpWidget(MaterialApp(
    theme: ThemeData(platform: TargetPlatform.macOS),
    home: Material(
      child: Align(
        alignment: Alignment.topLeft,
        child: SizedBox.fromSize(
          key: key,
          size: _size,
          child: editor
              ? Builder(
                  builder: (context) => LineEditor(
                    controller: lines,
                    focusNode: focus,
                    style: Theme.of(context).textTheme.bodyLarge!.merge(_style),
                  ),
                )
              : TextField(controller: field, focusNode: focus, maxLines: null, expands: true, style: _style, decoration: null),
        ),
      ),
    ),
  ));
  focus.requestFocus();
  await tester.pump();
  if (editor) {
    lines.selection = start;
  } else {
    field.selection = start;
  }
  await tester.pump();
  final origin = tester.getTopLeft(find.byKey(key));
  for (final g in gestures) {
    await g(tester, origin);
  }
  final scrolled = tester.state<ScrollableState>(find.byType(Scrollable).first).position.pixels;
  final result = (editor ? lines.selection : field.selection, scrolled);
  await tester.pumpWidget(const SizedBox());
  await tester.pump(const Duration(seconds: 1));
  focus.dispose();
  return result;
}

Future<String?> _differs(WidgetTester tester, List<_Gesture> gestures, {TextSelection start = const TextSelection.collapsed(offset: 0)}) async {
  final (field, fieldScroll) = await _run(tester, false, start, gestures);
  final (editor, editorScroll) = await _run(tester, true, start, gestures);
  if ((editor.baseOffset, editor.extentOffset) == (field.baseOffset, field.extentOffset) &&
      (editorScroll - fieldScroll).abs() < 0.01) {
    return null;
  }
  return 'TextField (${field.baseOffset}, ${field.extentOffset}) scrolled $fieldScroll, '
      'LineEditor (${editor.baseOffset}, ${editor.extentOffset}) scrolled $editorScroll';
}

Future<void> _same(WidgetTester tester, List<_Gesture> gestures, {TextSelection start = const TextSelection.collapsed(offset: 0)}) async {
  expect(await _differs(tester, gestures, start: start), isNull);
}

Future<void> _sameAtEveryPoint(WidgetTester tester, _Gesture Function(Offset p) gesture,
    {TextSelection start = const TextSelection.collapsed(offset: 0), List<Offset> points = _points}) async {
  final differ = <String>[];
  for (final p in points) {
    final d = await _differs(tester, [gesture(p)], start: start);
    if (d != null) differ.add('at $p: $d');
  }
  expect(differ, isEmpty);
}

const _points = [Offset(30, 8), Offset(70, 27), Offset(5, 47), Offset(200, 47), Offset(120, 66), Offset(60, 86), Offset(230, 110)];

/// [_points] but the last: at the bottom edge of its view, where a click scrolls the caret into
/// view, a TextField's double and triple clicks select nothing.
const _multiClickPoints = [Offset(30, 8), Offset(70, 27), Offset(5, 47), Offset(200, 47), Offset(120, 66), Offset(60, 86)];

void main() {
  testWidgets('a click places the caret', (tester) async {
    await _sameAtEveryPoint(tester, (p) => _clicks(p, 1));
  }, variant: _mac);

  testWidgets('shift and a click extend the selection', (tester) async {
    await _sameAtEveryPoint(tester, (p) => _clicks(p, 1, shift: true), start: const TextSelection.collapsed(offset: 20));
  }, variant: _mac);

  testWidgets('a double click selects a word', (tester) async {
    await _sameAtEveryPoint(tester, (p) => _clicks(p, 2), points: _multiClickPoints);
  }, variant: _mac);

  testWidgets('a triple click selects a line', (tester) async {
    await _sameAtEveryPoint(tester, (p) => _clicks(p, 3), points: _multiClickPoints);
  }, variant: _mac);

  testWidgets('a secondary click opens no menu of its own and leaves the selection', (tester) async {
    for (final start in const [TextSelection.collapsed(offset: 0), TextSelection(baseOffset: 16, extentOffset: 30)]) {
      for (final p in const [Offset(30, 8), Offset(70, 27), Offset(200, 47)]) {
        final menu = <String>[];
        final (editor, _) = await _run(tester, true, start, [_secondaryClick(p, menu)]);
        expect((editor.baseOffset, editor.extentOffset), (start.baseOffset, start.extentOffset), reason: 'selection at $p from $start');
        expect(menu, isEmpty, reason: 'menu at $p from $start');
      }
    }
  }, variant: _mac);

  testWidgets('a drag selects what it passes', (tester) async {
    await _same(tester, [_drag(const Offset(30, 8), const Offset(120, 66))]);
    await _same(tester, [_drag(const Offset(200, 66), const Offset(10, 27))]);
  }, variant: _mac);
}
