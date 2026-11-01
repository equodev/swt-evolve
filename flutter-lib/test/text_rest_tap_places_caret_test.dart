// A field at rest that hands over to its TextField keeps the selection a native Text would have: a
// click puts the caret where it landed, a focus from the keyboard or from Java keeps the selection
// Java holds. Neither selects the whole text, as a TextField focused on desktop and web does. A press
// that arrives before the pointer's hover has mounted the TextField still starts a double click or a
// drag.

import 'package:flutter/gestures.dart';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/gen/event.dart';
import 'package:swtflutter/src/gen/point.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/gen/text.dart';
import 'package:swtflutter/src/impl/focus_requests.dart';

import 'delivery/support/deliver.dart';

class _RecordingTextSwt extends TextSwt<VText> {
  const _RecordingTextSwt({required super.value, required this.modified});

  final List<String> modified;

  @override
  void sendModifyModify(VText val, VEvent? payload) => modified.add(payload!.text!);
}

VText _value({VPoint? selection, int? caret}) => VText()
  ..id = 7
  ..seq = 10
  ..style = SWT.SINGLE | SWT.BORDER
  ..enabled = true
  ..editable = true
  ..visible = true
  ..text = 'edit'
  ..selection = selection
  ..caretPosition = caret
  ..bounds = (VRectangle()
    ..x = 0
    ..y = 0
    ..width = 160
    ..height = 24);

Future<List<String>> _mount(WidgetTester tester, {VPoint? selection, int? caret}) async {
  freshClient();
  final modified = <String>[];
  await tester.pumpWidget(EvolveApp(
    theme: ThemeMode.light,
    contentWidget: Align(
      alignment: Alignment.topLeft,
      child: _RecordingTextSwt(value: _value(selection: selection, caret: caret), modified: modified),
    ),
  ));
  await tester.pumpAndSettle();
  expect(find.byType(EditableText), findsNothing, reason: 'the field is not at rest');
  return modified;
}

/// Types [character] the way the platform's text input does: in place of the field's selection.
Future<void> _type(WidgetTester tester, String character) async {
  final value = tester.widget<EditableText>(find.byType(EditableText)).controller.value;
  tester.testTextInput.updateEditingValue(value.replaced(value.selection, character).copyWith(
        selection: TextSelection.collapsed(offset: value.selection.start + character.length),
      ));
  await tester.pump();
}

TextSelection _selection(WidgetTester tester) =>
    tester.widget<EditableText>(find.byType(EditableText)).controller.selection;

final _desktop = TargetPlatformVariant(const {TargetPlatform.linux, TargetPlatform.windows, TargetPlatform.macOS});

void main() {
  setUp(() => FocusRequests.instance.reset());
  tearDown(() => FocusRequests.instance.reset());

  testWidgets('a click past the end of the text at rest puts the caret at its end', (tester) async {
    final modified = await _mount(tester);
    await tester.tap(find.byType(_RecordingTextSwt));
    await tester.pumpAndSettle();
    await _type(tester, 'a');
    expect(modified, ['edita']);
  }, variant: _desktop);

  testWidgets('a click on the text at rest puts the caret where it landed', (tester) async {
    final modified = await _mount(tester);
    final text = tester.getTopLeft(find.byWidgetPredicate((w) => '${w.runtimeType}' == '_TextAtRest'));
    final mouse = await tester.startGesture(text + const Offset(1, 8), kind: PointerDeviceKind.mouse);
    await mouse.up();
    await tester.pumpAndSettle();
    await _type(tester, 'a');
    expect(modified, ['aedit']);
  }, variant: _desktop);

  testWidgets('a double click on the text at rest, with no hover first, selects the word', (tester) async {
    await _mount(tester);
    final at = tester.getCenter(find.byWidgetPredicate((w) => '${w.runtimeType}' == '_TextAtRest'));
    final mouse = await tester.createGesture(kind: PointerDeviceKind.mouse);
    await mouse.addPointer(location: at);
    await mouse.down(at);
    await mouse.up();
    await tester.pump(const Duration(milliseconds: 50));
    await mouse.down(at);
    await mouse.up();
    await tester.pumpAndSettle();
    expect(_selection(tester), const TextSelection(baseOffset: 0, extentOffset: 4));
  }, variant: _desktop);

  testWidgets('a double click on the text at rest whose presses both come before a frame selects the word',
      (tester) async {
    await _mount(tester);
    final at = tester.getCenter(find.byWidgetPredicate((w) => '${w.runtimeType}' == '_TextAtRest'));
    final mouse = await tester.createGesture(kind: PointerDeviceKind.mouse);
    await mouse.addPointer(location: at);
    await mouse.down(at);
    await mouse.up();
    await mouse.down(at);
    await mouse.up();
    await tester.pumpAndSettle();
    expect(_selection(tester), const TextSelection(baseOffset: 0, extentOffset: 4));
  }, variant: _desktop);

  testWidgets('a drag from the text at rest, with no hover first, selects to where it ends', (tester) async {
    await _mount(tester);
    final text = tester.getTopLeft(find.byWidgetPredicate((w) => '${w.runtimeType}' == '_TextAtRest'));
    final from = text + const Offset(1, 8);
    final mouse = await tester.createGesture(kind: PointerDeviceKind.mouse);
    await mouse.addPointer(location: from);
    await mouse.down(from);
    for (var x = 10.0; x <= 120; x += 10) {
      await mouse.moveTo(from + Offset(x, 0));
      await tester.pump(const Duration(milliseconds: 16));
    }
    await mouse.up();
    await tester.pumpAndSettle();
    expect(_selection(tester), const TextSelection(baseOffset: 0, extentOffset: 4));
  }, variant: _desktop);

  testWidgets('a right-click on the text at rest leaves the caret where Java has it', (tester) async {
    final modified = await _mount(tester, caret: 2);
    final text = tester.getTopLeft(find.byWidgetPredicate((w) => '${w.runtimeType}' == '_TextAtRest'));
    final mouse = await tester.startGesture(text + const Offset(1, 8),
        kind: PointerDeviceKind.mouse, buttons: kSecondaryMouseButton);
    await mouse.up();
    await tester.pumpAndSettle();
    await _type(tester, 'a');
    expect(modified, ['edait']);
  }, variant: _desktop);

  testWidgets('Tab into a field at rest keeps the selection Java holds', (tester) async {
    final modified = await _mount(tester, caret: 2);
    await tester.sendKeyEvent(LogicalKeyboardKey.tab);
    await tester.pumpAndSettle();
    await _type(tester, 'a');
    expect(modified, ['edait']);
  }, variant: _desktop);

  testWidgets('a focus request from Java keeps the selection Java holds', (tester) async {
    final modified = await _mount(tester, selection: VPoint()
      ..x = 1
      ..y = 3);
    FocusRequests.instance.request(7);
    await tester.pumpAndSettle();
    await _type(tester, 'a');
    expect(modified, ['eat']);
  }, variant: _desktop);
}
