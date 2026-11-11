import 'dart:async';
import 'dart:convert';

import 'package:flutter/services.dart' show FontLoader, rootBundle;

/// The font families the web page keeps out of the engine's start (web/flutter_bootstrap.js). Each
/// is registered the first time text asks for it ([request]): a face costs main-thread time to
/// register (the CanvasKit renderer rebuilds its whole font collection for each one), so a face no
/// text uses is never paid for. Text laid out before its face arrives is laid out again when it
/// does, as for any font change; what cannot be laid out again (text drawn into an Image, a
/// measurement) waits for [all].
class DeferredFonts {
  static Map<String, List<String>> _families = const {};
  static final Map<String, Future<void>> _loading = {};
  static Future<void> Function(String family, List<String> assets) _register = _registerFromBundle;

  /// Takes the families of [manifestJson] (FontManifest.json's format); null, as off the web, is
  /// none. Loads nothing yet.
  static void configure(
    String? manifestJson, {
    Future<void> Function(String family, List<String> assets)? register,
  }) {
    _loading.clear();
    _register = register ?? _registerFromBundle;
    if (manifestJson == null || manifestJson.isEmpty) {
      _families = const {};
      return;
    }
    _families = {
      for (final family in (jsonDecode(manifestJson) as List).cast<Map<String, dynamic>>())
        family['family'] as String: [
          for (final font in (family['fonts'] as List).cast<Map<String, dynamic>>())
            font['asset'] as String,
        ],
    };
  }

  /// Registers [family] if it is one of the deferred ones and is not in yet. Completes once it is
  /// in, or failed to load; at once for any other family.
  static Future<void> request(String? family) {
    final assets = family == null ? null : _families[family];
    if (assets == null) return Future<void>.value();
    return _loading.putIfAbsent(
      family!,
      () => _register(family, assets).catchError((Object e) {
        print('Font $family not loaded: $e');
      }),
    );
  }

  /// Registers every deferred family. Completes once all are in, or failed to load.
  static Future<void> all() => Future.wait<void>(_families.keys.map(request)).then((_) {});

  static Future<void> _registerFromBundle(String family, List<String> assets) {
    final loader = FontLoader(family);
    for (final asset in assets) {
      loader.addFont(rootBundle.load(asset));
    }
    return loader.load();
  }
}
