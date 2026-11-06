import 'dart:convert';

import 'package:flutter/services.dart';

/// Where this package's own fonts and assets (pubspec.yaml) are found in the running app's bundle.
///
/// Flutter keeps the root app's at the top of the bundle and a dependency's under
/// `packages/<name>/`, families included. This package is the root when it runs as the app, and a
/// dependency when another app depends on it, so the keys are only known once the bundle is read.
class BundledAssets {
  static const String package = 'swtflutter';

  /// The families pubspec.yaml bundles, by the name Java asks for them.
  static const Set<String> families = {'Inter', 'Liberation Sans', 'Liberation Serif', 'Liberation Mono'};

  static String _prefix = '';

  /// Reads which of the two this app is. Until it has, as in this package's own tests, the assets
  /// are taken to be the root app's.
  static Future<void> resolve([AssetBundle? bundle]) async {
    final manifest = jsonDecode(await (bundle ?? rootBundle).loadString('FontManifest.json')) as List;
    final asDependency = manifest.any((font) => (font as Map)['family'] == 'packages/$package/Inter');
    _prefix = asDependency ? 'packages/$package/' : '';
  }

  /// The key the asset pubspec.yaml lists as [path] is loaded by.
  static String path(String path) => '$_prefix$path';

  /// The family [family] is registered under: a bundled one under this package's prefix, any other
  /// as it is.
  static String family(String family) => families.contains(family) ? '$_prefix$family' : family;

  /// The bundled family [family] names, whichever of its two names it is given; null for another.
  static String? bundledFamily(String? family) {
    if (family == null) return null;
    final name = family.startsWith('packages/$package/') ? family.substring('packages/$package/'.length) : family;
    return families.contains(name) ? name : null;
  }
}
