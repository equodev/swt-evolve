// The same keys and typing given to a multi-line TextField and to a LineEditor leave them with the
// same text and selection.

import 'dart:math' as math;

import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:swtflutter/src/impl/utils/line_editor.dart';

const _style = TextStyle(fontSize: 13, color: Color(0xFF202020));
const _size = Size(240, 120);

final _mac = TargetPlatformVariant.only(TargetPlatform.macOS);

const _text = 'first line here\nsecond line, with words\n\nfourth word word word word word word word word word word word\nlast';

typedef _Step = Future<void> Function(WidgetTester tester);

_Step _key(LogicalKeyboardKey key, {bool shift = false, bool alt = false, bool meta = false}) => (tester) async {
      if (shift) await tester.sendKeyDownEvent(LogicalKeyboardKey.shiftLeft);
      if (alt) await tester.sendKeyDownEvent(LogicalKeyboardKey.altLeft);
      if (meta) await tester.sendKeyDownEvent(LogicalKeyboardKey.metaLeft);
      await tester.sendKeyEvent(key);
      if (meta) await tester.sendKeyUpEvent(LogicalKeyboardKey.metaLeft);
      if (alt) await tester.sendKeyUpEvent(LogicalKeyboardKey.altLeft);
      if (shift) await tester.sendKeyUpEvent(LogicalKeyboardKey.shiftLeft);
      await tester.pump();
    };

/// Typing, as the input method reports it: each character replaces the selection it was told of.
_Step _type(String chars) => (tester) async {
      for (final ch in chars.split('')) {
        final state = tester.testTextInput.editingState!;
        final text = state['text'] as String;
        final base = state['selectionBase'] as int, extent = state['selectionExtent'] as int;
        final start = math.min(base, extent), end = math.max(base, extent);
        tester.testTextInput.updateEditingValue(TextEditingValue(
          text: text.replaceRange(start, end, ch),
          selection: TextSelection.collapsed(offset: start + ch.length),
        ));
        await tester.pump();
      }
    };

_Step _wait(int milliseconds) => (tester) => tester.pump(Duration(milliseconds: milliseconds));

/// A clipboard for the platform channel, as the engine keeps one.
void _mockClipboard(WidgetTester tester) {
  String? held;
  tester.binding.defaultBinaryMessenger.setMockMethodCallHandler(SystemChannels.platform, (call) async {
    switch (call.method) {
      case 'Clipboard.setData':
        held = (call.arguments as Map)['text'] as String?;
        return null;
      case 'Clipboard.getData':
        return held == null ? null : <String, dynamic>{'text': held};
      case 'Clipboard.hasStrings':
        return <String, dynamic>{'value': held != null};
    }
    return null;
  });
}

Future<(String, TextSelection, double)> _run(WidgetTester tester, bool editor, TextSelection start, List<_Step> steps) async {
  final field = TextEditingController(text: _text);
  final lines = LineEditorController(_text);
  final focus = FocusNode();
  await tester.pumpWidget(MaterialApp(
    theme: ThemeData(platform: TargetPlatform.macOS),
    home: Material(
      child: Align(
        alignment: Alignment.topLeft,
        child: SizedBox.fromSize(
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
  for (final step in steps) {
    await step(tester);
  }
  final scrolled = tester.state<ScrollableState>(find.byType(Scrollable).first).position.pixels;
  final result = editor ? (lines.text, lines.selection, scrolled) : (field.text, field.selection, scrolled);
  await tester.pumpWidget(const SizedBox());
  await tester.pump(const Duration(seconds: 1));
  focus.dispose();
  return result;
}

Future<void> _same(WidgetTester tester, TextSelection start, List<_Step> steps) async {
  final field = await _run(tester, false, start, steps);
  final editor = await _run(tester, true, start, steps);
  expect(editor.$1, field.$1, reason: 'text');
  expect((editor.$2.baseOffset, editor.$2.extentOffset), (field.$2.baseOffset, field.$2.extentOffset), reason: 'selection');
  expect(editor.$3, closeTo(field.$3, 0.01), reason: 'scroll offset');
}

const _mid = TextSelection.collapsed(offset: 22);

void main() {
  testWidgets('arrows move by character and by row', (tester) async {
    await _same(tester, _mid, [
      _key(LogicalKeyboardKey.arrowLeft),
      _key(LogicalKeyboardKey.arrowLeft),
      _key(LogicalKeyboardKey.arrowRight),
      _key(LogicalKeyboardKey.arrowDown),
      _key(LogicalKeyboardKey.arrowDown),
      _key(LogicalKeyboardKey.arrowDown),
      _key(LogicalKeyboardKey.arrowUp),
    ]);
  }, variant: _mac);

  testWidgets('down and up keep the column across a wrapped line', (tester) async {
    await _same(tester, const TextSelection.collapsed(offset: 30), [
      for (var i = 0; i < 5; i++) _key(LogicalKeyboardKey.arrowDown),
      for (var i = 0; i < 3; i++) _key(LogicalKeyboardKey.arrowUp),
    ]);
  }, variant: _mac);

  testWidgets('shift extends', (tester) async {
    await _same(tester, _mid, [
      _key(LogicalKeyboardKey.arrowRight, shift: true),
      _key(LogicalKeyboardKey.arrowRight, shift: true),
      _key(LogicalKeyboardKey.arrowDown, shift: true),
      _key(LogicalKeyboardKey.arrowLeft, shift: true),
    ]);
  }, variant: _mac);

  testWidgets('option moves by word', (tester) async {
    await _same(tester, _mid, [
      _key(LogicalKeyboardKey.arrowRight, alt: true),
      _key(LogicalKeyboardKey.arrowRight, alt: true),
      _key(LogicalKeyboardKey.arrowRight, alt: true),
      _key(LogicalKeyboardKey.arrowRight, alt: true),
      _key(LogicalKeyboardKey.arrowLeft, alt: true),
      _key(LogicalKeyboardKey.arrowLeft, alt: true, shift: true),
    ]);
  }, variant: _mac);

  testWidgets('option moves by word over line breaks', (tester) async {
    await _same(tester, const TextSelection.collapsed(offset: 15), [
      _key(LogicalKeyboardKey.arrowRight, alt: true),
      _key(LogicalKeyboardKey.arrowLeft, alt: true),
      _key(LogicalKeyboardKey.arrowLeft, alt: true),
    ]);
    await _same(tester, const TextSelection.collapsed(offset: 40), [
      _key(LogicalKeyboardKey.arrowRight, alt: true),
      _key(LogicalKeyboardKey.arrowRight, alt: true),
      _key(LogicalKeyboardKey.arrowLeft, alt: true),
      _key(LogicalKeyboardKey.arrowLeft, alt: true),
    ]);
  }, variant: _mac);

  testWidgets('command moves to the row ends and the text ends', (tester) async {
    await _same(tester, _mid, [_key(LogicalKeyboardKey.arrowRight, meta: true)]);
    await _same(tester, _mid, [_key(LogicalKeyboardKey.arrowLeft, meta: true)]);
    await _same(tester, const TextSelection.collapsed(offset: 50), [_key(LogicalKeyboardKey.arrowRight, meta: true)]);
    await _same(tester, _mid, [_key(LogicalKeyboardKey.arrowDown, meta: true)]);
    await _same(tester, _mid, [_key(LogicalKeyboardKey.arrowUp, meta: true, shift: true)]);
  }, variant: _mac);

  testWidgets('deleting', (tester) async {
    await _same(tester, _mid, [_key(LogicalKeyboardKey.backspace), _key(LogicalKeyboardKey.delete)]);
    await _same(tester, _mid, [_key(LogicalKeyboardKey.backspace, alt: true)]);
    await _same(tester, _mid, [_key(LogicalKeyboardKey.backspace, meta: true)]);
    await _same(tester, const TextSelection.collapsed(offset: 16), [_key(LogicalKeyboardKey.backspace)]);
    await _same(tester, const TextSelection(baseOffset: 5, extentOffset: 30), [_key(LogicalKeyboardKey.backspace)]);
  }, variant: _mac);

  testWidgets('typing, a new line, and replacing a selection', (tester) async {
    await _same(tester, _mid, [_type('abc'), _type('\n'), _type('d')]);
    await _same(tester, const TextSelection(baseOffset: 5, extentOffset: 30), [_type('xy')]);
  }, variant: _mac);

  testWidgets('page keys', (tester) async {
    await _same(tester, _mid, [_key(LogicalKeyboardKey.pageDown), _wait(300)]);
    await _same(tester, _mid, [_key(LogicalKeyboardKey.pageDown, shift: true), _wait(300)]);
    await _same(tester, const TextSelection.collapsed(offset: 100), [_key(LogicalKeyboardKey.pageUp, shift: true), _wait(300)]);
    await _same(tester, _mid, [_key(LogicalKeyboardKey.end), _wait(300), _key(LogicalKeyboardKey.home), _wait(300)]);
  }, variant: _mac);

  testWidgets('copy, cut and paste', (tester) async {
    _mockClipboard(tester);
    await _same(tester, const TextSelection(baseOffset: 5, extentOffset: 30), [
      _key(LogicalKeyboardKey.keyC, meta: true),
      _key(LogicalKeyboardKey.arrowRight),
      _key(LogicalKeyboardKey.keyV, meta: true),
      _wait(100),
    ]);
    await _same(tester, const TextSelection(baseOffset: 5, extentOffset: 30), [
      _key(LogicalKeyboardKey.keyX, meta: true),
      _wait(100),
      _key(LogicalKeyboardKey.arrowDown),
      _key(LogicalKeyboardKey.keyV, meta: true),
      _wait(100),
    ]);
  }, variant: _mac);

  testWidgets('select all, then type', (tester) async {
    await _same(tester, _mid, [_key(LogicalKeyboardKey.keyA, meta: true), _type('z')]);
  }, variant: _mac);
}
