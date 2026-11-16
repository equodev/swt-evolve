import 'package:flutter/widgets.dart' show Locale, WidgetsBinding;

import '../widget_config.dart';

/// On desktop the locale is the JVM's, which Java sends in the config flags.
Locale userLocale() {
  final tag = getConfigFlags().locale;
  if (tag == null || tag.isEmpty) return WidgetsBinding.instance.platformDispatcher.locale;
  return _parseTag(tag);
}

/// On desktop the locale of the OS user's regional settings, which the OS's own calendars follow
/// whatever locale the JVM runs in. Java reads it from the OS: Flutter's [Locale], and dart:io's
/// `Platform.localeName` with it, is the OS's display language instead.
Locale systemLocale() {
  final tag = getConfigFlags().regional_locale;
  if (tag == null || tag.isEmpty) return userLocale();
  return _parseTag(tag);
}

Locale _parseTag(String tag) {
  final parts = tag.split('-');
  String? script;
  String? country;
  for (final part in parts.skip(1)) {
    if (part.length == 4) {
      script ??= part;
    } else if (part.length == 2 || (part.length == 3 && int.tryParse(part) != null)) {
      country ??= part;
    }
  }
  return Locale.fromSubtags(languageCode: parts.first, scriptCode: script, countryCode: country);
}

/// On desktop the locale is the JVM's, which Java sends in the config flags.
String decimalSeparator() => getConfigFlags().decimal_separator ?? '.';
