package org.eclipse.swt.widgets;

import dev.equo.swt.Config;
import dev.equo.swt.FlutterBridge;
import dev.equo.swt.FlutterLibraryLoader;
import dev.equo.swt.GCImageDrawer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.*;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;

import java.util.function.Consumer;

import java.io.File;
import java.io.FileInputStream;
import java.util.*;

public abstract class SwtFlutterBridgeBase extends FlutterBridge {
    protected final DartWidget forWidget;
    protected long context;

    // Cached theme to avoid reading preferences file multiple times
    private static String cachedTheme = null;

    static {
        if (!"false".equals(System.getProperty("dev.equo.swt.loadLibrary")))
            FlutterLibraryLoader.initialize();
    }

    public SwtFlutterBridgeBase(DartWidget widget) {
        this.forWidget = widget;
    }

    @Override
    protected DartWidget forWidget() {
        return forWidget;
    }

    public static SwtFlutterBridge of(DartWidget widget) {
        if (widget instanceof DartControl dartControl && dartControl.parent.getImpl() instanceof SwtComposite) {
            SwtFlutterBridge bridge = new SwtFlutterBridge(widget);
            widget.bridge = bridge;
            bridge.initFlutterView(dartControl.parent, dartControl);
            if (widget instanceof DartCTabFolder t) { // workaround
                t.addSelectionListener(SelectionListener.widgetSelectedAdapter(e -> {
                    Rectangle bounds = t.getBounds();
                    bridge.setBoundsCTabFolder(t, bounds, true);
                }));
            }
            return bridge;
        }
        if (widget instanceof DartControl dartControl && dartControl.parent.getImpl() instanceof DartComposite c) {
            FlutterBridge bridge = c.getBridge();
            return (SwtFlutterBridge) bridge;
        }
        return null;
    }

    private String readThemeFromPreferences() {
        try {
            // Get workspace location from OSGi system property
            String workspaceLocation = System.getProperty("osgi.instance.area");
            if (workspaceLocation == null) {
                return null;
            }

            // Remove "file:" prefix if present
            if (workspaceLocation.startsWith("file:")) {
                workspaceLocation = workspaceLocation.substring(5);
            }

            // Build path to theme preferences file
            File prefsFile = new File(workspaceLocation,
                ".metadata/.plugins/org.eclipse.core.runtime/.settings/org.eclipse.e4.ui.css.swt.theme.prefs");

            if (!prefsFile.exists()) {
                System.out.println("Theme preferences file not found: " + prefsFile.getAbsolutePath());
                return null;
            }

            // Read and parse the properties file
            Properties props = new Properties();
            try (FileInputStream fis = new FileInputStream(prefsFile)) {
                props.load(fis);
            }

            String themeId = props.getProperty("themeid");
            if (themeId == null) {
                System.out.println("No themeid found in preferences");
                return null;
            }

            System.out.println("Eclipse theme ID from preferences: " + themeId);

            // Determine if it's dark or light based on theme ID
            if (themeId.toLowerCase().contains("dark")) {
                return "dark";
            } else if (themeId.toLowerCase().contains("light") ||
                       themeId.toLowerCase().contains("default") || themeId.toLowerCase().contains("classic")) {
                return "light";
            }

            return null;

        } catch (Exception e) {
            System.err.println("Unexpected error reading theme: " + e.getMessage());
            return null;
        }
    }

    private String detectTheme() {
        if (cachedTheme != null) {
//            System.out.println("Cached theme " + cachedTheme);
            return cachedTheme;
        }

        String theme = readThemeFromPreferences();
        if (theme != null) {
            cachedTheme = theme;
            return theme;
        }
        // Fallback to OS theme
        boolean isDark = Display.isSystemDarkTheme();
        theme = isDark ? "dark" : "light";
        System.out.println("Using OS theme (fallback): " + theme);
        cachedTheme = theme;
        return theme;
    }

    /**
     * Creates a headless Flutter view for a GC whose drawable is an Image.
     * Analogous to how DartWidget.register() calls FlutterBridge.of(this).
     *
     * Usage in DartGC.init() when drawable is an Image:
     *   this.bridge = SwtFlutterBridgeBase.of(this);
     */
    public static GCImageDrawer of(long gcId, Image dartImage, Consumer<String> onImageResult) {
        GCImageDrawer drawer = new GCImageDrawer();
        drawer.initFlutterView(gcId, dartImage, onImageResult);
        return drawer;
    }

    void initFlutterView(Composite parent, DartControl control) {
        super.onReady(control, Void.class);

        String theme = detectTheme();
//        System.out.println("Final detected theme: " + theme);

        Color backgroundColor = parent.getShell().getBackground();
//        System.out.println("Color from Shell: " + backgroundColor);

        if (backgroundColor == null) {
            backgroundColor = parent.getDisplay().getSystemColor(SWT.COLOR_WIDGET_BACKGROUND);
        }
        int backgroundColorInt = (backgroundColor.getRed() << 16) | (backgroundColor.getGreen() << 8) | backgroundColor.getBlue();

        // Get parent background color for the Button
        Color parentBackgroundColor = parent.getBackground();
//        System.out.println("Color from parent: " + parentBackgroundColor);

        if (parentBackgroundColor == null) {
            parentBackgroundColor = backgroundColor; // use shell color as fallback
        }
        int parentBackgroundColorInt = (parentBackgroundColor.getRed() << 16) | (parentBackgroundColor.getGreen() << 8) | parentBackgroundColor.getBlue();

        long parentHandle = getParentHandleForInit(parent, control);
        context = initializeFlutterWindow(client.getPort(), parentHandle, id(control), widgetName(control), theme, backgroundColorInt, parentBackgroundColorInt);

        long view = getView(context);
        setHandle(control, view);
        if (parentHandle == 0) {
            postCreateLimboReparent(parent, control);
        }
    }

    protected long getParentHandleForInit(Composite parent, DartControl control) {
        return getHandle(parent);
    }

    protected void postCreateLimboReparent(Composite parent, DartControl control) {
    }

    protected abstract long getHandle(Control control);

    protected abstract void setHandle(DartControl control, long view);

    @Override
    public void destroy(DartWidget control) {
        if (control instanceof DartControl dartControl && control == forWidget) {
            super.destroy(control);
            // Dispose Flutter context FIRST to handle view disconnection
            dispose(context);
            context = 0;
            destroyHandle(dartControl);
        }
    }

    protected abstract void destroyHandle(DartControl dartControl);


    @Override
    public void setBounds(DartControl dartControl, Rectangle bounds) {
        if (dartControl.bridge != null && forWidget == dartControl) {
            System.out.println("SET BOUNDS: " + dartControl + " Rectangle {" + bounds.x + ", " + bounds.y + ", " + bounds.width + ", " + bounds.height + "}");
            if (dartControl instanceof DartCTabFolder folder) {
                setBoundsCTabFolder(folder, bounds, false);
            } else {
                setBounds(context, bounds.x, bounds.y, bounds.width, bounds.height,
                        0, 0, bounds.width, bounds.height);
            }

            //dartControl.resized();
            //dartControl.sendEvent(SWT.Resize);
            if (dartControl instanceof DartComposite c && c.layout != null) {
                c.markLayout(false, false);
                c.updateLayout(false);
            }
        } else {
            if (dartControl instanceof DartComposite c && c.layout != null) {
                c.markLayout(false, false);
                c.updateLayout(false);
            }
        }
    }

    public void setBoundsCTabFolder(DartCTabFolder folder, Rectangle bounds, boolean viewOnly) {
        if (folder.getSelection() != null && folder.getSelection().getControl() != null && folder.getSelection().getControl().getImpl() instanceof SwtControl) {
            if (folder._onBottom()) {
                setBounds(context, bounds.x, bounds.y, bounds.width, bounds.height, 0, bounds.height - 32, bounds.width, 32);
            } else {
                setBounds(context, bounds.x, bounds.y, bounds.width, bounds.height, 0, 0, bounds.width, 32);
            }
        } else {
            setBounds(context, bounds.x, bounds.y, bounds.width, bounds.height, 0, 0, bounds.width, bounds.height);
        }
    }

    @Override
    public abstract Object container(DartComposite parent);

    public abstract void reparent(DartControl control, Composite newParent);

    protected static long initializeFlutterWindow(int port, long parentHandle, long widgetId, String widgetName, String theme, int backgroundColor, int parentBackgroundColor) {
        return InitializeFlutterWindow(port, parentHandle, widgetId, widgetName, theme, backgroundColor, parentBackgroundColor);
    }
    private static native long InitializeFlutterWindow(int port, long parentHandle, long widgetId, String widgetName, String theme, int backgroundColor, int parentBackgroundColor);

    protected static long dispose(long context) {
        return Dispose(context);
    }
    private static native long Dispose(long context);

    static long setBounds(long context, int x, int y, int w, int h, int vx, int vy, int vw, int vh) {
        return SetBounds(context, x, y, w, h, vx, vy, vw, vh);
    }
    private static native long SetBounds(long context, int x, int y, int w, int h, int vx, int vy, int vw, int vh);

    static long getView(long context) {
        return GetView(context);
    }
    private static native long GetView(long context);

    protected static int pumpMessages(int maxMessages) {
        return PumpMessages(maxMessages);
    }
    private static native int PumpMessages(int maxMessages);
}

