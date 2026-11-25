package org.eclipse.swt.widgets;


/**
 * The popup menus and tooltips a shell must draw itself, sent on {@code Shell/{id}/Popups}.
 *
 * <p>Both belong to the Display, and the Display's own client is the only one that draws
 * {@code VDisplay}'s. A shell in a window of its own is a second client, rooted at that shell
 * and never listening on {@code Display/{id}}, so a popup opened over it would be drawn by the
 * application's window instead — the wrong window entirely, not merely the wrong place in the right
 * one. This carries them to the client that owns the control they were opened for; {@code VDisplay}
 * leaves out the ones sent here, so exactly one client draws each.
 *
 * <p>The popups the Display's own client draws are sent the same way, as a {@code DisplayPopups}
 * record under the Display's id; its tooltips are in its own record.
 *
 * <p>Write-only, like every value object on this side: the client renders it and never sends one
 * back, so the reader is a stub.
 */
public class VShellPopups {

    public long shellId;
    public Menu[] popups;
    public ToolTip[] tooltips;

    /** The properties the record describes, numbered as the Display's of the same name. */
    public static final int[] WIRE_KEYS = { VDisplay.POPUPS, VDisplay.TOOLTIPS };

    /** Writes one of {@link #WIRE_KEYS}, as a widget's keyed writer does. */
    public boolean writeWire(dev.equo.swt.WidgetWire w, int key) {
        if (key == VDisplay.POPUPS) {
            w.refs(popups);
            return true;
        }
        if (key == VDisplay.TOOLTIPS) {
            w.refs(tooltips);
            return true;
        }
        return false;
    }

}
