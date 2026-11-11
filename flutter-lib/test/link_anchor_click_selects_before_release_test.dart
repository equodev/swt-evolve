// A click on a Link's anchor reports the anchor's Selection between the press and the release, as
// every native does; a click on the plain text between anchors selects nothing.

import 'package:flutter/gestures.dart';
import 'package:flutter/material.dart';
import 'package:flutter/rendering.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/gen/event.dart';
import 'package:swtflutter/src/gen/link.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/swt.dart';

class _RecordingLinkSwt extends LinkSwt<VLink> {
  const _RecordingLinkSwt({required super.value, required this.calls});

  final List<String> calls;

  @override
  void sendMouseMouseDown(VLink val, VEvent? payload) => calls.add('down');

  @override
  void sendMouseMouseUp(VLink val, VEvent? payload) => calls.add('up');

  @override
  void sendSelectionSelection(VLink val, VEvent? payload) =>
      calls.add('selection ${payload?.text} ${payload?.index}');
}

VLink _link() => VLink()
  ..id = 1
  ..style = SWT.NONE
  ..enabled = true
  ..visible = true
  ..tabStop = true
  ..text = '<a href="https://www.eclipse.org">First</a> plain text <a>Second</a>'
  ..bounds = (VRectangle()
    ..x = 0
    ..y = 0
    ..width = 400
    ..height = 20);

/// Clicks the middle of [word] in the Link's text and answers what reached Java.
Future<List<String>> _click(WidgetTester tester, String word) async {
  final calls = <String>[];
  await tester.pumpWidget(EvolveApp(
    theme: ThemeMode.light,
    contentWidget: SizedBox(
      width: 400,
      height: 20,
      child: _RecordingLinkSwt(value: _link(), calls: calls),
    ),
  ));
  await tester.pumpAndSettle();
  final paragraph = tester.renderObject<RenderParagraph>(find.byType(RichText).last);
  final start = paragraph.text.toPlainText().indexOf(word);
  final box = paragraph
      .getBoxesForSelection(TextSelection(baseOffset: start, extentOffset: start + word.length))
      .first
      .toRect();
  calls.clear();
  final gesture = await tester.createGesture(buttons: kPrimaryButton);
  await gesture.down(paragraph.localToGlobal(box.center));
  await gesture.up();
  await tester.pump(Duration.zero);
  return calls;
}

void main() {
  testWidgets('a click on the anchor with an href reports it, with its index, before the release', (tester) async {
    expect(await _click(tester, 'First'), ['down', 'selection https://www.eclipse.org 0', 'up']);
  });

  testWidgets('a click on the anchor without one reports its label and index', (tester) async {
    expect(await _click(tester, 'Second'), ['down', 'selection Second 1', 'up']);
  });

  testWidgets('a click on the plain text between the anchors selects nothing', (tester) async {
    expect(await _click(tester, 'plain'), ['down', 'up']);
  });
}
