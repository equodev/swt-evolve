import 'dart:typed_data';

import 'package:flutter_test/flutter_test.dart';
import 'package:swtflutter/src/comm/widget_wire.dart';
import 'package:swtflutter/src/gen/display.dart';
import 'package:swtflutter/src/gen/widgets.dart' show WireKey;
import 'package:swtflutter/src/impl/config_flags.dart';
import 'package:swtflutter/src/impl/widget_config.dart';

/// "Late ConfgFlags cause flicker".
///
/// The flags used to reach Flutter only on the `swt.evolve.properties` channel, which Java pushes on
/// the first ClientReady — and Flutter only sends ClientReady after its first frame. The Display
/// update carrying the shells, by contrast, is buffered before the client connects. So the content
/// was always painted at least one frame before the real flags landed: default theme first, then a
/// full MaterialApp rebuild. The flags now also ride in the Display value, parsed straight off it.
/// The flags [theme] and [name] name, as the fields of a flags object Java writes.
void _flagFields(WidgetWireWriter w, {String? theme, String? name}) {
  if (theme != null) {
    w.key(WireKey.k_ConfigFlags_force_theme);
    w.string(theme);
  }
  if (name != null) {
    w.key(WireKey.k_ConfigFlags_theme_name);
    w.string(name);
  }
}

/// A display record's properties, as Java writes them: no shells, and [config] when given.
VDisplay _display({void Function(WidgetWireWriter w)? config}) =>
    WidgetWireReader(Uint8List.fromList(WidgetWireWriter.encodeWith((w) {
      w.key(WireKey.k_Display_shells);
      w.refs([]);
      if (config != null) {
        w.key(WireKey.k_Display_config);
        w.object(config);
      }
      w.u8(0);
    }))).fields(VDisplay());

/// Flags read from the bytes the `swt.evolve.properties` channel carries.
ConfigFlags _flags(String theme) => ConfigFlags.read(Uint8List.fromList(WidgetWireWriter.encodeWith((w) {
      _flagFields(w, theme: theme);
      w.u8(0);
    })));

void main() {
  setUp(resetConfigFlags);
  tearDown(resetConfigFlags);

  test('a Display value parses its config, and the flags apply', () {
    final display = _display(config: (w) => _flagFields(w, theme: 'light', name: 'marketplace'));

    applyConfigFlags(display.config);

    expect(getConfigFlags().force_theme, 'light');
    expect(getConfigFlags().theme_name, 'marketplace');
    expect(configFlagsVersion.value, 1, reason: 'the app rebuilds with the real theme');
  });

  test('a Display value with no config leaves the flags alone', () {
    applyConfigFlags(_flags('light'));
    final display = _display();

    applyConfigFlags(display.config); // config is null — the fallback path

    expect(getConfigFlags().force_theme, 'light');
    expect(configFlagsVersion.value, 1);
  });

  test('unchanged flags do not rebuild the app', () {
    applyConfigFlags(_flags('light'));

    // The flags repeat in every Display update (a shell shown, a tooltip, a window drag). Without the
    // dedup each one would rebuild the whole MaterialApp with a fresh ThemeData.
    applyConfigFlags(_flags('light'));
    applyConfigFlags(_flags('light'));
    expect(configFlagsVersion.value, 1);

    applyConfigFlags(_flags('dark')); // a real change still gets through
    expect(configFlagsVersion.value, 2);
    expect(getConfigFlags().force_theme, 'dark');
  });
}
