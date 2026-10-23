import '../gen/sashform.dart';
import '../impl/composite_evolve.dart';

/// A SashForm's dividers are its Sash children: Java lays them out with the panes, and a drag on
/// one is that Sash's own Selection, which SashForm turns into the panes' new bounds and weights.
class SashFormImpl<T extends SashFormSwt, V extends VSashForm>
    extends CompositeImpl<T, V> {}
