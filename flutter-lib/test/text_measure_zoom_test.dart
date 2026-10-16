// Every Text under SwtZoomScale paints through a textScaler of 1 / uiScale. A TextPainter that
// measures the same text to size a box has to use that scaler too, or on a monitor
// whose zoom autoscale rounds down the box comes out shorter and narrower than the text in it.

import 'package:flutter/material.dart';
import 'package:flutter/rendering.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/gen/button.dart';
import 'package:swtflutter/src/gen/ccombo.dart';
import 'package:swtflutter/src/gen/combo.dart';
import 'package:swtflutter/src/gen/label.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/gen/toolbar.dart';
import 'package:swtflutter/src/gen/toolitem.dart';
import 'package:swtflutter/src/impl/table_evolve.dart';
import 'package:swtflutter/src/impl/utils/swt_zoom_scale.dart';
import 'package:swtflutter/src/impl/widget_config.dart';
import 'package:swtflutter/src/theme/theme_extensions/button_theme_extension.dart';
import 'package:swtflutter/src/theme/theme_extensions/label_theme_extension.dart';
import 'package:swtflutter/src/theme/theme_extensions/toolitem_theme_extension.dart';

const _textKey = Key('text');
const _style = TextStyle(fontSize: 14);
const _sample = '0x50060000';

// A 150% monitor under the integer autoScale: SWT lays out at 100, the tree is drawn at 2/3.
const _integerAt150 = 2 / 3;

Future<Size> _paintedSizeAt(WidgetTester tester, double scale) async {
  appScaleNotifier.value = scale;
  await tester.pumpWidget(MaterialApp(
    builder: (context, child) => SwtZoomScale(child: child!),
    home: const Align(
      alignment: Alignment.topLeft,
      child: Text(_sample, key: _textKey, style: _style),
    ),
  ));
  return tester.getSize(find.byKey(_textKey));
}

VRectangle _rect(int width, int height) => VRectangle()
  ..x = 0
  ..y = 0
  ..width = width
  ..height = height;

/// Mounts [control]; with [bounds], held to them the way its parent Composite would. A widget keeps
/// the first value pumped for its id, so a second mount needs an id of its own.
Future<void> _pump(WidgetTester tester, Widget control, {VRectangle? bounds}) async {
  await tester.pumpWidget(EvolveApp(
    theme: ThemeMode.light,
    contentWidget: Align(
      alignment: Alignment.topLeft,
      child: bounds == null
          ? control
          : SizedBox(
              width: bounds.width!.toDouble(), height: bounds.height!.toDouble(), child: control),
    ),
  ));
  await tester.pumpAndSettle();
}

/// The width [text] takes when painted in [style] where [at] sits, zoom included.
double _paintedWidth(WidgetTester tester, Finder at, String text, TextStyle style) =>
    (TextPainter(
      text: TextSpan(text: text, style: style),
      textDirection: TextDirection.ltr,
      textScaler: MediaQuery.textScalerOf(tester.element(at)),
      maxLines: 1,
    )..layout())
        .width;

bool _ellipsized(WidgetTester tester, String text) =>
    tester.renderObject<RenderParagraph>(find.text(text)).didExceedMaxLines;

void main() {
  tearDown(() => appScaleNotifier.value = 1.0);

  testWidgets('a packed column fits the text painted on a monitor autoscale rounds down',
      (tester) async {
    // A 125% monitor under the default autoScale: SWT lays out at 100, the tree is drawn at 0.8.
    final painted = await _paintedSizeAt(tester, 0.8);
    expect(measureTableText(_sample, _style), closeTo(painted.width, 0.01));
  });

  testWidgets('measuring is unchanged while the app is unzoomed', (tester) async {
    final painted = await _paintedSizeAt(tester, 1.0);
    expect(measureTableText(_sample, _style), closeTo(painted.width, 0.01));
  });

  _measuredAtScale(_integerAt150, 0);
  _measuredAtScale(1.0, 100);
}

/// At [scale] 2/3 text is painted larger than SWT measures it; 1.0 is the unzoomed control. [ids]
/// offsets every widget id, since a widget keeps the first value pumped for its id.
void _measuredAtScale(double scale, int ids) {
  group('at uiScale ${scale.toStringAsFixed(2)}', () {
    setUp(() => appScaleNotifier.value = scale);

    testWidgets('a Combo at its preferred width shows its longest item', (tester) async {
      const longest = 'MOV (only for Mac)';
      await _pump(
          tester,
          ComboSwt<VCombo>(
              value: VCombo()
                ..id = ids + 1
                ..style = SWT.DROP_DOWN | SWT.READ_ONLY
                ..enabled = true
                ..text = longest
                ..items = ['AVI', longest]));

      final editable = find.byType(EditableText);
      final needed =
          _paintedWidth(tester, editable, longest, tester.widget<EditableText>(editable).style);
      expect(tester.getSize(editable).width, greaterThanOrEqualTo(needed));
    });

    testWidgets('a CCombo at its preferred width shows its longest item', (tester) async {
      const longest = 'X-Large selection';
      await _pump(
          tester,
          CComboSwt<VCCombo>(
              value: VCCombo()
                ..id = ids + 2
                ..style = SWT.BORDER | SWT.READ_ONLY
                ..enabled = true
                ..text = longest
                ..items = ['Small', longest]));

      final editable = find.byType(EditableText);
      final needed =
          _paintedWidth(tester, editable, longest, tester.widget<EditableText>(editable).style);
      expect(tester.getSize(editable).width, greaterThanOrEqualTo(needed));
    });

    testWidgets('a Label whose bounds hold its text but not its padding is not ellipsized',
        (tester) async {
      const text = 'Initially fold these elements';
      VLabel label(int id, int width) => VLabel()
        ..id = id
        ..style = SWT.NONE
        ..text = text
        ..bounds = _rect(width, 20);

      await _pump(tester, LabelSwt<VLabel>(value: label(ids + 3, 1000)));
      final textFinder = find.text(text);
      final needed =
          _paintedWidth(tester, textFinder, text, tester.widget<Text>(textFinder).style!);
      final padding = Theme.of(tester.element(textFinder))
          .extension<LabelThemeExtension>()!
          .padding
          .horizontal;

      final pinned = label(ids + 8, (needed + padding / 2).ceil());
      await _pump(tester, LabelSwt<VLabel>(value: pinned), bounds: pinned.bounds);
      expect(_ellipsized(tester, text), isFalse);
    });

    testWidgets('a PUSH button pinned short of its padding is not ellipsized', (tester) async {
      const text = 'Restore Defaults';
      VButton button(int id, {VRectangle? bounds}) => VButton()
        ..id = id
        ..style = SWT.PUSH
        ..enabled = true
        ..text = text
        ..bounds = bounds;

      await _pump(tester, ButtonSwt<VButton>(value: button(ids + 4)));
      final natural = tester.getSize(find.byType(ButtonSwt<VButton>));
      final padding = Theme.of(tester.element(find.text(text)))
          .extension<ButtonThemeExtension>()!
          .pushButtonPadding
          .horizontal;

      final pinned = button(ids + 9,
          bounds: _rect((natural.width - padding / 2).ceil(), natural.height.ceil()));
      await _pump(tester, ButtonSwt<VButton>(value: pinned), bounds: pinned.bounds);
      expect(_ellipsized(tester, text), isFalse);
    });

    testWidgets('the selected segment of a ToolBar is highlighted across its whole text',
        (tester) async {
      VToolItem item(int id, String text, bool selected) => VToolItem()
        ..id = id
        ..style = SWT.RADIO
        ..enabled = true
        ..selection = selected
        ..text = text;

      await _pump(
          tester,
          ToolBarSwt<VToolBar>(
              value: VToolBar()
                ..id = ids + 5
                ..style = SWT.HORIZONTAL | SWT.FLAT
                ..enabled = true
                ..visible = true
                ..items = [item(ids + 6, 'Keyword', true), item(ids + 7, 'Debug', false)]
                ..bounds = _rect(400, 40)));

      final keyword = find.text('Keyword');
      final needed = _paintedWidth(
          tester, keyword, 'Keyword', DefaultTextStyle.of(tester.element(keyword)).style);
      final padding = Theme.of(tester.element(keyword))
          .extension<ToolItemThemeExtension>()!
          .segmentPadding
          .horizontal;
      final highlight =
          find.descendant(of: find.byType(AnimatedPositioned), matching: find.byType(Container));
      expect(tester.getSize(highlight).width, closeTo(needed + padding, 0.5));
    });
  });
}
