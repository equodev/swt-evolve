import 'dart:math' as math;
import 'dart:typed_data';
import 'dart:ui' as ui;

import 'package:flutter/foundation.dart';
import 'package:flutter/rendering.dart';
import 'package:flutter/widgets.dart';
import 'package:flutter/services.dart';

import 'line_document.dart';

/// Pixels between the text's right edge and the box's: what RenderEditable keeps for the caret.
const double _kCaretGap = 1.0;

/// Lines laid out beyond the view on either side, so a short scroll finds them ready.
const double _kBandMargin = 400.0;

/// Painters kept, beyond the ones on screen; a painter holds a laid-out paragraph.
const int _kKeptPainters = 400;

/// A multi-line text box drawn the way [RenderEditable] draws a multi-line text that wraps, with a
/// forced strut and `forceLine`, but laid out and painted a line at a time, and only for the lines
/// in view: a text of any length costs what its visible lines cost.
///
/// A line not yet laid out is taken to be as many strut rows as its length would fill at an average
/// advance; it gets its real height when it comes into view, and the scroll offset moves with it so
/// that what is on screen stays where it is.
class RenderLineEditor extends RenderBox implements TextLayoutMetrics {
  RenderLineEditor({
    required LineDocument document,
    required TextSelection? selection,
    required TextRange composing,
    required TextStyle style,
    required StrutStyle strutStyle,
    required TextAlign textAlign,
    required TextDirection textDirection,
    required TextScaler textScaler,
    required TextHeightBehavior? textHeightBehavior,
    required ViewportOffset offset,
    required ValueListenable<bool> showCursor,
    required Color cursorColor,
    required double cursorWidth,
    required Radius? cursorRadius,
    required Offset cursorOffset,
    required bool paintCursorAboveText,
    required Color? selectionColor,
    required double devicePixelRatio,
    required bool hasFocus,
  })  : _document = document,
        _selection = selection,
        _composing = composing,
        _style = style,
        _strutStyle = strutStyle,
        _textAlign = textAlign,
        _textDirection = textDirection,
        _textScaler = textScaler,
        _textHeightBehavior = textHeightBehavior,
        _offset = offset,
        _showCursor = showCursor,
        _cursorColor = cursorColor,
        _cursorWidth = cursorWidth,
        _cursorRadius = cursorRadius,
        _cursorOffset = cursorOffset,
        _paintCursorAboveText = paintCursorAboveText,
        _selectionColor = selectionColor,
        _devicePixelRatio = devicePixelRatio,
        _hasFocus = hasFocus {
    _resetLines();
  }

  // --- configuration ---

  LineDocument _document;
  LineDocument get document => _document;
  set document(LineDocument value) {
    if (identical(value, _document)) return;
    final old = _document;
    _document = value;
    if (value.isEditOf(old)) {
      _spliceLines(value);
    } else {
      _resetLines();
    }
    markNeedsLayout();
  }

  TextSelection? _selection;
  TextSelection? get selection => _selection;
  set selection(TextSelection? value) {
    if (value == _selection) return;
    _selection = value;
    markNeedsPaint();
  }

  /// The range an input method is composing, drawn underlined; empty when there is none to draw.
  TextRange _composing;
  set composing(TextRange value) {
    if (value == _composing) return;
    final old = _composing;
    _composing = value;
    // Only the lines either range touches are laid out again, with or without the underline.
    for (final range in [old, value]) {
      if (!range.isValid || range.isCollapsed) continue;
      final last = _document.lineAt(range.end.clamp(0, _document.length));
      for (var line = _document.lineAt(range.start.clamp(0, _document.length)); line <= last; line++) {
        _painters[line]?.dispose();
        _painters[line] = null;
      }
    }
    markNeedsLayout();
  }

  TextStyle _style;
  set style(TextStyle value) {
    if (value == _style) return;
    _style = value;
    _resetLayout();
  }

  StrutStyle _strutStyle;
  set strutStyle(StrutStyle value) {
    if (value == _strutStyle) return;
    _strutStyle = value;
    _resetLayout();
  }

  TextAlign _textAlign;
  set textAlign(TextAlign value) {
    if (value == _textAlign) return;
    _textAlign = value;
    _resetLayout();
  }

  TextDirection _textDirection;
  set textDirection(TextDirection value) {
    if (value == _textDirection) return;
    _textDirection = value;
    _resetLayout();
  }

  TextScaler _textScaler;
  set textScaler(TextScaler value) {
    if (value == _textScaler) return;
    _textScaler = value;
    _resetLayout();
  }

  TextHeightBehavior? _textHeightBehavior;
  set textHeightBehavior(TextHeightBehavior? value) {
    if (value == _textHeightBehavior) return;
    _textHeightBehavior = value;
    _resetLayout();
  }

  ViewportOffset _offset;
  ViewportOffset get offset => _offset;
  set offset(ViewportOffset value) {
    if (identical(value, _offset)) return;
    if (attached) _offset.removeListener(markNeedsLayout);
    _offset = value;
    if (attached) _offset.addListener(markNeedsLayout);
    markNeedsLayout();
  }

  ValueListenable<bool> _showCursor;
  set showCursor(ValueListenable<bool> value) {
    if (identical(value, _showCursor)) return;
    if (attached) _showCursor.removeListener(markNeedsPaint);
    _showCursor = value;
    if (attached) _showCursor.addListener(markNeedsPaint);
    markNeedsPaint();
  }

  Color _cursorColor;
  set cursorColor(Color value) {
    if (value == _cursorColor) return;
    _cursorColor = value;
    markNeedsPaint();
  }

  double _cursorWidth;
  set cursorWidth(double value) {
    if (value == _cursorWidth) return;
    _cursorWidth = value;
    markNeedsLayout();
  }

  Radius? _cursorRadius;
  set cursorRadius(Radius? value) {
    if (value == _cursorRadius) return;
    _cursorRadius = value;
    markNeedsPaint();
  }

  Offset _cursorOffset;
  set cursorOffset(Offset value) {
    if (value == _cursorOffset) return;
    _cursorOffset = value;
    markNeedsPaint();
  }

  bool _paintCursorAboveText;
  set paintCursorAboveText(bool value) {
    if (value == _paintCursorAboveText) return;
    _paintCursorAboveText = value;
    markNeedsPaint();
  }

  Color? _selectionColor;
  set selectionColor(Color? value) {
    if (value == _selectionColor) return;
    _selectionColor = value;
    markNeedsPaint();
  }

  double _devicePixelRatio;
  set devicePixelRatio(double value) {
    if (value == _devicePixelRatio) return;
    _devicePixelRatio = value;
    markNeedsPaint();
  }

  bool _hasFocus;
  set hasFocus(bool value) {
    if (value == _hasFocus) return;
    _hasFocus = value;
    markNeedsPaint();
  }

  // --- per line ---

  /// Each line's height: laid out when [_measured] says so, else estimated.
  Float64List _heights = Float64List(0);
  Uint8List _measured = Uint8List(0);
  List<TextPainter?> _painters = [];

  /// The running total of [_heights]: line i spans [_tops[i], _tops[i + 1]).
  Float64List? _tops;

  /// The width the lines are laid out in, and that their measured heights hold for.
  double _layoutWidth = -1;

  /// The strut row every line is made of, and the average advance of a character in the style.
  double? _rowHeight;
  double? _averageAdvance;

  final TextPainter _prototype = TextPainter();

  void _resetLines() {
    _disposePainters();
    final n = _document.lineCount;
    _heights = Float64List(n);
    _measured = Uint8List(n);
    _painters = List<TextPainter?>.filled(n, null, growable: true);
    _tops = null;
    _widest = null;
    for (var i = 0; i < n; i++) {
      _heights[i] = _estimate(i);
    }
  }

  /// Carries what is known per line across [doc]'s edit of the document it was made from.
  void _spliceLines(LineDocument doc) {
    final at = doc.editLine, removed = doc.removedLines, inserted = doc.insertedLines;
    for (var i = at; i < at + removed; i++) {
      _painters[i]?.dispose();
    }
    final n = doc.lineCount;
    final heights = Float64List(n), measured = Uint8List(n);
    heights.setRange(0, at, _heights);
    measured.setRange(0, at, _measured);
    final tail = _heights.length - at - removed;
    heights.setRange(at + inserted, at + inserted + tail, _heights, at + removed);
    measured.setRange(at + inserted, at + inserted + tail, _measured, at + removed);
    _painters.replaceRange(at, at + removed, List<TextPainter?>.filled(inserted, null));
    _heights = heights;
    _measured = measured;
    _tops = null;
    _widest = null;
    for (var i = at; i < at + inserted; i++) {
      _heights[i] = _estimate(i);
    }
  }

  /// Everything laid out goes: the style, the scale or the width it was laid out in changed.
  void _resetLayout() {
    _rowHeight = null;
    _averageAdvance = null;
    _halfLeading = null;
    _row = null;
    _layoutWidth = -1;
    _resetLines();
    markNeedsLayout();
  }

  void _disposePainters() {
    for (final p in _painters) {
      p?.dispose();
    }
  }

  TextSpan _span(String text) => TextSpan(text: text, style: _style);

  /// [line]'s text, the part of it an input method is composing underlined, as EditableText
  /// underlines it.
  TextSpan _lineSpan(int line) {
    final text = _document.line(line);
    final composing = _composing;
    if (!composing.isValid || composing.isCollapsed) return _span(text);
    final start = _document.lineStart(line);
    final from = (composing.start - start).clamp(0, text.length);
    final to = (composing.end - start).clamp(0, text.length);
    if (from >= to) return _span(text);
    return TextSpan(style: _style, children: [
      TextSpan(text: text.substring(0, from)),
      TextSpan(style: _style.merge(const TextStyle(decoration: TextDecoration.underline)), text: text.substring(from, to)),
      TextSpan(text: text.substring(to)),
    ]);
  }

  void _configure(TextPainter p, String text) => _configureSpan(p, _span(text));

  void _configureSpan(TextPainter p, TextSpan span) {
    p
      ..text = span
      ..textAlign = _textAlign
      ..textDirection = _textDirection
      ..textScaler = _textScaler
      ..strutStyle = _strutStyle
      ..textHeightBehavior = _textHeightBehavior
      ..textWidthBasis = TextWidthBasis.parent;
  }

  double get rowHeight {
    final known = _rowHeight;
    if (known != null) return known;
    _configure(_prototype, '');
    _prototype.layout();
    return _rowHeight = _prototype.preferredLineHeight;
  }

  /// What a line is taken to be before it is laid out.
  double _estimate(int line) {
    final width = _layoutWidth;
    final chars = _document.lineEnd(line) - _document.lineStart(line);
    if (width <= 0 || chars == 0) return rowHeight;
    final rows = (chars * _advance / width).ceil();
    return rowHeight * math.max(1, rows);
  }

  double get _advance {
    final known = _averageAdvance;
    if (known != null) return known;
    const sample = 'The quick brown fox jumps over the lazy dog, 0123456789.';
    _configure(_prototype, sample);
    _prototype.layout();
    return _averageAdvance = _prototype.width / sample.length;
  }

  /// [line] laid out in the current width.
  TextPainter _painter(int line) {
    var p = _painters[line];
    if (p == null) {
      p = TextPainter();
      _configureSpan(p, _lineSpan(line));
      _painters[line] = p;
    }
    p.layout(minWidth: _layoutWidth, maxWidth: _layoutWidth);
    if (_measured[line] == 0 || _heights[line] != p.height) {
      _heights[line] = p.height;
      _measured[line] = 1;
      _tops = null;
    }
    return p;
  }

  Float64List get tops {
    final known = _tops;
    if (known != null) return known;
    final n = _heights.length;
    final out = Float64List(n + 1);
    var y = 0.0;
    for (var i = 0; i < n; i++) {
      out[i] = y;
      y += _heights[i];
    }
    out[n] = y;
    return _tops = out;
  }

  /// The line whose span holds [y], in content coordinates.
  int _lineAtY(double y) {
    final t = tops;
    if (y <= 0) return 0;
    var lo = 0, hi = t.length - 2;
    while (lo < hi) {
      final mid = (lo + hi + 1) >> 1;
      if (t[mid] <= y) {
        lo = mid;
      } else {
        hi = mid - 1;
      }
    }
    return lo;
  }

  double get contentHeight => tops[_heights.length];

  double get preferredLineHeight => rowHeight;

  double get _caretMargin => _kCaretGap + _cursorWidth;

  double _maxScrollExtent = 0;

  /// Whether more text is laid out than shows, which the paint is clipped to.
  bool get _hasVisualOverflow => _maxScrollExtent > 0 || _offset.pixels != 0;

  // --- layout ---

  @override
  void attach(PipelineOwner owner) {
    super.attach(owner);
    _offset.addListener(markNeedsLayout);
    _showCursor.addListener(markNeedsPaint);
  }

  @override
  void detach() {
    _offset.removeListener(markNeedsLayout);
    _showCursor.removeListener(markNeedsPaint);
    super.detach();
  }

  @override
  bool get sizedByParent => false;

  @override
  void performLayout() {
    final width = math.max(0.0, constraints.maxWidth - _caretMargin);
    if (width != _layoutWidth) {
      _layoutWidth = width;
      // Every height was laid out in, or estimated for, the old width.
      _measured.fillRange(0, _measured.length, 0);
      for (var i = 0; i < _heights.length; i++) {
        _heights[i] = _estimate(i);
      }
      for (final p in _painters) {
        p?.dispose();
      }
      _painters.fillRange(0, _painters.length, null);
      _tops = null;
      _widest = null;
      _halfLeading = null;
    }
    size = constraints.constrain(Size(constraints.maxWidth, contentHeight));
    // The line at the top of the view, and how far into it the view starts: laying out the lines
    // in the band can change the height of lines above it, and the view stays on that line.
    for (var pass = 0; pass < 3; pass++) {
      final pixels = _offset.pixels;
      final anchor = _lineAtY(pixels);
      final intoAnchor = pixels - tops[anchor];
      _layoutBand(pixels);
      final shift = tops[anchor] + intoAnchor - pixels;
      if (shift != 0 && pixels > 0) _offset.correctBy(shift);
      _maxScrollExtent = math.max(0.0, contentHeight - size.height);
      _offset.applyViewportDimension(size.height);
      final settled = _offset.applyContentDimensions(0, _maxScrollExtent);
      if (settled && (shift == 0 || pixels <= 0)) break;
    }
    _evictPainters();
  }

  /// Lays out every line within the view and [_kBandMargin] of it.
  void _layoutBand(double pixels) {
    final top = math.max(0.0, pixels - _kBandMargin);
    final bottom = pixels + size.height + _kBandMargin;
    var line = _lineAtY(top);
    while (line < _heights.length) {
      final lineTop = tops[line];
      if (lineTop > bottom) break;
      _painter(line);
      line++;
    }
  }

  /// Lets go of the painters far from the view.
  void _evictPainters() {
    var held = 0;
    for (final p in _painters) {
      if (p != null) held++;
    }
    if (held <= _kKeptPainters) return;
    final first = _lineAtY(math.max(0.0, _offset.pixels - _kBandMargin));
    final last = _lineAtY(_offset.pixels + size.height + _kBandMargin);
    for (var i = 0; i < _painters.length; i++) {
      if (i >= first && i <= last) continue;
      final p = _painters[i];
      if (p != null) {
        p.dispose();
        _painters[i] = null;
      }
    }
  }

  @override
  double computeMinIntrinsicHeight(double width) => rowHeight;

  @override
  double computeMaxIntrinsicHeight(double width) => rowHeight;

  @override
  Size computeDryLayout(BoxConstraints constraints) =>
      constraints.constrain(Size(constraints.maxWidth, rowHeight));

  // --- geometry ---

  /// The paint origin of the content: the view scrolls it up by the offset.
  Offset get _paintOffset => Offset(0, -_offset.pixels);

  /// [position] in content coordinates, as the line it falls in reports it.
  ({int line, TextPainter painter, int local}) _locate(TextPosition position) {
    final offset = position.offset.clamp(0, _document.length);
    final line = _document.lineAt(offset);
    return (line: line, painter: _painter(line), local: offset - _document.lineStart(line));
  }

  late Rect _caretPrototype;

  void _computeCaretPrototype() {
    final height = preferredLineHeight;
    switch (defaultTargetPlatform) {
      case TargetPlatform.iOS:
      case TargetPlatform.macOS:
        _caretPrototype = Rect.fromLTWH(0.0, 0.0, _cursorWidth, height + 2);
      case TargetPlatform.android:
      case TargetPlatform.fuchsia:
      case TargetPlatform.linux:
      case TargetPlatform.windows:
        _caretPrototype = Rect.fromLTWH(0.0, 2.0, _cursorWidth, height - 2.0 * 2.0);
    }
  }

  /// Where the caret at [caretPosition] is drawn, in this box's coordinates, as
  /// [RenderEditable.getLocalRectForCaret] places it.
  Rect getLocalRectForCaret(TextPosition caretPosition) {
    _computeCaretPrototype();
    final at = _locate(caretPosition);
    final local = TextPosition(offset: at.local, affinity: caretPosition.affinity);
    final caretOffset = at.painter.getOffsetForCaret(local, _caretPrototype) + Offset(0, tops[at.line]);
    var caretRect = _caretPrototype.shift(caretOffset + _cursorOffset);
    final scrollableWidth = math.max(_layoutWidth + _caretMargin, size.width);
    final caretX = clampDouble(caretRect.left, 0, math.max(scrollableWidth - _caretMargin, 0));
    caretRect = Offset(caretX, caretRect.top) & caretRect.size;
    final fullHeight = at.painter.getFullHeightForCaret(local, _caretPrototype);
    switch (defaultTargetPlatform) {
      case TargetPlatform.iOS:
      case TargetPlatform.macOS:
        final heightDiff = fullHeight - caretRect.height;
        caretRect = Rect.fromLTWH(caretRect.left, caretRect.top + heightDiff / 2, caretRect.width, caretRect.height);
      case TargetPlatform.android:
      case TargetPlatform.fuchsia:
      case TargetPlatform.linux:
      case TargetPlatform.windows:
        final caretHeight = preferredLineHeight;
        final heightDiff = fullHeight - caretHeight;
        caretRect = Rect.fromLTWH(caretRect.left, caretRect.top - 2.0 + heightDiff / 2, caretRect.width, caretHeight);
    }
    caretRect = caretRect.shift(_paintOffset);
    return caretRect.shift(_snapToPhysicalPixel(caretRect.topLeft));
  }

  Offset _snapToPhysicalPixel(Offset sourceOffset) {
    final globalOffset = localToGlobal(sourceOffset);
    final pixelMultiple = 1.0 / _devicePixelRatio;
    return Offset(
      globalOffset.dx.isFinite ? (globalOffset.dx / pixelMultiple).round() * pixelMultiple - globalOffset.dx : 0,
      globalOffset.dy.isFinite ? (globalOffset.dy / pixelMultiple).round() * pixelMultiple - globalOffset.dy : 0,
    );
  }

  /// The text position at [local], a point in this box's coordinates.
  TextPosition getPositionForPoint(Offset local) {
    final content = local - _paintOffset;
    if (content.dy < 0) return const TextPosition(offset: 0);
    if (content.dy >= contentHeight) return TextPosition(offset: _document.length, affinity: TextAffinity.upstream);
    final line = _lineAtY(content.dy);
    final p = _painter(line);
    final inLine = p.getPositionForOffset(Offset(content.dx, content.dy - tops[line]));
    return TextPosition(offset: _document.lineStart(line) + inLine.offset, affinity: inLine.affinity);
  }

  /// The word at [position], as the line it is in breaks into words; on an empty line, its line
  /// break, which a paragraph of the whole text takes for a word.
  @override
  TextRange getWordBoundary(TextPosition position) {
    final at = _locate(position);
    final start = _document.lineStart(at.line);
    if (_document.lineEnd(at.line) == start) {
      return at.line < _document.lineCount - 1 ? TextRange(start: start, end: start + 1) : TextRange.collapsed(start);
    }
    final word = at.painter.getWordBoundary(TextPosition(offset: at.local, affinity: position.affinity));
    return TextRange(start: start + word.start, end: start + word.end);
  }

  /// How [line]'s own layout moves the caret a word at a time, in offsets into the line.
  TextBoundary wordBoundariesOf(int line) => _painter(line).wordBoundaries.moveByWordBoundary;

  @override
  TextSelection getLineAtOffset(TextPosition position) {
    final row = getLineBoundary(position);
    return TextSelection(baseOffset: row.start, extentOffset: row.end);
  }

  @override
  TextPosition getTextPositionAbove(TextPosition position) =>
      positionOnAdjacentRow(position, caretX(position), -1) ?? const TextPosition(offset: 0);

  @override
  TextPosition getTextPositionBelow(TextPosition position) =>
      positionOnAdjacentRow(position, caretX(position), 1) ?? TextPosition(offset: _document.length);

  /// The visual row [position] is on: its start and end, as offsets into the text.
  TextRange getLineBoundary(TextPosition position) {
    final at = _locate(position);
    final start = _document.lineStart(at.line);
    // An empty line has no row for its painter to report: it is the row it starts.
    if (_document.lineEnd(at.line) == start) return TextRange.collapsed(start);
    final row = at.painter.getLineBoundary(TextPosition(offset: at.local, affinity: position.affinity));
    return TextRange(start: start + row.start, end: start + row.end);
  }

  /// The position a row up ([rows] < 0) or down from [from], at [x] in content coordinates, as a
  /// vertical caret move finds it; null past the first or last row.
  TextPosition? positionOnAdjacentRow(TextPosition from, double x, int rows) {
    var at = _locate(from);
    var metrics = _rowsOf(at.painter);
    final caret = at.painter.getOffsetForCaret(TextPosition(offset: at.local, affinity: from.affinity), Rect.zero);
    var row = 0;
    for (var m = 0; m < metrics.length; m++) {
      final top = metrics[m].baseline - metrics[m].ascent;
      if (caret.dy + 0.5 >= top) row = m;
    }
    var line = at.line;
    var target = row + rows;
    while (target < 0) {
      if (line == 0) return null;
      line--;
      target += _rowsOf(_painter(line)).length;
    }
    while (true) {
      metrics = _rowsOf(_painter(line));
      if (target < metrics.length) break;
      if (line == _document.lineCount - 1) return null;
      target -= metrics.length;
      line++;
    }
    final m = metrics[target];
    final y = m.baseline - m.ascent + m.height / 2;
    final inLine = _painter(line).getPositionForOffset(Offset(x, y));
    return TextPosition(offset: _document.lineStart(line) + inLine.offset, affinity: inLine.affinity);
  }

  /// The caret's x in content coordinates, what vertical moves keep.
  double caretX(TextPosition position) {
    final at = _locate(position);
    return at.painter.getOffsetForCaret(TextPosition(offset: at.local, affinity: position.affinity), Rect.zero).dx;
  }

  /// The boxes [selection] covers, in this box's coordinates.
  List<Rect> getBoxesForSelection(TextSelection selection) {
    final start = selection.start.clamp(0, _document.length);
    final end = selection.end.clamp(0, _document.length);
    if (start == end) return const [];
    final out = <Rect>[];
    for (var line = _document.lineAt(start); line <= _document.lineAt(end); line++) {
      out.addAll(_lineBoxes(line, start, end).map((r) => r.shift(_paintOffset)));
    }
    return out;
  }

  /// The selection boxes of [start, end) on [line], in content coordinates, as one paragraph of the
  /// whole text reports them with [ui.BoxHeightStyle.includeLineSpacingMiddle] and
  /// [ui.BoxWidthStyle.max], what EditableText asks for.
  ///
  /// A line laid out on its own is its own first and last row: the half of the leading it shares
  /// with the line above and below is added back. And a row the selection runs past is highlighted
  /// out to the widest row of the whole text, which is [_widestRow].
  List<Rect> _lineBoxes(int line, int start, int end) {
    final lineStart = _document.lineStart(line);
    final lineEnd = _document.lineEnd(line);
    final from = math.max(start, lineStart) - lineStart;
    final to = math.min(end, lineEnd) - lineStart;
    final pastEnd = end > lineEnd;
    if (from > to || (from == to && !pastEnd)) return const [];
    final p = _painter(line);
    final metrics = _rowsOf(p);
    final half = _halfLeadingAbove;
    final firstLine = line == 0, lastLine = line == _document.lineCount - 1;
    final top = tops[line];
    // RenderEditable clips each box to the paragraph, which here is the whole text.
    final bounds = Rect.fromLTRB(0, -top, _layoutWidth, contentHeight - top);
    final out = <Rect>[];
    final rowRight = List<double?>.filled(metrics.length, null);
    final rowTop = List<double?>.filled(metrics.length, null);
    final rowBottom = List<double?>.filled(metrics.length, null);
    if (from < to) {
      for (final box in p.getBoxesForSelection(
        TextSelection(baseOffset: from, extentOffset: to),
        boxHeightStyle: ui.BoxHeightStyle.includeLineSpacingMiddle,
      )) {
        final row = _rowAt(metrics, (box.top + box.bottom) / 2);
        var rect = box.toRect();
        if (row == 0 && !firstLine) rect = Rect.fromLTRB(rect.left, rect.top - half, rect.right, rect.bottom);
        if (row == metrics.length - 1 && !lastLine) rect = Rect.fromLTRB(rect.left, rect.top, rect.right, rect.bottom + half);
        out.add(rect);
        rowRight[row] = math.max(rowRight[row] ?? rect.right, rect.right);
        rowTop[row] = rect.top;
        rowBottom[row] = rect.bottom;
      }
    }
    // Rows the selection reaches the end of: every row of the range but its last, and the last too
    // when the selection reaches this line's end; never the last row of the text.
    final firstRow = _rowOfOffset(p, metrics, from);
    final lastRow = _rowOfOffset(p, metrics, to);
    for (var row = firstRow; row <= lastRow; row++) {
      if (row == lastRow && (to < lineEnd - lineStart || (lastLine && row == metrics.length - 1))) break;
      var right = rowRight[row];
      var boxTop = rowTop[row], boxBottom = rowBottom[row];
      if (right == null || boxTop == null || boxBottom == null) {
        // Nothing selected on the row itself (an empty line): the highlight starts where its caret is.
        final m = metrics[row];
        right = p.getOffsetForCaret(TextPosition(offset: from), Rect.zero).dx + _glyphInset;
        final glyphTop = m.baseline - m.ascent + 2 * half;
        boxTop = glyphTop - (row == 0 && firstLine ? 0 : half);
        boxBottom = m.baseline + m.descent + (row == metrics.length - 1 && lastLine ? 0 : half);
        out.add(Rect.fromLTRB(right, boxTop, right, boxBottom));
      }
      final widest = _widestRow;
      if (right < widest) out.add(Rect.fromLTRB(right, boxTop, widest, boxBottom));
    }
    return [for (final r in out) r.intersect(bounds).shift(Offset(0, top))];
  }

  /// The rows [p] is laid out in. An empty line reports none; it is one row like any other, which
  /// the forced strut makes the same as every row.
  List<ui.LineMetrics> _rowsOf(TextPainter p) {
    final rows = p.computeLineMetrics();
    if (rows.isNotEmpty) return rows;
    return [_emptyRow];
  }

  ui.LineMetrics get _emptyRow {
    final known = _row;
    if (known != null) return known;
    _configure(_prototype, 'M');
    _prototype.layout();
    final m = _prototype.computeLineMetrics().first;
    return _row = ui.LineMetrics(
      hardBreak: true,
      ascent: m.ascent,
      descent: m.descent,
      unscaledAscent: m.unscaledAscent,
      height: m.height,
      width: 0,
      left: m.left,
      baseline: m.baseline,
      lineNumber: 0,
    );
  }

  ui.LineMetrics? _row;

  int _rowAt(List<ui.LineMetrics> metrics, double y) {
    var row = 0;
    for (var m = 1; m < metrics.length; m++) {
      if (y >= metrics[m].baseline - metrics[m].ascent) row = m;
    }
    return row;
  }

  int _rowOfOffset(TextPainter p, List<ui.LineMetrics> metrics, int offset) {
    final caret = p.getOffsetForCaret(TextPosition(offset: offset), Rect.zero);
    return _rowAt(metrics, caret.dy + 0.5);
  }

  /// Half the leading above a row's glyphs: what a paragraph adds above each row's selection box but
  /// its first, and below each but its last.
  double get _halfLeadingAbove {
    final known = _halfLeading;
    if (known != null) return known;
    _configure(_prototype, 'M');
    _prototype.layout();
    final m = _prototype.computeLineMetrics().first;
    final glyph = _prototype.getBoxesForSelection(const TextSelection(baseOffset: 0, extentOffset: 1)).first;
    final half = (glyph.top - (m.baseline - m.ascent)) / 2;
    // Measured from where an empty line's caret is, which is where its highlight starts from.
    _configure(_prototype, '');
    _prototype.layout(minWidth: math.max(0, _layoutWidth), maxWidth: math.max(0, _layoutWidth));
    _inset = glyph.left - _prototype.getOffsetForCaret(const TextPosition(offset: 0), Rect.zero).dx;
    return _halfLeading = half;
  }

  double? _halfLeading;

  /// How far right of the caret a glyph's selection box starts: half the letter spacing.
  double get _glyphInset {
    _halfLeadingAbove;
    return _inset!;
  }

  double? _inset;

  /// The right edge of the widest row of the whole text, its trailing spaces counted, as a paragraph
  /// of it measures the widest line. Found only when a selection runs past a row, and kept until
  /// the text, the style or the width changes; every line is laid out to find it.
  double get _widestRow {
    final known = _widest;
    if (known != null) return known;
    var widest = 0.0;
    for (var line = 0; line < _document.lineCount; line++) {
      final length = _document.lineEnd(line) - _document.lineStart(line);
      if (length == 0) continue;
      final p = _painter(line);
      final boxes = p.getBoxesForSelection(TextSelection(baseOffset: 0, extentOffset: length));
      if (boxes.isEmpty) continue;
      // Each row's boxes are measured from where its first glyph starts.
      final metrics = p.computeLineMetrics();
      final left = List<double?>.filled(metrics.length, null);
      final right = List<double>.filled(metrics.length, 0);
      for (final b in boxes) {
        final row = _rowAt(metrics, (b.top + b.bottom) / 2);
        left[row] = math.min(left[row] ?? b.left, b.left);
        right[row] = math.max(right[row], b.right);
      }
      for (var row = 0; row < metrics.length; row++) {
        final l = left[row];
        if (l != null) widest = math.max(widest, right[row] - l);
      }
    }
    _evictPainters();
    return _widest = widest;
  }

  double? _widest;

  // --- paint ---

  final LayerHandle<ClipRectLayer> _clipRectLayer = LayerHandle<ClipRectLayer>();

  @override
  void paint(PaintingContext context, Offset offset) {
    if (_hasVisualOverflow) {
      _clipRectLayer.layer = context.pushClipRect(
        needsCompositing,
        offset,
        Offset.zero & size,
        _paintContents,
        oldLayer: _clipRectLayer.layer,
      );
    } else {
      _clipRectLayer.layer = null;
      _paintContents(context, offset);
    }
  }

  void _paintContents(PaintingContext context, Offset offset) {
    final canvas = context.canvas;
    final origin = offset + _paintOffset;
    final pixels = _offset.pixels;
    final first = _lineAtY(pixels);
    var last = first;
    while (last + 1 < _heights.length && tops[last + 1] < pixels + size.height) {
      last++;
    }

    final selection = _selection;
    final color = _selectionColor;
    if (selection != null && color != null && !selection.isCollapsed) {
      final paint = Paint()..color = color;
      final start = selection.start, end = selection.end;
      final from = math.max(first, _document.lineAt(start));
      final to = math.min(last, _document.lineAt(end));
      for (var line = from; line <= to; line++) {
        for (final box in _lineBoxes(line, start, end)) {
          canvas.drawRect(box.shift(origin), paint);
        }
      }
    }

    if (!_paintCursorAboveText) _paintCaret(canvas, offset);
    for (var line = first; line <= last; line++) {
      _painter(line).paint(canvas, origin + Offset(0, tops[line]));
    }
    if (_paintCursorAboveText) _paintCaret(canvas, offset);
  }

  void _paintCaret(Canvas canvas, Offset offset) {
    final selection = _selection;
    if (!_hasFocus || !_showCursor.value || selection == null || !selection.isCollapsed || !selection.isValid) {
      return;
    }
    final rect = getLocalRectForCaret(selection.extent).shift(offset);
    final paint = Paint()..color = _cursorColor;
    final radius = _cursorRadius;
    if (radius == null) {
      canvas.drawRect(rect, paint);
    } else {
      canvas.drawRRect(RRect.fromRectAndRadius(rect, radius), paint);
    }
  }

  @override
  bool hitTestSelf(Offset position) => true;

  @override
  void dispose() {
    _clipRectLayer.layer = null;
    _disposePainters();
    _prototype.dispose();
    super.dispose();
  }
}

/// The widget for [RenderLineEditor].
class LineEditorViewport extends LeafRenderObjectWidget {
  const LineEditorViewport({
    super.key,
    required this.document,
    required this.selection,
    required this.composing,
    required this.style,
    required this.strutStyle,
    required this.textAlign,
    required this.textDirection,
    required this.textScaler,
    required this.textHeightBehavior,
    required this.offset,
    required this.showCursor,
    required this.cursorColor,
    required this.cursorWidth,
    required this.cursorRadius,
    required this.cursorOffset,
    required this.paintCursorAboveText,
    required this.selectionColor,
    required this.devicePixelRatio,
    required this.hasFocus,
  });

  final LineDocument document;
  final TextSelection? selection;
  final TextRange composing;
  final TextStyle style;
  final StrutStyle strutStyle;
  final TextAlign textAlign;
  final TextDirection textDirection;
  final TextScaler textScaler;
  final TextHeightBehavior? textHeightBehavior;
  final ViewportOffset offset;
  final ValueListenable<bool> showCursor;
  final Color cursorColor;
  final double cursorWidth;
  final Radius? cursorRadius;
  final Offset cursorOffset;
  final bool paintCursorAboveText;
  final Color? selectionColor;
  final double devicePixelRatio;
  final bool hasFocus;

  @override
  RenderLineEditor createRenderObject(BuildContext context) => RenderLineEditor(
        document: document,
        selection: selection,
        composing: composing,
        style: style,
        strutStyle: strutStyle,
        textAlign: textAlign,
        textDirection: textDirection,
        textScaler: textScaler,
        textHeightBehavior: textHeightBehavior,
        offset: offset,
        showCursor: showCursor,
        cursorColor: cursorColor,
        cursorWidth: cursorWidth,
        cursorRadius: cursorRadius,
        cursorOffset: cursorOffset,
        paintCursorAboveText: paintCursorAboveText,
        selectionColor: selectionColor,
        devicePixelRatio: devicePixelRatio,
        hasFocus: hasFocus,
      );

  @override
  void updateRenderObject(BuildContext context, RenderLineEditor renderObject) {
    renderObject
      ..document = document
      ..selection = selection
      ..composing = composing
      ..style = style
      ..strutStyle = strutStyle
      ..textAlign = textAlign
      ..textDirection = textDirection
      ..textScaler = textScaler
      ..textHeightBehavior = textHeightBehavior
      ..offset = offset
      ..showCursor = showCursor
      ..cursorColor = cursorColor
      ..cursorWidth = cursorWidth
      ..cursorRadius = cursorRadius
      ..cursorOffset = cursorOffset
      ..paintCursorAboveText = paintCursorAboveText
      ..selectionColor = selectionColor
      ..devicePixelRatio = devicePixelRatio
      ..hasFocus = hasFocus;
  }
}

/// Used by the tests that compare this box with a TextField: how many painters it holds.
@visibleForTesting
int debugHeldPainters(RenderLineEditor editor) => editor._painters.where((p) => p != null).length;

/// Used by the tests: whether [line] has been laid out.
@visibleForTesting
bool debugLineMeasured(RenderLineEditor editor, int line) => editor._measured[line] == 1;
