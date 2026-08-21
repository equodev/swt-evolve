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

/// The widget that currently owns the keyboard, or `null` when none does.
Object? _keyOwningEditor;

/// True while a focused widget runs its own keyboard pipeline (today: StyledText) and the
/// top-level handler stays out of its way. The owner must then forward *every* keystroke it sees,
/// whatever mode it is in: nothing else forwards them.
bool get focusedEditorHandlesOwnKeys => _keyOwningEditor != null;

/// Claims (or releases) keyboard ownership for [editor]. A release only takes effect for the
/// current owner, so a blur cannot clear the claim a newly focused editor just made — the two
/// arrive in no guaranteed order.
void setEditorKeyOwnership(Object editor, bool owns) {
  if (owns) {
    _keyOwningEditor = editor;
  } else if (identical(_keyOwningEditor, editor)) {
    _keyOwningEditor = null;
  }
}
