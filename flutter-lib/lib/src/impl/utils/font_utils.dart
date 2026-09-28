import 'package:flutter/foundation.dart' show TargetPlatform, defaultTargetPlatform;
import 'package:flutter/widgets.dart';
import '../../gen/font.dart';
import '../../gen/fontdata.dart';
import '../../gen/color.dart';
import '../widget_config.dart';

/// Utility class to convert SWT Font/FontData to Flutter TextStyle
class FontUtils {
  /// A typographic point is 1/72", the unit an SWT FontData height is given in.
  static const double _pointsPerInch = 72;

  /// The DPI Win32 and GTK render a point-sized font at. macOS lays out in 72-dpi points instead,
  /// so a point is a logical pixel there and the scale is 1.
  static const double _win32GtkDpi = 96;

  /// Logical pixels per SWT font point, as the Java side reports for the host it runs on. Text
  /// measured there and painted here has to use the one factor. This client's own platform is the
  /// browser's, which says nothing about the host, so it only answers until the first config
  /// arrives.
  ///
  /// Divided by the zoom for the same reason `SwtZoomScale` shrinks every other piece of text:
  /// `swt.autoScale` grows boxes and leaves glyphs alone, and the transform above this text would
  /// otherwise grow it too. The factor is read live rather than taken off the flag — the flag is
  /// pushed before the client has reported its monitor, so it cannot carry the ratio.
  static double get pointScale => _hostPointScale / uiScale;

  static double get _hostPointScale {
    final fromHost = getConfigFlags().font_point_scale;
    if (fromHost != null && fromHost > 0) return fromHost;
    return defaultTargetPlatform == TargetPlatform.macOS
        ? 1.0
        : _win32GtkDpi / _pointsPerInch;
  }

  /// The factor the whole tree is magnified by. Painted text divides by it by hand: a scene drawn
  /// through a TextPainter is out of reach of the `MediaQuery.textScaler` that shrinks every Text
  /// widget under the same transform.
  static double get uiScale {
    final scale = appScaleNotifier.value;
    return scale > 0 ? scale : 1.0;
  }

  /// Convert SWT font style to Flutter FontWeight and FontStyle
  static (FontWeight, FontStyle) convertSwtFontStyle(int swtFontStyle) {
    FontWeight weight = FontWeight.normal;
    FontStyle style = FontStyle.normal;

    switch (swtFontStyle) {
      case 1: // SWT.BOLD
        weight = FontWeight.bold;
        break;
      case 2: // SWT.ITALIC
        style = FontStyle.italic;
        break;
      case 3: // SWT.BOLD | SWT.ITALIC
        weight = FontWeight.bold;
        style = FontStyle.italic;
        break;
    }

    return (weight, style);
  }

  /// Create a TextStyle from VFont and optional color.
  /// If no font is provided, returns the system default font with size 12.
  /// Set [applyDpiScaling] to true for StyledText widgets that need DPI conversion.
  static TextStyle textStyleFromVFont(
    VFont? vFont,
    BuildContext? context, {
    Color? color,
    bool applyDpiScaling = false,
    bool inherit = false,
  }) {
    if (vFont == null || vFont.fontData == null || vFont.fontData!.isEmpty) {
      final defaultStyle = context != null
          ? DefaultTextStyle.of(context).style
          : const TextStyle(fontSize: 12);
      // Painted text has no font of its own here, but it is still painted: the fallback size is in
      // logical pixels and has to come down by the zoom like every other glyph.
      return defaultStyle.copyWith(
          color: color, fontSize: applyDpiScaling ? 12 / uiScale : 12);
    }

    final fontData = vFont.fontData!.first;
    return _createTextStyleFromFontData(
      fontData,
      color: color,
      applyDpiScaling: applyDpiScaling,
      inherit: inherit,
    );
  }

  /// Create a TextStyle from VFontData and optional color.
  /// Set [applyDpiScaling] to true for StyledText widgets that need DPI conversion.
  static TextStyle _createTextStyleFromFontData(
    VFontData fontData, {
    Color? color,
    bool applyDpiScaling = false,
    bool inherit = false,
  }) {
    const defaultFontSize = 12.0;
    const defaultFontName = 'System';

    final fontName = fontData.name?.isNotEmpty == true
        ? fontData.name!
        : defaultFontName;
    final fontHeightPoints = fontData.height?.toDouble() ?? defaultFontSize;
    final fontSize =
        applyDpiScaling ? fontHeightPoints * pointScale : fontHeightPoints;
    final swtStyle = fontData.style;

    final (fontWeight, fontStyle) = convertSwtFontStyle(swtStyle);
    return TextStyle(
      inherit: inherit,
      fontFamily: fontName,
      fontSize: fontSize,
      fontWeight: fontWeight,
      fontStyle: fontStyle,
      color: color,
      textBaseline: inherit ? null : TextBaseline.alphabetic,
    );
  }

  /// Print color data for debugging
  static void printColorData(
    VColor? vColor, {
    String? context,
    String? colorName,
  }) {
    final prefix = context != null ? '[$context] ' : '';
    final name = colorName != null ? '$colorName ' : '';

    print('${prefix}=== ${name}COLOR DATA ===');
    print('${prefix}Color available: ${vColor != null}');
    if (vColor != null) {
      print(
        '${prefix}ARGB: (${vColor.alpha}, ${vColor.red}, ${vColor.green}, ${vColor.blue})',
      );
    }
    print('${prefix}==================');
  }
}
