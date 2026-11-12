import 'dart:async';
import 'dart:convert';
import 'dart:math' as math;
import 'dart:typed_data';
import 'dart:ui' show LineMetrics;
import 'package:collection/collection.dart';
import 'package:flutter/foundation.dart';
import 'package:flutter/gestures.dart';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:flutter/widgets.dart';
import '../comm/comm.dart';
import '../comm/channel_header.dart';
import '../comm/comm_frame.dart' show EquoCommBase;
import '../comm/delivery_gate.dart' show deliveryGate;
import '../comm/v_registry.dart' show VRegistry;
import 'utils/style_index_change.dart';
import 'utils/perf_marks_stub.dart'
    if (dart.library.js_interop) 'utils/perf_marks_web.dart';
import '../gen/event.dart';
import 'control_evolve.dart' show ControlImpl;
import '../gen/color.dart';
import '../gen/gc.dart';
import '../gen/stylerange.dart';
import '../gen/styledtext.dart';
import '../gen/styledtextrenderer.dart';
import '../gen/swt.dart';
import '../gen/widget.dart';
import '../gen/widgets.dart' show WireKey, WireChannel, WireType;
import '../impl/canvas_evolve.dart';
import '../impl/gcdrawer_evolve.dart';
import 'widget_config.dart';
import 'key_forwarding.dart';
import 'key_mapping.dart';
import 'utils/composed_text_input.dart';
import 'utils/resize_reports.dart';
import 'utils/font_utils.dart';
import 'utils/widget_utils.dart';
import 'color_utils.dart';
import '../theme/theme_extensions/scrolledcomposite_theme_extension.dart';
import '../theme/theme_extensions/styledtext_theme_extension.dart';
import '../testing/render_facts.dart';
import 'utils/wheel_input.dart';
import '../comm/widget_wire.dart' show WidgetWireReader, WidgetWireWriter, WireVisitable;

/// Paints a StyledText as Java describes it and reports raw input back; the document, carets,
/// selection and scroll offset are Java's and nothing here edits them.
class StyledTextImpl<T extends StyledTextSwt, V extends VStyledText>
    extends CanvasImpl<T, V> implements RenderFactsSource {
  /// With scroll bars the wheel scrolls the text, through [_onWheel]; without them a StyledText
  /// reports it like any other control and passes it on.
  @override
  bool get forwardsWheelFromWrap => !hasStyle(state.style, SWT.H_SCROLL | SWT.V_SCROLL);

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

  /// The text [_delimiters] and [_shownText] were worked out from. Both scan the whole document,
  /// so a rebuild that keeps the text (a tab or font change) keeps them.
  String? _delimitersSource;
  String _shownText = '';

  /// The mounted StyledTexts by id, where the handlers below find the editor a frame addresses.
  static final Map<int, StyledTextImpl> _live = {};

  @override
  Map<int, WidgetSwtState> get liveOfKind => _live;

  /// Registers what every StyledText answers (see `registerWidgetOps`).
  static void registerOps() {
    final type = WireType.styledText;
    EquoCommService.onWidgetOp(type, WireChannel.textChange, _textChangeOp);
    EquoCommService.onWidgetOp(type, WireChannel.resendGeometry, _resendGeometryOp);
  }

  /// What Java sends a StyledText: one handler per channel for every StyledText.
  static FutureOr<bool> _textChangeOp(int id, Uint8List frame, int start, int end) {
    final editor = _live[id];
    if (editor == null) return false;
    editor._onTextChange((WidgetWireReader(frame)..pos = start).fields(_edit));
    return true;
  }

  /// Reused for every edit: each one writes all the fields read.
  static final VEvent _edit = VEvent();

  static FutureOr<bool> _resendGeometryOp(int id, Uint8List frame, int start, int end) {
    final editor = _live[id];
    if (editor == null) return false;
    editor._sendWholeGeometry();
    return true;
  }

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
    EquoCommService.sendWritten(WidgetWireWriter.startTo(state.wireType, state.id, WireChannel.imeComposition)
          ..string(text)
          ..int32(caret)
          ..boolean(commit));
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
        minContrast: _styledTextTheme.minTextContrast,
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

  /// The width a word-wrapped editor keeps while its window is dragged, or null. Rewrapping every line
  /// at each width of a drag costs more than a frame; the editor rewraps once the drag ends, at the
  /// size it ends at, as it does when nothing is laid out until then.
  double? _heldWrapWidth;

  Size _canvasSize(TextShape? previous) {
    final bounds = getBounds();
    if (state.wordWrap != true || !ResizeReports.windowDragged.value) {
      _heldWrapWidth = null;
      return bounds;
    }
    final held = _heldWrapWidth ??= previous?.canvasSize?.width ?? bounds.width;
    return Size(held, bounds.height);
  }

  void _onWindowDragged() {
    if (!ResizeReports.windowDragged.value && _heldWrapWidth != null && mounted) setState(() => _shapeStale = true);
  }

  @override
  void initState() {
    super.initState();
    _focusNode.addListener(_onFocusChange);
    ResizeReports.windowDragged.addListener(_onWindowDragged);
    // Updates that arrived while nothing showed this editor were merged with no one to resolve
    // them; a change among them is relative to an index this state never held.
    _takeStyleIndex(state);
    // Java still holds the table sent before this editor was last hidden: the next one is a change
    // to it, and none at all when the layout is the same.
    final sent = _geometryHeldByJava[state];
    if (sent != null) {
      _geometrySent = sent.table;
      _geometryVersion = sent.version;
      _geometryFullNeeded = false;
    }
  }

  /// Applies one edit Java made: [VEvent.text] put in place of [VEvent.start] to [VEvent.end],
  /// leaving [VEvent.count] characters. A resulting length that differs from Java's means an edit was
  /// missed, so the whole document is requested instead.
  void _onTextChange(VEvent change) {
    if (!mounted) return;
    final start = change.start;
    final end = change.end;
    final inserted = change.text;
    final charCount = change.count;
    if (start == null || end == null || inserted == null) return;
    final replaced = end - start;
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
    noteTextEdit(current, updated, start, replaced, inserted);
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
        EquoCommService.sendOn(WireChannel.swtEvolveWidgetRefresh, '${value.id}');
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
      if (lastChange?.touches(WireKey.k_StyledText_styleIndex) ?? true) {
        _takeStyleIndex(value);
        _splicedStyleIndex = null;
      }
      _splicedCaretOffset = null;
      _splicedCaretOffsets = null;
      _splicedSelectionRanges = null;
    }
    _shapeStale = true;
    super.setValue(value);
  }

  /// The write stamp of the last frame of this widget's own that was applied here.
  int? _appliedSeq;

  void _requestText() {
    EquoCommService.sendTo(WireType.styledText, state.id, WireChannel.resendText);
  }

  @override
  void didChangeDependencies() {
    super.didChangeDependencies();
    // Colours and fonts come from the theme.
    _shapeStale = true;
  }

  @override
  void dispose() {
    _geometryTimer?.cancel();
    final sent = _geometrySent;
    _geometryHeldByJava[state] =
        sent == null || _geometryFullNeeded ? null : (table: sent, version: _geometryVersion);
    _composedInput.detach();
    setCanvasEditorFocus(this, false);
    _focusNode.removeListener(_onFocusChange);
    ResizeReports.windowDragged.removeListener(_onWindowDragged);
    _focusNode.dispose();
    _caretBlinkTimer?.cancel();
    super.dispose();
  }

  /// A parent's rebuild hands this widget down again unchanged: the shape still holds unless the
  /// widget now shows another StyledText. What Java sends arrives through [setValue].
  @override
  void didUpdateWidget(covariant T oldWidget) {
    super.didUpdateWidget(oldWidget);
    if (VRegistry.keyOf(widget.value) != VRegistry.keyOf(oldWidget.value)) _shapeStale = true;
  }

  bool get _focused => _focusNode.hasFocus;

  /// Java focuses a pressed StyledText (ControlHelper.pressed, StyledText.handleMouseDown) and asks
  /// the client to move its keyboard focus there, onto [swtFocusNode].
  @override
  void takeFocusOnPress() {}

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
    if (!identical(original, _delimitersSource) && original != _delimitersSource) {
      debugDelimiterScans++;
      _delimiters = _Delimiters.of(original);
      _delimitersSource = original;
      _shownText = _Delimiters.normalize(original);
    }
    final text = _shownText;

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
      color: _textColor,
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
    final canvas = _canvasSize(previousShape);
    final shape = TextShape(
      text,
      Offset((state.leftMargin ?? 0).toDouble(), (state.topMargin ?? 0).toDouble()),
      defaultStyle,
      null,
      caretInfo,
      state.wordWrap ?? false,
      canvas,
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
      state.fixedLineHeight ?? false,
    );
    if (previousShape != null) shape.inheritMeasurementsFrom(previousShape);
    shape.onRowsOutgrown = () => WidgetsBinding.instance.addPostFrameCallback((_) => _refitRows());
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

  /// What [range] draws as, as a key: every property but where it lies. Read through [visitWire],
  /// which holds every field; a range's writer holds only what Java reads back from the client.
  static String _drawnAs(VStyleRange range) {
    final out = StringBuffer();
    void add(Object? value) {
      if (value is WireVisitable) {
        out.write('{');
        value.visitWire((key, field) {
          if (key == WireKey.k_StyleRange_start || key == WireKey.k_StyleRange_length) return;
          out
            ..write(key)
            ..write(':');
          add(field);
          out.write(',');
        });
        out.write('}');
      } else if (value is List) {
        out.write('[');
        for (final e in value) {
          add(e);
          out.write(',');
        }
        out.write(']');
      } else {
        out.write(value);
      }
    }

    add(range);
    return out.toString();
  }

  /// What a decoded entry also depends on besides its own fields.
  Object? _paletteBasis;

  /// Forgets the decoded entries when what they depend on changed, and says whether it did.
  bool _checkPaletteBasis() {
    final basis = (_getDefaultTextStyle(), _textColor, _linkForeground);
    final changed = basis != _paletteBasis;
    if (changed || _paletteByContent.length > 512) _paletteByContent.clear();
    _paletteBasis = basis;
    return changed;
  }

  /// Rebuilt only when an input changed; every input arrives as a new object when it changes, so
  /// identity is enough.
  TextEditingState _editingStateFor(String shownText) {
    final styles = state.styles;
    final styleIndex = _splicedStyleIndex ?? state.styleIndex;
    // Styled runs inherit the default style too: a font change re-derives them even when the
    // palette is the same object, as it stays in a large document.
    final defaultStyle = _stableDefaultStyle();
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
    final previous = _heldEditingState?.editingState.characterRanges;
    var reused = 0;
    var inOrder = true;
    var lastStart = -1;

    if (styles != null && styles.isNotEmpty) {
      final count =
          banded ? (styleIndex!.length - 1) ~/ 3 : (renderer?.styleCount ?? styles.length);

      // Decoded once per palette entry and kept while the palette is: the paint cache recognises a
      // rasterised run by the identity of its TextStyle.
      final basisChanged = _checkPaletteBasis();
      final samePalette =
          identical(_paletteSource, styles) && _paletteDecoded != null && !basisChanged;
      final decoded = samePalette
          ? _paletteDecoded!
          : (_paletteDecoded = List<_PaletteStyle?>.filled(styles.length, null));
      _paletteSource = styles;

      for (int i = 0; i < count; i++) {
        final named = banded ? styleIndex![1 + 3 * i] : i;
        if (named < 0 || named >= styles.length) continue;
        final vRange = styles[named];
        if (vRange == null) continue;
        final entry = decoded[named] ??= _paletteByContent[_drawnAs(vRange)] ??= _PaletteStyle(
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
        final shownStart = _delimiters.toShown(start);
        final shownEnd = _delimiters.toShown(start + length);
        if (shownStart < lastStart) inOrder = false;
        lastStart = shownStart;
        // The run the last build made here, while it still matches: an edit moves only the runs
        // after it, so typing allocates for those alone.
        final reuse = previous != null && reused < previous.length ? previous[reused] : null;
        if (reuse != null &&
            reuse.start == shownStart &&
            reuse.end == shownEnd &&
            identical(reuse.style, entry.style) &&
            identical(reuse.borderColor, entry.borderColor) &&
            identical(reuse.underlineColor, entry.underlineColor) &&
            identical(reuse.strikeoutColor, entry.strikeoutColor) &&
            reuse.underlineStyle == entry.underlineStyle &&
            reuse.borderStyle == entry.borderStyle &&
            reuse.glyphWidth == entry.glyphWidth &&
            reuse.paintsStrikeout == entry.paintsStrikeout &&
            reuse.rise == entry.rise) {
          characterRanges.add(reuse);
          reused++;
          continue;
        }
        reused = previous?.length ?? 0;
        characterRanges.add(StyleRange(
          start: shownStart,
          end: shownEnd,
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

    // Java sends the runs in order; only an index out of order needs sorting.
    if (!inOrder) characterRanges.sort((a, b) => a.start.compareTo(b.start));

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
  /// glyphs are drawn in it over the highlight. Null leaves [SelectionInfo]'s default.
  Color? get _selectionForeground => state.selectionForeground == null
      ? null
      : colorFromVColor(state.selectionForeground);

  @visibleForTesting
  TextStyle debugRangeStyle(VStyleRange vRange) => _convertVStyleRangeToTextStyle(vRange);

  /// The runs the text is drawn in, as last built.
  @visibleForTesting
  List<StyleRange>? get debugCharacterRanges => _heldEditingState?.editingState.characterRanges;

  TextStyle _convertVStyleRangeToTextStyle(VStyleRange vRange) {
    final defaultTextColor = _textColor;
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
      fontFamilyFallback: FontUtils.fallbackFor(fontFamily),
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
    final defaultTextColor = _textColor;

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

  /// A line painted in this frame was taller than the rows: place them again, at its height.
  void _refitRows() {
    if (mounted && (_shape?.rowsOutgrown ?? false)) setState(() => _shapeStale = true);
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
        SizedBox(
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

  /// Both axes go through Java, where a listener may veto before the bar moves. An unmodified
  /// vertical tick sends pixels rather than lines, so a trackpad does not jump.
  void _onWheel(PointerScrollEvent event) {
    // A StyledText scrolls by the wheel, so it keeps it: its parents do not report it again.
    if (ControlImpl.wheelKeptBelow(event)) return;
    ControlImpl.markWheelKept(event);
    final stateMask = moveStateMask(0);
    for (final wheel in wheelEvents(event, stateMask: stateMask)) {
      if (wheel.type == SWT.MouseWheel && stateMask & SWT.MODIFIER_MASK == 0) {
        final exact = _wheelCarry + wheelDelta(event).dy;
        final pixels = exact.round();
        _wheelCarry = exact - pixels;
        if (pixels == 0) continue;
        wheel.detail = pixels;
      }
      widget.sendMouseWheelMouseWheel(state, wheel);
    }
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
    // The measured layout counts wrapped rows; the logical line count does not, and a content
    // box sized from it clips the text and the wheel short of the bottom of the view.
    final shape = _shape;
    final textHeight = shape != null
        ? shape.off.dy + shape.textHeight + (state.bottomMargin ?? 0)
        : _metricsLineCount * lineHeight;
    final totalHeight = math.max(getBounds().height, textHeight);

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

  /// A geometry push's kind, its first byte: as Java's `StyledTextHelper.PUSH_*` reads it.
  static const int _pushWhole = 0, _pushSplice = 1, _pushBand = 2;
  bool _geometryFullNeeded = true;
  double? _geometrySentBandTop;
  double? _geometrySentBandBottom;
  GeometryTable? _geometrySent;

  /// The table Java holds for an editor no state shows, kept on the editor's value, which outlives
  /// the states that show it.
  static final Expando<({GeometryTable table, int version})> _geometryHeldByJava = Expando();

  /// Comparing the widget's text style is expensive, so layout keys carry a token for it instead. The
  /// layout cache is shared by every StyledText, so the token stands for the style's value: equal
  /// styles share a token, and with it the lines another editor already laid out; any other style,
  /// a font or a colour apart, has its own.
  static final Map<TextStyle, int> _styleTokens = {};
  static int _nextStyleToken = 0;
  TextStyle? _lastDefaultStyle;
  int _styleToken = 0;

  TextStyle _stableDefaultStyle() {
    final built = _getDefaultTextStyle();
    final last = _lastDefaultStyle;
    if (last == null || built != last) {
      _lastDefaultStyle = built;
      // Tokens keep counting up, so one handed out before a clear is never reused for another style.
      if (_styleTokens.length >= 256) _styleTokens.clear();
      _styleToken = _styleTokens[built] ??= ++_nextStyleToken;
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

  /// How many times a shape rebuild scanned the text for its line delimiters.
  @visibleForTesting
  static int debugDelimiterScans = 0;

  /// Test seam: the transport is a no-op in a widget test, so a push is otherwise
  /// unobservable. Counts every payload pushed and keeps the last one.
  @visibleForTesting
  static int debugGeometryPushes = 0;
  static GeometryTable? _debugLastTable;
  @visibleForTesting
  static Map<String, dynamic>? get debugLastGeometry => _debugLastTable?.toMap();
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
    final geometry = perfMarkValue(
        'StyledText.geometryTable',
        () => shape.bandsGeometry
            ? shape.computeBandTable(top, bottom)
            : shape.computeGeometryTable(visibleTop: top, visibleBottom: bottom));
    debugGeometryMicros += geometryWatch.elapsedMicroseconds;
    debugGeometryCalls++;
    _geometrySentForLayout = shape._lineTopsCache;
    _geometrySentBandTop = top;
    _geometrySentBandBottom = bottom;
    _delimiters.geometryToDocument(geometry);
    final previous = _geometrySent;
    // One pass answers both whether anything changed and what to send.
    // A splice replaces rows of the table Java holds, so both have to hold the same rows.
    final splice = (previous == null || _geometryFullNeeded || !previous.holdsTheRowsOf(geometry))
        ? null
        : _spliceOf(previous.rows, geometry.rows);
    if (splice != null &&
        splice.rowsReplaced == 0 &&
        splice.rows.isEmpty &&
        previous!.sameExtentsAs(geometry)) {
      return;
    }
    _geometrySent = geometry;
    debugGeometryPushes++;
    _debugLastTable = geometry;
    final w = WidgetWireWriter.startTo(state.wireType, state.id, WireChannel.textGeometry);
    _writeGeometry(w, geometry, splice);
    EquoCommService.sendWritten(w);
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
  ///
  /// `u8 kind (_pushWhole, _pushSplice, _pushBand), version, [base], charCount, contentWidth, contentHeight,
  /// [rowHeight, bandFirstLine], [rowStart, rowsReplaced, lineDelta, offsetDelta, yDelta], rows`, as
  /// Java's `StyledTextHelper.readPush` reads it. A splice of a band keeps the band's row height and
  /// first line.
  void _writeGeometry(WidgetWireWriter w, GeometryTable table, _GeometrySplice? splice) {
    final version = ++_geometryVersion;
    // A splice that rewrites most of the table (a resize, a wrap toggle) is the table.
    final whole = splice == null || splice.rows.length * 2 > table.rows.length;
    if (splice == null) _geometryFullNeeded = false;
    final band = table.bandFirstLine;
    w
      ..u8(!whole ? _pushSplice : band != null ? _pushBand : _pushWhole)
      ..int32(version);
    if (!whole) w.int32(version - 1);
    w
      ..int32(table.charCount)
      ..float64(table.contentWidth)
      ..float64(table.contentHeight);
    if (whole && band != null) {
      w
        ..float64(table.rowHeight!)
        ..int32(band);
    }
    final rows = whole ? table.rows : splice!.rows;
    if (!whole) {
      w
        ..int32(splice!.rowStart)
        ..int32(splice.rowsReplaced)
        ..int32(splice.lineDelta)
        ..int32(splice.offsetDelta)
        ..float64(splice.yDelta);
    }
    w.varint(rows.length);
    final writer = GeometryRowWriter(w);
    for (final row in rows) {
      writer.write(row);
    }
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


/// What a style's text lays out to vertically, when every printable ASCII character and a tab lay
/// out to the same: null for a style where some of them would not (a glyph from a fallback font,
/// say), whose lines have to be laid out to know their height.
class _VerticalMetrics {
  _VerticalMetrics._(this.ascent, this.descent, this.height, this.emptyHeight, this.averageAdvance);

  final double ascent;
  final double descent;
  final double height;

  /// The mean width of the printable ASCII characters, for a line's width before it is laid out.
  final double averageAdvance;

  /// An empty line's height, which comes from the style rather than from any glyph.
  final double emptyHeight;

  bool sameAs(_VerticalMetrics other) =>
      ascent == other.ascent && descent == other.descent && height == other.height;

  /// What [of] found for each style, with [_none] for a style it found no metrics for. Kept as
  /// long as the line layouts measured in the same fonts are ([debugResetLineLayoutCache]).
  static Expando<Object> _ofStyle = Expando();
  static final Object _none = Object();

  /// The same by the style's value: a font change makes every style anew, and zooming back makes
  /// styles equal to ones measured before.
  static final Map<TextStyle, Object> _ofEqualStyle = {};

  static _VerticalMetrics? of(TextStyle style) {
    final known = _ofStyle[style] ?? _ofEqualStyle[style];
    if (known != null) {
      _ofStyle[style] = known;
      return known is _VerticalMetrics ? known : null;
    }
    final metrics = _measure(style);
    if (_ofEqualStyle.length >= 512) _ofEqualStyle.clear();
    _ofStyle[style] = _ofEqualStyle[style] = metrics ?? _none;
    return metrics;
  }

  static double _lastWidth = 0;

  static (double, double, double)? _laidOut(InlineSpan span, [List<PlaceholderDimensions>? placeholders]) {
    final painter = TextPainter(text: span, textDirection: TextDirection.ltr);
    if (placeholders != null) painter.setPlaceholderDimensions(placeholders);
    painter.layout();
    final lines = painter.computeLineMetrics();
    final result = lines.length == 1 ? (lines.first.ascent, lines.first.descent, painter.height) : null;
    _lastWidth = painter.width;
    painter.dispose();
    return result;
  }

  static _VerticalMetrics? _measure(TextStyle style) {
    TextShape.debugStylesMeasured++;
    // As a run inside a line, and as a line's own style: both have to agree.
    final asRoot = _laidOut(TextSpan(text: 'x', style: style));
    final reference = _laidOut(TextSpan(children: [TextSpan(text: 'x', style: style)]));
    if (asRoot == null || reference == null || asRoot != reference) return null;
    var widths = 0.0;
    for (var c = 0x20; c <= 0x7E; c++) {
      if (_laidOut(TextSpan(children: [TextSpan(text: String.fromCharCode(c), style: style)])) != reference) {
        return null;
      }
      widths += _lastWidth;
    }
    final tabbed = _laidOut(
        TextSpan(children: [TextSpan(text: 'x', style: style), _tabPlaceholder]),
        const [PlaceholderDimensions(
          size: Size(8, 0),
          alignment: PlaceholderAlignment.baseline,
          baseline: TextBaseline.alphabetic,
          baselineOffset: 0,
        )]);
    if (tabbed != reference) return null;
    final empty = TextPainter(text: TextSpan(text: '', style: style), textDirection: TextDirection.ltr)
      ..layout();
    final emptyHeight = empty.height;
    empty.dispose();
    return _VerticalMetrics._(reference.$1, reference.$2, reference.$3, emptyHeight, widths / (0x7E - 0x20 + 1));
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

/// The x of one line's character boundaries for the geometry table: its layout's caret x when it
/// has them, else a painter laid out on first use, which [dispose] releases.
class _LineCharX {
  _LineCharX(this._caretX, this._newPainter);

  final List<double>? _caretX;
  final TextPainter Function() _newPainter;
  TextPainter? _painter;

  /// The boundaries [from]..[to] of the line, each offset by [base].
  List<double> call(int from, int to, double base) {
    final caretX = _caretX;
    if (caretX != null) return [for (int k = from; k <= to; k++) base + caretX[k]];
    final tp = _painter ??= _newPainter();
    return [for (int k = from; k <= to; k++) base + TextShape._caretXOf(tp, k)];
  }

  void dispose() => _painter?.dispose();
}

/// What the geometry table and the line-tops prefix sum need from one logical line's
/// layout. Every field is a pure function of the [_LineLayoutKey] it is stored under,
/// so a cached entry cannot go stale: a line whose text, styles, alignment, wrap width
/// or tab width changes hashes to a different key.
class _LineLayout {
  _LineLayout(this.width, this.height, this.metrics, this.rowEnds);

  /// What a layout without caret positions is charged against the cache budget: its rows, the
  /// object itself and its key, which together weigh about what a dozen caret positions do.
  static const int _entryCost = 16;

  /// What this entry weighs against the cache budget now: one unit per caret position, the array
  /// the memory goes into, plus [_entryCost]. A line measured only for its height and width costs
  /// little, so a large document's layouts all fit and a rebuilt shape finds them again.
  int get cost => _entryCost + metrics.length + (caretX?.length ?? 0);

  /// [cost] when the entry was last put in the cache: what it gives back when it leaves.
  int charged = 0;

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
/// re-inserts its key. The budget counts caret positions (see [_LineLayout.cost]) rather than
/// entries because the per-character x array is what the memory goes into.
final Map<_LineLayoutKey, _LineLayout> _lineLayoutCache = {};
int _lineLayoutCacheChars = 0;
const int _lineLayoutCacheCharBudget = 200000;

/// Every change to [_lineLayoutCache], as the text whose entry came or went, numbered from
/// [_layoutCacheLogStart]. A lookup answers as it did while no entry of its text has come or gone
/// since, since an entry that matches has the same text.
final List<String> _layoutCacheLog = [];
int _layoutCacheLogStart = 0;
const int _layoutCacheLogLimit = 4096;

/// The number the next change to [_lineLayoutCache] is logged under.
int get _layoutCacheSeq => _layoutCacheLogStart + _layoutCacheLog.length;

/// A line's length and a few of its characters: the same text always gives the same number, and
/// lines of one length, which code is full of, mostly do not; cheaper than hashing the line.
int _textFingerprint(String text) {
  final n = text.length;
  if (n == 0) return 0;
  var h = (n * 31 + text.codeUnitAt(n >> 2)) & 0x3FFFFFFF;
  h = (h * 31 + text.codeUnitAt(n >> 1)) & 0x3FFFFFFF;
  h = (h * 31 + text.codeUnitAt((n * 3) >> 2)) & 0x3FFFFFFF;
  return (h * 31 + text.codeUnitAt(n - 1)) & 0x3FFFFFFF;
}

void _noteLayoutCacheChange(String text) {
  _layoutCacheLog.add(text);
  if (_layoutCacheLog.length > _layoutCacheLogLimit) {
    final dropped = _layoutCacheLog.length - _layoutCacheLogLimit ~/ 2;
    _layoutCacheLog.removeRange(0, dropped);
    _layoutCacheLogStart += dropped;
  }
}

/// The texts whose entries came or went since change [seq], or null when that is further back than
/// the log reaches.
_ChangedTexts? _layoutTextsChangedSince(int seq) {
  if (seq < _layoutCacheLogStart) return null;
  final changed = _ChangedTexts();
  for (var k = seq - _layoutCacheLogStart; k < _layoutCacheLog.length; k++) {
    changed.add(_layoutCacheLog[k]);
  }
  return changed;
}

/// Texts, with their [_textFingerprint]s, so that most lines are told apart from them without
/// hashing the whole line.
class _ChangedTexts {
  final Set<int> _fingerprints = {};
  final Set<String> _texts = {};

  void add(String text) {
    if (_texts.add(text)) _fingerprints.add(_textFingerprint(text));
  }

  bool contains(String text) => _fingerprints.contains(_textFingerprint(text)) && _texts.contains(text);
}

void _cacheLineLayout(_LineLayoutKey key, _LineLayout layout) {
  _watchSystemFonts();
  // The replaced entry stops counting, so a re-inserted line is charged to the budget once.
  final replaced = _lineLayoutCache.remove(key);
  if (replaced != null) {
    _lineLayoutCacheChars -= replaced.charged;
    // Same key, same layout: keep the caret positions an earlier pass measured.
    layout.caretX ??= replaced.caretX;
  }
  _lineLayoutCache[key] = layout;
  // A hit put back at the young end answers as it did; only a new or different layout is a change.
  if (!identical(replaced, layout)) _noteLayoutCacheChange(key.lineText);
  // The key's text stays in memory too, at about a quarter of a caret position per character.
  layout.charged = layout.cost + key.lineText.length ~/ 4;
  _lineLayoutCacheChars += layout.charged;
  while (_lineLayoutCacheChars > _lineLayoutCacheCharBudget &&
      _lineLayoutCache.length > 1) {
    final eldest = _lineLayoutCache.keys.first;
    final evicted = _lineLayoutCache.remove(eldest);
    if (evicted != null) _lineLayoutCacheChars -= evicted.charged;
    _noteLayoutCacheChange(eldest.lineText);
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
  _watchSystemFonts();
  _paintedLineCache[key] = painter;
  while (_paintedLineCache.length > _paintedLineCacheEntries) {
    final oldest = _paintedLineCache.keys.first;
    _paintedLineCache.remove(oldest)?.dispose();
  }
}

/// The document's lines and line starts, memoised by the text's identity, for the texts used last:
/// an edit is laid out against the text before it (TextShape._lineDonor), and each view of a document
/// split in two holds a text of its own, before the edit and after it.
final List<_LineCacheEntry> _lineCache = [];
const _lineCacheEntries = 4;

/// How many times a whole text was split into lines, for tests asserting what an edit costs.
@visibleForTesting
int debugWholeTextSplits = 0;

class _LineCacheEntry {
  _LineCacheEntry(this.text, this.lines, this.starts);

  final String text;
  final List<String> lines;
  final List<int> starts;
}

/// The entry held for [text], made the most recently used; null when none is.
_LineCacheEntry? _heldLines(String text) {
  for (var i = 0; i < _lineCache.length; i++) {
    final entry = _lineCache[i];
    if (!identical(entry.text, text)) continue;
    if (i != 0) {
      _lineCache
        ..removeAt(i)
        ..insert(0, entry);
    }
    return entry;
  }
  return null;
}

void _holdLines(_LineCacheEntry entry) {
  _lineCache.insert(0, entry);
  if (_lineCache.length > _lineCacheEntries) _lineCache.removeLast();
}

_LineCacheEntry _fillLineCache(String text) {
  final held = _heldLines(text);
  if (held != null) return held;
  // Another view of the same document holds the same text in a string of its own.
  for (final entry in _lineCache) {
    if (entry.text.length == text.length && entry.text == text) {
      final same = _LineCacheEntry(text, entry.lines, entry.starts);
      _holdLines(same);
      return same;
    }
  }
  debugWholeTextSplits++;
  final lines = text.split('\n');
  final starts = List<int>.filled(lines.length, 0);
  int at = 0;
  for (int i = 0; i < lines.length; i++) {
    starts[i] = at;
    at += lines[i].length + 1;
  }
  final entry = _LineCacheEntry(text, lines, starts);
  _holdLines(entry);
  return entry;
}

/// The columns [line] takes, each tab reaching the next stop of [tabColumns]. Tab to tab rather
/// than character by character: on the web each character read is a call out to the string, and
/// this runs for every line out of view on every keystroke.
@visibleForTesting
int columnsOf(String line, int tabColumns) {
  int columns = 0;
  int from = 0;
  while (true) {
    final tab = line.indexOf('\t', from);
    if (tab < 0) return columns + line.length - from;
    columns = ((columns + tab - from) ~/ tabColumns + 1) * tabColumns;
    from = tab + 1;
  }
}

/// Takes [after], which is [before] with [replaced] characters at [start] replaced by [inserted],
/// into the line cache from [before]'s lines when the cache holds them: the lines the edit did not
/// reach are reused and only the ones it did are split again, where splitting [after] would read
/// the whole document on every keystroke.
void noteTextEdit(String before, String after, int start, int replaced, String inserted) {
  final held = _heldLines(before);
  if (held == null) return;
  final lines = held.lines;
  final starts = held.starts;
  final end = start + replaced;
  if (start < 0 || end > before.length) return;
  final first = _lineAt(starts, start);
  final last = _lineAt(starts, end);
  final firstStart = starts[first];
  final lastEnd = starts[last] + lines[last].length;
  final middle = (before.substring(firstStart, start) + inserted + before.substring(end, lastEnd)).split('\n');
  final delta = inserted.length - replaced;
  final newLines = List<String>.filled(lines.length - (last - first + 1) + middle.length, '');
  final newStarts = List<int>.filled(newLines.length, 0);
  newLines.setRange(0, first, lines);
  newStarts.setRange(0, first, starts);
  var at = firstStart;
  for (int k = 0; k < middle.length; k++) {
    newLines[first + k] = middle[k];
    newStarts[first + k] = at;
    at += middle[k].length + 1;
  }
  final tail = first + middle.length;
  newLines.setRange(tail, newLines.length, lines, last + 1);
  for (int k = last + 1, j = tail; k < lines.length; k++, j++) {
    newStarts[j] = starts[k] + delta;
  }
  _holdLines(_LineCacheEntry(after, newLines, newStarts));
}

/// The line [offset] falls on, by its start among [starts].
int _lineAt(List<int> starts, int offset) {
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

/// The lines the line cache holds for [text], or null when it holds none: a test seam.
@visibleForTesting
List<String>? cachedLinesFor(String text) => _heldLines(text)?.lines;

/// Fills the line cache for [text] the way a shape does: a test seam.
@visibleForTesting
List<String> fillLineCacheFor(String text) => _linesOf(text);

/// [cachedLinesFor]'s line starts.
@visibleForTesting
List<int>? cachedLineStartsFor(String text) => _heldLines(text)?.starts;

List<String> _linesOf(String text) => _fillLineCache(text).lines;

List<int> _lineStartsOf(String text) => _fillLineCache(text).starts;

/// [memo], or a new one, long enough to hold [index]. It grows with the lines asked about, by
/// doubling, rather than starting at the document's length on every new shape.
List<int?> _memoHolding(List<int?>? memo, int index) {
  memo ??= <int?>[];
  if (index >= memo.length) memo.length = math.max(index + 1, memo.length * 2);
  return memo;
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
    'geometryMs': StyledTextImpl.debugGeometryMicros / 1000.0,
    'geometryCalls': StyledTextImpl.debugGeometryCalls,
    'geometryRowsReused': TextShape.debugGeometryRowsReused,
    'rebuildMs': StyledTextImpl.debugRebuildMicros / 1000.0,
    'rebuildCalls': StyledTextImpl.debugRebuildCalls,
    'marksEnabled': perfMarksEnabled,
  };
  StyledTextImpl.debugGeometryMicros = 0;
  StyledTextImpl.debugGeometryCalls = 0;
  StyledTextImpl.debugRebuildMicros = 0;
  StyledTextImpl.debugRebuildCalls = 0;
  TextShape.debugDrawCalls = 0;
  TextShape.debugDrawMicros = 0;
  TextShape.debugLineTopsMicros = 0;
  TextShape.debugLayoutLineCalls = 0;
  TextShape.debugGeometryRowsReused = 0;
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
  for (final painter in _paintedLineCache.values) {
    painter.dispose();
  }
  _dropLineCaches();
  debugLineLayoutHits = 0;
}

/// Dropped, not disposed: a picture already recorded with a painted line may be rasterised again,
/// and on the web a disposed paragraph draws as garbage there. The engine frees it once nothing
/// references it.
void _dropLineCaches() {
  _fontEpoch++;
  _lineLayoutCache.clear();
  // Every entry went: no lookup made before answers as it did.
  _layoutCacheLogStart += _layoutCacheLog.length + 1;
  _layoutCacheLog.clear();
  _lineLayoutCacheChars = 0;
  _paintedLineCache.clear();
  _VerticalMetrics._ofStyle = Expando();
  _VerticalMetrics._ofEqualStyle.clear();
}

/// Moves on whenever the line caches are dropped because the fonts changed: what a shape measured
/// before then is not what its lines lay out to now. A font that finishes loading after the first
/// layout is the case that matters: an empty line measured in the stand-in font kept that height.
int _fontEpoch = 0;

bool _watchingSystemFonts = false;

void _watchSystemFonts() {
  if (!_watchingSystemFonts) _watchingSystemFonts = FontUtils.listenToSystemFonts(_dropLineCaches);
}

class TextShape extends Shape {
  @override
  String describe() => 'StyledText "$text" @ ${off.dx.round()},${off.dy.round()} '
      'font=${style.fontFamily}/${style.fontSize} '
      'color=${style.color == null ? "-" : style.color!.toString()} '
      'wrap=$wordWrap tabs=$tabs lineHeight=$lineHeight';

  final String text;
  final Offset off;
  final TextStyle style;
  final TextSpan? textSpan;
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
    this.fixedLineHeight = false,
  ]);

  /// Stands for [style] in a layout key: a change count, since comparing the style is expensive.
  final int styleToken;

  /// StyledText#isFixedLineHeight: every line is one height, as native SWT lays such an editor out.
  final bool fixedLineHeight;

  /// Every row is [_rowHeight] tall: the rows are known without measuring a line, so the lines out of
  /// view are left unmeasured, as native SWT leaves them.
  bool get _uniformRows =>
      fixedLineHeight && wordWrap != true && lineHeight > 0 && lineSpacing == 0 && lineSpacings == null;

  /// Whether every line is one row of [_rowHeight] with no vertical indent above it, so line `i`
  /// starts at `i * _rowHeight`: the line tops and the rows Java derives from a band both rely on
  /// it. A line with properties of its own, such as the current line's background, has no indent.
  bool get _rowsOfRowHeight =>
      _uniformRows && !(editingState?.lineProperties.values.any((p) => (p.verticalIndent ?? 0) != 0) ?? false);

  /// The tallest line measured by this shape and the shapes it replaced. Native SWT makes every
  /// line as tall as the tallest one it has laid out (a fallback glyph's font can be taller than the
  /// editor's), and so does [_rowHeight].
  double _tallestLine = 0;
  double? _rowHeightValue;

  /// Set when a line measured after the rows were placed is taller than they are: the owner
  /// builds the shape again, with rows that fit it.
  bool rowsOutgrown = false;

  /// Told once, when [rowsOutgrown] is set.
  VoidCallback? onRowsOutgrown;

  double get _rowHeight =>
      _rowHeightValue ??= math.max(lineHeight, math.max(_tallestLine, _plainLineHeight));

  /// How tall a line of the editor's own font lays out, before any line is measured.
  late final double _plainLineHeight = () {
    final tp = TextPainter(text: TextSpan(text: ' ', style: style), textDirection: TextDirection.ltr)
      ..layout();
    final height = tp.height;
    tp.dispose();
    return height;
  }();

  /// A line holding a character that breaks it (a form feed, U+2028) lays out on several rows even
  /// without wrapping; native SWT sizes the rows by the tallest of them, and the rest overflow.
  static double _tallestRowOf(_LineLayout layout) {
    if (layout.metrics.isEmpty) return layout.height;
    var tallest = 0.0;
    for (final row in layout.metrics) {
      if (row.height > tallest) tallest = row.height;
    }
    return tallest;
  }

  void _noteLineHeight(double height) {
    if (height <= _tallestLine) return;
    _tallestLine = height;
    final rows = _rowHeightValue;
    if (!rowsOutgrown && rows != null && height > rows + 0.5) {
      rowsOutgrown = true;
      onRowsOutgrown?.call();
    }
  }

  /// What the rows of [other] were placed with, for a shape that shares its line tops.
  void _takeRowHeight(TextShape other) {
    _tallestLine = math.max(_tallestLine, other._tallestLine);
    _rowHeightValue = other._rowHeightValue;
  }

  /// Everything a line's measured height depends on but the text; shapes that agree on it and on
  /// the text share measurements. The text is compared rather than hashed: a web string's hash reads
  /// every character, and a document's text is a new string on every keystroke.
  late final int layoutSignature = Object.hash(
      _frameSignature,
      editingState?.layoutSignatureFor(style) ?? 0);

  /// What every line's layout depends on apart from its own text and styling.
  late final int _frameSignature = Object.hash(
      styleToken,
      // The width only matters when wrapping, so a resize is free for a document that does not wrap.
      wordWrap == true ? Object.hash(canvasSize?.width, off.dx) : null,
      tabs,
      const ListEquality<int>().hash(tabStops),
      Object.hash(lineSpacing, lineHeight, direction, fixedLineHeight),
      lineSpacings == null ? 0 : const ListEquality<int>().hash(lineSpacings!),
      widgetLine.indent,
      widgetLine.wrapIndent,
      widgetLine.alignment,
      widgetLine.justify);

  /// Takes over what [other] measured when both lay out the same way. After an edit, [other] answers
  /// for the lines the edit did not reach ([_donatedLine]).
  void inheritMeasurementsFrom(TextShape other) {
    // Measured in fonts that have changed since: nothing of it carries over.
    if (other._measuredEpoch != _fontEpoch) return;
    if (other._frameSignature == _frameSignature) {
      _tallestLine = math.max(_tallestLine, other._tallestLine);
      // The last shape that described the document: one that was replaced before it did hands on
      // its own donor. As for [_lineDonor], one generation back is all that is held.
      _geometryDonor = other._geometryMemo != null ? other : other._geometryDonor;
      other._geometryDonor = null;
    }
    if (other.layoutSignature == layoutSignature && other.text == text) {
      // Rows that a measured line outgrew are placed again, at the height that fits it.
      if (!other.rowsOutgrown) {
        _lineTopsCache = other._lineTopsCache;
        _rowHeightValue = other._rowHeightValue;
      }
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
    if (donor == null || donor._measuredEpoch != _fontEpoch) return null;
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
      final mine = _rangesTokenCache = _memoHolding(_rangesTokenCache, lineIndex);
      mine[lineIndex] = tokens[theirs];
    }
    return layout;
  }

  /// Whether the line carries the same style objects at the same offsets as [donor]'s line
  /// [theirs]; palette entries are shared objects, so no hashing is needed.
  bool _sameRunsAs(TextShape donor, int theirs, int lineIndex, int lineLength) {
    final a = donor._runsOnLine(theirs);
    final b = _runsOnLine(lineIndex);
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

  /// The [_fontEpoch] this shape's own measurements were taken in.
  int _measuredEpoch = _fontEpoch;

  /// Drops what this shape measured in fonts that have changed since: its line layouts, the line
  /// tops summed from them and the shape it would take lines from.
  void _forgetStaleMeasurements() {
    if (_measuredEpoch == _fontEpoch) return;
    _measuredEpoch = _fontEpoch;
    _measuredLineCache = null;
    _lineTopsCache = _LineTopsCache();
    _lineDonor = null;
  }

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
  ///
  /// [laidOut] is the line's paint paragraph when the caller holds it, and [paintParagraph] asks for
  /// one to be laid out and kept for the paint: either way the line is laid out once.
  _LineLayout _measuredLine(int lineIndex, String lineText,
      {bool wantCaretX = false, TextPainter? laidOut, bool paintParagraph = false}) {
    _forgetStaleMeasurements();
    final layout = _measuredLineNow(lineIndex, lineText, wantCaretX, laidOut, paintParagraph);
    if (_uniformRows) _noteLineHeight(_tallestRowOf(layout));
    return layout;
  }

  _LineLayout _measuredLineNow(
      int lineIndex, String lineText, bool wantCaretX, TextPainter? laidOut, bool paintParagraph) {
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
        align: props.align, maxWidth: _lineMaxWidth(props.layoutIndent), wantCaretX: wantCaretX, laidOut: laidOut, paintParagraph: paintParagraph);
    if (inRange) memo[lineIndex] = layout;
    return layout;
  }

  /// The layout of [lineIndex] if this shape, or one it replaced, already measured it.
  _LineLayout? _peekLine(int lineIndex, String lineText) {
    final memo = _measuredLineCache;
    if (memo != null && lineIndex >= 0 && lineIndex < memo.length && memo[lineIndex] != null) {
      _peekedFrom = _GeometryMemo.ownLayout;
      return memo[lineIndex];
    }
    final donated = _donatedLine(lineIndex, lineText, false);
    if (donated != null) {
      _peekedFrom = _GeometryMemo.donatedLayout;
      return donated;
    }
    final props = _linePropsFor(lineIndex);
    final cached = _lineLayoutCache[_layoutKeyFor(lineIndex, lineText, props.align, _lineMaxWidth(props.layoutIndent))];
    _peekedFrom = cached != null ? _GeometryMemo.cachedLayout : _GeometryMemo.estimated;
    return cached;
  }

  /// Where the last [_peekLine] found its answer, as a [_GeometryMemo] kind.
  int _peekedFrom = _GeometryMemo.notOutOfView;

  /// What [lineIndex]'s layout is cached under.
  _LineLayoutKey _layoutKeyFor(int lineIndex, String lineText, TextAlign align, double maxWidth) => _LineLayoutKey(
        lineText,
        styleToken,
        _rangesTokenFor(lineIndex, lineText.length),
        align,
        maxWidth,
        tabs,
        _tabStopsFor(lineIndex),
      );

  double? _columnAdvance;

  /// A line's width from its column count, tabs expanded to the default stops: exact in a fixed-pitch
  /// font without pixel tab stops, an estimate otherwise, until the line is laid out.
  double _estimatedWidth(String lineText) {
    final advance = _columnAdvance ??= _measureRun('0' * 10, style) / 10;
    return columnsOf(lineText, tabs > 0 ? tabs : 4) * advance;
  }

  /// What a line without properties of its own is laid out with: the same for every such line.
  ({int indent, int wrapIndent, int layoutIndent, TextAlign align, int vIndent, String? bulletText})?
      _widgetLineProps;

  ({int indent, int wrapIndent, int layoutIndent, TextAlign align, int vIndent, String? bulletText})
      _buildLinePropsFor(int lineIndex) {
    final props = editingState?.lineProperties[lineIndex];
    if (props == null) return _widgetLineProps ??= _linePropsOf(null);
    return _linePropsOf(props);
  }

  ({int indent, int wrapIndent, int layoutIndent, TextAlign align, int vIndent, String? bulletText})
      _linePropsOf(LineProperties? props) {
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
    final memo = _measuredLineCache;
    double y = 0;
    if (_rowsOfRowHeight) {
      // The same sum, without asking each line.
      final row = _rowHeight;
      for (int i = 0; i < lines.length; i++) {
        tops[i] = y;
        y += row;
      }
      tops[lines.length] = y;
      debugLineTopsMicros += watch.elapsedMicroseconds;
      return tops;
    }
    for (int i = 0; i < lines.length; i++) {
      tops[i] = y;
      final props = _linePropsFor(i);
      if (_uniformRows) {
        y += props.vIndent + _rowHeight;
      } else {
        final height = (memo != null && i < memo.length ? memo[i]?.height : null) ??
            _shapelessHeight(i, lines[i]) ??
            _measuredLine(i, lines[i]).height;
        y += props.vIndent + _advance(height, i);
      }
    }
    tops[lines.length] = y;
    debugLineTopsMicros += watch.elapsedMicroseconds;
    return tops;
  }

  /// Each line's [_shapelessHeight] beside the height laying it out gives.
  @visibleForTesting
  List<(double?, double)> debugShapelessHeights() => [
        for (final (i, line) in _lines.indexed) (_shapelessHeight(i, line), _measuredLine(i, line).height),
      ];

  /// The height [lineIndex] lays out to, when that is known without laying it out, else null.
  ///
  /// A line's height is the tallest ascent plus the deepest descent of the fonts its characters are
  /// drawn in. An unwrapped line of printable ASCII and tabs, in styles that all have the same
  /// vertical metrics for each of those characters ([_VerticalMetrics]), is that height whatever
  /// its text: the one every line of those styles has.
  double? _shapelessHeight(int lineIndex, String lineText) {
    if (wordWrap == true) return null;
    final base = _VerticalMetrics.of(style);
    if (base == null) return null;
    if (lineText.isEmpty) return base.emptyHeight;
    for (var k = 0; k < lineText.length; k++) {
      final c = lineText.codeUnitAt(k);
      if ((c < 0x20 || c > 0x7E) && c != 0x09) return null;
    }
    final ranges = _runsOnLine(lineIndex);
    if (ranges.isEmpty) return base.height;
    final lineStart = lineIndex < _lineStarts.length ? _lineStarts[lineIndex] : 0;
    final lineEnd = lineStart + lineText.length;
    for (final range in ranges) {
      if (range.end <= lineStart || range.start >= lineEnd) continue;
      if (range.rise != 0 || range.glyphWidth != null) return null;
      if (identical(range.style, style)) continue;
      final metrics = _VerticalMetrics.of(range.style);
      if (metrics == null || !metrics.sameAs(base)) return null;
    }
    return base.height;
  }

  List<double> _lineTops(List<String> lines) {
    _forgetStaleMeasurements();
    return _lineTopsCache.tops ??= _lineTopsFor(lines, canvasSize);
  }

  /// Height of the laid-out text, wrapped rows included, from the top of its first line.
  double get textHeight => _lineTops(_lines).last;

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
    if (_uniformRows) return _rowHeight;
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
    final selections = [for (final s in allSelections) if (s.hasSelection) s];
    debugPaintedSelectionRects.clear();

    for (int i = firstLine; i < lines.length; i++) {
      currentY = paintOffset.dy + tops[i];
      if (currentY > cullBottom) break;
      final line = lines[i];

      final props = _linePropsFor(i);
      final vIndent = props.vIndent;
      final maxW = _lineMaxWidth(props.layoutIndent);

      final tp = _layoutLine(
        line,
        i,
        align: props.align,
        maxWidth: maxW,
      );
      final advance = vIndent + _advance(_measuredLine(i, line, laidOut: tp).height, i);
      if (firstPainted < 0) firstPainted = i;
      lastPainted = i;
      paintedRows.add([i.toDouble(), currentY]);

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
      final selected = _selectionsOnLine(selections, i, line);
      void paintRows(double x, Rect? band) {
        if (band != null) {
          c.save();
          c.clipRect(band);
        }
        final origin = Offset(x, currentY + vIndent);
        _drawRangeBackgrounds(c, i, line, tp, origin, currentY + advance);
        _paintLineWithRises(c, tp, i, line, origin);
        _drawRangeBorders(c, i, line, tp, origin);
        _drawSeparateStrikeouts(c, i, line, tp, origin);
        for (final run in selected) {
          if (run.from < run.to) _drawSelectedRun(c, run, i, line, tp, maxW, origin, band);
        }
        if (band != null) c.restore();
      }
      _drawBullet(c, props, paintOffset.dx, currentY + vIndent);
      if (_uniformRows && rows.length > 1) {
        // The rows its own characters broke it onto lie under the lines below, as natively.
        paintRows(originX, Rect.fromLTRB(0, currentY, canvasSize?.width ?? tp.width, currentY + advance));
      } else if (wrapShift == 0 || rows.length < 2) {
        paintRows(originX, null);
      } else {
        final split = currentY + vIndent + rows.first.height;
        final width = canvasSize?.width ?? tp.width;
        paintRows(originX, Rect.fromLTRB(0, currentY, width, split));
        paintRows(originX + wrapShift,
            Rect.fromLTRB(0, split, width, currentY + vIndent + tp.height));
      }
      for (final run in selected) {
        if (run.delimiter) _drawSelectedDelimiter(c, run.selection, tp, originX, currentY + vIndent);
      }
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
    for (final offset in allCarets) {
      final rect = caretRectAt(offset);
      if (rect != null) {
        c.drawRect(rect, Paint()..color = caret.color..style = PaintingStyle.fill);
      }
    }
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
      computeGeometryTable(visibleTop: visibleTop, visibleBottom: visibleBottom).toMap();

  /// What the last geometry pass worked out for the lines it described out of view.
  _GeometryMemo? _geometryMemo;

  /// The shape this one replaced, whose [_geometryMemo] its first pass can take lines from.
  TextShape? _geometryDonor;

  /// How this shape's next geometry pass can take lines from the last pass, its own or the shape's
  /// it replaced; null when it can take none.
  _GeometryReuse? _geometryReuse() {
    if (!_uniformRows) return null;
    final own = _geometryMemo;
    final donor = own != null ? this : _geometryDonor;
    _geometryDonor = null;
    final memo = own ?? donor?._geometryMemo;
    if (donor == null || memo == null) return null;
    if (memo.epoch != _fontEpoch || memo.canvasWidth != canvasSize?.width || !identical(memo.style, style)) {
      return null;
    }
    final changed = _layoutTextsChangedSince(memo.seq);
    if (changed == null) return null;
    final count = _lineStarts.length;
    if (identical(donor, this)) return _GeometryReuse(donor, memo, count, 0, 0, Uint8List(count), changed);
    if (donor._frameSignature != _frameSignature || !donor._uniformRows) return null;
    final (prefix, suffix) = _sameLinesAs(donor);
    final dirty = Uint8List(count);
    for (var l = prefix; l < count - suffix; l++) {
      dirty[l] = 1;
    }
    final reuse = _GeometryReuse(donor, memo, prefix, suffix, count - donor._lineStarts.length, dirty, changed);
    _markRunChanges(reuse);
    _markLinePropertyChanges(reuse);
    return reuse;
  }

  /// How many lines from the start, and from the end, are the same text in [donor] and here.
  (int, int) _sameLinesAs(TextShape donor) {
    if (identical(_lineDonor, donor)) {
      if (_donorPrefix < 0) _matchDonorLines(donor);
      return (_donorPrefix, _donorSuffix);
    }
    if (donor.text == text) return (_lineStarts.length, 0);
    final theirs = _linesOf(donor.text);
    final mine = _linesOf(text);
    final limit = math.min(mine.length, theirs.length);
    var prefix = 0;
    while (prefix < limit && mine[prefix] == theirs[prefix]) {
      prefix++;
    }
    var suffix = 0;
    while (suffix < limit - prefix && mine[mine.length - 1 - suffix] == theirs[theirs.length - 1 - suffix]) {
      suffix++;
    }
    return (prefix, suffix);
  }

  /// Marks the lines whose runs differ from the donor's, in one pass over both: runs before the edit
  /// keep their offsets, runs after it move by what the edit added.
  void _markRunChanges(_GeometryReuse reuse) {
    final donor = reuse.donor;
    final oldRuns = donor.editingState?.characterRanges ?? const <StyleRange>[];
    final newRuns = editingState?.characterRanges ?? const <StyleRange>[];
    if (identical(oldRuns, newRuns) && donor.text == text) return;
    final oldStarts = donor._lineStarts;
    final newStarts = _lineStarts;
    final oldCount = oldStarts.length;
    final newCount = newStarts.length;
    // Old offsets below [head] are on lines before the edit; from [tail] on, on lines after it.
    final head = reuse.prefix < oldCount ? oldStarts[reuse.prefix] : donor.text.length + 1;
    final tail = reuse.suffix > 0 ? oldStarts[oldCount - reuse.suffix] : donor.text.length + 1;
    final shift = reuse.suffix > 0 ? newStarts[newCount - reuse.suffix] - tail : 0;
    void markOld(StyleRange run) {
      final from = _lineAt(oldStarts, run.start);
      final to = _lineAt(oldStarts, math.max(run.start, run.end - 1));
      for (var l = from; l <= to; l++) {
        final mine = reuse.ownLineOf(l, oldCount);
        if (mine >= 0) reuse.dirty[mine] = 1;
      }
    }

    void markNew(StyleRange run) {
      final to = _lineAt(newStarts, math.max(run.start, run.end - 1));
      for (var l = _lineAt(newStarts, run.start); l <= to; l++) {
        reuse.dirty[l] = 1;
      }
    }

    var i = 0, j = 0;
    while (i < oldRuns.length || j < newRuns.length) {
      final o = i < oldRuns.length ? oldRuns[i] : null;
      final n = j < newRuns.length ? newRuns[j] : null;
      if (o != null && o.end <= o.start) {
        i++;
        continue;
      }
      if (n != null && n.end <= n.start) {
        j++;
        continue;
      }
      if (o == null) {
        markNew(n!);
        j++;
        continue;
      }
      // Where the old run is now: one across the edit keeps its start and ends where the text after
      // the edit has moved its end; one ending or starting inside the edit is nowhere.
      final int start, end;
      if (o.end <= head) {
        start = o.start;
        end = o.end;
      } else if (o.start >= tail) {
        start = o.start + shift;
        end = o.end + shift;
      } else if (o.start < head && o.end > tail) {
        start = o.start;
        end = o.end + shift;
      } else {
        markOld(o);
        i++;
        continue;
      }
      if (n == null) {
        markOld(o);
        i++;
        continue;
      }
      if (start == n.start &&
          end == n.end &&
          identical(o.style, n.style) &&
          o.glyphWidth == n.glyphWidth &&
          o.rise == n.rise &&
          o.borderStyle == n.borderStyle) {
        i++;
        j++;
        continue;
      }
      final oldFirst = start <= n.start, newFirst = n.start <= start;
      if (oldFirst) {
        markOld(o);
        i++;
      }
      if (newFirst) {
        markNew(n);
        j++;
      }
    }
  }

  /// Marks the lines whose own properties differ from the donor's in what lays a line out.
  void _markLinePropertyChanges(_GeometryReuse reuse) {
    final donor = reuse.donor;
    final oldProps = donor.editingState?.lineProperties ?? const <int, LineProperties>{};
    final newProps = editingState?.lineProperties ?? const <int, LineProperties>{};
    final oldCount = donor._lineStarts.length;
    bool same(int theirs, int mine) =>
        donor._linePropsFor(theirs) == _linePropsFor(mine) &&
        const ListEquality<int>().equals(donor._tabStopsFor(theirs), _tabStopsFor(mine));
    for (final theirs in oldProps.keys) {
      final mine = reuse.ownLineOf(theirs, oldCount);
      if (mine >= 0 && mine < reuse.dirty.length && !same(theirs, mine)) reuse.dirty[mine] = 1;
    }
    for (final mine in newProps.keys) {
      if (mine < 0 || mine >= reuse.dirty.length) continue;
      final theirs = reuse.donorLineOf(mine);
      if (theirs < 0 || !same(theirs, mine)) reuse.dirty[mine] = 1;
    }
  }

  GeometryTable computeGeometryTable({double? visibleTop, double? visibleBottom}) {
    _forgetStaleMeasurements();
    final lines = _lines;
    final wholeDocument = text.length <= _charXPayloadLimit || visibleTop == null;
    final top = visibleTop ?? 0;
    final bottom = visibleBottom ?? 0;
    bool rowIsVisible(double y, double h) => wholeDocument || (y + h >= top && y <= bottom);
    final visual = <GeometryRow>[];
    double currentY = 0;
    double maxWidth = 0;
    int docOffset = 0;
    final reuse = wholeDocument ? null : _geometryReuse();
    final memo = _GeometryMemo(lines.length, _layoutCacheSeq, _fontEpoch, canvasSize?.width, style);
    final starts = _lineStarts;
    final textLength = text.length;
    final ownLayouts = _measuredLineCache;

    for (int i = 0; i < lines.length; i++) {
      // A line out of view that nothing it is made of has changed for since the last pass is
      // described as that pass described it, without asking anything of the line.
      final theirs = reuse?.donorLineOf(i) ?? -1;
      if (theirs >= 0) {
        final last = reuse!.memo;
        final kind = last.kind[theirs];
        final vIndent = last.vIndent[theirs];
        final length = (i + 1 < starts.length ? starts[i + 1] - 1 : textLength) - starts[i];
        if (kind != _GeometryMemo.notOutOfView &&
            !rowIsVisible(currentY, vIndent + _rowHeight) &&
            reuse.answersAsBefore(this, i, theirs, kind, last.source[theirs], ownLayouts) &&
            !reuse.textChanged(lines[i])) {
          final width = last.width[theirs];
          final x = last.x[theirs];
          maxWidth = math.max(maxWidth, x + width);
          visual.add(GeometryRow(
            i,
            docOffset,
            docOffset + length,
            _roundPx(x),
            _roundPx(currentY),
            _roundPx(width),
            _roundPx(vIndent + _rowHeight),
            vIndent != 0 ? vIndent : null,
            null,
          ));
          memo.remember(i, identical(reuse.donor, this) || kind != _GeometryMemo.ownLayout ? kind : _GeometryMemo.donatedLayout,
              last.source[theirs], width, x, vIndent);
          debugGeometryRowsReused++;
          currentY += vIndent + _rowHeight;
          docOffset += length + 1;
          continue;
        }
      }

      final line = lines[i];

      final props = _linePropsFor(i);
      final vIndent = props.vIndent;
      final maxW = _lineMaxWidth(props.layoutIndent);

      if (!wholeDocument && _uniformRows && !rowIsVisible(currentY, vIndent + _rowHeight)) {
        // Out of view: its width as far as known, without laying the line out for it.
        final peeked = _peekLine(i, line);
        final width = peeked?.width ?? _estimatedWidth(line);
        final x = _lineOriginX(
            props.indent, props.align, width, canvasSize == null ? double.infinity : maxW);
        maxWidth = math.max(maxWidth, x + width);
        visual.add(GeometryRow(
          i,
          docOffset,
          docOffset + line.length,
          _roundPx(x),
          _roundPx(currentY),
          _roundPx(width),
          _roundPx(vIndent + _rowHeight),
          vIndent != 0 ? vIndent : null,
          null,
        ));
        memo.remember(i, _peekedFrom, peeked, width, x, vIndent);
        currentY += vIndent + _rowHeight;
        docOffset += line.length + 1;
        continue;
      }

      // A line off screen that nothing has laid out yet, whose height is known without laying it
      // out, is described by that height and a width estimated from its length, and left unmeasured
      // until it is shown: the native renderer measures a line of fixed height only then, so the
      // document's width is that of the lines measured so far.
      if (!wholeDocument && _knownLine(i, line) == null) {
        final height = _shapelessHeight(i, line);
        if (height != null && !rowIsVisible(currentY, vIndent + _advance(height, i))) {
          final advance = vIndent + _advance(height, i);
          visual.add(GeometryRow(
            i,
            docOffset,
            docOffset + line.length,
            _roundPx(props.indent.toDouble()),
            _roundPx(currentY),
            _roundPx(line.length * _VerticalMetrics.of(style)!.averageAdvance),
            _roundPx(advance),
            vIndent != 0 ? vIndent : null,
            null,
          ));
          currentY += advance;
          docOffset += line.length + 1;
          continue;
        }
      }

      var layout = _measuredLine(i, line, wantCaretX: wholeDocument);
      final lineVisible = rowIsVisible(currentY, vIndent + _advance(layout.height, i));
      // A wrapped line sends per-character x for its rows in view only, so only those are measured:
      // one caret query per character is most of what describing a long line costs.
      final caretsByRow = !wholeDocument &&
          layout.caretX == null &&
          lineVisible &&
          layout.metrics.length > 1 &&
          !_uniformRows;
      // Per-character x is worth its weight only for the rows someone can see; the line has to
      // be laid out to know where it ends, so the decision comes after.
      if (!wholeDocument && layout.caretX == null && lineVisible && !caretsByRow) {
        layout = _measuredLine(i, line, wantCaretX: true);
      }

      final finalX = _lineOriginX(
          props.indent, props.align, layout.width, canvasSize == null ? double.infinity : maxW);

      final caretX = layout.caretX;
      final charX = _LineCharX(caretX, () => _measuringPainter(line, i, align: props.align, maxWidth: maxW));

      // The visual line's box: `y`/`h` include the line's vertical indent so bands
      // stay contiguous; `vi` (first visual row only) is the indent within the box,
      // i.e. the glyphs sit at y + vi. The Java side adds `vi` when answering
      // text-position queries and uses the box for line/pixel queries.
      final metrics = layout.metrics;
      final includeCharX = caretX != null || caretsByRow;
      // Uniform rows hold one line each, whatever rows its layout broke into.
      if (metrics.length <= 1 || _uniformRows) {
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
      charX.dispose();

      currentY += vIndent + _advance(layout.height, i);
      docOffset += line.length + 1;
    }

    if (!wholeDocument) _geometryMemo = memo;
    return GeometryTable(text.length, _roundPx(maxWidth), _roundPx(currentY), visual);
  }

  /// Whether the geometry table is the band around the viewport ([computeBandTable]) rather than
  /// every row: each line is one row of [_rowHeight] with nothing above it, so the rows outside the
  /// band follow from the row height and the line starts, which Java holds.
  bool get bandsGeometry => _rowsOfRowHeight && text.length > _charXPayloadLimit;

  /// The rows of the lines between [visibleTop] and [visibleBottom], with their per-character x, for
  /// an editor that [bandsGeometry]: what Java cannot work out for itself. Costs the band, not the
  /// document, so a change that moves every row (tabs, a font) does too.
  GeometryTable computeBandTable(double visibleTop, double visibleBottom) {
    _forgetStaleMeasurements();
    final lines = _lines;
    final starts = _lineStarts;
    final rowHeight = _rowHeight;
    final first = math.max(0, (visibleTop / rowHeight).floor() - 1);
    final last = math.min(lines.length - 1, (visibleBottom / rowHeight).ceil());
    final rows = <GeometryRow>[];
    double maxWidth = 0;
    // The band's lines are the next ones painted, so they are laid out as the paint keeps them -- while
    // they fit the paint's cache with room to spare: evicting a paragraph the view was drawn with
    // disposes it.
    final paintParagraph = last - first + 1 <= _paintedLineCacheEntries * 3 ~/ 4;
    for (int i = first; i <= last; i++) {
      final line = lines[i];
      final props = _linePropsFor(i);
      final maxW = _lineMaxWidth(props.layoutIndent);
      final layout = _measuredLine(i, line, wantCaretX: true, paintParagraph: paintParagraph);
      final x = _lineOriginX(
          props.indent, props.align, layout.width, canvasSize == null ? double.infinity : maxW);
      final charX = _LineCharX(layout.caretX, () => _measuringPainter(line, i, align: props.align, maxWidth: maxW));
      maxWidth = math.max(maxWidth, x + layout.width);
      rows.add(GeometryRow(i, starts[i], starts[i] + line.length, _roundPx(x), _roundPx(i * rowHeight),
          _roundPx(layout.width), _roundPx(rowHeight), null, charXEntry(charX(0, line.length, x))));
      charX.dispose();
    }
    // Out-of-view lines were not visited, so there is nothing a later full pass could take.
    _geometryMemo = null;
    return GeometryTable(text.length, _roundPx(maxWidth), _roundPx(lines.length * rowHeight), rows,
        bandFirstLine: first, rowHeight: rowHeight);
  }

  /// The layout this shape already holds for [lineIndex], or the one the shape before it measured
  /// for the same line, without laying anything out.
  _LineLayout? _knownLine(int lineIndex, String lineText) {
    final memo = _measuredLineCache ??= List.filled(_lineStarts.length + 1, null);
    if (lineIndex < memo.length && memo[lineIndex] != null) return memo[lineIndex];
    final donated = _donatedLine(lineIndex, lineText, false);
    if (donated != null && lineIndex < memo.length) memo[lineIndex] = donated;
    return donated;
  }

  /// Caret positions asked for to describe the document, for tests asserting their cost.
  @visibleForTesting
  static int debugCaretQueries = 0;

  /// Test seam: every boundary through [_caretXOf], to compare the two ways of finding them.
  @visibleForTesting
  static bool debugCaretXByQuery = false;

  /// x of every character boundary of [lineText] in [tp], relative to the line's own origin.
  ///
  /// Where each character is a grapheme of its own in a left-to-right script, the caret before it
  /// is its box's leading edge: one query per character, where [_caretXOf] makes two.
  List<double> _caretXsOf(TextPainter tp, String lineText, int lineIndex) {
    final length = lineText.length;
    if (debugCaretXByQuery || !_oneBoxPerCharacter(lineText) || _runsOnLine(lineIndex).any((r) => r.glyphWidth != null)) {
      return [for (int k = 0; k <= length; k++) _caretXOf(tp, k)];
    }
    final width = tp.width;
    return [
      for (int k = 0; k < length; k++) _leadingEdgeOf(tp, k, width) ?? _caretXOf(tp, k),
      _caretXOf(tp, length),
    ];
  }

  /// The left edge of character [k]'s box, as [_caretXOf] clamps it; null when it has no box of
  /// its own, as a placeholder of no width.
  static double? _leadingEdgeOf(TextPainter tp, int k, double width) {
    final boxes = tp.getBoxesForSelection(TextSelection(baseOffset: k, extentOffset: k + 1));
    if (boxes.isEmpty || boxes.first.right <= boxes.first.left) return null;
    return clampDouble(boxes.first.left, 0, width);
  }

  /// Whether every code unit of [text] is a tab or a character of a left-to-right script below
  /// the combining marks, each a grapheme of its own.
  static bool _oneBoxPerCharacter(String text) {
    for (int k = 0; k < text.length; k++) {
      final c = text.codeUnitAt(k);
      if (c != 0x09 && (c < 0x20 || c >= 0x0300)) return false;
    }
    return true;
  }

  /// x of the character boundary at [offset] in [tp], relative to the line's own origin.
  static double _caretXOf(TextPainter tp, int offset) {
    debugCaretQueries++;
    return tp.getOffsetForCaret(TextPosition(offset: offset), Rect.zero).dx;
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

  /// Rows a geometry pass took from the pass before without asking anything of their line.
  @visibleForTesting
  static int debugGeometryRowsReused = 0;

  /// Styles whose vertical metrics were measured, a hundred or so layouts each.
  @visibleForTesting
  static int debugStylesMeasured = 0;

  @visibleForTesting
  static int debugLineTopsMicros = 0;

  /// How many lines had their style runs sorted into per-line lists.
  @visibleForTesting
  static int debugLinesBucketed = 0;

  /// The style runs on [lineIndex].
  @visibleForTesting
  List<StyleRange> debugRunsOnLine(int lineIndex) => _runsOnLine(lineIndex);
  @visibleForTesting
  static int debugDrawMicros = 0;

  /// What the style ranges on [lineIndex] amount to, as a number: nothing when the line carries
  /// none, which is the ordinary case and costs a list lookup.
  int _rangesTokenFor(int lineIndex, int lineLength) {
    if (editingState == null || lineIndex < 0) return 0;
    final memo = _rangesTokenCache = _memoHolding(_rangesTokenCache, lineIndex);
    if (memo[lineIndex] != null) return memo[lineIndex]!;
    final donated = _donatedRangesToken(lineIndex, lineLength);
    if (donated != null) {
      memo[lineIndex] = donated;
      return donated;
    }
    final ranges = _runsOnLine(lineIndex);
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
    memo[lineIndex] = token;
    return token;
  }

  /// Per line, as [_memoHolding] grows it.
  List<int?>? _rangesTokenCache;

  /// [lineIndex]'s token as the pre-edit shape worked it out, when the edit did not reach the line
  /// and it carries the same runs: the token is made of nothing else. Every line out of view asks
  /// for its token on each keystroke, so working them all out again was most of describing a
  /// large document.
  int? _donatedRangesToken(int lineIndex, int lineLength) {
    final donor = _lineDonor;
    if (donor == null || !identical(donor.style, style) || donor.editingState == null) return null;
    final tokens = donor._rangesTokenCache;
    if (tokens == null) return null;
    if (_donorPrefix < 0) _matchDonorLines(donor);
    final int theirs;
    if (lineIndex < _donorPrefix) {
      theirs = lineIndex;
    } else if (lineIndex >= _ownLineCount - _donorSuffix) {
      theirs = lineIndex - (_ownLineCount - _donorLineCount);
    } else {
      return null;
    }
    if (theirs < 0 || theirs >= tokens.length) return null;
    final token = tokens[theirs];
    if (token == null || !_sameRunsAs(donor, theirs, lineIndex, lineLength)) return null;
    return token;
  }

  /// What the line is drawn in, as a number; unlike [_rangesTokenFor] it covers colours, which a
  /// paragraph bakes in. Palette entries are named by identity.
  int _paintTokenFor(int lineIndex, int lineLength) {
    if (editingState == null || lineIndex < 0) return 0;
    final memo = _paintTokenCache = _memoHolding(_paintTokenCache, lineIndex);
    if (memo[lineIndex] != null) return memo[lineIndex]!;
    final ranges = _runsOnLine(lineIndex);
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
    memo[lineIndex] = token;
    return token;
  }

  /// Per line, as [_memoHolding] grows it.
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
    TextPainter? laidOut,
    bool paintParagraph = false,
  }) {
    final key = _layoutKeyFor(lineIndex, lineText, align, maxWidth);

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
      // A left-to-right paint paragraph is laid out exactly as the measuring one.
      final shared = direction != TextDirection.ltr
          ? null
          : laidOut ?? (paintParagraph ? _layoutLine(lineText, lineIndex, align: align, maxWidth: maxWidth) : null);
      final tp = shared ?? _measuringPainter(lineText, lineIndex, align: align, maxWidth: maxWidth);

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
      );
      if (wantCaretX) {
        layout.caretX = _caretXsOf(tp, lineText, lineIndex);
      }
      if (shared == null) tp.dispose();
    }
    // Re-inserting on a hit puts the entry back at the young end of the eviction order.
    _cacheLineLayout(key, layout);
    return layout;
  }

  /// [lineText] laid out as the geometry measures it, for the caller to dispose.
  TextPainter _measuringPainter(String lineText, int lineIndex,
      {required TextAlign align, required double maxWidth}) {
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
    return tp;
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
      _layoutKeyFor(lineIndex, lineText, align, maxWidth),
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

  /// Style ranges bucketed by the lines they cover, for runs that do not ascend
  /// ([TextEditingState.runsAscend]).
  List<List<StyleRange>>? _rangesByLineCache;

  /// The last slice [_runsOnLine] answered, which the next line usually shares: a run over
  /// many lines is on each of them.
  List<StyleRange> _lastRuns = const [];
  int _lastRunsFrom = 0;
  int _lastRunsTo = 0;

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

  /// The runs sharing a character with [lineIndex] (its delimiter included). The paint loop and
  /// the geometry table ask line by line; a tab or font change makes a new shape, so working out
  /// every line's runs up front cost the whole document on each change.
  List<StyleRange> _runsOnLine(int lineIndex) {
    final state = editingState;
    final starts = _lineStarts;
    if (state == null || state.characterRanges.isEmpty || lineIndex < 0 || lineIndex >= starts.length) {
      return const [];
    }
    if (!state.runsAscend) return _rangesByLine[lineIndex];
    final runs = state.characterRanges;
    // Ends ascend, so the first run ending past the line's start is found by bisection.
    final lineStart = starts[lineIndex];
    int from = 0, to = runs.length;
    while (from < to) {
      final mid = (from + to) >> 1;
      if (runs[mid].end <= lineStart) {
        from = mid + 1;
      } else {
        to = mid;
      }
    }
    to = from;
    if (lineIndex + 1 < starts.length) {
      final next = starts[lineIndex + 1];
      while (to < runs.length && runs[to].start < next) {
        to++;
      }
    } else {
      to = runs.length;
    }
    if (to == from) return const [];
    if (from != _lastRunsFrom || to != _lastRunsTo) {
      _lastRuns = runs.sublist(from, to);
      _lastRunsFrom = from;
      _lastRunsTo = to;
    }
    return _lastRuns;
  }

  List<List<StyleRange>> get _rangesByLine {
    final cached = _rangesByLineCache;
    if (cached != null) return cached;
    final starts = _lineStarts;
    debugLinesBucketed += starts.length;
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

    final lineRanges = _runsOnLine(lineIndex)
        .where((range) => range.start < lineEndOffset && range.end > lineStartOffset)
        .toList();

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

  /// What each selection covers of line [lineIndex]: the columns of its glyphs, and whether it
  /// goes on past the line's delimiter.
  List<({SelectionInfo selection, int from, int to, bool delimiter})> _selectionsOnLine(
      List<SelectionInfo> selections, int lineIndex, String line) {
    if (selections.isEmpty) return const [];
    final lineStart = lineIndex < _lineStarts.length ? _lineStarts[lineIndex] : 0;
    final lineEnd = lineStart + line.length;
    return [
      for (final selection in selections)
        if (selection.normalizedStart.clamp(0, text.length) <= lineEnd &&
            selection.normalizedEnd.clamp(0, text.length) >= lineStart)
          (
            selection: selection,
            from: math.max(selection.normalizedStart.clamp(0, text.length), lineStart) - lineStart,
            to: math.min(selection.normalizedEnd.clamp(0, text.length), lineEnd) - lineStart,
            delimiter: selection.normalizedEnd.clamp(0, text.length) > lineEnd,
          ),
    ];
  }

  /// TextLayout.draw's order for a selected run: the highlight over whatever the run painted
  /// behind and under its glyphs, then the glyphs again on top in the selection foreground, laid
  /// out and placed as the line's own so they land on the glyphs they replace.
  void _drawSelectedRun(
      Canvas c,
      ({SelectionInfo selection, int from, int to, bool delimiter}) run,
      int lineIndex,
      String line,
      TextPainter tp,
      double maxWidth,
      Offset origin,
      Rect? band) {
    final boxes = tp.getBoxesForSelection(TextSelection(baseOffset: run.from, extentOffset: run.to));
    if (boxes.isEmpty) return;
    final highlight = Paint()..color = run.selection.selectionColor;
    final selected = Path();
    for (final box in boxes) {
      final rect = box.toRect().shift(origin);
      if (band == null || rect.overlaps(band)) debugPaintedSelectionRects.add(rect);
      c.drawRect(rect, highlight);
      selected.addRect(rect);
    }
    final recoloured = TextPainter(
      text: _recolour(tp.text!, run.selection.selectionForeground),
      textAlign: tp.textAlign,
      textDirection: direction,
    );
    if (tp.inlinePlaceholderBoxes != null) {
      recoloured.setPlaceholderDimensions(_placeholderDimensionsOf(tp));
    }
    recoloured.layout(maxWidth: maxWidth);
    c.save();
    c.clipPath(selected);
    _paintLineWithRises(c, recoloured, lineIndex, line, origin);
    c.restore();
    recoloured.dispose();
  }

  /// A line whose delimiter is inside the selection is highlighted past its last character: to
  /// the right edge with FULL_SELECTION, by the delimiter's own extent otherwise. It is the only
  /// highlight an empty line inside a selection gets.
  void _drawSelectedDelimiter(Canvas c, SelectionInfo selection, TextPainter tp, double originX, double top) {
    final tail = originX + tp.width;
    final right = fullSelection
        ? math.max(tail, canvasSize?.width ?? tail)
        : tail + _measureRun(' ', style);
    final rect = Rect.fromLTRB(tail, top, right, top + _advance(tp.height));
    debugPaintedSelectionRects.add(rect);
    c.drawRect(rect, Paint()..color = selection.selectionColor);
  }

  InlineSpan _recolour(InlineSpan span, Color color) {
    if (span is! TextSpan) return span;
    return TextSpan(
      text: span.text,
      // The highlight is the selected run's background: a range's own would cover it again.
      style: (span.style ?? const TextStyle())
          .copyWith(color: color, background: Paint()..color = const Color(0x00000000)),
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

  TextShape copyWithCaret(CaretInfo caretInfo) {
    return TextShape(
      text,
      off,
      style,
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
      fixedLineHeight,
    )
      .._lineTopsCache = _lineTopsCache
      .._takeRowHeight(this);
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

  @override
  String toString() =>
      'EditableText "${text.length > 20 ? "${text.substring(0, 20)}..." : text}" @ $off${caretInfo != null ? " [caret at ${caretInfo!.offset}]" : ""}${editable ? " [editable]" : ""}';
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
  /// The highlight behind the selected run. The default is the Display's COLOR_LIST_SELECTION,
  /// what StyledText#getSelectionBackground answers while none is set.
  final Color selectionColor;
  /// The colour the selected glyphs are drawn in over the highlight. The default is the Display's
  /// COLOR_LIST_SELECTION_TEXT, what StyledText#getSelectionForeground answers while none is set.
  final Color selectionForeground;
  final bool isActive;

  SelectionInfo({
    required this.start,
    required this.end,
    this.selectionColor = const Color(0xFF3399FF),
    this.selectionForeground = const Color(0xFFFFFFFF),
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

  /// Whether no run is empty and the runs ascend by start and by end, as the widget's own are
  /// (sorted, none inside another): a line's runs are then one slice of the list.
  bool get runsAscend => _runsAscend ??= _checkRunsAscend();
  bool? _runsAscend;

  bool _checkRunsAscend() {
    StyleRange? previous;
    for (final run in characterRanges) {
      if (run.end <= run.start) return false;
      if (previous != null && (run.start < previous.start || run.end <= previous.end)) return false;
      previous = run;
    }
    return true;
  }

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


/// The layout table Java answers StyledText's position queries from, one [GeometryRow] per visual
/// row; kept as objects so only the rows that changed are sent.
@visibleForTesting
class GeometryTable {
  GeometryTable(this.charCount, this.contentWidth, this.contentHeight, this.rows,
      {this.bandFirstLine, this.rowHeight});

  int charCount;
  final double contentWidth;
  final double contentHeight;
  final List<GeometryRow> rows;

  /// For a band ([TextShape.computeBandTable]), the line of its first row; null when [rows] are
  /// every row of the document.
  final int? bandFirstLine;

  /// For a band, the height every line has: line `i` is the row at `i * rowHeight`, both rounded to
  /// a hundredth as the rows are.
  final double? rowHeight;

  /// Whether [rows] and [other]'s rows are the same lines, so one table is a splice of the other.
  bool holdsTheRowsOf(GeometryTable other) =>
      bandFirstLine == other.bandFirstLine && rowHeight == other.rowHeight;

  bool sameExtentsAs(GeometryTable other) =>
      charCount == other.charCount &&
      contentWidth == other.contentWidth &&
      contentHeight == other.contentHeight &&
      holdsTheRowsOf(other);

  Map<String, dynamic> toMap() => {
        'charCount': charCount,
        'contentWidth': contentWidth,
        'contentHeight': contentHeight,
        if (bandFirstLine != null) 'bandFirstLine': bandFirstLine,
        if (rowHeight != null) 'rowHeight': rowHeight,
        'lines': [for (final row in rows) row.toMap()],
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

  Map<String, dynamic> toMap() => {
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

/// Writes rows each against the one written before it, as Java's `StyledTextHelper.readRow` reads
/// them: lines and offsets as steps between rows, and pixels as exact hundredths, the precision the
/// table is rounded to. A row's top is a step from the bottom of the row before and its height one
/// from that row's height, so in an editor whose lines are one height both are zero. A pixel value
/// that is not a whole hundredth goes as the double it is.
class GeometryRowWriter {
  GeometryRowWriter(this._out);

  final WidgetWireWriter _out;
  int _line = 0;
  int _end = 0;
  int? _x;
  int? _bottom;
  int? _height;

  /// `l s e x y w h vi`, then the character positions: `u8 0` none, `1` each one, `2` origin and
  /// advance.
  void write(GeometryRow row) {
    final out = _out;
    out
      ..int64(row.l - _line)
      ..int64(row.s - _end)
      ..int64(row.e - row.s);
    _line = row.l;
    _end = row.e;
    final x = _px(row.x, _x);
    final y = _px(row.y, _bottom);
    _px(row.w, null);
    final h = _px(row.h, _height);
    _x = x;
    _height = h;
    _bottom = y == null || h == null ? null : y + h;
    out.int32(row.vi ?? 0);
    final charX = row.charX;
    final cx = charX?['cx'];
    final cxu = charX?['cxu'];
    if (cx is List) {
      out
        ..u8(1)
        ..varint(cx.length);
      var previous = x;
      for (final v in cx) {
        previous = _px((v as num).toDouble(), previous);
      }
    } else if (cxu is List && cxu.length == 2) {
      out
        ..u8(2)
        ..float64((cxu[0] as num).toDouble())
        ..float64((cxu[1] as num).toDouble());
    } else {
      out.u8(0);
    }
  }

  /// Writes [v] as a step from [from] hundredths (from 0 when null), or as a double when it is not a
  /// whole hundredth; returns its hundredths, or null for a double.
  int? _px(double v, int? from) {
    final n = _hundredths(v);
    if (n == null) {
      _out
        ..varint(1)
        ..float64(v);
      return null;
    }
    final step = n - (from ?? 0);
    // Even: a zigzagged step. Arithmetic, as a bitwise result is unsigned on the web.
    _out.varint((step >= 0 ? step * 2 : -step * 2 - 1) * 2);
    return n;
  }

  static int? _hundredths(double v) {
    if (!v.isFinite || v.abs() > 1e12) return null;
    final n = (v * 100).round();
    return n / 100 == v ? n : null;
  }
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

/// Drops the ranges another range already covers, from a list sorted by start; the sort makes
/// the furthest end so far enough to decide.
@visibleForTesting
List<StyleRange> dedupeSortedRanges(List<StyleRange> sorted) {
  // Most documents have no run inside another: the list is kept as it is.
  var reach = -1;
  var nested = false;
  for (final range in sorted) {
    if (reach >= range.end) {
      nested = true;
      break;
    }
    if (range.end > reach) reach = range.end;
  }
  if (!nested) return sorted;
  final kept = <StyleRange>[];
  reach = -1;
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

/// What a geometry pass worked out for each line it described out of view, and where the width
/// came from, for the next pass to take over where nothing it was worked out from has changed.
class _GeometryMemo {
  _GeometryMemo(int lines, this.seq, this.epoch, this.canvasWidth, this.style)
      : width = Float64List(lines),
        x = Float64List(lines),
        vIndent = Int32List(lines),
        kind = Uint8List(lines),
        source = List<Object?>.filled(lines, null);

  /// Kinds of width: the line was not described out of view; this shape's own layout of it; the
  /// replaced shape's; a layout cached for its text; its length, nothing being cached for it.
  static const int notOutOfView = 0, ownLayout = 1, donatedLayout = 2, cachedLayout = 3, estimated = 4;

  /// [_layoutCacheSeq] when the pass began, [_fontEpoch], and what every line's width depends on
  /// for the whole document.
  final int seq;
  final int epoch;
  final double? canvasWidth;
  final TextStyle style;

  final Float64List width;
  final Float64List x;
  final Int32List vIndent;
  final Uint8List kind;
  final List<Object?> source;

  void remember(int line, int kind, Object? source, double width, double x, int vIndent) {
    if (line >= this.kind.length) return;
    this.kind[line] = kind;
    this.source[line] = source;
    this.width[line] = width;
    this.x[line] = x;
    this.vIndent[line] = vIndent;
  }
}

/// How a geometry pass lines up with the last one: the lines both have, and those since changed.
class _GeometryReuse {
  _GeometryReuse(this.donor, this.memo, this.prefix, this.suffix, this.lineShift, this.dirty, this.changedTexts);

  final TextShape donor;
  final _GeometryMemo memo;

  /// Lines the same text at the start and the end of both, and how many lines the edit added.
  final int prefix, suffix, lineShift;

  /// Per line here, 1 where its text, runs or properties differ from the donor's line.
  final Uint8List dirty;

  /// The texts whose cached layouts came or went since the last pass, up to change [_seenUntil].
  final _ChangedTexts changedTexts;
  int _seenUntil = _layoutCacheSeq;
  bool _lost = false;

  /// Whether a cached layout for a text like [line]'s came or went since the last pass, this one
  /// included: laying out a line in view caches what a line further down may look up.
  bool textChanged(String line) {
    final now = _layoutCacheSeq;
    if (now != _seenUntil) {
      // The log dropped changes this pass has not seen: any text may have changed.
      if (_seenUntil < _layoutCacheLogStart) _lost = true;
      for (var k = math.max(0, _seenUntil - _layoutCacheLogStart); k < _layoutCacheLog.length; k++) {
        changedTexts.add(_layoutCacheLog[k]);
      }
      _seenUntil = now;
    }
    return _lost || changedTexts.contains(line);
  }

  /// The donor's line [mine] is, unchanged; -1 when there is none.
  int donorLineOf(int mine) {
    if (mine >= dirty.length || dirty[mine] != 0) return -1;
    if (mine < prefix) return mine;
    if (mine >= dirty.length - suffix) return mine - lineShift;
    return -1;
  }

  /// The line here the donor's line [theirs] is, of [donorCount]; -1 when the edit reached it.
  int ownLineOf(int theirs, int donorCount) {
    if (theirs < prefix) return theirs;
    if (theirs >= donorCount - suffix) return theirs + lineShift;
    return -1;
  }

  /// Whether [shape]'s look-up of [mine] finds what the donor's found for [theirs], given as [kind]
  /// and [source]: the donor's own layout, still there, which [shape] takes over; or the cache's
  /// answer, when neither shape has laid the line out since.
  bool answersAsBefore(TextShape shape, int mine, int theirs, int kind, Object? source, List<_LineLayout?>? ownLayouts) {
    final theirLayouts = donor._measuredLineCache;
    final theirLayout = theirLayouts != null && theirs < theirLayouts.length ? theirLayouts[theirs] : null;
    final myLayout = ownLayouts != null && mine < ownLayouts.length ? ownLayouts[mine] : null;
    switch (kind) {
      case _GeometryMemo.ownLayout:
        if (!identical(theirLayout, source)) return false;
        // Shared measurements, or this shape's own still empty and the donor answering for it.
        return identical(ownLayouts, theirLayouts) || (myLayout == null && identical(shape._lineDonor, donor));
      case _GeometryMemo.cachedLayout:
      case _GeometryMemo.estimated:
        return myLayout == null && theirLayout == null;
      default:
        return false;
    }
  }
}
