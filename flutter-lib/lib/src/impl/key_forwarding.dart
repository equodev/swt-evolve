/// Whether a whole-tree [DisplaySwt] is mounted and forwarding keystrokes from a single
/// top-level handler (see `display_evolve.dart`).
///
/// In the whole-tree (desk/web) model every keystroke is captured once at the Display root and
/// routed to Java's focused control, reproducing SWT's "the Display sees the key first" contract
/// (which is what Eclipse's `Display.addFilter(SWT.KeyDown/Traverse, …)` command dispatch relies
/// on). While that is active, the per-control forwarders (`ControlImpl.wrap`, Text, Canvas,
/// StyledText) stay silent so the same key isn't dispatched to Java twice.
///
/// In the embedded backend there is no whole-tree Display surface, so this stays `false` and the
/// per-control forwarding remains the sole path — unchanged.
bool displayLevelKeyForwardingActive = false;

/// True while a focused widget runs its own keyboard pipeline and forwards its own key events
/// (today: StyledText). The top-level handler then stays out of its way entirely — it neither
/// forwards nor floods the comm channel for those keystrokes — so the editor's own per-key editing
/// and content sync are not raced by a parallel forward. Such widgets still reach Display filters
/// (Eclipse command bindings) through their own forwarding.
bool focusedEditorHandlesOwnKeys = false;
