import 'dart:math';

import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:swtflutter/src/impl/utils/line_document.dart';
import 'package:swtflutter/src/impl/utils/line_editor.dart';

void main() {
  test('an edited document has the lines of its text, and says which it replaced', () {
    final random = Random(3);
    const alphabet = 'ab \n\n';
    String randomText(int n) => String.fromCharCodes([for (var i = 0; i < n; i++) alphabet.codeUnitAt(random.nextInt(alphabet.length))]);
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
}
