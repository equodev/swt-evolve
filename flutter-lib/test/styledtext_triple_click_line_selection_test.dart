// Triple-click selects the clicked line, not the whole document.
//
// SWT's handleMouseDown treats an odd click count above one as a line select: the selection
// runs from the line's start offset to the start of the next line (so the trailing newline is
// included), or to the end of the content on the last line. Ours called selectAll().

import 'package:flutter/material.dart';
import 'package:flutter/widgets.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/styledtext.dart';
import 'package:swtflutter/src/impl/gcdrawer_evolve.dart' show ScenePainter;
import 'package:swtflutter/src/impl/styledtext_evolve.dart';

const double _kWidth = 300;
const double _kHeight = 160;

// Offsets: line 0 = [0,10), '\n' at 10; line 1 = [11,22), '\n' at 22; line 2 = [23,30).
const String _kText = 'alpha beta\ngamma delta\nepsilon';

void main() {
  VStyledText value({bool? doubleClickEnabled}) => VStyledText()
    ..doubleClickEnabled = doubleClickEnabled
    ..swt = 'StyledText'
    ..id = 1
    ..style = 0
    ..enabled = true
    ..editable = true
    ..caretOffset = 0
    ..wordWrap = false
    ..text = _kText
    ..bounds = (VRectangle()
      ..x = 0
      ..y = 0
      ..width = _kWidth.toInt()
      ..height = _kHeight.toInt());

  Widget appWith(VStyledText v) => EvolveApp(
        theme: ThemeMode.light,
        contentWidget: SizedBox(
          width: _kWidth,
          height: _kHeight,
          child: StyledTextSwt<VStyledText>(value: v),
        ),
      );

  Finder editor() => find.byType(StyledTextSwt<VStyledText>);

  List<TextShape> paintedText(WidgetTester tester) => tester
      .widgetList<CustomPaint>(
        find.descendant(of: editor(), matching: find.byType(CustomPaint)),
      )
      .map((w) => w.painter)
      .whereType<ScenePainter>()
      .expand((p) => p.shapes)
      .whereType<TextShape>()
      .toList();

  (int, int)? liveSelection(WidgetTester tester) {
    for (final s in paintedText(tester)) {
      final sel = s.selectionInfo;
      if (sel != null && sel.hasSelection) {
        return (sel.normalizedStart, sel.normalizedEnd);
      }
    }
    return null;
  }

  Future<void> pumpEditor(WidgetTester tester, {bool? doubleClickEnabled}) async {
    await tester.pumpWidget(appWith(value(doubleClickEnabled: doubleClickEnabled)));
    await tester.pumpWidget(appWith(value(doubleClickEnabled: doubleClickEnabled)));
  }

  /// Three taps at the same point inside the DoubleTapDetector window.
  Future<void> tripleTapAt(WidgetTester tester, Offset local) async {
    final at = tester.getTopLeft(editor()) + local;
    for (var i = 0; i < 3; i++) {
      await tester.tapAt(at);
      await tester.pump();
    }
    await tester.pump();
  }

  /// A local point that the editor's own hit-test resolves inside [lineIndex].
  ///
  /// Derived by probing getOffsetFromPosition rather than assuming a line height, so the
  /// test stays valid whatever font the harness picks.
  Offset pointOnLine(WidgetTester tester, int lineIndex, double x) {
    final shape = paintedText(tester).firstWhere((s) => s.text == _kText);
    final size = tester.getSize(editor());
    var start = 0;
    for (var i = 0; i < lineIndex; i++) {
      start = _kText.indexOf('\n', start) + 1;
    }
    final nl = _kText.indexOf('\n', start);
    final end = nl == -1 ? _kText.length : nl;

    for (double y = 0.5; y < size.height; y += 0.5) {
      final o = shape.getOffsetFromPosition(Offset(x, y), size);
      if (o >= start && o <= end) return Offset(x, y);
    }
    fail('no y in the editor hit-tests into line $lineIndex');
  }

  testWidgets('triple-click selects the clicked middle line, not the document',
      (tester) async {
    await pumpEditor(tester);
    await tripleTapAt(tester, pointOnLine(tester, 1, 30));

    expect(liveSelection(tester), (11, 23),
        reason: 'line 1 runs [11,22) and native includes its trailing newline, so [11,23)');
  });

  testWidgets('triple-click on the last line selects to the end of the content',
      (tester) async {
    await pumpEditor(tester);
    await tripleTapAt(tester, pointOnLine(tester, 2, 20));

    expect(liveSelection(tester), (23, 30),
        reason: 'the last line has no trailing newline, so it ends at text.length');
  });

  testWidgets('triple-click on the first line does not reach the second', (tester) async {
    await pumpEditor(tester);
    await tripleTapAt(tester, pointOnLine(tester, 0, 20));

    expect(liveSelection(tester), (0, 11),
        reason: 'selecting the whole document here is the reported bug');
  });

  testWidgets('setDoubleClickEnabled(false) suppresses the line select', (tester) async {
    await pumpEditor(tester, doubleClickEnabled: false);
    await tripleTapAt(tester, pointOnLine(tester, 1, 30));

    expect(liveSelection(tester), isNull,
        reason: 'native gates the whole clickCount > 1 branch on doubleClickEnabled');
  });

  testWidgets('setDoubleClickEnabled(false) suppresses the word select too', (tester) async {
    await pumpEditor(tester, doubleClickEnabled: false);
    final at = tester.getTopLeft(editor()) + pointOnLine(tester, 1, 30);
    await tester.tapAt(at);
    await tester.pump();
    await tester.tapAt(at);
    await tester.pump();
    await tester.pump();

    expect(liveSelection(tester), isNull,
        reason: 'the same native gate covers word select');
  });
}
