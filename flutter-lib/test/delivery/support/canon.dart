/// The canonical form of a serialized widget payload, Dart side.
///
/// Every convergence check in the delivery suite ends in "are these two states the same", and the
/// two states being compared were produced by different code paths - a full send against a merged
/// sequence of diffs, or Java's view against the client's. They are equal in meaning long before
/// they are equal as text: one side elides a default the other writes out, key order differs, a
/// write stamp advanced. This reduces both to a form where equal-in-meaning is equal-as-text.
///
/// The rules are stated once, in `../canon_cases.json`, and implemented twice - here and in Java's
/// `dev.equo.swt.delivery.Canon`. Neither implementation may be used to test the other; the shared
/// case file is what keeps them honest.
///
/// One bias governs every arguable rule: when it is unclear whether a difference is real,
/// **report a difference**. A canonicalizer that normalises away a real change turns every probe
/// downstream of it into a silent false green. A spurious difference only costs someone a look.
library;

import 'dart:convert';

/// [value] reduced to its canonical form. Objects and lists are rebuilt, not mutated.
Object? canon(Object? value) {
  if (value is Map) {
    final out = <String, Object?>{};
    final keys = value.keys.map((k) => k.toString()).toList()..sort(); // R5
    for (final key in keys) {
      if (_isProtocolKey(key)) continue; // R1
      final canonical = canon(value[key]);
      if (_isTypeDefault(canonical)) continue; // R2
      out[key] = canonical;
    }
    return out;
  }
  if (value is List) return value.map(canon).toList(); // R6 - order preserved
  if (value is double && value == value.roundToDouble() && value.isFinite) {
    return value.toInt(); // R4
  }
  return value;
}

/// Bookkeeping about the message rather than the widget: the write stamp, and anything the
/// underscore convention marks as protocol - which state can never collide with, since a property
/// name never starts with one.
bool _isProtocolKey(String key) => key == '_s' || key.startsWith('_');

/// Values both codecs elide when writing, so absent and default-valued must compare equal.
/// Deliberately excludes the empty string and the empty object (R3): a sender that writes one
/// means something by it, and the bias rule sends every arguable case the strict way.
bool _isTypeDefault(Object? value) {
  if (value == null) return true;
  if (value is bool) return value == false;
  if (value is num) return value == 0;
  if (value is List) return value.isEmpty;
  return false;
}

/// Canonical form as compact text - the comparison key.
///
/// Compact and not pretty because Java's `Canon` emits the same bytes for the same state: when an
/// end-to-end probe compares Java's view of a widget against the client's, the two strings are
/// produced by different languages and must still match character for character.
String canonJson(Object? value) => json.encode(canon(value));

/// Canonical form indented, for a failure message only. Never compare with this.
String canonPretty(Object? value) => const JsonEncoder.withIndent('  ').convert(canon(value));

/// Whether two payloads carry the same state.
bool canonEquals(Object? a, Object? b) => canonJson(a) == canonJson(b);
