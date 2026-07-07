package dev.equo.swt;

/**
 * Registers web font-name substitutions into {@link FontMetricsUtil} so that Java-side size
 * calculations use metrics CanvasKit can actually render for a name it can't: Liberation
 * metrics for named system fonts (Arial, Times New Roman, ...), and CanvasKit's own measured
 * fallback typeface — under the "System Web" key, see {@code all_fonts_web.dart} — for "System"
 * (SWT's "use the system default font"). {@code org.eclipse.swt.graphics.DartFontData#getName()}
 * substitutes the same way before a font name reaches Flutter, so this is the single source of
 * truth for the mapping — Dart carries no copy of its own.
 */
public final class WebFontSubstitutions {
    private WebFontSubstitutions() {}

    /**
     * No-op whose only purpose is to be a normal static-method call: invoking it is genuine
     * "active use" (JLS 12.4.1) and guarantees this class's static initializer — which populates
     * the substitution table below — has run. A plain {@code WebFontSubstitutions.class} or
     * {@code .getName()} reference does NOT count as active use and would silently skip that
     * initializer; call this method instead (see {@code WebFlutterServer}'s own static block).
     */
    public static void ensureRegistered() {}

    static {
        // Sans-serif → Liberation Sans
        reg("Arial",             "Liberation Sans");
        reg("Arial Black",       "Liberation Sans");
        reg("Arial Narrow",      "Liberation Sans");
        reg("Helvetica",         "Liberation Sans");
        reg("Helvetica Neue",    "Liberation Sans");
        reg("Verdana",           "Liberation Sans");
        reg("Tahoma",            "Liberation Sans");
        reg("Trebuchet MS",      "Liberation Sans");
        reg("Geneva",            "Liberation Sans");
        reg("Calibri",           "Liberation Sans");
        reg("Candara",           "Liberation Sans");
        reg("Segoe UI",          "Liberation Sans");
        reg("Ubuntu",            "Liberation Sans");
        reg("Cantarell",         "Liberation Sans");
        reg("DejaVu Sans",       "Liberation Sans");
        reg("Noto Sans",         "Liberation Sans");

        // The SWT "use the system default font" placeholder → CanvasKit's own measured fallback
        // typeface, not Liberation Sans (see all_fonts_web.dart/gen_fonts_web.dart).
        reg("System",            "System Web");

        // Serif → Liberation Serif
        reg("Times New Roman",   "Liberation Serif");
        reg("Times",             "Liberation Serif");
        reg("Georgia",           "Liberation Serif");
        reg("Palatino",          "Liberation Serif");
        reg("Palatino Linotype", "Liberation Serif");
        reg("Book Antiqua",      "Liberation Serif");
        reg("Garamond",          "Liberation Serif");
        reg("Baskerville",       "Liberation Serif");
        reg("Cambria",           "Liberation Serif");
        reg("Constantia",        "Liberation Serif");
        reg("DejaVu Serif",      "Liberation Serif");
        reg("FreeSerif",         "Liberation Serif");
        reg("Noto Serif",        "Liberation Serif");

        // Monospace → Liberation Mono
        reg("Courier New",           "Liberation Mono");
        reg("Courier",               "Liberation Mono");
        reg("Consolas",              "Liberation Mono");
        reg("Monaco",                "Liberation Mono");
        reg("Menlo",                 "Liberation Mono");
        reg("Lucida Console",        "Liberation Mono");
        reg("DejaVu Sans Mono",      "Liberation Mono");
        reg("FreeMono",              "Liberation Mono");
        reg("Ubuntu Mono",           "Liberation Mono");
        reg("Roboto Mono",           "Liberation Mono");
        reg("Source Code Pro",       "Liberation Mono");
        reg("JetBrains Mono",        "Liberation Mono");
        reg("Fira Mono",             "Liberation Mono");
        reg("Fira Code",             "Liberation Mono");
        reg("Andale Mono",           "Liberation Mono");
        reg("Droid Sans Mono",       "Liberation Mono");
        reg("Noto Mono",             "Liberation Mono");
        reg("American Typewriter",   "Liberation Mono");
        reg("PT Mono",               "Liberation Mono");

        // Additional serif fonts → Liberation Serif
        reg("Bodoni 72",             "Liberation Serif");
        reg("Bodoni 72 Oldstyle",    "Liberation Serif");
        reg("Bodoni 72 Smallcaps",   "Liberation Serif");
        reg("Cochin",                "Liberation Serif");
        reg("Hiragino Mincho ProN",  "Liberation Serif");
        reg("Hoefler Text",          "Liberation Serif");
        reg("PT Serif Caption",      "Liberation Serif");
        reg("Rockwell",              "Liberation Serif");
        reg("Songti SC",             "Liberation Serif");
        reg("Songti TC",             "Liberation Serif");

        // macOS / system fonts without web equivalents → Liberation Sans
        reg("Al Bayan",                  "Liberation Sans");
        reg("Apple Chancery",            "Liberation Sans");
        reg("Apple Symbols",             "Liberation Sans");
        reg("Avenir",                    "Liberation Sans");
        reg("Avenir Next",               "Liberation Sans");
        reg("Avenir Next Condensed",     "Liberation Sans");
        reg("Ayuthaya",                  "Liberation Sans");
        reg("Bangla MN",                 "Liberation Sans");
        reg("Bodoni Ornaments",          "Liberation Sans");
        reg("Brush Script MT",           "Liberation Sans");
        reg("Chalkduster",               "Liberation Sans");
        reg("Comic Sans MS",             "Liberation Sans");
        reg("Copperplate",               "Liberation Sans");
        reg("DIN Condensed",             "Liberation Sans");
        reg("DecoType Naskh",            "Liberation Sans");
        reg("Devanagari MT",             "Liberation Sans");
        reg("Devanagari Sangam MN",      "Liberation Sans");
        reg("Diwan Kufi",                "Liberation Sans");
        reg("Diwan Thuluth",             "Liberation Sans");
        reg("Farisi",                    "Liberation Sans");
        reg("Futura",                    "Liberation Sans");
        reg("Galvji",                    "Liberation Sans");
        reg("Gill Sans",                 "Liberation Sans");
        reg("Grantha Sangam MN",         "Liberation Sans");
        reg("Gujarati MT",               "Liberation Sans");
        reg("Gujarati Sangam MN",        "Liberation Sans");
        reg("Gurmukhi MN",               "Liberation Sans");
        reg("Heiti TC",                  "Liberation Sans");
        reg("Herculanum",                "Liberation Sans");
        reg("Hiragino Maru Gothic ProN", "Liberation Sans");
        reg("Hiragino Sans",             "Liberation Sans");
        reg("Hiragino Sans GB",          "Liberation Sans");
        reg("ITF Devanagari",            "Liberation Sans");
        reg("ITF Devanagari Marathi",    "Liberation Sans");
        reg("InaiMathi",                 "Liberation Sans");
        reg("Kannada MN",                "Liberation Sans");
        reg("Kannada Sangam MN",         "Liberation Sans");
        reg("Khmer MN",                  "Liberation Sans");
        reg("Khmer Sangam MN",           "Liberation Sans");
        reg("Kohinoor Bangla",           "Liberation Sans");
        reg("Kohinoor Devanagari",       "Liberation Sans");
        reg("Kohinoor Gujarati",         "Liberation Sans");
        reg("Kohinoor Telugu",           "Liberation Sans");
        reg("Krungthep",                 "Liberation Sans");
        reg("KufiStandardGK",            "Liberation Sans");
        reg("Lao MN",                    "Liberation Sans");
        reg("Lucida Grande",             "Liberation Sans");
        reg("Malayalam MN",              "Liberation Sans");
        reg("Mishafi",                   "Liberation Sans");
        reg("Mishafi Gold",              "Liberation Sans");
        reg("Mukta Mahee",               "Liberation Sans");
        reg("Nadeem",                    "Liberation Sans");
        reg("Noteworthy",                "Liberation Sans");
        reg("Noto Nastaliq Urdu",        "Liberation Sans");
        reg("Noto Sans Oriya",           "Liberation Sans");
        reg("Oriya Sangam MN",           "Liberation Sans");
        reg("PT Sans Caption",           "Liberation Sans");
        reg("PT Sans Narrow",            "Liberation Sans");
        reg("Papyrus",                   "Liberation Sans");
        reg("Phosphate",                 "Liberation Sans");
        reg("PingFang HK",               "Liberation Sans");
        reg("PingFang SC",               "Liberation Sans");
        reg("PingFang TC",               "Liberation Sans");
        reg("Shree Devanagari 714",      "Liberation Sans");
        reg("SignPainter",               "Liberation Sans");
        reg("Silom",                     "Liberation Sans");
        reg("Sinhala Sangam MN",         "Liberation Sans");
        reg("Snell Roundhand",           "Liberation Sans");
        reg("Sukhumvit Set",             "Liberation Sans");
        reg("Telugu Sangam MN",          "Liberation Sans");
        reg("Thonburi",                  "Liberation Sans");
        reg("Trattatello",               "Liberation Sans");
        reg("Waseem",                    "Liberation Sans");
        reg("Webdings",                  "Liberation Sans");
        reg("Wingdings",                 "Liberation Sans");
        reg("Wingdings 2",               "Liberation Sans");
        reg("Wingdings 3",               "Liberation Sans");
    }

    private static void reg(String from, String to) {
        FontMetricsUtil.registerFontSubstitution(from, to);
    }
}
