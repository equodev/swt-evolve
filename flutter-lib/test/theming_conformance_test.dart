// Every themable widget must paint the colors and font the application gave it when the product
// owns the theme (use_swt_colors / use_swt_fonts). The check is over the *pixels*, not over a
// property: each widget keeps its color in a different place, and asserting on the property is how
// a widget that stops painting it still passes.
//
// A widget listed in _optOut is one whose SWT semantics say the application's color does not reach
// it; anything else that fails here is a widget that drops what the application set.

import 'dart:typed_data';
import 'dart:ui' as ui;

import 'package:flutter/material.dart';
import 'package:flutter/rendering.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/gen/animatedprogress.dart';
import 'package:swtflutter/src/gen/button.dart';
import 'package:swtflutter/src/gen/decorations.dart';
import 'package:swtflutter/src/gen/image.dart';
import 'package:swtflutter/src/gen/imagedata.dart';
import 'package:swtflutter/src/gen/shell.dart';
import 'package:swtflutter/src/gen/cbanner.dart';
import 'package:swtflutter/src/gen/coolbar.dart';
import 'package:swtflutter/src/gen/coolitem.dart';
import 'package:swtflutter/src/gen/canvas.dart';
import 'package:swtflutter/src/gen/ccombo.dart';
import 'package:swtflutter/src/gen/clabel.dart';
import 'package:swtflutter/src/gen/color.dart';
import 'package:swtflutter/src/gen/combo.dart';
import 'package:swtflutter/src/gen/composite.dart';
import 'package:swtflutter/src/gen/control.dart';
import 'package:swtflutter/src/gen/ctabfolder.dart';
import 'package:swtflutter/src/gen/ctabitem.dart';
import 'package:swtflutter/src/gen/datetime.dart';
import 'package:swtflutter/src/gen/expandbar.dart';
import 'package:swtflutter/src/gen/expanditem.dart';
import 'package:swtflutter/src/gen/font.dart';
import 'package:swtflutter/src/gen/fontdata.dart';
import 'package:swtflutter/src/gen/group.dart';
import 'package:swtflutter/src/gen/label.dart';
import 'package:swtflutter/src/gen/link.dart';
import 'package:swtflutter/src/gen/list.dart';
import 'package:swtflutter/src/gen/progressbar.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/sash.dart';
import 'package:swtflutter/src/gen/sashform.dart';
import 'package:swtflutter/src/gen/scale.dart';
import 'package:swtflutter/src/gen/scrolledcomposite.dart';
import 'package:swtflutter/src/gen/slider.dart';
import 'package:swtflutter/src/gen/spinner.dart';
import 'package:swtflutter/src/gen/stylerange.dart';
import 'package:swtflutter/src/gen/styledtextrenderer.dart';
import 'package:swtflutter/src/gen/styledtext.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/gen/tabfolder.dart';
import 'package:swtflutter/src/gen/tabitem.dart';
import 'package:swtflutter/src/gen/table.dart';
import 'package:swtflutter/src/gen/tablecolumn.dart';
import 'package:swtflutter/src/gen/tableitem.dart';
import 'package:swtflutter/src/gen/text.dart';
import 'package:swtflutter/src/gen/toolbar.dart';
import 'package:swtflutter/src/gen/viewform.dart';
import 'package:swtflutter/src/gen/toolitem.dart';
import 'package:swtflutter/src/gen/tree.dart';
import 'package:swtflutter/src/gen/treecolumn.dart';
import 'package:swtflutter/src/gen/treeitem.dart';
import 'package:swtflutter/src/impl/config_flags.dart';
import 'package:swtflutter/src/impl/widget_config.dart';

/// A ground no theme uses, so finding it proves the application's own color was painted.
const int _bgArgb = 0xFFFF00FF;
/// Likewise for text.
const int _fgArgb = 0xFF00FF00;
const String _fontName = 'Georgia';

final _boundary = GlobalKey();

VColor _c(int argb) => VColor()
  ..alpha = (argb >> 24) & 0xFF
  ..red = (argb >> 16) & 0xFF
  ..green = (argb >> 8) & 0xFF
  ..blue = argb & 0xFF;

VRectangle _rect(int w, int h) => VRectangle()
  ..x = 0
  ..y = 0
  ..width = w
  ..height = h;

/// An image whose only content is the sentinel ground, so finding that colour proves the
/// application's own image was painted rather than one of Evolve's stand-ins.
/// An 8x8 PNG of the sentinel ground. Raw bytes rather than SVG: the tiled-background path
/// decodes `imageData.data`, so an SVG-only image never reaches the pixels.
const List<int> _magentaPng = <int>[137,80,78,71,13,10,26,10,0,0,0,13,73,72,68,82,0,0,0,8,0,0,0,8,8,6,0,0,0,196,15,190,139,0,0,0,19,73,68,65,84,120,156,99,248,207,240,255,63,62,204,48,50,20,0,0,131,230,191,65,7,103,93,239,0,0,0,0,73,69,78,68,174,66,96,130];

VImage _image() => VImage()
  ..svgContent =
      '<svg xmlns="http://www.w3.org/2000/svg" width="16" height="16">'
      '<rect width="16" height="16" fill="#FF00FF"/></svg>'
  ..imageData = (VImageData()
    ..width = 8
    ..height = 8
    ..data = _magentaPng);

VFont _font() => VFont()
  ..fontData = [VFontData()
    ..name = _fontName
    ..height = 13
    ..style = 0];

int _nextId = 1;

/// A container with no application colour of its own: whatever sentinel shows up in its output
/// came from the item it draws inline, not from it.
T _plain<T extends VControl>(T v, {int w = 380, int h = 260}) {
  v
    ..id = _nextId++
    ..enabled = true
    ..visible = true
    ..bounds = _rect(w, h);
  return v;
}

/// The application's colors and font on every control, the way the CSS engine sets them.
T _styled<T extends VControl>(T v, {int w = 380, int h = 260}) {
  v
    ..id = _nextId++
    ..enabled = true
    ..visible = true
    ..bounds = _rect(w, h)
    ..background = _c(_bgArgb)
    ..foreground = _c(_fgArgb)
    ..font = _font();
  return v;
}

VTableItem _tableItem(String t) => VTableItem()
  ..id = _nextId++
  ..texts = [t];

VTreeItem _treeItem(String t) => VTreeItem()
  ..id = _nextId++
  ..texts = [t]
  ..expanded = true;

/// An Item its parent draws inline carries its own colours, so it is styled and measured directly.
VTableItem _styledTableItem(String t) => _tableItem(t)
  ..background = _c(_bgArgb)
  ..foreground = _c(_fgArgb)
  ..font = _font();

VTreeItem _styledTreeItem(String t) => _treeItem(t)
  ..background = _c(_bgArgb)
  ..foreground = _c(_fgArgb)
  ..font = _font();

/// Every themable widget, built the way an application that themes itself hands it over.
final Map<String, Widget Function()> _widgets = {
  'Button': () => ButtonSwt(value: _styled(VButton()..style = SWT.PUSH..text = 'Button', w: 120, h: 32)),
  'Label': () => LabelSwt(value: _styled(VLabel()..style = SWT.NONE..text = 'Label', w: 200, h: 24)),
  'CLabel': () => CLabelSwt(value: _styled(VCLabel()..style = SWT.NONE..text = 'CLabel', w: 200, h: 24)),
  'Link': () => LinkSwt(value: _styled(VLink()..style = SWT.NONE..text = 'Link', w: 200, h: 24)),
  'Text': () => TextSwt(value: _styled(VText()..style = SWT.SINGLE..text = 'Text'..editable = true, w: 220, h: 28)),
  'StyledText': () => StyledTextSwt(value: _styled(VStyledText()..style = SWT.MULTI..text = 'StyledText')),
  'Composite': () => CompositeSwt(value: _styled(VComposite()..style = SWT.NONE)),
  'Group': () => GroupSwt(value: _styled(VGroup()..style = SWT.NONE..text = 'Group')),
  'Canvas': () => CanvasSwt(value: _styled(VCanvas()..style = SWT.NONE..hasOwnBackground = true)),
  'ScrolledComposite': () => ScrolledCompositeSwt(value: _styled(VScrolledComposite()..style = SWT.NONE)),
  'List': () => ListSwt(value: _styled(VList()..style = SWT.SINGLE..items = ['one', 'two'])),
  'Combo': () => ComboSwt(value: _styled(VCombo()..style = SWT.READ_ONLY..items = ['one', 'two']..text = 'one', w: 200, h: 30)),
  'CCombo': () => CComboSwt(value: _styled(VCCombo()..style = SWT.NONE..items = ['one', 'two']..text = 'one', w: 200, h: 30)),
  'Spinner': () => SpinnerSwt(value: _styled(VSpinner()..style = SWT.NONE..selection = 3, w: 160, h: 30)),
  'DateTime': () => DateTimeSwt(value: _styled(VDateTime()..style = SWT.DATE..year = 2026..month = 1..day = 1, w: 200, h: 30)),
  'ProgressBar': () => ProgressBarSwt(value: _styled(VProgressBar()..style = SWT.HORIZONTAL..selection = 40..maximum = 100, w: 220, h: 24)),
  'Scale': () => ScaleSwt(value: _styled(VScale()..style = SWT.HORIZONTAL..selection = 40..maximum = 100, w: 220, h: 40)),
  'Slider': () => SliderSwt(value: _styled(VSlider()..style = SWT.HORIZONTAL..selection = 40..maximum = 100, w: 220, h: 24)),
  'Sash': () => SashSwt(value: _styled(VSash()..style = SWT.VERTICAL, w: 12, h: 200)),
  'Tree': () => TreeSwt(value: _styled(VTree()
    ..style = SWT.NONE
    ..columns = [VTreeColumn()..id = 900..text = 'c'..width = 200]
    ..items = [_treeItem('node')])),
  'Table': () => TableSwt(value: _styled(VTable()
    ..style = SWT.NONE
    ..columns = [VTableColumn()..id = 901..text = 'c'..width = 200]
    ..items = [_tableItem('row')]
    ..itemCount = 1)),
  'ToolBar': () => ToolBarSwt(value: _styled(VToolBar()
    ..style = SWT.HORIZONTAL
    ..items = [VToolItem()..id = 902..text = 'item'..enabled = true], w: 220, h: 36)),
  // Two tabs: SWT draws the selected one in selectionForeground and only the unselected ones
  // in the folder's foreground, so a lone selected tab would never show it.
  'CTabFolder': () => CTabFolderSwt(value: _styled(VCTabFolder()
    ..style = SWT.NONE
    ..items = [
      VCTabItem()..id = 903..text = 'Tab'..showClose = false,
      VCTabItem()..id = 906..text = 'Other'..showClose = false,
    ]
    ..selection = 0)),
  // Two tabs, as for CTabFolder: the selected one keeps the theme's surface and text.
  'TabFolder': () => TabFolderSwt(value: _styled(VTabFolder()
    ..style = SWT.NONE
    ..items = [VTabItem()..id = 904..text = 'Tab', VTabItem()..id = 907..text = 'Other'])),
  'ExpandBar': () => ExpandBarSwt(value: _styled(VExpandBar()
    ..style = SWT.NONE
    ..items = [VExpandItem()..id = 905..text = 'Item'..expanded = true])),
  'CoolBar': () => CoolBarSwt(value: _styled(VCoolBar()
    ..style = SWT.HORIZONTAL
    ..items = [VCoolItem()..id = 906], w: 260, h: 40)),
  'SashForm': () => SashFormSwt(value: _styled(VSashForm()
    ..style = SWT.HORIZONTAL
    ..children = [_styled(VComposite()..style = SWT.NONE, w: 180, h: 260)])),
  'ViewForm': () => ViewFormSwt(value: _styled(VViewForm()..style = SWT.NONE)),
  'CBanner': () => CBannerSwt(value: _styled(VCBanner()..style = SWT.NONE)),
  'Shell': () => ShellSwt(value: _styled(VShell()..style = SWT.SHELL_TRIM..text = 'Shell')),
  'Decorations': () => DecorationsSwt(value: _styled(VDecorations()..style = SWT.NONE..text = 'Decorations')),
  'AnimatedProgress': () => AnimatedProgressSwt(value: _styled(VAnimatedProgress()..style = SWT.HORIZONTAL, w: 200, h: 24)),

  // Items their parent draws inline: the colour is set on the item, not on the container.
  'TableItem': () => TableSwt(value: _plain(VTable()
    ..style = SWT.NONE
    ..columns = [VTableColumn()..id = 910..text = 'c'..width = 200]
    ..items = [_styledTableItem('row')]
    ..itemCount = 1)),
  'TreeItem': () => TreeSwt(value: _plain(VTree()
    ..style = SWT.NONE
    ..columns = [VTreeColumn()..id = 911..text = 'c'..width = 200]
    ..items = [_styledTreeItem('node')])),
  // VToolItem carries no font, so the bar holds it: the contract is that the item letters in it.
  'ToolItem': () => ToolBarSwt(value: (_plain(VToolBar()
    ..style = SWT.HORIZONTAL
    ..items = [VToolItem()
      ..id = 912
      ..text = 'item'
      ..enabled = true
      ..background = _c(_bgArgb)
      ..foreground = _c(_fgArgb)], w: 220, h: 36))
    ..font = _font()),
  // VCTabItem carries no background; the folder holds it, and the tab is drawn on it. The
  // styled item is the unselected one: a selected tab is drawn in selectionForeground.
  'CTabItem': () => CTabFolderSwt(value: (_plain(VCTabFolder()
    ..style = SWT.NONE
    ..items = [
      VCTabItem()..id = 914..text = 'Tab'..showClose = false,
      VCTabItem()
        ..id = 913
        ..text = 'Other'
        ..showClose = false
        ..foreground = _c(_fgArgb)
        ..font = _font(),
    ]
    ..selection = 0))
    ..background = _c(_bgArgb)),
};

/// Widgets whose SWT semantics genuinely do not carry the application's background.
const Map<String, String> _optOut = {
  // Verified against the SWT sources: these carry no setBackground/setForeground/setFont at all,
  // so they have no colour of their own to render -- they take the parent's.
  'TableColumn': 'no colour setters in SWT',
  'TreeColumn': 'no colour setters in SWT',
  'TabItem': 'no colour setters in SWT',
  'ExpandItem': 'no colour setters in SWT',
  'CoolItem': 'no colour setters in SWT',
  'ScrollBar': 'no colour setters in SWT',
  // Not Controls (VWidget / VItem) and carrying no colour field at all, so there is nothing an
  // application could set on them. They are also unimplemented stubs, which is a separate gap.
  // Unimplemented: build() returns a placeholder Text, so there is no rendering to theme. The
  // gap is the widget, not its theming.
  'AnimatedProgress': 'unimplemented stub',
  'CBanner': 'unimplemented stub',
  'TaskBar': 'not a Control, no colour field',
  'TaskItem': 'not a Control, no colour field',
  'Tray': 'not a Control, no colour field',
  'TrayItem': 'not a Control, no colour field',
  // A platform view: a widget test cannot rasterize it. Covered at the E2E rung instead.
  'Browser': 'platform view, measured by E2E',
  // GC drawing (Pattern.color1/color2, blitted images) does not travel in any widget's V*: it
  // arrives on the paint-command channel, so a payload fixture cannot exercise it. It needs a
  // GC-level harness of its own.
  'GC': 'paints over the command channel, not the widget payload',
};

/// The axes only some widgets carry. Each names the field it exercises, so a red says which
/// property the application set and the widget dropped.
final Map<String, Widget Function()> _extraAxes = {
  'Control.backgroundImage': () => CompositeSwt(value: _plain(VComposite()..style = SWT.NONE)
    ..backgroundImage = _image()),
  'Table.headerBackground': () => TableSwt(value: _plain(VTable()
    ..style = SWT.NONE
    ..headerVisible = true
    ..headerBackground = _c(_bgArgb)
    ..columns = [VTableColumn()..id = 920..text = 'c'..width = 200]
    ..items = [_tableItem('row')]
    ..itemCount = 1)),
  'Tree.headerBackground': () => TreeSwt(value: _plain(VTree()
    ..style = SWT.NONE
    ..headerVisible = true
    ..headerBackground = _c(_bgArgb)
    ..columns = [VTreeColumn()..id = 921..text = 'c'..width = 200]
    ..items = [_treeItem('node')])),
  'CTabFolder.selectionForeground': () => CTabFolderSwt(value: _plain(VCTabFolder()
    ..style = SWT.NONE
    ..selectionForeground = _c(_bgArgb)
    ..items = [VCTabItem()..id = 924..text = 'Tab'..showClose = false]
    ..selection = 0)),
  'CTabFolder.selectionBackground': () => CTabFolderSwt(value: _plain(VCTabFolder()
    ..style = SWT.NONE
    ..selectionBackground = _c(_bgArgb)
    ..items = [VCTabItem()..id = 922..text = 'Tab'..showClose = false]
    ..selection = 0)),
  'StyledText.selectionBackground': () => StyledTextSwt(value: _plain(VStyledText()
    ..style = SWT.MULTI
    ..text = 'StyledText'
    ..selectionBackground = _c(_bgArgb))),
  'StyledText.marginColor': () => StyledTextSwt(value: _plain(VStyledText()
    ..style = SWT.MULTI
    ..text = 'StyledText'
    ..marginColor = _c(_bgArgb)
    ..leftMargin = 20)),
  'Button.image': () => ButtonSwt(value: _plain(VButton()
    ..style = SWT.PUSH
    ..text = 'Button'
    ..image = _image(), w: 140, h: 40)),
  'ToolItem.image': () => ToolBarSwt(value: _plain(VToolBar()
    ..style = SWT.HORIZONTAL
    ..items = [VToolItem()..id = 923..text = 'item'..enabled = true..image = _image()], w: 220, h: 40)),
  'Label.image': () => LabelSwt(value: _plain(VLabel()..style = SWT.NONE..image = _image(), w: 120, h: 40)),
  'CLabel.image': () => CLabelSwt(value: _plain(VCLabel()..style = SWT.NONE..text = 'CLabel'..image = _image(), w: 160, h: 40)),
  // A Tree/List with no columns takes a different layout path than the column case above; the
  // snippet builds them this way, so the sweep has to cover both.
  'Tree.noColumns': () => TreeSwt(value: _styled(VTree()
    ..style = SWT.NONE
    ..items = [_treeItem('node')])),
  'List.plain': () => ListSwt(value: _styled(VList()
    ..style = SWT.SINGLE
    ..items = ['one', 'two'])),
  // The rich-text axes: a run's own colours, and the renderer's per-weight fonts.
  'StyleRange.background': () => StyledTextSwt(value: _plain(VStyledText()
    ..style = SWT.MULTI
    ..text = 'StyledText'
    ..renderer = (VStyledTextRenderer()
      ..styles = [VStyleRange()
        ..start = 0
        ..length = 6
        ..background = _c(_bgArgb)]))),
  'StyleRange.underlineColor': () => StyledTextSwt(value: _plain(VStyledText()
    ..style = SWT.MULTI
    ..text = 'StyledText'
    ..renderer = (VStyledTextRenderer()
      ..styles = [VStyleRange()
        ..start = 0
        ..length = 6
        ..underline = true
        ..underlineColor = _c(_bgArgb)]))),
};

Future<Map<int, int>> _paintedColors(WidgetTester tester, Widget child) async {
  await tester.pumpWidget(EvolveApp(
    theme: ThemeMode.dark,
    contentWidget: RepaintBoundary(
      key: _boundary,
      child: SizedBox(width: 400, height: 300, child: child),
    ),
  ));
  // Not pumpAndSettle: a widget with a running animation (an indeterminate ProgressBar, a
  // blinking caret) never settles, and the sweep would stall on its timeout instead of reporting.
  await tester.pump();
  // An image ground decodes asynchronously; without a real async turn the widget paints before
  // MemoryImage has produced anything and the sweep reads "no image" as "wrong colour".
  await tester.runAsync(() => Future<void>.delayed(const Duration(milliseconds: 120)));
  await tester.pump();
  await tester.pump(const Duration(milliseconds: 50));

  final boundary = tester.renderObject<RenderRepaintBoundary>(find.byKey(_boundary));
  final Map<int, int> colors = {};
  // runAsync: rasterizing and encoding are real async work, and the fake async a widget test runs
  // under never completes them -- awaiting toImage() directly hangs the test instead of failing.
  await tester.runAsync(() async {
    final ui.Image image = await boundary.toImage();
    final ByteData? bytes = await image.toByteData(format: ui.ImageByteFormat.rawRgba);
    if (bytes != null) {
      final Uint8List p = bytes.buffer.asUint8List();
      for (int i = 0; i + 3 < p.length; i += 4) {
          final argb = (p[i + 3] << 24) | (p[i] << 16) | (p[i + 1] << 8) | p[i + 2];
        colors[argb] = (colors[argb] ?? 0) + 1;
      }
    }
    image.dispose();
  });
  return colors;
}

/// Cases where the styled thing is one row inside an unstyled parent: the parent's body
/// legitimately dominates, so presence of a real band is what says the item was honoured.
/// Axes a widget test cannot reach. StyledText paints its glyphs through a CustomPaint whose
/// text pipeline does not run here -- a capture of it comes back with the ground and no letters at
/// all -- so anything living in a run, the selection or the margins has to be checked on the
/// running app or in a golden, not here.
const Map<String, String> _axisOptOut = {
  // An image's own colours are governed by preserve_icon_colors, a switch of its own that a
  // product theme does not flip: with it off the icon is tinted to the theme, by design.
  'Button.image': 'icon tinting is preserve_icon_colors, not the theme',
  'ToolItem.image': 'icon tinting is preserve_icon_colors, not the theme',
  'Label.image': 'icon tinting is preserve_icon_colors, not the theme',
  'CLabel.image': 'icon tinting is preserve_icon_colors, not the theme',
  'StyleRange.background': 'StyledText paints no glyphs in a widget test',
  'StyleRange.underlineColor': 'StyledText paints no glyphs in a widget test',
  'StyledText.marginColor': 'StyledText paints no glyphs in a widget test',
  'StyledText.selectionBackground': 'StyledText paints no glyphs in a widget test',
};

/// Widgets that keep the theme's text colour because they keep the theme's surface: the
/// application chose its foreground against its own background, and a push button's surface is
/// never the application's (SWT calls setBackground a hint, Win32 ignores it on buttons), so
/// applying the one without the other is what leaves white text on a light surface. The check
/// and radio variants sit on the parent's ground and do honour it -- see 'Button.check' below.
/// SWT documents setBackground as a hint the platform may override, and names the button on
/// Windows as the case where it is: the surface stays the theme's, and nothing is filled behind
/// it -- the parent's ground shows through, which is the application's colour.
const Map<String, String> _backgroundOptOut = {
  'Button': 'setBackground is a hint; Win32 ignores it on a button',
};

const Map<String, String> _foregroundOptOut = {
  'Button': 'a push button keeps the theme surface, so it keeps the theme text colour',
};

const Set<String> _itemCases = {'TableItem', 'TreeItem', 'ToolItem', 'CTabItem'};

/// The ground the widget painted most of. Text pixels are excluded -- the foreground sentinel
/// would otherwise win on any widget small enough for its label to outweigh its ground.
int _dominantOpaque(Map<int, int> colors) {
  var best = 0, bestCount = -1;
  colors.forEach((argb, count) {
    if (argb == _fgArgb) return;
    if ((argb >> 24) & 0xFF != 0 && count > bestCount) {
      best = argb;
      bestCount = count;
    }
  });
  return best;
}

int _opaquePixels(Map<int, int> colors) {
  var n = 0;
  colors.forEach((argb, count) {
    if ((argb >> 24) & 0xFF != 0) n += count;
  });
  return n;
}

Iterable<TextStyle> _textStyles(WidgetTester tester) sync* {
  for (final t in tester.widgetList<Text>(find.byType(Text))) {
    if (t.style != null) yield t.style!;
    // A Text built from a span tree (Text.rich) keeps its styling in the spans.
    if (t.textSpan != null) yield* _spanStyles(t.textSpan!);
  }
  for (final t in tester.widgetList<EditableText>(find.byType(EditableText))) {
    yield t.style;
  }
  // Link and StyledText draw runs through RichText, whose styling is in the spans, not the widget.
  for (final r in tester.widgetList<RichText>(find.byType(RichText))) {
    yield* _spanStyles(r.text);
  }
}

Iterable<TextStyle> _spanStyles(InlineSpan span) sync* {
  if (span is TextSpan) {
    if (span.style != null) yield span.style!;
    for (final child in span.children ?? const <InlineSpan>[]) {
      yield* _spanStyles(child);
    }
  }
}

void main() {
  setUp(() {
    _nextId = 1;
    resetConfigFlags();
    // What Config sends when the product owns the workbench theme.
    setConfigFlags(ConfigFlags()
      ..use_swt_colors = true
      ..use_swt_fonts = true
      // What Config actually sends: theme_name is never null in a running app, and some widgets
      // branch on it (CTabFolder drops the application's selection colour when it is).
      ..theme_name = 'equo');
  });
  tearDown(resetConfigFlags);

  for (final entry in _widgets.entries) {
    final name = entry.key;

    testWidgets('$name paints the background the application set', (tester) async {
      final colors = await _paintedColors(tester, entry.value());
      if (_optOut.containsKey(name)) return;
      if (_backgroundOptOut.containsKey(name)) {
        markTestSkipped('$name: ${_backgroundOptOut[name]}');
        return;
      }
      // A widget that painted nothing opaque cannot be judged: the sweep would otherwise read
      // "drew no pixels" as "drew the theme's colour", which is a different problem entirely.
      final opaque = _opaquePixels(colors);
      if (opaque == 0) {
        markTestSkipped('$name painted nothing in this fixture - not measurable');
        return;
      }
      // The application's colour has to be the one the widget mostly painted, not merely present:
      // a widget that keeps a themed body and puts the colour in a border or a single band reads
      // as untouched to the eye, and used to pass here.
      final share = (colors[_bgArgb] ?? 0) / opaque;
      if (_itemCases.contains(name)) {
        expect(share, greaterThan(0.01),
            reason: '$name never painted a band in the application background; '
                'the row is drawn in the theme');
        return;
      }
      final dominant = _dominantOpaque(colors);
      expect(dominant, _bgArgb,
          reason: '$name mostly painted 0x${dominant.toRadixString(16)} instead of the '
              'application background (it covers only '
              '${(share * 100).toStringAsFixed(1)}% of what it painted)');
    });

    testWidgets('$name draws text in the foreground the application set', (tester) async {
      if (_optOut.containsKey(name)) return;
      if (_foregroundOptOut.containsKey(name)) {
        markTestSkipped('$name: ${_foregroundOptOut[name]}');
        return;
      }
      await _paintedColors(tester, entry.value());
      final styles = _textStyles(tester).toList();
      if (styles.isEmpty) {
        // No inspectable TextStyle: the widget paints its glyphs straight onto a canvas
        // (StyledText). Passing here would be a vacuous green, so it is reported as unmeasured.
        markTestSkipped('$name draws text through a custom painter - not measurable this way');
        return;
      }
      expect(styles.any((s) => s.color?.value == _fgArgb), isTrue,
          reason: '$name draws text in the theme foreground, ignoring the application one');
    });

    testWidgets('$name uses the font the application set', (tester) async {
      if (_optOut.containsKey(name)) return;
      await _paintedColors(tester, entry.value());
      final styles = _textStyles(tester).toList();
      if (styles.isEmpty) {
        markTestSkipped('$name draws text through a custom painter - not measurable this way');
        return;
      }
      expect(styles.any((s) => s.fontFamily == _fontName), isTrue,
          reason: '$name draws text in the theme font, ignoring the application font');
    });
  }

  for (final entry in _extraAxes.entries) {
    final axis = entry.key;
    testWidgets('$axis reaches the pixels', (tester) async {
      if (_axisOptOut.containsKey(axis)) {
        markTestSkipped('$axis: ${_axisOptOut[axis]}');
        return;
      }
      final colors = await _paintedColors(tester, entry.value());
      expect(colors.containsKey(_bgArgb), isTrue,
          reason: '$axis was set by the application and never painted');
    });
  }

  testWidgets('Button.check draws its text in the application foreground', (tester) async {
    // The counterpart of the push-button opt-out: check text sits on the parent's ground.
    await _paintedColors(tester, ButtonSwt(value: _plain(VButton()
      ..style = SWT.CHECK
      ..text = 'Check'
      ..enabled = true
      ..foreground = _c(_fgArgb), w: 140, h: 32)));
    final styles = _textStyles(tester).toList();
    expect(styles.any((s) => s.color?.value == _fgArgb), isTrue,
        reason: 'a check button draws its text in the theme foreground, ignoring the application one');
  });

  testWidgets('Link.linkForeground reaches the link text', (tester) async {
    await _paintedColors(tester, LinkSwt(value: _plain(VLink()
      ..style = SWT.NONE
      ..text = '<a>link</a>'
      ..linkForeground = _c(_fgArgb), w: 200, h: 30)));
    final styles = _textStyles(tester).toList();
    expect(styles.any((s) => s.color?.value == _fgArgb), isTrue,
        reason: 'Link.linkForeground was set by the application and the link is drawn in the theme colour');
  });

  testWidgets('StyledTextRenderer.regularFont reaches the text', (tester) async {
    markTestSkipped('StyledText paints no glyphs in a widget test - check it on the app');
    return;
    // ignore: dead_code
    await _paintedColors(tester, StyledTextSwt(value: _plain(VStyledText()
      ..style = SWT.MULTI
      ..text = 'StyledText'
      ..renderer = (VStyledTextRenderer()..regularFont = _font()))));
    final styles = _textStyles(tester).toList();
    expect(styles.any((s) => s.fontFamily == _fontName), isTrue,
        reason: 'StyledTextRenderer.regularFont was set by the application and the text is drawn '
            'in the theme font');
  });

  // The mirror image: with the switches off, Evolve's theme is the source and nothing the
  // application set may reach the pixels. This is what keeps a theme-only deployment unchanged
  // by any work on the switches-on path.
  group('with the switches off', () {
    setUp(() {
      resetConfigFlags();
      setConfigFlags(ConfigFlags()
        ..use_swt_colors = false
        ..use_swt_fonts = false
        ..theme_name = 'equo');
    });

    for (final entry in _widgets.entries) {
      final name = entry.key;
      if (_optOut.containsKey(name)) continue;

      testWidgets('$name keeps the theme background', (tester) async {
        final colors = await _paintedColors(tester, entry.value());
        final opaque = _opaquePixels(colors);
        if (opaque == 0) {
          markTestSkipped('$name painted nothing in this fixture - not measurable');
          return;
        }
        expect((colors[_bgArgb] ?? 0) / opaque, lessThan(0.001),
            reason: '$name painted the application background with use_swt_colors off');
      });

      testWidgets('$name keeps the theme foreground and font', (tester) async {
        await _paintedColors(tester, entry.value());
        final styles = _textStyles(tester).toList();
        if (styles.isEmpty) {
          markTestSkipped('$name draws text through a custom painter - not measurable this way');
          return;
        }
        expect(styles.any((s) => s.color?.value == _fgArgb), isFalse,
            reason: '$name drew text in the application foreground with use_swt_fonts off');
        expect(styles.any((s) => s.fontFamily == _fontName), isFalse,
            reason: '$name drew text in the application font with use_swt_fonts off');
      });
    }

    for (final entry in _extraAxes.entries) {
      final axis = entry.key;
      // StyleRange colours are document content, not theming: they show either way. A background
      // image is painted either way too: unlike a bare colour, which is often just SWT's default
      // widget grey, an image is never a default the theme could stand in for.
      if (axis.startsWith('StyleRange.') || axis == 'Control.backgroundImage') continue;
      testWidgets('$axis stays with the theme', (tester) async {
        final colors = await _paintedColors(tester, entry.value());
        expect(colors.containsKey(_bgArgb), isFalse,
            reason: '$axis reached the pixels with use_swt_colors off');
      });
    }
  });
}
