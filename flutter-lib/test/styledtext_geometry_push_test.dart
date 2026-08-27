// Java answers every StyledText position query — getTextBounds, getLinePixel, getLineIndex —
// from the geometry table the render side pushes, and falls back to a glyph-table estimate
// that knows nothing about wrapping when no table has arrived. The push happens in the
// pre-paint hook, whose only caller is CanvasImpl's paint request; StyledTextImpl replaces
// CanvasImpl.build, so the sole request is the one initState makes — before the first state
// push has built a text shape to describe. An editor that receives its text after mount, which
// is every editor, therefore never delivers its geometry at all, and with word wrap on JFace's
// line-number ruler advances one row per logical line and numbers every wrapped visual row.
//
// The layout must also be pushed again when it changes (a wrap toggle here), and not when only
// the caret blinks: the payload is O(document).

import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/styledtext.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/impl/styledtext_evolve.dart';

// Three canvas widths wide, so wrapping splits it into several visual rows.
const _longLine =
    'bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb';
const _text = 'aaaa\n$_longLine\ncccc';
const _logicalLines = 3;
const _size = Size(200, 400);

void main() {
  VStyledText value({required bool wrap}) => VStyledText()
    ..swt = 'StyledText'
    ..id = 1
    ..style = SWT.H_SCROLL | SWT.V_SCROLL
    ..enabled = true
    ..editable = true
    ..caretOffset = 0
    ..text = _text
    ..wordWrap = wrap
    ..bounds = (VRectangle()
      ..x = 0
      ..y = 0
      ..width = _size.width.toInt()
      ..height = _size.height.toInt());

  Widget appWith(GlobalKey<StyledTextImpl> key, VStyledText v) => EvolveApp(
        theme: ThemeMode.light,
        contentWidget: SizedBox(
          width: _size.width,
          height: _size.height,
          child: StyledTextSwt<VStyledText>(key: key, value: v),
        ),
      );

  setUp(() {
    StyledTextImpl.debugGeometryPushes = 0;
    StyledTextImpl.debugLastGeometry = null;
  });

  testWidgets('the geometry reaches Java once the text has a layout',
      (tester) async {
    final key = GlobalKey<StyledTextImpl>();
    await tester.pumpWidget(appWith(key, value(wrap: false)));
    // initState doesn't run extraSetState, so a second push builds the text shape.
    await tester.pumpWidget(appWith(key, value(wrap: false)));
    await tester.pump();

    expect(
      StyledTextImpl.debugGeometryPushes,
      greaterThan(0),
      reason: 'an editor whose text arrives after mount must still deliver the '
          'geometry its position API is answered from',
    );
    expect(
      (StyledTextImpl.debugLastGeometry!['lines'] as List).length,
      _logicalLines,
      reason: 'with wrapping off every logical line is one visual row',
    );
  });

  testWidgets('a wrap toggle pushes the wrapped rows', (tester) async {
    final key = GlobalKey<StyledTextImpl>();
    await tester.pumpWidget(appWith(key, value(wrap: false)));
    await tester.pumpWidget(appWith(key, value(wrap: false)));
    await tester.pump();

    StyledTextImpl.debugGeometryPushes = 0;
    await tester.pumpWidget(appWith(key, value(wrap: true)));
    await tester.pump();

    expect(
      StyledTextImpl.debugGeometryPushes,
      greaterThan(0),
      reason: 'the wrapped layout is a different geometry and Java is still '
          'answering position queries from the unwrapped one until it is sent',
    );
    expect(
      (StyledTextImpl.debugLastGeometry!['lines'] as List).length,
      greaterThan(_logicalLines),
      reason: 'the long line wraps, so the table carries more visual rows than '
          'the document has logical lines',
    );
  });

  testWidgets('an idle caret blink pushes nothing', (tester) async {
    final key = GlobalKey<StyledTextImpl>();
    await tester.pumpWidget(appWith(key, value(wrap: false)));
    await tester.pumpWidget(appWith(key, value(wrap: false)));

    // Click into the editor: the caret appears and its blink timer starts.
    await tester.tapAt(const Offset(5, 5));
    await tester.pump();
    await tester.pump();

    StyledTextImpl.debugGeometryPushes = 0;
    await tester.pump(const Duration(milliseconds: 560));
    await tester.pump(const Duration(milliseconds: 560));

    expect(
      StyledTextImpl.debugGeometryPushes,
      0,
      reason: 'a caret-visibility flip changes no geometry, and the payload is '
          'one layout per line plus a per-character x array for the document',
    );
  }, timeout: const Timeout(Duration(minutes: 2)));
}
