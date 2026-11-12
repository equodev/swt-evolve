// A word-wrapped StyledText keeps its wrap while its window is dragged and rewraps once the drag ends.
// The table Java answers from is always a real layout: the one it is drawn with during the drag, and
// one at the size the drag ended at afterwards.

import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/styledtext.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/impl/styledtext_evolve.dart';
import 'package:swtflutter/src/impl/utils/resize_reports.dart';

final String _text = List.generate(20, (i) => 'Line $i: ${List.filled(12, 'word').join(' ')}').join('\n');

VStyledText _value(int width, {bool wrap = true, int id = 1}) => VStyledText()
  ..swt = 'StyledText'
  ..id = id
  ..style = SWT.V_SCROLL | (wrap ? SWT.WRAP : SWT.H_SCROLL)
  ..enabled = true
  ..visible = true
  ..editable = false
  ..caretOffset = 0
  ..text = _text
  ..wordWrap = wrap
  ..bounds = (VRectangle()
    ..x = 0
    ..y = 0
    ..width = width
    ..height = 300);

Widget _app(GlobalKey<StyledTextImpl> key, VStyledText value) => EvolveApp(
      theme: ThemeMode.light,
      contentWidget: Align(
        alignment: Alignment.topLeft,
        child: SizedBox(width: 400, height: 300, child: StyledTextSwt<VStyledText>(key: key, value: value)),
      ),
    );

/// The content height of the table [key]'s editor pushes to Java once it shows [value]; initState
/// doesn't build the text shape, so the value is given twice.
Future<double> _shown(WidgetTester tester, GlobalKey<StyledTextImpl> key, VStyledText value) async {
  StyledTextImpl.debugLastGeometry = null;
  await tester.pumpWidget(_app(key, value));
  await tester.pumpWidget(_app(key, value));
  await tester.pump(const Duration(milliseconds: 100));
  return (StyledTextImpl.debugLastGeometry!['contentHeight'] as num).toDouble();
}

/// As Java's layout of the dragged window arrives: the registry has made it the state before telling
/// the widget.
void _widen(WidgetTester tester, GlobalKey<StyledTextImpl> key, int width) {
  final editor = tester.state<StyledTextImpl>(find.byKey(key));
  editor.state.bounds = VRectangle()
    ..x = 0
    ..y = 0
    ..width = width
    ..height = 300;
  editor.setValue(editor.state);
}

void main() {
  tearDown(() => ResizeReports.windowDragged.value = false);

  testWidgets('a wrapped editor widened during a window drag keeps its wrap, then rewraps at the end',
      (tester) async {
    final key = GlobalKey<StyledTextImpl>();
    final narrow = await _shown(tester, key, _value(200));

    ResizeReports.windowDragged.value = true;
    StyledTextImpl.debugLastGeometry = null;
    _widen(tester, key, 400);
    await tester.pump();
    await tester.pump(const Duration(milliseconds: 100));
    final pushed = StyledTextImpl.debugLastGeometry;
    expect(pushed == null || (pushed['contentHeight'] as num).toDouble() == narrow, isTrue,
        reason: 'during the drag Java holds the layout the editor is drawn with, at its old width');

    StyledTextImpl.debugLastGeometry = null;
    ResizeReports.windowDragged.value = false;
    await tester.pump();
    await tester.pump(const Duration(milliseconds: 100));
    final wide = (StyledTextImpl.debugLastGeometry!['contentHeight'] as num).toDouble();

    await tester.pumpWidget(const SizedBox());
    expect(wide, await _shown(tester, GlobalKey<StyledTextImpl>(), _value(400, id: 2)),
        reason: 'once the drag ends the table is a fresh editor\'s at the width it ended at');
    expect(wide, lessThan(narrow), reason: 'wider, the lines wrap into fewer rows');
  });
}
