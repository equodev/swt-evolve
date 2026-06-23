package org.eclipse.swt.widgets;

import dev.equo.swt.Config;
import dev.equo.swt.ConfigFlags;
import org.eclipse.swt.graphics.Point;

public class DartMainToolbar extends DartComposite {
    private static final int HEIGHT_HORIZONTAL_MENU = 70;
    private static final int HEIGHT_VERTICAL_MENU = 36;
    private static final int WIDTH_VERTICAL_MENU_BUTTON = 48;

    public DartMainToolbar(Composite parent, int style, Composite composite) {
        super(parent, style, composite);
    }

    @Override
    public Point computeSize(int wHint, int hHint, boolean changed) {
        return new Point(1650, getMainToolbarHeight());
    }

    @Override
    public void updateLayout(boolean all) {
        if (layout == null) {
            super.updateLayout(all);
            return;
        }
        getApi().state |= LAYOUT_CHANGED;

        int reserve = verticalMenuReservedWidth();
        if (reserve > 0 && bounds.width > reserve) {
            bounds.width -= reserve;
            super.updateLayout(all);
            bounds.width += reserve;
        } else {
            super.updateLayout(all);
        }
    }

    private static int verticalMenuReservedWidth() {
        ConfigFlags flags = Config.getConfigFlags();
        String align = flags != null ? flags.decorations_align : null;
        if (align == null) return 0;
        String mode = align.trim().toLowerCase();
        return ("vleft".equals(mode) || "vright".equals(mode)) ? WIDTH_VERTICAL_MENU_BUTTON : 0;
    }

    private static int getMainToolbarHeight() {
        ConfigFlags flags = Config.getConfigFlags();
        String align = flags != null ? flags.decorations_align : null;
        if (align == null) return HEIGHT_HORIZONTAL_MENU;
        String mode = align.trim().toLowerCase();
        return ("vleft".equals(mode) || "vright".equals(mode)) ? HEIGHT_VERTICAL_MENU : HEIGHT_HORIZONTAL_MENU;
    }
}
