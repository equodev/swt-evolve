package dev.equo.swt.awt;

import org.junit.jupiter.api.Test;

import java.awt.Canvas;
import java.awt.Component;
import java.awt.Container;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * An application can host a heavyweight inside another heavyweight: an OpenGL canvas inside the
 * applet that holds each of its editors. The outer one paints only its lightweight children and
 * routes input only to them, so the embedding has to reach the inner one itself, both to paint it
 * and to deliver presses and drags to it.
 *
 * <p>The component tree is mocked: real heavyweights need a display, and CI runs headless.
 */
class NestedHeavyweightTest {

    private static Component heavyLeaf(int x, int y, int w, int h) {
        Component c = mock(Component.class);
        stub(c, false, x, y, w, h);
        return c;
    }

    private static Container container(boolean lightweight, int x, int y, int w, int h, Component... children) {
        Container c = mock(Container.class);
        stub(c, lightweight, x, y, w, h);
        when(c.getComponents()).thenReturn(children);
        return c;
    }

    private static void stub(Component c, boolean lightweight, int x, int y, int w, int h) {
        when(c.isLightweight()).thenReturn(lightweight);
        when(c.isShowing()).thenReturn(true);
        when(c.isVisible()).thenReturn(true);
        when(c.getX()).thenReturn(x);
        when(c.getY()).thenReturn(y);
        when(c.getBounds()).thenReturn(new Rectangle(x, y, w, h));
    }

    /** root > applet (heavy, at 10,20) > panel (light, at 5,7) > canvas (heavy, at 3,4, 100x50). */
    private final Component canvas = heavyLeaf(3, 4, 100, 50);
    private final Container panel = container(true, 5, 7, 200, 100, canvas);
    private final Container applet = container(false, 10, 20, 300, 200, panel);
    private final Container root = container(true, 0, 0, 400, 300, applet);

    @Test
    void paintingReachesTheHeavyweightInsideAnotherOneAfterItsAncestor() {
        List<Component> visited = new ArrayList<>();
        List<int[]> offsets = new ArrayList<>();

        EvolveSwingHost.visitHeavyweights(root, 0, 0, (heavy, x, y) -> {
            visited.add(heavy);
            offsets.add(new int[] {x, y});
        });

        assertEquals(List.of(applet, canvas), visited);
        assertArrayEquals(new int[] {10, 20}, offsets.get(0));
        assertArrayEquals(new int[] {10 + 5 + 3, 20 + 7 + 4}, offsets.get(1));
    }

    @Test
    void aPressOnTheInnerHeavyweightIsRoutedToIt() {
        int[] offset = new int[2];

        Component hit = AwtInput.findHeavyweightAt(root, 18 + 50, 31 + 25, offset);

        assertSame(canvas, hit);
        assertArrayEquals(new int[] {18, 31}, offset);
    }

    @Test
    void aPressBesideTheInnerHeavyweightStillGoesToTheOuterOne() {
        int[] offset = new int[2];

        Component hit = AwtInput.findHeavyweightAt(root, 10 + 250, 20 + 150, offset);

        assertSame(applet, hit);
        assertArrayEquals(new int[] {10, 20}, offset);
    }

    /** Stands in for an OpenGL canvas with an off-screen rendering mode. */
    public static class OffscreenCapableCanvas extends Canvas {
        private boolean offscreen;

        public boolean isOffScreenBuffer() {
            return offscreen;
        }

        public void setOffScreenBuffer(boolean on) {
            offscreen = on;
        }
    }

    @Test
    void offscreenModeIsSwitchedOnAndBackOnWhenTheApplicationTurnsItOff() {
        OffscreenCapableCanvas gl = new OffscreenCapableCanvas();

        EvolveSwingHost.enableOffscreenRendering(gl);
        assertTrue(gl.isOffScreenBuffer());

        gl.setOffScreenBuffer(false);
        EvolveSwingHost.enableOffscreenRendering(gl);
        assertTrue(gl.isOffScreenBuffer());
    }

    @Test
    void aHeavyweightWithoutAnOffscreenModeIsLeftAlone() {
        assertDoesNotThrow(() -> EvolveSwingHost.enableOffscreenRendering(new Canvas()));
    }
}
