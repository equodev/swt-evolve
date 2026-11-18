// A wait sent to the page answers once: at once when its condition already holds, after the frame
// that makes it hold otherwise, and with what it last saw when its time runs out.

import 'dart:convert';

import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/gen/composite.dart';
import 'package:swtflutter/src/gen/label.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/gen/widgets.dart' show WireKey;
import 'package:swtflutter/src/comm/v_registry.dart';
import 'package:swtflutter/test_harness.dart';

VRectangle _rect(int x, int y, int w, int h) => VRectangle()
  ..x = x
  ..y = y
  ..width = w
  ..height = h;

String _labelText(String op, List<String> values, {String expect = 'some'}) => jsonEncode({
      'kinds': ['Label/'],
      'where': [
        {'keys': [WireKey.k_Label_text], 'op': op, 'values': values}
      ],
      'expect': expect,
    });

void main() {
  testWidgets('a wait answers when its condition holds, or with what it saw when time runs out', (tester) async {
    final label = VLabel()
      ..id = 2
      ..style = SWT.NONE
      ..visible = true
      ..text = 'Save &As'
      ..bounds = _rect(0, 0, 200, 20);
    await tester.pumpWidget(EvolveApp(
      theme: ThemeMode.light,
      contentWidget: SizedBox(
          width: 400,
          height: 100,
          child: CompositeSwt<VComposite>(
              value: VComposite()
                ..id = 1
                ..style = SWT.NONE
                ..visible = true
                ..bounds = _rect(0, 0, 400, 100)
                ..children = [label])),
    ));
    await tester.pumpAndSettle();

    final now = jsonDecode(await waitForStatesJson(_labelText('eqIgnoringMnemonic', ['Save As']), 1000));
    expect(now['met'], isTrue);
    expect((now['states'] as Map).keys, ['Label/2']);

    String? later;
    waitForStatesJson(_labelText('eq', ['Saved']), 5000).then((json) => later = json);
    await tester.pump();
    expect(later, isNull, reason: 'the label does not read "Saved" yet');
    (VRegistry.instance.nodeAt(VRegistry.keyOf(label))!.value as VLabel).text = 'Saved';
    // What a delivery from Java does: the value changes and the widget rendering it is redrawn.
    tester.binding.scheduleFrame();
    await tester.pump();
    await tester.idle();
    expect(later, isNotNull, reason: 'the frame after the change answers the wait');
    expect(jsonDecode(later!)['met'], isTrue);

    String? none;
    waitForStatesJson(_labelText('present', const [], expect: 'none'), 300).then((json) => none = json);
    await tester.pump(const Duration(milliseconds: 400));
    await tester.idle();
    expect(jsonDecode(none!)['met'], isFalse, reason: 'the label is there for the whole wait');
    expect((jsonDecode(none!)['states'] as Map).keys, ['Label/2']);
  });
}
