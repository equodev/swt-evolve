import 'package:flutter_test/flutter_test.dart';
import 'package:swtflutter/src/gen/display.dart';
import 'package:swtflutter/src/impl/config_flags.dart';
import 'package:swtflutter/src/impl/widget_config.dart';

/// #821 — "Late ConfgFlags cause flicker".
///
/// The flags used to reach Flutter only on the `swt.evolve.properties` channel, which Java pushes on
/// the first ClientReady — and Flutter only sends ClientReady after its first frame. The Display
/// update carrying the shells, by contrast, is buffered before the client connects. So the content
/// was always painted at least one frame before the real flags landed: default theme first, then a
/// full MaterialApp rebuild. The flags now also ride in the Display value, parsed straight off it.
void main() {
  setUp(resetConfigFlags);
  tearDown(resetConfigFlags);

  test('a Display value parses its config, and the flags apply', () {
    final display = VDisplay.fromJson({
      'id': 1,
      'swt': 'Display',
      'shells': <dynamic>[],
      'config': {'force_theme': 'light', 'theme_name': 'marketplace'},
    });

    applyConfigFlags(display.config);

    expect(getConfigFlags().force_theme, 'light');
    expect(getConfigFlags().theme_name, 'marketplace');
    expect(configFlagsVersion.value, 1, reason: 'the app rebuilds with the real theme');
  });

  test('a Display value with no config leaves the flags alone', () {
    applyConfigFlags(ConfigFlags()..force_theme = 'light');
    final display = VDisplay.fromJson({'id': 1, 'swt': 'Display', 'shells': <dynamic>[]});

    applyConfigFlags(display.config); // config is null — the fallback path

    expect(getConfigFlags().force_theme, 'light');
    expect(configFlagsVersion.value, 1);
  });

  test('unchanged flags do not rebuild the app', () {
    applyConfigFlags(ConfigFlags()..force_theme = 'light');

    // The flags repeat in every Display update (a shell shown, a tooltip, a window drag). Without the
    // dedup each one would rebuild the whole MaterialApp with a fresh ThemeData.
    applyConfigFlags(ConfigFlags()..force_theme = 'light');
    applyConfigFlags(ConfigFlags()..force_theme = 'light');
    expect(configFlagsVersion.value, 1);

    applyConfigFlags(ConfigFlags()..force_theme = 'dark'); // a real change still gets through
    expect(configFlagsVersion.value, 2);
    expect(getConfigFlags().force_theme, 'dark');
  });
}
