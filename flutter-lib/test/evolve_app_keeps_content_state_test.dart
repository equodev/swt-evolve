import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/impl/config_flags.dart';
import 'package:swtflutter/src/impl/widget_config.dart';

/// The Display is created once per window: nothing seeds a recreated one with the tree Java
/// already sent, so a Display remounted by a config change would render empty. Every flag that
/// can arrive after the first frame must therefore leave the window's content in place.
class _Probe extends StatefulWidget {
  final List<String> log;
  const _Probe(this.log);

  @override
  State<_Probe> createState() => _ProbeState();
}

class _ProbeState extends State<_Probe> {
  @override
  void initState() {
    super.initState();
    widget.log.add('init');
  }

  @override
  void dispose() {
    widget.log.add('dispose');
    super.dispose();
  }

  @override
  Widget build(BuildContext context) => const SizedBox.expand();
}

void main() {
  setUp(resetConfigFlags);
  tearDown(() {
    resetConfigFlags();
    appScaleNotifier.value = 1.0;
  });

  testWidgets('the window content survives the flags that arrive after the first frame',
      (tester) async {
    final log = <String>[];
    await tester.pumpWidget(EvolveApp(
      theme: ThemeMode.light,
      isMainWindow: true,
      contentWidget: _Probe(log),
    ));
    expect(log, ['init']);

    applyConfigFlags(ConfigFlags()..csd_placement = 'overlay');
    await tester.pump();
    applyConfigFlags(ConfigFlags()
      ..csd_placement = 'overlay'
      ..force_theme = 'dark'
      ..theme_name = 'equo');
    await tester.pump();
    appScaleNotifier.value = 1.5;
    await tester.pump();

    expect(log, ['init'],
        reason: 'a remounted Display starts empty and Java does not send its tree again');
  });
}
