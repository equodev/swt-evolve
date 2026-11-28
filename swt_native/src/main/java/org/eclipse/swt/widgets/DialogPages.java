package org.eclipse.swt.widgets;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Places a page (a dialog {@link DialogPresentation} presents as {@link DialogPresentation#PAGE}) over
 * the workbench's editor area, under the tab strip the client draws there, and keeps it there while
 * the area moves or resizes and whatever size the dialog asks for itself. With no editor area the
 * page is a large sheet instead: modal, as a dialog is.
 *
 * <p>The editor area is the control the e4 workbench tags with its model element, the area named
 * {@link #EDITOR_AREA}, read by reflection: e4 is not a dependency.
 */
public final class DialogPages {

    /** The widest a page is drawn: forms laid out wider than this read badly. */
    public static final int MAX_WIDTH = 1000;

    /** The height of the tab strip the client draws above the page, inside the editor area. */
    public static final int TAB_STRIP = 34;

    /** A page with no editor area to go to takes this share of its window, as a sheet. */
    public static final int LARGE_SHEET_PERCENT = 85;

    static final String EDITOR_AREA = "org.eclipse.ui.editorss";

    /** The shell data holding a placed page's editor area. */
    static final String HOST_KEY = "swt.evolve.pageHost";

    /** The tag e4's MinMaxAddon maximizes an element by (IPresentationEngine.MAXIMIZED). */
    static final String MAXIMIZED = "Maximized";
    /** The tag e4 puts on the editor area while another stack is maximized, which hides it. */
    static final String MINIMIZED = "Minimized";
    static final String MINIMIZED_BY_ZOOM = "MinimizedByZoom";

    /**
     * Editor area data: the open pages the area was maximized for. The area is restored when the last
     * of them closes; a maximize or restore the user asks for clears it, the choice being theirs then.
     */
    private static final String NEEDING_ROOM_KEY = "swt.evolve.pagesNeedingRoom";

    /** Set while a page is being placed, so the Resize that placing sends does not place it again. */
    private static final String PLACING_KEY = "swt.evolve.pagePlacing";

    private DialogPages() {
    }

    /** Places {@code page}, about to open, and keeps it placed until it is disposed. */
    static void attach(Shell page) {
        Shell window = window(page);
        Control shown = editorArea(window);
        // Hidden while another stack is maximized: the page still goes there, over the area maximized.
        boolean hidden = shown == null;
        Control host = hidden ? hiddenEditorArea(window) : shown;
        if (host == null) {
            Rectangle client = window.getClientArea();
            page.setSize(client.width * LARGE_SHEET_PERCENT / 100, client.height * LARGE_SHEET_PERCENT / 100);
            return;
        }
        page.setData(HOST_KEY, host);
        Listener follow = e -> place(page);
        int[] events = { SWT.Resize, SWT.Move };
        for (int type : events) {
            host.addListener(type, follow);
            window.addListener(type, follow);
            page.addListener(type, follow);
        }
        page.addListener(SWT.Dispose, e -> {
            for (int type : events) {
                if (!host.isDisposed()) host.removeListener(type, follow);
                if (!window.isDisposed()) window.removeListener(type, follow);
            }
            if (!host.isDisposed()) released(host, page);
        });
        if (hidden || !fits(page, host)) {
            needingRoom(host).add(page);
            setMaximized(host, true);
        }
        if (page.getImpl() instanceof DartShell) {
            dev.equo.swt.FlutterBridge.on((DartShell) page.getImpl(), dev.equo.swt.WireTable.CHANNEL_DIALOG_FRAME_MAXIMIZE,
                    e -> page.getDisplay().asyncExec(() -> {
                        if (!page.isDisposed()) toggleMaximized(page);
                    }));
        }
        place(page);
        DialogPageParts.attach(page);
    }

    /** Whether the editor area a placed page covers is maximized. */
    public static boolean isAreaMaximized(Shell page) {
        if (!isHosted(page)) return false;
        java.util.List<String> tags = areaTags((Control) page.getData(HOST_KEY));
        return tags != null && tags.contains(MAXIMIZED);
    }

    /** Maximizes or restores the editor area under {@code page}, as the user asked from the page strip. */
    public static void toggleMaximized(Shell page) {
        if (!isHosted(page)) return;
        Control host = (Control) page.getData(HOST_KEY);
        needingRoom(host).clear();
        setMaximized(host, !isAreaMaximized(page));
    }

    /** Whether {@code page}'s content fits the editor area at the size it would be placed at. */
    private static boolean fits(Shell page, Control host) {
        Rectangle area = displayBounds(host);
        int width = Math.min(area.width, MAX_WIDTH);
        org.eclipse.swt.graphics.Point wanted = page.computeSize(width, SWT.DEFAULT);
        org.eclipse.swt.graphics.Point natural = page.computeSize(SWT.DEFAULT, SWT.DEFAULT);
        return wanted.y <= area.height - TAB_STRIP && Math.min(natural.x, MAX_WIDTH) <= area.width;
    }

    private static void released(Control host, Shell page) {
        java.util.Set<Shell> needing = needingRoom(host);
        if (needing.remove(page) && needing.isEmpty()) setMaximized(host, false);
    }

    @SuppressWarnings("unchecked")
    private static java.util.Set<Shell> needingRoom(Control host) {
        Object set = host.getData(NEEDING_ROOM_KEY);
        if (!(set instanceof java.util.Set)) {
            set = new java.util.LinkedHashSet<Shell>();
            host.setData(NEEDING_ROOM_KEY, set);
        }
        return (java.util.Set<Shell>) set;
    }

    private static void setMaximized(Control host, boolean on) {
        java.util.List<String> tags = areaTags(host);
        if (tags == null || tags.contains(MAXIMIZED) == on) return;
        if (on) tags.removeAll(java.util.Arrays.asList(MINIMIZED, MINIMIZED_BY_ZOOM));
        if (on) tags.add(MAXIMIZED);
        else tags.remove(MAXIMIZED);
    }

    /**
     * The tags of what the perspective shows the editor area through (its placeholder), where e4 looks
     * for {@link #MAXIMIZED}; null when the model gives none.
     */
    @SuppressWarnings("unchecked")
    private static java.util.List<String> areaTags(Control host) {
        Object area = host.getData("modelElement");
        if (area == null) return null;
        try {
            Object shown = area.getClass().getMethod("getCurSharedRef").invoke(area);
            Object element = shown != null ? shown : area;
            Object tags = element.getClass().getMethod("getTags").invoke(element);
            return tags instanceof java.util.List ? (java.util.List<String>) tags : null;
        } catch (ReflectiveOperationException | RuntimeException e) {
            return null;
        }
    }

    /** Whether {@code shell} is a page placed over an editor area. */
    static boolean isHosted(Shell shell) {
        Object host = shell.getData(HOST_KEY);
        return host instanceof Control && !((Control) host).isDisposed();
    }

    /** The editor area a placed {@code page} covers, in display coordinates. */
    static Rectangle hostBounds(Shell page) {
        return displayBounds((Control) page.getData(HOST_KEY));
    }

    /**
     * Places {@code page} over its editor area, as tall as the area or as its content when that is taller.
     * A taller page scrolls by moving up inside the area, which the client clips it to: Java then knows
     * where every control is, for a popup or a caret. A position the client moved it to within that
     * range is the scroll; anything else is put back.
     */
    private static void place(Shell page) {
        if (page.isDisposed() || page.getData(PLACING_KEY) != null || !isHosted(page)) return;
        Rectangle area = hostBounds(page);
        int width = Math.min(area.width, MAX_WIDTH);
        int top = area.y + TAB_STRIP;
        int visible = Math.max(0, area.height - TAB_STRIP);
        int height = Math.max(visible, preferredHeight(page, width));
        boolean placed = page.getData(PLACED_KEY) != null;
        page.setData(PLACED_KEY, Boolean.TRUE);
        int scroll = placed ? Math.max(0, Math.min(height - visible, top - page.getBounds().y)) : 0;
        Rectangle wanted = new Rectangle(area.x + (area.width - width) / 2, top - scroll, width, height);
        if (!wanted.equals(page.getBounds())) {
            page.setData(PLACING_KEY, Boolean.TRUE);
            try {
                // A dialog sizes its minimum for a window of its own; a page takes the area's size.
                page.setMinimumSize(0, 0);
                page.setBounds(wanted);
            } finally {
                page.setData(PLACING_KEY, null);
            }
        }
        markChanged(page, VShell.PAGE_HOST, hostBounds(page));
        markChanged(page, VShell.PAGE_STRIP, TAB_STRIP);
        markChanged(page, VShell.PAGE_MAXIMIZED, isAreaMaximized(page));
    }

    /** The shell data holding the value each page property was last sent with, by its key. */
    private static final String SENT_KEY = "swt.evolve.pageSent";

    /** Marks the page property {@code key} dirty when {@code now} is not the value it was last sent with. */
    static void markChanged(Shell page, int key, Object now) {
        if (!(page.getImpl() instanceof DartShell)) return;
        @SuppressWarnings("unchecked")
        Map<Integer, Object> sent = (Map<Integer, Object>) page.getData(SENT_KEY);
        if (sent == null) page.setData(SENT_KEY, sent = new HashMap<>());
        if (sent.containsKey(key) && Objects.deepEquals(sent.get(key), now)) return;
        sent.put(key, now);
        ((DartShell) page.getImpl()).getValue().markDirty(key);
    }

    /** Set once a page has been placed: until then its position is the dialog's own, not a scroll. */
    private static final String PLACED_KEY = "swt.evolve.pagePlaced";

    /** The shell data caching a page's preferred height, by the width it was measured at. */
    private static final String PREFERRED_KEY = "swt.evolve.pagePreferred";

    private static int preferredHeight(Shell page, int width) {
        Object cached = page.getData(PREFERRED_KEY);
        if (cached instanceof int[] && ((int[]) cached)[0] == width) return ((int[]) cached)[1];
        int height = page.computeSize(width, SWT.DEFAULT).y;
        page.setData(PREFERRED_KEY, new int[] { width, height });
        return height;
    }

    /** Forgets a page's measured height: its content changed (another wizard page, say). */
    static void contentChanged(Shell page) {
        page.setData(PREFERRED_KEY, null);
        place(page);
    }

    private static Rectangle displayBounds(Control control) {
        Point at = control.toDisplay(0, 0);
        Point size = control.getSize();
        return new Rectangle(at.x, at.y, size.x, size.y);
    }

    /** The outermost shell {@code shell} belongs to: the window its dialogs are drawn in. */
    static Shell window(Shell shell) {
        Shell window = shell;
        while (window.getParent() instanceof Shell) window = (Shell) window.getParent();
        return window;
    }

    /** The editor area shown under {@code composite}. */
    private static Control editorArea(Composite composite) {
        java.util.List<Control> found = new java.util.ArrayList<>();
        editorAreas(composite, false, found);
        return found.isEmpty() ? null : found.get(0);
    }

    private static void editorAreas(Composite composite, boolean hidden, java.util.List<Control> found) {
        for (Control child : composite.getChildren()) {
            if ((!hidden && !child.getVisible()) || child instanceof Shell) continue;
            if (EDITOR_AREA.equals(elementId(child.getData("modelElement")))) found.add(child);
            if (child instanceof Composite) editorAreas((Composite) child, hidden, found);
        }
    }

    /**
     * The editor area while it is hidden: e4 keeps it in the window, or moves it to a hidden shell of its
     * own while another stack is maximized. Of its controls, the one shown through the perspective's
     * placeholder, whose tags maximize it.
     */
    private static Control hiddenEditorArea(Shell window) {
        java.util.List<Control> found = new java.util.ArrayList<>();
        editorAreas(window, true, found);
        for (Shell shell : window.getDisplay().getShells()) {
            if (shell != window && shell.getParent() == null) editorAreas(shell, true, found);
        }
        for (Control area : found) {
            if (sharedRef(area) != null) return area;
        }
        return found.isEmpty() ? null : found.get(0);
    }

    private static Object sharedRef(Control area) {
        Object model = area.getData("modelElement");
        try {
            return model == null ? null : model.getClass().getMethod("getCurSharedRef").invoke(model);
        } catch (ReflectiveOperationException | RuntimeException e) {
            return null;
        }
    }

    private static String elementId(Object modelElement) {
        if (modelElement == null) return null;
        try {
            Object id = modelElement.getClass().getMethod("getElementId").invoke(modelElement);
            return id instanceof String ? (String) id : null;
        } catch (ReflectiveOperationException | RuntimeException e) {
            return null;
        }
    }
}
