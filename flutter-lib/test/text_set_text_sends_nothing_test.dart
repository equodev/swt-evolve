// Text.setText from Java moves the caret, but it is no edit: SWT sends a Verify only for a change
// that a user proposes. The field must not answer Java's own text with events of its own.

import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/comm/comm_frame.dart';
import 'package:swtflutter/src/comm/comm_ws.dart';
import 'package:swtflutter/src/gen/composite.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/gen/text.dart';
import 'package:swtflutter/src/gen/widgets.dart' show WireKey;

import 'delivery/support/deliver.dart';

VText _text(String text, {int seq = 10}) => VText()
  ..id = 10
  ..style = SWT.BORDER | SWT.SINGLE
  ..seq = seq
  ..text = text
  ..enabled = true
  ..editable = true
  ..bounds = (VRectangle()
    ..width = 160
    ..height = 24);

void main() {
  testWidgets('a text set by Java sends nothing back', (tester) async {
    EquoCommBase.recordSentFrames = true;
    addTearDown(() => EquoCommBase.recordSentFrames = false);
    freshClient();
    EquoCommService.commForTesting.clearSentFrames();
    final root = VComposite()
      ..id = 1
      ..style = SWT.NONE
      ..seq = 1
      ..bounds = (VRectangle()
        ..width = 200
        ..height = 100)
      ..children = [_text('first')];
    await tester.pumpWidget(EvolveApp(
      theme: ThemeMode.light,
      contentWidget: SizedBox(width: 200, height: 100, child: CompositeSwt<VComposite>(value: root)),
    ));
    await tester.pumpAndSettle();

    await deliverChange(_text('a longer second text', seq: 11), keys: [WireKey.k_Text_text], base: 10);
    await tester.pumpAndSettle();
    await deliverChange(_text('third', seq: 12), keys: [WireKey.k_Text_text], base: 11);
    await tester.pumpAndSettle();

    final sent = EquoCommService.commForTesting.sentFrames
        .map((frame) => EquoCommBase.decodeFrame(frame).$1)
        .where((channel) => channel.startsWith('Text/'));
    expect(sent, isEmpty);
  });
}
