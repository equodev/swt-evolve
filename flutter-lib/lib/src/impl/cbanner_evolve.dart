import '../gen/cbanner.dart';
import '../impl/composite_evolve.dart';

/// CBanner needs no build() of its own: DartCBannerLayout does the left/right/bottom arrangement
/// in Java, so every child reaches Dart with its final bounds and the plain Composite build lays
/// them out unchanged. Declaring one would be actively harmful — overriding build() bypasses
/// CompositeImpl.buildComposite(), which is what renders `children` at all, so the banner's
/// content (in an Eclipse 3.x workbench, the whole top CoolBar) disappears.
class CBannerImpl<T extends CBannerSwt, V extends VCBanner>
    extends CompositeImpl<T, V> {}
