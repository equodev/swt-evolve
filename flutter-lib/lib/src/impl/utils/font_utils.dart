import 'package:flutter/widgets.dart';
import '../../gen/font.dart';
import '../../gen/fontdata.dart';
import '../../gen/color.dart';

/// Utility class to convert SWT Font/FontData to Flutter TextStyle
class FontUtils {
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
  static TextStyle textStyleFromVFont(
    VFont? vFont,
    BuildContext context, {
    Color? color,
  }) {
    final defaultStyle = DefaultTextStyle.of(context).style;

    if (vFont == null || vFont.fontData == null || vFont.fontData!.isEmpty) {
      return defaultStyle.copyWith(color: color, fontSize: 12);
    }

    final fontData = vFont.fontData!.first;
    return _createTextStyleFromFontData(fontData, color: color);
  }

  /// Create a TextStyle from VFontData and optional color
  static TextStyle _createTextStyleFromFontData(
    VFontData fontData, {
    Color? color,
  }) {
    const defaultFontSize = 12.0;
    const defaultFontName = 'System';

    final fontName = fontData.name?.isNotEmpty == true
        ? fontData.name!
        : defaultFontName;
    final fontSize = fontData.height?.toDouble() ?? defaultFontSize;
    final swtStyle = fontData.style ?? 0;

    final (fontWeight, fontStyle) = convertSwtFontStyle(swtStyle);
    //print("Using font $fontName $fontSize ${fontWeight.value} ${fontStyle}");
    return TextStyle(
      fontFamily: fontName,
      fontSize: fontSize,
      fontWeight: fontWeight,
      fontStyle: fontStyle,
      color: color,
    );
  }

  /// Get font properties as a map for easy access
  static Map<String, dynamic> getFontProperties(VFont? vFont) {
    if (vFont == null) {
      return {'name': null, 'height': null, 'style': null, 'locale': null};
    }

    final fontDataList = vFont.fontData;
    if (fontDataList == null || fontDataList.isEmpty) {
      return {'name': null, 'height': null, 'style': null, 'locale': null};
    }

    final fontData = fontDataList.first;
    return {
      'name': fontData.name,
      'height': fontData.height,
      'style': fontData.style,
      'locale': fontData.locale,
    };
  }

  /// Print font data for debugging
  static void printFontData(VFont? vFont, {String? context}) {
    final prefix = context != null ? '[$context] ' : '';
    final props = getFontProperties(vFont);

    print('${prefix}=== FONT DATA ===');
    print('${prefix}Font available: ${vFont != null}');
    print('${prefix}Font name: ${props['name']}');
    print('${prefix}Font height: ${props['height']}');
    print('${prefix}Font style: ${props['style']}');
    print('${prefix}Font locale: ${props['locale']}');
    print('${prefix}==================');
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
