// Fixed Liberation + Inter font list used for web metrics generation (see gen_fonts.dart and
// gen_fonts_web.dart) — a plain importable library, not a `part of` file, since both entrypoints
// need it (a `part` file can only belong to a single library).
import 'package:flutter/widgets.dart';

void addFontsWebMain(Map<String, List<(String, FontStyle, FontWeight)>> fonts) {
  fonts["webMain"] = [
    // Liberation Sans — bundled sans-serif, metric-compatible with Arial/Helvetica
    ('Liberation Sans', FontStyle.normal, FontWeight.normal),
    ('Liberation Sans', FontStyle.italic, FontWeight.normal),
    ('Liberation Sans', FontStyle.normal, FontWeight.bold),
    ('Liberation Sans', FontStyle.italic, FontWeight.bold),
    // Liberation Serif — bundled serif, metric-compatible with Times New Roman
    ('Liberation Serif', FontStyle.normal, FontWeight.normal),
    ('Liberation Serif', FontStyle.italic, FontWeight.normal),
    ('Liberation Serif', FontStyle.normal, FontWeight.bold),
    ('Liberation Serif', FontStyle.italic, FontWeight.bold),
    // Liberation Mono — bundled monospace, metric-compatible with Courier New
    ('Liberation Mono', FontStyle.normal, FontWeight.normal),
    ('Liberation Mono', FontStyle.italic, FontWeight.normal),
    ('Liberation Mono', FontStyle.normal, FontWeight.bold),
    ('Liberation Mono', FontStyle.italic, FontWeight.bold),
    // Inter — already bundled with extra weights
    ('Inter', FontStyle.normal, FontWeight.normal),
    ('Inter', FontStyle.italic, FontWeight.normal),
    ('Inter', FontStyle.normal, FontWeight.bold),
    ('Inter', FontStyle.italic, FontWeight.bold),
    ('Inter', FontStyle.normal, FontWeight.w300),
    ('Inter', FontStyle.italic, FontWeight.w300),
    ('Inter', FontStyle.normal, FontWeight.w500),
    ('Inter', FontStyle.italic, FontWeight.w500),
    ('Inter', FontStyle.normal, FontWeight.w600),
    ('Inter', FontStyle.italic, FontWeight.w600),
    // 'System' (SWT's "use the system default font", an unregistered family name) — measures
    // CanvasKit's own fallback typeface, not a bundled font. gen_fonts_web.dart renames these
    // entries to "System Web-*" before merging, since they must not overwrite the desktop-measured
    // "System-*" entries (a different renderer, a different fallback typeface).
    ('System', FontStyle.normal, FontWeight.normal),
    ('System', FontStyle.italic, FontWeight.normal),
    ('System', FontStyle.normal, FontWeight.bold),
    ('System', FontStyle.italic, FontWeight.bold),
  ];
}
