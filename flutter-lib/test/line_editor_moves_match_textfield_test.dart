// Every caret move, from every offset of a text with wrapped and empty lines, lands a LineEditor
// where it lands a multi-line TextField.

import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:swtflutter/src/impl/utils/line_editor.dart';

const _text = 'first line here\nsecond line, with words\n\nfourth word word word word word word word word word word word\nlast';
const _style = TextStyle(fontSize: 13, color: Color(0xFF202020));

Future<void> _press(WidgetTester tester, LogicalKeyboardKey key, LogicalKeyboardKey? modifier) async {
  if (modifier != null) await tester.sendKeyDownEvent(modifier);
  await tester.sendKeyEvent(key);
  if (modifier != null) await tester.sendKeyUpEvent(modifier);
  await tester.pump();
}

void main() {
  for (final modifier in [null, LogicalKeyboardKey.altLeft, LogicalKeyboardKey.metaLeft]) {
    for (final key in [
      LogicalKeyboardKey.arrowRight,
      LogicalKeyboardKey.arrowLeft,
      LogicalKeyboardKey.arrowDown,
      LogicalKeyboardKey.arrowUp,
    ]) {
      testWidgets('${modifier?.debugName ?? 'no modifier'} + ${key.debugName}', (tester) async {
        final field = TextEditingController(text: _text);
        final lines = LineEditorController(_text);
        final fieldFocus = FocusNode(), editorFocus = FocusNode();
        await tester.pumpWidget(MaterialApp(
          theme: ThemeData(platform: TargetPlatform.macOS),
          home: Material(
            child: Column(children: [
              SizedBox(
                width: 240,
                height: 120,
                child: TextField(
                    controller: field, focusNode: fieldFocus, maxLines: null, expands: true, style: _style, decoration: null),
              ),
              SizedBox(
                width: 240,
                height: 120,
                child: Builder(
                  builder: (context) => LineEditor(
                    controller: lines,
                    focusNode: editorFocus,
                    style: Theme.of(context).textTheme.bodyLarge!.merge(_style),
                  ),
                ),
              ),
            ]),
          ),
        ));
        final differ = <String>[];
        for (var at = 0; at <= _text.length; at++) {
          fieldFocus.requestFocus();
          await tester.pump();
          field.selection = TextSelection.collapsed(offset: at);
          await tester.pump();
          await _press(tester, key, modifier);
          editorFocus.requestFocus();
          await tester.pump();
          lines.selection = TextSelection.collapsed(offset: at);
          await tester.pump();
          await _press(tester, key, modifier);
          if (field.selection.extentOffset != lines.selection.extentOffset) {
            differ.add('$at: TextField ${field.selection.extentOffset}, LineEditor ${lines.selection.extentOffset}');
          }
        }
        expect(differ, isEmpty);
      }, variant: TargetPlatformVariant.only(TargetPlatform.macOS));
    }
  }
}
