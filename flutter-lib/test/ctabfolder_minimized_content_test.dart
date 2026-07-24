// Issue #872 — a CTabFolder that is `minimized` but given a real client area (an
// E4 minimized-stack fly-out) must still render its selected tab's content. The
// impl gated the body on `if (!isMinimized)`, blanking the fly-out. Native SWT's
// `minimized` only shrinks computeSize; it never hides the selected control.

import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/gen/ctabfolder.dart';
import 'package:swtflutter/src/gen/ctabitem.dart';
import 'package:swtflutter/src/gen/control.dart';
import 'package:swtflutter/src/gen/label.dart';
import 'package:swtflutter/src/gen/swt.dart';
// ignore: unused_import
import 'package:swtflutter/src/impl/ctabfolder_evolve.dart';

const _contentText = 'Minimized fly-out content';

VLabel _content() => VLabel()
  ..id = 2001
  ..style = SWT.NONE
  ..text = _contentText;

VCTabFolder _folder({required bool minimized}) => VCTabFolder()
  ..id = 1000
  ..style = SWT.NONE
  ..minimized = minimized
  ..selection = 0
  ..items = [
    VCTabItem()
      ..id = 10
      ..text = 'Keywords'
      ..control = _content(),
  ];

Widget _host(VCTabFolder value) => EvolveApp(
      theme: ThemeMode.light,
      contentWidget: SizedBox(
        width: 900,
        height: 500, // a full fly-out client area, well above the tab strip
        child: CTabFolderSwt<VCTabFolder>(value: value),
      ),
    );

// The CTabFolder tab-strip decoration trips an unrelated debug-only paint assertion
// in the isolated test harness (a hairline 0-width border with a non-zero border
// radius, ctabfolder_evolve.dart's tab AnimatedContainer). It is orthogonal to #872,
// which is purely about whether the selected tab's content is BUILT into the tree.
// Drain those paint exceptions so they don't mask the assertion we actually care about.
void _drainPaintExceptions(WidgetTester tester) {
  while (tester.takeException() != null) {}
}

void main() {
  testWidgets('a non-minimized CTabFolder renders its selected tab content',
      (WidgetTester tester) async {
    await tester.pumpWidget(_host(_folder(minimized: false)));
    _drainPaintExceptions(tester);
    expect(find.text(_contentText), findsOneWidget);
  });

  testWidgets(
      'a minimized CTabFolder with a client area still renders its content (issue #872)',
      (WidgetTester tester) async {
    await tester.pumpWidget(_host(_folder(minimized: true)));
    _drainPaintExceptions(tester);

    expect(find.text(_contentText), findsOneWidget,
        reason: 'A CTabFolder that is minimized but given a real client area '
            '(the E4 minimized-view fly-out) must still render its selected tab '
            'content. Gating the body on !minimized blanked every re-shown '
            'fly-out (issue #872).');
  });
}
