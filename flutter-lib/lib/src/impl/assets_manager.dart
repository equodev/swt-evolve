import 'dart:io';
import 'dart:typed_data';
import 'dart:ui' as ui;
import 'package:flutter/services.dart';
import 'package:path/path.dart' as path;
import 'package:swtflutter/src/impl/config_flags.dart';
import 'package:swtflutter/src/impl/widget_config.dart';

class AssetsManager {
  static String? _cachedAssetsPath;
  static bool _hasAttemptedResolve = false;

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
      return await _loadFromExternalPath(filename, assetsPath);
    }
    return null;
  }

  static Future<Object?> _loadFromExternalPath(
      String filename, String assetsPath) async {
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
            return await file.readAsString();
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
