import 'dart:async';
import 'dart:math' as math;
import 'package:flutter/gestures.dart';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:flutter/widgets.dart';
import '../comm/comm.dart';
import '../gen/event.dart';
import '../gen/color.dart';
import '../gen/stylerange.dart';
import '../gen/styledtext.dart';
import '../gen/styledtextrenderer.dart';
import '../gen/swt.dart';
import '../gen/widget.dart';
import '../impl/canvas_evolve.dart';
import '../impl/gcdrawer_evolve.dart';
import 'widget_config.dart';
import 'key_mapping.dart';
import 'utils/double_tap_detector.dart';
import 'utils/font_utils.dart';
import 'utils/text_utils.dart';
import 'utils/widget_utils.dart';
import 'color_utils.dart';
import '../theme/theme_extensions/scrolledcomposite_theme_extension.dart';
import '../theme/theme_extensions/styledtext_theme_extension.dart';

class StyledTextImpl<T extends StyledTextSwt, V extends VStyledText>
    extends CanvasImpl<T, V> {
  List<Shape> shapes = [];
  TextShape? _editableTextShape;
  bool _isEditingText = false;
  Timer? _caretBlinkTimer;

  TextEditingState? _localEditingState;
  TextShape? _originalServerTextShape;
  bool _isInLocalEditMode = false;
  bool _hasProgrammaticSelection = false;

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

  @override
  Color get bg =>
      getBackgroundColor(
        background: state.background,
        defaultColor: _styledTextTheme.backgroundColor,
      ) ??
      _styledTextTheme.backgroundColor;

  @override
  void initState() {
    super.initState();
    _verticalController = ScrollController();
    _horizontalController = ScrollController();
    _verticalController.addListener(_onVerticalScroll);
    _horizontalController.addListener(_onHorizontalScroll);
    EquoCommService.onRaw("${state.swt}/${state.id}/focusLost", (_) {
      if (_isEditingText) {
        _stopEditing();
      }
    });
  }

  @override
  void dispose() {
    _verticalController.dispose();
    _horizontalController.dispose();
    _focusNode.dispose();
    _caretBlinkTimer?.cancel();
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
      } else if (selectionFromState != null) {
        _editableTextShape = _editableTextShape!.copyWithSelection(selectionFromState);
        _hasProgrammaticSelection = true;
      } else if (_hasProgrammaticSelection) {
        // Java cleared the Find/Replace highlight (selectionFromState == null but we had one).
        _editableTextShape = _editableTextShape!.clearSelection();
        _hasProgrammaticSelection = false;
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
      fontFamily = fontData.name ?? fontFamily;
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
        onPointerDown: (e) => _handlePointerDown(e.localPosition),
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

  void _notifyTextChanged(String newText, int caretPos) {
    // Clear the GC overlay to remove placeholder
    clearGCShapes();

    final event = VEvent()
      ..text = newText
      ..start = caretPos;
    widget.sendModifyModify(state, event);
  }

  //-----------Edition----------------
  void _handleTap(Offset position) {
    final RenderBox? renderBox = context.findRenderObject() as RenderBox?;
    if (renderBox == null) return;

    final canvasSize = renderBox.size;
    position = _toContentPosition(position);

    TextShape? tappedTextShape;
    int? shapeIndex;

    for (int i = shapes.length - 1; i >= 0; i--) {
      final shape = shapes[i];
      if (shape is TextShape &&
          shape.editable &&
          shape.containsPoint(position, canvasSize)) {
        tappedTextShape = shape;
        shapeIndex = i;
        break;
      }
    }

    if (tappedTextShape == null && _editable) {
      if (_isEditingText && _editableTextShape != null && _editableTextShape!.editable) {
        tappedTextShape = _editableTextShape;
        shapeIndex = -1;
      } else {
        shapeIndex = shapes.lastIndexWhere((s) => s is TextShape && s.editable);
        if (shapeIndex != -1) {
          tappedTextShape = shapes[shapeIndex] as TextShape;
        }
      }
    }

    if (tappedTextShape != null && shapeIndex != null) {
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
      });
      _sendSelectionCleared(caretOffset);

      _onCaretMoved();
      _focusNode.requestFocus();
    } else {
      bool clickedInEditableText = false;

      if (_isEditingText && _editableTextShape != null) {
        if (_editableTextShape!.containsPoint(position, canvasSize)) {
          clickedInEditableText = true;

          final caretOffset = _editableTextShape!.getOffsetFromPosition(
            position,
            canvasSize,
          );

          setState(() {
            _editableTextShape = _editableTextShape!.clearSelection().moveCaret(
              caretOffset,
            );

            _isSelecting = false;
            _selectionStartOffset = null;
          });
          _sendSelectionCleared(caretOffset);

          _onCaretMoved();
        }
      }

      if (!clickedInEditableText && _isEditingText) {
        _stopEditing();
      }
      if (!clickedInEditableText) {
        widget.sendFocusFocusIn(state, null);
        _focusNode.requestFocus();
      }
    }
  }

  void _sendKeyDownEvent(RawKeyEvent event) {
    final vEvent = mapKeyEventToSwt(event);
    widget.sendKeyKeyDown(state, vEvent);
  }

  void _handleKeyEvent(RawKeyEvent event) {
    if (!_isEditingText || _editableTextShape == null) return;
    if (event is! RawKeyDownEvent) return;

    // Send KeyDown event to Java
    _sendKeyDownEvent(event);

    widget.sendVerifyKeyverifyKey(state, mapKeyEventToSwt(event));

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
        newShape = (newShape ?? currentShape).moveCaret(newOffset);
      } else if (event.logicalKey == LogicalKeyboardKey.arrowRight) {
        final newOffset = (currentShape.caretInfo?.offset ?? 0) + 1;
        if (isShiftPressed) {
          newShape = currentShape.extendSelectionTo(newOffset);
        } else {
          newShape = currentShape.clearSelection();
        }
        newShape = (newShape ?? currentShape).moveCaret(newOffset);
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
        newShape = (newShape ?? currentShape).moveCaret(newOffset);
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
        newShape = (newShape ?? currentShape).moveCaret(newOffset);
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
        newShape = (newShape ?? currentShape).moveCaret(newOffset);
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
        newShape = (newShape ?? currentShape).moveCaret(newOffset);
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
  static const _panSlop = 4.0;

  void _handlePointerDown(Offset position) {
    _isSelecting = false;
    _pointerDownPos = position;
    _lastTapCount = _tapDetector.registerTap(position: position);
    if (_lastTapCount == 2) _doubleTapPosition = position;
    if (_lastTapCount == 3) _tripleTapPosition = position;
  }

  void _handlePointerMove(Offset position) {
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

  void _handleDoubleTap() {
    final pos = _doubleTapPosition;
    if (pos == null) return;
    final RenderBox? rb = context.findRenderObject() as RenderBox?;
    if (rb == null) return;

    if (!_isEditingText || _editableTextShape == null) {
      final contentPos = _toContentPosition(pos);
      TextShape? textShape;
      int shapeIndex = -1;
      for (int i = shapes.length - 1; i >= 0; i--) {
        final s = shapes[i];
        if (s is TextShape && s.editable && s.containsPoint(contentPos, rb.size)) {
          textShape = s;
          shapeIndex = i;
          break;
        }
      }
      if (textShape == null && _editable) {
        shapeIndex = shapes.lastIndexWhere((s) => s is TextShape && s.editable);
        if (shapeIndex != -1) textShape = shapes[shapeIndex] as TextShape?;
      }
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
    final pos = _tripleTapPosition;
    if (pos == null) return;
    final RenderBox? rb = context.findRenderObject() as RenderBox?;
    if (rb == null) return;

    if (!_isEditingText || _editableTextShape == null) {
      final contentPos = _toContentPosition(pos);
      TextShape? textShape;
      int shapeIndex = -1;
      for (int i = shapes.length - 1; i >= 0; i--) {
        final s = shapes[i];
        if (s is TextShape && s.editable && s.containsPoint(contentPos, rb.size)) {
          textShape = s;
          shapeIndex = i;
          break;
        }
      }
      if (textShape == null && _editable) {
        shapeIndex = shapes.lastIndexWhere((s) => s is TextShape && s.editable);
        if (shapeIndex != -1) textShape = shapes[shapeIndex] as TextShape?;
      }
      if (textShape == null) return;
      if (_isEditingText) _stopEditing();
      _startEditing(textShape, shapeIndex);
      _focusNode.requestFocus();
      WidgetsBinding.instance.addPostFrameCallback((_) {
        if (mounted) _applyTripleClickSelectAll();
      });
      return;
    }

    _applyTripleClickSelectAll();
  }

  void _applyTripleClickSelectAll() {
    if (_editableTextShape == null) return;
    setState(() {
      _editableTextShape = _editableTextShape!.selectAll();
    });
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
    _enterLocalEditMode(textShape);
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

  void _updateLocalTextShape() {
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
      _originalServerTextShape!.caretInfo,
      _originalServerTextShape!.wordWrap,
      _originalServerTextShape!.canvasSize,
      _originalServerTextShape!.editable,
      _originalServerTextShape!.styledTextId,
      _notifyTextChanged,
      _localEditingState,
      null,
      _originalServerTextShape!.lineHeight,
    );
  }

  void _cleanupLocalEditMode() {
    _isInLocalEditMode = false;
    _originalServerTextShape = null;
    _localEditingState = null;
    _hasProgrammaticSelection = false;
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
  final Function(String, int)? onTextChanged;

  final TextEditingState? editingState;
  final SelectionInfo? selectionInfo;
  // Java-calculated line height (ascent + descent). When > 0, used for
  // currentY advancement so ruler positions stay in sync with Flutter rendering.
  final double lineHeight;

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
  ]);

  // Vertical advance for one logical line. Each logical line is painted by a single
  // TextPainter that may wrap into several visual lines (tpHeight is the wrapped total),
  // so advance by tpHeight; fall back to lineHeight as a floor (empty lines whose painter
  // measures ~0, or to keep a consistent single-line height).
  double _advance(double tpHeight) {
    final lh = lineHeight > 0 ? lineHeight : (style.fontSize ?? 16) * 1.2;
    return tpHeight > lh ? tpHeight : lh;
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

    for (int i = 0; i < lines.length; i++) {
      final line = lines[i];

      int indent = 0;
      int alignValue = 16384; // Default SWT.LEFT
      bool justify = false;

      if (editingState != null && editingState!.lineProperties.containsKey(i)) {
        final lineProps = editingState!.lineProperties[i]!;
        indent = lineProps.indent ?? 0;
        alignValue = lineProps.alignment ?? 16384;
        justify = lineProps.justify ?? false;
      }

      final align = _mapSwtAlignmentToTextAlign(alignValue);
      final effectiveAlign = justify ? TextAlign.justify : align;

      final lineTextSpan = _getTextSpanForLine(line, i, effectiveTextSpan);

      final tp = TextPainter(
        text: lineTextSpan,
        textAlign: effectiveAlign,
        textDirection: TextDirection.ltr,
      );

      double maxW = double.infinity;
      if (wordWrap == true && canvasSize != null) {
        maxW = canvasSize!.width - paintOffset.dx - indent.toDouble();
        if (maxW <= 0) maxW = double.infinity;
      }

      tp.layout(maxWidth: maxW);

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

      tp.paint(c, Offset(finalX, currentY));
      currentY += _advance(tp.height);
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

  TextSpan _buildLineTextSpanFromState(String lineText, int lineIndex) {
    if (editingState == null || editingState!.characterRanges.isEmpty) {
      return TextSpan(text: lineText, style: style);
    }

    int lineStartOffset = 0;
    final lines = text.split('\n');
    for (int i = 0; i < lineIndex; i++) {
      lineStartOffset += lines[i].length + 1;
    }

    final lineEndOffset = lineStartOffset + lineText.length;

    final lineRanges = editingState!.characterRanges.where((range) {
      return range.start < lineEndOffset && range.end > lineStartOffset;
    }).toList();

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
    if (caretInfo == null || !caretInfo!.visible) return;

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

    int indent = 0;
    int alignValue = 16384;
    bool justify = false;

    if (editingState != null &&
        editingState!.lineProperties.containsKey(currentLineIndex)) {
      final lineProps = editingState!.lineProperties[currentLineIndex]!;
      indent = lineProps.indent ?? 0;
      alignValue = lineProps.alignment ?? 16384;
      justify = lineProps.justify ?? false;
    }

    final align = _mapSwtAlignmentToTextAlign(alignValue);
    final effectiveAlign = justify ? TextAlign.justify : align;

    double maxW = double.infinity;
    if (wordWrap == true && canvasSize != null) {
      maxW = canvasSize!.width - off.dx - indent.toDouble();
      if (maxW <= 0) maxW = double.infinity;
    }

    double currentY = off.dy;
    for (int i = 0; i < currentLineIndex; i++) {
      final prevLine = lines[i];
      final prevLineTextSpan = _getTextSpanForLine(
        prevLine,
        i,
        TextSpan(text: text, style: style),
      );

      int prevIndent = 0;
      int prevAlignValue = 16384;
      bool prevJustify = false;

      if (editingState != null && editingState!.lineProperties.containsKey(i)) {
        final prevProps = editingState!.lineProperties[i]!;
        prevIndent = prevProps.indent ?? 0;
        prevAlignValue = prevProps.alignment ?? 16384;
        prevJustify = prevProps.justify ?? false;
      }

      final prevAlign = _mapSwtAlignmentToTextAlign(prevAlignValue);
      final prevEffectiveAlign = prevJustify ? TextAlign.justify : prevAlign;

      double prevMaxW = double.infinity;
      if (wordWrap == true && canvasSize != null) {
        prevMaxW = canvasSize!.width - off.dx - prevIndent.toDouble();
        if (prevMaxW <= 0) prevMaxW = double.infinity;
      }

      final prevTp = TextPainter(
        text: prevLineTextSpan,
        textAlign: prevEffectiveAlign,
        textDirection: TextDirection.ltr,
      );

      prevTp.layout(maxWidth: prevMaxW);
      currentY += _advance(prevTp.height);
    }

    final currentLine = lines[currentLineIndex];
    final lineTextSpan = _getTextSpanForLine(
      currentLine,
      currentLineIndex,
      TextSpan(text: text, style: style),
    );

    final tp = TextPainter(
      text: lineTextSpan,
      textAlign: effectiveAlign,
      textDirection: TextDirection.ltr,
    );

    tp.layout(maxWidth: maxW);

    double finalX = off.dx + indent.toDouble();

    if (canvasSize != null && maxW != double.infinity) {
      switch (effectiveAlign) {
        case TextAlign.center:
          final textWidth = tp.width;
          if (textWidth < maxW) {
            finalX = off.dx + indent.toDouble() + (maxW - textWidth) / 2;
          }
          break;
        case TextAlign.right:
          final textWidth = tp.width;
          if (textWidth < maxW) {
            finalX = off.dx + indent.toDouble() + (maxW - textWidth);
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

    final caretRect = Rect.fromLTWH(
      finalX + caretPosition.dx,
      currentY + caretPosition.dy,
      caretInfo!.width,
      caretInfo!.height > 0 ? caretInfo!.height : (style.fontSize ?? 16) * 1.2,
    );

    c.drawRect(
      caretRect,
      Paint()
        ..color = caretInfo!.color
        ..style = PaintingStyle.fill,
    );
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

    double currentY = off.dy;

    for (int lineIndex = 0; lineIndex <= endLineIndex; lineIndex++) {
      final line = lines[lineIndex];

      int indent = 0;
      int alignValue = 16384;
      bool justify = false;

      if (editingState != null &&
          editingState!.lineProperties.containsKey(lineIndex)) {
        final lineProps = editingState!.lineProperties[lineIndex]!;
        indent = lineProps.indent ?? 0;
        alignValue = lineProps.alignment ?? 16384;
        justify = lineProps.justify ?? false;
      }

      final align = _mapSwtAlignmentToTextAlign(alignValue);
      final effectiveAlign = justify ? TextAlign.justify : align;

      final lineTextSpan = _getTextSpanForLine(
        line,
        lineIndex,
        TextSpan(text: text, style: style),
      );
      final tp = TextPainter(
        text: lineTextSpan,
        textAlign: effectiveAlign,
        textDirection: TextDirection.ltr,
      );

      double maxW = double.infinity;
      if (canvasSize != null) {
        maxW = canvasSize!.width - off.dx - indent.toDouble();
        if (maxW <= 0) maxW = double.infinity;
      }

      tp.layout(maxWidth: maxW);

      if (lineIndex >= startLineIndex && lineIndex <= endLineIndex) {
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
              off.dx + indent.toDouble() + box.left,
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

      currentY += _advance(tp.height);
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
    );
  }

  TextShape updateCaretOffset(int offset) {
    if (caretInfo == null) return this;
    return copyWithCaret(caretInfo!.copyWith(offset: offset, visible: true));
  }

  TextShape insertText(String insertText, int position) {
    String newText;
    int newCaretPos;
    int insertPosition = position;

    if (selectionInfo != null && selectionInfo!.hasSelection) {
      final start = selectionInfo!.normalizedStart;
      final end = selectionInfo!.normalizedEnd;
      newText = text.substring(0, start) + insertText + text.substring(end);
      newCaretPos = start + insertText.length;
      insertPosition = start;
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

    onTextChanged?.call(newText, newCaretPos);

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

    onTextChanged?.call(newText, actualStart);

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

    double currentY = 0;
    int globalOffset = 0;

    for (int i = 0; i < lines.length; i++) {
      final line = lines[i];

      int indent = 0;
      int alignValue = 16384;
      bool justify = false;

      if (editingState != null && editingState!.lineProperties.containsKey(i)) {
        final lineProps = editingState!.lineProperties[i]!;
        indent = lineProps.indent ?? 0;
        alignValue = lineProps.alignment ?? 16384;
        justify = lineProps.justify ?? false;
      }

      final align = _mapSwtAlignmentToTextAlign(alignValue);
      final effectiveAlign = justify ? TextAlign.justify : align;

      final lineTextSpan = _getTextSpanForLine(
        line,
        i,
        TextSpan(text: text, style: style),
      );

      final tp = TextPainter(
        text: lineTextSpan,
        textAlign: effectiveAlign,
        textDirection: TextDirection.ltr,
      );

      double maxW = double.infinity;
      maxW = canvasSize.width - off.dx - indent.toDouble();
      if (maxW <= 0) maxW = double.infinity;

      tp.layout(maxWidth: maxW);

      double finalX = indent.toDouble();

      if (maxW != double.infinity) {
        switch (effectiveAlign) {
          case TextAlign.center:
            final availableWidth = maxW;
            final textWidth = tp.width;
            if (textWidth < availableWidth) {
              finalX = indent.toDouble() + (availableWidth - textWidth) / 2;
            }
            break;
          case TextAlign.right:
            final availableWidth = maxW;
            final textWidth = tp.width;
            if (textWidth < availableWidth) {
              finalX = indent.toDouble() + (availableWidth - textWidth);
            }
            break;
          case TextAlign.justify:
          case TextAlign.left:
          default:
            break;
        }
      }

      final advance = _advance(tp.height);
      if (relativePosition.dy >= currentY &&
          relativePosition.dy < currentY + advance) {
        final lineRelativeX = relativePosition.dx - finalX;
        final lineRelativePosition = Offset(
          lineRelativeX,
          relativePosition.dy - currentY,
        );

        final textPosition = tp.getPositionForOffset(lineRelativePosition);
        final offsetInLine = textPosition.offset.clamp(0, line.length);

        return (globalOffset + offsetInLine).clamp(0, text.length);
      }

      currentY += advance;
      globalOffset += line.length;

      if (i < lines.length - 1) {
        globalOffset += 1;
      }
    }

    return text.length;
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
      maxLines: wordWrap == true ? null : 1,
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

  const LineProperties({this.alignment, this.indent, this.justify});

  LineProperties copyWith({int? alignment, int? indent, bool? justify}) {
    return LineProperties(
      alignment: alignment ?? this.alignment,
      indent: indent ?? this.indent,
      justify: justify ?? this.justify,
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
