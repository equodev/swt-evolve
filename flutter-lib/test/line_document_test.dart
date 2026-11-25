import 'dart:math';

import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:swtflutter/src/impl/utils/line_document.dart';
import 'package:swtflutter/src/impl/utils/line_editor.dart';

void main() {
  // With `\r` too, an edit can make or unmake a `\r\n` at either of its ends.
  for (final alphabet in ['ab \n\n', 'ab \r\n\r']) {
    test('an edited document has the lines of its text, and says which it replaced '
        '(${alphabet.replaceAll('\r', r'\r').replaceAll('\n', r'\n')})', () {
      final random = Random(3);
      String randomText(int n) =>
          String.fromCharCodes([for (var i = 0; i < n; i++) alphabet.codeUnitAt(random.nextInt(alphabet.length))]);
      var doc = LineDocument(randomText(40));
      for (var step = 0; step < 2000; step++) {
        final start = random.nextInt(doc.length + 1);
        final end = start + random.nextInt(doc.length - start + 1);
        final insert = randomText(random.nextInt(8));
        final edited = doc.replaced(start, end, insert);
        final fresh = LineDocument(edited.text);
        expect(edited.text, doc.text.replaceRange(start, end, insert));
        expect(edited.starts, fresh.starts);
        // The lines before and after the edit are the ones the old document had there.
        for (var i = 0; i < edited.editLine; i++) {
          expect(edited.line(i), doc.line(i));
        }
        final keptAfter = doc.lineCount - edited.editLine - edited.removedLines;
        expect(edited.lineCount - edited.editLine - edited.insertedLines, keptAfter);
        for (var i = 0; i < keptAfter; i++) {
          expect(edited.line(edited.lineCount - 1 - i), doc.line(doc.lineCount - 1 - i));
        }
        doc = edited;
      }
    });
  }

  test('a lone \\r breaks a line, and a \\r\\n is one break', () {
    final doc = LineDocument('a\rb\r\nc\r');
    expect([for (var i = 0; i < doc.lineCount; i++) doc.line(i)], ['a', 'b\r', 'c', '']);
    expect(doc.replaced(1, 1, '\n').lineCount, 5, reason: '"a\\n\\rb": the \\r stays a break of its own');
    expect(doc.replaced(2, 2, '\n').lineCount, 4, reason: '"a\\r\\nb": the inserted \\n joins the \\r');
    expect(doc.replaced(4, 4, 'x').lineCount, 5, reason: '"b\\rx\\nc": the \\r and the \\n break apart');
  });

  test('an edited document is an edit of the one it was made from, and of no other', () {
    final doc = LineDocument('one\ntwo');
    final edited = doc.replaced(3, 3, '!');
    expect(edited.isEditOf(doc), isTrue);
    expect(edited.replaced(0, 0, '>').isEditOf(doc), isFalse);
    expect(LineDocument(edited.text).isEditOf(doc), isFalse);
    expect(doc.isEditOf(edited), isFalse);
  });

  test('lineAt finds the line an offset is in, its end before the line break included', () {
    final doc = LineDocument('ab\n\ncd');
    expect([for (var o = 0; o <= doc.length; o++) doc.lineAt(o)], [0, 0, 0, 1, 2, 2, 2]);
    expect([doc.lineEnd(0), doc.lineEnd(1), doc.lineEnd(2)], [2, 3, 6]);
  });

  testWidgets('typing into a long text, which the input method sees a window of, lands where the caret is', (tester) async {
    final text = List.generate(2000, (i) => 'line $i of a long text').join('\n');
    final lines = LineEditorController(text);
    final focus = FocusNode();
    await tester.pumpWidget(MaterialApp(
      home: Material(
        child: SizedBox(
          width: 300,
          height: 200,
          child: LineEditor(controller: lines, focusNode: focus, style: const TextStyle(fontSize: 13)),
        ),
      ),
    ));
    focus.requestFocus();
    await tester.pump();
    final at = text.indexOf('line 1500 ');
    lines.selection = TextSelection.collapsed(offset: at);
    await tester.pump();
    final state = tester.testTextInput.editingState!;
    final window = state['text'] as String;
    expect(window.length, lessThan(text.length));
    final caret = state['selectionBase'] as int;
    tester.testTextInput.updateEditingValue(TextEditingValue(
      text: window.replaceRange(caret, caret, 'X'),
      selection: TextSelection.collapsed(offset: caret + 1),
    ));
    await tester.pump();
    expect(lines.text, text.replaceRange(at, at, 'X'));
    expect(lines.selection, TextSelection.collapsed(offset: at + 1));
  });

  test('the ends two texts share are the same matched a block at a time, as the web matches them', () {
    final random = Random(11);
    const alphabet = 'ab\n';
    String randomText(int n) => String.fromCharCodes([for (var i = 0; i < n; i++) alphabet.codeUnitAt(random.nextInt(alphabet.length))]);
    for (var step = 0; step < 300; step++) {
      final before = randomText(random.nextInt(5000));
      final at = random.nextInt(before.length + 1);
      final cut = random.nextInt(before.length - at + 1);
      final after = before.replaceRange(at, at + cut, randomText(random.nextInt(3000)));
      expect(commonEnds(before, after, byBlocks: true), commonEnds(before, after, byBlocks: false), reason: 'step $step');
    }
    final long = 'x' * 10000;
    expect(commonEnds(long, long.substring(1), byBlocks: true), (prefix: 9999, suffix: 0));
    expect(commonEnds(long, 'y$long', byBlocks: true), (prefix: 0, suffix: 10000));
  });

  String kept(String sent, String reported) => keepLineBreaks(sent, TextEditingValue(text: reported)).text;
  int caret(String sent, String reported, int offset) =>
      keepLineBreaks(sent, TextEditingValue(text: reported, selection: TextSelection.collapsed(offset: offset)))
          .selection
          .baseOffset;

  test('a report with each line break as "\\n" keeps the "\\r"s the edit did not touch', () {
    expect(kept('a\rb\rc', 'a\nb\ncx'), 'a\rb\rcx');
    expect(kept('a\rb\rc', 'xa\nb\nc'), 'xa\rb\rc');
    expect(kept('a\rb', 'a\nb'), 'a\rb', reason: 'a selection-only report');
    expect(kept('a\rb', 'ab'), 'ab', reason: 'the edit deleted the "\\r"');
    expect(kept('a\rb', 'a\n\nb'), 'a\r\nb', reason: 'a newline typed after the "\\r"');
    expect(kept('a\rb', 'a\rbx'), 'a\rbx', reason: 'an input method that keeps "\\r" is taken as it is');
    expect(kept('a\nb', 'a\nbx'), 'a\nbx');
    expect(kept('a\r\nb\r\nc', 'a\nb\ncx'), 'a\r\nb\r\ncx');
    expect(kept('a\r\nb\r\nc', 'a\nxb\nc'), 'a\r\nxb\r\nc');
    expect(kept('a\r\nb\rc', 'a\nb\nc'), 'a\r\nb\rc', reason: 'a selection-only report');
    expect(kept('a\r\nb', 'ab'), 'ab', reason: 'the edit deleted the "\\r\\n"');
    expect(kept('a\r\nb\r\n', 'a\nb\nc'), 'a\r\nb\r\nc');
  });

  test('a report counted in the text with each line break as "\\n" moves its offsets past the "\\r"s', () {
    expect(caret('a\r\nb\r\nc', 'a\nb\ncx', 6), 8);
    expect(caret('a\r\nb\r\nc', 'a\nxb\nc', 3), 4);
    expect(caret('a\r\nb\r\nc', 'a\nb\nc', 2), 3, reason: 'a selection-only report');
    expect(caret('a\r\nb', 'ab', 1), 1);
    expect(caret('a\rb', 'a\nbx', 4), 4);
  });

  test('an input method is given the text with each line break as "\\n", its offsets counted there', () {
    final given = asInputValue(const TextEditingValue(
        text: 'a\r\nb\rc\r\n', selection: TextSelection(baseOffset: 3, extentOffset: 8), composing: TextRange(start: 0, end: 1)));
    expect(given.text, 'a\nb\nc\n');
    expect(given.selection, const TextSelection(baseOffset: 2, extentOffset: 6));
    expect(given.composing, const TextRange(start: 0, end: 1));
    expect(asInputValue(const TextEditingValue(text: 'a\nb')), const TextEditingValue(text: 'a\nb'));
  });
}
