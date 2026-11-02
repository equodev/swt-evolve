import '../gen/widgets.dart' show wireTypeNames;

/// A widget as one number: its type and its id together, which is what its channel `<type>/<id>`
/// names. The id alone is not enough, since a control's GC overlay shares its control's id.
///
/// What the registry and the delivery gate key their per-widget state by, so a frame that names a
/// widget by its type's number and its id reaches that state without building or hashing a string.
/// A channel turns into the same key as the numbers it spells out.
abstract final class WidgetKey {
  /// Type numbers per id: wire type numbers stay well below it. An id is 32 bits, so a key is exact
  /// on the web too.
  static const int _typeSlots = 65536;

  /// Keys of channels that name no widget (`Type/id`): above every widget's. 2^52, written out
  /// because a shift is 32 bits on the web.
  static const int _firstNamedChannel = 4503599627370496;

  static final Map<String, int> _typeNumbers = {
    for (final e in wireTypeNames.entries) e.value: e.key,
  };
  static final Map<String, int> _namedChannels = {};

  /// The key of the widget of type number [type] with [id].
  static int of(int type, int id) => id * _typeSlots + type;

  /// The number the wire gives the type [swt]. Every type has one, given when the code is generated:
  /// one without is a mistake, and throws.
  static int typeOf(String swt) =>
      _typeNumbers[swt] ?? (throw ArgumentError('type "$swt" has no wire number: add it to the generator\'s types'));

  /// The name of type number [type].
  static String typeName(int type) => wireTypeNames[type] ?? '';

  /// The type number of the widget [key].
  static int typeOfKey(int key) => key % _typeSlots;

  /// The id of the widget [key].
  static int idOfKey(int key) => (key - key % _typeSlots) ~/ _typeSlots;

  /// [key] as the channel it stands for, `Type/id`: for logs and tests, never to look anything up.
  static String describe(int key) {
    if (key < _firstNamedChannel) return '${typeName(typeOfKey(key))}/${idOfKey(key)}';
    for (final e in _namedChannels.entries) {
      if (e.value == key) return e.key;
    }
    return '#$key';
  }

  /// The key of [channel]. Parsed, so for where a channel arrives as a string; a frame's numbers
  /// give it through [of].
  static int ofChannel(String channel) {
    final slash = channel.lastIndexOf('/');
    if (slash > 0) {
      final id = int.tryParse(channel.substring(slash + 1));
      if (id != null) return of(typeOf(channel.substring(0, slash)), id);
    }
    return _namedChannels[channel] ??= _firstNamedChannel + _namedChannels.length;
  }
}
