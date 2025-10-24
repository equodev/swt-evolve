/// Strips accelerator characters (&) from text while preserving escaped ampersands (&&)
///
/// SWT uses & as accelerator characters for keyboard navigation, but in Flutter
/// we want to display the text without these special characters.
library;

String stripAccelerators(String? text) {
  if (text == null || text.isEmpty) {
    return text ?? '';
  }
  // Replace escaped ampersands (&& -> temporary placeholder)
  String result = text.replaceAll('&&', '\u0001ESCAPED_AMP\u0001');
  // Remove single accelerator ampersands
  result = result.replaceAll('&', '');
  // Restore escaped ampersands
  result = result.replaceAll('\u0001ESCAPED_AMP\u0001', '&');
  return result;
}
