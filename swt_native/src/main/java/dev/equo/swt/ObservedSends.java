package dev.equo.swt;

/**
 * Hands every outbound frame to the {@link FlutterBridge.SendObserver} a verification harness set.
 * Installed only under {@code dev.equo.swt.observeSends}, which a test run sets.
 */
final class ObservedSends {

    private static volatile FlutterBridge.SendObserver observer;

    private ObservedSends() {
    }

    static void set(FlutterBridge.SendObserver o) {
        if (o != null && !BridgeTap.installed(TAP))
            throw new IllegalStateException("sends are observed only with -Ddev.equo.swt.observeSends=true");
        observer = o;
    }

    static final BridgeTap TAP = new BridgeTap() {
        @Override
        void widgetFrame(WidgetWire frame) {
            FlutterBridge.SendObserver o = observer;
            if (o != null) o.onSend(WidgetWire.CHANNEL, frame.payload());
        }

        @Override
        void sent(byte[] buffer, int payloadStart, int length) {
            FlutterBridge.SendObserver o = observer;
            if (o != null) o.onSend(FlutterBridge.channelOf(buffer, payloadStart), java.util.Arrays.copyOfRange(buffer, payloadStart, length));
        }
    };
}
