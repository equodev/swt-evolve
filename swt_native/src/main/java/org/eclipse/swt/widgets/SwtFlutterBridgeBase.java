package org.eclipse.swt.widgets;

import dev.equo.swt.FlutterBridge;
import dev.equo.swt.FlutterLibraryLoader;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.*;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.internal.Platform;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;

public abstract class SwtFlutterBridgeBase extends FlutterBridge {
    protected final DartWidget forWidget;
    protected long context;

    // Cached theme to avoid reading preferences file multiple times
    private static String cachedTheme = null;

    static {
        FlutterLibraryLoader.initialize();
    }

    public SwtFlutterBridgeBase(DartWidget widget) {
        this.forWidget = widget;
    }

    public static SwtFlutterBridge of(DartWidget widget) {
        if (widget instanceof DartControl dartControl && (dartControl.parent.getImpl() instanceof SwtComposite
                || (dartControl.getApi().getClass().getName().endsWith("ContributedPartRenderer$1") && dartControl.parent.getImpl() instanceof DartCTabFolder))) {
//            SwtComposite parentComposite = new SwtComposite(dartControl.parent, SWT.NONE, null);
            SwtFlutterBridge bridge = new SwtFlutterBridge(widget);
            widget.bridge = bridge;
            bridge.initFlutterView(dartControl.parent, dartControl);
            if (widget instanceof DartCTabFolder t) { // workaround
                if (!Platform.PLATFORM.equals("win32")) {
                    t.addSelectionListener(SelectionListener.widgetSelectedAdapter(e -> {
                        Rectangle bounds = t.getBounds();
                        bounds.height = bounds.height + 1;
                        bridge.setBounds(t, bounds);
                    }));
                }
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
            System.out.println("Cached theme " + cachedTheme);
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

    void initFlutterView(Composite parent, DartControl control) {
        super.onReady(control, Void.class);

        String theme = detectTheme();
        System.out.println("Final detected theme: " + theme);

        Color backgroundColor = parent.getShell().getBackground();
        System.out.println("Color from Shell: " + backgroundColor);

        if (backgroundColor == null) {
            backgroundColor = parent.getDisplay().getSystemColor(SWT.COLOR_WIDGET_BACKGROUND);
        }
        int backgroundColorInt = (backgroundColor.getRed() << 16) | (backgroundColor.getGreen() << 8) | backgroundColor.getBlue();

        // Get parent background color for the Button
        Color parentBackgroundColor = parent.getBackground();
        System.out.println("Color from parent: " + parentBackgroundColor);

        if (parentBackgroundColor == null) {
            parentBackgroundColor = backgroundColor; // use shell color as fallback
        }
        int parentBackgroundColorInt = (parentBackgroundColor.getRed() << 16) | (parentBackgroundColor.getGreen() << 8) | parentBackgroundColor.getBlue();

        context = InitializeFlutterWindow(client.getPort(), getHandle(parent), id(control), widgetName(control), theme, backgroundColorInt, parentBackgroundColorInt);

        long view = GetView(context);
        setHandle(control, view);
    }

    protected abstract long getHandle(Control control);

    protected abstract void setHandle(DartControl control, long view);

    @Override
    public void destroy(DartWidget control) {
        if (control instanceof DartControl dartControl && control == forWidget) {
            super.destroy(control);
            // Dispose Flutter context FIRST to handle view disconnection
            Dispose(context);
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
                if (folder._onBottom()) {
                    SetBounds(context, bounds.x, bounds.y, bounds.width, bounds.height, 0, bounds.height - 28, bounds.width, 28);
                } else {
                    SetBounds(context, bounds.x, bounds.y, bounds.width, bounds.height, 0, 0, bounds.width, 28);
                }

            } else {
                SetBounds(context, bounds.x, bounds.y, bounds.width, bounds.height,
                        bounds.x, bounds.y, bounds.width, bounds.height);
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

    @Override
    public abstract Object container(DartComposite parent);

    public abstract void reparent(DartControl control, Composite newParent);

    protected static native long InitializeFlutterWindow(int port, long parentHandle, long widgetId, String widgetName, String theme, int backgroundColor, int parentBackgroundColor);

    protected static native long Dispose(long context);

    static native long SetBounds(long context, int x, int y, int w, int h, int vx, int vy, int vw, int vh);

    static native long GetView(long context);

    protected static native int PumpMessages(int maxMessages);
}

