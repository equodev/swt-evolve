import 'dart:convert';
import 'dart:io';
import 'package:flutter/material.dart';
import 'package:flutter/scheduler.dart';
import 'package:swtflutter/src/custom/toolbar_composite.dart';
import 'package:swtflutter/src/gen/menu.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/impl/decorations_evolve.dart';
import 'package:swtflutter/src/theme/theme.dart';
import 'measure.dart' show ENABLE_INTERACTIVITY;

void main() => runApp(const _MeasureApp());

class _MeasureApp extends StatefulWidget {
  const _MeasureApp();
  @override
  State<_MeasureApp> createState() => _MeasureAppState();
}

class _MeasureAppState extends State<_MeasureApp> {
  final _btnKey      = GlobalKey();
  final _menuBarKey  = GlobalKey();

  Map<String, int>? _results;

  @override
  void initState() {
    super.initState();
    SchedulerBinding.instance.addPostFrameCallback((_) => _measure());
  }

  void _measure() {
    double sz(GlobalKey k, bool width) {
      final rb = k.currentContext!.findRenderObject()! as RenderBox;
      return width ? rb.size.width : rb.size.height;
    }
    final btnW             = sz(_btnKey, true).round();
    final heightVertical   = sz(_btnKey, false).round();
    final menuBarH         = sz(_menuBarKey, false).round();
    final heightHorizontal = heightVertical + menuBarH;

    final results = {
      'verticalMenuButtonWidth': btnW,
      'heightVerticalMenu':      heightVertical,
      'menuBarHeight':           menuBarH,
      'heightHorizontalMenu':    heightHorizontal,
    };
    stdout.writeln(jsonEncode(results));
    _writeMenuSizesJava(results);
    if (!ENABLE_INTERACTIVITY) {
      exit(0);
    } else {
      setState(() => _results = results);
    }
  }

  void _writeMenuSizesJava(Map<String, int> r) {
    final content = 'package dev.equo.swt.size;\n\n'
        'public class MenuSizes {\n'
        '    public static final int VERTICAL_MENU_BUTTON_WIDTH = ${r['verticalMenuButtonWidth']};\n'
        '    public static final int HEIGHT_VERTICAL_MENU       = ${r['heightVerticalMenu']};\n'
        '    public static final int HEIGHT_HORIZONTAL_MENU     = ${r['heightHorizontalMenu']};\n'
        '}\n';
    final file = File('../swt_native/src/main/java/dev/equo/swt/size/MenuSizes.java');
    file.parent.createSync(recursive: true);
    file.writeAsStringSync(content);
    print('Exported: ${file.path}');
  }

  @override
  Widget build(BuildContext context) {
    final dummyMenuBar = VMenu()..style = SWT.BAR..visible = true;

    return MaterialApp(
      theme: createLightDefaultTheme(null),
      home: Scaffold(
        appBar: AppBar(title: const Text('measure_menu')),
        body: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            DecorationsMenuData(
              menuBar: VMenu(),
              isAtStart: true,
              isHorizontal: false,
              child: IntrinsicHeight(
                child: Row(
                  crossAxisAlignment: CrossAxisAlignment.stretch,
                  mainAxisSize: MainAxisSize.min,
                  children: [
                    VerticalMenuButton(key: _btnKey, atStart: true),
                  ],
                ),
              ),
            ),
            const SizedBox(height: 8),
            DecorationsMenuData(
              menuBar: dummyMenuBar,
              isAtStart: false,
              isHorizontal: true,
              child: HorizontalMenuBar(key: _menuBarKey),
            ),
            if (_results != null)
              Padding(
                padding: const EdgeInsets.all(16),
                child: Text(
                  jsonEncode(_results),
                  style: const TextStyle(fontFamily: 'monospace', fontSize: 14),
                ),
              ),
          ],
        ),
      ),
    );
  }
}
