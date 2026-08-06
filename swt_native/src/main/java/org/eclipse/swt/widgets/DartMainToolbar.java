package org.eclipse.swt.widgets;

import dev.equo.swt.Config;
import dev.equo.swt.ConfigFlags;
import dev.equo.swt.DecorationsAlign;
import dev.equo.swt.size.CsdSizes;
import dev.equo.swt.size.MenuSizes;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;

public class DartMainToolbar extends DartComposite {
    private int appliedXReserve = 0;

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
        return new Point(1650, getMainToolbarHeight());
    }

    @Override
    public void updateLayout(boolean all) {
        if (layout == null || isStatusSide()) {
            super.updateLayout(all);
            return;
        }
        getApi().state |= LAYOUT_CHANGED;

        DecorationsAlign align = decorationsAlign();
        int menuButton   = align.isVertical() && hasMenuBarItems() ? MenuSizes.VERTICAL_MENU_BUTTON_WIDTH : 0;
        int xOffset      = align == DecorationsAlign.VLEFT ? menuButton : 0;
        int widthReserve = menuButton + csdToolbarWidth();

        if (appliedXReserve > 0) {
            for (Control child : _getChildren()) {
                if (child == null) continue;
                org.eclipse.swt.graphics.Rectangle cb = child.getBounds();
                child.setBounds(cb.x - appliedXReserve, cb.y, cb.width, cb.height);
            }
            appliedXReserve = 0;
        }

        if (widthReserve > 0 && bounds.width > widthReserve) bounds.width -= widthReserve;
        super.updateLayout(all);
        if (widthReserve > 0) bounds.width += widthReserve;

        int yOffset   = hasMenuStrip() ? MenuSizes.MENU_BAR_HEIGHT : 0;
        int rowHeight = getMainToolbarHeight() - yOffset;
        for (Control child : _getChildren()) {
            if (child == null) continue;
            org.eclipse.swt.graphics.Rectangle cb = child.getBounds();
            int h = Math.min(cb.height, rowHeight);
            int y = yOffset + Math.max(0, (rowHeight - h) / 2);
            if (xOffset != 0 || cb.y != y || cb.height != h)
                child.setBounds(cb.x + xOffset, y, cb.width, h);
        }
        appliedXReserve = xOffset;
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
     * vertical alignment, no menu strip height for a horizontal one.
     */
    private boolean hasMenuBarItems() {
        Shell shell = getApi().getShell();
        if (shell == null || shell.isDisposed()) return false;
        Menu bar = shell.getMenuBar();
        return bar != null && !bar.isDisposed() && bar.getItemCount() > 0;
    }
}
