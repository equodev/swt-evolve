import 'dart:async';

import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/impl/config_flags.dart';
import 'package:swtflutter/src/impl/widget_config.dart';

/// The `Theme config ->` line is what a support diagnosis reads to learn which theme a product is
/// actually configured with. The flags arrive over the socket after the first build, so a line
/// emitted only on that first build always reports them as null and answers nothing.
void main() {
  setUp(resetConfigFlags);
  tearDown(resetConfigFlags);

  testWidgets('the theme config line reports the flags that arrive, not the pre-socket nulls',
      (tester) async {
    final lines = <String>[];
    await runZoned(
      () async {
        await tester.pumpWidget(const EvolveApp(
          contentWidget: SizedBox.shrink(),
          theme: ThemeMode.light,
        ));

        // What Java pushes once the client is ready.
        applyConfigFlags(ConfigFlags()..force_theme = 'dark');
        await tester.pump();
      },
      zoneSpecification: ZoneSpecification(
        print: (_, __, ___, line) {
          if (line.startsWith('Theme config ->')) lines.add(line);
        },
      ),
    );

    expect(lines.first, contains('force_theme=null'),
        reason: 'the first build runs before the flags arrive');
    expect(lines.last, contains('force_theme=dark'),
        reason: 'the configured theme has to reach the log, or the line is useless for diagnosis');
    expect(lines.last, contains('effective_theme_mode=dark'));
  });
}
