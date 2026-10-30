import 'dart:math' as math;

import 'package:flutter/foundation.dart';
import 'package:flutter/services.dart';
import 'package:flutter/widgets.dart';

/// Undo of SWT's Text as the control under it on the user's platform keeps it. SWT adds no undo of
/// its own, so a Text's undo is whatever that control does:
///
/// * [cocoa]: NSTextView and the field editor of NSTextField record into their window's undo
///   manager. Steps replace a range by position and are unlimited; typing coalesces into one step.
/// * [win32]: the EDIT control keeps one level, and undoing it twice redoes it.
/// * [none]: GTK 3's entry and text view have no undo.
enum TextUndoFlavor {
  cocoa,
  win32,
  none;

  /// The platform the user is on: on the web, the browser's.
  static TextUndoFlavor get current => switch (defaultTargetPlatform) {
        TargetPlatform.macOS || TargetPlatform.iOS => cocoa,
        TargetPlatform.windows => win32,
        TargetPlatform.linux || TargetPlatform.android || TargetPlatform.fuchsia => none,
      };
}

/// What made an edit, as far as undo tells edits apart.
enum TextEditKind {
  /// Characters typed, a line break, or what an input method composed.
  typing,

  /// The character before the caret, or the selection, deleted with the backspace key.
  backspace,

  /// The selection cut to the clipboard.
  cut,

  /// Anything else: pasting, deleting forward or by word or line, transposing.
  other,
}

/// What Java did to a Text's contents through its API, as the codes Java sends them under.
enum TextApiEdit {
  setText(1),
  append(2),
  insert(3),
  paste(4),
  cut(5);

  const TextApiEdit(this.code);

  final int code;

  static TextApiEdit? of(int code) {
    for (final edit in values) {
      if (edit.code == code) return edit;
    }
    return null;
  }
}

/// A text field whose edits undo records.
abstract interface class UndoableTextField {
  String get undoText;

  bool get undoSingleLine;

  bool get undoFocused;

  bool get undoReadOnly;

  /// False once the field is gone; what is recorded for it is then skipped.
  bool get undoAlive;

  /// Replaces [start, end) with [replacement] and selects [selection], as an edit of the user's.
  void applyUndo(int start, int end, String replacement, TextSelection selection);
}

/// The undo of a field: where its edits are recorded and what its undo keys do.
abstract class TextUndo {
  /// Records an edit the user made: [deleted] at [start] replaced by [inserted]. [fromCaret] is
  /// whether nothing was selected before it.
  void edited(UndoableTextField field, int start, String deleted, String inserted, TextEditKind kind,
      {required bool fromCaret});

  /// Records what Java did through the API.
  void api(UndoableTextField field, TextApiEdit edit, int start, String deleted, String inserted);

  void focused(UndoableTextField field) {}

  void undo(UndoableTextField field);

  void redo(UndoableTextField field);

  /// The undo [field] takes part in, under [flavor]: Cocoa's is its window's, shared with every
  /// other field in it; Win32 keeps one per field.
  static TextUndo of(UndoableTextField field, Object? window, TextUndoFlavor flavor) {
    switch (flavor) {
      case TextUndoFlavor.cocoa:
        final owner = window ?? field;
        return _cocoa[owner] ??= CocoaUndo();
      case TextUndoFlavor.win32:
        return _win32[field] ??= Win32Undo();
      case TextUndoFlavor.none:
        return const NoUndo();
    }
  }

  static final Expando<CocoaUndo> _cocoa = Expando('Cocoa undo');
  static final Expando<Win32Undo> _win32 = Expando('Win32 undo');
}

class NoUndo implements TextUndo {
  const NoUndo();

  @override
  void edited(UndoableTextField field, int start, String deleted, String inserted, TextEditKind kind,
      {required bool fromCaret}) {}

  @override
  void api(UndoableTextField field, TextApiEdit edit, int start, String deleted, String inserted) {}

  @override
  void focused(UndoableTextField field) {}

  @override
  void undo(UndoableTextField field) {}

  @override
  void redo(UndoableTextField field) {}
}

/// One undo step: replace [length] characters at [start] of its target with [replacement].
class _Step {
  _Step(this.target, this.start, this.length, this.replacement, {this.typing = false});

  /// The multi-line field the step edits, or [CocoaUndo.fieldEditor] for whichever single-line
  /// field is being edited when it is undone.
  final Object target;
  int start;
  int length;
  String replacement;

  /// Whether typing may still extend it.
  final bool typing;
}

/// A window's undo manager, as NSTextView and the field editor use it.
class CocoaUndo extends TextUndo {
  /// The window's field editor, which every single-line field edits through.
  static final Object fieldEditor = Object();

  final List<_Step> _undo = [];
  final List<_Step> _redo = [];

  /// Set by anything that ends the typing step on top, so the next keystroke starts another.
  bool _broken = false;

  @visibleForTesting
  int get undoCount => _undo.length;

  @visibleForTesting
  int get redoCount => _redo.length;

  static Object _targetOf(UndoableTextField field) => field.undoSingleLine ? fieldEditor : field;

  @override
  void edited(UndoableTextField field, int start, String deleted, String inserted, TextEditKind kind,
      {required bool fromCaret}) {
    if (field.undoSingleLine && kind == TextEditKind.cut) {
      // Cutting in a field editor drops every step of the window.
      _clear();
      return;
    }
    final target = _targetOf(field);
    final typing = kind == TextEditKind.typing || kind == TextEditKind.backspace;
    final end = start + deleted.length;
    final top = _undo.isEmpty ? null : _undo.last;
    if (typing && fromCaret && !_broken && top != null && top.typing && identical(top.target, target) &&
        end == top.start + top.length) {
      if (start >= top.start) {
        top.length = start - top.start + inserted.length;
      } else {
        // Deleting back past where the typing began: what was there before it is what undo restores.
        top.replacement = deleted.substring(0, top.start - start) + top.replacement;
        top.start = start;
        top.length = inserted.length;
      }
    } else {
      _undo.add(_Step(target, start, inserted.length, deleted, typing: typing));
    }
    _redo.clear();
    _broken = false;
  }

  @override
  void api(UndoableTextField field, TextApiEdit edit, int start, String deleted, String inserted) {
    final top = _undo.isEmpty ? null : _undo.last;
    if (top != null && identical(top.target, _targetOf(field))) _broken = true;
    if (field.undoSingleLine) {
      if (edit == TextApiEdit.cut || (edit == TextApiEdit.setText && field.undoFocused)) _clear();
      return;
    }
    if (edit == TextApiEdit.paste || edit == TextApiEdit.cut) {
      _undo.add(_Step(field, start, inserted.length, deleted));
      _redo.clear();
    }
  }

  /// A multi-line field becoming first responder drops every step of the window once a field editor
  /// has any.
  @override
  void focused(UndoableTextField field) {
    if (field.undoSingleLine) return;
    if (_undo.any((step) => identical(step.target, fieldEditor))) _clear();
  }

  @override
  void undo(UndoableTextField field) {
    if (_undo.isEmpty) return;
    _broken = true;
    final step = _undo.removeLast();
    final inverse = _apply(step, field, selectReplacement: true);
    if (inverse != null) _redo.add(inverse);
  }

  @override
  void redo(UndoableTextField field) {
    if (_redo.isEmpty) return;
    _broken = true;
    final step = _redo.removeLast();
    final inverse = _apply(step, field, selectReplacement: false);
    if (inverse != null) _undo.add(inverse);
  }

  /// Applies [step] where it lands, by position whatever the text has become since, and returns the
  /// step that reverses it; null when its target is gone, the step spent all the same.
  _Step? _apply(_Step step, UndoableTextField invoker, {required bool selectReplacement}) {
    final UndoableTextField? field;
    if (identical(step.target, fieldEditor)) {
      field = invoker.undoSingleLine ? invoker : null;
    } else {
      final target = step.target as UndoableTextField;
      field = target.undoAlive ? target : null;
    }
    if (field == null) return null;
    final text = field.undoText;
    final start = math.min(step.start, text.length);
    final end = math.min(step.start + step.length, text.length);
    final replaced = text.substring(start, end);
    final after = start + step.replacement.length;
    field.applyUndo(
      start,
      end,
      step.replacement,
      selectReplacement
          ? TextSelection(baseOffset: start, extentOffset: after)
          : TextSelection.collapsed(offset: after),
    );
    return _Step(step.target, start, step.replacement.length, replaced);
  }

  void _clear() {
    _undo.clear();
    _redo.clear();
  }
}

/// The EDIT control's undo buffer: the text the last run of edits deleted, and how much it
/// inserted where.
class Win32Undo extends TextUndo {
  String _deleted = '';
  int _position = 0;
  int _inserted = 0;

  bool get canUndo => _inserted != 0 || _deleted.isNotEmpty;

  @override
  void edited(UndoableTextField field, int start, String deleted, String inserted, TextEditKind kind,
          {required bool fromCaret}) =>
      _replace(start, start + deleted.length, deleted, inserted, canUndo: true);

  @override
  void api(UndoableTextField field, TextApiEdit edit, int start, String deleted, String inserted) {
    // setText writes the window text and append/insert replace without undo, all of which empty the
    // buffer; paste() and cut() are the control's own clipboard messages, which record.
    if (edit == TextApiEdit.paste || edit == TextApiEdit.cut) {
      _replace(start, start + deleted.length, deleted, inserted, canUndo: true);
    } else {
      _empty();
    }
  }

  /// EM_REPLACESEL: [deleted], at [start, end), replaced by [inserted].
  void _replace(int start, int end, String deleted, String inserted, {required bool canUndo}) {
    if (end != start) {
      if (canUndo) {
        if (_inserted == 0 && _deleted.isNotEmpty && start == _position) {
          _deleted = _deleted + deleted;
        } else if (_inserted == 0 && _deleted.isNotEmpty && end == _position) {
          _deleted = deleted + _deleted;
          _position = start;
        } else {
          _deleted = deleted;
          _position = start;
        }
        _inserted = 0;
      } else {
        _empty();
      }
    }
    if (inserted.isNotEmpty) {
      if (canUndo) {
        if (start == _position || (_inserted != 0 && start == _position + _inserted)) {
          _inserted += inserted.length;
        } else {
          _position = start;
          _inserted = inserted.length;
          _deleted = '';
        }
      } else {
        _empty();
      }
    }
  }

  void _empty() {
    _deleted = '';
    _inserted = 0;
  }

  /// EM_UNDO: what was inserted is replaced by what was deleted, which is then selected, and the
  /// buffer now holds the reverse, so undoing again redoes.
  @override
  void undo(UndoableTextField field) {
    if (field.undoReadOnly || !canUndo) return;
    final text = field.undoText;
    final start = math.min(_position, text.length);
    final end = math.min(_position + _inserted, text.length);
    final restored = _deleted;
    _empty();
    _replace(start, end, text.substring(start, end), restored, canUndo: true);
    field.applyUndo(start, end, restored, TextSelection(baseOffset: _position, extentOffset: _position + _inserted));
  }

  /// The EDIT control has no redo but undoing again.
  @override
  void redo(UndoableTextField field) {}
}

/// Undoes the focused field's last step.
class TextUndoIntent extends Intent {
  const TextUndoIntent();
}

/// Redoes the focused field's last undone step.
class TextRedoIntent extends Intent {
  const TextRedoIntent();
}

/// A key some other platform undoes with, which does nothing here.
class TextNoUndoIntent extends Intent {
  const TextNoUndoIntent();
}

/// The keys each platform's control undoes and redoes with. Every other platform's undo key is
/// claimed as well, so neither Flutter's own undo nor, on the web, the browser's acts on it.
Map<ShortcutActivator, Intent> textUndoShortcuts(TextUndoFlavor flavor) {
  const undo = TextUndoIntent(), redo = TextRedoIntent(), none = TextNoUndoIntent();
  final keys = <ShortcutActivator, Intent>{
    const SingleActivator(LogicalKeyboardKey.keyZ, control: true): none,
    const SingleActivator(LogicalKeyboardKey.keyZ, control: true, shift: true): none,
    const SingleActivator(LogicalKeyboardKey.keyY, control: true): none,
    const SingleActivator(LogicalKeyboardKey.keyZ, meta: true): none,
    const SingleActivator(LogicalKeyboardKey.keyZ, meta: true, shift: true): none,
  };
  switch (flavor) {
    case TextUndoFlavor.cocoa:
      keys[const SingleActivator(LogicalKeyboardKey.keyZ, meta: true)] = undo;
      keys[const SingleActivator(LogicalKeyboardKey.keyZ, meta: true, shift: true)] = redo;
    case TextUndoFlavor.win32:
      // Ctrl+Z is the control character the EDIT control undoes on, with or without shift, and
      // Alt+Backspace the system key it undoes on.
      keys[const SingleActivator(LogicalKeyboardKey.keyZ, control: true)] = undo;
      keys[const SingleActivator(LogicalKeyboardKey.keyZ, control: true, shift: true)] = undo;
      keys[const SingleActivator(LogicalKeyboardKey.backspace, alt: true)] = undo;
    case TextUndoFlavor.none:
      break;
  }
  return keys;
}

/// Watches the text editing intents a field's editor invokes, to tell what each edit was.
class TextEditKindDispatcher extends ActionDispatcher {
  /// What the intent being invoked edits as, while it runs.
  TextEditKind? current;

  /// A paste reads the clipboard first, so its edit arrives after the intent has returned.
  bool pastePending = false;

  @override
  Object? invokeAction(covariant Action<Intent> action, covariant Intent intent, [BuildContext? context]) {
    final previous = _enter(intent);
    try {
      return super.invokeAction(action, intent, context);
    } finally {
      current = previous;
    }
  }

  @override
  (bool, Object?) invokeActionIfEnabled(covariant Action<Intent> action, covariant Intent intent,
      [BuildContext? context]) {
    final previous = _enter(intent);
    try {
      return super.invokeActionIfEnabled(action, intent, context);
    } finally {
      current = previous;
    }
  }

  /// The intent an edit is made by is the outermost one: deleting a character is done by replacing
  /// it, an intent of its own.
  TextEditKind? _enter(Intent intent) {
    final previous = current;
    current ??= kindOf(intent);
    if (intent is PasteTextIntent) pastePending = true;
    return previous;
  }

  static TextEditKind? kindOf(Intent intent) => switch (intent) {
        DeleteCharacterIntent(forward: false) => TextEditKind.backspace,
        CopySelectionTextIntent(collapseSelection: true) => TextEditKind.cut,
        DirectionalTextEditingIntent() || PasteTextIntent() || ReplaceTextIntent() || TransposeCharactersIntent() =>
          TextEditKind.other,
        _ => null,
      };

  /// The key last pressed in the field, for the edits that reach it as a new value rather than
  /// through an intent: on the web, the browser deletes, cuts and pastes in its own input element.
  TextEditKind? _keyKind;

  void keyDown(KeyEvent event) {
    // A paste that found nothing to paste made no edit; the next one is this key's.
    pastePending = false;
    final keyboard = HardwareKeyboard.instance;
    final chord = keyboard.isControlPressed || keyboard.isMetaPressed;
    final key = event.logicalKey;
    _keyKind = switch (key) {
      LogicalKeyboardKey.keyX when chord => TextEditKind.cut,
      LogicalKeyboardKey.keyV when chord => TextEditKind.other,
      LogicalKeyboardKey.backspace when !chord && !keyboard.isAltPressed => TextEditKind.backspace,
      LogicalKeyboardKey.backspace || LogicalKeyboardKey.delete => TextEditKind.other,
      _ => null,
    };
  }

  /// What an edit that replaced [deleted] with [inserted] was: as the intent that made it said, or,
  /// for one the input method sent, by the key that made it or by its shape.
  TextEditKind kindOfEdit(String deleted, String inserted, {required bool composing, required bool fromCaret,
      required bool endsAtCaret}) {
    final byKey = _keyKind;
    _keyKind = null;
    final known = current;
    if (known != null) return known;
    if (pastePending) {
      pastePending = false;
      return TextEditKind.other;
    }
    if (byKey != null && !composing) return byKey;
    if (composing) return TextEditKind.typing;
    if (inserted.isEmpty) {
      return endsAtCaret && (!fromCaret || deleted.characters.length == 1)
          ? TextEditKind.backspace
          : TextEditKind.other;
    }
    return inserted.characters.length == 1 ? TextEditKind.typing : TextEditKind.other;
  }
}
