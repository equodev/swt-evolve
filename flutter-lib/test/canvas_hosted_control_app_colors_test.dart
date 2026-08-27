// A Canvas paints with the colors the application gave it unless disable_swt_canvas_colors hands
// that drawing to the theme. A control the application hosts *over* such a Canvas -- a grid's cell
// editor is the usual case -- is part of that drawing, so it must follow the same rule; taking the
// theme's colors instead puts a dark editor on an application-colored light grid.
//
// The boundary is the window: upstream SWT paints an editor that sits *in* a themed cell with that
// cell's colors, but opens its drop-down in the platform's own -- so a popup keeps the app theme.

import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/gen/canvas.dart';
import 'package:swtflutter/src/gen/color.dart';
import 'package:swtflutter/src/gen/control.dart';
import 'package:swtflutter/src/gen/datetime.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/gen/text.dart';
import 'package:swtflutter/src/gen/display.dart';
import 'package:swtflutter/src/gen/shell.dart';
import 'package:swtflutter/src/impl/config_flags.dart';
import 'package:swtflutter/src/impl/display_evolve.dart';
import 'package:swtflutter/src/impl/widget_config.dart';

const _editorBackground = Color(0xFFF0F0F0);
const _editorForeground = Color(0xFF000000);

VRectangle _rect(int x, int y, int w, int h) => VRectangle()
  ..x = x
  ..y = y
  ..width = w
  ..height = h;

VColor _vColor(int r, int g, int b) => VColor()
  ..alpha = 0xFF
  ..red = r
  ..green = g
  ..blue = b;

/// The Text a grid opens over its Canvas, carrying the cell's own colors.
VText _textEditor() => VText()
  ..id = 2
  ..style = SWT.SINGLE
  ..enabled = true
  ..visible = true
  ..editable = true
  ..bounds = _rect(40, 40, 99, 19)
  ..text = 'Timothy'
  ..background = _vColor(0xF0, 0xF0, 0xF0)
  ..foreground = _vColor(0x00, 0x00, 0x00);

/// The same, for a date column: a DateTime whose drop-down opens a calendar in an Overlay.
VDateTime _dateEditor() => VDateTime()
  ..id = 3
  ..style = SWT.DATE | SWT.DROP_DOWN
  ..enabled = true
  ..visible = true
  ..bounds = _rect(40, 40, 130, 19)
  ..year = 1966
  ..month = 10
  ..day = 6
  ..background = _vColor(0xF0, 0xF0, 0xF0)
  ..foreground = _vColor(0x00, 0x00, 0x00);

VCanvas _gridHosting(VControl editor) => VCanvas()
  ..id = 1
  ..style = SWT.NONE
  ..enabled = true
  ..visible = true
  ..hasOwnBackground = true
  ..background = _vColor(0xFF, 0xFF, 0xFF)
  ..bounds = _rect(0, 0, 400, 300)
  ..children = [editor];

Future<void> _pump(WidgetTester tester, VControl editor) async {
  await tester.pumpWidget(EvolveApp(
    theme: ThemeMode.dark,
    contentWidget: CanvasSwt<VCanvas>(value: _gridHosting(editor)),
  ));
  await tester.pumpAndSettle();
}

/// The DateTime field's own painted ground.
Color? _dateFieldBackground(WidgetTester tester) {
  for (final c in tester.widgetList<Container>(find.byType(Container))) {
    final d = c.decoration;
    if (d is BoxDecoration && d.border != null) return d.color;
  }
  return null;
}

void main() {
  setUp(resetConfigFlags);
  tearDown(resetConfigFlags);

  testWidgets('a Text hosted on an application-colored Canvas keeps the application colors',
      (tester) async {
    setConfigFlags(ConfigFlags()..disable_swt_canvas_colors = false);

    await _pump(tester, _textEditor());
    final field = tester.widget<TextField>(find.byType(TextField));

    expect(field.decoration!.fillColor, _editorBackground,
        reason: 'the grid under it keeps the application colors, so its editor must too');
    expect(field.style!.color, _editorForeground);
  });

  testWidgets('a DateTime hosted on an application-colored Canvas keeps the application colors',
      (tester) async {
    setConfigFlags(ConfigFlags()..disable_swt_canvas_colors = false);

    await _pump(tester, _dateEditor());

    expect(_dateFieldBackground(tester), _editorBackground);
    expect(tester.widget<Text>(find.text('11')).style!.color, _editorForeground);

    // The spinner buttons and the drop-down trigger stay on the theme: upstream SWT keeps those
    // platform affordances in the platform's colors even when the field follows the cell.
    for (final icon in tester.widgetList<Icon>(find.byType(Icon))) {
      expect(icon.color, isNot(_editorForeground));
    }
  });

  testWidgets('the drop-down calendar keeps the app theme, not the cell colors', (tester) async {
    setConfigFlags(ConfigFlags()..disable_swt_canvas_colors = false);

    await _pump(tester, _dateEditor());
    // Two arrow_drop_down icons: the spinner's own down button, then the drop-down trigger.
    await tester.tap(find.byIcon(Icons.arrow_drop_down).last);
    await tester.pumpAndSettle();

    final popup = tester
        .widgetList<Material>(find.byType(Material))
        .firstWhere((m) => m.elevation == 4);

    expect(popup.color, isNot(_editorBackground),
        reason: 'upstream SWT opens the drop-down in the platform colors even when the field it '
            'belongs to follows a themed cell -- the window is the boundary');
  });

  testWidgets("a dialog's own Text keeps the theme, mounted the way the Display mounts it",
      (tester) async {
    setConfigFlags(ConfigFlags()..disable_swt_canvas_colors = false);

    // Shell extends Decorations extends Canvas in SWT, so every Shell IS a CanvasImpl: without the
    // Decorations exclusion the scope is published inside every window and repaints the fields of
    // every dialog the application opens. Only the real Display mounting shows that -- a Canvas
    // pumped on its own never puts a Shell in the picture.
    await tester.pumpWidget(EvolveApp(
      theme: ThemeMode.dark,
      contentWidget: DisplaySwt(
        value: VDisplay()
          ..shells = [
            VShell()
              ..id = 1
              ..style = SWT.SHELL_TRIM
              ..text = 'main'
              ..bounds = _rect(0, 0, 800, 600)
              ..children = [_gridHosting(_textEditor())],
            VShell()
              ..id = 5
              ..style = SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL
              ..text = 'dialog'
              ..bounds = _rect(200, 150, 400, 300)
              ..children = [_textEditor()..id = 6],
          ],
      ),
    ));
    await tester.pumpAndSettle();

    final fills = tester
        .widgetList<TextField>(find.byType(TextField))
        .map((f) => f.decoration!.fillColor)
        .toList();

    expect(fills.length, 2);
    expect(fills.where((c) => c == _editorBackground).length, 1,
        reason: "exactly one of them -- the grid's editor -- follows the application colors; the "
            "dialog's own field keeps the theme however its background was set");
  });

  testWidgets('a control hosted on a themed Canvas stays themed', (tester) async {
    setConfigFlags(ConfigFlags()..disable_swt_canvas_colors = true);

    await _pump(tester, _textEditor());
    final field = tester.widget<TextField>(find.byType(TextField));

    expect(field.decoration!.fillColor, isNot(_editorBackground),
        reason: 'the grid under it was handed to the theme, so its editor follows the theme too');
    expect(field.style!.color, isNot(_editorForeground));
  });
}
