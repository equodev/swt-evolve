// One of three slices of the theming conformance matrix; see theming_conformance.dart for what it
// checks and why it is split.
import 'theming_conformance.dart';

void main() => defineThemingConformance(shard: 0, shards: 3);
