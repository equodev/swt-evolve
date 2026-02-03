import 'dart:async';
import 'dart:math' as math;
import 'package:flutter/services.dart';
import 'package:flutter/widgets.dart';
import '../comm/comm.dart';
import '../gen/event.dart';
import '../gen/color.dart';
import '../gen/stylerange.dart';
import '../gen/styledtext.dart';
import '../gen/styledtextrenderer.dart';
import '../gen/widget.dart';
import '../impl/canvas_evolve.dart';
import 'widget_config.dart';
import 'utils/font_utils.dart';
import 'color_utils.dart';

class StyledTextImpl<T extends StyledTextSwt, V extends VStyledText>
    extends CanvasImpl<T, V> {
  TextShape? _editableTextShape;
  bool _isEditingText = false;
  Timer? _caretBlinkTimer;

  TextEditingState? _localEditingState;
  TextShape? _originalServerTextShape;
  bool _isInLocalEditMode = false;

  bool _isSelecting = false;
  int? _selectionStartOffset;

  bool _editable = true;
  bool _wordWrap = true;

  final bool useDarkTheme = getCurrentTheme();

  @override
  Color get bg => colorFromVColor(state.background, defaultColor: const Color(0x00000000));

  @override
  void initState() {
    super.initState();
  }

  @override
  void extraSetState() {
    super.extraSetState();
    _editable = state.editable ?? true;  // SWT default is editable
    _wordWrap = state.wordWrap ?? true;  // SWT default with WRAP style
    _buildTextShapeFromState();
  }

  /// Build TextShape from serialized state data
  void _buildTextShapeFromState() {
    final originalText = state.text ?? '';
    final text = originalText.replaceAll('\r\n', '\n').replaceAll('\r', '\n');
    if (text.isEmpty && state.renderer == null) return;

    final styledTextId = state.id;
    final caretOffset = _adjustOffsetForNormalizedText(originalText, state.caretOffset ?? 0);

    // Build editing state from renderer style ranges
    final editingState = _buildEditingStateFromRenderer();

    // Get default text style
    final defaultStyle = _getDefaultTextStyle();

    // Build caret info
    final caretColor = useDarkTheme ? const Color(0xFFFFFFFF) : applyAlpha(fg);
    final caretHeight = (defaultStyle.fontSize ?? 12.0) * 1.2;
    final caretInfo = CaretInfo(
      offset: caretOffset,
      width: 1.0,
      height: caretHeight,
      color: caretColor,
      visible: true,
      blinking: true,
      styledTextId: styledTextId,
      blinkRate: 500,
    );

    final textShape = TextShape(
      text,
      const Offset(0, 0),
      defaultStyle,
      clipRect,
      null,
      caretInfo,
      _wordWrap,
      getBounds(),
      _editable,
      styledTextId,
      (newText, caretPos) {},
      editingState,
      null,
    );

    // Update shapes list - remove old shape with same id and add new one
    shapes = shapes.where((shape) {
      return !(shape is TextShape && shape.styledTextId == styledTextId);
    }).toList();

    // Update editable text shape if we're editing this one, or add to shapes list
    if (_isEditingText && _editableTextShape?.styledTextId == styledTextId) {
      if (!_isInLocalEditMode) {
        _editableTextShape = textShape;
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
    final maxIndex = offset < originalText.length ? offset : originalText.length;
    for (int i = 0; i < maxIndex; i++) {
      if (originalText[i] == '\r' && i + 1 < originalText.length && originalText[i + 1] == '\n') {
        adjustment++;
      }
    }
    return offset - adjustment;
  }

  /// Build TextEditingState from renderer's styleRanges and lineProperties
  TextEditingState _buildEditingStateFromRenderer() {
    final renderer = state.renderer;
    final originalText = state.text ?? '';
    final normalizedText = originalText.replaceAll('\r\n', '\n').replaceAll('\r', '\n');

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

        final adjustedStart = _adjustOffsetForNormalizedText(originalText, start);
        final adjustedEnd = _adjustOffsetForNormalizedText(originalText, start + length);
        characterRanges.add(StyleRange(
          start: adjustedStart,
          end: adjustedEnd,
          style: style,
        ));
      }
    } else if (normalizedText.isNotEmpty) {
      // Default range for entire text
      final defaultStyle = _getDefaultTextStyle();
      characterRanges.add(StyleRange(
        start: 0,
        end: normalizedText.length,
        style: defaultStyle,
      ));
    }

    // Sort ranges by start position
    characterRanges.sort((a, b) => a.start.compareTo(b.start));

    if (characterRanges.isNotEmpty) {
      List<StyleRange> dedupedRanges = [];
      for (final range in characterRanges) {
        bool isDuplicate = dedupedRanges.any((existing) =>
            existing.start <= range.start && existing.end >= range.end);
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
    final defaultTextColor = useDarkTheme ? const Color(0xFFFFFFFF) : applyAlpha(fg);
    final baseStyle = _getDefaultTextStyle();

    Color? foreground;
    if (vRange.foreground != null) {
      foreground = colorFromVColor(vRange.foreground);
    }

    Color? background;
    if (vRange.background != null) {
      background = colorFromVColor(vRange.background);
    }

    final (fontWeight, fontStyle) = FontUtils.convertSwtFontStyle(vRange.fontStyle);

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
    final defaultTextColor = useDarkTheme ? const Color(0xFFFFFFFF) : applyAlpha(fg);

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

    return TextStyle(
      fontSize: 12,
      color: defaultTextColor,
    );
  }

  @override
  Widget build(BuildContext context) {
    final bounds = getBounds();

    return SizedBox(
      width: bounds.width,
      height: bounds.height,
      child: Stack(
        children: [
          Positioned.fill(
            child: CustomPaint(
              painter: ScenePainter(
                  bg,
                  List.unmodifiable([
                    ...shapes,
                    if (_editableTextShape != null) _editableTextShape!
                  ])),
            ),
          ),
          Positioned.fill(
            child: RawKeyboardListener(
              focusNode: FocusNode(),
              autofocus: _isEditingText,
              onKey: _handleKeyEvent,
              child: GestureDetector(
                onTapDown: (details) => _handleTapDown(details.localPosition),
                onTapUp: (details) => _handleTapUp(details.localPosition),
                onPanStart: (details) => _handlePanStart(details.localPosition),
                onPanUpdate: (details) => _handlePanUpdate(details.localPosition),
                onPanEnd: (details) => _handlePanEnd(),
                behavior: HitTestBehavior.translucent,
                child: Container(),
              ),
            ),
          ),
        ],
      ),
    );
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

  //-----------Edition----------------
  void _handleTap(Offset position) {
    final RenderBox? renderBox = context.findRenderObject() as RenderBox?;
    if (renderBox == null) return;

    final canvasSize = renderBox.size;

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

    if (tappedTextShape != null && shapeIndex != null) {
      final caretOffset =
          tappedTextShape.getOffsetFromPosition(position, canvasSize);

      if (!_isEditingText ||
          _editableTextShape?.styledTextId != tappedTextShape.styledTextId) {
        if (_isEditingText) {
          _stopEditing();
        }
        _startEditing(tappedTextShape, shapeIndex);
      }

      setState(() {
        _editableTextShape =
            _editableTextShape!.clearSelection().moveCaret(caretOffset);
      });

      _startCaretBlinking();
    } else {
      bool clickedInEditableText = false;

      if (_isEditingText && _editableTextShape != null) {
        if (_editableTextShape!.containsPoint(position, canvasSize)) {
          clickedInEditableText = true;

          final caretOffset =
              _editableTextShape!.getOffsetFromPosition(position, canvasSize);

          setState(() {
            _editableTextShape =
                _editableTextShape!.clearSelection().moveCaret(caretOffset);

            _isSelecting = false;
            _selectionStartOffset = null;
          });

          _startCaretBlinking();
        }
      }

      if (!clickedInEditableText && _isEditingText) {
        _stopEditing();
      }
    }
  }

  void _handleKeyEvent(RawKeyEvent event) {
    if (!_isEditingText || _editableTextShape == null) return;
    if (event is! RawKeyDownEvent) return;

    final isShiftPressed = event.data.isShiftPressed;

    setState(() {
      final currentShape = _editableTextShape!;
      TextShape? newShape;

      if (event.logicalKey == LogicalKeyboardKey.backspace) {
        if (!_editable) return;
        newShape = currentShape.backspace();
      } else if (event.logicalKey == LogicalKeyboardKey.delete) {
        if (!_editable) return;
        newShape = currentShape.delete();
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
        final newOffset = _calculateLineBasedVerticalNavigation(currentShape, -1);
        if (isShiftPressed) {
          newShape = currentShape.extendSelectionTo(newOffset);
        } else {
          newShape = currentShape.clearSelection();
        }
        newShape = (newShape ?? currentShape).moveCaret(newOffset);
      } else if (event.logicalKey == LogicalKeyboardKey.arrowDown) {
        final newOffset = _calculateLineBasedVerticalNavigation(currentShape, 1);
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
          event.data.isControlPressed) {
        newShape = currentShape.selectAll();
      } else if (event.logicalKey == LogicalKeyboardKey.keyC &&
          event.data.isControlPressed) {
        final selectedText = currentShape.getSelectedText();
        if (selectedText.isNotEmpty) {
          Clipboard.setData(ClipboardData(text: selectedText));
        }
        return;
      } else if (event.logicalKey == LogicalKeyboardKey.keyX &&
          event.data.isControlPressed) {
        final selectedText = currentShape.getSelectedText();
        if (selectedText.isNotEmpty) {
          Clipboard.setData(ClipboardData(text: selectedText));
          if (_editable) {
            newShape = currentShape.deleteSelection();
          }
        }
      } else if (event.logicalKey == LogicalKeyboardKey.keyV &&
          event.data.isControlPressed) {
        if (_editable) {
          _handlePaste();
        }
        return;
      } else if (event.logicalKey == LogicalKeyboardKey.escape) {
        newShape = currentShape.clearSelection();
      } else if (event.logicalKey == LogicalKeyboardKey.enter) {
        if (!_editable) return;
        newShape = currentShape.insertText(
            '\n', currentShape.caretInfo?.offset ?? 0);
      } else if (event.character != null && event.character!.isNotEmpty) {
        if (!_editable) return;
        final char = event.character!;
        if (char.codeUnitAt(0) >= 32) {
          newShape = currentShape.insertText(
              char, currentShape.caretInfo?.offset ?? 0);
        }
      }

      if (newShape != null) {
        _editableTextShape = newShape;

        if (_isInLocalEditMode && newShape.editingState != null) {
          _localEditingState = newShape.editingState;
        }
      }
    });
  }

  void _handlePaste() async {
    if (!_isEditingText || _editableTextShape == null || !_editable) return;

    try {
      final data = await Clipboard.getData('text/plain');
      if (data?.text != null && _editableTextShape != null) {
        setState(() {
          _editableTextShape = _editableTextShape!.insertText(
              data!.text!, _editableTextShape!.caretInfo?.offset ?? 0);

          if (_isInLocalEditMode && _editableTextShape!.editingState != null) {
            _localEditingState = _editableTextShape!.editingState;
          }
        });
      }
    } catch (e) {
      // Error pasting
    }
  }

  void _handleTapDown(Offset position) {
    _isSelecting = false;
  }

  void _handleTapUp(Offset position) {
    if (!_isSelecting) {
      _handleTap(position);
    }
    _isSelecting = false;
    _selectionStartOffset = null;
  }

  void _handlePanStart(Offset position) {
    final RenderBox? renderBox = context.findRenderObject() as RenderBox?;
    if (renderBox == null) return;

    final canvasSize = renderBox.size;
    TextShape? textShape;

    if (_isEditingText && _editableTextShape != null) {
      if (_editableTextShape!.containsPoint(position, canvasSize)) {
        textShape = _editableTextShape!;
      }
    }

    textShape ??= _findEditableTextShapeAtPosition(position, canvasSize);

    if (textShape != null) {
      _isSelecting = true;
      _selectionStartOffset =
          textShape.getOffsetFromPosition(position, canvasSize);

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
    final currentOffset =
        _editableTextShape!.getOffsetFromPosition(position, canvasSize);

    setState(() {
      final selection =
          SelectionInfo.fromRange(_selectionStartOffset!, currentOffset);
      _editableTextShape = _editableTextShape!.copyWithSelection(selection);

      final caretPos = currentOffset;
      _editableTextShape = _editableTextShape!.updateCaretOffset(caretPos);
    });
  }

  void _handlePanEnd() {
    _isSelecting = false;
    _selectionStartOffset = null;
    _sendSelectionChange();
  }

  void _startCaretBlinking() {
    _caretBlinkTimer?.cancel();
    _caretBlinkTimer =
        Timer.periodic(const Duration(milliseconds: 500), (timer) {
      if (_editableTextShape?.caretInfo != null) {
        setState(() {
          final caret = _editableTextShape!.caretInfo!;
          final newCaret = caret.copyWith(visible: !caret.visible);
          _editableTextShape = _editableTextShape!.copyWithCaret(newCaret);

          final index = shapes.indexWhere(
              (s) => s is TextShape && s.styledTextId == caret.styledTextId);
          if (index != -1) {
            shapes[index] =
                (shapes[index] as TextShape).copyWithCaret(newCaret);
          }
        });
      }
    });
  }

  void _startEditing(TextShape textShape, int shapeIndex) {
    _enterLocalEditMode(textShape);
    setState(() {
      _isEditingText = true;
      if (shapeIndex >= 0 && shapeIndex < shapes.length) {
        shapes.removeAt(shapeIndex);
      }
    });
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
          textShape.textSpan!, characterRanges, 0);
    } else {
      final defaultTextColor =
          useDarkTheme ? const Color(0xFFFFFFFF) : textShape.style.color;

      characterRanges.add(StyleRange(
        start: 0,
        end: textShape.text.length,
        style: textShape.style.copyWith(color: defaultTextColor),
      ));
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
        ranges.add(StyleRange(
          start: currentOffset,
          end: currentOffset + textLength,
          style: span.style!,
        ));
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
      (newText, caretOffset) {},
      _localEditingState,
      null,
    );
  }

  void _cleanupLocalEditMode() {
    _isInLocalEditMode = false;
    _originalServerTextShape = null;
    _localEditingState = null;
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
      EquoCommService.sendPayload("${state.swt}/${state.id}/StateUpdate", updatedState);
    }

    _cleanupLocalEditMode();

    setState(() {
      _isEditingText = false;

      if (_editableTextShape != null) {
        shapes.add(_editableTextShape!);
        _editableTextShape = null;
      }
    });

    _caretBlinkTimer?.cancel();
  }

  TextShape? _findEditableTextShapeAtPosition(
      Offset position, Size canvasSize) {
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
  int _calculateLineBasedVerticalNavigation(TextShape textShape, int direction) {
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
        isHome: isHome, canvasSize: getBounds());
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
  ]);

  @override
  void draw(Canvas c) {
    if (clipRect != null) {
      c.save();
      c.clipRect(clipRect!);
    }

    TextSpan effectiveTextSpan;

    if (editingState != null) {
      effectiveTextSpan =
          TextRenderer.buildFinalTextSpan(text, editingState!, style);
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
              finalX = paintOffset.dx +
                  indent.toDouble() +
                  (availableWidth - textWidth) / 2;
            }
            break;
          case TextAlign.right:
            final availableWidth = maxW;
            final textWidth = tp.width;
            if (textWidth < availableWidth) {
              finalX = paintOffset.dx +
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
      currentY += tp.height;
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
      String lineText, int lineIndex, TextSpan unifiedTextSpan) {
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
      String lineText, List<StyleRange> ranges) {
    if (ranges.isEmpty) {
      return TextSpan(text: lineText, style: style);
    }

    ranges.sort((a, b) => a.start.compareTo(b.start));

    List<TextSpan> children = [];
    int currentPos = 0;

    for (final range in ranges) {
      if (currentPos < range.start) {
        children.add(TextSpan(
          text: lineText.substring(currentPos, range.start),
          style: style,
        ));
      }

      children.add(TextSpan(
        text: lineText.substring(range.start, range.end),
        style: range.style,
      ));

      currentPos = range.end;
    }

    if (currentPos < lineText.length) {
      children.add(TextSpan(
        text: lineText.substring(currentPos),
        style: style,
      ));
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
    if (caretInfo == null || !caretInfo!.visible || text.isEmpty) return;

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
    if (canvasSize != null) {
      maxW = canvasSize!.width - off.dx - indent.toDouble();
      if (maxW <= 0) maxW = double.infinity;
    }

    double currentY = off.dy;
    for (int i = 0; i < currentLineIndex; i++) {
      final prevLine = lines[i];
      final prevLineTextSpan =
          _getTextSpanForLine(prevLine, i, TextSpan(text: text, style: style));

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
      if (canvasSize != null) {
        prevMaxW = canvasSize!.width - off.dx - prevIndent.toDouble();
        if (prevMaxW <= 0) prevMaxW = double.infinity;
      }

      final prevTp = TextPainter(
        text: prevLineTextSpan,
        textAlign: prevEffectiveAlign,
        textDirection: TextDirection.ltr,
      );

      prevTp.layout(maxWidth: prevMaxW);
      currentY += prevTp.height;
    }

    final currentLine = lines[currentLineIndex];
    final lineTextSpan = _getTextSpanForLine(
        currentLine, currentLineIndex, TextSpan(text: text, style: style));

    final tp = TextPainter(
      text: lineTextSpan,
      textAlign: effectiveAlign,
      textDirection: TextDirection.ltr,
    );

    tp.layout(maxWidth: maxW);

    double finalX = off.dx + indent.toDouble();

    final caretPosition = tp.getOffsetForCaret(
      TextPosition(offset: caretPositionInLine.clamp(0, currentLine.length)),
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
          line, lineIndex, TextSpan(text: text, style: style));
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

      currentY += tp.height;
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
        selection);
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

  TextShape copyWithText(String newText, int caretOffset,
      [TextEditingState? newEditingState]) {
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
        selectionInfo);
  }

  TextShape updateCaretOffset(int offset) {
    if (caretInfo == null) return this;
    return copyWithCaret(caretInfo!.copyWith(offset: offset));
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
        final afterDelete = TextEditor.deleteText(text, start, end, editingState!);
        newEditingState = TextEditor.insertText(
          text.substring(0, start) + text.substring(end),
          insertText,
          start,
          afterDelete,
        );
      } else {
        newEditingState =
            TextEditor.insertText(text, insertText, position, editingState!);
      }
    } else {
      final currentStyle = _getStyleAtPosition(insertPosition);

      newEditingState = TextEditingState(
        characterRanges: [
          if (insertPosition > 0) StyleRange(start: 0, end: insertPosition, style: style),
          StyleRange(
              start: insertPosition,
              end: insertPosition + insertText.length,
              style: currentStyle),
          if (insertPosition < text.length)
            StyleRange(
                start: insertPosition + insertText.length,
                end: newText.length,
                style: style),
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
    return useDarkTheme ? style.copyWith(color: const Color(0xFFFFFFFF)) : style;
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
      newEditingState =
          TextEditor.deleteText(text, actualStart, actualEnd, editingState!);
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

      final lineTextSpan =
          _getTextSpanForLine(line, i, TextSpan(text: text, style: style));

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

      if (relativePosition.dy >= currentY &&
          relativePosition.dy < currentY + tp.height) {
        final lineRelativeX = relativePosition.dx - finalX;
        final lineRelativePosition =
            Offset(lineRelativeX, relativePosition.dy - currentY);

        final textPosition = tp.getPositionForOffset(lineRelativePosition);
        final offsetInLine = textPosition.offset.clamp(0, line.length);

        return (globalOffset + offsetInLine).clamp(0, text.length);
      }

      currentY += tp.height;
      globalOffset += line.length;

      if (i < lines.length - 1) {
        globalOffset += 1;
      }
    }

    return text.length;
  }

  bool containsPoint(Offset point, Size canvasSize) {
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

    final textRect = Rect.fromLTWH(
      off.dx,
      off.dy,
      textWidth,
      textHeight,
    );

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

    final newPosition =
        tp.getPositionForOffset(Offset(currentPosition.dx, clampedY));
    return newPosition.offset.clamp(0, text.length);
  }

  int calculateLineNavigation(
      {required bool isHome, required Size canvasSize}) {
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
    final targetPosition =
        tp.getPositionForOffset(Offset(targetX, currentPosition.dy));

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
    this.blinkRate = 500,
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

  const LineProperties({
    this.alignment,
    this.indent,
    this.justify,
  });

  LineProperties copyWith({
    int? alignment,
    int? indent,
    bool? justify,
  }) {
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
              (range) => _rangeOverlapsLine(range, currentOffset, lineLength))
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
      StyleRange range, int lineStart, int lineLength) {
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
        .map((range) => StyleRange(
              start: math.max(0, range.start - lineStartOffset),
              end: math.min(lineText.length, range.end - lineStartOffset),
              style: range.style,
            ))
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
        children.add(TextSpan(
          text: text.substring(currentPos, range.start),
          style: defaultStyle,
        ));
      }

      children.add(TextSpan(
        text: text.substring(range.start, range.end),
        style: range.style,
      ));

      currentPos = range.end;
    }

    if (currentPos < text.length) {
      children.add(TextSpan(
        text: text.substring(currentPos),
        style: defaultStyle,
      ));
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
    final deletedLineCount =
        '\n'.allMatches(originalText.substring(deleteStart, deleteEnd)).length;
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
