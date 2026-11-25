// Where a multi-line Text breaks its lines: at each of its delimiters, and at its width only with
// SWT.WRAP. Each case runs on the LineEditor that draws the field while semantics are off, as in
// production, and on the TextField it is with semantics on.

import 'package:flutter/gestures.dart';
import 'package:flutter/material.dart';
import 'package:flutter/rendering.dart';
import 'package:flutter/services.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/gen/event.dart';
import 'package:swtflutter/src/gen/point.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/gen/text.dart';
import 'package:swtflutter/src/gen/widgets.dart' show WireKey, wireChannelNames;
import 'package:swtflutter/src/impl/utils/force_live.dart';
import 'package:swtflutter/src/impl/utils/line_editor.dart';
import 'package:swtflutter/src/impl/utils/line_editor_render.dart';

import 'delivery/support/deliver.dart';

const _firstLine = 'The quick brown fox jumps over the lazy dog.';

VText _text({required int style, required int width, required String value}) => VText()
  ..swt = 'Text'
  ..id = 1
  ..visible = true
  ..style = style
  ..enabled = true
  ..editable = true
  ..text = value
  ..bounds = (VRectangle()
    ..x = 0
    ..y = 0
    ..width = width
    ..height = 80);

Widget _wrap(VText value, {List<VEvent>? modifies}) => ForceLive(child: EvolveApp(
      theme: ThemeMode.light,
      contentWidget: Align(
          alignment: Alignment.topLeft,
          child: modifies == null ? TextSwt<VText>(value: value) : _RecordingTextSwt(value: value, modifies: modifies)),
    ));

class _RecordingTextSwt extends TextSwt<VText> {
  const _RecordingTextSwt({required super.value, required this.modifies});

  final List<VEvent> modifies;

  @override
  void sendEvent(VText val, int channel, VEvent? payload) {
    if (wireChannelNames[channel] == 'Modify/Modify' && payload != null) modifies.add(payload);
    super.sendEvent(val, channel, payload);
  }
}

/// What a browser's textarea does with the editing state it was last sent when [typed] is typed: it
/// holds each line break as "\n", puts its caret at the offsets it was sent, clamped to that text,
/// and types there.
TextEditingValue _typedInTextarea(WidgetTester tester, String typed) {
  final sent = tester.testTextInput.editingState!;
  final text = (sent['text'] as String).replaceAll(RegExp('\r\n?'), '\n');
  final start = (sent['selectionBase'] as int).clamp(0, text.length);
  final end = (sent['selectionExtent'] as int).clamp(0, text.length);
  final from = start < end ? start : end;
  final to = start < end ? end : start;
  return TextEditingValue(
      text: text.replaceRange(from, to, typed), selection: TextSelection.collapsed(offset: from + typed.length));
}

/// Where a tap puts the caret before the first [of] the field shows.
Offset _tapTargetBefore(WidgetTester tester, String of) {
  final lines = find.byType(LineEditorViewport);
  if (lines.evaluate().isNotEmpty) {
    final render = tester.renderObject<RenderLineEditor>(lines);
    final text = tester.widget<LineEditor>(find.byType(LineEditor)).controller.text;
    final caret = render.getLocalRectForCaret(TextPosition(offset: text.indexOf(of)));
    return render.localToGlobal(caret.centerLeft + const Offset(1, 0));
  }
  late RenderEditable re;
  void visit(RenderObject ro) {
    if (ro is RenderEditable) re = ro;
    ro.visitChildren(visit);
  }

  visit(tester.binding.renderViewElement!.renderObject!);
  final caret = re.getLocalRectForCaret(TextPosition(offset: re.text!.toPlainText().indexOf(of)));
  return re.localToGlobal(caret.centerLeft + const Offset(1, 0));
}

/// Where the caret at an offset is, in whichever editor draws the field.
Rect Function(int offset) _caretOf(WidgetTester tester) {
  final lines = find.byType(LineEditorViewport);
  if (lines.evaluate().isNotEmpty) {
    final render = tester.renderObject<RenderLineEditor>(lines);
    return (offset) => render.getLocalRectForCaret(TextPosition(offset: offset));
  }
  RenderEditable? re;
  void visit(RenderObject ro) {
    if (ro is RenderEditable) re = ro;
    ro.visitChildren(visit);
  }

  visit(tester.binding.renderViewElement!.renderObject!);
  expect(re, isNotNull, reason: 'no editor found for the Text');
  return (offset) => re!.getLocalRectForCaret(TextPosition(offset: offset));
}

Future<Rect Function(int)> _pump(WidgetTester tester, {required int style, required int width, required String value}) async {
  await tester.pumpWidget(_wrap(_text(style: style, width: width, value: value)));
  await tester.pumpAndSettle();
  return _caretOf(tester);
}

void main() {
  _noWrapCases();
  // The TextField cases last: the semantics tree a test turns on stays on in the tests after it.
  for (final semantics in [false, true]) {
    final editor = semantics ? 'TextField' : 'LineEditor';

    testWidgets('a MULTI WRAP Text still wraps a line longer than its width ($editor)', semanticsEnabled: semantics,
        (tester) async {
      final caret = await _pump(tester, style: SWT.MULTI | SWT.WRAP | SWT.V_SCROLL | SWT.BORDER, width: 150, value: _firstLine);
      expect(caret(_firstLine.indexOf('dog')).top, greaterThan(caret(0).top));
    });

    for (final delimiter in ['\r', '\n', '\r\n']) {
      final label = delimiter.replaceAll('\r', r'\r').replaceAll('\n', r'\n');
      testWidgets('a MULTI Text starts a new line at "$label" ($editor)', semanticsEnabled: semantics, (tester) async {
        final value = 'One$delimiter' 'Two';
        final caret = await _pump(tester, style: SWT.MULTI | SWT.WRAP | SWT.V_SCROLL | SWT.BORDER, width: 300, value: value);
        expect(caret(value.indexOf('Two')).top, greaterThan(caret(0).top),
            reason: '"$label" is a line delimiter (Text.DELIMITER is "\\r")');
      });
    }

    for (final lineBreak in ['\r', '\r\n']) {
      final label = lineBreak.replaceAll('\r', r'\r').replaceAll('\n', r'\n');
      testWidgets('a "$label" the browser reports as "\\n" stays a "$label" when typing ($editor)',
          semanticsEnabled: semantics, (tester) async {
        final modifies = <VEvent>[];
        await tester.pumpWidget(_wrap(
            _text(style: SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER, width: 300, value: 'One${lineBreak}Two'),
            modifies: modifies));
        await tester.pumpAndSettle();
        final focus = semantics
            ? tester.widget<TextField>(find.byType(TextField)).focusNode!
            : tester.widget<LineEditor>(find.byType(LineEditor)).focusNode;
        focus.requestFocus();
        await tester.pump();
        expect(tester.testTextInput.editingState!['text'], 'One\nTwo');
        // A textarea's value turns each line break into "\n", and counts its offsets in that text.
        tester.testTextInput.updateEditingValue(
            const TextEditingValue(text: 'One\nTwox', selection: TextSelection.collapsed(offset: 8)));
        await tester.pump();
        final typed = 'One${lineBreak}Twox';
        expect(modifies.last.text, typed);
        expect(modifies.last.start, typed.length);
        final client = semantics
            ? tester.state<EditableTextState>(find.byType(EditableText))
            : tester.state<LineEditorState>(find.byType(LineEditor));
        expect((client as TextInputClient).currentTextEditingValue,
            const TextEditingValue(text: 'One\nTwox', selection: TextSelection.collapsed(offset: 8)));
      });
    }

    // A textarea counts its offsets in its own text, where each "\r\n" is one "\n".
    testWidgets('typing after a click past a "\\r\\n" lands at the caret ($editor)', semanticsEnabled: semantics,
        (tester) async {
      final modifies = <VEvent>[];
      await tester.pumpWidget(_wrap(_text(style: SWT.MULTI | SWT.WRAP | SWT.V_SCROLL | SWT.BORDER, width: 300, value: 'One\r\nTwo'),
          modifies: modifies));
      await tester.pumpAndSettle();
      await tester.tapAt(_tapTargetBefore(tester, 'Two'));
      await tester.pumpAndSettle();

      tester.testTextInput.updateEditingValue(_typedInTextarea(tester, 'x'));
      await tester.pumpAndSettle();
      expect(modifies.last.text, 'One\r\nxTwo');
      expect(modifies.last.start, 6, reason: 'the caret Java is told about is after the "x"');
    });

    testWidgets('typing over a range Java selected past a "\\r\\n" replaces that range ($editor)',
        semanticsEnabled: semantics, (tester) async {
      final modifies = <VEvent>[];
      await tester.pumpWidget(_wrap(_text(style: SWT.MULTI | SWT.WRAP | SWT.V_SCROLL | SWT.BORDER, width: 300, value: 'One\r\nTwo'),
          modifies: modifies));
      await tester.pumpAndSettle();
      final focus = semantics
          ? tester.widget<TextField>(find.byType(TextField)).focusNode!
          : tester.widget<LineEditor>(find.byType(LineEditor)).focusNode;
      focus.requestFocus();
      await tester.pumpAndSettle();
      applyChange(
          VText()
            ..id = 1
            ..seq = 1
            ..selection = (VPoint()
              ..x = 5
              ..y = 8),
          keys: [WireKey.k_Text_selection],
          base: 0);
      await tester.pumpAndSettle();

      tester.testTextInput.updateEditingValue(_typedInTextarea(tester, 'x'));
      await tester.pumpAndSettle();
      expect(modifies.last.text, 'One\r\nx', reason: 'Java selected "Two"');
    });
  }

  testWidgets('a MULTI Text without WRAP keeps a line longer than its width on one line (TextField)',
      semanticsEnabled: true, (tester) async {
    final caret = await _pump(tester, style: SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER, width: 150, value: _firstLine);
    expect(caret(_firstLine.indexOf('dog')).top, caret(0).top,
        reason: 'without SWT.WRAP the line must not soft-wrap at the control width');
  });

  testWidgets('a MULTI Text without WRAP scrolls sideways to show the caret at the end of a long line (TextField)',
      semanticsEnabled: true, (tester) async {
    await _pump(tester, style: SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER, width: 150, value: _firstLine);
    final sideways = tester
        .state<ScrollableState>(find.descendant(of: find.byType(SingleChildScrollView), matching: find.byType(Scrollable)).first)
        .position;
    expect(sideways.maxScrollExtent, greaterThan(0), reason: 'the line is wider than the field');
    final field = tester.widget<TextField>(find.byType(TextField));
    field.focusNode!.requestFocus();
    field.controller!.selection = const TextSelection.collapsed(offset: 0);
    await tester.pumpAndSettle();
    expect(sideways.pixels, 0);
    await tester.sendKeyEvent(LogicalKeyboardKey.end);
    await tester.pumpAndSettle();
    expect(field.controller!.selection.extentOffset, _firstLine.length);
    expect(sideways.pixels, sideways.maxScrollExtent);
  });

  testWidgets('a MULTI Text without WRAP takes a change from Java inside its sideways scroll view (TextField)',
      semanticsEnabled: true, (tester) async {
    const noWrap = SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER;
    await _pump(tester, style: noWrap, width: 150, value: _firstLine);
    expect(tester.widget<TextField>(find.byType(TextField)).readOnly, isFalse);
    applyChange(VText()..id = 1..seq = 1..editable = false, keys: [WireKey.k_Text_editable], base: 0);
    await tester.pumpAndSettle();
    expect(tester.widget<TextField>(find.byType(TextField)).readOnly, isTrue);
  });
}

void _noWrapCases() {
  testWidgets('a MULTI Text without WRAP keeps a line longer than its width on one line', semanticsEnabled: false,
      (tester) async {
    final caret = await _pump(tester, style: SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER, width: 150, value: _firstLine);
    expect(caret(_firstLine.indexOf('dog')).top, caret(0).top,
        reason: 'without SWT.WRAP the line must not soft-wrap at the control width');
  });

  testWidgets('a MULTI Text without WRAP scrolls sideways to show the caret at the end of a long line',
      semanticsEnabled: false, (tester) async {
    final caret = await _pump(tester, style: SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER, width: 150, value: _firstLine);
    final render = tester.renderObject<RenderLineEditor>(find.byType(LineEditorViewport));
    expect(caret(_firstLine.length).left, greaterThan(render.size.width), reason: 'the end of the line starts out of view');

    tester.state<LineEditorState>(find.byType(LineEditor)).bringIntoView(TextPosition(offset: _firstLine.length));
    await tester.pump();

    final end = caret(_firstLine.length);
    expect(end.right, lessThanOrEqualTo(render.size.width));
    expect(end.left, greaterThanOrEqualTo(0));
    expect(caret(0).left, lessThan(0), reason: 'the start of the line has scrolled out on the left');
  });

  const noWrap = SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER;

  testWidgets('a horizontal wheel scrolls a MULTI Text without WRAP sideways, up to the end of its line',
      semanticsEnabled: false, (tester) async {
    final caret = await _pump(tester, style: noWrap, width: 150, value: _firstLine);
    final render = tester.renderObject<RenderLineEditor>(find.byType(LineEditorViewport));
    final wheel = TestPointer(1, PointerDeviceKind.mouse)..hover(tester.getCenter(find.byType(LineEditorViewport)));

    await tester.sendEventToBinding(wheel.scroll(const Offset(40, 0)));
    await tester.pump();
    expect(caret(0).left, lessThan(0), reason: 'the start of the line has scrolled out on the left');

    await tester.sendEventToBinding(wheel.scroll(const Offset(10000, 0)));
    await tester.pump();
    final end = caret(_firstLine.length);
    expect(end.right, lessThanOrEqualTo(render.size.width), reason: 'the end of the line shows');
    expect(end.right, greaterThan(render.size.width - 10), reason: 'no blank past the end of the line');
  });

  testWidgets('Shift and a vertical wheel scroll a MULTI Text without WRAP sideways', semanticsEnabled: false,
      (tester) async {
    final caret = await _pump(tester, style: noWrap, width: 150, value: _firstLine);
    final wheel = TestPointer(1, PointerDeviceKind.mouse)..hover(tester.getCenter(find.byType(LineEditorViewport)));

    await tester.sendKeyDownEvent(LogicalKeyboardKey.shiftLeft);
    await tester.sendEventToBinding(wheel.scroll(const Offset(0, 40)));
    await tester.sendKeyUpEvent(LogicalKeyboardKey.shiftLeft);
    await tester.pump();
    expect(caret(0).left, lessThan(0));
  });

  testWidgets('a sideways trackpad pan scrolls a MULTI Text without WRAP sideways', semanticsEnabled: false,
      (tester) async {
    final caret = await _pump(tester, style: noWrap, width: 150, value: _firstLine);
    final center = tester.getCenter(find.byType(LineEditorViewport));
    final pad = TestPointer(2, PointerDeviceKind.trackpad);

    await tester.sendEventToBinding(pad.panZoomStart(center));
    await tester.sendEventToBinding(pad.panZoomUpdate(center, pan: const Offset(-40, 0)));
    await tester.sendEventToBinding(pad.panZoomEnd());
    await tester.pump();
    expect(caret(0).left, lessThan(0));
  });

  testWidgets('a MULTI Text without WRAP scrolled to the end of a line scrolls back once the line fits',
      semanticsEnabled: false, (tester) async {
    await _pump(tester, style: noWrap, width: 150, value: _firstLine);
    tester.state<LineEditorState>(find.byType(LineEditor)).bringIntoView(TextPosition(offset: _firstLine.length));
    await tester.pump();
    final render = tester.renderObject<RenderLineEditor>(find.byType(LineEditorViewport));
    expect(render.scrollX, greaterThan(0));

    final lines = tester.widget<LineEditor>(find.byType(LineEditor)).controller;
    lines.value = lines.value.editedTo('The', const TextSelection.collapsed(offset: 3));
    await tester.pumpAndSettle();
    expect(render.scrollX, 0, reason: 'the line is narrower than the field');
  });
}
