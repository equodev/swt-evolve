package org.eclipse.swt.widgets;

import dev.equo.swt.Config;
import dev.equo.swt.ConfigFlags;
import dev.equo.swt.DecorationsAlign;
import dev.equo.swt.size.CsdSizes;
import dev.equo.swt.size.MenuSizes;
import dev.equo.swt.size.ToolbarControlsSizes;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;

public class DartMainToolbar extends DartComposite {
    private int appliedXReserve = 0;

    /** Bottom of the lowest child the trim layout actually laid out while wrapped; 0 when unwrapped. */
    private int measuredExtent = 0;

    /** Menu-strip band currently added to every child's y, so the next pass can take it back off. */
    private int appliedYOffset = 0;

    /**
     * A width no trim bar can exceed, used to ask the layout for its *unwrapped* height — the height
     * of a single row. Anything taller than that at a real width means the trim wrapped.
     */
    private static final int UNCONSTRAINED_WIDTH = 1 << 14;

    public DartMainToolbar(Composite parent, int style, Composite composite) {
        super(parent, style, composite);
    }

    /**
     * The first horizontal trim is ambiguous at construction, so this class can land on the BOTTOM
     * (status) trim. Its real side is authoritative here (the layout field is populated by layout time).
     */
    private boolean isStatusSide() {
        return Config.trimSide(getApi().getParent(), getApi()) == SWT.BOTTOM;
    }

    @Override
    public Point computeSize(int wHint, int hHint, boolean changed) {
        if (isStatusSide()) return new Point(1650, 36); // behave as status bar
        // Report the height the wrapped rows actually need. Reporting a single row's height while the
        // layout wraps into several leaves the extra rows with nowhere to go: they land back on row 0
        // and every icon is drawn over the one above it.
        int wrapped = wrappedHeight(wHint, changed);
        if (wrapped <= 0) return new Point(1650, getMainToolbarHeight());
        return new Point(1650, rowsTopOffset() + Math.max(wrapped, measuredExtent));
    }

    /**
     * The height the trim layout gives one row. It is not {@link #getMainToolbarHeight()}: e4's
     * TrimBarLayout sizes a row from the trim contributions themselves, while Evolve renders the bar
     * at its own, more compact height.
     */
    private int layoutRowHeight(boolean changed) {
        if (layout == null) return getMainToolbarHeight();
        Point unwrapped = layout.computeSize(getApi(), UNCONSTRAINED_WIDTH, SWT.DEFAULT, changed);
        return unwrapped.y > 0 ? unwrapped.y : getMainToolbarHeight();
    }

    /**
     * The height the trim layout needs at {@code wHint}, or 0 when a single row fits. Rows are not
     * uniform — a wrapped row is only as tall as the contributions on it — so the count cannot be
     * derived by dividing; the layout's own total is the only trustworthy figure.
     */
    private int wrappedHeight(int wHint, boolean changed) {
        if (layout == null || wHint == SWT.DEFAULT) return 0;
        int row = layoutRowHeight(changed);
        if (row <= 0) return 0;
        // Ask about the width the layout will really get. updateLayout runs it against the bar minus
        // the reserve, so querying the full width makes this claim a single row fits across a band of
        // widths where the layout is already wrapping — the bar then stays one row tall and the
        // wrapped row falls outside it.
        int effective = wHint - widthReserve();
        if (effective <= 0) return 0;
        int needed = layout.computeSize(getApi(), effective, SWT.DEFAULT, changed).y;
        return needed > row ? needed : 0;
    }

    /** Width the bar keeps for itself, so the trim layout never lays out under the menu/CSD controls. */
    private int widthReserve() {
        return menuButtonWidth() + csdToolbarWidth() + optionalControlsWidth();
    }

    private int menuButtonWidth() {
        return decorationsAlign().isVertical() && hasMenuBarItems()
                ? MenuSizes.VERTICAL_MENU_BUTTON_WIDTH : 0;
    }

    /**
     * The theme palette / scaling controls render at the trailing end of the bar on the Flutter side
     * only. Without a reserve the trim hands that strip to a contribution and both draw there.
     */
    private static int optionalControlsWidth() {
        ConfigFlags flags = Config.getConfigFlags();
        if (flags == null) return 0;
        return flags.show_theme_color_palette || flags.show_scaling_control
                ? ToolbarControlsSizes.COLLAPSED_WIDTH : 0;
    }

    /** Vertical space above the first row: the menu strip sits there when there is one. */
    private int rowsTopOffset() {
        return hasMenuStrip() ? MenuSizes.MENU_BAR_HEIGHT : 0;
    }

    @Override
    public void updateLayout(boolean all) {
        if (layout == null || isStatusSide()) {
            super.updateLayout(all);
            return;
        }
        getApi().state |= LAYOUT_CHANGED;

        DecorationsAlign align = decorationsAlign();
        int xOffset      = align == DecorationsAlign.VLEFT ? menuButtonWidth() : 0;
        int widthReserve = widthReserve();

        // Put the children back in the trim layout's own coordinates before handing them to it: it
        // reads their current bounds, and both the x reserve and the menu-strip offset below are
        // ours, not its. Re-applying them on top of themselves would drift a little every pass.
        if (appliedXReserve > 0 || appliedYOffset > 0) {
            for (Control child : _getChildren()) {
                if (child == null) continue;
                org.eclipse.swt.graphics.Rectangle cb = child.getBounds();
                child.setBounds(cb.x - appliedXReserve, cb.y - appliedYOffset, cb.width, cb.height);
            }
            appliedXReserve = 0;
            appliedYOffset = 0;
        }

        if (widthReserve > 0 && bounds.width > widthReserve) bounds.width -= widthReserve;
        super.updateLayout(all);
        if (widthReserve > 0) bounds.width += widthReserve;

        int yOffset   = rowsTopOffset();
        int rowHeight = getMainToolbarHeight() - yOffset;
        // The trim wrapped when two children sit on rows that do not vertically overlap. Read that
        // off the geometry rather than off a row height queried from the layout, whose answer is
        // cached: a stale one makes this disagree with computeSize, and the bar then reserves room
        // for the rows while this pass squeezes every child back onto the first. Empty children are
        // placeholders the trim keeps around — their zero-height span overlaps nothing, so counting
        // them would report a wrap on every single-row layout.
        java.util.List<org.eclipse.swt.graphics.Rectangle> laid = new java.util.ArrayList<>();
        for (Control child : _getChildren()) {
            if (child == null) continue;
            org.eclipse.swt.graphics.Rectangle cb = child.getBounds();
            if (cb.width > 0 && cb.height > 0) laid.add(cb);
        }
        boolean wrapped = false;
        int extent = 0;
        outer:
        for (int i = 0; i < laid.size(); i++) {
            org.eclipse.swt.graphics.Rectangle a = laid.get(i);
            for (int j = i + 1; j < laid.size(); j++) {
                org.eclipse.swt.graphics.Rectangle b = laid.get(j);
                if (a.y >= b.y + b.height || b.y >= a.y + a.height) {
                    wrapped = true;
                    break outer;
                }
            }
        }
        if (wrapped) {
            for (org.eclipse.swt.graphics.Rectangle cb : laid) {
                if (cb.y + cb.height > extent) extent = cb.y + cb.height;
            }
        }
        // The trim layout under-reports its own wrapped height (it sizes the last row from one
        // contribution, then puts a taller one on it), which clips the bottom row. What it actually
        // laid out is the only reliable figure, so remember it and floor computeSize with it.
        measuredExtent = wrapped ? extent : 0;
        for (Control child : _getChildren()) {
            if (child == null) continue;
            org.eclipse.swt.graphics.Rectangle cb = child.getBounds();
            // Wrapped rows are not uniform in height, so there is no stride to map them onto Evolve's
            // compact row: keep the geometry the trim layout produced, which is what the native
            // toolbar shows too. It is produced in the layout's own coordinates though, which start
            // at the top of the bar — the menu strip's band has to be added back, exactly as the
            // unwrapped branch does, or the rows are drawn over the strip and clipped.
            if (wrapped) {
                if (xOffset != 0 || yOffset != 0)
                    child.setBounds(cb.x + xOffset, cb.y + yOffset, cb.width, cb.height);
                continue;
            }
            int h = Math.min(cb.height, rowHeight);
            int y = yOffset + Math.max(0, (rowHeight - h) / 2);
            if (xOffset != 0 || cb.y != y || cb.height != h)
                child.setBounds(cb.x + xOffset, y, cb.width, h);
        }
        appliedXReserve = xOffset;
        appliedYOffset = wrapped ? yOffset : 0;
    }

    private static DecorationsAlign decorationsAlign() {
        ConfigFlags flags = Config.getConfigFlags();
        return flags != null && flags.decorations_align != null ? flags.decorations_align : DecorationsAlign.HLEFT;
    }

    private static int csdToolbarWidth() {
        ConfigFlags flags = Config.getConfigFlags();
        if (flags == null) return 0;
        String placement = flags.csd_placement;
        if (placement != null && !"toolbar".equals(placement.trim().toLowerCase())) return 0;
        String os = flags.csd_os != null ? flags.csd_os.trim().toLowerCase() : "linux";
        switch (os) {
            case "mac":     return CsdSizes.CSD_WIDTH_MAC;
            case "windows": return CsdSizes.CSD_WIDTH_WINDOWS;
            default:        return CsdSizes.CSD_WIDTH_LINUX;
        }
    }

    private int getMainToolbarHeight() {
        return hasMenuStrip() ? MenuSizes.HEIGHT_HORIZONTAL_MENU : MenuSizes.HEIGHT_VERTICAL_MENU;
    }

    /**
     * Whether the menu bar is drawn as its own strip above the toolbar row. Only a horizontally
     * aligned menu bar with something in it is: the height it adds and the offset it pushes the
     * toolbar row down by must agree, so both read this.
     */
    private boolean hasMenuStrip() {
        return !decorationsAlign().isVertical() && hasMenuBarItems();
    }

    /**
     * An empty menu bar is not rendered at all (see {@code DecorationsMenuData.hasItems} on the
     * Flutter side), so it must not reserve layout space here either: no button width for a
     * vertical alignment, no menu strip height for a horizontal one. The application menu counts
     * too: the client draws it ahead of the Shell's own menus, so it fills a bar that is otherwise
     * empty.
     */
    private boolean hasMenuBarItems() {
        Shell shell = getApi().getShell();
        if (shell == null || shell.isDisposed()) return false;
        if (hasApplicationMenu(shell)) return true;
        Menu bar = shell.getMenuBar();
        return bar != null && !bar.isDisposed() && bar.getItemCount() > 0;
    }

    /**
     * Only a Dart-backed application menu is drawn by the client. The embedded backend has one too
     * — the real Cocoa menu — but that one lives in the system menu bar and reserves nothing here.
     */
    private static boolean hasApplicationMenu(Shell shell) {
        Display display = shell.getDisplay();
        if (display == null || display.isDisposed()) return false;
        Menu appMenu = display.getSystemMenu();
        return appMenu != null && !appMenu.isDisposed()
                && appMenu.getImpl() instanceof DartMenu && appMenu.getItemCount() > 0;
    }
}
