// The buttons a MessageBox shows: one for each id of its style, labelled with what
// the Java side sends in `buttonLabels` (the labels set with setButtonLabels, else
// SWT's own), laid out the same on every OS, and pressing one answers that id.

import 'dart:convert';
import 'dart:typed_data';

import 'package:flutter/foundation.dart';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/comm/comm_frame.dart';
import 'package:swtflutter/src/comm/comm_ws.dart';
import 'package:swtflutter/src/comm/widget_wire.dart';
import 'package:swtflutter/src/gen/display.dart';
import 'package:swtflutter/src/gen/messagebox.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/shell.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/impl/display_evolve.dart';
import 'package:swtflutter/src/impl/messagebox_evolve.dart';
import 'package:swtflutter/src/impl/messagebox_layout.dart';

/// What the Java side sends, in the order OK, Cancel, Yes, No, Abort, Retry, Ignore.
const List<String?> _swtLabels = ['OK', 'Cancel', 'Yes', 'No', 'Abort', 'Retry', 'Ignore'];

List<(String, int, bool)> _shown(int style, [List<String?>? labels]) =>
    messageBoxButtons(style, labels).map((b) => (b.label, b.id, b.primary)).toList();

List<int> _order(int style) => _shown(style).map((b) => b.$2).toList();

int _default(int style) => _shown(style).singleWhere((b) => b.$3).$2;

VRectangle _rect(int x, int y, int w, int h) => VRectangle()
  ..x = x
  ..y = y
  ..width = w
  ..height = h;

void main() {
  test('a box with one button shows that button and answers its id', () {
    expect(_shown(SWT.OK, _swtLabels), [('OK', SWT.OK, true)]);
    expect(_shown(SWT.CANCEL, _swtLabels), [('Cancel', SWT.CANCEL, true)]);
    expect(_shown(SWT.YES, _swtLabels), [('Yes', SWT.YES, true)]);
    expect(_shown(SWT.NO, _swtLabels), [('No', SWT.NO, true)]);
  });

  test('the buttons sit in one order on every OS, the affirmative one rightmost', () {
    expect(_order(SWT.OK | SWT.CANCEL), [SWT.CANCEL, SWT.OK]);
    expect(_order(SWT.YES | SWT.NO), [SWT.NO, SWT.YES]);
    expect(_order(SWT.YES | SWT.NO | SWT.CANCEL), [SWT.CANCEL, SWT.NO, SWT.YES]);
    expect(_order(SWT.RETRY | SWT.CANCEL), [SWT.CANCEL, SWT.RETRY]);
    expect(_order(SWT.ABORT | SWT.RETRY | SWT.IGNORE), [SWT.IGNORE, SWT.RETRY, SWT.ABORT]);
  });

  test('the default button is the affirmative one, as on Cocoa and Win32', () {
    expect(_default(SWT.OK | SWT.CANCEL), SWT.OK);
    expect(_default(SWT.YES | SWT.NO), SWT.YES);
    expect(_default(SWT.YES | SWT.NO | SWT.CANCEL), SWT.YES);
    expect(_default(SWT.RETRY | SWT.CANCEL), SWT.RETRY);
    expect(_default(SWT.ABORT | SWT.RETRY | SWT.IGNORE), SWT.ABORT);
  });

  test('Escape presses Cancel where there is one, and nothing in a lone-OK box, as on Cocoa and GTK', () {
    expect(messageBoxLayout(SWT.OK | SWT.CANCEL).escapeId, SWT.CANCEL);
    expect(messageBoxLayout(SWT.YES | SWT.NO | SWT.CANCEL).escapeId, SWT.CANCEL);
    expect(messageBoxLayout(SWT.RETRY | SWT.CANCEL).escapeId, SWT.CANCEL);
    expect(messageBoxLayout(SWT.CANCEL).escapeId, SWT.CANCEL);
    expect(messageBoxLayout(SWT.YES | SWT.NO).escapeId, isNull);
    expect(messageBoxLayout(SWT.ABORT | SWT.RETRY | SWT.IGNORE).escapeId, isNull);
    expect(messageBoxLayout(SWT.OK).escapeId, isNull);
  });

  test('a box without a valid set of buttons gets OK', () {
    expect(_order(SWT.ICON_WARNING | SWT.APPLICATION_MODAL), [SWT.OK]);
    expect(_order(SWT.YES | SWT.RETRY), [SWT.OK]);
  });

  test('the labels the Java side sends name the buttons', () {
    final labels = List<String?>.of(_swtLabels);
    labels[2] = 'Save';
    labels[3] = "Don't Save";
    expect(_shown(SWT.YES | SWT.NO | SWT.CANCEL, labels), [
      ('Cancel', SWT.CANCEL, false),
      ("Don't Save", SWT.NO, false),
      ('Save', SWT.YES, true),
    ]);
    expect(_shown(SWT.RETRY | SWT.CANCEL, ['Fine', 'Annuler', null, null, null, 'Réessayer', null]), [
      ('Annuler', SWT.CANCEL, false),
      ('Réessayer', SWT.RETRY, true),
    ]);
  });

  test('a label that does not arrive falls back to the English one', () {
    expect(_shown(SWT.ABORT | SWT.RETRY | SWT.IGNORE).map((b) => b.$1), ['Ignore', 'Retry', 'Abort']);
    expect(_shown(SWT.OK | SWT.CANCEL, [null, 'Stop']).map((b) => b.$1), ['Stop', 'OK']);
  });

  test('the labels travel in the value', () {
    final written = VMessageBox()
      ..swt = 'MessageBox'
      ..id = 7
      ..style = SWT.YES | SWT.NO
      ..buttonLabels = ['OK', 'Cancel', 'Save', "Don't Save", 'Abort', 'Retry', 'Ignore'];
    final value = WidgetWireReader(Uint8List.fromList(WidgetWireWriter.encodeWith((w) {
      written.writeWire(w);
      w.u8(0);
    }))).fields(VMessageBox());
    expect(value.buttonLabels?[2], 'Save');
  });

  testWidgets('a box parked on the Shell shows its labelled buttons', (WidgetTester tester) async {
    final labels = List<String?>.of(_swtLabels);
    labels[1] = 'Close';
    await _showBox(tester, SWT.CANCEL | SWT.ICON_ERROR | SWT.APPLICATION_MODAL, labels);

    expect(find.text('Close'), findsOneWidget);
    expect(find.text('OK'), findsNothing, reason: 'a CANCEL box has no OK button');
    expect(find.text('Message'), findsOneWidget);
  });

  testWidgets('Return presses the default button', (WidgetTester tester) async {
    await _showBox(tester, SWT.YES | SWT.NO | SWT.CANCEL);
    await tester.sendKeyEvent(LogicalKeyboardKey.enter);
    await tester.pumpAndSettle();
    expect(_answers(), ['${SWT.YES}']);
    expect(find.text('Message'), findsNothing, reason: 'the box closes');
  }, variant: TargetPlatformVariant.desktop());

  testWidgets('Escape presses the Cancel button', (WidgetTester tester) async {
    await _showBox(tester, SWT.OK | SWT.CANCEL);
    await tester.sendKeyEvent(LogicalKeyboardKey.escape);
    await tester.pumpAndSettle();
    expect(_answers(), ['${SWT.CANCEL}']);
    expect(find.text('Message'), findsNothing, reason: 'the box closes');
  }, variant: TargetPlatformVariant.desktop());

  testWidgets('Escape leaves a box without a Cancel button open', (WidgetTester tester) async {
    await _showBox(tester, SWT.YES | SWT.NO);
    await tester.sendKeyEvent(LogicalKeyboardKey.escape);
    await tester.pumpAndSettle();
    expect(_answers(), isEmpty);
    expect(find.text('Message'), findsOneWidget);
  }, variant: TargetPlatformVariant.desktop());
}

/// Parks a box of [style] on a Shell, as the Java side does when the box opens, and starts
/// recording what the box answers.
Future<void> _showBox(WidgetTester tester, int style, [List<String?>? labels]) async {
  VShell shellValue({List<VMessageBox>? dialogs}) => VShell()
    ..id = 1
    ..style = SWT.SHELL_TRIM
    ..text = 'shell'
    ..bounds = _rect(0, 0, 800, 600)
    ..dialogs = dialogs;

  EquoCommBase.recordSentFrames = true;
  addTearDown(() => EquoCommBase.recordSentFrames = false);
  await tester.pumpWidget(
    EvolveApp(
      theme: ThemeMode.light,
      contentWidget: DisplaySwt(value: VDisplay()..shells = [shellValue()]),
    ),
  );
  await tester.pumpAndSettle();

  final box = VMessageBox()
    ..id = 4242
    ..swt = 'MessageBox'
    ..style = style
    ..text = 'Title'
    ..message = 'Message'
    ..buttonLabels = labels ?? _swtLabels;
  final shellState = tester.state(find.byType(ShellSwt<VShell>)) as dynamic;
  shellState.setValue(shellValue(dialogs: [box]));
  await tester.pumpAndSettle();
  EquoCommService.commForTesting.clearSentFrames();
}

/// The ids the box has answered with since it was shown.
List<String> _answers() => EquoCommService.commForTesting.sentFrames
    .map(EquoCommBase.decodeFrame)
    .where((f) => f.$1 == 'MessageBox/4242/close')
    .map((f) => jsonDecode(f.$2).toString())
    .toList();
