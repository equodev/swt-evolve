/// Strips accelerator characters (&) from text while preserving escaped ampersands (&&)
///
/// SWT uses & as accelerator characters for keyboard navigation, but in Flutter
/// we want to display the text without these special characters.
library;

import 'package:flutter/material.dart';
import 'package:flutter/rendering.dart';
import '../../gen/event.dart';
import '../../gen/point.dart';
import 'double_tap_detector.dart';

String stripAccelerators(String? text) {
  if (text == null || text.isEmpty) {
    return text ?? '';
  }
  // Replace escaped ampersands (&& -> temporary placeholder)
  String result = text.replaceAll('&&', '\u0001ESCAPED_AMP\u0001');
  // Remove single accelerator ampersands
  result = result.replaceAll('&', '');
  // Restore escaped ampersands
  result = result.replaceAll('\u0001ESCAPED_AMP\u0001', '&');
  return result;
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

class DoubleClickWordSelector extends StatefulWidget {
  final Widget child;
  final TextEditingController controller;
  final FocusNode? focusNode;
  final String text;
  final void Function(int start, int end) onWordSelected;
  final VoidCallback? onTripleTap;

  const DoubleClickWordSelector({
    super.key,
    required this.child,
    required this.controller,
    this.focusNode,
    required this.text,
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
        return textPos.offset.clamp(0, widget.text.length);
      }
    }
    return widget.controller.selection.baseOffset.clamp(0, widget.text.length);
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
      final len = widget.text.length;
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
      final (start, end) = getWordBoundaries(widget.text, offset);
      widget.controller.selection = TextSelection(baseOffset: start, extentOffset: end);
      widget.onWordSelected(start, end);
    });
  }

  @override
  Widget build(BuildContext context) =>
      Listener(onPointerDown: _trackTap, child: widget.child);
}

/// Splits "Label\tShortcut" into (label, shortcut).
/// If no \t, shortcut is null and label is the full stripped text.
({String label, String? shortcut}) splitMenuItemText(String? text) {
  if (text == null) return (label: '', shortcut: null);
  final idx = text.indexOf('\t');
  if (idx < 0) return (label: stripAccelerators(text), shortcut: null);
  return (
    label: stripAccelerators(text.substring(0, idx)),
    shortcut: text.substring(idx + 1),
  );
}
