import 'package:flutter/widgets.dart';

import '../gen/sashform.dart';
import '../impl/composite_evolve.dart';

/// A SashForm's dividers are its Sash children: Java lays them out with the panes, and a drag on
/// one is that Sash's own Selection, which SashForm turns into the panes' new bounds and weights.
class SashFormImpl<T extends SashFormSwt, V extends VSashForm>
    extends CompositeImpl<T, V> {
  @override
  Widget build(BuildContext context) {
    final built = super.build(context);
    // A childless Composite is already tagged by ControlImpl.wrap().
    final children = state.children;
    return children == null || children.isEmpty ? built : tagSemantics(built);
  }
}
