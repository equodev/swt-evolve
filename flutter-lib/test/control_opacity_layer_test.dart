// A control at full opacity paints into its parent's layer, so many controls changing together are
// not composited one by one; a disabled one is its own layer, so a change of enablement only sets
// that layer's alpha.

import 'package:flutter/material.dart';
import 'package:flutter/rendering.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/gen/progressbar.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/impl/widget_config.dart';
import 'package:swtflutter/src/gen/widgets.dart' show WireKey;

import 'delivery/support/deliver.dart';

VProgressBar _bar({required bool enabled, int seq = 10}) => VProgressBar()
  ..id = 1
  ..seq = seq
  ..style = SWT.HORIZONTAL
  ..enabled = enabled
  ..visible = true
  ..minimum = 0
  ..maximum = 100
  ..selection = 40
  ..bounds = (VRectangle()
    ..x = 0
    ..y = 0
    ..width = 200
    ..height = 20);

Widget _app(VProgressBar bar) => EvolveApp(
      theme: ThemeMode.light,
      contentWidget: Align(
        alignment: Alignment.topLeft,
        child: SizedBox(width: 200, height: 20, child: ProgressBarSwt<VProgressBar>(value: bar)),
      ),
    );

/// The control's own opacity: the outermost [RenderOpacity] its build puts around it.
RenderOpacity _controlOpacity(WidgetTester tester) => tester
    .renderObjectList<RenderOpacity>(find.descendant(
        of: find.byType(ProgressBarSwt<VProgressBar>),
        matching: find.byWidgetPredicate((w) => w is Opacity)))
    .first;

void main() {
  setUp(freshClient);

  testWidgets('an enabled control is not a layer of its own', (tester) async {
    await tester.pumpWidget(_app(_bar(enabled: true)));
    await tester.pumpAndSettle();

    final opacity = _controlOpacity(tester);
    expect(opacity.opacity, 1.0);
    expect(opacity.isRepaintBoundary, isFalse);
    expect(opacity.debugLayer, isNull);
  });

  testWidgets('a disabled control is a layer of its own at the disabled alpha', (tester) async {
    await tester.pumpWidget(_app(_bar(enabled: false)));
    await tester.pumpAndSettle();

    final layer = _controlOpacity(tester).debugLayer;
    expect(layer, isA<OpacityLayer>());
    expect((layer! as OpacityLayer).alpha, Color.getAlphaFromOpacity(AppOpacities.disabled));
  });

  testWidgets('enabling a disabled control leaves its own layer', (tester) async {
    final bar = _bar(enabled: false);
    await tester.pumpWidget(_app(bar));
    await tester.pumpAndSettle();

    bar
      ..enabled = true
      ..seq = 20;
    await deliverChange(bar, keys: [WireKey.k_Control_enabled], base: 10);
    await tester.pumpAndSettle();

    final opacity = _controlOpacity(tester);
    expect(opacity.opacity, 1.0);
    expect(opacity.isRepaintBoundary, isFalse);
  });
}
