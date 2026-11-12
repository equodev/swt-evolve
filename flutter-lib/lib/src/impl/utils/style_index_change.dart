import '../../gen/styledtext.dart';

/// First element of a style index that is a change from the one held rather than a whole index.
const int styleIndexChangeMark = -2147483648;

/// [base] with a change `[mark, checksum, serial, paletteName, from, removed, inserted...]`
/// applied, or null when it does not fit or fails its checksum.
List<int>? applyStyleIndexChange(List<int> base, List<int> change) {
  if (change.length < 6 || base.isEmpty) return null;
  final from = change[4];
  final removed = change[5];
  if (from < 1 || removed < 0 || from + removed > base.length) return null;
  // Nothing changed: the same list, so nothing that compares by identity sees a new index.
  if (removed == 0 && change.length == 6 && change[3] == base[0]) {
    return styleIndexChecksum(base) == change[1] ? base : null;
  }
  final inserted = change.length - 6;
  final tail = base.length - from - removed;
  final out = List<int>.filled(from + inserted + tail, 0);
  out[0] = change[3];
  out.setRange(1, from, base, 1);
  out.setRange(from, from + inserted, change, 6);
  out.setRange(from + inserted, out.length, base, from + removed);
  return styleIndexChecksum(out) == change[1] ? out : null;
}

/// The checksum Java sends with a change. Kept below 2^30 at every step, so the arithmetic is
/// exact in a JavaScript double as well.
int styleIndexChecksum(List<int> index) {
  var hash = index.length;
  for (final value in index) {
    hash = (hash * 31 + value) & 0x3FFFFFFF;
  }
  return hash;
}

/// Resolves a style-index change that reached [value] while no state showed it, against [held],
/// the index the value had before. Nothing carried that index across edits meanwhile, so it is the
/// one the change was made from; a change that does not fit it is left for the state to ask for
/// the widget again.
void resolveUnshownStyleIndexChange(VStyledText value, List<int>? held) {
  final incoming = value.styleIndex;
  if (held == null || incoming == null || incoming.isEmpty || incoming[0] != styleIndexChangeMark) return;
  final resolved = applyStyleIndexChange(held, incoming);
  if (resolved != null) value.styleIndex = resolved;
}
