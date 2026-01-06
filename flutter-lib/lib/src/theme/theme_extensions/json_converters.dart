import 'package:flutter/material.dart';
import 'package:json_annotation/json_annotation.dart';

class ColorConverter implements JsonConverter<Color, String> {
  const ColorConverter();

  @override
  Color fromJson(String json) {
    final hexString = json.startsWith('#') ? json.substring(1) : json;
    if (hexString.length == 6) {
      return Color(int.parse('FF$hexString', radix: 16));
    } else if (hexString.length == 8) {
      return Color(int.parse(hexString, radix: 16));
    }
    throw ArgumentError('Invalid hex color format: $json');
  }

  @override
  String toJson(Color color) {
    final value = color.value;
    final hex = value.toRadixString(16).padLeft(8, '0').toUpperCase();
    return '#$hex';
  }
}



class DurationConverter implements JsonConverter<Duration?, int?> {
  const DurationConverter();

  @override
  Duration? fromJson(int? json) => json != null ? Duration(milliseconds: json) : null;

  @override
  int? toJson(Duration? duration) => duration?.inMilliseconds;
}

class EdgeInsetsConverter implements JsonConverter<EdgeInsets, Map<String, dynamic>> {
  const EdgeInsetsConverter();

  @override
  EdgeInsets fromJson(Map<String, dynamic> json) {
    return EdgeInsets.fromLTRB(
      (json['left'] as num).toDouble(),
      (json['top'] as num).toDouble(),
      (json['right'] as num).toDouble(),
      (json['bottom'] as num).toDouble(),
    );
  }

  @override
  Map<String, dynamic> toJson(EdgeInsets edgeInsets) {
    return {
      'left': edgeInsets.left,
      'top': edgeInsets.top,
      'right': edgeInsets.right,
      'bottom': edgeInsets.bottom,
    };
  }
}

class OffsetConverter implements JsonConverter<Offset, Map<String, dynamic>> {
  const OffsetConverter();

  @override
  Offset fromJson(Map<String, dynamic> json) {
    return Offset(
      (json['dx'] as num).toDouble(),
      (json['dy'] as num).toDouble(),
    );
  }

  @override
  Map<String, dynamic> toJson(Offset offset) {
    return {
      'dx': offset.dx,
      'dy': offset.dy,
    };
  }
}

class FontWeightConverter implements JsonConverter<FontWeight, int> {
  const FontWeightConverter();

  @override
  FontWeight fromJson(int json) {
    return FontWeight.values.firstWhere(
      (fw) => fw.index == json,
      orElse: () => FontWeight.normal,
    );
  }

  @override
  int toJson(FontWeight fontWeight) => fontWeight.index;
}

class TextDecorationConverter implements JsonConverter<TextDecoration, String> {
  const TextDecorationConverter();

  @override
  TextDecoration fromJson(String json) {
    switch (json) {
      case 'none':
        return TextDecoration.none;
      case 'underline':
        return TextDecoration.underline;
      case 'overline':
        return TextDecoration.overline;
      case 'lineThrough':
        return TextDecoration.lineThrough;
      case 'underline,lineThrough':
        return TextDecoration.combine([
          TextDecoration.underline,
          TextDecoration.lineThrough,
        ]);
      default:
        return TextDecoration.none;
    }
  }

  @override
  String toJson(TextDecoration decoration) {
    if (decoration == TextDecoration.none) {
      return 'none';
    } else if (decoration == TextDecoration.underline) {
      return 'underline';
    } else if (decoration == TextDecoration.overline) {
      return 'overline';
    } else if (decoration == TextDecoration.lineThrough) {
      return 'lineThrough';
    } else {
      // For combined decorations, return a comma-separated string
      final decorations = <String>[];
      if (decoration.contains(TextDecoration.underline)) {
        decorations.add('underline');
      }
      if (decoration.contains(TextDecoration.overline)) {
        decorations.add('overline');
      }
      if (decoration.contains(TextDecoration.lineThrough)) {
        decorations.add('lineThrough');
      }
      return decorations.join(',');
    }
  }
}

class TextAlignConverter implements JsonConverter<TextAlign, int> {
  const TextAlignConverter();

  @override
  TextAlign fromJson(int json) {
    return TextAlign.values.firstWhere(
      (ta) => ta.index == json,
      orElse: () => TextAlign.start,
    );
  }

  @override
  int toJson(TextAlign textAlign) => textAlign.index;
}

class MainAxisAlignmentConverter implements JsonConverter<MainAxisAlignment, int> {
  const MainAxisAlignmentConverter();

  @override
  MainAxisAlignment fromJson(int json) {
    return MainAxisAlignment.values.firstWhere(
      (ma) => ma.index == json,
      orElse: () => MainAxisAlignment.start,
    );
  }

  @override
  int toJson(MainAxisAlignment alignment) => alignment.index;
}

class CrossAxisAlignmentConverter implements JsonConverter<CrossAxisAlignment, int> {
  const CrossAxisAlignmentConverter();

  @override
  CrossAxisAlignment fromJson(int json) {
    return CrossAxisAlignment.values.firstWhere(
      (ca) => ca.index == json,
      orElse: () => CrossAxisAlignment.center,
    );
  }

  @override
  int toJson(CrossAxisAlignment alignment) => alignment.index;
}

class TextStyleConverter implements JsonConverter<TextStyle?, Map<String, dynamic>?> {
  const TextStyleConverter();

  @override
  TextStyle? fromJson(Map<String, dynamic>? json) {
    if (json == null) return null;
    
    return TextStyle(
      fontSize: json['fontSize'] as double?,
      fontWeight: json['fontWeight'] != null
          ? FontWeight.values.firstWhere(
              (fw) => fw.index == json['fontWeight'] as int,
              orElse: () => FontWeight.normal,
            )
          : null,
      fontFamily: json['fontFamily'] as String?,
      letterSpacing: json['letterSpacing'] as double?,
      height: json['height'] as double?,
      color: json['color'] != null
          ? (json['color'] is String
              ? _colorFromString(json['color'] as String)
              : Color(json['color'] as int))
          : null,
    );
  }

  @override
  Map<String, dynamic>? toJson(TextStyle? textStyle) {
    if (textStyle == null) return null;
    
    return {
      if (textStyle.fontSize != null) 'fontSize': textStyle.fontSize,
      if (textStyle.fontWeight != null) 'fontWeight': textStyle.fontWeight!.index,
      if (textStyle.fontFamily != null) 'fontFamily': textStyle.fontFamily,
      if (textStyle.letterSpacing != null) 'letterSpacing': textStyle.letterSpacing,
      if (textStyle.height != null) 'height': textStyle.height,
      if (textStyle.color != null) 'color': '#${textStyle.color!.value.toRadixString(16).padLeft(8, '0').toUpperCase()}',
    };
  }

  Color _colorFromString(String hexString) {
    final hex = hexString.startsWith('#') ? hexString.substring(1) : hexString;
    if (hex.length == 6) {
      return Color(int.parse('FF$hex', radix: 16));
    } else if (hex.length == 8) {
      return Color(int.parse(hex, radix: 16));
    }
    throw ArgumentError('Invalid hex color format: $hexString');
  }
}

class CurveConverter implements JsonConverter<Curve, String> {
  const CurveConverter();

  @override
  Curve fromJson(String json) {
    switch (json) {
      case 'linear':
        return Curves.linear;
      case 'ease':
        return Curves.ease;
      case 'easeIn':
        return Curves.easeIn;
      case 'easeOut':
        return Curves.easeOut;
      case 'easeInOut':
        return Curves.easeInOut;
      case 'fastOutSlowIn':
        return Curves.fastOutSlowIn;
      case 'bounceIn':
        return Curves.bounceIn;
      case 'bounceOut':
        return Curves.bounceOut;
      case 'bounceInOut':
        return Curves.bounceInOut;
      case 'elasticIn':
        return Curves.elasticIn;
      case 'elasticOut':
        return Curves.elasticOut;
      case 'elasticInOut':
        return Curves.elasticInOut;
      default:
        return Curves.easeInOut;
    }
  }

  @override
  String toJson(Curve curve) {
    if (curve == Curves.linear) return 'linear';
    if (curve == Curves.ease) return 'ease';
    if (curve == Curves.easeIn) return 'easeIn';
    if (curve == Curves.easeOut) return 'easeOut';
    if (curve == Curves.easeInOut) return 'easeInOut';
    if (curve == Curves.fastOutSlowIn) return 'fastOutSlowIn';
    if (curve == Curves.bounceIn) return 'bounceIn';
    if (curve == Curves.bounceOut) return 'bounceOut';
    if (curve == Curves.bounceInOut) return 'bounceInOut';
    if (curve == Curves.elasticIn) return 'elasticIn';
    if (curve == Curves.elasticOut) return 'elasticOut';
    if (curve == Curves.elasticInOut) return 'elasticInOut';
    return 'easeInOut'; // Default
  }
}
