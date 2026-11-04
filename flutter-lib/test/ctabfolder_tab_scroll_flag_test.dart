// ctabfolder_tab_scroll (default true) keeps the scrolling tab strip. Set to false, the folder lays
// its tabs out as SWT does: tabs shrink towards minimumCharacters to fit, the rest go behind the
// chevron, and nothing scrolls.

import 'package:flutter/material.dart';
import 'package:flutter/rendering.dart';
import 'package:flutter/services.dart' show FontLoader, rootBundle;
import 'package:flutter_test/flutter_test.dart';
import 'package:swtflutter/src/gen/widgets.dart' show wireChannelNames;

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/gen/ctabfolder.dart';
import 'package:swtflutter/src/gen/ctabitem.dart';
import 'package:swtflutter/src/gen/event.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/impl/config_flags.dart';
import 'package:swtflutter/src/impl/ctab_tab_compression.dart';
// ignore: unused_import
import 'package:swtflutter/src/impl/ctabfolder_evolve.dart';
import 'package:swtflutter/src/impl/widget_config.dart';

const _labels = ['Search', 'Server Templates', 'Task Tracker', 'Template Preview Settings'];

VCTabFolder _folder({bool minMax = false}) => VCTabFolder()
  ..id = 1000
  ..minimizeVisible = minMax
  ..maximizeVisible = minMax
  ..style = SWT.NONE
  ..selection = 0
  ..minimumCharacters = 1
  ..items = [
    for (var i = 0; i < _labels.length; i++)
      VCTabItem()
        ..id = 10 + i
        ..text = _labels[i]
        ..showing = true,
  ];

class _ReportingCTabFolderSwt extends CTabFolderSwt<VCTabFolder> {
  const _ReportingCTabFolderSwt({required super.value, required this.onStrip});

  final void Function(String report) onStrip;

  @override
  void sendEvent(VCTabFolder val, int channel, VEvent? payload) {
    if (wireChannelNames[channel] == 'CTabFolder/stripLaidOut' && payload?.text != null) onStrip(payload!.text!);
  }
}

final _strips = <String>[];

Widget _host(double width, {bool minMax = false}) => EvolveApp(
      theme: ThemeMode.light,
      contentWidget: SizedBox(
        width: width,
        height: 200,
        child: _ReportingCTabFolderSwt(value: _folder(minMax: minMax), onStrip: _strips.add),
      ),
    );

/// The last strip report as (left, width) per tab.
List<(int, int)> _reportedTabs() {
  final values = _strips.last.split(';').first.split(',').map(int.parse).toList();
  return [for (var i = 0; i + 1 < values.length; i += 2) (values[i], values[i + 1])];
}

// The theme's font. Loaded here because another test in the same isolate may have loaded it: label
// widths, and so where the tabs compress, must not depend on test order.
Future<void> _loadInter() async {
  final loader = FontLoader('Inter')
    ..addFont(rootBundle.load('assets/fonts/Inter_18pt-Medium.ttf'));
  await loader.load();
}

void _useTabScroll(bool? value) => setConfigFlags(ConfigFlags()..ctabfolder_tab_scroll = value);

Future<void> _pump(WidgetTester tester, double width, {bool minMax = false}) async {
  tester.view
    ..physicalSize = const Size(1400, 600)
    ..devicePixelRatio = 1;
  addTearDown(tester.view.reset);
  await tester.pumpWidget(_host(width, minMax: minMax));
  for (var i = 0; i < 3; i++) {
    await tester.pump();
  }
  while (tester.takeException() != null) {}
}

double _horizontalScrollExtent(WidgetTester tester) {
  var extent = 0.0;
  for (final state in tester.stateList<ScrollableState>(find.descendant(
      of: find.byWidgetPredicate((w) => w is CTabFolderSwt), matching: find.byType(Scrollable)))) {
    if (state.position.axis == Axis.horizontal && state.position.hasContentDimensions) {
      extent += state.position.maxScrollExtent;
    }
  }
  return extent;
}

/// The text each tab label draws, in strip order.
List<String> _drawnLabels(WidgetTester tester) => [
      for (final text in tester.widgetList<Text>(
          find.descendant(of: find.byType(ShrinkableTabLabel), matching: find.byType(Text))))
        text.data ?? text.textSpan!.toPlainText(),
    ];

/// Each painted tab's background, in strip order.
List<Color?> _tabBackgrounds(WidgetTester tester) => [
      for (final el in find
          .descendant(of: find.byWidgetPredicate((w) => w is CTabFolderSwt), matching: find.byWidgetPredicate((w) => w is AnimatedContainer))
          .evaluate())
        if ((el.widget as AnimatedContainer).decoration case final BoxDecoration d when d.color != null) d.color,
    ];

void main() {
  setUpAll(_loadInter);
  setUp(() {
    resetConfigFlags();
    _strips.clear();
  });
  tearDown(resetConfigFlags);

  testWidgets('ctabfolder_tab_scroll=false: a narrow folder compresses its tabs instead of scrolling',
      (tester) async {
    _useTabScroll(false);
    await _pump(tester, 120);

    expect(_horizontalScrollExtent(tester), 0, reason: 'the tab strip must not scroll');
    expect(find.text('Search'), findsNothing, reason: 'the tabs shrink to share the strip');
    expect(find.text('S'), findsWidgets);
    expect(find.byIcon(Icons.expand_more), findsNothing,
        reason: 'every tab fits at its minimum width, so SWT shows no chevron');

    final reported = _reportedTabs();
    expect(reported, hasLength(_labels.length));
    var left = reported.first.$1;
    for (final (tabLeft, width) in reported) {
      expect(tabLeft, closeTo(left, 1), reason: 'Java hit-tests the tabs at their compressed widths');
      left = tabLeft + width;
    }
    expect(left, lessThanOrEqualTo(120));
  });

  testWidgets('ctabfolder_tab_scroll=false: tabs that do not fit at their minimum go behind the chevron',
      (tester) async {
    _useTabScroll(false);
    await _pump(tester, 70);

    expect(_horizontalScrollExtent(tester), 0, reason: 'the tab strip must not scroll');
    expect(find.byIcon(Icons.expand_more), findsOneWidget);
    // At its minimum a tab shows minimumCharacters, with no ellipsis when they number four or fewer
    // (CTabFolderRenderer.computeSize with MINIMUM_SIZE): the selected "Search" draws as "S".
    expect(find.text('S').hitTestable(), findsWidgets);
    expect(find.text('T').hitTestable(), findsNothing, reason: 'the tabs past the strip are not drawn');
    final parked = _reportedTabs().where((tab) => tab.$1 >= 70);
    expect(parked, isNotEmpty, reason: 'Java finds no tab under a point past the folder');
  });

  testWidgets('ctabfolder_tab_scroll=false: the strip report shows only the tabs before the chevron',
      (tester) async {
    _useTabScroll(false);
    await _pump(tester, 70);

    final sections = _strips.last.split(';');
    expect(sections, hasLength(6),
        reason: 'tabs; close buttons; shown part of the strip; shortened labels; folder width; layout seq');
    final shown = sections[2].split(',').map(int.parse).toList();
    final right = shown[0] + shown[1];
    final tabs = _reportedTabs();
    expect(tabs.first.$1 + tabs.first.$2, lessThanOrEqualTo(right + 1),
        reason: 'the selected tab is shown');
    expect(tabs.where((tab) => tab.$1 >= right), isNotEmpty,
        reason: 'Java counts the parked tabs as not showing, so the chevron list bolds them');
  });

  for (final width in [120.0, 400.0, 1200.0]) {
    testWidgets('ctabfolder_tab_scroll=false: the strip report names the labels drawn shortened at $width px',
        (tester) async {
      _useTabScroll(false);
      await _pump(tester, width);

      final expected = [
        for (final label in _labels) find.text(label).evaluate().isEmpty ? '1' : '0',
      ];
      expect(_strips.last.split(';')[3], expected.join(','),
          reason: 'Java shows the whole text as the tooltip of a tab whose label is shortened');
      if (width == 400) {
        expect(expected, containsAll(['0', '1']), reason: 'sanity: some labels fit whole, some do not');
      }
    });
  }

  testWidgets('ctabfolder_tab_scroll=false: a folder wide enough shows full labels', (tester) async {
    _useTabScroll(false);
    await _pump(tester, 1200);

    for (final label in _labels) {
      expect(find.text(label), findsOneWidget, reason: 'minimumCharacters applies only when tabs do not fit');
    }
    expect(find.byIcon(Icons.expand_more), findsNothing);
  });

  testWidgets('default: a narrow folder keeps the scrolling tab strip', (tester) async {
    _useTabScroll(null);
    await _pump(tester, 236);

    expect(_horizontalScrollExtent(tester), greaterThan(0));
  });

  testWidgets('ctabfolder_tab_scroll=false: compressed tabs paint the same backgrounds as scrolling ones',
      (tester) async {
    _useTabScroll(true);
    await _pump(tester, 236);
    final scrolling = _tabBackgrounds(tester);
    await tester.pumpWidget(const SizedBox());
    _useTabScroll(false);
    await _pump(tester, 236);

    expect(find.text('Search'), findsNothing, reason: 'the tabs are compressed');
    expect(scrolling, hasLength(_labels.length));
    expect(_tabBackgrounds(tester), scrolling);
  });
  testWidgets('ctabfolder_tab_scroll=false: a tab that changes width without a rebuild is reported again',
      (tester) async {
    _useTabScroll(false);
    await _pump(tester, 236);
    expect(find.byIcon(Icons.expand_more), findsNothing);

    // An icon that loads late resizes a tab the same way: a relayout, with no widget rebuilt.
    final label = tester.renderObjectList(find.descendant(
        of: find.byType(CompressedTabRow),
        matching: find.byWidgetPredicate((w) => w.runtimeType.toString() == '_LabelIntrinsics'))).first;
    (label as dynamic).minWidth = (label as RenderBox).getMinIntrinsicWidth(double.infinity) + 30;
    for (var i = 0; i < 3; i++) {
      await tester.pump();
    }
    expect(find.byIcon(Icons.expand_more), findsNothing, reason: 'the tabs still fit at their minimum');

    final row = tester.renderObject<RenderCompressedTabRow>(find.byType(CompressedTabRow));
    final drawn = <int>[];
    for (RenderBox? tab = row.firstChild; tab != null; tab = row.childAfter(tab)) {
      drawn.add(tab.size.width.round());
    }
    expect([for (final (_, width) in _reportedTabs()) width], drawn,
        reason: 'Java hit-tests the tabs where they are drawn now');
  });

  testWidgets(
      'ctabfolder_tab_scroll=false, minimize/maximize shown: the first width that shows every tab '
      'shows each at its minimum label', (tester) async {
    _useTabScroll(false);
    // Widen as a resized view stack does until no tab is left behind the chevron.
    var width = 40.0;
    for (;; width++) {
      await _pump(tester, width, minMax: true);
      if (find.byIcon(Icons.expand_more).evaluate().isEmpty) break;
      expect(width, lessThan(1200), reason: 'some width shows every tab');
    }

    // CTabFolder.setItemSize decides overflow before it reserves the chevron, and the minimize and
    // maximize buttons always take their width, so every tab shows at its minimum before any grows.
    expect(_drawnLabels(tester), [for (final label in _labels) label[0]],
        reason: 'at width $width all tabs fit only at their minimum');
  });

  testWidgets(
      'ctabfolder_tab_scroll=false: in the frame the tabs stop overflowing, no tab paints past the row',
      (tester) async {
    _useTabScroll(false);
    var width = 40.0;
    for (;; width++) {
      await _pump(tester, width, minMax: true);
      if (find.byIcon(Icons.expand_more).evaluate().isEmpty) break;
      expect(width, lessThan(1200), reason: 'some width shows every tab');
    }
    await _pump(tester, width - 1, minMax: true);
    expect(find.byIcon(Icons.expand_more), findsOneWidget);

    // One frame only: the chevron goes on the rebuild that follows it.
    await tester.pumpWidget(_host(width, minMax: true));
    expect(find.byIcon(Icons.expand_more), findsOneWidget);
    final row = tester.renderObject<RenderCompressedTabRow>(find.byType(CompressedTabRow));
    var end = 0.0;
    for (RenderBox? tab = row.firstChild; tab != null; tab = row.childAfter(tab)) {
      end = (tab.parentData! as BoxParentData).offset.dx + tab.size.width;
    }
    expect(end, greaterThan(row.size.width), reason: 'the tabs are laid out for the chevron-free width');
    expect(row, paints..clipRect(rect: Offset.zero & row.size));
  });
}
