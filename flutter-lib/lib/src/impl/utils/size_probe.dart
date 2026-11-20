import 'package:flutter/widgets.dart';

/// Marks the box whose size the size measurement reports under [name] (a table's `header`), next
/// to the size of the whole widget.
class SizeProbe {
  const SizeProbe(this.name);

  final String name;

  /// [child], measured as [name].
  static Widget wrap(String name, Widget child) => MetaData(metaData: SizeProbe(name), child: child);
}
