package dev.equo.swt;

import org.eclipse.swt.custom.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.widgets.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import static dev.equo.swt.Config.getConfigFlags;

public abstract class FlutterBridge {
    private static final String DEV_EQU_SWT_NEW = "dev.equ.swt.new";
    protected static final FlutterClient client;
    private static final Serializer serializer = new Serializer();
    private static final Set<Object> dirty = new HashSet<>();
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

    public static CompletableFuture<Void> update() {
        if (dirty.isEmpty()) {
            return CompletableFuture.completedFuture(null);
        }
        
        List<CompletableFuture<Void>> futures = new ArrayList<>();
        for (Object widget : dirty) {
            if (isDisposed(widget)) break;
            CompletableFuture<Void> future = getBridge(widget).clientReady.thenRun(() -> {
                try {
                    if (widget instanceof DartStyledText){
                        getDisplay(widget).asyncExec(() -> {
                            StyledTextBridge.drawStyledText((DartStyledText) widget,
                                ((DartStyledText) widget).getLocation().x,
                                ((DartStyledText) widget).getLocation().y,
                                ((DartStyledText) widget).getCaret(),
                                id(widget),
                                client
                            );
                        });

                    }
                    if (!isNew(widget)) { // send with the parent
                        String event = event(widget);
                        try {
                            serializeAndSend(event, getApi(widget));
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
            futures.add(future);
        }
        dirty.clear();
        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
    }

    private static boolean isNew(Object widget) {
        if (widget instanceof DartWidget)
            return ((DartWidget) widget).getData("dev.equ.swt.new") == null;
        return false;
    }

    private static boolean isDisposed(Object w) {
        if (w instanceof DartWidget) return ((DartWidget) w).isDisposed();
        if (w instanceof DartResource) return ((DartResource) w).isDisposed();
        return true;
    }

    private static FlutterBridge getBridge(Object w) {
        if (w instanceof DartWidget) return ((DartWidget) w).getBridge();
        if (w instanceof DartResource) return ((DartResource) w).getBridge();
        return null;
    }

    private static Display getDisplay(Object w) {
        if (w instanceof DartWidget) return ((DartWidget) w).getDisplay();
        if (w instanceof DartResource) {
            Drawable drawable = ((DartGC) w)._drawable();
            DartWidget widget = null;
            if (drawable instanceof Canvas) {
                widget = (DartWidget) ((Canvas) drawable).getImpl();
            }
            return widget.getDisplay();
        }
        return null;
    }

    private static Object getApi(Object w) {
        if (w instanceof DartWidget) return ((DartWidget) w).getApi();
        if (w instanceof DartResource) return ((DartResource) w).getApi();
        return null;
    }

    private static void serializeAndSend(String eventName, Object args) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        serializer.to(args, out);
        String serialized = out.toString(StandardCharsets.UTF_8);
        System.out.println("send: " + eventName + ": " + serialized);
        client.getComm().send(eventName, serialized);
    }

    private static void setNotNew(Object control) {
        if (control instanceof DartWidget)
            ((DartWidget) control).setData(DEV_EQU_SWT_NEW, false);
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
        String eventName =  eventName(widget, event);
        client.getComm().on(eventName, p -> {
            System.out.println(eventName + ", payload:"+p);
            cb.accept(p);
        });
    }

    public static void send(DartResource resource, String event, Object args) {
        if (dirty.contains(resource)) {
            update().join();
        }
        try {
            serializeAndSend(eventName(resource, event), args);
        } catch (IOException e) {
            e.printStackTrace();
        }
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

    public void dirty(DartResource resource) {
        if (resource == null)
            return;
        synchronized (dirty) {
            dirty.add(resource);
        }
    }

    public void dirty(DartWidget widget) {
        if (widget == null)
            return;
        synchronized (dirty) {
            dirty.add(widget);
        }
    }

    public static String widgetName(Object w) {
        if (w instanceof DartWidget) {
            return w.getClass().getSimpleName().substring(4);
        }
        if (w instanceof DartResource) {
            return w.getClass().getSimpleName().substring(4);
        }
        return null;
    }

    public static String eventName(Object w, String event) {
        return widgetName(w) + "/" + id(w) + "/" + event;
    }

    public static String event(Object w, String... events) {
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

    public boolean hasFocus(DartControl dartControl) {
        return false;
    }

    public Object container(DartComposite parent) {
        return null;
    }

    public Point getWindowOrigin(DartControl control) {
        return new Point(0, 0);
    }

    public void setCursor(DartControl control, long cursor) {
    }

    public static long id(Object w) {
        if (w instanceof DartWidget)
            return ((DartWidget) w).getApi().hashCode();
        if (w instanceof DartResource) {
            return ((DartResource) w).getApi().hashCode();
        }
        return 0;
    }

    static long id(Widget w) {
        return w.hashCode();
    }

    public void sendSwtEvolveProperties() {
        ConfigFlags properties = getConfigFlags();
        System.out.println("will send: " + properties);
        try {
            serializeAndSend("swt.evolve.properties", properties);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}

