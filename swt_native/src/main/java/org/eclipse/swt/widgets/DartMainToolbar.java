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
        int xOffset      = align == DecorationsAlign.VLEFT ? MenuSizes.VERTICAL_MENU_BUTTON_WIDTH : 0;
        int widthReserve = (align.isVertical() ? MenuSizes.VERTICAL_MENU_BUTTON_WIDTH : 0) + csdToolbarWidth();

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

        int yOffset   = align.isVertical() ? 0 : MenuSizes.MENU_BAR_HEIGHT;
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

    private static int getMainToolbarHeight() {
        return decorationsAlign().isVertical() ? MenuSizes.HEIGHT_VERTICAL_MENU : MenuSizes.HEIGHT_HORIZONTAL_MENU;
    }
}
