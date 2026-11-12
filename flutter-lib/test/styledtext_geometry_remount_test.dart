// An editor in a background tab is not mounted: its parent is hidden, so the state that showed it is
// disposed and a new one shows it when its tab is selected again. Java still holds the geometry
// table it was sent, which is a whole document's layout; the new state sends what changed since,
// and nothing when the layout is the same.

import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/comm/v_registry.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/styledtext.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/impl/styledtext_evolve.dart';

const _width = 300.0;
const _height = 200.0;

void main() {
  VStyledText editor(String text) => VStyledText()
    ..swt = 'StyledText'
    ..id = 7
    ..style = SWT.H_SCROLL | SWT.V_SCROLL
    ..enabled = true
    ..editable = true
    ..caretOffset = 0
    ..text = text
    ..bounds = (VRectangle()
      ..x = 0
      ..y = 0
      ..width = _width.toInt()
      ..height = _height.toInt());

  Widget shown(VStyledText v) => EvolveApp(
        theme: ThemeMode.light,
        contentWidget: SizedBox(
          width: _width,
          height: _height,
          child: StyledTextSwt<VStyledText>(key: GlobalKey(), value: v),
        ),
      );

  Widget hidden() => EvolveApp(theme: ThemeMode.light, contentWidget: const SizedBox(width: _width, height: _height));

  Future<void> showAndSettle(WidgetTester tester, VStyledText v) async {
    // initState doesn't run extraSetState, so a second push builds the text shape.
    final app = shown(v);
    await tester.pumpWidget(app);
    await tester.pumpWidget(app);
    await tester.pump(Duration.zero);
  }

  setUp(() {
    VRegistry.instance.clear();
    StyledTextImpl.debugGeometryPushes = 0;
    StyledTextImpl.debugLastGeometry = null;
  });

  testWidgets('shown again with the same layout, an editor sends no geometry', (tester) async {
    final v = editor('alpha\nbeta\ngamma');
    await showAndSettle(tester, v);
    expect(StyledTextImpl.debugGeometryPushes, greaterThan(0), reason: 'sanity: the first showing sends the table');

    await tester.pumpWidget(hidden());
    StyledTextImpl.debugGeometryPushes = 0;
    await showAndSettle(tester, v);

    expect(StyledTextImpl.debugGeometryPushes, 0);
  });

  testWidgets('shown again after its text changed, an editor sends the change', (tester) async {
    final v = editor('alpha\nbeta\ngamma');
    await showAndSettle(tester, v);

    await tester.pumpWidget(hidden());
    v.text = 'alpha\nbeta\ngamma\ndelta';
    StyledTextImpl.debugGeometryPushes = 0;
    await showAndSettle(tester, v);

    expect(StyledTextImpl.debugGeometryPushes, 1);
    expect((StyledTextImpl.debugLastGeometry!['lines'] as List).length, 4);
  });
}
