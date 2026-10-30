import 'package:flutter/material.dart';
import 'package:flutter/rendering.dart';
import '../../gen/event.dart';
import '../../gen/point.dart';
import 'double_tap_detector.dart';
import 'font_utils.dart';

/// The code unit of '&'.
const _ampersand = 0x26;

/// The text a widget draws for an SWT string in which '&' marks a mnemonic, by the rule Java measures
/// it with (`Mnemonics.display`): every single '&' is dropped, the one at the end too, and "&&" draws
/// one '&'.
String stripAccelerators(String? text) => parseMnemonic(text).text;

/// The text a CLabel draws: its text without the mnemonic markers, and nothing for the placeholder
/// `<none>`.
String clabelDrawnText(String? text) => text == '<none>' ? '' : stripAccelerators(text);

/// [stripAccelerators]'s text plus where the mnemonic landed: [index] is the offset in [text] of the
/// character after the first single '&', or -1 when there is none. This is the only copy of the rule
/// on this side; `MnemonicHelper.findMnemonic` is its Java twin, which decides what Alt+letter activates.
({String text, int index}) parseMnemonic(String? text) {
  if (text == null) return (text: '', index: -1);
  final first = text.indexOf('&');
  if (first < 0) return (text: text, index: -1);
  final drawn = StringBuffer(text.substring(0, first));
  var index = -1;
  for (var i = first; i < text.length; i++) {
    final c = text.codeUnitAt(i);
    if (c != _ampersand) {
      drawn.writeCharCode(c);
    } else if (i + 1 < text.length && text.codeUnitAt(i + 1) == _ampersand) {
      drawn.write('&');
      i++;
    } else if (index < 0 && i + 1 < text.length) {
      index = drawn.length;
    }
  }
  return (text: drawn.toString(), index: index);
}

(int start, int end) getWordBoundaries(String text, int offset) {
  if (text.isEmpty) return (0, 0);
  final clamped = offset.clamp(0, text.length);
  bool isWord(String c) => RegExp(r'\w').hasMatch(c);
  int start = clamped;
  while (start > 0 && isWord(text[start - 1])) {
    start--;
  }
  int end = clamped;
  while (end < text.length && isWord(text[end])) {
    end++;
  }
  return (start, end);
}

/// The logical line containing [offset]: its first offset, and the offset of its last
/// character -- the newline that ends it is not included.
///
/// Callers that need SWT's line-select range (triple click) add the newline back; the ones
/// that need its caret range (Home/End) do not.
(int start, int end) getLineBoundaries(String text, int offset) {
  final clamped = offset.clamp(0, text.length);
  final start = clamped == 0 ? 0 : text.lastIndexOf('\n', clamped - 1) + 1;
  final nl = text.indexOf('\n', clamped);
  return (start, nl == -1 ? text.length : nl);
}

class DoubleClickWordSelector extends StatefulWidget {
  final Widget child;
  final TextEditingController controller;
  final FocusNode? focusNode;
  final void Function(int start, int end) onWordSelected;
  final VoidCallback? onTripleTap;

  const DoubleClickWordSelector({
    super.key,
    required this.child,
    required this.controller,
    this.focusNode,
    required this.onWordSelected,
    this.onTripleTap,
  });

  @override
  State<DoubleClickWordSelector> createState() =>
      _DoubleClickWordSelectorState();
}

class _DoubleClickWordSelectorState extends State<DoubleClickWordSelector> {
  final _detector = DoubleTapDetector(slop: 20.0);
  bool _pendingWordSelect = false;
  bool _tripleClickPending = false;
  bool _guardSelectAll = false;
  int _selectAllLen = 0;

  @override
  void initState() {
    super.initState();
    widget.controller.addListener(_guardSelectionChange);
  }

  @override
  void didUpdateWidget(DoubleClickWordSelector oldWidget) {
    super.didUpdateWidget(oldWidget);
    if (oldWidget.controller != widget.controller) {
      oldWidget.controller.removeListener(_guardSelectionChange);
      widget.controller.addListener(_guardSelectionChange);
    }
  }

  @override
  void dispose() {
    widget.controller.removeListener(_guardSelectionChange);
    super.dispose();
  }

  void _guardSelectionChange() {
    if (!_guardSelectAll) return;
    final sel = widget.controller.selection;
    if (sel.baseOffset == 0 && sel.extentOffset == _selectAllLen) return;
    _guardSelectAll = false;
    final len = _selectAllLen;
    Future.microtask(() {
      if (mounted) {
        widget.controller.selection =
            TextSelection(baseOffset: 0, extentOffset: len);
      }
    });
  }

  RenderEditable? _findRenderEditable(RenderObject root) {
    if (root is RenderEditable) return root;
    RenderEditable? found;
    root.visitChildren((child) => found ??= _findRenderEditable(child));
    return found;
  }

  int _offsetAt(Offset localPos) {
    final renderBox = context.findRenderObject() as RenderBox?;
    if (renderBox != null) {
      final re = _findRenderEditable(renderBox);
      if (re != null) {
        final textPos = re.getPositionForPoint(renderBox.localToGlobal(localPos));
        return textPos.offset.clamp(0, widget.controller.text.length);
      }
    }
    return widget.controller.selection.baseOffset.clamp(0, widget.controller.text.length);
  }

  void _trackTap(PointerDownEvent e) {
    final count = _detector.registerTap(position: e.localPosition);
    if (count == 3) {
      _pendingWordSelect = false;
      _tripleClickPending = true;
      _selectAll();
    } else if (count == 2) {
      _tripleClickPending = false;
      _pendingWordSelect = true;
      final pos = e.localPosition;
      Future.microtask(() {
        if (_pendingWordSelect) {
          _pendingWordSelect = false;
          _selectWord(pos);
        }
      });
    } else {
      _guardSelectAll = false;
      _tripleClickPending = false;
      _pendingWordSelect = false;
    }
  }

  void _selectAll() {
    if (!mounted) return;
    _applyAfterFocus(() {
      _tripleClickPending = false;
      final len = widget.controller.text.length;
      _selectAllLen = len;
      _guardSelectAll = true;
      widget.controller.selection =
          TextSelection(baseOffset: 0, extentOffset: len);
      widget.onTripleTap?.call();
    });
  }

  void _applyAfterFocus(VoidCallback apply) {
    if (widget.focusNode != null && !widget.focusNode!.hasFocus) {
      widget.focusNode!.requestFocus();
    }
    WidgetsBinding.instance.addPostFrameCallback((_) {
      if (mounted) apply();
    });
  }

  void _selectWord(Offset localPos) {
    if (!mounted) return;
    _applyAfterFocus(() {
      if (_tripleClickPending) return;
      final offset = _offsetAt(localPos);
      final (start, end) = getWordBoundaries(widget.controller.text, offset);
      widget.controller.selection = TextSelection(baseOffset: start, extentOffset: end);
      widget.onWordSelected(start, end);
    });
  }

  @override
  Widget build(BuildContext context) =>
      Listener(onPointerDown: _trackTap, child: widget.child);
}

/// Splits "Label\tShortcut" into (label, shortcut). [label] keeps its `&` marker, for a
/// [MnemonicText]; with no \t, shortcut is null and label is the whole text.
({String label, String? shortcut}) splitMenuItemText(String? text) {
  if (text == null) return (label: '', shortcut: null);
  final idx = text.indexOf('\t');
  if (idx < 0) return (label: text, shortcut: null);
  return (label: text.substring(0, idx), shortcut: text.substring(idx + 1));
}

/// The laid-out width of [text] on one line in [style], measured once per text and style.
///
/// A build that checks whether its text fits its bounds would otherwise shape the paragraph on every
/// rebuild, whatever changed; most rebuilds leave both the same.
double singleLineTextWidth(String text, TextStyle? style) => singleLineTextSize(text, style).width;

/// The laid-out size of [text] on one line in [style]; see [singleLineTextWidth].
Size singleLineTextSize(String text, TextStyle? style) => _measure(text, style, 1);

/// The laid-out size of [text] in [style] with no width limit: its widest line, and all its lines.
Size textSize(String text, TextStyle? style) => _measure(text, style, null);

/// [singleLineTextSize] for many texts in one [style]: the style is looked up once, not per text.
Size Function(String text) singleLineTextSizes(TextStyle? style) {
  final measures = _measuresFor(style);
  return (text) => _measureIn(measures, text, style, 1);
}

/// [singleLineTextWidth], asking [shaped] first: the width text about to be drawn reports when it is
/// shaped exactly as this measure would shape it, or null when it is not. Remembered like a measure.
double shapedSingleLineTextWidth(String text, TextStyle style, double? Function() shaped) {
  var measures = _measuresFor(style);
  final known = measures.shapedWidths[text];
  if (known != null) return known;
  final width = shaped() ?? _measureIn(measures, text, style, 1).width;
  _listenForFonts();
  if (_keptSizes >= _maxTextSizes) _dropMeasures();
  measures = _measuresFor(style);
  _keptSizes++;
  return measures.shapedWidths[text] = width;
}

void _listenForFonts() {
  if (_listeningForFonts) return;
  _listeningForFonts = FontUtils.listenToSystemFonts(() {
    _dropMeasures();
    textMeasureEpoch++;
  });
}

/// Moves on whenever the fonts change, and with them what text measures: a width kept outside these
/// caches is valid only for the epoch it was measured in.
int textMeasureEpoch = 0;

Size _measure(String text, TextStyle? style, int? maxLines) =>
    _measureIn(_measuresFor(style), text, style, maxLines);

Size _measureIn(_Measures measures, String text, TextStyle? style, int? maxLines) {
  final key = (text, maxLines);
  final cached = measures.sizes[key];
  if (cached != null) return cached;
  _listenForFonts();
  final painter = TextPainter(
    text: TextSpan(text: text, style: style),
    maxLines: maxLines,
    textDirection: TextDirection.ltr,
    // As the Text widget that paints it scales it under the zoom, or box and text disagree by it.
    textScaler: FontUtils.textWidgetScaler,
  )..layout();
  final size = Size(painter.width, painter.height);
  painter.dispose();
  // Emptied rather than trimmed one entry at a time: removing from the front of an ordered map
  // leaves holes that every later trim has to step over.
  if (_keptSizes >= _maxTextSizes) {
    _dropMeasures();
    measures = _measuresFor(style);
  }
  _keptSizes++;
  measures.sizes[key] = size;
  return size;
}

/// The sizes kept for one style, from the [_generation] they were measured in.
class _Measures {
  _Measures(this.generation);

  final int generation;
  final Map<(String, int?), Size> sizes = {};

  /// See [shapedSingleLineTextWidth].
  final Map<String, double> shapedWidths = {};
}

/// The kept sizes for [style]. A style is hashed and compared field by field, so the instance asked
/// about remembers its entry: callers that keep their style find it without either.
_Measures _measuresFor(TextStyle? style) {
  // A size measured at another zoom is not this one's.
  final scale = FontUtils.uiScale;
  if (scale != _measuredScale) {
    _measuredScale = scale;
    _dropMeasures();
    textMeasureEpoch++;
  }
  if (style != null) {
    final known = _measuresOfStyle[style];
    if (known != null && known.generation == _generation) return known;
  }
  final measures = _measuresByStyle[style] ??= _Measures(_generation);
  if (style != null) _measuresOfStyle[style] = measures;
  return measures;
}

void _dropMeasures() {
  _measuresByStyle.clear();
  _keptSizes = 0;
  _generation++;
}

const _maxTextSizes = 4096;
final Map<TextStyle?, _Measures> _measuresByStyle = {};
final Expando<_Measures> _measuresOfStyle = Expando();
int _keptSizes = 0;
int _generation = 0;
double _measuredScale = 1.0;
bool _listeningForFonts = false;
