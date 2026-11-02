// A Text undoes as native SWT's does on each platform, the same steps replayed on the same fields
// in a shell and the text and selection compared after each:
//
// * macOS: every scenario recorded from native Text widgets (support/native_text_undo_cocoa.dart).
// * Windows: the EDIT control's single-level undo, as Wine's reimplementation of the control
//   (dlls/user32/edit.c) specifies it: what the last run of edits deleted and how much it inserted
//   where; undo swaps the two, selecting what it puts back, so undoing again redoes.
// * Linux: GTK 3's entry and text view have no undo.

import 'dart:math' as math;

import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/gen/display.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/shell.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/gen/text.dart';
import 'package:swtflutter/src/gen/widgets.dart' show WireKey;
import 'package:swtflutter/src/impl/display_evolve.dart';
import 'package:swtflutter/src/impl/focus_requests.dart';
import 'package:swtflutter/src/impl/utils/line_editor.dart';
import 'package:swtflutter/src/impl/utils/pending_text_echoes.dart';
import 'package:swtflutter/src/impl/utils/force_live.dart';
import 'package:swtflutter/src/impl/widget_config.dart';

import 'delivery/support/deliver.dart';
import 'support/native_text_undo_cocoa.dart';

typedef _Scenario = (List<String>, List<(String, List<(String, int, int)?>)>);

/// The EDIT control. Typing extends one insertion; a deletion right after it starts a new buffer.
const _win32 = <_Scenario>[
  (
    ['multi'],
    [
      ('t:abc', [('abc', 3, 3)]),
      ('ctrl:z', [('', 0, 0)]),
      ('ctrl:z', [('abc', 0, 3)]),
      ('ctrlsh:z', [('', 0, 0)]),
    ],
  ),
  (
    ['multi'],
    [
      ('t:abc', [('abc', 3, 3)]),
      ('k:left', [('abc', 2, 2)]),
      ('t:x', [('abxc', 3, 3)]),
      ('ctrl:z', [('abc', 2, 2)]),
      ('ctrl:z', [('abxc', 2, 3)]),
    ],
  ),
  (
    ['multi'],
    [
      ('t:abc', [('abc', 3, 3)]),
      ('k:bs', [('ab', 2, 2)]),
      ('ctrl:z', [('abc', 2, 3)]),
      ('ctrl:z', [('ab', 2, 2)]),
    ],
  ),
  (
    ['multi'],
    [
      ('t:ab', [('ab', 2, 2)]),
      ('k:bs', [('a', 1, 1)]),
      ('k:bs', [('', 0, 0)]),
      ('ctrl:z', [('ab', 0, 2)]),
    ],
  ),
  (
    ['multi'],
    [
      ('t:abc', [('abc', 3, 3)]),
      ('k:bs', [('ab', 2, 2)]),
      ('t:d', [('abd', 3, 3)]),
      ('ctrl:z', [('abc', 2, 3)]),
    ],
  ),
  (
    ['multi'],
    [
      ('t:abc', [('abc', 3, 3)]),
      ('set:hello', [('hello', 0, 0)]),
      ('ctrl:z', [('hello', 0, 0)]),
    ],
  ),
  (
    ['multi'],
    [
      ('t:abc', [('abc', 3, 3)]),
      ('app:zz', [('abczz', 5, 5)]),
      ('ctrl:z', [('abczz', 5, 5)]),
    ],
  ),
  // paste() is the control's own WM_PASTE: an insertion right after the typing extends it.
  (
    ['multi'],
    [
      ('clip:PP', [('', 0, 0)]),
      ('t:abc', [('abc', 3, 3)]),
      ('paste', [('abcPP', 5, 5)]),
      ('ctrl:z', [('', 0, 0)]),
    ],
  ),
  (
    ['single'],
    [
      ('t:abc', [('abc', 3, 3)]),
      ('alt:bs', [('', 0, 0)]),
    ],
  ),
  (
    ['single'],
    [
      ('t:abc', [('abc', 3, 3)]),
      ('ctrl:z', [('', 0, 0)]),
      ('ctrl:y', [('', 0, 0)]),
    ],
  ),
  (
    ['single'],
    [
      ('t:abc', [('abc', 3, 3)]),
      ('sh:left', [('abc', 2, 3)]),
      ('ctrl:x', [('ab', 2, 2)]),
      ('ctrl:z', [('abc', 2, 3)]),
    ],
  ),
  // One buffer per control.
  (
    ['multi', 'multi'],
    [
      ('t:abc', [('abc', 3, 3), ('', 0, 0)]),
      ('to:1', [('abc', 3, 3), ('', 0, 0)]),
      ('t:x', [('abc', 3, 3), ('x', 1, 1)]),
      ('ctrl:z', [('abc', 3, 3), ('', 0, 0)]),
      ('ctrl:z', [('abc', 3, 3), ('x', 0, 1)]),
    ],
  ),
];

/// GTK 3: nothing undoes.
const _gtk = <_Scenario>[
  (
    ['multi'],
    [
      ('t:abc', [('abc', 3, 3)]),
      ('ctrl:z', [('abc', 3, 3)]),
      ('ctrlsh:z', [('abc', 3, 3)]),
      ('ctrl:y', [('abc', 3, 3)]),
    ],
  ),
  (
    ['single'],
    [
      ('t:abc', [('abc', 3, 3)]),
      ('ctrl:z', [('abc', 3, 3)]),
      ('ctrlsh:z', [('abc', 3, 3)]),
    ],
  ),
];

const _shellId = 900;
const _firstField = 901;

VRectangle _rect(int x, int y, int w, int h) => VRectangle()
  ..x = x
  ..y = y
  ..width = w
  ..height = h;

/// The fields as Java holds them, and what it does to them through the API.
class _Java {
  _Java(this.kinds, {this.windows = const []});

  final List<String> kinds;

  /// The window each field is in, by index; a field past the end is in window 0.
  final List<int> windows;

  int windowOf(int index) => index < windows.length ? windows[index] : 0;
  final Map<int, int> _seq = {};
  final Set<int> disposed = {};
  int _editNumber = 1000;

  int idOf(int index) => _firstField + index;

  VText field(int index, {String text = '', List<int>? edits, List<String>? editTexts}) => VText()
    ..id = idOf(index)
    ..style = kinds[index] == 'multi' ? SWT.MULTI | SWT.BORDER | SWT.V_SCROLL : SWT.SINGLE | SWT.BORDER
    ..text = text
    ..enabled = true
    ..editable = true
    ..visible = true
    ..edits = edits
    ..editTexts = editTexts
    ..seq = _seq[index] ?? 1
    ..bounds = _rect(10, 10 + index * 100, 300, kinds[index] == 'multi' ? 80 : 24);

  VShell shell([int window = 0]) => VShell()
    ..id = _shellId + 100 * window
    ..style = SWT.SHELL_TRIM
    ..text = 'shell $window'
    ..seq = _seq[-1 - window] ?? 1
    ..visible = true
    ..bounds = _rect(420 * window, 0, 400, 500)
    ..children = [
      for (var i = 0; i < kinds.length; i++)
        if (!disposed.contains(i) && windowOf(i) == window) field(i),
    ];

  List<VShell> shells() => [for (var w = 0; w <= windows.fold(0, math.max); w++) shell(w)];

  /// Java's edit of field [index]: the text it leaves and the edit it logs.
  Future<void> edit(int index, String text, int kind, int start, String replaced, String inserted) async {
    final base = _seq[index] ?? 1;
    final seq = base + 1;
    _seq[index] = seq;
    await deliverChange(
      field(index, text: text, edits: [++_editNumber, kind, start], editTexts: [replaced, inserted]),
      keys: [WireKey.k_Text_text, WireKey.k_Text_edits, WireKey.k_Text_editTexts],
      base: base,
    );
  }

  Future<void> dispose(int index) async {
    disposed.add(index);
    final base = _seq[-1] ?? 1;
    _seq[-1] = base + 1;
    await deliverChange(shell(), keys: [WireKey.k_Composite_children], base: base);
  }
}

/// What field [index] shows: its text and selection.
(String, TextSelection) _shown(WidgetTester tester, int index) {
  final field = find.byWidgetPredicate((w) => w is TextSwt && w.value.id == _firstField + index);
  final lines = find.descendant(of: field, matching: find.byType(LineEditor));
  if (lines.evaluate().isNotEmpty) {
    final value = tester.widget<LineEditor>(lines).controller.value;
    return (value.text, value.selection);
  }
  final editable = find.descendant(of: field, matching: find.byType(EditableText));
  if (editable.evaluate().isNotEmpty) {
    final value = tester.widget<EditableText>(editable).controller.value;
    return (value.text, value.selection);
  }
  // A single-line field at rest, never focused: what it shows is what it was given.
  return ('', const TextSelection.collapsed(offset: 0));
}

/// Java has had every Modify the field sent, and echoed it, by the time it calls the API.
void _echoed(WidgetTester tester, int index) {
  final field = find.byWidgetPredicate((w) => w is TextSwt && w.value.id == _firstField + index);
  (tester.state(field) as PendingTextEchoes).clearSentTextEchoes();
}

void _setSelection(WidgetTester tester, int index, TextSelection selection) {
  final field = find.byWidgetPredicate((w) => w is TextSwt && w.value.id == _firstField + index);
  final lines = find.descendant(of: field, matching: find.byType(LineEditor));
  if (lines.evaluate().isNotEmpty) {
    tester.widget<LineEditor>(lines).controller.selection = selection;
  } else {
    tester.widget<EditableText>(find.descendant(of: field, matching: find.byType(EditableText))).controller.selection =
        selection;
  }
}

Future<void> _key(
  WidgetTester tester,
  LogicalKeyboardKey key, {
  bool shift = false,
  bool alt = false,
  bool meta = false,
  bool control = false,
}) async {
  if (shift) await tester.sendKeyDownEvent(LogicalKeyboardKey.shiftLeft);
  if (alt) await tester.sendKeyDownEvent(LogicalKeyboardKey.altLeft);
  if (meta) await tester.sendKeyDownEvent(LogicalKeyboardKey.metaLeft);
  if (control) await tester.sendKeyDownEvent(LogicalKeyboardKey.controlLeft);
  await tester.sendKeyEvent(key);
  if (control) await tester.sendKeyUpEvent(LogicalKeyboardKey.controlLeft);
  if (meta) await tester.sendKeyUpEvent(LogicalKeyboardKey.metaLeft);
  if (alt) await tester.sendKeyUpEvent(LogicalKeyboardKey.altLeft);
  if (shift) await tester.sendKeyUpEvent(LogicalKeyboardKey.shiftLeft);
  await tester.pumpAndSettle();
}

/// Typing, as the input method reports it: each character replaces the field's selection.
Future<void> _type(WidgetTester tester, int index, String chars) async {
  for (final ch in chars.split('')) {
    final (text, selection) = _shown(tester, index);
    final start = math.min(selection.baseOffset, selection.extentOffset);
    final end = math.max(selection.baseOffset, selection.extentOffset);
    tester.testTextInput.updateEditingValue(
      TextEditingValue(
        text: text.replaceRange(start, end, ch),
        selection: TextSelection.collapsed(offset: start + ch.length),
      ),
    );
    await tester.pump();
  }
  await tester.pumpAndSettle();
}

const _keys = {
  'left': LogicalKeyboardKey.arrowLeft,
  'right': LogicalKeyboardKey.arrowRight,
  'bs': LogicalKeyboardKey.backspace,
  'del': LogicalKeyboardKey.delete,
  'home': LogicalKeyboardKey.home,
};

LogicalKeyboardKey _letter(String c) => LogicalKeyboardKey.findKeyByKeyId(c.toLowerCase().codeUnitAt(0))!;

Future<void> _focus(WidgetTester tester, _Java java, int index) async {
  FocusRequests.instance.request(java.idOf(index));
  await tester.pumpAndSettle();
}

void main() {
  setUp(() {
    resetConfigFlags();
    FocusRequests.instance.reset();
  });
  tearDown(() {
    resetConfigFlags();
    FocusRequests.instance.reset();
  });

  for (final (platform, scenarios) in [
    (TargetPlatform.macOS, nativeCocoaTextUndo),
    (TargetPlatform.windows, _win32),
    (TargetPlatform.linux, _gtk),
  ]) {
    for (final (kinds, steps) in scenarios) {
      testWidgets('${kinds.join('+')}: ${steps.map((s) => s.$1).join(' ')}', (tester) async {
        String? clipboard;
        tester.binding.defaultBinaryMessenger.setMockMethodCallHandler(SystemChannels.platform, (call) async {
          switch (call.method) {
            case 'Clipboard.setData':
              clipboard = (call.arguments as Map)['text'] as String?;
              return null;
            case 'Clipboard.getData':
              return clipboard == null ? null : <String, dynamic>{'text': clipboard};
            case 'Clipboard.hasStrings':
              return <String, dynamic>{'value': clipboard != null};
          }
          return null;
        });
        freshClient();
        final java = _Java(kinds);
        await tester.pumpWidget(
          ForceLive(
            child: EvolveApp(
              theme: ThemeMode.light,
              contentWidget: DisplaySwt(value: VDisplay()..shells = [java.shell()]),
            ),
          ),
        );
        await tester.pumpAndSettle();
        var current = 0;
        await _focus(tester, java, current);
        // Where the API leaves the caret is the field's own business, not undo's: SWT's Cocoa Text puts
        // it at the start after setText, this one at the end. A field's selection is compared again
        // once a later step has edited it.
        final apiPlaced = <int>{};

        for (final (step, expected) in steps) {
          final textBefore = [
            for (var i = 0; i < kinds.length; i++) java.disposed.contains(i) ? '' : _shown(tester, i).$1,
          ];
          final colon = step.indexOf(':');
          final op = colon < 0 ? step : step.substring(0, colon);
          final arg = colon < 0 ? '' : step.substring(colon + 1);
          var compareSelection = true;
          switch (op) {
            case 't':
              await _type(tester, current, arg);
            case 'k' when arg == 'ret':
              await _type(tester, current, '\n');
            case 'k':
              await _key(tester, _keys[arg]!);
            case 'opt':
              await _key(tester, _keys[arg]!, alt: true);
            case 'sh':
              await _key(tester, _keys[arg]!, shift: true);
            case 'cmd':
              await _key(tester, _letter(arg), meta: true);
            case 'cmdsh':
              await _key(tester, _letter(arg), meta: true, shift: true);
            case 'ctrl':
              await _key(tester, _letter(arg), control: true);
            case 'ctrlsh':
              await _key(tester, _letter(arg), control: true, shift: true);
            case 'alt':
              await _key(tester, _keys[arg]!, alt: true);
            case 'to':
              current = int.parse(arg);
              await _focus(tester, java, current);
            case 'away':
              FocusManager.instance.primaryFocus?.unfocus();
              await tester.pumpAndSettle();
            case 'back':
              await _focus(tester, java, current);
            case 'sel':
              final parts = arg.split(',');
              _setSelection(
                tester,
                current,
                TextSelection(baseOffset: int.parse(parts[0]), extentOffset: int.parse(parts[1])),
              );
              await tester.pumpAndSettle();
            case 'clip':
              clipboard = arg;
            case 'set' || 'app' || 'ins' || 'paste' || 'cut' || 'setn' || 'appn':
              // Java leaves the selection where its own control would; what is compared is the text.
              compareSelection = false;
              var index = current;
              var text = arg;
              if (op == 'setn' || op == 'appn') {
                final parts = arg.split(':');
                index = int.parse(parts[0]);
                text = parts.sublist(1).join(':');
              }
              _echoed(tester, index);
              apiPlaced.add(index);
              final (shown, selection) = _shown(tester, index);
              final start = selection.isValid ? selection.start : shown.length;
              final end = selection.isValid ? selection.end : shown.length;
              switch (op) {
                case 'set' || 'setn':
                  await java.edit(index, text, 1, 0, '', '');
                case 'app' || 'appn':
                  await java.edit(index, shown + text, 2, 0, '', '');
                case 'ins':
                  await java.edit(index, shown.replaceRange(start, end, text), 3, 0, '', '');
                case 'paste':
                  final pasted = clipboard ?? '';
                  await java.edit(
                    index,
                    shown.replaceRange(start, end, pasted),
                    4,
                    start,
                    shown.substring(start, end),
                    pasted,
                  );
                case 'cut':
                  clipboard = shown.substring(start, end);
                  await java.edit(index, shown.replaceRange(start, end, ''), 5, start, shown.substring(start, end), '');
              }
              await tester.pumpAndSettle();
            case 'disp':
              await java.dispose(int.parse(arg));
              await tester.pumpAndSettle();
            default:
              fail('no such step: $step');
          }
          if (op == 'to' || op == 'away' || op == 'back' || op == 'disp' || op == 'clip') compareSelection = false;
          if ((op == 'to' || op == 'back') && kinds[current] == 'single') {
            // A single-line field here selects all of its text when it gains focus, where SWT's keeps
            // the selection it had; that is no part of undo, so the native selection is put back.
            final want = expected[current];
            if (want != null && _shown(tester, current).$1 == want.$1) {
              _setSelection(tester, current, TextSelection(baseOffset: want.$2, extentOffset: want.$3));
              await tester.pumpAndSettle();
            }
          }

          for (var i = 0; i < expected.length; i++) {
            final want = expected[i];
            if (want == null) continue;
            final (text, selection) = _shown(tester, i);
            expect(text, want.$1, reason: 'field $i after $step');
            if (compareSelection && text != textBefore[i]) apiPlaced.remove(i);
            if (compareSelection && i == current && !apiPlaced.contains(i)) {
              expect((selection.start, selection.end), (want.$2, want.$3), reason: 'selection of field $i after $step');
            }
          }
        }
      }, variant: TargetPlatformVariant.only(platform));
    }
  }

  // Cocoa records into the undo manager of the field's window, so one window's undo never reaches
  // a field in another.
  testWidgets('cocoa: each window keeps its own undo', (tester) async {
    freshClient();
    final java = _Java(['multi', 'multi'], windows: [0, 1]);
    await tester.pumpWidget(
      ForceLive(
        child: EvolveApp(
          theme: ThemeMode.light,
          contentWidget: DisplaySwt(value: VDisplay()..shells = java.shells()),
        ),
      ),
    );
    await tester.pumpAndSettle();
    await _focus(tester, java, 0);
    await _type(tester, 0, 'abc');
    await _focus(tester, java, 1);
    await _type(tester, 1, 'x');

    await _key(tester, LogicalKeyboardKey.keyZ, meta: true);
    expect((_shown(tester, 0).$1, _shown(tester, 1).$1), ('abc', ''));
    await _key(tester, LogicalKeyboardKey.keyZ, meta: true);
    expect((_shown(tester, 0).$1, _shown(tester, 1).$1), ('abc', ''), reason: "window 1's undo is empty");

    await _focus(tester, java, 0);
    await _key(tester, LogicalKeyboardKey.keyZ, meta: true);
    expect((_shown(tester, 0).$1, _shown(tester, 1).$1), ('', ''));
  }, variant: TargetPlatformVariant.only(TargetPlatform.macOS));
}
