import 'dart:async';
import 'dart:convert';
import 'dart:math' as math;
import 'dart:ui' show LineMetrics;
import 'package:collection/collection.dart';
import 'package:flutter/foundation.dart';
import 'package:flutter/gestures.dart';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:flutter/widgets.dart';
import '../comm/comm.dart';
import '../comm/comm_frame.dart' show EquoCommBase;
import '../comm/delivery_gate.dart' show deliveryGate, widgetRefreshChannel;
import 'utils/perf_marks_stub.dart'
    if (dart.library.js_interop) 'utils/perf_marks_web.dart';
import '../gen/event.dart';
import '../gen/color.dart';
import '../gen/gc.dart';
import '../gen/stylerange.dart';
import '../gen/styledtext.dart';
import '../gen/styledtextrenderer.dart';
import '../gen/swt.dart';
import '../gen/widget.dart';
import '../impl/canvas_evolve.dart';
import '../impl/gcdrawer_evolve.dart';
import 'widget_config.dart';
import 'key_forwarding.dart';
import 'key_mapping.dart';
import 'utils/composed_text_input.dart';
import 'utils/font_utils.dart';
import 'utils/widget_utils.dart';
import 'color_utils.dart';
import '../theme/theme_extensions/scrolledcomposite_theme_extension.dart';
import '../theme/theme_extensions/styledtext_theme_extension.dart';
import '../testing/render_facts.dart';

/// Paints a StyledText as Java describes it and reports raw input back; the document, carets,
/// selection and scroll offset are Java's and nothing here edits them.
class StyledTextImpl<T extends StyledTextSwt, V extends VStyledText>
    extends CanvasImpl<T, V> implements RenderFactsSource {
  // Keys are forwarded from [_onKeyEvent], which also keeps them away from Flutter's shortcuts.
  @override
  bool get forwardsKeysFromWrap => false;

  // Flutter's Draggable fires on any pointer jitter, turning ordinary clicks into spurious drag/drop.
  @override
  bool get wrapsWholeWidgetForDnd => false;

  final FocusNode _focusNode = FocusNode(debugLabel: 'StyledText');

  @override
  FocusNode? get swtFocusNode => _focusNode;

  /// What Java last described, laid out; rebuilt on the next build after a delivery.
  TextShape? _shape;
  bool _shapeStale = true;
  _Delimiters _delimiters = _Delimiters.none;
  Object? _textChangeToken;
  Object? _resendGeometryToken;

  static const int _caretBlinkMs = 560;
  Timer? _caretBlinkTimer;
  bool _caretOn = true;
  int? _lastCaretOffset;

  /// Holds a text-input connection open while focused so the platform can compose; what it
  /// composes goes to Java's IME.
  late final ComposedTextInput _composedInput = ComposedTextInput(
    onComposing: (text, caret) => _sendComposition(text, caret, commit: false),
    onComposedText: (text) => _sendComposition(text, text.length, commit: true),
  );

  void _sendComposition(String text, int caret, {required bool commit}) {
    EquoCommService.sendPayload(
      "${state.swt}/${state.id}/ImeComposition",
      {'text': text, 'caret': caret, 'commit': commit},
    );
  }

  String? _metricsSource;
  int _metricsLineCount = 1;
  int _metricsMaxLineLength = 0;

  StyledTextThemeExtension get _styledTextTheme =>
      Theme.of(context).extension<StyledTextThemeExtension>()!;

  /// The text colour: the application's own when it set one, else the theme's.
  Color get _textColor => getForegroundColor(
        context: context,
        foreground: state.foreground,
        defaultColor: _styledTextTheme.foregroundColor,
      );

  /// The editor fill marks a place to type. A read-only StyledText is text to read, so it sits on the
  /// surface like a Label or a read-only Text does.
  @override
  Color get bg =>
      ParentBackgroundScope.backgroundOf(context) ??
      getBackgroundColor(background: state.background, defaultColor: null) ??
      ((state.editable ?? false)
          ? _styledTextTheme.backgroundColor
          : Theme.of(context).colorScheme.surface);

  @override
  Widget wrapWithGCOverlay(Widget child) {
    final gc = gcOverlay ?? (VGC()..id = state.id);
    final gcWidget = GCSwt<VGC>(key: gcOverlayKey, value: gc);
    return Stack(
      children: [
        child,
        if (gcOverlay != null)
          Positioned.fill(child: IgnorePointer(child: gcWidget))
        else
          Offstage(child: gcWidget),
      ],
    );
  }

  @override
  void initState() {
    super.initState();
    _focusNode.addListener(_onFocusChange);
    _textChangeToken =
        EquoCommService.onRaw("StyledText/${state.id}/TextChange", _onTextChange);
    _resendGeometryToken = EquoCommService.onRaw(
        "StyledText/${state.id}/ResendGeometry", (_) => _sendWholeGeometry());
    // Updates that arrived while nothing showed this editor were merged with no one to resolve
    // them; a change among them is relative to an index this state never held.
    _takeStyleIndex(state);
  }

  /// Applies one edit Java made. A resulting length that differs from Java's means an edit was
  /// missed, so the whole document is requested instead.
  void _onTextChange(Object? message) {
    final change = message is Map ? message : null;
    if (change == null || !mounted) return;
    final start = (change['start'] as num?)?.toInt();
    final replaced = (change['replaced'] as num?)?.toInt();
    final inserted = change['text'] as String?;
    final charCount = (change['charCount'] as num?)?.toInt();
    if (start == null || replaced == null || inserted == null) return;
    final current = state.text ?? '';
    if (start < 0 || replaced < 0 || start + replaced > current.length) {
      _requestText();
      return;
    }
    final updated = current.replaceRange(start, start + replaced, inserted);
    if (charCount != null && updated.length != charCount) {
      _requestText();
      return;
    }
    state.text = updated;
    // Carry the styling across the edit now: until Java's next frame, painting the new text against
    // the old offsets shifts every run past the edit.
    final added = inserted.length;
    _splicedStyleIndex = spliceStyleIndex(
        _splicedStyleIndex ?? state.styleIndex ?? const [], start, replaced, added);
    _splicedCaretOffset =
        spliceOffset(_splicedCaretOffset ?? state.caretOffset ?? 0, start, replaced, added);
    _splicedCaretOffsets = [
      for (final o in _splicedCaretOffsets ?? state.caretOffsets ?? const <int>[])
        spliceOffset(o, start, replaced, added),
    ];
    _splicedSelectionRanges = spliceRangePairs(
        _splicedSelectionRanges ?? state.selectionRanges ?? const [], start, replaced, added);
    setState(() {
      _shapeStale = true;
    });
  }

  /// Java's sent edits applied to the style index; null outside that window, replaced by the next frame.
  List<int>? _splicedStyleIndex;
  int? _splicedCaretOffset;
  List<int>? _splicedCaretOffsets;
  List<int>? _splicedSelectionRanges;

  /// The index a change from Java is relative to, including the edits since ([_splicedStyleIndex]).
  List<int>? _heldStyleIndex;

  /// Applies a style-index change from Java. A result that fails Java's checksum was made against an
  /// index this side does not hold, so the whole widget is requested.
  void _takeStyleIndex(V value) {
    final incoming = value.styleIndex;
    if (incoming != null && incoming.isNotEmpty && incoming[0] == styleIndexChangeMark) {
      final base = _splicedStyleIndex ?? _heldStyleIndex;
      final resolved = base == null ? null : applyStyleIndexChange(base, incoming);
      if (resolved == null) {
        value.styleIndex = _heldStyleIndex;
        deliveryGate.recoveries++;
        print('[delivery] style index change does not fit StyledText/${value.id}: asking for it again');
        EquoCommService.sendPayload(widgetRefreshChannel, '${value.id}');
        return;
      }
      value.styleIndex = resolved;
    }
    _heldStyleIndex = value.styleIndex;
  }

  @override
  void setValue(V value) {
    // Only a frame of this widget's own replaces what was carried across an edit: the caret, a
    // child nothing else renders, notifies this widget too, ahead of the frame the edit belongs to.
    if (value.seq != _appliedSeq) {
      _appliedSeq = value.seq;
      // A frame without the index leaves the one carried across the edits standing, which is also
      // what Java takes this side to hold until it sends the index again.
      if (lastChange?.touches('styleIndex') ?? true) {
        _takeStyleIndex(value);
        _splicedStyleIndex = null;
      }
      _splicedCaretOffset = null;
      _splicedCaretOffsets = null;
      _splicedSelectionRanges = null;
    }
    super.setValue(value);
  }

  /// The write stamp of the last frame of this widget's own that was applied here.
  int? _appliedSeq;

  void _requestText() {
    EquoCommService.sendPayload("StyledText/${state.id}/ResendText", const {});
  }

  @override
  void didChangeDependencies() {
    super.didChangeDependencies();
    // Colours and fonts come from the theme.
    _shapeStale = true;
  }

  @override
  void dispose() {
    EquoCommService.remove("StyledText/${state.id}/TextChange", _textChangeToken);
    _geometryTimer?.cancel();
    EquoCommService.remove("StyledText/${state.id}/ResendGeometry", _resendGeometryToken);
    _composedInput.detach();
    setCanvasEditorFocus(this, false);
    _focusNode.removeListener(_onFocusChange);
    _focusNode.dispose();
    _caretBlinkTimer?.cancel();
    super.dispose();
  }

  @override
  void extraSetState() {
    super.extraSetState();
    _shapeStale = true;
  }

  bool get _focused => _focusNode.hasFocus;

  void _onFocusChange() {
    if (!mounted) return;
    setCanvasEditorFocus(this, _focused);
    if (_focused) {
      widget.sendFocusFocusIn(state, null);
      if (state.editable ?? false) _composedInput.attach();
      _restartCaretBlink();
    } else {
      _composedInput.detach();
      _caretBlinkTimer?.cancel();
      _caretBlinkTimer = null;
      widget.sendFocusFocusOut(state, null);
    }
    setState(() {});
  }

  void _restartCaretBlink() {
    _caretBlinkTimer?.cancel();
    _caretOn = true;
    if (!_focused) return;
    _caretBlinkTimer = Timer.periodic(
      const Duration(milliseconds: _caretBlinkMs),
      (_) {
        if (!mounted) return;
        setState(() => _caretOn = !_caretOn);
      },
    );
  }

  /// Keys reach Java from the Display's handler when one is mounted, else from here, and are kept
  /// from Flutter's shortcuts (which would scroll or move focus); only Tab goes on, to traversal.
  KeyEventResult _onKeyEvent(FocusNode node, KeyEvent event) {
    if (!displayLevelKeyForwardingActive) {
      final v = mapNewKeyEventToSwt(event);
      // A key the composition owns must not reach the document twice (ComposedTextInput.swallowsKey).
      // Only the press is tested: dropping the release would leave Java holding the key down.
      if (event is KeyDownEvent && compositionOwnsKey(event.character ?? '')) {
        return KeyEventResult.handled;
      }
      if (v.keyCode != 0 || v.character != 0) {
        if (event is KeyUpEvent) {
          widget.sendKeyKeyUp(state, v);
        } else {
          widget.sendKeyKeyDown(state, v);
        }
      }
    }
    if (event.logicalKey == LogicalKeyboardKey.tab) {
      // Flutter's shortcuts traverse on Tab and Shift+Tab only. SWT offers the traversal with any
      // modifier, and StyledText lets a modified Tab leave the editor.
      final keys = HardwareKeyboard.instance;
      if (event is KeyDownEvent &&
          (keys.isControlPressed || keys.isAltPressed || keys.isMetaPressed)) {
        Actions.maybeInvoke(
          context,
          keys.isShiftPressed ? const PreviousFocusIntent() : const NextFocusIntent(),
        );
        return KeyEventResult.handled;
      }
      return KeyEventResult.ignored;
    }
    return KeyEventResult.handled;
  }

  /// The platform focuses a control on any button press, ahead of the MouseDown that
  /// [ControlImpl.wrap] sends for the same press.
  void _onPointerDown(PointerDownEvent event) {
    if (scrollBarPointers.contains(event.pointer)) return;
    widget.sendFocusFocusIn(state, null);
    _focusNode.requestFocus();
    // The second press of a double click also delivers MouseDoubleClick, after its MouseDown. The
    // MouseDown is sent by the same pointer dispatch, once this handler returns.
    final position = event.localPosition;
    scheduleMicrotask(() {
      if (!mounted || swtClickCount != 2) return;
      widget.sendMouseMouseDoubleClick(
        state,
        VEvent()
          ..button = swtButton
          ..count = 2
          ..x = position.dx.round()
          ..y = position.dy.round()
          ..stateMask = moveStateMask(0),
      );
    });
  }

  /// Line count and longest line in one pass, over code units — `text[i]` allocates a
  /// one-character String per character.
  void _ensureTextMetrics(String text) {
    if (identical(_metricsSource, text) || _metricsSource == text) {
      _metricsSource = text;
      return;
    }
    const newline = 0x0A;
    int lines = 1;
    int longest = 0;
    int lineStart = 0;
    for (int i = 0; i < text.length; i++) {
      if (text.codeUnitAt(i) == newline) {
        final len = i - lineStart;
        if (len > longest) longest = len;
        lineStart = i + 1;
        lines++;
      }
    }
    final tailLength = text.length - lineStart;
    if (tailLength > longest) longest = tailLength;
    _metricsSource = text;
    _metricsLineCount = lines;
    _metricsMaxLineLength = longest;
  }

  /// Offsets arrive in document terms and are mapped onto the shown text, which has a single
  /// character per line delimiter.
  void _rebuildShape() => perfMark('StyledText.rebuildShape', _rebuildShapeNow);

  void _rebuildShapeNow() {
    final rebuildWatch = Stopwatch()..start();
    _shapeStale = false;
    final original = state.text ?? '';
    _delimiters = _Delimiters.of(original);
    final text = _Delimiters.normalize(original);

    final caretOffset = _delimiters.toShown(_splicedCaretOffset ?? state.caretOffset ?? 0);
    if (caretOffset != _lastCaretOffset) {
      _lastCaretOffset = caretOffset;
      _restartCaretBlink();
    }

    final defaultStyle = _stableDefaultStyle();
    // StyledText#getCaret(): the application's caret sizes the one painted here.
    final caretBounds = state.caret?.bounds;
    final caretInfo = CaretInfo(
      offset: caretOffset,
      width: (caretBounds?.width ?? 0) > 0 ? caretBounds!.width.toDouble() : 1.0,
      // 0 asks the shape for the height of the row the caret sits on, as a native caret is
      // as tall as its line.
      height: (caretBounds?.height ?? 0) > 0 ? caretBounds!.height.toDouble() : 0,
      color: applyAlpha(_textColor),
      styledTextId: state.id,
      blinkRate: _caretBlinkMs,
    );

    final ranges = _splicedSelectionRanges ?? state.selectionRanges ?? const <int>[];
    final selections = <SelectionInfo>[
      for (int i = 0; i + 1 < ranges.length; i += 2)
        if (ranges[i + 1] != 0)
          SelectionInfo.fromRange(
            _delimiters.toShown(ranges[i]),
            _delimiters.toShown(ranges[i] + ranges[i + 1]),
          ).copyWith(
            selectionColor: _selectionColor,
            selectionForeground: _selectionForeground,
          ),
    ];
    SelectionInfo? selection = selections.isNotEmpty ? selections.first : null;
    if (selection == null) {
      final sel = state.selection;
      if (sel != null && sel.x != sel.y) {
        selection = SelectionInfo.fromRange(
          _delimiters.toShown(sel.x),
          _delimiters.toShown(sel.y),
        ).copyWith(
          selectionColor: _selectionColor,
          selectionForeground: _selectionForeground,
        );
      }
    }
    final carets = [
      for (final offset in _splicedCaretOffsets ?? state.caretOffsets ?? const <int>[])
        _delimiters.toShown(offset),
    ];

    final javaLineHeight =
        ((state.renderer?.ascent ?? 0) + (state.renderer?.descent ?? 0)).toDouble();

    final previousShape = _shape;
    final shape = TextShape(
      text,
      Offset((state.leftMargin ?? 0).toDouble(), (state.topMargin ?? 0).toDouble()),
      defaultStyle,
      clipRect,
      null,
      caretInfo,
      state.wordWrap ?? false,
      getBounds(),
      state.editable ?? false,
      state.id,
      null,
      _editingStateFor(text),
      selection,
      javaLineHeight,
      state.tabs ?? 4,
      state.tabStops,
      LineProperties(
        alignment: state.alignment,
        indent: state.indent,
        justify: state.justify,
        wrapIndent: state.wrapIndent,
      ),
      (state.lineSpacing ?? 0).toDouble(),
      state.renderer?.lineSpacings,
      hasStyle(state.style, SWT.RIGHT_TO_LEFT) ? TextDirection.rtl : TextDirection.ltr,
      hasStyle(state.style, SWT.FULL_SELECTION),
      carets,
      selections,
      _styleToken,
    );
    if (previousShape != null) shape.inheritMeasurementsFrom(previousShape);
    _shape = shape;
    debugRebuildMicros += rebuildWatch.elapsedMicroseconds;
    debugRebuildCalls++;
  }

  /// The shape as painted this frame: the caret shows only while focused, and blinks.
  TextShape? _lastPainted;

  TextShape? get _paintedShape {
    final shape = _shape;
    final caret = shape?.caretInfo;
    if (shape == null || caret == null) return shape;
    final visible = _focused && _caretOn;
    if (caret.visible == visible) return shape;
    return shape.copyWithCaret(caret.copyWith(visible: visible, blinking: _focused));
  }

  // StyledTextRenderer's LineInfo flags: which attributes the line sets for itself.
  static const int _lineAlignment = 1 << 1;
  static const int _lineIndent = 1 << 2;
  static const int _lineJustify = 1 << 3;
  static const int _lineTabStops = 1 << 6;
  static const int _lineWrapIndent = 1 << 7;
  static const int _lineVerticalIndent = 1 << 9;

  List<VStyleRange?>? _paletteSource;
  List<_PaletteStyle?>? _paletteDecoded;

  /// Decoded palette entries by content, so an unchanged entry keeps its object and the layouts
  /// cached under that object's identity.
  final Map<String, _PaletteStyle> _paletteByContent = {};

  /// What a decoded entry also depends on besides its own fields.
  Object? _paletteBasis;

  void _checkPaletteBasis() {
    final basis = (_getDefaultTextStyle(), applyAlpha(_textColor), _linkForeground);
    if (basis != _paletteBasis || _paletteByContent.length > 512) _paletteByContent.clear();
    _paletteBasis = basis;
  }

  /// Rebuilt only when an input changed; every input arrives as a new object when it changes, so
  /// identity is enough.
  TextEditingState _editingStateFor(String shownText) {
    final styles = state.styles;
    final styleIndex = _splicedStyleIndex ?? state.styleIndex;
    final defaultStyle = styles == null || styles.isEmpty ? _stableDefaultStyle() : null;
    final held = _heldEditingState;
    if (held != null &&
        identical(held.renderer, state.renderer) &&
        identical(held.styles, styles) &&
        identical(held.styleIndex, styleIndex) &&
        identical(held.text, state.text) &&
        identical(held.defaultStyle, defaultStyle)) {
      return held.editingState;
    }
    final editingState = _buildEditingStateFromRenderer(shownText);
    _heldEditingState = (
      renderer: state.renderer,
      styles: styles,
      styleIndex: styleIndex,
      text: state.text,
      defaultStyle: defaultStyle,
      editingState: editingState,
    );
    return editingState;
  }

  ({
    Object? renderer,
    Object? styles,
    Object? styleIndex,
    String? text,
    TextStyle? defaultStyle,
    TextEditingState editingState,
  })? _heldEditingState;

  TextEditingState _buildEditingStateFromRenderer(String shownText) {
    final renderer = state.renderer;
    // Styles are a widget property rather than the renderer's, so a frame that only moves their
    // offsets does not resend them.
    final styles = state.styles;
    // The palette entry each run is drawn in; without an index there is one style per run. The first
    // element names the palette, so a reordered palette cannot arrive without it; runs start at 1.
    final styleIndex = _splicedStyleIndex ?? state.styleIndex;
    final banded = styleIndex != null && styleIndex.length > 1;
    List<StyleRange> characterRanges = [];

    if (styles != null && styles.isNotEmpty) {
      final count =
          banded ? (styleIndex!.length - 1) ~/ 3 : (renderer?.styleCount ?? styles.length);

      // Decoded once per palette entry and kept while the palette is: the paint cache recognises a
      // rasterised run by the identity of its TextStyle.
      final samePalette = identical(_paletteSource, styles) && _paletteDecoded != null;
      final decoded = samePalette
          ? _paletteDecoded!
          : (_paletteDecoded = List<_PaletteStyle?>.filled(styles.length, null));
      _paletteSource = styles;
      if (!samePalette) _checkPaletteBasis();

      for (int i = 0; i < count; i++) {
        final named = banded ? styleIndex![1 + 3 * i] : i;
        if (named < 0 || named >= styles.length) continue;
        final vRange = styles[named];
        if (vRange == null) continue;
        final entry = decoded[named] ??= _paletteByContent[jsonEncode(vRange.toJson())] ??= _PaletteStyle(
          style: _convertVStyleRangeToTextStyle(vRange),
          underlineStyle:
              vRange.underline == true ? (vRange.underlineStyle ?? SWT.UNDERLINE_SINGLE) : null,
          borderStyle: (vRange.borderStyle ?? 0) == 0 ? null : vRange.borderStyle,
          borderColor: vRange.borderColor != null ? colorFromVColor(vRange.borderColor) : null,
          glyphWidth: (vRange.metrics?.width ?? 0) > 0 ? vRange.metrics!.width.toDouble() : null,
          underlineColor: vRange.underline == true && vRange.underlineColor != null
              ? colorFromVColor(vRange.underlineColor)
              : null,
          strikeoutColor: vRange.strikeout == true && vRange.strikeoutColor != null
              ? colorFromVColor(vRange.strikeoutColor)
              : null,
          paintsStrikeout: _strikeoutIsPaintedSeparately(vRange),
          rise: vRange.rise ?? 0,
        );

        int start, length;
        if (banded) {
          start = styleIndex![2 + 3 * i];
          length = styleIndex[3 + 3 * i];
        } else {
          start = vRange.start;
          length = vRange.length;
        }
        characterRanges.add(StyleRange(
          start: _delimiters.toShown(start),
          end: _delimiters.toShown(start + length),
          style: entry.style,
          underlineStyle: entry.underlineStyle,
          borderStyle: entry.borderStyle,
          borderColor: entry.borderColor,
          glyphWidth: entry.glyphWidth,
          underlineColor: entry.underlineColor,
          strikeoutColor: entry.strikeoutColor,
          paintsStrikeout: entry.paintsStrikeout,
          rise: entry.rise,
        ));
      }
    } else if (shownText.isNotEmpty) {
      characterRanges.add(
        StyleRange(start: 0, end: shownText.length, style: _stableDefaultStyle()),
      );
    }

    characterRanges.sort((a, b) => a.start.compareTo(b.start));

    characterRanges = dedupeSortedRanges(characterRanges);

    // Array index corresponds to line index; nulls keep the position mapping.
    Map<int, LineProperties> lineProps = {};
    if (renderer?.lines != null && renderer!.lines!.isNotEmpty) {
      for (int i = 0; i < renderer.lines!.length; i++) {
        final vLineInfo = renderer.lines![i];
        if (vLineInfo != null) {
          // flags says which of these the line set for itself; the rest fall back to the widget's.
          final flags = vLineInfo.flags ?? 0;
          lineProps[i] = LineProperties(
            alignment: flags & _lineAlignment != 0 ? vLineInfo.alignment : null,
            indent: flags & _lineIndent != 0 ? vLineInfo.indent : null,
            justify: flags & _lineJustify != 0 ? vLineInfo.justify : null,
            verticalIndent: flags & _lineVerticalIndent != 0 ? vLineInfo.verticalIndent : null,
            wrapIndent: flags & _lineWrapIndent != 0 ? vLineInfo.wrapIndent : null,
            bulletText: (renderer.bulletTexts?.length ?? 0) > i ? renderer.bulletTexts![i] : null,
            background: vLineInfo.background == null ? null : colorFromVColor(vLineInfo.background),
            tabStops: flags & _lineTabStops != 0 ? vLineInfo.tabStops : null,
          );
        }
      }
    }

    return TextEditingState(
      characterRanges: characterRanges,
      lineProperties: lineProps,
    );
  }

  /// StyledText#setSelectionBackground is a real SWT setter the CSS engine drives, so the
  /// application's choice replaces the built-in highlight rather than going through [getBackgroundColor].
  Color get _selectionColor =>
      (state.selectionBackground == null
          ? null
          : colorFromVColor(state.selectionBackground)) ??
      const Color(0xFF3399FF);

  /// StyledText#setSelectionForeground, the CSS engine's other selection setter: the selected
  /// glyphs are repainted in it over the highlight. Null keeps the glyphs as they are.
  Color? get _selectionForeground => state.selectionForeground == null
      ? null
      : colorFromVColor(state.selectionForeground);

  TextStyle _convertVStyleRangeToTextStyle(VStyleRange vRange) {
    final defaultTextColor = applyAlpha(_textColor);
    final baseStyle = _getDefaultTextStyle();

    Color? foreground;
    if (vRange.foreground != null) {
      foreground = colorFromVColor(vRange.foreground);
    }

    Color? background;
    if (vRange.background != null) {
      background = colorFromVColor(vRange.background);
    }

    final (fontWeight, fontStyle) = FontUtils.convertSwtFontStyle(
      vRange.fontStyle,
    );

    // A range's own font, measured the way the widget font is — the same point-to-pixel
    // conversion, and the font's own bold/italic on top of the range's.
    double? fontSize = baseStyle.fontSize;
    String? fontFamily = baseStyle.fontFamily;
    FontWeight? rangeWeight = fontWeight;
    FontStyle? rangeSlant = fontStyle;
    if (vRange.font?.fontData != null && vRange.font!.fontData!.isNotEmpty) {
      final fontStyleOfRange = FontUtils.textStyleFromVFont(
        vRange.font,
        context,
        applyDpiScaling: true,
      );
      fontSize = fontStyleOfRange.fontSize ?? fontSize;
      fontFamily = fontStyleOfRange.fontFamily ?? fontFamily;
      if (fontStyleOfRange.fontWeight == FontWeight.bold) rangeWeight = FontWeight.bold;
      if (fontStyleOfRange.fontStyle == FontStyle.italic) rangeSlant = FontStyle.italic;
    }

    // A strikeout in its own colour is painted separately (see _strikeoutIsPaintedSeparately).
    TextDecoration? decoration;
    List<TextDecoration> decorations = [];
    if (vRange.underline == true) {
      decorations.add(TextDecoration.underline);
    }
    if (vRange.strikeout == true && !_strikeoutIsPaintedSeparately(vRange)) {
      decorations.add(TextDecoration.lineThrough);
    }
    if (decorations.isNotEmpty) {
      decoration = TextDecoration.combine(decorations);
    }

    // A link takes the platform's link colour unless the range names one of its own, as
    // TextLayout does natively.
    if (foreground == null && vRange.underline == true &&
        vRange.underlineStyle == SWT.UNDERLINE_LINK) {
      foreground = _linkForeground;
    }

    return TextStyle(
      fontSize: fontSize,
      fontFamily: fontFamily,
      fontWeight: rangeWeight,
      fontStyle: rangeSlant,
      color: foreground ?? defaultTextColor,
      backgroundColor: background,
      decoration: decoration,
      decorationStyle: _underlineDecoration(vRange.underlineStyle),
      // SWT lets a run colour its underline and strikeout apart from its text; only one of the
      // two can reach a Flutter TextStyle, so the underline's colour wins when both are set.
      decorationColor: (vRange.underline == true
              ? colorFromVColor(vRange.underlineColor)
              : null) ??
          (vRange.strikeout == true
              ? colorFromVColor(vRange.strikeoutColor)
              : null) ??
          foreground ??
          defaultTextColor,
    );
  }

  /// Whether the strikeout has to be painted apart from the text: SWT lets a run colour its
  /// underline and its strikeout differently, and a TextStyle has one decoration colour for both.
  static bool _strikeoutIsPaintedSeparately(VStyleRange range) =>
      range.strikeout == true &&
      range.underline == true &&
      range.strikeoutColor != null &&
      range.underlineColor != null &&
      colorFromVColor(range.strikeoutColor) != colorFromVColor(range.underlineColor);

  /// The colour a link is painted in when its range names none: the platform's own
  /// (`SWT.COLOR_LINK_FOREGROUND`, which this backend answers with this value).
  static const Color _linkForeground = Color(0xFF0066CC);

  /// SWT's underline styles, as far as a text decoration can draw them: a double line, a wavy one
  /// for the two squiggles, a plain line for the rest.
  static TextDecorationStyle? _underlineDecoration(int? underlineStyle) {
    switch (underlineStyle) {
      case SWT.UNDERLINE_DOUBLE:
        return TextDecorationStyle.double;
      case SWT.UNDERLINE_ERROR:
      case SWT.UNDERLINE_SQUIGGLE:
        return TextDecorationStyle.wavy;
      default:
        return null;
    }
  }

  /// Get default text style from state.font or fallback
  TextStyle _getDefaultTextStyle() {
    final defaultTextColor = applyAlpha(_textColor);

    if (state.font != null) {
       return FontUtils.textStyleFromVFont(
        state.font,
        context,
        color: defaultTextColor,
        applyDpiScaling: true,
      );
    }

    // Fallback to renderer's regular font
    final renderer = state.renderer;
    if (renderer?.regularFont != null) {
      return FontUtils.textStyleFromVFont(
        renderer!.regularFont,
        context,
        color: defaultTextColor,
        applyDpiScaling: true,
      );
    }

    return TextStyle(fontSize: 12, color: defaultTextColor);
  }


  @override
  Widget build(BuildContext context) {
    if (_shapeStale) _rebuildShape();
    _oweGeometry();
    final bounds = getBounds();
    final hasHScroll = hasStyle(state.style, SWT.H_SCROLL);
    final hasVScroll = hasStyle(state.style, SWT.V_SCROLL);
    final showHBar = hasHScroll && (state.horizontalBar?.visible ?? true);
    final showVBar = hasVScroll && (state.verticalBar?.visible ?? true);
    final contentSize = _computeContentSize();
    // The rasterised band: the view plus a quarter screen either side, on a quarter-screen step so
    // scrolling inside it repaints nothing.
    final double viewHeight = bounds.height;
    final double bandMargin = viewHeight / 4;
    final bool banded = viewHeight > 0 && contentSize.height > viewHeight * 1.5;
    final double bandTop = banded
        ? math.max(0.0, ((_scrollOffset.dy - bandMargin) / bandMargin).floorToDouble() * bandMargin)
        : 0.0;
    final double bandHeight = banded
        ? math.min(contentSize.height - bandTop, viewHeight * 1.5)
        : contentSize.height;
    final shape = _lastPainted = _shape;

    final blockSelection = state.blockSelection == true ? state.blockSelectionBounds : null;
    Widget contentLayer = ClipRect(
      child: OverflowBox(
        alignment: Alignment.topLeft,
        minWidth: 0,
        minHeight: 0,
        maxWidth: double.infinity,
        maxHeight: double.infinity,
        child: Transform.translate(
          offset: -_scrollOffset,
          child: SizedBox(
            width: contentSize.width,
            height: contentSize.height,
            child: Stack(children: [Positioned(
              top: bandTop,
              left: 0,
              width: contentSize.width,
              height: bandHeight,
              // Its own band-sized layer, so scrolling inside the band moves it. Clipped because culling reads
              // the canvas clip, which is unbounded inside a repaint boundary.
              child: RepaintBoundary(child: ClipRect(child: CustomPaint(
              painter: ScenePainter(
              originY: bandTop,
                bg,
                List.unmodifiable([
                  if (shape != null) shape,
                  // StyledText#setBlockSelection: a rectangle over the text, not a run of it.
                  if (blockSelection != null && blockSelection.width != 0)
                    _BlockSelectionShape(
                      Rect.fromLTWH(
                        blockSelection.x.toDouble(),
                        blockSelection.y.toDouble(),
                        blockSelection.width.toDouble(),
                        blockSelection.height.toDouble(),
                      ),
                      _selectionColor,
                    ),
                ]),
              ),
            ))),
            )]),
          ),
        ),
      ),
    );
    // The caret has its own layer so its blink, restarted by typing, does not re-rasterise the text.
    contentLayer = Stack(
      fit: StackFit.passthrough,
      children: [
        contentLayer,
        Positioned.fill(
          child: ClipRect(
            child: OverflowBox(
              alignment: Alignment.topLeft,
              minWidth: 0,
              minHeight: 0,
              maxWidth: double.infinity,
              maxHeight: double.infinity,
              child: Transform.translate(
                offset: -_scrollOffset,
                child: SizedBox(
                  width: contentSize.width,
                  height: contentSize.height,
                  child: CustomPaint(
                    painter: _CaretPainter(shape, _focused && _caretOn),
                  ),
                ),
              ),
            ),
          ),
        ),
      ],
    );
    if (hasHScroll || hasVScroll) {
      contentLayer = wrapWithScrollbars(contentLayer, onWheel: _onWheel);
    }

    final trackSize = Theme.of(context)
        .extension<ScrolledCompositeThemeExtension>()!
        .scrollbarThickness;
    final interactionLayer = Focus(
      focusNode: _focusNode,
      onKeyEvent: _onKeyEvent,
      child: Padding(
        padding: EdgeInsets.only(
          right: showVBar ? trackSize : 0,
          bottom: showHBar ? trackSize : 0,
        ),
        child: Container(),
      ),
    );

    // Outermost boundary: a scroll, blink or keystroke re-lays the editor out, and a layout pass
    // repaints up to the nearest boundary above it.
    return RepaintBoundary(
      child: wrap(
        Listener(
          onPointerDown: _onPointerDown,
          child: SizedBox(
            width: bounds.width,
            height: bounds.height,
            child: Stack(
              children: [
                Positioned.fill(child: contentLayer),
                ..._marginStrips(showVBar ? trackSize : 0, showHBar ? trackSize : 0),
                Positioned.fill(child: interactionLayer),
              ],
            ),
          ),
        ),
      ),
    );
  }
  /// StyledText#setMarginColor: the four margins painted in the application's colour, over the
  /// content so they stay put while the text scrolls. Nothing when the application set none.
  List<Widget> _marginStrips(double vTrack, double hTrack) {
    final color = getBackgroundColor(
      background: state.marginColor,
      defaultColor: null,
      context: context,
    );
    if (color == null) return const [];
    final left = (state.leftMargin ?? 0).toDouble();
    final top = (state.topMargin ?? 0).toDouble();
    final right = (state.rightMargin ?? 0).toDouble();
    final bottom = (state.bottomMargin ?? 0).toDouble();
    Widget strip(double? l, double? t, double? r, double? b, double? w, double? h) =>
        Positioned(left: l, top: t, right: r, bottom: b, width: w, height: h,
            child: ColoredBox(color: color));
    return [
      if (left > 0) strip(0, 0, null, hTrack, left, null),
      if (top > 0) strip(0, 0, vTrack, null, null, top),
      if (right > 0) strip(null, 0, vTrack, hTrack, right, null),
      if (bottom > 0) strip(0, null, vTrack, hTrack, null, bottom),
    ];
  }

  /// Where Java has the content scrolled to.
  Offset get _scrollOffset =>
      Offset((state.horizontalPixel ?? 0).toDouble(), (state.topPixel ?? 0).toDouble());

  /// Less than a pixel of vertical wheel travel, held until it adds up to one.
  double _wheelCarry = 0;

  /// Vertical ticks go through SWT.MouseWheel (a listener may veto), horizontal ones move the bar.
  /// An unmodified vertical tick sends pixels rather than lines, so a trackpad does not jump.
  void _onWheel(PointerScrollEvent event) {
    final dy = event.scrollDelta.dy;
    final dx = event.scrollDelta.dx;
    if (dy != 0) {
      final stateMask = moveStateMask(0);
      int? pixels;
      if (stateMask & SWT.MODIFIER_MASK == 0) {
        final exact = _wheelCarry + dy;
        pixels = exact.round();
        _wheelCarry = exact - pixels;
        if (pixels == 0) return;
      }
      widget.sendMouseWheelMouseWheel(
        state,
        VEvent()
          ..x = event.localPosition.dx.round()
          ..y = event.localPosition.dy.round()
          ..count = -_wheelLines(dy)
          ..detail = pixels
          ..stateMask = stateMask,
      );
    }
    final hBar = state.horizontalBar;
    if (dx != 0 && hBar != null && hBar.visible != false && hBar.enabled != false) {
      final minimum = hBar.minimum ?? 0;
      final maximum = hBar.maximum ?? 100;
      final thumb = hBar.thumb ?? 0;
      final next = _hScroll.next(
        reported: hBar.selection ?? minimum,
        lines: _wheelLines(dx),
        increment: hBar.increment ?? 1,
        minimum: minimum,
        maximum: maximum,
        thumb: thumb,
      );
      if (next != null) {
        sendScrollBarSelection(hBar, next, dx > 0 ? SWT.ARROW_DOWN : SWT.ARROW_UP);
      }
    }
  }

  final HorizontalWheelScroll _hScroll = HorizontalWheelScroll();

  /// Lines per wheel event: three per 120-unit notch, as a desktop scrolls by default.
  static int _wheelLines(double delta) {
    final lines = (delta / 40).round();
    return lines != 0 ? lines : (delta > 0 ? 1 : -1);
  }

  Size _computeContentSize() {
    final text = state.text ?? '';
    if (text.isEmpty) return getBounds();
    final defaultStyle = _getDefaultTextStyle();
    final fontSize = defaultStyle.fontSize ?? 12.0;
    final javaAscent = state.renderer?.ascent ?? 0;
    final javaDescent = state.renderer?.descent ?? 0;
    final lineHeight = (javaAscent + javaDescent) > 0
        ? (javaAscent + javaDescent).toDouble()
        : fontSize * 1.4;
    _ensureTextMetrics(text);
    final totalHeight =
        math.max(getBounds().height, _metricsLineCount * lineHeight);

    double totalWidth = getBounds().width;
    if (state.wordWrap != true) {
      final charWidth = fontSize * 0.6;
      totalWidth =
          math.max(getBounds().width, _metricsMaxLineLength * charWidth + 20);
    }

    return Size(totalWidth, totalHeight);
  }

  // ---- Text geometry push (Java answers its position API from this) ----
  //
  // Java's StyledText position API (getLocationAtOffset, getOffsetAtPoint,
  // getLinePixel, getLineIndex, getTextBounds) repeats back what is painted here
  // instead of estimating it from glyph tables, and its estimate knows nothing
  // about wrapping — so a layout that is never described is a wrap-blind Java side.
  //
  // CanvasImpl calls this hook from its paint request, and this class replaces
  // CanvasImpl.build, so build() drives it too.
  //
  // Two gates, because the payload is one layout per line plus a per-character x array
  // for the document: the holder gate skips caret-blink copies sharing the layout holder,
  // the payload gate a rebuilt shape whose layout did not change.
  _LineTopsCache? _geometrySentForLayout;
  /// Which table the other side holds: it names this version when it applies the next splice.
  int _geometryVersion = 0;
  bool _geometryFullNeeded = true;
  double? _geometrySentBandTop;
  double? _geometrySentBandBottom;
  GeometryTable? _geometrySent;

  /// Comparing the widget's text style is expensive, so layout keys carry a count of its changes.
  TextStyle? _lastDefaultStyle;
  int _styleToken = 0;

  TextStyle _stableDefaultStyle() {
    final built = _getDefaultTextStyle();
    final last = _lastDefaultStyle;
    if (last == null || built != last) {
      _lastDefaultStyle = built;
      _styleToken++;
      return built;
    }
    return last;
  }

  /// Microseconds the last geometry computation and shape rebuild took.
  @visibleForTesting
  static int debugGeometryMicros = 0;
  @visibleForTesting
  static int debugGeometryCalls = 0;
  @visibleForTesting
  static int debugRebuildMicros = 0;
  @visibleForTesting
  static int debugRebuildCalls = 0;

  /// Test seam: the transport is a no-op in a widget test, so a push is otherwise
  /// unobservable. Counts every payload pushed and keeps the last one.
  @visibleForTesting
  static int debugGeometryPushes = 0;
  static GeometryTable? _debugLastTable;
  @visibleForTesting
  static Map<String, dynamic>? get debugLastGeometry => _debugLastTable?.toJson();
  @visibleForTesting
  static set debugLastGeometry(Map<String, dynamic>? cleared) {
    assert(cleared == null, 'only clearing is supported');
    _debugLastTable = null;
  }

  @override
  void beforePaintRequest() {
    final shape = _currentTextShape();
    if (shape == null) return;
    // Quantized so scrolling inside the band sends no new geometry.
    final step = (shape.lineHeight > 0 ? shape.lineHeight : 16) * TextShape._charXRowMargin;
    final height = shape.canvasSize?.height ?? 0;
    final top = ((_scrollOffset.dy / step).floor() - 1) * step.toDouble();
    final bottom = (((_scrollOffset.dy + height) / step).ceil() + 1) * step.toDouble();
    if (identical(shape._lineTopsCache, _geometrySentForLayout) &&
        top == _geometrySentBandTop &&
        bottom == _geometrySentBandBottom) {
      return;
    }
    final geometryWatch = Stopwatch()..start();
    final geometry = perfMarkValue('StyledText.geometryTable',
        () => shape.computeGeometryTable(visibleTop: top, visibleBottom: bottom));
    debugGeometryMicros += geometryWatch.elapsedMicroseconds;
    debugGeometryCalls++;
    _geometrySentForLayout = shape._lineTopsCache;
    _geometrySentBandTop = top;
    _geometrySentBandBottom = bottom;
    _delimiters.geometryToDocument(geometry);
    final previous = _geometrySent;
    // One pass answers both whether anything changed and what to send.
    final splice = (previous == null || _geometryFullNeeded)
        ? null
        : _spliceOf(previous.rows, geometry.rows);
    if (splice != null &&
        splice.rowsReplaced == 0 &&
        splice.rows.isEmpty &&
        previous!.sameExtentsAs(geometry)) {
      return;
    }
    final payload = _geometryPayload(geometry, splice);
    _geometrySent = geometry;
    debugGeometryPushes++;
    _debugLastTable = geometry;
    EquoCommService.sendPayload(
      "${state.swt}/${state.id}/TextGeometry",
      payload,
    );
  }

  /// Worked out after the frame, since the table only answers Java's position queries: owed ahead
  /// of this client's next send ([EquoCommBase.owe]), else pushed right after the frame.
  void _oweGeometry() {
    EquoCommBase.owe(_pushOwedGeometry);
    _geometryTimer?.cancel();
    _geometryTimer = Timer(Duration.zero, () {
      _geometryTimer = null;
      EquoCommBase.payOwed();
    });
  }

  Timer? _geometryTimer;

  void _pushOwedGeometry() {
    if (mounted) beforePaintRequest();
  }

  /// Java holds no table it can apply a change to: describe the whole layout again, now.
  void _sendWholeGeometry() {
    if (!mounted) return;
    _geometryFullNeeded = true;
    _geometrySent = null;
    _geometrySentForLayout = null;
    beforePaintRequest();
  }

  /// What to send for [table]: the splice from the table the other side holds, or the whole thing.
  Map<String, dynamic> _geometryPayload(GeometryTable table, _GeometrySplice? splice) {
    final version = ++_geometryVersion;
    // A splice that rewrites most of the table (a resize, a wrap toggle) is the table.
    if (splice == null || splice.rows.length * 2 > table.rows.length) {
      if (splice == null) _geometryFullNeeded = false;
      return <String, dynamic>{...table.toJson(), 'v': version};
    }
    return <String, dynamic>{
      'v': version,
      'base': version - 1,
      'charCount': table.charCount,
      'contentWidth': table.contentWidth,
      'contentHeight': table.contentHeight,
      'rowStart': splice.rowStart,
      'rowsReplaced': splice.rowsReplaced,
      'lineDelta': splice.lineDelta,
      'offsetDelta': splice.offsetDelta,
      'yDelta': splice.yDelta,
      'rows': [for (final row in splice.rows) row.toJson()],
    };
  }

  /// The rows that changed between [before] and [after], as a replacement of one run of rows plus
  /// the constant shift the rows after it took.
  _GeometrySplice _spliceOf(List<GeometryRow> before, List<GeometryRow> after) {
    int prefix = 0;
    while (prefix < before.length &&
        prefix < after.length &&
        after[prefix].sameAs(before[prefix])) {
      prefix++;
    }
    int suffix = 0;
    int? lineDelta;
    int? offsetDelta;
    double? yDelta;
    while (suffix < before.length - prefix && suffix < after.length - prefix) {
      final b = before[before.length - 1 - suffix];
      final a = after[after.length - 1 - suffix];
      lineDelta ??= a.l - b.l;
      offsetDelta ??= a.s - b.s;
      yDelta ??= a.y - b.y;
      if (!a.isShiftOf(b, lineDelta, offsetDelta, yDelta)) break;
      suffix++;
    }
    return _GeometrySplice(
      prefix,
      before.length - prefix - suffix,
      lineDelta ?? 0,
      offsetDelta ?? 0,
      yDelta ?? 0,
      after.sublist(prefix, after.length - suffix),
    );
  }

  TextShape? _currentTextShape() {
    if (_shapeStale && mounted) _rebuildShape();
    return _shape;
  }

  /// What is on screen, positioned as (line, column) so the description does not depend on how
  /// line delimiters are stored. Rects are in the widget's own coordinates, after scrolling.
  @override
  Map<String, dynamic> debugRenderFacts() {
    final scrollX = _scrollOffset.dx;
    final scrollY = _scrollOffset.dy;
    final facts = <String, dynamic>{
      'focused': _focused,
      'direction': hasStyle(state.style, SWT.RIGHT_TO_LEFT) ? 'rtl' : 'ltr',
      'backgroundImage': state.backgroundImage != null,
      'scroll': {'x': scrollX, 'y': scrollY},
      'carets': <Map<String, dynamic>>[],
      'selections': <Map<String, dynamic>>[],
      'selectionRects': <Map<String, double>>[],
      'lineBackgrounds': <Map<String, dynamic>>[],
      'bullets': <Map<String, dynamic>>[],
    };
    final shape = _currentTextShape();
    if (shape == null) return facts;

    final lines = _linesOf(shape.text);
    final starts = <int>[0];
    for (int i = 0; i < lines.length - 1; i++) {
      starts.add(starts[i] + lines[i].length + 1);
    }
    Map<String, int> position(int offset) {
      final clamped = offset.clamp(0, shape.text.length);
      int line = 0;
      while (line + 1 < starts.length && starts[line + 1] <= clamped) {
        line++;
      }
      return {'line': line, 'column': clamped - starts[line]};
    }

    Map<String, double> viewport(Rect r) => {
          'x': r.left - scrollX,
          'y': r.top - scrollY,
          'width': r.width,
          'height': r.height,
        };

    facts['lines'] = lines;
    facts['lineBackgrounds'] = [
      for (final entry in shape.editingState?.lineProperties.entries ?? const <MapEntry<int, LineProperties>>[])
        if (entry.value.background != null)
          {'line': entry.key.toDouble(), 'color': _hex(entry.value.background)},
    ];
    final caret = shape.caretInfo;
    if (caret != null && _focused) {
      final shown = shape.copyWithCaret(caret.copyWith(visible: true));
      facts['carets'] = [
        for (final offset in shape.allCarets)
          {
            ...position(offset),
            if (shown.caretRectAt(offset) != null) 'rect': viewport(shown.caretRectAt(offset)!),
            'color': _hex(caret.color),
          },
      ];
    }
    // Canvas#getCaret() as it arrived, to tell a wrong caret size from one that never arrived.
    final widgetCaret = state.caret?.bounds;
    if (widgetCaret != null) {
      facts['widgetCaret'] = {
        'width': widgetCaret.width?.toDouble() ?? 0.0,
        'height': widgetCaret.height?.toDouble() ?? 0.0,
      };
    }
    // Layout cache hits and misses this frame.
    facts['layout'] = {
      'laidOut': TextShape.debugLayoutLineCalls.toDouble(),
      'cached': debugLineLayoutHits.toDouble(),
      'painters': TextShape.debugPainterLayouts.toDouble(),
      'paintersLastDraw': TextShape.debugPaintersLastDraw.toDouble(),
      'draws': TextShape.debugDrawCalls.toDouble(),
      'geometryMs': debugGeometryMicros / 1000.0,
      'lineTopsMs': TextShape.debugLineTopsMicros / 1000.0,
      'drawMs': TextShape.debugDrawMicros / 1000.0,
      'drawCalls': TextShape.debugDrawCalls.toDouble(),
      'geometryCalls': debugGeometryCalls.toDouble(),
      'rebuildCalls': debugRebuildCalls.toDouble(),
      'rebuildMs': debugRebuildMicros / 1000.0,
    };
    // The lines the last paint drew; a line the client shows must be in here.
    facts['paintedLines'] = [for (final line in TextShape.debugPaintedLines) line.toDouble()];
    // Where each painted line landed in the widget; must agree with Java's getLinePixel.
    facts['paintedRowY'] = [
      for (final row in TextShape.debugPaintedRows)
        {'line': row[0], 'y': row[1] - scrollY},
    ];
    // StyledText#setLineBullet: which lines carry one, and what it paints.
    facts['bullets'] = [
      for (final entry in shape.editingState?.lineProperties.entries ?? const <MapEntry<int, LineProperties>>[])
        if (entry.value.bulletText != null)
          {'line': entry.key.toDouble(), 'text': entry.value.bulletText},
    ];
    final block = state.blockSelection == true ? state.blockSelectionBounds : null;
    if (block != null && block.width != 0 && block.height != 0) {
      facts['blockSelection'] = {
        'x': block.x.toDouble(),
        'y': block.y.toDouble(),
        'width': block.width.toDouble(),
        'height': block.height.toDouble(),
      };
    }
    final painted = shape.allSelections.where((s) => s.hasSelection).toList();
    if (painted.isNotEmpty) {
      facts['selections'] = [
        for (final selection in painted)
          {
            'start': position(selection.normalizedStart),
            'end': position(selection.normalizedEnd),
          },
      ];
      facts['selectionRects'] = [
        for (final r in _lastPainted?.debugPaintedSelectionRects ?? const <Rect>[]) viewport(r),
      ];
      final selection = painted.first;
      facts['selectionBackground'] =
          _hex(selection.selectionColor);
      facts['selectionForeground'] = _hex(selection.selectionForeground);
    }
    const runLineLimit = 500;
    final runs = <List<Map<String, dynamic>>>[];
    for (int i = 0; i < lines.length && i < runLineLimit; i++) {
      final leaves = <_StyledRun>[];
      shape._flattenSpan(shape._getTextSpanForLine(lines[i], i), shape.style, leaves);
      int column = 0;
      final lineRuns = <Map<String, dynamic>>[];
      for (final leaf in leaves) {
        lineRuns.add({
          'start': column,
          'end': column + leaf.text.length,
          ..._describeTextStyle(leaf.style),
          // Attributes SWT names and a TextStyle cannot carry, from the range this run came from.
          ...shape.debugSwtAttributesAt(starts[i] + column),
        });
        column += leaf.text.length;
      }
      runs.add(lineRuns);
    }
    facts['runs'] = runs;
    return facts;
  }

  static String? _hex(Color? c) =>
      c == null ? null : '#${c.toARGB32().toRadixString(16).padLeft(8, '0')}';

  /// A painted run in SWT's `StyleRange` terms. `underlineStyle` is the `SWT.UNDERLINE_*` value the
  /// painted decoration corresponds to.
  static Map<String, dynamic> _describeTextStyle(TextStyle style) {
    final decoration = style.decoration;
    final underline = decoration != null && decoration.contains(TextDecoration.underline);
    final strikeout = decoration != null && decoration.contains(TextDecoration.lineThrough);
    final int? underlineStyle = !underline
        ? null
        : switch (style.decorationStyle) {
            TextDecorationStyle.double => SWT.UNDERLINE_DOUBLE,
            TextDecorationStyle.wavy => SWT.UNDERLINE_SQUIGGLE,
            _ => SWT.UNDERLINE_SINGLE,
          };
    return {
      'foreground': _hex(style.color),
      'background': _hex(style.backgroundColor ?? style.background?.color),
      'bold': (style.fontWeight?.value ?? 400) >= 600,
      'italic': style.fontStyle == FontStyle.italic,
      'fontSize': style.fontSize,
      'fontFamily': style.fontFamily,
      'underline': underline,
      'underlineStyle': underlineStyle,

      'underlineColor': underline ? _hex(style.decorationColor ?? style.color) : null,
      'strikeout': strikeout,
      'strikeoutColor': strikeout ? _hex(style.decorationColor ?? style.color) : null,
    };
  }
}

/// Where a document's CRLF delimiters are: the client shows each as one character, so offsets
/// are shifted by the pairs before them.
class _Delimiters {
  const _Delimiters._(this._crlf);

  static const none = _Delimiters._([]);

  /// Document offset of each "\r" that starts a "\r\n", ascending.
  final List<int> _crlf;

  factory _Delimiters.of(String text) {
    if (!text.contains('\r')) return none;
    final crlf = <int>[];
    for (int i = 0; i + 1 < text.length; i++) {
      if (text.codeUnitAt(i) == 0x0D && text.codeUnitAt(i + 1) == 0x0A) crlf.add(i);
    }
    return _Delimiters._(crlf);
  }

  static String normalize(String text) => text.contains('\r')
      ? text.replaceAll('\r\n', '\n').replaceAll('\r', '\n')
      : text;

  /// How many pairs start before [offset].
  int _before(int offset) {
    int lo = 0, hi = _crlf.length;
    while (lo < hi) {
      final mid = (lo + hi) >> 1;
      if (_crlf[mid] < offset) {
        lo = mid + 1;
      } else {
        hi = mid;
      }
    }
    return lo;
  }

  int toShown(int offset) => _crlf.isEmpty || offset <= 0 ? offset : offset - _before(offset);

  int toDocument(int shown) {
    if (_crlf.isEmpty || shown <= 0) return shown;
    // Pair k is shown at _crlf[k] - k; every pair shown before [shown] adds one.
    int lo = 0, hi = _crlf.length;
    while (lo < hi) {
      final mid = (lo + hi) >> 1;
      if (_crlf[mid] - mid < shown) {
        lo = mid + 1;
      } else {
        hi = mid;
      }
    }
    return shown + lo;
  }

  /// Rewrites a geometry table computed over the shown text into document offsets.
  void geometryToDocument(GeometryTable geometry) {
    if (_crlf.isEmpty) return;
    geometry.charCount = toDocument(geometry.charCount);
    for (final row in geometry.rows) {
      row.s = toDocument(row.s);
      row.e = toDocument(row.e);
    }
  }
}


// Stands in for one '\t'. Its width comes from the PlaceholderDimensions computed per
// tab, so the child is never measured or painted.
const WidgetSpan _tabPlaceholder = WidgetSpan(
  alignment: PlaceholderAlignment.baseline,
  baseline: TextBaseline.alphabetic,
  child: SizedBox.shrink(),
);

class _StyledRun {
  final String text;
  final TextStyle style;

  const _StyledRun(this.text, this.style);
}

class _TabExpandedLine {
  final TextSpan span;

  /// One entry per tab placeholder in [span], in paragraph order. Empty when the
  /// line has no tabs, in which case [span] is the untouched original.
  final List<PlaceholderDimensions> tabStops;

  const _TabExpandedLine(this.span, this.tabStops);
}

/// Prefix-sum of line heights for one layout identity, so finding a line's y is
/// a lookup instead of one TextPainter layout per preceding line on every paint.
/// Blink and selection copies change no layout input and share the holder; any
/// copy that changes text, style, wrap or line properties gets a fresh one.
class _LineTopsCache {
  /// tops[i] = y of logical line i relative to the text origin,
  /// tops[lineCount] = total content height. Null until first computed.
  List<double>? tops;
}

/// What the geometry table and the line-tops prefix sum need from one logical line's
/// layout. Every field is a pure function of the [_LineLayoutKey] it is stored under,
/// so a cached entry cannot go stale: a line whose text, styles, alignment, wrap width
/// or tab width changes hashes to a different key.
class _LineLayout {
  _LineLayout(this.width, this.height, this.metrics, this.rowEnds, this.cost);

  /// The line's length in characters — what this entry is charged against the cache
  /// budget, whether or not [caretX] has been materialized yet.
  final int cost;

  final double width;
  final double height;
  final List<LineMetrics> metrics;

  /// For a wrapped line, the end offset of each visual row but the last.
  final List<int> rowEnds;

  /// x of every character boundary, relative to the line's own origin. Materialized on
  /// first use because the geometry payload skips it past [TextShape._charXPayloadLimit].
  List<double>? caretX;
}

/// Everything a line's layout depends on. [style] rides along because it is the
/// fallback style tab expansion measures its runs with, and [tabs] because a tab
/// advances to the next multiple of that many columns.
class _LineLayoutKey {
  const _LineLayoutKey(
    this.lineText,
    this.styleToken,
    this.rangesToken,
    this.align,
    this.maxWidth,
    this.tabs,
    this.tabStops,
  );

  /// The line's text plus tokens for everything else its layout depends on. Not the span itself:
  /// a TextSpan hashes and compares through its TextStyle, field by field.
  final String lineText;
  final int styleToken;
  final int rangesToken;
  final TextAlign align;
  final double maxWidth;
  final int tabs;
  final List<int>? tabStops;

  @override
  bool operator ==(Object other) =>
      other is _LineLayoutKey &&
      other.styleToken == styleToken &&
      other.rangesToken == rangesToken &&
      other.align == align &&
      other.maxWidth == maxWidth &&
      other.tabs == tabs &&
      other.lineText == lineText &&
      const ListEquality<int>().equals(other.tabStops, tabStops);

  @override
  int get hashCode => Object.hash(
      lineText, styleToken, rangesToken, align, maxWidth, tabs, const ListEquality<int>().hash(tabStops));
}

/// A keystroke rewrites one logical line and leaves the rest of the document laid out
/// exactly as before, but every shape it produces is a new object that re-derives all of
/// them — one TextPainter layout per line plus, for the geometry table, one
/// getOffsetForCaret per character of the document. Keying the result by the layout's
/// own inputs turns those back into lookups for the lines the edit did not touch.
///
/// Insertion-ordered, so the eldest entry is the least recently used once a hit
/// re-inserts its key. The budget counts characters rather than entries because the
/// per-character x array is what the memory goes into.
final Map<_LineLayoutKey, _LineLayout> _lineLayoutCache = {};
int _lineLayoutCacheChars = 0;
const int _lineLayoutCacheCharBudget = 200000;

void _cacheLineLayout(_LineLayoutKey key, _LineLayout layout) {
  // The replaced entry stops counting, so a re-inserted line is charged to the budget once.
  final replaced = _lineLayoutCache.remove(key);
  if (replaced != null) {
    _lineLayoutCacheChars -= replaced.cost;
    // Same key, same layout: keep the caret positions an earlier pass measured.
    layout.caretX ??= replaced.caretX;
  }
  _lineLayoutCache[key] = layout;
  _lineLayoutCacheChars += layout.cost;
  while (_lineLayoutCacheChars > _lineLayoutCacheCharBudget &&
      _lineLayoutCache.length > 1) {
    final evicted = _lineLayoutCache.remove(_lineLayoutCache.keys.first);
    if (evicted != null) _lineLayoutCacheChars -= evicted.cost;
  }
}

/// What every run drawn in one palette entry shares, decoded once for the lot of them.
class _PaletteStyle {
  const _PaletteStyle({
    required this.style,
    required this.underlineStyle,
    required this.borderStyle,
    required this.borderColor,
    required this.glyphWidth,
    required this.underlineColor,
    required this.strikeoutColor,
    required this.paintsStrikeout,
    required this.rise,
  });

  final TextStyle style;
  final int? underlineStyle;
  final int? borderStyle;
  final Color? borderColor;
  final double? glyphWidth;
  final Color? underlineColor;
  final Color? strikeoutColor;
  final bool paintsStrikeout;
  final int rise;
}

/// Draws a widget's carets over its text, so a blink does not re-rasterise the text.
class _CaretPainter extends CustomPainter {
  const _CaretPainter(this.shape, this.visible);

  final TextShape? shape;
  final bool visible;

  @override
  void paint(Canvas canvas, Size size) {
    if (visible) shape?.drawCarets(canvas);
  }

  @override
  bool shouldRepaint(_CaretPainter old) =>
      old.visible != visible || !identical(old.shape, shape);
}

/// Laid-out paragraphs for painted lines, reused across scrolls; disposed on eviction. Keyed by
/// layout inputs and paint styles too, since a paragraph bakes in its colours.
final Map<_PaintedLineKey, TextPainter> _paintedLineCache = {};

/// Enough that scrolling back finds a line, bounded so a long document cannot hold one per line.
const int _paintedLineCacheEntries = 240;

/// A laid-out paragraph's identity: the layout it was measured under, plus the runs it was
/// painted in. Two lines that agree on both draw the same pixels.
class _PaintedLineKey {
  const _PaintedLineKey(this.layout, this.paintToken);

  final _LineLayoutKey layout;
  final int paintToken;

  @override
  bool operator ==(Object other) =>
      other is _PaintedLineKey &&
      other.paintToken == paintToken &&
      other.layout == layout;

  @override
  int get hashCode => Object.hash(layout, paintToken);
}

void _cachePaintedLine(_PaintedLineKey key, TextPainter painter) {
  _paintedLineCache[key] = painter;
  while (_paintedLineCache.length > _paintedLineCacheEntries) {
    final oldest = _paintedLineCache.keys.first;
    _paintedLineCache.remove(oldest)?.dispose();
  }
}

/// The document's lines and line starts, memoised by the text's identity. Two slots, because an
/// edit is laid out against the text before it (TextShape._lineDonor).
String? _lineCacheText;
List<String>? _lineCacheLines;
List<int>? _lineCacheStarts;
String? _lineCachePrevText;
List<String>? _lineCachePrevLines;
List<int>? _lineCachePrevStarts;

void _fillLineCache(String text) {
  if (identical(_lineCacheText, text) && _lineCacheLines != null) return;
  if (identical(_lineCachePrevText, text) && _lineCachePrevLines != null) {
    final t = _lineCacheText, l = _lineCacheLines, st = _lineCacheStarts;
    _lineCacheText = _lineCachePrevText;
    _lineCacheLines = _lineCachePrevLines;
    _lineCacheStarts = _lineCachePrevStarts;
    _lineCachePrevText = t;
    _lineCachePrevLines = l;
    _lineCachePrevStarts = st;
    return;
  }
  _lineCachePrevText = _lineCacheText;
  _lineCachePrevLines = _lineCacheLines;
  _lineCachePrevStarts = _lineCacheStarts;
  final lines = text.split('\n');
  final starts = List<int>.filled(lines.length, 0);
  int at = 0;
  for (int i = 0; i < lines.length; i++) {
    starts[i] = at;
    at += lines[i].length + 1;
  }
  _lineCacheText = text;
  _lineCacheLines = lines;
  _lineCacheStarts = starts;
}

List<String> _linesOf(String text) {
  _fillLineCache(text);
  return _lineCacheLines!;
}

List<int> _lineStartsOf(String text) {
  _fillLineCache(text);
  return _lineCacheStarts!;
}

/// Perf counters for every StyledText, reset on read; exposed as `window.evolveTest.styledTextPerf()`.
Map<String, dynamic> styledTextPerfSnapshot() {
  final snapshot = <String, dynamic>{
    'paints': TextShape.debugDrawCalls,
    'paintMs': TextShape.debugDrawMicros / 1000.0,
    'lineTopsMs': TextShape.debugLineTopsMicros / 1000.0,
    'linesLaidOut': TextShape.debugLayoutLineCalls,
    'linesFromCache': debugLineLayoutHits,
    'lineKeyMisses': debugLineKeyMisses,
    'lineCaretMisses': debugLineCaretMisses,
    'textPaintersBuilt': TextShape.debugPainterLayouts,
    'paintersInLastPaint': TextShape.debugPaintersLastDraw,
    'linesInLastPaint': TextShape.debugPaintedLines.length < 2
        ? 0
        : TextShape.debugPaintedLines[1] - TextShape.debugPaintedLines[0] + 1,
    'marksEnabled': perfMarksEnabled,
  };
  TextShape.debugDrawCalls = 0;
  TextShape.debugDrawMicros = 0;
  TextShape.debugLineTopsMicros = 0;
  TextShape.debugLayoutLineCalls = 0;
  debugLineLayoutHits = 0;
  debugLineKeyMisses = 0;
  debugLineCaretMisses = 0;
  TextShape.debugPainterLayouts = 0;
  return snapshot;
}

/// Test seam: layouts served from the cache instead of re-derived.
@visibleForTesting
int debugLineLayoutHits = 0;
int debugLineKeyMisses = 0;
int debugLineCaretMisses = 0;

@visibleForTesting
void debugResetLineLayoutCache() {
  _lineLayoutCache.clear();
  _lineLayoutCacheChars = 0;
  for (final painter in _paintedLineCache.values) {
    painter.dispose();
  }
  _paintedLineCache.clear();
  debugLineLayoutHits = 0;
}

class TextShape extends Shape {
  @override
  String describe() => 'StyledText "$text" @ ${off.dx.round()},${off.dy.round()} '
      'font=${style.fontFamily}/${style.fontSize} '
      'color=${style.color == null ? "-" : style.color!.toString()} '
      'wrap=$wordWrap tabs=$tabs lineHeight=$lineHeight'
      '${clipRect == null ? "" : " clip=${clipRect!.left.round()},${clipRect!.top.round()} "
          "${clipRect!.width.round()}x${clipRect!.height.round()}"}';

  final String text;
  final Offset off;
  final TextStyle style;
  final TextSpan? textSpan;
  @override
  final Rect? clipRect;
  final CaretInfo? caretInfo;
  final bool? wordWrap;
  final Size? canvasSize;

  final bool editable;
  final int? styledTextId;
  final Function(String newText, int caretPos, int rangeStart, int rangeEnd, String insertedText)?
  onTextChanged;

  final TextEditingState? editingState;
  final SelectionInfo? selectionInfo;
  // Java-calculated line height (ascent + descent). When > 0, used for
  // currentY advancement so ruler positions stay in sync with Flutter rendering.
  final double lineHeight;
  // StyledText.getTabs(): tab stop spacing in columns. SWT's default is 4.
  final int tabs;
  // StyledText.getTabStops(): absolute stops, which a line's own stops override.
  final List<int>? tabStops;
  // StyledText.getAlignment()/getIndent()/getJustify(): what a line uses when it sets none itself.
  final LineProperties widgetLine;
  // StyledText.getLineSpacing(): space below each line.
  final double lineSpacing;
  /// The spacing below each line when they differ — a LineSpacingProvider's answers.
  final List<int>? lineSpacings;
  // SWT.RIGHT_TO_LEFT
  final TextDirection direction;
  // SWT.FULL_SELECTION: a selected line is highlighted to the right edge.
  final bool fullSelection;
  /// Every caret the widget has; empty means the one [caretInfo] names.
  final List<int> carets;
  /// Every selected range; empty means the one [selectionInfo] names.
  final List<SelectionInfo> selections;

  TextShape(
    this.text,
    this.off,
    this.style, [
    this.clipRect,
    this.textSpan,
    this.caretInfo,
    this.wordWrap,
    this.canvasSize,
    this.editable = false,
    this.styledTextId,
    this.onTextChanged,
    this.editingState,
    this.selectionInfo,
    this.lineHeight = 0.0,
    this.tabs = 4,
    this.tabStops,
    this.widgetLine = const LineProperties(),
    this.lineSpacing = 0,
    this.lineSpacings,
    this.direction = TextDirection.ltr,
    this.fullSelection = false,
    this.carets = const [],
    this.selections = const [],
    this.styleToken = 0,
  ]);

  /// Stands for [style] in a layout key: a change count, since comparing the style is expensive.
  final int styleToken;

  /// Everything a line's measured height depends on; shapes that agree on it share measurements.
  late final int layoutSignature = Object.hash(
      identityHashCode(text),
      _frameSignature,
      editingState?.layoutSignatureFor(style) ?? 0);

  /// What every line's layout depends on apart from its own text and styling.
  late final int _frameSignature = Object.hash(
      styleToken,
      // The width only matters when wrapping, so a resize is free for a document that does not wrap.
      wordWrap == true ? Object.hash(canvasSize?.width, off.dx) : null,
      tabs,
      const ListEquality<int>().hash(tabStops),
      Object.hash(lineSpacing, lineHeight, direction),
      lineSpacings == null ? 0 : const ListEquality<int>().hash(lineSpacings!),
      widgetLine.indent,
      widgetLine.wrapIndent,
      widgetLine.alignment,
      widgetLine.justify);

  /// Takes over what [other] measured when both lay out the same way. After an edit, [other] answers
  /// for the lines the edit did not reach ([_donatedLine]).
  void inheritMeasurementsFrom(TextShape other) {
    if (other.layoutSignature == layoutSignature) {
      _lineTopsCache = other._lineTopsCache;
      _measuredLineCache = other._measuredLineCache;
      _linePropsCache = other._linePropsCache;
      _rangesTokenCache = other._rangesTokenCache;
      return;
    }
    if (other._frameSignature == _frameSignature && other._measuredLineCache != null) {
      _lineDonor = other;
      // One generation back is all a donor is for; holding the chain would hold every document.
      other._lineDonor = null;
    }
  }

  /// The shape before an edit, and how its lines line up with this one's: the first
  /// [_donorPrefix] and last [_donorSuffix] lines are the same text in both.
  TextShape? _lineDonor;
  int _donorPrefix = -1;
  int _donorSuffix = 0;
  int _donorLineCount = 0;
  int _ownLineCount = 0;
  List<int> _donorStarts = const [];
  List<int> _ownStarts = const [];

  void _matchDonorLines(TextShape donor) {
    // The split is memoised for one text at a time: take the donor's first, so this shape's own
    // is the one left in place for the pass that follows.
    final theirs = _linesOf(donor.text);
    _donorStarts = _lineStartsOf(donor.text);
    final mine = _linesOf(text);
    _ownStarts = _lineStartsOf(text);
    final limit = math.min(mine.length, theirs.length);
    var prefix = 0;
    while (prefix < limit && mine[prefix] == theirs[prefix]) {
      prefix++;
    }
    var suffix = 0;
    while (suffix < limit - prefix &&
        mine[mine.length - 1 - suffix] == theirs[theirs.length - 1 - suffix]) {
      suffix++;
    }
    _donorPrefix = prefix;
    _donorSuffix = suffix;
    _donorLineCount = theirs.length;
    _ownLineCount = mine.length;
  }

  /// [lineIndex]'s layout as the pre-edit shape measured it, when its text, runs, line properties
  /// and tab stops are unchanged.
  _LineLayout? _donatedLine(int lineIndex, String lineText, bool wantCaretX) {
    final donor = _lineDonor;
    if (donor == null) return null;
    if (_donorPrefix < 0) _matchDonorLines(donor);
    final int theirs;
    if (lineIndex < _donorPrefix) {
      theirs = lineIndex;
    } else if (lineIndex >= _ownLineCount - _donorSuffix) {
      theirs = lineIndex - (_ownLineCount - _donorLineCount);
    } else {
      return null;
    }
    final memo = donor._measuredLineCache;
    if (memo == null || theirs < 0 || theirs >= memo.length) return null;
    final layout = memo[theirs];
    if (layout == null || (wantCaretX && layout.caretX == null)) return null;
    if (!_sameRunsAs(donor, theirs, lineIndex, lineText.length) ||
        donor._linePropsFor(theirs) != _linePropsFor(lineIndex) ||
        !const ListEquality<int>().equals(donor._tabStopsFor(theirs), _tabStopsFor(lineIndex))) {
      return null;
    }
    // The runs are the donor's, so their token is too.
    final tokens = donor._rangesTokenCache;
    if (tokens != null && theirs < tokens.length && tokens[theirs] != null) {
      final mine = _rangesTokenCache ??= List<int?>.filled(_lineStarts.length + 1, null);
      if (lineIndex < mine.length) mine[lineIndex] = tokens[theirs];
    }
    return layout;
  }

  /// Whether the line carries the same style objects at the same offsets as [donor]'s line
  /// [theirs]; palette entries are shared objects, so no hashing is needed.
  bool _sameRunsAs(TextShape donor, int theirs, int lineIndex, int lineLength) {
    final a = theirs < donor._rangesByLine.length ? donor._rangesByLine[theirs] : const <StyleRange>[];
    final b = lineIndex < _rangesByLine.length ? _rangesByLine[lineIndex] : const <StyleRange>[];
    if (a.length != b.length) return false;
    if (a.isEmpty) return true;
    final aStart = _donorStarts[theirs];
    final bStart = _ownStarts[lineIndex];
    for (int k = 0; k < a.length; k++) {
      final x = a[k], y = b[k];
      if (!identical(x.style, y.style) ||
          x.glyphWidth != y.glyphWidth ||
          x.rise != y.rise ||
          x.borderStyle != y.borderStyle ||
          math.max(0, x.start - aStart) != math.max(0, y.start - bStart) ||
          math.min(lineLength, x.end - aStart) != math.min(lineLength, y.end - bStart)) {
        return false;
      }
    }
    return true;
  }

  _LineTopsCache _lineTopsCache = _LineTopsCache();

  /// Per-line properties that affect layout, from [editingState] when present.
  /// Computed once and shared by every pass over the document.
  List<({int indent, int wrapIndent, int layoutIndent, TextAlign align, int vIndent, String? bulletText})?>?
      _linePropsCache;
  List<_LineLayout?>? _measuredLineCache;

  ({int indent, int wrapIndent, int layoutIndent, TextAlign align, int vIndent, String? bulletText})
      _linePropsFor(int lineIndex) {
    final memo = _linePropsCache ??= List.filled(_lineStarts.length + 1, null);
    if (lineIndex >= 0 && lineIndex < memo.length) {
      final cached = memo[lineIndex];
      if (cached != null) return cached;
      final built = _buildLinePropsFor(lineIndex);
      memo[lineIndex] = built;
      return built;
    }
    return _buildLinePropsFor(lineIndex);
  }

  /// The layout of [lineIndex] as this shape lays it out, measured once.
  _LineLayout _measuredLine(int lineIndex, String lineText, {bool wantCaretX = false}) {
    final memo = _measuredLineCache ??= List.filled(_lineStarts.length + 1, null);
    final inRange = lineIndex >= 0 && lineIndex < memo.length;
    if (inRange && !wantCaretX) {
      final cached = memo[lineIndex];
      if (cached != null) return cached;
    }
    final donated = _donatedLine(lineIndex, lineText, wantCaretX);
    if (donated != null) {
      if (inRange) memo[lineIndex] = donated;
      return donated;
    }
    final props = _linePropsFor(lineIndex);
    final layout = _lineLayout(lineText, lineIndex,
        align: props.align, maxWidth: _lineMaxWidth(props.layoutIndent), wantCaretX: wantCaretX);
    if (inRange) memo[lineIndex] = layout;
    return layout;
  }

  ({int indent, int wrapIndent, int layoutIndent, TextAlign align, int vIndent, String? bulletText})
      _buildLinePropsFor(int lineIndex) {
    final props = editingState?.lineProperties[lineIndex];
    final indent = props?.indent ?? widgetLine.indent ?? 0;
    final wrapIndent = props?.wrapIndent ?? widgetLine.wrapIndent ?? 0;
    final align = _mapSwtAlignmentToTextAlign(props?.alignment ?? widgetLine.alignment ?? 16384);
    return (
      indent: indent,
      wrapIndent: wrapIndent,
      // A line is laid out in what is left after the wider of the two indents, so neither
      // row is measured against a width it will not be painted in.
      layoutIndent: indent > wrapIndent ? indent : wrapIndent,
      align: (props?.justify ?? widgetLine.justify ?? false) ? TextAlign.justify : align,
      vIndent: props?.verticalIndent ?? 0,
      bulletText: props?.bulletText,
    );
  }

  /// The x a line's first row starts at: its indent, plus what the alignment shifts it by.
  double _lineOriginX(int indent, TextAlign align, double width, double maxW) {
    final x = indent.toDouble();
    if (maxW == double.infinity || width >= maxW) return x;
    switch (align) {
      case TextAlign.center:
        return x + (maxW - width) / 2;
      case TextAlign.right:
        return x + (maxW - width);
      default:
        return x;
    }
  }

  /// StyledText#setLineWrapIndent: the rows a line continues on start at the wrap indent
  /// instead of at the line's own indent.
  double _rowOriginX(({int indent, int wrapIndent, int layoutIndent, TextAlign align, int vIndent, String? bulletText}) props,
          double lineOriginX, int row) =>
      row == 0 ? lineOriginX : lineOriginX - props.indent + props.wrapIndent;

  /// The visual row [dy] falls in, found by each row's own top (`baseline - ascent`) as
  /// getOffsetForCaret reports it; summing row heights is off by the leading.
  int _rowIndexAt(TextPainter tp, double dy) {
    final metrics = tp.computeLineMetrics();
    int row = 0;
    for (int m = 1; m < metrics.length; m++) {
      if (dy + 0.5 < _rowTop(metrics, m)) break;
      row = m;
    }
    return row;
  }

  /// The top of row [m] within the layout. The first row starts at the layout's own top, so the
  /// leading above it is part of the row and not a gap the caret should skip.
  double _rowTop(List<LineMetrics> metrics, int m) =>
      m == 0 ? 0 : metrics[m].baseline - metrics[m].ascent;

  /// The tab stops a line is laid out with: its own, else the widget's.
  List<int>? _tabStopsFor(int lineIndex) =>
      editingState?.lineProperties[lineIndex]?.tabStops ?? tabStops;

  // A line's vertical indent (StyledText.setLineVerticalIndent) is extra space
  // above the line's text, inside the line's own box: the box top stays where the
  // previous line ended, the box grows by the indent, and the glyphs (and caret,
  // selection, hit targets) sit vIndent below the box top. _lineTops holds box
  // tops; every text-coordinate consumer adds vIndent for its line.
  List<double> _lineTopsFor(List<String> lines, Size? canvas) =>
      perfMarkValue('StyledText.lineTops', () => _lineTopsNow(lines, canvas));

  List<double> _lineTopsNow(List<String> lines, Size? canvas) {
    final watch = Stopwatch()..start();
    final tops = List<double>.filled(lines.length + 1, 0);
    double y = 0;
    for (int i = 0; i < lines.length; i++) {
      tops[i] = y;
      final props = _linePropsFor(i);
      y += props.vIndent + _advance(_measuredLine(i, lines[i]).height, i);
    }
    tops[lines.length] = y;
    debugLineTopsMicros += watch.elapsedMicroseconds;
    return tops;
  }

  List<double> _lineTops(List<String> lines) =>
      _lineTopsCache.tops ??= _lineTopsFor(lines, canvasSize);

  /// The first line whose band reaches [y], by bisection over the running totals.
  int _lineAtTop(List<double> tops, double y) {
    if (y <= 0) return 0;
    int lo = 0, hi = tops.length - 1;
    while (lo < hi) {
      final mid = (lo + hi) >> 1;
      if (tops[mid + 1] <= y) {
        lo = mid + 1;
      } else {
        hi = mid;
      }
    }
    return lo;
  }

  // Vertical advance for one logical line. Each logical line is painted by a single
  // TextPainter that may wrap into several visual lines (tpHeight is the wrapped total),
  // so advance by tpHeight; fall back to lineHeight as a floor (empty lines whose painter
  // measures ~0, or to keep a consistent single-line height).
  double _advance(double tpHeight, [int lineIndex = -1]) {
    final lh = lineHeight > 0 ? lineHeight : (style.fontSize ?? 16) * 1.2;
    return (tpHeight > lh ? tpHeight : lh) + _spacingFor(lineIndex);
  }

  /// The space below [lineIndex]: its own when the lines differ, else the widget's.
  double _spacingFor(int lineIndex) {
    final spacings = lineSpacings;
    if (spacings != null && lineIndex >= 0 && lineIndex < spacings.length) {
      return spacings[lineIndex].toDouble();
    }
    return lineSpacing;
  }

  // The single wrap policy for per-line layout, matching draw(): a site that wraps on its own
  // drifts from the painted rows and misplaces hit tests.
  double _lineMaxWidth(int indent, [Size? canvas]) {
    final size = canvas ?? canvasSize;
    if (wordWrap != true || size == null) return double.infinity;
    final maxW = size.width - off.dx - indent.toDouble();
    return maxW > 0 ? maxW : double.infinity;
  }

  @override
  void draw(Canvas c) => perfMarkDetail('StyledText.paint', () => _drawNow(c));

  void _drawNow(Canvas c) {
    final drawWatch = Stopwatch()..start();
    final paintersAtStart = debugPainterLayouts;
    int firstPainted = -1, lastPainted = -1;
    final paintedRows = <List<double>>[];
    if (clipRect != null) {
      c.save();
      c.clipRect(clipRect!);
    }

    final lines = _lines;
    final paintOffset = off;
    double currentY = paintOffset.dy;

    // Lines outside the band are laid out (the running Y needs their height) but not painted. The band
    // comes from the canvas clip because the scroll is a translation above this shape.
    final localClip = c.getLocalClipBounds();
    final cullTop = localClip.top - _cullMargin;
    final cullBottom = localClip.bottom + _cullMargin;

    // Running line tops, kept with the layout; the paint starts and stops at the band's lines.
    final tops = _lineTops(lines);
    int firstLine = _lineAtTop(tops, cullTop - paintOffset.dy);

    for (int i = firstLine; i < lines.length; i++) {
      currentY = paintOffset.dy + tops[i];
      if (currentY > cullBottom) break;
      final line = lines[i];

      final props = _linePropsFor(i);
      final vIndent = props.vIndent;
      final maxW = _lineMaxWidth(props.layoutIndent);

      // The height comes from the measured layout, which the cache answers for; only a line that
      // is going to be painted is worth a live TextPainter.
      final advance = vIndent + _advance(_measuredLine(i, line).height, i);
      if (firstPainted < 0) firstPainted = i;
      lastPainted = i;
      paintedRows.add([i.toDouble(), currentY]);
      final tp = _layoutLine(
        line,
        i,
        align: props.align,
        maxWidth: maxW,
      );

      // StyledText#setLineBackground and the LineBackground listener: the line's own colour,
      // behind the whole line rather than behind its glyphs.
      final lineBackground = editingState?.lineProperties[i]?.background;
      if (lineBackground != null) {
        c.drawRect(
          Rect.fromLTWH(0, currentY, canvasSize?.width ?? tp.width, advance),
          Paint()..color = lineBackground,
        );
      }

      final originX = paintOffset.dx +
          _lineOriginX(props.indent, props.align, tp.width, canvasSize == null ? double.infinity : maxW);
      final rows = tp.computeLineMetrics();
      // A wrap indent shifts only the rows the line continues on, so the layout is painted
      // once per indent, each pass clipped to the rows it applies to.
      final wrapShift = props.wrapIndent - props.indent;
      void paintRows(double x, Rect? band) {
        if (band != null) {
          c.save();
          c.clipRect(band);
        }
        _drawRangeBackgrounds(c, i, line, tp, Offset(x, currentY + vIndent), currentY + advance);
        _paintLineWithRises(c, tp, i, line, Offset(x, currentY + vIndent));
        _drawRangeBorders(c, i, line, tp, Offset(x, currentY + vIndent));
        _drawSeparateStrikeouts(c, i, line, tp, Offset(x, currentY + vIndent));
        if (band != null) c.restore();
      }
      _drawBullet(c, props, paintOffset.dx, currentY + vIndent);
      if (wrapShift == 0 || rows.length < 2) {
        paintRows(originX, null);
      } else {
        final split = currentY + vIndent + rows.first.height;
        final width = canvasSize?.width ?? tp.width;
        paintRows(originX, Rect.fromLTRB(0, currentY, width, split));
        paintRows(originX + wrapShift,
            Rect.fromLTRB(0, split, width, currentY + vIndent + tp.height));
      }
    }

    debugPaintedSelectionRects.clear();
    for (final selection in allSelections) {
      if (selection.hasSelection) _drawSelection(c, selection);
    }

    if (clipRect != null) {
      c.restore();
    }
    debugPaintersLastDraw = debugPainterLayouts - paintersAtStart;
    debugDrawCalls++;
    debugDrawMicros += drawWatch.elapsedMicroseconds;
    debugPaintedLines = firstPainted < 0 ? const [] : [firstPainted, lastPainted];
    debugPaintedRows = paintedRows;
  }

  /// Whether [other] would paint exactly what this shape paints; compared field by field, since a
  /// hash collision shows stale pixels. Carets, empty selections and undrawn fields are left out.
  @override
  bool paintsSameAs(Shape shape) {
    if (identical(this, shape)) return true;
    if (shape is! TextShape) return false;
    final other = shape;
    return other.text == text &&
        other.off == off &&
        other.style == style &&
        other.styleToken == styleToken &&
        other.textSpan == textSpan &&
        other.clipRect == clipRect &&
        other.wordWrap == wordWrap &&
        other.canvasSize == canvasSize &&
        other.lineHeight == lineHeight &&
        other.tabs == tabs &&
        other.lineSpacing == lineSpacing &&
        other.direction == direction &&
        other.fullSelection == fullSelection &&
        const ListEquality<int>().equals(other.tabStops, tabStops) &&
        const ListEquality<int>().equals(other.lineSpacings, lineSpacings) &&
        other.widgetLine.paintsSameAs(widgetLine) &&
        _selectionsSame(other._paintedSelections, _paintedSelections) &&
        _editingSame(other.editingState, editingState);
  }

  List<SelectionInfo> get _paintedSelections =>
      [for (final selection in allSelections) if (selection.hasSelection) selection];

  static bool _selectionSame(SelectionInfo? a, SelectionInfo? b) {
    if (a == null || b == null) return a == b;
    return a.start == b.start &&
        a.end == b.end &&
        a.isActive == b.isActive &&
        a.selectionColor == b.selectionColor &&
        a.selectionForeground == b.selectionForeground;
  }

  static bool _selectionsSame(List<SelectionInfo> a, List<SelectionInfo> b) {
    if (a.length != b.length) return false;
    for (int i = 0; i < a.length; i++) {
      if (!_selectionSame(a[i], b[i])) return false;
    }
    return true;
  }

  static bool _editingSame(TextEditingState? a, TextEditingState? b) {
    if (identical(a, b)) return true;
    if (a == null || b == null) return false;
    return a.paintsSameAs(b);
  }

  /// Just the carets, for the layer over the text, so a blink does not re-rasterise it.
  void drawCarets(Canvas c) {
    final caret = caretInfo;
    if (caret == null) return;
    final clip = clipRect;
    if (clip != null) {
      c.save();
      c.clipRect(clip);
    }
    for (final offset in allCarets) {
      final rect = caretRectAt(offset);
      if (rect != null) {
        c.drawRect(rect, Paint()..color = caret.color..style = PaintingStyle.fill);
      }
    }
    if (clip != null) c.restore();
  }

  /// StyledText#setLineBullet: the bullet sits in the width it reserved before the text, right
  /// aligned against it the way the renderer draws it (8px of margin, as BULLET_MARGIN).
  void _drawBullet(Canvas c,
      ({int indent, int wrapIndent, int layoutIndent, TextAlign align, int vIndent, String? bulletText}) props,
      double originX, double y) {
    final bullet = props.bulletText;
    if (bullet == null || bullet.isEmpty) return;
    final painter = TextPainter(
      text: TextSpan(text: bullet, style: style),
      textDirection: direction,
    )..layout();
    final x = math.max(0.0, props.indent - painter.width - _bulletMargin);
    painter.paint(c, Offset(originX + x, y));
    painter.dispose();
  }

  static const double _bulletMargin = 8;

  /// How far outside the canvas a line is still painted: a line whose glyphs rise above their
  /// own box (a raised style range, a taller font) would otherwise be cut off at the edge.
  static const double _cullMargin = 80;

  /// StyleRange#borderStyle: the outline SWT draws around a range, on the line it falls on.
  /// A run's background fills the height of the row it is on, as SWT's does; the TextStyle's own
  /// covers only the glyphs. The last row reaches [lineBottom], the line's full advance.
  void _drawRangeBackgrounds(
      Canvas c, int lineIndex, String line, TextPainter tp, Offset origin, double lineBottom) {
    if (!_decorations.background) return;
    final lineStart = lineIndex < _lineStarts.length ? _lineStarts[lineIndex] : 0;
    final lineEnd = lineStart + line.length;
    List<LineMetrics>? rows;
    for (final range in _runsOnLine(lineIndex)) {
      final color = range.style.backgroundColor;
      if (color == null || range.start >= lineEnd || range.end <= lineStart) continue;
      rows ??= tp.computeLineMetrics();
      final paint = Paint()..color = color;
      for (final box in tp.getBoxesForSelection(TextSelection(
        baseOffset: math.max(0, range.start - lineStart),
        extentOffset: math.min(line.length, range.end - lineStart),
      ))) {
        final middle = (box.top + box.bottom) / 2;
        var top = box.top, bottom = box.bottom;
        for (var r = 0; r < rows.length; r++) {
          final rowTop = r == 0 ? 0.0 : rows[r].baseline - rows[r].ascent;
          final rowBottom = rows[r].baseline + rows[r].descent;
          if (middle >= rowTop && middle <= rowBottom) {
            top = rowTop;
            bottom = rowBottom;
            if (r == rows.length - 1) bottom = math.max(bottom, lineBottom - origin.dy);
            break;
          }
        }
        c.drawRect(Rect.fromLTRB(origin.dx + box.left, origin.dy + top, origin.dx + box.right,
            origin.dy + bottom), paint);
      }
    }
  }

  void _drawRangeBorders(Canvas c, int lineIndex, String line, TextPainter tp, Offset origin) {
    if (!_decorations.border) return;
    final ranges = _runsOnLine(lineIndex);
    final lineStart = lineIndex < _lineStarts.length ? _lineStarts[lineIndex] : 0;
    final lineEnd = lineStart + line.length;
    for (final range in ranges) {
      if (range.borderStyle == null || range.start >= lineEnd || range.end <= lineStart) continue;
      final boxes = tp.getBoxesForSelection(TextSelection(
        baseOffset: math.max(0, range.start - lineStart),
        extentOffset: math.min(line.length, range.end - lineStart),
      ));
      final paint = Paint()
        ..color = range.borderColor ?? range.style.color ?? style.color ?? const Color(0xFF000000)
        ..style = PaintingStyle.stroke
        ..strokeWidth = 1;
      for (final box in boxes) {
        final rect = Rect.fromLTRB(
          origin.dx + box.left, origin.dy + box.top, origin.dx + box.right, origin.dy + box.bottom);
        if (range.borderStyle == SWT.BORDER_SOLID) {
          c.drawRect(rect, paint);
        } else {
          _strokeDashed(c, rect, paint, range.borderStyle == SWT.BORDER_DOT ? 1.0 : 3.0);
        }
      }
    }
  }

  /// A TextStyle cannot shift a run vertically, so a raised range is painted hidden in the line
  /// (keeping its layout) and again on its own, moved.
  void _paintLineWithRises(Canvas c, TextPainter tp, int lineIndex, String line, Offset origin) {
    final risen = _risenRangesOn(lineIndex, line);
    if (risen.isEmpty) {
      tp.paint(c, origin);
      return;
    }
    final lineStart = lineIndex < _lineStarts.length ? _lineStarts[lineIndex] : 0;
    _paintSpan(c, tp, _isolate(tp.text!, lineStart, risen, hide: true), origin);
    for (final range in risen) {
      _paintSpan(c, tp, _isolate(tp.text!, lineStart, [range], hide: false),
          origin.translate(0, -range.rise.toDouble()));
    }
  }

  List<StyleRange> _risenRangesOn(int lineIndex, String line) {
    if (!_decorations.rise) return const [];
    final ranges = _runsOnLine(lineIndex);
    final lineStart = lineIndex < _lineStarts.length ? _lineStarts[lineIndex] : 0;
    final lineEnd = lineStart + line.length;
    return [
      for (final range in ranges)
        if (range.rise != 0 && range.start < lineEnd && range.end > lineStart) range,
    ];
  }

  /// The line's span with [ranges] hidden, or with everything but them hidden.
  InlineSpan _isolate(InlineSpan span, int lineStart, List<StyleRange> ranges, {required bool hide}) {
    int column = 0;
    bool inRange(int at) =>
        ranges.any((r) => r.start <= lineStart + at && lineStart + at < r.end);
    InlineSpan walk(InlineSpan node) {
      if (node is! TextSpan) {
        column++;
        return node;
      }
      final text = node.text;
      final children = <InlineSpan>[];
      TextSpan? own;
      if (text != null && text.isNotEmpty) {
        final start = column;
        column += text.length;
        final hidden = inRange(start) == hide;
        own = TextSpan(
          text: text,
          style: hidden ? (node.style ?? const TextStyle()).copyWith(color: const Color(0x00000000)) : node.style,
        );
      }
      for (final child in node.children ?? const <InlineSpan>[]) {
        children.add(walk(child));
      }
      if (own != null && children.isEmpty) return own;
      return TextSpan(
        text: own?.text,
        style: own?.style ?? node.style,
        children: children.isEmpty ? null : children,
      );
    }
    return walk(span);
  }

  void _paintSpan(Canvas c, TextPainter measured, InlineSpan span, Offset at) {
    final painter = TextPainter(
      text: span,
      textAlign: measured.textAlign,
      textDirection: direction,
    );
    if (measured.inlinePlaceholderBoxes != null) {
      painter.setPlaceholderDimensions(_placeholderDimensionsOf(measured));
    }
    painter.layout(maxWidth: measured.width);
    painter.paint(c, at);
    painter.dispose();
  }

  /// The strikeouts a text decoration could not carry, in the colour the range asked for.
  void _drawSeparateStrikeouts(Canvas c, int lineIndex, String line, TextPainter tp, Offset origin) {
    if (!_decorations.strikeout) return;
    final ranges = _runsOnLine(lineIndex);
    final lineStart = lineIndex < _lineStarts.length ? _lineStarts[lineIndex] : 0;
    final lineEnd = lineStart + line.length;
    for (final range in ranges) {
      if (!range.paintsStrikeout || range.start >= lineEnd || range.end <= lineStart) continue;
      final boxes = tp.getBoxesForSelection(TextSelection(
        baseOffset: math.max(0, range.start - lineStart),
        extentOffset: math.min(line.length, range.end - lineStart),
      ));
      final paint = Paint()
        ..color = range.strikeoutColor ?? range.style.color ?? const Color(0xFF000000)
        ..strokeWidth = 1;
      for (final box in boxes) {
        final y = origin.dy + (box.top + box.bottom) / 2;
        c.drawLine(Offset(origin.dx + box.left, y), Offset(origin.dx + box.right, y), paint);
      }
    }
  }

  /// A rectangle outlined in [dash]-long strokes, for the dashed and dotted borders.
  static void _strokeDashed(Canvas c, Rect rect, Paint paint, double dash) {
    void along(Offset from, Offset to) {
      final total = (to - from).distance;
      if (total <= 0) return;
      final step = (to - from) / total;
      for (double at = 0; at < total; at += dash * 2) {
        final end = math.min(at + dash, total);
        c.drawLine(from + step * at, from + step * end, paint);
      }
    }
    along(rect.topLeft, rect.topRight);
    along(rect.bottomLeft, rect.bottomRight);
    along(rect.topLeft, rect.bottomLeft);
    along(rect.topRight, rect.bottomRight);
  }

  // Per-char x is shipped only for rows on screen plus this margin; Java estimates the rest.
  static const int _charXPayloadLimit = 20000;
  static const int _charXRowMargin = 20;

  /// Per-visual-line geometry of the document exactly as [draw] paints it, for the
  /// Java side to answer its position API from (the `TextGeometry` push).
  ///
  /// Coordinates are relative to the text origin ([off] is not applied — the Java
  /// side adds margins and scroll offsets). Each entry describes one visual line
  /// after wrapping: `l` logical line, `s`/`e` document offset range, `x`/`y`/`w`/`h`
  /// box, and `cx` the per-character x boundaries (length `e - s + 1`).
  /// Per-character x for one row: `cxu` (origin and step) for a uniform advance, else a list
  /// rounded to a hundredth of a pixel.
  @visibleForTesting
  static Map<String, dynamic> charXEntry(List<double> raw) {
    if (raw.length > 2) {
      final advance = raw[1] - raw[0];
      var uniform = true;
      for (int k = 2; k < raw.length; k++) {
        // Tolerance of half the hundredth of a pixel the explicit list is rounded to.
        if ((raw[k] - raw[k - 1] - advance).abs() > 0.005) {
          uniform = false;
          break;
        }
      }
      // Full precision: every position is derived from the step.
      if (uniform) return {'cxu': [_roundPx(raw[0]), advance]};
    }
    return {'cx': [for (final v in raw) _roundPx(v)]};
  }

  /// Rounded to a hundredth, which Dart writes in its shortest form.
  static double _roundPx(double v) => (v * 100).roundToDouble() / 100;

  Map<String, dynamic>? computeGeometry({double? visibleTop, double? visibleBottom}) =>
      computeGeometryTable(visibleTop: visibleTop, visibleBottom: visibleBottom).toJson();

  GeometryTable computeGeometryTable({double? visibleTop, double? visibleBottom}) {
    final lines = _lines;
    final wholeDocument = text.length <= _charXPayloadLimit || visibleTop == null;
    final top = visibleTop ?? 0;
    final bottom = visibleBottom ?? 0;
    bool rowIsVisible(double y, double h) => wholeDocument || (y + h >= top && y <= bottom);
    final visual = <GeometryRow>[];
    double currentY = 0;
    double maxWidth = 0;
    int docOffset = 0;

    for (int i = 0; i < lines.length; i++) {
      final line = lines[i];

      final props = _linePropsFor(i);
      final vIndent = props.vIndent;
      final maxW = _lineMaxWidth(props.layoutIndent);

      var layout = _measuredLine(i, line, wantCaretX: wholeDocument);
      // Per-character x is worth its weight only for the rows someone can see; the line has to
      // be laid out to know where it ends, so the decision comes after.
      if (!wholeDocument &&
          layout.caretX == null &&
          rowIsVisible(currentY, vIndent + _advance(layout.height, i))) {
        layout = _measuredLine(i, line, wantCaretX: true);
      }

      final finalX = _lineOriginX(
          props.indent, props.align, layout.width, canvasSize == null ? double.infinity : maxW);

      final caretX = layout.caretX;
      // Rounded to a hundredth of a pixel: far below what changes Java's answers, and shorter to send.
      List<double> charX(int from, int to, double base) => [
        for (int k = from; k <= to; k++) base + caretX![k],
      ];

      // The visual line's box: `y`/`h` include the line's vertical indent so bands
      // stay contiguous; `vi` (first visual row only) is the indent within the box,
      // i.e. the glyphs sit at y + vi. The Java side adds `vi` when answering
      // text-position queries and uses the box for line/pixel queries.
      final metrics = layout.metrics;
      final includeCharX = caretX != null;
      if (metrics.length <= 1) {
        maxWidth = math.max(maxWidth, finalX + layout.width);
        visual.add(GeometryRow(
          i,
          docOffset,
          docOffset + line.length,
          _roundPx(finalX),
          _roundPx(currentY),
          _roundPx(layout.width),
          _roundPx(vIndent + _advance(layout.height, i)),
          vIndent != 0 ? vIndent : null,
          includeCharX && rowIsVisible(currentY, _advance(layout.height, i))
              ? charXEntry(charX(0, line.length, finalX))
              : null,
        ));
      } else {
        int local = 0;
        // Rows are stacked as SWT reports them; a fractional glyph top would map a whole-pixel point to
        // the row above.
        var top = currentY;
        final lineBottom = currentY + vIndent + _advance(layout.height, i);
        for (int m = 0; m < metrics.length && local <= line.length; m++) {
          final lm = metrics[m];
          int vEnd = m < layout.rowEnds.length ? layout.rowEnds[m] : line.length;
          final rowX = _rowOriginX(props, finalX, m);
          maxWidth = math.max(maxWidth, rowX + lm.left + lm.width);
          final bottom = m + 1 < metrics.length
              ? top + (m == 0 ? vIndent : 0) + lm.height
              : math.max(lineBottom, top + (m == 0 ? vIndent : 0) + lm.height);
          visual.add(GeometryRow(
            i,
            docOffset + local,
            docOffset + vEnd,
            _roundPx(rowX + lm.left),
            _roundPx(top),
            _roundPx(lm.width),
            _roundPx(bottom - top),
            m == 0 && vIndent != 0 ? vIndent : null,
            includeCharX &&
                    rowIsVisible(m == 0 ? currentY : currentY + vIndent + (lm.baseline - lm.ascent),
                        lm.height)
                ? charXEntry(charX(local, vEnd, rowX))
                : null,
          ));
          local = vEnd;
          top = bottom;
        }
      }

      currentY += vIndent + _advance(layout.height, i);
      docOffset += line.length + 1;
    }

    return GeometryTable(text.length, _roundPx(maxWidth), _roundPx(currentY), visual);
  }

  /// The span for one logical line. Deliberately built per line rather than sliced out
  /// of a document-wide span: building that span is O(lines x styleRanges) and every
  /// caller here needs one line, so the whole-document form was pure waste per paint.
  TextSpan _getTextSpanForLine(String lineText, int lineIndex) {
    if (editingState != null) {
      return _buildLineTextSpanFromState(lineText, lineIndex);
    } else {
      return TextSpan(text: lineText, style: style);
    }
  }

  /// Count of per-line layouts performed, for tests asserting layout cost.
  @visibleForTesting
  static int debugLayoutLineCalls = 0;

  @visibleForTesting
  static int debugLineTopsMicros = 0;
  @visibleForTesting
  static int debugDrawMicros = 0;

  /// What the style ranges on [lineIndex] amount to, as a number: nothing when the line carries
  /// none, which is the ordinary case and costs a list lookup.
  int _rangesTokenFor(int lineIndex, int lineLength) {
    if (editingState == null) return 0;
    final memo = _rangesTokenCache ??= List<int?>.filled(_lineStarts.length + 1, null);
    if (lineIndex < memo.length && memo[lineIndex] != null) return memo[lineIndex]!;
    final ranges = lineIndex < _rangesByLine.length ? _rangesByLine[lineIndex] : const <StyleRange>[];
    int token = 0;
    if (ranges.isNotEmpty) {
      final lineStart = lineIndex < _lineStarts.length ? _lineStarts[lineIndex] : 0;
      for (final range in ranges) {
        // Only the part of the range on this line: a whole-document range's moving end would make
        // every line look new on each keystroke.
        final from = math.max(0, range.start - lineStart);
        final to = math.min(lineLength, range.end - lineStart);
        if (to <= from) continue;
        // The widget's own style is named 0 rather than hashed; other styles by what moves a glyph, so
        // a recoloured range finds the line it already measured.
        final styleToken = identical(range.style, style) ? 0 : _layoutTokenOf(range.style);
        // A widget-style range overriding no metric lays out like no range, so it must not name the line.
        if (styleToken == 0 &&
            range.glyphWidth == null &&
            range.rise == 0 &&
            range.borderStyle == null) {
          continue;
        }
        token = Object.hash(token == 0 ? 17 : token, from, to, styleToken, range.glyphWidth,
            range.rise, range.borderStyle);
      }
    }
    if (lineIndex < memo.length) memo[lineIndex] = token;
    return token;
  }

  List<int?>? _rangesTokenCache;

  /// What the line is drawn in, as a number; unlike [_rangesTokenFor] it covers colours, which a
  /// paragraph bakes in. Palette entries are named by identity.
  int _paintTokenFor(int lineIndex, int lineLength) {
    if (editingState == null) return 0;
    final memo = _paintTokenCache ??= List<int?>.filled(_lineStarts.length + 1, null);
    if (lineIndex < memo.length && memo[lineIndex] != null) return memo[lineIndex]!;
    final ranges =
        lineIndex < _rangesByLine.length ? _rangesByLine[lineIndex] : const <StyleRange>[];
    int token = 0;
    if (ranges.isNotEmpty) {
      final lineStart = lineIndex < _lineStarts.length ? _lineStarts[lineIndex] : 0;
      for (final range in ranges) {
        final from = math.max(0, range.start - lineStart);
        final to = math.min(lineLength, range.end - lineStart);
        if (to <= from) continue;
        token = Object.hash(token == 0 ? 17 : token, from, to, identityHashCode(range.style));
      }
    }
    if (lineIndex < memo.length) memo[lineIndex] = token;
    return token;
  }

  List<int?>? _paintTokenCache;

  /// Count of live TextPainters built — the paint and hit-test path, which keeps none.
  @visibleForTesting
  static int debugPainterLayouts = 0;

  /// Painters the last paint built, and how many paints have run.
  @visibleForTesting
  static int debugPaintersLastDraw = 0;
  @visibleForTesting
  static int debugDrawCalls = 0;

  /// The logical lines the last paint drew, as [first, last]. Empty when it drew none.
  @visibleForTesting
  static List<int> debugPaintedLines = const [];

  /// Where the last paint put each line it drew, as [line, y] in the document's own coordinates.
  @visibleForTesting
  static List<List<double>> debugPaintedRows = const [];

  /// The layout of one logical line, from the shared cache when an identical line has
  /// already been measured. Only the paint loop needs a live TextPainter; everything
  /// that just reads the layout back — the line-tops prefix sum and the geometry table
  /// — goes through here, so an edit re-measures the line it touched and no other.
  ///
  /// [wantCaretX] materializes the per-character x boundaries the geometry table sends;
  /// they are the dominant cost of describing a document, and a cached line keeps them.
  _LineLayout _lineLayout(
    String lineText,
    int lineIndex, {
    required TextAlign align,
    required double maxWidth,
    required bool wantCaretX,
  }) {
    final key = _LineLayoutKey(
      lineText,
      styleToken,
      _rangesTokenFor(lineIndex, lineText.length),
      align,
      maxWidth,
      tabs,
      _tabStopsFor(lineIndex),
    );

    var layout = _lineLayoutCache[key];
    if (layout != null && (!wantCaretX || layout.caretX != null)) {
      debugLineLayoutHits++;
    } else {
      // A key miss means the line changed; a caret miss means it was measured without caret positions.
      if (layout == null) {
        debugLineKeyMisses++;
      } else {
        debugLineCaretMisses++;
      }
      final expanded = _expandTabStops(_getTextSpanForLine(lineText, lineIndex), lineIndex);
      debugLayoutLineCalls++;
      final tp = TextPainter(
        text: expanded.span,
        textAlign: align,
        textDirection: TextDirection.ltr,
      );
      if (expanded.tabStops.isNotEmpty) {
        tp.setPlaceholderDimensions(expanded.tabStops);
      }
      tp.layout(maxWidth: maxWidth);

      final metrics = tp.computeLineMetrics();
      final rowEnds = <int>[];
      if (metrics.length > 1) {
        int local = 0;
        for (int m = 0; m < metrics.length - 1 && local <= lineText.length; m++) {
          // Ask from inside the row, not from the offset it starts: a wrap boundary belongs to
          // both rows, and the row it ends answers first -- which would end this row one
          // character in.
          final inside = local + 1 <= lineText.length ? local + 1 : local;
          final boundary = tp.getLineBoundary(TextPosition(offset: inside));
          int end = boundary.end > local ? boundary.end : local + 1;
          if (end > lineText.length) end = lineText.length;
          rowEnds.add(end);
          local = end;
        }
      }
      layout ??= _LineLayout(
        tp.width,
        tp.height,
        metrics,
        rowEnds,
        lineText.length + 1,
      );
      if (wantCaretX) {
        layout.caretX = [
          for (int k = 0; k <= lineText.length; k++)
            tp.getOffsetForCaret(TextPosition(offset: k), Rect.zero).dx,
        ];
      }
      tp.dispose();
    }
    // Re-inserting on a hit puts the entry back at the young end of the eviction order.
    _cacheLineLayout(key, layout);
    return layout;
  }

  // Every per-line TextPainter goes through here so painting, caret placement and
  // hit-testing measure a line the same way — in particular they all get the same
  // tab stops (see _expandTabStops).
  TextPainter _layoutLine(
    String lineText,
    int lineIndex, {
    required TextAlign align,
    required double maxWidth,
  }) {
    final key = _PaintedLineKey(
      _LineLayoutKey(
        lineText,
        styleToken,
        _rangesTokenFor(lineIndex, lineText.length),
        align,
        maxWidth,
        tabs,
        _tabStopsFor(lineIndex),
      ),
      _paintTokenFor(lineIndex, lineText.length),
    );
    final cached = _paintedLineCache[key];
    if (cached != null) {
      // Back to the young end, so what survives eviction is the band on screen.
      _paintedLineCache.remove(key);
      _paintedLineCache[key] = cached;
      return cached;
    }
    debugPainterLayouts++;
    final line = _expandTabStops(_getTextSpanForLine(lineText, lineIndex), lineIndex);
    final tp = TextPainter(
      text: line.span,
      textAlign: align,
      textDirection: direction,
    );
    if (line.tabStops.isNotEmpty) {
      tp.setPlaceholderDimensions(line.tabStops);
    }
    tp.layout(maxWidth: maxWidth);
    _cachePaintedLine(key, tp);
    return tp;
  }

  // SWT lays a line out with a repeating tab stop every `tabs` spaces wide
  // (SwtStyledTextRenderer: tabWidth = width of `tabs` spaces), so a tab advances to
  // the next multiple of that width from the line start — a tab after "ab" moves 2
  // columns, not 4. Flutter's TextPainter has no tab stops, so each '\t' is replaced
  // by a zero-height placeholder sized to reach the next stop. A placeholder occupies
  // exactly one UTF-16 code unit (U+FFFC) just like the '\t' it stands in for, which
  // keeps getOffsetForCaret/getPositionForOffset offsets identical to document offsets.
  /// A GlyphMetrics range's fixed character width becomes the same kind of placeholder.
  _TabExpandedLine _expandTabStops(TextSpan lineSpan, int lineIndex) {
    final leaves = <_StyledRun>[];
    _flattenSpan(lineSpan, style, leaves);
    final lineStart = lineIndex < _lineStarts.length ? _lineStarts[lineIndex] : 0;
    final hasTab = leaves.any((leaf) => leaf.text.contains('\t'));
    final hasFixedWidth = _runsOnLine(lineIndex).any((r) => r.glyphWidth != null);
    if (!hasTab && !hasFixedWidth) {
      return _TabExpandedLine(lineSpan, const []);
    }

    final stops = _tabStopsFor(lineIndex);
    final tabWidth = _tabStopWidth();
    final children = <InlineSpan>[];
    final placeholders = <PlaceholderDimensions>[];
    final buffer = StringBuffer();
    double x = 0;
    int column = 0;

    void flush(TextStyle runStyle) {
      if (buffer.isEmpty) return;
      final run = buffer.toString();
      children.add(TextSpan(text: run, style: runStyle));
      x += _measureRun(run, runStyle);
      buffer.clear();
    }

    void placeholder(double width) {
      placeholders.add(PlaceholderDimensions(
        size: Size(width, 0),
        alignment: PlaceholderAlignment.baseline,
        baseline: TextBaseline.alphabetic,
        baselineOffset: 0,
      ));
      children.add(_tabPlaceholder);
    }

    for (final leaf in leaves) {
      for (int i = 0; i < leaf.text.length; i++, column++) {
        final char = leaf.text[i];
        final fixedWidth = _glyphWidthAt(lineStart + column);
        if (char == '\t') {
          flush(leaf.style);
          // Epsilon guards against a run measured a hair under an exact stop, which would
          // otherwise make the tab advance ~0 instead of a full column.
          final nextStop = _nextTabStop(x + 0.01, stops, tabWidth);
          placeholder(nextStop - x);
          x = nextStop;
        } else if (fixedWidth != null) {
          flush(leaf.style);
          placeholder(fixedWidth);
          x += fixedWidth;
        } else {
          buffer.write(char);
        }
      }
      flush(leaf.style);
    }

    return _TabExpandedLine(TextSpan(style: style, children: children), placeholders);
  }

  /// The width a GlyphMetrics range fixes for the character at [offset], or null.
  double? _glyphWidthAt(int offset) {
    for (final range in editingState?.characterRanges ?? const <StyleRange>[]) {
      if (range.glyphWidth == null || range.start > offset || offset >= range.end) continue;
      return range.glyphWidth;
    }
    return null;
  }

  /// The stop a tab starting at [x] advances to: the first of [stops] past it, and past the last
  /// one the final interval repeats, as SWT's own tab stops do. Without stops, every [tabWidth].
  static double _nextTabStop(double x, List<int>? stops, double tabWidth) {
    if (stops == null || stops.isEmpty) return ((x / tabWidth).floor() + 1) * tabWidth;
    for (final stop in stops) {
      if (stop > x) return stop.toDouble();
    }
    final last = stops.last.toDouble();
    final interval = stops.length > 1 ? last - stops[stops.length - 2] : tabWidth;
    if (interval <= 0) return last;
    return last + (((x - last) / interval).floor() + 1) * interval;
  }

  void _flattenSpan(
    InlineSpan span,
    TextStyle inherited,
    List<_StyledRun> out,
  ) {
    if (span is! TextSpan) return;
    final effective = span.style == null
        ? inherited
        : inherited.merge(span.style);
    if (span.text != null && span.text!.isNotEmpty) {
      out.add(_StyledRun(span.text!, effective));
    }
    for (final child in span.children ?? const <InlineSpan>[]) {
      _flattenSpan(child, effective, out);
    }
  }

  double _tabStopWidth() {
    final columns = tabs > 0 ? tabs : 4;
    final width = _measureRun(' ' * columns, style);
    return width > 0 ? width : columns * (style.fontSize ?? 16) * 0.5;
  }

  double _measureRun(String run, TextStyle runStyle) {
    final tp = TextPainter(
      text: TextSpan(text: run, style: runStyle),
      textDirection: TextDirection.ltr,
    )..layout();
    final width = tp.width;
    tp.dispose();
    return width;
  }

  /// Document offset each logical line starts at. Held per shape because the callers
  /// below run once per line: recomputing it in each of them splits the whole document
  /// as many times as the document has lines.

  /// Looked up once per shape: on the web, comparing two equal strings for the shared cache reads
  /// both through.
  List<String> get _lines => _linesCache ??= _linesOf(text);
  List<int> get _lineStarts => _lineStartsCache ??= _lineStartsOf(text);
  List<String>? _linesCache;
  List<int>? _lineStartsCache;

  /// Style ranges bucketed by the lines they cover. Both the paint loop and the geometry
  /// table ask line by line, so scanning the whole range list per line costs the document
  /// its line count times its range count on every frame.
  List<List<StyleRange>>? _rangesByLineCache;

  /// Which extra passes over a line's glyphs any run asks for; most documents ask for none.
  ({bool rise, bool border, bool strikeout, bool background}) get _decorations =>
      _decorationsCache ??= _scanDecorations();
  ({bool rise, bool border, bool strikeout, bool background})? _decorationsCache;

  ({bool rise, bool border, bool strikeout, bool background}) _scanDecorations() {
    var rise = false, border = false, strikeout = false, background = false;
    for (final range in editingState?.characterRanges ?? const <StyleRange>[]) {
      if (range.rise != 0) rise = true;
      if (range.borderStyle != null) border = true;
      if (range.paintsStrikeout) strikeout = true;
      if (range.style.backgroundColor != null) background = true;
    }
    return (rise: rise, border: border, strikeout: strikeout, background: background);
  }

  List<StyleRange> _runsOnLine(int lineIndex) =>
      lineIndex >= 0 && lineIndex < _rangesByLine.length ? _rangesByLine[lineIndex] : const [];

  List<List<StyleRange>> get _rangesByLine {
    final cached = _rangesByLineCache;
    if (cached != null) return cached;
    final starts = _lineStarts;
    final buckets = List.generate(starts.length, (_) => <StyleRange>[]);
    // Runs arrive in order, so one sweep finds each run's line.
    int line = 0;
    int? lastStart;
    for (final range in editingState?.characterRanges ?? const <StyleRange>[]) {
      if (range.end <= range.start) continue;
      if (lastStart != null && range.start < lastStart) line = 0;
      lastStart = range.start;
      while (line + 1 < starts.length && starts[line + 1] <= range.start) {
        line++;
      }
      for (int l = line; l < buckets.length && starts[l] <= range.end - 1; l++) {
        buckets[l].add(range);
      }
    }
    return _rangesByLineCache = buckets;
  }

  int _lineOfOffset(int offset) {
    final starts = _lineStarts;
    int lo = 0, hi = starts.length - 1;
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

  TextSpan _buildLineTextSpanFromState(String lineText, int lineIndex) {
    if (editingState == null || editingState!.characterRanges.isEmpty) {
      return TextSpan(text: lineText, style: style);
    }

    final starts = _lineStarts;
    final lineStartOffset = lineIndex < starts.length ? starts[lineIndex] : 0;

    final lineEndOffset = lineStartOffset + lineText.length;

    final lineRanges = lineIndex < _rangesByLine.length
        ? _rangesByLine[lineIndex]
            .where((range) =>
                range.start < lineEndOffset && range.end > lineStartOffset)
            .toList()
        : const <StyleRange>[];

    if (lineRanges.isEmpty) {
      return TextSpan(text: lineText, style: style);
    }

    final relativeRanges = lineRanges
        .map((range) {
          return StyleRange(
            start: math.max(0, range.start - lineStartOffset),
            end: math.min(lineText.length, range.end - lineStartOffset),
            style: range.style,
          );
        })
        .where((range) => range.start < range.end)
        .toList();

    return _buildTextSpanFromRelativeRanges(lineText, relativeRanges);
  }

  TextSpan _buildTextSpanFromRelativeRanges(
    String lineText,
    List<StyleRange> ranges,
  ) {
    if (ranges.isEmpty) {
      return TextSpan(text: lineText, style: style);
    }

    ranges.sort((a, b) => a.start.compareTo(b.start));

    List<TextSpan> children = [];
    int currentPos = 0;

    for (final range in ranges) {
      if (currentPos < range.start) {
        children.add(
          TextSpan(
            text: lineText.substring(currentPos, range.start),
            style: style,
          ),
        );
      }

      children.add(
        TextSpan(
          text: lineText.substring(range.start, range.end),
          style: range.style,
        ),
      );

      currentPos = range.end;
    }

    if (currentPos < lineText.length) {
      children.add(
        TextSpan(text: lineText.substring(currentPos), style: style),
      );
    }

    return TextSpan(children: children);
  }

  TextAlign _mapSwtAlignmentToTextAlign(int swtAlign) {
    switch (swtAlign) {
      case 16384:
        return TextAlign.left;
      case 16777216:
        return TextAlign.center;
      case 131072:
        return TextAlign.right;
      default:
        return TextAlign.left;
    }
  }

  /// Every caret's offset: the widget's own, or the one [caretInfo] names.
  List<int> get allCarets =>
      carets.isNotEmpty ? carets : (caretInfo == null ? const [] : [caretInfo!.offset]);

  /// Every selected range: the widget's own, or the one [selectionInfo] names.
  List<SelectionInfo> get allSelections => selections.isNotEmpty
      ? selections
      : (selectionInfo == null ? const [] : [selectionInfo!]);

  /// Geometry of the painted caret, or null when no caret is visible.
  Rect? caretRect() => caretInfo == null ? null : caretRectAt(caretInfo!.offset);

  /// Geometry of a caret sitting at [offset].
  Rect? caretRectAt(int offset) {
    if (caretInfo == null || !caretInfo!.visible) return null;

    final caretOffset = offset.clamp(0, text.length);
    final lines = _lines;

    int currentLineIndex = 0;
    int currentLineStartOffset = 0;
    int caretPositionInLine = caretOffset;

    for (int i = 0; i < lines.length; i++) {
      final lineLength = lines[i].length;
      final lineEndOffset = currentLineStartOffset + lineLength;

      if (caretOffset <= lineEndOffset) {
        currentLineIndex = i;
        caretPositionInLine = caretOffset - currentLineStartOffset;
        break;
      }

      currentLineStartOffset = lineEndOffset + 1;
    }

    final props = _linePropsFor(currentLineIndex);
    final maxW = _lineMaxWidth(props.layoutIndent);
    final currentY =
        off.dy + _lineTops(lines)[currentLineIndex] + props.vIndent;

    final currentLine = lines[currentLineIndex];
    final tp = _layoutLine(
      currentLine,
      currentLineIndex,
      align: props.align,
      maxWidth: maxW,
    );

    final originX = off.dx +
        _lineOriginX(props.indent, props.align, tp.width, canvasSize == null ? double.infinity : maxW);

    final posInLine = caretPositionInLine.clamp(0, currentLine.length);
    final affinity = posInLine >= currentLine.length && currentLine.isNotEmpty
        ? TextAffinity.upstream
        : TextAffinity.downstream;

    final caretPosition = tp.getOffsetForCaret(
      TextPosition(offset: posInLine, affinity: affinity),
      Rect.fromLTWH(0, 0, tp.width, tp.height),
    );

    // An offset that starts a wrapped row belongs to that row, not to the end of the one above
    // (SWT's own rule -- a wrapped row start counts as a line begin), and that is where the
    // geometry table puts it. Flutter's affinity answers the other way for some breaks.
    final metrics = tp.computeLineMetrics();
    final startedRow = _rowStartingAt(currentLine, currentLineIndex, props, maxW, posInLine);
    final row = startedRow ?? _rowIndexAt(tp, caretPosition.dy);
    final dx = startedRow != null && startedRow < metrics.length
        ? metrics[startedRow].left
        : caretPosition.dx;
    final rowTop = _rowTop(metrics, row);
    return Rect.fromLTWH(
      _rowOriginX(props, originX, row) + dx,
      currentY + rowTop,
      caretInfo!.width,
      caretInfo!.height > 0 ? caretInfo!.height : _caretLineHeight(tp, rowTop),
    );
  }

  /// The visual row [posInLine] starts, when it starts one after the first.
  int? _rowStartingAt(String line, int lineIndex,
      ({int indent, int wrapIndent, int layoutIndent, TextAlign align, int vIndent, String? bulletText}) props,
      double maxW, int posInLine) {
    if (posInLine == 0) return null;
    final rowEnds = _lineLayout(line, lineIndex,
            align: props.align, maxWidth: maxW, wantCaretX: false)
        .rowEnds;
    for (int m = 0; m < rowEnds.length; m++) {
      if (rowEnds[m] == posInLine) return m + 1;
    }
    return null;
  }

  /// Top of the visual line [dy] falls in.
  ///
  /// A tab is painted as a zero-height, baseline-aligned placeholder (see
  /// [_expandTabStops]), whose box therefore starts at the baseline rather than at the
  /// top of the line. A caret resolving to one — any caret sitting right after a tab —
  /// would otherwise be drawn a baseline's worth too low. Line metrics give the line
  /// box itself, which is what the caret spans, and stay correct under word wrap where
  /// [dy] legitimately selects a wrapped sub-line.
  double _caretLineTop(TextPainter tp, double dy) {
    final metrics = tp.computeLineMetrics();
    if (metrics.isEmpty) return dy;
    return _rowTop(metrics, _rowIndexAt(tp, dy));
  }

  /// The height of the visual row starting at [rowTop], which is what the caret spans.
  double _caretLineHeight(TextPainter tp, double rowTop) {
    final metrics = tp.computeLineMetrics();
    for (int m = 0; m < metrics.length; m++) {
      if (_rowTop(metrics, m) >= rowTop - 0.5) return _advance(metrics[m].height);
    }
    return _advance(0);
  }

  /// What SWT was asked for at [offset], for the attributes a painted [TextStyle] cannot report:
  /// the underline style, the border, and the raised baseline.
  Map<String, dynamic> debugSwtAttributesAt(int offset) {
    for (final range in editingState?.characterRanges ?? const <StyleRange>[]) {
      if (range.start > offset || offset >= range.end) continue;
      return {
        if (range.underlineStyle != null) 'underlineStyle': range.underlineStyle,
        if (range.underlineColor != null) 'underlineColor': StyledTextImpl._hex(range.underlineColor),
        if (range.strikeoutColor != null) 'strikeoutColor': StyledTextImpl._hex(range.strikeoutColor),
        if (range.borderStyle != null) 'borderStyle': range.borderStyle,
        if (range.borderColor != null) 'borderColor': StyledTextImpl._hex(range.borderColor),
        if (range.rise != 0) 'rise': range.rise,
      };
    }
    return const {};
  }

  /// The highlight rectangles the last paint drew, for [StyledTextImpl.debugRenderFacts].
  final List<Rect> debugPaintedSelectionRects = [];

  void _drawSelection(Canvas c, SelectionInfo selection) {
    final startOffset = selection.normalizedStart.clamp(0, text.length);
    final endOffset = selection.normalizedEnd.clamp(0, text.length);

    if (startOffset == endOffset) return;

    final lines = _lines;

    int currentLineStartOffset = 0;
    int startLineIndex = 0;
    int endLineIndex = 0;
    int startPositionInLine = 0;
    int endPositionInLine = 0;

    for (int i = 0; i < lines.length; i++) {
      final lineLength = lines[i].length;
      final lineEndOffset = currentLineStartOffset + lineLength;

      if (startOffset >= currentLineStartOffset &&
          startOffset <= lineEndOffset) {
        startLineIndex = i;
        startPositionInLine = startOffset - currentLineStartOffset;
      }

      if (endOffset >= currentLineStartOffset && endOffset <= lineEndOffset) {
        endLineIndex = i;
        endPositionInLine = endOffset - currentLineStartOffset;
        break;
      }

      currentLineStartOffset = lineEndOffset + 1;
    }

    final tops = _lineTops(lines);

    for (
      int lineIndex = startLineIndex;
      lineIndex <= endLineIndex;
      lineIndex++
    ) {
      final line = lines[lineIndex];
      final props = _linePropsFor(lineIndex);

      final tp = _layoutLine(
        line,
        lineIndex,
          align: props.align,
        maxWidth: _lineMaxWidth(props.layoutIndent),
      );

      final currentY = off.dy + tops[lineIndex] + props.vIndent;

      int lineSelectionStart = 0;
      int lineSelectionEnd = line.length;

      if (lineIndex == startLineIndex) {
        lineSelectionStart = startPositionInLine;
      }
      if (lineIndex == endLineIndex) {
        lineSelectionEnd = endPositionInLine;
      }

      // A line whose delimiter is inside the selection is highlighted past its last character:
      // to the right edge with FULL_SELECTION, by the delimiter's own extent otherwise. It is the
      // only highlight an empty line inside a selection gets.
      if (lineIndex < endLineIndex) {
        final tail = tp.width + off.dx + props.indent;
        final right = fullSelection
            ? math.max(tail, canvasSize?.width ?? tail)
            : tail + _measureRun(' ', style);
        final rect = Rect.fromLTRB(tail, currentY, right, currentY + _advance(tp.height));
        debugPaintedSelectionRects.add(rect);
        c.drawRect(rect, Paint()..color = selection.selectionColor);
      }

      if (lineSelectionStart < lineSelectionEnd) {
        final boxes = tp.getBoxesForSelection(
          TextSelection(
            baseOffset: lineSelectionStart.clamp(0, line.length),
            extentOffset: lineSelectionEnd.clamp(0, line.length),
          ),
        );

        for (final box in boxes) {
          final selectionRect = Rect.fromLTWH(
            off.dx + props.indent.toDouble() + box.left,
            currentY + box.top,
            box.right - box.left,
            box.bottom - box.top,
          );
          debugPaintedSelectionRects.add(selectionRect);

          c.drawRect(
            selectionRect,
            Paint()
              ..color = selection.selectionColor
              ..style = PaintingStyle.fill,
          );
        }

        final selectionForeground = selection.selectionForeground;
        if (selectionForeground != null && boxes.isNotEmpty) {
          final recoloured = TextPainter(
            text: _recolour(tp.text!, selectionForeground),
            textAlign: tp.textAlign,
            textDirection: TextDirection.ltr,
          );
          if (tp.inlinePlaceholderBoxes != null) {
            recoloured.setPlaceholderDimensions(_placeholderDimensionsOf(tp));
          }
          recoloured.layout(maxWidth: _lineMaxWidth(props.layoutIndent));
          c.save();
          c.clipRect(boxes
              .map((b) => Rect.fromLTRB(
                  off.dx + props.indent + b.left, currentY + b.top,
                  off.dx + props.indent + b.right, currentY + b.bottom))
              .reduce((a, b) => a.expandToInclude(b)));
          recoloured.paint(c, Offset(off.dx + props.indent.toDouble(), currentY));
          c.restore();
        }
      }
    }
  }

  InlineSpan _recolour(InlineSpan span, Color color) {
    if (span is! TextSpan) return span;
    return TextSpan(
      text: span.text,
      style: (span.style ?? const TextStyle()).copyWith(color: color),
      children: span.children?.map((s) => _recolour(s, color)).toList(),
    );
  }

  List<PlaceholderDimensions> _placeholderDimensionsOf(TextPainter tp) =>
      tp.inlinePlaceholderBoxes!
          .map((b) => PlaceholderDimensions(
              size: Size(b.right - b.left, b.bottom - b.top),
              alignment: PlaceholderAlignment.baseline,
              baseline: TextBaseline.alphabetic))
          .toList();

  TextShape copyWithSelection(SelectionInfo? selection) {
    return TextShape(
      text,
      off,
      style,
      clipRect,
      textSpan,
      caretInfo,
      wordWrap,
      canvasSize,
      editable,
      styledTextId,
      onTextChanged,
      editingState,
      selection,
      lineHeight,
      tabs,
      tabStops,
      widgetLine,
      lineSpacing,
      lineSpacings,
      direction,
      fullSelection,
      carets,
      selections,
      styleToken,
    ).._lineTopsCache = _lineTopsCache;
  }

  TextShape copyWithEditingState(TextEditingState newEditingState) {
    return TextShape(
      text,
      off,
      style,
      clipRect,
      TextRenderer.buildFinalTextSpan(text, newEditingState, style),
      caretInfo,
      wordWrap,
      canvasSize,
      editable,
      styledTextId,
      onTextChanged,
      newEditingState,
      selectionInfo,
      lineHeight,
      tabs,
      tabStops,
      widgetLine,
      lineSpacing,
      lineSpacings,
      direction,
      fullSelection,
      carets,
      selections,
      styleToken,
    );
  }

  TextShape selectAll() {
    return copyWithSelection(SelectionInfo.fromRange(0, text.length));
  }

  TextShape clearSelection() {
    return copyWithSelection(null);
  }

  TextShape extendSelectionTo(int position) {
    final currentCaret = caretInfo?.offset ?? 0;

    if (selectionInfo == null || !selectionInfo!.hasSelection) {
      return copyWithSelection(SelectionInfo.fromRange(currentCaret, position));
    } else {
      return copyWithSelection(selectionInfo!.copyWith(end: position));
    }
  }

  TextShape copyWithText(
    String newText,
    int caretOffset, [
    TextEditingState? newEditingState,
  ]) {
    return TextShape(
      newText,
      off,
      style,
      clipRect,
      newEditingState != null
          ? TextRenderer.buildFinalTextSpan(newText, newEditingState, style)
          : textSpan,
      caretInfo?.copyWith(offset: caretOffset),
      wordWrap,
      canvasSize,
      editable,
      styledTextId,
      onTextChanged,
      newEditingState ?? editingState,
      selectionInfo,
      lineHeight,
      tabs,
      tabStops,
      widgetLine,
      lineSpacing,
      lineSpacings,
      direction,
      fullSelection,
      carets,
      selections,
      styleToken,
    );
  }

  TextShape copyWithCaret(CaretInfo caretInfo) {
    return TextShape(
      text,
      off,
      style,
      clipRect,
      textSpan,
      caretInfo,
      wordWrap,
      canvasSize,
      editable,
      styledTextId,
      onTextChanged,
      editingState,
      selectionInfo,
      lineHeight,
      tabs,
      tabStops,
      widgetLine,
      lineSpacing,
      lineSpacings,
      direction,
      fullSelection,
      carets,
      selections,
      styleToken,
    ).._lineTopsCache = _lineTopsCache;
  }

  TextShape updateCaretOffset(int offset) {
    if (caretInfo == null) return this;
    return copyWithCaret(caretInfo!.copyWith(offset: offset, visible: true));
  }

  TextShape insertText(String insertText, int position) {
    String newText;
    int newCaretPos;
    int insertPosition = position;
    int replacedRangeEnd = position;

    if (selectionInfo != null && selectionInfo!.hasSelection) {
      final start = selectionInfo!.normalizedStart;
      final end = selectionInfo!.normalizedEnd;
      newText = text.substring(0, start) + insertText + text.substring(end);
      newCaretPos = start + insertText.length;
      insertPosition = start;
      replacedRangeEnd = end;
    } else {
      newText =
          text.substring(0, position) + insertText + text.substring(position);
      newCaretPos = position + insertText.length;
    }

    TextEditingState? newEditingState;

    if (editingState != null) {
      if (selectionInfo != null && selectionInfo!.hasSelection) {
        // When replacing selection: first delete the selected range, then insert
        final start = selectionInfo!.normalizedStart;
        final end = selectionInfo!.normalizedEnd;
        final afterDelete = TextEditor.deleteText(
          text,
          start,
          end,
          editingState!,
        );
        newEditingState = TextEditor.insertText(
          text.substring(0, start) + text.substring(end),
          insertText,
          start,
          afterDelete,
        );
      } else {
        newEditingState = TextEditor.insertText(
          text,
          insertText,
          position,
          editingState!,
        );
      }
    } else {
      final currentStyle = _getStyleAtPosition(insertPosition);

      newEditingState = TextEditingState(
        characterRanges: [
          if (insertPosition > 0)
            StyleRange(start: 0, end: insertPosition, style: style),
          StyleRange(
            start: insertPosition,
            end: insertPosition + insertText.length,
            style: currentStyle,
          ),
          if (insertPosition < text.length)
            StyleRange(
              start: insertPosition + insertText.length,
              end: newText.length,
              style: style,
            ),
        ].where((range) => range.start < range.end).toList(),
        lineProperties: editingState?.lineProperties ?? const {},
      );
    }

    onTextChanged?.call(
      newText,
      newCaretPos,
      insertPosition,
      replacedRangeEnd,
      insertText,
    );

    return copyWithText(newText, newCaretPos, newEditingState).clearSelection();
  }

  TextStyle _getStyleAtPosition(int position) {
    if (editingState != null) {
      for (final range in editingState!.characterRanges) {
        if (position >= range.start && position < range.end) {
          return range.style;
        }
      }
    }

    final useDarkTheme = getCurrentTheme();
    return useDarkTheme
        ? style.copyWith(color: const Color(0xFFFFFFFF))
        : style;
  }

  TextShape deleteText(int start, int end) {
    final actualStart = start.clamp(0, text.length);
    final actualEnd = end.clamp(actualStart, text.length);

    if (actualStart == actualEnd) {
      return this;
    }

    final newText = text.substring(0, actualStart) + text.substring(actualEnd);

    TextEditingState? newEditingState;

    if (editingState != null) {
      newEditingState = TextEditor.deleteText(
        text,
        actualStart,
        actualEnd,
        editingState!,
      );
    } else if (newText.isEmpty) {
      newEditingState = TextEditingState(
        characterRanges: [],
        lineProperties: {},
      );
    }

    onTextChanged?.call(newText, actualStart, actualStart, actualEnd, '');

    return copyWithText(newText, actualStart, newEditingState);
  }

  TextShape backspace() {
    if (selectionInfo != null && selectionInfo!.hasSelection) {
      return deleteSelection();
    }

    final caretPos = caretInfo?.offset ?? text.length;
    if (caretPos <= 0) return this;
    return deleteText(caretPos - 1, caretPos);
  }

  TextShape delete() {
    if (selectionInfo != null && selectionInfo!.hasSelection) {
      return deleteSelection();
    }

    final caretPos = caretInfo?.offset ?? 0;
    if (caretPos >= text.length) return this;
    return deleteText(caretPos, caretPos + 1);
  }

  TextShape deleteSelection() {
    if (selectionInfo == null || !selectionInfo!.hasSelection) return this;

    final start = selectionInfo!.normalizedStart;
    final end = selectionInfo!.normalizedEnd;
    return deleteText(start, end).clearSelection();
  }

  String getSelectedText() {
    if (selectionInfo == null || !selectionInfo!.hasSelection) return '';

    final start = selectionInfo!.normalizedStart;
    final end = selectionInfo!.normalizedEnd;
    return text.substring(start, end);
  }

  TextShape moveCaret(int newOffset) {
    final clampedOffset = newOffset.clamp(0, text.length);
    return updateCaretOffset(clampedOffset);
  }

  int getOffsetFromPosition(Offset tapPosition, Size canvasSize) {
    final lines = _lines;
    final relativePosition = tapPosition - off;

    // The cache is keyed to this shape's own canvas width; a caller-supplied
    // size that changes the wrap width gets a fresh, uncached walk instead.
    final cacheValid =
        wordWrap != true || canvasSize.width == this.canvasSize?.width;
    final tops = cacheValid
        ? _lineTops(lines)
        : _lineTopsFor(lines, canvasSize);

    final dy = relativePosition.dy;
    if (dy < 0 || dy >= tops[lines.length]) return text.length;

    int lineIndex = lines.length - 1;
    for (int i = 0; i < lines.length; i++) {
      if (dy < tops[i + 1]) {
        lineIndex = i;
        break;
      }
    }

    int globalOffset = 0;
    for (int i = 0; i < lineIndex; i++) {
      globalOffset += lines[i].length + 1;
    }

    final line = lines[lineIndex];
    final props = _linePropsFor(lineIndex);
    final maxW = _lineMaxWidth(props.layoutIndent, canvasSize);

    final tp = _layoutLine(
      line,
      lineIndex,
      align: props.align,
      maxWidth: maxW,
    );

    final originX = _lineOriginX(props.indent, props.align, tp.width, maxW);
    final dyInLine = dy - tops[lineIndex] - props.vIndent;
    final lineRelativePosition = Offset(
      relativePosition.dx - _rowOriginX(props, originX, _rowIndexAt(tp, dyInLine)),
      dyInLine,
    );

    final textPosition = tp.getPositionForOffset(lineRelativePosition);
    final offsetInLine = textPosition.offset.clamp(0, line.length);

    return (globalOffset + offsetInLine).clamp(0, text.length);
  }

  bool containsPoint(Offset point, Size canvasSize) {
    // When text is empty, the entire canvas should be clickable to allow editing
    if (text.isEmpty && editable) {
      final fullRect = Rect.fromLTWH(0, 0, canvasSize.width, canvasSize.height);
      return fullRect.contains(point);
    }

    final tp = TextPainter(
      text: textSpan ?? TextSpan(text: text.isEmpty ? " " : text, style: style),
      textDirection: TextDirection.ltr,
    );

    if (wordWrap == true) {
      final maxWidth = canvasSize.width - off.dx;
      tp.layout(maxWidth: maxWidth > 0 ? maxWidth : double.infinity);
    } else {
      tp.layout(maxWidth: double.infinity);
    }

    final textWidth = text.isEmpty ? 20.0 : tp.width;
    final textHeight = text.isEmpty ? (style.fontSize ?? 16) * 1.2 : tp.height;

    final textRect = Rect.fromLTWH(off.dx, off.dy, textWidth, textHeight);

    return textRect.contains(point);
  }

  int calculateVerticalNavigation(int direction, Size canvasSize) {
    final tp = TextPainter(
      text: textSpan ?? TextSpan(text: text, style: style),
      textDirection: TextDirection.ltr,
      maxLines: wordWrap == true ? null : 1,
    );

    if (wordWrap == true) {
      final maxWidth = canvasSize.width - off.dx;
      tp.layout(maxWidth: maxWidth > 0 ? maxWidth : double.infinity);
    } else {
      tp.layout(maxWidth: double.infinity);
    }

    final currentOffset = caretInfo?.offset ?? 0;
    final currentPosition = tp.getOffsetForCaret(
      TextPosition(offset: currentOffset),
      Rect.fromLTWH(0, 0, tp.width, tp.height),
    );

    final lineHeight = style.fontSize ?? 16;
    final newY = currentPosition.dy + (direction * lineHeight);
    final clampedY = newY.clamp(0.0, tp.height);

    final newPosition = tp.getPositionForOffset(
      Offset(currentPosition.dx, clampedY),
    );
    return newPosition.offset.clamp(0, text.length);
  }

  int calculateLineNavigation({
    required bool isHome,
    required Size canvasSize,
  }) {
    final tp = TextPainter(
      text: textSpan ?? TextSpan(text: text, style: style),
      textDirection: TextDirection.ltr,
      maxLines: wordWrap == true ? null : 1,
    );

    if (wordWrap == true) {
      final maxWidth = canvasSize.width - off.dx;
      tp.layout(maxWidth: maxWidth > 0 ? maxWidth : double.infinity);
    } else {
      tp.layout(maxWidth: double.infinity);
    }

    final currentOffset = caretInfo?.offset ?? 0;
    final currentPosition = tp.getOffsetForCaret(
      TextPosition(offset: currentOffset),
      Rect.fromLTWH(0, 0, tp.width, tp.height),
    );

    final targetX = isHome ? 0.0 : tp.width;
    final targetPosition = tp.getPositionForOffset(
      Offset(targetX, currentPosition.dy),
    );

    return targetPosition.offset.clamp(0, text.length);
  }

  @override
  String toString() =>
      'EditableText "${text.length > 20 ? "${text.substring(0, 20)}..." : text}" @ $off${caretInfo != null ? " [caret at ${caretInfo!.offset}]" : ""}${editable ? " [editable]" : ""}${clipRect != null ? " [clipped]" : ""}';
}

/// The rectangle a block selection covers, painted over the text it selects.
class _BlockSelectionShape extends Shape {
  @override
  String describe() => 'BlockSelection ${bounds.left.round()},${bounds.top.round()} '
      '${bounds.width.round()}x${bounds.height.round()} $color';

  _BlockSelectionShape(this.bounds, this.color);

  final Rect bounds;
  final Color color;

  @override
  void draw(Canvas c) {
    c.drawRect(bounds, Paint()..color = color.withOpacity(0.6));
  }
}

class CaretInfo {
  final int offset;
  final double width;
  final double height;
  final Color color;
  final bool visible;
  final bool blinking;
  final int styledTextId;
  final int blinkRate;

  CaretInfo({
    required this.offset,
    this.width = 1.0,
    this.height = 0.0,
    required this.color,
    this.visible = true,
    this.blinking = true,
    required this.styledTextId,
    this.blinkRate = 560,
  });

  CaretInfo copyWith({
    int? offset,
    double? width,
    double? height,
    Color? color,
    bool? visible,
    bool? blinking,
    int? styledTextId,
    int? blinkRate,
  }) {
    return CaretInfo(
      offset: offset ?? this.offset,
      width: width ?? this.width,
      height: height ?? this.height,
      color: color ?? this.color,
      visible: visible ?? this.visible,
      blinking: blinking ?? this.blinking,
      styledTextId: styledTextId ?? this.styledTextId,
      blinkRate: blinkRate ?? this.blinkRate,
    );
  }
}

class StyleRange {
  final int start;
  final int end;
  final TextStyle style;

  /// SWT's own values for the attributes a [TextStyle] cannot carry, so what is painted can be
  /// described in the terms it was asked for (`SWT.UNDERLINE_*`, `SWT.BORDER_*`).
  final int? underlineStyle;
  final int? borderStyle;
  final Color? borderColor;
  final int rise;
  /// StyleRange#metrics: the width each character of the range occupies, whatever it draws.
  final double? glyphWidth;
  /// StyleRange#underlineColor / #strikeoutColor, which SWT lets differ from each other.
  final Color? underlineColor;
  final Color? strikeoutColor;
  /// Whether the strikeout is painted here rather than by the text decoration, because its colour
  /// differs from the underline's and a TextStyle carries only one.
  final bool paintsStrikeout;

  /// Whether [other] is the same run, drawn the same way.
  bool paintsSameAs(StyleRange other) =>
      identical(this, other) ||
      (other.start == start &&
          other.end == end &&
          other.rise == rise &&
          other.underlineStyle == underlineStyle &&
          other.borderStyle == borderStyle &&
          other.borderColor == borderColor &&
          other.glyphWidth == glyphWidth &&
          other.underlineColor == underlineColor &&
          other.strikeoutColor == strikeoutColor &&
          other.paintsStrikeout == paintsStrikeout &&
          // Shared across every run drawn in one palette entry, so this is usually one compare.
          (identical(other.style, style) || other.style == style));

  const StyleRange({
    required this.start,
    required this.end,
    required this.style,
    this.underlineStyle,
    this.borderStyle,
    this.borderColor,
    this.rise = 0,
    this.glyphWidth,
    this.underlineColor,
    this.strikeoutColor,
    this.paintsStrikeout = false,
  });
}

class SelectionInfo {
  final int start;
  final int end;
  final Color selectionColor;
  /// Colour the selected glyphs are repainted in; null leaves them in their own colour.
  final Color? selectionForeground;
  final bool isActive;

  SelectionInfo({
    required this.start,
    required this.end,
    this.selectionColor = const Color(0xFF3399FF),
    this.selectionForeground,
    this.isActive = false,
  });

  bool get hasSelection => start != end;
  int get length => (end - start).abs();
  int get normalizedStart => math.min(start, end);
  int get normalizedEnd => math.max(start, end);

  SelectionInfo copyWith({
    int? start,
    int? end,
    Color? selectionColor,
    Color? selectionForeground,
    bool? isActive,
  }) {
    return SelectionInfo(
      start: start ?? this.start,
      end: end ?? this.end,
      selectionColor: selectionColor ?? this.selectionColor,
      selectionForeground: selectionForeground ?? this.selectionForeground,
      isActive: isActive ?? this.isActive,
    );
  }

  static SelectionInfo collapsed(int position) {
    return SelectionInfo(start: position, end: position);
  }

  static SelectionInfo fromRange(int start, int end) {
    return SelectionInfo(start: start, end: end, isActive: true);
  }
}

class TextEditingState {
  final List<StyleRange> characterRanges;
  final Map<int, LineProperties> lineProperties;

  TextStyle? _signatureBaseline;
  int _signatureValue = 0;

  /// What of this state a line's layout depends on, as a number. Ranges that move no glyph are left
  /// out, as in the per-line token, so recolouring keeps the signature.
  int layoutSignatureFor(TextStyle defaultStyle) {
    if (identical(_signatureBaseline, defaultStyle)) return _signatureValue;
    final baseline = _layoutTokenOf(defaultStyle);
    int token = lineProperties.length;
    for (final range in characterRanges) {
      final styleToken = _layoutTokenOf(range.style);
      if (styleToken == baseline &&
          range.glyphWidth == null &&
          range.rise == 0 &&
          range.borderStyle == null) {
        continue;
      }
      token = Object.hash(token, range.start, range.end, styleToken, range.glyphWidth,
          range.rise, range.borderStyle);
    }
    for (final entry in lineProperties.entries) {
      final line = entry.value;
      token = Object.hash(token, entry.key, line.indent, line.wrapIndent, line.alignment,
          line.justify, line.verticalIndent, line.bulletText,
          const ListEquality<int>().hash(line.tabStops));
    }
    _signatureBaseline = defaultStyle;
    return _signatureValue = token;
  }

  TextEditingState({
    this.characterRanges = const [],
    this.lineProperties = const {},
  });

  /// Whether [other] would paint the same runs, in the same styles, on the same lines.
  bool paintsSameAs(TextEditingState other) {
    if (identical(this, other)) return true;
    if (other.characterRanges.length != characterRanges.length) return false;
    for (int i = 0; i < characterRanges.length; i++) {
      if (!characterRanges[i].paintsSameAs(other.characterRanges[i])) return false;
    }
    if (other.lineProperties.length != lineProperties.length) return false;
    for (final entry in lineProperties.entries) {
      final mine = entry.value;
      final theirs = other.lineProperties[entry.key];
      if (theirs == null || !mine.paintsSameAs(theirs)) return false;
    }
    return true;
  }

  TextEditingState updateCharacterRanges(List<StyleRange> newRanges) {
    return TextEditingState(
      characterRanges: newRanges,
      lineProperties: lineProperties,
    );
  }

  TextEditingState updateLineProperties(Map<int, LineProperties> newProps) {
    return TextEditingState(
      characterRanges: characterRanges,
      lineProperties: newProps,
    );
  }

  TextEditingState copyWith({
    List<StyleRange>? characterRanges,
    Map<int, LineProperties>? lineProperties,
  }) {
    return TextEditingState(
      characterRanges: characterRanges ?? this.characterRanges,
      lineProperties: lineProperties ?? this.lineProperties,
    );
  }
}

class LineProperties {
  final int? alignment;
  final int? indent;
  final bool? justify;
  final int? verticalIndent;
  /// StyledText#setLineWrapIndent: the indent of the rows a wrapped line continues on.
  final int? wrapIndent;
  /// StyledText#setLineBullet: what to paint before the line, inside the width its style's glyph
  /// metrics reserved (which is already part of [indent]).
  final String? bulletText;
  /// StyledText#setLineBackground: painted behind the whole line.
  final Color? background;
  /// StyledText#setLineTabStops: absolute stops, the last interval repeating past the end.
  final List<int>? tabStops;

  const LineProperties({
    this.alignment,
    this.indent,
    this.justify,
    this.verticalIndent,
    this.wrapIndent,
    this.bulletText,
    this.background,
    this.tabStops,
  });

  /// Whether [other] would lay this line out and paint it the same way.
  bool paintsSameAs(LineProperties other) =>
      identical(this, other) ||
      (other.alignment == alignment &&
          other.indent == indent &&
          other.justify == justify &&
          other.verticalIndent == verticalIndent &&
          other.wrapIndent == wrapIndent &&
          other.bulletText == bulletText &&
          other.background == background &&
          const ListEquality<int>().equals(other.tabStops, tabStops));
}

class TextRenderer {
  static TextSpan buildFinalTextSpan(
    String text,
    TextEditingState state,
    TextStyle defaultStyle,
  ) {
    if (text.isEmpty) {
      return TextSpan(text: '', style: defaultStyle);
    }

    final lines = _linesOf(text);
    List<TextSpan> lineSpans = [];

    int currentOffset = 0;
    for (int lineIndex = 0; lineIndex < lines.length; lineIndex++) {
      final line = lines[lineIndex];
      final lineLength = line.length;

      final lineCharRanges = state.characterRanges
          .where(
            (range) => _rangeOverlapsLine(range, currentOffset, lineLength),
          )
          .toList();

      final lineSpan = _buildLineTextSpan(
        line,
        lineCharRanges,
        currentOffset,
        defaultStyle,
      );

      lineSpans.add(lineSpan);
      currentOffset += lineLength + 1;
    }

    return TextSpan(children: lineSpans);
  }

  static bool _rangeOverlapsLine(
    StyleRange range,
    int lineStart,
    int lineLength,
  ) {
    final lineEnd = lineStart + lineLength;
    return range.start < lineEnd && range.end > lineStart;
  }

  static TextSpan _buildLineTextSpan(
    String lineText,
    List<StyleRange> lineRanges,
    int lineStartOffset,
    TextStyle defaultStyle,
  ) {
    if (lineRanges.isEmpty) {
      return TextSpan(text: lineText, style: defaultStyle);
    }

    final relativeRanges = lineRanges
        .map(
          (range) => StyleRange(
            start: math.max(0, range.start - lineStartOffset),
            end: math.min(lineText.length, range.end - lineStartOffset),
            style: range.style,
          ),
        )
        .where((range) => range.start < range.end)
        .toList();

    return _buildTextSpanFromRanges(lineText, relativeRanges, defaultStyle);
  }

  static TextSpan _buildTextSpanFromRanges(
    String text,
    List<StyleRange> ranges,
    TextStyle defaultStyle,
  ) {
    if (ranges.isEmpty) {
      return TextSpan(text: text, style: defaultStyle);
    }

    ranges.sort((a, b) => a.start.compareTo(b.start));

    List<TextSpan> children = [];
    int currentPos = 0;

    for (final range in ranges) {
      if (currentPos < range.start) {
        children.add(
          TextSpan(
            text: text.substring(currentPos, range.start),
            style: defaultStyle,
          ),
        );
      }

      children.add(
        TextSpan(
          text: text.substring(range.start, range.end),
          style: range.style,
        ),
      );

      currentPos = range.end;
    }

    if (currentPos < text.length) {
      children.add(
        TextSpan(text: text.substring(currentPos), style: defaultStyle),
      );
    }

    return TextSpan(children: children);
  }
}


/// The layout table Java answers StyledText's position queries from, one [GeometryRow] per visual
/// row; kept as objects so only the rows that changed are sent.
@visibleForTesting
class GeometryTable {
  GeometryTable(this.charCount, this.contentWidth, this.contentHeight, this.rows);

  int charCount;
  final double contentWidth;
  final double contentHeight;
  final List<GeometryRow> rows;

  bool sameExtentsAs(GeometryTable other) =>
      charCount == other.charCount &&
      contentWidth == other.contentWidth &&
      contentHeight == other.contentHeight;

  Map<String, dynamic> toJson() => {
        'charCount': charCount,
        'contentWidth': contentWidth,
        'contentHeight': contentHeight,
        'lines': [for (final row in rows) row.toJson()],
      };
}

/// One visual row: logical line [l], offsets [s]..[e], its box, vertical indent [vi], and
/// per-character x ([TextShape.charXEntry]) when it is in the band that carries them.
@visibleForTesting
class GeometryRow {
  GeometryRow(this.l, this.s, this.e, this.x, this.y, this.w, this.h, this.vi, this.charX);

  final int l;
  int s;
  int e;
  final double x;
  final double y;
  final double w;
  final double h;
  final int? vi;
  final Map<String, dynamic>? charX;

  bool sameAs(GeometryRow other) =>
      l == other.l && s == other.s && e == other.e && y == other.y && _sameBoxAs(other);

  /// Whether this is [before] moved by the shift an edit gave every row after it.
  bool isShiftOf(GeometryRow before, int lineDelta, int offsetDelta, double yDelta) =>
      l - before.l == lineDelta &&
      s - before.s == offsetDelta &&
      e - before.e == offsetDelta &&
      y - before.y == yDelta &&
      _sameBoxAs(before);

  bool _sameBoxAs(GeometryRow other) =>
      x == other.x && w == other.w && h == other.h && vi == other.vi && _sameCharX(other.charX);

  bool _sameCharX(Map<String, dynamic>? other) {
    final mine = charX;
    if (identical(mine, other)) return true;
    if (mine == null || other == null || mine.length != other.length) return false;
    for (final entry in mine.entries) {
      final a = entry.value as List;
      final b = other[entry.key];
      if (b is! List || a.length != b.length) return false;
      for (int i = 0; i < a.length; i++) {
        if (a[i] != b[i]) return false;
      }
    }
    return true;
  }

  Map<String, dynamic> toJson() => {
        'l': l,
        's': s,
        'e': e,
        'x': x,
        'y': y,
        'w': w,
        'h': h,
        if (vi != null) 'vi': vi,
        ...?charX,
      };
}

/// The change from one table to the next: [rowsReplaced] rows from [rowStart] became [rows], and
/// every row after them moved by the deltas.
class _GeometrySplice {
  _GeometrySplice(
      this.rowStart, this.rowsReplaced, this.lineDelta, this.offsetDelta, this.yDelta, this.rows);

  final int rowStart;
  final int rowsReplaced;
  final int lineDelta;
  final int offsetDelta;
  final double yDelta;
  final List<GeometryRow> rows;
}

/// Where the horizontal bar is being asked to go across one gesture: Java's answer is a round
/// trip away, so each event builds on the last request rather than on the stale position.
@visibleForTesting
class HorizontalWheelScroll {
  int? _target;
  int? _from;

  /// Where to ask the bar to go after a wheel event worth [lines], or null when it would not move.
  int? next({
    required int reported,
    required int lines,
    required int increment,
    required int minimum,
    required int maximum,
    required int thumb,
  }) {
    // Java reporting anything other than what it reported when we started asking means it has
    // caught up, or moved for its own reasons. Either way its position is now the truth.
    if (_from != reported) _target = null;
    final base = _target ?? reported;
    final next = (base + lines * increment)
        .clamp(minimum, math.max(minimum, maximum - thumb))
        .toInt();
    if (next == base) return null;
    _from = reported;
    _target = next;
    return next;
  }
}

/// Drops the ranges another range already covers, from a list sorted by start; the sort makes
/// the furthest end so far enough to decide.
@visibleForTesting
List<StyleRange> dedupeSortedRanges(List<StyleRange> sorted) {
  final kept = <StyleRange>[];
  int reach = -1;
  for (final range in sorted) {
    if (reach >= range.end) continue;
    kept.add(range);
    if (range.end > reach) reach = range.end;
  }
  return kept;
}

/// Each style's token, cached: a TextStyle is immutable and runs share a few dozen of them.
final Expando<int> _layoutTokens = Expando<int>('layoutToken');

int _layoutTokenOf(TextStyle? style) {
  if (style == null) return 0;
  return _layoutTokens[style] ??= _computeLayoutToken(style);
}

/// Hashes only what moves a glyph; a field left out keeps a stale layout, so err towards including.
int _computeLayoutToken(TextStyle style) {
  return Object.hash(
    style.fontSize,
    // "Normal" and unset lay out the same; the widget style leaves these null while a decoded range
    // names them.
    style.fontWeight == FontWeight.normal ? null : style.fontWeight,
    style.fontStyle == FontStyle.normal ? null : style.fontStyle,
    style.letterSpacing,
    style.wordSpacing,
    style.height,
    style.textBaseline == TextBaseline.alphabetic ? null : style.textBaseline,
    style.leadingDistribution,
    style.locale,
    style.fontFamily,
    style.fontFamilyFallback == null
        ? 0
        : Object.hashAll(style.fontFamilyFallback!),
    style.fontFeatures == null ? 0 : Object.hashAll(style.fontFeatures!),
    style.fontVariations == null ? 0 : Object.hashAll(style.fontVariations!),
  );
}

/// Where [offset] is after [replaced] characters at [start] became [inserted] of them, as
/// StyledText's renderer moves its ranges: text typed at a point lands after it.
@visibleForTesting
int spliceOffset(int offset, int start, int replaced, int inserted) {
  if (offset >= start + replaced) return offset + inserted - replaced;
  if (offset > start) return start + inserted;
  return offset;
}

/// Where a run's end is after the same edit. An end at the edit stays put, so a character typed
/// just past a run is not drawn in its style; one inside the replaced text is cut back to it.
int _spliceEnd(int end, int start, int replaced, int inserted) {
  if (end > start + replaced) return end + inserted - replaced;
  if (end > start) return start;
  return end;
}

/// First element of a style index that is a change from the one held rather than a whole index.
const int styleIndexChangeMark = -2147483648;

/// [base] with a change `[mark, checksum, serial, paletteName, from, removed, inserted...]`
/// applied, or null when it does not fit or fails its checksum.
@visibleForTesting
List<int>? applyStyleIndexChange(List<int> base, List<int> change) {
  if (change.length < 6 || base.isEmpty) return null;
  final from = change[4];
  final removed = change[5];
  if (from < 1 || removed < 0 || from + removed > base.length) return null;
  // Nothing changed: the same list, so nothing that compares by identity sees a new index.
  if (removed == 0 && change.length == 6 && change[3] == base[0]) {
    return styleIndexChecksum(base) == change[1] ? base : null;
  }
  final inserted = change.length - 6;
  final tail = base.length - from - removed;
  final out = List<int>.filled(from + inserted + tail, 0);
  out[0] = change[3];
  out.setRange(1, from, base, 1);
  out.setRange(from, from + inserted, change, 6);
  out.setRange(from + inserted, out.length, base, from + removed);
  return styleIndexChecksum(out) == change[1] ? out : null;
}

/// The checksum Java sends with a change. Kept below 2^30 at every step, so the arithmetic is
/// exact in a JavaScript double as well.
@visibleForTesting
int styleIndexChecksum(List<int> index) {
  var hash = index.length;
  for (final value in index) {
    hash = (hash * 31 + value) & 0x3FFFFFFF;
  }
  return hash;
}

/// [spliceOffset] for a palette index — its name, then name, start and length per run. A run the
/// edit removed entirely is dropped.
@visibleForTesting
List<int> spliceStyleIndex(List<int> index, int start, int replaced, int inserted) {
  if (index.length < 4) return index;
  final out = <int>[index[0]];
  for (int i = 1; i + 2 < index.length; i += 3) {
    final from = spliceOffset(index[i + 1], start, replaced, inserted);
    final to = _spliceEnd(index[i + 1] + index[i + 2], start, replaced, inserted);
    if (to > from) out..add(index[i])..add(from)..add(to - from);
  }
  return out;
}

/// [spliceOffset] for start and length pairs. An empty range is a caret and moves as one.
@visibleForTesting
List<int> spliceRangePairs(List<int> pairs, int start, int replaced, int inserted) {
  final out = <int>[];
  for (int i = 0; i + 1 < pairs.length; i += 2) {
    final from = spliceOffset(pairs[i], start, replaced, inserted);
    final to = pairs[i + 1] == 0
        ? from
        : _spliceEnd(pairs[i] + pairs[i + 1], start, replaced, inserted);
    out..add(from)..add(to > from ? to - from : 0);
  }
  return out;
}

class TextEditor {
  static TextEditingState insertText(
    String originalText,
    String insertText,
    int position,
    TextEditingState currentState,
  ) {
    final newCharRanges = _updateCharacterRanges(
      currentState.characterRanges,
      position,
      insertText.length,
    );

    Map<int, LineProperties> newLineProps = currentState.lineProperties;
    if (insertText.contains('\n')) {
      newLineProps = _updateLinePropertiesForInsertion(
        currentState.lineProperties,
        originalText,
        insertText,
        position,
      );
    }

    return TextEditingState(
      characterRanges: newCharRanges,
      lineProperties: newLineProps,
    );
  }

  static TextEditingState deleteText(
    String originalText,
    int start,
    int end,
    TextEditingState currentState,
  ) {
    final deleteLength = end - start;

    final newCharRanges = _updateCharacterRanges(
      currentState.characterRanges,
      start,
      -deleteLength,
    );

    final newText =
        originalText.substring(0, start) + originalText.substring(end);
    final newLineProps = _updateLinePropertiesForDeletion(
      currentState.lineProperties,
      originalText,
      newText,
      start,
      end,
    );

    return TextEditingState(
      characterRanges: newCharRanges,
      lineProperties: newLineProps,
    );
  }

  static List<StyleRange> _updateCharacterRanges(
    List<StyleRange> currentRanges,
    int position,
    int textDiff,
  ) {
    List<StyleRange> updatedRanges = [];

    for (final range in currentRanges) {
      StyleRange? newRange;

      if (textDiff > 0) {
        if (position <= range.start) {
          newRange = StyleRange(
            start: range.start + textDiff,
            end: range.end + textDiff,
            style: range.style,
          );
        } else if (position >= range.end) {
          newRange = range;
        } else {
          newRange = StyleRange(
            start: range.start,
            end: range.end + textDiff,
            style: range.style,
          );
        }
      } else {
        final deleteLength = -textDiff;
        final deleteEnd = position + deleteLength;

        if (deleteEnd <= range.start) {
          newRange = StyleRange(
            start: range.start + textDiff,
            end: range.end + textDiff,
            style: range.style,
          );
        } else if (position >= range.end) {
          newRange = range;
        } else if (position <= range.start && deleteEnd >= range.end) {
          newRange = null;
        } else if (position <= range.start && deleteEnd < range.end) {
          newRange = StyleRange(
            start: position,
            end: range.end + textDiff,
            style: range.style,
          );
        } else if (position > range.start && deleteEnd >= range.end) {
          newRange = StyleRange(
            start: range.start,
            end: position,
            style: range.style,
          );
        } else {
          newRange = StyleRange(
            start: range.start,
            end: range.end + textDiff,
            style: range.style,
          );
        }
      }

      if (newRange != null &&
          newRange.start < newRange.end &&
          newRange.start >= 0) {
        updatedRanges.add(newRange);
      }
    }

    return updatedRanges;
  }

  static Map<int, LineProperties> _updateLinePropertiesForInsertion(
    Map<int, LineProperties> currentProps,
    String originalText,
    String insertText,
    int position,
  ) {
    final newLineCount = '\n'.allMatches(insertText).length;
    if (newLineCount == 0) return currentProps;

    final insertLineIndex = _getLineFromOffset(position, originalText);
    Map<int, LineProperties> newProps = {};

    currentProps.forEach((lineIndex, props) {
      if (lineIndex < insertLineIndex) {
        newProps[lineIndex] = props;
      } else if (lineIndex == insertLineIndex) {
        newProps[lineIndex] = props;
        for (int i = 1; i <= newLineCount; i++) {
          newProps[lineIndex + i] = LineProperties(
            alignment: props.alignment,
            indent: props.indent,
            justify: props.justify,
          );
        }
      } else {
        newProps[lineIndex + newLineCount] = props;
      }
    });

    return newProps;
  }

  static Map<int, LineProperties> _updateLinePropertiesForDeletion(
    Map<int, LineProperties> currentProps,
    String originalText,
    String newText,
    int deleteStart,
    int deleteEnd,
  ) {
    final deletedLineCount = '\n'
        .allMatches(originalText.substring(deleteStart, deleteEnd))
        .length;
    if (deletedLineCount == 0) return currentProps;

    final deleteStartLine = _getLineFromOffset(deleteStart, originalText);
    Map<int, LineProperties> newProps = {};

    currentProps.forEach((lineIndex, props) {
      if (lineIndex < deleteStartLine) {
        newProps[lineIndex] = props;
      } else if (lineIndex >= deleteStartLine + deletedLineCount) {
        newProps[lineIndex - deletedLineCount] = props;
      }
    });

    return newProps;
  }

  static int _getLineFromOffset(int offset, String text) {
    return '\n'.allMatches(text.substring(0, offset)).length;
  }
}
