import 'dart:io';
import 'dart:typed_data';
import 'dart:ui' as ui;
import 'package:flutter/foundation.dart';
import 'package:flutter/services.dart';
import 'package:path/path.dart' as path;
import 'package:swtflutter/src/impl/config_flags.dart';
import 'package:swtflutter/src/impl/widget_config.dart';

class AssetsManager {
  static String? _cachedAssetsPath;
  static bool _hasAttemptedResolve = false;

  /// Strips a leading UTF-8 BOM (U+FEFF) from an SVG string. flutter_svg's XML
  /// parser fails on a BOM before the root element, leaving the icon blank
  /// (which reads as "white" on a light theme). Most bundled icons carry one.
  static String _stripBom(String svg) =>
      svg.isNotEmpty && svg.codeUnitAt(0) == 0xFEFF ? svg.substring(1) : svg;

  /// True when [content] actually looks like an SVG document. On Flutter web,
  /// `rootBundle.loadString` for a MISSING asset does not throw — the app is
  /// served by a server whose SPA fallback returns index.html (HTTP 200) for
  /// unknown paths. That HTML would otherwise be handed to SvgPicture.string,
  /// which fails to parse it and renders a blank icon. Guard against it so a
  /// missing bundled icon falls through to the real image instead.
  static bool _looksLikeSvg(String content) {
    final t = content.trimLeft();
    if (t.startsWith('<svg')) return true;
    // Allow a leading XML prolog / comment before the root <svg> element.
    return t.startsWith('<?xml') && t.contains('<svg');
  }

  static Future<String?> _resolveAssetsPath() async {
    // Return cached value if we've already resolved it successfully
    if (_cachedAssetsPath != null) {
      return _cachedAssetsPath;
    }

    var configPath = getConfigFlags().assets_path;

    if (configPath == null) {
      if (!_hasAttemptedResolve) {
        _hasAttemptedResolve = true;
      }
      return null;
    }

    configPath = configPath.replaceAll('"', '');

    String absolutePath;
    if (path.isRelative(configPath)) {
      absolutePath = path.join(Directory.current.path, configPath);
    } else {
      absolutePath = configPath;
    }

    if (!await Directory(absolutePath).exists()) {
      return null;
    }

    // Cache the successful result
    _cachedAssetsPath = absolutePath;
    return absolutePath;
  }

  static Future<Object?> loadReplacement(String filename) async {
    final assetsPath = await _resolveAssetsPath();
    if (assetsPath != null) {
      final result = await _loadFromExternalPath(filename, assetsPath);
      if (result != null) return result;
    }

    if (getConfigFlags().use_default_icons == true) {
      return await _loadFromBundleAssets(filename);
    }

    return null;
  }

  static Future<Object?> _loadFromBundleAssets(String filename) async {
    String filenameOnly = filename;
    if (filename.contains('/')) {
      filenameOnly = filename.split('/').last;
    }
    final base = filenameOnly.split('.').first;
    final formats = ['svg', 'png', 'jpg', 'jpeg'];

    for (final format in formats) {
      try {
        final assetPath = 'assets/icons/$base.$format';
        if (format == 'svg') {
          try {
            final content = _stripBom(await rootBundle.loadString(assetPath));
            // On web a missing asset yields the SPA fallback HTML, not a 404.
            if (!_looksLikeSvg(content)) continue;
            return content;
          } on UnsupportedError {
            continue;
          } catch (e) {
            continue;
          }
        } else {
          final bytes = await rootBundle.load(assetPath);
          final codec = await ui.instantiateImageCodec(bytes.buffer.asUint8List());
          final frame = await codec.getNextFrame();
          return frame.image as Object;
        }
      } catch (_) {
        // asset not found, try next format
      }
    }
    return null;
  }

  static Future<Object?> _loadFromExternalPath(
    String filename,
    String assetsPath,
  ) async {
    // Extract just the filename without path (e.g., "toolbar/new_24.png" -> "new_24")
    String filenameOnly = filename;
    if (filename.contains('/')) {
      filenameOnly = filename.split('/').last;
    }
    final base = filenameOnly.split('.').first;
    final formats = ['svg', 'png', 'jpg', 'jpeg'];

    for (final format in formats) {
      try {
        final file = File('$assetsPath/$base.$format');
        if (await file.exists()) {
          if (format == 'svg') {
            final content = _stripBom(await file.readAsString());
            if (!_looksLikeSvg(content)) continue;
            return content;
          } else {
            final bytes = await file.readAsBytes();
            final codec = await ui.instantiateImageCodec(bytes);
            final frame = await codec.getNextFrame();
            return frame.image;
          }
        }
      } catch (e) {
        // Silently ignore errors
      }
    }
    return null;
  }
}
