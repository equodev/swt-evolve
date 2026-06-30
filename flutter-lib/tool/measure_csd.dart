import 'dart:convert';
import 'dart:io';
import 'package:flutter/material.dart';
import 'package:flutter/scheduler.dart';
import 'package:swtflutter/src/custom/csd/window_controls.dart';
import 'package:swtflutter/src/theme/theme.dart';
import 'measure.dart' show ENABLE_INTERACTIVITY;

void main() => runApp(const _MeasureApp());

class _MeasureApp extends StatefulWidget {
  const _MeasureApp();
  @override
  State<_MeasureApp> createState() => _MeasureAppState();
}

class _MeasureAppState extends State<_MeasureApp> {
  final _macKey   = GlobalKey();
  final _winKey   = GlobalKey();
  final _linuxKey = GlobalKey();

  Map<String, int>? _results;

  @override
  void initState() {
    super.initState();
    SchedulerBinding.instance.addPostFrameCallback((_) => _measure());
  }

  void _measure() {
    final mac   = (_macKey.currentContext!.findRenderObject()! as RenderBox).size.width;
    final win   = (_winKey.currentContext!.findRenderObject()! as RenderBox).size.width;
    final linux = (_linuxKey.currentContext!.findRenderObject()! as RenderBox).size.width;
    final results = {
      'mac': mac.round(),
      'windows': win.round(),
      'linux': linux.round(),
    };
    stdout.writeln(jsonEncode(results));
    _writeCsdSizesJava(results);
    if (!ENABLE_INTERACTIVITY) {
      exit(0);
    } else {
      setState(() => _results = results);
    }
  }

  void _writeCsdSizesJava(Map<String, int> results) {
    final content = 'package dev.equo.swt.size;\n\n'
        'public class CsdSizes {\n'
        '    public static final int CSD_WIDTH_MAC     = ${results['mac']};\n'
        '    public static final int CSD_WIDTH_WINDOWS = ${results['windows']};\n'
        '    public static final int CSD_WIDTH_LINUX   = ${results['linux']};\n'
        '}\n';
    final file = File('../swt_native/src/main/java/dev/equo/swt/size/CsdSizes.java');
    file.parent.createSync(recursive: true);
    file.writeAsStringSync(content);
    print('Exported: ${file.path}');
  }

  @override
  Widget build(BuildContext context) {
    final noop = WindowControlActions(
      onMinimize: () {},
      onMaxRestore: (_) {},
      onClose: () {},
    );
    return MaterialApp(
      home: Scaffold(
        appBar: AppBar(title: const Text('measure_csd')),
        body: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Padding(
              padding: const EdgeInsets.all(16),
              child: Row(
                mainAxisSize: MainAxisSize.min,
                children: [
                  _labeled('mac',     WindowControls(key: _macKey,   osOverride: 'mac',     actions: noop)),
                  const SizedBox(width: 32),
                  _labeled('windows', WindowControls(key: _winKey,   osOverride: 'windows', actions: noop)),
                  const SizedBox(width: 32),
                  _labeled('linux',   WindowControls(key: _linuxKey, osOverride: 'linux',   actions: noop)),
                ],
              ),
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

  Widget _labeled(String label, Widget child) {
    return Column(
      mainAxisSize: MainAxisSize.min,
      children: [
        child,
        const SizedBox(height: 8),
        Text(label),
      ],
    );
  }
}
