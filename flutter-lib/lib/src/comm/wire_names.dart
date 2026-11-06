import '../gen/widgets.dart' show wireChannelNames, wireTypeNames;

/// The wire's channel and type names by number: this client's own (gen/widgets.dart), then those an
/// app built on it adds with [extend], from a table generated with them. Java finds the same table as
/// a `WireExtension` service, and the fingerprint both sides compare covers it.
abstract final class WireNames {
  /// Channel names by number.
  static final Map<int, String> channels = {...wireChannelNames};

  /// Type names by number.
  static final Map<int, String> types = {...wireTypeNames};

  /// Type numbers by name.
  static final Map<String, int> typeNumbers = {for (final e in wireTypeNames.entries) e.value: e.key};

  /// Adds [channelNames], numbered from [firstChannel], and [typeNames], numbered from [firstType].
  /// Called before the first message names one; adding the same table again changes nothing.
  static void extend({
    required int firstChannel,
    required List<String> channelNames,
    required int firstType,
    required List<String> typeNames,
  }) {
    _append(channels, firstChannel, channelNames, 'channels');
    _append(types, firstType, typeNames, 'types');
    for (var i = 0; i < typeNames.length; i++) {
      typeNumbers[typeNames[i]] = firstType + i;
    }
  }

  static void _append(Map<int, String> into, int first, List<String> names, String what) {
    if (names.isEmpty || into[first] == names.first) return;
    final next = into.keys.fold(0, (a, b) => a > b ? a : b) + 1;
    if (first != next) {
      throw StateError('the $what added start at $first, not $next: they were not generated with this client');
    }
    for (var i = 0; i < names.length; i++) {
      into[first + i] = names[i];
    }
  }
}
