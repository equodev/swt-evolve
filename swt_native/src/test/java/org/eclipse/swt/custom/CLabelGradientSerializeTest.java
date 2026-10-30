package org.eclipse.swt.custom;

import static dev.equo.swt.harness.WireAssert.assertThatWire;
import static org.assertj.core.api.Assertions.assertThat;
import static org.eclipse.swt.widgets.Mocks.device;
import static org.mockito.Mockito.when;
import static org.eclipse.swt.widgets.Mocks.shell;

import dev.equo.swt.SerializeTestBase;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Mocks;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.List;

/**
 * The gradient and the image a CLabel paints behind its text are the label's own state, not the
 * Control's background: they have to reach the render side, and every call that sets or clears
 * one has to say so, or the label keeps painting what it painted before.
 */
@ExtendWith(Mocks.class)
public class CLabelGradientSerializeTest extends SerializeTestBase {

    // The wire names, spelled out: the test is about what is sent under them.
    private static final int GRADIENT_COLORS = VCLabel.GRADIENT_COLORS;
    private static final int GRADIENT_PERCENTS = VCLabel.GRADIENT_PERCENTS;
    private static final int GRADIENT_VERTICAL = VCLabel.GRADIENT_VERTICAL;

    private static final Color RED = new Color(device(), 255, 0, 0);
    private static final Color BLUE = new Color(device(), 0, 0, 255);

    /**
     * A gradient on a display of fewer than 15 bits per pixel collapses to its last colour, and the
     * mock display answers 0 where the real one answers 24.
     */
    @BeforeEach
    void aColourDisplay() {
        when(device().getDepth()).thenReturn(24);
    }

    @AfterEach
    void backToTheMocksDefault() {
        when(device().getDepth()).thenReturn(0);
    }

    /** A label sent whole once, so that what changes next is all that is outstanding. */
    private CLabel sentLabel() {
        CLabel label = new CLabel(shell(), SWT.NONE);
        serialize(label);
        value(label).clearDirty();
        return label;
    }

    /** An opaque colour as the wire dump writes it. */
    private static String rgb(int r, int g, int b) {
        return String.format("#ff%02x%02x%02x", r, g, b);
    }

    private static VCLabel value(CLabel label) {
        return ((DartCLabel) label.getImpl()).getValue();
    }

    @Test
    void aGradientIsSerializedWithItsPercentsAndDirection() {
        CLabel label = sentLabel();
        label.setBackground(new Color[] { RED, BLUE }, new int[] { 60 }, true);

        assertThat(value(label).changedKeys()).contains(GRADIENT_COLORS, GRADIENT_PERCENTS, GRADIENT_VERTICAL)
                .as("a label with no image has none to clear").doesNotContain(VCLabel.BACKGROUND_IMAGE);

        assertThatWire(serialize(label)).isObject()
                .node("gradientColors").hasSize(2);
        assertThatWire(serialize(label)).node("gradientColors[0]").isEqualTo(rgb(255, 0, 0));
        assertThatWire(serialize(label)).node("gradientColors[1]").isEqualTo(rgb(0, 0, 255));
        assertThatWire(serialize(label)).node("gradientPercents").isEqualTo(List.of(60));
        assertThatWire(serialize(label)).node("gradientVertical").isEqualTo(true);
    }

    @Test
    void aHorizontalGradientSaysSoWhenItReplacesAVerticalOne() {
        CLabel label = sentLabel();
        label.setBackground(new Color[] { RED, BLUE }, new int[] { 100 }, true);
        value(label).clearDirty();

        label.setBackground(new Color[] { RED, BLUE }, new int[] { 100 }, false);

        assertThat(value(label).changedKeys()).contains(GRADIENT_VERTICAL);
        assertThat(value(label).getGradientVertical()).isFalse();
    }

    /** SWT stores the label's background where the application passed null, so the colour is on the wire. */
    @Test
    void aNullColourTravelsAsTheLabelsBackground() {
        CLabel label = sentLabel();
        Color background = label.getBackground();
        label.setBackground(new Color[] { RED, null, BLUE }, new int[] { 30, 100 }, false);

        assertThatWire(serialize(label)).node("gradientColors").hasSize(3);
        assertThatWire(serialize(label)).node("gradientColors[1]")
                .isEqualTo(rgb(background.getRed(), background.getGreen(), background.getBlue()));
        assertThatWire(serialize(label)).node("gradientPercents").isEqualTo(List.of(30, 100));
    }

    @Test
    void clearingTheGradientIsSentToo() {
        CLabel label = sentLabel();
        label.setBackground(new Color[] { RED, BLUE }, new int[] { 100 }, false);
        serialize(label);
        value(label).clearDirty();

        label.setBackground((Color[]) null, null);

        assertThat(value(label).changedKeys()).contains(GRADIENT_COLORS, GRADIENT_PERCENTS);
        assertThatWire(serialize(label)).node("gradientColors").isAbsent();
        assertThatWire(serialize(label)).node("gradientPercents").isAbsent();
    }

    @Test
    void aPlainColourClearsTheGradientOnTheRenderSideToo() {
        CLabel label = sentLabel();
        label.setBackground(new Color[] { RED, BLUE }, new int[] { 100 }, false);
        serialize(label);
        value(label).clearDirty();

        label.setBackground(BLUE);

        assertThat(value(label).changedKeys()).contains(GRADIENT_COLORS, GRADIENT_PERCENTS);
        assertThat(value(label).getGradientColors()).isNull();
    }

    @Test
    void anImageReplacingAGradientClearsIt() {
        CLabel label = sentLabel();
        label.setBackground(new Color[] { RED, BLUE }, new int[] { 100 }, false);
        serialize(label);
        value(label).clearDirty();

        label.setBackground(new Image(device(), 16, 16));

        assertThat(value(label).changedKeys()).contains(VCLabel.BACKGROUND_IMAGE, GRADIENT_COLORS, GRADIENT_PERCENTS);
        assertThat(value(label).getGradientColors()).isNull();
    }

    @Test
    void aGradientReplacingAnImageClearsIt() {
        CLabel label = sentLabel();
        label.setBackground(new Image(device(), 16, 16));
        serialize(label);
        value(label).clearDirty();

        label.setBackground(new Color[] { RED, BLUE }, new int[] { 100 }, false);

        assertThat(value(label).changedKeys()).contains(VCLabel.BACKGROUND_IMAGE, GRADIENT_COLORS);
        assertThat(value(label).getBackgroundImage()).isNull();
    }

    @Test
    void aPlainColourOnALabelWithNeitherNamesNeither() {
        CLabel label = sentLabel();

        label.setBackground(RED);

        assertThat(value(label).changedKeys()).doesNotContain(VCLabel.BACKGROUND_IMAGE, GRADIENT_COLORS, GRADIENT_PERCENTS);
    }

    @Test
    void aPlainColourReplacingAnImageClearsIt() {
        CLabel label = sentLabel();
        label.setBackground(new Image(device(), 16, 16));
        serialize(label);
        value(label).clearDirty();

        label.setBackground(RED);

        assertThat(value(label).changedKeys()).contains(VCLabel.BACKGROUND_IMAGE);
        assertThat(value(label).getBackgroundImage()).isNull();
    }
}
