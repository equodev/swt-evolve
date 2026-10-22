// A popup Java activates on a field (JFace PopupDialog: shell, then its filter Text) keeps that field
// focused whatever order the activation and the focus request arrive in.

import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/gen/composite.dart';
import 'package:swtflutter/src/gen/display.dart';
import 'package:swtflutter/src/gen/label.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/shell.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/gen/table.dart';
import 'package:swtflutter/src/gen/tableitem.dart';
import 'package:swtflutter/src/gen/text.dart';
import 'package:swtflutter/src/impl/display_evolve.dart';
import 'package:swtflutter/src/impl/focus_requests.dart';

import 'delivery/support/deliver.dart';

const int base = 7600;
const int mainShellId = base + 1;
const int mainFieldId = base + 2;
const int popupId = base + 3;
const int filterId = base + 5;

VRectangle _rect(int x, int y, int w, int h) => VRectangle()
  ..x = x
  ..y = y
  ..width = w
  ..height = h;

VText _text(int id, String text, VRectangle bounds) => VText()
  ..swt = 'Text'
  ..id = id
  ..style = SWT.SINGLE
  ..enabled = true
  ..visible = true
  ..editable = true
  ..text = text
  ..bounds = bounds;

VShell _mainShell() => VShell()
  ..swt = 'Shell'
  ..id = mainShellId
  ..style = SWT.SHELL_TRIM
  ..text = 'workbench'
  ..bounds = _rect(0, 0, 800, 600)
  ..children = [_text(mainFieldId, 'main', _rect(10, 10, 200, 24))];

/// Quick Access: a trim-less resizable popup, the filter in a title composite, a separator, then the
/// result table.
VShell _popup() => VShell()
  ..swt = 'Shell'
  ..id = popupId
  ..style = SWT.RESIZE | SWT.ON_TOP | SWT.TOOL | SWT.NO_TRIM
  ..bounds = _rect(200, 100, 400, 300)
  ..children = [
    VComposite()
      ..swt = 'Composite'
      ..id = base + 4
      ..style = SWT.NONE
      ..enabled = true
      ..visible = true
      ..bounds = _rect(0, 0, 400, 30)
      ..children = [_text(filterId, 'filter', _rect(4, 4, 392, 22))],
    VLabel()
      ..swt = 'Label'
      ..id = base + 8
      ..style = SWT.SEPARATOR | SWT.HORIZONTAL
      ..enabled = true
      ..visible = true
      ..bounds = _rect(0, 30, 400, 2),
    VTable()
      ..swt = 'Table'
      ..id = base + 6
      ..style = SWT.SINGLE | SWT.FULL_SELECTION
      ..enabled = true
      ..visible = true
      ..items = [VTableItem()..id = base + 7..texts = ['Show View']]
      ..bounds = _rect(0, 32, 400, 268),
  ];

VDisplay _display({required List<VShell> shells, required int activeShellId}) => VDisplay()
  ..swt = 'Display'
  ..id = base
  ..shells = shells
  ..activeShellId = activeShellId;

String? _focusedFieldText(WidgetTester tester) {
  for (final field in tester.widgetList<EditableText>(find.byType(EditableText))) {
    if (field.focusNode.hasPrimaryFocus) return field.controller.text;
  }
  return null;
}

/// The app showing only the workbench shell, active.
Future<void> _openMainShell(WidgetTester tester) async {
  await tester.pumpWidget(EvolveApp(
    theme: ThemeMode.light,
    contentWidget: DisplaySwt(
      value: _display(shells: [_mainShell()], activeShellId: mainShellId),
    ),
  ));
  await tester.pumpAndSettle();
}

/// The Display update that shows the popup over the workbench, with [activeShellId] active.
Future<void> _showPopup({required int activeShellId}) => deliverFrame(
      'Display/$base',
      _display(shells: [_mainShell(), _popup()], activeShellId: activeShellId).toJson(),
    );

/// Settles, then one more frame for a focus the shell defers past the frame it is built in.
Future<void> _settle(WidgetTester tester) async {
  await tester.pumpAndSettle();
  await tester.pump();
}

void main() {
  setUp(() => FocusRequests.instance.reset());
  tearDown(() => FocusRequests.instance.reset());

  testWidgets('a popup SWT activated on its filter field gives that field the keyboard',
      (tester) async {
    await _openMainShell(tester);

    // Java's order: the Display update that activates the popup, then the focus request behind it.
    await _showPopup(activeShellId: popupId);
    FocusRequests.instance.request(filterId);
    await _settle(tester);

    expect(_focusedFieldText(tester), 'filter',
        reason: 'typing right after Quick Access opens must land in its filter field');
  });

  testWidgets('the field keeps the keyboard when the shell is activated after the field took it',
      (tester) async {
    await _openMainShell(tester);

    // The popup is on screen and its field already holds the keyboard when the client gets round to
    // activating the shell: the activation must not move the keyboard on to the next control.
    await _showPopup(activeShellId: mainShellId);
    FocusRequests.instance.request(filterId);
    await _settle(tester);
    expect(_focusedFieldText(tester), 'filter', reason: 'sanity: the request focused the field');

    await _showPopup(activeShellId: popupId);
    await _settle(tester);

    expect(_focusedFieldText(tester), 'filter',
        reason: "the shell's own initial focus must not move the keyboard off the field it opened on");
  });

  testWidgets('the field keeps the keyboard when its request and the activation are applied together',
      (tester) async {
    await _openMainShell(tester);
    await _showPopup(activeShellId: mainShellId);
    await tester.pumpAndSettle();

    // The field is already built: its request is queued for the end of the frame before the shell
    // queues its own initial focus, and a requested focus only takes effect after both have run.
    FocusRequests.instance.request(filterId);
    await _showPopup(activeShellId: popupId);
    await _settle(tester);

    expect(_focusedFieldText(tester), 'filter',
        reason: "the shell's own initial focus must not move the keyboard off the requested field");
  });

  testWidgets('the field keeps the keyboard when the request lands in the same frame as the activation',
      (tester) async {
    await _openMainShell(tester);

    // Both messages are handled before the next frame is built.
    await _showPopup(activeShellId: popupId);
    FocusRequests.instance.request(filterId);
    await tester.pump();
    await _settle(tester);

    expect(_focusedFieldText(tester), 'filter',
        reason: "the shell's own initial focus must not move the keyboard off the requested field");
  });
}
