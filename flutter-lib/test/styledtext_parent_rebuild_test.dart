import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/styledtext.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/impl/styledtext_evolve.dart';

import 'delivery/support/deliver.dart';

/// A StyledText is laid out again for what it shows, not because the widget holding it rebuilt: a
/// CTabFolder rebuilds every page it holds when another tab is picked.
void main() {
  VStyledText value({String text = 'alpha beta gamma'}) => VStyledText()
    ..swt = 'StyledText'
    ..id = 1
    ..style = SWT.H_SCROLL | SWT.V_SCROLL
    ..enabled = true
    ..editable = true
    ..caretOffset = 0
    ..text = text
    ..bounds = (VRectangle()
      ..x = 0
      ..y = 0
      ..width = 300
      ..height = 200);

  testWidgets('a parent that rebuilds does not lay the text out again; a new value does',
      (tester) async {
    final key = GlobalKey<StyledTextImpl>();
    late StateSetter rebuildParent;
    await tester.pumpWidget(EvolveApp(
      theme: ThemeMode.light,
      contentWidget: StatefulBuilder(builder: (context, setState) {
        rebuildParent = setState;
        return SizedBox(
            width: 300, height: 200, child: StyledTextSwt<VStyledText>(key: key, value: value()));
      }),
    ));
    await deliverWhole(value()..seq = 2);
    await tester.pump();
    final laidOut = StyledTextImpl.debugRebuildCalls;

    rebuildParent(() {});
    await tester.pump();
    expect(StyledTextImpl.debugRebuildCalls, laidOut, reason: 'nothing it shows changed');

    await deliverWhole(value(text: 'alpha beta gamma delta')..seq = 3);
    await tester.pump();
    expect(StyledTextImpl.debugRebuildCalls, laidOut + 1);
    expect(key.currentState!.state.text, 'alpha beta gamma delta');
  });
}
