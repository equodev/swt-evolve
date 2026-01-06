import 'dart:convert';
import 'dart:typed_data';
import 'dart:ui' as ui;
import 'package:flutter/material.dart';
import 'package:flutter_svg/flutter_svg.dart';
import '../../gen/image.dart';
import '../assets_manager.dart';
import '../icons_map.dart';
import '../widget_config.dart';

class ImageUtils {
  static final Map<String, Widget> _iconCache = {};
  static final Map<String, Widget> _imageCache = {};
  // Cache for async image loading Futures to prevent recreation on every rebuild
  static final Map<String, Future<Widget?>> _futureCache = {};

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

    // Apply opacity for disabled icons
    iconWidget = Opacity(
      opacity: enabled ? 1.0 : 0.5,
      child: iconWidget,
    );

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
          child: Opacity(
            opacity: enabled ? 1.0 : 0.5,
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

  /// Helper to build widget from replacement object (SVG string or ui.Image)
  static Widget? _buildReplacementWidget(
    Object replacement, {
    required String filename,
    double? size,
    double? width,
    double? height,
    Color? color,
    bool enabled = true,
    BoxConstraints? constraints,
    bool renderAsIcon = true,
  }) {
    final cacheKey =
        'replacement-$filename-${size ?? width ?? height ?? 'default'}-${color?.value ?? 'default'}-$enabled-$renderAsIcon';

    if (_imageCache.containsKey(cacheKey)) {
      return _imageCache[cacheKey];
    }

    Widget? widget;

    if (replacement is String) {
      // SVG replacement
      final svgSize = size ?? width ?? height ?? AppSizes.icon;
      final svgColor = color ?? AppColors.getColor(enabled);

      if (renderAsIcon) {
        widget = ConstrainedBox(
          constraints: constraints ??
              BoxConstraints(
                minWidth: AppSizes.toolbarMinSize,
                minHeight: AppSizes.toolbarMinSize,
              ),
          child: Opacity(
            opacity: enabled ? 1.0 : 0.5,
            child: Center(
              child: SizedBox(
                width: svgSize,
                height: svgSize,
                child: SvgPicture.string(
                  replacement,
                  width: svgSize,
                  height: svgSize,
                  colorFilter: ColorFilter.mode(svgColor, BlendMode.srcIn),
                ),
              ),
            ),
          ),
        );
      } else {
        widget = ConstrainedBox(
          constraints: constraints ??
              BoxConstraints(
                maxWidth: width ?? 64,
                maxHeight: height ?? 64,
              ),
          child: Opacity(
            opacity: enabled ? 1.0 : 0.5,
            child: SvgPicture.string(
              replacement,
              width: width,
              height: height,
              fit: BoxFit.contain,
            ),
          ),
        );
      }
    } else if (replacement is ui.Image) {
      // PNG/JPG replacement - show colors as-is (no color filter)
      final imageSize = size ?? width ?? height ?? AppSizes.icon;

      if (renderAsIcon) {
        widget = ConstrainedBox(
          constraints: constraints ??
              BoxConstraints(
                minWidth: AppSizes.toolbarMinSize,
                minHeight: AppSizes.toolbarMinSize,
              ),
          child: Opacity(
            opacity: enabled ? 1.0 : 0.5,
            child: Center(
              child: SizedBox(
                width: imageSize,
                height: imageSize,
                child: RawImage(
                  image: replacement,
                  width: imageSize,
                  height: imageSize,
                  fit: BoxFit.contain,
                ),
              ),
            ),
          ),
        );
      } else {
        widget = ConstrainedBox(
          constraints: constraints ??
              BoxConstraints(
                maxWidth: width ?? 64,
                maxHeight: height ?? 64,
              ),
          child: Opacity(
            opacity: enabled ? 1.0 : 0.5,
            child: RawImage(
              image: replacement,
              width: width,
              height: height,
              fit: BoxFit.contain,
            ),
          ),
        );
      }
    }

    if (widget != null) {
      _imageCache[cacheKey] = widget;
    }
    return widget;
  }

  /// Async version that supports AssetsManager replacement
  /// Caches Futures to prevent recreation on every rebuild
  static Future<Widget?> buildVImageAsync(
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
    if (image == null) {
      return Future.value(null);
    }

    // Generate cache key based on all parameters that affect the result
    final cacheKey = 'future-${image.filename ?? ''}-${image.imageData?.data?.length ?? 0}-'
        '${size ?? width ?? height ?? 'default'}-${color?.value ?? 'default'}-$enabled-$renderAsIcon';

    // Return cached Future if it exists
    if (_futureCache.containsKey(cacheKey)) {
      return _futureCache[cacheKey]!;
    }

    // Create and cache the Future
    final future = _buildVImageAsyncImpl(
      image,
      size: size,
      width: width,
      height: height,
      color: color,
      enabled: enabled,
      constraints: constraints,
      useBinaryImage: useBinaryImage,
      renderAsIcon: renderAsIcon,
    );

    _futureCache[cacheKey] = future;
    return future;
  }

  /// Internal implementation that actually loads the image
  static Future<Widget?> _buildVImageAsyncImpl(
    VImage image, {
    double? size,
    double? width,
    double? height,
    Color? color,
    required bool enabled,
    BoxConstraints? constraints,
    required bool useBinaryImage,
    required bool renderAsIcon,
  }) async {
    // Try AssetsManager replacement first (if filename exists)
    if (image.filename?.isNotEmpty ?? false) {
      try {
        final replacement = await AssetsManager.loadReplacement(image.filename!);
        if (replacement != null) {
          return _buildReplacementWidget(
            replacement,
            filename: image.filename!,
            size: size,
            width: width,
            height: height,
            color: color,
            enabled: enabled,
            constraints: constraints,
            renderAsIcon: renderAsIcon,
          );
        }
      } catch (e) {
        // Silently ignore AssetsManager errors
      }
    }

    // Try icon filename (from iconMap)
    if (image.filename?.isNotEmpty ?? false) {
      final iconWidget = buildIconWidget(
        image.filename!,
        size: size ?? width ?? height,
        color: color,
        enabled: enabled,
      );
      if (iconWidget != null) {
        return iconWidget;
      }
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

  /// Synchronous version (backwards compatibility) - without AssetsManager support
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
    _futureCache.clear();
  }

  static Map<String, int> getCacheStats() => {
        'iconCache': _iconCache.length,
        'imageCache': _imageCache.length,
        'futureCache': _futureCache.length,
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
