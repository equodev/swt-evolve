// An edit takes the document's lines from the ones held for the text before it, splitting again only
// the lines it reached; the result is what splitting the whole new text gives. The views of a document
// split in two hold a text each and are edited together, and between them split it once.

import 'dart:math';

import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/src/impl/styledtext_evolve.dart';

List<int> startsOf(List<String> lines) {
  final out = <int>[];
  var at = 0;
  for (final l in lines) {
    out.add(at);
    at += l.length + 1;
  }
  return out;
}

void expectSplice(String before, int start, int replaced, String inserted) {
  final after = before.replaceRange(start, start + replaced, inserted);
  fillLineCacheFor(before);
  noteTextEdit(before, after, start, replaced, inserted);
  final lines = after.split('\n');
  expect(cachedLinesFor(after), lines, reason: 'edit at $start, $replaced replaced by "$inserted"');
  expect(cachedLineStartsFor(after), startsOf(lines));
}

void main() {
  test('typing and deleting a character inside a line', () {
    const doc = 'class A {\n  int x;\n}\n';
    expectSplice(doc, 12, 0, 'y');
    expectSplice(doc, 12, 1, '');
  });

  test('an edit that adds or joins lines', () {
    const doc = 'one\ntwo\nthree';
    expectSplice(doc, 3, 0, '\n');
    expectSplice(doc, 3, 1, '');
    expectSplice(doc, 1, 8, 'X\nY\nZ');
  });

  test('edits at the very start and end of the document', () {
    const doc = 'a\nb\n';
    expectSplice(doc, 0, 0, '\n');
    expectSplice(doc, doc.length, 0, 'c');
    expectSplice(doc, 0, doc.length, '');
    expectSplice('', 0, 0, 'x\ny');
  });

  test('random edits come out as a split of the new text', () {
    final random = Random(7);
    const alphabet = 'ab\n\t \r';
    String text(int n) => String.fromCharCodes([for (var i = 0; i < n; i++) alphabet.codeUnitAt(random.nextInt(alphabet.length))]);
    for (var i = 0; i < 500; i++) {
      final before = text(random.nextInt(60));
      final start = random.nextInt(before.length + 1);
      final replaced = random.nextInt(before.length - start + 1);
      expectSplice(before, start, replaced, text(random.nextInt(8)));
    }
  });

  test('two views of one document edited together split it once between them', () {
    final doc = [for (var i = 0; i < 200; i++) 'line $i'].join('\n');
    // Each view holds the document in a string of its own.
    final mine = String.fromCharCodes(doc.codeUnits);
    final theirs = String.fromCharCodes(doc.codeUnits);
    debugWholeTextSplits = 0;
    fillLineCacheFor(mine);
    fillLineCacheFor(theirs);
    var myText = mine, theirText = theirs;
    for (var k = 0; k < 20; k++) {
      final at = 7 * k + 3;
      final myNext = myText.replaceRange(at, at, 'x');
      final theirNext = theirText.replaceRange(at, at, 'x');
      noteTextEdit(myText, myNext, at, 0, 'x');
      noteTextEdit(theirText, theirNext, at, 0, 'x');
      // A view's layout looks its lines up against the other's, and each against the text before it.
      for (final text in [myNext, theirNext, myText, theirText]) {
        fillLineCacheFor(text);
      }
      myText = myNext;
      theirText = theirNext;
    }
    expect(debugWholeTextSplits, 1);
    expect(cachedLinesFor(myText), myText.split('\n'));
    expect(cachedLinesFor(theirText), theirText.split('\n'));
  });
}
