import 'dart:io';
import 'dart:typed_data';
import 'dart:ui' as ui;
import 'package:flutter/services.dart';
import 'package:path/path.dart' as path;
import 'package:swtflutter/src/impl/config_flags.dart';
import 'package:swtflutter/src/impl/widget_config.dart';

class AssetsManager {
  static Future<String?> _resolveAssetsPath() async {
    var configPath = getConfigFlags().assets_path;
    if (configPath == null) return null;

    configPath = configPath.replaceAll('"', '');

    String absolutePath;
    if (path.isRelative(configPath)) {
      absolutePath = path.join(Directory.current.path, configPath);
    } else {
      absolutePath = configPath;
    }

    print('AssetsManager: assets_path resolved to = $absolutePath');

    if (!await Directory(absolutePath).exists()) {
      print('AssetsManager: Directory $absolutePath does not exist');
      return null;
    }

    return absolutePath;
  }

  static Future<Object?> loadReplacement(String filename) async {
    final assetsPath = await _resolveAssetsPath();
    if (assetsPath != null) {
      return await _loadFromExternalPath(filename, assetsPath);
    }
    return null;
  }

  static Future<Object?> _loadFromExternalPath(String filename, String assetsPath) async {
    final base = filename.split('.').first;
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
      } catch (_) {}
    }
    return null;
  }

}