package org.eclipse.swt.custom;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import dev.equo.swt.SerializeTestBase;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.Mocks;
import org.eclipse.swt.widgets.Shell;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/** What a CBanner does with its empty client area, and what it sends Flutter to draw. */
@Tag("native-unit")
public class CBannerImplementationNativeTest extends SerializeTestBase {

    private static Composite fixed(Composite parent, int width, int height) {
        Composite child = new Composite(parent, SWT.NONE);
        child.setLayout(new Layout() {
            @Override
            protected Point computeSize(Composite composite, int wHint, int hHint, boolean flushCache) {
                return new Point(wHint == SWT.DEFAULT ? width : wHint, hHint == SWT.DEFAULT ? height : hHint);
            }

            @Override
            protected void layout(Composite composite, boolean flushCache) {
            }
        });
        return child;
    }

    /** The client area is empty, as SWT's is, and the layout still spreads the children over the banner. */
    @Test
    void theClientAreaIsEmptyAndTheChildrenAreLaidOutAllTheSame() {
        CBanner banner = new CBanner(Mocks.shell(), SWT.NONE);
        banner.setSize(300, 60);
        Composite left = fixed(banner, 100, 20), right = fixed(banner, 80, 20), bottom = fixed(banner, 150, 10);
        banner.setLeft(left);
        banner.setRight(right);
        banner.setBottom(bottom);
        assertThat(banner.getClientArea()).isEqualTo(new Rectangle(0, 0, 0, 0));
        assertThat(left.getBounds()).isEqualTo(new Rectangle(0, 4, 211, 20));
        assertThat(right.getBounds()).isEqualTo(new Rectangle(220, 4, 80, 20));
        assertThat(bottom.getBounds()).isEqualTo(new Rectangle(0, 50, 300, 10));
        banner.setSize(400, 80);
        assertThat(right.getBounds()).isEqualTo(new Rectangle(320, 4, 80, 20));
        banner.dispose();
        assertThat(banner.getClientArea()).isEqualTo(new Rectangle(0, 0, 0, 0));
    }

    /** Like every Composite here, a banner with no size waits for one before laying its children out. */
    @Test
    void aBannerWithoutASizeWaitsForOneBeforeLayingOut() {
        CBanner banner = new CBanner(Mocks.shell(), SWT.NONE);
        Composite left = fixed(banner, 100, 20);
        banner.setLeft(left);
        assertThat(left.getBounds()).isEqualTo(new Rectangle(0, 0, 0, 0));
        banner.setSize(0, 60);
        assertThat(left.getBounds()).isEqualTo(new Rectangle(0, 0, 0, 20));
        banner.setSize(300, 60);
        assertThat(left.getBounds()).isEqualTo(new Rectangle(0, 0, 300, 20));
    }

    /** A shown banner is laid out whatever its size, as every Composite. */
    @Test
    void aShownBannerIsLaidOutWithoutASize() {
        Shell shell = Mocks.shell();
        when(shell.isVisible()).thenReturn(true);
        CBanner banner = new CBanner(shell, SWT.NONE);
        Composite left = fixed(banner, 100, 20);
        banner.setLeft(left);
        assertThat(left.getBounds()).isEqualTo(new Rectangle(0, 0, 0, 20));
    }

    /** What Flutter is sent to draw the curve and the bottom line, named again after each layout. */
    @Test
    void theValueCarriesWhatTheBannerDraws() {
        CBanner banner = new CBanner(Mocks.shell(), SWT.NONE);
        DartCBanner impl = (DartCBanner) banner.getImpl();
        VCBanner value = impl.getValue();
        banner.setSize(300, 60);
        Composite left = fixed(banner, 100, 20), right = fixed(banner, 80, 20), bottom = fixed(banner, 150, 10);
        assertThat(value.getBottomLine()).as("no line without a bottom").isEqualTo(-1);
        banner.setBottom(bottom);
        assertThat(value.getBottomLine()).as("no line without a left or a right").isEqualTo(-1);
        banner.setLeft(left);
        assertThat(value.getCurveLine()).as("no curve without a right").isNull();
        assertThat(value.getBottomLine()).isEqualTo(48);

        value.clearDirty();
        banner.setRight(right);
        // The curve follows the height on Resize, which this display does not deliver.
        impl.onResize();
        assertThat(value.changedKeys()).contains(VCBanner.CURVE_LINE, VCBanner.BOTTOM_LINE);
        assertThat(value.getCurveLine()).containsExactly(214, 59, 213, 59, 214, 59, 215, 58, 216, 57, 216, 2, 217, 1, 218, 0, 218, 0, 300, 0);

        banner.setSimple(false);
        int[] curve = value.getCurveLine();
        assertThat(curve).hasSize(2 * 51 + 6);
        assertThat(new int[] {curve[0], curve[1], curve[curve.length - 4], curve[curve.length - 3], curve[curve.length - 2], curve[curve.length - 1]})
                .containsExactly(176, 59, 225, 0, 300, 0);

        banner.setLeft(null);
        assertThat(value.getCurveLine()).isNull();
        assertThat(value.getBottomLine()).as("a right alone keeps the line").isEqualTo(48);
        bottom.dispose();
        assertThat(value.getBottomLine()).as("a disposed bottom has no bounds to draw above").isEqualTo(-1);
    }
}
