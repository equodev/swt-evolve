/// Window commands on a native surface: nothing to do.
///
/// A shell the Java side detached into a window of its own gets a real top-level window there,
/// created natively alongside this one (see `DeskDisplayBridge`), so no client is asked to open
/// anything. Only the web surface has to be driven from inside a page.
library;

void registerWindowCommands(int displayId) {}

void disposeWindowCommands() {}

/// The windows this client opened, by shell id. Always empty here; the web implementation fills it
/// and the tests read it.
Map<int, Object> get openedWindows => const {};
