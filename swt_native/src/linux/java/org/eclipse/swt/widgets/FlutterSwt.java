package org.eclipse.swt.widgets;

import com.equo.comm.api.ICommService;
import org.eclipse.swt.values.WidgetValue;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class FlutterSwt {

    public static enum ExpandPolicy {
        FOLLOW_H_PARENT(0), FOLLOW_W_PARENT(1), FOLLOW_PARENT(2);

        private final int value;

        ExpandPolicy(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }

    public static Serializer SERIALIZER = new Serializer();
    public static FlutterClient CLIENT = null;
    static {
        System.load(
                "/home/elias/Documents/Equo/swt-flutter/flutter-lib/build/linux/x64/debug/runner/libflutter_library.so");
        CLIENT = new FlutterClient();
        CLIENT.createComm();
    }

    public static native long InitializeFlutterWindow(long hwnd, int port, long widgetId, String widgetName, int width,
                                                      int height, int policy);

    public static native void CloseFlutterWindow(long flutterContext);

    public static native void ResizeFlutterWindow(long flutterContext, int width, int height);

    public static String getWidgetName(FlutterWidget w) {
        return w.getClass().getSimpleName().substring("Flutter".length());
    }

    public static String getEvent(FlutterWidget w, String... events) {
        String ev = getWidgetName(w) + "/" + w.hashCode();
        if (events.length > 0)
            ev += "/" + String.join("/", events);
        return ev;
    }

    static WidgetValue build(FlutterWidget widget) {
        WidgetValue.Builder builder = widget.builder();
//      checkAndBuildMenu(widget);

        if (widget != null && widget.children != null) {
            List<WidgetValue> childrenValues = widget.children.stream().map(FlutterSwt::build).collect(Collectors.toList());
            builder.setChildren(childrenValues);
        }
        return builder.build();
    }

    private static Set<FlutterWidget> DIRTY = new HashSet<>();

    public static void handleDirty() {
        ICommService comm = CLIENT.getComm();
        for (FlutterWidget widget : DIRTY) {
            widget.clientReady.thenRun(() -> {
                String event = getEvent(widget);
                System.out.println("will send: " + event);
                try {
                    WidgetValue value = build(widget);
                    String payload = SERIALIZER.to(value);
                    System.out.println("send: " + event + ": " + payload);
                    comm.send(event, payload);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }
        DIRTY.clear();
    }

    public static void dirty(FlutterWidget control) {
        if (control == null)
            return;
        synchronized (DIRTY) {
            DIRTY.add(control);
        }
    }

}
