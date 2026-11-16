/// The user's locale, the OS's regional one, and the decimal separator numbers are written with,
/// behind one import.
library;

export 'client_locale_stub.dart' if (dart.library.js_interop) 'client_locale_web.dart';
