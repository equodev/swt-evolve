package org.eclipse.swt.widgets;

import org.eclipse.swt.SWT;

public class ToolItemHelper {

    /**
     * Announce the activation a native tool item click carries. A native click activates the control
     * chain it lands in, and an embedding workbench keys part activation off that — e4's StackRenderer
     * listens for SWT.Activate on a CTabFolder and activates the selected tab's part, and a view's
     * tool bar (its own buttons and the view menu) sits inside that folder. The render side reports
     * only the selection, so without this the workbench never learns which part the user is acting on
     * and every contribution resolved from the active part comes up empty.
     */
    public static void announceActivation(DartToolItem item) {
        if (item == null || item.getApi() == null || item.getApi().isDisposed())
            return;
        ToolBar parent = item.getApi().getParent();
        if (parent == null || parent.isDisposed())
            return;
        if (parent.getImpl() instanceof DartControl bar)
            ControlHelper.sendActivateToAncestors(bar, SWT.MouseDown);
    }
}
