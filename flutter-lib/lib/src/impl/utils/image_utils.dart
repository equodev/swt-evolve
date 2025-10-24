import 'dart:convert';
import 'dart:typed_data';
import 'dart:ui' as ui;
import 'package:flutter/material.dart';
import '../../gen/image.dart';
import '../icons_map.dart';
import '../widget_config.dart';

class ImageUtils {
  static final Map<String, Widget> _iconCache = {};
  static final Map<String, Widget> _imageCache = {};

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

    final isFontAwesome = (iconData.fontPackage == 'font_awesome_flutter') ||
        (iconData.fontFamily ?? '').toLowerCase().contains('fontawesome');

    final iconSize =
        size ?? (isFontAwesome ? AppSizes.fontAwesomeIcon : AppSizes.icon);
    final iconColor = color ?? AppColors.getColor(enabled);

    Widget iconWidget = Icon(iconData, size: iconSize, color: iconColor);

    if (isFontAwesome) {
      iconWidget = Padding(
        padding: const EdgeInsets.symmetric(horizontal: 4),
        child: iconWidget,
      );
    }

    _iconCache[cacheKey] = iconWidget;
    return iconWidget;
  }

  static Widget? _buildBinaryImage(
    Uint8List bytes, {
    double? size,
    double? width,
    double? height,
    Color? color,
    bool enabled = true,
    BoxConstraints? constraints,
    bool renderAsIcon = true,
  }) {
    final cacheKey = renderAsIcon
        ? 'icon-${bytes.length}-${size ?? 'default'}-${color?.value ?? 'default'}-$enabled'
        : 'img-${bytes.length}-${width ?? 'default'}-${height ?? 'default'}-$enabled';

    if (_imageCache.containsKey(cacheKey)) {
      return _imageCache[cacheKey];
    }

    try {
      Widget imageWidget;

      if (renderAsIcon) {
        // Icon rendering (for toolbars)
        final imageSize = size ?? AppSizes.icon;
        final imageColor = color ?? AppColors.getColor(enabled);

        imageWidget = ConstrainedBox(
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
      } else {
        // Real image rendering (for labels)
        imageWidget = ConstrainedBox(
          constraints: constraints ??
              BoxConstraints(
                maxWidth: width ?? 64,
                maxHeight: height ?? 64,
              ),
          child: Opacity(
            opacity: enabled ? 1.0 : 0.5,
            child: Image.memory(
              bytes,
              width: width,
              height: height,
              fit: BoxFit.contain,
              errorBuilder: (context, error, stackTrace) =>
                  Icon(Icons.broken_image, size: width ?? height ?? 32),
            ),
          ),
        );
      }

      _imageCache[cacheKey] = imageWidget;
      return imageWidget;
    } catch (e) {
      print('Error decoding image bytes: $e');
      final fallbackSize =
          renderAsIcon ? (size ?? AppSizes.icon) : (width ?? height ?? 32);
      return Icon(Icons.broken_image, size: fallbackSize);
    }
  }

  static Widget? buildVImage(
    VImage? image, {
    double? size,
    double? width,
    double? height,
    Color? color,
    bool enabled = true,
    BoxConstraints? constraints,
    bool useBinaryImage = true,
    bool renderAsIcon = true,
  }) {
    if (image == null) return null;

    // Try icon filename first
    if (image.filename?.isNotEmpty ?? false) {
      final iconWidget = buildIconWidget(
        image.filename!,
        size: size ?? width ?? height,
        color: color,
        enabled: enabled,
      );
      if (iconWidget != null) return iconWidget;
    }

    // Try binary image data
    if (useBinaryImage && image.imageData?.data != null) {
      return _buildBinaryImage(
        Uint8List.fromList(image.imageData!.data!),
        size: size,
        width: width,
        height: height,
        color: color,
        enabled: enabled,
        constraints: constraints,
        renderAsIcon: renderAsIcon,
      );
    }

    return null;
  }

  // Public APIs - Binary images
  static Widget? buildImageWidget(Uint8List bytes,
          {double? size,
          Color? color,
          bool enabled = true,
          BoxConstraints? constraints}) =>
      _buildBinaryImage(bytes,
          size: size,
          color: color,
          enabled: enabled,
          constraints: constraints,
          renderAsIcon: true);

  static Widget? buildRealImageWidget(Uint8List bytes,
          {double? width,
          double? height,
          bool enabled = true,
          BoxConstraints? constraints}) =>
      _buildBinaryImage(bytes,
          width: width,
          height: height,
          enabled: enabled,
          constraints: constraints,
          renderAsIcon: false);

  // Cache management
  static void clearCache() {
    _iconCache.clear();
    _imageCache.clear();
  }

  static Map<String, int> getCacheStats() => {
        'iconCache': _iconCache.length,
        'imageCache': _imageCache.length,
      };

  // Byte array utilities
  static List<int>? parseByteArray(dynamic value) {
    if (value == null) return null;

    if (value is String) {
      try {
        return base64Decode(value).toList();
      } catch (e) {
        return null;
      }
    }

    if (value is List) {
      return value.map((e) => (e as num).toInt()).toList();
    }

    return null;
  }

  static String? serializeByteArray(List<int>? value) {
    if (value == null) return null;

    final bytes = Uint8List.fromList(value);
    return base64Encode(bytes);
  }

  static Future<ui.Image?> decodeVImageToUIImage(VImage? image) async {
    if (image?.imageData?.data == null) {
      return null;
    }

    try {
      final data = image!.imageData!.data!;
      final bytes = data is String
          ? base64Decode(data as String)
          : Uint8List.fromList((data as List).cast<int>());

      final codec = await ui.instantiateImageCodec(bytes);
      final frame = await codec.getNextFrame();
      return frame.image;
    } catch (e) {
      print('Error decoding VImage to ui.Image: $e');
      return null;
    }
  }
}
