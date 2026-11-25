import 'dart:typed_data';

import 'package:flutter/foundation.dart' show kIsWeb, visibleForTesting;
import 'package:flutter/services.dart' show TextEditingValue, TextRange;

/// A text as the lines it breaks into at each line break (`\n`, or a `\r` not followed by `\n`),
/// each found by where it starts.
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

  /// Where [line] ends, before its line break. A `\r\n` line keeps its `\r`.
  int lineEnd(int line) => line + 1 < starts.length ? starts[line + 1] - 1 : text.length;

  String line(int line) => text.substring(starts[line], lineEnd(line));

  /// The line [offset] is in. An offset at a line's end, before its line break, is in that line.
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
  LineDocument replaced(int start, int end, String insert) =>
      _replacedBy(text.replaceRange(start, end, insert), start, end, start + insert.length);

  /// This document edited into [result], which is it with `[start, end)` replaced by
  /// `result[start, insertEnd)`: the text is taken as it is, not built again from the edit.
  LineDocument editedInto(String result, int start, int end, int insertEnd) =>
      _replacedBy(result, start, end, insertEnd);

  LineDocument _replacedBy(String newText, int start, int end, int insertEnd) {
    // Whether a `\r` just before the edit breaks a line depends on what follows it now.
    final from = start > 0 && text.codeUnitAt(start - 1) == 0x0D ? start - 1 : start;
    final first = lineAt(from);
    final last = lineAt(end);
    final insertLength = insertEnd - start;
    final delta = insertLength - (end - start);
    final inserted = _startsOf(newText, from, insertEnd, from);
    // Lines before [first] keep their starts, [first] keeps its own, the inserted text adds one
    // line per line break, and every line after [last] moves by [delta].
    final keptAfter = starts.length - last - 1;
    final out = Int32List(first + 1 + inserted.length - 1 + keptAfter);
    out.setRange(0, first + 1, starts);
    // _startsOf counts the text's own start as a line; the first one is [first]'s, already copied.
    out.setRange(first + 1, first + inserted.length, inserted, 1);
    for (var i = 0; i < keptAfter; i++) {
      out[first + inserted.length + i] = starts[last + 1 + i] + delta;
    }
    return LineDocument._edited(newText, out, _identity, first, last - first + 1, inserted.length, start, end, insertLength);
  }

  /// The starts of the lines of `text[from, to)`, offset by [base], its own start first.
  static Int32List _startsOf(String text, int from, int to, int base) {
    var count = 1;
    for (var i = from; i < to; i++) {
      if (breaksLineAt(text, i)) count++;
    }
    final out = Int32List(count);
    var n = 1;
    out[0] = base;
    for (var i = from; i < to; i++) {
      if (breaksLineAt(text, i)) out[n++] = base + i - from + 1;
    }
    return out;
  }

  /// Whether `text[i]` is a line break: a `\n`, or a `\r` that no `\n` follows.
  static bool breaksLineAt(String text, int i) {
    final c = text.codeUnitAt(i);
    return c == 0x0A || (c == 0x0D && (i + 1 == text.length || text.codeUnitAt(i + 1) != 0x0A));
  }
}

/// [value] as an input method is given it: a browser's textarea holds each line break as `\n` and
/// counts its offsets in that text, so every editor hands it that form.
TextEditingValue asInputValue(TextEditingValue value) {
  final text = value.text;
  if (!text.contains('\r')) return value;
  int map(int offset) {
    if (offset < 0) return offset;
    var mapped = 0;
    for (var i = 0; i < offset; i++, mapped++) {
      if (_isCrLf(text, i) && i + 1 < offset) i++;
    }
    return mapped;
  }

  return TextEditingValue(
    text: text.replaceAll(_lineBreak, '\n'),
    selection: value.selection.copyWith(baseOffset: map(value.selection.baseOffset), extentOffset: map(value.selection.extentOffset)),
    composing: value.composing.isValid ? TextRange(start: map(value.composing.start), end: map(value.composing.end)) : value.composing,
  );
}

/// What an input method reports, [reported], read against [sent], the text it was given in
/// [asInputValue]'s form: the line breaks the edit left alone keep their form in [sent], and the
/// offsets count them so.
TextEditingValue keepLineBreaks(String sent, TextEditingValue reported) {
  final text = reported.text;
  if (text.contains('\r') || !sent.contains('\r')) return reported;
  final asInput = sent.replaceAll(_lineBreak, '\n');
  // Each offset into [asInput], as an offset into [sent].
  final toSent = Int32List(asInput.length + 1);
  for (var i = 0, s = 0; i <= asInput.length; i++, s++) {
    toSent[i] = s;
    if (s < sent.length && _isCrLf(sent, s)) s++;
  }
  final String kept;
  final int Function(int) map;
  if (asInput == text) {
    kept = sent;
    map = (offset) => toSent[offset];
  } else {
    final (:prefix, :suffix) = commonEnds(asInput, text);
    final head = toSent[prefix];
    final tail = toSent[asInput.length - suffix];
    kept = sent.substring(0, head) + text.substring(prefix, text.length - suffix) + sent.substring(tail);
    map = (offset) => offset <= prefix
        ? toSent[offset]
        : offset >= text.length - suffix
            ? kept.length - sent.length + toSent[asInput.length - text.length + offset]
            : head + offset - prefix;
  }
  int mapped(int offset) => offset < 0 || offset > text.length ? offset : map(offset);
  return TextEditingValue(
    text: kept,
    selection: reported.selection.copyWith(
        baseOffset: mapped(reported.selection.baseOffset), extentOffset: mapped(reported.selection.extentOffset)),
    composing: reported.composing.isValid
        ? TextRange(start: mapped(reported.composing.start), end: mapped(reported.composing.end))
        : reported.composing,
  );
}

bool _isCrLf(String text, int i) =>
    text.codeUnitAt(i) == 0x0D && i + 1 < text.length && text.codeUnitAt(i + 1) == 0x0A;

final _lineBreak = RegExp('\r\n?');

/// How many code units [a] and [b] have in common at their start, and then, of what is left, at
/// their end: what lies between is the one range an edit of [a] into [b] replaced.
({int prefix, int suffix}) commonEnds(String a, String b, {@visibleForTesting bool byBlocks = kIsWeb}) {
  final max = a.length < b.length ? a.length : b.length;
  var prefix = byBlocks ? _sameBlocksAtStart(a, b, max) : 0;
  while (prefix < max && a.codeUnitAt(prefix) == b.codeUnitAt(prefix)) {
    prefix++;
  }
  var suffix = byBlocks ? _sameBlocksAtEnd(a, b, max - prefix) : 0;
  while (suffix < max - prefix && a.codeUnitAt(a.length - 1 - suffix) == b.codeUnitAt(b.length - 1 - suffix)) {
    suffix++;
  }
  return (prefix: prefix, suffix: suffix);
}

/// On the web a string is the browser's: a slice of one costs next to nothing and two compare
/// natively, where reading one a code unit at a time is a call per unit. So the ends are matched a
/// block at a time first, and only the block that differs is read unit by unit.
const _block = 1024;

int _sameBlocksAtStart(String a, String b, int max) {
  var at = 0;
  while (at + _block <= max && a.substring(at, at + _block) == b.substring(at, at + _block)) {
    at += _block;
  }
  return at;
}

int _sameBlocksAtEnd(String a, String b, int max) {
  var n = 0;
  while (n + _block <= max &&
      a.substring(a.length - n - _block, a.length - n) == b.substring(b.length - n - _block, b.length - n)) {
    n += _block;
  }
  return n;
}
