import 'package:flutter/material.dart';

import '../gen/composite.dart';
import '../impl/composite_evolve.dart';

/// Extension hook for the EWT integration.
///
/// Evolve knows nothing about EWT. It only exposes an `EwtWidget` region and
/// this hook; the EWT side (when present) sets [ewtRegionBuilder] at startup so
/// the region draws an EWT-built subtree. If it stays null, the region renders
/// empty — which is why flutter-lib keeps building without any EWT dependency.
///
/// [id] is the region's `value.id`. Each EwtWidget region passes its own id so the
/// EWT side builds the right subtree for it — without an id, multiple regions would
/// share one builder and overwrite each other (last one wins).
typedef EwtRegionBuilder = Widget Function(int id);

EwtRegionBuilder? ewtRegionBuilder;

/// A composite whose content is provided by EWT via [ewtRegionBuilder].
class EwtWidgetSwt extends CompositeSwt<VComposite> {
  const EwtWidgetSwt({super.key, required super.value});

  @override
  State<StatefulWidget> createState() => EwtWidgetImpl();
}

class EwtWidgetImpl extends CompositeImpl<EwtWidgetSwt, VComposite> {
  @override
  Widget buildComposite() {
    final builder = ewtRegionBuilder;
    if (builder == null) return const SizedBox.shrink();
    // Contain EWT-side failures to this region: Evolve stays agnostic AND resilient,
    // so a bug in the EWT subtree must not take down the whole Evolve widget tree.
    try {
      // Clip to the region's own bounds by default: an EWT subtree larger than its
      // SWT-allotted box stays inside instead of bleeding over neighbouring widgets
      // (adjacent EwtWidget regions, Evolve controls), matching how an SWT Composite
      // clips its children. Overflow/overlay could be opted into later (e.g. a style
      // bit) for cases that want the EWT subtree to extend past the region.
      return ClipRect(child: builder(state.id));
    } catch (e, st) {
      debugPrint('EwtWidget region builder failed: $e\n$st');
      return const SizedBox.shrink();
    }
  }
}
