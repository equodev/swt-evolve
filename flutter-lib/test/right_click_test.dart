// A right-click reports what a native SWT control reports for it: an edit field (Text, multi-line
// Text, Spinner, CCombo, Combo) never moves its caret, not even for a moment, and reports none to
// Java, takes the focus and opens the control's Menu, never Flutter's own toolbar, and closing that
// Menu leaves the selection it held; a Scale reports no Selection, and a Table row's press comes from
// the pointer, ahead of the menu request.

import 'package:flutter/gestures.dart' show kSecondaryMouseButton;
import 'package:flutter/material.dart';
import 'package:flutter/services.dart' show LogicalKeyboardKey;
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/gen/ccombo.dart';
import 'package:swtflutter/src/gen/combo.dart';
import 'package:swtflutter/src/gen/event.dart';
import 'package:swtflutter/src/gen/menu.dart';
import 'package:swtflutter/src/gen/menuitem.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/scale.dart';
import 'package:swtflutter/src/gen/spinner.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/gen/table.dart';
import 'package:swtflutter/src/gen/tablecolumn.dart';
import 'package:swtflutter/src/gen/tableitem.dart';
import 'package:swtflutter/src/gen/text.dart';
import 'package:swtflutter/src/impl/control_evolve.dart' show ControlImpl;
import 'package:swtflutter/src/gen/widgets.dart' show wireChannelNames;
import 'package:swtflutter/src/impl/utils/force_live.dart';
import 'package:swtflutter/src/impl/utils/line_editor.dart';

import 'support/menu_shown_ack.dart';

VRectangle _bounds(int w, int h) => VRectangle()
  ..x = 0
  ..y = 0
  ..width = w
  ..height = h;

Widget _app(Widget child) => ForceLive(child: EvolveApp(
      theme: ThemeMode.light,
      contentWidget: Align(alignment: Alignment.topLeft, child: child),
    ));

class _RecordingScaleSwt extends ScaleSwt<VScale> {
  final List<String> calls;
  const _RecordingScaleSwt({required VScale value, required this.calls}) : super(value: value);

  @override
  void sendEvent(VScale val, int channel, VEvent? payload) => calls.add(wireChannelNames[channel]!);
}

class _RecordingTableSwt extends TableSwt<VTable> {
  final List<String> calls;
  const _RecordingTableSwt({required VTable value, required this.calls}) : super(value: value);

  @override
  void sendEvent(VTable val, int channel, VEvent? payload) =>
      calls.add('${wireChannelNames[channel]!}:${payload?.button}@${payload?.x},${payload?.y}');
}

class _RecordingTextSwt extends TextSwt<VText> {
  final List<String> calls;
  const _RecordingTextSwt({required VText value, required this.calls}) : super(value: value);

  @override
  void sendEvent(VText val, int channel, VEvent? payload) => calls.add(wireChannelNames[channel]!);
}

class _RecordingSpinnerSwt extends SpinnerSwt<VSpinner> {
  final List<String> calls;
  const _RecordingSpinnerSwt({required VSpinner value, required this.calls}) : super(value: value);

  @override
  void sendEvent(VSpinner val, int channel, VEvent? payload) => calls.add(wireChannelNames[channel]!);
}

class _RecordingCComboSwt extends CComboSwt<VCCombo> {
  final List<String> calls;
  const _RecordingCComboSwt({required VCCombo value, required this.calls}) : super(value: value);

  @override
  void sendEvent(VCCombo val, int channel, VEvent? payload) => calls.add(wireChannelNames[channel]!);
}

class _RecordingComboSwt extends ComboSwt<VCombo> {
  final List<String> calls;
  const _RecordingComboSwt({required VCombo value, required this.calls}) : super(value: value);

  @override
  void sendEvent(VCombo val, int channel, VEvent? payload) => calls.add(wireChannelNames[channel]!);
}

/// Past the hover timer a release starts, so no timer is left pending when a test ends.
final _pastTheHover = Duration(milliseconds: 2 * ControlImpl.hoverDelayMs);

const _fieldWidth = 200;
const _fieldHeight = 30;

VRectangle _fieldBounds() => _bounds(_fieldWidth, _fieldHeight);

/// Every edit field, built to record what it sends into the given list, with [menu] as its Menu.
final Map<String, Widget Function(List<String> calls, {VMenu? menu})> _editFields = {
  'Text': (calls, {menu}) => _RecordingTextSwt(
      calls: calls,
      value: VText()
        ..swt = 'Text'
        ..id = 1
        ..style = SWT.BORDER
        ..enabled = true
        ..editable = true
        ..text = 'abc'
        ..menu = menu
        ..bounds = _fieldBounds()),
  'Spinner': (calls, {menu}) => _RecordingSpinnerSwt(
      calls: calls,
      value: VSpinner()
        ..id = 1
        ..style = SWT.BORDER
        ..enabled = true
        ..minimum = 0
        ..maximum = 1000
        ..selection = 123
        ..increment = 1
        ..digits = 0
        ..menu = menu
        ..bounds = _fieldBounds()),
  'CCombo': (calls, {menu}) => _RecordingCComboSwt(
      calls: calls,
      value: VCCombo()
        ..swt = 'CCombo'
        ..id = 1
        ..style = SWT.BORDER
        ..enabled = true
        ..visible = true
        ..text = 'abc'
        ..items = ['abc', 'def']
        ..menu = menu
        ..bounds = _fieldBounds()),
  'Combo': (calls, {menu}) => _RecordingComboSwt(
      calls: calls,
      value: VCombo()
        ..swt = 'Combo'
        ..id = 1
        ..style = SWT.DROP_DOWN
        ..enabled = true
        ..text = 'abc'
        ..items = ['abc', 'def']
        ..menu = menu
        ..bounds = _fieldBounds()),
};

/// The caret a field holds when it is right-clicked: inside the text, away from where the
/// right-click lands.
const _heldCaret = TextSelection.collapsed(offset: 1);

/// Gives [field]'s only editable text [_heldCaret], with or without the focus, and right-clicks its far
/// end, past the last character: the caret never leaves [_heldCaret], not even until the click ends,
/// and no caret report reaches Java ([calls] records what the field sends). Flutter's own field moves
/// it on every desktop platform when unfocused, and on macOS also when focused.
Future<void> _expectRightClickKeepsTheCaret(WidgetTester tester, Widget field, List<String> calls,
    {required bool focused}) async {
  await tester.pumpWidget(_app(SizedBox(width: _fieldWidth.toDouble(), height: _fieldHeight.toDouble(), child: field)));
  await tester.pump();

  final editable = find.byType(EditableText);
  final rect = tester.getRect(editable);
  final controller = tester.widget<EditableText>(editable).controller;
  if (focused) {
    tester.widget<EditableText>(editable).focusNode.requestFocus();
    await tester.pump();
  }
  controller.selection = _heldCaret;
  await tester.pump(_pastTheHover);
  expect(controller.selection, _heldCaret);

  final seen = <TextSelection>[];
  void record() {
    if (seen.isEmpty ? controller.selection != _heldCaret : controller.selection != seen.last) {
      seen.add(controller.selection);
    }
  }

  calls.clear();
  controller.addListener(record);
  await tester.tapAt(rect.centerRight - const Offset(3, 0), buttons: kSecondaryMouseButton);
  await tester.pumpAndSettle();
  controller.removeListener(record);

  expect(calls.where((c) => c == 'Verify/Verify'), isEmpty, reason: 'a caret report to Java');
  expect(calls.where((c) => c.startsWith('MenuDetect/')), hasLength(1), reason: 'MenuDetect still reaches Java');
  expect(seen, isEmpty, reason: 'the selections the field went through');
  expect(controller.selection, _heldCaret);
  expect(tester.widget<EditableText>(editable).focusNode.hasFocus, isTrue,
      reason: 'a right press focuses an edit field');
  expect(find.byType(AdaptiveTextSelectionToolbar), findsNothing, reason: "Flutter's own toolbar");
  await tester.pump(_pastTheHover);
}

/// A multi-line Text, drawn by a LineEditor rather than a TextField.
Widget _multiLineText(List<String> calls, {VMenu? menu}) => _RecordingTextSwt(
    calls: calls,
    value: VText()
      ..swt = 'Text'
      ..id = 1
      ..style = SWT.MULTI | SWT.BORDER
      ..enabled = true
      ..editable = true
      ..visible = true
      ..text = 'abc\ndef'
      ..menu = menu
      ..bounds = _bounds(_fieldWidth, 3 * _fieldHeight));

Future<LineEditor> _pumpMultiLine(WidgetTester tester, List<String> calls, {VMenu? menu}) async {
  await tester.pumpWidget(_app(SizedBox(
      width: _fieldWidth.toDouble(), height: 3.0 * _fieldHeight, child: _multiLineText(calls, menu: menu))));
  await tester.pump();
  return tester.widget<LineEditor>(find.byType(LineEditor));
}

/// Records every selection [controller] goes through after [held], until the returned stop is called.
List<TextSelection> Function() _recordSelections(LineEditorController controller, TextSelection held) {
  final seen = <TextSelection>[];
  void record() {
    if (seen.isEmpty ? controller.selection != held : controller.selection != seen.last) {
      seen.add(controller.selection);
    }
  }

  controller.addListener(record);
  return () {
    controller.removeListener(record);
    return seen;
  };
}

const _menuId = 100;

VMenu _menu() => VMenu()
  ..id = _menuId
  ..style = SWT.POP_UP
  ..enabled = true
  ..items = [
    VMenuItem()
      ..id = _menuId + 1
      ..style = SWT.PUSH
      ..enabled = true
      ..text = 'Context Item',
  ];

void main() {
  testWidgets('a right-click leaves a Text caret where it was', variant: TargetPlatformVariant.desktop(), (tester) async {
    final value = VText()
      ..swt = 'Text'
      ..id = 1
      ..style = 0
      ..enabled = true
      ..editable = true
      ..text = 'abc'
      ..bounds = _bounds(200, 30);
    await tester.pumpWidget(_app(SizedBox(width: 200, height: 30, child: TextSwt<VText>(value: value))));
    await tester.pump();

    final field = find.byType(EditableText);
    final controller = tester.widget<EditableText>(field).controller;
    expect(controller.selection.isValid, isFalse, reason: 'the field has never held a caret');

    await tester.tapAt(tester.getCenter(field), buttons: kSecondaryMouseButton);
    await tester.pumpAndSettle();

    expect(controller.selection, const TextSelection.collapsed(offset: 0));
    await tester.pump(_pastTheHover);
  });

  for (final MapEntry(key: name, value: build) in _editFields.entries) {
    for (final focused in [true, false]) {
      final holder = focused ? 'a focused' : 'an unfocused';
      testWidgets('a right-click never moves the caret $holder $name holds',
          variant: TargetPlatformVariant.desktop(), (tester) async {
        final calls = <String>[];
        await _expectRightClickKeepsTheCaret(tester, build(calls), calls, focused: focused);
      });
    }

    testWidgets('a right-click on the text of $name opens the Menu set on it',
        variant: TargetPlatformVariant.desktop(), (tester) async {
      await tester.pumpWidget(_app(SizedBox(
          width: _fieldWidth.toDouble(), height: _fieldHeight.toDouble(), child: build([], menu: _menu()))));
      await tester.pump();

      await tester.tapAt(tester.getCenter(find.byType(EditableText)), buttons: kSecondaryMouseButton);
      await tester.pump();
      await tester.pump();
      await ackMenuShown(tester, _menuId);

      expect(find.text('Context Item'), findsOneWidget);
      await tester.pump(_pastTheHover);
    });

    testWidgets('a Ctrl+Click on the text of $name opens its Menu on macOS and keeps the caret',
        variant: TargetPlatformVariant.only(TargetPlatform.macOS), (tester) async {
      await tester.pumpWidget(_app(SizedBox(
          width: _fieldWidth.toDouble(), height: _fieldHeight.toDouble(), child: build([], menu: _menu()))));
      await tester.pump();
      final editable = find.byType(EditableText);
      final controller = tester.widget<EditableText>(editable).controller;
      controller.selection = _heldCaret;
      await tester.pump(_pastTheHover);

      await tester.sendKeyDownEvent(LogicalKeyboardKey.controlLeft);
      await tester.tapAt(tester.getRect(editable).centerRight - const Offset(3, 0));
      await tester.pump();
      await tester.pump();
      await ackMenuShown(tester, _menuId);
      await tester.sendKeyUpEvent(LogicalKeyboardKey.controlLeft);

      expect(find.text('Context Item'), findsOneWidget);
      expect(controller.selection, _heldCaret);
      await tester.pump(_pastTheHover);
    });
  }

  for (final focused in [true, false]) {
    final holder = focused ? 'a focused' : 'an unfocused';
    testWidgets('a right-click never moves the caret $holder multi-line Text holds', semanticsEnabled: false,
        variant: TargetPlatformVariant.desktop(), (tester) async {
      final calls = <String>[];
      final editor = await _pumpMultiLine(tester, calls);
      final lines = editor.controller;
      if (focused) {
        editor.focusNode.requestFocus();
        await tester.pump();
      }
      lines.selection = _heldCaret;
      await tester.pump(_pastTheHover);

      calls.clear();
      final stop = _recordSelections(lines, _heldCaret);
      await tester.tapAt(tester.getRect(find.byType(LineEditor)).bottomRight - const Offset(3, 3),
          buttons: kSecondaryMouseButton);
      await tester.pumpAndSettle();

      expect(calls.where((c) => c == 'Verify/Verify'), isEmpty, reason: 'a caret report to Java');
      expect(calls.where((c) => c.startsWith('MenuDetect/')), hasLength(1), reason: 'MenuDetect still reaches Java');
      expect(stop(), isEmpty, reason: 'the selections the field went through');
      expect(lines.selection, _heldCaret);
      expect(editor.focusNode.hasFocus, isTrue, reason: 'a right press focuses an edit field');
      await tester.pump(_pastTheHover);
    });
  }

  testWidgets('a right-click on a multi-line Text opens the Menu set on it', semanticsEnabled: false,
      variant: TargetPlatformVariant.desktop(), (tester) async {
    await _pumpMultiLine(tester, [], menu: _menu());

    await tester.tapAt(tester.getCenter(find.byType(LineEditor)), buttons: kSecondaryMouseButton);
    await tester.pump();
    await tester.pump();
    await ackMenuShown(tester, _menuId);

    expect(find.text('Context Item'), findsOneWidget);
    await tester.pump(_pastTheHover);
  });

  testWidgets('a Ctrl+Click on a multi-line Text opens its Menu on macOS and keeps the caret', semanticsEnabled: false,
      variant: TargetPlatformVariant.only(TargetPlatform.macOS), (tester) async {
    final editor = await _pumpMultiLine(tester, [], menu: _menu());
    final lines = editor.controller;
    lines.selection = _heldCaret;
    await tester.pump(_pastTheHover);

    final stop = _recordSelections(lines, _heldCaret);
    await tester.sendKeyDownEvent(LogicalKeyboardKey.controlLeft);
    await tester.tapAt(tester.getRect(find.byType(LineEditor)).bottomRight - const Offset(3, 3));
    await tester.pump();
    await tester.pump();
    await ackMenuShown(tester, _menuId);
    await tester.sendKeyUpEvent(LogicalKeyboardKey.controlLeft);

    expect(find.text('Context Item'), findsOneWidget);
    expect(stop(), isEmpty, reason: 'the selections the field went through');
    expect(lines.selection, _heldCaret);
    await tester.pump(_pastTheHover);
  });

  testWidgets('a plain, double and triple click on a Text still reach the field',
      variant: TargetPlatformVariant.desktop(), (tester) async {
    final calls = <String>[];
    await tester.pumpWidget(_app(SizedBox(
        width: _fieldWidth.toDouble(), height: _fieldHeight.toDouble(),
        child: _RecordingTextSwt(
            calls: calls,
            value: VText()
              ..swt = 'Text'
              ..id = 1
              ..style = SWT.BORDER
              ..enabled = true
              ..editable = true
              ..text = 'abc def'
              ..bounds = _fieldBounds()))));
    await tester.pump();
    final editable = find.byType(EditableText);
    final controller = tester.widget<EditableText>(editable).controller;
    final rect = tester.getRect(editable);
    final end = rect.centerRight - const Offset(3, 0);

    // Far from the clicks that follow, so they start a sequence of their own.
    await tester.tapAt(rect.centerLeft + const Offset(1, 0));
    await tester.pump(_pastTheHover);
    expect((controller.selection.isCollapsed, controller.selection.baseOffset), (true, 0),
        reason: 'a click places the caret');

    await tester.tapAt(end);
    await tester.pump(const Duration(milliseconds: 50));
    await tester.tapAt(end);
    await tester.pump(const Duration(milliseconds: 50));
    expect(controller.selection, const TextSelection(baseOffset: 4, extentOffset: 7),
        reason: 'a double click selects the word');

    await tester.tapAt(end);
    await tester.pumpAndSettle();
    expect(controller.selection, const TextSelection(baseOffset: 0, extentOffset: 7),
        reason: 'a triple click selects the line');
    await tester.pump(_pastTheHover);
  });

  testWidgets('closing a Text context Menu with Escape leaves the selection it held',
      variant: TargetPlatformVariant.desktop(), (tester) async {
    final calls = <String>[];
    await tester.pumpWidget(_app(SizedBox(
        width: _fieldWidth.toDouble(), height: _fieldHeight.toDouble(),
        child: _editFields['Text']!(calls, menu: _menu()))));
    await tester.pump();
    final editable = find.byType(EditableText);
    final controller = tester.widget<EditableText>(editable).controller;
    tester.widget<EditableText>(editable).focusNode.requestFocus();
    await tester.pump();
    const held = TextSelection(baseOffset: 1, extentOffset: 2);
    controller.selection = held;
    await tester.pump(_pastTheHover);

    await tester.tapAt(tester.getCenter(editable), buttons: kSecondaryMouseButton);
    await tester.pump();
    await tester.pump();
    await ackMenuShown(tester, _menuId);
    expect(find.text('Context Item'), findsOneWidget);

    calls.clear();
    await tester.sendKeyEvent(LogicalKeyboardKey.escape);
    await tester.pumpAndSettle();

    expect(find.text('Context Item'), findsNothing);
    expect(tester.widget<EditableText>(editable).focusNode.hasFocus, isTrue);
    expect(controller.selection, held);
    expect(calls.where((c) => c == 'Verify/Verify'), isEmpty, reason: 'a selection report to Java');
    await tester.pump(_pastTheHover);
  });

  testWidgets('a Text the focus comes back to after its Menu sent it elsewhere selects all again',
      variant: TargetPlatformVariant.desktop(), (tester) async {
    final other = FocusNode(debugLabel: 'other control');
    addTearDown(other.dispose);
    await tester.pumpWidget(_app(Column(mainAxisSize: MainAxisSize.min, children: [
      SizedBox(
          width: _fieldWidth.toDouble(), height: _fieldHeight.toDouble(),
          child: _editFields['Text']!([], menu: _menu())),
      Focus(focusNode: other, child: const SizedBox(width: 10, height: 10)),
    ])));
    await tester.pump();
    final editable = find.byType(EditableText);
    final field = tester.widget<EditableText>(editable);
    field.focusNode.requestFocus();
    await tester.pump();
    field.controller.selection = const TextSelection.collapsed(offset: 1);
    await tester.pump(_pastTheHover);

    await tester.tapAt(tester.getCenter(editable), buttons: kSecondaryMouseButton);
    await tester.pump();
    await tester.pump();
    await ackMenuShown(tester, _menuId);
    // What a chosen item's action does: it focuses another control while the menu is still up.
    other.requestFocus();
    await tester.pumpAndSettle();
    expect(field.focusNode.hasFocus, isFalse);
    if (find.text('Context Item').evaluate().isNotEmpty) {
      await tester.tapAt(Offset.zero + const Offset(500, 500));
      await tester.pumpAndSettle();
    }
    other.requestFocus();
    await tester.pumpAndSettle();

    field.focusNode.requestFocus();
    await tester.pumpAndSettle();
    expect(field.controller.selection, const TextSelection(baseOffset: 0, extentOffset: 3));
    await tester.pump(_pastTheHover);
  });

  testWidgets('a right-click on a Combo that never held a caret takes Java\'s and reports none',
      variant: TargetPlatformVariant.desktop(), (tester) async {
    final calls = <String>[];
    await tester.pumpWidget(_app(SizedBox(
        width: _fieldWidth.toDouble(), height: _fieldHeight.toDouble(),
        child: _RecordingComboSwt(
            calls: calls,
            value: VCombo()
              ..swt = 'Combo'
              ..id = 1
              ..style = SWT.DROP_DOWN
              ..enabled = true
              ..text = 'alpha bravo'
              ..items = ['alpha bravo']
              ..bounds = _fieldBounds()))));
    await tester.pump();
    final editable = find.byType(EditableText);
    final controller = tester.widget<EditableText>(editable).controller;
    expect(controller.selection.isValid, isFalse, reason: 'the field has never held a caret');

    await tester.tapAt(tester.getCenter(editable), buttons: kSecondaryMouseButton);
    await tester.pumpAndSettle();

    expect(tester.widget<EditableText>(editable).focusNode.hasFocus, isTrue);
    expect(controller.selection, const TextSelection.collapsed(offset: 0));
    expect(calls.where((c) => c == 'Verify/Verify'), isEmpty, reason: 'a caret report to Java');
    await tester.pump(_pastTheHover);
  });

  testWidgets('a right-click on a Scale reports no Selection', (tester) async {
    final calls = <String>[];
    final value = VScale()
      ..id = 1
      ..style = SWT.HORIZONTAL
      ..enabled = true
      ..minimum = 0
      ..maximum = 100
      ..selection = 50
      ..bounds = _bounds(300, 40);
    await tester.pumpWidget(_app(SizedBox(width: 300, height: 40, child: _RecordingScaleSwt(value: value, calls: calls))));
    await tester.pump();

    await tester.tapAt(tester.getCenter(find.byType(_RecordingScaleSwt)), buttons: kSecondaryMouseButton);
    await tester.pump();

    expect(calls.where((c) => c.startsWith('Selection/')), isEmpty);
    await tester.pump(_pastTheHover);
  });

  testWidgets('a right-click on a Table row is pressed where the pointer is, before MenuDetect',
      (tester) async {
    final calls = <String>[];
    final value = VTable()
      ..id = 1
      ..style = SWT.NONE
      ..enabled = true
      ..headerVisible = false
      ..linesVisible = false
      ..columns = [
        VTableColumn()
          ..id = 2
          ..width = 400
          ..alignment = SWT.LEFT
          ..text = 'Col'
      ]
      ..items = [
        VTableItem()
          ..id = 10
          ..texts = ['Row 0'],
      ]
      ..bounds = _bounds(800, 400);
    await tester.pumpWidget(_app(SizedBox(width: 800, height: 400, child: _RecordingTableSwt(value: value, calls: calls))));
    await tester.pump();

    final origin = tester.getTopLeft(find.byType(_RecordingTableSwt));
    final at = tester.getCenter(find.text('Row 0')) + const Offset(30, 0);
    await tester.tapAt(at, buttons: kSecondaryMouseButton);
    await tester.pump();

    final local = at - origin;
    final press = calls.indexWhere((c) => c.startsWith('Mouse/MouseDown:3'));
    final menu = calls.indexWhere((c) => c.startsWith('MenuDetect/'));
    expect(press, isNonNegative);
    expect(calls[press], 'Mouse/MouseDown:3@${local.dx.round()},${local.dy.round()}');
    expect(menu, greaterThan(press));
    await tester.pump(_pastTheHover);
  });
}
