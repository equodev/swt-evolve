package dev.equo.swt;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.internal.WidgetSpy;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.*;

import java.util.*;
import java.util.List;

/**
 * Default implementation simply collects all created and not disposed widgets
 */
public class IdWidgetTracker implements WidgetSpy.WidgetTracker {

    private final Map<Widget, String> nonDisposedWidgets = new LinkedHashMap<>();

    private final Set<Class<? extends Widget>> trackedTypes = new HashSet<>();
    private boolean activated;
    Listener listener;
    private Shell inspectorShell;
    private Text inspectorText;

    class TrackListener implements Listener {
        private final Display display;
        private Control lastControl;

        public TrackListener(Display display) {
            this.display = display;
        }

        @Override
        public void handleEvent(Event event) {
            Control c = display.getCursorControl();
            if (c != lastControl) {
                lastControl = c;
                if (c != null) onMouseEnterAnyWidget(c);
            }
        }

        private void onMouseEnterAnyWidget(Widget widget) {
            String id = nonDisposedWidgets.get(widget);
            updateInspector(widget, id);
        }
    }

    @Override
    public void widgetCreated(Widget widget) {
        if (widget instanceof Control c && c.getImpl()._parent() != null) {
            widgetCreated(widget, c.getImpl()._parent());
        }
    }

    void widgetCreated(Widget widget, Widget parent) {
        boolean isTracked = isTracked(widget);
        if (isTracked && parent instanceof Composite c) {
            nonDisposedWidgets.put(widget, Config.getId(widget.getClass(), c));
        }
    }

    @Override
    public void widgetDisposed(Widget widget) {
        boolean isTracked = isTracked(widget);
        if (isTracked) {
            nonDisposedWidgets.remove(widget);
        }
    }

    public void startTracking() {
        clearNonDisposedWidgets();
        WidgetSpy.getInstance().setWidgetTracker(this);

        Display display = Display.getCurrent();
        display.addFilter(SWT.KeyDown, e -> {
            if ((e.stateMask & SWT.CTRL) != 0 && (e.stateMask & SWT.SHIFT) != 0 && e.keyCode == '`') {
                activated = !activated;
                if (activated) {
                    activate(display);
                } else {
                    deactivate(display);
                }
            }
        });
    }

    private void activate(Display display) {
        System.out.println("++++ ID tracking activated");
        display.addFilter(SWT.MouseMove, listener = new TrackListener(display));
        openInspectorShell(display);
    }

    private void deactivate(Display display) {
        System.out.println("---- ID tracking deactivated");
        display.removeFilter(SWT.MouseMove, listener);
    }

    private void openInspectorShell(Display display) {
        if (inspectorShell != null && !inspectorShell.isDisposed()) {
            inspectorShell.setVisible(true);
            return;
        }
        inspectorShell = new Shell(display, SWT.SHELL_TRIM | SWT.ON_TOP);
        inspectorShell.setText("Widget Inspector");
        inspectorShell.setLayout(new FillLayout());
        inspectorShell.setSize(800, 600);

        inspectorText = new Text(inspectorShell, SWT.MULTI | SWT.READ_ONLY | SWT.V_SCROLL | SWT.H_SCROLL);
        inspectorText.setFont(display.getSystemFont());
        inspectorText.setText("Hover over a widget to inspect it...");

        // Position at the right side of the primary monitor
        Rectangle monitorBounds = display.getPrimaryMonitor().getBounds();
        inspectorShell.setLocation(monitorBounds.x + monitorBounds.width - inspectorShell.getSize().x, monitorBounds.y + 20);

        inspectorShell.addListener(SWT.Close, e -> {
            deactivate(e.display);
        });

        inspectorShell.open();
    }

    private void updateInspector(Widget widget, String id) {
        if (inspectorText == null || inspectorText.isDisposed()) return;

        StringBuilder sb = new StringBuilder();

        sb.append("=== Widget ===\n");
        sb.append(">>> ").append(widgetClassName(widget)).append(" <<<");
        sb.append("  ").append(widget);
        appendBoundsInfo(sb, widget);
        if (widget instanceof Composite comp) {
            appendLayoutInfo(sb, comp);
        }
        sb.append("\n");
        sb.append(id != null ? id : "(no id)").append("\n\n");

        sb.append("=== Parent Hierarchy ===\n");
        List<Composite> ancestors = new ArrayList<>();
        if (widget instanceof Control control) {
            Composite parent = control.getParent();
            while (parent != null) {
                ancestors.add(parent);
                parent = parent.getParent();
            }
        }
        Collections.reverse(ancestors);
        for (int i = 0; i < ancestors.size(); i++) {
            Composite ancestor = ancestors.get(i);
            String indent = "  ".repeat(i);
            sb.append(indent).append("- ").append(widgetClassName(ancestor));
            sb.append("  ").append(ancestor);
            appendBoundsInfo(sb, ancestor);
            appendLayoutInfo(sb, ancestor);
            sb.append("\n");
            String ancestorId = nonDisposedWidgets.get(ancestor);
            if (ancestorId != null) {
                sb.append(indent).append(ancestorId).append("\n");
            }
        }
        sb.append("\n");

        if (widget instanceof Composite composite) {
            Control[] children = composite.getChildren();
            if (children.length > 0) {
                sb.append("=== Children (").append(children.length).append(") ===\n");
                for (Control child : children) {
                    appendWidgetTree(sb, child, 0, 2);
                }
            } else {
                sb.append("=== No Children ===\n");
            }
        }

        inspectorText.setText(sb.toString());
    }

    private void appendWidgetTree(StringBuilder sb, Control widget, int depth, int maxDepth) {
        String indent = "  ".repeat(depth);
        sb.append(indent).append("- ").append(widgetClassName(widget));
        sb.append("  ").append(widget);
        appendBoundsInfo(sb, widget);
        if (widget instanceof Composite comp) {
            appendLayoutInfo(sb, comp);
        }
        sb.append("\n");

        String childId = nonDisposedWidgets.get(widget);
        if (childId != null) {
            sb.append(indent).append(childId).append("\n");
        }

        if (depth < maxDepth && widget instanceof Composite composite) {
            for (Control child : composite.getChildren()) {
                appendWidgetTree(sb, child, depth + 1, maxDepth);
            }
        }
    }

    private void appendBoundsInfo(StringBuilder sb, Widget widget) {
        if (widget instanceof Control control) {
            Rectangle bounds = control.getBounds();
            sb.append("  [").append(bounds.x).append(", ").append(bounds.y)
                    .append(" - ").append(bounds.width).append("x").append(bounds.height).append("]");
        }
    }

    private void appendLayoutInfo(StringBuilder sb, Composite composite) {
        Layout layout = composite.getLayout();
        if (layout == null) return;
        sb.append("  layout=");
        sb.append(layout);
    }

    private String widgetClassName(Widget widget) {
        String name = widget.getClass().getSimpleName();
        if (name.isEmpty()) {
            String[] parts = widget.getClass().getName().split("\\.");
            name = parts[parts.length - 1];
        }
        return name;
    }

    private void clearNonDisposedWidgets() {
        nonDisposedWidgets.clear();
    }

    public void stopTracking() {
        WidgetSpy.getInstance().setWidgetTracker(null);
    }

    public void setTrackingEnabled(boolean enabled) {
        if (enabled) {
            startTracking();
        } else {
            stopTracking();
        }
    }

    public void setTrackedTypes(List<Class<? extends Widget>> types) {
        trackedTypes.clear();
        trackedTypes.addAll(types);
    }

    private boolean isTracked(Widget widget) {
        boolean isTrackingAllTypes = trackedTypes.isEmpty();
        if (isTrackingAllTypes) {
            return true;
        }
        if (widget != null) {
            Class<? extends Widget> widgetType = widget.getClass();
            if (trackedTypes.contains(widgetType)) {
                return true;
            }
            for (Class<? extends Widget> filteredType : trackedTypes) {
                if (filteredType.isAssignableFrom(widgetType)) {
                    return true;
                }
            }
        }
        return false;
    }
}