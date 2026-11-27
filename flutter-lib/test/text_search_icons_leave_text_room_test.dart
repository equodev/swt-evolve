import 'package:flutter/material.dart';
import 'package:flutter/rendering.dart';
import 'package:flutter/services.dart' show FontLoader, rootBundle;
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/gen/text.dart';
import 'package:swtflutter/src/impl/utils/force_live.dart';

const _searchStyle = SWT.SINGLE | SWT.BORDER | SWT.SEARCH | SWT.ICON_SEARCH | SWT.ICON_CANCEL;

/// Native macOS: a 165 px search field with both icons keeps 115 px for its text (26 px left, 24 px right).
const _nativeIconSides = 50.0;

VText _text(String value) => VText()
  ..id = 1
  ..style = _searchStyle
  ..enabled = true
  ..editable = true
  ..text = value
  ..bounds = (VRectangle()
    ..x = 0
    ..y = 0
    ..width = 165
    ..height = 28);

Widget _wrap(VText value) => ForceLive(child: EvolveApp(
      theme: ThemeMode.light,
      contentWidget: Center(child: TextSwt<VText>(value: value)),
    ));

RenderEditable _findEditable(WidgetTester tester) {
  RenderEditable? re;
  void visit(RenderObject ro) {
    if (ro is RenderEditable) re = ro;
    ro.visitChildren(visit);
  }

  visit(tester.binding.renderViewElement!.renderObject!);
  expect(re, isNotNull, reason: 'no RenderEditable found for the Text');
  return re!;
}

void main() {
  testWidgets('a search Text with both icons leaves its text the room native leaves', (tester) async {
    // The theme's font, so the text is as wide as in the app rather than in the test font.
    await (FontLoader('Inter')..addFont(rootBundle.load('assets/fonts/Inter_18pt-Medium.ttf'))).load();
    await tester.pumpWidget(_wrap(_text('Content Types')));
    await tester.pumpAndSettle();

    final editable = _findEditable(tester);
    final fieldWidth = tester.getSize(find.byType(TextSwt<VText>)).width;
    expect(fieldWidth - editable.size.width, lessThanOrEqualTo(_nativeIconSides),
        reason: 'the icons take more of the field than native search icons do');
    expect(editable.getMaxIntrinsicWidth(double.infinity), lessThanOrEqualTo(editable.size.width),
        reason: '"Content Types" must fit unscrolled in a 165 px search field');
  });
}
