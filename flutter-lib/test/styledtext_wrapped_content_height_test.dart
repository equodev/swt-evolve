// Guards that a word-wrapped StyledText paints, and takes the wheel, all the way down its view: the
// content box is as tall as the wrapped rows, not as the count of logical lines.

import 'package:flutter/material.dart';
import 'package:flutter/rendering.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/scrollbar.dart';
import 'package:swtflutter/src/gen/styledtext.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/impl/gcdrawer_evolve.dart';
import 'package:swtflutter/src/impl/styledtext_evolve.dart';

const _width = 200.0;
const _height = 300.0;

// Two logical lines, each many view widths long, as a generated INSERT is.
final String _text = List.generate(2, (i) => List.filled(120, 'value$i,').join(' ')).join('\n');

VStyledText _value({int topPixel = 0}) => VStyledText()
  ..swt = 'StyledText'
  ..id = 1
  ..style = SWT.V_SCROLL | SWT.WRAP
  ..enabled = true
  ..visible = true
  ..editable = false
  ..caretOffset = 0
  ..text = _text
  ..wordWrap = true
  ..topPixel = topPixel
  ..verticalBar = (VScrollBar()
    ..swt = 'ScrollBar'
    ..id = 2
    ..style = SWT.V_SCROLL
    ..visible = true
    ..enabled = true
    ..minimum = 0
    ..maximum = 5000
    ..thumb = _height.toInt()
    ..selection = topPixel)
  ..bounds = (VRectangle()
    ..x = 0
    ..y = 0
    ..width = _width.toInt()
    ..height = _height.toInt());

Widget _app(GlobalKey<StyledTextImpl> key, VStyledText value) => EvolveApp(
      theme: ThemeMode.light,
      contentWidget: Align(
        alignment: Alignment.topLeft,
        child: SizedBox(
          width: _width,
          height: _height,
          child: StyledTextSwt<VStyledText>(key: key, value: value),
        ),
      ),
    );

Future<double> _wrappedHeight(WidgetTester tester, GlobalKey<StyledTextImpl> key,
    VStyledText value) async {
  StyledTextImpl.debugLastGeometry = null;
  await tester.pumpWidget(_app(key, value));
  // initState doesn't run extraSetState, so a second push builds the text shape.
  await tester.pumpWidget(_app(key, value));
  await tester.pump(Duration.zero);
  return (StyledTextImpl.debugLastGeometry!['contentHeight'] as num).toDouble();
}

/// The text layer's painted area, in the StyledText's own coordinates.
Rect _paintedRect(WidgetTester tester, GlobalKey<StyledTextImpl> key) {
  final paint = find.descendant(
    of: find.byKey(key),
    matching: find.byWidgetPredicate((w) => w is CustomPaint && w.painter is ScenePainter),
  );
  final box = tester.renderObject<RenderBox>(paint.first);
  final origin = tester.getTopLeft(find.byKey(key));
  return (box.localToGlobal(Offset.zero) - origin) & box.size;
}

void main() {
  testWidgets('the wrapped rows are painted down to the bottom of the view', (tester) async {
    final key = GlobalKey<StyledTextImpl>();
    // Scrolled past what two unwrapped lines would occupy.
    final wrapped = await _wrappedHeight(tester, key, _value(topPixel: 400));
    expect(wrapped, greaterThan(_height * 2),
        reason: 'precondition: the two lines wrap into many view heights of rows');

    final painted = _paintedRect(tester, key);
    expect(painted.top, lessThanOrEqualTo(0));
    expect(painted.bottom, greaterThanOrEqualTo(_height),
        reason: 'the text layer is sized from the wrapped layout; sized from the logical line '
            'count it stops short and the rest of the view is left blank');
  });

  testWidgets('the wheel reaches the StyledText anywhere in its view', (tester) async {
    final key = GlobalKey<StyledTextImpl>();
    await _wrappedHeight(tester, key, _value(topPixel: 400));

    final wheelListeners = find
        .descendant(of: find.byKey(key), matching: find.byType(Listener))
        .evaluate()
        .map((e) => e.renderObject)
        .whereType<RenderPointerListener>()
        .where((l) => l.onPointerSignal != null)
        .toSet();
    expect(wheelListeners, isNotEmpty, reason: 'precondition: the StyledText listens to the wheel');

    final bottomOfView = tester.getTopLeft(find.byKey(key)) + const Offset(_width / 2, _height - 20);
    final hits = tester.hitTestOnBinding(bottomOfView).path.map((e) => e.target);
    expect(hits.where(wheelListeners.contains), isNotEmpty,
        reason: 'the wheel listener defers to its child, so a content box shorter than the view '
            'leaves the bottom of the view deaf to scrolling');
  });
}
