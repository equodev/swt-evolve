// SWT parity: `Shell.setVisible(true)` shows a shell without activating it, so the control that
// opened it keeps the keyboard. JFace relies on that for content assist — `ContentAssistant`
// drives its `SWT.ON_TOP` proposal popup from a `VerifyKeyListener` on the StyledText, which only
// fires while the editor still holds the keyboard. A shell that grabbed focus on mount took every
// keystroke away from the editor.
//
// `VDisplay.activeShellId` carries SWT's active shell, and only that shell takes focus here.
//
// A click inside the popup activates it too, and Java then reports it as the active shell. That
// report must leave the keyboard where the click put it: moving it on lands on the popup's status
// Label, and JFace's `PopupCloser` takes an `SWT.Activate` from a Label as focus leaving the
// proposal list and hides the additional-info popup beside it.

import 'dart:convert';
import 'dart:typed_data';

import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
// The io transport directly: `flutter test` runs on the VM, where comm.dart resolves to it —
// but the analyzer resolves the conditional export to the web variant, which has no test hook.
import 'package:swtflutter/src/comm/comm_ws.dart';
import 'package:swtflutter/src/gen/composite.dart';
import 'package:swtflutter/src/gen/control.dart';
import 'package:swtflutter/src/gen/display.dart';
import 'package:swtflutter/src/gen/label.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/shell.dart';
import 'package:swtflutter/src/gen/styledtext.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/gen/table.dart';
import 'package:swtflutter/src/gen/tableitem.dart';
import 'package:swtflutter/src/gen/text.dart';
import 'package:swtflutter/src/impl/display_evolve.dart';
import 'package:swtflutter/src/impl/focus_requests.dart';
import 'package:swtflutter/src/impl/key_forwarding.dart';
import 'package:swtflutter/src/comm/v_registry.dart';
import 'package:swtflutter/test_harness.dart';

// DisplaySwt caches the last state it saw per display id, so each test uses its own id —
// otherwise the second one starts with the first one's popup already mounted.
int _idsFor(int n) => 7000 + n * 10;

VRectangle _rect(int x, int y, int w, int h) => VRectangle()
  ..x = x
  ..y = y
  ..width = w
  ..height = h;

VStyledText _editor(int base) => VStyledText()
  ..swt = 'StyledText'
  ..id = base + 3
  ..style = 0
  ..enabled = true
  ..editable = true
  ..caretOffset = 0
  ..text = 'String s = "hello";'
  ..bounds = _rect(0, 0, 400, 200);

VShell _mainShell(int base) => VShell()
  ..swt = 'Shell'
  ..id = base + 1
  ..style = SWT.SHELL_TRIM
  ..text = 'workbench'
  ..bounds = _rect(0, 0, 800, 600)
  ..children = [_editor(base)];

/// The JFace proposal popup: trim-less, always on top, shown with `setVisible(true)`.
VShell _popupShell(int base, {List<VControl> children = const []}) => VShell()
  ..swt = 'Shell'
  ..id = base + 2
  ..style = SWT.ON_TOP | SWT.RESIZE
  ..bounds = _rect(120, 140, 300, 200)
  ..children = children;

/// What `CompletionProposalPopup` puts in it: the proposal Table above the status Label.
List<VControl> _proposalList(int base) => [
  VTable()
    ..swt = 'Table'
    ..id = base + 4
    ..style = SWT.SINGLE | SWT.FULL_SELECTION
    ..enabled = true
    ..visible = true
    ..items = [
      for (final (i, text) in const [
        'closure - closure block',
        'if - if statement',
      ].indexed)
        VTableItem()
          ..swt = 'TableItem'
          ..id = base + 6 + i
          ..texts = [text],
    ]
    ..selection = <int>[0]
    ..bounds = _rect(0, 0, 300, 170),
  VLabel()
    ..swt = 'Label'
    ..id = base + 5
    ..style = SWT.NONE
    ..enabled = true
    ..visible = true
    ..text = "Press 'Tab' from proposal table or click/hover for focus"
    ..bounds = _rect(0, 170, 300, 30),
];

VDisplay _display(
  int base, {
  required List<VShell> shells,
  required int activeShellId,
}) => VDisplay()
  ..swt = 'Display'
  ..id = base
  ..shells = shells
  ..activeShellId = activeShellId;

/// Java describing the display, as the record its update arrives in: the only way a Display update
/// reaches a mounted DisplaySwt, which keeps its own state rather than taking it from a parent.
void _javaDescribesDisplay(String channel, VDisplay display) {
  final id = int.parse(channel.split('/')[1]);
  VRegistry.instance.deliverHolderForTesting('Display', id, display);
}

void main() {
  /// Brings up the workbench with the editor holding the keyboard, then shows a second shell
  /// and reports whether the editor still holds it.
  Future<bool> editorKeepsKeysWhenShellAppears(
    WidgetTester tester, {
    required int base,
    required bool popupIsActivated,
  }) async {
    await tester.pumpWidget(
      EvolveApp(
        theme: ThemeMode.light,
        contentWidget: DisplaySwt(
          value: _display(
            base,
            shells: [_mainShell(base)],
            activeShellId: base + 1,
          ),
        ),
      ),
    );
    await tester.pumpAndSettle();

    // Click into the editor, as the user does before asking for content assist.
    await tester.tapAt(const Offset(60, 40));
    // Java focuses the pressed editor and asks the client to move its keyboard focus there.
    FocusRequests.instance.request(base + 3);
    addTearDown(FocusRequests.instance.reset);
    await tester.pumpAndSettle();
    expect(
      canvasEditorFocused,
      isTrue,
      reason: 'sanity: a clicked StyledText holds the keyboard',
    );

    _javaDescribesDisplay(
      'Display/$base',
      _display(
        base,
        shells: [_mainShell(base), _popupShell(base)],
        activeShellId: popupIsActivated ? base + 2 : base + 1,
      ),
    );
    await tester.pumpAndSettle();

    return canvasEditorFocused;
  }

  testWidgets('a shell SWT did not activate leaves the keyboard with the editor', (
    tester,
  ) async {
    expect(
      await editorKeepsKeysWhenShellAppears(
        tester,
        base: _idsFor(1),
        popupIsActivated: false,
      ),
      isTrue,
      reason:
          'the content assist popup is shown without activating its shell, so the '
          "editor must keep the keyboard — that is the only path that reaches ContentAssistant's "
          'VerifyKeyListener',
    );
  });

  testWidgets(
    'the proposal list and its documentation popup leave the keyboard with the editor',
    (tester) async {
      final base = _idsFor(5);
      await tester.pumpWidget(
        EvolveApp(
          theme: ThemeMode.light,
          contentWidget: DisplaySwt(
            value: _display(base, shells: [_mainShell(base)], activeShellId: base + 1),
          ),
        ),
      );
      await tester.pumpAndSettle();
      await tester.tapAt(const Offset(60, 40));
      FocusRequests.instance.request(base + 3);
      addTearDown(FocusRequests.instance.reset);
      await tester.pumpAndSettle();
      expect(canvasEditorFocused, isTrue, reason: 'sanity: a clicked StyledText holds the keyboard');

      final proposals = _popupShell(base, children: _proposalList(base));
      _javaDescribesDisplay(
        'Display/$base',
        _display(base, shells: [_mainShell(base), proposals], activeShellId: base + 1),
      );
      await tester.pumpAndSettle();
      expect(canvasEditorFocused, isTrue, reason: 'the proposal list alone');
      expect(queryPrimaryFocus(), 'StyledText/${base + 3}');

      final info = VShell()
        ..swt = 'Shell'
        ..id = base + 20
        ..style = SWT.ON_TOP | SWT.TOOL | SWT.RESIZE
        ..bounds = _rect(420, 140, 300, 200)
        ..children = [
          VComposite()
            ..swt = 'Composite'
            ..id = base + 21
            ..style = SWT.NONE
            ..enabled = true
            ..visible = true
            ..bounds = _rect(0, 0, 300, 200),
        ];
      _javaDescribesDisplay(
        'Display/$base',
        _display(base, shells: [_mainShell(base), proposals, info], activeShellId: base + 1),
      );
      await tester.pumpAndSettle();
      expect(queryPrimaryFocus(), 'StyledText/${base + 3}', reason: 'with the documentation popup');
      expect(canvasEditorFocused, isTrue, reason: 'with the documentation popup');
    },
  );

  testWidgets('a shell SWT activated does take the keyboard', (tester) async {
    expect(
      await editorKeepsKeysWhenShellAppears(
        tester,
        base: _idsFor(2),
        popupIsActivated: true,
      ),
      isFalse,
      reason:
          'a shell opened with Shell.open() is the active shell and must take focus '
          'away from the editor, as it did before',
    );
  });

  /// Shows the workbench with the proposal popup, not yet active, holding [popupChildren].
  Future<void> showProposalPopup(
    WidgetTester tester,
    int base,
    List<VControl> popupChildren,
  ) async {
    await tester.pumpWidget(
      EvolveApp(
        theme: ThemeMode.light,
        contentWidget: DisplaySwt(
          value: _display(
            base,
            shells: [
              _mainShell(base),
              _popupShell(base, children: popupChildren),
            ],
            activeShellId: base + 1,
          ),
        ),
      ),
    );
    await tester.pumpAndSettle();
  }

  void javaActivatesPopup(int base, List<VControl> popupChildren) =>
      _javaDescribesDisplay(
        'Display/$base',
        _display(
          base,
          shells: [
            _mainShell(base),
            _popupShell(base, children: popupChildren),
          ],
          activeShellId: base + 2,
        ),
      );

  bool focusIsUnderLabel() =>
      FocusManager.instance.primaryFocus?.context
          ?.findAncestorWidgetOfExactType<LabelSwt<VLabel>>() !=
      null;

  testWidgets(
    'a click that activates the popup does not leave the status Label focused',
    (tester) async {
      final base = _idsFor(3);
      final children = _proposalList(base);
      await showProposalPopup(tester, base, children);

      await tester.tap(find.text('if - if statement'));
      await tester.pumpAndSettle();
      javaActivatesPopup(base, children);
      await tester.pumpAndSettle();

      expect(
        focusIsUnderLabel(),
        isFalse,
        reason:
            'the status Label is no Tab stop; focused, it makes JFace hide the '
            'additional-info popup',
      );
    },
  );

  testWidgets('activating the popup keeps the focus already inside it', (
    tester,
  ) async {
    final base = _idsFor(4);
    final children = [
      ..._proposalList(base),
      VText()
        ..swt = 'Text'
        ..id = base + 9
        ..style = SWT.SINGLE | SWT.BORDER
        ..enabled = true
        ..visible = true
        ..text = ''
        ..bounds = _rect(0, 200, 300, 24),
    ];
    await showProposalPopup(tester, base, children);

    // On web a click into the popup leaves its own node focused (the client reports Shell
    // FocusIn), with a focusable Text after it for traversal to land on.
    final popupNode = tester
        .widget<Focus>(
          find.descendant(
            of: find.byWidgetPredicate(
              (w) => w is ShellSwt && w.value.id == base + 2,
            ),
            matching: find.byWidgetPredicate(
              (w) => w is Focus && w.focusNode?.debugLabel == 'CanvasKeyboard',
            ),
          ),
        )
        .focusNode!;
    popupNode.requestFocus();
    await tester.pumpAndSettle();
    javaActivatesPopup(base, children);
    await tester.pumpAndSettle();

    expect(
      popupNode.hasPrimaryFocus,
      isTrue,
      reason:
          'the activation report must not hand the focus on to the next control',
    );
  });
}
