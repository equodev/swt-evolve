import 'dart:async';
import 'dart:convert';
import 'dart:io';
import 'dart:typed_data';
import 'dart:ui' as ui;
import 'package:flutter/material.dart';
import 'package:flutter_svg/flutter_svg.dart';
import '../../gen/image.dart';
import '../assets_manager.dart';
import '../icons_map.dart';
import '../widget_config.dart';
import 'glyph_tone.dart';

class ImageUtils {
  static final Map<String, Widget> _iconCache = {};
  static final Map<String, Widget> _imageCache = {};
  // Cache for async image loading Futures to prevent recreation on every rebuild
  static final Map<String, Future<Widget?>> _futureCache = {};

  /// What each cached render resolved to, readable synchronously by a widget mounted after it did.
  static final Expando<Widget> _resolvedRenders = Expando('resolved render');

  /// The widget [render] resolved to, or null while it is still pending.
  static Widget? resolvedRender(Future<Widget?> render) => _resolvedRenders[render];
  static final Map<String, DecorationImage> _backgroundImageCache = {};
  static final Map<String, bool> _replacedByName = {};
  static final Map<String, Future<bool>> _replacedByNamePending = {};

  /// Whether Evolve substitutes its own artwork for the icon named [filename] — the bundled set or
  /// the icon map, the same lookup [buildVImageAsync] performs, remembered so a caller that has to
  /// choose *which* image to draw can ask without resolving again.
  ///
  /// Null while the answer is still unknown: the bundle lookup is asynchronous, so a caller
  /// deciding inside `build()` gets an answer only once [resolveReplacedByName] has run.
  static bool? replacedByName(String? filename) =>
      (filename == null || filename.isEmpty) ? null : _replacedByName[filename];

  /// Resolves and remembers [replacedByName]. Safe to call repeatedly; the lookup runs once.
  static Future<bool> resolveReplacedByName(String filename) {
    final known = _replacedByName[filename];
    if (known != null) return Future.value(known);
    return _replacedByNamePending[filename] ??= _resolveReplacedByName(filename);
  }

  static Future<bool> _resolveReplacedByName(String filename) async {
    var replaced = false;
    try {
      replaced = await AssetsManager.loadReplacement(filename) != null;
    } catch (_) {
      // Treated as "no replacement", same as buildVImageAsync does.
    }
    replaced = replaced || buildIconWidget(filename) != null;
    _replacedByName[filename] = replaced;
    _replacedByNamePending.remove(filename);
    return replaced;
  }

  /// SWT tiles a Control's backgroundImage at native size (unlike buildVImage's
  /// BoxFit.contain path for icons/labels).
  static DecorationImage? buildTiledBackgroundImage(VImage? image) {
    final data = image?.imageData?.data;
    if (data == null) return null;

    final cacheKey = 'tiled-${stableImageKey(image)}';
    final cached = _backgroundImageCache[cacheKey];
    if (cached != null) return cached;

    final decorationImage = DecorationImage(
      image: MemoryImage(asBytes(data)),
      repeat: ImageRepeat.repeat,
      alignment: Alignment.topLeft,
      // Without an explicit fit, repeat defaults to BoxFit.scaleDown per tile.
      fit: BoxFit.none,
    );
    _backgroundImageCache[cacheKey] = decorationImage;
    return decorationImage;
  }

  // ui.Image instances rendered by GCDrawer.standalone, keyed by the remoteRef Java holds so a
  // later drawImage() can reuse them instead of round-tripping PNG bytes (see GCImageDrawer.java).
  // Java mints the refs and hands one down with gcDispose, so it can treat a render as owned by
  // Flutter without waiting for an answer.
  static final Map<int, RemoteRender> _remoteImageCache = {};

  /// Whether [ref]'s picture is already rendered and held here. A ref is minted once per render
  /// and never reused, so a hit can only be that render's own output — nothing still queued can
  /// change it.
  static bool hasRemoteImage(int ref) => _remoteImageCache.containsKey(ref);

  /// Widgets that were handed [ref] before its render landed here, waiting for it.
  static final Map<int, List<Completer<void>>> _remoteWaiters = {};

  static void registerRemoteImage(int ref, ui.Image image) {
    final previous = _remoteImageCache[ref];
    _remoteImageCache[ref] = RemoteRender.ofImage(image);
    previous?.dispose();
    _wakeRemoteWaiters(ref);
  }

  /// Holds a render as a picture; pixels are made only when something reads them.
  static void registerRemotePicture(int ref, ui.Picture picture, int width, int height,
      {int depth = 0}) {
    final previous = _remoteImageCache[ref];
    _remoteImageCache[ref] = RemoteRender(picture, width, height, depth);
    previous?.dispose();
    _wakeRemoteWaiters(ref);
  }

  static void _wakeRemoteWaiters(int ref) {
    for (final waiter in _remoteWaiters.remove(ref) ?? const <Completer<void>>[]) {
      if (!waiter.isCompleted) waiter.complete();
    }
  }

  /// The render behind [ref], waiting for it when a widget got the ref before the render that
  /// fills it arrived. Null if it never does within [timeout].
  static Future<RemoteRender?> _awaitRemoteRender(int ref,
      {Duration timeout = const Duration(seconds: 10)}) async {
    final ready = _remoteImageCache[ref];
    if (ready != null) return ready;
    final waiter = Completer<void>();
    (_remoteWaiters[ref] ??= []).add(waiter);
    try {
      await waiter.future.timeout(timeout);
    } on TimeoutException {
      _remoteWaiters[ref]?.remove(waiter);
    }
    return _remoteImageCache[ref];
  }

  /// The render behind [ref], still as a picture where it never needed to be pixels.
  static RemoteRender? remoteRender(int ref) => _remoteImageCache[ref];

  /// The rendered pixels behind [ref], PNG-encoded — the answer to Java's explicit
  /// `Image/requestPixels`. Null when nothing is registered under that ref.
  static Future<Uint8List?> encodeRemoteImagePng(int ref) async {
    final render = _remoteImageCache[ref];
    if (render == null) {
      print('[Image] no render registered for remoteRef $ref');
      return null;
    }
    return toBytes(render.rasteriseSync(), ui.ImageByteFormat.png);
  }

  /// [image]'s pixels in [format]; null if the engine cannot produce them.
  ///
  /// On Skwasm a read-back sizes the surface frames are drawn on to the image, then rasterises
  /// into it; a frame that resizes it in between gets the read-back done at the view's size.
  /// Read-backs therefore run one at a time, and one whose dimensions are not the image's is
  /// done again: matching dimensions mean the surface was the image's own when it rasterised.
  static Future<Uint8List?> toBytes(ui.Image image, ui.ImageByteFormat format) {
    final next = _readBack.then((_) async {
      for (var attempt = 0; ; attempt++) {
        final bytes = (await image.toByteData(format: format))?.buffer.asUint8List();
        if (bytes == null || attempt == 4 || _hasSize(bytes, format, image.width, image.height)) {
          return bytes;
        }
      }
    });
    _readBack = next.then((_) {}, onError: (_) {});
    return next;
  }

  static Future<void> _readBack = Future.value();

  static bool _hasSize(Uint8List bytes, ui.ImageByteFormat format, int width, int height) {
    if (format != ui.ImageByteFormat.png) return bytes.length == width * height * 4;
    // IHDR is always the first chunk; its width and height are at bytes 16 and 20.
    if (bytes.length < 24) return false;
    final header = ByteData.sublistView(bytes);
    return header.getUint32(16) == width && header.getUint32(20) == height;
  }

  static void releaseRemoteImage(int ref) {
    // This release arrives on a different comm registry than a drawImage() referencing
    // the same ref, so dispatch order isn't guaranteed to match send order even though
    // Java always sends the draw first. Evicting synchronously could beat an in-flight
    // draw's cache lookup, leaving it permanently blank. Defer by one event-loop turn.
    Future(() {
      _remoteImageCache.remove(ref)?.dispose();
    });
  }

  static Widget? buildIconWidget(
    String filename, {
    double? size,
    Color? color,
    bool enabled = true,
    double? disabledOpacity,
  }) {
    // Our icon map is a substitution like the bundled set, so the same flag governs it.
    if (!useEvolveIcons) return null;

    final preserveColors = preserveIconColors;
    final fade = disabledOpacity ?? AppOpacities.disabled;
    final cacheKey =
        '$filename-${size ?? 'default'}-${color?.value ?? 'default'}-$enabled-$preserveColors-$fade';

    if (_iconCache.containsKey(cacheKey)) {
      return _iconCache[cacheKey];
    }

    final iconData = getIconByName(filename);
    if (iconData == null) return null;

    final isFontAwesome =
        (iconData.fontPackage == 'font_awesome_flutter') ||
        (iconData.fontFamily ?? '').toLowerCase().contains('fontawesome');

    final iconSize =
        size ?? (isFontAwesome ? AppSizes.fontAwesomeIcon : AppSizes.icon);
    final iconColor = preserveColors
        ? null
        : (color ?? AppColors.getColor(enabled));

    Widget iconWidget = Icon(iconData, size: iconSize, color: iconColor);

    if (isFontAwesome) {
      iconWidget = Padding(
        padding: const EdgeInsets.symmetric(horizontal: 4),
        child: iconWidget,
      );
    }

    // Fade only when nothing tinted the icon: a disabled tint already carries the state, and
    // applying both leaves this branch's icons washed out next to a replacement's in the same
    // toolbar. Same rule as _buildBinaryImage and _buildReplacementWidget.
    if (preserveColors || color == null) {
      iconWidget = Opacity(opacity: enabled ? 1.0 : fade, child: iconWidget);
    }

    _iconCache[cacheKey] = iconWidget;
    return iconWidget;
  }

  static Widget? _buildBinaryImage({
    Uint8List? bytes,
    String? file,
    double? size,
    double? width,
    double? height,
    Color? color,
    bool enabled = true,
    BoxConstraints? constraints,
    double? disabledOpacity,
    bool renderAsIcon = true,
  }) {
    final preserveColors = preserveIconColors;
    final fade = disabledOpacity ?? AppOpacities.disabled;
    // Only a monochrome glyph takes the tint: color artwork would be flattened into a silhouette.
    final tinted = renderAsIcon &&
        !preserveColors &&
        bytes != null &&
        GlyphTone.ofEncoded(bytes, GlyphTintLimits.fallback) != null;
    final cacheKey = renderAsIcon
        ? 'icon-${bytes != null ? GlyphTone.contentKey(bytes) : file ?? 'none'}-${size ?? 'default'}-${color?.value ?? 'default'}-$enabled-$tinted-$fade'
        : (file != null)
        ? 'img-${file}-${width ?? 'default'}-${height ?? 'default'}-$enabled-$fade'
        : 'img-${bytes?.length ?? 'none'}-$enabled-$fade';

    if (_imageCache.containsKey(cacheKey)) {
      return _imageCache[cacheKey];
    }

    try {
      Widget imageWidget;

      if (renderAsIcon) {
        // Icon rendering (for toolbars)
        final imageSize = size ?? AppSizes.icon;

        Widget iconContent;

        if (tinted) {
          final imageColor = color ?? AppColors.getColor(enabled);
          iconContent = SizedBox(
            width: imageSize,
            height: imageSize,
            child: ImageIcon(
              MemoryImage(bytes!),
              size: imageSize,
              color: imageColor,
            ),
          );
          if (color == null) {
            iconContent = Opacity(
              opacity: enabled ? 1.0 : fade,
              child: iconContent,
            );
          }
        } else {
          iconContent = SizedBox(
            width: imageSize,
            height: imageSize,
            child: Image.memory(
              bytes!,
              width: imageSize,
              height: imageSize,
              fit: BoxFit.contain,
            ),
          );
          iconContent = Opacity(
            opacity: enabled ? 1.0 : fade,
            child: iconContent,
          );
        }

        if (constraints != null) {
          imageWidget = ConstrainedBox(
            constraints: constraints,
            child: iconContent,
          );
        } else if (size == null && width == null && height == null) {
          // Toolbar click targets get a minimum box. A caller that passed an
          // explicit box must not: label-like widgets (Label, CLabel, Button,
          // ExpandItem) are sized in Java by *Sizes.java from imageData, so a
          // wider image here is taken out of the text, which then ellipsizes.
          imageWidget = ConstrainedBox(
            constraints: BoxConstraints(
              minWidth: AppSizes.toolbarMinSize,
              minHeight: AppSizes.toolbarMinSize,
            ),
            child: iconContent,
          );
        } else {
          imageWidget = iconContent;
        }
      } else {
        // Real image rendering (for labels)
        // Prefer file path for now (most reliable for tests)
        Image image;
        if (bytes != null) {
          image = Image.memory(
            bytes,
            width: width,
            height: height,
            fit: BoxFit.contain,
            errorBuilder: (context, error, stackTrace) =>
                Icon(Icons.broken_image, size: width ?? height ?? 32),
          );
        } else if (file != null) {
          image = Image.file(
            File(file),
            width: width,
            height: height,
            fit: BoxFit.contain,
            errorBuilder: (context, error, stackTrace) =>
                Icon(Icons.broken_image, size: width ?? height ?? 32),
          );
        } else {
          print("ERROR: No image source");
          image = Image.memory(Uint8List(0)); // fallback
        }
        imageWidget = ConstrainedBox(
          constraints:
              constraints ??
              BoxConstraints(maxWidth: width ?? 64, maxHeight: height ?? 64),
          child: Opacity(opacity: enabled ? 1.0 : fade, child: image),
        );
      }

      _imageCache[cacheKey] = imageWidget;
      return imageWidget;
    } catch (e) {
      print('Error decoding image bytes: $e');
      final fallbackSize = renderAsIcon
          ? (size ?? AppSizes.icon)
          : (width ?? height ?? 32);
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
    double? disabledOpacity,
  }) {
    final preserveColors = preserveIconColors;
    final Color? effectiveTint = preserveColors
        ? null
        : (color ?? AppColors.getColor(enabled));

    final fade = disabledOpacity ?? AppOpacities.disabled;
    final cacheKey =
        'replacement-$filename-${size ?? width ?? height ?? 'default'}-'
        '${color?.value ?? 'default'}-${effectiveTint?.value ?? 'none'}-'
        '$preserveColors-$enabled-$renderAsIcon-$fade';

    if (_imageCache.containsKey(cacheKey)) {
      return _imageCache[cacheKey];
    }

    Widget? widget;

    if (replacement is String) {
      // SVG replacement
      final svgSize = size ?? width ?? height ?? AppSizes.icon;

      if (renderAsIcon) {
        Widget svgContent = SizedBox(
          width: svgSize,
          height: svgSize,
          child: SvgPicture.string(
            replacement,
            width: svgSize,
            height: svgSize,
            colorFilter: effectiveTint != null
                ? ColorFilter.mode(effectiveTint, BlendMode.srcIn)
                : null,
          ),
        );

        if (preserveColors || color == null) {
          svgContent = Opacity(opacity: enabled ? 1.0 : fade, child: svgContent);
        }

        if (constraints != null) {
          widget = ConstrainedBox(constraints: constraints, child: svgContent);
        } else if (size == null && width == null && height == null) {
          widget = ConstrainedBox(
            constraints: BoxConstraints(
              minWidth: AppSizes.toolbarMinSize,
              minHeight: AppSizes.toolbarMinSize,
            ),
            child: svgContent,
          );
        } else {
          widget = svgContent;
        }
      } else {
        Widget svgWidget = SvgPicture.string(
          replacement,
          width: width,
          height: height,
          fit: BoxFit.contain,
          colorFilter: effectiveTint != null
              ? ColorFilter.mode(effectiveTint, BlendMode.srcIn)
              : null,
        );

        if (preserveColors || color == null) {
          svgWidget = Opacity(opacity: enabled ? 1.0 : fade, child: svgWidget);
        }

        widget = ConstrainedBox(
          constraints:
              constraints ??
              BoxConstraints(maxWidth: width ?? 64, maxHeight: height ?? 64),
          child: svgWidget,
        );
      }
    } else if (replacement is ui.Image) {
      // PNG/JPG replacement - show colors as-is (no color filter)
      final imageSize = size ?? width ?? height ?? AppSizes.icon;

      if (renderAsIcon) {
        Widget imageContent = SizedBox(
          width: imageSize,
          height: imageSize,
          child: effectiveTint != null
              ? ColorFiltered(
                  colorFilter: ColorFilter.mode(effectiveTint, BlendMode.srcIn),
                  child: RawImage(
                    image: replacement,
                    width: imageSize,
                    height: imageSize,
                    fit: BoxFit.contain,
                  ),
                )
              : RawImage(
                  image: replacement,
                  width: imageSize,
                  height: imageSize,
                  fit: BoxFit.contain,
                ),
        );

        if (preserveColors || color == null) {
          imageContent = Opacity(
            opacity: enabled ? 1.0 : fade,
            child: imageContent,
          );
        }

        if (constraints != null) {
          widget = ConstrainedBox(
            constraints: constraints,
            child: imageContent,
          );
        } else if (size == null && width == null && height == null) {
          widget = ConstrainedBox(
            constraints: BoxConstraints(
              minWidth: AppSizes.toolbarMinSize,
              minHeight: AppSizes.toolbarMinSize,
            ),
            child: imageContent,
          );
        } else {
          widget = imageContent;
        }
      } else {
        Widget imageWidget = effectiveTint != null
            ? ColorFiltered(
                colorFilter: ColorFilter.mode(effectiveTint, BlendMode.srcIn),
                child: RawImage(
                  image: replacement,
                  width: width,
                  height: height,
                  fit: BoxFit.contain,
                ),
              )
            : RawImage(
                image: replacement,
                width: width,
                height: height,
                fit: BoxFit.contain,
              );

        if (preserveColors || color == null) {
          imageWidget = Opacity(
            opacity: enabled ? 1.0 : fade,
            child: imageWidget,
          );
        }

        widget = ConstrainedBox(
          constraints:
              constraints ??
              BoxConstraints(maxWidth: width ?? 64, maxHeight: height ?? 64),
          child: imageWidget,
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
    double? disabledOpacity,
  }) {
    if (image == null) {
      return Future.value(null);
    }

    // Generate cache key based on all parameters that affect the result
    final preserveColors = preserveIconColors;
    final cacheKey =
        'future-${stableImageKey(image)}-'
        '${size ?? width ?? height ?? 'default'}-${color?.value ?? 'default'}-'
        '$preserveColors-$useEvolveIcons-$enabled-$renderAsIcon-'
        '${disabledOpacity ?? AppOpacities.disabled}';

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
      disabledOpacity: disabledOpacity,
    );

    _futureCache[cacheKey] = future;
    future.then((widget) {
      if (widget != null) _resolvedRenders[future] = widget;
    });
    if (image.remoteRef != null) {
      // A render that never arrived is retried on the next build instead of staying blank.
      future.then((widget) {
        if (widget == null) _futureCache.remove(cacheKey);
      });
    }
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
    double? disabledOpacity,
  }) async {
    // SVG content loaded by Java (no filesystem access needed)
    if (image.svgContent?.isNotEmpty ?? false) {
      return _buildReplacementWidget(
        image.svgContent!,
        filename: image.svgContent!,
        size: size,
        width: width,
        height: height,
        color: color,
        enabled: enabled,
        constraints: constraints,
        renderAsIcon: renderAsIcon,
        disabledOpacity: disabledOpacity,
      );
    }

    // Try AssetsManager replacement first (if filename exists)
    if (image.filename?.isNotEmpty ?? false) {
      try {
        final replacement = await AssetsManager.loadReplacement(
          image.filename!,
        );
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
            disabledOpacity: disabledOpacity,
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
        disabledOpacity: disabledOpacity,
      );
      if (iconWidget != null) {
        return iconWidget;
      }
    }

    // Try binary image data (encoded PNG/JPG bytes sent from Java)
    if (useBinaryImage && image.imageData?.data != null) {
      return _buildBinaryImage(
        bytes: asBytes(image.imageData!.data!),
        size: size,
        width: width,
        height: height,
        color: color,
        enabled: enabled,
        constraints: constraints,
        renderAsIcon: renderAsIcon,
        disabledOpacity: disabledOpacity,
      );
    }

    // Drawn with a GC: the render stayed here and Java sent only its ref, no pixels.
    final ref = image.remoteRef;
    if (ref != null) {
      final render = await _awaitRemoteRender(ref);
      if (render == null) return null;
      // A clone, so the widget keeps a valid handle when the ref's render is released.
      return _buildReplacementWidget(
        render.rasteriseSync().clone(),
        filename: 'remote-$ref',
        size: size,
        width: width,
        height: height,
        color: color,
        enabled: enabled,
        constraints: constraints,
        renderAsIcon: renderAsIcon,
        disabledOpacity: disabledOpacity,
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
    double? disabledOpacity,
  }) {
    if (image == null) return null;

    // SVG content loaded by Java (no filesystem access needed)
    if (image.svgContent?.isNotEmpty ?? false) {
      return _buildReplacementWidget(
        image.svgContent!,
        filename: image.svgContent!,
        size: size,
        width: width,
        height: height,
        color: color,
        enabled: enabled,
        constraints: constraints,
        renderAsIcon: renderAsIcon,
        disabledOpacity: disabledOpacity,
      );
    }

    if (image.filename?.isNotEmpty ?? false) {
      final filename = image.filename!;

      // Same precedence as the async path: a resolved replacement wins, then our icon map, then
      // the application's own bytes. loadReplacement is always consulted so an assets_path
      // override applies even with disable_evolve_icons on; the flag gates what it falls back to.
      Widget? syncFallback = buildIconWidget(
        filename,
        size: size ?? width ?? height,
        color: color,
        enabled: enabled,
        disabledOpacity: disabledOpacity,
      );
      if (syncFallback == null && useBinaryImage && image.imageData?.data != null) {
        syncFallback = _buildBinaryImage(
          bytes: asBytes(image.imageData!.data!),
          file: filename,
          size: size, width: width, height: height,
          color: color, enabled: enabled,
          constraints: constraints, renderAsIcon: renderAsIcon,
        );
      }

      final fallback = syncFallback ?? const SizedBox.shrink();
      return FutureBuilder<Object?>(
        future: AssetsManager.loadReplacement(filename),
        builder: (context, snapshot) {
          final data = snapshot.data;
          if (data != null) {
            return _buildReplacementWidget(
              data,
              filename: filename,
              size: size, width: width, height: height,
              color: color, enabled: enabled,
              constraints: constraints, renderAsIcon: renderAsIcon,
            ) ?? fallback;
          }
          return fallback;
        },
      );
    }

    if (useBinaryImage && image.imageData?.data != null) {
      return _buildBinaryImage(
        bytes: asBytes(image.imageData!.data!),
        file: image.filename,
        size: size, width: width, height: height,
        color: color, enabled: enabled,
        constraints: constraints, renderAsIcon: renderAsIcon,
      );
    }

    return null;
  }

  /// Returns a stable string key for a VImage suitable for use as a widget key.
  /// Replaced images have filename=null (Java clears it), so we derive a stable
  /// key from the content instead of the object reference (which changes each rebuild).
  static String stableImageKey(VImage? image) {
    if (image == null) return 'no-image';
    if (image.filename?.isNotEmpty ?? false) return image.filename!;
    if (image.svgContent?.isNotEmpty ?? false) {
      return 'svg-${image.svgContent!.hashCode}';
    }
    final data = image.imageData?.data;
    if (data != null && data.isNotEmpty) {
      final len = data.length;
      // Use length + first 32 bytes folded into a hash for content identity
      final limit = len < 32 ? len : 32;
      var h = 0;
      for (var i = 0; i < limit; i++) {
        h = h * 31 + data[i];
      }
      return 'bin-$len-$h';
    }
    final ref = image.remoteRef;
    if (ref != null) return 'remote-$ref';
    return 'no-image';
  }

  // Cache management
  static void clearCache() {
    _iconCache.clear();
    _imageCache.clear();
    _futureCache.clear();
    _backgroundImageCache.clear();
    _replacedByName.clear();
    _replacedByNamePending.clear();
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
        return base64Decode(value);
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
    return base64Encode(asBytes(value));
  }

  /// [data] as bytes, copied only when it is not bytes already.
  static Uint8List asBytes(List<int> data) => data is Uint8List ? data : Uint8List.fromList(data);

  static Future<ui.Image?> decodeVImageToUIImage(VImage? image) async {
    final remoteRef = image?.remoteRef;
    if (remoteRef != null) {
      final cached = _remoteImageCache[remoteRef];
      // clone() so one reader's dispose() does not invalidate other readers of the cached image.
      if (cached != null) return (await cached.rasterise()).clone();
      print('No cached render for remoteRef $remoteRef; falling back to decode');
    }

    if (image?.imageData?.data == null) {
      return null;
    }

    final Object data = image!.imageData!.data!;
    if (data is String) return _decode(data);
    // A clone: callers dispose what they are given, and the decoded original serves the next draw.
    final decoded = await (_decodedFrom[data] ??= _decode(data));
    return decoded?.clone();
  }

  /// Decoded images by the bytes they were decoded from, so an image drawn again is not decoded
  /// again. Held for as long as those bytes are, which is as long as the image is.
  static final Expando<Future<ui.Image?>> _decodedFrom = Expando();

  static Future<ui.Image?> _decode(Object data) async {
    try {
      final bytes = data is String ? base64Decode(data) : asBytes(data as List<int>);
      final codec = await ui.instantiateImageCodec(bytes);
      final frame = await codec.getNextFrame();
      return frame.image;
    } catch (e) {
      print('Error decoding VImage to ui.Image: $e');
      return null;
    }
  }
}

/// A GC render kept as a picture, rasterised at most once and only when its pixels are read.
class RemoteRender {
  RemoteRender(this.picture, this.width, this.height, [this.depth = 0]);

  RemoteRender.ofImage(ui.Image image)
      : picture = null,
        width = image.width,
        height = image.height,
        depth = 0,
        _raster = image;

  final ui.Picture? picture;
  final int width, height;

  /// How many earlier renders [picture] draws inside itself as its base.
  final int depth;
  ui.Image? _raster;

  bool get hasPixels => _raster != null;

  Future<ui.Image> rasterise() async => rasteriseSync();

  /// Uses [ui.Picture.toImageSync]: the image stays on the GPU, so a blit never reads pixels back.
  ui.Image rasteriseSync() {
    final made = _raster;
    if (made != null) return made;
    return _raster = picture!.toImageSync(width, height);
  }

  void dispose() {
    final raster = _raster;
    _raster = null;
    // After the frame, not now: on the web renderers, disposing an image made by toImageSync while
    // another is being rasterised in the same frame corrupts that one.
    if (raster != null) WidgetsBinding.instance.endOfFrame.then((_) => raster.dispose());
    // The picture is not disposed: committed display lists may still draw it, and it cannot be
    // cloned. The engine reclaims it once unreferenced.
  }
}
