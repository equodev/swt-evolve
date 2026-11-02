// LineEditor lays a text out a line at a time and only where it shows. What it draws must be what
// a multi-line TextField draws for the same text, width, scroll, caret and selection.

import 'dart:typed_data';
import 'dart:ui' as ui;

import 'package:flutter/material.dart';
import 'package:flutter/rendering.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:swtflutter/src/impl/utils/line_editor.dart';

const _style = TextStyle(fontSize: 13, color: Color(0xFF202020));
const _size = Size(240, 120);

Future<Uint8List> _pixels(WidgetTester tester, Key key) async {
  final boundary = tester.renderObject<RenderRepaintBoundary>(find.byKey(key));
  final image = await tester.runAsync(() => boundary.toImage(pixelRatio: 2));
  final bytes = await tester.runAsync(() => image!.toByteData(format: ui.ImageByteFormat.rawRgba));
  image!.dispose();
  return bytes!.buffer.asUint8List();
}

class _Pair {
  _Pair(String text, {TextSelection? selection, this.scroll = 0, this.focused = false, this.composing = TextRange.empty})
      : field = TextEditingController(text: text),
        editor = LineEditorController(text) {
    final sel = selection ?? TextSelection.collapsed(offset: text.length);
    field.selection = sel;
    editor.selection = sel;
  }

  final TextEditingController field;
  final LineEditorController editor;
  final FocusNode fieldFocus = FocusNode();
  final FocusNode editorFocus = FocusNode();
  final ScrollController fieldScroll = ScrollController();
  final double scroll;
  final bool focused;
  final TextRange composing;
}

Widget _host(_Pair pair) => MaterialApp(
      theme: ThemeData(platform: TargetPlatform.macOS),
      home: Material(
        child: Align(
          alignment: Alignment.topLeft,
          child: Column(children: [
            RepaintBoundary(
              key: const Key('field'),
              child: SizedBox.fromSize(
                size: _size,
                child: TextField(
                  controller: pair.field,
                  focusNode: pair.fieldFocus,
                  scrollController: pair.fieldScroll,
                  maxLines: null,
                  expands: true,
                  style: _style,
                  decoration: null,
                ),
              ),
            ),
            RepaintBoundary(
              key: const Key('editor'),
              child: SizedBox.fromSize(
                size: _size,
                // TextField draws its style over the theme's bodyLarge; the editor is handed that.
                child: Builder(
                  builder: (context) => LineEditor(
                    controller: pair.editor,
                    focusNode: pair.editorFocus,
                    style: Theme.of(context).textTheme.bodyLarge!.merge(_style),
                  ),
                ),
              ),
            ),
          ]),
        ),
      ),
    );

Future<void> _expectSame(WidgetTester tester, _Pair pair) async {
  await tester.pumpWidget(_host(pair));
  if (pair.scroll > 0) {
    pair.fieldScroll.jumpTo(pair.scroll);
    final editorScroll = tester.state<ScrollableState>(
        find.descendant(of: find.byType(LineEditor), matching: find.byType(Scrollable)));
    editorScroll.position.jumpTo(pair.scroll);
  }
  Uint8List field, editor;
  if (pair.focused) {
    // One focus at a time: each is drawn while it has it, the caret at the start of its blink.
    final selection = pair.field.selection;
    pair.fieldFocus.requestFocus();
    await tester.pump();
    pair.field.value = pair.field.value.copyWith(selection: selection, composing: pair.composing);
    await tester.pump();
    field = await _pixels(tester, const Key('field'));
    pair.editorFocus.requestFocus();
    await tester.pump();
    pair.editor.value = pair.editor.value.copyWith(selection: selection, composing: pair.composing);
    await tester.pump();
    editor = await _pixels(tester, const Key('editor'));
  } else {
    await tester.pump();
    field = await _pixels(tester, const Key('field'));
    editor = await _pixels(tester, const Key('editor'));
  }
  var differ = 0;
  for (var i = 0; i < field.length; i++) {
    if (field[i] != editor[i]) differ++;
  }
  expect(differ, 0, reason: 'bytes that differ between TextField and LineEditor');
}

final _mac = TargetPlatformVariant.only(TargetPlatform.macOS);

void main() {
  final long = List.generate(40, (i) => 'line $i of the text, which goes on for a while').join('\n');

  testWidgets('short lines', (tester) async {
    await _expectSame(tester, _Pair('one\ntwo\nthree'));
  }, variant: _mac);

  testWidgets('a line that wraps', (tester) async {
    await _expectSame(tester, _Pair('${'word ' * 40}\nafter'));
  }, variant: _mac);

  testWidgets('empty lines and a trailing newline', (tester) async {
    await _expectSame(tester, _Pair('a\n\n\nb\n'));
  }, variant: _mac);

  testWidgets('tabs and characters outside ASCII', (tester) async {
    await _expectSame(tester, _Pair('a\tb\tc\nünïcødé ✓ 中文\n'));
  }, variant: _mac);

  testWidgets('scrolled', (tester) async {
    await _expectSame(tester, _Pair(long, scroll: 137));
  }, variant: _mac);

  testWidgets('the caret, focused', (tester) async {
    await _expectSame(tester, _Pair('one\ntwo\nthree', selection: const TextSelection.collapsed(offset: 6), focused: true));
  }, variant: _mac);

  testWidgets('a selection across lines, focused', (tester) async {
    await _expectSame(tester,
        _Pair('first line\nsecond line\nthird', selection: const TextSelection(baseOffset: 3, extentOffset: 18), focused: true));
  }, variant: _mac);

  const mixed = 'first line\nsecond line\nthird\n\nfifth word word word word word word word word word word\nlast';

  testWidgets('a selection through a wrapped line', (tester) async {
    await _expectSame(tester, _Pair(mixed, selection: const TextSelection(baseOffset: 40, extentOffset: 80), focused: true));
  }, variant: _mac);

  testWidgets('a selection over an empty line', (tester) async {
    await _expectSame(tester, _Pair(mixed, selection: const TextSelection(baseOffset: 25, extentOffset: 36), focused: true));
  }, variant: _mac);

  testWidgets('a selection that ends at a line end', (tester) async {
    await _expectSame(tester, _Pair(mixed, selection: const TextSelection(baseOffset: 2, extentOffset: 10), focused: true));
  }, variant: _mac);

  testWidgets('a selection over the trailing newline', (tester) async {
    await _expectSame(tester, _Pair('one\ntwo\n', selection: const TextSelection(baseOffset: 1, extentOffset: 8), focused: true));
  }, variant: _mac);

  testWidgets('everything selected, scrolled', (tester) async {
    await _expectSame(tester, _Pair(long, selection: TextSelection(baseOffset: 0, extentOffset: long.length), scroll: 211, focused: true));
  }, variant: _mac);

  testWidgets('a composing range across a wrapped line, underlined', (tester) async {
    await _expectSame(tester, _Pair(mixed, selection: const TextSelection.collapsed(offset: 60), composing: const TextRange(start: 45, end: 70), focused: true));
  }, variant: _mac);

  testWidgets('the caret on the last line, scrolled', (tester) async {
    await _expectSame(tester, _Pair(long, selection: TextSelection.collapsed(offset: long.length - 3), scroll: 300, focused: true));
  }, variant: _mac);
}
