import 'dart:typed_data';

/// A text as the lines it breaks into at `\n`, each found by where it starts.
///
/// Immutable. An edit makes a new document that remembers which lines it replaced in the one it was
/// made from ([isEditOf], [editLine], [removedLines], [insertedLines]), so whatever is kept per line
/// can be carried across the edit instead of rebuilt.
class LineDocument {
  LineDocument(this.text)
      : starts = _startsOf(text, 0, text.length, 0),
        _madeFrom = null,
        editLine = 0,
        removedLines = 0,
        insertedLines = 0,
        editStart = 0,
        editEnd = 0,
        insertedLength = 0;

  LineDocument._edited(this.text, this.starts, this._madeFrom, this.editLine, this.removedLines, this.insertedLines,
      this.editStart, this.editEnd, this.insertedLength);

  static final LineDocument empty = LineDocument('');

  final String text;

  /// Where each line starts; there is always at least one line.
  final Int32List starts;

  /// Who this document is, and who the one it was made from by one edit is: a token rather than the
  /// document itself, which would keep every earlier text alive.
  final Object _identity = Object();
  final Object? _madeFrom;

  /// Whether this document was made from [other] by one edit.
  bool isEditOf(LineDocument other) => identical(_madeFrom, other._identity);

  /// The first line of the document this one was made from that the edit touched.
  final int editLine;

  /// How many lines of the document this one was made from, from [editLine], the edit replaced.
  final int removedLines;

  /// How many lines, from [editLine], replace them here.
  final int insertedLines;

  /// The range of the document this one was made from that the edit replaced, and how long its
  /// replacement is here.
  final int editStart, editEnd, insertedLength;

  int get length => text.length;

  int get lineCount => starts.length;

  int lineStart(int line) => starts[line];

  /// Where [line] ends, before its `\n`.
  int lineEnd(int line) => line + 1 < starts.length ? starts[line + 1] - 1 : text.length;

  String line(int line) => text.substring(starts[line], lineEnd(line));

  /// The line [offset] is in. An offset at a line's end, before its `\n`, is in that line.
  int lineAt(int offset) {
    var lo = 0, hi = starts.length - 1;
    while (lo < hi) {
      final mid = (lo + hi + 1) >> 1;
      if (starts[mid] <= offset) {
        lo = mid;
      } else {
        hi = mid - 1;
      }
    }
    return lo;
  }

  /// This text with [start, end) replaced by [insert].
  LineDocument replaced(int start, int end, String insert) {
    final first = lineAt(start);
    final last = lineAt(end);
    final newText = text.replaceRange(start, end, insert);
    final delta = insert.length - (end - start);
    final inserted = _startsOf(insert, 0, insert.length, start);
    // Lines before [first] keep their starts, [first] keeps its own, the inserted text adds one
    // line per `\n`, and every line after [last] moves by [delta].
    final keptAfter = starts.length - last - 1;
    final out = Int32List(first + 1 + inserted.length - 1 + keptAfter);
    out.setRange(0, first + 1, starts);
    // _startsOf counts the text's own start as a line; the first one is [first]'s, already copied.
    out.setRange(first + 1, first + inserted.length, inserted, 1);
    for (var i = 0; i < keptAfter; i++) {
      out[first + inserted.length + i] = starts[last + 1 + i] + delta;
    }
    return LineDocument._edited(newText, out, _identity, first, last - first + 1, inserted.length, start, end, insert.length);
  }

  /// The starts of the lines of `text[from, to)`, offset by [base], its own start first.
  static Int32List _startsOf(String text, int from, int to, int base) {
    var count = 1;
    for (var i = from; i < to; i++) {
      if (text.codeUnitAt(i) == 0x0A) count++;
    }
    final out = Int32List(count);
    var n = 1;
    out[0] = base;
    for (var i = from; i < to; i++) {
      if (text.codeUnitAt(i) == 0x0A) out[n++] = base + i - from + 1;
    }
    return out;
  }
}

/// How many code units [a] and [b] have in common at their start, and then, of what is left, at
/// their end: what lies between is the one range an edit of [a] into [b] replaced.
({int prefix, int suffix}) commonEnds(String a, String b) {
  final max = a.length < b.length ? a.length : b.length;
  var prefix = 0;
  while (prefix < max && a.codeUnitAt(prefix) == b.codeUnitAt(prefix)) {
    prefix++;
  }
  var suffix = 0;
  while (suffix < max - prefix && a.codeUnitAt(a.length - 1 - suffix) == b.codeUnitAt(b.length - 1 - suffix)) {
    suffix++;
  }
  return (prefix: prefix, suffix: suffix);
}
