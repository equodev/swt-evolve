// A multi-line Text with bounds is drawn by a LineEditor while semantics are off, as in production.
// What Text does around the TextField it does around the LineEditor too: Java's pushes, the
// VerifyListener's veto, the text limit, read-only, alignment, and the mouse.

import 'package:flutter/gestures.dart';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
// The io transport directly: `flutter test` runs on the VM, where comm.dart resolves to it —
// but the analyzer resolves the conditional export to the web variant, which has no test hook.
import 'package:swtflutter/src/comm/comm_ws.dart';
import 'package:swtflutter/src/comm/tagged_values.dart';
import 'package:swtflutter/src/gen/event.dart';
import 'package:swtflutter/src/gen/point.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/gen/text.dart';
import 'package:swtflutter/src/gen/widgets.dart' show wireChannelNames;
import 'package:swtflutter/src/impl/config_flags.dart';
import 'package:swtflutter/src/impl/utils/line_editor.dart';
import 'package:swtflutter/src/impl/utils/line_editor_render.dart';
import 'package:swtflutter/src/impl/widget_config.dart';

import 'delivery/support/deliver.dart';
import 'support/channels.dart' as channels;

class _RecordingTextSwt extends TextSwt<VText> {
  final List<String> events;
  const _RecordingTextSwt({required super.value, required this.events});

  @override
  void sendEvent(VText val, int channel, VEvent? payload) {
    events.add(wireChannelNames[channel]!);
    super.sendEvent(val, channel, payload);
  }
}

VText _value(String text, {int style = 0, int? textLimit}) => VText()
  ..swt = 'Text'
  ..id = 1
  ..style = SWT.MULTI | SWT.V_SCROLL | style
  ..enabled = true
  ..editable = true
  ..visible = true
  ..text = text
  ..textLimit = textLimit
  ..bounds = (VRectangle()
    ..x = 0
    ..y = 0
    ..width = 240
    ..height = 120);

Future<List<String>> _pump(WidgetTester tester, VText value) async {
  final events = <String>[];
  await tester.pumpWidget(EvolveApp(
    theme: ThemeMode.light,
    contentWidget: Align(
      alignment: Alignment.topLeft,
      child: SizedBox(width: 240, height: 120, child: _RecordingTextSwt(value: value, events: events)),
    ),
  ));
  await tester.pump();
  return events;
}

LineEditorController _lines(WidgetTester tester) => tester.widget<LineEditor>(find.byType(LineEditor)).controller;

Future<void> _focus(WidgetTester tester) async {
  tester.widget<LineEditor>(find.byType(LineEditor)).focusNode.requestFocus();
  await tester.pump();
}

void _type(WidgetTester tester, String text) => tester.testTextInput.updateEditingValue(
      TextEditingValue(text: text, selection: TextSelection.collapsed(offset: text.length)),
    );

/// Delivers an inbound frame as the transport would: its numbered header, then a tagged-values body.
void _deliver(String actionId, Object payload) {
  EquoCommService.commForTesting.receiveBinary(channels.frame(actionId, TaggedValues.encode(payload)));
}

void main() {
  setUp(resetConfigFlags);
  tearDown(resetConfigFlags);

  testWidgets('a text Java pushes is shown, with the selection Java gives', semanticsEnabled: false, (tester) async {
    await _pump(tester, _value('one\ntwo'));
    expect(_lines(tester).text, 'one\ntwo');

    await deliverWhole(_value('one\ntwo\nthree')
      ..seq = 2
      ..selection = (VPoint()
        ..x = 4
        ..y = 7));
    await tester.pump();

    expect(_lines(tester).text, 'one\ntwo\nthree');
    expect(_lines(tester).selection, const TextSelection(baseOffset: 4, extentOffset: 7));
  });

  testWidgets('a vetoable field shows an edit only once Java allows it', semanticsEnabled: false, (tester) async {
    await _pump(tester, _value('12'));
    await _focus(tester);
    _deliver('Text/1/modify/vetoable', {'value': true});
    await tester.pump();

    _type(tester, '123');
    await tester.pump();
    expect(_lines(tester).text, '12', reason: 'held until the verdict');

    _deliver('Text/1/modify/verdict', {'doit': false});
    await tester.pump();
    expect(_lines(tester).text, '12', reason: 'a vetoed edit is never shown');

    _type(tester, '124');
    await tester.pump();
    _deliver('Text/1/modify/verdict', {'doit': true});
    await tester.pump();
    expect(_lines(tester).text, '124');
  });

  testWidgets('keystrokes typed before their verdicts arrive all survive', semanticsEnabled: false, (tester) async {
    await _pump(tester, _value('12'));
    await _focus(tester);
    _deliver('Text/1/modify/vetoable', {'value': true});
    await tester.pump();

    for (final char in '3 4'.split('')) {
      _type(tester, _lines(tester).text + char);
      await tester.pump();
    }
    // Java vetoes the space and allows the rest.
    for (final doit in [true, false, true]) {
      _deliver('Text/1/modify/verdict', {'doit': doit});
      await tester.pump();
    }

    expect(_lines(tester).text, '1234', reason: 'every allowed keystroke is kept, the vetoed one alone dropped');
  });

  testWidgets('an edit past the text limit is cut to fit', semanticsEnabled: false, (tester) async {
    await _pump(tester, _value('ab', textLimit: 5));
    await _focus(tester);

    _type(tester, 'abcdefg');
    await tester.pump();

    expect(_lines(tester).text, 'abcde');
  });

  testWidgets('a read-only field takes no edit from the keyboard', semanticsEnabled: false, (tester) async {
    await _pump(tester, _value('fixed', style: SWT.READ_ONLY));
    expect(tester.widget<LineEditor>(find.byType(LineEditor)).readOnly, isTrue);
    await _focus(tester);
    _lines(tester).selection = const TextSelection.collapsed(offset: 5);
    await tester.pump();

    await tester.sendKeyEvent(LogicalKeyboardKey.backspace);
    await tester.pump();

    expect(_lines(tester).text, 'fixed');
  });

  // A read-only TextField shows no caret (EditableText's showCursor defaults to !readOnly).
  for (final (style, caret) in [(SWT.READ_ONLY, false), (SWT.NONE, true)]) {
    testWidgets('a focused ${caret ? 'editable' : 'read-only'} field ${caret ? 'shows' : 'shows no'} caret',
        semanticsEnabled: false, (tester) async {
      await _pump(tester, _value('fixed', style: style));
      await _focus(tester);
      bool shown() => tester.widget<LineEditorViewport>(find.byType(LineEditorViewport)).showCursor.value;

      expect(shown(), caret);
      _lines(tester).selection = const TextSelection.collapsed(offset: 2);
      await tester.pump();
      expect(shown(), caret, reason: 'after the caret moves');
      await tester.pump(const Duration(milliseconds: 1000));
      expect(shown(), caret, reason: 'after two blink half-periods');

      await tester.pumpWidget(const SizedBox());
    });
  }

  testWidgets('a focused field hides its caret when Java makes it read-only, and shows it again after',
      semanticsEnabled: false, (tester) async {
    await _pump(tester, _value('fixed'));
    await _focus(tester);
    bool shown() => tester.widget<LineEditorViewport>(find.byType(LineEditorViewport)).showCursor.value;
    expect(shown(), isTrue);

    await deliverWhole(_value('fixed')
      ..seq = 2
      ..editable = false);
    await tester.pump();
    expect(shown(), isFalse, reason: 'setEditable(false)');
    await tester.pump(const Duration(milliseconds: 500));
    expect(shown(), isFalse, reason: 'after a blink half-period');

    await deliverWhole(_value('fixed')..seq = 3);
    await tester.pump();
    expect(shown(), isTrue, reason: 'setEditable(true)');

    await tester.pumpWidget(const SizedBox());
  });

  for (final (style, align) in [(SWT.LEFT, TextAlign.left), (SWT.CENTER, TextAlign.center), (SWT.RIGHT, TextAlign.right)]) {
    testWidgets('the style aligns the text $align', semanticsEnabled: false, (tester) async {
      await _pump(tester, _value('text', style: style));
      expect(tester.widget<LineEditor>(find.byType(LineEditor)).textAlign, align);
    });
  }

  testWidgets('a double click reports one MouseDoubleClick', semanticsEnabled: false, (tester) async {
    setConfigFlags(ConfigFlags()..double_click_timeout_ms = 60000);
    final events = await _pump(tester, _value('some words here'));
    final at = tester.getTopLeft(find.byType(LineEditor)) + const Offset(20, 8);

    await tester.tapAt(at, kind: PointerDeviceKind.mouse);
    await tester.pump(const Duration(milliseconds: 50));
    await tester.tapAt(at, kind: PointerDeviceKind.mouse);
    await tester.pump(const Duration(milliseconds: 600));

    expect(events.where((e) => e == 'Mouse/MouseDoubleClick'), hasLength(1));
    expect(_lines(tester).selection.isCollapsed, isFalse, reason: 'the double click selects a word');
  });

  testWidgets('a secondary click on a field never focused keeps the caret Java has', semanticsEnabled: false, (tester) async {
    await _pump(tester, _value('first\nsecond')..caretPosition = 2);
    final at = tester.getTopLeft(find.byType(LineEditor)) + const Offset(60, 30);

    final gesture = await tester.startGesture(at, kind: PointerDeviceKind.mouse, buttons: kSecondaryButton);
    await tester.pump();
    await gesture.up();
    await tester.pump(const Duration(milliseconds: 600));

    expect(_lines(tester).selection, const TextSelection.collapsed(offset: 2));
  });

  testWidgets('what was typed in the LineEditor is in the TextField that replaces it', semanticsEnabled: false, (tester) async {
    await _pump(tester, _value(''));
    await _focus(tester);
    _type(tester, 'typed\nhere');
    await tester.pump();

    // Semantics turning on, as assistive technology does after the page loads, makes the next build
    // a TextField; a hover brings that build.
    final semantics = tester.ensureSemantics();
    final mouse = await tester.createGesture(kind: PointerDeviceKind.mouse);
    await mouse.addPointer(location: tester.getCenter(find.byType(LineEditor)));
    await tester.pump();

    expect(find.byType(LineEditor), findsNothing);
    expect(tester.widget<EditableText>(find.byType(EditableText)).controller.text, 'typed\nhere');
    await mouse.removePointer();
    semantics.dispose();
  });
}
