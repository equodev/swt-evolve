package org.eclipse.swt.custom;

import org.eclipse.swt.widgets.Event;

/** Strip reports as the render side sends them, for the CTabFolder tests. */
final class CTabFolderStripReports {

    private CTabFolderStripReports() {
    }

    /** Hands the folder a strip report, as the render side does after it lays out the strip. */
    static void reportsStrip(CTabFolder folder, String report) {
        Event e = new Event();
        e.text = report;
        CTabFolderHelper.handleStripLaidOut((DartCTabFolder) folder.getImpl(), e);
    }
}
