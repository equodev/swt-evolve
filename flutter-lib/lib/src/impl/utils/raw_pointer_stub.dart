/// Off the web, pointer moves already reach the app as they happen.
void listenRawPointerMoves(void Function(double x, double y, int buttons) onMove) {}

/// Whether the browser's own pointer presses can be read: only on the web.
const bool rawPointerPressesAvailable = false;

/// Off the web, every press reaches the app as pointer events.
void listenRawPointerPresses(
    void Function(String type, int pointerId, String pointerType, double x, double y, int buttons,
            Duration timeStamp)
        onEvent) {}
