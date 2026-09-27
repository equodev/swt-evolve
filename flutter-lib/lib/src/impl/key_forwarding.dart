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

/// The canvas editor (a StyledText) that holds keyboard focus, or `null`.
Object? _focusedCanvasEditor;

/// True while a canvas editor holds keyboard focus; the browser must then not move DOM focus on
/// Tab, since the editor's Traverse listeners decide.
bool get canvasEditorFocused => _focusedCanvasEditor != null;

/// Records that [editor] gained or lost focus. A loss only counts for the current holder, so a blur
/// cannot clear what a newly focused editor just set — the two arrive in no guaranteed order.
void setCanvasEditorFocus(Object editor, bool focused) {
  if (focused) {
    _focusedCanvasEditor = editor;
  } else if (identical(_focusedCanvasEditor, editor)) {
    _focusedCanvasEditor = null;
  }
}
