import 'package:flutter/widgets.dart' show Locale, WidgetsBinding;

import '../widget_config.dart';

/// On desktop the locale is the JVM's, which Java sends in the config flags.
Locale userLocale() {
  final tag = getConfigFlags().locale;
  if (tag == null || tag.isEmpty) return WidgetsBinding.instance.platformDispatcher.locale;
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
