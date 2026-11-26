// A List draws its frame inside its bounds, and its items inside the frame: Java's preferred width
// (ListSizes) is the widest item, its padding and that frame. With H_SCROLL an item wider than the
// list is shown in full and the items scroll sideways, as on Win32, GTK and Cocoa.

import 'package:flutter/gestures.dart';
import 'package:flutter/material.dart';
import 'package:flutter/rendering.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/comm/comm_frame.dart';
import 'package:swtflutter/src/comm/comm_ws.dart';
import 'package:swtflutter/src/gen/event.dart';
import 'package:swtflutter/src/gen/list.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/theme/theme_extensions/list_theme_extension.dart';

const _longest = 'The Longest String';

VList _list(int style, double width, List<String> items) => VList()
  ..id = 1
  ..style = style
  ..enabled = true
  ..items = items
  ..selection = <int>[]
  ..bounds = (VRectangle()
    ..x = 0
    ..y = 0
    ..width = width.round()
    ..height = 200);

late ListThemeExtension _theme;

Future<void> _pump(WidgetTester tester, int style, double width,
    {List<String> items = const ['Apples', _longest]}) async {
  await tester.pumpWidget(EvolveApp(
    theme: ThemeMode.light,
    contentWidget: Builder(builder: (context) {
      _theme = Theme.of(context).extension<ListThemeExtension>()!;
      return Align(
        alignment: Alignment.topLeft,
        child: SizedBox(
            width: width, height: 200, child: ListSwt(value: _list(style, width, items))),
      );
    }),
  ));
  await tester.pump();
}

RenderParagraph _longestItem(WidgetTester tester) =>
    tester.renderObject<RenderParagraph>(find.text(_longest));

double _textWidth(RenderParagraph p) => (TextPainter(
      text: TextSpan(text: _longest, style: p.text.style),
      textDirection: TextDirection.ltr,
    )..layout())
        .width;

Iterable<ScrollableState> _scrollables(WidgetTester tester, Axis axis) => tester
    .stateList<ScrollableState>(find.byType(Scrollable))
    .where((s) => s.position.axis == axis);

List<VEvent> _scrollsSent() => EquoCommService.commForTesting.sentFrames
    .where((frame) => EquoCommBase.decodeFrame(frame).$1.endsWith('/Scroll/Scroll'))
    .map(EquoCommBase.decodeEvent)
    .toList();

void main() {
  setUp(() => EquoCommBase.recordSentFrames = true);
  tearDown(() {
    EquoCommBase.recordSentFrames = false;
    EquoCommService.commForTesting.clearSentFrames();
  });

  testWidgets('with H_SCROLL the sideways offset, viewport and items\' width are reported',
      (tester) async {
    await _pump(tester, SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL, 80);
    final across = _scrollables(tester, Axis.horizontal).single.position;
    final itemsWidth = (across.maxScrollExtent + across.viewportDimension).round();

    expect(_scrollsSent().last,
        isA<VEvent>()
            .having((e) => e.x, 'x', 0)
            .having((e) => e.width, 'width', across.viewportDimension.round())
            .having((e) => e.end, 'end', itemsWidth));
    expect(itemsWidth, greaterThan(across.viewportDimension));

    across.jumpTo(across.maxScrollExtent);
    await tester.pump();

    expect(_scrollsSent().last.x, across.maxScrollExtent.round());
  });

  testWidgets('with H_SCROLL items that fit report the viewport as their width', (tester) async {
    await _pump(tester, SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL, 400);

    final scroll = _scrollsSent().last;
    expect(scroll.end, scroll.width);
    expect(scroll.x, 0);
  });

  testWidgets('an item fits a list as wide as its text, its padding and the frame', (tester) async {
    await _pump(tester, SWT.SINGLE | SWT.V_SCROLL, 400);
    final text = _textWidth(_longestItem(tester));

    await _pump(tester, SWT.SINGLE | SWT.V_SCROLL,
        (text + _theme.itemPadding.horizontal + 2 * _theme.borderWidth).ceilToDouble());

    expect(_longestItem(tester).didExceedMaxLines, isFalse);
  });

  testWidgets('without H_SCROLL an item wider than the list is elided', (tester) async {
    await _pump(tester, SWT.SINGLE | SWT.V_SCROLL, 80);

    expect(_longestItem(tester).didExceedMaxLines, isTrue);
    expect(_scrollables(tester, Axis.horizontal), isEmpty);
  });

  testWidgets('with H_SCROLL an item wider than the list is shown in full and scrolls sideways',
      (tester) async {
    await _pump(tester, SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL, 80);
    final item = _longestItem(tester);

    expect(item.didExceedMaxLines, isFalse);
    expect(item.size.width, greaterThanOrEqualTo(_textWidth(item)));
    final across = _scrollables(tester, Axis.horizontal).single.position;
    expect(across.maxScrollExtent, greaterThan(0));
  });

  testWidgets('with H_SCROLL items that fit do not scroll sideways', (tester) async {
    await _pump(tester, SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL, 400);

    final across = _scrollables(tester, Axis.horizontal);
    expect(across.every((s) => s.position.maxScrollExtent == 0), isTrue);
  });

  testWidgets('with H_SCROLL the vertical bar follows the rows', (tester) async {
    await _pump(tester, SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL, 80,
        items: [for (var i = 0; i < 40; i++) '$_longest $i']);
    final rows = _scrollables(tester, Axis.vertical).single.position;
    final bar = _verticalBar(tester);
    final size = tester.getSize(find.byWidget(bar.widget));
    final edge = size.width - 4;

    rows.jumpTo(rows.maxScrollExtent);
    await tester.pump();
    await tester.pump(const Duration(milliseconds: 100));

    expect(bar.painter.hitTestOnlyThumbInteractive(
        Offset(edge, size.height * 0.9), PointerDeviceKind.mouse), isTrue);
    expect(bar.painter.hitTestOnlyThumbInteractive(
        Offset(edge, size.height * 0.1), PointerDeviceKind.mouse), isFalse);
    // On desktop the bar takes the pointer, so its thumb can be hit-tested.
  }, variant: TargetPlatformVariant.only(TargetPlatform.linux));
}

/// The bar painted by the Scrollbar that drives the rows' vertical controller.
({CustomPaint widget, ScrollbarPainter painter}) _verticalBar(WidgetTester tester) {
  final scrollbar = find.byWidgetPredicate(
      (w) => w is Scrollbar && w.controller?.position.axis == Axis.vertical);
  final paint = tester
      .widgetList<CustomPaint>(find.descendant(of: scrollbar, matching: find.byType(CustomPaint)))
      .firstWhere((p) => p.foregroundPainter is ScrollbarPainter);
  return (widget: paint, painter: paint.foregroundPainter! as ScrollbarPainter);
}
