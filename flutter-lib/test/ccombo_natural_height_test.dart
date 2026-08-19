// A CCombo given no explicit bounds (the pass computeSize()/measure_ccombo.dart captures) must
// render at a compact, native-like height. If it inflates to Material's default text-field
// baseline height instead, that inflated number gets baked into CComboSizes' MIN_HEIGHT and any
// layout that sizes the combo from computeSize() (e.g. a ToolBar item) renders it far taller than
// the row that holds it.

import 'package:flutter/material.dart';
import 'package:flutter/widgets.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/gen/ccombo.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/impl/ccombo_evolve.dart';

Widget _app(Widget child) => EvolveApp(
      theme: ThemeMode.light,
      contentWidget: child,
    );

void main() {
  testWidgets('a bare READ_ONLY CCombo renders at a compact, native-like height',
      (tester) async {
    final key = GlobalKey<CComboImpl>();

    final value = VCCombo()
      ..swt = 'CCombo'
      ..id = 1
      ..style = SWT.READ_ONLY
      ..enabled = true
      ..items = const ['One', 'Two'];

    await tester.pumpWidget(_app(
      Center(
        child: CComboSwt<VCCombo>(key: key, value: value),
      ),
    ));
    await tester.pumpAndSettle();

    final size = tester.getSize(find.byKey(key));
    // Native SWT CCombo is ~24-26px tall; Material's uncollapsed default text-field height is
    // ~48-56px. A tight band around native rules out the inflated-default-height regression
    // while leaving room for minor font-metric variance.
    expect(size.height, inInclusiveRange(20, 30),
        reason: 'CCombo natural height must stay close to native (~24-26px), not fall back to '
            "Material's default text-field height");
  });
}
