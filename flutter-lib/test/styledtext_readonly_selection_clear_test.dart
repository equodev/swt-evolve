// Clicking in a StyledText clears any existing selection, read-only editors included.
//
// SWT clears the selection on a mouse-down regardless of getEditable() — a read-only
// StyledText (a JFace SourceViewer preview, for instance) still selects and deselects.
// The Flutter editor reaches its selection state through the local edit mode, and every
// lookup that can clear a selection used to be gated on the shape being editable, so a
// read-only editor could enter that mode by dragging but never leave it cleanly: the
// shape handed back to the paint list on exit kept its selectionInfo, and no later click
// could reach it again.

import 'package:flutter/material.dart';
import 'package:flutter/widgets.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/styledtext.dart';
import 'package:swtflutter/src/impl/gcdrawer_evolve.dart' show ScenePainter;
import 'package:swtflutter/src/impl/styledtext_evolve.dart';

const double _kWidth = 300;
const double _kHeight = 120;

void main() {
  VStyledText value({required bool editable}) => VStyledText()
    ..swt = 'StyledText'
    ..id = 1
    ..style = 0
    ..enabled = true
    ..editable = editable
    ..caretOffset = 0
    ..text = 'alpha beta\ngamma delta\nepsilon'
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

  /// Everything the widget actually paints, which is `shapes` plus the shape currently
  /// being edited — the only place a highlighted selection can live.
  List<TextShape> paintedText(WidgetTester tester) => tester
      .widgetList<CustomPaint>(
        find.descendant(of: editor(), matching: find.byType(CustomPaint)),
      )
      .map((w) => w.painter)
      .whereType<ScenePainter>()
      .expand((p) => p.shapes)
      .whereType<TextShape>()
      .toList();

  bool hasHighlight(WidgetTester tester) =>
      paintedText(tester).any((s) => s.selectionInfo?.hasSelection == true);

  Future<void> pumpEditor(WidgetTester tester, {required bool editable}) async {
    await tester.pumpWidget(appWith(value(editable: editable)));
    // initState doesn't run extraSetState, so a second push is what builds the text shape
    // (in production Java always sends this push).
    await tester.pumpWidget(appWith(value(editable: editable)));
  }

  /// Drag across the first line to select part of it.
  Future<void> dragSelect(WidgetTester tester) async {
    final origin = tester.getTopLeft(editor());
    final gesture = await tester.startGesture(origin + const Offset(6, 4));
    await gesture.moveTo(origin + const Offset(60, 4));
    await tester.pump();
    await gesture.up();
    await tester.pump();
  }

  for (final editable in [false, true]) {
    final label = editable ? 'editable' : 'read-only';

    testWidgets('a click on empty space clears the selection in a $label StyledText',
        (tester) async {
      await pumpEditor(tester, editable: editable);
      await dragSelect(tester);

      expect(hasHighlight(tester), isTrue,
          reason: 'sanity: dragging must select text in a $label StyledText');

      // "Somewhere else in the editor" — past the end of the text, which is most of the
      // editor's area for a short document like a formatter preview.
      final origin = tester.getTopLeft(editor());
      await tester.tapAt(origin + const Offset(_kWidth - 20, _kHeight - 20));
      await tester.pump();

      expect(hasHighlight(tester), isFalse,
          reason: 'clicking in the editor must dismiss the previous selection');
    });

    testWidgets('a click back on the text clears a stale selection in a $label StyledText',
        (tester) async {
      await pumpEditor(tester, editable: editable);
      await dragSelect(tester);

      final origin = tester.getTopLeft(editor());
      // First click leaves the editor; the second lands back on the text. Neither may
      // leave the highlight behind — the reported symptom is that no click clears it.
      await tester.tapAt(origin + const Offset(_kWidth - 20, _kHeight - 20));
      await tester.pump();
      await tester.tapAt(origin + const Offset(20, 4));
      await tester.pump();

      expect(hasHighlight(tester), isFalse,
          reason: 'a later click on the text must clear the selection too');
    });

    testWidgets('double-click selects a word in a $label StyledText', (tester) async {
      await pumpEditor(tester, editable: editable);

      final origin = tester.getTopLeft(editor());
      await tester.tapAt(origin + const Offset(20, 4));
      await tester.pump();
      await tester.tapAt(origin + const Offset(20, 4));
      await tester.pump();
      await tester.pump();

      expect(hasHighlight(tester), isTrue,
          reason: 'double-click word selection is not conditioned on editability either');
    });
  }
}
