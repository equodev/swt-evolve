package dev.equo.swt.awt;

import org.junit.jupiter.api.Test;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.EventQueue;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.util.concurrent.atomic.AtomicInteger;

import javax.swing.JApplet;
import javax.swing.JComponent;
import javax.swing.JPanel;

import sun.swing.JLightweightFrame;
import sun.swing.LightweightContent;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeFalse;

/**
 * Embedded Swing content can draw outside its paint cycle, straight through
 * {@code Component.getGraphics()} — the rubber-band box of an area-zoom tool, drawn in XOR mode on
 * every drag and erased by drawing it again. Nothing is repainted and the frame reports nothing, so
 * that drawing only reaches the screen if it lands in the buffer the embedding reads and the
 * embedding is told to push it.
 *
 * <p>The applications that do this wrap their Swing content in a {@code JApplet}, a heavyweight,
 * and a heavyweight's {@code getGraphics()} draws on a native surface nobody shows.
 *
 * <p>Needs a real lightweight frame, so it is skipped where AWT runs headless.
 */
@SuppressWarnings("removal")
class ImmediateModeDrawingTest {

    private static final int W = 200;
    private static final int H = 150;

    @Test
    void getGraphicsDrawingInsideAHostedAppletReachesTheFrameBufferAndIsPushed() throws Exception {
        assumeFalse(GraphicsEnvironment.isHeadless(), "a lightweight frame needs a display");
        EvolveSwingHost.shareFrameBuffer();

        AtomicInteger pushRequests = new AtomicInteger();
        BufferCapture content = new BufferCapture(new EvolveSwingHost.ContentRoot(pushRequests::incrementAndGet));
        JLightweightFrame[] frame = new JLightweightFrame[1];
        JPanel tile = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                g.setColor(Color.WHITE);
                g.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        EventQueue.invokeAndWait(() -> {
            frame[0] = new JLightweightFrame();
            frame[0].setContent(content);
            frame[0].getContentPane().add(content.root, BorderLayout.CENTER);
            frame[0].setSize(W, H);
            frame[0].setVisible(true);

            JApplet applet = new JApplet();
            frame[0].add(applet);
            applet.add(tile);

            frame[0].remove(applet);
            frame[0].add(frame[0].getRootPane(), BorderLayout.CENTER);
            EvolveSwingHost.hostAsLightweight(applet);
            content.root.add(applet);
        });
        try {
            EventQueue.invokeAndWait(() -> {
                frame[0].getRootPane().setBounds(0, 0, W, H);
                frame[0].getRootPane().validate();
                content.root.paintImmediately(0, 0, W, H);
            });
            int before = content.pixel(60, 40);
            int requestsBefore = pushRequests.get();

            EventQueue.invokeAndWait(() -> {
                Graphics2D g = (Graphics2D) tile.getGraphics();
                g.setColor(Color.BLACK);
                g.setXORMode(Color.WHITE);
                g.drawRect(60, 40, 50, 30);
                g.dispose();
            });

            assertEquals(0xFFFFFFFF, before);
            assertEquals(0xFF000000, content.pixel(60, 40));
            assertTrue(pushRequests.get() > requestsBefore, "no push was requested for the drawing");
        } finally {
            EventQueue.invokeAndWait(() -> frame[0].dispose());
        }
    }

    /** Holds the buffer a lightweight frame hands its content, the one the host reads frames from. */
    private static final class BufferCapture implements LightweightContent {
        final JPanel root;
        private volatile int[] buffer;
        private volatile int stride;
        private volatile double scale = 1.0;

        BufferCapture(JPanel root) {
            this.root = root;
        }

        /** The pixel at a point in the frame's own coordinates; the buffer is at the display scale. */
        int pixel(int x, int y) {
            return buffer[(int) Math.round(y * scale) * stride + (int) Math.round(x * scale)];
        }

        @Override public JComponent getComponent() { return root; }
        @Override public void paintLock() {}
        @Override public void paintUnlock() {}

        @Override
        public void imageBufferReset(int[] data, int x, int y, int width, int height,
                                     int linestride, double scaleX, double scaleY) {
            buffer = data;
            stride = linestride;
            scale = scaleX > 0 ? scaleX : 1.0;
        }

        @Override public void imageReshaped(int x, int y, int width, int height) {}
        @Override public void imageUpdated(int dirtyX, int dirtyY, int dirtyWidth, int dirtyHeight) {}
        @Override public void focusGrabbed() {}
        @Override public void focusUngrabbed() {}
        @Override public void preferredSizeChanged(int width, int height) {}
        @Override public void maximumSizeChanged(int width, int height) {}
        @Override public void minimumSizeChanged(int width, int height) {}
    }
}
