package dev.equo.swt;

import org.eclipse.swt.internal.DPIUtil;

/**
 * {@code swt.autoScale=integer} on a 150 % monitor, the default on the oldest supported SWT: SWT
 * draws at 100, so one SWT unit takes two thirds of a logical pixel.
 */
public final class IntegerOn150 {

    private IntegerOn150() {
    }

    /** Runs {@code body} at that scale, and restores both zooms afterwards. */
    public static void run(Runnable body) {
        int savedZoom = DPIUtil.getDeviceZoom();
        try {
            AutoScalePolicy.runWith("integer", () -> {
                DPIUtil.setDeviceZoom(150);
                Config.setClientDeviceZoom(150);
                body.run();
            });
        } finally {
            DPIUtil.setDeviceZoom(savedZoom);
            Config.setClientDeviceZoom(100);
        }
    }
}
