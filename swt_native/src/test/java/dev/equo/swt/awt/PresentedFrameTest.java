package dev.equo.swt.awt;

import org.junit.jupiter.api.Test;

import java.awt.image.BufferedImage;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * An OpenGL heavyweight's presented frame stands in for what its paint left in the blit, at the
 * blit's size, and only on Linux unless turned off.
 */
class PresentedFrameTest {

    private static final int SCENE = 0xFF336699;
    private static final int MARK = 0xFFFF0000;

    /** A {@code w}x{@code h} frame of {@link #SCENE} with {@link #MARK} at the top-left pixel. */
    private static int[] frame(int w, int h) {
        int[] frame = new int[2 + w * h];
        frame[0] = w;
        frame[1] = h;
        java.util.Arrays.fill(frame, 2, frame.length, SCENE);
        frame[2] = MARK;
        return frame;
    }

    @Test
    void aFrameOfTheBlitsSizeIsShownAsPresented() {
        BufferedImage image = EvolveSwingHost.presentedImage(frame(6, 4), 6, 4);

        assertThat(image.getWidth()).isEqualTo(6);
        assertThat(image.getHeight()).isEqualTo(4);
        assertThat(image.getRGB(0, 0)).isEqualTo(MARK);
        assertThat(image.getRGB(5, 3)).isEqualTo(SCENE);
    }

    @Test
    void aFrameOfAnotherSizeIsStretchedToTheBlit() {
        BufferedImage image = EvolveSwingHost.presentedImage(frame(4, 4), 8, 8);

        assertThat(image.getWidth()).isEqualTo(8);
        assertThat(image.getHeight()).isEqualTo(8);
        assertThat(image.getRGB(0, 0)).isEqualTo(MARK);
        assertThat(image.getRGB(7, 7)).isEqualTo(SCENE);
    }

    @Test
    void noFrameLeavesThePaintAsItWas() {
        assertThat(EvolveSwingHost.presentedImage(null, 6, 4)).isNull();
        assertThat(EvolveSwingHost.presentedImage(new int[] {0, 0}, 6, 4)).isNull();
        assertThat(EvolveSwingHost.presentedImage(new int[] {6, 4, SCENE}, 6, 4)).isNull();
    }

    @Test
    void theHookRunsOnLinuxUnlessTurnedOff() {
        assertThat(GlPresentHook.enabled("Linux", null)).isTrue();
        assertThat(GlPresentHook.enabled("Linux", "false")).isFalse();
        assertThat(GlPresentHook.enabled("Windows 11", null)).isFalse();
        assertThat(GlPresentHook.enabled("Mac OS X", null)).isFalse();
    }
}
