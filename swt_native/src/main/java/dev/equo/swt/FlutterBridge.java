package dev.equo.swt;

import org.eclipse.swt.widgets.*;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

public abstract class FlutterBridge {
    private static final String DEV_EQU_SWT_NEW = "dev.equ.swt.new";
    protected static final FlutterClient client;
    private static final Serializer serializer = new Serializer();
    private static final Set<DartControl> dirty = new HashSet<>();
    private static FlutterBridge bridge;

    static {
        client = new FlutterClient();
        client.createComm();
    }

    private final CompletableFuture<Boolean> clientReady = new CompletableFuture<>();

    public static FlutterBridge of(DartControl dartControl) {
        if (bridge != null)
            return bridge;
        //if (isWeb) {}
//        if (isSwt)
        return SwtFlutterBridge.of(dartControl);
    }

    protected FlutterBridge() {
    }

    public static void update() {
        for (DartControl widget : dirty) {
            widget.getBridge().clientReady.thenRun(() -> {
                if (!isNew(widget)) { // send with the parent
                    String event = event(widget);
                    System.out.println("will send: " + event);
                    try {
                        ByteArrayOutputStream out = new ByteArrayOutputStream();
                        serializer.to(widget.getApi(), out);
                        String payload = out.toString(StandardCharsets.UTF_8);
                        System.out.println("send: " + event + ": " + payload);
                        client.getComm().send(event, payload);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    setNotNew(widget);
                }
            });
        }
        dirty.clear();
    }

    private static boolean isNew(DartControl widget) {
        return widget.getData("dev.equ.swt.new") == null;
    }

    private static void setNotNew(DartControl control) {
        control.setData(DEV_EQU_SWT_NEW, false);
    }

    public static void set(FlutterBridge staticBridge) {
        bridge = staticBridge;
    }

    protected void onReady(DartControl control) {
        setNotNew(control);
        client.getComm().on(event(control,"ClientReady"), p -> {
            if (!clientReady.isDone()) {
                System.out.println("ClientReady "+event(control));
                clientReady.complete(true);
            } else { // hot reload
                dirty(control);
                update();
            }
        });
    }

    public void dirty(DartControl widget) {
        if (widget == null)
            return;
        synchronized (dirty) {
            dirty.add(widget);
        }
    }

    public static String widgetName(DartWidget w) {
        return w.getClass().getSimpleName().substring(4);
    }

    public static String event(DartWidget w, String... events) {
        String ev = widgetName(w) + "/" + w.hashCode();
        if (events.length > 0)
            ev += "/" + String.join("/", events);
        return ev;
    }
}
