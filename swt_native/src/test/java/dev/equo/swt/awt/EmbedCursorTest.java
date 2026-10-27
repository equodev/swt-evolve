package dev.equo.swt.awt;

import java.awt.Cursor;
import java.awt.EventQueue;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Display;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * The embedded frame has no native window, so the cursor its Swing components ask for reaches the
 * screen only if the embedding sets it on its canvas. Without that the canvas shows whatever its
 * ancestors show, and a hand or crosshair asked for by a Swing component never appears.
 *
 * <p>The canvas and display are mocks: CI runs headless. The Swing tree is real, since lightweight
 * components need no display.
 */
class EmbedCursorTest {

    // panel (300x200, default cursor) > button (at 10,10, hand) + box (at 10,100, crosshair) > label (no cursor of its own)
    private final JPanel panel = new JPanel(null);
    private final JButton button = new JButton("pan");
    private final JPanel box = new JPanel(null);
    private final JLabel label = new JLabel("map");

    private final Canvas canvas = mock(Canvas.class);
    private final Display display = mock(Display.class);
    private final Map<Integer, org.eclipse.swt.graphics.Cursor> systemCursors = new HashMap<>();
    private EmbedCursor cursor;

    @BeforeEach
    void setUp() {
        panel.setBounds(0, 0, 300, 200);
        button.setBounds(10, 10, 100, 30);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        box.setBounds(10, 100, 200, 60);
        box.setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
        label.setBounds(0, 0, 100, 30);
        box.add(label);
        panel.add(button);
        panel.add(box);

        when(display.getSystemCursor(anyInt()))
                .thenAnswer(call -> systemCursor(call.getArgument(0)));
        doAnswer(call -> {
            ((Runnable) call.getArgument(0)).run();
            return null;
        }).when(display).asyncExec(any());
        cursor = new EmbedCursor(canvas, display, panel);
    }

    private org.eclipse.swt.graphics.Cursor systemCursor(int id) {
        return systemCursors.computeIfAbsent(id, k -> mock(org.eclipse.swt.graphics.Cursor.class));
    }

    private void onEdt(Runnable r) throws Exception {
        EventQueue.invokeAndWait(r);
    }

    @Test
    void movingOverAComponentShowsItsCursor() throws Exception {
        onEdt(() -> cursor.moved(20, 20, false));

        verify(canvas).setCursor(systemCursor(SWT.CURSOR_HAND));
    }

    @Test
    void aComponentWithoutACursorShowsItsAncestors() throws Exception {
        onEdt(() -> cursor.moved(20, 110, false));

        verify(canvas).setCursor(systemCursor(SWT.CURSOR_CROSS));
    }

    @Test
    void leavingAComponentForOneWithTheDefaultCursorShowsTheArrow() throws Exception {
        // An explicit arrow, not "no cursor": that would show an ancestor's cursor instead.
        onEdt(() -> cursor.moved(20, 20, false));
        onEdt(() -> cursor.moved(250, 20, false));

        verify(canvas).setCursor(systemCursor(SWT.CURSOR_ARROW));
    }

    @Test
    void theSameCursorIsSetOnce() throws Exception {
        onEdt(() -> cursor.moved(20, 20, false));
        onEdt(() -> cursor.moved(30, 25, false));

        verify(canvas, times(1)).setCursor(any());
    }

    @Test
    void aDragKeepsTheCursorItStartedWith() throws Exception {
        onEdt(() -> cursor.moved(20, 20, false));
        onEdt(() -> cursor.moved(250, 20, true));

        verify(canvas, times(1)).setCursor(any());
        verify(canvas).setCursor(systemCursor(SWT.CURSOR_HAND));
    }

    @Test
    void aComponentChangingItsCursorShowsTheNewOne() throws Exception {
        onEdt(() -> cursor.moved(20, 20, false));
        onEdt(() -> {
            button.setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
            cursor.refresh();
        });

        verify(canvas).setCursor(systemCursor(SWT.CURSOR_SIZEALL));
    }

    @Test
    void nothingIsSetWhileThePointerIsOutsideTheCanvas() throws Exception {
        onEdt(() -> cursor.refresh());

        verify(canvas, never()).setCursor(any());
    }

    /** A custom cursor the way the toolkit keeps one: a subclass holding the image it was made from. */
    static class ImageCursor extends Cursor {
        protected Image image;

        ImageCursor(Image image) {
            super("tool");
            this.image = image;
        }
    }

    private static BufferedImage twoPixels() {
        BufferedImage image = new BufferedImage(2, 1, BufferedImage.TYPE_INT_ARGB);
        image.setRGB(0, 0, 0xFF112233);
        image.setRGB(1, 0, 0x00000000);
        return image;
    }

    @Test
    void aCustomCursorsImageIsFoundWhereTheToolkitKeepsIt() {
        BufferedImage image = twoPixels();

        assertSame(image, EmbedCursor.customImage(new ImageCursor(image)));
    }

    @Test
    void aCustomCursorsImageKeepsItsPixelsAndTransparency() {
        ImageData data = EmbedCursor.imageDataOf(twoPixels(), 2, 1);

        assertEquals(new RGB(0x11, 0x22, 0x33), data.palette.getRGB(data.getPixel(0, 0)));
        assertEquals(0xFF, data.getAlpha(0, 0));
        assertEquals(0, data.getAlpha(1, 0));
    }

    @Test
    void aCustomCursorWithoutAnImageShowsTheArrow() throws Exception {
        button.setCursor(new Cursor("no image") { });
        onEdt(() -> cursor.moved(20, 20, false));

        verify(canvas).setCursor(systemCursor(SWT.CURSOR_ARROW));
    }

    @Test
    void awtCursorsMapToTheSwtOnesTheRenderSideDraws() {
        assertEquals(SWT.CURSOR_ARROW, EmbedCursor.swtCursor(Cursor.getDefaultCursor()));
        assertEquals(SWT.CURSOR_IBEAM, EmbedCursor.swtCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR)));
        assertEquals(SWT.CURSOR_WAIT, EmbedCursor.swtCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR)));
        assertEquals(SWT.CURSOR_SIZEWE, EmbedCursor.swtCursor(Cursor.getPredefinedCursor(Cursor.E_RESIZE_CURSOR)));
        assertEquals(SWT.CURSOR_SIZENS, EmbedCursor.swtCursor(Cursor.getPredefinedCursor(Cursor.S_RESIZE_CURSOR)));
        assertEquals(SWT.CURSOR_SIZENESW, EmbedCursor.swtCursor(Cursor.getPredefinedCursor(Cursor.SW_RESIZE_CURSOR)));
        assertEquals(SWT.CURSOR_SIZENWSE, EmbedCursor.swtCursor(Cursor.getPredefinedCursor(Cursor.SE_RESIZE_CURSOR)));
    }
}
