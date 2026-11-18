// SWT does not display the mnemonic indicator in a ToolItem label: `Startup &Options` shows as
// `Startup Options`, and a doubled `&&` is a single literal `&`. The size Java gives the item is
// measured without the '&' too.

import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/gen/toolbar.dart';
import 'package:swtflutter/src/gen/toolitem.dart';

const _cases = {
  'Startup &Options': 'Startup Options',
  'Save && Close': 'Save & Close',
};

const _styles = {'PUSH': SWT.PUSH, 'DROP_DOWN': SWT.DROP_DOWN};

VToolBar _toolBar(int style, String text) => VToolBar()
  ..id = 1
  ..style = SWT.FLAT | SWT.RIGHT
  ..enabled = true
  ..visible = true
  ..items = [
    VToolItem()
      ..id = 2
      ..style = style
      ..enabled = true
      ..text = text,
  ]
  ..bounds = (VRectangle()
    ..x = 0
    ..y = 0
    ..width = 300
    ..height = 40);

void main() {
  for (final MapEntry(key: styleName, value: style) in _styles.entries) {
    for (final MapEntry(key: raw, value: shown) in _cases.entries) {
      testWidgets('a $styleName ToolItem labelled "$raw" reads "$shown"', (tester) async {
        await tester.pumpWidget(EvolveApp(
          theme: ThemeMode.light,
          contentWidget: Align(
            alignment: Alignment.topLeft,
            child: SizedBox(width: 300, height: 40, child: ToolBarSwt(value: _toolBar(style, raw))),
          ),
        ));
        await tester.pumpAndSettle();

        expect(find.text(shown), findsOneWidget);
        expect(find.text(raw), findsNothing);
      });
    }
  }
}
