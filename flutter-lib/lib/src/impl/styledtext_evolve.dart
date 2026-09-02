import 'dart:async';
import 'dart:convert';
import 'dart:math' as math;
import 'dart:ui' show LineMetrics;
import 'package:flutter/foundation.dart';
import 'package:flutter/gestures.dart';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:flutter/widgets.dart';
import '../comm/comm.dart';
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
import 'utils/double_tap_detector.dart';
import 'utils/font_utils.dart';
import 'utils/text_utils.dart';
import 'utils/widget_utils.dart';
import 'utils/pending_text_echoes.dart';
import 'utils/verify_key_gate.dart';
import 'color_utils.dart';
import '../theme/theme_extensions/scrolledcomposite_theme_extension.dart';
import '../theme/theme_extensions/styledtext_theme_extension.dart';

class StyledTextImpl<T extends StyledTextSwt, V extends VStyledText>
    extends CanvasImpl<T, V> with PendingTextEchoes {
  // The RawKeyboardListener below already sends Key/VerifyKey per keystroke.
  @override
  bool get forwardsKeysFromWrap => false;

  // Flutter's Draggable fires on any pointer jitter, turning ordinary clicks into spurious drag/drop.
  @override
  bool get wrapsWholeWidgetForDnd => false;

  List<Shape> shapes = [];
  TextShape? _editableTextShape;
  bool _isEditingText = false;
  Timer? _caretBlinkTimer;

  TextEditingState? _localEditingState;
  TextShape? _originalServerTextShape;
  bool _isInLocalEditMode = false;
  bool _hasProgrammaticSelection = false;

  final VerifyKeyGate _verifyKeyGate = VerifyKeyGate();
  Object? _verifyKeyVetoableToken;
  Object? _verifyKeyVerdictToken;

  String get _verifyKeyVetoableChannel =>
      '${state.swt}/${state.id}/verifyKey/vetoable';
  String get _verifyKeyVerdictChannel =>
      '${state.swt}/${state.id}/verifyKey/verdict';

  bool _isSelecting = false;
  int? _selectionStartOffset;
  Offset? _doubleTapPosition;
  Offset? _tripleTapPosition;
  final _tapDetector = DoubleTapDetector(slop: 20.0, timeout: const Duration(milliseconds: 500));
  int _lastTapCount = 1;

  bool _editable = true;
  bool _wordWrap = true;

  ScrollController _verticalController = ScrollController();
  ScrollController _horizontalController = ScrollController();
  int _lastSentVerticalOffset = 0;
  int _lastSentHorizontalOffset = 0;
  final Set<int> _pendingVerticalScrollValues = {};
  final Set<int> _pendingHorizontalScrollValues = {};

  StyledTextThemeExtension get _styledTextTheme =>
      Theme.of(context).extension<StyledTextThemeExtension>()!;

  final FocusNode _focusNode = FocusNode();

  /// Held open while the editor is in edit mode so the platform has somewhere to compose; the
  /// raw key stream alone never carries a composed character.
  late final ComposedTextInput _composedInput =
      ComposedTextInput(onComposedText: _insertComposedText);

  @override
  Color get bg =>
      ParentBackgroundScope.backgroundOf(context) ??
      getBackgroundColor(
        background: state.background,
        defaultColor: _styledTextTheme.backgroundColor,
      ) ??
      _styledTextTheme.backgroundColor;

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
    _verticalController = ScrollController();
    _horizontalController = ScrollController();
    _verticalController.addListener(_onVerticalScroll);
    _horizontalController.addListener(_onHorizontalScroll);
    _focusNode.addListener(_syncEditorKeyOwnership);
    _focusNode.onKeyEvent = _keepTabInsideEditor;
    EquoCommService.onRaw("${state.swt}/${state.id}/focusLost", (_) {
      if (_isEditingText) {
        _stopEditing();
      }
    });
    _verifyKeyVetoableToken = EquoCommService.onRaw(
      _verifyKeyVetoableChannel,
      (args) => _verifyKeyGate.armed = _boolArg(args, 'value') ?? false,
    );
    _verifyKeyVerdictToken = EquoCommService.onRaw(
      _verifyKeyVerdictChannel,
      (args) => _verifyKeyGate.verdict(
        _boolArg(args, 'doit') ?? true,
        _intArg(args, 'keyCode'),
      ),
    );
  }

  bool? _boolArg(dynamic args, String key) {
    final decoded = args is String ? jsonDecode(args) : args;
    return decoded is Map ? decoded[key] as bool? : null;
  }

  int? _intArg(dynamic args, String key) {
    final decoded = args is String ? jsonDecode(args) : args;
    final value = decoded is Map ? decoded[key] : null;
    return value is int ? value : null;
  }

  /// A multi-line editable StyledText keeps Tab as content instead of traversing away from it
  /// (SWT's `StyledText.handleTraverse`), and Java relies on that: it applies the tab itself,
  /// because the Flutter editor skips control characters. Flutter's focus traversal has no such
  /// rule, so unless the key is marked handled here it moves focus off the editor and every
  /// later keystroke is stranded until the user clicks back in. Marking it handled stops only
  /// the traversal — [_handleKeyEvent] listens on the raw keyboard stream and still forwards the
  /// Tab to Java.
  KeyEventResult _keepTabInsideEditor(FocusNode node, KeyEvent event) {
    if (event is KeyUpEvent) return KeyEventResult.ignored;
    if (event.logicalKey != LogicalKeyboardKey.tab) return KeyEventResult.ignored;
    if (!_editable || hasStyle(state.style, SWT.SINGLE)) return KeyEventResult.ignored;
    final keys = HardwareKeyboard.instance;
    if (keys.isShiftPressed ||
        keys.isControlPressed ||
        keys.isAltPressed ||
        keys.isMetaPressed) {
      return KeyEventResult.ignored;
    }
    return KeyEventResult.handled;
  }

  /// While this StyledText holds focus, tell the top-level key handler to stay out of the way — it
  /// forwards its own key events (see [_handleKeyEvent]) and must not be raced by a parallel forward.
  void _syncEditorKeyOwnership() {
    setEditorKeyOwnership(this, _focusNode.hasFocus);
  }

  @override
  void dispose() {
    _verticalController.dispose();
    _horizontalController.dispose();
    _composedInput.detach();
    setEditorKeyOwnership(this, false);
    _focusNode.removeListener(_syncEditorKeyOwnership);
    _focusNode.dispose();
    _caretBlinkTimer?.cancel();
    EquoCommService.remove(_verifyKeyVetoableChannel, _verifyKeyVetoableToken);
    EquoCommService.remove(_verifyKeyVerdictChannel, _verifyKeyVerdictToken);
    _verifyKeyGate.dispose();
    super.dispose();
  }

  @override
  void extraSetState() {
    super.extraSetState();
    _editable = state.editable ?? false;
    _wordWrap = state.wordWrap ?? false; // SWT default is no wrap; wrap only when explicitly set

    final newTopPixel = state.topPixel ?? 0;
    if (!_pendingVerticalScrollValues.remove(newTopPixel) &&
        newTopPixel != _lastSentVerticalOffset) {
      _lastSentVerticalOffset = newTopPixel;
      WidgetsBinding.instance.addPostFrameCallback((_) {
        if (_verticalController.hasClients) {
          final maxScroll = _verticalController.position.maxScrollExtent;
          _verticalController.jumpTo(newTopPixel.toDouble().clamp(0.0, maxScroll));
        }
      });
    }

    final newHorizPixel = state.horizontalPixel ?? 0;
    if (!_pendingHorizontalScrollValues.remove(newHorizPixel) &&
        newHorizPixel != _lastSentHorizontalOffset) {
      _lastSentHorizontalOffset = newHorizPixel;
      WidgetsBinding.instance.addPostFrameCallback((_) {
        if (_horizontalController.hasClients) {
          final maxScroll = _horizontalController.position.maxScrollExtent;
          _horizontalController.jumpTo(newHorizPixel.toDouble().clamp(0.0, maxScroll));
        }
      });
    }

    _buildTextShapeFromState();
  }

  /// Build TextShape from serialized state data
  void _buildTextShapeFromState() {
    final originalText = state.text ?? '';
    final text = originalText.replaceAll('\r\n', '\n').replaceAll('\r', '\n');

    // While the user types, edits are applied optimistically on the client and only sent to
    // Java as async Modify events, so any full-state snapshot Java pushes mid-edit (e.g. from
    // a ControlHelper.paint-driven dirty()) can still carry older text. Re-basing onto it
    // (the `_enterLocalEditMode(textShape)` path below) would drop the just-typed characters
    // whenever such a push interleaves with typing. If the incoming text is a stale echo of
    // our own in-flight edit (see PendingTextEchoes), keep the local buffer authoritative and
    // re-sync `state` to it; a value we never sent is a genuine external setText and falls
    // through to re-base (matching SWT, which swaps the content even mid-edit).
    if (_isEditingText &&
        _isInLocalEditMode &&
        _editableTextShape != null &&
        _editableTextShape!.styledTextId == state.id &&
        isStaleTextEcho(text, _editableTextShape!.text)) {
      state.text = _editableTextShape!.text;
      state.caretOffset =
          _editableTextShape!.caretInfo?.offset ?? state.caretOffset;
      return;
    }

    final styledTextId = state.id;
    final caretOffset = _adjustOffsetForNormalizedText(
      originalText,
      state.caretOffset ?? 0,
    );

    // Build editing state from renderer style ranges
    final editingState = _buildEditingStateFromRenderer();

    // Get default text style
    final defaultStyle = _getDefaultTextStyle();

    // Build caret info
    final caretColor = applyAlpha(_styledTextTheme.foregroundColor);
    final caretHeight = (defaultStyle.fontSize ?? 12.0) * 1.2;
    final caretInfo = CaretInfo(
      offset: caretOffset,
      width: 1.0,
      height: caretHeight,
      color: caretColor,
      visible: _isEditingText,
      blinking: _isEditingText,
      styledTextId: styledTextId,
      blinkRate: 560,
    );

    SelectionInfo? selectionFromState;
    final sr = state.selectionRange;
    if (sr != null && sr.x != sr.y) {
      selectionFromState = SelectionInfo.fromRange(sr.x, sr.y);
    }

    final topMargin = (state.topMargin ?? 0).toDouble();
    final leftMargin = (state.leftMargin ?? 0).toDouble();
    final javaAscent = state.renderer?.ascent ?? 0;
    final javaDescent = state.renderer?.descent ?? 0;
    final javaLineHeight = (javaAscent + javaDescent).toDouble();

    final textShape = TextShape(
      text,
      Offset(leftMargin, topMargin),
      defaultStyle,
      clipRect,
      null,
      caretInfo,
      _wordWrap,
      getBounds(),
      _editable,
      styledTextId,
      _notifyTextChanged,
      editingState,
      selectionFromState,
      javaLineHeight,
      state.tabs ?? 4,
    );

    // Update shapes list - remove old shape with same id and add new one
    shapes = shapes.where((shape) {
      return !(shape is TextShape && shape.styledTextId == styledTextId);
    }).toList();

    // Update editable text shape if we're editing this one, or add to shapes list
    if (_isEditingText && _editableTextShape?.styledTextId == styledTextId) {
      if (!_isInLocalEditMode) {
        _editableTextShape = textShape;
        _hasProgrammaticSelection = false;
      } else if (_editableTextShape!.text != text) {
        _enterLocalEditMode(textShape);
        _hasProgrammaticSelection = false;
      } else {
        // Text already matches what we echoed locally, but the push still carries everything
        // else Java may have changed meanwhile: fresh StyleRanges from an async recolor (e.g.
        // JDT's reconciler), and the presentation the layout is measured against — wrap state,
        // bounds, margins, font metrics, tab width. Re-base the painted shape on this snapshot
        // instead of keeping the one taken when the edit session opened; only the caret and
        // selection stay local, because Java's copy of those lags the keystrokes we predicted.
        _localEditingState = editingState;
        _originalServerTextShape = textShape;
        _updateLocalTextShape(
          caret: _editableTextShape!.caretInfo,
          selection: _editableTextShape!.selectionInfo,
        );
        if (selectionFromState != null) {
          _editableTextShape = _editableTextShape!.copyWithSelection(selectionFromState);
          _hasProgrammaticSelection = true;
        } else if (_hasProgrammaticSelection) {
          // Java cleared the Find/Replace highlight (selectionFromState == null but we had one).
          _editableTextShape = _editableTextShape!.clearSelection();
          _hasProgrammaticSelection = false;
        }
      }
    } else {
      shapes = [...shapes, textShape];
    }
  }

  /// Adjust offset from original text (with \r\n) to normalized text (with \n only)
  /// Counts how many \r characters appear before the given offset and subtracts them
  int _adjustOffsetForNormalizedText(String originalText, int offset) {
    if (offset <= 0) return 0;
    int adjustment = 0;
    final maxIndex = offset < originalText.length
        ? offset
        : originalText.length;
    for (int i = 0; i < maxIndex; i++) {
      if (originalText[i] == '\r' &&
          i + 1 < originalText.length &&
          originalText[i + 1] == '\n') {
        adjustment++;
      }
    }
    return offset - adjustment;
  }

  /// Build TextEditingState from renderer's styleRanges and lineProperties
  TextEditingState _buildEditingStateFromRenderer() {
    final renderer = state.renderer;
    final originalText = state.text ?? '';
    final normalizedText = originalText
        .replaceAll('\r\n', '\n')
        .replaceAll('\r', '\n');

    List<StyleRange> characterRanges = [];

    if (renderer?.styles != null && renderer!.styles!.isNotEmpty) {
      final styles = renderer.styles!;
      final ranges = renderer.ranges;
      final count = renderer.styleCount ?? styles.length;

      for (int i = 0; i < count && i < styles.length; i++) {
        final vRange = styles[i];
        final style = _convertVStyleRangeToTextStyle(vRange);

        int start, length;
        if (ranges != null && (i * 2) + 1 < ranges.length) {
          start = ranges[i * 2];
          length = ranges[i * 2 + 1];
        } else {
          start = vRange.start;
          length = vRange.length;
        }

        final adjustedStart = _adjustOffsetForNormalizedText(
          originalText,
          start,
        );
        final adjustedEnd = _adjustOffsetForNormalizedText(
          originalText,
          start + length,
        );
        characterRanges.add(
          StyleRange(start: adjustedStart, end: adjustedEnd, style: style),
        );
      }
    } else if (normalizedText.isNotEmpty) {
      // Default range for entire text
      final defaultStyle = _getDefaultTextStyle();
      characterRanges.add(
        StyleRange(start: 0, end: normalizedText.length, style: defaultStyle),
      );
    }

    // Sort ranges by start position
    characterRanges.sort((a, b) => a.start.compareTo(b.start));

    if (characterRanges.isNotEmpty) {
      List<StyleRange> dedupedRanges = [];
      for (final range in characterRanges) {
        bool isDuplicate = dedupedRanges.any(
          (existing) =>
              existing.start <= range.start && existing.end >= range.end,
        );
        if (!isDuplicate) {
          dedupedRanges.add(range);
        }
      }
      characterRanges = dedupedRanges;
    }

    // Build line properties from renderer - array index corresponds to line index
    // (nulls are preserved in the array to maintain position mapping)
    Map<int, LineProperties> lineProps = {};
    if (renderer?.lines != null && renderer!.lines!.isNotEmpty) {
      for (int i = 0; i < renderer.lines!.length; i++) {
        final vLineInfo = renderer.lines![i];
        if (vLineInfo != null) {
          lineProps[i] = LineProperties(
            alignment: vLineInfo.alignment,
            indent: vLineInfo.indent,
            justify: vLineInfo.justify,
            verticalIndent: vLineInfo.verticalIndent,
          );
        }
      }
    }

    return TextEditingState(
      characterRanges: characterRanges,
      lineProperties: lineProps,
    );
  }

  /// Convert VStyleRange to Flutter TextStyle
  TextStyle _convertVStyleRangeToTextStyle(VStyleRange vRange) {
    final defaultTextColor = applyAlpha(_styledTextTheme.foregroundColor);
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

    // Get font info from VStyleRange.font if available, otherwise use renderer's font
    double? fontSize = baseStyle.fontSize;
    String? fontFamily = baseStyle.fontFamily;
    if (vRange.font?.fontData != null && vRange.font!.fontData!.isNotEmpty) {
      final fontData = vRange.font!.fontData!.first;
      fontSize = fontData.height?.toDouble() ?? fontSize;
      fontFamily = fontData.name;
    }

    // Build decoration
    TextDecoration? decoration;
    List<TextDecoration> decorations = [];
    if (vRange.underline == true) {
      decorations.add(TextDecoration.underline);
    }
    if (vRange.strikeout == true) {
      decorations.add(TextDecoration.lineThrough);
    }
    if (decorations.isNotEmpty) {
      decoration = TextDecoration.combine(decorations);
    }

    return TextStyle(
      fontSize: fontSize,
      fontFamily: fontFamily,
      fontWeight: fontWeight,
      fontStyle: fontStyle,
      color: foreground ?? defaultTextColor,
      backgroundColor: background,
      decoration: decoration,
      decorationColor: foreground ?? defaultTextColor,
    );
  }

  /// Get default text style from state.font or fallback
  TextStyle _getDefaultTextStyle() {
    final defaultTextColor = applyAlpha(_styledTextTheme.foregroundColor);

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
    beforePaintRequest();
    final bounds = getBounds();
    final hasHScroll = hasStyle(state.style, SWT.H_SCROLL);
    final hasVScroll = hasStyle(state.style, SWT.V_SCROLL);
    final contentSize = _computeContentSize();
    final alwaysShow = state.alwaysShowScrollBars ?? false;

    Widget contentLayer = SizedBox(
      width: contentSize.width,
      height: contentSize.height,
      child: CustomPaint(
        painter: ScenePainter(
          bg,
          List.unmodifiable([
            ...shapes,
            if (_editableTextShape != null) _editableTextShape!,
          ]),
        ),
      ),
    );

    if (hasHScroll) {
      contentLayer = SingleChildScrollView(
        scrollDirection: Axis.horizontal,
        controller: _horizontalController,
        child: contentLayer,
      );
    }
    if (hasVScroll) {
      contentLayer = SingleChildScrollView(
        controller: _verticalController,
        child: contentLayer,
      );
    }
    if (hasVScroll || hasHScroll) {
      contentLayer = ScrollConfiguration(
        behavior: ScrollConfiguration.of(context).copyWith(
          dragDevices: const <PointerDeviceKind>{},
        ),
        child: contentLayer,
      );
    }

    if (hasHScroll) {
      contentLayer = Scrollbar(
        controller: _horizontalController,
        thumbVisibility: alwaysShow,
        notificationPredicate: hasVScroll ? (n) => n.depth == 1 : null,
        child: contentLayer,
      );
    }
    if (hasVScroll) {
      contentLayer = Scrollbar(
        controller: _verticalController,
        thumbVisibility: alwaysShow,
        child: contentLayer,
      );
    }

    final trackSize = Theme.of(context)
        .extension<ScrolledCompositeThemeExtension>()!
        .scrollbarThickness;
    final interactionLayer = RawKeyboardListener(
      focusNode: _focusNode,
      autofocus: _isEditingText,
      onKey: _handleKeyEvent,
      child: Padding(
        padding: EdgeInsets.only(
          right: hasVScroll ? trackSize : 0,
          bottom: hasHScroll ? trackSize : 0,
        ),
        child: Container(),
      ),
    );

    return wrap(
      Listener(
        onPointerSignal: _handlePointerSignal,
        onPointerDown: (e) => _handlePointerDown(e.localPosition, e.buttons),
        onPointerMove: (e) => _handlePointerMove(e.localPosition),
        onPointerUp: (e) => _handlePointerUp(e.localPosition),
        child: SizedBox(
          width: bounds.width,
          height: bounds.height,
          child: Stack(
            children: [
              Positioned.fill(child: contentLayer),
              Positioned.fill(child: interactionLayer),
            ],
          ),
        ),
      ),
    );
  }

  void _onVerticalScroll() {
    final offset = _verticalController.offset.round();
    if (offset == _lastSentVerticalOffset) return;
    _lastSentVerticalOffset = offset;
    _pendingVerticalScrollValues.add(offset);
    _sendScrollUpdate();
  }

  void _onHorizontalScroll() {
    final offset = _horizontalController.offset.round();
    if (offset == _lastSentHorizontalOffset) return;
    _lastSentHorizontalOffset = offset;
    _pendingHorizontalScrollValues.add(offset);
    _sendScrollUpdate();
  }

  void _sendScrollUpdate() {
    final scrollState = VStyledText()
      ..id = state.id
      ..swt = state.swt
      ..topPixel = _lastSentVerticalOffset
      ..horizontalPixel = _lastSentHorizontalOffset;
    EquoCommService.sendPayload(
      "${state.swt}/${state.id}/StateUpdate",
      scrollState,
    );
  }

  void _handlePointerSignal(PointerSignalEvent event) {
    if (event is PointerScrollEvent) {
      if (_verticalController.hasClients && event.scrollDelta.dy != 0) {
        final newOffset = (_verticalController.offset + event.scrollDelta.dy)
            .clamp(0.0, _verticalController.position.maxScrollExtent);
        _verticalController.jumpTo(newOffset);
      }
      if (_horizontalController.hasClients && event.scrollDelta.dx != 0) {
        final newOffset = (_horizontalController.offset + event.scrollDelta.dx)
            .clamp(0.0, _horizontalController.position.maxScrollExtent);
        _horizontalController.jumpTo(newOffset);
      }
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
    final lineCount = '\n'.allMatches(text).length + 1;
    final totalHeight =
        math.max(getBounds().height, lineCount * lineHeight);

    double totalWidth = getBounds().width;
    if (_wordWrap != true) {
      int maxLen = 0;
      int start = 0;
      for (int i = 0; i <= text.length; i++) {
        if (i == text.length || text[i] == '\n') {
          final len = i - start;
          if (len > maxLen) maxLen = len;
          start = i + 1;
        }
      }
      final charWidth = fontSize * 0.6;
      totalWidth = math.max(getBounds().width, maxLen * charWidth + 20);
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
  // CanvasImpl.build; the only request left is initState's, which runs before the
  // first state push has built a text shape. build() therefore drives the hook too,
  // so a layout that changes after mount is still described.
  //
  // Gate on the shape's layout holder, not the shape reference: caret-blink and
  // selection copies share the holder (their geometry is unchanged), while any copy
  // that changes a layout input carries a fresh one. The payload is one layout per
  // line plus a per-character x array for the document, so an ungated push per frame
  // would serialize the whole document on every caret blink.
  _LineTopsCache? _geometrySentForLayout;

  /// Test seam: the transport is a no-op in a widget test, so a push is otherwise
  /// unobservable. Counts every payload pushed and keeps the last one.
  @visibleForTesting
  static int debugGeometryPushes = 0;
  @visibleForTesting
  static Map<String, dynamic>? debugLastGeometry;

  @override
  void beforePaintRequest() {
    final shape = _currentTextShape();
    if (shape == null ||
        identical(shape._lineTopsCache, _geometrySentForLayout)) {
      return;
    }
    final geometry = shape.computeGeometry();
    if (geometry == null) return;
    _geometrySentForLayout = shape._lineTopsCache;
    debugGeometryPushes++;
    debugLastGeometry = geometry;
    EquoCommService.sendPayload(
      "${state.swt}/${state.id}/TextGeometry",
      geometry,
    );
  }

  TextShape? _currentTextShape() {
    if (_editableTextShape != null &&
        _editableTextShape!.styledTextId == state.id) {
      return _editableTextShape;
    }
    for (int i = shapes.length - 1; i >= 0; i--) {
      final s = shapes[i];
      if (s is TextShape && s.styledTextId == state.id) return s;
    }
    return null;
  }

  /// Convert a viewport-space position to content-space by adding scroll offsets.
  Offset _toContentPosition(Offset viewportPosition) {
    final scrollX =
        _horizontalController.hasClients ? _horizontalController.offset : 0.0;
    final scrollY =
        _verticalController.hasClients ? _verticalController.offset : 0.0;
    return viewportPosition + Offset(scrollX, scrollY);
  }

  //-----------Events to Java-----------------

  void _sendSelectionChange() {
    if (_editableTextShape?.selectionInfo != null &&
        _editableTextShape!.selectionInfo!.hasSelection) {
      final sel = _editableTextShape!.selectionInfo!;
      final event = VEvent()
        ..start = sel.normalizedStart
        ..end = sel.normalizedEnd
        ..text = _editableTextShape!.getSelectedText();
      widget.sendSelectionSelection(state, event);
    }
  }

  void _sendSelectionCleared(int caretOffset) {
    final event = VEvent()
      ..start = caretOffset
      ..end = caretOffset
      ..text = '';
    widget.sendSelectionSelection(state, event);
  }

  void _notifyTextChanged(
    String newText,
    int caretPos,
    int rangeStart,
    int rangeEnd,
    String insertedText,
  ) {
    // Clear the GC overlay to remove placeholder
    clearGCShapes();

    // Keep the value object in sync with the local edit (same as text_evolve), so the live
    // state (queryState) reflects the content without waiting for a Java-side setText.
    state.text = newText;

    // Remember what we forward so its eventual Java echo doesn't clobber later keystrokes.
    recordSentText(newText);

    // Carries the edit as a range replacement (start/end/text), not the full document, so Java
    // can apply it via replaceTextRange instead of setText.
    final event = VEvent()
      ..text = insertedText
      ..start = rangeStart
      ..end = rangeEnd;
    widget.sendModifyModify(state, event);
  }

  //-----------Edition----------------

  /// This editor's own text shape, plus its index in [shapes] (-1 when it is the shape
  /// currently being edited, which lives outside that list).
  ///
  /// Keyed on `styledTextId`: `editable` says whether the user may *type*, not whether the
  /// shape is ours, and SWT moves the caret, selects and deselects in a read-only StyledText
  /// exactly as in an editable one. A position past the end of the text still resolves to the
  /// shape — SWT clamps such a click to the nearest offset rather than ignoring it.
  (TextShape?, int) _ownTextShapeAt(Offset contentPos, Size canvasSize) {
    if (_isEditingText && _editableTextShape != null) {
      return (_editableTextShape, -1);
    }
    for (int i = shapes.length - 1; i >= 0; i--) {
      final shape = shapes[i];
      if (shape is TextShape &&
          shape.styledTextId == state.id &&
          shape.containsPoint(contentPos, canvasSize)) {
        return (shape, i);
      }
    }
    final index =
        shapes.lastIndexWhere((s) => s is TextShape && s.styledTextId == state.id);
    return index == -1 ? (null, -1) : (shapes[index] as TextShape, index);
  }

  void _handleTap(Offset position) {
    final RenderBox? renderBox = context.findRenderObject() as RenderBox?;
    if (renderBox == null) return;

    final canvasSize = renderBox.size;
    position = _toContentPosition(position);

    final (tappedTextShape, shapeIndex) = _ownTextShapeAt(position, canvasSize);

    if (tappedTextShape == null) {
      if (_isEditingText) {
        _stopEditing();
      }
      widget.sendFocusFocusIn(state, null);
      _focusNode.requestFocus();
      return;
    }

    final caretOffset = tappedTextShape.getOffsetFromPosition(position, canvasSize);

    if (!_isEditingText ||
        _editableTextShape?.styledTextId != tappedTextShape.styledTextId) {
      if (_isEditingText) {
        _stopEditing();
      }
      _startEditing(tappedTextShape, shapeIndex);
    }

    setState(() {
      _editableTextShape = _editableTextShape!.clearSelection().moveCaret(
        caretOffset,
      );

      _isSelecting = false;
      _selectionStartOffset = null;
    });
    _sendSelectionCleared(caretOffset);

    _onCaretMoved();
    _focusNode.requestFocus();
  }

  /// Forwards the key to Java and reports the SWT event it sent, whose keyCode the verdicts for
  /// caret-only keys are matched against.
  VEvent _sendKeyDownEvent(RawKeyEvent event) {
    final vEvent = mapKeyEventToSwt(event);
    widget.sendKeyKeyDown(state, vEvent);
    return vEvent;
  }

  /// Keys whose whole local effect is moving the caret. They produce no Modify, so a vetoed one
  /// has no corrective push behind it the way a text-changing key does -- see [VerifyKeyGate].
  static bool _movesCaretOnly(RawKeyEvent event) {
    final key = event.logicalKey;
    return key == LogicalKeyboardKey.arrowLeft ||
        key == LogicalKeyboardKey.arrowRight ||
        key == LogicalKeyboardKey.arrowUp ||
        key == LogicalKeyboardKey.arrowDown ||
        key == LogicalKeyboardKey.home ||
        key == LogicalKeyboardKey.end;
  }

  void _handleKeyEvent(RawKeyEvent event) {
    if (event is! RawKeyDownEvent) return;

    // Must run before the edit-mode guard below: while this editor owns the keyboard nothing else
    // forwards, so a key not sent here reaches Java through no path at all.
    final sent = _sendKeyDownEvent(event);

    // Snapshot before applying: a caret-only key is applied straight away and undone if Java's
    // VerifyKey listeners reject it. Only these keys are queued -- Java answers only these, because
    // it never sees the keystrokes Eclipse consumes ahead of the widget.
    final gated = _verifyKeyGate.armed && _movesCaretOnly(event);
    final undo = gated ? _captureCaretRollback() : null;
    _applyKeyLocally(event);
    if (gated) _verifyKeyGate.record(sent.keyCode ?? 0, undo);
  }

  /// Captures the caret and selection so a rejected navigation key can be put back.
  ///
  /// The undo is skipped when the text has changed since: the user typed inside the round trip, and
  /// restoring an offset measured against the older document would put the caret in the wrong
  /// place. Leaving the caret where the typing left it is the safe direction.
  VoidCallback? _captureCaretRollback() {
    final shape = _editableTextShape;
    if (shape == null) return null;
    final caret = shape.caretInfo;
    final selection = shape.selectionInfo;
    final text = shape.text;
    return () {
      final current = _editableTextShape;
      if (!mounted || current == null || current.text != text) return;
      setState(() {
        var restored = caret == null ? current : current.copyWithCaret(caret);
        restored = selection == null
            ? restored.clearSelection()
            : restored.copyWithSelection(selection);
        _editableTextShape = restored;
      });
      _onCaretMoved();
    };
  }

  void _applyKeyLocally(RawKeyEvent event) {
    if (!_isEditingText || _editableTextShape == null) return;

    final isShiftPressed = event.data.isShiftPressed;
    final bool hadSelection = _editableTextShape?.selectionInfo?.hasSelection == true;
    final oldCaretOffset = _editableTextShape!.caretInfo?.offset;

    setState(() {
      final currentShape = _editableTextShape!;
      TextShape? newShape;

      if (event.logicalKey == LogicalKeyboardKey.backspace) {
        if (!_editable) return;
        if (currentShape.selectionInfo?.hasSelection == true) {
          newShape = currentShape.deleteSelection();
        } else {
          newShape = currentShape.backspace();
        }
      } else if (event.logicalKey == LogicalKeyboardKey.delete) {
        if (!_editable) return;
        if (currentShape.selectionInfo?.hasSelection == true) {
          newShape = currentShape.deleteSelection();
        } else {
          newShape = currentShape.delete();
        }
      } else if (event.logicalKey == LogicalKeyboardKey.arrowLeft) {
        final newOffset = (currentShape.caretInfo?.offset ?? 0) - 1;
        if (isShiftPressed) {
          newShape = currentShape.extendSelectionTo(newOffset);
        } else {
          newShape = currentShape.clearSelection();
        }
        newShape = (newShape).moveCaret(newOffset);
      } else if (event.logicalKey == LogicalKeyboardKey.arrowRight) {
        final newOffset = (currentShape.caretInfo?.offset ?? 0) + 1;
        if (isShiftPressed) {
          newShape = currentShape.extendSelectionTo(newOffset);
        } else {
          newShape = currentShape.clearSelection();
        }
        newShape = (newShape).moveCaret(newOffset);
      } else if (event.logicalKey == LogicalKeyboardKey.arrowUp) {
        final newOffset = _calculateLineBasedVerticalNavigation(
          currentShape,
          -1,
        );
        if (isShiftPressed) {
          newShape = currentShape.extendSelectionTo(newOffset);
        } else {
          newShape = currentShape.clearSelection();
        }
        newShape = (newShape).moveCaret(newOffset);
      } else if (event.logicalKey == LogicalKeyboardKey.arrowDown) {
        final newOffset = _calculateLineBasedVerticalNavigation(
          currentShape,
          1,
        );
        if (isShiftPressed) {
          newShape = currentShape.extendSelectionTo(newOffset);
        } else {
          newShape = currentShape.clearSelection();
        }
        newShape = (newShape).moveCaret(newOffset);
      } else if (event.logicalKey == LogicalKeyboardKey.home) {
        int newOffset;
        if (currentShape.wordWrap == true) {
          newOffset = _calculateLineNavigation(currentShape, isHome: true);
        } else {
          newOffset = 0;
        }
        if (isShiftPressed) {
          newShape = currentShape.extendSelectionTo(newOffset);
        } else {
          newShape = currentShape.clearSelection();
        }
        newShape = (newShape).moveCaret(newOffset);
      } else if (event.logicalKey == LogicalKeyboardKey.end) {
        int newOffset;
        if (currentShape.wordWrap == true) {
          newOffset = _calculateLineNavigation(currentShape, isHome: false);
        } else {
          newOffset = currentShape.text.length;
        }
        if (isShiftPressed) {
          newShape = currentShape.extendSelectionTo(newOffset);
        } else {
          newShape = currentShape.clearSelection();
        }
        newShape = (newShape).moveCaret(newOffset);
      } else if (event.logicalKey == LogicalKeyboardKey.keyA &&
          (event.data.isControlPressed || event.data.isMetaPressed)) {
        newShape = currentShape.selectAll();
      } else if (event.logicalKey == LogicalKeyboardKey.keyC &&
          (event.data.isControlPressed || event.data.isMetaPressed)) {
        final selectedText = currentShape.getSelectedText();
        if (selectedText.isNotEmpty) {
          Clipboard.setData(ClipboardData(text: selectedText));
        }
        return;
      } else if (event.logicalKey == LogicalKeyboardKey.keyX &&
          (event.data.isControlPressed || event.data.isMetaPressed)) {
        final selectedText = currentShape.getSelectedText();
        if (selectedText.isNotEmpty) {
          Clipboard.setData(ClipboardData(text: selectedText));
          if (_editable) {
            newShape = currentShape.deleteSelection();
          }
        }
      } else if (event.logicalKey == LogicalKeyboardKey.keyV &&
          (event.data.isControlPressed || event.data.isMetaPressed)) {
        if (_editable) {
          _handlePaste();
        }
        return;
      } else if (event.logicalKey == LogicalKeyboardKey.escape) {
        newShape = currentShape.clearSelection();
      } else if (event.logicalKey == LogicalKeyboardKey.enter) {
        if (!_editable) return;
        if ((state.style & SWT.SINGLE) == 0) {
          var shape = currentShape;
          if (shape.selectionInfo?.hasSelection == true) shape = shape.deleteSelection();
          newShape = shape.insertText('\n', shape.caretInfo?.offset ?? 0);
        }
      } else if (event.character != null && event.character!.isNotEmpty) {
        if (!_editable) return;
        if (event.data.isMetaPressed || event.data.isControlPressed) return;
        if (_composedInput.isComposing) return;
        final char = event.character!;
        if (char.codeUnitAt(0) >= 32) {
          var shape = currentShape;
          if (shape.selectionInfo?.hasSelection == true) shape = shape.deleteSelection();
          newShape = shape.insertText(char, shape.caretInfo?.offset ?? 0);
        }
      }

      if (newShape != null) {
        _editableTextShape = newShape;

        if (_isInLocalEditMode && newShape.editingState != null) {
          _localEditingState = newShape.editingState;
        }
      }
    });

    final bool isDestructiveKey = event.logicalKey == LogicalKeyboardKey.backspace ||
        event.logicalKey == LogicalKeyboardKey.delete ||
        (event.character != null &&
         event.character!.isNotEmpty &&
         !_composedInput.isComposing &&
         !event.data.isMetaPressed &&
         !event.data.isControlPressed);
    if (hadSelection && isDestructiveKey) {
      _sendSelectionCleared(_editableTextShape?.caretInfo?.offset ?? 0);
    }

    final newCaretOffset = _editableTextShape?.caretInfo?.offset;
    if (newCaretOffset != null && newCaretOffset != oldCaretOffset) {
      _onCaretMoved();
    }
  }

  /// Inserts text the platform composed — a dead-key sequence, an accent, an IME run. It arrives
  /// on a later keystroke than the [RawKeyEvent] that started it, so it carries the selection and
  /// caret bookkeeping [_handleKeyEvent] does for a typed character.
  void _insertComposedText(String text) {
    if (!_isEditingText || _editableTextShape == null || !_editable) return;

    final bool hadSelection = _editableTextShape!.selectionInfo?.hasSelection == true;
    final int? oldCaretOffset = _editableTextShape!.caretInfo?.offset;

    setState(() {
      var shape = _editableTextShape!;
      if (shape.selectionInfo?.hasSelection == true) shape = shape.deleteSelection();
      final newShape = shape.insertText(text, shape.caretInfo?.offset ?? 0);
      _editableTextShape = newShape;
      if (_isInLocalEditMode && newShape.editingState != null) {
        _localEditingState = newShape.editingState;
      }
    });

    if (hadSelection) {
      _sendSelectionCleared(_editableTextShape?.caretInfo?.offset ?? 0);
    }
    final newCaretOffset = _editableTextShape?.caretInfo?.offset;
    if (newCaretOffset != null && newCaretOffset != oldCaretOffset) {
      _onCaretMoved();
    }
  }

  void _handlePaste() async {
    if (!_isEditingText || _editableTextShape == null || !_editable) return;

    try {
      final data = await Clipboard.getData('text/plain');
      if (data?.text != null && _editableTextShape != null) {
        setState(() {
          _editableTextShape = _editableTextShape!.insertText(
            data!.text!,
            _editableTextShape!.caretInfo?.offset ?? 0,
          );

          if (_isInLocalEditMode && _editableTextShape!.editingState != null) {
            _localEditingState = _editableTextShape!.editingState;
          }
        });
        _onCaretMoved();
      }
    } catch (e) {
      // Error pasting
    }
  }

  Offset? _pointerDownPos;
  bool _secondaryPointerDown = false;
  static const _panSlop = 4.0;

  void _handlePointerDown(Offset position, int buttons) {
    _isSelecting = false;
    _pointerDownPos = position;
    // SwtStyledText.handleMouseDown returns before touching selection or caret unless
    // event.button == 1, so a secondary click only takes focus. It must not reach the tap
    // detector either, or it would count toward a following double/triple click.
    // The same guard excludes Ctrl+Click on macOS (its IS_MAC && MOD4 clause), which the
    // platform and ControlImpl.applyMenu both treat as the context-menu gesture.
    _secondaryPointerDown = buttons & kSecondaryButton != 0 ||
        (defaultTargetPlatform == TargetPlatform.macOS &&
            HardwareKeyboard.instance.isControlPressed);
    if (_secondaryPointerDown) {
      _lastTapCount = 1;
      return;
    }
    _lastTapCount = _tapDetector.registerTap(position: position);
    if (_lastTapCount == 2) _doubleTapPosition = position;
    if (_lastTapCount == 3) _tripleTapPosition = position;
  }

  void _handlePointerMove(Offset position) {
    if (_secondaryPointerDown) return;
    if (_pointerDownPos == null) return;
    if (!_isSelecting) {
      final d = position - _pointerDownPos!;
      if (d.dx * d.dx + d.dy * d.dy < _panSlop * _panSlop) return;
      _handlePanStart(_pointerDownPos!);
    }
    _handlePanUpdate(position);
  }

  void _handlePointerUp(Offset position) {
    _pointerDownPos = null;
    if (_secondaryPointerDown) {
      _secondaryPointerDown = false;
      // handleMouseDown still runs forceFocus() before the button check.
      _focusNode.requestFocus();
      return;
    }
    if (_isSelecting) {
      _handlePanEnd();
    } else {
      if (_lastTapCount == 3) {
        _handleTripleTap();
      } else if (_lastTapCount == 2) {
        _handleDoubleTap();
      } else {
        _handleTap(position);
      }
    }
    _isSelecting = false;
    _selectionStartOffset = null;
  }

  void _handlePanStart(Offset position) {
    final RenderBox? renderBox = context.findRenderObject() as RenderBox?;
    if (renderBox == null) return;

    final canvasSize = renderBox.size;
    position = _toContentPosition(position);
    TextShape? textShape;

    if (_isEditingText && _editableTextShape != null) {
      textShape = _editableTextShape;
    }

    if (textShape == null) {
      for (int i = shapes.length - 1; i >= 0; i--) {
        final shape = shapes[i];
        if (shape is TextShape && shape.containsPoint(position, canvasSize)) {
          textShape = shape;
          break;
        }
      }
    }

    if (textShape == null) {
      for (int i = shapes.length - 1; i >= 0; i--) {
        if (shapes[i] is TextShape) {
          textShape = shapes[i] as TextShape;
          break;
        }
      }
    }

    if (textShape != null) {
      _isSelecting = true;
      _selectionStartOffset = textShape.getOffsetFromPosition(
        position,
        canvasSize,
      );

      if (!_isEditingText) {
        _startEditing(textShape, _getTextShapeIndex(textShape));
      }
    }
  }

  void _handlePanUpdate(Offset position) {
    if (!_isSelecting ||
        _editableTextShape == null ||
        _selectionStartOffset == null) {
      return;
    }

    final RenderBox? renderBox = context.findRenderObject() as RenderBox?;
    if (renderBox == null) return;

    final canvasSize = renderBox.size;
    final currentOffset = _editableTextShape!.getOffsetFromPosition(
      _toContentPosition(position),
      canvasSize,
    );

    setState(() {
      final selection = SelectionInfo.fromRange(
        _selectionStartOffset!,
        currentOffset,
      );
      _editableTextShape = _editableTextShape!.copyWithSelection(selection);

      final caretPos = currentOffset;
      _editableTextShape = _editableTextShape!.updateCaretOffset(caretPos);
    });
    _onCaretMoved();
  }

  void _handlePanEnd() {
    _isSelecting = false;
    _selectionStartOffset = null;
    _sendSelectionChange();
  }

  /// StyledText.getDoubleClickEnabled(); SWT defaults it to true, so an absent value is true.
  bool get _doubleClickEnabled => state.doubleClickEnabled != false;

  void _handleDoubleTap() {
    if (!_doubleClickEnabled) return;
    final pos = _doubleTapPosition;
    if (pos == null) return;
    final RenderBox? rb = context.findRenderObject() as RenderBox?;
    if (rb == null) return;

    if (!_isEditingText || _editableTextShape == null) {
      final contentPos = _toContentPosition(pos);
      final (textShape, shapeIndex) = _ownTextShapeAt(contentPos, rb.size);
      if (textShape == null) return;
      if (_isEditingText) _stopEditing();
      _startEditing(textShape, shapeIndex);
      _focusNode.requestFocus();
      WidgetsBinding.instance.addPostFrameCallback((_) {
        if (mounted) _applyDoubleClickWordSelection(pos, rb.size);
      });
      return;
    }

    _applyDoubleClickWordSelection(pos, rb.size);
  }

  void _applyDoubleClickWordSelection(Offset pos, Size canvasSize) {
    if (_editableTextShape == null) return;
    final contentPos = _toContentPosition(pos);
    final charOffset = _editableTextShape!.getOffsetFromPosition(contentPos, canvasSize);
    final text = _editableTextShape!.text;
    final (wordStart, wordEnd) = getWordBoundaries(text, charOffset);
    if (wordStart == wordEnd) return;
    setState(() {
      _editableTextShape = _editableTextShape!
          .updateCaretOffset(charOffset)
          .copyWithSelection(SelectionInfo.fromRange(wordStart, wordEnd));
    });
    _onCaretMoved();
    _sendSelectionChange();
    widget.sendMouseMouseDoubleClick(
      state,
      VEvent()
        ..button = 1
        ..count = 2
        ..start = wordStart
        ..end = wordEnd,
    );
  }

  void _handleTripleTap() {
    if (!_doubleClickEnabled) return;
    final pos = _tripleTapPosition;
    if (pos == null) return;
    final RenderBox? rb = context.findRenderObject() as RenderBox?;
    if (rb == null) return;

    if (!_isEditingText || _editableTextShape == null) {
      final contentPos = _toContentPosition(pos);
      final (textShape, shapeIndex) = _ownTextShapeAt(contentPos, rb.size);
      if (textShape == null) return;
      if (_isEditingText) _stopEditing();
      _startEditing(textShape, shapeIndex);
      _focusNode.requestFocus();
      WidgetsBinding.instance.addPostFrameCallback((_) {
        if (mounted) _applyTripleClickLineSelection(pos, rb.size);
      });
      return;
    }

    _applyTripleClickLineSelection(pos, rb.size);
  }

  void _applyTripleClickLineSelection(Offset pos, Size canvasSize) {
    if (_editableTextShape == null) return;
    final contentPos = _toContentPosition(pos);
    final charOffset = _editableTextShape!.getOffsetFromPosition(contentPos, canvasSize);
    final text = _editableTextShape!.text;
    final (lineStart, lineEnd) = getLineBoundaries(text, charOffset);
    // SwtStyledText.handleMouseDown ends a line select at the start of the next line, so
    // the trailing newline is part of the selection; on the last line it ends at the content.
    final selectionEnd = lineEnd < text.length ? lineEnd + 1 : text.length;
    setState(() {
      _editableTextShape = _editableTextShape!
          .updateCaretOffset(charOffset)
          .copyWithSelection(SelectionInfo.fromRange(lineStart, selectionEnd));
    });
    _onCaretMoved();
    _sendSelectionChange();
  }

  void _onCaretMoved() => _resetCaretBlink();

  void _resetCaretBlink() {
    _caretBlinkTimer?.cancel();
    _caretBlinkTimer = null;
    if (!_isEditingText || _editableTextShape?.caretInfo == null) return;

    final rate = _editableTextShape!.caretInfo!.blinkRate;
    if (rate == 0) return;

    final caret = _editableTextShape!.caretInfo!;
    if (!caret.visible) {
      setState(() {
        _editableTextShape = _editableTextShape!.copyWithCaret(
          caret.copyWith(visible: true),
        );
      });
    }

    _caretBlinkTimer = Timer.periodic(Duration(milliseconds: rate), (timer) {
      if (_editableTextShape?.caretInfo != null) {
        setState(() {
          final currentCaret = _editableTextShape!.caretInfo!;
          final newCaret = currentCaret.copyWith(visible: !currentCaret.visible);
          _editableTextShape = _editableTextShape!.copyWithCaret(newCaret);

          final index = shapes.indexWhere(
            (s) => s is TextShape && s.styledTextId == currentCaret.styledTextId,
          );
          if (index != -1) {
            shapes[index] = (shapes[index] as TextShape).copyWithCaret(
              newCaret,
            );
          }
        });
      }
    });
  }

  void _startEditing(TextShape textShape, int shapeIndex) {
    // Seed the pre-edit baseline so a stale snapshot carrying the text as it stood at edit
    // start (the one value not covered by per-keystroke sends) is recognised as an echo.
    seedTextEchoBaseline(textShape.text);
    _enterLocalEditMode(textShape);
    if (_editable) _composedInput.attach();
    widget.sendFocusFocusIn(state, null);
    setState(() {
      _isEditingText = true;
      if (shapeIndex >= 0 && shapeIndex < shapes.length) {
        shapes.removeAt(shapeIndex);
      }
    });
    _onCaretMoved();
  }

  void _enterLocalEditMode(TextShape textShape) {
    _isInLocalEditMode = true;
    _originalServerTextShape = textShape;

    _localEditingState =
        textShape.editingState ?? _extractEditingStateFromTextShape(textShape);

    _updateLocalTextShape();
  }

  TextEditingState _extractEditingStateFromTextShape(TextShape textShape) {
    List<StyleRange> characterRanges = [];

    if (textShape.editingState != null) {
      return textShape.editingState!;
    }

    if (textShape.textSpan != null) {
      _extractCharacterRangesFromTextSpan(
        textShape.textSpan!,
        characterRanges,
        0,
      );
    } else {
      final defaultTextColor = _styledTextTheme.foregroundColor;

      characterRanges.add(
        StyleRange(
          start: 0,
          end: textShape.text.length,
          style: textShape.style.copyWith(color: defaultTextColor),
        ),
      );
    }

    return TextEditingState(
      characterRanges: characterRanges,
      lineProperties: const {},
    );
  }

  void _extractCharacterRangesFromTextSpan(
    TextSpan span,
    List<StyleRange> ranges,
    int currentOffset,
  ) {
    if (span.text != null && span.text!.isNotEmpty) {
      final textLength = span.text!.length;
      if (span.style != null) {
        ranges.add(
          StyleRange(
            start: currentOffset,
            end: currentOffset + textLength,
            style: span.style!,
          ),
        );
      }
      currentOffset += textLength;
    }

    if (span.children != null) {
      for (var child in span.children!) {
        if (child is TextSpan) {
          _extractCharacterRangesFromTextSpan(child, ranges, currentOffset);
          currentOffset += child.toPlainText().length;
        }
      }
    }
  }

  // `caret`/`selection` override what the server snapshot carries: while an edit session is
  // open the local caret and selection are ahead of anything Java has echoed back.
  void _updateLocalTextShape({CaretInfo? caret, SelectionInfo? selection}) {
    if (!_isInLocalEditMode ||
        _originalServerTextShape == null ||
        _localEditingState == null) {
      return;
    }

    _editableTextShape = TextShape(
      _originalServerTextShape!.text,
      _originalServerTextShape!.off,
      _originalServerTextShape!.style,
      _originalServerTextShape!.clipRect,
      null,
      caret ?? _originalServerTextShape!.caretInfo,
      _originalServerTextShape!.wordWrap,
      _originalServerTextShape!.canvasSize,
      _originalServerTextShape!.editable,
      _originalServerTextShape!.styledTextId,
      _notifyTextChanged,
      _localEditingState,
      selection,
      _originalServerTextShape!.lineHeight,
      _originalServerTextShape!.tabs,
    );
  }

  void _cleanupLocalEditMode() {
    _isInLocalEditMode = false;
    _originalServerTextShape = null;
    _localEditingState = null;
    _hasProgrammaticSelection = false;
    // Edit session over: Java is authoritative again (we just sent the definitive
    // StateUpdate), so any leftover in-flight echoes are irrelevant.
    clearSentTextEchoes();
  }

  /// Build a VStyledText with updated text, caret, and style ranges
  VStyledText _buildUpdatedVStyledText() {
    final updatedState = VStyledText()
      ..id = state.id
      ..swt = state.swt
      ..text = _editableTextShape!.text
      ..caretOffset = _editableTextShape!.caretInfo?.offset ?? 0;

    // Build renderer with style ranges
    if (_localEditingState != null) {
      final renderer = VStyledTextRenderer();
      renderer.styles = _convertLocalRangesToVStyleRanges();
      updatedState.renderer = renderer;
    }

    return updatedState;
  }

  /// Convert local StyleRange list to VStyleRange list for serialization
  List<VStyleRange> _convertLocalRangesToVStyleRanges() {
    if (_localEditingState == null) return [];

    final currentText = _editableTextShape?.text ?? '';
    final validRanges = _localEditingState!.characterRanges
        .where((range) => range.start >= 0 && range.end <= currentText.length)
        .toList();

    return validRanges.map((range) {
      final vRange = VStyleRange()
        ..start = range.start
        ..length = range.end - range.start;

      final style = range.style;

      // Convert foreground color
      if (style.color != null) {
        vRange.foreground = VColor()
          ..red = style.color!.red
          ..green = style.color!.green
          ..blue = style.color!.blue
          ..alpha = style.color!.alpha;
      }

      // Convert background color
      if (style.backgroundColor != null) {
        vRange.background = VColor()
          ..red = style.backgroundColor!.red
          ..green = style.backgroundColor!.green
          ..blue = style.backgroundColor!.blue
          ..alpha = style.backgroundColor!.alpha;
      }

      // Convert font style
      int fontStyle = 0;
      if (style.fontWeight == FontWeight.bold &&
          style.fontStyle == FontStyle.italic) {
        fontStyle = 3; // BOLD | ITALIC
      } else if (style.fontWeight == FontWeight.bold) {
        fontStyle = 1; // BOLD
      } else if (style.fontStyle == FontStyle.italic) {
        fontStyle = 2; // ITALIC
      }
      vRange.fontStyle = fontStyle;

      // Convert decorations
      if (style.decoration != null) {
        if (style.decoration!.contains(TextDecoration.underline)) {
          vRange.underline = true;
          vRange.underlineStyle = 1;
        }
        if (style.decoration!.contains(TextDecoration.lineThrough)) {
          vRange.strikeout = true;
        }
      }

      return vRange;
    }).toList();
  }

  void _stopEditing() {
    _composedInput.detach();
    if (_isInLocalEditMode && _editableTextShape != null) {
      // Send all updated state in a single event
      final updatedState = _buildUpdatedVStyledText();
      EquoCommService.sendPayload(
        "${state.swt}/${state.id}/StateUpdate",
        updatedState,
      );
    }

    _cleanupLocalEditMode();

    setState(() {
      _isEditingText = false;

      if (_editableTextShape != null) {
        final hiddenCaret = _editableTextShape!.caretInfo != null
            ? _editableTextShape!.copyWithCaret(
                _editableTextShape!.caretInfo!.copyWith(visible: false),
              )
            : _editableTextShape!;
        shapes.add(hiddenCaret);
        _editableTextShape = null;
      }
    });

    _caretBlinkTimer?.cancel();
  }

  TextShape? _findEditableTextShapeAtPosition(
    Offset position,
    Size canvasSize,
  ) {
    for (int i = shapes.length - 1; i >= 0; i--) {
      final shape = shapes[i];
      if (shape is TextShape &&
          shape.editable &&
          shape.containsPoint(position, canvasSize)) {
        return shape;
      }
    }
    return null;
  }

  int _getTextShapeIndex(TextShape textShape) {
    for (int i = 0; i < shapes.length; i++) {
      if (shapes[i] == textShape) return i;
    }
    return -1;
  }

  /// Navigate vertically based on explicit line breaks (\n) in the text
  int _calculateLineBasedVerticalNavigation(
    TextShape textShape,
    int direction,
  ) {
    final text = textShape.text;
    final currentOffset = textShape.caretInfo?.offset ?? 0;
    final lines = text.split('\n');

    // Find current line and position within line
    int currentLineIndex = 0;
    int lineStartOffset = 0;
    int positionInLine = currentOffset;

    for (int i = 0; i < lines.length; i++) {
      final lineLength = lines[i].length;
      final lineEndOffset = lineStartOffset + lineLength;

      if (currentOffset <= lineEndOffset) {
        currentLineIndex = i;
        positionInLine = currentOffset - lineStartOffset;
        break;
      }

      lineStartOffset = lineEndOffset + 1; // +1 for the \n
    }

    // Calculate target line
    final targetLineIndex = currentLineIndex + direction;

    // Bounds check
    if (targetLineIndex < 0) {
      return 0; // Go to start of text
    }
    if (targetLineIndex >= lines.length) {
      return text.length; // Go to end of text
    }

    // Calculate offset in target line
    int targetLineStartOffset = 0;
    for (int i = 0; i < targetLineIndex; i++) {
      targetLineStartOffset += lines[i].length + 1; // +1 for \n
    }

    final targetLineLength = lines[targetLineIndex].length;
    final targetPositionInLine = positionInLine.clamp(0, targetLineLength);

    return targetLineStartOffset + targetPositionInLine;
  }

  int _calculateVerticalNavigation(TextShape textShape, int direction) {
    return textShape.calculateVerticalNavigation(direction, getBounds());
  }

  int _calculateLineNavigation(TextShape textShape, {required bool isHome}) {
    return textShape.calculateLineNavigation(
      isHome: isHome,
      canvasSize: getBounds(),
    );
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
    this.span,
    this.style,
    this.align,
    this.maxWidth,
    this.tabs,
  );

  final InlineSpan span;
  final TextStyle style;
  final TextAlign align;
  final double maxWidth;
  final int tabs;

  @override
  bool operator ==(Object other) =>
      other is _LineLayoutKey &&
      other.span == span &&
      other.style == style &&
      other.align == align &&
      other.maxWidth == maxWidth &&
      other.tabs == tabs;

  @override
  int get hashCode => Object.hash(span, style, align, maxWidth, tabs);
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
  _lineLayoutCache[key] = layout;
  _lineLayoutCacheChars += layout.cost;
  while (_lineLayoutCacheChars > _lineLayoutCacheCharBudget &&
      _lineLayoutCache.length > 1) {
    final evicted = _lineLayoutCache.remove(_lineLayoutCache.keys.first);
    if (evicted != null) _lineLayoutCacheChars -= evicted.cost;
  }
}

/// Test seam: layouts served from the cache instead of re-derived.
@visibleForTesting
int debugLineLayoutHits = 0;

@visibleForTesting
void debugResetLineLayoutCache() {
  _lineLayoutCache.clear();
  _lineLayoutCacheChars = 0;
  debugLineLayoutHits = 0;
}

class TextShape extends Shape {
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
  ]);

  _LineTopsCache _lineTopsCache = _LineTopsCache();

  /// Per-line properties that affect layout, from [editingState] when present.
  ({int indent, TextAlign align, int vIndent}) _linePropsFor(int lineIndex) {
    final props = editingState?.lineProperties[lineIndex];
    final indent = props?.indent ?? 0;
    final align = _mapSwtAlignmentToTextAlign(props?.alignment ?? 16384);
    return (
      indent: indent,
      align: (props?.justify ?? false) ? TextAlign.justify : align,
      vIndent: props?.verticalIndent ?? 0,
    );
  }

  // A line's vertical indent (StyledText.setLineVerticalIndent) is extra space
  // above the line's text, inside the line's own box: the box top stays where the
  // previous line ended, the box grows by the indent, and the glyphs (and caret,
  // selection, hit targets) sit vIndent below the box top. _lineTops holds box
  // tops; every text-coordinate consumer adds vIndent for its line.
  List<double> _lineTopsFor(List<String> lines, Size? canvas) {
    final tops = List<double>.filled(lines.length + 1, 0);
    double y = 0;
    for (int i = 0; i < lines.length; i++) {
      tops[i] = y;
      final props = _linePropsFor(i);
      final layout = _lineLayout(
        lines[i],
        i,
        align: props.align,
        maxWidth: _lineMaxWidth(props.indent, canvas),
        wantCaretX: false,
      );
      y += props.vIndent + _advance(layout.height);
    }
    tops[lines.length] = y;
    return tops;
  }

  List<double> _lineTops(List<String> lines) =>
      _lineTopsCache.tops ??= _lineTopsFor(lines, canvasSize);

  // Vertical advance for one logical line. Each logical line is painted by a single
  // TextPainter that may wrap into several visual lines (tpHeight is the wrapped total),
  // so advance by tpHeight; fall back to lineHeight as a floor (empty lines whose painter
  // measures ~0, or to keep a consistent single-line height).
  double _advance(double tpHeight) {
    final lh = lineHeight > 0 ? lineHeight : (style.fontSize ?? 16) * 1.2;
    return tpHeight > lh ? tpHeight : lh;
  }

  // The single wrap policy for every per-line layout: a line wraps at the canvas
  // width only when wordWrap is on, matching what draw() paints. A site that wraps
  // on its own measures visual rows the paint never drew, so its running Y outpaces
  // the painted one and hit-tests resolve above the glyphs that were clicked.
  double _lineMaxWidth(int indent, [Size? canvas]) {
    final size = canvas ?? canvasSize;
    if (wordWrap != true || size == null) return double.infinity;
    final maxW = size.width - off.dx - indent.toDouble();
    return maxW > 0 ? maxW : double.infinity;
  }

  @override
  void draw(Canvas c) {
    if (clipRect != null) {
      c.save();
      c.clipRect(clipRect!);
    }

    TextSpan effectiveTextSpan;

    if (editingState != null) {
      effectiveTextSpan = TextRenderer.buildFinalTextSpan(
        text,
        editingState!,
        style,
      );
    } else if (textSpan != null) {
      effectiveTextSpan = textSpan!;
    } else {
      effectiveTextSpan = TextSpan(text: text, style: style);
    }

    final lines = text.split('\n');
    final paintOffset = off;
    double currentY = paintOffset.dy;

    // The paint loop measures every line anyway, so fill the line-tops cache
    // for free while at it — _drawSelection/_drawCaret below then just look up.
    final cacheTops = _lineTopsCache.tops == null
        ? List<double>.filled(lines.length + 1, 0)
        : null;

    for (int i = 0; i < lines.length; i++) {
      final line = lines[i];
      cacheTops?[i] = currentY - paintOffset.dy;

      int indent = 0;
      int alignValue = 16384; // Default SWT.LEFT
      bool justify = false;
      int vIndent = 0;

      if (editingState != null && editingState!.lineProperties.containsKey(i)) {
        final lineProps = editingState!.lineProperties[i]!;
        indent = lineProps.indent ?? 0;
        alignValue = lineProps.alignment ?? 16384;
        justify = lineProps.justify ?? false;
        vIndent = lineProps.verticalIndent ?? 0;
      }

      final align = _mapSwtAlignmentToTextAlign(alignValue);
      final effectiveAlign = justify ? TextAlign.justify : align;

      final maxW = _lineMaxWidth(indent);

      final tp = _layoutLine(
        line,
        i,
        effectiveTextSpan,
        align: effectiveAlign,
        maxWidth: maxW,
      );

      double finalX = paintOffset.dx + indent.toDouble();

      if (canvasSize != null && maxW != double.infinity) {
        switch (effectiveAlign) {
          case TextAlign.center:
            final availableWidth = maxW;
            final textWidth = tp.width;
            if (textWidth < availableWidth) {
              finalX =
                  paintOffset.dx +
                  indent.toDouble() +
                  (availableWidth - textWidth) / 2;
            }
            break;
          case TextAlign.right:
            final availableWidth = maxW;
            final textWidth = tp.width;
            if (textWidth < availableWidth) {
              finalX =
                  paintOffset.dx +
                  indent.toDouble() +
                  (availableWidth - textWidth);
            }
            break;
          case TextAlign.justify:
          case TextAlign.left:
          default:
            break;
        }
      }

      tp.paint(c, Offset(finalX, currentY + vIndent));
      currentY += vIndent + _advance(tp.height);
    }

    if (cacheTops != null) {
      cacheTops[lines.length] = currentY - paintOffset.dy;
      _lineTopsCache.tops = cacheTops;
    }

    if (selectionInfo != null && selectionInfo!.hasSelection) {
      _drawSelection(c);
    }

    if (caretInfo != null && caretInfo!.visible) {
      _drawCaret(c);
    }

    if (clipRect != null) {
      c.restore();
    }
  }

  // Per-char x boundaries are skipped past this size so a large document doesn't
  // ship a payload proportional to its character count on every paint; the Java
  // side then falls back to its glyph-table estimate for within-line x only.
  static const int _charXPayloadLimit = 20000;

  /// Per-visual-line geometry of the document exactly as [draw] paints it, for the
  /// Java side to answer its position API from (the `TextGeometry` push).
  ///
  /// Coordinates are relative to the text origin ([off] is not applied — the Java
  /// side adds margins and scroll offsets). Each entry describes one visual line
  /// after wrapping: `l` logical line, `s`/`e` document offset range, `x`/`y`/`w`/`h`
  /// box, and `cx` the per-character x boundaries (length `e - s + 1`).
  Map<String, dynamic>? computeGeometry() {
    final lines = text.split('\n');
    final includeCharX = text.length <= _charXPayloadLimit;
    final visual = <Map<String, dynamic>>[];
    double currentY = 0;
    double maxWidth = 0;
    int docOffset = 0;

    for (int i = 0; i < lines.length; i++) {
      final line = lines[i];

      int indent = 0;
      int alignValue = 16384; // Default SWT.LEFT
      bool justify = false;
      int vIndent = 0;
      if (editingState != null && editingState!.lineProperties.containsKey(i)) {
        final lineProps = editingState!.lineProperties[i]!;
        indent = lineProps.indent ?? 0;
        alignValue = lineProps.alignment ?? 16384;
        justify = lineProps.justify ?? false;
        vIndent = lineProps.verticalIndent ?? 0;
      }
      final align = _mapSwtAlignmentToTextAlign(alignValue);
      final effectiveAlign = justify ? TextAlign.justify : align;

      final maxW = _lineMaxWidth(indent);

      final layout = _lineLayout(
        line,
        i,
        align: effectiveAlign,
        maxWidth: maxW,
        wantCaretX: includeCharX,
      );

      double finalX = indent.toDouble();
      if (canvasSize != null && maxW != double.infinity) {
        switch (effectiveAlign) {
          case TextAlign.center:
            if (layout.width < maxW) {
              finalX = indent.toDouble() + (maxW - layout.width) / 2;
            }
            break;
          case TextAlign.right:
            if (layout.width < maxW) {
              finalX = indent.toDouble() + (maxW - layout.width);
            }
            break;
          default:
            break;
        }
      }

      final caretX = layout.caretX;
      List<double> charX(int from, int to) => [
        for (int k = from; k <= to; k++) finalX + caretX![k],
      ];

      // The visual line's box: `y`/`h` include the line's vertical indent so bands
      // stay contiguous; `vi` (first visual row only) is the indent within the box,
      // i.e. the glyphs sit at y + vi. The Java side adds `vi` when answering
      // text-position queries and uses the box for line/pixel queries.
      final metrics = layout.metrics;
      if (metrics.length <= 1) {
        maxWidth = math.max(maxWidth, finalX + layout.width);
        visual.add({
          'l': i,
          's': docOffset,
          'e': docOffset + line.length,
          'x': finalX,
          'y': currentY,
          'w': layout.width,
          'h': vIndent + _advance(layout.height),
          if (vIndent != 0) 'vi': vIndent,
          if (includeCharX) 'cx': charX(0, line.length),
        });
      } else {
        int local = 0;
        for (int m = 0; m < metrics.length && local <= line.length; m++) {
          final lm = metrics[m];
          int vEnd = m < layout.rowEnds.length ? layout.rowEnds[m] : line.length;
          maxWidth = math.max(maxWidth, finalX + lm.left + lm.width);
          visual.add({
            'l': i,
            's': docOffset + local,
            'e': docOffset + vEnd,
            'x': finalX + lm.left,
            'y': m == 0
                ? currentY
                : currentY + vIndent + (lm.baseline - lm.ascent),
            'w': lm.width,
            'h': m == 0 ? vIndent + lm.height : lm.height,
            if (m == 0 && vIndent != 0) 'vi': vIndent,
            if (includeCharX) 'cx': charX(local, vEnd),
          });
          local = vEnd;
        }
      }

      currentY += vIndent + _advance(layout.height);
      docOffset += line.length + 1;
    }

    return {
      'charCount': text.length,
      'contentWidth': maxWidth,
      'contentHeight': currentY,
      'lines': visual,
    };
  }

  TextSpan _getTextSpanForLine(
    String lineText,
    int lineIndex,
    TextSpan unifiedTextSpan,
  ) {
    if (editingState != null) {
      return _buildLineTextSpanFromState(lineText, lineIndex);
    } else {
      return TextSpan(text: lineText, style: style);
    }
  }

  /// Count of per-line layouts performed, for tests asserting layout cost.
  @visibleForTesting
  static int debugLayoutLineCalls = 0;

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
      _getTextSpanForLine(lineText, lineIndex, TextSpan(text: text, style: style)),
      style,
      align,
      maxWidth,
      tabs,
    );

    var layout = _lineLayoutCache.remove(key);
    if (layout != null && (!wantCaretX || layout.caretX != null)) {
      debugLineLayoutHits++;
    } else {
      final expanded = _expandTabStops(key.span as TextSpan);
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
          final boundary = tp.getLineBoundary(TextPosition(offset: local));
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
    int lineIndex,
    TextSpan unifiedTextSpan, {
    required TextAlign align,
    required double maxWidth,
  }) {
    debugLayoutLineCalls++;
    final line = _expandTabStops(
      _getTextSpanForLine(lineText, lineIndex, unifiedTextSpan),
    );
    final tp = TextPainter(
      text: line.span,
      textAlign: align,
      textDirection: TextDirection.ltr,
    );
    if (line.tabStops.isNotEmpty) {
      tp.setPlaceholderDimensions(line.tabStops);
    }
    tp.layout(maxWidth: maxWidth);
    return tp;
  }

  // SWT lays a line out with a repeating tab stop every `tabs` spaces wide
  // (SwtStyledTextRenderer: tabWidth = width of `tabs` spaces), so a tab advances to
  // the next multiple of that width from the line start — a tab after "ab" moves 2
  // columns, not 4. Flutter's TextPainter has no tab stops, so each '\t' is replaced
  // by a zero-height placeholder sized to reach the next stop. A placeholder occupies
  // exactly one UTF-16 code unit (U+FFFC) just like the '\t' it stands in for, which
  // keeps getOffsetForCaret/getPositionForOffset offsets identical to document offsets.
  _TabExpandedLine _expandTabStops(TextSpan lineSpan) {
    final leaves = <_StyledRun>[];
    _flattenSpan(lineSpan, style, leaves);
    if (!leaves.any((leaf) => leaf.text.contains('\t'))) {
      return _TabExpandedLine(lineSpan, const []);
    }

    final tabWidth = _tabStopWidth();
    final children = <InlineSpan>[];
    final tabStops = <PlaceholderDimensions>[];
    double x = 0;

    for (final leaf in leaves) {
      final parts = leaf.text.split('\t');
      for (int i = 0; i < parts.length; i++) {
        if (i > 0) {
          // Epsilon guards against a run measured a hair under an exact stop, which
          // would otherwise make the tab advance ~0 instead of a full column.
          final nextStop = (((x + 0.01) / tabWidth).floor() + 1) * tabWidth;
          tabStops.add(
            PlaceholderDimensions(
              size: Size(nextStop - x, 0),
              alignment: PlaceholderAlignment.baseline,
              baseline: TextBaseline.alphabetic,
              baselineOffset: 0,
            ),
          );
          children.add(_tabPlaceholder);
          x = nextStop;
        }
        if (parts[i].isEmpty) continue;
        children.add(TextSpan(text: parts[i], style: leaf.style));
        x += _measureRun(parts[i], leaf.style);
      }
    }

    return _TabExpandedLine(
      TextSpan(style: style, children: children),
      tabStops,
    );
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
  List<int>? _lineStartsCache;

  List<int> get _lineStarts {
    final cached = _lineStartsCache;
    if (cached != null) return cached;
    final starts = <int>[0];
    for (int i = 0; i < text.length; i++) {
      if (text.codeUnitAt(i) == 0x0A) starts.add(i + 1);
    }
    return _lineStartsCache = starts;
  }

  /// Style ranges bucketed by the lines they cover. Both the paint loop and the geometry
  /// table ask line by line, so scanning the whole range list per line costs the document
  /// its line count times its range count on every frame.
  List<List<StyleRange>>? _rangesByLineCache;

  List<List<StyleRange>> get _rangesByLine {
    final cached = _rangesByLineCache;
    if (cached != null) return cached;
    final starts = _lineStarts;
    final buckets = List.generate(starts.length, (_) => <StyleRange>[]);
    for (final range in editingState?.characterRanges ?? const <StyleRange>[]) {
      if (range.end <= range.start) continue;
      int first = _lineOfOffset(range.start);
      final last = _lineOfOffset(range.end - 1);
      for (; first <= last && first < buckets.length; first++) {
        buckets[first].add(range);
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

  void _drawCaret(Canvas c) {
    final rect = caretRect();
    if (rect == null) return;
    c.drawRect(
      rect,
      Paint()
        ..color = caretInfo!.color
        ..style = PaintingStyle.fill,
    );
  }

  /// Geometry of the painted caret, or null when no caret is visible.
  Rect? caretRect() {
    if (caretInfo == null || !caretInfo!.visible) return null;

    final caretOffset = caretInfo!.offset.clamp(0, text.length);
    final lines = text.split('\n');

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
    final maxW = _lineMaxWidth(props.indent);
    final currentY =
        off.dy + _lineTops(lines)[currentLineIndex] + props.vIndent;

    final currentLine = lines[currentLineIndex];
    final tp = _layoutLine(
      currentLine,
      currentLineIndex,
      TextSpan(text: text, style: style),
      align: props.align,
      maxWidth: maxW,
    );

    double finalX = off.dx + props.indent.toDouble();

    if (canvasSize != null && maxW != double.infinity) {
      switch (props.align) {
        case TextAlign.center:
          final textWidth = tp.width;
          if (textWidth < maxW) {
            finalX = off.dx + props.indent.toDouble() + (maxW - textWidth) / 2;
          }
          break;
        case TextAlign.right:
          final textWidth = tp.width;
          if (textWidth < maxW) {
            finalX = off.dx + props.indent.toDouble() + (maxW - textWidth);
          }
          break;
        default:
          break;
      }
    }

    final posInLine = caretPositionInLine.clamp(0, currentLine.length);
    final affinity = posInLine >= currentLine.length && currentLine.isNotEmpty
        ? TextAffinity.upstream
        : TextAffinity.downstream;

    final caretPosition = tp.getOffsetForCaret(
      TextPosition(offset: posInLine, affinity: affinity),
      Rect.fromLTWH(0, 0, tp.width, tp.height),
    );

    return Rect.fromLTWH(
      finalX + caretPosition.dx,
      currentY + _caretLineTop(tp, caretPosition.dy),
      caretInfo!.width,
      caretInfo!.height > 0 ? caretInfo!.height : (style.fontSize ?? 16) * 1.2,
    );
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
    double top = 0;
    for (final line in tp.computeLineMetrics()) {
      if (dy < top + line.height) return top;
      top += line.height;
    }
    return tp.computeLineMetrics().isEmpty ? dy : top;
  }

  void _drawSelection(Canvas c) {
    if (selectionInfo == null || !selectionInfo!.hasSelection) return;

    final startOffset = selectionInfo!.normalizedStart.clamp(0, text.length);
    final endOffset = selectionInfo!.normalizedEnd.clamp(0, text.length);

    if (startOffset == endOffset) return;

    final lines = text.split('\n');

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
        TextSpan(text: text, style: style),
        align: props.align,
        maxWidth: _lineMaxWidth(props.indent),
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

          c.drawRect(
            selectionRect,
            Paint()
              ..color = selectionInfo!.selectionColor.withOpacity(0.6)
              ..style = PaintingStyle.fill,
          );
        }
      }
    }
  }

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
      newEditingState = const TextEditingState(
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
    final lines = text.split('\n');
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
    final maxW = _lineMaxWidth(props.indent, canvasSize);

    final tp = _layoutLine(
      line,
      lineIndex,
      TextSpan(text: text, style: style),
      align: props.align,
      maxWidth: maxW,
    );

    double finalX = props.indent.toDouble();

    if (maxW != double.infinity) {
      switch (props.align) {
        case TextAlign.center:
          if (tp.width < maxW) {
            finalX = props.indent.toDouble() + (maxW - tp.width) / 2;
          }
          break;
        case TextAlign.right:
          if (tp.width < maxW) {
            finalX = props.indent.toDouble() + (maxW - tp.width);
          }
          break;
        default:
          break;
      }
    }

    final lineRelativePosition = Offset(
      relativePosition.dx - finalX,
      dy - tops[lineIndex] - props.vIndent,
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

  const StyleRange({
    required this.start,
    required this.end,
    required this.style,
  });
}

class SelectionInfo {
  final int start;
  final int end;
  final Color selectionColor;
  final bool isActive;

  SelectionInfo({
    required this.start,
    required this.end,
    this.selectionColor = const Color(0xFF3399FF),
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
    bool? isActive,
  }) {
    return SelectionInfo(
      start: start ?? this.start,
      end: end ?? this.end,
      selectionColor: selectionColor ?? this.selectionColor,
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

  const TextEditingState({
    this.characterRanges = const [],
    this.lineProperties = const {},
  });

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

  const LineProperties({
    this.alignment,
    this.indent,
    this.justify,
    this.verticalIndent,
  });

  LineProperties copyWith({
    int? alignment,
    int? indent,
    bool? justify,
    int? verticalIndent,
  }) {
    return LineProperties(
      alignment: alignment ?? this.alignment,
      indent: indent ?? this.indent,
      justify: justify ?? this.justify,
      verticalIndent: verticalIndent ?? this.verticalIndent,
    );
  }
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

    final lines = text.split('\n');
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
