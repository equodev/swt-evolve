package dev.equo.swt;

import org.eclipse.swt.custom.*;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public abstract class FlutterBridge {
    private static final String DEV_EQU_SWT_NEW = "dev.equ.swt.new";
    protected static final FlutterClient client;
    private static final Serializer serializer = new Serializer();
    private static final Set<DartWidget> dirty = new HashSet<>();
    private static FlutterBridge bridge;

    static {
        client = new FlutterClient();
        client.createComm();
    }

    private final CompletableFuture<Boolean> clientReady = new CompletableFuture<>();

    public static FlutterBridge of(DartWidget dartControl) {
        if (bridge != null)
            return bridge;
        //if (isWeb) {}
//        if (isSwt)
        return SwtFlutterBridge.of(dartControl);
    }

    protected FlutterBridge() {
    }

    public static void update() {
        for (DartWidget widget : dirty) {
            if (widget.isDisposed()) break;
            widget.getBridge().clientReady.thenRun(() -> {
                try {
                    if (widget instanceof DartStyledText){
                        if (!isNew(widget)) { // sends StyledText widget info
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
                        }
                        StyledTextBridge.drawStyledText((DartStyledText) widget,
                                ((DartStyledText) widget).getLocation().x,
                                ((DartStyledText) widget).getLocation().y,
                                ((DartStyledText) widget).getCaret(),
                                id(widget),
                                client);

                    }
                    else if (!isNew(widget)) { // send with the parent
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
                } catch (RuntimeException e) {
                    e.printStackTrace();
                }
            });
        }
        dirty.clear();
    }


    private static boolean isNew(DartWidget widget) {
        return widget.getData("dev.equ.swt.new") == null;
    }

    private static void setNotNew(DartWidget control) {
        control.setData(DEV_EQU_SWT_NEW, false);
    }

    public static void set(FlutterBridge staticBridge) {
        bridge = staticBridge;
    }

    public static void on(DartWidget widget, String listener, String event, Consumer<Event> cb) {
        String eventName = event(widget, listener, event);
        client.getComm().on(eventName, p -> {
            System.out.println(eventName + ", payload:"+p);
            if (p != null) {
                ByteArrayInputStream in = new ByteArrayInputStream(p.getBytes(StandardCharsets.UTF_8));
                try {
                    Event ev = serializer.from(Event.class, in);
                    cb.accept(ev);
                    return;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            cb.accept(null);
        });
    }

    public static void onPayload(DartWidget widget, String event, Consumer<Object> cb) {
        String eventName =  widgetName(widget)  + "/" + id(widget) + "/" + event;
        client.getComm().on(eventName, p -> {
            System.out.println(eventName + ", payload:"+p);
            cb.accept(p);
        });
    }

    protected void onReady(DartControl control) {
        setNotNew(control);
        dirty(control);
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

    public void dirty(DartWidget widget) {
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
        String ev = widgetName(w) + "/" + id(w);
        if (events.length > 0)
            ev += "/" + String.join("/", events);
        return ev;
    }

    public abstract void destroy(DartWidget control);

    public void setBounds(DartControl dartControl, Rectangle bounds) {
    }

    public void setFocus(DartControl dartControl) {
    }

    public Object container(DartComposite parent) {
        return null;
    }

    public static long id(DartWidget w) {
        return w.getApi().hashCode();
    }

    static long id(Widget w) {
        return w.hashCode();
    }
}
