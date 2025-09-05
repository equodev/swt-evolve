import 'dart:convert';
import 'dart:typed_data';
import 'package:flutter/material.dart';
import '../../gen/image.dart';
import '../icons_map.dart';
import '../widget_config.dart';

class ImageUtils {
  static final Map<String, Widget> _iconCache = {};
  static final Map<String, Widget> _imageCache = {};

  /// Creates an icon widget from filename with consistent styling
  static Widget? buildIconWidget(
    String filename, {
    double? size,
    Color? color,
    bool enabled = true,
  }) {
    final cacheKey =
        '$filename-${size ?? 'default'}-${color?.value ?? 'default'}-$enabled';

    if (_iconCache.containsKey(cacheKey)) {
      return _iconCache[cacheKey];
    }

    final iconData = getIconByName(filename);
    if (iconData == null) return null;

    // FontAwesome icons detection and sizing
    final isFontAwesome = (iconData.fontPackage == 'font_awesome_flutter') ||
        (iconData.fontFamily ?? '').toLowerCase().contains('fontawesome');

    final iconSize =
        size ?? (isFontAwesome ? AppSizes.fontAwesomeIcon : AppSizes.icon);
    final iconColor = color ?? AppColors.getColor(enabled);

    Widget iconWidget = Icon(
      iconData,
      size: iconSize,
      color: iconColor,
    );

    // Add padding for FontAwesome icons
    if (isFontAwesome) {
      iconWidget = Padding(
        padding: const EdgeInsets.symmetric(horizontal: 4),
        child: iconWidget,
      );
    }

    _iconCache[cacheKey] = iconWidget;
    return iconWidget;
  }

  /// Creates an image widget from binary data
  static Widget? buildImageWidget(
    Uint8List bytes, {
    double? size,
    Color? color,
    bool enabled = true,
    BoxConstraints? constraints,
  }) {
    final cacheKey =
        '${bytes.length}-${size ?? 'default'}-${color?.value ?? 'default'}-$enabled';

    if (_imageCache.containsKey(cacheKey)) {
      return _imageCache[cacheKey];
    }

    try {
      final imageSize = size ?? AppSizes.icon;
      final imageColor = color ?? AppColors.getColor(enabled);

      final imageWidget = ConstrainedBox(
        constraints: constraints ??
            BoxConstraints(
              minWidth: AppSizes.toolbarMinSize,
              minHeight: AppSizes.toolbarMinSize,
            ),
        child: Center(
          child: SizedBox(
            width: imageSize,
            height: imageSize,
            child: ImageIcon(
              MemoryImage(bytes),
              size: imageSize,
              color: imageColor,
            ),
          ),
        ),
      );

      _imageCache[cacheKey] = imageWidget;
      return imageWidget;
    } catch (e) {
      print('Error decoding image bytes: $e');
      return Icon(Icons.broken_image, size: size ?? AppSizes.icon);
    }
  }

  /// Main method to build widget from VImage with caching
  static Widget? buildVImageWidget(
    VImage? image, {
    double? size,
    Color? color,
    bool enabled = true,
    BoxConstraints? constraints,
    bool useBinaryImage = true,
  }) {
    if (image == null) return null;

    // Try icon filename first
    if (image.filename != null && image.filename!.isNotEmpty) {
      final iconWidget = buildIconWidget(
        image.filename!,
        size: size,
        color: color,
        enabled: enabled,
      );
      if (iconWidget != null) return iconWidget;
    }

    // Try binary image data
    if (useBinaryImage && image.imageData?.data != null) {
      final bytes = Uint8List.fromList(image.imageData!.data!);
      return buildImageWidget(
        bytes,
        size: size,
        color: color,
        enabled: enabled,
        constraints: constraints,
      );
    }

    return null;
  }

  /// Clear caches to free memory
  static void clearCache() {
    _iconCache.clear();
    _imageCache.clear();
  }

  /// Get cache stats for debugging
  static Map<String, int> getCacheStats() {
    return {
      'iconCache': _iconCache.length,
      'imageCache': _imageCache.length,
    };
  }

  static List<int>? parseByteArray(dynamic value) {
    if (value == null) return null;

    if (value is String) {
      try {
        final decoded = base64Decode(value);
        return decoded.toList();
      } catch (e) {
        return null;
      }
    } else if (value is List) {
      return value.map((e) => (e as num).toInt()).toList();
    }
    return null;
  }

  static String? serializeByteArray(List<int>? value) {
    if (value == null) return null;

    final bytes = Uint8List.fromList(value);
    return base64Encode(bytes);
  }
}
