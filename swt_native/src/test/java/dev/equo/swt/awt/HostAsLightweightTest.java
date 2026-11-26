package dev.equo.swt.awt;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.Container;
import java.awt.Panel;

import org.junit.jupiter.api.Test;

/**
 * Which heavyweight wrappers of embedded Swing content become part of the frame's lightweight tree.
 * Applications put their Swing content in a plain AWT {@code Panel} before adding it to the frame
 * {@code SWT_AWT} returns; left heavyweight, its native surface answers no graphics, and a Swing
 * text editor that measures its font with {@code getGraphics()} when it is added fails.
 */
@SuppressWarnings("removal")
class HostAsLightweightTest {

    @Test
    void aPlainAwtPanelBecomesLightweight() {
        Panel wrapper = new Panel();

        EvolveSwingHost.hostAsLightweight(wrapper);

        assertTrue(wrapper.isLightweight(), "a plain Panel wrapping Swing content kept its native surface");
    }

    @Test
    void aPanelSubclassKeepsItsNativeSurface() {
        Panel ownDrawing = new Panel() {
        };

        EvolveSwingHost.hostAsLightweight(ownDrawing);

        assertFalse(ownDrawing.isLightweight(), "a Panel subclass may draw on its native surface itself");
    }

    /**
     * The frame notifies a child it is given before the host relocates it, so the wrapper has to be
     * lightweight by the time the add notifies it, not only once it is relocated.
     */
    @Test
    void theFramesLayoutMakesAPanelAddedToItLightweightBeforeItIsNotified() {
        Container frame = new Container();
        frame.setLayout(EvolveSwingHost.hostingLayout(null));
        frame.addNotify();
        frame.validate();
        Panel wrapper = new Panel();

        frame.add(wrapper);

        assertTrue(wrapper.isLightweight(), "the wrapper was notified as a heavyweight");
    }
}
